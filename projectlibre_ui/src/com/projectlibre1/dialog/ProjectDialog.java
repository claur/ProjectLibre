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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import net.sf.nachocalendar.CalendarFactory;
import net.sf.nachocalendar.components.DateField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projectlibre1.dialog.util.FieldComponentMap;
import com.projectlibre1.help.HelpUtil;
import com.projectlibre1.configuration.Configuration;
import com.projectlibre1.configuration.FieldDictionary;
import com.projectlibre1.configuration.Settings;
import com.projectlibre1.field.HasExtraFields;
import com.projectlibre1.field.HasExtraFieldsImpl;
import com.projectlibre1.options.CalendarOption;
import com.projectlibre1.pm.resource.ResourcePool;
import com.projectlibre1.pm.resource.ResourcePoolFactory;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.util.Alert;
import com.projectlibre1.util.DateTime;
import com.projectlibre1.util.Environment;

public final class ProjectDialog extends FieldDialog { // extends FieldDialog for extra fields handling
	private static final long serialVersionUID = 1L;

	public static class Form {
		String notes;
		String manager;
		String name;
		long startDate = CalendarOption.getInstance().makeValidStart(DateTime.gmt(new Date()), true);
		ResourcePool resourcePool = null;
		boolean forward = true;
		boolean local=Environment.getStandAlone();
		int projectType;
		int projectStatus;
		int expenseType;
		String group;
		String division;
		HasExtraFields extra = new HasExtraFieldsImpl();
		int accessControlType;
		
		/**
		 * @return Returns the manager.
		 */
		public String getManager() {
			return manager;
		}
		/**
		 * @param manager
		 *            The manager to set.
		 */
		public void setManager(String manager) {
			this.manager = manager;
		}
		/**
		 * @return Returns the name.
		 */
		public String getName() {
			return name;
		}
		/**
		 * @param name
		 *            The name to set.
		 */
		public void setName(String name) {
			this.name = name;
		}
		/**
		 * @return Returns the notes.
		 */
		public String getNotes() {
			return notes;
		}
		/**
		 * @param notes
		 *            The notes to set.
		 */
		public void setNotes(String notes) {
			this.notes = notes;
		}
		/**
		 * @return Returns the start.
		 */
		public long getStartDate() {
			return startDate;
		}
		/**
		 * @param start
		 *            The start to set.
		 */
		public void setStartDate(long startDate) {
			this.startDate = startDate;
		}
		/**
		 * @return Returns the resourcePool.
		 */
		public ResourcePool getResourcePool() {
			return resourcePool;
		}
		/**
		 * @param resourcePool The resourcePool to set.
		 */
		public void setResourcePool(ResourcePool resourcePool) {
			this.resourcePool = resourcePool;
		}
		/**
		 * @return Returns the forward.
		 */
		public final boolean isForward() {
			return forward;
		}
		/**
		 * @param forward The forward to set.
		 */
		public final void setForward(boolean forwardScheduled) {
			this.forward = forwardScheduled;
		}
		public boolean isLocal() {
			return local;
		}
		public void setLocal(boolean local) {
			this.local = local;
		}
		public final HasExtraFields getExtra() {
			return extra;
		}
		public int getExpenseType() {
			return expenseType;
		}
		public void setExpenseType(int expenseType) {
			this.expenseType = expenseType;
		}
		public int getProjectType() {
			return projectType;
		}
		public void setProjectType(int projectType) {
			this.projectType = projectType;
		}
		public String getDivision() {
			return division;
		}
		public void setDivision(String division) {
			this.division = division;
		}
		public String getGroup() {
			return group;
		}
		public void setGroup(String group) {
			this.group = group;
		}
		public int getProjectStatus() {
			return projectStatus;
		}
		public void setProjectStatus(int projectStatus) {
			this.projectStatus = projectStatus;
		}
		public int getAccessControlType() {
			return accessControlType;
		}
		public void setAccessControlType(int accessControlType) {
			this.accessControlType = accessControlType;
		}
		
		
	}
	private Form form;
	// use property utils to copy to project like struts

