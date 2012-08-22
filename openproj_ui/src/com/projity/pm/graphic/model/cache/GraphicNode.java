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
package com.projity.pm.graphic.model.cache;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import com.projity.functor.IntervalConsumer;
import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.grouping.core.GroupNodeImpl;
import com.projity.grouping.core.LazyParent;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.transform.HierarchicObject;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.graphic.timescale.CoordinatesConverter;
import com.projity.pm.scheduling.Schedule;
import com.projity.pm.scheduling.ScheduleInterval;
import com.projity.pm.scheduling.ScheduleService;
import com.projity.pm.task.Task;
import com.projity.server.data.CommonDataObject;
import com.projity.server.data.DataObject;
/**
 *
 */
public class GraphicNode implements HierarchicObject{
	protected Node node;
	protected int level;
	protected int pertLevel;
	protected boolean voidNode;
	protected boolean composite;
	protected boolean summary;
	protected boolean collapsed;
	protected boolean dirty;



	/**
	 * @param node
	 * @param level
	 */
	public GraphicNode(Node node, int level) {
		setNode(node);
		this.level = level;
		dirty=false;
		pertLevel=-1;
		setScheduleCaching(false);
	}

	//Not real GraphicNode this is a hack for Depencencies. This is to modify
	/*public GraphicNode(Node node) {
		setNode(node);
		level = -1;
		ganttCell=null;
		pertCell=null;
		treeCell=null;
		dirty=false;
		pertLevel=-1;
	}*/


	/*public ReferenceNodeModelCache getCache() {
		return cache;
	}*/
	/**
	 * @return Returns the level.
	 */
	public int getLevel() {
		return level;
	}
	/**
	 * @param level The level to set.
	 */
	void setLevel(int level) {
		this.level = level;
		dirty=true;
	}

    public int getPertLevel() {
        return pertLevel;
    }
    void setPertLevel(int pertLevel) {
        this.pertLevel = pertLevel;
    }
	/**
	 * @return Returns the node.
	 */
	public Node getNode() {
		return node;
	}
	/**
	 * @param node The node to set.
	 */
	public void setNode(Node node) {
		this.node = node;
		dirty=true;
	}

	/**
	 * @return Returns the composite.
	 */
	public  boolean isComposite() {
		return composite;
	}
	/**
	 * @param composite The composite to set.
	 */
	public void setComposite(boolean composite) {
		this.composite = composite;
		dirty=true;
	}

	public boolean isSummary() {
		return summary;
	}
	public void setSummary(boolean summary) {
		this.summary = summary;
		dirty=true;
	}
	public boolean isLazyParent() {
		return node.getImpl() instanceof LazyParent;
	}
	public boolean isValidLazyParent() {
		if (node.getImpl() instanceof LazyParent)
			return ((LazyParent)node.getImpl()).isValid();
		return false;
	}
	public boolean isFetched() {
		if (node.getImpl() instanceof LazyParent)
			return ((LazyParent)node.getImpl()).isDataFetched();
		else
			return true;
	}
	public boolean fetch() {
		if (node.getImpl() instanceof LazyParent)
			return ((LazyParent)node.getImpl()).fetchData(node);
		return true;
	}
	/**
	 * @return Returns the collapsed.
	 */
	public boolean isCollapsed() {
		return collapsed;
	}
	/**
	 * @param collapsed The collapsed to set.
	 */
	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
		dirty=true;
	}

	public boolean isVoid(){
	    return voidNode;//getNode().isVoid();
	}
	public void setVoid(boolean voidNode) {
		this.voidNode=voidNode;
		dirty=true;
	}


	public boolean isAssignment(){
	    return getNode().getImpl() instanceof Assignment;
	}
	public boolean isGroup(){
	    return getNode().getImpl() instanceof GroupNodeImpl;
	}
	public int getSubprojectLevel(){
//		if (getNode().getImpl() instanceof Task){
//			Task task=(Task)getNode().getImpl();
//			if (task.isInSubproject()) return 1;
//		}

//		Node node=getNode();
//		int level=0;
//		while (node.isInSubproject()){
//			node=(Node)node.getParent();
//			level+=1;
//		}
//		return level;

		//if (getNode().isInSubproject()) return 1;
		//return 0;

		return node.getSubprojectLevel();
	}

	public boolean isLinkable() {
		Object impl = getNode().getImpl();
		if (impl instanceof Assignment)
			return false;
		if (impl instanceof Task && ((Task)impl).isExternal())
			return false;
		return true;
	}
	public boolean isServer(){
	    Object impl=getNode().getImpl();
	    if (!(impl instanceof DataObject)) return false;
	    return !CommonDataObject.isLocal((DataObject)impl);
	}


	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
