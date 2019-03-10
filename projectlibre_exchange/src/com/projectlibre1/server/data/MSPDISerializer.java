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
 * Copyright (c) 2012. All Rights Reserved. All portions of the code written by 
 * ProjectLibre are Copyright (c) 2012. All Rights Reserved. Contributor 
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
 * Attribution Copyright Notice: Copyright (c) 2012, ProjectLibre, Inc.
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
package com.projectlibre1.server.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.projectlibre1.exchange.ImportedCalendarService;
import com.projectlibre1.server.data.linker.Linker;
import com.projectlibre1.server.data.linker.ResourceLinker;
import com.projectlibre1.server.data.linker.TaskLinker;
import com.projectlibre1.server.data.mspdi.ModifiedMSPDIWriter;
import com.projectlibre1.association.AssociationList;
import com.projectlibre1.configuration.Settings;
import com.projectlibre1.grouping.core.Node;
import com.projectlibre1.grouping.core.VoidNodeImpl;
import com.projectlibre1.grouping.core.model.NodeModelUtil;
import com.projectlibre1.job.JobRunnable;
import com.projectlibre1.options.CalendarOption;
import com.projectlibre1.pm.assignment.Assignment;
import com.projectlibre1.pm.calendar.CalendarService;
import com.projectlibre1.pm.calendar.WorkingCalendar;
import com.projectlibre1.pm.dependency.Dependency;
import com.projectlibre1.pm.key.uniqueid.UniqueIdException;
import com.projectlibre1.pm.resource.ResourceImpl;
import com.projectlibre1.pm.snapshot.Snapshottable;
import com.projectlibre1.pm.task.NormalTask;
import com.projectlibre1.pm.task.Project;
import com.projectlibre1.pm.task.Task;
import com.projectlibre1.pm.task.TaskSnapshot;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.util.Alert;

import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.ProjectProperties;
import net.sf.mpxj.Relation;
import net.sf.mpxj.RelationType;
import net.sf.mpxj.ResourceAssignment;

/**
 *
 */
public class MSPDISerializer implements ProjectSerializer {
    public static final boolean TMP_FILES=false;
    protected JobRunnable job=null;
    
    
    
    protected Linker resourceLinker=new ResourceLinker(){
//    	int count = 0; // unassigned should start at 0
    	public Object addTransformedObjects(Object child) throws Exception{
    		Project project=(Project)parent;
    		ModifiedMSPDIWriter projectData=(ModifiedMSPDIWriter)transformedParent;
    		ResourceImpl resource=(ResourceImpl)child;
//    		resource.setId(count++); // enumerate them
    		net.sf.mpxj.Resource resourceData=projectData.getProjectFile().addResource();
    		MPXConverter.toMPXResource(resource,resourceData);
    		            
            transformationMap.put(resource,resourceData);
            return resourceData;
    	}
    	public boolean addOutlineElement(Object outlineChild,Object outlineParent,long position){
    		return true;
    	}
    		
    };
    protected Linker taskLinker=new TaskLinker(){
    	public Object addTransformedObjects(Object child) throws Exception{
    		Project project=(Project)parent;
    		ModifiedMSPDIWriter projectData=(ModifiedMSPDIWriter)transformedParent;
    		NormalTask task=(NormalTask)child;
    		
    		net.sf.mpxj.Task taskData=projectData.getProjectFile().addTask();
    		MPXConverter.toMPXTask(task,taskData);

    		Map resourceMap=(Map)args[0];
            for (int s=0;s<Settings.numBaselines();s++){
                TaskSnapshot snapshot=(TaskSnapshot)task.getSnapshot(new Integer(s));
                if (snapshot==null) continue;
                AssociationList snapshotAssignments=snapshot.getHasAssignments().getAssignments();
                if (snapshotAssignments.size()>0){
                    for (Iterator j=snapshotAssignments.iterator();j.hasNext();){
                        Assignment assignment=(Assignment)j.next();
                        ResourceImpl r=(ResourceImpl)assignment.getResource();
                        if (s!=Snapshottable.CURRENT.intValue()) continue;
                        net.sf.mpxj.Resource resourceData=(net.sf.mpxj.Resource)resourceMap.get(r);
                        
                        ResourceAssignment assignmentData=taskData.addResourceAssignment(resourceData);
                 
                        
                        projectData.putOPPrAssignmentMap(assignmentData,assignment);
                        projectData.putOPPrSnapshotIdMap(assignmentData,new Integer(s));
                        if (s==Snapshottable.CURRENT.intValue()){
                        	MPXConverter.toMPXAssignment(assignment,assignmentData);
                        }
                        
                    }
                }
            }

    		

            transformationMap.put(task,taskData);
            return taskData;
    	}
    	public boolean addOutlineElement(Object outlineChild,Object outlineParent,long position){
    		if (outlineChild instanceof VoidNodeImpl) // skip void nodes
    			return false;
    		net.sf.mpxj.Task taskData=(net.sf.mpxj.Task)getTransformationMap().get(outlineChild);
    		net.sf.mpxj.Task parentData=(outlineParent==null)?null:((net.sf.mpxj.Task)getTransformationMap().get(outlineParent));
   			taskData.setOutlineLevel(new Integer(((parentData==null)?1:(parentData.getOutlineLevel().intValue()+1)))); // outline levels start at 1
   			//fix from vitaliff
   			//setSummary is normally done in mpxj post processing
   			if (parentData != null) 
   				parentData.setSummary(true);
			taskData.setOutlineNumber(((parentData==null)?"":(parentData.getOutlineNumber()+"."))+(position+1));
			return true;
    	}
        
        
    };
    
