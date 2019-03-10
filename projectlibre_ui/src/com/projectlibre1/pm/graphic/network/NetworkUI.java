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
package com.projectlibre1.pm.graphic.network;

import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.util.List;
import java.util.ListIterator;

import com.projectlibre1.pm.graphic.graph.GraphRenderer;
import com.projectlibre1.pm.graphic.graph.GraphUI;
import com.projectlibre1.pm.graphic.graph.GraphZone;
import com.projectlibre1.pm.graphic.model.cache.GraphicNode;

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
