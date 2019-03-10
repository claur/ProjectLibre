/*******************************************************************************
 * The contents of this file are subject to the Common Public Attribution License 
 * Version 1.0 (the "License"); you may not use this file except in compliance with 
 * the License. You may obtain a copy of the License at 
 * http://www.projectlibre.com/license . The License is based on the Mozilla Public 
 * License Version 1.1 but Sections 14 and 15 have been added to cover use of 
 * software over a computer network and provide for limited attribution for the 
 * Original Developer. In addition, Exhibit A has been modified to be consistent 
 * with Exhibit B. 
 *
 * Software distributed under the License is distributed on an "AS IS" basis, 
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the 
 * specific language governing rights and limitations under the License. The 
 * Original Code is ProjectLibre. The Original Developer is the Initial Developer 
 * and is ProjectLibre Inc. All portions of the code written by ProjectLibre are 
 * Copyright (c) 2012-2019. All Rights Reserved. All portions of the code written by 
 * ProjectLibre are Copyright (c) 2012-2019. All Rights Reserved. Contributor 
 * ProjectLibre, Inc.
 *
 * Alternatively, the contents of this file may be used under the terms of the 
 * ProjectLibre End-User License Agreement (the ProjectLibre License) in which case 
 * the provisions of the ProjectLibre License are applicable instead of those above. 
 * If you wish to allow use of your version of this file only under the terms of the 
 * ProjectLibre License and not to allow others to use your version of this file 
 * under the CPAL, indicate your decision by deleting the provisions above and 
 * replace them with the notice and other provisions required by the ProjectLibre 
 * License. If you do not delete the provisions above, a recipient may use your 
 * version of this file under either the CPAL or the ProjectLibre Licenses. 
 *
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text of the notices 
 * in the Source Code files of the Original Code. You should use the text of this 
 * Exhibit A rather than the text found in the Original Code Source Code for Your 
 * Modifications.] 
 *
 * EXHIBIT B. Attribution Information for ProjectLibre required
 *
 * Attribution Copyright Notice: Copyright (c) 2012-2019, ProjectLibre, Inc.
 * Attribution Phrase (not exceeding 10 words): 
 * ProjectLibre, open source project management software.
 * Attribution URL: http://www.projectlibre.com
 * Graphic Image as provided in the Covered Code as file: projectlibre-logo.png with 
 * alternatives listed on http://www.projectlibre.com/logo 
 *
 * Display of Attribution Information is required in Larger Works which are defined 
 * in the CPAL as a work which combines Covered Code or portions thereof with code 
 * not governed by the terms of the CPAL. However, in addition to the other notice 
 * obligations, all copies of the Covered Code in Executable and Source Code form 
 * distributed must, as a form of attribution of the original author, include on 
 * each user interface screen the "ProjectLibre" logo visible to all users. 
 * The ProjectLibre logo should be located horizontally aligned with the menu bar 
 * and left justified on the top left of the screen adjacent to the File menu. The 
 * logo must be at least 144 x 31 pixels. When users click on the "ProjectLibre" 
 * logo it must direct them back to http://www.projectlibre.com. 
 *******************************************************************************/
package com.projectlibre1.pm.graphic.spreadsheet.editor;

import java.awt.Component;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.NumberEditor;

import com.projectlibre1.configuration.Settings;
import com.projectlibre1.datatype.PercentFormat;
import com.projectlibre1.datatype.Rate;
import com.projectlibre1.field.Field;
import com.projectlibre1.field.Range;
import com.projectlibre1.util.MathUtils;

/**
 * A spinner in a spreadsheet or dialog
 */
public class SpinEditor extends SimpleEditor {
	private Double defaultValue = new Double(1.0D);
	private static double MAX_VALUE = 60000000.0; 
	private static final String NUMBER_TEMPLATE="#######################";
	Field field;
	KeyboardFocusSpinner spin;

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean arg2, int arg3, int arg4) {
		
		cachedTable = table;
		if (value == null || (field.isPercent() && value instanceof Double && PercentFormat.isSpecialValue((Double)value))) {
			spin.setValue(defaultValue);
		} else {
			if (value instanceof Rate)
				value = new Double(((Rate)value).getValue());
			spin.setValue(value);
		}
		return spin;
	}

/** A static function to get a JSpinner.  This may be used outside a spreadsheet
 * 
 * @param field
 * @param value
 * @return
 */
	public static JSpinner getJSpinnerInstance(Field field, double value, boolean inSpreadSheet) {
		double min = 0;
		double max = MAX_VALUE;
		double step = 0.5;
		double spinnerValue = value;
		Range range = field.getRange();
		if (range != null) {
			max = range.getMaximum();
			min = range.getMinimum();
			step = range.getStep();
			spinnerValue = Math.max(min,Math.min(value,max)); // put in range
		}
		
		
		// Set focus to editor always
		//TODO first key is currently swallowed incorrectly (same pb with date edits). This is bad
		JSpinner spinner;
		if (inSpreadSheet)
			spinner = new KeyboardFocusSpinner(new SpinnerNumberModel(spinnerValue,min,max,step));
		else
			spinner = new JSpinner(new SpinnerNumberModel(spinnerValue,min,max,step));
		
		String template = NUMBER_TEMPLATE.substring(Double.toString(max).length()); // enough space to hold biggest
		JSpinner.NumberEditor editor;
		if (field.isPercent())
			editor = new JSpinner.NumberEditor(spinner,template + Settings.PERCENT);
		else
			editor = new JSpinner.NumberEditor(spinner,template);
		spinner.setEditor(editor);
		editor.getTextField().setHorizontalAlignment(JTextField.RIGHT);
		return spinner;
	}
	
	public static Object getValue(JSpinner spinner, Field field) {
		JSpinner.NumberEditor editor = (NumberEditor) spinner.getEditor();
		Object value = null;
		try {
			if (field.isPercent()) {
				value = PercentFormat.getInstance().parseObject(editor.getTextField().getText());
//	JSpinner screws up and sometimes adds a small fraction to the value.  Round it to get rid of it.  Example, 15% shows up with a miniscule .000000000000000002 at the end
				value = new Double(MathUtils.roundToDecentPrecision(((Number) value).doubleValue()));
			}
			else
				value = NumberFormat.getInstance().parseObject(editor.getTextField().getText());
		} catch (ParseException e) {
			return null;
		}
		if (field.isRate())
			value = new Rate(((Number)value).doubleValue());
		return value;
	}

	public SpinEditor(Field field) {
		super();
		this.field = field;
		spin = (KeyboardFocusSpinner) getJSpinnerInstance(field,0.0,true);
	}

	/**
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue() {
		return getValue(spin,field);
	}
	
	public void cancelCellEditing() {
		super.cancelCellEditing();
	}

	public boolean stopCellEditing() {
		fireEditingStopped(); 
		if (handledPostErrorFocus()) {
			spin.getTextField().setValue(spin.getValue()); // put back old value
			return false;
		}
		return true;
	};
	
	
}
