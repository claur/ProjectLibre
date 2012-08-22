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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.configuration.Configuration;
import com.projity.dialog.util.ComponentFactory;
import com.projity.dialog.util.FieldComponentMap;
import com.projity.field.Field;
import com.projity.help.HelpUtil;
import com.projity.menu.MenuActionConstants;
import com.projity.pm.graphic.IconManager;
import com.projity.pm.graphic.frames.GraphicManager;
import com.projity.strings.Messages;

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
