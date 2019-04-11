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
package com.projectlibre1.session;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.Closure;

import com.projectlibre1.exchange.FileImporter;
import com.projectlibre1.grouping.core.model.DefaultNodeModel;
import com.projectlibre1.job.Job;
import com.projectlibre1.job.JobRunnable;
import com.projectlibre1.pm.resource.ResourcePool;
import com.projectlibre1.pm.resource.ResourcePoolFactory;
import com.projectlibre1.pm.task.Project;
import com.projectlibre1.pm.task.ProjectFactory;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.undo.DataFactoryUndoController;
import com.projectlibre1.util.Alert;
import com.projectlibre1.util.ClassUtils;
import com.projectlibre1.util.Environment;

public class LocalSession extends AbstractSession{
	public static final String LOCAL_PROJECT_IMPORTER = "com.projectlibre1.exchange.LocalFileImporter";
	public static final String SERVER_LOCAL_PROJECT_IMPORTER = "com.projectlibre1.exchange.ServerLocalFileImporter";
	public static final String MICROSOFT_PROJECT_IMPORTER = "com.projectlibre1.exchange.MicrosoftImporter";
	
	
	protected long localSeed;
	public synchronized long getId(){
		return localSeed++;
	}
    public Job getCloseProjectsJob(final Collection projects){
    	Job job=new Job(jobQueue,"closeProjects","Closing...",false);
    	job.addRunnable(new JobRunnable("LocalAccess: closeProjects",0.1f){
    		public Object run() throws Exception{
				setProgress(1.0f);
    			return null;
    		}
    	});
    	job.addExceptionRunnable(new JobRunnable("Local: exception"){
    		public Object run() throws Exception{
    			Alert.error(Messages.getString("Message.serverError"));
    			return null;
    		}
    	});
    	return job;
    }

    
    
    
    public Job getLoadProjectJob(final LoadOptions opt){
    	final Job job=new Job(jobQueue,"loadProject","Loading...",true);
        job.setCancelMonitorClosure(new Closure(){
			public void execute(Object o) {
				System.out.println("Monitor Canceled");
				jobQueue.endCriticalSection(job);
			}
        });
		try {
			final FileImporter importer = (FileImporter) ClassUtils.forName(opt.getImporter()).newInstance();
	    	importer.setFileName(opt.getFileName());
	    	importer.setFileInputStream(opt.getFileInputStream());
	    	importer.setResourceMapping(opt.getResourceMapping());
	    	importer.setProjectFactory(ProjectFactory.getInstance());//used?
	    	importer.setJobQueue(jobQueue);
	        
	        job.addSwingRunnable(new JobRunnable("LocalAccess: loadProject.begin",1.0f){
	    		public Object run() throws Exception{
	    			ResourcePool resourcePool=null;
	    			if (MICROSOFT_PROJECT_IMPORTER.equals(opt.getImporter())){
	    				DataFactoryUndoController undoController=new DataFactoryUndoController();
	    				resourcePool = ResourcePoolFactory.getInstance().createResourcePool("",undoController);
	    				resourcePool.setLocal(importer.getResourceMapping()==null);
	    				Project project = Project.createProject(resourcePool,undoController);
	    				
	    				((DefaultNodeModel)project.getTaskOutline()).setDataFactory(project);		
	    				importer.setProject(project);
	    			}
	     			setProgress(1.0f);
	                return null;
	    		}
	        });
	    	job.addJob(importer.getImportFileJob());
	        job.addRunnable(new JobRunnable("LocalAccess: loadProject.end",1.0f){
	    		public Object run() throws Exception{
	    	    	Project project=importer.getProject();
	    	    	project.setFileName(opt.getFileName()); //overrides project name
	    			if (MICROSOFT_PROJECT_IMPORTER.equals(opt.getImporter()))
	    				project.getResourcePool().setName(project.getName());
	    			if (Environment.getStandAlone()){ //force local in this case
	    				project.setMaster(true); //local project is always master
	    				project.setLocal(true);
	    			}
	     			setProgress(1.0f);
	                return project;
	 			
	    		}
	    	});
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     	return job;
    }

    
    public static FileImporter getImporter(String name){
		FileImporter importer=null;
		try {
			importer=(FileImporter) ClassUtils.forName(name).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return importer;
    }
    
    
    public Job getSaveProjectJob(final List<Project> projs,final SaveOptions opt){
    	final String title="Saving";
    	final Job job=new Job(jobQueue,"saveProject",title+"...",true);
        job.setCancelMonitorClosure(new Closure(){
			public void execute(Object o) {
				System.out.println("Monitor Canceled");
				jobQueue.endCriticalSection(job);
			}
        });
        //claur
//        FileImporter importer=getImporter(opt.getImporter());
//		importer.setJobQueue(jobQueue);
//		importer.setProjectFactory(ProjectFactory.getInstance());//used?
		int count=projs.size();
		int i=0;
		for (final Project project : projs) {
			//if projs.size()>1 opt.getFileName() must be null
			String fileN=(opt.getFileName()==null)?project.getGuessedFileName():opt.getFileName();//+(count>1?("("+i+")"):""));
			if (!FileHelper.isFileNameAllowed(fileN, true)){
				fileN=SessionFactory.getInstance().getLocalSession().chooseFileName(true,FileHelper.changeFileExtension(fileN, /*project.getFileType()*/FileHelper.PROJECTLIBRE_FILE_TYPE));
			}
			final String fileName=fileN;
			if (fileName==null) continue;
			
			//claur saving mpp as pod was selecting xml exporter
			if (fileName.endsWith(".pod")){ //$NON-NLS-1$
				opt.setFileName(fileName);
				opt.setImporter(LocalSession.LOCAL_PROJECT_IMPORTER);
			}
			else{
				opt.setFileName(fileName/*+((fileName.endsWith(".xml"))?"":".xml")*/);
				opt.setImporter(LocalSession.MICROSOFT_PROJECT_IMPORTER);

			}
	        FileImporter importer=getImporter(opt.getImporter());
			importer.setJobQueue(jobQueue);
			importer.setProjectFactory(ProjectFactory.getInstance());//used?

			
			
			importer.setFileName(fileName);
			importer.setProject(project);
			if (opt.getPreSaving() != null)
				opt.getPreSaving().execute(project);

			job.addJob(importer.getExportFileJob());
			job.addRunnable(new JobRunnable("Local: saveProject end"){
				public Object run() throws Exception{
					project.setFileName(fileName);
		    		project.setGroupDirty(false);
					if (opt.getPostSaving()!=null) opt.getPostSaving().execute(project);
	    	    	return null;
				}
			});


        	//setProgress(((float)++i)/((float)count));
		}
     	job.addExceptionRunnable(new JobRunnable("Local: exception"){
    		public Object run() throws Exception{
    			Alert.error(Messages.getString("Message.serverError"));
    			return null;
    		}
    	});
    	return job;
     }
    
    
    private FileHelper fileHelper = null;
    private FileHelper getFileHelper() {
    	if (fileHelper == null)
    		fileHelper = new FileHelper();
    	return fileHelper;
    }
 
    public String chooseFileName(final boolean save,String selectedFileName){
    	return getFileHelper().chooseFileName(save, selectedFileName, getJobQueue().getComponent());
    }
    
    public static String getImporter(int fileType){
    	switch (fileType) {
		case FileHelper.PROJECTLIBRE_FILE_TYPE: return LOCAL_PROJECT_IMPORTER;
		case FileHelper.MSP_FILE_TYPE: return MICROSOFT_PROJECT_IMPORTER;
		default:
			return null;
		}
    }
	public boolean projectExists(long id) {
		return true;
	}

   
}
