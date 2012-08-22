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
package com.projity.job;

import java.awt.Component;
import java.awt.Frame;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ProgressMonitor;
import javax.swing.event.EventListenerList;

import org.apache.commons.collections.Closure;

import com.projity.util.Environment;


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

	private static final String GRAPHIC_MANAGER="com.projity.pm.graphic.frames.GraphicManager";
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
