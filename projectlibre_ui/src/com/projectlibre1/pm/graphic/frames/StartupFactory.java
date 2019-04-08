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
package com.projectlibre1.pm.graphic.frames;

import java.awt.Container;
import java.awt.HeadlessException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import org.apache.commons.collections.Closure;
import org.projectlibre1.util.UpdateChecker;

import com.projectlibre1.company.DefaultUser;
import com.projectlibre1.configuration.Configuration;
import com.projectlibre1.configuration.ConfigurationReader;
import com.projectlibre1.configuration.Dictionary;
import com.projectlibre1.configuration.Settings;
import com.projectlibre1.contrib.ClassLoaderUtils;
import com.projectlibre1.dialog.LicenseDialog;
import com.projectlibre1.dialog.LoginDialog;
import com.projectlibre1.dialog.LoginForm;
import com.projectlibre1.dialog.TipOfTheDay;
import com.projectlibre1.dialog.UserInfoDialog;
import com.projectlibre1.pm.graphic.laf.LafManagerImpl;
import com.projectlibre1.pm.task.Project;
import com.projectlibre1.pm.task.ProjectFactory;
import com.projectlibre1.server.access.PartnerInfo;
import com.projectlibre1.server.data.ProjectData;
import com.projectlibre1.session.Session;
import com.projectlibre1.session.SessionFactory;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.util.Alert;
import com.projectlibre1.util.DebugUtils;
import com.projectlibre1.util.Environment;
import com.projectlibre1.util.VersionUtils;

public abstract class StartupFactory {
	public static final String defaultServerUrl = Settings.SITE_HOME;
	private static final int NUM_INVALID_LOGINS = 3;


	protected String serverUrl=null;
	protected String[] projectUrls=null;
	protected String login=null;
	protected String password=null;
	protected Map credentials=new HashMap();
	protected long projectId;
	protected HashMap opts=null;

	protected StartupFactory() {
//		System.out.println("---------- StartupFactory");
	}

	/**
	 * Used to test restoring of workspace to simulate applet restart
	 * @param old
	 * @return
	 */
	public GraphicManager restart(GraphicManager old) {
		RootPaneContainer con = (RootPaneContainer) old.getContainer();
		old.encodeWorkspace();
		old.cleanUp();
		con.getContentPane().removeAll();
		GraphicManager g = instanceFromExistingSession((Container) con);
//		g.decodeWorkspace();

//		System.out.println("restarted");
		return g;
	}

	public GraphicManager instanceFromExistingSession(Container container) {


		System.gc(); // hope to avoid out of memory problems

		DebugUtils.isMemoryOk(true);


		long t=System.currentTimeMillis();
//		System.out.println("---------- StartupFactory instanceFromExistingSession#1");
		final GraphicManager graphicManager = new GraphicManager(container);
		graphicManager.setStartupFactory(this);
		SessionFactory.getInstance().setJobQueue(graphicManager.getJobQueue());
		//if (Environment.isNewLook())
			graphicManager.initLookAndFeel();
//		System.out.println("---------- StartupFactory instanceFromExistingSession#1 done in "+(System.currentTimeMillis()-t)+" ms");
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				long t=System.currentTimeMillis();
//				System.out.println("---------- StartupFactory instanceFromExistingSession#2");
				graphicManager.initView();
//				System.out.println("---------- StartupFactory instanceFromExistingSession#2 done in "+(System.currentTimeMillis()-t)+" ms");
			}});
