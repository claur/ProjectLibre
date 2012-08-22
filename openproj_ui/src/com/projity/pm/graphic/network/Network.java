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
package com.projity.pm.graphic.network;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import javax.swing.JViewport;

import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.pm.graphic.graph.Graph;
import com.projity.pm.graphic.graph.GraphParams;
import com.projity.pm.graphic.graph.LinkRouting;
import com.projity.pm.graphic.network.layout.NetworkLayout;
import com.projity.pm.graphic.network.layout.NetworkLayoutEvent;
import com.projity.pm.graphic.network.layout.NetworkLayoutListener;
import com.projity.pm.graphic.network.link_routing.DefaultNetworkLinkRouting;
import com.projity.pm.task.Project;
import com.projity.workspace.SavableToWorkspace;
import com.projity.workspace.WorkspaceSetting;

/**
 *
 */
public class Network extends Graph implements NetworkLayoutListener, NetworkParams, SavableToWorkspace{
	private static final long serialVersionUID = -7976852605189565105L;
	protected AffineTransform transform;
    protected int zoom;

	public Network(Project project,String viewName) {
		this(new NetworkModel(project,viewName),project);
		transform=new AffineTransform();
	}
	protected Network(NetworkModel model, Project project) {
		super(model,project);
	}



	public void updateSize(){
		Rectangle bounds=((NetworkModel)getModel()).getBounds();
		GraphicConfiguration config=GraphicConfiguration.getInstance();
		setPreferredSize(new Dimension(bounds.x+bounds.width+config.getPertXOffset(),bounds.y+bounds.height+config.getPertYOffset()));
	}

	public void layoutChanged(NetworkLayoutEvent e){
		updateSize();
		revalidate();
		repaint();
	}

	public void zoomIn(){
		if (zoom==barStyles.getMaxZoom()) return;
		((NetworkUI)ui).resetForms();
		transform.concatenate(AffineTransform.getScaleInstance(barStyles.getRatioX(zoom,true),barStyles.getRatioY(zoom++,true)));
		((NetworkModel)getModel()).updateCellBounds();
	}
	public void zoomOut(){
		if (zoom==barStyles.getMinZoom()) return;
		((NetworkUI)ui).resetForms();
		transform.concatenate(AffineTransform.getScaleInstance(barStyles.getRatioX(zoom,false),barStyles.getRatioY(zoom--,false)));
		((NetworkModel)getModel()).updateCellBounds();
	}
	public boolean canZoomIn() {
		return zoom!=barStyles.getMaxZoom();
	}
	public boolean canZoomOut() {
		return zoom!=barStyles.getMinZoom();
	}
	public AffineTransform getTransform() {
		return transform;
	}
	public int getZoom() {
		return zoom;
	}

	public GeneralPath scale(GeneralPath path){
		if (path == null)
			return null;
		GeneralPath transformed=(GeneralPath)path.clone();
		transformed.transform(getTransform());
		return transformed;
	}
	public Point2D scale(Point2D p){
		AffineTransform t=getTransform();
		return new Point2D.Double(p.getX()*t.getScaleX()+t.getTranslateX(),p.getY()*t.getScaleY()+t.getTranslateY());
	}
	public Point2D scaleVector(Point2D p){
		AffineTransform t=getTransform();
		return new Point2D.Double(p.getX()*t.getScaleX(),p.getY()*t.getScaleY());
	}
	public Point2D scaleVector_1(Point2D p){
		AffineTransform t=getTransform();
		return new Point2D.Double(p.getX()/t.getScaleX(),p.getY()/t.getScaleY());
	}

	public Rectangle scale(Rectangle r){
		AffineTransform t=getTransform();
		if (t==null) return r;
		Rectangle sr=new Rectangle();
		sr.setFrameFromDiagonal(
				r.getMinX()*t.getScaleX()+t.getTranslateX(),
				r.getMinY()*t.getScaleY()+t.getTranslateY(),
				r.getMaxX()*t.getScaleX()+t.getTranslateX(),
				r.getMaxY()*t.getScaleY()+t.getTranslateY()
			);
		return sr;
	}

   	protected LinkRouting routing=new DefaultNetworkLinkRouting();
	public LinkRouting getRouting(){
		return routing;
	}
	public void setRouting(LinkRouting routing) {
		this.routing=routing;
	}
	private void makeZoom(int newZoom) {
		int factor = newZoom - zoom;
		if (factor > 0) {
			for (int i =0; i < factor; i++)
				zoomIn();
		} else {
			for (int i =0; i > factor; i--)
				zoomOut();
		}
	}
	public boolean useTextures() {
		return true;
	}
	public void restoreWorkspace(WorkspaceSetting w, int context) {
		Workspace ws = (Workspace) w;
		makeZoom(ws.zoom);
     	Container p = getParent();
     	if (p instanceof JViewport && ws.viewPosition != null) {
     		try {
     		((JViewport)p).setViewPosition(ws.viewPosition);
     		} catch (RuntimeException e) {
     			System.out.println("problem restoring viewport to point " + ws.viewPosition);
     		}
     	}

	}
	public WorkspaceSetting createWorkspace(int context) {
		Workspace ws = new Workspace();
		ws.zoom = zoom;
     	Container p = getParent();
     	if (p instanceof JViewport) {
     		ws.viewPosition = ((JViewport)p).getViewPosition();
     	}
		return ws;
	}
	public static class Workspace implements WorkspaceSetting  {
		private static final long serialVersionUID = 7804032466144588065L;
		int zoom;
		Point viewPosition = null;
		public int getZoom() {
			return zoom;
		}

		public void setZoom(int zoom) {
			this.zoom = zoom;
		}

		public Point getViewPosition() {
			return viewPosition;
		}

		public void setViewPosition(Point viewPosition) {
			this.viewPosition = viewPosition;
		}
	}
	public Rectangle getPrintBounds() {
		return null;
	}
	public void setPrintBounds(Rectangle printBounds) {
	}
	public int getPrintCols() {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getPrintRows() {
		// TODO Auto-generated method stub
		return 0;
	}
	public NetworkLayout getNetworkLayout() {
		// TODO Auto-generated method stub
		return ((NetworkModel)getModel()).getNetworkLayout();
	}
	public boolean isLeftPartVisible() {
		return true;
	}
	public boolean isRightPartVisible() {
		return true;
	}
	public void setLeftPartVisible(boolean visible){}
	public void setRightPartVisible(boolean visible){}
	public boolean isSupportLeftAndRightParts(){return false;}
	public void setSupportLeftAndRightParts(boolean supports){}
	public GraphParams createSafePrintCopy() {return this;}


}