    protected Map saveResources(Project project,ModifiedMSPDIWriter projectData) throws Exception{

		NodeModelUtil.enumerateNonAssignments(project.getResourcePool().getResourceOutline());
    	resourceLinker.setParent(project);
    	resourceLinker.setTransformedParent(projectData);
    	//resourceLinker.setGlobalIdsOnly(globalIdsOnly);
    	resourceLinker.init();
    	resourceLinker.addTransformedObjects(ResourceImpl.getUnassignedInstance());
    	resourceLinker.addTransformedObjects();
    	resourceLinker.addOutline(null);
//    	resourceLinker.getTransformationMap().put(new Long(ResourceImpl.getUnassignedInstance().getUniqueId()),ResourceImpl.getUnassignedInstance());
        return resourceLinker.getTransformationMap();
    }

    protected Map saveTasks(Project project,ModifiedMSPDIWriter projectData,Map resourceMap) throws Exception{
		NodeModelUtil.enumerateNonAssignments(project.getTaskOutline()); // to fix bug, I moved this before tasks are saved. 16.2.06 hk
    	taskLinker.setParent(project);
    	taskLinker.setTransformedParent(projectData);
    	//taskLinker.setGlobalIdsOnly(globalIdsOnly);
    	taskLinker.setArgs(new Object[]{resourceMap});
    	taskLinker.init();
    	taskLinker.addTransformedObjects();
    	taskLinker.addOutline(null);

    	//dependencies
		// mpxj uses default options when importing link leads and lags
		CalendarOption oldOptions = CalendarOption.getInstance();
		CalendarOption.setInstance(CalendarOption.getDefaultInstance());

		int taskCount = 0;
		LinkedList voidTasksQueue=new LinkedList(); // we do not want to export nulls lines at end, so once all tasks done, stop
    	for (Iterator i=project.getTaskOutline().iterator();i.hasNext();){
    		Object obj = ((Node)i.next()).getImpl();
    		if (voidTasksQueue.size()>0 && !(obj instanceof VoidNodeImpl)){
    			//insert voids
    			for (Object voidTask:voidTasksQueue){
            		net.sf.mpxj.Task taskData=projectData.getProjectFile().addTask();
            		MPXConverter.toMPXVoid((VoidNodeImpl)voidTask,taskData);
    			}
    			voidTasksQueue.clear();
    		}
    		if (obj instanceof Assignment)
    			continue;
    		if (obj instanceof VoidNodeImpl) {
    			if (taskCount == 0) //TODO see why there is a void node at the beginning always
    				continue;
    			voidTasksQueue.add(obj);
    		} else {
	            Task task=(Task)obj; //ResourceImpl to have the EnterpriseResource link
//	            task.setUniqueId(task.getId()); // set unique id and id to the same thing on export. Ensures unique id is unique
	            net.sf.mpxj.Task taskData=(net.sf.mpxj.Task)taskLinker.getTransformationMap().get(task);
	            projectData.putOPPrTaskMap(taskData,task);
		        
	            for (Iterator j=task.getPredecessorList().iterator();j.hasNext();){
	            	Dependency dependency=(Dependency)j.next();
	            	Task pred=(Task)dependency.getPredecessor();
	            	net.sf.mpxj.Task predData=(net.sf.mpxj.Task)taskLinker.getTransformationMap().get(pred);
	            	
	            	Relation rel=taskData.addPredecessor(predData,RelationType.getInstance(dependency.getDependencyType()),MPXConverter.toMPXDuration(dependency.getLag())); //claur
	            }
	            taskCount++;
    		}
        }
    	
		CalendarOption.setInstance(oldOptions);
        return taskLinker.getTransformationMap();
    }

