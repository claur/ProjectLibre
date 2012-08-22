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
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JRadioButton;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.dialog.util.ComponentFactory;
import com.projity.dialog.util.ExtDateField;
import com.projity.options.CalendarOption;
import com.projity.strings.Messages;

/**
 *
 */
public class UpdateProjectDialogBox extends AbstractDialog {
	private static final long serialVersionUID = 1L;
	public static class Form {
		Boolean update;
		Boolean progress;
		Boolean entireProject;
		Date updateDate = new Date(CalendarOption.getInstance().makeValidStart(System.currentTimeMillis(), true));		
		Date rescheduleDate = new Date(CalendarOption.getInstance().makeValidStart(System.currentTimeMillis(), true));
	
		public Date getActiveDate() {
			return update.booleanValue() ? updateDate : rescheduleDate;
		}
		
	    public Boolean getEntireProject() {
	        return entireProject;
	    }
	    public void setEntireProject(Boolean entireProject) {
	        this.entireProject = entireProject;
	    }
	    public Boolean getProgress() {
	        return progress;
	    }
	    public void setProgress(Boolean progress) {
	        this.progress = progress;
	    }
	    public Date getRescheduleDate() {
	        return rescheduleDate;
	    }
	    public void setRescheduleDate(Date rescheduleDate) {
	        this.rescheduleDate = rescheduleDate;
	    }
	    public Boolean getUpdate() {
	        return update;
	    }
	    public void setUpdate(Boolean update) {
	        this.update = update;
	    }
	    public Date getUpdateDate() {
	        return updateDate;
	    }
	    public void setUpdateDate(Date updateDate) {
	        this.updateDate = updateDate;
	    }
    }	
    
    private Form form;
    boolean hasTasksSelected;
    JRadioButton entireProject;
    JRadioButton selectedTask;
    ButtonGroup projectOrTask;
    JRadioButton update;
    JRadioButton reschedule;
    ButtonGroup updateOrReschedule;
	ExtDateField updateDateChooser;
	ExtDateField rescheduleDateChooser;
	JRadioButton progress;
	JRadioButton completeOrNotOnly;
	ButtonGroup progressCalculationType;

    

    
	public static UpdateProjectDialogBox getInstance(Frame owner, Form project, boolean hasTasksSelected) {
		return new UpdateProjectDialogBox(owner, project,hasTasksSelected);
	}

	private UpdateProjectDialogBox(Frame owner, Form project, boolean hasTasksSelected) {
		super(owner, Messages.getString("UpdateProjectDialogBox.UpdateProject"), true); //$NON-NLS-1$
		addDocHelp("Update_Project");
		this.hasTasksSelected = hasTasksSelected;
		if (project != null)
			this.form = project;
		else
			this.form = new Form();
	}
	
	protected void initControls() {
	    entireProject = new JRadioButton(Messages.getString("UpdateProjectDialogBox.EntireProject")); //$NON-NLS-1$
	    entireProject.setSelected(true);
	    selectedTask = new JRadioButton(Messages.getString("UpdateProjectDialogBox.SelectedTasks")); //$NON-NLS-1$
	    if (!hasTasksSelected)
	    	selectedTask.setEnabled(false);
	    projectOrTask = new ButtonGroup();
	    projectOrTask.add(entireProject);
	    projectOrTask.add(selectedTask);
	    
	    update = new JRadioButton();
	    update.setSelected(true);
	    reschedule = new JRadioButton();
	    updateOrReschedule = new ButtonGroup();
	    updateOrReschedule.add(update);
	    updateOrReschedule.add(reschedule);
	    
		updateDateChooser = ComponentFactory.createDateField();
		rescheduleDateChooser = ComponentFactory.createDateField();
		rescheduleDateChooser.setEnabled(false);
		
		progress= new JRadioButton(Messages.getString("UpdateProjectDialogBox.SetZeroHundred")); //$NON-NLS-1$
		progress.setSelected(true);
		completeOrNotOnly= new JRadioButton(Messages.getString("UpdateProjectDialogBox.SetZeroOrHundredOnly")); //$NON-NLS-1$
		progressCalculationType = new ButtonGroup();;
		progressCalculationType.add(progress);
		progressCalculationType.add(completeOrNotOnly);

		update.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent e){
		        progress.setEnabled(true);
		        completeOrNotOnly.setEnabled(true);
		        updateDateChooser.setEnabled(true);
		        rescheduleDateChooser.setEnabled(false);
		    }});
		reschedule.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent e){
		        progress.setEnabled(false);
		        completeOrNotOnly.setEnabled(false);
		        updateDateChooser.setEnabled(false);
		        rescheduleDateChooser.setEnabled(true);
		    }});




	}
	
	protected boolean bind(boolean get) {
		if (form == null)
			return false;
		if (get) {
		    entireProject.setSelected((form.getEntireProject()).booleanValue());
		    update.setSelected((form.getUpdate()).booleanValue());
		    progress.setSelected(form.getProgress().booleanValue());			
			updateDateChooser.setValue(form.getUpdateDate());
			rescheduleDateChooser.setValue(form.getRescheduleDate());
		} else {
			Boolean b1=new Boolean(entireProject.isSelected());
			form.setEntireProject(b1);
			Boolean b2=new Boolean(update.isSelected());
			form.setUpdate(b2);
			Boolean b3=new Boolean(progress.isSelected());
			form.setProgress(b3);	    		    
			long d1 = updateDateChooser.getDateValue().getTime();
			d1 = CalendarOption.getInstance().makeValidStart(d1, true);
			form.setUpdateDate(new Date(d1));
			long d2 = rescheduleDateChooser.getDateValue().getTime();
			d2 = CalendarOption.getInstance().makeValidStart(d2, true);
			form.setRescheduleDate(new Date(d2));
		}
		return true;
	}
	
	public JComponent createContentPanel() {
	
		initControls();
		
		FormLayout layout = new FormLayout(
		        "20dlu,3dlu,p, 3dlu,75dlu,3dlu,30dlu ", //$NON-NLS-1$
	    		  "p,1dlu,p,1dlu,p,10dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p"); //$NON-NLS-1$

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		builder.append(update);
		builder.append(Messages.getString("UpdateProjectDialogBox.UpdateWorkAsCompleteThrough")); //$NON-NLS-1$
		builder.append(updateDateChooser);
		builder.nextLine(2);
		builder.nextColumn(2);
		builder.add(progress,cc.xyw(builder.getColumn(), builder
				.getRow(), 5));
		builder.nextLine(2);
		builder.nextColumn(2);
		builder.add(completeOrNotOnly,cc.xyw(builder.getColumn(), builder
				.getRow(), 5));
		builder.nextLine(2);
		builder.append(reschedule);
		builder.append(Messages.getString("UpdateProjectDialogBox.RescheduleCompletedWorkToStartAfter")); //$NON-NLS-1$
		builder.append(rescheduleDateChooser);
		builder.nextLine(8);
		builder.addSeparator(""); //$NON-NLS-1$
		builder.nextLine(2);
		builder.append(Messages.getString("UpdateProjectDialogBox.For")); //$NON-NLS-1$
		builder.nextLine(2);
		builder.nextColumn(2);
		builder.append(entireProject);
		builder.nextLine(2);
		builder.nextColumn(2);
		builder.append(selectedTask);

		return builder.getPanel();
	}
	
	
	public Form getForm() {
		return form;
	}
	public Object getBean(){
		return form;
	}
	
}
