/*
The contents of this file are subject to the Common Public Attribution License
Version 1.0 (the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at
http://www.projity.com/license . The License is based on the Mozilla Public
License Version 1.1 but Sections 14 and 15 have been added to cover use of
software over a computer network and provide for limited attribution for the
Original Developer. In addition, Exhibit A has been modified to be consistent
with Exhibit B.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
specific language governing rights and limitations under the License. The
Original Code is OpenProj. The Original Developer is the Initial Developer and
is Projity, Inc. All portions of the code written by Projity are Copyright (c)
2006, 2007. All Rights Reserved. Contributors Projity, Inc.

Alternatively, the contents of this file may be used under the terms of the
Projity End-User License Agreeement (the Projity License), in which case the
provisions of the Projity License are applicable instead of those above. If you
wish to allow use of your version of this file only under the terms of the
Projity License and not to allow others to use your version of this file under
the CPAL, indicate your decision by deleting the provisions above and replace
them with the notice and other provisions required by the Projity  License. If
you do not delete the provisions above, a recipient may use your version of this
file under either the CPAL or the Projity License.

[NOTE: The text of this license may differ slightly from the text of the notices
in Exhibits A and B of the license at http://www.projity.com/license. You should
use the latest text at http://www.projity.com/license for your modifications.
You may not remove this license text from the source files.]

Attribution Information: Attribution Copyright Notice: Copyright � 2006, 2007
Projity, Inc. Attribution Phrase (not exceeding 10 words): Powered by OpenProj,
an open source solution from Projity. Attribution URL: http://www.projity.com
Graphic Image as provided in the Covered Code as file:  openproj_logo.png with
alternatives listed on http://www.projity.com/logo

Display of Attribution Information is required in Larger Works which are defined
in the CPAL as a work which combines Covered Code or portions thereof with code
not governed by the terms of the CPAL. However, in addition to the other notice
obligations, all copies of the Covered Code in Executable and Source Code form
distributed must, as a form of attribution of the original author, include on
each user interface screen the "OpenProj" logo visible to all users.  The
OpenProj logo should be located horizontally aligned with the menu bar and left
justified on the top left of the screen adjacent to the File menu.  The logo
must be at least 100 x 25 pixels.  When users click on the "OpenProj" logo it
must direct them back to http://www.projity.com.
*/
package com.projity.pm.graphic.spreadsheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import com.projity.configuration.Configuration;
import com.projity.field.Field;
import com.projity.graphic.configuration.SpreadSheetFieldArray;
import com.projity.pm.graphic.spreadsheet.editor.MoneyEditor;
import com.projity.pm.graphic.spreadsheet.editor.RateEditor;
import com.projity.pm.graphic.spreadsheet.editor.SimpleComboBoxEditor;
import com.projity.pm.graphic.spreadsheet.editor.SimpleEditor;
import com.projity.pm.graphic.spreadsheet.editor.SpinEditor;
import com.projity.pm.graphic.spreadsheet.editor.SpreadSheetCellEditorAdapter;
import com.projity.pm.graphic.spreadsheet.renderer.DateRenderer;
import com.projity.pm.graphic.spreadsheet.renderer.IndicatorsRenderer;
import com.projity.pm.graphic.spreadsheet.renderer.LookupRenderer;
import com.projity.pm.graphic.spreadsheet.renderer.OfflineCapableBooleanRenderer;
import com.projity.pm.graphic.spreadsheet.renderer.PercentRenderer;
import com.projity.pm.graphic.spreadsheet.renderer.RateRenderer;
import com.projity.pm.graphic.spreadsheet.renderer.SimpleRenderer;
import com.projity.pm.graphic.spreadsheet.renderer.SpreadSheetCellRendererAdapter;
import com.projity.pm.graphic.spreadsheet.renderer.SpreadSheetColumnHeaderRenderer;
import com.projity.pm.graphic.spreadsheet.renderer.SpreadSheetNameCellRenderer;

/**
 *
 */
public class SpreadSheetColumnModel extends DefaultTableColumnModel {
	int columnIndex = 0;

