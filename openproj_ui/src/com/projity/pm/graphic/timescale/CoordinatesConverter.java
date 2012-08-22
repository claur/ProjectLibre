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
package com.projity.pm.graphic.timescale;

import java.io.Serializable;
import java.util.Calendar;
import java.util.EventListener;

import javax.swing.event.EventListenerList;

import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.pm.graphic.model.cache.GraphicNode;
import com.projity.pm.scheduling.ScheduleEvent;
import com.projity.pm.scheduling.ScheduleEventListener;
import com.projity.pm.scheduling.ScheduleInterval;
import com.projity.pm.task.Project;
import com.projity.timescale.TimeInterval;
import com.projity.timescale.TimeIterator;
import com.projity.timescale.TimeScaleEvent;
import com.projity.timescale.TimeScaleListener;
import com.projity.timescale.TimeScaleManager;
import com.projity.util.DateTime;
import com.projity.workspace.SavableToWorkspace;
import com.projity.workspace.WorkspaceSetting;

/**
 *
 */
public class CoordinatesConverter implements ScheduleEventListener, Serializable, SavableToWorkspace {
	private static final long serialVersionUID = 3657308109433257760L;
	protected TimeScaleManager timescaleManager;
	protected long origin;
	protected long end;
	
	protected Project project;
	
	
	public CoordinatesConverter(Project project){
		this(project,TimeScaleManager.createInstance());
	}
	/**
	 * 
	 */
	public CoordinatesConverter(Project project,TimeScaleManager timescaleManager) {
		this.project=project;
		this.timescaleManager=timescaleManager;
		updateLargeInterval(false);
		project.addScheduleListener(this);
	}
	

	/**
	 * @return Returns the origin.
	 */
	public long getOrigin() {
		//adaptOrigin(getLargeStart(),true);
		return origin;
	}
    public long getEnd() {
		//adaptEnd(getLargeEnd(),true);
        return end;
    }
	/*public long getFloorOrigin() {
		long t=getTimescaleManager().getScale().floor1(origin);
		System.out.println("Origin: "+CalendarUtil.toString(t)+"/"+CalendarUtil.toString(origin));
		return t;
	}
    public long getCeilEnd() {
		long t=getTimescaleManager().getScale().ceil1(end);
		System.out.println("End: "+CalendarUtil.toString(end)+"/"+CalendarUtil.toString(t));
		return t;
    }*/
    
	protected void adaptOrigin(Calendar calendar,boolean event){
		//System.out.println("adaptOrigin: begin");
		getTimescaleManager().getScale().floor1(calendar,-1);
		
		long tmp = calendar.getTimeInMillis();
		if (this.origin!=tmp){
			//System.out.println("adaptOrigin: change: old="+CalendarUtil.toString(this.origin)+", new="+CalendarUtil.toString(tmp));
			this.origin=tmp;
			if (event) fireTimeScaleChanged(this,TimeScaleEvent.ORIGIN_AND_END_CHANGE);
		}
		//System.out.println("adaptOrigin: end");
	}
	protected void adaptEnd(Calendar calendar,boolean event){
		//System.out.println("adaptEnd: begin");
		getTimescaleManager().getScale().ceil1(calendar,-1);
		
		long tmp = calendar.getTimeInMillis();
		
		if (this.end!=tmp){
			//System.out.println("adaptEnd: change: old="+CalendarUtil.toString(this.end)+", new="+CalendarUtil.toString(tmp));
			this.end=tmp;
			if (event) fireTimeScaleChanged(this,TimeScaleEvent.END_ONLY_CHANGE);
		}
		//System.out.println("adaptEnd: end");
	}
	
	protected void adaptInterval(Calendar origin,Calendar end,boolean event){
		int modifType=0;
		long tmp=this.end;
		adaptEnd(end,false);
		if (this.end!=tmp)
			modifType=TimeScaleEvent.END_ONLY_CHANGE;
		tmp=this.origin;
		adaptOrigin(origin,false);
		if (this.origin!=tmp)
			modifType=TimeScaleEvent.ORIGIN_AND_END_CHANGE;
		if (modifType>0&&event) fireTimeScaleChanged(this,modifType);
		
	}
	
