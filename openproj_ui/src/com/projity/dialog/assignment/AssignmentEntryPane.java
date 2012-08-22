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
package com.projity.dialog.assignment;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Transformer;

import com.projity.configuration.Dictionary;
import com.projity.datatype.Rate;
import com.projity.field.Field;
import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.graphic.configuration.SpreadSheetFieldArray;
import com.projity.graphic.configuration.shape.Colors;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeList;
import com.projity.grouping.core.event.HierarchyEvent;
import com.projity.grouping.core.event.HierarchyListener;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelFactory;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.AssignmentEntry;
import com.projity.pm.assignment.HasAssignments;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.model.cache.NodeModelCacheFactory;
import com.projity.pm.graphic.spreadsheet.SpreadSheet;
import com.projity.pm.graphic.spreadsheet.SpreadSheetModel;
import com.projity.pm.graphic.spreadsheet.editor.RateEditor;
import com.projity.pm.graphic.spreadsheet.renderer.RateRenderer;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourcePool;
import com.projity.pm.task.Project;
import com.projity.strings.Messages;

/**
 *
 */
public class AssignmentEntryPane extends JScrollPane implements HierarchyListener {
	private static final long serialVersionUID = 1L;
	CommonAssignmentDialog dialog;
	AssignmentSpreadSheet spreadSheet;
	NodeModel assignmentModel;
	Project project;
	ResourceAssigner resourceAssigner;
	List taskList = new ArrayList(); // empty selection to start
	public static final String spreadsheetCategory="assignmentEntrySpreadsheet";
	protected NodeModelCache cache;
	private boolean replace;
	
	private static final int REQUEST_DEMAND_TYPE_COLUMN = 0; // hidden now
	private static final int UNITS_COLUMN = REQUEST_DEMAND_TYPE_COLUMN+1;
	
	class AssignmentSpreadSheet extends SpreadSheet {
		private static final long serialVersionUID = 1L;
		ResourceAssigner resourceAssigner;
		/**
		 * @param resourceAssigner
		 */
		public AssignmentSpreadSheet(ResourceAssigner resourceAssigner) {
			this.resourceAssigner = resourceAssigner;
	    	setCanModifyColumns(false);
	    	//setPopupActions(null);
	    	setCanSelectFieldArray(false);

		}

		private AssignmentEntry getEntryInRow(int row) {
			Node node = ((SpreadSheetModel)getModel()).getNode(row).getNode();
			if (node != null && !node.isVirtual()) 
				return (AssignmentEntry)node.getImpl();
			else
				return null;
		}
			
		public void setValueAt(Object aValue, int row, int column) {
			AssignmentEntry entry = getEntryInRow(row);
			if (entry == null)
				return;
			
			if (!entry.isAssigned()) { // assign it first, then set value
				if (resourceAssigner != null) {
					Field field = ((SpreadSheetModel)getModel()).getFieldInColumn(column+1);
					double units = 1.0;
					if (field == AssignmentEntry.getRateField()) {
						units = ((Rate)aValue).getValue();
					}
					resourceAssigner.assign((Resource)entry.getResource(), units);
				}
			}
			super.setValueAt(aValue,row,column);
		}
        
/**
 * Gets selected resources on spreadsheet.
 * @param assignedOnly - if true, only selected resources are returned
 * @return
 */	 	List getSelectedResources(boolean assignedOnly){
	 		List list = NodeList.nodeListToImplList(getSelectedNodes());
	 		ArrayList resourceList = new ArrayList();
			Iterator i = list.iterator();
			AssignmentEntry entry;
			while (i.hasNext()) {
				entry = (AssignmentEntry)i.next();
				if (!assignedOnly || entry.isAssigned()) // see if should add.
					resourceList.add(entry.getResource());
			}
			return resourceList;
	 	}
 		public TableCellEditor getCellEditor(int row, int column) {
 			TableCellEditor editor = null;
			AssignmentEntry entry = getEntryInRow(row);
			
			if (entry != null) {
				Field field = ((SpreadSheetModel)getModel()).getFieldInColumn(column+1);
				if (field == AssignmentEntry.getRateField()) {
					if (entry.getTimeUnitLabel() != null) {
						boolean labor = ((AssignmentEntry)entry).getResource().isLabor();
						editor = new RateEditor(entry.getTimeUnitLabel(),field.isMoney(),labor && field.isPercent(),labor);
					}
				}
			}
			if (editor == null)
				editor =  super.getCellEditor(row, column);
			return editor;
		}
 		