	int colWidth = 0;

	private ArrayList fieldArray; //changes when columns are moved - needed to update the current definition
	private ArrayList originalFieldArray; // will not change
	private Map<String,Integer> colWidthMap;

	boolean svg;
	/**
	 * @param fieldArray
	 *            TODO
	 *
	 */
	public SpreadSheetColumnModel(final ArrayList fieldArray) {
		this(fieldArray,null);

	}
	public SpreadSheetColumnModel(ArrayList fieldArray,List<Integer> colWidthList) {
		super();
		setFieldArray(fieldArray);
		colWidthMap=new HashMap<String,Integer>();
		if (fieldArray instanceof SpreadSheetFieldArray){
			SpreadSheetFieldArray sa=(SpreadSheetFieldArray)fieldArray;
			if (colWidthList==null&&sa!=null&&sa.getWidths()!=null&&sa.getWidths().size()>0){
				colWidthList=sa.getWidths();
			}
			if (colWidthList==null) return;
			Iterator<Field> a=(Iterator<Field>)sa.iterator();
			Iterator<Integer> s=colWidthList.iterator();
			while (a.hasNext()&&s.hasNext()){
				String f=a.next().getId();
				int size=s.next();
				if (!colWidthMap.containsKey(f)) colWidthMap.put(f, size);
			}
		}
	}

	public void addColumn(TableColumn tc) {
		tc.setHeaderRenderer(new SpreadSheetColumnHeaderRenderer());

		if (columnIndex == 0) {
			Field field = (Field) originalFieldArray.get(columnIndex);
			tc.setIdentifier(field); // store the field with the column
			// tc.setIdentifier(null); // store the field with the column
			tc.setPreferredWidth(0);

			colWidth = 0;
			// nothing
		} else {
			super.addColumn(tc);
			Field field = (Field) originalFieldArray.get(columnIndex);
			tc.setIdentifier(field); // store the field with the column
//			System.out.println("setting column " + columnIndex + " to field " + field + " ok = " + (field == getFieldInColumn(columnIndex)));

			if (field.isNameField()) {
				tc.setPreferredWidth((svg)?170:150);
				tc.setCellRenderer(new SpreadSheetNameCellRenderer());
//				tc.setCellEditor(new SpreadSheetNameCellEditor(new SimpleEditor(String.class)));
				tc.setCellEditor(new SpreadSheetCellEditorAdapter(new SimpleEditor(String.class)));
			} else if (field == Configuration.getFieldFromId("Field.indicators")) {
				tc.setPreferredWidth(50);
				tc.setCellRenderer(new SpreadSheetCellRendererAdapter(new IndicatorsRenderer()));
				tc.setHeaderRenderer(new SpreadSheetColumnHeaderRenderer(IndicatorsRenderer.getCellHeader()));
			} else if (field.getLookupTypes() != null) {
				tc.setCellRenderer(new SpreadSheetCellRendererAdapter(new LookupRenderer()));
			} else {
				tc.setPreferredWidth(150);
				if (field.hasOptions()) {
					tc.setPreferredWidth(150);
					tc.setCellRenderer(new SpreadSheetCellRendererAdapter(new SimpleRenderer()));
					// note that in Spreadsheet, there getCellEditor() is
					// overridden and dynamic combos are filled there
					tc.setCellEditor(new SpreadSheetCellEditorAdapter(new SimpleComboBoxEditor(new DefaultComboBoxModel(field.getOptions(null)))));
				} else if (field.getRange() != null) {
					if (field.isPercent())
						tc.setCellRenderer(new SpreadSheetCellRendererAdapter(new PercentRenderer()));
					else
						tc.setCellRenderer(new SpreadSheetCellRendererAdapter(new SimpleRenderer()));
					tc.setCellEditor(new SpreadSheetCellEditorAdapter(new SpinEditor(field)));
				} else if (field.isRate()) {
					tc.setCellRenderer(new SpreadSheetCellRendererAdapter(new RateRenderer()));
					tc.setCellEditor(new SpreadSheetCellEditorAdapter(new RateEditor(null, field.isMoney(),field.isPercent(),true)));
				} else if (field.isMoney()) {
					tc.setCellRenderer(new SpreadSheetCellRendererAdapter(new SimpleRenderer()));
					tc.setCellEditor(new SpreadSheetCellEditorAdapter(new MoneyEditor()));
				} else if (field.isPercent()) {
					tc.setCellRenderer(new SpreadSheetCellRendererAdapter(new PercentRenderer()));
				} else if (field.isDate()) {
					tc.setCellRenderer(new SpreadSheetCellRendererAdapter(new DateRenderer()));
				} else if (field.isBoolean()){
					tc.setCellRenderer(new SpreadSheetCellRendererAdapter(new OfflineCapableBooleanRenderer()));
				} else {
					//SimpleRenderer in other cases, LC 8/2006
					tc.setCellRenderer(new SpreadSheetCellRendererAdapter(new SimpleRenderer()));
					tc.setPreferredWidth(field.getColumnWidth(svg));
				}
			}
			Integer size=colWidthMap.get(field.getId());
			if (size==null||size<=0) colWidthMap.put(field.getId(),tc.getPreferredWidth());
			else tc.setPreferredWidth(size);
			colWidth += tc.getPreferredWidth();
		}
		columnIndex++;
	}

