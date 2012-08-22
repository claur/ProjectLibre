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
package com.projity.pm.graphic.graph;

import java.awt.Graphics;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.pm.graphic.model.cache.GraphicDependency;
import com.projity.pm.graphic.model.cache.GraphicNode;

/**
 *
 */
public class GraphUI extends ComponentUI implements Serializable {
	private static final long serialVersionUID = -8309077056249013471L;
	protected Graph graph;
    protected GraphInteractor interactor;
	protected GraphicConfiguration config;
	protected GraphRenderer graphRenderer;

	public GraphUI(Graph graph,GraphRenderer graphRenderer) {
		super();
		this.graph=graph;
		this.graphRenderer=graphRenderer;
		config=GraphicConfiguration.getInstance();
		//graphRenderer.setGraphInfo(graph);

	}





	public Graph getGraph() {
		return graph;
	}




    public GraphZone getObjectAt(double x,double y){
    	GraphZone o=getNodeAt(x,y);
    	return (o==null)?getLinkAt(x,y):o;
    }

    protected double[] segment=new double[6];
    public GraphZone getLinkAt(double x,double y){
    	return getLinkAt(x, y, graph.getModel().getDependencyIterator());
    }
    protected GraphZone getLinkAt(double x,double y,Iterator i){
		double delta=config.getSelectionSquare();
		double flatness=config.getLinkFlatness();
    	Rectangle2D selectionZone=(delta==0)?null:new Rectangle2D.Double(x-delta,y-delta,2*delta+1,2*delta+1);
    	GraphicDependency dependency;
		while(i.hasNext()){
			dependency=(GraphicDependency)i.next();
			if (selectionZone==null&&dependency.getPath().contains(x,y)) return dependency==null?null:new GraphZone(dependency);
			else if (selectionZone!=null){
				int segType;
				double lx=-1;
				double ly=-1;
				for (PathIterator j=(flatness<=0)?dependency.getPath().getPathIterator(null):dependency.getPath().getPathIterator(null,flatness);!j.isDone();j.next()){
					switch (j.currentSegment(segment)) {
						case PathIterator.SEG_LINETO:
						//case PathIterator.SEG_CLOSE:
							if (Line2D.ptSegDist(lx,ly,segment[0],segment[1],x,y)<=delta)
								return dependency==null?null:new GraphZone(dependency);
							break;
					}
					lx=segment[0];
					ly=segment[1];
				}
			}
		}
		return null;
    }


    public LinkRouting getRouting() {
		return graphRenderer.getRouting();
	}

	public void setRouting(LinkRouting routing) {
		graphRenderer.setRouting(routing);
	}





	public void updateShapes(List nodes){
    	graphRenderer.updateShapes(nodes);
    }
	public void updateShapes(){
    	graphRenderer.updateShapes();
    }
    public void paint(Graphics g, JComponent c) {
    	graphRenderer.paint(g);
    }
    public void updateShape(GraphicNode node){
    	graphRenderer.updateShape(node);
    }



    public GraphRenderer getGraphRenderer() {
		return graphRenderer;
	}



	//to override
    public GraphZone getNodeAt(double x,double y){return null;}
    public boolean isEditing(GraphicNode node){return false;}





}