	JTextPane notes;
//	DateComboBox startDateChooser;
	DateField startDateChooser;// = CalendarFactory.createDateField();
	JTextField manager;
	JTextField name;
	JComboBox resourcePool;
	JCheckBox forward,remote;
	JLabel dateLabel;
	JComboBox projectType;
	JComboBox projectStatus;
	JComboBox expenseType;
	JTextField group;
	JTextField division;
	JComboBox accessControl;

	public static ProjectDialog getInstance(Frame owner, Form project) {
		return new ProjectDialog(owner, project);
	}

	private ProjectDialog(Frame owner, Form project) {
		super(owner, Messages.getString("ProjectDialog.NewProject"), true,false); //$NON-NLS-1$
		addDocHelp("Creating_a_Project");
		if (project != null)
			this.form = project;
		else
			this.form = new Form();
		setObjectClass(HasExtraFieldsImpl.class);
		setObject(form.extra);
		
	}

	// Component Creation and Initialization **********************************

	/**
	 * Creates, intializes and configures the UI components. Real applications
	 * may further bind the components to underlying models.
	 */
	protected void initControls() {
		notes = new JTextPane(); //TODO add scrollbars
		notes.setAutoscrolls(true);
//		startDateChooser = ComponentFactory.createDateComboBox();
		startDateChooser = CalendarFactory.createDateField();
		manager = new JTextField();
		name = new JTextField();
		ArrayList choices = new ArrayList();
		choices.add(new String());
		choices.addAll(ResourcePoolFactory.getInstance().getResourcePools());
		resourcePool = new JComboBox(choices.toArray());
		forward = new JCheckBox(Messages.getString("Field.forward")); //$NON-NLS-1$
		dateLabel = new JLabel();
		forward.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent e){
		    	setDateLabel();
		    }});
		
//		remote = new JCheckBox(Messages.getString("Text.newServerProject"));
		if (!Environment.getStandAlone()){
			accessControl=new JComboBox(new Object[]{Messages.getString("ProjectDialog.AllResourcesExceptCustomerPartner"),Messages.getString("ProjectDialog.BasedOnProjectRole")}); //$NON-NLS-1$ //$NON-NLS-2$
			HelpUtil.addDocHelp(accessControl,"Project_Team");
		}
