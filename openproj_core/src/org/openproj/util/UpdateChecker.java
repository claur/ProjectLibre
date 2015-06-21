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

Attribution Information: Attribution Copyright Notice: Copyright  2006, 2007
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
package org.openproj.util;

import groovy.lang.GroovyClassLoader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

import com.projity.strings.Messages;
import com.projity.util.Alert;
import com.projity.util.BrowserControl;
import com.projity.util.VersionUtils;

/*
 * Format:
 * version: series of integers separated by dots
 * name: string
 *
 * %UpdateChecker
 * <groovy formula>
 */
public class UpdateChecker {
	private static final int UPDATE_CHECKER_VERSION=1;
	private static final String updateAddress = "http://projectlibre.org/versions-"+UPDATE_CHECKER_VERSION; //claur openproj.org version not working any more
	private static final String downloadAddress = "http://sourceforge.net/projects/projectlibre/files/latest/download";
	private static void checkForUpdate() {
		if (! Preferences.userNodeForPackage(UpdateChecker.class).getBoolean("checkForUpdates", true) ) {
			return;
		}
		String thisVersion = VersionUtils.getVersion();//Messages.getString("Release.version");
		if (thisVersion==null) thisVersion="0";
		URL url;
		try {
//			System.out.println("Encoded: "+URLEncoder.encode(System.getProperty("java.vendor"),"UTF-8"));
//			System.exit(1);
			//identify installed version, locale, jvm , to know if an update is available
			url = new URL(updateAddress+
					"?version="+URLEncoder.encode(thisVersion==null?"0":thisVersion,"UTF-8")+
					"&locale="+URLEncoder.encode(Locale.getDefault().toString(),"UTF-8")+
					"&timeZone="+URLEncoder.encode(TimeZone.getDefault().getID().toString(),"UTF-8")+
					"&osName="+URLEncoder.encode(System.getProperty("os.name"),"UTF-8")+
					"&osVersion="+URLEncoder.encode(System.getProperty("os.version"),"UTF-8")+
					"&osArch="+URLEncoder.encode(System.getProperty("os.arch"),"UTF-8")+
					"&javaVersion="+URLEncoder.encode(System.getProperty("java.version"),"UTF-8")+
					"&javaVendor="+URLEncoder.encode(System.getProperty("java.vendor"),"UTF-8")+
					"&validation="+URLEncoder.encode(System.getProperty("projectlibre.validation","0"),"UTF-8")+
					"&runNumber="+URLEncoder.encode(System.getProperty("projectlibre.runNumber","0"),"UTF-8")+
					"&firstRun="+URLEncoder.encode(System.getProperty("projectlibre.firstRun","0"),"UTF-8")+
					"&openprojRunNumber="+URLEncoder.encode(System.getProperty("projectlibre.openprojRunNumber","0"),"UTF-8")+
					"&openprojFirstRun="+URLEncoder.encode(System.getProperty("projectlibre.openprojFirstRun","0"),"UTF-8")+
					"&email="+URLEncoder.encode(System.getProperty("projectlibre.userEmail","0"),"UTF-8")
					); //$NON-NLS-1$
			InputStream stream=  url.openStream();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
					stream));
			String latestVersion = in.readLine();
			if (thisVersion==null){
				in.close();
				return;
			}
			UpdateCheckerFormula f=new UpdateCheckerFormula();
			if (f.mainCompare(thisVersion, latestVersion)>=0){
				in.close();
				return;
			}
			if (f.mainCompare(Preferences.userNodeForPackage(UpdateChecker.class).get("lastVersionChecked","-1"),latestVersion)>=0){
				int runNumber = Preferences.userNodeForPackage(Class.forName("com.projity.main.Main")).getInt("projectlibreRunNumber",0);
				int showEvery = Integer.parseInt(Messages.getString("UpdateDialog.showEvery"));
				int showEveryStagger = Integer.parseInt(Messages.getString("UpdateDialog.showEveryStagger"));
				if ((runNumber-showEveryStagger)%showEvery != 0) {
					in.close();
					return; //already asked
				}
			}

			String latestName = in.readLine();

			StringBuffer formulaDef=new StringBuffer();
			String s=null;
			while ((s=in.readLine())!=null ){
				if(s.trim().toUpperCase().equals("%UPDATECHECKER")) break;
			}
			if (s!=null){
				while ((s=in.readLine())!=null){
					formulaDef.append(s).append('\n');
				}
			}
			in.close();

			UpdateCheckerFormula formula=getFormula(formulaDef.toString().trim());
			if (formula.mainCompare(thisVersion, latestVersion) < 0){
				final String message = MessageFormat.format(Messages.getString("Text.newVersion"), new Object[] {latestVersion,thisVersion});
				Preferences.userNodeForPackage(UpdateChecker.class).put("lastVersionChecked",latestVersion);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (Alert.okCancel(message))
							BrowserControl.displayURL(downloadAddress);
					}});
			}

		} catch (Exception e) {
			//e.printStackTrace();
		}
	}



	private static UpdateCheckerFormula getFormula(String formulaDef){
	    if (formulaDef.length()==0) return new UpdateCheckerFormula();
	    StringBuffer classText=new StringBuffer();
	    classText.append("package org.openproj.util;\n");
	    classText.append("public class UpdateCheckerFormulaImpl extends UpdateCheckerFormula{\n");
	    classText.append("\tpublic int mainCompare(String currentVersion,String latestVersion){\n");
	    classText.append("\t\t").append(formulaDef).append('\n');
	    classText.append("\t}\n");
	    classText.append("}\n");
	    GroovyClassLoader loader = new GroovyClassLoader(UpdateChecker.class.getClassLoader());
		try {
			Class groovyClass = loader.parseClass(classText.toString()); //TODO this his horribly slow (~500ms)  Can we parse all at once or can we do this lazily or initialize in another thread?
			return (UpdateCheckerFormula)groovyClass.newInstance();
		} catch (Exception e) {
			return new UpdateCheckerFormula();
		}
	}


//	double start=1.20E12;
//	double end=1.21E12;
//	double maxdays=5.0;
//	long ref=1208993169371L;
//	double day=86400000.0;
//	double dcount=((double)(System.currentTimeMillis()-ref))/day+1;
//	double t=end-dcount/maxdays*(end-start);
//	double v=0.0;
//	try{
//		v=Double.parseDouble(System.getProperty("openproj.validation","0.0"));
//	}catch (Exception e) {}
//	if (v<start) v=start;
//	double r=t-v;
//	System.out.println("New version: "+r/day);
//	System.out.println(r<=0?-1:1);




	public static void checkForUpdateInBackground() {
		new Thread(new Runnable() {
			public void run() {
				checkForUpdate();
			}}).start();
	}

}
