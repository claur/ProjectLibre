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

Attribution Information: Attribution Copyright Notice: Copyright � 2006, 2007
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
package com.projity.pm.graphic.spreadsheet.common;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.CellEditor;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import com.projity.configuration.Dictionary;
import com.projity.field.Field;
import com.projity.field.FieldParseException;
import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.graphic.configuration.SpreadSheetFieldArray;
import com.projity.graphic.configuration.shape.Colors;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeFactory;
import com.projity.grouping.core.model.NodeModel;
import com.projity.pm.graphic.ChangeAwareTextField;
import com.projity.pm.graphic.model.cache.GraphicNode;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.model.event.CacheListener;
import com.projity.pm.graphic.model.event.CompositeCacheEvent;
import com.projity.pm.graphic.spreadsheet.SpreadSheetColumnModel;
import com.projity.pm.graphic.spreadsheet.SpreadSheetModel;
import com.projity.pm.graphic.spreadsheet.SpreadSheetSearchContext;
import com.projity.pm.graphic.spreadsheet.SpreadSheetUtils;
import com.projity.pm.graphic.spreadsheet.editor.KeyboardFocusable;
import com.projity.pm.graphic.spreadsheet.renderer.NameCellComponent;
import com.projity.pm.graphic.spreadsheet.selection.SpreadSheetSelectionModel;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeEvent;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeListener;
import com.projity.pm.graphic.timescale.ScaledScrollPane;
import com.projity.pm.graphic.views.SearchContext;
import com.projity.pm.graphic.views.Searchable;
import com.projity.server.access.ErrorLogger;
import com.projity.util.Alert;
import com.projity.workspace.SavableToWorkspace;
import com.projity.workspace.WorkspaceSetting;
/**
 *
 */
public class CommonSpreadSheet extends CommonTable implements CacheListener, SavableToWorkspace, Searchable {
	/**
	 *
	 */
	private static final long serialVersionUID = 2541466281456673698L;
	public static final String RESOURCE_CATEGORY="resourceSpreadsheet";
	public static final String TASK_CATEGORY="taskSpreadsheet";

	protected SpreadSheetSelectionModel selection;
	protected String spreadSheetCategory = null;
	protected SpreadSheetRowHeader rowHeader;
	protected SpreadSheetCorner corner;
	protected int lastEditingRow = -1;
	protected boolean canModifyColumns = true;
	protected boolean canSelectFieldArray = true;


	public CommonSpreadSheet() {
		super();
		setGridColor(Colors.GRAY);
		putClientProperty("JTable.autoStartsEdit",Boolean.TRUE);
		//setSurrendersFocusOnKeystroke(true); //has the side effect of selecting the first character of cell after ENTER keystroke
		setAutoCreateColumnsFromModel(false);
		rowHeader=new SpreadSheetRowHeader(this);
		rowHeader.setRowHeight(getRowHeight());

		setFocusCycleRoot(true);

	}
	public void cleanUp() {
		getCache().removeNodeModelListener((CacheListener) getModel());
	}

//	public void setModel(CommonSpreadSheetModel spreadSheetModel, DefaultTableColumnModel spreadSheetColumnModel) {
//
//		setModel(spreadSheetModel);
//	    setColumnModel(spreadSheetColumnModel);
//
//	    selection = new SpreadSheetSelectionModel(this);
//		selection.setRowSelection(new SpreadSheetListSelectionModel(selection,
//				true));
//		selection.setColumnSelection(new SpreadSheetListSelectionModel(
//				selection, false));
//		setSelectionModel(selection.getRowSelection());
//		createDefaultColumnsFromModel();
//		getColumnModel().setSelectionModel(selection.getColumnSelection());
//
//		registerEditors();
//		initRowHeader(spreadSheetModel);
//		initModel();
//		initListeners();
//
//
//
//	}

	//helper
	public void setCache(NodeModelCache cache){
		((CommonSpreadSheetModel)getModel()).setCache(cache);
	}
	public NodeModelCache getCache(){
		TableModel model=getModel();
		if (model==null||!(model instanceof CommonSpreadSheetModel)) return null;
		return ((CommonSpreadSheetModel)model).getCache();
	}

