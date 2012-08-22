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
package com.projity.pm.graphic.graph;

import java.io.Serializable;
import java.util.EventListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.event.EventListenerList;

import com.projity.configuration.Configuration;
import com.projity.field.Field;
import com.projity.graphic.configuration.BarStyles;
import com.projity.grouping.core.model.NodeModel;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.graphic.graph.event.GraphEvent;
import com.projity.pm.graphic.graph.event.GraphListener;
import com.projity.pm.graphic.model.cache.GraphicNode;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.model.event.CacheListener;
import com.projity.pm.graphic.model.event.CompositeCacheEvent;
import com.projity.pm.task.Project;
import com.projity.pm.task.Task;
import com.projity.util.Environment;

/**
 *
 */
public class GraphModel implements Serializable, /*ScheduleEventListener,*/ CacheListener  /*ObjectEvent.Listener*/{
	private static final long serialVersionUID = -6589463266745797527L;
	protected NodeModelCache cache;
    protected BarStyles barStyles;
    protected Project project;
    protected static Field nameField=Configuration.getFieldFromId("Field.name");
    
    
    
	public GraphModel(Project project,String viewName) {
		this.project = project;
	}

	
	public void close(){
		setCache(null);
	}
	
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}

	
	
	public BarStyles getBarStyles() {
		return barStyles;
	}
	public void setBarStyles(BarStyles barStyles) {
		this.barStyles = barStyles;
	}

	
	
//cache: nodes
	public NodeModelCache getCache() {
		return cache;
	}
	public void setCache(NodeModelCache cache){
		if (this.cache!=null){
			this.cache.removeNodeModelListener(this);
		}
		this.cache = cache;
		cache.addNodeModelListener(this);
		
	}
	
	
	public ListIterator getNodeIterator(){
		return cache.getIterator();
	}
	public ListIterator getNodeIterator(int i){
		return cache.getIterator(i);
	}
	public ListIterator getDependencyIterator(){
		return cache.getEdgesIterator();
	}
	
	
	

	
	public List searchJustModifiedNodes(){
		LinkedList gnodes=new LinkedList();
		GraphicNode gnode;
		Object impl;
		for (Iterator i=getCache().getIterator();i.hasNext();){
			gnode=(GraphicNode)i.next();
			impl=gnode.getNode().getImpl();
			if (impl instanceof Task){
				if (((Task)impl).isJustModified()) gnodes.add(gnode);
			}else if (impl instanceof Assignment){ //assignment
				if (((Task)getCache().getModel().getParent(gnode.getNode()).getImpl()).isJustModified()) gnodes.add(gnode);
			}
		}
		return gnodes;
	}
	public List searchNode(Object impl){
		LinkedList gnodes=new LinkedList();
		GraphicNode gnode;
		for (Iterator i=getCache().getIterator();i.hasNext();){
			gnode=(GraphicNode)i.next();
			if (gnode.getNode().getImpl()==impl){
				gnodes.add(gnode);
				break;
			}
		}
		return gnodes;
	}
	
	
	//ScheduleEventListener
//	public void scheduleChanged(ScheduleEvent evt) {
//		//update((evt.getObject()==null)?searchJustModifiedNodes():searchNode(evt.getObject()),true);
////		done throught cache
//	}
//	
//	//ObjectEvent.Listener
//	public void objectChanged(ObjectEvent objectEvent) {
//	}	
	 
	//NodeModelListener
	public void graphicNodesCompositeEvent(CompositeCacheEvent compositeEvent){
	    if (compositeEvent.getRemovedNodes()!=null) remove(compositeEvent.getRemovedNodes(),false);
        
	    if (compositeEvent.getInsertedNodes()!=null) insert(compositeEvent.getInsertedNodes(),false);
        
//	    updateAll(false);
	    update(compositeEvent.getUpdatedNodes(),false);
 
	    if (compositeEvent.getRemovedEdges()!=null) removeEdges(compositeEvent.getRemovedEdges(),false);
	    
	    if (compositeEvent.getInsertedEdges()!=null) insertEdges(compositeEvent.getInsertedEdges(),false);
        
        if (compositeEvent.getUpdatedEdges()!=null) updateEdges(compositeEvent.getUpdatedEdges(),false);
	}

	
	public void updateAll(boolean event) {
		update(null,event);
	}
	
	protected void update(List nodes, boolean event){
		fireUpdate(this,nodes);
	}
	
	public void insertAll(boolean event){
		if (cache.getSize()>0) insert(null,true,event);
	}	
	protected void insert(List nodes,boolean event){
		insert(nodes,false,event);
	}
	
	
	protected void insert(List nodes,boolean init,boolean event){
	}
	
	public void removeAll(boolean event) {
		remove((List)null,event);
	}
	
	protected void remove(List nodes,boolean event){
	}
	
	
	protected void insertEdges(List edges, boolean event){
	}
	
	protected void removeEdges(List edges, boolean event){
	}
	
	protected void updateEdges(List edges, boolean event){
	}

	
	
	

	
	

	
	/*public void scheduleChanged(ScheduleEvent evt) {
		super.scheduleChanged(evt);
		if (evt.getType() == ScheduleEvent.BASELINE){
			if (evt.isSaveSnapshot())
				baseLines.add(evt.getSnapshot());
			else baseLines.remove(evt.getSnapshot());
			int num=(baseLines.size()==0)?0:((Integer)baseLines.last()).intValue();
			rowHeight=GraphicConfiguration.getInstance().getRowHeight()
					+(num+1)*GraphicConfiguration.getInstance().getBaselineHeight();
			updateAll(true);
		}
	}*/

	
	//view events
	protected EventListenerList listenerList = new EventListenerList();

	public void addGraphListener(GraphListener l) {
		listenerList.add(GraphListener.class, l);
	}
	public void removeGraphListener(GraphListener l) {
		listenerList.remove(GraphListener.class, l);
	}
	public GraphListener[] getGraphListeners() {
		return (GraphListener[]) listenerList.getListeners(GraphListener.class);
	}
	 protected void fireUpdate(Object source,List nodes) {
			Object[] listeners = listenerList.getListenerList();
			GraphEvent e = null;
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == GraphListener.class) {
					if (e == null) {
						e = new GraphEvent(source,nodes);
					}
					((GraphListener) listeners[i + 1]).updateGraph(e);
				}
			}
		}



    public EventListener[] getListeners(Class listenerType) { 
    	return listenerList.getListeners(listenerType); 
       }
	
    public boolean isReadOnly(){
    	NodeModel nodeModel=cache.getModel();
    	return !nodeModel.isLocal()&&!nodeModel.isMaster()&&!Environment.getStandAlone();
    }
	

	
}
