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

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import org.apache.commons.collections.Closure;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.projectlibre1.field.FieldConverter;
import com.projectlibre1.pm.task.Project;
import com.projectlibre1.pm.task.ProjectFactory;
import com.projectlibre1.server.data.ProjectData;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.util.Environment;

public final class OpenProjectDialog extends AbstractDialog {
	private static final long serialVersionUID = 1L;
	private Object[] form;
	private List projects;
	private Set currentProjectIds;
	private boolean allowMaster = true;
	private boolean allowOpenAs;
	//private User user;

	//ActionJList list;
	OpenProjectTable table;
	JScrollPane scrollPane;
	JLabel resourcePoolMessage;
	protected JButton openReadOnly;
	private boolean openCopy;


	public static OpenProjectDialog getInstance(Frame owner, List projects, String title, boolean allowMaster, boolean allowOpenAs, Project anyProjectButThisOne) {
		return new OpenProjectDialog(owner, projects,title, allowMaster, allowOpenAs, anyProjectButThisOne);
	}

	private OpenProjectDialog(Frame owner, List projects, String title, boolean allowMaster, boolean allowOpenAs, Project anyProjectButThisOne) {
	    super(owner, title, true);
	    this.allowMaster = allowMaster;
	    this.allowOpenAs=allowOpenAs;
	    this.projects=projects;
	    currentProjectIds=new HashSet();
	    if (anyProjectButThisOne != null) {
	    	currentProjectIds.add(new Long(anyProjectButThisOne.getUniqueId()));
	    } else {
	    	ProjectFactory.getInstance().getPortfolio().forProjects(new Closure(){
	    		public void execute(Object impl) {
	    			Project project=(Project)impl;
	    			currentProjectIds.add(new Long(project.getUniqueId()));
	    		}
	    	});
	    }
	}

	protected void initControls() {
//		Vector v=new Vector();
//		v.addAll(projects);
//		list=new ActionJList(v);
//		  list.addActionListener(
//	  	    new ActionListener() {
//	  	       public void actionPerformed(ActionEvent ae) {
//	  	      OpenProjectDialog.this.onOk();
//	  	        }
//	  	    });
		table=new OpenProjectTable(this);
		bind(true);
	}