	public void setFieldArray(ArrayList fieldArray){
		((SpreadSheetColumnModel)getColumnModel()).setFieldArray(fieldArray);
//
//		((CommonSpreadSheetModel)getModel()).setFieldArray(fieldArray);
	}
	public final ArrayList getFieldArray() {
		return ((CommonSpreadSheetModel)getModel()).getFieldArray();
	}

	public final SpreadSheetFieldArray getFieldArrayWithWidths(ArrayList fieldArray) {
		if (fieldArray == null)
			fieldArray =   getFieldArray();
		// the widths don't work now anyway, and someone had a crash due to code below
		SpreadSheetColumnModel cols = (SpreadSheetColumnModel)getColumnModel();
		ArrayList<Integer> colWidths=new ArrayList<Integer>(cols.getColumnCount());
		colWidths.add(-1); //id column ignored
		for (int i=0; i < cols.getColumnCount(); i++)
			colWidths.add(cols.getColumn(i).getWidth());
		((SpreadSheetFieldArray)fieldArray).setWidths(colWidths);
		return (SpreadSheetFieldArray) fieldArray;
	}

	public final void setFieldArrayWithWidths(SpreadSheetFieldArray fieldArray) {
		setFieldArray(fieldArray);
		// the widths don't work now anyway, and someone had a crash due to code below
//		SpreadSheetColumnModel cols = (SpreadSheetColumnModel)getColumnModel();
//		for (int i=0; i < cols.getColumnCount(); i++)
//			cols.getColumn(i).setWidth(fieldArray.getWidth(i));
	}


	public void setRowHeight(int rowHeight) {
		super.setRowHeight(rowHeight);
		if (rowHeader!=null) rowHeader.setRowHeight(rowHeight);
	}
	protected void initRowHeader(CommonSpreadSheetModel spreadSheetModel){
		rowHeader.setModel(spreadSheetModel,new SpreadSheetRowHeaderColumnModel());
		rowHeader.createDefaultColumnsFromModel();
	}

	protected void initModel(){


		GraphicConfiguration config=GraphicConfiguration.getInstance();
		setRowHeight(config.getRowHeight());
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setCellSelectionEnabled(true);
		//setRowSelectionAllowed(true);
		//setColumnSelectionAllowed(true);
	}

	protected void initListeners(){
	}

	/**
	 * @return Returns the selection.
	 */
	public SpreadSheetSelectionModel getSelection() {
		return selection;
	}
	public boolean isCellEditing(int row, int col) {
		return (!(isEditing() && getEditingRow() == row && getEditingColumn() == col));
	}

	//editing for example
	public int[] finishCurrentOperations(){
		int[] rows=null;
		if (isEditing()){
			lastEditingRow = getEditingRow();
			CellEditor editor=getCellEditor();
			if (editor!=null){
				rows=getSelectedRows();
				editor.stopCellEditing();//editor.cancelCellEditing();

			}
		}
		//System.out.println("finishCurrentOperations()="+rows);
		return rows;
	}




	//node selection
	protected EventListenerList selectionNodeListenerList = new EventListenerList();

