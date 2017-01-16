/*
The contents of this file are subject to the Common Public Attribution License 
Version 1.0 (the "License"); you may not use this file except in compliance with 
the License. You may obtain a copy of the License at 
http://www.projectlibre.com/license . The License is based on the Mozilla Public 
License Version 1.1 but Sections 14 and 15 have been added to cover use of 
software over a computer network and provide for limited attribution for the 
Original Developer. In addition, Exhibit A has been modified to be consistent 
with Exhibit B. 

Software distributed under the License is distributed on an "AS IS" basis, 
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the 
specific language governing rights and limitations under the License. The 
Original Code is OpenProj and ProjectLibre.
The Original Developer is the Initial Developer and is both Projity, Inc and 
ProjectLibre Inc. All portions of the code written by Projity are Copyright (c) 
2006, 2008. All Rights Reserved. All portions of the code written by ProjectLibre 
are Copyright (c) 2012. All Rights Reserved. Contributors Projity, Inc. and 
ProjectLibre, Inc.

Alternatively, the contents of this file may be used under the terms of the 
ProjectLibre End-User License Agreement (the ProjectLibre License) in which case 
the provisions of the ProjectLibre License are applicable instead of those above. 
If you wish to allow use of your version of this file only under the terms of the 
ProjectLibre License and not to allow others to use your version of this file under 
the CPAL, indicate your decision by deleting the provisions above and replace them 
with the notice and other provisions required by the Project Libre License. If you 
do not delete the provisions above, a recipient may use your version of this file 
under either the CPAL or the ProjectLibre Licenses. 


[NOTE: The text of this Exhibit A may differ slightly from the text of the notices 
in the Source Code files of the Original Code. You should use the text of this 
Exhibit A rather than the text found in the Original Code Source Code for Your 
Modifications.] 
EXHIBIT B. Attribution Information both ProjectLibre and OpenProj required

Attribution Copyright Notice: Copyright (c) 2012, ProjectLibre, Inc.
Attribution Phrase (not exceeding 10 words): ProjectLibre, the updated version of 
OpenProj
Attribution URL: http://www.projectlibre.com
Graphic Image as provided in the Covered Code as file: projectlibre-logo.png with 
alternatives listed on http://www.projectlibre.com/logo 

Display of Attribution Information is required in Larger Works which are defined in 
the CPAL as a work which combines Covered Code or portions thereof with code not 
governed by the terms of the CPAL. However, in addition to the other notice 
obligations, all copies of the Covered Code in Executable and Source Code form 
distributed must, as a form of attribution of the original author, include on each 
user interface screen the "OpenProj"  and "ProjectLibre" logos visible to all users. 
The OpenProj logo should be located horizontally aligned with the menu bar and left 
justified on the top left of the screen adjacent to the File menu. The logo must be 
at least 100 x 25 pixels. When users click on the "OpenProj" logo it must direct them 
back to http://www.projity.com. The ProjectLibre logo should be located horizontally 
aligned immediately above the OpenProj logo and left justified in alignment with the 
OpenProj logo. The logo must be at least 144 x 31 pixels. When users click on the 
"ProjectLibre" logo it must direct them back to http://www.projectlibre.com.

Attribution Copyright Notice: Copyright (c) 2006, 2008 Projity, Inc.
Attribution Phrase (not exceeding 10 words): Powered by OpenProj, an open source 
solution from Projity
Attribution URL: http://www.projity.com
Graphic Image as provided in the Covered Code as file: openproj_logo.png with 
alternatives listed on http://www.projity.com/logo 

Display of Attribution Information is required in Larger Works which are defined in 
the CPAL as a work which combines Covered Code or portions thereof with code not 
governed by the terms of the CPAL. However, in addition to the other notice 
obligations, all copies of the Covered Code in Executable and Source Code form 
distributed must, as a form of attribution of the original author, include on each 
user interface screen the "OpenProj" and "ProjectLibre" logos visible to all users. 
The OpenProj logo should be located horizontally aligned with the menu bar and left 
justified on the top left of the screen adjacent to the File menu. The logo must be 
at least 100 x 25 pixels. When users click on the "OpenProj" logo it must direct them 
back to http://www.projity.com.
*/
package com.projectlibre.core.pm.exchange;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.sf.mpxj.Duration;
import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Relation;
import net.sf.mpxj.ResourceAssignment;
import net.sf.mpxj.mpp.MPPReader;
import net.sf.mpxj.mpx.MPXReader;
import net.sf.mpxj.mspdi.schema.TimephasedDataType;
import net.sf.mpxj.planner.PlannerReader;
import net.sf.mpxj.reader.AbstractProjectReader;

