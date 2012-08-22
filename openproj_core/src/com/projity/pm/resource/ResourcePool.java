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
package com.projity.pm.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.projity.configuration.Settings;
import com.projity.document.Document;
import com.projity.document.ObjectEvent;
import com.projity.document.ObjectEventManager;
import com.projity.document.ObjectSelectionEventManager;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeList;
import com.projity.grouping.core.OutlineCollection;
import com.projity.grouping.core.OutlineCollectionImpl;
import com.projity.grouping.core.model.AssignmentNodeModel;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelDataFactory;
import com.projity.pm.calendar.CalendarService;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.task.Project;
import com.projity.undo.DataFactoryUndoController;


/**
 *
 */
public class ResourcePool implements Document, NodeModelDataFactory {
	private String name = "";
	private ArrayList resourceList = new ArrayList();
	private ArrayList projects = new ArrayList();
	private ObjectEventManager objectEventManager = new ObjectEventManager();
	private int resourceIdCounter = 0;
	private WorkingCalendar defaultCalendar;
    private static ResourcePool globalPool = null; // TODO is it ok to be global?
	protected ResourcePool(String name,DataFactoryUndoController undo) {
		this.name = name;
		globalPool = this;
		defaultCalendar = CalendarService.getInstance().getDefaultInstance();
		undoController=undo;
		
		//initUndo();
	}
	public static ResourcePool createRourcePool(String name,DataFactoryUndoController undo) {
		ResourcePool pool=new ResourcePool(name,undo);
		pool.initializeOutlines();
		return pool;
	}
	
    private transient HashMap<Long,Resource> idMap = null;
	public Resource findById(long id) {
		if (idMap == null) {
		    idMap = new HashMap<Long,Resource>();
			Iterator i = getResourceList().iterator();
			Resource resource;
			while (i.hasNext()) {
				resource = (Resource)i.next();
				idMap.put(resource.getUniqueId(),resource);
			}
		}
		return idMap.get(id);
	}
	public void initializeId(Resource resource) {
		long id = ++resourceIdCounter;
		resource.setId(id); //starts at 1TODO check for duplicates -
		//resource.setUniqueId(id); //TODO use a GUID generator
	}
	
	public void initializeOutlines(){
		int count=Settings.numHierarchies();
		for (int i=0;i<count;i++){
			NodeModel model=resourceOutlines.getOutline(i);
			if (model==null) continue;
			if (model instanceof AssignmentNodeModel){
				AssignmentNodeModel aModel=(AssignmentNodeModel)model;
				aModel.setDocument(this);
			}
			initOutline(model);
		}
	}
	
	
	public void addAndInitializeId(Resource resource) {
		add(resource);
		initializeId(resource);
	}
	public void add(Resource resource) {
		resourceList.add(resource);
	}
	public void remove(Resource resource) {
		resourceList.remove(resource);
	}
	
	public ResourceImpl newResourceInstance() {
		EnterpriseResource globalResource = new EnterpriseResource(isLocal(),this);
		ResourceImpl newOne = new ResourceImpl(globalResource);
		
		addAndInitializeId(newOne);
		return newOne;
	}
	
	public Resource createScriptedResource() {
		Resource res = newResourceInstance();
		resourceOutlines.addToAll(res,null); // update all node models 
		return res;
	}
	/**
	 * @return Returns the resourceList.
	 */
	public ArrayList getResourceList() {
		return resourceList;
	}
	
	public static Resource findResourceByName(Object idObject, Object resourcePoolObject) {
		Iterator i = ((ResourcePool)resourcePoolObject).getResourceList().iterator();
		String id = (String)idObject;
		Resource resource;
		while (i.hasNext()) {
			resource = (Resource)i.next();
			if (resource.getName().equals(id))
				return resource;
		}
		return null;
	}
	public static Resource findResourceByInitials(Object idObject, Object resourcePoolObject) {
		Iterator i = ((ResourcePool)resourcePoolObject).getResourceList().iterator();
		int id = ((Integer)idObject).intValue();
		Resource resource;
		while (i.hasNext()) {
			resource = (Resource)i.next();
			if (resource.getId() == id)
				return resource;
		}
		return null;
	}
	
