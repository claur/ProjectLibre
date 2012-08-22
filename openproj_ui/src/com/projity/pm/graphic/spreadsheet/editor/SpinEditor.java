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

Attribution Information: Attribution Copyright Notice: Copyright © 2006, 2007 
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
package com.projity.pm.graphic.spreadsheet.editor;

import java.awt.Component;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.NumberEditor;

import com.projity.configuration.Settings;
import com.projity.datatype.PercentFormat;
import com.projity.datatype.Rate;
import com.projity.field.Field;
import com.projity.field.Range;
import com.projity.util.MathUtils;

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
