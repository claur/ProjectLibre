package com.projity.preference;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;

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
			else{
				String language=null;
				String country=null;
				String variant=null;
				StringTokenizer st=new StringTokenizer(l,"_-");
				if (!st.hasMoreTokens()) locale=Locale.getDefault();
				else{
					language=st.nextToken();
					if (!st.hasMoreTokens()) locale=new Locale(language);
					else{
						country=st.nextToken();
						if (!st.hasMoreTokens()) locale=new Locale(language,country);
						else{
							variant=st.nextToken();
							locale=new Locale(language,country,variant);
						}
						
					}
					
				}
				
			}
		}
		return locale;
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


}
