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
package com.projity.offline_graphics;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.projity.configuration.Dictionary;
import com.projity.graphic.configuration.BarStyles;
import com.projity.pm.graphic.graph.GraphParams;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.model.cache.NodeModelCacheFactory;
import com.projity.pm.graphic.model.cache.ReferenceNodeModelCache;
import com.projity.pm.graphic.model.event.CacheListener;
import com.projity.pm.graphic.model.event.CompositeCacheEvent;
import com.projity.pm.graphic.network.NetworkParamsImpl;
import com.projity.pm.graphic.network.NetworkRenderer;
import com.projity.pm.graphic.pert.PertLayout;
import com.projity.pm.graphic.pert.PertRenderer;
import com.projity.pm.graphic.spreadsheet.SpreadSheet;
import com.projity.pm.graphic.timescale.CoordinatesConverter;
import com.projity.pm.graphic.xbs.XbsLayout;
import com.projity.pm.graphic.xbs.XbsRenderer;
import com.projity.pm.task.Project;

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
