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
package com.projity.pm.graphic.gantt;

import java.awt.Font;
import java.awt.Rectangle;
import java.io.Serializable;

import com.projity.configuration.Dictionary;
import com.projity.graphic.configuration.BarStyles;
import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.pm.graphic.gantt.link_routing.DefaultGanttLinkRouting;
import com.projity.pm.graphic.graph.GraphParams;
import com.projity.pm.graphic.graph.LinkRouting;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.timescale.CoordinatesConverter;

public class GanttParamsImpl implements GanttParams, Serializable,Cloneable {
	private static final long serialVersionUID = 2314555242629487089L;
	protected NodeModelCache cache;
	protected BarStyles barStyles;
	protected GraphicConfiguration configuration;

	protected Font columnHeaderFont;
	protected LinkRouting routing=new DefaultGanttLinkRouting();
	protected CoordinatesConverter coord;
	protected Rectangle printBounds;
	protected boolean rightPartVisible=true,leftPartVisible=true;
	protected int rowHeight;

	public GanttParamsImpl(){
		configuration=GraphicConfiguration.getInstance();
		barStyles=(BarStyles) Dictionary.get(BarStyles.category,"standard");
		columnHeaderFont=new Font("Default",Font.PLAIN,10);
		routing=new DefaultGanttLinkRouting();
		rowHeight=configuration.getRowHeight();
	}

	public GraphicConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(GraphicConfiguration configuration) {
		this.configuration = configuration;
	}

	public BarStyles getBarStyles() {
		return barStyles;
	}
	public void setBarStyles(BarStyles barStyles) {
		this.barStyles = barStyles;
	}
	public NodeModelCache getCache() {
		return cache;
	}
	public void setCache(NodeModelCache cache) {
		this.cache = cache;
	}
	public CoordinatesConverter getCoord() {
		return coord;
	}
	public void setCoord(CoordinatesConverter coord) {
		this.coord = coord;
	}
	public LinkRouting getRouting() {
		return routing;
	}
	public void setRouting(LinkRouting routing) {
		this.routing = routing;
	}
	public int getRowHeight() {
		return rowHeight;
	}
	public void setRowHeight(int rowHeight) {
		this.rowHeight=rowHeight;
	}
	public Rectangle getGanttBounds() {
		return new Rectangle(0,configuration.getColumnHeaderHeight(),(int)Math.ceil(coord.getWidth()),getRowHeight()*cache.getSize());
//		return new Rectangle(0,configuration.getColumnHeaderHeight(),(int)Math.ceil(coord.getWidth()),configuration.getRowHeight()*cache.getSize());
	}
	public Rectangle getDrawingBounds() {
		return getGanttBounds();
	}

	public Font getColumnHeaderFont() {
		return columnHeaderFont;
	}
	public void setColumnHeaderFont(Font columnHeaderFont) {
		this.columnHeaderFont = columnHeaderFont;
	}
	public boolean useTextures() {
		return false;
	}

	public Rectangle getPrintBounds() {
		return printBounds;
	}

	public void setPrintBounds(Rectangle printBounds) {
		this.printBounds = printBounds;
		updateDrawingBounds(); //remove?
	}
	public void updateDrawingBounds(){}

	public int getPrintCols(){
		return (int)Math.ceil(getGanttBounds().getWidth()/getPrintBounds().getWidth());
	}
	public int getPrintRows(){
		return (int)Math.ceil(getGanttBounds().getHeight()/getPrintBounds().getHeight());
	}

	public boolean isLeftPartVisible() {
		return leftPartVisible;
	}

	public void setLeftPartVisible(boolean leftPartVisible) {
		this.leftPartVisible = leftPartVisible;
	}

	public boolean isRightPartVisible() {
		return rightPartVisible;
	}

	public void setRightPartVisible(boolean rightPartVisible) {
		this.rightPartVisible = rightPartVisible;
	}
	protected boolean supportLeftAndRightParts=false;
	public boolean isSupportLeftAndRightParts(){
		return supportLeftAndRightParts;
	}
	public void setSupportLeftAndRightParts(boolean supports){
		this.supportLeftAndRightParts=supports;
	}

	public Object clone(){
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
	public GraphParams createSafePrintCopy(){
		GanttParamsImpl c=(GanttParamsImpl)clone();
		if (c.printBounds!=null) c.printBounds=(Rectangle)c.printBounds.clone();
		return c;
	}


}
