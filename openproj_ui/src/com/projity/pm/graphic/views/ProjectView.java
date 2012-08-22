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
package com.projity.pm.graphic.views;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

import com.projity.configuration.Dictionary;
import com.projity.document.Document;
import com.projity.document.ObjectEvent;
import com.projity.graphic.configuration.SpreadSheetFieldArray;
import com.projity.grouping.core.model.NodeModel;
import com.projity.help.HelpUtil;
import com.projity.menu.MenuActionConstants;
import com.projity.pm.graphic.frames.GraphicManager;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.model.cache.NodeModelCacheFactory;
import com.projity.pm.graphic.spreadsheet.SpreadSheet;
import com.projity.pm.task.Project;
import com.projity.pm.task.ProjectFactory;
import com.projity.strings.Messages;
import com.projity.undo.UndoController;
import com.projity.workspace.WorkspaceSetting;
/**
 * Resource view with spreadsheet
 */
public class ProjectView extends JScrollPane implements BaseView, ObjectEvent.Listener {
	private static final long serialVersionUID = -4440711646626221865L;
	private static final String spreadsheetCategory=projectSpreadsheetCategory;
	protected SpreadSheet spreadSheet;
	protected NodeModel model;
	protected NodeModelCache cache;
	Document document;
	/**
	 * 
	 */
	public ProjectView(NodeModel model, Document document) {
		super();
		this.model = model;
		this.document =document;
		HelpUtil.addDocHelp(this,"Projects_View");
		createSpreadsheet(model);
		GraphicManager.getInstance(this).getProjectFactory().getPortfolio().addObjectListener(this);		
	}
	
	public void cleanUp() {
		GraphicManager.getInstance(this).getProjectFactory().getPortfolio().removeObjectListener(this);		
		spreadSheet.cleanUp();
		spreadSheet = null;
		model = null;
		cache = null;
		document = null;
		
	}

	public void createSpreadsheet(NodeModel model){
        spreadSheet = new SpreadSheet();
		spreadSheet.setSpreadSheetCategory(spreadsheetCategory); // for columns - must do first
		
		cache=NodeModelCacheFactory.getInstance().createDefaultCache(model,document,NodeModelCache.PROJECT_TYPE,getViewName(),null);
		SpreadSheetFieldArray fields=getFields();
		spreadSheet.setCache(cache,fields,fields.getCellStyle(),fields.getActionList());

		JViewport viewport = createViewport();
		viewport.setView(spreadSheet);
		setViewport(viewport);
		
		cache.update(); //this is not required by certain views 

	}

	
	//spreadsheet fields
	private static SpreadSheetFieldArray getFields() {
		return (SpreadSheetFieldArray) Dictionary.get(spreadsheetCategory,Messages.getString("Spreadsheet.Project.default")); //TODO don't hardcode
	}

	/**
	 * @return Returns the spreadSheet.
	 */
	public SpreadSheet getSpreadSheet() {
		return spreadSheet;
	}

	/* (non-Javadoc)
	 * @see com.projity.document.ObjectEvent.Listener#objectChanged(com.projity.document.ObjectEvent)
	 */
	public void objectChanged(ObjectEvent objectEvent) {
		if (objectEvent.getObject() instanceof Project) {
			if (objectEvent.isCreate() /*|| objectEvent.isDelete()*/) {
				if (model == null) {
					return; // in the process of being cleaned up
				} else {
					cache.update();
					spreadSheet.invalidate();
				}
			}
		}
	}

	public UndoController getUndoController() {
		return null;
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
		return MenuActionConstants.ACTION_PROJECTS;
	}
	public boolean showsTasks() {
		return false;
	}
	public boolean showsResources() {
		return false;
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
		private static final long serialVersionUID = -1801198970620970719L;
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