	public void addSelectionNodeListener(SelectionNodeListener l) {
	    selectionNodeListenerList.add(SelectionNodeListener.class, l);
	}
	public void removeSelectionNodeListener(SelectionNodeListener l) {
	    selectionNodeListenerList.remove(SelectionNodeListener.class, l);
	}
	public SelectionNodeListener[] getSelectionNodeListeners() {
		return (SelectionNodeListener[]) selectionNodeListenerList.getListeners(SelectionNodeListener.class);
	}
	public void fireContentsChanged(Object source, List nodes, Node currentNode) {
		Object[] listeners = selectionNodeListenerList.getListenerList();
		SelectionNodeEvent e = null;
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SelectionNodeListener.class) {
				if (e == null) {
					e = new SelectionNodeEvent(source,
							SelectionNodeEvent.SELECTION_CHANGED, nodes, currentNode,getSpreadSheetCategory());
				}
				((SelectionNodeListener) listeners[i + 1]).selectionChanged(e);
			}
		}
	}


    public EventListener[] getSelectionNodeListeners(Class listenerType) {
    	return selectionNodeListenerList.getListeners(listenerType);
    }

    public boolean isNodeDeletable(Node node) {
    	return true;
    }
    public boolean isNodeCuttable(Node node) {
    	return true;
    }
    public List getSelectedDeletableRows() {
    	ArrayList list = getSelectedNodes();
    	CollectionUtils.filter(list, new Predicate() {
			public boolean evaluate(Object arg0) {
				return isNodeDeletable((Node)arg0);
			}});
    	return list;

    }
    public List getSelectedCuttableRows(List nodes) {
    	CollectionUtils.filter(nodes, new Predicate() {
			public boolean evaluate(Object arg0) {
				return isNodeCuttable((Node)arg0);
			}});
    	return nodes;

    }
    public ArrayList getSelectedNodes(){
        SpreadSheetModel model=(SpreadSheetModel)getModel();
		int[] rows=getSelectedRows();
		ArrayList nodes=new ArrayList(rows.length);
		for (int i=0;i<rows.length;i++){
		    nodes.add(model.getNode(rows[i]).getNode());
		}
		return nodes;
    }
    public ArrayList getSelectedNodesImpl(){
        SpreadSheetModel model=(SpreadSheetModel)getModel();
		int[] rows=getSelectedRows();
		ArrayList nodes=new ArrayList(rows.length);
		for (int i=0;i<rows.length;i++){
		    nodes.add(model.getNode(rows[i]).getNode().getImpl());
		}
		return nodes;
    }
    public ArrayList getSelectedFields(){
    	if (getRowHeader().getSelectedColumns().length>0) return null;
		int[] columns=getSelectedColumns();
		ArrayList fields=new ArrayList(columns.length);
		List fieldArray=getFieldArray();
		for (int i=0;i<columns.length;i++){
			fields.add(fieldArray.get(columns[i]+1));
		}
		return fields;
    }
    public ArrayList getSelectableFields(){
    	List fa=getFieldArray();
    	ArrayList fields=new ArrayList(fa.size());
    	fields.addAll(fa);
    	if (fields.size()>0) fields.remove(0); //ID not selectable
    	return fields;
    }

    public Object getCurrentRowImpl() {
        SpreadSheetModel model=(SpreadSheetModel)getModel();
        return model.getObjectInRow(getSelectedRow());
    }
    public Node getCurrentRowNode() {
        SpreadSheetModel model=(SpreadSheetModel)getModel();
        int row = getCurrentRow();
        return model.getNodeInRow(row);
    }
    public int getCurrentRow() {
        int row = getSelectedRow();
        if (row == -1)
        	row = getEditingRow();
        if (row == -1)
        	row = lastEditingRow;
        return row;

    }

    protected boolean cellEditable=true;

	public boolean isCellEditable(int row,int col) {
		return (cellEditable)?super.isCellEditable(row,col):false;
	}
	public void setCellEditable(boolean cellEditable) {
		this.cellEditable = cellEditable;
	}
    // edit triggered by click
    public boolean editCellAt(int row, int column, EventObject e){
    	boolean b=super.editCellAt(row,column,e);
    	if (b&&editorComp!=null){
//    		System.out.println("editing cell at " + row + " " + column);
    		Component comp;
    		boolean nameCell=false;
    		if (editorComp instanceof NameCellComponent){
    			nameCell=true;
        		NameCellComponent nameCellComp=(NameCellComponent)editorComp;
        		comp=nameCellComp.getTextComponent();
        	}else
        		comp=editorComp;

    		if (comp instanceof KeyboardFocusable)
    			((KeyboardFocusable)comp).selectAll(e == null);

    		else if (comp instanceof ChangeAwareTextField){
        		ChangeAwareTextField text=((ChangeAwareTextField)comp);
        		if (e==null){
        			text.selectAll();
        		}
        		else if (e instanceof MouseEvent){
        			if (nameCell){
	        			MouseEvent me=(MouseEvent)e;
	        			Rectangle bounds=text.getBounds(null);
	        			Rectangle cell=getCellRect(row,column,false);
	        			bounds.setFrame(cell.getX()+bounds.getX(),cell.getY()+bounds.getY(),bounds.getWidth(),bounds.getHeight());
	            		if(bounds.contains(me.getPoint())){
	                 		text.selectAllOnNextCaretUpdate(); //to avoid diselection when the cell is clicked
	            		}
	         			else{ //because if it's outside there's no caret update
	         				text.requestFocus();
	         				text.selectAll();
	         			}
        			}else
        				text.selectAllOnNextCaretUpdate(); //to avoid diselection when the cell is clicked
        		}
        		text.resetChange();
        	}
        }
    	return b;
    }



    protected boolean editOnSelect=false;

	/**
	 * @return Returns the editOnSelect.
	 */
	public boolean isEditOnSelect() {
		return editOnSelect;
	}
	/**
	 * @param editOnSelect The editOnSelect to set.
	 */
	public void setEditOnSelect(boolean editOnSelect) {
		this.editOnSelect = editOnSelect;
	}

	public void changeSelection(int rowIndex, int columnIndex, boolean toggle,
			boolean extend) {
		changeSelection(rowIndex,columnIndex,toggle,extend,true);
	}
	public void changeSelection(int rowIndex, int columnIndex, boolean toggle,
			boolean extend,boolean forwards) {
    	super.changeSelection(rowIndex,columnIndex,toggle,extend);
    	if (editOnSelect&&!isEditing()){
       		editCellAt(rowIndex, columnIndex); //TODO prevent setValueAt from being called because of this on selection
    	} else {
    	}
		if (forwards){
			rowHeader.clearSelection();
			//rowHeader.changeSelection(rowIndex, columnIndex, toggle, extend,false);
		}
	}



 	public void clearSelection() {
 		if (rowHeader!=null) rowHeader.clearSelection();
		super.clearSelection();
	}


	/**
	 * @return Returns the spreadSheetCategory.
	 */
	public String getSpreadSheetCategory() {
		return spreadSheetCategory;
	}
	/**
	 * @param spreadSheetCategory The spreadSheetCategory to set.
	 */
	public void setSpreadSheetCategory(String spreadSheetCategory) {
		this.spreadSheetCategory = spreadSheetCategory;
	}

	public List getAvailableFields() {
		return SpreadSheetUtils.getFieldsForCategory(getSpreadSheetCategory());
	}

	protected void configureScrollPaneHeaders(JScrollPane scrollPane){
        if (scrollPane instanceof ScaledScrollPane)
        	scrollPane.setColumnHeaderView(((ScaledScrollPane)scrollPane).getTimeScaleComponent());
        else scrollPane.setColumnHeaderView(getTableHeader());
        JViewport vp=new JViewport();
        vp.setView(rowHeader);
        vp.setPreferredSize(rowHeader.getPreferredSize());
        scrollPane.setRowHeader(vp);
        corner=new SpreadSheetCorner(this);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER,corner);
        //scrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER,new GradientCorner());
	}

    protected void configureEnclosingScrollPane() {
    	super.configureEnclosingScrollPane();
     	Container p = getParent();
     	if (p instanceof JViewport) {
     		Container gp = p.getParent();
     		if (gp instanceof JScrollPane) {
     			JScrollPane scrollPane = (JScrollPane)gp;
     			JViewport viewport = scrollPane.getViewport();
     			if (viewport == null || viewport.getView() != this) return;

     			configureScrollPaneHeaders(scrollPane);

     			Border border = scrollPane.getBorder();
     			if (border == null || border instanceof UIResource) {
     				scrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
     			}
     		}
     	}
     }

	public Node addNodeForImpl(Object impl) {
		return addNodeForImpl(impl,NodeModel.NORMAL);
	}
	public Node addNodeForImpl(Object impl,int eventType) {
        int row = getCurrentRow();
        if (row == -1)  { // fix for bug when inserting subproject and no selection
        	row = 0; // use 0th row if no selection
        	addRowSelectionInterval(0, 0);
        }
		Node current = getCurrentRowNode();
		Node newNode = NodeFactory.getInstance().createNode(impl);
        SpreadSheetModel model=(SpreadSheetModel)getModel();
        NodeModel nodeModel = model.getCache().getModel();

		LinkedList previousNodes=model.getPreviousVisibleNodesFromRow(row);
		if (previousNodes==null) previousNodes=new LinkedList();
		previousNodes.add(current);
        nodeModel.addBefore(previousNodes,newNode,eventType);
        return newNode;
	}

    public void graphicNodesCompositeEvent(CompositeCacheEvent compositeEvent){
    	//System.out.println("cache event -> editCellAt");
    	if (isEditing()){
    		int row=getEditingRow();
    		int col=getEditingColumn();
    		TableCellEditor editor=getCellEditor();
    		editor.cancelCellEditing();
    		editCellAt(row,col);
    	}
    }




	public SpreadSheetRowHeader getRowHeader() {
		return rowHeader;
	}


	public SpreadSheetCorner getCorner() {
		return corner;
	}


