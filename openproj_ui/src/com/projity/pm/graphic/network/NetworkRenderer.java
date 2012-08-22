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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.ListIterator;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;

import org.apache.commons.collections.Closure;

import com.projity.graphic.configuration.BarFormat;
import com.projity.graphic.configuration.BarStyles;
import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.graphic.configuration.TexturedShape;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.graphic.graph.GraphParams;
import com.projity.pm.graphic.graph.GraphRenderer;
import com.projity.pm.graphic.graph.LinkRouting;
import com.projity.pm.graphic.model.cache.GraphicDependency;
import com.projity.pm.graphic.model.cache.GraphicNode;
import com.projity.pm.graphic.network.link_routing.NetworkLinkRouting;
import com.projity.pm.graphic.network.rendering.NetworkCellEditor;
import com.projity.pm.graphic.network.rendering.NetworkCellRenderer;

public abstract class NetworkRenderer extends GraphRenderer{
	protected LinkRenderer linkRenderer;
	protected NetworkCellRenderer renderer;
	protected NetworkCellEditor editor;
	protected boolean vertical=true;
	protected CellRendererPane rendererPane;
	protected JComponent container;
	
	protected GraphicConfiguration config;

	
	
	public NetworkRenderer(){
		super();
		init();
	}
	public NetworkRenderer(GraphParams graphInfo){
		super(graphInfo);
		init();
	}
	
	public void init(){
		GraphParams graphInfo=getGraphInfo();
		if (graphInfo instanceof JComponent)
			container=(JComponent)graphInfo;
		config=GraphicConfiguration.getInstance();
		linkRenderer = new LinkRenderer();
		renderer=new NetworkCellRenderer(graphInfo);
		if (container!=null){
			editor=new NetworkCellEditor(graphInfo,container);
			rendererPane=new CellRendererPane();
			container.add(rendererPane);
		}
	}


	public boolean isVertical() {
		return vertical;
	}
	public void setVertical(boolean vertical) {
		this.vertical = vertical;
		((NetworkLinkRouting)getRouting()).setVertical(vertical);
	}
	

	private class LinkRenderer implements Closure{
		protected BarFormat format;
		protected GraphicDependency dependency;
		protected Graphics2D g2;
		void initialize(Graphics2D g2, GraphicDependency dependency) {
			this.g2 = g2;
			this.dependency = dependency;
		}


		
		public void execute(Object arg0) {
			format = (BarFormat)arg0;
			
			//if (format.getMiddle()!=null){
			    GraphicNode from=dependency.getPredecessor();
			    GraphicNode to=dependency.getSuccessor();
//			    Rectangle2D fromBounds=scale(getBounds(from));
//			    Rectangle2D toBounds=scale(getBounds(to));
			    double[] fromPoints=new double[4];
			    double[] toPoints=new double[4];
			    updateLinkConnections(from,fromPoints);
			    updateLinkConnections(to,toPoints);
			    Point2D fromCenter=getCenter(from);
			    Point2D toCenter=getCenter(to);
			    if (fromCenter == null) //TODO SUB this prevents crashing with external tasks, however, now they won't show up in the PERT
			    	return;
			    
				GeneralPath path=dependency.getPath();
				NetworkLinkRouting routing=(NetworkLinkRouting)getRouting();
				if (vertical) routing.routePath(path,fromCenter.getX(),fromPoints[3],toCenter.getX(),toPoints[2],(fromPoints[3]+toPoints[2])/2,dependency.getType());
				else routing.routePath(path,fromPoints[1],fromCenter.getY(),toPoints[0],toCenter.getY(),(fromPoints[1]+toPoints[0])/2,dependency.getType());
				
				
				
				Color oldColor=g2.getColor();
				Stroke oldStroke = g2.getStroke();
				Dependency dep = dependency.getDependency();
				if (dep != null && dep.isDisabled())
					g2.setStroke(GraphRenderer.DISABLED_LINK_STROKE);
				if (dep != null && dep.isCrossProject())
					g2.setColor(GraphRenderer.EXTERNAL_LINK_COLOR);
				else
					g2.setColor(format.getMiddle().getColor());
				g2.draw(path);
				
			//}
			if (format.getStart()==null&&format.getEnd()==null) return;
			if (format.getStart()!=null){
				double theta=routing.getFirstAngle();
				AffineTransform transform=(theta==0)?null:AffineTransform.getRotateInstance(theta,routing.getFirstX(),routing.getFirstY());
				drawLinkArrows(dep,transform,format.getEnd());
			}
			if (format.getEnd()!=null){
				double theta=routing.getLastAngle();
				AffineTransform transform=(theta==Math.PI||theta==-Math.PI)?null:AffineTransform.getRotateInstance(Math.PI-theta,routing.getLastX(),routing.getLastY());
				drawLinkArrows(dep,transform,format.getEnd());
			}
			if (oldColor!=null) g2.setColor(oldColor);
			if (oldStroke!= null) g2.setStroke(oldStroke);

		}
		private void drawLinkArrows(Dependency dep, AffineTransform transform, TexturedShape shape) {
			Color oldEndColor = format.getEnd().getColor();
			if (dep != null && dep.isCrossProject())
				shape.setPaint(GraphRenderer.EXTERNAL_LINK_COLOR);
			g2.setColor(shape.getColor());
			LinkRouting routing=getRouting();
			shape.draw(g2,routing.getLastX(),routing.getLastY(),transform,useTextures());
			if (dep != null && dep.isCrossProject())
				shape.setPaint(oldEndColor);
		}

	}

