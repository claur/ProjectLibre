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
package com.projectlibre1.pm.graphic.views;

import java.awt.Component;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;

import com.projectlibre1.help.HelpUtil;
import com.projectlibre1.menu.MenuActionConstants;
import com.projectlibre1.pm.graphic.model.cache.NodeModelCache;
import com.projectlibre1.pm.graphic.model.cache.NodeModelCacheFactory;
import com.projectlibre1.pm.graphic.model.cache.ReferenceNodeModelCache;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheet;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheetModel;
import com.projectlibre1.configuration.Dictionary;
import com.projectlibre1.document.Document;
import com.projectlibre1.field.FieldContext;
import com.projectlibre1.graphic.configuration.CellStyle;
import com.projectlibre1.graphic.configuration.SpreadSheetFieldArray;
import com.projectlibre1.grouping.core.Node;
import com.projectlibre1.grouping.core.model.NodeModel;
import com.projectlibre1.pm.resource.Resource;
import com.projectlibre1.pm.resource.ResourcePool;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.undo.UndoController;
import com.projectlibre1.util.Alert;
import com.projectlibre1.util.Environment;
import com.projectlibre1.workspace.WorkspaceSetting;

/**
 * Resource view with spreadsheet
 */
public class ResourceView extends JScrollPane implements BaseView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 591334548533168582L;
	public static final String spreadsheetCategory=resourceSpreadsheetCategory;
	protected SpreadSheet spreadSheet;
	protected NodeModel model;
	protected NodeModelCache cache;
	Document document;
	FieldContext fieldContext;
	CellStyle cellStyle;
	boolean readOnly;
	/**
	 * @param master 
	 * 
	 */
	public ResourceView(ReferenceNodeModelCache cache,NodeModel model, Document document, boolean readOnly, boolean master) {
		super();
		HelpUtil.addDocHelp(this,"Resource_View");
		this.model = model;
		this.document =document;
		this.cache=NodeModelCacheFactory.getInstance().createFilteredCache((ReferenceNodeModelCache)cache,getViewName(),null);
		fieldContext = new FieldContext();
		fieldContext.setLeftAssociation(false);
		/*cellStyle=new CellStyle(){
			CellFormat cellProperties=new CellFormat();
			public CellFormat getCellProperties(GraphicNode node){
				cellProperties.setBold(node.isSummary());
				cellProperties.setItalic(node.isAssignment());
				//cellProperties.setBackground((node.isAssignment())?"NORMAL_LIGHT_YELLOW":"NORMAL_YELLOW");
				cellProperties.setCompositeIcon(node.isComposite());
				return cellProperties;
			}
			
		};*/
		createSpreadsheet();
		JViewport viewport = createViewport();
		viewport.setView(spreadSheet);
		setViewport(viewport);
		
		cache.update(); //this is not required by certain views 
		if (!master && !Environment.isProjectLibre()) {
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					Alert.warnWithOnceOption(Messages.getString("Info.resourceView"),"warnedResourceView");
				}});
		}
	}

	public void cleanUp() {
		spreadSheet.cleanUp();
		spreadSheet = null;
		model = null;
		cache = null;
		document = null;
		fieldContext = null;
		cellStyle = null;
	}
	public void createSpreadsheet(){
        spreadSheet = new SpreadSheet() {

    		private Object getEntryInRow(int row) {
    			Node node = ((SpreadSheetModel)getModel()).getNode(row).getNode();
    			if (node != null && !node.isVirtual()) 
    				return node.getImpl();
    			else
    				return null;
    		}

        
    		public Component prepareRenderer(TableCellRenderer renderer, int row,
    				int column) {
    			Component component =  super.prepareRenderer(renderer, row, column);
//    			Object r = getEntryInRow(row);
//    			if (r instanceof ResourceImpl) {
//    				if (((ResourceImpl)r).isUser()) // make user resources have a special color
//    					component.setBackground(Colors.PALE_GREEN);
//    			}
    			//Done in cellstyle
    			return component;
    		}
    		
    	    public boolean isNodeDeletable(Node node) {
    	    	if (node != null && node.getImpl() instanceof Resource) {
    	    		Resource r = (Resource)node.getImpl();
    	    		if (r.isUser()) {
    	    			Alert.warn(Messages.getString("ResourceView.YouCannotDeleteTheResource") + r.getName() + Messages.getString("ResourceView.UsersCanOnlyBeRemoved")); //$NON-NLS-1$ //$NON-NLS-2$
    	    			return false;
    	    		}
    	    		if (r.isAssignedToSomeProject()) {
    	    			Alert.warn(Messages.getString("ResourceView.YouCannotDeleteTheResource") + r.getName() + Messages.getString("ResourceView.ThisResourceCurrentlyHasAssignments")); //$NON-NLS-1$ //$NON-NLS-2$
    	    			return false;
    	    		}
    	    		List children=node.getChildren();
    	    		if (children!=null)
    	    		for (Iterator i=children.listIterator();i.hasNext();){
    	    			Node child=(Node)i.next();
    	    			if (!isNodeDeletable(child)) return false;
    	    		}
    	    	}
    	    	return true;
    	    }
    	    public boolean isNodeCuttable(Node node) {
    	    	if (node != null && node.getImpl() instanceof Resource) {
    	    		Resource r = (Resource)node.getImpl();
    	    		if (r.isUser()) {
    	    			Alert.warn(Messages.getString("ResourceView.YouCannotDeleteTheResource") + r.getName() + Messages.getString("ResourceView.UsersCanOnlyBeRemoved")+ "\n" + Messages.getString("ResourceView.ToMoveAProtectedResource")); //$NON-NLS-1$ //$NON-NLS-2$
    	    			return false;
    	    		}
    	    		if (r.isAssignedToSomeProject()) {
    	    			Alert.warn(Messages.getString("ResourceView.YouCannotDeleteTheResource") + r.getName() + Messages.getString("ResourceView.ThisResourceCurrentlyHasAssignments")+ "\n" + Messages.getString("ResourceView.ToMoveAProtectedResource")); //$NON-NLS-1$ //$NON-NLS-2$
    	    			return false;
    	    		}
    	    		List children=node.getChildren();
    	    		if (children!=null)
    	    		for (Iterator i=children.listIterator();i.hasNext();){
    	    			Node child=(Node)i.next();
    	    			if (!isNodeDeletable(child)) return false;
    	    		}
    	    	}
    	    	return true;
    	    }

        	
        };
		spreadSheet.setSpreadSheetCategory(spreadsheetCategory); // for columns - must do first
		
		SpreadSheetFieldArray fields=getFields();
		if (((ResourcePool)document).isMaster()){
			fields=(SpreadSheetFieldArray)fields.clone();
			fields.removeField("Field.userRole"); //$NON-NLS-1$
		}
		spreadSheet.setCache(cache,fields,fields.getCellStyle(),fields.getActionList());
		((SpreadSheetModel)spreadSheet.getModel()).setFieldContext(fieldContext);
		spreadSheet.setReadOnly(readOnly);
	}

	
	//spreadsheet fields
	private static SpreadSheetFieldArray getFields() {
		return (SpreadSheetFieldArray) Dictionary.get(spreadsheetCategory,Messages.getString("Spreadsheet.Resource.entryWorkResources")); //TODO don't hardcode //$NON-NLS-1$
	}

	/**
	 * @return Returns the spreadSheet.
	 */
	public SpreadSheet getSpreadSheet() {
		return spreadSheet;
	}

	public UndoController getUndoController() {
		return ((ResourcePool)document).getUndoController();
	}

	public void zoomIn() {
	}

	public void zoomOut() {
	}
	public boolean canZoomIn() {
		return false;
	}
	public boolean canZoomOut() {
		return false;
	}
	public int getScale() {
		return -1;
	}
	public boolean hasNormalMinWidth() {
		return true;
	}
	public String getViewName() {
		return MenuActionConstants.ACTION_RESOURCES;
	}
	public boolean showsTasks() {
		return false;
	}
	public boolean showsResources() {
		return true;
	}
	public void onActivate(boolean activate) {
	}
	
	public boolean isPrintable() {
		return true;
	}

	public void restoreWorkspace(WorkspaceSetting w, int context) {
		Workspace ws = (Workspace) w;
		spreadSheet.restoreWorkspace(ws.spreadSheet, context);
	}
	public WorkspaceSetting createWorkspace(int context) {
		Workspace ws = new Workspace();
		ws.spreadSheet = spreadSheet.createWorkspace(context);
		return ws;
	}

	public static class Workspace implements WorkspaceSetting { 
		private static final long serialVersionUID = -1251204386431239291L;
		WorkspaceSetting spreadSheet;
		public WorkspaceSetting getSpreadSheet() {
			return spreadSheet;
		}
		public void setSpreadSheet(WorkspaceSetting spreadSheet) {
			this.spreadSheet = spreadSheet;
		}
	}

	public boolean canScrollToTask() {
		// TODO Auto-generated method stub
		return false;
	}

	public void scrollToTask() {
		// TODO Auto-generated method stub
		
	}
	
	public NodeModelCache getCache(){
		return cache;
	}
	
	
}
