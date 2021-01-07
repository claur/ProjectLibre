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
package com.projectlibre1.preference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import com.projectlibre1.session.FileHelper;

public class ConfigurationFile {
	   
	private static final String[] OPENPROJ_CONF_DIRS={".projectlibre","ProjectLibre"};
	private static File confFile;
	public static File getConfDir(){
		if (confFile==null){
	    	String home=System.getProperty("user.home");
	    	if (home!=null){
	    		File f;
	    		for (int i=0;i<OPENPROJ_CONF_DIRS.length;i++){
	    			f=new File(home+File.separator+OPENPROJ_CONF_DIRS[i]);
	        		if (f.isDirectory()){
	        			System.out.println("Conf file "+f.getPath()+" found");
	        			confFile=f;
	        			return f;
	        		}
	    		}
	     	}
		}
    	return confFile;
	}
	
	private static final String OPENPROJ_CONF_FILE="projectlibre.conf";
	private static Properties confProps;
	public static String getProperty(String key){
		if ("locale".equals(key)) {
			String locale=Preferences.userNodeForPackage(ConfigurationFile.class).get("locale","default");
			if (!"default".equals(locale)) {
				return locale;
			}
		}
		if (confProps==null){
			File confDir=getConfDir();
			if (confDir==null) return null;
			File f=new File(confDir,OPENPROJ_CONF_FILE);
			if (!f.exists()) return null;
			confProps=new Properties();
			try {
				FileInputStream in=new FileInputStream(f);
				confProps.load(in);
				in.close();
			} catch (Exception e) {}
		}
		return confProps.getProperty(key);
	}
	
	private static Locale locale=null;
	public static Locale getLocale(){
		if (locale==null){
			String l=getProperty("locale");
			if (l==null) locale=Locale.getDefault();
			else locale=getLocale(l);
		}
		return locale;
	}
	public static Locale getLocale(String code){
		Locale defaultLocale=Locale.getDefault();
		String language=null;
		String country=null;
		String variant=null;
		StringTokenizer st=new StringTokenizer(code,"_-");
		if (!st.hasMoreTokens()) locale=defaultLocale;
		else{
			language=st.nextToken();
			if (!st.hasMoreTokens()) locale=new Locale(language,defaultLocale.getCountry());
			else{
				country=st.nextToken();
				if (!st.hasMoreTokens()) locale=new Locale(language,country);
				else{
					variant=st.nextToken();
					locale=new Locale(language,country,variant);
				}
				
			}
			
		}
		return locale;
	}
	public static String[] getLocaleCodes(String code){
		Locale defaultLocale=Locale.getDefault();
		String language=null;
		String country=null;
		String variant=null;
		StringTokenizer st=new StringTokenizer(code,"_-");
		if (!st.hasMoreTokens()) locale=defaultLocale;
		else{
			language=st.nextToken();
			if (!st.hasMoreTokens()) locale=new Locale(language,defaultLocale.getCountry());
			else{
				country=st.nextToken();
				if (!st.hasMoreTokens()) locale=new Locale(language,country);
				else{
					variant=st.nextToken();
				}
				
			}
			
		}
		return new String[] {language, country, variant};
	}
	
	private static final String OPENPROJ_RUN_CONF_FILE="run.conf";
	private static Properties runProps;
	public static String getRunProperty(String key){
		if (runProps==null){
			File confDir=getConfDir();
			if (confDir==null) return null;
			File f=new File(confDir,OPENPROJ_RUN_CONF_FILE);
			if (!f.exists()) return null;
			runProps=new Properties();
			try {
				FileInputStream in=new FileInputStream(f);
				runProps.load(in);
				in.close();
			} catch (Exception e) {}
		}
		return runProps.getProperty(key);
	}
	
	public static File getGeneratedDirectory(String externalDirectory) {
		File directory=new File(externalDirectory,"import");
		return directory.isDirectory()?directory:null;

		//		File directory=null;
//		Preferences pref=Preferences.userNodeForPackage(ConfigurationFile.class);
//		if (pref.getBoolean("useExternalLocales",false)) {
//			String dir=pref.get("externalLocalesDirectory","");
//			directory=new File(dir,"generated");
//			if (!directory.isDirectory())
//				return null;
//		}		
//		return directory;
	}
	
	public static File getExportDirectory(String externalDirectory) {
		File directory=new File(externalDirectory,"export");
		return directory.isDirectory()?directory:null;
	}
	

	
	public static ResourceBundle getDirectoryBundle(String name) {
		File directory=null;
		Preferences pref=Preferences.userNodeForPackage(ConfigurationFile.class);
		if (pref.getBoolean("useExternalLocales",false)) {
			String dir=pref.get("externalLocalesDirectory","");
			directory=new File(dir,"import");
			if (!directory.isDirectory())
				return null;
		}
//		if (directory==null)
//			directory=getConfDir();
			
		try {
			URL[] urls={directory.toURI().toURL()};
			ClassLoader cl=new URLClassLoader(urls);
			ResourceBundle rb=ResourceBundle.getBundle(name, Locale.getDefault(), cl);
			return rb;
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		
//			Locale locale=Locale.getDefault();
//			try {
////				FileReader in = new FileReader(dir+File.separator+"client_"+locale+".properties");
//				FileInputStream in = new FileInputStream(dir+File.separator+name+"_"+locale+".properties");
//				return new PropertyResourceBundle(in);
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		
		return null;
	}


}
