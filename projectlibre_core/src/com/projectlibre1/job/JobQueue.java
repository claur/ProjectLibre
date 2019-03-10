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
package com.projectlibre1.job;

import java.awt.Component;
import java.awt.Frame;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ProgressMonitor;
import javax.swing.event.EventListenerList;

import org.apache.commons.collections.Closure;

import com.projectlibre1.util.Environment;


/**
 *
 */
public class JobQueue extends ThreadGroup{
	public final static int MAX_PROGRESS=10000;
	protected boolean documentBased = false;
	public JobQueue(String name,boolean documentBased) {
		super(name);
		this.documentBased=documentBased;
	}

	public boolean hasNext(){
		return activeCount()!=0;
	}
	public synchronized void startNext(){
		if (hasNext()){
			Thread[] threads=new Thread[1];
			if (enumerate(threads)==1){
				threads[0].start();
			}
		}
	}
	public synchronized void cancel(){
		int count=activeCount();
		if (count==0) return;
		Thread[] threads=new Thread[count];
		count=enumerate(threads);
		for (int i=0;i<count;i++){
			if (threads[i] instanceof Job) ((Job)threads[i]).cancel();
		}
	}

	private Set<String> executingJobs=Collections.synchronizedSet(new HashSet<String>());

	public void addExecutingJob(Job job) {
		executingJobs.add(job.getName());
	}
	public void removeExecutingJob(Job job) {
		executingJobs.remove(job.getName());
	}

	public void schedule(Job job){
		if (executingJobs.contains(job.getName())) return;//to avoid double click
		job.execute();
	}


	public ExtendedProgressMonitor getProgressMonitor(String name,Component component){
		if (component==null) component=getComponent();
		if (component==null)
			return null;
		ExtendedProgressMonitor progressMonitor = new ExtendedProgressMonitor(component,
	                name,
	                "", 0, MAX_PROGRESS);
	    progressMonitor.setProgress(0);

	    progressMonitor.setMillisToPopup(0);
	    progressMonitor.setMillisToDecideToPopup(0);
	    //progressMonitor.setMillisToDecideToPopup(2000);
	    return progressMonitor;
	}

	protected void enableComponent(boolean enabled){
		if (getComponent()==null)
			return;
		getComponent().setEnabled(enabled);
	}


	protected EventListenerList queueListenerList = new EventListenerList();

	public void addListener(JobQueueListener l) {
		queueListenerList.add(JobQueueListener.class, l);
	}
	public void removeListener(JobQueueListener l) {
		queueListenerList.remove(JobQueueListener.class, l);
	}
	public JobQueueListener[] getListeners() {
		return (JobQueueListener[]) queueListenerList.getListeners(JobQueueListener.class);
	}
    public EventListener[] getListeners(Class listenerType) {
    	return queueListenerList.getListeners(listenerType);
    }

 	protected void fireProgressChanged(Object source,float progress) {
		Object[] listeners = queueListenerList.getListenerList();
		JobQueueEvent e = null;
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == JobQueueListener.class) {
				if (e == null) {
					e = new JobQueueEvent(source,progress);
				}
				((JobQueueListener) listeners[i + 1]).progressChanged(e);

			}
		}
	}

	protected Object criticalSectionMutex=new Object();

 	protected Job criticalSectionOwner;

 	//for free jobs (queued==false)
	public boolean executeCriticalSectionClosure(Job job,Closure c,Object arg) {
		 synchronized (criticalSectionMutex) {
			 if (criticalSectionOwner==job){
				 c.execute(arg);
				 return true;
			 }else{
			 	 System.out.println(job.getName()+" can execute, lost critical section");
				 return false;
			 }
		 }
	}

	public void beginCriticalSection(Job job){
		synchronized (criticalSectionMutex) {
			while (criticalSectionOwner!=null&&criticalSectionOwner.isQueued()){
				try {
					criticalSectionMutex.wait();
				} catch (InterruptedException e) {
				}
			}
	 		criticalSectionOwner=job;
	 		job.logBegin("Critical section");
		}
 	}

 	public void endCriticalSection(Job job){
		synchronized (criticalSectionMutex) {
	 		job.logEnd("Critical section");
			if (criticalSectionOwner==job){
				criticalSectionOwner=null;
				criticalSectionMutex.notify();
			}

		}
 	}

	private static final String GRAPHIC_MANAGER="com.projectlibre1.pm.graphic.frames.GraphicManager";
	public Component getComponent(){
		if (!Environment.isVisible())
			return null;
		String methodName = documentBased ? "getDocumentFrameInstance" : "getFrameInstance";
		try {
		    return (Frame)Class.forName(GRAPHIC_MANAGER).getMethod(methodName,null).invoke(null,null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
