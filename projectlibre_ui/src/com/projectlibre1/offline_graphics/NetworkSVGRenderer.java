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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.projectlibre1.pm.graphic.graph.GraphParams;
import com.projectlibre1.pm.graphic.model.cache.NodeModelCache;
import com.projectlibre1.pm.graphic.model.cache.NodeModelCacheFactory;
import com.projectlibre1.pm.graphic.model.cache.ReferenceNodeModelCache;
import com.projectlibre1.pm.graphic.model.event.CacheListener;
import com.projectlibre1.pm.graphic.model.event.CompositeCacheEvent;
import com.projectlibre1.pm.graphic.network.NetworkParamsImpl;
import com.projectlibre1.pm.graphic.network.NetworkRenderer;
import com.projectlibre1.pm.graphic.pert.PertLayout;
import com.projectlibre1.pm.graphic.pert.PertRenderer;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheet;
import com.projectlibre1.pm.graphic.timescale.CoordinatesConverter;
import com.projectlibre1.pm.graphic.xbs.XbsLayout;
import com.projectlibre1.pm.graphic.xbs.XbsRenderer;
import com.projectlibre1.configuration.Dictionary;
import com.projectlibre1.graphic.configuration.BarStyles;
import com.projectlibre1.pm.task.Project;

public class NetworkSVGRenderer implements SVGRenderer, CacheListener, Cloneable{
	public static final int PERT=1;
	public static final int WBS=2;
	public static final int RBS=3;
	protected NetworkParamsImpl params;
	protected CoordinatesConverter coord;
	protected SpreadSheet spreadSheet;
	protected NetworkRenderer renderer;
	protected Project project;
	public void init(Project project, ReferenceNodeModelCache refCache) {
		init(project, NodeModelCacheFactory.getInstance().createFilteredCache((ReferenceNodeModelCache)refCache,"Network",null),PERT,-1);
	}
	public void init(Project project, NodeModelCache cache,int type,int scale) {
		this.project=project;
		params=new NetworkParamsImpl();
		params.setNetworkLayout(type==PERT?new PertLayout(params):new XbsLayout(params));
		String viewName=null;
//		NodeModelCache cache=null;
		switch (type) {
			case PERT:
				viewName="pert";
//				cache=NodeModelCacheFactory.getInstance().createFilteredCache((ReferenceNodeModelCache)refCache,"Network",null);
				renderer=new PertRenderer(params);
				renderer.setVertical(false);
				break;
			case WBS:
				viewName="WBS";
//				cache=NodeModelCacheFactory.getInstance().createFilteredCache((ReferenceNodeModelCache)refCache,"WBS",null);
				renderer=new XbsRenderer(params);
				renderer.setVertical(true);
				break;
			case RBS:
				viewName="RBS";
//				cache=NodeModelCacheFactory.getInstance().createFilteredCache((ReferenceNodeModelCache)refCache,"RBS",null);
				renderer=new XbsRenderer(params);
				renderer.setVertical(true);
				break;
		}
		params.setBarStyles((BarStyles) Dictionary.get(BarStyles.category,viewName));
		params.setZoom(scale);
		params.setCache(cache);
		cache.addNodeModelListener(this);
//		renderer=new PertRenderer(params);
//		renderer.setVertical(false);
		cache.update();
		params.updateLayout();
	}


	public Object clone(){
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
	public SVGRenderer createSafePrintCopy(){
		NetworkSVGRenderer c=(NetworkSVGRenderer)clone();
		c.params=(NetworkParamsImpl)c.params.createSafePrintCopy();
		c.renderer.setGraphInfo(c.params);
		return c;
	}


	public void paint(Graphics2D g){
		paint(g,-1,-1);
	}
	public void paint(Graphics2D g,int prow,int pcol){
		Rectangle drawingBounds=params.getDrawingBounds();
		if (prow==-1){
			g.drawRect(0, 0, drawingBounds.width, drawingBounds.height);
			renderer.paint(g);
		}else{
			Rectangle printBounds=params.getPrintBounds();
			Rectangle networkPrintBounds=params.getNetworkPrintBounds(prow, pcol);
			g.translate(-pcol*printBounds.width,-prow*printBounds.height);
			//g.draw(networkPrintBounds);
			renderer.paint(g,networkPrintBounds);

		}
	 }

	public Dimension getCanvasSize(){
		return params.getDrawingBounds().getSize();
	}

	public void graphicNodesCompositeEvent(CompositeCacheEvent e) {
		params.getNetworkLayout().graphicNodesCompositeEvent(e);
	}

	public GraphParams getParams() {
		return params;
	}
	public Project getProject() {
		return project;
	}

}
