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
package com.projity.strings;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.CollectionUtils;

import com.projity.util.ClassLoaderUtils;
import com.projity.util.Environment;

/**
 *
 */
public class Messages {
	private static final String META_BUNDLE_NAME = "com.projity.configuration.meta"; //$NON-NLS-1$
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
	static DirectoryClassLoader directoryClassLoader=new DirectoryClassLoader();
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
					if (directoryClassLoader.isValid()){
						//foundBundles=new ArrayList<String>(bundleNames.length+directoryBundleNames.length);
						
						for (int i =0; i < directoryBundleNames.length;i++) {
							try {
								ResourceBundle bundle=ResourceBundle.getBundle(directoryBundleNames[i],Locale.getDefault(),directoryClassLoader);
								buns.add(bundle);
								foundBundles.add("com.projity.strings."+directoryBundleNames[i]);
							}catch (Exception e) {}
						}
					}else buns=new LinkedList<ResourceBundle>();
					for (int i =bundleNames.length-1; i >=0; i--) { // reverse order since the later ones should be searched first
						String bname=bundleNames[i];
						
						//find right position to insert in bundles
						int j=0;
						int pos=0;
						for (String b : foundBundles){
							if (bname.equals(b))
								break;
							pos++;
						}
						buns.add(pos,ResourceBundle.getBundle(bname,Locale.getDefault(),ClassLoaderUtils.getLocalClassLoader()/*Messages.class.getClassLoader()*/));
						foundBundles.add(pos,bname);
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
		if (Environment.isOpenProj()) {
			String result = getStringFromBundles("Open_" + key);
			if (result == null) {
				System.out.println("getContextString not found Open_" + key);
			} else
				return result;
		}
		return getStringFromBundles(key);
	}

}
