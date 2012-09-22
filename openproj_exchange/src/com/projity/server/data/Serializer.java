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

Attribution Information: Attribution Copyright Notice: Copyright ��� 2006, 2007
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
package com.projity.server.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;

import com.projity.association.AssociationList;
import com.projity.association.InvalidAssociationException;
import com.projity.company.ApplicationUser;
import com.projity.company.UserUtil;
import com.projity.configuration.CircularDependencyException;
import com.projity.configuration.Configuration;
import com.projity.configuration.FieldDictionary;
import com.projity.configuration.Settings;
import com.projity.field.FieldValues;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeFactory;
import com.projity.grouping.core.VoidNodeImpl;
import com.projity.grouping.core.model.DefaultNodeModel;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.calendar.CalendarService;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.dependency.DependencyService;
import com.projity.pm.key.HasId;
import com.projity.pm.key.HasKey;
import com.projity.pm.key.uniqueid.UniqueIdException;
import com.projity.pm.resource.EnterpriseResource;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.resource.ResourcePool;
import com.projity.pm.resource.ResourcePoolFactory;
import com.projity.pm.snapshot.Snapshottable;
import com.projity.pm.task.NormalTask;
import com.projity.pm.task.Project;
import com.projity.pm.task.SubProj;
import com.projity.pm.task.Task;
import com.projity.pm.task.TaskSnapshot;
import com.projity.server.access.ErrorLogger;
import com.projity.server.data.linker.Linker;
import com.projity.server.data.linker.ResourceLinker;
import com.projity.server.data.linker.TaskLinker;
import com.projity.session.Session;
import com.projity.session.SessionFactory;
import com.projity.strings.Messages;
import com.projity.undo.DataFactoryUndoController;
import com.projity.util.Environment;

/**
 *
 */
public class Serializer {
    public static final boolean TMP_FILES=false;

    protected Linker resourceLinker=new ResourceLinker(){
    	public Object addTransformedObjects(Object child) throws IOException, UniqueIdException{
    		Project project=(Project)parent;
    		ResourceImpl resource=(ResourceImpl)child;

    		ResourceData resourceData=new ResourceData();
    		resourceData.setUniqueId(resource.getUniqueId());
    		resourceData.setRole(resource.getRole());
            //ResourceImpl doesn't contain anything. Not serialized in V1
            //ResourceData resourceData=(ResourceData)serialize((ResourceImpl)child,ResourceData.FACTORY,null);

            EnterpriseResourceData enterpriseResourceData;
            if (resource.isDefault())
            	return null;
//        	return transformationMap;//enterpriseResourceData=null;
            else if (project.isMaster()){
            	enterpriseResourceData=(EnterpriseResourceData)serialize(resource.getGlobalResource(),EnterpriseResourceData.FACTORY,null);
            }else{
            	enterpriseResourceData=new EnterpriseResourceData(); //no need to save data
            	enterpriseResourceData.setUniqueId(resource.getGlobalResource().getUniqueId());
            }
            String emailAddress=resource.getGlobalResource().getEmailAddress();
            enterpriseResourceData.setEmailAddress((emailAddress==null||emailAddress.length()==0)?null:emailAddress); //this is used to map a new user to an existing resource
            resourceData.setEnterpriseResource(enterpriseResourceData);

            transformationMap.put(new Long(resource.getUniqueId()),resourceData); // the resource map uses ids now
            return resourceData;
    	}
    	public void executeFinally(){
    		((ProjectData)getTransformedParent()).setResources(transformed);
    	}
    	public boolean addOutlineElement(Object outlineChild,Object outlineParent,long position){
			if (outlineChild instanceof VoidNodeImpl) return false;
    		ResourceData resourceData=(ResourceData)getTransformationMap().get(new Long(((Resource)outlineChild).getUniqueId()));
			ResourceData parentData=(outlineParent==null)?null:(ResourceData)getTransformationMap().get(new Long(((Resource)outlineParent).getUniqueId()));
			EnterpriseResourceData enterpriseResourceData=resourceData.getEnterpriseResource(); //enterprise resource version
			enterpriseResourceData.setParentResource((parentData==null)?null:parentData.getEnterpriseResource()); //enterprise resource version
			enterpriseResourceData.setChildPosition(position); //enterprise resource version
			return true;
    	}

    };
    public Map saveResources(Project project,ProjectData projectData) throws Exception{
        resourceLinker.setParent(project);
    	resourceLinker.setTransformedParent(projectData);
    	resourceLinker.init();
    	resourceLinker.addTransformedObjects();
    	resourceLinker.addOutline(null); // root is null
        return resourceLinker.getTransformationMap();
    }
    
    public static interface AssignmentClosure{
    	public void execute(Assignment assignment,int snapshotId) throws IOException;
    }
    public static void forAssignments(NormalTask task,AssignmentClosure c) throws IOException{
        for (int s=0;s<Settings.numBaselines();s++){
            TaskSnapshot snapshot=(TaskSnapshot)task.getSnapshot(new Integer(s));
            if (snapshot==null) continue;
            AssociationList snapshotAssignments=snapshot.getHasAssignments().getAssignments();
            if (snapshotAssignments.size()>0){
                for (Iterator j=snapshotAssignments.iterator();j.hasNext();){
                    c.execute((Assignment)j.next(),s);
                }
            }
        }
    }


