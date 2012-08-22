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
package com.projity.pm.graphic.model.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.projity.association.AssociationList;
import com.projity.association.InvalidAssociationException;
import com.projity.document.Document;
import com.projity.document.ObjectEvent;
import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.event.HierarchyEvent;
import com.projity.grouping.core.event.HierarchyListener;
import com.projity.grouping.core.hierarchy.NodeHierarchy;
import com.projity.grouping.core.model.NodeModel;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.dependency.DependencyService;
import com.projity.pm.dependency.DependencyType;
import com.projity.pm.dependency.HasDependencies;
import com.projity.pm.resource.Resource;
import com.projity.pm.scheduling.ScheduleEvent;
import com.projity.pm.scheduling.ScheduleEventListener;
import com.projity.pm.task.Project;
import com.projity.pm.task.Task;
/**
 * This class lies between the SpreadSheet and the SpreadSheetModel.
 * It holds the states directly linked to the view.
 * The collapsed state and level of the nodes here.
 * The level is not a view state but it is calculated and cached for performance purposes.
 */

public class ReferenceNodeModelCache implements ObjectEvent.Listener, HierarchyListener, /*TreeModel,*/ ScheduleEventListener {
	private NodeModel model;
	
	protected NodeCache nodeCache;
	protected DependencyCache edgeCache;
	protected Document document;
	
	protected int type;
		
	
	/**
	 * @param model
	 */
	public ReferenceNodeModelCache(NodeModel model, Document document, int type) {
		this.document = document;
		nodeCache=new NodeCache();
		edgeCache=new DependencyCache();
		setModel(model);
		this.type=type;
	}
//	public ReferenceNodeModelCache(NodeModel model) {
//		this(model,null);
//	}
	/**
	 * 
	 */
//	public ReferenceNodeModelCache(Document document) {
//		this.document = document;
//		nodeCache=new NodeCache();
//		edgeCache=new DependencyCache();
//	}

	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	public GraphicNode getGraphicNode(Node node) {
		return (GraphicNode)nodeCache.getElement(node);
	}
	public void bindView(VisibleNodes nodes,VisibleDependencies deps){
	    nodeCache.addVisibleElements(nodes);
	    edgeCache.addVisibleElements(deps);
	    //updateVisibleElements(nodes,deps,new HashSet());
	    //updateVisibleElements(nodes,new HashSet());
	}
	public void unbindView(VisibleNodes nodes,VisibleDependencies deps){
	    nodeCache.addVisibleElements(nodes);
	    edgeCache.addVisibleElements(deps);
	}
	
	
	
	public void close(){
	    if (model!=null) {
	    	removeListeners();
	    	nodeCache.removeAllVisibleElements();
	    	nodeCache.clear();
	    	edgeCache.removeAllVisibleElements();
	    	edgeCache.clear();
	    }
	}
	
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}

	private void removeListeners() {
		model.getHierarchy().removeHierarchyListener(this);
    	if (document!=null) document.removeObjectListener(this);
	    if (document!=null&&document instanceof Project) ((Project)document).removeScheduleListener(this);
	}
	
	
	public Document getDocument(){
	    return document;
	}
	
	
	
	public GraphicNode getParent(GraphicNode node){
		Node parent=getModel().getHierarchy().getParent(node.getNode()); //can be null
		return (GraphicNode)nodeCache.getElement(parent);
	}
	public List getChildren(GraphicNode node){
		Collection children=getModel().getHierarchy().getChildren((node==null)?null:node.getNode());
		if (children==null) return null;
		List list=new LinkedList();//new ArrayList(children.size());
		Object child;
		for (Iterator i=children.iterator();i.hasNext();){
			child=nodeCache.getElement(i.next());
			if (child!=null) list.add(child);
		}
		return list;
	}
	public List getEdges(){
		return edgeCache.getCache();
	}
	
	
	public Object getGraphicNode(Object base){
		return nodeCache.getElement(base);
	}
	public Object getGraphicDependency(Object base){
		return edgeCache.getElement(base);
	}
	
	
	
