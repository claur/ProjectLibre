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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projectlibre1.dialog.util.ComponentFactory;
import com.projectlibre1.dialog.util.FieldComponentMap;
import com.projectlibre1.help.HelpUtil;
import com.projectlibre1.menu.MenuActionConstants;
import com.projectlibre1.pm.graphic.IconManager;
import com.projectlibre1.pm.graphic.frames.GraphicManager;
import com.projectlibre1.configuration.Configuration;
import com.projectlibre1.field.Field;
import com.projectlibre1.strings.Messages;

/**
 *
 */
public abstract class InformationDialog extends FieldDialog {
	private JButton changeWorkingTimeButton = null;
	private JButton assignResourceButton = null;
  	protected InformationDialog(final Frame owner, String title) {
		super(owner, title, false, false);
	}
  	
  	protected JButton getChangeWorkingTimeButton() {
  		if (changeWorkingTimeButton == null) {
			changeWorkingTimeButton= new JButton();
			changeWorkingTimeButton.setToolTipText(Messages.getString("InformationDialog.ChangeWorkingTime")); //$NON-NLS-1$
			ImageIcon icon = IconManager.getIcon("menu.changeWorkingTime"); //$NON-NLS-1$
			changeWorkingTimeButton.setIcon(icon);
			changeWorkingTimeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					//route message to main frame
					GraphicManager.getInstance(changeWorkingTimeButton).getMenuManager().getActionFromId(MenuActionConstants.ACTION_CHANGE_WORKING_TIME).actionPerformed(arg0);
				}});
  		}
  		return changeWorkingTimeButton;
  	}
  	protected JButton getAssignResourceButton() {
  		if (assignResourceButton == null) {
			assignResourceButton= new JButton();
			assignResourceButton.setToolTipText(Messages.getString("InformationDialog.AssignResources")); //$NON-NLS-1$
			ImageIcon icon = IconManager.getIcon("menu24.assignResources"); //$NON-NLS-1$
			assignResourceButton.setIcon(icon);
			assignResourceButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					//route message to main frame
					GraphicManager.getInstance(assignResourceButton).getMenuManager().getActionFromId(MenuActionConstants.ACTION_ASSIGN_RESOURCES).actionPerformed(arg0);
				}});
  		}
  		return assignResourceButton;
  	}

  	protected abstract JComponent createHeaderFieldsPanel(FieldComponentMap map);
	protected  JComponent createNotesPanel(){
		FieldComponentMap map = createMap();
		FormLayout layout = new FormLayout(
		        "p:grow", // extra padding on right is for estimated field //$NON-NLS-1$
				"p, 3dlu,p, 3dlu, fill:50dlu:grow"); //$NON-NLS-1$

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		CellConstraints cc = new CellConstraints();
		builder.setDefaultDialogBorder();
		JComponent header = createHeaderFieldsPanel(map);
		if (header != null)
			builder.add(header,cc.xyw(builder.getColumn(), builder
				.getRow(), 1));
		
		builder.nextLine(2);
		builder.append(map.getLabel("Field.notes") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
		builder.nextLine(2);
		builder.append(map.getComponent("Field.notes", 0)); //$NON-NLS-1$
		return builder.getPanel();
	}

	protected  JComponent pairedComponents(FieldComponentMap map,String fieldId,int fieldFlag,JComponent tool){
		FormLayout layout = new FormLayout(
		        "p:grow,0dlu,16dlu", // extra padding on right is for estimated field //$NON-NLS-1$
				"p"); //$NON-NLS-1$

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		Component c = map.getComponent(fieldId, fieldFlag);
		Field field = Configuration.getFieldFromId(fieldId);
		builder.append(c);
		if (field.getHelp() != null)
			HelpUtil.addDocHelp(c,field.getHelp());
		if (fieldFlag!=ComponentFactory.READ_ONLY) builder.append(tool);
		return builder.getPanel();
	}
	
	protected boolean hasCloseButton() {
		return true;
	}
	
}
