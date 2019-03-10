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
package com.projectlibre1.offline_graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.projectlibre1.pm.graphic.gantt.GanttRenderer;
import com.projectlibre1.pm.graphic.graph.GraphParams;
import com.projectlibre1.pm.graphic.model.cache.NodeModelCache;
import com.projectlibre1.pm.graphic.model.cache.NodeModelCacheFactory;
import com.projectlibre1.pm.graphic.model.cache.ReferenceNodeModelCache;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheetParamsImpl;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheetRenderer;
import com.projectlibre1.pm.graphic.spreadsheet.renderer.FontManager;
import com.projectlibre1.pm.graphic.timescale.CoordinatesConverter;
import com.projectlibre1.pm.graphic.timescale.TimeScaleComponent;
import com.projectlibre1.print.FooterRenderer;
import com.projectlibre1.print.PrintSettings;
import com.projectlibre1.configuration.Settings;
import com.projectlibre1.graphic.configuration.GraphicConfiguration;
import com.projectlibre1.graphic.configuration.SpreadSheetFieldArray;
import com.projectlibre1.pm.snapshot.Snapshottable;
import com.projectlibre1.pm.task.Project;
import com.projectlibre1.pm.task.Task;
import com.projectlibre1.pm.task.TaskSnapshot;
import com.projectlibre1.workspace.SavableToWorkspace;

/*
 *
 */
public class GanttSVGRenderer implements SVGRenderer,Cloneable{
	protected SpreadSheetParamsImpl params;
	protected CoordinatesConverter coord;
	protected GanttRenderer ganttRenderer;
	protected SpreadSheetRenderer spreadSheetRenderer;
	protected FooterRenderer footerRenderer;
	protected Project project;
	public void init(Project project, ReferenceNodeModelCache refCache) {
		SpreadSheetFieldArray fieldArray=null;
		PrintSettings printSettings=project.getPrintSettings(SavableToWorkspace.PERSIST);
		if (printSettings!=null) fieldArray=printSettings.getFieldArray();
		init(project,NodeModelCacheFactory.getInstance().createFilteredCache((ReferenceNodeModelCache)refCache,"OfflineGantt",null),fieldArray,null,-1,true);
	}
	public void init(Project project, NodeModelCache cache,SpreadSheetFieldArray fieldArray,List<Integer> colWidth,int scale,boolean printGantt) {
		this.project=project;
		coord = new CoordinatesConverter(project);
		if (scale!=-1) coord.getTimescaleManager().setCurrentScaleIndex(scale);
		params=new SpreadSheetParamsImpl(fieldArray,colWidth,printGantt);
		int rowHeight=project.getRowHeight(new TreeSet<Integer>());
		params.setRowHeight(rowHeight);

		params.setCache(cache);
		params.setCoord(coord);
		ganttRenderer=new GanttRenderer(params);
		spreadSheetRenderer=new SpreadSheetRenderer(params);
		footerRenderer=new FooterRenderer(params);
		cache.update();
	}



