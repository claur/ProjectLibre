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
package com.projectlibre1.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;

import com.projectlibre1.company.ApplicationUser;
import com.projectlibre1.contrib.ClassLoaderUtils;
import com.projectlibre1.session.SessionFactory;

public class Environment {
	private static boolean clientSide = false;
	private static boolean standAlone = false;
	private static boolean batchMode = false;
	private static ApplicationUser user = null;
	private static String partnerId = null;
	private static String login = null;
	private static boolean importing = false;
	private static boolean ribbonUI = true;
	private static boolean newLook = false;
	private static boolean newLaf = false;
	private static boolean scripting = false;
	private static boolean visible = true;
	private static boolean applet = false;
	private static boolean outOfMemory = false;
	public static int LINUX=1;
	public static int MAC=2;
	private static int os=-1;
	private static boolean updated=false;
	private static boolean needToRestart = false;
	protected static boolean keepExternalLinks=true;
	private static boolean plugin = false;
	public static final boolean isBatchMode() {
		return batchMode;
	}
	public static final void setBatchMode(boolean processingUndoRedo) {
		Environment.batchMode = processingUndoRedo;
	}
	public Environment() {
		super();
		// TODO Auto-generated constructor stub
	}
	public static String getLogin() {
		if (login == null)
		   login = SessionFactory.getInstance().getLogin();
		return login;
	}
	public static final boolean isClientSide() {
		return clientSide;
	}
	public static final void setClientSide(boolean clientSide) {
		Environment.clientSide = clientSide;
	}

	public static float getJavaVersionNumber() {
		String javaVersion = System.getProperty("java.specification.version");
		return Float.parseFloat(javaVersion);
	}
	public static final boolean getStandAlone() {
		return standAlone;
	}
	public static final void setStandAlone(boolean standAlone) {
		Environment.standAlone = standAlone;
	}
	public static final ApplicationUser getUser() {
		return user;
	}
	public static final void setUser(ApplicationUser user) {
		Environment.user = user;
	}
	public static final boolean isAdministrator() {
		return user.isAdministrator();
	}
	public static final boolean isExternal() {
		return user.isExternal();
	}
	public static final String getPartnerId() {
		return partnerId;
	}
	public static final void setPartnerId(String partnerId) {
		Environment.partnerId = partnerId;
	}
	public static final boolean isWindows() {
		//false for some linux window managers
		return true;//System.getProperty("os.name").toUpperCase().contains("WINDOWS");
	}
	public static boolean isImporting() {
		return importing;
	}
	public static void setImporting(boolean importing) {
		System.out.println("set importing " + importing);
		Environment.importing = importing;
	}
	public static boolean isNewLook() {
		return newLook;// || isRibbonUI();
	}
	public static void setNewLook(boolean newLook) {
		Environment.newLook = newLook;
//Environment.setNewLaf(false);
//		Environment.setNewLaf(newLook && Environment.getJavaVersionNumber() >= 1.5f && Environment.getOs()!=Environment.LINUX && Environment.getOs()!=Environment.MAC
//		&& !Environment.isChinese());
	}
	public static boolean isNewLaf() {
	return false;
	//	return newLaf;
	}
	public static void setNewLaf(boolean newLaf) {
		Environment.newLaf = newLaf;
	}
	public static boolean isScripting() {
		return scripting;
	}
	public static void setScripting(boolean scripting) {
		Environment.scripting = scripting;
	}
	public static boolean isVisible() {
		return visible;
	}
	public static void setVisible(boolean visible) {
		Environment.visible = visible;
	}
	public static boolean isProjectLibre() {
		return getStandAlone();
	}
	public static boolean isApplet() {
		return applet;
	}
	public static void setApplet(boolean applet) {
		Environment.applet = applet;
	}
	public static int getOs() {
		if (os==-1){
			String osName=System.getProperty("os.name").toLowerCase();
			if (osName.startsWith("linux")) os=LINUX;
			else if (osName.startsWith("mac os x")) os=MAC;
			else os=0;
		}
		return os;
	}
	public static boolean isMac(){
		return Environment.getOs()==Environment.MAC;
	}

	public static final int DEFAULT_FONT=0;
	public static final int GANTT_ANNOTATIONS_FONT=1;
	public static final int NETWORK_FONT=2;

	private static HashMap<Integer, String> fonts=new HashMap<Integer, String>();
	static{
		fonts.put(GANTT_ANNOTATIONS_FONT,"TimesRoman BOLD 11");
		fonts.put(NETWORK_FONT, "TimesRoman");
	}
	public static String getFont(int type) {
		String font=fonts.get(type);
		return font==null?fonts.get(DEFAULT_FONT):font;
	}
	public static void setFont(String font,int type) {
		fonts.put(type, font);
	}
	public static void resetFonts(){
		fonts.clear();
	}

	public static boolean isChinese(){
		Locale locale = Locale.getDefault();
		return locale.equals(Locale.SIMPLIFIED_CHINESE) || locale.equals(Locale.TRADITIONAL_CHINESE);
	}

	/**
    * Compares this version with the specified version for order.  Returns a
    * negative integer, zero, or a positive integer as this version is less
    * than, equal to, or greater than the specified version.
    */
	public static int compareJavaVersionTo(String version){
		return ClassLoaderUtils.compareJavaVersionTo(version);
	}
	public static int compareJavaVersion(String version1,String version2){
		return ClassLoaderUtils.compareJavaVersion(version1, version2);
	}
	public static boolean isOutOfMemory() {
		return outOfMemory;
	}
	public static void setOutOfMemory(boolean outOfMemory) {
		Environment.outOfMemory = outOfMemory;
	}
	public static boolean isUpdated() {
		return updated;
	}
	public static void setUpdated(boolean updated) {
		Environment.updated = updated;
	}
	public static boolean isNeedToRestart() {
		return needToRestart;
	}
	public static void setNeedToRestart(boolean needToRestart) {
		Environment.needToRestart = needToRestart;
	}
	public static boolean isKeepExternalLinks() {
		return keepExternalLinks;
	}
	public static void setKeepExternalLinks(boolean keepExternalLinks) {
		Environment.keepExternalLinks = keepExternalLinks;
	}
	public static boolean isPlugin() {
		return plugin;
	}
	public static void setPlugin(boolean plugin) {
		Environment.plugin = plugin;
	}
	public static boolean isRibbonUI() {
		return ribbonUI;
	}
	public static void setRibbonUI(boolean ribbonUI) {
		Environment.ribbonUI = ribbonUI;
	}


}