		public TableCellRenderer getCellRenderer(int row, int column) {
			TableCellRenderer renderer = null;
			AssignmentEntry entry = getEntryInRow(row);
			
			if (entry != null) {
				Field field = ((SpreadSheetModel)getModel()).getFieldInColumn(column+1);
				if (field == AssignmentEntry.getRateField()) {
					if (entry.getTimeUnitLabel() != null) {
						renderer = new RateRenderer();
					}
				}
			}
			if (renderer == null)
				renderer =  super.getCellRenderer(row, column);
			return renderer;
		}

 	
	 	int getSelectedCount() {
	 		return NodeList.nodeListToImplList(getSelectedNodes()).size(); // doesn't count void nodes
	 	}
		
			/* (non-Javadoc)
		 * @see javax.swing.JTable#prepareRenderer(javax.swing.table.TableCellRenderer, int, int)
		 */
		public Component prepareRenderer(TableCellRenderer renderer, int row,
				int column) {
			Component component =  super.prepareRenderer(renderer, row, column);
			AssignmentEntry entry = getEntryInRow(row);
			component.setForeground(Colors.BLACK);
			
			if (entry != null) {
				if (entry.isAssigned()) {
					if (taskList.size() == entry.getAssignmentCount()) { // if all selected tasks are assigned to this resource, show it green
						//if (column == 0)
							component.setBackground(Colors.PALE_GREEN);						
					} else {
						//if (column == 0)
							component.setBackground(Colors.PALE_YELLOW);
						if (column!=0)  {
							Field field = ((SpreadSheetModel)getModel()).getFieldInColumn(column);
							if (field == Assignment.getRequestDemandTypeField() || field == AssignmentEntry.getRateField()) {
								((JLabel)component).setText(Field.MULTIPLE_VALUES);
							}
						}
					}	
				}
			}
			return component;
		}
		
		
        public Component prepareEditor(TableCellEditor editor, int row,
                int column) {
            dialog.setEditorButtonsVisible(true);
            return super.prepareEditor(editor, row, column);
        }
        public void editingCanceled(ChangeEvent e) {
            dialog.setEditorButtonsVisible(false);
            super.editingCanceled(e);
        }
        public void editingStopped(ChangeEvent e) {
            dialog.setEditorButtonsVisible(false);
            super.editingStopped(e);
        }
        
        public void doDoubleClick(int row, int col) {
        	if (dialog instanceof AssignmentDialog) {
        		((AssignmentDialog)dialog).assign();
        		((AssignmentDialog)dialog).setVisible(false);
        	} else if (dialog instanceof ReplaceAssignmentDialog)
        		((ReplaceAssignmentDialog)dialog).onOk();
        }
}
	// A transformer to create new elements from Resources
	public class NodeFactoryTransformer implements Transformer{
		public Object transform(Object impl) {
		    if (impl instanceof HasAssignments){
		        HasAssignments hasAssignments = (HasAssignments) impl;
		        return new AssignmentEntry(hasAssignments, null,project);
		    }
		    return null;
		}
    }

	protected Closure transformerClosure;
	
	/**
	 * 
	 */
	public AssignmentEntryPane(CommonAssignmentDialog dialog, Project project, ResourceAssigner resourceAssigner, boolean replace,Closure transformerClosure) {
		super();
		this.replace = replace;
		//setProject(project);
		this.resourceAssigner = resourceAssigner;
		this.dialog=dialog;
		//setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.transformerClosure=transformerClosure;
	}
	
