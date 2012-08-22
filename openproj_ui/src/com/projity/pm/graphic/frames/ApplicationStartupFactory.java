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
package com.projity.pm.graphic.frames;

import java.awt.Container;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;

import com.projity.configuration.Settings;
import com.projity.util.Environment;
import com.projity.util.FontUtil;

public class ApplicationStartupFactory extends StartupFactory {

	public ApplicationStartupFactory(String args[]){
		this(ApplicationStartupFactory.extractOpts(args));
	}
	public ApplicationStartupFactory(HashMap opts) {
		try{
			Class.forName("java.net.CookieHandler").getMethod("setDefault",new Class[]{CookieHandler.class}).invoke(null,new Object[]{null});
		}catch(Exception e){}

		this.opts=opts;
		dumpOpts();

		serverUrl=getOpt("serverUrl");
		if (serverUrl==null)
			serverUrl=defaultServerUrl;

		String projectIdS=getOpt("projectId");
		if (projectIdS!=null) projectId=Long.parseLong(projectIdS);

		String font=(String)getOpt("font");
		if (font==null){
			String javaVendor=System.getProperty("java.vendor");
			if (javaVendor.startsWith("IBM")){ //to avoid font bug on SLED with IBM jvm
				font=FontUtil.getValidFont(new String[]{"DejaVu Sans","Andale Sans"}); //Lucida Sans
			}
		}else{
			font=font.replace('_', ' ');
		}
		//FontUtil.listFonts();
		if (font!=null){
			Environment.resetFonts();
			Environment.setFont(font,Environment.DEFAULT_FONT);
			FontUtil.setUIFont(font);
		}

		Object o=opts.get("fileNames");
		List fileNames;
		if (o==null) fileNames=null;
		else if (o instanceof List){
			fileNames=(List)o;
		}else{
			fileNames=new ArrayList(1);
			fileNames.add(o);
		}

		if (fileNames!=null) projectUrls=(String[])fileNames.toArray(new String[]{});


		if (Settings.VERSION_TYPE_STANDALONE.equals(getOpt("versionType"))) Environment.setStandAlone(true);

	}

	protected void abort() {
		System.exit(-1);
	}

	protected void getCredentials() {
		String authType=getOpt("credentials",0);
		if (authType!=null){
			if ("login".equals(authType)){
				login=getOpt("credentials",1);
				password=getOpt("credentials",2);
			} else if ("session".equals(authType)){
				String partnerConnectionString =getOpt("credentials",2);
				String timestamp=getOpt("timestamp");
				long d=0L;
				if (timestamp!=null){
					try {
						d=System.currentTimeMillis()-Long.parseLong(timestamp);
					} catch (NumberFormatException e) {}
				}
				String sessionId=getOpt("credentials",1);
				//if (sessionId!=null&&d<=SESSION_EXPIRATION)
				if (sessionId!=null||partnerConnectionString!=null)
				try{
					Properties props=new Properties();
					String urlString = serverUrl + "/" + Settings.WEB_APP + ((partnerConnectionString==null)?"":"/partner")+"/jnlp/projity_credentials.jnlp";
					if (partnerConnectionString != null)
						urlString += "?"+ partnerConnectionString;
					URL url = new URL(urlString);
					HttpURLConnection http = (HttpURLConnection) url.openConnection();
					if (sessionId!=null) http.setRequestProperty("Cookie", "JSESSIONID=" + sessionId);
	//				if (partnerConnectionString == null) {
	//					http.setRequestMethod("POST");
	//				} else {
						http.setRequestMethod("GET");
	//				}
					http.connect();


					props.load(http.getInputStream());
					http.disconnect();

					//props.load((new URL(serverUrl+"/web/jnlp/projity_credentials.jnlp;jsessionid="+args[3])).openStream());
					login=props.getProperty("login");
					password=props.getProperty("password");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private String getOpt(String name){
		return getOpt(name,0);
	}
	private String getOpt(String name,int index){
		if (index<0) return null;
		Object o=opts.get(name);
		if (o==null) return null;
		else if (o instanceof String) return (index==0)?((String)o):null;
		else if (o instanceof List){
			List lopt=(List)o;
			if (index>=lopt.size()) return null;
			return (String)lopt.get(index);
		}
		else return null;
	}

//	private void computeOpts(String args[]){
//		opts = extractOpts(args);
//	}
	public static HashMap extractOpts(String args[]){
		HashMap opts=new HashMap();
		if (args.length==0) return opts;
		String arg=args[0];
		if (arg!=null&&arg.length()>1&&(!arg.startsWith("--"))){
			//assume old format
			if (args.length<4) return opts;
			opts.put("serverUrl",args[0]);
			if ("login".equals(args[1])){
				List lopt=new LinkedList();
				lopt.add(args[1]);
				lopt.add(args[2]);
				lopt.add(args[3]);
				opts.put("credentials",lopt);
			}
		}else{
			String opt=null,label=null;
			List lopt=null;
			for (int i=0;i<args.length;i++){
				arg=args[i];
				if (arg.length()>2&&arg.startsWith("--")){
					if (label!=null){
						if (lopt!=null) opts.put(label,lopt);
						else if (opt!=null) opts.put(label,opt);
					}
					label=arg.substring(2);
					opt=null;
					lopt=null;
				}else{
					if (lopt!=null) lopt.add(arg);
					else if (opt!=null){
						lopt=new LinkedList();
						lopt.add(opt);
						lopt.add(arg);
						opt=null;
					}else opt=arg;
				}
			}
			if (label!=null){
				if (lopt!=null) opts.put(label,lopt);
				else if (opt!=null) opts.put(label,opt);
			}
		}
		return opts;
	}
	public void dumpOpts() {
		System.out.println("opts:");
		for (Iterator i=opts.keySet().iterator();i.hasNext();){
			String opt=(String)i.next();
			System.out.println(opt+":");
			String arg;
			int index=0;
			while ((arg=getOpt(opt,index++))!=null) System.out.println("\t"+arg);
		}
	}
	public void doPostInitView(Container container) {
		if (!Environment.isPlugin()) ((JFrame)container).pack();
	}


}