    protected TaskLinker taskLinker=new TaskLinker(){
    	public Object addTransformedObjects(Object child) throws IOException, UniqueIdException{
    		//Project project=(Project)parent;
    		//ProjectData projectData=(ProjectData)transformedParent;
    		NormalTask task=(NormalTask)child;
    		final Project project = ((Project)getParent());
	        if (task.getOwningProject() != project||task.isExternal()) // don't do tasks in subprojects, dont include externals
	        	return null;
    		final Map resourceMap=(Map)args[0];
            final TaskData taskData;
            final boolean taskDirty=!incremental||task.isDirty();
            if (taskDirty/*||Environment.isNoPodServer()*/) { //claur
//            	if (Environment.isNoPodServer()){
//            		final List persistedAssignments=new ArrayList();
//                    Project.forAssignments(task, new Project.AssignmentClosure(){
//                    	public void execute(Assignment assignment,int s){
//        						ResourceImpl r=(ResourceImpl)assignment.getResource();
//        						//if (r.isDefault()&&s==Snapshottable.CURRENT){
//        						if (r.isDefault()) persistedAssignments.add(new PersistedAssignment(assignment,s));//save the default assignment in the task
//        						else if (s!=Snapshottable.CURRENT){
//        							persistedAssignments.add(new PersistedAssignment(assignment,s,r.getUniqueId()));
//        						}
//                    	}
//                    });
//                    if (persistedAssignments.size()>0)
//                    	task.setPersistedAssignments(persistedAssignments);
//            	}
            	taskData=(TaskData)serialize(task,TaskData.FACTORY,null);
            	//task.setPersistedAssignments(null); //claur

    	        taskData.setNotes(task.getNotes()); //assignments notification
// this code is to set fields which are exposed in database
//    	        taskData.setStart(task.getStart());
//    	        taskData.setFinish(task.getEnd());
//    	        taskData.setBaselineStart(task.getBaselineStartOrZero());
//    	        taskData.setBaselineFinish(task.getBaselineFinishOrZero());
//    	        taskData.setCompletedThrough(task.getCompletedThrough());
//    	        taskData.setPercentComplete(task.getPercentComplete());
    	       // if (!taskDirty&&Environment.isNoPodServer()) taskData.setSerialized(null); //claur
            }
            else{
            	taskData=new TaskData();
            	taskData.setUniqueId(task.getUniqueId());
//            	getUnchanged().add(task.getUniqueId());
//            	return null;
            }
	        // set the status of the task using dirty flag
	        taskData.setStatus(taskDirty ? SerializedDataObject.UPDATE : 0);

        	taskData.setProjectId(task.getProjectId());
	        if (task.isSubproject()) {
	        	taskData.setSubprojectId(((SubProj)task).getSubprojectUniqueId());
	        }


            //assignments
            final Collection assignments=(flatAssignments==null)?new ArrayList():flatAssignments;
            if (taskDirty)
            forAssignments(task, new AssignmentClosure(){ //claur
            	public void execute(Assignment assignment,int s){
                    try {
						ResourceImpl r=(ResourceImpl)assignment.getResource();
						AssignmentData assignmentData=(AssignmentData)serialize(assignment,AssignmentData.FACTORY,null);
						assignmentData.setStatus(SerializedDataObject.UPDATE);

						if (flatAssignments==null) assignmentData.setTask(taskData);
						else assignmentData.setTaskId(taskData.getUniqueId());
						EnterpriseResourceData enterpriseResourceData=(r.isDefault())?
						    	null:
						    		((ResourceData)resourceMap.get(new Long(r.getUniqueId()))).getEnterpriseResource();
						if (flatAssignments==null) assignmentData.setResource(enterpriseResourceData);
						else assignmentData.setResourceId((enterpriseResourceData==null)?-1L:enterpriseResourceData.getUniqueId());
						assignmentData.setSnapshotId(s);

						assignmentData.setCachedStart(new Date(assignment.getStart()));
						assignmentData.setCachedEnd(new Date(assignment.getEnd()));
						assignmentData.setTimesheetStatus(assignment.getTimesheetStatus());
						assignmentData.setLastTimesheetUpdate(new Date(assignment.getLastTimesheetUpdate()));
						assignmentData.setWorkflowState(assignment.getWorkflowState());
						assignmentData.setPercentComplete(assignment.getPercentComplete()); //assignments notification
						assignmentData.setDuration(assignment.getDuration()); //assignments notification

						assignments.add(assignmentData);


					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            });
            if (flatAssignments==null) taskData.setAssignments(assignments);
//~            taskData.setStart(new Date(task.getStart()));
//~            taskData.setEnd(new Date(task.getEnd()));

            transformationMap.put(task,taskData);
            return taskData;
    	}
    	public void executeFinally(){
    		((ProjectData)getTransformedParent()).setTasks(transformed);
    	}
    	public boolean addOutlineElement(Object outlineChild,Object outlineParent,long position){
			TaskData taskData=(TaskData)getTransformationMap().get(outlineChild);

			//voidNodes
			if (outlineChild instanceof VoidNodeImpl){ //TODO remove not called?
				taskData=new TaskData();
				((ProjectData)getTransformedParent()).getTasks().add(taskData);
			}

			if (taskData == null) // in case belongs to different project
				return false;

			TaskData parentData=(outlineParent==null)?null:((TaskData)getTransformationMap().get(outlineParent));
//			System.out.println("parent "+parentData);
			if (parentData != null && parentData.isSubproject()) {
//				System.out.println("sub " + parentData.getName());
				parentData = null;
			}
			//if (taskData.isDirty()){
				taskData.setParentTask(parentData);
				taskData.setChildPosition(position);
			//}
			if (outlineChild instanceof Task){
				Task task=(Task)outlineChild;
				long parentId=parentData==null?-1L:parentData.getUniqueId();
				if (parentId!=task.getLastSavedParentId()||position!=task.getLastSavedPosistion()) taskData.setMoved(true);
			}

			return true;
    	}


    };

    private void markAncestorsOfDirtyTasksDirty(Project project) {
    	for(Object otask:project.getTasks()) {
    		Task task = (Task)otask;
    		if(task.isDirty()) {
    			Task parent = task.getWbsParentTask();
    			while(parent != null && !parent.isDirty()) {
    				parent.setDirty(true);
    				parent = parent.getWbsParentTask();
    			}

    		}
    	}
    }

    //flatAssignments and flatLinks mustn't be null if incremental
    protected void saveTasks(Project project,ProjectData projectData,Map resourceMap,Collection flatAssignments,Collection flatLinks,boolean incremental,SerializeOptions options) throws Exception{
    	ArrayList<Long> unchangedTasks=null;
    	ArrayList<Long> unchangedLinks=null;
    	if (incremental){
    		unchangedTasks=new ArrayList<Long>();
    		unchangedLinks=new ArrayList<Long>();
    		//taskLinker.setUnchanged(unchangedTasks);
    	}
    	this.markAncestorsOfDirtyTasksDirty(project);

    	taskLinker.setIncremental(incremental);
    	taskLinker.setFlatAssignments(flatAssignments);
    	taskLinker.setParent(project);
    	taskLinker.setTransformedParent(projectData);
    	//taskLinker.setGlobalIdsOnly(globalIdsOnly);
    	taskLinker.setArgs(new Object[]{resourceMap});
    	taskLinker.init();
    	taskLinker.setOptions(options);
    	taskLinker.addTransformedObjects();
    	taskLinker.addOutline(project.getTaskOutlineRoot());
    	long projectId = project.getUniqueId();
        //dependencies
        //Count depCount=new Count("Dependencies");
        for (Iterator i=project.getTaskOutlineIterator();i.hasNext();){
            NormalTask task=(NormalTask)i.next(); //ResourceImpl to have the EnterpriseResource link
            if (task.getProjectId() != projectId||task.isExternal()) // skip if in another project, don't write externals to server
            	continue;
	        TaskData taskData=(TaskData)taskLinker.getTransformationMap().get(task);
	        if (taskData == null)
	        	continue;

            Iterator j=task.getPredecessorList().iterator();
	        if (j.hasNext()){
	            List predecessors=new ArrayList();
	            while (j.hasNext()){
	                Dependency dependency=(Dependency)j.next();
	                LinkData linkData;
	                boolean dirty=!incremental||dependency.isDirty();
	                if (dirty) {
	                	linkData=(LinkData)serialize(dependency,LinkData.FACTORY,null);
	                }
	                else{
	                	//linkData=new LinkData();
	                	unchangedLinks.add(dependency.getPredecessorId());
	                	unchangedLinks.add(dependency.getSuccessorId());
	                	continue;
	                }
                	linkData.setDirty(dependency.isDirty());
	                //linkData.setExternalId(dependency.getExternalId());

	                if (flatLinks==null)
	                	linkData.setSuccessor(taskData);
	                else
	                	linkData.setSuccessorId(taskData.getUniqueId());

	                Task pred=(Task)dependency.getPredecessor();
	                TaskData predData=(TaskData)taskLinker.getTransformationMap().get(pred);

	                if (flatLinks==null){
		                if (predData != null && !predData.isExternal())
		                	linkData.setPredecessor(predData);
		                else {
		                	linkData.setPredecessorId(pred.getUniqueId()); // external link
		                }
		                predecessors.add(linkData);
	                } else {
	                	linkData.setPredecessorId(pred.getUniqueId());
	                	flatLinks.add(linkData);

	                }
	            }
	            if (flatLinks==null)
		            taskData.setPredecessors(predecessors);

	        }

        }

        //depCount.dump();

    	if (incremental){
    		//if (unchangedTasks.size()>0){
    		Collection tasks=projectData.getTasks();
    		if (tasks!=null)
    		for(Iterator i=tasks.iterator();i.hasNext();){
    			TaskData t=(TaskData)i.next();
    			if (!t.isDirty()&&!t.isMoved()){
    				unchangedTasks.add(t.getUniqueId());
    				i.remove();
    			}
    		}
    		if (unchangedTasks.size()>0){
    			long[] a=new long[unchangedTasks.size()];
    			int i=0;
    			for (long l:unchangedTasks) a[i++]=l;
    			projectData.setUnchangedTasks(a);
    		}
    		if (unchangedLinks.size()>0){
    			long[] a=new long[unchangedLinks.size()];
    			int i=0;
    			for (long l:unchangedLinks) a[i++]=l;
    			projectData.setUnchangedLinks(a);
    		}
    	}

        return; //taskLinker.getTransformationMap();
    }

    public DocumentData serializeDocument(Project project) throws Exception{
    	return serializeProject(project,null,null,false,null);
    }

    public ProjectData serializeProject(Project project) throws Exception{
    	return serializeProject(project,null,null,false,null);
    }
    public ProjectData serializeProject(Project project,Collection flatAssignments,Collection flatLinks,boolean incremental,SerializeOptions options) throws Exception{
    	if (TMP_FILES) initTmpDir();
    	if (project.isForceNonIncremental()) incremental=false;
    	boolean incrementalDistributions=incremental&&!project.isForceNonIncrementalDistributions();

 //   	calendars.clear();
        Count projectCount=new Count("Project");
        //if (globalIdsOnly) makeGLobal(project);
        ProjectData projectData=(ProjectData)serialize(project,ProjectData.FACTORY,projectCount);
        if (project.isForceNonIncremental()) projectData.setVersion(0);
        projectData.setMaster(project.isMaster());
//        projectData.setExternalId(project.getExternalId());

        //exposed attributes
//        projectData.setAttributes(SpreadSheetFieldArray.convertFields(project, "projectExposed", new Transformer(){
//        	public Object transform(Object value) {
//        		if (value instanceof Money) return ((Money)value).doubleValue();
//        		return null;
//        	}
//        }));

        projectCount.dump();


        //resources
        Map resourceMap=saveResources(project,projectData);

        //tasks
        saveTasks(project,projectData,resourceMap,flatAssignments,flatLinks,incremental,options);

        //distribution
        long t=System.currentTimeMillis();
        Collection<DistributionData> dist=(Collection<DistributionData>)(new DistributionConverter()).createDistributionData(project,incrementalDistributions);
    	if (dist==null){
    		dist=new ArrayList<DistributionData>();
    	}
		projectData.setDistributions(dist);
		projectData.setIncrementalDistributions(incrementalDistributions);

    	TreeMap<DistributionData, DistributionData> distMap=project.getDistributionMap();
    	if (distMap==null){
    		distMap=new TreeMap<DistributionData, DistributionData>(new DistributionComparator());
    		project.setDistributionMap(distMap);
    	}
    	TreeMap<DistributionData, DistributionData> newDistMap=new TreeMap<DistributionData, DistributionData>(new DistributionComparator());
    	//ArrayList<DistributionData> toInsertInOld=new ArrayList<DistributionData>();

    	//insert, update dist
    	for (Iterator<DistributionData> i=dist.iterator();i.hasNext();){
    		DistributionData d=i.next();
    		if (incrementalDistributions){
	    		DistributionData oldD=distMap.get(d);
	    		if (oldD==null){
	    			d.setStatus(DistributionData.INSERT);
	    		}else{
	    			if (oldD.getWork()==d.getWork()&&oldD.getCost()==d.getCost()){
	    				//System.out.println(d+" did not change");
	    				d.setStatus(0);
	    				i.remove();
	    			}
	    			else d.setStatus(DistributionData.UPDATE);
	    		}
    		}else{
    			d.setStatus(DistributionData.INSERT);
    		}
			newDistMap.put(d,d);
    	}
    	//remove dist
    	if (incrementalDistributions&&distMap.size()>0){
        	Set<Long> noChangeTaskIds=new HashSet<Long>();

			Task task;
			for(Iterator i = project.getTaskOutlineIterator();i.hasNext();) {
				task = (Task)i.next();
				if(incremental&&!task.isDirty()) noChangeTaskIds.add(task.getUniqueId());
			}
//        	for (Iterator i=projectData.getTasks().iterator();i.hasNext();){
//        		TaskData task=(TaskData)i.next();
//        		if (!task.isDirty()) noChangeTaskIds.add(task.getUniqueId());
//        	}
        	for (Iterator<DistributionData> i=distMap.values().iterator();i.hasNext();){
        		DistributionData d=i.next();
        		if (newDistMap.containsKey(d)) continue;
        		if (noChangeTaskIds.contains(d.getTaskId())){
        			d.setStatus(0);
        			newDistMap.put(d, d);
        		}else{
        			d.setStatus(DistributionData.REMOVE);
        			dist.add(d);
        		}
        	}
    	}
    	project.setNewDistributionMap(newDistMap);
    	System.out.println("Distributions generated in "+(System.currentTimeMillis()-t)+" ms");


    	// send project field values to server too
        HashMap fieldValues = FieldValues.getValues(FieldDictionary.getInstance().getProjectFields(),project);
        if (project.getContainingSubprojectTask() != null) { // special case in which we want to use the duration from subproject task
        	Object durationFieldValue = Configuration.getFieldFromId("Field.duration").getValue(project.getContainingSubprojectTask(), null);
        	fieldValues.put("Field.duration", durationFieldValue);
        }
        projectData.setFieldValues(fieldValues);
        projectData.setGroup(project.getGroup());
        projectData.setDivision(project.getDivision());
        projectData.setExpenseType(project.getExpenseType());
        projectData.setProjectType(project.getProjectType());
        projectData.setProjectStatus(project.getProjectStatus());
        projectData.setExtraFields(project.getExtraFields());
        projectData.setAccessControlPolicy(project.getAccessControlPolicy());
        projectData.setCreationDate(project.getCreationDate());
        projectData.setLastModificationDate(project.getLastModificationDate());
        //  	System.out.println("done serialize project " + project);

//        Collection<DistributionData> dis=(Collection<DistributionData>)projectData.getDistributions();
//        for (DistributionData d: dis) System.out.println("Dist: "+d.getTimeId()+", "+d.getType()+", "+d.getStatus());

//        project.setNewTaskIds(null);
//        if (projectData.getTasks()!=null){
//        	Set<Long> ids=new HashSet<Long>();
//        	project.setNewTaskIds(ids);
//        	for (TaskData task:(Collection<TaskData>)projectData.getTasks()){
//        		ids.add(task.getUniqueId());
//        	}
//        }
//        long[] unchangedTasks=projectData.getUnchangedTasks();
//        if (unchangedTasks!=null){
//        	Set<Long> ids=project.getNewTaskIds();
//        	if (ids==null){
//        		ids=new HashSet<Long>();
//        		project.setNewTaskIds(ids);
//        	}
//        	for (int i=0;i<unchangedTasks.length;i++) ids.add(unchangedTasks[i]);
//        }
//
//        project.setNewLinkIds(null);
//        if (flatLinks!=null){
//        	Set<DependencyKey> ids=new HashSet<DependencyKey>();
//        	project.setNewLinkIds(ids);
//        	for (LinkData link:(Collection<LinkData>)flatLinks){
//        		ids.add(new DependencyKey(link.getPredecessorId(),link.getSuccessorId()/*,link.getExternalId()*/));
//        	}
//        }
//        long[] unchangedLinks=projectData.getUnchangedLinks();
//        if (unchangedLinks!=null){
//        	Set<DependencyKey> ids=project.getNewLinkIds();
//        	if (ids==null){
//        		ids=new HashSet<DependencyKey>();
//        		project.setNewLinkIds(ids);
//        	}
//        	for (int i=0;i<unchangedLinks.length;i+=2) ids.add(new DependencyKey(unchangedLinks[i],unchangedLinks[i+1]));
//        }


        //project.setNewIds(); //claur - useful ?

        return projectData;

    }
//    public ProjectData serializeResources(Project project) throws Exception{
//    	//if (globalIdsOnly) makeGLobal(project);
//    	ProjectData projectData=(ProjectData)serialize(project,ProjectData.FACTORY,null);
//        //projectData just here to hold resources
//        saveResources(project,projectData);
//        return projectData;
//    }



   //incremental serialization
/*
    public IncrementalData serializeIncrementalProject(Project project) throws Exception{
        if (!project.isGroupDirty()) return null;
        final IncrementalData newData=new IncrementalData();
        final IncrementalData oldData=(IncrementalData)project.getPublishedData().clone();
    	ProjectData projectData=serializeProject(project,null,null);
    	//TODO useless serialization are done in serializeProject, use dirty tag

    	for (Iterator i=projectData.getTasks().iterator();i.hasNext();){
    		TaskData t=(TaskData)i.next();
    		for (Iterator j=t.getAssignments().iterator();j.hasNext();){
       			AssignmentData data=(AssignmentData)j.next();
        		if (oldData.getAssignments().contains(data)){
        			if (data.isDirty()){
//            			System.out.println("UPDATE: "+data);
        				newData.addAssignment(data);
        			}
        			oldData.getAssignments().remove(data);
        		}else{
        			data.setStatus(SerializedDataObject.INSERT);
//        			System.out.println("INSERT: "+data);
    				newData.addAssignment(data);
        		}
    		}
    		for (Iterator j=t.getPredecessors().iterator();j.hasNext();){
       			LinkData data=(LinkData)j.next();
        		if (oldData.getLinks().contains(data)){
        			if (data.isDirty()){
//            			System.out.println("UPDATE: "+data);
        				newData.addLink(data);
        			}
        			oldData.getLinks().remove(data);
        		}else{
        			data.setStatus(SerializedDataObject.INSERT);
//        			System.out.println("INSERT: "+data);
    				newData.addLink(data);
        		}
    		}

    		if (oldData.getTasks().containsKey(t)){
    			TaskData oldT=(TaskData)oldData.getTasks().get(t);
    			if (t.getParentTaskId()==oldT.getParentTaskId()&&t.getCalendarId()==t.getChildPosition()){
        			if (t.isDirty()){
//            			System.out.println("UPDATE: "+t);
        				newData.addTask(t);
        			}
    			}else{
    				t.setStatus(t.getStatus()|SerializedDataObject.MOVE);
//        			System.out.println("MOVE: "+t);
    				newData.addTask(t);
    			}
    			oldData.getTasks().remove(t);
    		}else{
    			t.setStatus(SerializedDataObject.INSERT);
//    			System.out.println("INSERT: "+t);
				newData.addTask(t);
    		}
    		//calendars?
    	}
    	for (Iterator i=projectData.getResources().iterator();i.hasNext();){
    		ResourceData r=(ResourceData)i.next();

    		//TODO r.getEnterpriseResource()
    		if (oldData.getResources().contains(r)){
    			if (r.isDirty()){
//        			System.out.println("INSERT: "+r);
    				newData.addResource(r);
    			}
    			oldData.getResources().remove(r);
    		}else{
    			r.setStatus(SerializedDataObject.INSERT);
//    			System.out.println("INSERT: "+r);
				newData.addResource(r);
    		}
    		//calendars?
    	}

    	//REMOVE
    	for (Iterator i=oldData.getResources().iterator();i.hasNext();){
    		ResourceData data=(ResourceData)i.next();
    		data.setStatus(SerializedDataObject.REMOVE);
//			System.out.println("REMOVE: "+data);
    		newData.addResource(data);
    	}
    	for (Iterator i=oldData.getTasks().keySet().iterator();i.hasNext();){
    		TaskData data=(TaskData)i.next();
    		data.setStatus(SerializedDataObject.REMOVE);
//			System.out.println("REMOVE: "+data);
    		newData.addTask(data);
    	}
    	for (Iterator i=oldData.getAssignments().iterator();i.hasNext();){
    		AssignmentData data=(AssignmentData)i.next();
    		data.setStatus(SerializedDataObject.REMOVE);
//			System.out.println("REMOVE: "+data);
    		newData.addAssignment(data);
    	}
    	for (Iterator i=oldData.getLinks().iterator();i.hasNext();){
    		LinkData data=(LinkData)i.next();
    		data.setStatus(SerializedDataObject.REMOVE);
//			System.out.println("REMOVE: "+data);
    		newData.addLink(data);
    	}




//    	if (project.getCalendar()!=null){
    		//TODO calendar?
    		//base calendars to handle?
		if (oldData.getResources().contains(projectData)){
			if (projectData.isDirty()){
//				System.out.println("UPDATE: "+projectData);
				newData.setProject(projectData);
			}
		}
        return newData;
    }
 */

    //deserialization

    public Project deserializeLocalDocument(DocumentData documentData) throws IOException, ClassNotFoundException {
    	return deserializeProject((ProjectData)documentData,false,SessionFactory.getInstance().getLocalSession(),null,null);
    }

    /**
     * enterpriseResources to use instead of enterprise resources given by projectData
     */
    public Project deserializeProject(ProjectData projectData, final boolean subproject, final Session reindex, Map enterpriseResources) throws IOException, ClassNotFoundException {
    	return deserializeProject(projectData, subproject, reindex, enterpriseResources,null,true);
    }
    public Project deserializeProject(ProjectData projectData, final boolean subproject, final Session reindex, Map enterpriseResources,Closure loadResources) throws IOException, ClassNotFoundException {
    	return deserializeProject(projectData, subproject, reindex, enterpriseResources,loadResources,true);
    }

    //DEF165936: 	Projity: .pod file import fails mapped to resource with modified calendar
    //the only way i found to make this work was to pass over the original ResourceImpls mapped by selected resource Id
//    private Project _existingProject = null;
//    Map<Long, Resource> _localResourceMap;
//    public void SetStuffForPODDeserialization(Project existingProject, Map<Long, Resource> localResourceMap)
//    {
//    	_existingProject = existingProject;
//    	_localResourceMap = localResourceMap;
//    }
    public Project deserializeProject(ProjectData projectData, final boolean subproject, final Session reindex, Map enterpriseResources,Closure loadResources,boolean updateDistribution) throws IOException, ClassNotFoundException {
    	DataFactoryUndoController undoController=new DataFactoryUndoController();
    	Project project=(Project)deserialize(projectData,reindex);
    	project.setUndoController(undoController);
    	project.setMaster(projectData.isMaster()); //not necessary
    	project.setLocal(projectData.isLocal());
    	project.setReadOnly(!projectData.canBeUsed());
    	project.setCreationDate(projectData.getCreationDate());
    	project.setLastModificationDate(projectData.getLastModificationDate());
    	//project.setExternalId(projectData.getExternalId());
    	boolean fixCorruption=false;

    	//IncrementalData incremental=new IncrementalData();

    	//calendar
//  	WorkCalendar calendar = project.getWorkCalendar();
//  	if (projectData.getCalendar()==null) {
//  	System.out.println("deserializing null project calendar");
//  	calendar= CalendarService.getInstance().getStandardBasedInstance(project);
//  	} else {
//  	calendar= (WorkingCalendar)deserializeCalendar(projectData.getCalendar());
//  	calendar.setDocument(project);
//  	CalendarService.getInstance().add(calendar);
//  	}
//  	CalendarService.getInstance().add((WorkingCalendar) calendar);


    	//calendar
    	//TODO this code only exists to guarantee that older projects wont crash when read 25/8/05
    	WorkCalendar calendar=project.getWorkCalendar();
    	if (calendar==null)
    		calendar = CalendarService.getInstance().getDefaultInstance();

    	project.setWorkCalendar(calendar); // needed for objects using
    	project.setExtraFields(projectData.getExtraFields());

    	project.setGroup(projectData.getGroup());
    	project.setDivision(projectData.getDivision());
    	project.setExpenseType(projectData.getExpenseType());
    	project.setProjectType(projectData.getProjectType());
    	project.setProjectStatus(projectData.getProjectStatus());
    	project.setAccessControlPolicy(projectData.getAccessControlPolicy());

    	project.postDeserialization();


    	//resources
    	final Map resourceNodeMap=new HashMap();
    	ResourcePool resourcePool = ResourcePoolFactory.getInstance().createResourcePool(project.getName(),undoController);
    	resourcePool.setMaster(project.isMaster());
    	resourcePool.setLocal(project.isLocal());
    	resourcePool.updateOutlineTypes();
    	Collection resources=projectData.getResources();
    	if (resources!=null)
    		//order by position parents don't matter
    		Collections.sort((List<ResourceData>)resources,new Comparator<ResourceData>(){
    			public int compare(ResourceData resource1, ResourceData resource2) {
    				if (resource1.getChildPosition()>resource2.getChildPosition()) return 1;
    				else return -1;
    			}
    		});

    	if (resources!=null)
    		for (Iterator i=resources.iterator();i.hasNext();){
    			ResourceData resourceData=(ResourceData)i.next();
    			ResourceImpl resource=deserializeResourceAndAddToPool(resourceData,resourcePool,reindex,enterpriseResources);
    			
       			//Change for DEF165936 but doesn't work
    			//Resource origImpl =  _localResourceMap.get(resourceData.getUniqueId());
    			//resourceNodeMap.put(resourceData.getEnterpriseResource(),NodeFactory.getInstance().createNode(origImpl));
     			resourceNodeMap.put(resourceData.getEnterpriseResource(),NodeFactory.getInstance().createNode(resource));
    		}
    	project.setResourcePool(resourcePool);

    	//resource outline
    	/* version with outline on project resource
    	 * if (resources!=null){
            for (Iterator i=resources.iterator();i.hasNext();){
                ResourceData resourceData=(ResourceData)i.next();
                ResourceData parentData=(ResourceData)resourceData.getParentResource();
                Node node=(Node)resourceNodeMap.get(resourceData.getEnterpriseResource());
                Node parentNode=(parentData==null)?
                		null:
                		((Node)resourceNodeMap.get(parentData.getEnterpriseResource()));
                project.getResourcePool().addToDefaultOutline(parentNode,node,(int)resourceData.getChildPosition());
            }
        }*/
    	if (resources!=null){

    		for (Iterator i=resources.iterator();i.hasNext();){
    			ResourceData resourceData=(ResourceData)i.next();
    			EnterpriseResourceData enterpriseResourceData=resourceData.getEnterpriseResource();
    			EnterpriseResourceData parentData=enterpriseResourceData.getParentResource();
    			Node node=(Node)resourceNodeMap.get(enterpriseResourceData);
    			Node parentNode=(parentData==null)?
    					null:
    						((Node)resourceNodeMap.get(parentData));
    			project.getResourcePool().addToDefaultOutline(parentNode,node,(int)enterpriseResourceData.getChildPosition(),false);
    			((ResourceImpl)node.getImpl()).getGlobalResource().setResourcePool(project.getResourcePool());
    		}
    		project.getResourcePool().getResourceOutline().getHierarchy().cleanVoidChildren();

    		//renumber resources
    		project.getResourcePool().getResourceOutline().getHierarchy().visitAll(new Closure(){
    			int id=1;
    			public void execute(Object o) {
    				Node node=(Node)o;
    				if (node.getImpl() instanceof HasId){
    					HasId impl=(HasId)node.getImpl();
    					if (impl.getId()>0) impl.setId(id++); //if id=0 means id not used
    				}
    			}

    		});
    	}

    	if (loadResources!=null){
    		loadResources.execute(project);
    		resourceNodeMap.clear();
    		project.getResourcePool().getResourceOutline().getHierarchy().visitAll(new Closure(){
    			public void execute(Object o) {
    				Node node=(Node)o;
    				HasKey k=(HasKey)node.getImpl();
    				resourceNodeMap.put(k.getUniqueId(), node);
    			}

    		});
    	}


    	//tasks
    	Collection tasks=projectData.getTasks();
    	Map taskNodeMap=new HashMap();
    	long projectId = project.getUniqueId();
    	NormalTask task;

    	if (tasks!=null){
    		//order by position parents don't matter
    		Collections.sort((List<TaskData>)tasks,new Comparator<TaskData>(){
    			public int compare(TaskData task1, TaskData task2) {
    				if (!task1.isExternal() && task2.isExternal()) return -1; //keep external tasks at the end
    				else if (task1.isExternal() && !task2.isExternal()) return 1;
    				else if (task1.getChildPosition()>task2.getChildPosition()) return 1;
    				else if (task1.getChildPosition()<task2.getChildPosition()) return -1;
    				else return 0;
    			}
    		});

    		//Set<Long> initialTaskIds=new HashSet<Long>();
    		//project.setInitialTaskIds(initialTaskIds);
    		for (Iterator i=tasks.iterator();i.hasNext();){
    			task = null;
    			TaskData taskData=(TaskData)i.next();
//  			initialTaskIds.add(taskData.getUniqueId());
    			if (taskData.isDirty()) fixCorruption=true; //recovers errors
//    			if (Environment.isAddSummaryTask()&&taskData.getUniqueId()==Task.SUMMARY_UNIQUE_ID&&taskData.getSerialized()==null){ //claur
//					System.out.println("Fixing null binary summary task");
//					task = new NormalTask(project);
//					task.setName(taskData.getName());
//					task.setUniqueId(taskData.getUniqueId());
//    			}else
    			if (taskData.getSerialized()==null) {
    				if (taskData.isTimesheetCreated()) {
    					task = new NormalTask(project);
    					task.setName(taskData.getName());
    					System.out.println("made new task in serializer " + task + " parent " + taskData.getParentTask().getName());
    				} else {
    					continue; // void node
    				}
    			} else {
    				try {
    					task = (NormalTask)deserialize(taskData,reindex);
    				} catch (Exception e) {
    					if (taskData.isSubproject()){ //For migration
    						try {
    							task = (NormalTask) Class.forName(Messages.getMetaString("Subproject")).getConstructor(new Class[]{Project.class,Long.class}).newInstance(project,taskData.getSubprojectId());
    						} catch (Exception e1) {
    							e1.printStackTrace();
    						}

//  						task=new Subproject(project,taskData.getSubprojectId());
    						task.setUniqueId(taskData.getUniqueId());
    						task.setName(taskData.getName());
    						((SubProj)task).setSubprojectFieldValues(taskData.getSubprojectFieldValues());
    					}
    					else{
    						e.printStackTrace();
    						throw new IOException("Subproject:"+e);
    					}
    				}
    			}
    			taskNodeMap.put(taskData,NodeFactory.getInstance().createNode(task));
    			task.setProject(project);
    			project.initializeId(task);
    			project.add(task);
    			if (taskData.isExternal()) {
    				task.setExternal(true);
    				task.setProjectId(taskData.getProjectId());
    				task.setAllSchedulesToCurrentDates();
    				project.addExternalTask(task);
    			} else {
    				task.setOwningProject(project);
    				task.setProjectId(projectId);
    			}
    			if (taskData.isSubproject()) {
    				SubProj sub = (SubProj)task;
    				sub.setSubprojectUniqueId(taskData.getSubprojectId());
    				sub.setSubprojectFieldValues(taskData.getSubprojectFieldValues());
    				sub.setSchedulesFromSubprojectFieldValues();
    			}
//    			if (task.isRoot()){ //claur
//    				project.setSummaryTaskEnabled(true);
//    			}

    			WorkingCalendar cal=(WorkingCalendar) task.getWorkCalendar();
    			if (cal!=null){ // use global one
    				WorkingCalendar newCal = (WorkingCalendar) CalendarService.findBaseCalendar(cal.getName());
    				if (newCal != null && newCal != cal)
    					task.setWorkCalendar(newCal);
    			}

    			//project.addToDefaultOutline(null,);


    			//assignments
    			List assignments=new ArrayList();
//    			if (Environment.isNoPodServer()&&task.getPersistedAssignments()!=null){ //claur
//    				assignments.addAll(task.getPersistedAssignments());
//    			}
    			if (taskData.getAssignments()!=null) assignments.addAll(taskData.getAssignments());

    			if (assignments.size()>0)
				for (Iterator j=assignments.iterator();j.hasNext();){
					Object obj=j.next();
					AssignmentData assignmentData=null;
//					if (loadResources!=null&&obj instanceof PersistedAssignment){ //claur
//					}else{
						assignmentData=(AssignmentData)obj;
						if (assignmentData.getSerialized() == null) { // timesheet created
								System.out.println("==== no cached start found " + task.getName());
								if (assignments.size()==1)
									assignmentData.setResourceId(-1L);
								else j.remove();
						}
//					}
				}

    			if (assignments.size()>0)
    				for (Iterator j=assignments.iterator();j.hasNext();){
    					Object obj=j.next();
    					AssignmentData assignmentData=null;
    					Assignment assignment=null;
    					Resource resource;
    					boolean assigned=true;
    					int s;
//    					if (loadResources!=null&&obj instanceof PersistedAssignment){ //claur
//    						PersistedAssignment pa=(PersistedAssignment)obj;
//    						assignment=pa.getAssignment();
//    						s=pa.getSnapshot();
//
//   							long resId=pa.getResourceId();
//							Node node=(Node)resourceNodeMap.get(resId);
//							resource=node==null?ResourceImpl.getUnassignedInstance():(Resource)node.getImpl();
//
//							if (resource==null) assigned=false;
//    					}else{
    						assignmentData=(AssignmentData)obj;
    						if (loadResources==null){
    							EnterpriseResourceData r=assignmentData.getResource();
    							if (r==null) assigned=false;
    							resource=(r==null)?ResourceImpl.getUnassignedInstance():(Resource)((Node)resourceNodeMap.get(r)).getImpl();
    						}else{
    							long resId=assignmentData.getResourceId();
    							Node node=(Node)resourceNodeMap.get(resId);
    							resource=node==null?ResourceImpl.getUnassignedInstance():(Resource)node.getImpl();
    						}
    						if (assignmentData.getSerialized() != null){
    							try {
    								assignment=(Assignment)deserialize(assignmentData,reindex);
    							} catch (Exception e) {
    								e.printStackTrace();
    							}
    						}
    						if (assignmentData.getSerialized() == null||(assignmentData.getSerialized() != null&&assignment==null)) { // timesheet created
    							assignment = Assignment.getInstance(task,resource,	1.0, 0);
    							if (assignment.getCachedStart() == null) { //doesn't occur filtered above
    								System.out.println("==== no cached start found " + task.getName());

    							} else {
    								task.setActualStart(assignment.getCachedStart().getTime());
    								task.setActualFinish(assignment.getCachedEnd().getTime());
    							}
    						}
    						assignment.setCachedStart(assignmentData.getCachedStart());
    						assignment.setCachedEnd(assignmentData.getCachedEnd());
    						assignment.setTimesheetStatus(assignmentData.getTimesheetStatus());
    						long lastUpdate = (assignmentData.getLastTimesheetUpdate() == null) ? 0 : assignmentData.getLastTimesheetUpdate().getTime();
    						assignment.setLastTimesheetUpdate(lastUpdate);
    						assignment.setWorkflowState(assignmentData.getWorkflowState());
    						s=assignmentData.getSnapshotId();
//    					}

    					assignment.getDetail().setTask(task);
    					assignment.getDetail().setResource(resource);
    					Object snapshotId=new Integer(s);
    					TaskSnapshot snapshot=(TaskSnapshot)task.getSnapshot(snapshotId);

    					//TODO was commented but needed for loading  because task.getSnapshot(snapshotId)==null
    					//for snapshots other than CURRENT
    					if (snapshot==null){
    						snapshot=new TaskSnapshot();
    						snapshot.setCurrentSchedule(task.getCurrentSchedule());
    						task.setSnapshot(snapshotId,snapshot);
    					}
    					if (Snapshottable.TIMESHEET.equals(snapshotId)) {
    						assignment.setTimesheetAssignment(true);
    					}
    					//

    					snapshot.addAssignment(assignment);

    					if (assigned&&Snapshottable.CURRENT.equals(snapshotId)) resource.addAssignment(assignment);

    					if (assignmentData!=null) assignmentData.emtpy();
    					//incremental.addAssignment(assignmentData);
    				}
//    			task.setPersistedAssignments(null);
    		}


    		// the collection which holds a list of corresponding subproject tasks for projects which include this project
    		// note that their task names have been transformed to hold the name of the project
    		Collection referringSubprojectTaskData=projectData.getReferringSubprojectTasks();
    		if (tasks!=null&&referringSubprojectTaskData!=null){
    			ArrayList referringSubprojectTasks = new ArrayList(referringSubprojectTaskData.size());
    			project.setReferringSubprojectTasks(referringSubprojectTasks);
    			for (Iterator i=referringSubprojectTaskData.iterator();i.hasNext();){
    				TaskData taskData=(TaskData)i.next();
    				String projectName = taskData.getName(); // it was set to the referrig project name by synchronizer
    				task = null;
    				try {
    					task = (NormalTask)deserialize(taskData,reindex);
    				} catch (Exception e) {
    					if (taskData.isSubproject()){ //For migration
    						task=(NormalTask) project.getSubprojectHandler().createSubProj(taskData.getSubprojectId());
    						task.setUniqueId(taskData.getUniqueId());
    						task.setName(taskData.getName());
    						((SubProj)task).setSubprojectFieldValues(taskData.getSubprojectFieldValues());
    					}
    					else throw new IOException("Subproject:"+e);
    				}
    				task.setName(projectName);
    				task.setProjectId(taskData.getProjectId());
    				referringSubprojectTasks.add(task);
    			}
    		}

    		//dependencies
    		//Set<DependencyKey> initialLinkIds=null;
    		for (Iterator i=projectData.getTasks().iterator();i.hasNext();){
    			TaskData successorssorData=(TaskData)i.next();
    			if (successorssorData.getPredecessors()!=null){
    				final Task successor=(Task)((Node)taskNodeMap.get(successorssorData)).getImpl();
    				for (Iterator j=successorssorData.getPredecessors().iterator();j.hasNext();){
    					LinkData linkData=(LinkData)j.next();
//  					if (initialLinkIds==null){
//  					initialLinkIds=new HashSet<DependencyKey>();
//  					project.setInitialLinkIds(initialLinkIds);
//  					}
//  					initialLinkIds.add(new DependencyKey(linkData.getPredecessorId(),linkData.getSuccessorId()/*,externalId*/));
    					Dependency dependency=(Dependency)deserialize(linkData,reindex);

    					if (linkData.getPredecessor() == null) {
    						System.out.println("null pred - this shouldn't happen. skipping"); // todo treat it
    						continue;
    					}
    					final Task predecessor=(Task)((Node)taskNodeMap.get(linkData.getPredecessor())).getImpl();
    					connectDependency(dependency,predecessor,successor);

    					linkData.emtpy(); //why is this there?
    				}
    			}
    		}

    	}

    	//task outline
    	if (tasks!=null){

    		//add missing summary task
    		Node summaryNode=null;
//    		if (Environment.isAddSummaryTask()&&!project.isSummaryTaskEnabled() //needed for import, add other conditions? //claur
//    			&& (tasks.size()==0||((TaskData)tasks.iterator().next()).getUniqueId()!=Task.SUMMARY_UNIQUE_ID)){
//    			NormalTask projityTask = project.newNormalTaskInstance(false);
//    			projityTask.setName(Messages.getString("Text.DefaultSummaryTaskName"));
//    			projityTask.setUniqueId(DataObject.SUMMARY_UNIQUE_ID);
//    			projityTask.setOwningProject(project);
//    			projityTask.setProjectId(project.getUniqueId());
//    			summaryNode = NodeFactory.getInstance().createNode(projityTask); // get a node for this task
//    			project.addToDefaultOutline(null,summaryNode);
//    			project.setSummaryTaskEnabled(true);
//    		}


    		Map<Long, Node> subprojectsMap=new HashMap<Long, Node>();
    		for (Iterator i=tasks.iterator();i.hasNext();){
    			TaskData taskData=(TaskData)i.next();
    			TaskData parentData=taskData.getParentTask();
//  			if (taskData.isTimesheetCreated())
//  			System.out.println("timesheet created parent is  " + parentData == null ? null : parentData.getName());
    			Node node;
    			if (taskData.getSerialized()==null /*&& taskData.getUniqueId()!=Task.SUMMARY_UNIQUE_ID*/ &&!taskData.isTimesheetCreated()) //void node //claur
    				node=NodeFactory.getInstance().createVoidNode();
    			else node=(Node)taskNodeMap.get(taskData);
    			Node parentNode=null;
    			int position=-1;
    			if (taskData.isExternal()){
    				Node previous=subprojectsMap.get(taskData.getProjectId());
    				if (previous!=null) parentNode=(Node)previous.getParent();
    				if (parentNode!=null){
    					position=parentNode.getIndex(previous)+1;
    					if (parentNode.isRoot()) parentNode=null;
    				}
    			}
    			if (position==-1){
    				if (parentData==null&&summaryNode!=null)
    					parentNode=summaryNode;
    				else
    					parentNode=(parentData==null)?
    						null:
    							((Node)taskNodeMap.get(parentData));
    				position=(int)taskData.getChildPosition();
    			}
    			if (taskData.isTimesheetCreated())
    				System.out.println("new task " + node + "parent node is " + parentNode);
    			if (node.getImpl() instanceof SubProj){
    				SubProj sub=(SubProj)node.getImpl();
    				subprojectsMap.put(sub.getSubprojectUniqueId(), node);
    			}

    			project.addToDefaultOutline(parentNode,node,position,false);

    			taskData.emtpy();
    			//incremental.addTask(taskData);

    		}
    		//renumber tasks and save outline
    		project.getTaskOutline().getHierarchy().visitAll(new Closure(){
    			int id=1;
    			public void execute(Object o) {
    				Node node=(Node)o;
    				if (node.getImpl() instanceof HasId){ //renumber
    					HasId impl=(HasId)node.getImpl();
    					if (impl.getId()>0) impl.setId(id++); //if id=0 means id not used
    				}
//  				if (node.getImpl() instanceof Task){ //save outline
//  				Task t=(Task)node.getImpl();
//  				Node parent=(Node)node.getParent();
//  				if (parent==null||parent.isRoot()) t.setLastSavedParentId(-1L);
//  				else t.setLastSavedParentId(((Task)parent.getImpl()).getUniqueId());
//  				t.setLastSavedPosistion(parent.getIndex(node));
//  				}
    				//done in setAllTasksAsUnchangedFromPersisted
    			}

    		});


    	}


    	if (resources!=null)
    		for (Iterator i=resources.iterator();i.hasNext();){
    			ResourceData resourceData=(ResourceData)i.next();
    			EnterpriseResourceData enterpriseResourceData=resourceData.getEnterpriseResource();
    			resourceData.emtpy();
    			//incremental.addResource(resourceData);
    			enterpriseResourceData.emtpy();
    			//incremental.addEnterpriseResource(enterpriseResourceData);

    		}



    	((DefaultNodeModel)project.getTaskOutline()).setDataFactory(project);



    	project.initialize(subproject,updateDistribution&&!fixCorruption);

    	projectData.emtpy();
    	//incremental.setProject(projectData); //remove

    	(new DistributionConverter()).substractDistributionFromProject(project);


    	//distribution map
    	//project.updateDistributionMap();


    	if (fixCorruption) project.setForceNonIncremental(true);
    	if (project.getVersion()<1.2){
    		project.setForceNonIncrementalDistributions(true);
    	}
    	project.setVersion(Project.CURRENT_VERSION);

    	return project;
    }


    public static void connectDependency(Dependency dependency,Task predecessor,Task successor){
		try {
			DependencyService.getInstance().initDependency(dependency,predecessor,successor,null);
		} catch (InvalidAssociationException e) {
			dependency.setDisabled(true);
			try { // try a second time now that it's disabled
				DependencyService.getInstance().initDependency(dependency,predecessor,successor,null);
			} catch (InvalidAssociationException e1) {
				e1.printStackTrace();
			}
			DependencyService.warnCircularCrossProjectLinkMessage(predecessor, successor);
		}

    }


//    protected Map calendars=new HashMap();
//    protected CalendarData serializeCalendar(WorkCalendar calendar,boolean globalIdsOnly) throws IOException,UniqueIdException{
//        Count calendarsCount=new Count("Calendars");
//        if (calendars.containsKey(calendar))
//            return (CalendarData)calendars.get(calendar);
//    	if (globalIdsOnly) makeGLobal(calendar);
//        CalendarData calendarData=(CalendarData)serialize(calendar,CalendarData.FACTORY,calendarsCount);
//        if (calendar instanceof WorkingCalendar){
//            WorkCalendar baseCalendar=((WorkingCalendar)calendar).getBaseCalendar();
//            if (baseCalendar!=null){
//            	if (globalIdsOnly) makeGLobal(baseCalendar);
//                CalendarData baseCalendarData=(CalendarData)serialize(baseCalendar,CalendarData.FACTORY,calendarsCount);
//                calendarData.setBaseCalendar(baseCalendarData);
//            }
//        }
//        calendarsCount.dump();
//        calendars.put(calendar,calendarData);
//        return calendarData;
//
//    }
//    protected WorkCalendar deserializeCalendar(CalendarData calendarData/*,Project project*/) throws IOException, ClassNotFoundException{
//        //TODO avoid calendar instance duplication
//        WorkingCalendar calendar=(WorkingCalendar)deserialize(calendarData);
//        if (/*(calendar instanceof WorkingCalendar)&&*/calendarData.getBaseCalendar()!=null){
//            WorkingCalendar baseCalendar=(WorkingCalendar)deserialize(calendarData.getBaseCalendar());
//            try {
//				/*((WorkingCalendar)calendar)*/calendar.setBaseCalendar(baseCalendar);
//				baseCalendar.setDocument(null);
//			} catch (CircularDependencyException e) {
//				e.printStackTrace();
//			}
//        }
//        if (calendar.isBaseCalendar()) {
//        	calendar.setDocument(null);
//        	CalendarService.getInstance().add(calendar);
//        }
//        return calendar;
//    }
//
    public static ResourceImpl deserializeResourceAndAddToPool(EnterpriseResourceData enterpriseResourceData,ResourcePool resourcePool,Session reindex) throws IOException, ClassNotFoundException{
    	ResourceData resourceData=new ResourceData();
    	resourceData.setEnterpriseResource(enterpriseResourceData);
    	ResourceImpl resource=deserializeResourceAndAddToPool(resourceData,resourcePool,reindex,null);
        setRoles(resource, resourceData);
        return resource;

    }
    public static ResourceImpl deserializeResourceAndAddToPool(ResourceData resourceData,ResourcePool resourcePool,Session reindex,Map enterpriseResources) throws IOException, ClassNotFoundException{
        EnterpriseResourceData enterpriseResourceData=resourceData.getEnterpriseResource();
        EnterpriseResource enterpriseResource;
        if (enterpriseResources==null){
        	enterpriseResource =(EnterpriseResource)deserialize(enterpriseResourceData,reindex);
        	enterpriseResource.setUserAccount(enterpriseResourceData.getUserAccount());
        }else{
        	EnterpriseResourceData e=(EnterpriseResourceData)enterpriseResources.get(new Long(enterpriseResourceData.getUniqueId()));
        	if (e==null) return null; //TODO handle this
        	enterpriseResource =(EnterpriseResource)deserialize(e,reindex);
        	enterpriseResource.setUserAccount(e.getUserAccount());
        }
        enterpriseResource.setGlobalWorkVector(enterpriseResourceData.getGlobalWorkVector());
        enterpriseResource.setMaster(resourcePool.isMaster());
        ResourceImpl resource=(resourceData.getSerialized()==null)?
                createResourceFromEnterpriseResource(enterpriseResource):
                (ResourceImpl)deserialize(resourceData,reindex);

        resource.setGlobalResource(enterpriseResource);
        setRoles(resource, resourceData);


        // to ensure older projects import correctly
        WorkingCalendar cal = (WorkingCalendar) enterpriseResource.getWorkCalendar();
        if (cal==null)
            enterpriseResource.setWorkCalendar(WorkingCalendar.getInstanceBasedOn(resourcePool.getDefaultCalendar()));
        else {
        	try {
//				cal.setBaseCalendar(CalendarService.findBaseCalendar(cal.getBaseCalendar().getName()));// avoids multiple instances
        		WorkCalendar baseCal=CalendarService.findBaseCalendar(cal.getBaseCalendar().getName());
				//TODO verification in case the name isn't found, import problem
        		if (baseCal!=null) cal.setBaseCalendar(baseCal);// avoids multiple instances

        	} catch (CircularDependencyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        resourcePool.initializeId(enterpriseResource);
        resourcePool.add(resource);
        return resource;
    }

    private static void setRoles(ResourceImpl resource,ResourceData resourceData){
        resource.setRole(resourceData.getRole());

        int[] authRoles=resourceData.getEnterpriseResource().getAuthorizedRoles();
        if (authRoles!=null){
        	EnterpriseResource globalResource=resource.getGlobalResource();
        	globalResource.setDefaultRole(authRoles.length>0?authRoles[0]:ApplicationUser.INACTIVE);
        	Set<Integer> roles=new HashSet<Integer>();
        	for (int i=0;i<authRoles.length;i++) {
				roles.add(UserUtil.toExtendedRole(authRoles[i],resource.isUser()));
			}
        	globalResource.setAuthorizedRoles(roles);
        	globalResource.setLicense(resourceData.getEnterpriseResource().getLicense());
        	globalResource.setLicenseOptions(resourceData.getEnterpriseResource().getLicenseOptions());
        }

    }

    private static ResourceImpl createResourceFromEnterpriseResource(EnterpriseResource enterpriseResource){
        ResourceImpl resource=new ResourceImpl();
        return resource;
    }


    //call referenceCache.update() after
//    public void updateEnterpriseResources(Collection resources,NodeModel model) throws IOException, ClassNotFoundException{
//        model.removeAll(NodeModel.EVENT);
//    	Map resourceNodeMap=new HashMap();
//        if (resources!=null)
//        for (Iterator i=resources.iterator();i.hasNext();){
//            EnterpriseResourceData resourceData=(EnterpriseResourceData)i.next();
//            EnterpriseResource resource=(EnterpriseResource)deserialize(resourceData);
//            resourceNodeMap.put(resourceData,NodeFactory.getInstance().createNode(resource));
//        }
//
//        //resource outline
//        if (resources!=null){
//            for (Iterator i=resources.iterator();i.hasNext();){
//                EnterpriseResourceData resourceData=(EnterpriseResourceData)i.next();
//                EnterpriseResourceData parentData=(EnterpriseResourceData)resourceData.getParentResource();
//                Node node=(Node)resourceNodeMap.get(resourceData);
//                Node parentNode=(parentData==null)?
//                		null:
//                		((Node)resourceNodeMap.get(parentData));
//                model.add(parentNode,node,(int)resourceData.getChildPosition(),NodeModel.SILENT); //global update instead
//            }
//            Alert.error("cleanNullChildren not implemented");
//            //model.getHierarchy().cleanNullChildren();
//            model.getHierarchy().fireUpdate();
//        }
//    }
    public static void setEnterpriseResources(Collection resources,ResourcePool resourcePool,Session reindex) throws IOException, ClassNotFoundException{
        if (resources!=null){
        	Map resourceNodeMap=new HashMap();
            for (Iterator i=resources.iterator();i.hasNext();){
                EnterpriseResourceData resourceData=(EnterpriseResourceData)i.next();
                ResourceImpl resource=deserializeResourceAndAddToPool(resourceData,resourcePool,reindex);
                //resourceNodeMap.put(resourceData,resource.getGlobalResource()); //TODO Lolo - why is this line here given the line below?
                resourceNodeMap.put(resourceData,NodeFactory.getInstance().createNode(resource));
            }
            //NodeModel model=resourcePool.getResourceOutline();
            for (Iterator i=resources.iterator();i.hasNext();){
                EnterpriseResourceData resourceData=(EnterpriseResourceData)i.next();
                EnterpriseResourceData parentData=resourceData.getParentResource();
                Node node=(Node)resourceNodeMap.get(resourceData);
                Node parentNode=(parentData==null)?
                		null:
                		((Node)resourceNodeMap.get(parentData));
                //model.add(parentNode,node,(int)resourceData.getChildPosition(),NodeModel.SILENT); //global update instead
                resourcePool.addToDefaultOutline(parentNode,node,(int)resourceData.getChildPosition(),false);
                ((ResourceImpl)node.getImpl()).getGlobalResource().setResourcePool(resourcePool);
            }
            resourcePool.getResourceOutline().getHierarchy().cleanVoidChildren();
        }

    }



    public static void forProjectDataDo(ProjectData project,Closure c){
    	c.execute(project);
    	if (project.getCalendar()!=null){
    		c.execute(project.getCalendar());
    		//base calendars to handle?
    	}
    	for (Iterator i=project.getResources().iterator();i.hasNext();){
    		ResourceData r=(ResourceData)i.next();
    		c.execute(r);
    		c.execute(r.getEnterpriseResource());
    		//calendars?
    	}
    	for (Iterator i=project.getTasks().iterator();i.hasNext();){
    		TaskData t=(TaskData)i.next();
    		c.execute(t);
    		CollectionUtils.forAllDo(t.getAssignments(),c);
    		CollectionUtils.forAllDo(t.getPredecessors(),c);
    		//calendars?
    	}
    }
    public static void forProjectDataReversedDo(ProjectData project,Closure c){
    	for (Iterator i=project.getTasks().iterator();i.hasNext();){
    		TaskData t=(TaskData)i.next();
    		CollectionUtils.forAllDo(t.getAssignments(),c);
    		CollectionUtils.forAllDo(t.getPredecessors(),c);
    		c.execute(t);
    		//calendars?
    	}
    	for (Iterator i=project.getResources().iterator();i.hasNext();){
    		ResourceData r=(ResourceData)i.next();
    		c.execute(r.getEnterpriseResource());
    		c.execute(r);
    		//calendars?
    	}
    	if (project.getCalendar()!=null){
    		c.execute(project.getCalendar());
    		//base calendars to handle?
    	}
    	c.execute(project);
    }

    private static abstract class IdClosure implements Closure{
    	long id=1;
    }
    public static void renumberProject(ProjectData project){
    	forProjectDataDo(project,new IdClosure(){
			public void execute(Object arg0) {
				((CommonDataObject)arg0).setUniqueId(id++);
			}
    	});
    }



    class Count{
    	int count;
        int size;
        int max;
        int min=Integer.MAX_VALUE;
        String typeLabel;
        public Count(String typeLabel){
        	this.typeLabel=typeLabel;
        }
        void reset(){
        	count=0;
        	size=0;
        	max=0;
        	min=Integer.MAX_VALUE;
        }
        void add(int s){
        	count++;
        	size+=s;
        	if (s<min) min=s;
        	if (s>max) max=s;
        }
        void dump(){
        	System.out.println("Serialized "+count+" "+typeLabel+", total="+size+", average="+((count==0)?0:(size/count))+", min="+min+", max="+max);
        }
    }

    protected File tmpDir=null;
    protected void initTmpDir() throws IOException{
    	tmpDir=new File(System.getProperty("user.home"),"projity_tmp");
    	if (tmpDir.isDirectory()){
    		File[] files=tmpDir.listFiles();
    		if (files!=null) for (int i=0;i<files.length;i++) files[i].delete();
    	}
    	else if (!tmpDir.exists()) tmpDir.mkdir();
    }
    protected void writeTmpFile(SerializedDataObject data,Count count) throws IOException{
    	if (tmpDir!=null&&count!=null)
    	try {
			File f=new File(tmpDir,data.getPrefix()+"_"+count.count);
			FileOutputStream out=new FileOutputStream(f);
			if (data.getSerialized()!=null) out.write(data.getSerialized());
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
//    public void makeGLobal(DataObject data) throws UniqueIdException{
//    	CommonDataObject.makeGlobal(data);
//     }
    public DataObject serialize(DataObject obj,SerializedDataObjectFactory factory,Count count) throws IOException{
        SerializedDataObject data=SerializeUtil.serialize(obj,factory);
        if (TMP_FILES) writeTmpFile(data,count);
        byte[] bytes=data.getSerialized();
        if (count!=null) count.add((bytes==null)?0:bytes.length);
        return data;
    }
    public static DataObject serializeSingle(DataObject obj,SerializedDataObjectFactory factory,Count count) throws IOException{
        SerializedDataObject data=SerializeUtil.serialize(obj,factory);
        byte[] bytes=data.getSerialized();
        if (count!=null) count.add((bytes==null)?0:bytes.length);
        return data;
    }


    public static DataObject deserialize(DataObject obj,Session reindex) throws IOException, ClassNotFoundException{
        return SerializeUtil.deserialize((SerializedDataObject)obj,reindex);
    }
    protected Collection serialize(Collection objs,SerializedDataObjectFactory factory) throws IOException{
    	if (objs == null)
    		return new ArrayList(); // a user crashed here due to null objs.

        Collection r=new ArrayList(objs.size());
        for (Iterator i=objs.iterator();i.hasNext();)
            r.add(SerializeUtil.serialize((DataObject)i.next(),factory));
        return r;
    }
    protected Collection deserialize(Collection objs,Session reindex) throws IOException, ClassNotFoundException{
        Collection r=new ArrayList(objs.size());
        for (Iterator i=objs.iterator();i.hasNext();)
            r.add(SerializeUtil.deserialize((SerializedDataObject)i.next(),reindex));
        return r;
    }


//    public static void renumber(Map renumbered){
//        HasUniqueIdImpl.update(renumbered);
//    }



    public static void buildStructure(ProjectData projectData,Collection resources,Collection tasks,Collection assignments,Collection links, Collection externalTasks, Collection referringSubprojectTasks,boolean ignoreResourcesForAssignments){
    	if (externalTasks!=null) tasks.addAll(externalTasks);
    	Map resourceMap=createIdMap(resources);
    	Map taskMap=createIdMap(tasks);
    	buildTaskStructure(projectData, tasks, taskMap);
    	projectData.setTasks(tasks);
        projectData.setResources(resources);
        projectData.setReferringSubprojectTasks(referringSubprojectTasks);

        buildAssignmentsStructure(projectData,assignments,resourceMap,taskMap,ignoreResourcesForAssignments);
        buildLinksStructure(projectData,links,taskMap);

    }

    public static void buildTaskStructure(ProjectData projectData,Collection tasks,Map tMap){
    	if (tasks!=null){
	        for (Iterator i=tasks.iterator();i.hasNext();){
	        	TaskData task=(TaskData)i.next();
	        	if (task.getParentTask()==null&task.getParentTaskId()!=-1){
	        		//not built yet, building outline
	        		TaskData parentTask=(TaskData)tMap.get(task.getParentTaskId());
	        		task.setParentTask(parentTask);
	        	}
	        }
        }
    }


    public static void buildAssignmentsStructure(ProjectData projectData,Collection assignments){
    	buildAssignmentsStructure(projectData,assignments,null,null,false);
    }
    public static void buildAssignmentsStructure(ProjectData projectData,Collection assignments,Map rMap,Map tMap,boolean ignoreResourcesForAssignments){
    	Map resourceMap=(rMap==null)?createIdMap(projectData.getResources()):rMap;
    	Map taskMap=(tMap==null)?createIdMap(projectData.getTasks()):tMap;

    	if (assignments!=null){
	        for (Iterator i=assignments.iterator();i.hasNext();){
	        	AssignmentData assignment=(AssignmentData)i.next();
	        	ResourceData resource=(ResourceData)resourceMap.get(new Long(assignment.getResourceId()));
	        	if (!ignoreResourcesForAssignments) assignment.setResource((resource==null)?null:resource.getEnterpriseResource());
	            TaskData taskData=(TaskData)taskMap.get(new Long(assignment.getTaskId()));
	            if (taskData == null) {
	            	//System.out.println("null task data ("+assignment.getTaskId()+")- project " + projectData.getName());
	            	ErrorLogger.logOnce("null task data","null task data - project " + projectData.getName(),null);
	            } else
	            	taskData.addAssignment(assignment);
	        }
        }
    }
    public static void buildLinksStructure(ProjectData projectData,Collection links){
    	buildLinksStructure(projectData,links,null);
    }
    public static void buildLinksStructure(ProjectData projectData,Collection links,Map tMap){
    	Map taskMap=(tMap==null)?createIdMap(projectData.getTasks()):tMap;

        if (links!=null){
	        for (Iterator i=links.iterator();i.hasNext();){
	        	LinkData link=(LinkData)i.next();
	            TaskData predecessor=(TaskData)taskMap.get(new Long(link.getPredecessorId()));
	            TaskData successor=(TaskData)taskMap.get(new Long(link.getSuccessorId()));
	            if (predecessor==null||successor==null) continue; //external links
	            successor.addPredecessor(link);
	            link.setPredecessor(predecessor);
	        }
        }
    }


    public static Map createIdMap(Collection c){
    	Map map=new HashMap();
        if (c!=null){
	        for (Iterator i=c.iterator();i.hasNext();){
	        	DataObject d=(DataObject)i.next();
	        	map.put(new Long(d.getUniqueId()),d);
	        }
        }
        return map;

    }

    public void printTaskDataHierarchy(Collection tasks){
    	StringBuffer b=new StringBuffer();
    	printTaskDataHierarchy(tasks,b);
    	System.out.println(b);
    }
    public void printTaskDataHierarchy(Collection tasks,final StringBuffer b){
    	Map taskMap=new HashMap();
    	for (Iterator i=tasks.iterator();i.hasNext();){
    		TaskData taskData=(TaskData)i.next();
    		if (taskData == null)
    			continue;
    		Long key=new Long(taskData.getParentTaskId());
    		Set set=(Set)taskMap.get(key);
    		if (set==null){
    			set=new TreeSet(new Comparator(){
    				public int compare(Object arg0, Object arg1) {
    					TaskData task0=(TaskData)arg0;
    					TaskData task1=(TaskData)arg1;
    					int value=(task0.getChildPosition()<task1.getChildPosition())?-1:((task0.getChildPosition()==task1.getChildPosition())?0:1);
    					if (value==0){
    						b.append("Duplicates: task0="+task0.getName()+", "+task0.getParentTaskId()+", "+task0.getChildPosition()+" task1="+task1.getName()+", "+task1.getParentTaskId()+", "+task1.getChildPosition()+"\n");
    					}
    					return value;
    				}
    			});
    		}
    		set.add(taskData);
    		taskMap.put(key,set);
    	}
    	buildTaskDataHierarchy(-1L, "\t", taskMap, b);


    }
    private void buildTaskDataHierarchy(long key, String prefix, Map taskMap,final StringBuffer b){
    	Object o=taskMap.get(new Long(key));
    	if (o==null) return;
    	TreeSet children=(TreeSet)((TreeSet)o).clone();
    	for (Iterator i=children.iterator();i.hasNext();){
    		TaskData taskData=(TaskData)i.next();
    		//System.out.println("name: "+taskData.getName());
     		b.append(prefix).append(taskData.getName()).append(',').append(taskData.getUniqueId()).append('\n');
    		if (taskData.getUniqueId()!=-1L) //avoid voids
    			buildTaskDataHierarchy(taskData.getUniqueId(), prefix+"\t", taskMap, b);
    	}
    }




}