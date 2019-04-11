/*******************************************************************************
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
package com.projectlibre1.exchange;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Closure;

import com.projectlibre.core.hierarchy.Hierarchy;
import com.projectlibre.core.hierarchy.HierarchyNode;
import com.projectlibre.core.pm.exchange.MspImporter;
import com.projectlibre.core.pm.exchange.ProjectConverter;
import com.projectlibre.core.pm.exchange.converters.op.OpAssignmentConverter;
import com.projectlibre.core.pm.exchange.converters.op.OpDependencyConverter;
import com.projectlibre.core.pm.exchange.converters.op.OpImportState;
import com.projectlibre.core.pm.exchange.converters.op.OpProjectConverter;
import com.projectlibre.core.pm.exchange.converters.op.OpResourceConverter;
import com.projectlibre.core.pm.exchange.converters.op.OpTaskConverter;
import com.projectlibre.pm.calendar.CalendarOptions;
import com.projectlibre.pm.calendar.WorkCalendar;
import com.projectlibre.pm.scheduling.ScheduleFrom;
import com.projectlibre.pm.tasks.SnapshotList;
import com.projectlibre.pm.tasks.Task;
import com.projectlibre.pm.tasks.TaskSnapshot;
import com.projectlibre1.configuration.CircularDependencyException;
import com.projectlibre1.contrib.util.Log;
import com.projectlibre1.contrib.util.LogFactory;
import com.projectlibre1.grouping.core.Node;
import com.projectlibre1.grouping.core.NodeFactory;
import com.projectlibre1.job.Job;
import com.projectlibre1.job.JobRunnable;
import com.projectlibre1.options.CalendarOption;
import com.projectlibre1.pm.assignment.Assignment;
import com.projectlibre1.pm.assignment.AssignmentService;
import com.projectlibre1.pm.calendar.CalendarService;
import com.projectlibre1.pm.calendar.WorkingCalendar;
import com.projectlibre1.pm.criticalpath.TaskSchedule;
import com.projectlibre1.pm.resource.Resource;
import com.projectlibre1.pm.resource.ResourceImpl;
import com.projectlibre1.pm.resource.ResourcePool;
import com.projectlibre1.pm.task.NormalTask;
import com.projectlibre1.pm.task.Project;
import com.projectlibre1.server.data.EnterpriseResourceData;
import com.projectlibre1.server.data.MSPDISerializer;
import com.projectlibre1.server.data.Serializer;
import com.projectlibre1.session.Session;
import com.projectlibre1.session.SessionFactory;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.util.Alert;
import com.projectlibre1.util.DateTime;
import com.projectlibre1.util.Environment;
/**
 * This class is based on the project mpxj http://www.tapsterrock.com/mpxj/
 * The enumerated types in projectlibre currently correspond exactly to the types in mpx, so there is no need to convert them.
 * However, if the projectlibre enumerations change, it will be necessary to map them to mpx types.
 *
 */
public class MicrosoftImporter extends ServerFileImporter{
	static Log log = LogFactory.getLog(MicrosoftImporter.class);
	protected com.projectlibre.pm.tasks.Project plProject= null;
	protected OpImportState state=new OpImportState();
	List allTasks = null;
	ArrayList subprojects;
	private Date earliestStart = DateTime.getMaxDate();
	protected HashMap taskMap = new HashMap(); // keeps track of mapping mpx tasks to projectlibre1 tasks
	private HashMap resourceMap = new HashMap(); // keeps track of mappy mpx resources to projectlibre1 resources
	List allResources=null;
	public static boolean ADD_SUMMARY_TASK = false; //Environment.isAddSummaryTask(); // whether to automatically add an extra project summary task or not //claur
	private static final String ABORT = "Job aborted"; //$NON-NLS-1$
	private String errorDescription = null;
	private Exception lastException = null;
	private JobRunnable jobRunnable = null;

	protected Context context = new Context();
	public MicrosoftImporter() {
		System.out.println("-------MicrosoftImporter ctor");
	}


	@Override
	public void importFile() throws Exception {
		log.info("BEGIN: MicrosoftImporter.PrepareResources");
		parse();
		log.info("END: MicrosoftImporter.PrepareResources");
		Environment.setImporting(false);
		log.info("BEGIN: Finish import");
		convertToProjectLibre1();
		log.info("END: Finish import");
	}

    @Override
	public Project loadProject(InputStream in)  throws Exception{
		log.info("BEGIN: MicrosoftImporter.PrepareResources");
		parse(in, "xml");
		log.info("END: MicrosoftImporter.PrepareResources");
		Environment.setImporting(false);
		log.info("BEGIN: Finish import");
		convertToProjectLibre1();
		log.info("END: Finish import");
    	return project;
	}
    
