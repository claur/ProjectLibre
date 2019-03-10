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
package com.projectlibre1.exchange;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.projectlibre1.server.data.AssignmentData;
import com.projectlibre1.server.data.DistributionData;
import com.projectlibre1.server.data.EnterpriseResourceData;
import com.projectlibre1.server.data.ProjectData;
import com.projectlibre1.server.data.ResourceData;
import com.projectlibre1.server.data.Serializer;
import com.projectlibre1.server.data.TaskData;
import com.projectlibre1.job.Job;
import com.projectlibre1.job.JobCanceledException;
import com.projectlibre1.job.JobRunnable;
import com.projectlibre1.pm.resource.EnterpriseResource;
import com.projectlibre1.pm.task.Project;
import com.projectlibre1.session.Session;
import com.projectlibre1.session.SessionFactory;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.util.Environment;

/**
 * Loads/Saves a project from/to a pod file
 */
public class ServerLocalFileImporter extends ServerFileImporter {
	private static final String ABORT = "Job aborted"; //$NON-NLS-1$
	//private HashMap resourceMap = new HashMap(); // keeps track of imported resources to server resources
	public ServerLocalFileImporter() {
		super();
	}

	protected ProjectData projectData;

	
	
    @Override
	public void importFile() throws Exception {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void exportFile() throws Exception {
		// TODO Auto-generated method stub
		
	}



	public Job getImportFileJob(){
    	final Serializer serializer=new Serializer();
    	final FileImporter importer=this;
    	final Job job=new Job(importer.getJobQueue(),"importFile",Messages.getString("LocalFileImporter.Importing"),true); //$NON-NLS-1$ //$NON-NLS-2$
        job.addRunnable(new JobRunnable("Import",1.0f){ //$NON-NLS-1$
    		public Object run() throws Exception{
    	        //DataUtil serializer=new DataUtil();
    	        System.out.println("Loading "+importer.getFileName()+"..."); //$NON-NLS-1$ //$NON-NLS-2$

    	        long t1=System.currentTimeMillis();
    	        ObjectInputStream in=new ObjectInputStream(new FileInputStream(importer.getFileName()));
    	        Object obj=in.readObject();
    	        if (obj instanceof String) obj=in.readObject(); //check version in the future
    	        projectData=(ProjectData)obj;
    	        projectData.setMaster(false);
    	        projectData.setLocal(false);
    	        long t2=System.currentTimeMillis();
    	        System.out.println("Loading...Done in "+(t2-t1)+" ms"); //$NON-NLS-1$ //$NON-NLS-2$

    	        Collection<ResourceData> rs=(Collection<ResourceData>)projectData.getResources();
    	        List<EnterpriseResourceData> ers=new ArrayList<EnterpriseResourceData>(rs.size());
    	        for (ResourceData r: rs){
    	        	ers.add(r.getEnterpriseResource());
    	        }

				((ServerFileImporter)importer).prepareResources(ers,null,false);
				renumberProject();

    	        setProgress(1.0f);
                return null;
    		}
        });

    	job.addSwingRunnable(new JobRunnable("Import resources",1.0f){ //$NON-NLS-1$
			public Object run() throws Exception{
				ResourceMappingForm form=getResourceMapping();
				if (form!=null&&form.isLocal()) //if form==null we are in a case were have no server access. popup not needed
					if (!job.okCancel(Messages.getString("Message.ServerUnreacheableReadOnlyProject"),true)){ //$NON-NLS-1$
						setProgress(1.0f);
						throw new Exception(ABORT);
					}

				if(!importResources()){
					setProgress(1.0f);
					throw new JobCanceledException(ABORT);
				}
				setProgress(1f);
				return null;
	    	}
    	});

        job.addRunnable(new JobRunnable("Import",1.0f){ //$NON-NLS-1$
    		public Object run() throws Exception{
    	        System.out.println("Deserializing..."); //$NON-NLS-1$
    	        long t1=System.currentTimeMillis();
    	        ResourceMappingForm form=getResourceMapping();
//    	        project=serializer.deserializeProject(projectData,false,true,resourceMap);
    	        //DEF165936:  projectlibre1: .pod file import fails mapped to resource with modified calendar
    	        //pass the map into the serializer so it can grab the original impls
//    	        serializer.SetStuffForPODDeserialization(form.getExistingProject(), _localResourceMap); //claur
    	        Project project=serializer.deserializeProject(projectData,false,null,null,null,false);
    	        if (project!=null&&!Environment.getStandAlone()) project.setAllDirty();
    	        importer.setProject(project);
    	        long t2=System.currentTimeMillis();
    	        System.out.println("Deserializing...Done in "+(t2-t1)+" ms"); //$NON-NLS-1$ //$NON-NLS-2$

    	        //project.setDistributionMap(null);
    	        project.setGroupDirty(!Environment.getStandAlone());
    			/*if (!Environment.isNoPodServer())*/ project.setTemporaryLocal(true);

    			project.setAccessControlPolicy(form.getAccessControlType());
    			project.resetRoles(form.getAccessControlType()==0);
    			if (form.isLocal()) project.setLocal(true);
//    			project.setWasImported(true);

    	        setProgress(1.0f);
                return null;
    		}
        });



        return job;
    }

    Map<Long, com.projectlibre1.pm.resource.Resource> _localResourceMap;

	protected boolean importResources() throws Exception{
		ResourceMappingForm form=getResourceMapping();

		if (form!=null&&!form.isLocal()){
//			if (Environment.isNoPodServer()){ //claur
//				//importLocalResources.execute(null);
//				Project existingProject=form.getExistingProject();
//				existingProject.setMaster(true);
//				retrieveResourcesForMerge(existingProject);
//			}

			if (!form.execute()) return false;
			if (form.isLocal()){
				return true;
			}

			List<ResourceData> resources=new ArrayList<ResourceData>();
			Map<Long,ResourceData> resourceMap=new HashMap<Long,ResourceData>();

			Iterator r = form.getResources().iterator();
			while(r.hasNext()){
				EnterpriseResourceData enterpriseResource=(EnterpriseResourceData)r.next();
				if (enterpriseResource.getUniqueId()!=EnterpriseResource.UNASSIGNED_ID){
					ResourceData resource=new ResourceData();
					resource.setEnterpriseResource(enterpriseResource);
					resourceMap.put(enterpriseResource.getUniqueId(), resource);
					resources.add(resource);
				}
			}
			projectData.setResources(resources);


			Map<Long,ResourceData> idMap=new HashMap<Long,ResourceData>();
			Iterator ir = form.getImportedResources().iterator();
			Iterator sr = form.getSelectedResources().iterator();
			while(ir.hasNext()){
				EnterpriseResourceData enterpriseSrc=(EnterpriseResourceData)ir.next();
				EnterpriseResourceData enterpriseDest=(EnterpriseResourceData)sr.next();
				if (enterpriseDest.getUniqueId()!=EnterpriseResource.UNASSIGNED_ID){
					ResourceData resource=new ResourceData();
					resource.setEnterpriseResource(enterpriseDest);
					idMap.put(enterpriseSrc.getUniqueId(),resourceMap.get(enterpriseDest.getUniqueId()));
				}
			}


			//remove assignments that have lost resources
			for (TaskData task:(Collection<TaskData>)projectData.getTasks()){
				if (task.getAssignments()!=null)
				for (AssignmentData assignment:(Collection<AssignmentData>)task.getAssignments()){
					ResourceData resourceData=idMap.get(assignment.getUniqueId());
					if (resourceData==null){
						//assignment becomes unassigned
						assignment.setResource(null);
						assignment.setResourceId(-1L);
					}else{
						assignment.setResource(resourceData.getEnterpriseResource());
						assignment.setResourceId(resourceData.getEnterpriseResource().getUniqueId());
					}
				}
			}
			//remove distributions that have lost resources
			Collection<DistributionData> dists=(Collection<DistributionData>)projectData.getDistributions();
			if (dists!=null)
			for (Iterator<DistributionData> i=dists.iterator();i.hasNext();){
				DistributionData dist=i.next();
				ResourceData resourceData=idMap.get(dist.getResourceId());
				if (resourceData==null){
					i.remove();
				}else{
					dist.setResourceId(resourceData.getEnterpriseResource().getUniqueId());
				}
			}
		}

//		if (Environment.isNoPodServer()){ //claur
//			Project existingProject=form.getExistingProject();
//			projectData.setUniqueId(existingProject.getUniqueId()); //should be elsewhere, but it's easiest here in the import job
//		}


		return true;
	}



	protected void retrieveResourcesForMerge(Project existingProject) throws Exception{
		ResourceMappingForm form=getResourceMapping();
		if (form==null) return;


        Vector projectlibre1Resources=new Vector();
        //map the existint project resourse impls for later use
        _localResourceMap = new HashMap<Long, com.projectlibre1.pm.resource.Resource>();

		EnterpriseResourceData unassigned=new EnterpriseResourceData();
		unassigned.setUniqueId(EnterpriseResource.UNASSIGNED_ID);
		unassigned.setName(Messages.getString("Text.Unassigned")); //$NON-NLS-1$
		form.setUnassignedResource(unassigned);
		projectlibre1Resources.add(unassigned);

		Serializer serializer=new Serializer();

		ProjectData projectData=(ProjectData)serializer.serialize(existingProject,ProjectData.FACTORY,null);
        if (existingProject.isForceNonIncremental()) projectData.setVersion(0);
        projectData.setMaster(existingProject.isMaster());

        //resources
        Map resourceMap=serializer.saveResources(existingProject,projectData);
        List<com.projectlibre1.pm.resource.Resource> existingResources=(List<com.projectlibre1.pm.resource.Resource>)existingProject.getResourcePool().getResourceList();
        for (com.projectlibre1.pm.resource.Resource resource:existingResources){
        	if (resource==null) continue;
        	ResourceData r=(ResourceData)resourceMap.get(resource.getUniqueId());
        	if (r!=null){
        		EnterpriseResourceData er=r.getEnterpriseResource();
        		er.setName(resource.getName());
        		projectlibre1Resources.add(er);
        		_localResourceMap.put((Long)r.getUniqueId(), resource);
        	}
        }

		form.setResources(projectlibre1Resources);
	}


	protected void renumberProject() throws Exception{
		Session session=SessionFactory.getInstance().getSession(false);
		//renumber tasks
		Map<Long,Long> idMap=new HashMap<Long,Long>();
		for (TaskData task:(Collection<TaskData>)projectData.getTasks()){
			long oldUniqueId=task.getUniqueId();
			long uniqueId=session.getId();
			task.setUniqueId(uniqueId);
			idMap.put(oldUniqueId, uniqueId);
			if (task.getAssignments()!=null)
			for (AssignmentData assignment:(Collection<AssignmentData>)task.getAssignments()){
				assignment.setTaskId(uniqueId);
			}
		}

		Collection<DistributionData> dists=(Collection<DistributionData>)projectData.getDistributions();
		if (dists!=null)
		for (Iterator<DistributionData> i=dists.iterator();i.hasNext();){
			DistributionData dist=i.next();
			dist.setTaskId(idMap.get(dist.getTaskId()));
		}

		//renumber project
		projectData.setUniqueId(session.getId());
	}



    public Job getExportFileJob(){
    	return LocalFileImporter.getExportFileJob(this);
    }
    
    //disabled
    @Override
	public boolean saveProject(Project project,OutputStream out) throws Exception{
		return false;
	}
    @Override
	public Project loadProject(InputStream in)  throws Exception{
    	//disabled
    	return null;
	}


}
