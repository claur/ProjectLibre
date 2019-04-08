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

import java.awt.Component;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.swing.UIManager;

import com.projectlibre1.strings.Messages;
import com.projectlibre1.util.Environment;

public class FileHelper {
	public static final String DEFAULT_FILE_EXTENSION ="pod";
	public static final int PROJECTLIBRE_FILE_TYPE=1;
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
	JFileChooser fileChooser = getFileChooser();
	fileChooser=setUpdateUI(fileChooser);
    	fileChooser.setDialogType(save?JFileChooser.SAVE_DIALOG:JFileChooser.OPEN_DIALOG);
    	fileChooser.resetChoosableFileFilters();
    	if (selectedFileName==null){
    		try {
    			String initialDirName=Preferences.userNodeForPackage(FileHelper.class).get("lastDirectory",System.getProperty("user.home")+File.separator+"ProjectLibre");
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
    						return FileHelper.getIcon("format.projectlibre");
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
    	
    	
		final FileFilter projectlibreFilter=new FileFilter(){
		    public boolean accept(File f){
		    	return f.isDirectory()||f.getName().toLowerCase().endsWith("."+DEFAULT_FILE_EXTENSION);
		    }
		    public String getDescription(){
		    	//return "projectlibre";
		    	return Messages.getString("File.projectlibre")+" (*."+DEFAULT_FILE_EXTENSION+")";
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
		    	if (/*Environment.getStandAlone()&&*/projectlibreFilter.accept(f)) return true;
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
				if (Environment.getStandAlone()) fileChooser.addChoosableFileFilter(projectlibreFilter);
				fileChooser.addChoosableFileFilter(microsoftXMLFilter);
			}else{
				fileChooser.addChoosableFileFilter(microsoftXMLFilter);
				if (Environment.getStandAlone()) fileChooser.addChoosableFileFilter(projectlibreFilter);
			}
		}else{
			/*if (Environment.getStandAlone())*/ fileChooser.addChoosableFileFilter(projectlibreFilter);
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
			return (Icon)Class.forName("com.projectlibre1.pm.graphic.IconManager").getMethod("getIcon", new Class[]{String.class}).invoke(null, new Object[]{name});
		} catch (Exception e) {
		}
		return null;
    	
    }
    
    public static String getFileExtension(int fileType){
    	switch (fileType) {
		//case FileHelper.SERVER_FILE_TYPE: return null;
		case FileHelper.PROJECTLIBRE_FILE_TYPE: return DEFAULT_FILE_EXTENSION;
		case FileHelper.MSP_FILE_TYPE: return "xml";
		default:
			return DEFAULT_FILE_EXTENSION;
		}
    }

    public static int getFileType(String fileName){
    	if (fileName==null) return 0;
    	fileName=fileName.toLowerCase();
    	if (fileName.endsWith(DEFAULT_FILE_EXTENSION))
    		return PROJECTLIBRE_FILE_TYPE;
    	if (fileName.endsWith("mpp")||fileName.endsWith("mpx")||fileName.endsWith("xml")||fileName.endsWith("planner"))
    			return MSP_FILE_TYPE;
    	return 0;
    }

   public JFileChooser setUpdateUI(JFileChooser choose) {
       UIManager.put("FileChooser.openButtonText", Messages.getString("T_OPEN_TXT"));
       UIManager.put("FileChooser.cancelButtonText", Messages.getString("T_CANCEL"));
       UIManager.put("FileChooser.lookInLabelText", Messages.getString("T_LOOK_IN"));
       UIManager.put("FileChooser.fileNameLabelText", Messages.getString("T_FILE_NAME"));
       UIManager.put("FileChooser.filesOfTypeLabelText", Messages.getString("T_FILES_OF_TYPE"));

       UIManager.put("FileChooser.saveButtonText", Messages.getString("T_SAVE"));
       UIManager.put("FileChooser.saveButtonToolTipText", Messages.getString("T_SAVE"));
       UIManager.put("FileChooser.openButtonText", Messages.getString("T_OPEN_TXT"));
       UIManager.put("FileChooser.openButtonToolTipText", Messages.getString("T_OPEN_TXT"));
       UIManager.put("FileChooser.cancelButtonText", Messages.getString("T_CANCEL"));
       UIManager.put("FileChooser.cancelButtonToolTipText", Messages.getString("T_CANCEL"));

       UIManager.put("FileChooser.lookInLabelText", Messages.getString("T_LOOK_IN"));
       UIManager.put("FileChooser.saveInLabelText", Messages.getString("T_SAVE_IN"));
       UIManager.put("FileChooser.fileNameLabelText", Messages.getString("T_FILE_NAME"));
       UIManager.put("FileChooser.filesOfTypeLabelText", Messages.getString("T_FILES_OF_TYPE"));

       UIManager.put("FileChooser.upFolderToolTipText", Messages.getString("T_UP_FOLDER"));
       UIManager.put("FileChooser.homeFolderToolTipText", Messages.getString("T_HOME"));
       UIManager.put("FileChooser.newFolderToolTipText", Messages.getString("T_NEW_FOLDER"));
       UIManager.put("FileChooser.listViewButtonToolTipText", Messages.getString("T_LIST_VIEW"));
       UIManager.put("FileChooser.detailsViewButtonToolTipText", Messages.getString("T_DETAILS_VIEW"));
       UIManager.put("FileChooser.fileNameHeaderText", Messages.getString("T_NAME"));
       UIManager.put("FileChooser.fileSizeHeaderText", Messages.getString("T_FILE_SIZE"));
       UIManager.put("FileChooser.fileTypeHeaderText", Messages.getString("T_FILE_TYPE"));
       UIManager.put("FileChooser.fileDateHeaderText", Messages.getString("T_FILE_DATE"));
       UIManager.put("FileChooser.fileAttrHeaderText", Messages.getString("T_FILE_ATTR"));

       UIManager.put("FileChooser.acceptAllFileFilterText", Messages.getString("T_ALL_FILES"));

       UIManager.put("FileChooser.openDialogTitleText", Messages.getString("T_OPEN_TXT"));
       UIManager.put("FileChooser.saveDialogTitleText", Messages.getString("T_SAVE"));

       UIManager.put("FileChooser.refreshActionLabelText", Messages.getString("T_REFRESH"));
       UIManager.put("FileChooser.viewMenuLabelText", Messages.getString("T_VIEW"));
       UIManager.put("FileChooser.listViewActionLabelText", Messages.getString("T_LIST_VIEW"));
       UIManager.put("FileChooser.detailsViewActionLabelText", Messages.getString("T_DETAILS_VIEW"));
       UIManager.put("FileChooser.newFolderActionLabelText", Messages.getString("T_NEW_FOLDER"));

       UIManager.put("FileChooser.directoryOpenButtonText", Messages.getString("T_OPEN_TXT"));
       UIManager.put("FileChooser.directoryOpenButtonToolTipText", Messages.getString("T_OPEN_TXT"));

       choose.updateUI();
       return choose;
   }
    
}
