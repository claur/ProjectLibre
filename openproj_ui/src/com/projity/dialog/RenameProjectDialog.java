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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.strings.Messages;

public final class RenameProjectDialog extends AbstractDialog {
	private static final long serialVersionUID = 1L;
	public static class Form {
		String name;
		Set projectNames;
		boolean saveAs;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Set getProjectNames() {
			return projectNames;
		}
		public void setProjectNames(Set projectNames) {
			this.projectNames = projectNames;
		}
		public boolean isSaveAs() {
			return saveAs;
		}
		public void setSaveAs(boolean saveAs) {
			this.saveAs = saveAs;
		}
		
		
	}
	private Form form;

	JTextField nameField;

	public static RenameProjectDialog getInstance(Frame owner, Form project) {
		return new RenameProjectDialog(owner, project);
	}

	private RenameProjectDialog(Frame owner, Form project) {
		super(owner, Messages.getString("RenameProjectDialog.RenameProject"), true); //$NON-NLS-1$
		if (project != null)
			this.form = project;
		else
			this.form = new Form();
	}

	// Component Creation and Initialization **********************************

	/**
	 * Creates, intializes and configures the UI components. Real applications
	 * may further bind the components to underlying models.
	 */
	protected void initControls() {
		nameField = new JTextField();
		nameField.addKeyListener(new KeyAdapter(){
		    public void keyReleased(KeyEvent e) {
		    	String text=nameField.getText();
		    	ok.setEnabled(!form.getProjectNames().contains(text)&&text.length()>0);
		    }
		});
		bind(true);
	}
	
	@Override
	public void onOk() {
		if (!ok.isEnabled()) // enter key should not work if name not ok
			return;
		super.onOk();
	}

	protected boolean bind(boolean get) {
		if (form == null)
			return false;
		if (get) {
			String name=form.getName();
			String newName;
			if (name==null||name.length()==0) newName=Messages.getString("RenameProjectDialog.NewProject"); //$NON-NLS-1$
			else newName=name;
			for (int i=0;form.getProjectNames().contains(newName);i++){
				newName=name+"("+i+")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			nameField.setText(newName);
		} else {
			form.setName(nameField.getText());
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
		FormLayout layout = new FormLayout("250dlu:grow", // cols //$NON-NLS-1$
				"p, 3dlu,p, 3dlu"); // rows //$NON-NLS-1$

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		if (form.isSaveAs()) builder.append(Messages.getString("Message.saveProjectAs1")+form.getName()+Messages.getString("Message.saveProjectAs2")); //$NON-NLS-1$ //$NON-NLS-2$
		else builder.append(Messages.getString("Message.renameProject1")+form.getName()+Messages.getString("Message.renameProject2")); //$NON-NLS-1$ //$NON-NLS-2$
		builder.nextLine(2);
		builder.append(createFieldPanel());
		return builder.getPanel();
	}
	public JComponent createFieldPanel(){
		FormLayout layout = new FormLayout("p,3dlu,100dlu:grow",// cols //$NON-NLS-1$
		"p"); // rows //$NON-NLS-1$
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		builder.append(Messages.getString("RenameProjectDialog.NewProjectName"), nameField); //$NON-NLS-1$
		return builder.getPanel();
	}

	/**
	 * @return Returns the form.
	 */
	public Form getForm() {
		return form;
	}
	public Object getBean(){
		return form;
	}

}