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

Attribution Information: Attribution Copyright Notice: Copyright (c) 2006, 2007 
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
package com.projity.session;

import java.awt.Component;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;

import com.projity.strings.Messages;
import com.projity.util.Environment;

public class FileHelper {
	public static final String DEFAULT_FILE_EXTENSION ="pod";
	public static final int PROJITY_FILE_TYPE=1;
	public static final int MSP_FILE_TYPE=101;
	//public static final int SERVER_FILE_TYPE=1000;
	
    private JFileChooser fileChooser = null;
    private JFileChooser getFileChooser() {
    	if (fileChooser == null) {
    		fileChooser = new JFileChooser();
    		fileChooser.putClientProperty("FileChooser.useShellFolder", Boolean.FALSE); // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6317789
    	}
    	return fileChooser;
    }
    public synchronized String chooseFileName(final boolean save,String selectedFileName,Component fileChooserParent){
    	if (!Environment.getStandAlone()&&save&&selectedFileName!=null&&selectedFileName.endsWith("."+DEFAULT_FILE_EXTENSION)){
    		selectedFileName=changeFileExtension(selectedFileName,save?"xml":"mpp");
    	}
    	final JFileChooser fileChooser = getFileChooser();
    	fileChooser.setDialogType(save?JFileChooser.SAVE_DIALOG:JFileChooser.OPEN_DIALOG);
    	fileChooser.resetChoosableFileFilters();
    	if (selectedFileName==null){
    		try {
    			String initialDirName=Preferences.userNodeForPackage(FileHelper.class).get("lastDirectory",System.getProperty("user.home")+File.separator+"OpenProj");
				fileChooser.setCurrentDirectory(new File(initialDirName));
			} catch (Exception e) {
			}
    	}
    	else fileChooser.setSelectedFile(new File(selectedFileName));
    	
    	FileView fileView=new FileView(){
    		public Icon getIcon(File f) {
    			String extension=getFileExtension(f.getName());
    			if (extension != null) {
    					if ("pod".equals(extension)){
    						return FileHelper.getIcon("format.projity");
    					}
    					//Icon icon=fileChooser.getFileSystemView().getSystemIcon(f);
 //    					if ("mpp".equals(extension) || "mpx".equals(extension) || "planner".equals(extension)){
//    						return LocalSession.getIcon("format.other");
//    					}
    			}
    			return null;
    		}
    	};
    	fileChooser.setFileView(fileView);
    	
    	
		final FileFilter projityFilter=new FileFilter(){
		    public boolean accept(File f){
		    	return f.isDirectory()||f.getName().toLowerCase().endsWith("."+DEFAULT_FILE_EXTENSION);
		    }
		    public String getDescription(){
		    	//return "Projity";
		    	return Messages.getString("File.projity")+" (*."+DEFAULT_FILE_EXTENSION+")";
		    }
		};
		final FileFilter microsoftFilter=new FileFilter(){
		    public boolean accept(File f){
		    	boolean isAllowed;
				String n = f.getName().toLowerCase();
		    	if (save) isAllowed=false;
		    	else isAllowed=n.endsWith(".mpp") || n.endsWith(".mpx");			
		    	return f.isDirectory()||isAllowed;
		    }
		    public String getDescription(){
		    	return Messages.getString("File.microsoft")+" (*.mpp, *.mpx)";
		    }
	
		};
		final FileFilter microsoftXMLFilter=new FileFilter(){
		    public boolean accept(File f){
		    	boolean isAllowed;
				String n = f.getName().toLowerCase();
		    	if (save) isAllowed=n.endsWith(".xml");
		    	else isAllowed=n.endsWith(".xml");			
		    	return f.isDirectory()||isAllowed;
		    }
		    public String getDescription(){
		    	return Messages.getString("File.microsoftXML")+" (*.xml)";
		    }
	
		};
		final FileFilter plannerFilter=new FileFilter(){
		    public boolean accept(File f){
		    	boolean isAllowed;
				String n = f.getName().toLowerCase();
		    	if (save) isAllowed=false;
		    	else isAllowed=n.endsWith("*.planner");			
		    	return f.isDirectory()||isAllowed;
		    }
		    public String getDescription(){
		    	return Messages.getString("File.planner")+" (*.planner)";
		    }
	
		};
		FileFilter projectFilter=new FileFilter(){
		    public boolean accept(File f){
		    	if (/*Environment.getStandAlone()&&*/projityFilter.accept(f)) return true;
		    	if (microsoftXMLFilter.accept(f)) return true;
		    	if (plannerFilter.accept(f)) return true;
		    	if (microsoftFilter.accept(f)) return true;
		    	return false;
		    }
		    public String getDescription(){
		    	return Messages.getString("File.projects");
		    }
	
		};
		
		if (save){
			if (microsoftFilter.accept(fileChooser.getSelectedFile())){ //To select the good filter by default
				if (Environment.getStandAlone()) fileChooser.addChoosableFileFilter(projityFilter);
				fileChooser.addChoosableFileFilter(microsoftXMLFilter);
			}else{
				fileChooser.addChoosableFileFilter(microsoftXMLFilter);
				if (Environment.getStandAlone()) fileChooser.addChoosableFileFilter(projityFilter);
			}
		}else{
			/*if (Environment.getStandAlone())*/ fileChooser.addChoosableFileFilter(projityFilter);
			fileChooser.addChoosableFileFilter(microsoftFilter);
			fileChooser.addChoosableFileFilter(microsoftXMLFilter);
			fileChooser.addChoosableFileFilter(plannerFilter);
			fileChooser.addChoosableFileFilter(projectFilter);
		}

		if (fileChooser.showDialog(fileChooserParent, null)!=JFileChooser.APPROVE_OPTION)
			return null;
		File file=fileChooser.getSelectedFile();
		String fileName=file.toString();
		FileFilter currentFilter=fileChooser.getFileFilter();
		if (save){
			if (currentFilter==microsoftXMLFilter){
				if(!fileName.endsWith(".xml")) fileName+=".xml";
			}
			else if (!fileName.endsWith(".pod")) fileName+=".pod";
		}
		
		Preferences.userNodeForPackage(FileHelper.class).put("lastDirectory",file.getParent());
		return fileName;
    	
    }

