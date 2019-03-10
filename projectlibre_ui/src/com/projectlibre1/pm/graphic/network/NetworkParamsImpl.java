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

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import com.projectlibre1.pm.graphic.graph.GraphParams;
import com.projectlibre1.pm.graphic.graph.LinkRouting;
import com.projectlibre1.pm.graphic.model.cache.NodeModelCache;
import com.projectlibre1.pm.graphic.network.layout.NetworkLayout;
import com.projectlibre1.pm.graphic.network.layout.NetworkLayoutEvent;
import com.projectlibre1.pm.graphic.network.link_routing.DefaultNetworkLinkRouting;
import com.projectlibre1.graphic.configuration.BarStyles;
import com.projectlibre1.graphic.configuration.GraphicConfiguration;

public class NetworkParamsImpl implements NetworkParams,Cloneable {
	protected AffineTransform transform;
	protected int zoom;
	protected BarStyles barStyles;
	protected GraphicConfiguration configuration;
	protected NodeModelCache cache;
	protected LinkRouting routing;
	protected NetworkLayout networkLayout;
	protected Rectangle printBounds;

	public NetworkParamsImpl(){
		configuration=GraphicConfiguration.getInstance();
		routing=new DefaultNetworkLinkRouting();
		transform=new AffineTransform();
	}

	public AffineTransform getTransform() {
		return transform;
	}

	public int getZoom() {
		return zoom;
	}

	public void setZoom(int zoom) {
		if (zoom==this.zoom) return;
		boolean in=(zoom-this.zoom)>0;
		while (this.zoom!=zoom){
			transform.concatenate(AffineTransform.getScaleInstance(barStyles.getRatioX(this.zoom,in),barStyles.getRatioY(this.zoom,in)));
			if (in) this.zoom++;
			else this.zoom--;
		}
	}

	public BarStyles getBarStyles() {
		return barStyles;
	}

	public NodeModelCache getCache() {
		return cache;
	}

	public GraphicConfiguration getConfiguration() {
		return configuration;
	}

	public LinkRouting getRouting() {
		return routing;
	}

	public void setBarStyles(BarStyles barStyles) {
		this.barStyles=barStyles;
		networkLayout.setBarStyles(barStyles);
	}

	public void setCache(NodeModelCache cache) {
		this.cache=cache;
		networkLayout.setCache(cache);
	}

	public void setConfiguration(GraphicConfiguration configuration) {
		this.configuration=configuration;
	}

	public void setRouting(LinkRouting routing) {
		this.routing=routing;
	}

	public boolean useTextures() {
		return false;
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



	public Rectangle getDrawingBounds() {
		Rectangle bounds=networkLayout.getBounds();
		GraphicConfiguration config=GraphicConfiguration.getInstance();
		return new Rectangle(bounds.x+bounds.width+config.getPertXOffset(),bounds.y+bounds.height+config.getPertYOffset());
	}

	public void layoutChanged(NetworkLayoutEvent e) {
		// TODO Auto-generated method stub

	}

	public NetworkLayout getNetworkLayout() {
		return networkLayout;
	}

	public void setNetworkLayout(NetworkLayout networkLayout) {
		this.networkLayout = networkLayout;
	}

	public void updateLayout(){
		networkLayout.updateBounds();
	}

	public Rectangle getPrintBounds() {
		return printBounds;
	}

	public void setPrintBounds(Rectangle printBounds) {
		this.printBounds = printBounds;
	}

	public int getPrintCols(){
		return (int)Math.ceil(getDrawingBounds().getWidth()/getPrintBounds().getWidth());
	}
	public int getPrintRows(){
		return (int)Math.ceil(getDrawingBounds().getHeight()/getPrintBounds().getHeight());
	}

	public Rectangle getNetworkPrintBounds(int row,int col){
		int colCount=getPrintCols();
		int rowCount=getPrintRows();
		int w,h;
		if (col==colCount-1) w=getDrawingBounds().width%getPrintBounds().width;
		else w=getPrintBounds().width;
		if (row==rowCount-1) h=getDrawingBounds().height%getPrintBounds().height;
		else h=getPrintBounds().height;
		return new Rectangle(col*printBounds.width, row*printBounds.height,w,h);
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


	public Object clone(){
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
	public GraphParams createSafePrintCopy(){
		NetworkParamsImpl c=(NetworkParamsImpl)clone();
		if (c.printBounds!=null) c.printBounds=(Rectangle)c.printBounds.clone();
		return c;
	}



}