	private long getProjectStart(){
		long start=project.getEarliestStartingTaskOrStart();
		return (start==0)?System.currentTimeMillis():start;
	}
	private long getProjectEnd(){
		return project.getLatestFinishingTask();
	}
	
	private Calendar getLargeStart(){
		Calendar calendar=DateTime.calendarInstance();
		calendar.setTimeInMillis(getProjectStart());
		calendar.add(Calendar.DAY_OF_MONTH,-3);
		//CalendarUtil.roundTime(calendar);
		return calendar;
		
	}
	private Calendar getLargeEnd(){
		long end=getProjectEnd();
		Calendar calendar=DateTime.calendarInstance();
		calendar.setTimeInMillis(getProjectEnd());
		calendar.add(Calendar.DAY_OF_MONTH,30);
		//CalendarUtil.roundTime(calendar);
		return calendar;
	}
	
    protected void updateLargeInterval(boolean event){
    	adaptInterval(getLargeStart(),getLargeEnd(),event);
    	//System.out.println("updateLargeInterval: "+CalendarUtil.toString(getOrigin())+", "+CalendarUtil.toString(getEnd()));
    }
    
    
    public void toggleMinWidth(boolean normal){
    	if (timescaleManager.toggleMinWidth(normal)){
    		fireTimeScaleChanged(this,TimeScaleEvent.SCALE_CHANGE);
    	}
    }
   
    public boolean canZoomIn() {
    	return timescaleManager.canZoomIn();
    }
    public boolean canZoomOut() {
    	return timescaleManager.canZoomOut();
    }
    
	public void zoomIn(){
		if(timescaleManager.zoomIn()){
			updateLargeInterval(false);
			fireTimeScaleChanged(this,TimeScaleEvent.SCALE_CHANGE);
		}
	}
	
	public void zoomOut(){
		if(timescaleManager.zoomOut()){
			updateLargeInterval(false);
			fireTimeScaleChanged(this,TimeScaleEvent.SCALE_CHANGE);
		}
	}
	
	public void zoomReset(){
		if(timescaleManager.zoomReset()){
			updateLargeInterval(false);
			fireTimeScaleChanged(this,TimeScaleEvent.SCALE_CHANGE);
		}
	}
	public long getIntervalDuration() {
		return getTimescaleManager().getScale().getIntervalDuration(); 
	}
	/**
	 * @return Returns the timescaleManager.
	 */
	public TimeScaleManager getTimescaleManager() {
		return timescaleManager;
	}
	/**
	 * @param timescaleManager The timescaleManager to set.
	 */
	public void setTimescaleManager(TimeScaleManager timescaleManager) {
		this.timescaleManager = timescaleManager;
	}
	
	public double toTime(double x){
		return getOrigin()+timescaleManager.getScale().toTime(x);
	}
	public double toDuration(double w){
		return timescaleManager.getScale().toTime(w);
	}
	public double toX(double t){
		return timescaleManager.getScale().toX(t-getOrigin());
	}
	public double toW(double d){
		return timescaleManager.getScale().toX(d);
	}
	
	public double getWidth(){
		return toW(getEnd()-getOrigin());
	}
	
	public TimeIterator getTimeIterator(double x1,double x2){
		return new TimeIterator(toTime(x1),toTime(x2),timescaleManager.getScale(),getOrigin());
	}
	public TimeIterator getTimeIterator(double x1,double x2,boolean largeScale){
		return new TimeIterator(toTime(x1),toTime(x2),timescaleManager.getScale(),getOrigin(),largeScale);
	}

//	public TimeIterator getTimeIteratorFromDates(long start, long end){
//		return new TimeIterator(start,end,timescaleManager.getScale(),getOrigin());
//	}
	