	private OutlineCollection resourceOutlines = new OutlineCollectionImpl(Settings.numHierarchies(),this); 
	
	public NodeModel getResourceOutline() {
		NodeModel model=resourceOutlines.getOutline();
		return model;
	}
	public NodeModel getResourceOutline(int outlineNumber) {
		NodeModel model=resourceOutlines.getOutline(outlineNumber);
		return model;
	}
	
	public void addToDefaultOutline(Node parentNode, Node childNode) {
		resourceOutlines.addToDefaultOutline(parentNode,childNode);
	}
	public void addToDefaultOutline(Node parentNode, Node childNode,int position,boolean event) {
		resourceOutlines.addToDefaultOutline(parentNode,childNode,position,event);
	}
	/* (non-Javadoc)
	 * @see com.projity.grouping.core.NodeModelDataFactory#createUnvalidatedObject(com.projity.grouping.core.NodeModel)
	 */
	public Object createUnvalidatedObject(NodeModel nodeModel, Object parent) {
		EnterpriseResource globalResource = new EnterpriseResource(isLocal(),this);
		ResourceImpl newOne = new ResourceImpl(globalResource);
		newOne.getGlobalResource().setMaster(isMaster());
		newOne.getGlobalResource().setLocal(isLocal());
		addUnvalidatedObject(newOne,nodeModel,parent);
		return newOne;
	}
	public void addUnvalidatedObject(Object object, NodeModel nodeModel, Object parent) {
	}
	/* (non-Javadoc)
	 * @see com.projity.grouping.core.NodeModelDataFactory#validateObject(java.lang.Object, com.projity.grouping.core.NodeModel)
	 */
	public void validateObject(Object newlyCreated, NodeModel nodeModel, Object eventSource, Object hierarchyInfo,boolean isNew) {
		if (!(newlyCreated instanceof Resource)) return;// avoids VoidNodes
		Resource resource=(Resource)newlyCreated;
		
		((ResourceImpl)resource).getGlobalResource().setResourcePool(this);
		
		add(resource);
		if (isNew) initializeId(resource);
		resourceOutlines.addToAll(newlyCreated,nodeModel); // update all node models except the one passed in
		//objectEventManager.fireCreateEvent(this,newlyCreated);
	}
//	public void fireCreated(Object newlyCreated){
//		//objectEventManager.fireCreateEvent(this,newlyCreated);
//	}
	/* (non-Javadoc)
	 * @see com.projity.grouping.core.NodeModelDataFactory#remove(java.lang.Object)
	 */
	public void remove(Object toRemove, NodeModel nodeModel,boolean deep,boolean undo,boolean removeDependencies){
		remove((Resource)toRemove);
		resourceOutlines.removeFromAll(toRemove,nodeModel); // update all node models except the one passed in		
	}

	public void addProject(Project project) {
		projects.add(project);
//		initUndoControlerForAllOutines(project);
	}
	public void removeProject(Project project) {
		projects.remove(project);
	}
	
	/**
	 * @return Returns the projects.
	 */
	public ArrayList getProjects() {
		return projects;
	}
	/**
	 * @param listener
	 */
	public void addObjectListener(ObjectEvent.Listener listener) {
		objectEventManager.addListener(listener);
	}
	/**
	 * @param listener
	 */
	public void removeObjectListener(ObjectEvent.Listener listener) {
		objectEventManager.removeListener(listener);
	}	

	public ObjectEventManager getObjectEventManager() {
		return objectEventManager;
	}
	public void fireUpdateEvent(Object source, Object object) {
		objectEventManager.fireUpdateEvent(source,object);
	}
	
	
	public String toString() {
		return name;
	}	
	
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see com.projity.document.Document#fireMultipleTransaction(int, boolean)
	 */
	public int fireMultipleTransaction(int id, boolean begin) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	

	
	
	
	/**
	 * @return Returns the defaultCalendar.
	 */
	public final WorkCalendar getDefaultCalendar() {
		return defaultCalendar;
	}
	