/**
 * For minor spreadsheets that have fixed columns, make sure they are not modifiable
 * @return
 */
    public final boolean isCanModifyColumns() {
		return canModifyColumns && SpreadSheetUtils.getFieldsForCategory(getSpreadSheetCategory()) != null;
	}



	public final void setCanModifyColumns(boolean canModifyColumns) {
		this.canModifyColumns = canModifyColumns;
	}

	public boolean isHasColumnHeaderPopup() {
		return isCanModifyColumns();
	}

	public final boolean isCanSelectFieldArray() {
		return canSelectFieldArray;
	}


	public final void setCanSelectFieldArray(boolean canSelectFieldArray) {
		this.canSelectFieldArray = canSelectFieldArray;
	}

	public void resizeAndRepaintHeader() { // this is really abstract
	}

	protected Exception lastException;

	public final Exception getLastException() {
		return lastException;
	}

	protected void doPostExceptionTreatment() {

	}
	public void setValueAt(Object arg0, int arg1, int arg2) {
		lastException = null; // initialize. will get set if a throw
		try {
			super.setValueAt(arg0, arg1, arg2);
		} catch (Exception e) { // because setValue has no exceptions, I package it in a runtime one
			lastException = (Exception) e.getCause(); // editors will use this value to see if exception
			if (lastException==null) lastException=e;
			Alert.error(lastException.getMessage(),this); //TODO clean up messages

			doPostExceptionTreatment();
		}
	}

	public SearchContext createSearchContext() {
		SpreadSheetSearchContext ctx = new SpreadSheetSearchContext();
		return ctx;

	}

