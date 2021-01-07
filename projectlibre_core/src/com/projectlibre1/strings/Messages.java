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
package com.projectlibre1.strings;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.projectlibre1.preference.ConfigurationFile;
import com.projectlibre1.util.ClassLoaderUtils;
import com.projectlibre1.util.Environment;

/**
 *
 */
public class Messages {
	private static final String META_BUNDLE_NAME = "com.projectlibre1.configuration.meta"; //$NON-NLS-1$
	private static ResourceBundle metaBundle = null;

	public static void setMetaBundle(String bundleName) {
		metaBundle = ResourceBundle.getBundle(bundleName,Locale.getDefault(),ClassLoaderUtils.getLocalClassLoader()/*Messages.class.getClassLoader()*/);
	}

	public static String getMetaString(String key) {
		if (metaBundle==null){
			lock.lock(); //use lock to avoid useless synchronized when it's already initialized
			try{
				if (metaBundle==null){ //if it hasn't been initialized by an other thread
					metaBundle=ResourceBundle.getBundle(META_BUNDLE_NAME,Locale.getDefault(),ClassLoaderUtils.getLocalClassLoader()/*Messages.class.getClassLoader()*/);
				}
			}finally{
				lock.unlock();
			}
		}
		return metaBundle.getString(key);
	}

	static LinkedList<ResourceBundle> bundles = null;
	static Lock lock=new ReentrantLock();

	private static ResourceBundle[] bundleArray = null;
	private static String getStringFromBundles(String key) {
		if (key==null)
			return null;
		LinkedList<ResourceBundle> buns = new LinkedList<ResourceBundle>();
		LinkedList<String> foundBundles = new LinkedList<String>();;
		if (bundles==null) {
			lock.lock(); //use lock to avoid useless synchronized when it's already initialized
			try {
				if (bundles==null){ //if it hasn't been initialized by an other thread
					String bundleNames[] = getMetaString("ResourceBundles").split(";");
					String directoryBundleNames[] = getMetaString("DirectoryResourceBundles").split(";");

					for (int i =0; i < directoryBundleNames.length;i++) {
						try {
							ResourceBundle bundle=ConfigurationFile.getDirectoryBundle(directoryBundleNames[i]);
							if (bundle==null)
								continue;
							buns.add(bundle);
							foundBundles.add("com.projectlibre1.strings."+directoryBundleNames[i]);
						}catch (Exception e) {
							e.printStackTrace();
						}
					}

					for (int i =bundleNames.length-1; i >=0; i--) { // reverse order since the later ones should be searched first
						String bname=bundleNames[i];
						
						//find right position to insert in bundles

						int pos=0;
						boolean found=false;
						for (String b : foundBundles){
							if (bname.equals(b)) {
								found=true;
								break;
							}
							pos++;
						}
						if (!found) { 
							buns.add(pos,ResourceBundle.getBundle(bname,Locale.getDefault(),ClassLoaderUtils.getLocalClassLoader()/*Messages.class.getClassLoader()*/));
							foundBundles.add(pos,bname);
						}
					}
				}
			} finally {
				bundles = buns;
				lock.unlock();
			}
		}
		for (ResourceBundle bundle : bundles) {
			try {
				return bundle.getString(key);
			} catch (MissingResourceException e) {
			}
		}
		return null;
	}

	/**
	 * @param key
	 * @return
	 */
	public static String getString(String key) {

		if (key==null) return null;
		String result = getStringFromBundles(key);
		if (result == null)
			result = '!' + key + '!';
		return result;
	}
	public static String getStringOrSelf(String key) {
		if (key==null)
			return null;
		String result = getStringFromBundles(key);
		if (result == null)
			result = key;
		return result;

	}
    public static Properties getTipProperties() {
    	return getProperties(bundles.get(1));
    }
    public static Properties getProperties(ResourceBundle bundle) {
        Properties properties = new Properties();

        for (Enumeration keys = bundle.getKeys(); keys.hasMoreElements();) {
            String key = (String) keys.nextElement();
            properties.put(key, bundle.getString(key));
        }
        return properties;
    }

	public static String getStringWithParam(String key, String param) {
		return MessageFormat.format(getString(key),new Object[] {param});
	}
	public static String getStringWithParam(String key, Object[] params) {
		return MessageFormat.format(getString(key),params);
	}


	public static String toAppletVersion(String v){
		StringBuffer sb=new StringBuffer();
		String vNumbers[]=v.split("\\.");
		for (int i=0;i<4;i++){
			int vn=(i>=vNumbers.length)?0:Integer.parseInt(vNumbers[i]);
			if (i>0) sb.append('.');
			String hex=Integer.toHexString(vn);
			//for (int j=0;j<4-hex.length();j++) sb.append('0');
			sb.append(hex);
		}
		//System.out.println("toAppletVersion: "+v+" --> "+sb);
		return sb.toString();
	}
	public static String getContextString(String key) {
		if (Environment.isProjectLibre()) {
			String result = getStringFromBundles("Open_" + key);
			if (result == null) {
				System.out.println("getContextString not found Open_" + key);
			} else
				return result;
		}
		return getStringFromBundles(key);
	}

}
