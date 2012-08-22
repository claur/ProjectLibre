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

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JRadioButton;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.configuration.Settings;
import com.projity.pm.graphic.frames.GraphicManager;
import com.projity.pm.snapshot.SnapshottableImpl;
import com.projity.strings.Messages;

public final class BaselineDialog extends AbstractDialog {
	private static final long serialVersionUID = 1L;
	boolean hasTasksSelected;
    boolean entire = true;
	public static class Form {
		int baselineNumber = 0; // baselines start at 1
		boolean entireProject = true;
		/**
		 * @return Returns the baselineNumber.
		 */
		public int getBaselineNumber() {
			return baselineNumber;
		}
		/**
		 * @param baselineNumber The baselineNumber to set.
		 */
		public void setBaselineNumber(int baselineNumber) {
			this.baselineNumber = baselineNumber;
		}
		/**
		 * @return Returns the entireProject.
		 */
		public boolean isEntireProject() {
			return entireProject;
		}
		/**
		 * @param entireProject The entireProject to set.
		 */
		public void setEntireProject(boolean entireProject) {
			this.entireProject = entireProject;
		}
	}
	private Form form;
	
	// use property utils to copy to project like struts

	JComboBox baseline;
	JRadioButton entireProject;
	JRadioButton selectedTasks;
	ButtonGroup radioGroup;

	public final void setHasTasksSelected(boolean hasTasksSelected) {
		this.hasTasksSelected = hasTasksSelected;
    	selectedTasks.setEnabled(hasTasksSelected);
    	if (!hasTasksSelected) {
    		entire = true;
    	}
    		
	}
	protected boolean bind(boolean get) {
		if (form == null)
			return false;
		if (get) {
			selectedTasks.setSelected(!entire);
			entireProject.setSelected(entire);
			baseline.setSelectedIndex(form.getBaselineNumber());
		} else {
			form.setEntireProject(entire);
			form.setBaselineNumber(baseline.getSelectedIndex());
		}
		return true;
	}
	public static BaselineDialog getInstance(GraphicManager graphicManager, Form project, boolean save, boolean hasTasksSelected) {
//		BaselineDialog instance = graphicManager.getBaselineDialog();
		BaselineDialog instance =null;// having problems with the radio buttons, so I'm creating a new one each time
		if (instance == null) {
			instance = new BaselineDialog(graphicManager.getFrame(), project,hasTasksSelected);
			graphicManager.setBaselineDialog(instance);
		} else
			instance.setHasTasksSelected(hasTasksSelected);
		instance.setTitle(Messages.getString( save ? "Text.SaveBaseline" : "Text.ClearBaseline")); //$NON-NLS-1$ //$NON-NLS-2$
		instance.addDocHelp(save ? "Save_Baseline_Dialog" : "Clear_Baseline_Dialog");
		
		return instance;
	}

	private BaselineDialog(Frame owner, Form baselineForm, boolean hasTasksSelected) {
		super(owner, "", true); //$NON-NLS-1$
		this.hasTasksSelected = hasTasksSelected;
		if (baselineForm != null)
			form = baselineForm;
		else
			form = new Form();
		
	}

	// Component Creation and Initialization **********************************

	/**
	 * Creates, intializes and configures the UI components. Real applications
	 * may further bind the components to underlying models.
	 */
	protected void initControls() {
		baseline = new JComboBox(SnapshottableImpl.getSnapshotNames());

		entireProject = new JRadioButton(Messages.getString("Text.EntireProject")); //$NON-NLS-1$
		selectedTasks = new JRadioButton(Messages.getString("Text.SelectedTasks")); //$NON-NLS-1$
		
		// for some strange reason, the value of the buttons is not correct in bind at the end, so I am using a listener instead
		entireProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				entire = ((JRadioButton)e.getSource()).isSelected();
			}});
		selectedTasks.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				entire = !((JRadioButton)e.getSource()).isSelected();
			}});
		radioGroup = new ButtonGroup();
		radioGroup.add(entireProject);
		radioGroup.add(selectedTasks);
    	setHasTasksSelected(hasTasksSelected);

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
		FormLayout layout = new FormLayout("default, 3dlu, fill:80dlu:grow", // cols //$NON-NLS-1$
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"); // rows //$NON-NLS-1$

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		builder.append(Messages.getString("BaselineDialog.Baseline")); //$NON-NLS-1$
		builder.append(baseline);
		builder.nextLine(2);
		builder.addSeparator(""); //$NON-NLS-1$
		builder.nextLine(2);
		builder.append(Messages.getString("BaselineDialog.For")); //$NON-NLS-1$
		builder.nextLine(2);
		builder.append(entireProject);
		builder.nextLine(2);
		builder.append(selectedTasks);
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
}
