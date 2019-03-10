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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.projectlibre1.job.Job;
import com.projectlibre1.job.JobQueue;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.util.ClassUtils;

/**
 *
 */
public class SessionFactory {
    protected static SessionFactory instance=null;
    protected SessionFactory() {
    }
    public static SessionFactory getInstance(){
        if (instance==null) instance=new SessionFactory();
        return instance;
    }
    
    protected Map<String,Session> sessionImpls=null;
    protected void initSessions(){
    	if (sessionImpls==null){
    		sessionImpls=new HashMap<String, Session>();
    		String impls=Messages.getMetaString("SessionImpls");
    		if (impls!=null){
    			StringTokenizer st=new StringTokenizer(impls,";");
    			while (st.hasMoreTokens()) {
					String key = st.nextToken();
					String implClass=Messages.getMetaString(key);
					if (implClass!=null){
			            try {
			            	Session session = (Session) ClassUtils.forName(implClass).newInstance();
			            	//session.init(credentials);
			            	if (session.getJobQueue()==null) session.setJobQueue(getJobQueue()); //because this method is called before jobQueue is set
			            	sessionImpls.put(key.substring(key.lastIndexOf('.')+1), session);
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
					}
				}
    		}
    	}
    }  	
    protected Session getSession(String name){
    	initSessions();
    	Session session=sessionImpls.get(name);
    	if (!session.isInitialized()) session.init(credentials);
    	return session;
    }
    public Session getSession(boolean local){
    	return local?getSession("local"):getSession("server");
    }
    
    public static Object call(Object object,String method,Class[] argsDesc, Object[] args) throws Exception{
    	try {
    		//System.out.println("call, "+method+"..."+object.getClass());
			return object.getClass().getMethod(method, argsDesc).invoke(object, args);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    public static Object callNoEx(Object object,String method,Class[] argsDesc, Object[] args){
    	try {
    		//System.out.println("callNoEx, "+method+"...");
			return object.getClass().getMethod(method, argsDesc).invoke(object, args);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    
    public void clearSessions() {
    	sessionImpls = null;
    }
    
    private final Map credentials=new HashMap();
    public void setCredentials(Map credentials){
    	if (credentials!=null){
    		this.credentials.clear();
    		this.credentials.putAll(credentials);
    	}
    }
    public String getLogin() {
    	return (String)credentials.get("login");
    	
    }
    public String getServerUrl(){
    	return (String)credentials.get("serverUrl");
    }

    public LocalSession getLocalSession(){
    	return (LocalSession)getSession("local");
    }
    
	protected JobQueue jobQueue=null;
	public JobQueue getJobQueue() {
		return jobQueue;
	}
	public void setJobQueue(JobQueue jobQueue) {
		this.jobQueue = jobQueue;
		if (sessionImpls==null) initSessions();
		for (Session session : sessionImpls.values())
			session.setJobQueue(jobQueue);
	}
	
	public void schedule(Job job){
    	jobQueue.schedule(job);
    }

    
}