//update	
	public void update(){
		update(new HashSet(),false);
	}
	public void update(boolean reschedule){
		update(new HashSet(),reschedule);
	}
	public void update(Set change,boolean reschedule){
//		System.out.println("ReferenceNodeModelCache update");
		NodeCache newCache=new NodeCache();
		update(null,newCache,change,reschedule);
		
		//edges
		Set edgeChange=new HashSet();
		updateEdges(edgeChange);
		
		
		nodeCache.copyContent(newCache);
		
		updateVisibleElements(change,edgeChange);
	}
	
	protected void updateVisibleElements(Set change,Set edgeChange){
//		long t0=System.currentTimeMillis();
		nodeCache.updateVisibleElements(change);
//		long t1=System.currentTimeMillis();
//		System.out.println("\tcache nodeCache.updateVisibleElements ran in "+(t1-t0)+"ms");

		edgeCache.updateVisibleElements(edgeChange);
//		t0=System.currentTimeMillis();
//		System.out.println("\tcache edgeCache.updateVisibleElements ran in "+(t0-t1)+"ms");

		nodeCache.fireEvents(this);
//		t1=System.currentTimeMillis();
//		System.out.println("\tcache nodeCache.fireEvents ran in "+(t1-t0)+"ms");

	}
	protected void updateVisibleElements(VisibleNodes nodes/*,Set change*/){
		nodeCache.updateVisibleElements(nodes,new HashSet());
		edgeCache.updateVisibleElements(nodes.getVisibleDependencies(),new HashSet());
		nodeCache.fireEvents(this,nodes);
	}
	
	
	
	public void updateEdges(Set change){
	    GraphicDependency current;
	    for (Iterator i=edgeCache.getCacheIterator();i.hasNext();){
	        current=(GraphicDependency)i.next();
	        if (current.isDirty()){
	            current.setDirty(false);
	            change.add(current);
	        }
	    }
	}
	
	public void update(GraphicNode node,NodeCache newCache, Set change,boolean reschedule){
		int level=(node==null)?0:node.getLevel();
		
		int collapseLevel=GraphicConfiguration.getInstance().getCollapseLevel();
		
		GraphicNode current;
		Collection children=model.getHierarchy().getChildren((node==null)?null:node.getNode());
		boolean summary=false;
		if (children!=null){
			Node child;
			for (Iterator i=children.iterator();i.hasNext();){
				child=(Node)i.next();
				Object impl=child.getImpl();
				if (!(impl instanceof Assignment)) summary=true;
				current=(GraphicNode) nodeCache.getElement(child);
				if (current==null){
					current=createNode(child);
					if (collapseLevel!=-1&&level>=collapseLevel-1) current.setCollapsed(true);
				}
				newCache.insertElement(current,current.getNode());
				if (current.getLevel()!=level+1){
					current.setLevel(level+1);
				}
				if (current.isVoid()&&(!current.getNode().isVoid())){
					current.setVoid(false);
				}else if (!current.isVoid()&&(current.getNode().isVoid())){
					current.setVoid(true);
				}
				if (reschedule&&impl instanceof Task&&((Task)impl).isJustModified()){
					current.setDirty(true);
				}
				if (current.isDirty()){
					change.add(current);
					current.setDirty(false);
				}
				update(current,newCache,change,reschedule);
			}
		}
		
		if (node!=null){
			boolean composite=!(children==null||children.size()==0);
			if (node.isComposite()!=composite){
				node.setComposite(composite);
			}
			if (node.isSummary()!=summary){
				node.setSummary(summary);
			}
			
			
			if (node.isDirty()){
				change.add(node);
				node.setDirty(false);
			}
			
			node.updateScheduleCache();
		}

	}
		
		
		
	
	
	
	/**
	 * @return Returns the model.
	 */
	public NodeModel getModel() {
		return model;
	}
	/**
	 * @param model The model to set.
	 */
	public void setModel(NodeModel model) {
	    if (this.model!=null) {
	    	removeListeners();
	    }
		this.model=model;//new FilteredNodeModel(model);
		//this.model.setFilter(filter);
	    model.getHierarchy().addHierarchyListener(this);
	    if (document!=null) document.addObjectListener(this);
	    if (document!=null&&document instanceof Project) ((Project)document).addScheduleListener(this);
		buildCache();
	}
	
	private void buildCache(){
		nodeCache.clear();
		getModel().getHierarchy().checkEndVoidNodes(NodeModel.SILENT);
		update();
		buildEdges();
		syncEdges();
	}
	

	
	public void changeCollapsedState(GraphicNode gnode){
		if (gnode.isComposite()) gnode.setCollapsed(!gnode.isCollapsed());
		update();
	}
	