import com.projectlibre.core.pm.exchange.converters.mpx.MpxAssignmentConverter;
import com.projectlibre.core.pm.exchange.converters.mpx.MpxCalendarConverter;
import com.projectlibre.core.pm.exchange.converters.mpx.MpxDependencyConverter;
import com.projectlibre.core.pm.exchange.converters.mpx.MpxImportState;
import com.projectlibre.core.pm.exchange.converters.mpx.MpxOptionsConverter;
import com.projectlibre.core.pm.exchange.converters.mpx.MpxProjectConverter;
import com.projectlibre.core.pm.exchange.converters.mpx.MpxResourceConverter;
import com.projectlibre.core.pm.exchange.converters.mpx.MpxTaskConverter;
import com.projectlibre.core.pm.exchange.converters.mpx.type.MpxDurationConverter;
import com.projectlibre.core.pm.exchange.converters.type.DateUTCConverter;
import com.projectlibre.core.pm.exchange.converters.type.PercentNumberRatioDoubleConverter;
import com.projectlibre.pm.calendar.DefaultWorkCalendar;
import com.projectlibre.pm.calendar.DuplicateCalendarException;
import com.projectlibre.pm.calendar.WorkCalendar;
import com.projectlibre.pm.resources.Resource;
import com.projectlibre.pm.resources.ResourcePool;
import com.projectlibre.pm.tasks.Assignment;
import com.projectlibre.pm.tasks.Dependency;
import com.projectlibre.pm.tasks.Project;
import com.projectlibre.pm.tasks.SnapshotList;
import com.projectlibre.pm.tasks.Task;
import com.projectlibre.pm.tasks.TaskSnapshot;

/**
 * @author Laurent Chretienneau
 *
 */
public class MspImporter {
	protected ProjectFile mpxProjectFile;
	protected MpxImportState state=new MpxImportState();
	protected AbstractProjectReader reader;
	protected long earliestTaskStart=-1L;
	protected net.sf.mpxj.Task mpxRootTask=null;
	
	public Project importProject(String name, ProgressClosure progress) throws Exception{
		progress.updateProgress(0.0f, "Start");
		parseProject(name);
		return importProject_(progress);		
	}

	public Project importProject(InputStream in, String extension, ProgressClosure progress) throws Exception{
		progress.updateProgress(0.0f, "Start");
		parseProject(in,extension);
		return importProject_(progress);		
	}

	private Project importProject_(ProgressClosure progress) throws Exception{
		progress.updateProgress(0.2f, "File parsed");

		//Identity the type of conversion. It will be used AssignmentConverter
		if (state.isMspdi()) 
			state.setMpxTimephasedMap(((ImprovedMSPDIReader)reader).getTimephasedMap());
		else state.setMpxTimephasedMap(new HashMap<ResourceAssignment,List<TimephasedDataType>>());

		Project project=new Project();

		importOptions(project);
		progress.updateProgress(0.3f, "Options converted");
		importCalendars(project);
		progress.updateProgress(0.4f, "Calendars converted");
		importResourcePool(project);
		progress.updateProgress(0.5f, "Resources converted");
		importTasks(project);
		progress.updateProgress(0.7f, "Tasks converted");
		importDependencies(project);
		progress.updateProgress(0.8f, "Dependencies converted");
		importProjectHeader(project); //must be done after tasks to correct project start
		progress.updateProgress(0.9f, "Project headers converted");

		progress.updateProgress(1f, "Completed");
		return project;
	}
	
	
	public void parseProject(InputStream in, String extension) throws Exception {
		try {
			if (extension.equals("xml")){
				reader=new ImprovedMSPDIReader();
				state.setMspdi(true);
			} else if (extension.equals("mpp"))
				reader=new MPPReader();
			else if (extension.equals("mpx"))
				reader=new MPXReader();
			else if (extension.equals("planner"))
				reader = new PlannerReader();
			mpxProjectFile = reader.read(in);
			state.setMpxProjectFile(mpxProjectFile);
		
		} finally {
			if (in!=null)
				in.close();
		}	


	}
	protected void parseProject(String fileName) throws Exception {
		fileName=fileName.trim();
		int extensionPosition=fileName.lastIndexOf("."); 
		String extension = extensionPosition==-1 ? "xml" : fileName.substring(extensionPosition+1).toLowerCase();
		parseProject(new FileInputStream(fileName), extension);
	}
	
	
	protected void importOptions(Project project) {
		MpxOptionsConverter converter=new MpxOptionsConverter();
		converter.from(mpxProjectFile.getProjectProperties(),project.getCalendarOptions(), state);
	}
	
