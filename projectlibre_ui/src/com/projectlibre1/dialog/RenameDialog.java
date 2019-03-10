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
package com.projectlibre1.dialog;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.projectlibre1.pm.graphic.frames.GraphicManager;
import com.projectlibre1.configuration.Dictionary;
import com.projectlibre1.configuration.NamedItem;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.util.Alert;

public final class RenameDialog extends AbstractDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2400103374793798171L;
	JLabel oldName;
	JTextField newName;

	NamedItem namedItem;
	String result = null;
//	private RenameDialog instance = null; //TODO make only once per graphic manager
	
	public static boolean doRename(Component component, NamedItem namedItem) {
		String value = getValue(component,namedItem);
		if (value == null || value.equals(namedItem.getName()))
			return false;
		Dictionary.rename(namedItem,value);
		return true;
	}

	public static String getValue(Component component, NamedItem namedItem) {
		RenameDialog dlg = getInstance(component,namedItem);
		if (dlg.doModal())
			return dlg.getResult();
		return null;
	}

	public static RenameDialog getInstance(Component component, NamedItem namedItem) {
		return new RenameDialog(component,namedItem);

		//		if (instance == null) {
//			instance = new RenameDialog(component,namedItem);
//		} else {
//			instance.namedItem = namedItem;
//			instance.bind(true);
//		}
//		return instance;
			
	}
	public final String getResult() {
		return result;
	}
	
	private RenameDialog(Component component, NamedItem namedItem) {
		super(GraphicManager.getInstance(component).getFrame(), Messages.getString("RenameDialog.Rename"), true); //$NON-NLS-1$
		this.namedItem = namedItem;
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
			oldName.setText(namedItem.getName());
			newName.setText(namedItem.getName());
			setTitle(Messages.getStringWithParam("Text.rename.mf", Dictionary.getCategoryText(namedItem.getCategory()))); //$NON-NLS-1$
		} else {
			result =newName.getText().trim();
			if (result.equals(namedItem.getName()))
				return true; // no change
			if (result.length() == 0) {
				Alert.warn(Messages.getString("RenameDialog.TheNameCannotBeEmpty"),this); //$NON-NLS-1$
				return false;
			}
			if (Dictionary.get(namedItem.getCategory(),result) != null) {
				Alert.warn(Messages.getString("RenameDialog.AnotherItemWithThatNameAlreadyExists"),this); //$NON-NLS-1$
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
				"p, 3dlu,p,3dlu,p"); // rows //$NON-NLS-1$

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.append(Messages.getString("RenameDialog.CurrentName"), oldName); //$NON-NLS-1$
		builder.nextLine(2);
		builder.append(Messages.getString("RenameDialog.NewName"), newName); //$NON-NLS-1$
		return builder.getPanel();
	}

}