//edges
	public void buildEdges(){
		Map implMap=new HashMap();
		List gnodes=new ArrayList();
		for (Iterator i=nodeCache.getCache().iterator();i.hasNext();){
			GraphicNode gnode=(GraphicNode)i.next();
			if (gnode.isVoid()||gnode.isAssignment()) continue;
			if (!(gnode.getNode().getImpl() instanceof HasDependencies))
				return; //TODO this is ugly.  perhaps subclass NodeBridge for cases when impl has dependencies
			gnodes.add(gnode);
			implMap.put(gnode.getNode().getImpl(),gnode);
		}
		
		for (Iterator i=gnodes.iterator();i.hasNext();){
			GraphicNode gnode=(GraphicNode)i.next();
			
			HasDependencies task=(HasDependencies)gnode.getNode().getImpl();			
			AssociationList dependencyList=task.getSuccessorList();
			for (Iterator j=dependencyList.iterator();j.hasNext();){
				Dependency dep=(Dependency)j.next();
				
				HasDependencies pre=dep.getPredecessor();
				HasDependencies suc=dep.getSuccessor();
				GraphicNode preGNode=(GraphicNode)implMap.get(pre);
				GraphicNode sucGNode=(GraphicNode)implMap.get(suc);
				if (preGNode!=null&&sucGNode!=null){
					newGraphicDependency(preGNode,sucGNode,dep);
				} else {
					System.out.println("no graphic node");
				}
			}
		}
	}
	
	
	public GraphicDependency newGraphicDependency(GraphicNode preGNode,GraphicNode sucGNode,Dependency dep){
		GraphicDependency gdep = new GraphicDependency(preGNode,sucGNode,dep);
		int depType=dep.getDependencyType();
		//gdep.setType(depType);
		edgeCache.insertElement(gdep,dep);
		return gdep;
	}
	
	
	private void syncEdges(){
		edgeCache.updateAllVisibleElements();
	}

	
	public void createDependency(GraphicNode startNode,GraphicNode endNode) throws InvalidAssociationException{
		DependencyService service=DependencyService.getInstance();
		HasDependencies startObject=(HasDependencies)startNode.getNode().getImpl();
		HasDependencies endObject=(HasDependencies)endNode.getNode().getImpl();
		//try {
			Dependency dep=service.newDependency(startObject,endObject,DependencyType.FS,0L,this);
		//} catch (InvalidAssociationException e) {
		//	e.printStackTrace();
		//}
	}