	public ArrayList extractCalendars() {
		return WorkingCalendar.extractCalendars(resourceList);
	}
	
	private transient boolean isDirty=false;
	public final boolean isGroupDirty() {
//		for (Iterator i=getProjects().iterator();i.hasNext();){
//			Project project=(Project)i.next();
//			if (project.isGroupDirty()) return true;
//		}
//		return false;
		return isDirty;
	}
	public final void setGroupDirty(boolean isDirty) {
		System.out.println("ResourcePool.setGroupDirty("+isDirty+")");
		this.isDirty = isDirty;
		if (isDirty)
		for (Iterator i=getProjects().iterator();i.hasNext();){
			Project project=(Project)i.next();
			project.setGroupDirty(true);
		}
	}
	
	protected boolean master;
	public boolean isMaster() {
		return master;
	}

	public void setMaster(boolean master) {
		this.master = master;
	}
	protected boolean local;
	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}
	
	
	public void updateOutlineTypes(){
		NodeModel[] models=resourceOutlines.getOutlines();
		for (int i=0;i<models.length;i++){
			initOutline(models[i]);
		}
	}
	public void initOutline(NodeModel nodeModel){
		if (nodeModel!=null){
			nodeModel.setLocal(local);
			nodeModel.setMaster(master);			
		}
	}
	
	//Undo
	protected transient DataFactoryUndoController undoController;
//	protected void initUndo(){
//		undoController=new DataFactoryUndoController(this);
//	}
	public DataFactoryUndoController getUndoController() {
		return undoController;
	}
	public void setUndoController(DataFactoryUndoController undoController) {
		this.undoController = undoController;
	}

	public void rollbackUnvalidated(NodeModel nodeModel, Object object) {
	}
	public NodeModelDataFactory getFactoryToUseForChildOfParent(Object impl) {
		return this;
	}

	public void setAllChildrenDirty(boolean dirty) {
	}

	
	public boolean containsAssignments(){return true;}

	public static final Object[] userResources() {
		Iterator i = globalPool.getResourceList().iterator();
		Resource resource;
		ArrayList result = new ArrayList();
		while (i.hasNext()) {
			resource = (Resource)i.next();
			if (resource.isUser())
				result.add(resource);
		}
		return result.toArray();
	}
	public static final Resource findResource(String name) {
		return findResourceByName(name, globalPool);
	}
	public List getChildrenResoures(Resource parent) {
		NodeModel resourceModel = getResourceOutline();
		Node node = resourceModel.search(parent);
		return NodeList.nodeListToImplList(resourceModel.getChildren(node));
	}
	public Resource getRbsParentResource(Resource child) {
		NodeModel resourceModel = getResourceOutline();
		Node node = resourceModel.search(child);
		Node parent = resourceModel.getParent(node);
		if (parent == null || parent.isVoid())
			return null;
		return (Resource)parent.getImpl();
	}
	public void setLocalParent(Resource child, Resource parent) {
		Node childNode = getResourceOutline().search(child);
		Node parentNode = parent == null ? null : getResourceOutline().search(parent);
		setLocalParent(childNode,parentNode);
	}

	public void setLocalParent(Node childNode, Node parentNode) {
		Resource child = (Resource) childNode.getImpl();
		Resource parent = (Resource) (parentNode == null ? null : parentNode.getImpl());
		if (getRbsParentResource(child) == parent)
			return;
		Node oldParentNode = getResourceOutline().search(getRbsParentResource(child));
		if (oldParentNode != null)
			oldParentNode.getChildren().remove(childNode);
		ArrayList temp = new ArrayList();
		temp.add(childNode);
		getResourceOutline().move(parentNode, temp, -1,NodeModel.NORMAL);
	}
	public ObjectSelectionEventManager getObjectSelectionEventManager() {
		// TODO Auto-generated method stub
		return null;
	}
}