	private SpreadSheetFieldArray getFields() {
		return (SpreadSheetFieldArray) Dictionary.get(spreadsheetCategory,Messages.getString(
				replace ? "Spreadsheet.AssignmentEntry.replaceResources"
						: "Spreadsheet.AssignmentEntry.assignResources"));
	}
	public void init() {
		if (project == null)
			return;
		ResourcePool pool = project.getResourcePool();
		
//		if (assignmentModel==null){
			pool.getResourceOutline().getHierarchy().removeHierarchyListener(this);
			pool.getResourceOutline().getHierarchy().addHierarchyListener(this);
	
			assignmentModel = NodeModelFactory.getInstance().replicate(pool.getResourceOutline(),new NodeFactoryTransformer());
			assignmentModel.getHierarchy().setNbEndVoidNodes(0); // don't allow blank lines
//		}
		if (spreadSheet==null){
			spreadSheet = new AssignmentSpreadSheet(resourceAssigner);
			spreadSheet.setSpreadSheetCategory(spreadsheetCategory);  // for columns.  Must do first
			spreadSheet.setActions(new String[]{});

		}
		
		
		cache=NodeModelCacheFactory.getInstance().createDefaultCache(assignmentModel,pool,NodeModelCache.ASSIGNMENT_TYPE,"AssignmentEntry",transformerClosure);
		SpreadSheetFieldArray fields=getFields();
		spreadSheet.setCache(cache,fields,fields.getCellStyle(),fields.getActionList());
		
		// set widths of these columns explicitly
		if (!replace) { //TODO width should come from definition in xml, not hard coded here
//			spreadSheet.getColumnModel().getColumn(REQUEST_DEMAND_TYPE_COLUMN).setPreferredWidth(50);
			spreadSheet.getColumnModel().getColumn(UNITS_COLUMN).setPreferredWidth(50);
		}

		JViewport viewport = createViewport();
		viewport.setView(spreadSheet);
		setViewport(viewport);
		
		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		Dimension d=spreadSheet.getPreferredSize();
		Dimension enclosing=new Dimension();
		
		//big awful hack to remove
		GraphicConfiguration config=GraphicConfiguration.getInstance();
		int rowHeaderWidth=config.getRowHeaderWidth() + spreadSheet.getRowMargin()*2; //should be rowHeader's one
		
//		TableColumnModel tm=spreadSheet.getColumnModel();
//		int w=tm.getTotalColumnWidth();
		
		enclosing.setSize(d.getWidth()/*+rowHeaderWidth*/,d.getHeight());
		viewport.setPreferredSize(enclosing);
		
		//setBorder(new EmptyBorder(0,0,0,0));
		updateTable();
	}
	
	/**
	 * @param project The project to set.
	 */
	public void setProject(Project project) {
		this.project = project;
		init();
	}
	
/**
 * Functor to call setAssignmentsFromTaskList
 */	private class AssignmentEntrySetter implements Closure {
		List taskList;
		AssignmentEntrySetter(List taskList) {
			this.taskList = taskList;
		}
		

		/* (non-Javadoc)
		 * @see org.apache.commons.collections.Closure#execute(java.lang.Object)
		 */
		public void execute(Object arg0) {
			AssignmentEntry entry = (AssignmentEntry)(((Node)arg0).getImpl());
			entry.setAssignmentsFromTaskList(taskList);
		}
		
	}

/**
 * Updates the spreadsheet based on the selected tasks
 * @param taskList
 */	void setSelectedTasks(List taskList) {
 		this.taskList = taskList;
		updateTable();
		// disable ss if no tasks selected. 
		boolean enabled = !taskList.isEmpty();
		spreadSheet.setEnabled(enabled);
		spreadSheet.getRowHeader().setEnabled(enabled);
//		((JDialog)dialog).setEnabled(enabled); // I choose not to disable the dialog so you can still close it

	}
 	List getSelectedResources(boolean assignedOnly){
 		return spreadSheet.getSelectedResources(assignedOnly);
 	}

 	
 	int getSelectedCount(){
 		return spreadSheet.getSelectedCount();
 	}

 	void updateTable() {
 		assignmentModel.getHierarchy().visitAll(new AssignmentEntrySetter(taskList));
 		((SpreadSheetModel)spreadSheet.getModel()).getCache().update();
 		((SpreadSheetModel)spreadSheet.getModel()).fireTableDataChanged(); // redraw it
 	}
 	
 	
 	
 	
    public AssignmentSpreadSheet getSpreadSheet() {
        return spreadSheet;
    }

    

	public void nodesChanged(HierarchyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void nodesInserted(HierarchyEvent e) {
		init();
	}

	public void nodesRemoved(HierarchyEvent e) {
		init();
	}

	public void structureChanged(HierarchyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
