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
package com.projectlibre1.pm.graphic.xbs;

import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.Closure;

import com.projectlibre1.pm.graphic.model.cache.GraphicDependency;
import com.projectlibre1.pm.graphic.model.cache.GraphicNode;
import com.projectlibre1.pm.graphic.model.cache.NodeModelCache;
import com.projectlibre1.pm.graphic.model.event.CompositeCacheEvent;
import com.projectlibre1.pm.graphic.network.NetworkParams;
import com.projectlibre1.pm.graphic.network.layout.AbstractNetworkLayout;
import com.projectlibre1.graphic.configuration.BarFormat;
import com.projectlibre1.graphic.configuration.GraphicConfiguration;
import com.projectlibre1.graphic.configuration.TexturedShape;

/**
 *
 */
public class XbsLayout extends AbstractNetworkLayout {
	protected List dependencies=new ArrayList();
	public XbsLayout(NetworkParams network){
		super(network);
	}
	public List getDependencies() {
		return dependencies;
	}
	public void setCache(NodeModelCache cache){
		super.setCache(cache);
	}
	
	protected TexturedShapeFinder texturedShapeFinder=new TexturedShapeFinder();
	protected class TexturedShapeFinder implements Closure{
		protected BarFormat format;
		protected GraphicNode node;
		protected TexturedShape shape;
		void initialize(GraphicNode node) {
			this.node = node;
			shape=null;
		}
		public void execute(Object arg0) {
			format = (BarFormat)arg0;
			if (format.getMiddle()!=null)
				shape=format.getMiddle();
		}
		public TexturedShape getShape(){
			return shape;
		}
	}
	protected TexturedShape findShape(GraphicNode node){
		texturedShapeFinder.initialize(node);
		barStyles.apply(node.getNode().getImpl(),texturedShapeFinder);
		return texturedShapeFinder.getShape();
	}
	
	
	
	private void setShape(GraphicNode node,Rectangle2D ref,double centerX,double centerY){
	    TexturedShape texturedShape=findShape(node);
	    if (texturedShape==null) return;
	    GeneralPath shape=texturedShape.toGeneralPath(ref.getWidth(),ref.getHeight(),centerX-ref.getWidth()/2,centerY,null);
	    node.setXbsShape(shape,centerX,centerY);
	    Rectangle.union(bounds,network.scale(shape.getBounds()),bounds);
	}
	
	protected int updateBounds(Point2D origin,Rectangle2D ref){//cache in current version isn't a tree
		double x=origin.getX()+ref.getWidth()/2;
		double y=origin.getY()+ref.getHeight()/2;
		GraphicNode node,previous=null;
		int maxLevel=0;
		for (ListIterator i=cache.getIterator();i.hasNext();){
			node=(GraphicNode)i.next();
			if (node.getLevel()>maxLevel) maxLevel=node.getLevel();
			if (previous!=null&&node.getLevel()<=previous.getLevel()){
				setShape(previous,ref,x,y+(previous.getLevel()-1)*(ref.getMaxY()));
				x+=ref.getMaxX();
			}
			previous=node;
		}
		if (previous!=null){
			setShape(previous,ref,x,y+(previous.getLevel()-1)*(ref.getMaxY()));
		}
		return maxLevel;
	}

	protected void updateBounds(int level,Point2D origin,Rectangle2D ref){//cache in current version isn't a tree
		double y=origin.getY()+ref.getHeight()/2+ref.getMaxY()*(level-1);
		Point2D childCenter,center;
		double x0,x1;
		GraphicNode node,child;
		boolean hasChild;
		for (ListIterator i=cache.getIterator();i.hasNext();){
			node=(GraphicNode)i.next();
			if (node.getLevel()==level){
				x0=-1;
				x1=-1;
				hasChild=false;
				while (i.hasNext()){
					child=(GraphicNode)i.next();
					if (child.getLevel()<=level){
						i.previous();
						break;
					}else if (child.getLevel()==level+1){
						hasChild=true;
						childCenter=child.getXbsCenter();
						if (x0==-1||childCenter.getX()<x0) x0=childCenter.getX();
						if (x1==-1||childCenter.getX()>x1) x1=childCenter.getX();
						dependencies.add(new GraphicDependency(node,child,null));
					}
				}
				if (hasChild) setShape(node,ref,(x0+x1)/2,y);
			}
			
		}
	}
	
	
	public void updateBounds(){	    
		GraphicConfiguration config=GraphicConfiguration.getInstance();
		
		Point2D origin=new Point2D.Double(config.getTreeXOffset(),config.getTreeYOffset());
		Rectangle2D ref=new Rectangle2D.Double(config.getTreeXOffset(),config.getTreeYOffset(),config.getTreeCellWidth(),config.getTreeCellHeight());
		setEmpty();
		dependencies.clear();
		
		bounds.setFrame(0.0,0.0,0.0,0.0);
		
		int maxLevel=updateBounds(origin,ref);
		if (maxLevel==0) return;
		for (int level=maxLevel-1;level>0;level--) updateBounds(level,origin,ref);
		
		fireLayoutChanged();
	}
	
	public void graphicNodesCompositeEvent(CompositeCacheEvent compositeEvent) {
    	if (!compositeEvent.isNodeHierarchy()) return;
		updateBounds();
	}
}
