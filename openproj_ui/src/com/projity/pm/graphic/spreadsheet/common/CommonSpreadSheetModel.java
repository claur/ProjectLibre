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
package com.projity.pm.graphic.spreadsheet.common;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.collections.Closure;

import com.projity.field.Field;
import com.projity.field.FieldContext;
import com.projity.graphic.configuration.ActionList;
import com.projity.graphic.configuration.CellFormat;
import com.projity.graphic.configuration.CellStyle;
import com.projity.grouping.core.Node;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.graphic.model.cache.CacheInterval;
import com.projity.pm.graphic.model.cache.GraphicNode;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.model.event.CacheEvent;
import com.projity.pm.graphic.model.event.CacheListener;
import com.projity.pm.graphic.model.event.CompositeCacheEvent;
import com.projity.pm.graphic.spreadsheet.SpreadSheetColumnModel;
import com.projity.pm.graphic.spreadsheet.SpreadSheetUtils;
/**
 *  
 */
public class CommonSpreadSheetModel extends AbstractTableModel implements CacheListener/*implements ObjectEvent.Listener*/ {
	protected NodeModelCache cache = null;
	protected FieldContext fieldContext = null; // only used if a field context is set
	protected CellStyle cellStyle;
	protected ActionList actionList;
	protected SpreadSheetColumnModel colModel;

	
	/**
	 *  
	 */
	public CommonSpreadSheetModel(NodeModelCache cache,SpreadSheetColumnModel colModel, CellStyle cellStyle, ActionList actionList) {
		super();
//		this.fieldArray = fieldArray;
		this.colModel = colModel;
		this.cellStyle=cellStyle;
		this.actionList=actionList;
		setCache(cache);
	}
	
	protected CacheListener cacheListener=null;
	public void setCache(NodeModelCache cache){
		if (cache==this.cache) return;
		if (this.cache!=null) 
			this.cache.removeNodeModelListener(this);
		this.cache = cache;
		cache.addNodeModelListener(this);
		fireTableDataChanged();

	}
    public void graphicNodesCompositeEvent(CompositeCacheEvent compositeEvent){
        for (Iterator i=compositeEvent.getNodeEvents().iterator();i.hasNext();){
            final CacheEvent e=(CacheEvent)i.next();
            e.forIntervals(new Closure() {
                public void execute(Object obj) {
                    CacheInterval i = (CacheInterval) obj;
                     if (e.getType()==CacheEvent.NODES_CHANGED)
                        fireTableRowsUpdated(i.getStart(), i.getEnd());
                    else if (e.getType()==CacheEvent.NODES_INSERTED)
                        fireTableRowsInserted(i.getStart(), i.getEnd());
                    else if (e.getType()==CacheEvent.NODES_REMOVED)
                        fireTableRowsDeleted(i.getStart(), i.getEnd());
                }
            });
        }
    }
	
//	public CommonSpreadSheetModel(NodeModel model,ArrayList fieldArray,CellStyle cellStyle,String viewName) {
//		this(NodeModelCacheFactory.getInstance().createDefaultCache(model,viewName),fieldArray,cellStyle);
//	}
	public ArrayList getFieldArray() {
		return colModel.getFieldArray();
	}
	public void setFieldArray(ArrayList fieldArray) {
		colModel.setFieldArray(fieldArray);
	}

	public NodeModelCache getCache() {
		return cache;
	}

	/*public CellStyle getCellStyle() {
		return cellStyle;
	}
	public void setCellStyle(CellStyle cellStyle) {
		this.cellStyle = cellStyle;
	}*/
	
	public Field getFieldInColumn(int col) {
		return null;
	}

	public String getColumnName(int col) {
		return ""+col;
	}

	
	public int getRowMultiple(){ //for TimeSpreadSheet
		return 1;
	}
	
	//real rows
	public int getRowCount() {
		return getCache().getSize()*getRowMultiple();
	}
	
	public GraphicNode getNode(int row) {
		return getNodeFromCacheRow(row);
	}

	public void changeCollapsedState(int row) {
		getCache().changeCollapsedState((GraphicNode)getCache().getElementAt(row));
	}
	
	
	
	public CellFormat getCellProperties(GraphicNode node){
		return cellStyle.getCellFormat(node);
	}
	
	private String[] actions=null;
	public String[] getActionList(){
		if (actions==null){
			actions=convertActions(actionList.getList(getCache().getModel()));
		}
		return actions;
	}
	public void clearActions(){
		actions=null;
	}
	public static String[] convertActions(String actionList){
		if (actionList==null) return null;
		StringTokenizer st=new StringTokenizer(actionList,",;:|");
		String[] actions=new String[st.countTokens()];
		for (int i=0;i<actions.length;i++) actions[i]=st.nextToken();
		return actions;
	}
	
	
	protected Node getNodeInRow(int row) {
		return SpreadSheetUtils.getNodeInRow(row,getRowMultiple(),cache);
//		GraphicNode gnode = getNodeFromCacheRow(row);
//		if (gnode == null)
//			return null;
//		return gnode.getNode();
		
	}
	public LinkedList getPreviousVisibleNodesFromRow(int row) {
		LinkedList siblings=null;
		for (int r=row-1;r>=0;r--){
			Node node=getNodeInRow(r);
			if (node.getImpl() instanceof Assignment) continue;
			if (siblings==null) siblings=new LinkedList();
			siblings.addFirst(node);
			if (!node.isVoid()) return siblings;
		}
		return null; //no need to move nodes in this case since they are children of root
	}
	protected Node getNextNonVoidSiblingFromRow(int row) {
		int rowCount=getRowCount();
		Node ref=getNodeInRow(row);
		Object parent=ref.getParent();
		for (int r=row+1;r<rowCount;r++){
			Node node=getNodeInRow(r);
			if (node.getImpl() instanceof Assignment) continue;
			if (node.getParent()!=parent) break;
			if (!node.isVoid()) return node;
		}
		return null;
	}

