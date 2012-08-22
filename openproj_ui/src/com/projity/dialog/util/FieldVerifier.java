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
package com.projity.dialog.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.undo.UndoableEditSupport;

import com.projity.dialog.FieldDialog;
import com.projity.field.Field;
import com.projity.field.FieldContext;
import com.projity.field.FieldParseException;
import com.projity.field.ObjectRef;
import com.projity.field.Select;
import com.projity.options.EditOption;
import com.projity.undo.FieldEdit;
import com.projity.util.Alert;
import com.projity.util.DateTime;

/**
 *
 */
public class FieldVerifier extends InputVerifier {
	protected FieldContext context = null;
	protected Field field;
	protected ObjectRef objectRef = null;
	protected Object source;
	protected Object value;
	protected Exception exception = null;
	protected boolean updating = false;
	boolean testing = false;
//	private UndoableEditSupport undoableEditSupport;
	/**
	 * @param value TODO
	 * 
	 */
	public FieldVerifier(Field field, ObjectRef objectRef, Object value/*,UndoableEditSupport undoableEditSupport*/) {
		super();
		this.field = field;
		this.objectRef = objectRef;
		setValue(value);
		this.source = this;
//		this.undoableEditSupport=undoableEditSupport;
	}
	
	/**
	 * Get the top level component.  For dates and spinners, the verification is triggered on a grandchild.  We need the grandparent
	 * @param component
	 * @return
	 */
	static JComponent valueHoldingComponent(JComponent component) {
		Object p = component.getParent();
		// for spinners and dates, need to go up to grandparent to get the control which holds the value
		if (p != null && p instanceof LookupField)
			p = ((LookupField)p).getDisplay();
		else if (p != null && p instanceof Component)
			p = ((Component)p).getParent();
		if (p instanceof JSpinner || p instanceof ExtDateField)
			component = (JComponent) p;
		return component;
	}


	
	/* (non-Javadoc)
	 * @see javax.swing.InputVerifier#verify(javax.swing.JComponent)
	 */
	public boolean verify(JComponent component) {
		if (updating) {
			return true;
		}

		FieldDialog parentFieldDialog = ComponentFactory.getParentFieldDialog(component);
		if (parentFieldDialog != null)
			parentFieldDialog.setDirtyComponent(null);
		
		
		JComponent c = component;
		component = valueHoldingComponent(component);
		
		component.setForeground(Color.BLACK);
		c.setForeground(Color.BLACK);
		
		Object newValue = ComponentFactory.getValueFromComponent(component, field);
//System.out.println("new value " + newValue + " " + (newValue != null ?newValue.getClass():""));
		
		// avoid validating unchanged controls
		if (newValue == value || (newValue != null && newValue.equals(value))) { //unchanged
			if (component instanceof JSpinner || component instanceof ExtDateField) { // if a spinner, check for modified text
				String text = ((JTextField)c).getText();
				try {
					if (!(component instanceof ExtDateField) || text.trim().length() > 0)
						newValue = field.getFormat().parseObject(text);
					else {
						((JTextField)c).setText(""); // put in empty text
						newValue = null;
					}
				} catch (ParseException e1) {
					exception = new FieldParseException(field.syntaxErrorForField());
					component.setForeground(Color.RED);
					c.setForeground(Color.RED);
					if (parentFieldDialog != null)
						parentFieldDialog.setDirtyComponent(c);

					return false;
				}
			} else {
				return true;
			}
		}
		if (newValue != null && value != null && newValue.toString().equals(value.toString()))
			return true;
		
		exception = null;
		try {
			if (field.hasOptions())  {
				if (newValue == null)
					newValue = Select.EMPTY;
				
				field.setText(objectRef,newValue.toString(),context);
			} else {
				if (field.isDate()) {
					if (newValue != null && newValue instanceof String) {
						try {
							newValue = EditOption.getInstance().getDateFormat().parseObject((String) newValue);
						} catch (ParseException e) {
						}
					}
					if (newValue == null || newValue.toString().trim().equals("")) // empty text on date is a null date
						newValue = DateTime.getZeroDate();
				}
				if (newValue != value){
					Object oldValue=field.getValue(objectRef, context);
					if (field.isMoney())
						field.setText(objectRef,""+newValue,context);
					else		
						field.setValue(objectRef,source,newValue,context);
					UndoableEditSupport undoableEditSupport=objectRef.getDataFactory().getUndoController().getEditSupport();
					if (undoableEditSupport!=null){
						undoableEditSupport.postEdit(new FieldEdit(field,objectRef,value,oldValue,this,context));
					}
				}
				
			}
		} catch (FieldParseException e) {
			exception = e;
			component.setForeground(Color.RED);
			c.setForeground(Color.RED);
			if (parentFieldDialog != null)
				parentFieldDialog.setDirtyComponent(c);
			return false;
		}
		setValue(newValue); // set to new value for next time
		return true;
	}

	
	/* (non-Javadoc)
	 * @see javax.swing.InputVerifier#shouldYieldFocus(javax.swing.JComponent)
	 */
	public boolean shouldYieldFocus(JComponent arg0) {
		if (testing) // sempaphore to protect infinite focus loop when popping up error dialog.  Does not need to be synchronized since the verifier is not shared
			return true;
		testing = true;
		boolean result = super.shouldYieldFocus(arg0);
		if (result == false)
			Alert.error(exception.getMessage(),arg0);
		testing = false;
		return result;
	}
	/**
	 * @param value The value to set.
	 */
	void setValue(Object value) {
		this.value = value;
	}

	/** A generic listener class that will validate on an event */
	public static class VerifierListener implements ActionListener {
	    public void actionPerformed(ActionEvent e){
	    	JComponent c = (JComponent)e.getSource();
	    	InputVerifier v = c.getInputVerifier();
	    	if (v != null) // on init, it is null
	    		v.verify(c);
	    }
	}
	public final void setUpdating(boolean doNotVerify) {
		this.updating = doNotVerify;
	}
	final Object getValue() {
		return value;
	}

	/**
	 * @return
	 */
	final boolean isUpdating() {
		return updating;
	}
	public final Exception getException() {
		return exception;
	}
}
