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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.text.Format;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.projectlibre1.pm.graphic.ChangeAwareTextField;
import com.projectlibre1.datatype.Money;
import com.projectlibre1.field.FieldConverter;

/**
 *
 */
public class TimeSimpleEditor extends DefaultCellEditor {
	protected ChangeAwareTextField component;
	protected Class clazz;
	protected Format useFormat = null;
	/**
	 * 
	 */
	public TimeSimpleEditor() {
		super(new ChangeAwareTextField());
		component = (ChangeAwareTextField) getComponent();
		clazz = String.class;
	}
	public TimeSimpleEditor(Class clazz) {
		this();
		this.clazz=clazz;
	}
	public TimeSimpleEditor(Class arg0, Format arg1) {
		this(arg0);		
		useFormat = arg1;
	}
	
	
	
	
	/**
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
	 */
	public Component getTableCellEditorComponent(JTable arg0, Object value,
			boolean arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub
		//component=(JTextField)super.getTableCellEditorComponent(arg0, value, arg2, arg3, arg4);
		String stringValue;
		if(value==null)
			stringValue=null;
//		else if (value instanceof Money) 
//			//this should be handled with an ObjectConverter in the
//			//editor specific context
//			stringValue=value.toString();
		else stringValue=FieldConverter.toString(value);
		component.setText(stringValue);
		//component.resetChange();
		component.setSelectedTextColor(Color.WHITE);
		component.setSelectionColor(Color.BLACK);
		component.setHorizontalAlignment(JTextField.RIGHT);
		return component;
	}
	
	/**
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public void focusGained(FocusEvent arg0) {
	}
	/**
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public void focusLost(FocusEvent arg0) {
	}
	
	
	public Object getCellEditorValue() {
		Object value;
		if (Money.class.equals(clazz)){
			try {
				//this should be handled with an ObjectConverter in the
				//editor specific context
				value=new Money(component.getText());
			} catch (NumberFormatException e) {
				value=null; //to force an error popup
			}
		}else value=FieldConverter.fromString(component.getText(),clazz);
		return value;
	}
	
	
	
	
	public boolean stopCellEditing() {
		if (component.hasChanged())
			return super.stopCellEditing();
		else{
			cancelCellEditing();
			return true;
		}
	}
	
	
	
	
	
	public void cancelCellEditing() {
		super.cancelCellEditing();
	}
}
