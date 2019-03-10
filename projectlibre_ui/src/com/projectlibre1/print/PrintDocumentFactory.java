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
package com.projectlibre1.print;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.projectlibre1.offline_graphics.GanttSVGRenderer;
import com.projectlibre1.offline_graphics.NetworkSVGRenderer;
import com.projectlibre1.offline_graphics.SVGRenderer;
import com.projectlibre1.pm.graphic.frames.DocumentFrame;
import com.projectlibre1.pm.graphic.model.cache.NodeModelCache;
import com.projectlibre1.pm.graphic.model.cache.NodeModelCacheFactory;
import com.projectlibre1.pm.graphic.model.cache.ReferenceNodeModelCache;
import com.projectlibre1.pm.graphic.model.transform.NodeCacheTransformer;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheet;
import com.projectlibre1.pm.graphic.views.BaseView;
import com.projectlibre1.pm.graphic.views.GanttView;
import com.projectlibre1.pm.graphic.views.PertView;
import com.projectlibre1.pm.graphic.views.ProjectView;
import com.projectlibre1.pm.graphic.views.ResourceView;
import com.projectlibre1.pm.graphic.views.TreeView;
import com.projectlibre1.graphic.configuration.SpreadSheetFieldArray;
import com.projectlibre1.grouping.core.transform.ViewTransformer;
import com.projectlibre1.pm.task.Portfolio;

public class PrintDocumentFactory {
	protected static PrintDocumentFactory instance;
	public static PrintDocumentFactory getInstance(){
		if (instance==null) instance=new PrintDocumentFactory();
		return instance;
	}
	public GraphPageable createDocument(DocumentFrame frame,boolean printOnly,boolean pdfAsDefault){
		BaseView view=frame.getActiveTopView();
		SVGRenderer renderer;
		NodeModelCache cache;
		if (view instanceof GanttView){
			renderer=new GanttSVGRenderer();
			SpreadSheet sp=frame.getActiveSpreadSheet();
			SpreadSheetFieldArray fieldArray=sp.getFieldArrayWithWidths(null);
			List<Integer> colWidth=null;
//			if (sp!=null){
//				fieldArray=(SpreadSheetFieldArray)sp.getFieldArray();
//				colWidth=getColWidth(sp, fieldArray);
//			}
			cache=NodeModelCacheFactory.getInstance().createFilteredCache((ReferenceNodeModelCache)frame.getReferenceCache(true),"OfflineGantt",null);
			((GanttSVGRenderer)renderer).init(frame.getProject(),cache,fieldArray,colWidth,frame.getScale(),true);
			renderer.getParams().setSupportLeftAndRightParts(true);
		}else if (view instanceof ResourceView){
			renderer=new GanttSVGRenderer();
			SpreadSheet sp=frame.getActiveSpreadSheet();
			SpreadSheetFieldArray fieldArray=sp.getFieldArrayWithWidths(null);
			List<Integer> colWidth=null;
//			if (sp!=null){
//				fieldArray=(SpreadSheetFieldArray)sp.getFieldArray();
//				colWidth=getColWidth(sp, fieldArray);
//			}
			cache=NodeModelCacheFactory.getInstance().createFilteredCache((ReferenceNodeModelCache)frame.getReferenceCache(false),"OfflineResources",null);
			((GanttSVGRenderer)renderer).init(frame.getProject(),cache,fieldArray,colWidth,frame.getScale(),false);
		}else if (view instanceof ProjectView){
			renderer=new GanttSVGRenderer();
			SpreadSheet sp=frame.getActiveSpreadSheet();
			SpreadSheetFieldArray fieldArray=sp.getFieldArrayWithWidths(null);
			List<Integer> colWidth=null;
//			if (sp!=null){
//				fieldArray=(SpreadSheetFieldArray)sp.getFieldArray();
//				colWidth=getColWidth(sp, fieldArray);
//			}
			Portfolio portfolio = frame.getGraphicManager().getProjectFactory().getPortfolio();
			cache=NodeModelCacheFactory.getInstance().createDefaultCache(portfolio.getNodeModel(), portfolio,NodeModelCache.PROJECT_TYPE,"OfflineProjects",null);
			((GanttSVGRenderer)renderer).init(frame.getProject(),cache,fieldArray,colWidth,frame.getScale(),false);
		}else if (view instanceof PertView){
			renderer=new NetworkSVGRenderer();
			cache=NodeModelCacheFactory.getInstance().createFilteredCache((ReferenceNodeModelCache)frame.getReferenceCache(true),"Network",null);
			((NetworkSVGRenderer)renderer).init(frame.getProject(),cache,NetworkSVGRenderer.PERT,frame.getScale());
		}else if (view instanceof TreeView){
			renderer=new NetworkSVGRenderer();
			TreeView treeView=(TreeView)view;
			if ("WBS".equals(treeView.getViewName())){
				cache=NodeModelCacheFactory.getInstance().createFilteredCache((ReferenceNodeModelCache)frame.getReferenceCache(true),"WBS",null);
				((NetworkSVGRenderer)renderer).init(frame.getProject(),cache,NetworkSVGRenderer.WBS,frame.getScale());
			}else{
				cache=NodeModelCacheFactory.getInstance().createFilteredCache((ReferenceNodeModelCache)frame.getReferenceCache(false),"RBS",null);
				((NetworkSVGRenderer)renderer).init(frame.getProject(),cache,NetworkSVGRenderer.RBS,frame.getScale());
			}
		}else return null;
		NodeModelCache srcCache=view.getCache();
		ViewTransformer transformer=((NodeCacheTransformer)cache.getVisibleNodes().getTransformer()).getTransformer();
		ViewTransformer srcTransformer=((NodeCacheTransformer)srcCache.getVisibleNodes().getTransformer()).getTransformer();
		transformer.setUserFilterId(srcTransformer.getUserFilterId()); //this is valid just because the views have the same transformers
		transformer.setUserSorterId(srcTransformer.getUserSorterId()); //this is valid just because the views have the same transformers
		transformer.setUserGrouperId(srcTransformer.getUserGrouperId()); //this is valid just because the views have the same transformers
		GraphPageable document=new GraphPageable(renderer,printOnly,pdfAsDefault,true);
		return document;
	}

//	private List<Integer> getColWidth(SpreadSheet sp,SpreadSheetFieldArray fieldArray){
//		List<Integer> colWidth=new ArrayList<Integer>(fieldArray.size());
//			colWidth.add(sp.getRowHeader().getColumnModel().getColumn(0).getWidth());
//			TableColumnModel columnModel=sp.getColumnModel();
//			TableColumn tc;
//			for (int i=0;i<columnModel.getColumnCount();i++){
//				tc=columnModel.getColumn(i);
//				colWidth.add(tc.getWidth());
//			}
//		return colWidth;
//	}
}
