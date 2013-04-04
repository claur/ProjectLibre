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
package com.projity.session;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.Closure;

import com.projity.exchange.FileImporter;
import com.projity.grouping.core.model.DefaultNodeModel;
import com.projity.job.Job;
import com.projity.job.JobRunnable;
import com.projity.pm.resource.ResourcePool;
import com.projity.pm.resource.ResourcePoolFactory;
import com.projity.pm.task.Project;
import com.projity.pm.task.ProjectFactory;
import com.projity.strings.Messages;
import com.projity.undo.DataFactoryUndoController;
import com.projity.util.Alert;
import com.projity.util.ClassUtils;
import com.projity.util.Environment;

public class LocalSession extends AbstractSession{
	public static final String LOCAL_PROJECT_IMPORTER = "com.projity.exchange.LocalFileImporter";
	public static final String SERVER_LOCAL_PROJECT_IMPORTER = "com.projity.exchange.ServerLocalFileImporter";
	public static final String MICROSOFT_PROJECT_IMPORTER = "com.projity.exchange.MicrosoftImporter";
	
	
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
        FileImporter importer=getImporter(opt.getImporter());
		importer.setJobQueue(jobQueue);
		importer.setProjectFactory(ProjectFactory.getInstance());//used?
		int count=projs.size();
		int i=0;
		for (final Project project : projs) {
			//if projs.size()>1 opt.getFileName() must be null
			String fileN=(opt.getFileName()==null)?project.getGuessedFileName():opt.getFileName();//+(count>1?("("+i+")"):""));
			if (!FileHelper.isFileNameAllowed(fileN, true)){
				fileN=SessionFactory.getInstance().getLocalSession().chooseFileName(true,FileHelper.changeFileExtension(fileN, /*project.getFileType()*/FileHelper.PROJITY_FILE_TYPE));
			}
			final String fileName=fileN;
			if (fileName==null) continue;
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
		case FileHelper.PROJITY_FILE_TYPE: return LOCAL_PROJECT_IMPORTER;
		case FileHelper.MSP_FILE_TYPE: return MICROSOFT_PROJECT_IMPORTER;
		default:
			return null;
		}
    }
	public boolean projectExists(long id) {
		return true;
	}

   
}