//		System.out.println("GraphicNode _setDirty");
		this.dirty = dirty;
	}

	public String toString(){
	    return node.toString();
	}


	public static Object getImpl(Object obj) {
		if (obj instanceof GraphicNode)
			return ((GraphicNode)obj).getNode().getImpl();
		else if (obj instanceof Node)
			return ((Node)obj).getImpl();
		else
			return obj;
	}

	public static boolean isVoid(Object obj) {
		if (obj instanceof GraphicNode)
			return ((GraphicNode)obj).isVoid();
		else if (obj instanceof Node)
			return ((Node)obj).isVoid();
		else
			return obj == null;
	}



	protected boolean scheduleCaching;
	protected ArrayList intervals=null;
	protected long start=-1;
	protected long end=-1;
	protected int intervalCount=1;
	//TODO add recurrent tasks support

	public long getStart(){
		return (scheduleCaching||!isSchedule())?start:((Schedule)node.getImpl()).getStart();
	}
	public long getEnd(){
		return (scheduleCaching||!isSchedule())?end:((Schedule)node.getImpl()).getEnd();
	}

	public int getIntervalCount() {
		return intervalCount;
	}

	public boolean isScheduleCaching() {
		return scheduleCaching;
	}
	public void setScheduleCaching(boolean scheduleCaching) {
		this.scheduleCaching = scheduleCaching;
		intervals=(scheduleCaching)?new ArrayList():null;
		ContainsIntervalConsumer containsConsumer=null;//clean if it wasn't scheduleCaching before
	}

	public void updateScheduleCache(){
		if (scheduleCaching || GraphicConfiguration.getInstance().getGanttBarMinWidth()>0){
			Object impl=node.getImpl();
			if (!isSchedule()) return;
			intervalConsumer.initCache(this,intervals);
			ScheduleService.getInstance().consumeIntervals((Schedule)impl,intervalConsumer);
			intervalCount=intervalConsumer.size>0?intervalConsumer.size:1;
		}
	}
	protected static CacheIntervalConsumer intervalConsumer=new CacheIntervalConsumer();
	protected static class CacheIntervalConsumer implements IntervalConsumer{
		protected List cache=null;
		protected GraphicNode gnode=null;
		int size;
		public void initCache(GraphicNode gnode,List cache){
			size=0;
			if (cache!=null) cache.clear();
			this.cache=cache;
			this.gnode=gnode;
		}
		public void consumeInterval(ScheduleInterval interval){
			if (size++==0) gnode.start=interval.getStart();
			gnode.end=interval.getEnd();
			if (cache!=null) cache.add(interval);
		}
	}

	public void consumeIntervals(IntervalConsumer consumer) {
		if (scheduleCaching){
			for (Iterator i=intervals.iterator();i.hasNext();){
				consumer.consumeInterval((ScheduleInterval)i.next());
			}
		}else{
			Object impl=node.getImpl();
			if (isSchedule()) ScheduleService.getInstance().consumeIntervals((Schedule)impl,consumer);
		}
	}



	//contains
	private ContainsIntervalConsumer containsConsumer=null; //need when no schedule caching
	private static class ContainsIntervalConsumer implements IntervalConsumer{
		ScheduleInterval interval=null;
		double t,deltaT1,deltaT2;
		CoordinatesConverter coord;
		GraphicNode node;
		public void init(double t,double deltaT1,double deltaT2,CoordinatesConverter coord,GraphicNode node){
			interval=null;
			this.t=t;
			this.deltaT1=deltaT1;
			this.deltaT2=deltaT2;
			this.coord=coord;
			this.node=node;
		}
		public ScheduleInterval getInterval(){
			return interval;
		}
		public void consumeInterval(ScheduleInterval interval){
			if (coord!=null) interval=coord.adaptSmallBarTimeInterval(interval, node, null);
			if (t>=interval.getStart()-deltaT1&&t<=interval.getEnd()+deltaT2) this.interval=interval;
		}

	}
	public ScheduleInterval contains(double t,double deltaT1,double deltaT2,CoordinatesConverter coord){
		if (scheduleCaching){
			ScheduleInterval interval;
			for (Iterator i=intervals.iterator();i.hasNext();){
				interval=(ScheduleInterval)i.next();
				if (coord!=null) interval=coord.adaptSmallBarTimeInterval(interval, this, null);
				if (t>=interval.getStart()-deltaT1&&t<=interval.getEnd()+deltaT2) return interval;
			}
			return null;
		}else{
			if (containsConsumer==null) containsConsumer=new ContainsIntervalConsumer();
			containsConsumer.init(t,deltaT1,deltaT2,coord,this);
			Object impl=node.getImpl();
			if (isSchedule()) ScheduleService.getInstance().consumeIntervals((Schedule)impl,containsConsumer);
			return containsConsumer.getInterval();
		}
	}