	public void paintLink(Graphics2D g2, GraphicDependency dependency){
		BarStyles barStyles = graphInfo.getBarStyles();
		linkRenderer.initialize(g2,dependency);
		barStyles.apply(dependency,linkRenderer,true,false,false, false);
	}

	
	
	protected GeneralPath getShape(GraphicNode node){
		return scale(getNonScaledShape(node));
	}
	protected abstract GeneralPath getNonScaledShape(GraphicNode node);
	protected abstract void translateNonScaledShape(GraphicNode node, double dx,double dy);
	protected void translateShape(GraphicNode node, double dx,double dy){
		Point2D v=scaleVector_1(new Point2D.Double(dx,dy));
		translateNonScaledShape(node,v.getX(),v.getY());
	}
	protected abstract Point2D getNonScaledCenter(GraphicNode node);
	protected Point2D getCenter(GraphicNode node){
		return scale(getNonScaledCenter(node));
	}
	
	protected Rectangle getBounds(GraphicNode node){
		GeneralPath shape=getShape(node);
		if (shape==null) return null;
		else return shape.getBounds();
	}
	protected Rectangle getNonScaledBounds(GraphicNode node){
		GeneralPath shape=getNonScaledShape(node);
		if (shape==null) return null;
		else return shape.getBounds();
	}
    protected double[] segment=new double[6];
	protected void updateLinkConnections(GraphicNode node,double[] linkPoints){
		GeneralPath shape=getShape(node);
		if (shape==null) return;
		Point2D center=getCenter(node);
		linkPoints[0]=center.getX();
		linkPoints[1]=center.getX();
		linkPoints[2]=center.getY();
		linkPoints[3]=center.getY();
		double x0=0.0,y0=0.0,x1=0.0,y1=0.0,x2=0.0,y2=0.0,x,y;
		for (PathIterator j=shape.getPathIterator(null);!j.isDone();j.next()){
			int segmentType=j.currentSegment(segment);
			switch (segmentType) {
				case PathIterator.SEG_MOVETO:
					x0=segment[0];
					y0=segment[1];
					x2=x0;
					y2=y0;
					break;
				case PathIterator.SEG_LINETO:
					x2=segment[0];
					y2=segment[1];
				case PathIterator.SEG_CLOSE:
					if (segmentType==PathIterator.SEG_CLOSE){
						x2=x0;
						y2=y0;
					}
					//works only convex shapes
					double lambda;
					if (y2!=y1){
						x=(center.getY()-y1)*(x2-x1)/(y2-y1)+x1;
						lambda=(x2==x1)?0:(x-x1)/(x2-x1);
						if (x1==x2||(lambda>=0&&lambda<=1)){
							if (x<linkPoints[0]) linkPoints[0]=x;
							if (x>linkPoints[1]) linkPoints[1]=x;
						}
					}
					if (x2!=x1){
						y=(center.getX()-x1)*(y2-y1)/(x2-x1)+y1;
						lambda=(y2==x1)?0:(y-y1)/(y2-y1);
						if (y1==y2||(lambda>=0&&lambda<=1)){
							if (y<linkPoints[2]) linkPoints[2]=y;
							if (y>linkPoints[3]) linkPoints[3]=y;
						}
					}

					break;
			}
			x1=x2;
			y1=y2;
		}
	}