	public Object clone(){
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
	public SVGRenderer createSafePrintCopy(){
		GanttSVGRenderer c=(GanttSVGRenderer)clone();
		c.params=(SpreadSheetParamsImpl)c.params.createSafePrintCopy();
		c.ganttRenderer=new GanttRenderer(c.params);
		c.spreadSheetRenderer=new SpreadSheetRenderer(c.params);
		c.footerRenderer=new FooterRenderer(c.params);

		return c;
	}


	public void paint(Graphics2D g){
		paint(g,-1,-1);
	}
	public void paint(Graphics2D g,int prow,int pcol){
		Rectangle drawingBounds=params.getDrawingBounds();
		int ganttX=params.getSpreadSheetBounds().width;
		int ganttY=0;
		Rectangle spreadsheetPrintBounds=null;
		Rectangle ganttPrintBounds=null;
		boolean drawSpreadsheet=true;
		boolean drawGantt=true;
		int rowH=params.getConfiguration().getColumnHeaderHeight();




		if (prow==-1){
		}else{
			spreadsheetPrintBounds=params.getSpreadsheetPrintBounds(prow,pcol,false);
			if (spreadsheetPrintBounds==null||!params.isLeftPartVisible()) drawSpreadsheet=false;
			ganttPrintBounds=params.getGanttPrintBounds(prow,pcol);
			if (ganttPrintBounds==null) drawGantt=false;
			else{
				ganttX=params.getGanttDeltaX(prow, pcol);
				ganttY=-ganttPrintBounds.y;

			}
		}


		if (drawSpreadsheet){
			spreadSheetRenderer.paint(g,prow,pcol);
		}
		if (drawGantt){
			g.translate(ganttX,0);
			if (ganttPrintBounds!=null) g.setClip(new Rectangle(ganttPrintBounds.x,0,ganttPrintBounds.width,rowH));
			TimeScaleComponent.paintTimeScale(g,params,FontManager.getOfflineDefaultFont());
			if (ganttPrintBounds!=null) g.setClip(null);
			g.translate(0, ganttY+params.getConfiguration().getColumnHeaderHeight());
			if (ganttPrintBounds==null) ganttRenderer.paint(g,null);
			else ganttRenderer.paint(g,new Rectangle(ganttPrintBounds.x,ganttPrintBounds.y+1,ganttPrintBounds.width,ganttPrintBounds.height-1));//1 pixel offset needed for edge
			g.translate(-ganttX, -ganttY-params.getConfiguration().getColumnHeaderHeight());
		}

		g.setColor(Color.BLACK);
		if (prow==-1){
				g.drawRect(0, 0, drawingBounds.width, drawingBounds.height);
				g.drawLine(drawingBounds.x, drawingBounds.y, drawingBounds.x+drawingBounds.width, drawingBounds.y);
		}else{
			int footerH=params.getConfiguration().getPrintFooterHeight();
			Rectangle printBounds=params.getPrintBounds();
			int footerY=params.getPrintBounds().height-footerH;
			int nbCols=params.getPrintCols();
			if (drawSpreadsheet&&!drawGantt){
				g.drawRect(0, 0, spreadsheetPrintBounds.width, /*spreadsheetPrintBounds.height+rowH*/printBounds.height);
				g.drawLine(0, rowH, spreadsheetPrintBounds.width, rowH);
				g.drawLine(0, spreadsheetPrintBounds.height+rowH, spreadsheetPrintBounds.width, spreadsheetPrintBounds.height+rowH);
				g.drawLine(0, footerY, spreadsheetPrintBounds.width, footerY);
				footerRenderer.paint(g, prow*nbCols+pcol, new Rectangle(0,footerY,spreadsheetPrintBounds.width,footerH),project.getName());
			}else if (!drawSpreadsheet&&drawGantt){
				g.drawRect(0, 0, ganttPrintBounds.width, printBounds.height);
				g.drawLine(0, rowH, ganttPrintBounds.width, rowH);
				g.drawLine(0, ganttPrintBounds.height+rowH, ganttPrintBounds.width, ganttPrintBounds.height+rowH);
				g.drawLine(0, footerY, ganttPrintBounds.width, footerY);
				footerRenderer.paint(g, prow*nbCols+pcol, new Rectangle(0,footerY,ganttPrintBounds.width,footerH),project.getName());
			}else if (drawSpreadsheet&&drawGantt){
				g.drawRect(0, 0, spreadsheetPrintBounds.width+ganttPrintBounds.width, printBounds.height);
				g.drawLine(0, rowH, spreadsheetPrintBounds.width+ganttPrintBounds.width, rowH);
				g.drawLine(0, spreadsheetPrintBounds.height+rowH, spreadsheetPrintBounds.width+ganttPrintBounds.width, spreadsheetPrintBounds.height+rowH);
				g.drawLine(0, footerY, spreadsheetPrintBounds.width+ganttPrintBounds.width, footerY);
				footerRenderer.paint(g, prow*nbCols+pcol, new Rectangle(0,footerY,spreadsheetPrintBounds.width+ganttPrintBounds.width,footerH),project.getName());
			}


		}




	 }



	public Dimension getCanvasSize(){
		return params.getDrawingBounds().getSize();
	}

	public GraphParams getParams() {
		return params;
	}
	public Project getProject() {
		return project;
	}

}