    public static boolean isFileNameAllowed(String fileName,boolean save) {
		String n = fileName.toLowerCase();
    	if (save) return n.endsWith(".xml")||n.endsWith("."+DEFAULT_FILE_EXTENSION);
    	else return n.endsWith(".xml")||n.endsWith(".mpp")||n.endsWith(".mpx")||n.endsWith(".planner")||n.endsWith("."+DEFAULT_FILE_EXTENSION) || n.endsWith(".mpx");
	}

    public static String getFileExtension(String fileName) {
        int i=fileName.lastIndexOf('.');
        if (i>0&&i<fileName.length()-1) return fileName.substring(i+1).toLowerCase();
        return null;
    }
    public static String changeFileExtension(String fileName,int fileType) {
    	return changeFileExtension(fileName, getFileExtension(fileType));
    }
    public static String changeFileExtension(String fileName,String extension) {
    	if( fileName==null) return null;
        int i=fileName.lastIndexOf('.');
        if (i<=0) return fileName+"."+extension;
        else return fileName.substring(0,i)+"."+extension;
    }

    public static Icon getIcon(String name) {
    	try {
			return (Icon)Class.forName("com.projity.pm.graphic.IconManager").getMethod("getIcon", new Class[]{String.class}).invoke(null, new Object[]{name});
		} catch (Exception e) {
		}
		return null;
    	
    }
    
    public static String getFileExtension(int fileType){
    	switch (fileType) {
		//case FileHelper.SERVER_FILE_TYPE: return null;
		case FileHelper.PROJITY_FILE_TYPE: return DEFAULT_FILE_EXTENSION;
		case FileHelper.MSP_FILE_TYPE: return "xml";
		default:
			return DEFAULT_FILE_EXTENSION;
		}
    }

    public static int getFileType(String fileName){
    	if (fileName==null) return 0;
    	fileName=fileName.toLowerCase();
    	if (fileName.endsWith(DEFAULT_FILE_EXTENSION))
    		return PROJITY_FILE_TYPE;
    	if (fileName.endsWith("mpp")||fileName.endsWith("mpx")||fileName.endsWith("xml")||fileName.endsWith("planner"))
    			return MSP_FILE_TYPE;
    	return 0;
    }

    
}