//		remote.addItemListener(new ItemListener(){
//			public void itemStateChanged(ItemEvent e) {
//				accessControl.setEnabled(!accessControl.isEnabled());
//			}
//		});
		
		projectType = new JComboBox(Configuration.getFieldFromId("Field.projectType").getOptions(null)); //$NON-NLS-1$
		projectStatus = new JComboBox(Configuration.getFieldFromId("Field.projectStatus").getOptions(null)); //$NON-NLS-1$
		expenseType = new JComboBox(Configuration.getFieldFromId("Field.expenseType").getOptions(null)); //$NON-NLS-1$
		group = new JTextField();
		division = new JTextField();
		bind(true);
	}
	
	private void setDateLabel() {
		if (forward.isSelected())
			dateLabel.setText(Messages.getString("ProjectDialog.StartDate")); //$NON-NLS-1$
		else
			dateLabel.setText(Messages.getString("ProjectDialog.FinishDate")); //$NON-NLS-1$
	}

	protected boolean bind(boolean get) {
		if (form == null)
			return false;
		if (get) {
			notes.setText(form.getNotes());
			manager.setText(form.getManager());
			name.setText(form.getName());
//			startDateChooser.setDate(new Date(form.getStartDate()));
			Date d = new Date(form.getStartDate());
			Date zz = DateTime.gmtDate(d);
//			System.out.println("start " +d);
//			System.out.println("zz " +zz);
			startDateChooser.setValue(d);
			forward.setSelected(form.isForward());
//			remote.setSelected(!form.isLocal());
			projectType.setSelectedItem(new Integer(form.getProjectType()));
			projectStatus.setSelectedItem(new Integer(form.getProjectStatus()));
			expenseType.setSelectedItem(new Integer(form.getExpenseType()));
			group.setText(form.getGroup());
			division.setText(form.getDivision());
			setDateLabel();
			
			if (!Environment.getStandAlone()) accessControl.setSelectedIndex(0);
		} else {
			form.setNotes(notes.getText());
			form.setManager(manager.getText());
			if (name.getText().length() == 0) {
				Alert.error(Messages.getString("Message.projectMustHaveName"),this); //$NON-NLS-1$
				return false;
			}
			form.setName(name.getText());
			// make valid start
//			long d = DateTime.gmt(startDateChooser.getDate()); // + startDateChooser.getDate().getTimezoneOffset() * 60000;
			long d = DateTime.gmt((Date) startDateChooser.getValue()); // + startDateChooser.getDate().getTimezoneOffset() * 60000;
//	System.out.println("chooser " + new Date(d));
			//		d = ((Date)startDateChooser.getValue()).getTime();
			if (forward.isSelected()) {
				d = CalendarOption.getInstance().makeValidStart(d, true);
			} else {
				d = CalendarOption.getInstance().makeValidEnd(d, true);
			}
			form.setStartDate(d);
//			Object pool = resourcePool.getSelectedItem();
//			if (pool instanceof ResourcePool)
//				form.setResourcePool((ResourcePool) pool);
			form.setForward(forward.isSelected());
//			form.setLocal(!remote.isSelected());
			form.setProjectType(projectType.getSelectedIndex()); // caution ids must be sequential
			form.setProjectStatus(projectStatus.getSelectedIndex()); // caution ids must be sequential
			form.setExpenseType(expenseType.getSelectedIndex());// caution ids must be sequential
			form.setGroup(group.getText());
			form.setDivision(division.getText());
			if (!Environment.getStandAlone()) form.setAccessControlType(accessControl.getSelectedIndex());
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
		FormLayout layout = new FormLayout("default, 3dlu, 220dlu, 3dlu, default:grow", // cols //$NON-NLS-1$
	// with commented fields			"p, 3dlu,p, 3dlu, p, 3dlu, p, 3dlu,p, 3dlu, p, 3dlu,p, 3dlu,p, 3dlu,p,3dlu,p 3dlu, p, 3dlu, fill:50dlu:grow"); // rows
				"p, 3dlu,p, 3dlu,p, 3dlu, p, 3dlu, p, 3dlu,p, 3dlu, fill:50dlu:grow"); // rows //$NON-NLS-1$

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		builder.append(Messages.getString("ProjectDialog.ProjectName"), name,3); //$NON-NLS-1$
		builder.nextLine(2);
		builder.append(Messages.getString("ProjectDialog.Manager"), manager,3); //$NON-NLS-1$
		builder.nextLine(2);
		builder.append(dateLabel);
		builder.append(startDateChooser);
		builder.append(forward);
		builder.nextLine(2);
		
		if (!Environment.getStandAlone()) {
			builder.append(Messages.getString("ProjectDialog.ProjectTeam")); //$NON-NLS-1$
			builder.add(accessControl, cc.xy(builder.getColumn(), builder.getRow(),
			"left,default")); //$NON-NLS-1$
		}
		HelpUtil.addDocHelp(accessControl,"Project_Team");
//		builder.nextLine(2);
//		builder.append("Project Status:",projectStatus);
//		builder.nextLine(2);
//		builder.append("Project Type:",projectType);
//		builder.nextLine(2);
//		builder.append("Expense Type:",expenseType);
//		builder.nextLine(2);
//		builder.append("Division:",division);
//		builder.nextLine(2);
//		builder.append("Group:",group);
		builder.nextLine(2);

		FieldComponentMap map = createMap();
		Collection extraFields = FieldDictionary.extractExtraFields(FieldDictionary.getInstance().getProjectFields(),true);
		JComponent extra = createFieldsPanel(map, extraFields);
		if (extra != null) {
			builder.add(extra,cc.xyw(builder.getColumn(), builder
					.getRow(), 3));
		}
		builder.nextLine(2);
		
//		builder.append("Shared resource Pool:", resourcePool);
//		builder.nextLine(2);
		builder.append(Messages.getString("ProjectDialog.Notes")); //$NON-NLS-1$
		builder.nextLine(2);
		builder.add(new JScrollPane(notes), cc.xyw(builder.getColumn(), builder
				.getRow(), 5)); // allow spanning 3 cols
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
	protected void onCancel() {
		setVisible(false);
	}

}