    @Override
	public boolean saveProject(Project project,OutputStream out) throws Exception{
		MSPDISerializer serializer = new MSPDISerializer();
		return serializer.saveProject(project,out);
	}

	@Override
	public void exportFile() throws Exception {
		MSPDISerializer serializer = new MSPDISerializer();
		//serializer.setJob(this);
		serializer.saveProject(project,fileName);
	}


	private void setProgress(float p) {
		if (jobRunnable == null)
			log.info("Progress " + 100 * p + "%");
		else
			jobRunnable.setProgress(p);
	}
	public void importProject(Project p) throws Exception {
		System.out.println("MicrosoftImporter.importProject()");

		this.project = p;
		parse();
		convertToProjectLibre1();
	}
	public void parse(InputStream in, String extension) throws Exception {
		System.out.println("MicrosoftImporter.parse()");

		Environment.setImporting(true); // will avoid certain popups
		
		setProgress(0.1f);
		
		
		MspImporter plImporter=new MspImporter();
		plProject=plImporter.importProject(in, extension, new MspImporter.ProgressClosure() {
			@Override
			public void updateProgress(float progress, String label) {
				setProgress(progress*0.1f);
				
			}
		});
		log.info(plProject.toString());
		
		
		if (plProject == null) {
			String errorText = (errorDescription == null) ? Messages.getString("Message.ImportError") : errorDescription; //$NON-NLS-1$
			if (jobRunnable != null) {
				jobRunnable.getJob().error(errorText,false);
				jobRunnable.getJob().cancel();
			}

			Environment.setImporting(false); // will avoid certain popups
			throw lastException == null ? new Exception("Failed to import file") : lastException; //$NON-NLS-1$
		}

		setProgress(0.2f);
		setProgress(1f);

	}
	public void parse() throws Exception {
		System.out.println("MicrosoftImporter.parse()");

		Environment.setImporting(true); // will avoid certain popups
		
		setProgress(0.1f);
		
		
		MspImporter plImporter=new MspImporter();
		if (fileInputStream==null)
			plProject=plImporter.importProject(fileName, new MspImporter.ProgressClosure() {
				@Override
				public void updateProgress(float progress, String label) {
					setProgress(progress*0.1f);
					
				}
			});
		else plProject=plImporter.importProject(fileInputStream, "xml", new MspImporter.ProgressClosure() {
			@Override
			public void updateProgress(float progress, String label) {
				setProgress(progress*0.1f);
				
			}
		});

		log.info(plProject.toString());
		
		
		if (plProject == null) {
			String errorText = (errorDescription == null) ? Messages.getString("Message.ImportError") : errorDescription; //$NON-NLS-1$
			if (jobRunnable != null) {
				jobRunnable.getJob().error(errorText,false);
				jobRunnable.getJob().cancel();
			}

			Environment.setImporting(false); // will avoid certain popups
			throw lastException == null ? new Exception("Failed to import file") : lastException; //$NON-NLS-1$
		}

		setProgress(0.2f);
		setProgress(1f);

	}
	/**
	 * This method imports an entire mpx, mpp or xml file
	 *
	 * @param filename
	 *            name of the inputfile
	 * @throws Exception
	 *             on file read error
	 */
    public Job getImportFileJob(){
		System.out.println("MicrosoftImporter.getImportFileJob()");

    	subprojects = new ArrayList();
    	errorDescription = null;
    	lastException = null;
    	Session session=SessionFactory.getInstance().getSession(resourceMapping==null);
		Job job=new Job(session.getJobQueue(),"importFile",Messages.getString("MicrosoftImporter.Importing"),true); //$NON-NLS-1$ //$NON-NLS-2$

//    	job.addRunnable(new JobRunnable(Messages.getString("MicrosoftImporter.PrepareResources"),1.0f){ //$NON-NLS-1$
//
//			public Object run() throws Exception{
//				log.info("BEGIN: MicrosoftImporter.PrepareResources");
//				//MicrosoftImporter.this.jobRunnable = this;
//				importFile();
//				log.info("END: MicrosoftImporter.PrepareResources");
//				return null;
//			}
//    	});

		
    	job.addRunnable(new JobRunnable(Messages.getString("MicrosoftImporter.PrepareResources"),1.0f){ //$NON-NLS-1$

			public Object run() throws Exception{
				log.info("BEGIN: MicrosoftImporter.PrepareResources");
				MicrosoftImporter.this.jobRunnable = this;
				parse();
				log.info("END: MicrosoftImporter.PrepareResources");
				return null;
			}
    	});
    	
    	job.addSwingRunnable(new JobRunnable("Import resources",1.0f){ //$NON-NLS-1$
			public Object run() throws Exception{
				log.info("BEGIN: Import resources");
				ResourceMappingForm form=getResourceMapping();
				if (form!=null&&form.isLocal()) //if form==null we are in a case were have no server access. popup not needed
					if (!job.okCancel(Messages.getString("Message.ServerUnreacheableReadOnlyProject"),true)){ //$NON-NLS-1$
						setProgress(1.0f);
						errorDescription = ABORT;
						Environment.setImporting(false); // will avoid certain popups
						throw new Exception(ABORT);
					}

// claur - Moved to convertToProjectLibre1 after import Calendar because base calendar must be imported before resources
//			log.info("import resources");		 //$NON-NLS-1$
//				if(!importResources()){
//					setProgress(1.0f);
//					errorDescription = ABORT;
//					Environment.setImporting(false); // will avoid certain popups
//					throw new Exception(ABORT);
//				}
				setProgress(1f);
				log.info("END: Import resources");
				return null;
	    	}
    	});
    	job.addRunnable(new JobRunnable("Finish import",1.0f){ //$NON-NLS-1$
			public Object run() throws Exception{
				log.info("BEGIN: Finish import");
				Object r=convertToProjectLibre1();
				log.info("END: Finish import");
				return r;
    		}
    	});
    	return job;
    }

