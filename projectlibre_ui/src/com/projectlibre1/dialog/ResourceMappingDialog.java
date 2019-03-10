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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import org.apache.commons.beanutils.BeanUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projectlibre1.help.HelpUtil;
import com.projectlibre1.configuration.Settings;
import com.projectlibre1.exchange.ResourceMappingForm;
import com.projectlibre1.strings.Messages;

public final class ResourceMappingDialog extends AbstractDialog {
	private static final long serialVersionUID = 1L;

	private ResourceMappingForm form;

	protected AssociationTable associationTable;
	protected JComboBox field1,editorCombo;
	protected JLabel field1Label,accessControlLabel;
	protected JCheckBox localProject;
	protected JCheckBox masterProject=null;
	protected JComboBox accessControl;

	public static ResourceMappingDialog getInstance( ResourceMappingForm form) {
		return new ResourceMappingDialog(form);
	}

	private ResourceMappingDialog(ResourceMappingForm form) {
		super(form.getOwner(), Messages.getString("ResourceMappingDialog.ResourceMerging"), true); //$NON-NLS-1$
		this.form = form;
		addDocHelp("Merge_Dialog");
	}



	// Component Creation and Initialization **********************************

	public void setForm(ResourceMappingForm form) {
		this.form = form;
	}

	/**
	 * Creates, intializes and configures the UI components. Real applications
	 * may further bind the components to underlying models.
	 */
	protected void initControls() {
		editorCombo=new JComboBox();
		associationTable=new AssociationTable();

//		field1=new JComboBox(form.getMergeFields());
//		field1.setSelectedItem(form.getMergeField());
		field1=new JComboBox();

		field1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				form.setMergeField((ResourceMappingForm.MergeField)field1.getSelectedItem());
				((AssociationTableModel)associationTable.getModel()).update();
			}
		});
		field1Label=new JLabel(Messages.getString("ResourceMappingDialog.MergeResourcesUsingField")); //$NON-NLS-1$
		localProject=new JCheckBox(Messages.getString("ResourceMappingDialog.DontMergeOpenProjectReadOnly")); //$NON-NLS-1$
		accessControlLabel=new JLabel(Messages.getString("ResourceMappingDialog.ProjectTeam")); //$NON-NLS-1$
		accessControl=new JComboBox(new Object[]{Messages.getString("ResourceMappingDialog.AllResourcesExceptCustomerPartner"),Messages.getString("ResourceMappingDialog.BasedOnProjectRoleInResourcesView")}); //$NON-NLS-1$ //$NON-NLS-2$
		HelpUtil.addDocHelp(accessControl,"Project_Team");
		//		localProject.addItemListener(new ItemListener(){
