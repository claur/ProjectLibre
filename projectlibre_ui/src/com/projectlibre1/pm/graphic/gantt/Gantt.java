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
package com.projectlibre1.pm.graphic.gantt;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JViewport;

import com.projectlibre1.pm.graphic.gantt.link_routing.DefaultGanttLinkRouting;
import com.projectlibre1.pm.graphic.graph.Graph;
import com.projectlibre1.pm.graphic.graph.GraphParams;
import com.projectlibre1.pm.graphic.graph.GraphUI;
import com.projectlibre1.pm.graphic.graph.LinkRouting;
import com.projectlibre1.pm.graphic.network.NetworkParamsImpl;
import com.projectlibre1.pm.graphic.timescale.CoordinatesConverter;
import com.projectlibre1.pm.graphic.timescale.ScaledComponent;
import com.projectlibre1.pm.task.Project;
import com.projectlibre1.pm.time.HasStartAndEnd;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.timescale.TimeScaleEvent;
import com.projectlibre1.timescale.TimeScaleListener;

/**
 *
 */
public class Gantt extends Graph implements ScaledComponent, TimeScaleListener, GanttParams{
//    protected GanttPopupMenu popup;

//    protected DependencyDialog dependencyPropertiesDialog;

	private static final long serialVersionUID = -1806070019043393474L;
	public Gantt(Project project,String viewName) {
		this(new GanttModel(project,viewName),project);
	}
	protected Gantt(GanttModel model, Project project) {
		super(model,project);
		this.setToolTipText(Messages.getString("Text.rightClickForOptions"));

	}

	public void cleanUp() {
		CoordinatesConverter c = getCoord();
    	if (c!= null) {
    		c.removeTimeScaleListener(this);
        	c.removeTimeScaleListener((GanttModel)model);
    	}
    	super.cleanUp();
	}

	public void updateUI() {
		setUI(new GanttUI(this));
		invalidate();
	}

//	public GanttPopupMenu getPopup() {
//		return popup;
//	}



     public CoordinatesConverter getCoord() {
        return ((GanttModel)model).getCoord();
    }
     public void setCoord(CoordinatesConverter coord) {
     	CoordinatesConverter modelCoord=getCoord();
        if (modelCoord!=null)
        	modelCoord.removeTimeScaleListener(this);
		coord.addTimeScaleListener(this);
		((GanttModel)model).setCoord(coord);
     }
 	public void timeScaleChanged(TimeScaleEvent e) {
 		updateSize();
// 		Component p;
// 		if ((p=getParent()) instanceof JViewport){
// 			//JViewport vp=(JViewport)p;
// 	 		if ((p=p.getParent()) instanceof ScaledScrollPane){
// 	 			ScaledScrollPane scp=(ScaledScrollPane)p;
// 	 			scp.updateTimeScaleComponentSize();
// 	 		}
//
// 		}
 	}



	public int getRow(double y){
		//double row=y/((double)config.getRowHeight());
		double row=y/((double)getRowHeight());
		return (int)row;
	}


	public int getRowHeight(){
	    return ((GanttModel)model).getRowHeight();
	}
	public void setRowHeight(int rowHeight){
		((GanttModel)model).setRowHeight(rowHeight);
	}
//	public int getColumnHeaderHeight() {
//		return ((GanttModel)model).getColumnHeaderHeight();
//	}
//	public void setColumnHeaderHeight(int columnHeaderHeight) {
//		((GanttModel)model).setColumnHeaderHeight(columnHeaderHeight);
//	}
	public Font getColumnHeaderFont() {
		return null;
	}
	public void setColumnHeaderFont(Font columnHeaderFont) {
	}

   	protected LinkRouting routing=new DefaultGanttLinkRouting();//new QuadraticGanttLinkRouting();
	public LinkRouting getRouting(){
		return routing;
	}
	public void setRouting(LinkRouting routing) {
		this.routing=routing;
	}



//	public void setDrawingBounds(Rectangle bounds) {
//		// TODO Auto-generated method stub
//
//	}
	public void updateSize(){
//		Component c=this;
//		while ((c=c.getParent())!=null&&(!(c instanceof JViewport)));
//		if (c instanceof JViewport){
//			JViewport v=(JViewport)c;
//			v.setViewSize(new Dimension((int)Math.ceil(getCoord().getWidth()),v.getViewSize().height));
//		}
		((GraphUI)ui).updateShapes();

		Component c;
		if ((c=getParent()) instanceof JViewport){
			JViewport vp=(JViewport)c;
			vp.setViewSize(new Dimension((int)Math.ceil(getCoord().getWidth()),vp.getViewSize().height));
		}

		setPreferredSize(new Dimension((int)Math.ceil(getCoord().getWidth()),getPreferredSize().height));

		revalidate();
	}

	public Rectangle getGanttBounds(){
		return getDrawingBounds();
	}
	public boolean useTextures() {
		return true;
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


	public void scrollToTask(HasStartAndEnd interval,boolean automatic){
		CoordinatesConverter coord=getCoord();
		double start=coord.toX(interval.getStart());
		double end=coord.toX(interval.getEnd());
		Rectangle visible=getVisibleRect();
		if (automatic&&(
				(start>=visible.x&&start<=visible.x+visible.width)||
				(end>=visible.x&&end<=visible.x+visible.width)||
				(start<visible.x&&end>visible.x+visible.width)))
			return; //already visible

		Component c;
		if ((c=getParent()) instanceof JViewport){
			JViewport vp=(JViewport)c;
			Point p=vp.getViewPosition();
			if (start</*(visible.width/3)*/150) p.x=0;
			else{
				p.x=(int)Math.ceil(start)-50; //3 days 1/3
				if (p.x<0) p.x=0;
			}
			vp.setViewPosition(p);
		}
		//scrollRectToVisible(visible);
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
	public GraphParams createSafePrintCopy(){return this;}


}