//	public boolean contains(double t,CoordinatesConverter coord){
//		return contains(t,0,0,coord)!=null;
//	}




	public boolean isSchedule(){
		return node.getImpl() instanceof Schedule;
	}



	protected double ganttShapeOffset=0,ganttShapeHeight=GraphicConfiguration.getInstance().getGanttBarHeight();;
	public double getGanttShapeHeight() {
		return ganttShapeHeight;
	}
	public void setGanttShapeHeight(double ganttShapeHeight) {
		this.ganttShapeHeight = ganttShapeHeight;
	}
	public double getGanttShapeOffset() {
		return ganttShapeOffset;
	}
	public void setGanttShapeOffset(double ganttShapeOffset) {
		this.ganttShapeOffset = ganttShapeOffset;
	}


	protected int row; //tmp value for performance reasons
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}
	protected GeneralPath pertShape=null;
	protected GeneralPath xbsShape=null;
	protected Point2D pertCenter=null;
	protected Point2D xbsCenter=null;
	public GeneralPath getPertShape() {
		return pertShape;
	}
	public void setPertShape(GeneralPath pertShape,double centerX, double centerY) {
		this.pertShape = pertShape;
		if (pertCenter==null)
			pertCenter=new Point2D.Double();
		pertCenter.setLocation(centerX,centerY);
	}
	public GeneralPath getXbsShape() {
		return xbsShape;
	}
	public void setXbsShape(GeneralPath xbsShape,double centerX, double centerY) {
		this.xbsShape = xbsShape;
		setXbsCenter(centerX,centerY);
	}
	private void setXbsCenter(double centerX, double centerY) {
		if (xbsCenter==null)
			xbsCenter=new Point2D.Double();
		xbsCenter.setLocation(centerX,centerY);
	}
	public Point2D getPertCenter() {
		return pertCenter;
	}
	public Point2D getXbsCenter() {
		return xbsCenter;
	}
	public void translatePertShape(double dx,double dy){
		AffineTransform t=AffineTransform.getTranslateInstance(dx,dy);
		getPertShape().transform(t);
		Point2D point=getPertCenter();
		point.setLocation(point.getX()+dx,point.getY()+dy);
	}
	public void translateXbsShape(double dx,double dy){
		AffineTransform t=AffineTransform.getTranslateInstance(dx,dy);
		getXbsShape().transform(t);
		Point2D point=getXbsCenter();
		point.setLocation(point.getX()+dx,point.getY()+dy);
	}

	public long getCompleted(){
		if (!(getNode().getImpl() instanceof Schedule)) return 0;
		long completedT=ScheduleService.getInstance().getCompleted((Schedule)getNode().getImpl());
		return (completedT==0)?getStart():completedT;
	}
	public boolean isStarted(){
		if (!(getNode().getImpl() instanceof Schedule)) return false;
		return ((Schedule)getNode().getImpl()).getPercentComplete() >0.0D;
//		return ScheduleService.getInstance().getCompleted((Schedule)getNode().getImpl())!=0;
	}

//	protected boolean manualPert=false;
//	protected boolean manualXbs=false;
//
//
//	public boolean isManualPert() {
//		return manualPert;
//	}
//	public void setManualPert(boolean manualPert) {
//		this.manualPert = manualPert;
//	}
//	public boolean isManualXbs() {
//		return manualXbs;
//	}
//	public void setManualXbs(boolean manualXbs) {
//		this.manualXbs = manualXbs;
//	}


	protected List tmpChildren=new ArrayList();
	public List getChildren() {
		return tmpChildren;
	}
	protected boolean tmpFiltered;
	public boolean isFiltered() {
		return tmpFiltered;
	}

	public void setFiltered(boolean filtered) {
		this.tmpFiltered = filtered;
	}

}