	public ButtonPanel createButtonPanel() {
		createOkCancelButtons();

		ButtonPanel buttonPanel = new ButtonPanel();
		buttonPanel = new ButtonPanel();
		buttonPanel.addButton(ok);
		if (allowOpenAs) {
			openReadOnly = new JButton(Messages.getString("ButtonText.OpenCopy"));
			openReadOnly.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					OpenProjectDialog.this.onOpenCopy();
				}
			});
			buttonPanel.add(openReadOnly);
		}


		if (hasOkAndCancelButtons())
			buttonPanel.addButton(cancel);
		return buttonPanel;
	}
    protected void onOpenCopy() {
    	this.openCopy = true;
    	onOk();
	}

	protected void createOkCancelButtons() {
    	if (allowOpenAs)
    		createOkCancelButtons(Messages.getString("ButtonText.Open"), Messages.getString("ButtonText.Cancel"));
    	else super.createOkCancelButtons();
    }

	protected boolean bind(boolean get) {
		if (get) {
		} else {
			int row=table.getSelectedRow();
			if (row<0||row>=projects.size()) form=null;
			else{
				ProjectData project=(ProjectData)projects.get(row);
				boolean copy = this.openCopy || !canBeUsed(project);
				this.openCopy = false; // for next time;
				if (!allowMaster && project.isMaster())
					return false;

				System.out.println("open " + project.getName() + " copy " + copy);

				form=new Object[]{project,copy};
			}

		}
		return true;
	}

	public JComponent createContentPanel() {
		initControls();
		//TODO set minimum size
		FormLayout layout = new FormLayout("400dlu:grow", // cols //$NON-NLS-1$
				"p,3dlu,p,2dlu"); // rows //$NON-NLS-1$

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.add(new JScrollPane(/*list*/table));
		builder.nextLine(2);
		resourcePoolMessage = new JLabel(Messages.getString("Warn.resourcePoolCannotOpen"));
		resourcePoolMessage.setVisible(false);
		builder.add(resourcePoolMessage);

		return builder.getPanel();
	}
	/**
	 * @return Returns the form.
	 */
	public Object[] getForm() {
		return form;
	}
	public Object getBean(){
		return form;
	}








	private class OpenProjectTable extends JTable {
		OpenProjectDialog dialog;
	    public OpenProjectTable(OpenProjectDialog dialog) {
	        super(new OpenProjectTableModel(),new OpenProjectableColumnModel());
	        this.dialog= dialog;
			//setCellSelectionEnabled(true);

			getTableHeader().setDefaultRenderer(new HeaderRenderer());

			registerEditors();
	        createDefaultColumnsFromModel();
	        setSelectionModel(new OpenProjectListSelectionModel());
	        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        addMouseListener();
			addKeyListener(new KeyAdapter() { // TODO need to fix focus problems elsewhere for this to always work
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
						OpenProjectDialog.this.onCancel();
					else if (e.getKeyCode() == KeyEvent.VK_ENTER)
						OpenProjectDialog.this.onOk();
				}
			});
			if (Environment.isMac()) setGridColor(Color.LIGHT_GRAY);

	    }

		protected void registerEditors(){
			//setDefaultEditor(Date.class,new DateEditor());
		}

		public void setEnabled(boolean enabled) {
			// TODO Auto-generated method stub
			super.setEnabled(enabled);
			getTableHeader().setEnabled(enabled);

		}
		private void addMouseListener() {
			addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) { // changed to mousePressed instead of mouseClicked() for snappier handling 17/5/04 hk
					if (SwingUtilities.isLeftMouseButton(e) &&!e.isConsumed() && e.getClickCount() == 2) {
						Point p = e.getPoint();
						int row = rowAtPoint(p);
						int col = columnAtPoint(p);
						if (allowOpenAs || canBeUsed((ProjectData) projects.get(row)))
							dialog.onOk();
					}
				}
			});

		}
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

	private class OpenProjectTableModel extends AbstractTableModel{
		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return projects.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			ProjectData project=(ProjectData)projects.get(rowIndex);
//			try {
			switch (columnIndex) {
			case 0:
				return project.getName();

			case 1:
				return project.getLockerInfo();
			case 2:
//				try {
						return FieldConverter.toString(project.getLastModificationDate());
//					} catch (FieldParseException e) {
//						return null;
//					}

			case 3:
				if (project.isMaster()) return null;
				return FieldConverter.toString(project.getCreationDate());


			default:
				break;
			}
			return null;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;//columnIndex==0;
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
		}

		public void update(){
			fireTableDataChanged();
		}
	}

	private class OpenProjectableColumnModel extends DefaultTableColumnModel{
		protected int columnIndex=0;
	    public OpenProjectableColumnModel() {
	        super();
	    }
		public void addColumn(TableColumn tc){
			switch (columnIndex) {
			case 0:
				tc.setHeaderValue(Messages.getString("OpenProjectDialog.Name")); //$NON-NLS-1$
				tc.setPreferredWidth(250);
				break;

			case 1:
				tc.setHeaderValue(Messages.getString("OpenProjectDialog.LockedBy")); //$NON-NLS-1$
				tc.setPreferredWidth(150);
				break;

			case 2:
				tc.setHeaderValue(Messages.getString("OpenProjectDialog.ModificationDate")); //$NON-NLS-1$
				tc.setPreferredWidth(100);
				break;

			case 3:
				tc.setHeaderValue(Messages.getString("OpenProjectDialog.CreationDate")); //$NON-NLS-1$
				tc.setPreferredWidth(100);
				break;


			default:
				break;
			}
			tc.setCellRenderer(new DefaultTableCellRenderer(){
				//protected Color defaultColor;
				public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column){
					ProjectData project=(ProjectData)projects.get(row);
					setEnabled(table == null || table.isEnabled());
					//if (defaultColor==null) defaultColor=getForeground();
					setForeground((canBeUsed(project))?Color.BLACK:Color.GRAY);
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

	private boolean canBeUsed(ProjectData project){
		return project.canBeUsed()
				&& (allowMaster || !project.isMaster())
				&& !currentProjectIds.contains(new Long(project.getUniqueId()));
	}

	private class OpenProjectListSelectionModel extends DefaultListSelectionModel{
		public void setSelectionInterval(int index0, int index1) {

			if (index0!=index1){
				return;
			}

			ProjectData project=(ProjectData)projects.get(index0);
			if (!allowMaster && project.isMaster()) {
				ok.setEnabled(false);
				if (openReadOnly != null)
					openReadOnly.setEnabled(false);
				resourcePoolMessage.setVisible(true);
			} else {
				resourcePoolMessage.setVisible(false);
				ok.setEnabled(canBeUsed(project));
				if (allowOpenAs && openReadOnly != null)
					openReadOnly.setEnabled(!project.isMaster());

				else{
					if (!canBeUsed(project)){
						clearSelection();
						return;
					}
				}
			}
			super.setSelectionInterval(index0, index1);
		}
	}



}
