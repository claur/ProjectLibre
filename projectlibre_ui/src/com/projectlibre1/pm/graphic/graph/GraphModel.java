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
package com.projectlibre1.pm.graphic.graph;

import java.io.Serializable;
import java.util.EventListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.event.EventListenerList;

import com.projectlibre1.pm.graphic.graph.event.GraphEvent;
import com.projectlibre1.pm.graphic.graph.event.GraphListener;
import com.projectlibre1.pm.graphic.model.cache.GraphicNode;
import com.projectlibre1.pm.graphic.model.cache.NodeModelCache;
import com.projectlibre1.pm.graphic.model.event.CacheListener;
import com.projectlibre1.pm.graphic.model.event.CompositeCacheEvent;
import com.projectlibre1.configuration.Configuration;
import com.projectlibre1.field.Field;
import com.projectlibre1.graphic.configuration.BarStyles;
import com.projectlibre1.grouping.core.model.NodeModel;
import com.projectlibre1.pm.assignment.Assignment;
import com.projectlibre1.pm.task.Project;
import com.projectlibre1.pm.task.Task;
import com.projectlibre1.util.Environment;

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