	private GraphicNode getNodeFromCacheRow(int row) {
		return SpreadSheetUtils.getNodeFromCacheRow(row,getRowMultiple(),cache);
		//return (GraphicNode) getCache().getElementAt(row/getRowMultiple());
	}
	

	protected int findNodeRow(Node node, int searchEnd) { // limit endpoint because parents are always above 
		for (int i = 0; i < searchEnd; i++) {
			if (getNodeInRow(i) == node)
				return i;
		}
		return -1;
	}

	public int findGraphicNodeRow(Object node) {
		int row=getCache().getRowAt(node);
		if (row==-1) return -1;
		return row*getRowMultiple();
	}
	
	public Object getObjectInRow(int row) {
		if (row == -1)
			return null;
		GraphicNode gnode = getNodeFromCacheRow(row);
		if (gnode == null)
			return null;
		return gnode.getNode().getImpl();
	
	}
	int findObjectRow(Object object) {
		for (int i = 0; i < getRowCount(); i++) {
			if (getObjectInRow(i) == object)
				return i;
		}
		return -1;
	}

	
	
	
	
	
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return -1;
	}
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int col) {
		//if (col==0) return String.class;
		return getFieldInColumn(col).getDisplayType(); 
	}
	
	
	public void fireUpdateAll(){
		fireTableDataChanged();
//		fireUpdate(NULL_ROW,NULL_COL);
	}
//	
//	private static final int NULL_ROW = -1;
//	private static final int NULL_COL = 0;
//	
//	/**
//	 * Update a single cell, a column, a row, or everything
//	 * @param row
//	 * @param col
//	 */
//	private void fireUpdate(int row, int col) { //cache row
//		if (row == NULL_ROW) {
//			if (col == NULL_COL) {
//				fireTableDataChanged(); // everything changed
//			} else { // a column changed
//				
//				//TODO this isn't updating correctly.  Is there any reason to update the cache?  Cells are just updated?
//				fireTableChanged(new TableModelEvent(
//					this,
//                    0,
//                    getRowCount()-1,
//                    col,
//                    TableModelEvent.UPDATE));
//			}
//		} else {
//			if (col == NULL_COL) {
//				fireTableRowsUpdated(row*getRowMultiple(),row*getRowMultiple()+getRowMultiple()-1); // a row changed
//			} else {
//				for (int i=0; i<getRowMultiple();i++)
//					fireTableCellUpdated(row*getRowMultiple()+i, col); // a cell changed
//			}
//		}
//	}
//
//
//
//	public void objectChanged(ObjectEvent objectEvent) {
//		Object object= objectEvent.getObject();
//		if (objectEvent.getSource() != this) { // if this was the source, then the event has alrady been fired for the cell
//			if (object == null) {
//				fireUpdate(NULL_ROW,NULL_COL) ;
//				return;
//			}
//			int row = findObjectRow(objectEvent.getObject());
//			fireUpdate(row,NULL_COL);
//		}
//		Node node = objectToNode(objectEvent.getObject()); // find the node if any
//		nodeChanged(node,NULL_COL,getRowCount());// do node parents recursively
//	}	
//	
//			
///**
// * Recursively update parent nodes
// * @param node starting node
// * @param col column to update
// * @param searchEnd end row number to use when searching.  Because parents are always above children there is no need to search
// * for a parent node past its child
// */	private void nodeChanged(Node node, int col, int searchEnd) {
// 		node = getCache().getWalkersModel().getParent(node); // initial child will have already been done. Note also using model and not cache.
//		if (node == null)
//			return;
//		int row = findNodeRow(node,searchEnd);
//		if (row != NULL_ROW) {
//			fireUpdate(row,col);
//			searchEnd = row;
//		}
//		nodeChanged(node,col,searchEnd);
//	}
	
/**
 * Finds the node for this object
 * @param object
 * @return Node found, null if not found
 */	private Node objectToNode(Object object) {
 		return getCache().getWalkersModel().search(object);
	}
 

	/**
	 * @param fieldContext The fieldContext to set.
	 */
	public void setFieldContext(FieldContext fieldContext) {
		this.fieldContext = fieldContext;
	}
	public FieldContext getFieldContext() {
		return fieldContext;
	}
	public boolean isRowEditable(int row) {
		return true;
	}

}