//	public void createHierarchyDependency(GraphicNode startNode,GraphicNode endNode){
//	    model.getHierarchy().move(endNode.getNode(),startNode.getNode());
//	}
	

	public void removeEdge(GraphicDependency dep){
		if (dep==null) return;
		edgeCache.deleteElement(dep);
	}
	public void modifyEdge(GraphicDependency dep,int type){
		if (type!=-1){
			//dep.setType(type);
		}
	}
	

	
	
	
	protected int getLevel(Node node){
	    int level=0;
	    NodeHierarchy hierarchy=getModel().getHierarchy();
	    for(Node current=node;current!=null;current=hierarchy.getParent(current)) level++;
	    return level;
	}
	protected boolean isComposite(Node node){
	    return !getModel().getHierarchy().isLeaf(node);
	}
	protected boolean isSummary(Node node){
	    return getModel().getHierarchy().isSummary(node);
	}
	
	public GraphicNode createNode(Node node){
		return new GraphicNode(node,-1);
	}
	
	
	
	protected boolean receiveEvents=true;
	public boolean isReceiveEvents() {
		return receiveEvents;
	}
	public void setReceiveEvents(boolean receiveEvents) {
		this.receiveEvents = receiveEvents;
	}
	
	public void scheduleChanged(ScheduleEvent e){
		//System.out.println("ScheduleEvent: type="+e.getType()+", snap="+e.getSnapshot()+", object="+e.getObject());
		if (!receiveEvents) return;
//		nodeCache.updateCachedSchedule();
//		nodeCache.fireScheduleEvent(e.getSource(),e);
		update(true);
	}
	
	
	public void objectChanged(ObjectEvent objectEvent) {
		//System.out.println("ObjectEvent: type="+objectEvent.getType()+", field="+objectEvent.getField()+", object="+objectEvent.getObject());
		if (!receiveEvents) return;
		Object object=objectEvent.getObject();
		if (object instanceof Dependency) {
			Dependency dependency = ((Dependency)object);
			if (dependency.getDocument() == document || dependency.getMasterDocument() == document) { // links can come from other projects too, but successor should be in this project
				if (objectEvent.isCreate()) {
					Node preNode=(Node)model.search(dependency.getPredecessor());
					Node sucNode=(Node)model.search(dependency.getSuccessor());
					GraphicNode preGNode=(GraphicNode)nodeCache.getElement(preNode);
					GraphicNode sucGNode=(GraphicNode)nodeCache.getElement(sucNode);
					if (preGNode!=null&&sucGNode!=null){
						GraphicDependency edge=(GraphicDependency)edgeCache.getElement(dependency);
						if (edge == null) { // for external tasks in subprojects, it's possible they already were created
							edge=newGraphicDependency(preGNode,sucGNode,dependency);
							update();
						}
					}
				} else if (objectEvent.isDelete()) {
					GraphicDependency edge=(GraphicDependency)edgeCache.getElement(dependency);
					if (edge!=null){
						removeEdge(edge);
						update();
					}
					//edgeCache.fireEdgesRemoved(this,new Object[]{edge});
				} else { //update
					GraphicDependency edge=(GraphicDependency)edgeCache.getElement(dependency);
					if (edge!=null){
						modifyEdge(edge,dependency.getDependencyType());
						update();
					}
					//edgeCache.fireEdgesUpdated(this,new Object[]{edge});
				}
			}
		}else{
			if (object!=null&&((object instanceof Task && (type&NodeModelCache.TASK_TYPE)==NodeModelCache.TASK_TYPE)||
				(object instanceof Resource && (type&NodeModelCache.RESOURCE_TYPE)==NodeModelCache.RESOURCE_TYPE)||
				(object instanceof Assignment && (type&NodeModelCache.ASSIGNMENT_TYPE)==NodeModelCache.ASSIGNMENT_TYPE)||
				(object instanceof Project && (type&NodeModelCache.PROJECT_TYPE)==NodeModelCache.PROJECT_TYPE))){
				if (object!=null&&!objectEvent.isDelete()){ //because node is already deleted
					//TODO cannot update parents in case of deletion
					Node node=model.search(object);
					if (node !=null) {
						for(;!node.isRoot();node=model.getParent(node)){
//						System.out.println("objectChanged "+objectEvent.getType()+": "+node);
						GraphicNode gnode=getGraphicNode(node);
						if (gnode != null) // on project list it is null
							gnode.setDirty(true);
						//nodeCache.fireObjectEvent(objectEvent.getSource(),objectEvent);
						}
					}
				}
				update();
			}
					
		}
	}

	
	
	public void nodesChanged(HierarchyEvent e) {
	    if (receiveEvents&&!e.isConsumed()) update();
	}
	public void nodesInserted(HierarchyEvent e) {
		if (receiveEvents&&!e.isConsumed()) update();
	}
	public void nodesRemoved(HierarchyEvent e) {
		if (receiveEvents&&!e.isConsumed()) update();
	}
	public void structureChanged(HierarchyEvent e) {
		if (receiveEvents&&!e.isConsumed()) update();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	public void addTreeModelListener(TreeModelListener arg0) {
//		model.addTreeModelListener(arg0);
//	}
//	public Object getChild(Object arg0, int arg1) {
//		return model.getChild(arg0, arg1);
//	}
//	public int getChildCount(Object arg0) {
//		return model.getChildCount(arg0);
//	}
//	public int getIndexOfChild(Object arg0, Object arg1) {
//		return model.getIndexOfChild(arg0, arg1);
//	}
	
	protected GraphicNode root=null; 
	public Object getRoot() {
		if (root==null) root=new GraphicNode((Node)model.getRoot(),0); 
		return root;
	}
//	public boolean isLeaf(Object arg0) {
//		return model.isLeaf(arg0);
//	}
//	public void removeTreeModelListener(TreeModelListener arg0) {
//		model.removeTreeModelListener(arg0);
//	}
//	public void valueForPathChanged(TreePath arg0, Object arg1) {
//		model.valueForPathChanged(arg0, arg1);
//	}
	
	
	
	
	public String toString(){
		return nodeCache.getVisibleElements().toString();
	}
	
	
	
	
	
	
	
	
	
	
}