//		graphicManager.invalidate();
		return graphicManager;
	}


	public GraphicManager instanceFromNewSession(Container container,  final boolean doWelcome) {
		VersionUtils.versionCheck(true);
		if (!VersionUtils.isJnlpUpToDate()) System.out.println("Jnlp isn't up to date, current version is: "+VersionUtils.getJnlpVersion());
		long t=System.currentTimeMillis();
//		System.out.println("---------- StartupFactory instanceFromNewSession#1 main");
		Environment.setClientSide(true);

		System.setSecurityManager(null);
		Thread loadConfigThread=new Thread("loadConfig"){
			public void run() {
				long t=System.currentTimeMillis();
//				System.out.println("---------- StartupFactory instanceFromNewSession#1 doLoadConfig");
				doLoadConfig();
//				System.out.println("---------- StartupFactory instanceFromNewSession#1 doLoadConfig done in "+(System.currentTimeMillis()-t)+" ms");
			}
		};
		loadConfigThread.start();

		GraphicManager graphicManager = null;
		//String projectUrl[]=null;
		try {
			graphicManager=new GraphicManager(/*projectUrl,*/serverUrl,container);
			graphicManager.setStartupFactory(this);
		} catch (HeadlessException e) {
			e.printStackTrace();
		}
		graphicManager.setConnected(false);

		if (!doLogin(graphicManager)) return null;
		//if (Environment.isNewLook())
			graphicManager.initLookAndFeel();

		SessionFactory.getInstance().setJobQueue(graphicManager.getJobQueue());

		PartnerInfo partnerInfo=null;
		if (!Environment.getStandAlone()) {
			Session session = SessionFactory.getInstance().getSession(false);
			try {
				partnerInfo=(PartnerInfo)SessionFactory.call(session,"retrievePartnerInfo",null,null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

//		System.out.println("---------- StartupFactory instanceFromNewSession#1 main done in "+(System.currentTimeMillis()-t)+" ms");
		try {
			loadConfigThread.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		t=System.currentTimeMillis();
//		System.out.println("---------- StartupFactory instanceFromNewSession#2");

		//graphicManager.showWaitCursor(true); //TODO use a progress bar - maybe a Job
		if (partnerInfo!=null){

			if (partnerInfo.getConfigurationXML() != null) {
				ConfigurationReader.readString(partnerInfo.getConfigurationXML(),Configuration.getInstance());
				Configuration.getInstance().setDonePopulating();
			}
			if (partnerInfo.getViewXML() != null) {
				ConfigurationReader.readString(partnerInfo.getViewXML(),Dictionary.getInstance());
			}
		}

		final GraphicManager gm = graphicManager;
		graphicManager.beginInitialization();
		try{

			graphicManager.initView();
			doStartupAction(gm,projectId,(projectUrls==null&&gm.getLastFileName()!=null)?new String[]{gm.getLastFileName()}:projectUrls,doWelcome,false);

			doPostInitView(gm.getContainer());
			
			
			
//			final Container cc=container;
//			SwingUtilities.invokeLater(new Runnable() {
//
//			    @Override
//			    public void run() {
//					//cc.setVisible(true);
//					gm.initView();
//					doStartupAction(gm,projectId,(projectUrls==null&&gm.getLastFileName()!=null)?new String[]{gm.getLastFileName()}:projectUrls,doWelcome,false);
//
//					doPostInitView(gm.getContainer());
//			    }
//			});


		}finally{
			graphicManager.finishInitialization();
		}
        return graphicManager;
	}

	public void doLoadConfig() {
		com.projectlibre1.init.Init.initialize();
	}
	public void doPostInitView(Container container) {
	}

	public boolean doLogin(GraphicManager graphicManager) {
		if (Environment.getStandAlone()){
//			graphicManager.getFrame().setVisible(true);
			Environment.setUser(new DefaultUser());
			return true;
		}
		credentials.put("serverUrl",serverUrl);
		getCredentials();
		Environment.setNewLook(true);

		int badLoginCount = 0;
		while (true) { // until a good login or exit because of too many bad
//			graphicManager.getFrame().setVisible(true);
			if (login==null||password==null || badLoginCount > 0){
				URL loginUrl=null;
				if (login==null||password==null){
					try {
						loginUrl=new URL(serverUrl+"/login");
					} catch (MalformedURLException e) {}
				}
				LoginForm form = LoginDialog.doLogin(graphicManager.getFrame(),loginUrl); // it's actually a singleton
				if (form.isCancelled())
					System.exit(-1);
				if (form.isUseMenus())
					Environment.setNewLook(false);

				login=form.getLogin();
				password=form.getPassword();
			}

			if ("_SA".equals(login)||Environment.getStandAlone()) {// for testing purposes!
				Environment.setStandAlone(true);
				Environment.setUser(new DefaultUser());
				break;
			} else {
				credentials.put("login",login);
				credentials.put("password",password);


				SessionFactory.getInstance().setCredentials(credentials);
				try {
					Session session = SessionFactory.getInstance().getSession(false);
				System.out.println("logging in");
					final GraphicManager gm = graphicManager;
					SessionFactory.callNoEx(session,"login",new Class[]{Closure.class},new Object[]{new Closure(){
						public void execute(Object arg0) {
							Map<String,String> env=(Map<String,String>)arg0;
							if (env!=null){
								String serverVersion=env.get("serverVersion");
								checkServerVersion(serverVersion);
							}
							gm.setConnected(true);

						}
					}});
					if (!((Boolean)SessionFactory.callNoEx(session,"isLicensedToRunClient",null,null)).booleanValue()) {
						Alert.error(Messages.getString("Error.roleCantRunClient"));
						abort();
						return false;
					}

//					System.out.println("Application started with args: credentials=" + credentials.get("login") + " name " + session.getUser().getName() + " Roles " + session.getUser().getServerRoles());
					break;
					//			TODO test if login is valid.  If not, reshow login dialog
				} catch (Exception e) {
					if (Session.EXPIRED.equals(e.getMessage())) {
						Alert.error(Messages.getString("Error.accountExpired"));
						abort();
						return false;

					}
					System.out.println("failure " + e);
					badLoginCount++;
					SessionFactory.getInstance().clearSessions();

					if (badLoginCount == NUM_INVALID_LOGINS) {
						Alert.error(Messages.getString("Login.tooManyBad"));
						abort();
						return false;
					} else {
						Alert.error(Messages.getString("Login.error"));
					}
				}
			}
		}
		return true;
	}

	protected void checkServerVersion(String serverVersion){
		String thisVersion=null;
		if (serverVersion!=null){
			thisVersion=VersionUtils.getVersion();
			if (thisVersion!=null) thisVersion=VersionUtils.toAppletVersion(thisVersion);
			if(thisVersion==null||serverVersion.equals(thisVersion)) return; //ok
		}
		String jnlpUrl="";//https://www.projectlibre.com/web/jnlp/projectlibre.jnlp";
		if (Alert.okCancel(Messages.getString("Text.newPODVersion"))){
			try {
				Object basicService = ClassLoaderUtils.forName("javax.jnlp.ServiceManager").getMethod("lookup", new Class[]{String.class})
				.invoke(null, new Object[] {"javax.jnlp.BasicService"});
				ClassLoaderUtils.forName("javax.jnlp.BasicService").getMethod("showDocument", new Class[]{URL.class})
				.invoke(basicService, new Object[] {new URL(jnlpUrl)});
			} catch(Exception e) {
				//e.printStackTrace();
				// Not running in JavaWebStart or service is not supported.
				return;
				//Runtime.getRuntime().exec("javaws ");
			}
//			try {
//			BasicService basicService=(BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
//			basicService.showDocument(/*new URL(basicService.getCodeBase(),*/new URL(jnlpUrl));
//			}catch (UnavailableServiceException e) {
//			Runtime.getRuntime().exec("javaws ");
//			}
			System.exit(0);
		}
	}




/*
 * Returns null if shouldn't open, returns false if open read only, true if open writable
 *
 */	public static Boolean verifyOpenWritable(Long projectId) {
		if (projectId == null || projectId == 0)
			return null;
		if (ProjectFactory.getInstance().isResourcePoolOpenAndWritable()) {
			Alert.warn(Messages.getString("Warn.resourcePoolOpen"));
			return null;
		}

		String locker = getLockerName(projectId);
		boolean openAs = false;
		if (locker != null) {
			openAs = (JOptionPane.YES_OPTION == Alert.confirmYesNo(Messages.getStringWithParam("Warn.lockMessage",locker)));
			if (openAs == false)
				return null;
		}
		return !openAs;
	}
	public static String getLockerName(long projectId) {
		ProjectData projectData = (ProjectData)ProjectFactory.getProjectData(projectId);
		System.out.println("Locked is " + projectData.isLocked() + "  Lock info: User is " + Environment.getUser().getUniqueId() + "  locker id is " + projectData.getLockedById() + " locker is "+projectData.getLockedByName() );

		if (projectData != null && projectData.isLocked()) {

			if (Environment.getUser().getUniqueId() != projectData.getLockedById())
				return projectData.getLockedByName();
		}
		return null;
	}



	protected abstract void abort();
	protected void getCredentials() {
	}
	public void doStartupAction(final GraphicManager gm, final long projectId, final String[] projectUrls, final boolean welcome, boolean readOnly) {
		if (Environment.isClientSide()) {
			if (projectId > 0) {

				Boolean writable = null;
				if (readOnly)
					writable = Boolean.FALSE;
				else
					writable = verifyOpenWritable(projectId);
				if (writable == null)
					return;
				gm.loadDocument(projectId, true,!writable,new Closure(){
					public void execute(Object arg0) {
						Project project=(Project)arg0;
						DocumentFrame frame=gm.getCurrentFrame();
						if (frame!=null&&frame.getProject().getUniqueId() != projectId) {
							gm.switchToProject(projectId);
						}
					}
				});
			}
			else if (projectUrls!=null && projectUrls.length > 0) {
				gm.loadLocalDocument(projectUrls[0],!Environment.getStandAlone());
			}else{
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (Environment.isProjectLibre()&&!Environment.isPlugin()) {
							LicenseDialog.showDialog(gm.getFrame(),false);
							UserInfoDialog.showDialog(gm.getFrame(),false);
//							DonateDialog.maybeShow(gm.getFrame(),false);
							UpdateChecker.checkForUpdateInBackground();
						}
						if (welcome&&!Environment.isPlugin()) {
							if (Environment.isProjectLibre()) {
								//LicenseDialog.showDialog(gm.getFrame(),false);
								TipOfTheDay.showDialog(gm.getFrame(), false);
							} else {
								if (Environment.isNeedToRestart())
									return;
								if (!LafManagerImpl.isLafOk()) // for startup glitch - we don't want people to work until restarting.
									return;

							}
							gm.doWelcomeDialog();
						}
						if (Environment.isPlugin()) gm.doNewProjectNoDialog(opts);
					}
				});

			}
		}

	}


}