    public void paint(Graphics g) {
    	paint(g,null);
    }
    public void paint(Graphics g,Rectangle visibleBounds) {
	    	Graphics2D g2=(Graphics2D)g;

			Rectangle clipBounds = g2.getClipBounds();
			Rectangle svgClip=clipBounds;
			if (clipBounds==null){
				clipBounds=getGraphInfo().getDrawingBounds();
				//start at O,O because it's already translated
				if (visibleBounds==null) clipBounds=new Rectangle(0,0,clipBounds.width,clipBounds.height);
				else {
					clipBounds=visibleBounds;
					g2.setClip(clipBounds);
				}
			}
			//Modif for offline graphics
			
			GraphicDependency dependency;
			for (Iterator i=getDependenciesIterator();i.hasNext();){
				dependency=(GraphicDependency)i.next();
				paintLink(g2,dependency);
			}
			
			
			GraphicNode node;
			Rectangle bounds;
			for (ListIterator i=graphInfo.getCache().getIterator();i.hasNext();){
				node=(GraphicNode)i.next();
				bounds=getBounds(node);
				if (bounds==null) continue;
				if (clipBounds.intersects(bounds))
					paintNode(g2,node);
			}
			
			if (visibleBounds!=null) g2.setClip(svgClip);
	   }
	   
	   

		public GeneralPath scale(GeneralPath path){
			return ((NetworkParams)graphInfo).scale(path);
		}
		public Point2D scale(Point2D p){
			if (p == null)
				return null;
			return ((NetworkParams)graphInfo).scale(p);
		}
		public Point2D scaleVector(Point2D p){
			return ((NetworkParams)graphInfo).scaleVector(p);
		}
		public Point2D scaleVector_1(Point2D p){
			return ((NetworkParams)graphInfo).scaleVector_1(p);
		}
		
		public Rectangle scale(Rectangle r){
			return ((NetworkParams)graphInfo).scale(r);
		}
		
		public void paintNode(Graphics2D g,GraphicNode node){
			Rectangle bounds=getBounds(node);
			if (isEditing(node)){
				editor.paintEditor(node);
			}else{
				JComponent c=renderer.getRendererComponent(node,((NetworkParams)graphInfo).getZoom());
				if (container==null){
					//c=new JLabel("test");
			    	c.setDoubleBuffered(false);
			    	c.setOpaque(false);
			    	c.setForeground(Color.BLACK);
					c.setSize(bounds.width, bounds.height);
			    	g.translate(bounds.x,bounds.y);
			    	c.doLayout();
			    	c.print(g);
			    	g.translate(-bounds.x,-bounds.y);
				}
				else rendererPane.paintComponent(g,c,container,bounds.x,bounds.y,bounds.width,bounds.height,true);
			}
		}

		public void resetForms(){
			renderer.resetForms();
			if (editor!=null) editor.resetForms();
		}
		public boolean isEditing(GraphicNode node) {
			if (editor==null) return false;
			return editor.isEditing(node);
		}


		public NetworkCellEditor getEditor() {
			return editor;
		}

		   public Iterator getDependenciesIterator(){
		   		return graphInfo.getCache().getEdgesIterator();
		   }

}