	public void removeColumn(TableColumn column) {
		columnIndex--;
		super.removeColumn(column);
		if (columnIndex == 1)
			columnIndex = 0;
	}

	/***************************************************************************
	 * @see javax.swing.table.TableColumnModel#moveColumn(int, int)
	 */
	public void moveColumn(int columnIndex, int newIndex) {
		if (newIndex != -1)
			super.moveColumn(columnIndex, newIndex);

		if (columnIndex == newIndex)
			return;
		SpreadSheetFieldArray f = (SpreadSheetFieldArray) getFieldArray();
		fieldArray = f.move(columnIndex+1, newIndex+1);
	}

	public ArrayList getFieldArray() {
		return fieldArray;
	}

	public void setFieldArray(ArrayList fieldArray) {
		this.fieldArray = fieldArray;
		originalFieldArray = (ArrayList) fieldArray.clone();
	}

	public int getColWidth() {
		return colWidth;
	}

	public boolean isSvg() {
		return svg;
	}

	public void setSvg(boolean svg) {
		this.svg = svg;
	}

/**
 * Normally, JTable automatically translates columns to take care of any columns that may have been moved
 * However, sometimes, such as when a column is determines from a mouse event, the column is not translated.
 * @param col
 * @return
 */
	public Field getFieldInNonTranslatedColumn(int col) {
		return (Field)fieldArray.get(col);
	}

	public Field getFieldInColumn(int col) {
//		return (Field)fieldArray.get(col);
		return (Field) originalFieldArray.get(col);

//		if (col >= getColumnCount()) // on initializing
//			return (Field) fieldArray.get(col);
//		if (col == 0) // the 0th column isn't displayed and isn't in the table, but calls are made to it
//			return (Field) fieldArray.get(col);
//		return (Field) getColumn(col - 1).getIdentifier();
	}

	public int findFieldColumn(Field field) {
		return originalFieldArray.indexOf(field);
//		Enumeration i = getColumns();
//		int count = 0;
//		while (i.hasMoreElements()) {
//			count++;
//			TableColumn col = (TableColumn) i.nextElement();
//			if (col.getIdentifier() == field)
//				return count;
//		}
//		if (field == fieldArray.get(0)) // in case hidden 0th column
//			return 0;
//		return -1;
	}

	public int getFieldColumnCount() {
		return getFieldArray().size();
	}

//	@Override
//	protected void fireColumnSelectionChanged(ListSelectionEvent lse) {
//		System.out.println("Model: "+((lse.getValueIsAdjusting())?"lse=":"LSE=")+lse.getFirstIndex()+", "+lse.getLastIndex());
//		super.fireColumnSelectionChanged(lse);
//	}




}