	protected void importProjectHeader(Project project) {
		MpxProjectConverter converter=new MpxProjectConverter();
		converter.from(mpxProjectFile.getProjectProperties(), project, state);
		
		if (earliestTaskStart!=-1L) //fix project start
			project.setPropertyValue("start", new Date(earliestTaskStart));
	}

	
	protected void importCalendars(Project project) {
		state.setCalendarManager(project.getCalendarManager());
		state.setProjectTitle(mpxProjectFile.getProjectProperties().getProjectTitle());
		
		MpxCalendarConverter converter=new MpxCalendarConverter();
		for (ProjectCalendar mpxBaseCalendar : mpxProjectFile.getCalendars()) {
			WorkCalendar calendar=new DefaultWorkCalendar();
			if (ProjectCalendar.DEFAULT_BASE_CALENDAR_NAME.equals(mpxBaseCalendar.getName())){
				state.setMpxStandardBaseCalendar(mpxBaseCalendar);
				project.getCalendarManager().setStandardBaseCalendar(calendar);
			}
			converter.from(mpxBaseCalendar, calendar, state);
			try {
				project.getCalendarManager().addBaseCalendar(calendar);
				state.mapBaseCalendar(calendar,mpxBaseCalendar);
			} catch (DuplicateCalendarException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	protected void importResourcePool(Project project) {
		ResourcePool resourcePool=new ResourcePool();
		project.setResourcePool(resourcePool);
		state.setResourcePool(resourcePool);
		importResources(resourcePool);
	}
	
	protected void importResources(ResourcePool resourcePool) {
		state.setResourcePool(resourcePool);
		MpxResourceConverter converter=new MpxResourceConverter();
		for (net.sf.mpxj.Resource mpxResource : mpxProjectFile.getAllResources()){
			if (mpxResource.getNull() || mpxResource.getID()==null)
				continue;
			Resource resource;
			if (mpxResource.getID()==0)
				resource=resourcePool.getUnassignedResource();
			else {
				resource=new Resource();
				resourcePool.addResource(resource);
			}
			converter.from(mpxResource, resource, state);
			state.mapResource(mpxResource, resource);
			//TODO insert blank lines ignored below
		}
	}
	
	protected void importTasks(Project project) {
		for (net.sf.mpxj.Task mpxTask : mpxProjectFile.getChildTasks()){
			importTasks(project,mpxTask,null);
		}
	}
	
	protected void importTasks(Project project,net.sf.mpxj.Task mpxTask, Task parentTask) {
		MpxTaskConverter converter=new MpxTaskConverter();
		if (mpxTask.getNull() || mpxTask.getID()==null)
			return; //TODO insert blank lines
//		if (mpxTask.getSubProject() != null)
//			return;
		Task task=null;
		if (mpxTask.getOutlineNumber()!=null &&
				mpxTask.getOutlineLevel() == 0){ //root task, not a real task
			if (mpxRootTask==null)
				mpxRootTask=mpxTask;
		} else { // normal task
			task=new Task();
			converter.from(mpxTask, task, state);

			final Date taskStartDate = (Date) task.getPropertyValue("start");
			if (taskStartDate != null) {
				final long taskStart = taskStartDate.getTime();
				if (earliestTaskStart == -1L || taskStart < earliestTaskStart)
					earliestTaskStart = taskStart;
			}

			project.addTask(task,parentTask);			
			
			state.mapTask(mpxTask, task);
			
			MpxDurationConverter durationConverter=new MpxDurationConverter();
			DateUTCConverter dateConverter=new DateUTCConverter();
			SnapshotList snapshotList=task.getSnapshotList();
			for (int snapshotId=0;snapshotId<SnapshotList.BASELINE_COUNT;snapshotId++){
				Date start;
				if (snapshotId==0)
					start=mpxTask.getBaselineStart();
				else start=mpxTask.getBaselineStart(snapshotId);
				if(start!=null){
					TaskSnapshot snapshot=snapshotList.getSnapshot(snapshotId, true);
					snapshot.setStart((Date)dateConverter.from(start));
					
					Date finish=snapshotId==0? mpxTask.getBaselineFinish():  mpxTask.getBaselineFinish(snapshotId);
					snapshot.setFinish((Date)dateConverter.from(finish));
					
					Duration duration=snapshotId==0? mpxTask.getBaselineDuration():  mpxTask.getBaselineDuration(snapshotId);
					snapshot.setDuration((com.projectlibre.core.time.Duration)durationConverter.from(duration));
				}
			}			
			
			importAssignments(mpxTask, task);
		}
		
		for (net.sf.mpxj.Task mpxChildTask : mpxTask.getChildTasks()){
			importTasks(project,mpxChildTask,task);
		}
	}
	
	protected void importAssignments(net.sf.mpxj.Task mpxTask, Task task) {
		for (net.sf.mpxj.ResourceAssignment mpxAssignment:mpxTask.getResourceAssignments()){
			MpxAssignmentConverter converter=new MpxAssignmentConverter();
			Assignment assignment=new Assignment();
			assignment.setTask(task);
			converter.from(mpxAssignment, assignment, state, SnapshotList.DEFAULT_SNAPSHOT);
			task.addAssignment(assignment);
			MpxDurationConverter durationConverter=new MpxDurationConverter();
			DateUTCConverter dateConverter=new DateUTCConverter();
			PercentNumberRatioDoubleConverter percentConverter=new PercentNumberRatioDoubleConverter();
			for (int snapshotId=0;snapshotId<SnapshotList.BASELINE_COUNT;snapshotId++){
				Date start;
				if (snapshotId==0)
					start=mpxAssignment.getBaselineStart();
				else start=mpxAssignment.getBaselineStart(snapshotId);
//				System.out.println("importAssigment task="+task.getFieldValue("Field.name")+" snapshotId="+snapshotId+" start="+start);
				if(start!=null){
					Assignment a=new Assignment();
					a.setTask(task);
//					protected String[] fieldsToConvert=new String[]{
//							//ProjectLibre, mpx, converter (mpx-> ProjectLibre
//						"units", "units", "com.projectlibre.core.pm.exchange.converters.type.PercentNumberRatioDoubleConverter",
//						"start", "start", "com.projectlibre.core.pm.exchange.converters.type.DateUTCConverter",
//						"finish", "finish", "com.projectlibre.core.pm.exchange.converters.type.DateUTCConverter",		
//						"work", "work", "com.projectlibre.core.pm.exchange.converters.mpx.type.MpxDurationConverter",		
//					};
					converter.from(mpxAssignment, a, state, snapshotId);
					
					a.setFieldValue("Field.start", dateConverter.from(start));
					
					Date finish=snapshotId==0? mpxAssignment.getBaselineFinish():  mpxAssignment.getBaselineFinish(snapshotId);
					a.setFieldValue("Field.finish", dateConverter.from(finish));
					
					Duration work=snapshotId==0? mpxAssignment.getBaselineWork():  mpxAssignment.getBaselineWork(snapshotId);
					a.setFieldValue("Field.work", durationConverter.from(work));

					a.setFieldValue("Field.units", percentConverter.from(mpxAssignment.getUnits()));

					
					task.addAssignment(a, snapshotId);
				}
			}
		}
	}
	
	protected void importDependencies(Project project) {
		MpxDependencyConverter converter=new MpxDependencyConverter();
		for (net.sf.mpxj.Task mpxTask : mpxProjectFile.getAllTasks()){
//			if (mpxTask.getNull() || mpxTask.getID()==null)
//				continue;
			if (mpxTask==mpxRootTask)
				continue;
			List<Relation> mpxRelations=mpxTask.getPredecessors();
			if (mpxRelations==null) continue;
			for (Relation mpxRelation : mpxRelations){
				Dependency dependency=new Dependency();
				converter.from(mpxRelation, dependency, state);
				project.addDependency(dependency);
			}
		}
	}

	public interface ProgressClosure{
		public void updateProgress(float progress, String label);
	}

	
	

	
	

}
