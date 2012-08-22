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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.Border;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.help.HelpUtil;
import com.projity.menu.MenuManager;
import com.projity.pm.graphic.IconManager;
import com.projity.strings.Messages;
import com.projity.util.Environment;

public final class WelcomeDialog extends AbstractDialog {
	private static final long serialVersionUID = 1L;

	public static class Form {
		boolean createProject = true;
		boolean openProject = false;
		boolean manageResources = false;
		public final boolean isCreateProject() {
			return createProject;
		}
		public final void setCreateProject(boolean createProject) {
			this.createProject = createProject;
		}
		public final boolean isManageResources() {
			return manageResources;
		}
		public final void setManageResources(boolean manageResources) {
			this.manageResources = manageResources;
		}
		public final boolean isOpenProject() {
			return openProject;
		}
		public final void setOpenProject(boolean openProject) {
			this.openProject = openProject;
		}
	}
	private Form form;
	private MenuManager menuManager;
	// use property utils to copy to project like struts

	ButtonGroup radioGroup;
	JButton createProject;
	JButton openProject;
	JButton manageResources;

	protected boolean bind(boolean get) {
		if (form == null)
			return false;
		if (!get) {
			form.setCreateProject(createProject.isSelected());
			form.setOpenProject(openProject.isSelected());
			form.setManageResources(manageResources.isSelected());
		}
		return true;
	}
	public static WelcomeDialog getInstance(Frame owner, MenuManager menuManager) {
		return new WelcomeDialog(owner,menuManager);
	}

	private WelcomeDialog(Frame owner, MenuManager menuManager) {
		super(owner, Messages.getContextString("Text.welcomeToPod"), true); //$NON-NLS-1$
		this.menuManager = menuManager;
		form = new Form();
	}

	// Component Creation and Initialization **********************************

	/**
	 * Creates, intializes and configures the UI components. Real applications
	 * may further bind the components to underlying models.
	 */
	protected void initControls() {
		createProject = new JButton(Messages.getString("Text.createProject"),IconManager.getIcon("menu24.new"));
		openProject = new JButton(Messages.getString("Text.openProject"),IconManager.getIcon("menu24.open"));
		manageResources = new JButton(Messages.getString("Text.manageResources"),IconManager.getIcon("view.resources"));
		
		HelpUtil.addDocHelp(createProject,"Creating_a_Project");
		HelpUtil.addDocHelp(manageResources,"Managing_your_resource_pool");
		
		createProject.setSelected(true);
		
		ActionListener buttonListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createProject.setSelected(false);
				openProject.setSelected(false);
				manageResources.setSelected(false);
				((JButton)e.getSource()).setSelected(true);
				onOk();
			}};
			
		createProject.addActionListener(buttonListener);
		openProject.addActionListener(buttonListener);
		manageResources.addActionListener(buttonListener);
		
		bind(true);
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
		FormLayout layout = new FormLayout("default, 3dlu, default, 3dlu, default", // cols //$NON-NLS-1$
				"p, 8dlu, p, 3dlu, p, 3dlu, p, 3dlu"); // rows //$NON-NLS-1$

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setBorder(BorderFactory.createEmptyBorder(20,20,20,20)); // use bigger border to fit title bar text
		builder.append(Messages.getString("WelcomeDialog.WhatWouldYouLikeToDo") + "      "); // adding spaces to widen dialog //$NON-NLS-1$ //$NON-NLS-2$
		builder.nextLine(2);
		builder.append(createProject);
		builder.nextLine(2);
		builder.append(openProject);
		if (Environment.isAdministrator()) {
			builder.nextLine(2);
			builder.append(manageResources);
		}
		requestFocusInWindow();
		return builder.getPanel();
	}

	/**
	 * @return Returns the form.
	 */
	public Form getForm() {
		return form;
	}
	
	public Object getBean() {
		return form;
	}
	@Override
	protected boolean hasOkAndCancelButtons() {
		return false;
	}
}