	public TimeIterator getProjectTimeIterator(){
		return new TimeIterator(getOrigin(),getEnd(),timescaleManager.getScale(),getOrigin());
	}
	
	public int countProjectIntervals(){
		int count=0;
		TimeIterator iterator=getProjectTimeIterator();
		TimeInterval interval;
		while (iterator.hasNext()){
			interval=iterator.next();
			count++;
		}
		return count;
	}

	
	public void scheduleChanged(ScheduleEvent evt) {
		updateLargeInterval(true);
		//if project start or end have changed, it triggers a TimeScaleEvent
	}
	
	
	
	
	
	//events handling
	
	protected EventListenerList listenerList = new EventListenerList();

	public void addTimeScaleListener(TimeScaleListener l) {
		listenerList.add(TimeScaleListener.class, l);
	}
	public void removeTimeScaleListener(TimeScaleListener l) { 
		listenerList.remove(TimeScaleListener.class, l);
	}
	public TimeScaleListener[] getTimeScaleListeners() {
		return (TimeScaleListener[]) listenerList.getListeners(TimeScaleListener.class);
	}
	protected void fireTimeScaleChanged(Object source,int type) {
		Object[] listeners = listenerList.getListenerList();
		TimeScaleEvent e = null;
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TimeScaleListener.class) {
				if (e == null) {
					e = new TimeScaleEvent(source,type);
				}
				((TimeScaleListener) listeners[i + 1]).timeScaleChanged(e);
			}
		}
	}
    public EventListener[] getListeners(Class listenerType) { 
    	return listenerList.getListeners(listenerType); 
       }
	public void restoreWorkspace(WorkspaceSetting w, int context) {
		Workspace ws = (Workspace) w;
		timescaleManager.setCurrentScaleIndex(ws.currentScaleIndex);
		origin = ws.origin;
		end = ws.end;
	}
	
	public WorkspaceSetting createWorkspace(int context) {
		Workspace ws = new Workspace();
		ws.currentScaleIndex = timescaleManager.getCurrentScaleIndex();
		ws.origin = origin;
		ws.end = end;
		return ws;
	}
	public static class Workspace implements WorkspaceSetting { 
		private static final long serialVersionUID = -6767009284584575457L;
		int currentScaleIndex;	
		long origin;
		long end;
		public final int getCurrentScaleIndex() {
			return currentScaleIndex;
		}
		public final void setCurrentScaleIndex(int currentScaleIndex) {
			this.currentScaleIndex = currentScaleIndex;
		}
		public final long getEnd() {
			return end;
		}
		public final void setEnd(long end) {
			this.end = end;
		}
		public final long getOrigin() {
			return origin;
		}
		public final void setOrigin(long origin) {
			this.origin = origin;
		}
	}
	public Project getProject() {
		return project;
	}

    public static double adaptSmallBarEndX(double start,double end, GraphicNode node, GraphicConfiguration config){
    	if (config==null) config=GraphicConfiguration.getInstance();
    	if (config.getGanttBarMinWidth()==0 || node==null || node.getIntervalCount()>1) return end;
    	if (start<end && end-start<config.getGanttBarMinWidth() && config.getGanttBarMinWidth()>0
    			) return start+config.getGanttBarMinWidth();
    	else return end;
    }
    public ScheduleInterval adaptSmallBarTimeInterval(ScheduleInterval interval, GraphicNode node,GraphicConfiguration config){
    	if (config==null) config=GraphicConfiguration.getInstance();
    	if (config.getGanttBarMinWidth()==0 || node==null || node.getIntervalCount()>1) return interval;
    	if (config.getGanttBarMinWidth()>0){
	    	double minT=timescaleManager.getScale().toTime(config.getGanttBarMinWidth());
	    	if (interval.getStart()!=interval.getEnd() && interval.getEnd()-interval.getStart()<minT){
	    		return new ScheduleInterval(interval.getStart(),interval.getStart()+(long)minT);
	    	}
    	}
    	return interval;
    }
	
	
}