/**
 * Used by find dialog
 */
	public boolean findNext(SearchContext context) {
		SpreadSheetSearchContext ctx = (SpreadSheetSearchContext)context;

		int row = this.getCurrentRow();
		// make sure in bounds
		if (row < 0)
			row =0;
		if (row >= getCache().getSize())
			row = getCache().getSize() -1;

		ListIterator i =getCache().getIterator(row);
		if (ctx.getRow() != -1) { // after the first search, need to move ahead or back
			if (ctx.isForward())
				if (i.hasNext())
					i.next();
			else
				if (i.hasPrevious())
					i.previous();
		}

		boolean found = false;
		GraphicNode gnode = null;
		Object obj;
		Node node;
		while (ctx.isForward() ? i.hasNext() : i.hasPrevious()) {
			gnode=(GraphicNode)(ctx.isForward() ? i.next() : i.previous());
			if (gnode.isVoid())
				continue;
			node = gnode.getNode();
			obj = node.getImpl();
			if (ctx.matches(obj)) {
				found = true;
				break;
			}
		}
		if (found) {
			int r = getCache().getRowAt(gnode);
			int col = getFieldArray().indexOf(ctx.getField())-1;
			this.changeSelection(r, col, false, false);
			ctx.setRow(r);
		}
		return found;
	}

	public void selectObject(Object object) {
		int row = ((CommonSpreadSheetModel)getModel()).findObjectRow(object);
		if (row != -1) {
			finishCurrentOperations();
			changeSelection(row, getSelectedColumn(), false, false);
		}

	}

	public void restoreWorkspace(WorkspaceSetting w, int context) {
		// this checks for invalid conditions and continues

		Workspace ws = (Workspace) w;
		if (getRowCount() > ws.editingRow)
			setEditingRow(ws.editingRow);
		if (getColumnCount() > ws.editingColumn)
			setEditingColumn(ws.editingColumn);
		if (getRowCount() > ws.lastEditingRow)
			lastEditingRow = ws.lastEditingRow;
		if (ws.selectedRows != null) {
			for (int i=0; i < ws.selectedRows.length; i++) {
				try {
					addRowSelectionInterval(ws.selectedRows[i], ws.selectedRows[i]);
					// this isn't quite right.
					rowHeader.addRowSelectionInterval(ws.selectedRows[i], ws.selectedRows[i]);

				} catch (RuntimeException e) {
					// in case out of bounds
				}
			}
		}
		if (ws.selectedColumns != null) {
			for (int i=0; i < ws.selectedColumns.length; i++) {
				try {
					addColumnSelectionInterval(ws.selectedColumns[i], ws.selectedColumns[i]);
				} catch (RuntimeException e) {
					// in case out of bounds
				}
			}
		}
		//TODO the column widths are not set, so if they change, they are not used
		SpreadSheetFieldArray s = (SpreadSheetFieldArray) Dictionary.get(getSpreadSheetCategory(),ws.fieldArrayName);
		if (s != null)
			setFieldArray(s);
     	Container p = getParent();
     	if (p instanceof JViewport && ws.viewPosition != null) {
     		try {
     		((JViewport)p).setViewPosition(ws.viewPosition);
     		} catch (RuntimeException e) {
     			System.out.println("problem restoring viewport to point " + ws.viewPosition);
     		}
     	}
	}



	public WorkspaceSetting createWorkspace(int context) {
		Workspace ws = new Workspace();
		ws.editingRow = getEditingRow();
		ws.editingColumn = getEditingColumn();
		ws.lastEditingRow = lastEditingRow;
		ws.selectedRows = getSelectedRows();
		ws.selectedColumns = getSelectedColumns();
		ws.fieldArrayName = getFieldArray().toString();
     	Container p = getParent();
     	if (p instanceof JViewport) {
     		ws.viewPosition = ((JViewport)p).getViewPosition();
     	}
		return ws;
	}

	public static class Workspace implements WorkspaceSetting {
		private static final long serialVersionUID = -847570793053006783L;
		//TODO the column sizes and possible reording aren't saved yet. maybe the easiest way would be to just serialize the column model.
		int editingRow;
		int editingColumn;
		int lastEditingRow;
		int[] selectedRows=null;
		int[] selectedColumns=null;
		String fieldArrayName;
		Point viewPosition = null;

		public final int getEditingColumn() {
			return editingColumn;
		}

		public final void setEditingColumn(int editingColumn) {
			this.editingColumn = editingColumn;
		}

		public final int getEditingRow() {
			return editingRow;
		}

		public final void setEditingRow(int editingRow) {
			this.editingRow = editingRow;
		}

		public final String getFieldArrayName() {
			return fieldArrayName;
		}

		public final void setFieldArrayName(String fieldArrayName) {
			this.fieldArrayName = fieldArrayName;
		}

		public final int getLastEditingRow() {
			return lastEditingRow;
		}

		public final void setLastEditingRow(int lastEditingRow) {
			this.lastEditingRow = lastEditingRow;
		}

		public final int[] getSelectedColumns() {
			return selectedColumns;
		}

		public final void setSelectedColumns(int[] selectedColumns) {
			this.selectedColumns = selectedColumns;
		}

		public final int[] getSelectedRows() {
			return selectedRows;
		}

		public final void setSelectedRows(int[] selectedRows) {
			this.selectedRows = selectedRows;
		}

		public Point getViewPosition() {
			return viewPosition;
		}

		public void setViewPosition(Point viewPosition) {
			this.viewPosition = viewPosition;
		}
	}




}