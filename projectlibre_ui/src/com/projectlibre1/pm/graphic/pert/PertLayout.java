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
package com.projectlibre1.pm.graphic.pert;

import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import org.apache.commons.collections.Closure;

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
public class PertLayout extends AbstractNetworkLayout {
	protected DependencyGraph dependencyGraph=new DependencyGraph();
	public PertLayout(NetworkParams network){
		super(network);
	}
	public void setCache(NodeModelCache cache){
		super.setCache(cache);
		dependencyGraph.setCache(cache);
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
	
	public void updateBounds(){
	    dependencyGraph.updatePertLevels();
	    
		GraphicConfiguration config=GraphicConfiguration.getInstance();
		
		Point2D origin=new Point2D.Double(config.getPertXOffset(),config.getPertYOffset());
		Rectangle2D ref=new Rectangle2D.Double(config.getPertXOffset(),config.getPertYOffset(),config.getPertCellWidth(),config.getPertCellHeight());
		int row=0;
		int col=-1;
		setEmpty();
		for (Iterator i=cache.getIterator();i.hasNext();){
		    GraphicNode current=(GraphicNode)i.next();
		    int currentCol=cache.getPertLevel(current)-1;
		    if (currentCol<=col) row++;
		    col=currentCol;
		    
		    TexturedShape texturedShape=findShape(current);
		    if (texturedShape==null) continue;
		    double centerX=origin.getX()+ref.getMaxX()*col+ref.getWidth()/2;
		    double centerY=origin.getY()+ref.getMaxY()*row+ref.getHeight()/2;
		    //System.out.println(centerX+"/"+centerY);
		    GeneralPath shape=texturedShape.toGeneralPath(ref.getWidth(),ref.getHeight(),centerX-ref.getWidth()/2,centerY,null);
		    current.setPertShape(shape,centerX,centerY);
		    Rectangle cellBounds=network.scale(shape.getBounds());
		    if (isEmpty())
				bounds.setBounds(cellBounds);
			else Rectangle.union(bounds,cellBounds,bounds);
		}
		fireLayoutChanged();
	}
	
	public void graphicNodesCompositeEvent(CompositeCacheEvent compositeEvent) {
 
		if (compositeEvent.getRemovedEdges()!=null) dependencyGraph.removeDependencies(compositeEvent.getRemovedEdges());
	    if (compositeEvent.getInsertedEdges()!=null) dependencyGraph.insertDependencies(compositeEvent.getInsertedEdges());
		updateBounds();
	}
}
