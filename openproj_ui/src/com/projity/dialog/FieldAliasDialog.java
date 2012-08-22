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
package com.projity.dialog;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.configuration.Dictionary;
import com.projity.configuration.NamedItem;
import com.projity.field.Field;
import com.projity.pm.graphic.frames.GraphicManager;
import com.projity.strings.Messages;
import com.projity.util.Alert;

public final class FieldAliasDialog extends AbstractDialog {
	JLabel defaultName;
	JLabel oldName;
	JTextField newName;

	Field field;
	String result = null;
	
	public static boolean doRename(Field field) {
		String value = getValue(field);
		if (value != null)
			field.setAlias(value);
		return true;
	}

	public static String getValue(Field field) {
		FieldAliasDialog dlg = getInstance(field);
		if (dlg.doModal())
			return dlg.getResult();
		return null;
	}

	public static FieldAliasDialog getInstance(Field field) {
		return new FieldAliasDialog(field);
	}
	public final String getResult() {
		return result;
	}
	
	private FieldAliasDialog(Field field) {
		super(GraphicManager.getFrameInstance(), Messages.getString("RenameDialog.Rename"), true); //$NON-NLS-1$
		this.field = field;
		defaultName = new JLabel();
		oldName = new JLabel();
		newName = new JTextField();

	}

	// Component Creation and Initialization **********************************

	/**
	 * Creates, intializes and configures the UI components. Real applications
	 * may further bind the components to underlying models.
	 */
	protected void initControls() {
		bind(true);
	}

	protected boolean bind(boolean get) {
		if (get) {
			defaultName.setText(field.getDefaultName());
			oldName.setText(field.getName());
			newName.setText(field.getName());
		} else {
			result =newName.getText().trim();
			if (result.equals(oldName))
				return true; // no change
			if (result.length() == 0) {
				Alert.warn(Messages.getString("RenameDialog.TheNameCannotBeEmpty"),this); //$NON-NLS-1$
				return false;
			}
		}
		return true;
	}

	// Building *************************************************************

	/**
	 * Builds the panel. Initializes and configures components first, then
	 * creates a FormLayout, configures the layout, creates a builder, sets a
	 * border, and finally adds the components.
	 * 
	 * @return the built panel
	 */

	public JComponent createContentPanel() {
		// Separating the component initialization and configuration
		// from the layout code makes both parts easier to read.
		initControls();
		//TODO set minimum size
		FormLayout layout = new FormLayout("default, 3dlu, 120dlu:grow", // cols //$NON-NLS-1$
				"p, 3dlu,p,3dlu,p,3dlu,p"); // rows //$NON-NLS-1$

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.append(Messages.getString("Field.name"), defaultName); //$NON-NLS-1$
		builder.nextLine(2);
		builder.append(Messages.getString("RenameDialog.CurrentName"), oldName); //$NON-NLS-1$
		builder.nextLine(2);
		builder.append(Messages.getString("RenameDialog.NewName"), newName); //$NON-NLS-1$
		return builder.getPanel();
	}

}