//			public void itemStateChanged(ItemEvent e) {
//				accessControl.setEnabled(!accessControl.isEnabled());
//			}
//		});
//
		localProject.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setLocal(localProject.isSelected());
				if (masterProject!=null&&localProject.isSelected()){
					form.setMaster(false);
					masterProject.setSelected(false);
				}

			}
		});

	}

	private void setLocal(boolean local){
		form.setLocal(local);
		field1Label.setEnabled(!local);
		field1.setEnabled(!local);
		associationTable.setEnabled(!local);
		//accessControl.setEnabled(!accessControl.isEnabled());
		accessControlLabel.setEnabled(!local);
		accessControl.setEnabled(!local);
	}


	public boolean bind(boolean get) {
		if (form == null)
			return false;
		if (get) {
			field1.setModel(new DefaultComboBoxModel(form.getMergeFields()));
			field1.setSelectedItem(form.getMergeField());
			editorCombo.setModel(new DefaultComboBoxModel(form.getResources()));
			editorCombo.setSelectedIndex(0);

			accessControl.setSelectedIndex(form.getAccessControlType());
			localProject.setSelected(form.isLocal());
			setLocal(form.isLocal());

			//associationTable.setModel(new AssociationTableModel());
			AssociationTableModel tableModel=(AssociationTableModel)associationTable.getModel();
			tableModel.update();
		} else {
			form.setAccessControlType(accessControl.getSelectedIndex());
			//associationTable.finishCurrentOperations();
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
		initControls();
		//TODO set minimum size
		FormLayout layout = new FormLayout("310dlu:grow", // cols //$NON-NLS-1$
				(masterProject==null)?"p,3dlu,p,3dlu,p,3dlu,p":"p,3dlu,p,3dlu,p,3dlu,p,3dlu,p"); // rows //$NON-NLS-1$ //$NON-NLS-2$

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		builder.append(createFieldPanel());
		builder.nextLine(2);
		builder.add(new JScrollPane(associationTable));
		if (masterProject!=null){
			builder.nextLine(2);
			builder.append(masterProject);
		}
		builder.nextLine(2);
		builder.append(localProject);
		builder.nextLine(2);
		builder.append(createFooterPanel());
		return builder.getPanel();
	}
	public JComponent createFieldPanel(){
		FormLayout layout = new FormLayout("p,3dlu,p",// cols //$NON-NLS-1$
		"p"); // rows //$NON-NLS-1$
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		builder.append(field1Label);
		builder.append(field1);
		return builder.getPanel();
	}
	public JComponent createFooterPanel(){
		FormLayout layout = new FormLayout("p,3dlu,p",// cols //$NON-NLS-1$
		"p"); // rows //$NON-NLS-1$
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		builder.append(accessControlLabel); //$NON-NLS-1$
		builder.add(accessControl, cc.xy(builder.getColumn(), builder.getRow(),
			"left,default")); //$NON-NLS-1$

		return builder.getPanel();
	}

	/**
	 * @return Returns the form.
	 */
	public ResourceMappingForm getForm() {
		return form;
	}
	public Object getBean(){
		return form;
	}

//	private void mapResources(String mpxFieldName){
//		Map fieldMap=new HashMap();
//		for (Iterator i=form.getImportedResources().iterator();i.hasNext();){
//
//		}
//		for (Iterator i=form.getImportedResources().iterator();i.hasNext();){
//
//		}
//
//	}



	private class AssociationTable extends JTable {
	    public AssociationTable() {
	        super(new AssociationTableModel(),new AssociationTableColumnModel());
			setCellSelectionEnabled(true);

			getTableHeader().setDefaultRenderer(new HeaderRenderer());

			registerEditors();
	        createDefaultColumnsFromModel();
	    }

		protected void registerEditors(){
			//setDefaultEditor(Date.class,new DateEditor());
		}

		public void setEnabled(boolean enabled) {
			// TODO Auto-generated method stub
			super.setEnabled(enabled);
			getTableHeader().setEnabled(enabled);

		}





//		public void finishCurrentOperations(){
//			if (isEditing()){
//				CellEditor editor=getCellEditor();
//				if (editor!=null){
//					editor.stopCellEditing();
//
//				}
//			}
//		}


	}

    private static class HeaderRenderer extends DefaultTableCellRenderer implements UIResource {
	    public HeaderRenderer(){
	    	super();
	    	setHorizontalAlignment(JLabel.CENTER);
	    }
    	public Component getTableCellRendererComponent(JTable table, Object value,
                         boolean isSelected, boolean hasFocus, int row, int column) {

	    	setEnabled(table == null || table.isEnabled());

	    	if (table != null) {
	            JTableHeader header = table.getTableHeader();
	            if (header != null) {
	                setForeground(header.getForeground());
	                setBackground(header.getBackground());
	                setFont(header.getFont());
	            }
                }

                setText((value == null) ? "" : value.toString()); //$NON-NLS-1$
		setBorder(UIManager.getBorder("TableHeader.cellBorder")); //$NON-NLS-1$
	        return this;
            }
    }

	private class AssociationTableModel extends AbstractTableModel{

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return form.getImportedResources().size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex==0){
				try {
					return BeanUtils.getProperty(form.getImportedResources().get(rowIndex),"name"); //$NON-NLS-1$
				} catch (Exception e) { //claur
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (columnIndex==1){
				return form.getSelectedResources().get(rowIndex);
			}
			return null;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex==1;
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (columnIndex==1){
				form.getSelectedResources().set(rowIndex,value);
			}
		}

		public void update(){
			fireTableDataChanged();
		}




	}




	private class AssociationTableColumnModel extends DefaultTableColumnModel{
		protected int columnIndex=0;
	    public AssociationTableColumnModel() {
	        super();
	    }
		public void addColumn(TableColumn tc){
			if (columnIndex==0){
				tc.setHeaderValue(Messages.getString("ResourceMappingDialog.ImportedResources")); //$NON-NLS-1$
				tc.setPreferredWidth(150);
			}else{
				tc.setHeaderValue(Messages.getString("ResourceMappingDialog.ServerResources")); //$NON-NLS-1$
				tc.setPreferredWidth(150);

//				tc.setCellEditor(new ListComboBoxCellEditor(new DefaultComboBoxModel(form.getResources())));
				tc.setCellEditor(new DefaultCellEditor(editorCombo));
			}
			tc.setCellRenderer(new DefaultTableCellRenderer(){
				public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column){
					setEnabled(table == null || table.isEnabled());
					super.getTableCellRendererComponent(table, value, selected, focused, row, column);
					return this;
				}
			});
			super.addColumn(tc);
			columnIndex++;
		}


		//no move
		public void moveColumn(int columnIndex, int newIndex) {
		}

	}




}
