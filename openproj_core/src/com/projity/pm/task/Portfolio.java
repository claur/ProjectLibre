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
package com.projity.pm.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Predicate;

import com.projity.document.Document;
import com.projity.document.ObjectEventManager;
import com.projity.document.ObjectSelectionEventManager;
import com.projity.document.ObjectEvent.Listener;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeFactory;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelDataFactory;
import com.projity.grouping.core.model.NodeModelFactory;
import com.projity.grouping.core.summaries.DeepChildSearcher;
import com.projity.job.Job;
import com.projity.job.JobRunnable;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.session.SessionFactory;
import com.projity.strings.Messages;
import com.projity.undo.DataFactoryUndoController;
import com.projity.util.Alert;

/**
 *
 */
public class Portfolio implements Document, NodeModelDataFactory {
	NodeModel nodeModel;
	ObjectEventManager objectEventManager;
	boolean creating = false;
	private transient boolean dirty;
	ProjectFactory projectFactory = null;
	/**
	 *
	 */
	public Portfolio(ProjectFactory projectFactory) {
		super();
		this.projectFactory = projectFactory;
		objectEventManager = new ObjectEventManager();
		nodeModel = NodeModelFactory.getInstance().createNodeModel(this);
		nodeModel.getHierarchy().setNbEndVoidNodes(0);
	}

	public Project findByUniqueId(long uniqueId) {
		return (Project) DeepChildSearcher.searchForUniqueId(nodeModel,uniqueId);
	}

	private class ResourcePoolFinder implements Predicate {
		public boolean evaluate(Object arg) {
			Project project = (Project)arg;
			if (project.isMaster() && !project.isReadOnly())
				return true;
			return false;
		};
	}

	public boolean isResourcePoolOpenAndWritable() {
		Project p = (Project) DeepChildSearcher.search(nodeModel,new ResourcePoolFinder());
		return (p != null);
	}


	void addProject(final Project project,boolean createJob,boolean verify) {
		if (!verify){
			_addProject(project);
			return;
		}
//		if (Environment.getStandAlone())
//			createJob = false;
		Job job=null;
		if (creating)
			return;
		Node oldNode=nodeModel.search(project,comparator);
		if (oldNode!=null){
			if (Alert.confirm(Messages.getString("Message.projectAlreadyExists"))==1){
				//TODO be sure all references are removed
				return;
			}else{
			    //removeProject((Project)oldNode.getImpl());
				job=getRemoveProjectJob((Project)oldNode.getImpl(),true);
				if (job!=null&&!createJob){
					//job.addSync(); //sync leads to a lock
			    	SessionFactory.getInstance().getSession(project.isLocal()).schedule(job);
				}
			}
		}

		if (!createJob){
			_addProject(project);
			return;
		}

	   	Job addProjectJob=new Job(SessionFactory.getInstance().getSession(project.isLocal()).getJobQueue(),"addProject","Adding project...",false);
	   	addProjectJob.addSwingRunnable(new JobRunnable("Local: addProject",1.0f){
    		public Object run() throws Exception{
    			_addProject(project);
    			setProgress(1.0f);
    			return null;
    		}
    	});
//    	job.addExceptionRunnable(new JobRunnable("Local: exception"){
//    		public Object run() throws Exception{
//    			Alert.error(Messages.getString("Message.serverError"));
//    			return null;
//    		}
//    	});
	   	if (job==null) job=addProjectJob;
	   	else job.addJob(addProjectJob);
    	SessionFactory.getInstance().getSession(project.isLocal()).schedule(job);

	}

	private void _addProject(Project project){
    	nodeModel.add(NodeFactory.getInstance().createNode(project),NodeModel.SILENT);
    	handleExternalTasks(project,true, false); 		// external link handling

    	objectEventManager.fireCreateEvent(this,project);
    	project.getResourcePool().addProject(project);
	}

	void handleExternalTasks(final Project project, final boolean opening, final boolean saving) {
		// external link handling
		forProjects(new Closure() {
			public void execute(Object arg0) {
				Project p = (Project)arg0;
				if (p != project)
					p.handleExternalTasks(project, opening, saving);
			}});

	}
	public void addSubproject(final Project child, Project parent, Project owning) {
		// parent is no longer used.
//System.out.println("addSubproject child " + child + " parent " + parent + " owning " +  owning)	;
		Node childNode = nodeModel.search(child);
		boolean modified = false;
		if (childNode == null) {
			addProject(child,false,true);
			childNode = nodeModel.search(child);
		} else {
			modified = true;
			objectEventManager.fireCreateEvent(this,child); // for mainframe to get rid of any open one
		}
		Node owningNode = nodeModel.search(owning);
		nodeModel.getHierarchy().move(childNode, owningNode, NodeModel.SILENT);

		objectEventManager.fireCreateEvent(this,child); // fire a second time too for projects view
	}




	public static ImplComparator comparator=new ImplComparator();
	public static class ImplComparator implements Comparator {
		ImplComparator() {}
		public int compare(Object node, Object impl) {
			if (node == null) // why  is node null?
				return impl == null ? 0 : 1;
			if (((Node)node).getImpl().equals(impl))
				return 0;
			else
				return 1;
		}
	}


	public Job getRemoveProjectJob(final Project project,boolean calledFromSwing) {
		return projectFactory.getRemoveProjectJob(project,true,true,calledFromSwing);
	}

