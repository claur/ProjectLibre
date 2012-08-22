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
package com.projity.pm.graphic.network;

import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.util.List;
import java.util.ListIterator;

import com.projity.pm.graphic.graph.GraphRenderer;
import com.projity.pm.graphic.graph.GraphUI;
import com.projity.pm.graphic.graph.GraphZone;
import com.projity.pm.graphic.model.cache.GraphicNode;

/**
 *
 */
public abstract class NetworkUI extends GraphUI{
	public NetworkUI(Network graph,GraphRenderer graphRenderer) {
		super(graph,graphRenderer);
	}



//	public AffineTransform getTransform(){
//		return ((Network)getGraph()).getTransform();
//	}
	public int getZoom() {
		return ((Network)getGraph()).getZoom();
	}

	void resetForms(){
		((NetworkRenderer)graphRenderer).resetForms();
	}


	public boolean isEditing(GraphicNode node) {
		return ((NetworkRenderer)graphRenderer).getEditor().isEditing(node);
	}
	public void editNode(GraphicNode node) {
		((NetworkRenderer)graphRenderer).getEditor().initEditorComponent(node,getZoom(),(node==null)?null:((NetworkRenderer)graphRenderer).getBounds(node));
	}

	public List getEditorChange(){
		return ((NetworkRenderer)graphRenderer).getEditor().getCellEditorChange();
	}
	public GraphicNode getEditorNode(){
		return ((NetworkRenderer)graphRenderer).getEditor().getNode();
	}
















    public GraphZone getNodeAt(double x,double y){
		GraphicNode node;
		GeneralPath shape;
		for (ListIterator i=graph.getModel().getNodeIterator();i.hasNext();){
			node=(GraphicNode)i.next();
			shape=((NetworkRenderer)graphRenderer).getShape(node);
			if (shape!=null&&shape.contains(x,y)){
				return node==null?null:new GraphZone(node);
			}
		}
		return null;
    }













	public boolean isOnBarEdge(GraphicNode node,double x,double y){
		double delta=config.getNetworkCellSelectionSquare();
		GeneralPath shape=((NetworkRenderer)graphRenderer).getShape(node);
		double lx=-1;
		double ly=-1;
		double fx=-1;
		double fy=-1;
		int segType;
		for (PathIterator j=shape.getPathIterator(null);!j.isDone();j.next()){
			segType=j.currentSegment(segment);
			switch (segType) {
			case PathIterator.SEG_MOVETO:
				fx=segment[0];
				fy=segment[1];
				lx=fx;
				ly=fy;
			case PathIterator.SEG_LINETO:
			case PathIterator.SEG_CLOSE:
				if (Line2D.ptSegDist(lx,ly,(segType==PathIterator.SEG_CLOSE)?fx:segment[0],(segType==PathIterator.SEG_CLOSE)?fy:segment[1],x,y)<=delta)
					return true;
			break;
			}
			lx=segment[0];
			ly=segment[1];
		}
		return false;
	}





   public void updateShapes(){
   }

}