    private Project convertToProjectLibre1() throws Exception {

		log.info("import options"); //$NON-NLS-1$
		importOptions();
		setProgress(0.3f);
		
		log.info("import calendars"); //$NON-NLS-1$
		importCalendars();
		setProgress(0.4f);
		
		log.info("import resources");		 //$NON-NLS-1$
		//claur - moved here because calendars must be imported first
		importLocalResources();
		setProgress(0.5f);
		
		log.info("import tasks");		 //$NON-NLS-1$
		importTasks();
		setProgress(0.6f);
		
		log.info("import project fields");		 //$NON-NLS-1$
		importProjectFields();
		setProgress(0.7f);
		
		log.info("import dependencies");		 //$NON-NLS-1$
		importDependencies();
		setProgress(0.8f);
		
		log.info("import assignments"); //$NON-NLS-1$
		importAssignments();
		setProgress(0.9f);
				
		log.info("about to initialize");		 //$NON-NLS-1$
			if (project.getName() == null)
				project.setName("error - name not set on import"); //$NON-NLS-1$

//			CalendarService.getInstance().renameImportedBaseCalendars(project.getName());
			try {
				project.initialize(false,false); // will run critical path
			} catch (RuntimeException e) {
				if (e.getMessage()==CircularDependencyException.RUNTIME_EXCEPTION_TEXT) {
					Environment.setImporting(false); // will avoid certain popups
					Alert.error(e.getMessage());
					plProject = null;
					project = null;
					throw new Exception(e.getMessage());
				}
			}
			//project.setGroupDirty(!Environment.getStandAlone());
			if (!Environment.getStandAlone()) project.setAllDirty();

			project.setBoundsAfterReadProject();
			
			if (plProject.getPropertyValue("scheduleFrom") == ScheduleFrom.FINISH) {
				project.setForward(false);
			}
			Environment.setImporting(false); // will avoid certain popups
			setProgress(1.0f);
			plProject=null;// remove reference
//			project.setWasImported(true); //claur
			return project;
    }


	protected void importCalendars() throws Exception{
		state.setCalendarManager(plProject.getCalendarManager());
		
		for (WorkCalendar plCalendar : plProject.getCalendarManager()) {
			WorkingCalendar opCalendar=WorkingCalendar.getStandardBasedInstance();
			ProjectConverter.getInstance().convert("op",ProjectConverter.Type.CALENDAR,false,opCalendar,plCalendar,state);
			if (CalendarService.findBaseCalendar(opCalendar.getName())!= null){
				//rename imported calendar if a calendar with the same name exists
				opCalendar.setName(opCalendar.getName() + "[Imported]");
			}
			CalendarService.getInstance().add(opCalendar);
			state.mapBaseCalendar(plCalendar,opCalendar);
		}
	}