    public ModifiedMSPDIWriter serializeProject(Project project) throws Exception{
    	return serializeProject(project,false);
    }
    public ModifiedMSPDIWriter serializeProject(Project project,boolean globalIdsOnly) throws Exception{
        if (globalIdsOnly) 
        	makeGLobal(project);
        ModifiedMSPDIWriter projectData=new ModifiedMSPDIWriter();
        ProjectFile projectFile = new ProjectFile();
        projectData.setProjectFile(projectFile);
        
        projectData.setOPPrProject(project);
//this doesn't appear in 2007 version of mpxj        projectData.setMicrosoftProjectCompatibleOutput(true);
        projectFile.getProjectConfig().setAutoTaskUniqueID(true);
        projectFile.getProjectConfig().setAutoResourceUniqueID(true);
        //project
        ProjectProperties projectHeader=projectFile.getProjectProperties();
        
		MPXConverter.toMPXOptions(projectHeader);

        MPXConverter.toMPXProject(project,projectHeader);
        if (job!=null) job.setProgress(0.2f);
        
        //calendars
//        WorkCalendar calendar=project.getWorkCalendar();
//        if (calendar!=null){
//            ProjectCalendar calendarData=projectData.addDefaultBaseCalendar();
//            calendarData.setName(calendar.getName());
//        }
        projectFile.getProjectConfig().setAutoCalendarUniqueID(true);
		CalendarService service = CalendarService.getInstance();
		Object[] calendars=CalendarService.allBaseCalendars();
		if (calendars!=null)
		for (int i=0;i<calendars.length;i++){
			WorkingCalendar workCalendar=(WorkingCalendar)calendars[i];
			ProjectCalendar cal = projectFile.addCalendar();
			MPXConverter.toMpxCalendar(workCalendar,cal);
			ImportedCalendarService.getInstance().addExportedCalendar(cal,workCalendar);
		}
        if (job!=null) job.setProgress(0.3f);
        
        //resources
        Map resourceMap=saveResources(project,projectData);
        if (job!=null) job.setProgress(0.5f);
        
        //tasks
        saveTasks(project,projectData,resourceMap);
        if (job!=null) job.setProgress(0.7f);

        return projectData;
        
    }
    

    
    
    
    public void makeGLobal(DataObject data) throws UniqueIdException{
    	CommonDataObject.makeGlobal(data);
     }
    
	public boolean saveProject(Project project,String fileName) {
		String extension="";
		String name=fileName;
		String tmpFileName=fileName;
		int i=fileName.lastIndexOf('.');
		if (i>0){
			extension=fileName.substring(i);
			name=fileName.substring(0, i);
		}
		
		File file=new File(fileName);
		File tmpFile=file;
		for (int count=0;tmpFile.exists();count++){
			tmpFileName=name+"_tmp"+count+extension;
			tmpFile=new File(tmpFileName);
		}
		
		
		try {
			if (saveProject(project,new FileOutputStream(tmpFile))
					 && tmpFile.length()>0){
				if (!file.equals(tmpFile)){
					file.delete();
					tmpFile.renameTo(file);
				}
				return true;
			}
		} catch (FileNotFoundException e) {
		}
		if (file.equals(tmpFile))
			Alert.error(Messages.getString("Message.saveError"));
		else Alert.error(Messages.getString("Message.saveErrorTmpFile")+tmpFileName);
		return false;
	}

	public boolean saveProject(Project project,OutputStream out) {
		try {
			//MSPDISerializer serializer=new MSPDISerializer();
			ModifiedMSPDIWriter data=/*serializer.*/serializeProject(project);
			if (job!=null) job.setProgress(0.9f);
			data.write(data.getProjectFile(),out);
			if (job!=null) job.setProgress(1.0f);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public JobRunnable getJob() {
		return job;
	}

	public void setJob(JobRunnable job) {
		this.job = job;
	}
	
	

}