	public Job getRemoveAllProjectsJob(JobRunnable exitRunnable,boolean calledFromSwing,boolean[] closeStatus){
		boolean exitOnClose=true;
		if (closeStatus!=null&&closeStatus.length>0) closeStatus[0]=true;
    	final Job job=new Job(SessionFactory.getInstance().getLocalSession().getJobQueue(),"removeAllProjects","Removing projects...",true);
		ArrayList toRemove=new ArrayList();
		for (Iterator i=nodeModel.iterator();i.hasNext();){
			Node node=(Node)i.next();
			if (!node.isRoot()) toRemove.add(node);
		}
		Project p;
		for (Iterator i=toRemove.iterator();i.hasNext();){
			p = (Project)((Node)i.next()).getImpl();
			if (p.isOpenedAsSubproject()) // subprojects are saved with their parents
				continue;
			Job rJob=getRemoveProjectJob(p,calledFromSwing);
			if (rJob==null) {
				if (calledFromSwing) exitOnClose=false; //close cancelled.
				if (closeStatus!=null&&closeStatus.length>0){
					System.out.println("Close cancelled");
					closeStatus[0]=false;
				}
			}
			else job.addJob(rJob);
		}
		if (exitOnClose) job.addRunnable(exitRunnable,!calledFromSwing,false,calledFromSwing,false);
		return job;
	}

	public void addObjectListener(Listener listener) {
		objectEventManager.addListener(listener);
	}
	public void removeObjectListener(Listener listener) {
		objectEventManager.removeListener(listener);
	}
	public ObjectEventManager getObjectEventManager() {
		return objectEventManager;
	}


	/* (non-Javadoc)
	 * @see com.projity.grouping.core.NodeModelDataFactory#createUnvalidatedObject(com.projity.grouping.core.NodeModel, java.lang.Object, java.lang.Object)
	 */
	public Object createUnvalidatedObject(NodeModel nodeModel, Object parent) {
		creating = true;
		Project project = projectFactory.createProject();
		creating = false;
		return project;
	}
	public void addUnvalidatedObject(Object object,NodeModel nodeModel, Object parent) {
	}
	/* (non-Javadoc)
	 * @see com.projity.grouping.core.NodeModelDataFactory#validateObject(java.lang.Object, com.projity.grouping.core.NodeModel, java.lang.Object, java.lang.Object)
	 */
	public void validateObject(Object newlyCreated, NodeModel nodeModel, Object eventSource, Object hierarchyInfo,boolean isNew) {
		//objectEventManager.fireCreateEvent(this,(Project)newlyCreated);
	}
//	public void fireCreated(Object newlyCreated){
//		//objectEventManager.fireCreateEvent(this,newlyCreated);
//	}
	/* (non-Javadoc)
	 * @see com.projity.grouping.core.NodeModelDataFactory#remove(java.lang.Object, com.projity.grouping.core.NodeModel)
	 */
	public void remove(Object toRemove, NodeModel nodeModel,boolean deep,boolean undo,boolean removeDependencies){
		//removeProject((Project) toRemove);
	}

	/**
	 * @return Returns the nodeModel.
	 */
	public NodeModel getNodeModel() {
		return nodeModel;
	}

	public void forProjects(Closure c){
    	Object impl;
    	for (Iterator i=getNodeModel().iterator();i.hasNext();){
    		impl=((Node)i.next()).getImpl();
    		if (!(impl instanceof Project)) continue;
    		c.execute(impl);
    	}
	}

	public Collection getDirtyProjectList() {
		final ArrayList list = new ArrayList();
		forProjects(new Closure() {
			public void execute(Object arg0) {
				if (((Project)arg0).needsSaving())
					list.add(arg0);
			}});
		return list;
	}

	public Collection getWritableProjectList() {
		final ArrayList list = new ArrayList();
		forProjects(new Closure() {
			public void execute(Object arg0) {
				if (!((Project)arg0).isReadOnly())
					list.add(arg0);
			}});
		return list;
	}

	public void fireUpdateEvent(Object source, Object object) {
		objectEventManager.fireUpdateEvent(source,object);
	}

	/* (non-Javadoc)
	 * @see com.projity.document.Document#fireMultipleTransaction(int, boolean)
	 */
	public int fireMultipleTransaction(int id, boolean begin) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.projity.document.Document#getDefaultCalendar()
	 */
	public WorkCalendar getDefaultCalendar() {
		// TODO Auto-generated method stub
		return null;
	}


	public final boolean isGroupDirty() {
		return dirty;
	}
	public final void setGroupDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public ArrayList extractCalendars() {
		return WorkingCalendar.extractCalendars(nodeModel.getHierarchy());
	}

	public DataFactoryUndoController getUndoController() {
		return null;
	}
	public void rollbackUnvalidated(NodeModel nodeModel, Object object) {
	}

	public void initOutline(NodeModel nodeModel){}

	public NodeModelDataFactory getFactoryToUseForChildOfParent(Object impl) {
		return this;
	}

	public void setAllChildrenDirty(boolean dirty) {
	}

	public boolean containsAssignments(){return false;}
	public boolean evaluate(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public ObjectSelectionEventManager getObjectSelectionEventManager() {
		// TODO Auto-generated method stub
		return null;
	}

}