	/**
	 * This method imports all resources defined in the file into the projectlibre1 model
	 *
	 * @param file
	 *            MPX file
	 */
	protected void importLocalResources(){
		ResourcePool resourcePool = project.getResourcePool();
		project.setLocal(true);
		resourcePool.setLocal(true);
		resourcePool.setMaster(false);
        resourcePool.updateOutlineTypes();
		ResourceImpl opResource;
		OpResourceConverter converter=new OpResourceConverter();
		for (com.projectlibre.pm.resources.Resource plResource : plProject.getResourcePool().getResources()){
			opResource = resourcePool.newResourceInstance();
			converter.to(opResource,plResource,state);
			state.mapOpResource(plResource, opResource);
			// Add to resource hierarchy.  MSProject does not actually have a hierarchy
			Node opResourceNode = NodeFactory.getInstance().createNode(opResource); // get a node for this resource
			resourcePool.addToDefaultOutline(null,opResourceNode);			
			state.mapOpResourceNode(opResource, opResourceNode);

		}
		//insertResourceVoids();
	}


	protected boolean importResources() throws Exception{
		return importResources(resourceMap,new Closure() {
			public void execute(Object arg0) {
				importLocalResources();
			}
		});
	}

	protected boolean importResources(HashMap resourceMap,Closure importLocalResources) throws Exception{
		ResourceMappingForm form=getResourceMapping();



		if (form==null||form.isLocal()){ //claur
				importLocalResources.execute(null);
		}else{
			if (!form.execute()) return false;
			if (form.isLocal()){
				importLocalResources.execute(null);
				return true;
			}

			com.projectlibre1.pm.resource.Resource projectlibre1Resource=null;
			int projectlibre1ResourceCount=0;
			ResourcePool resourcePool = project.getResourcePool();
			project.setTemporaryLocal(true);
			Object srcResource;
			EnterpriseResourceData data;
			Map enterpriseResourceDataMap=new HashMap();
			for (Iterator i=form.getResources().iterator();i.hasNext();){
				data=(EnterpriseResourceData)i.next();
				if (data.isLocal()) {
					projectlibre1Resource=ResourceImpl.getUnassignedInstance();
				} else {
//					try {
						projectlibre1Resource=Serializer.deserializeResourceAndAddToPool(data,resourcePool,null);

						//Handles only flat outlines
						Node node=NodeFactory.getInstance().createNode(projectlibre1Resource);
						resourcePool.addToDefaultOutline(null,node,projectlibre1ResourceCount++,false);
		                ((ResourceImpl)projectlibre1Resource).getGlobalResource().setResourcePool(resourcePool);
//					} catch (Exception e) {}
				}
				enterpriseResourceDataMap.put(data,projectlibre1Resource);

			}
			Iterator ir = form.getImportedResources().iterator();
			Iterator sr = form.getSelectedResources().iterator();
			while (ir.hasNext()) {
				srcResource = ir.next();
				data=(EnterpriseResourceData)sr.next();
				projectlibre1Resource=(com.projectlibre1.pm.resource.Resource)enterpriseResourceDataMap.get(data);
				mapResource((long)projectlibre1Resource.getUniqueId(),projectlibre1Resource );
			}

			resourcePool.setMaster(false);
			resourcePool.updateOutlineTypes();

			project.setAccessControlPolicy(form.getAccessControlType());
			project.resetRoles(form.getAccessControlType()==0);


			
		}
		return true;
	}


	protected void retrieveResourcesForMerge(List existingResources) throws Exception{

	}



	protected void importOptions() throws Exception{
		ProjectConverter converter=ProjectConverter.getInstance();
		CalendarOption opOptions=CalendarOption.getInstance();
		CalendarOptions options=plProject.getCalendarOptions();
		converter.convert("op", ProjectConverter.Type.OPTIONS, false, opOptions, options, state);
	}

	private void importProjectFields() {
		OpProjectConverter opConverter=new OpProjectConverter();
		opConverter.to(project, plProject, state);
	}
	
	/**
	 * This method imports all tasks defined in the file into the projectlibre1 model
	 *
	 */
	private void importTasks() {
		final OpTaskConverter converter=new OpTaskConverter();
		plProject.getHierarchy().visit(new Hierarchy.Visitor(){ //pre-order visitor, parents must be treated before children
			@Override
			public void visit(HierarchyNode hierarchyNode) {
				com.projectlibre.core.nodes.Node node=hierarchyNode.getNode();
				if (!(node instanceof Task)) //ignore assignments present in task hierarchy
					return;
				Task task=(Task)node;
				HierarchyNode parentHierarchyNode=hierarchyNode.getParent();
				Task parentTask=null;
				if (!parentHierarchyNode.isRoot())
					parentTask=(Task)parentHierarchyNode.getNode();

				//op task conversion
				NormalTask opTask=project.newNormalTaskInstance(false);
				opTask.setOwningProject(project);
				opTask.setProjectId(project.getUniqueId());
				converter.to(opTask, task, state);
				
				//op task node conversion
				Node opTaskNode=NodeFactory.getInstance().createNode(opTask);
				
				//op node hierarchy
				NormalTask opParentTask=parentTask==null? null : state.getOpTask(parentTask);
				Node opParentTaskNode=opParentTask==null? null : state.getOpTaskNode(opParentTask);
				project.addToDefaultOutline(opParentTaskNode,opTaskNode);
				
				
				SnapshotList snapshots=task.getSnapshotList();
				for (int snapshotId=0;snapshotId<SnapshotList.BASELINE_COUNT;snapshotId++){
					TaskSnapshot s=snapshots.getSnapshot(snapshotId);
					if (s!=null && s.getStart()!=null && s.getFinish()!=null){
						com.projectlibre1.pm.task.TaskSnapshot opSnapshot=new com.projectlibre1.pm.task.TaskSnapshot();
						opSnapshot.getHasAssignments(); //init hasAssignments
						TaskSchedule schedule=new TaskSchedule();//(TaskSchedule)opTask.getCurrentSchedule().clone();
						schedule.setStart(s.getStart().getTime());
						schedule.setFinish(s.getFinish().getTime());
						opSnapshot.setCurrentSchedule(schedule);
						opTask.setSnapshot(snapshotId, opSnapshot);
					}
				}


				
				state.mapOpTask(task, opTask);
				state.mapOpTaskNode(opTask, opTaskNode);
			}
		});
		

	}
	


	/**
	 * Import dependencies. Must be done after importing tasks
	 *
	 * @throws Exception
	 */
	public void importDependencies() throws Exception {
		// mpxj uses default options when importing link leads and lags, even when mpp format
		CalendarOption oldOptions = CalendarOption.getInstance();
		CalendarOption.setInstance(CalendarOption.getDefaultInstance());


		final OpDependencyConverter converter=new OpDependencyConverter();
		for (com.projectlibre.pm.tasks.Dependency plDependency : plProject.getDependencies()){
			converter.to(plDependency,state);
		}
		CalendarOption.setInstance(oldOptions);
	}


	/**
	 * Import mpx assignments into projectlibre1 model
	 *
	 */
	protected void importAssignments() {
		OpAssignmentConverter converter=new OpAssignmentConverter();
		for (Task task : plProject.getTasks()){
			NormalTask opTask=state.getOpTask(task);
			for (com.projectlibre.pm.tasks.Assignment assignment : task.getAssignments()){
				Assignment opAssignment=converter.to(assignment, state);
				AssignmentService.getInstance().connect(opAssignment, null);
			}
			SnapshotList snapshots=task.getSnapshotList();
			for (int snapshotId=0;snapshotId<SnapshotList.BASELINE_COUNT;snapshotId++){
				TaskSnapshot s=snapshots.getSnapshot(snapshotId);
				com.projectlibre1.pm.task.TaskSnapshot opSnapshot=(com.projectlibre1.pm.task.TaskSnapshot)opTask.getSnapshot(snapshotId);
				if (s!=null && opSnapshot!=null){
					for (com.projectlibre.pm.tasks.Assignment assignment : s.getAssignments()){
						Assignment opAssignment=converter.to(assignment, state);
						opSnapshot.addAssignment(opAssignment);
					}
				}
			}
		}
	}

	protected double assignmentPercentFactor() {
		return 100.0;
	}




	/**
	 * Currently not implemented
	 */
	public Job getExportFileJob(){
    	Session session=SessionFactory.getInstance().getLocalSession();
		Job job=new Job(session.getJobQueue(),"exportFile","Exporting...",true); //$NON-NLS-1$ //$NON-NLS-2$
    	job.addRunnable(new JobRunnable("Local: export",1.0f){ //$NON-NLS-1$
    		public Object run() throws Exception{
     			MSPDISerializer serializer = new MSPDISerializer();
    			serializer.setJob(this);
    			serializer.saveProject(project,fileName);
    			return null;
    		}
    	});
		//session.schedule(job);
    	return job;

	}
	protected void makeValidResourceId(Resource res) {

	}
	protected void mapResource(Number id, Object value) {
//		System.out.println("Mapping res " + id + "   " + value);
		resourceMap.put(id, value);
	}
	public HashMap getResourceMap() {
		return resourceMap;
	}
}
