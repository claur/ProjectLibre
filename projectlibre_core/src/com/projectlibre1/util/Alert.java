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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.projectlibre1.strings.Messages;

/**
 *
 */
public class Alert {
	public static void warn(Object errorObject) {
		if (allowPopups())
			warn(errorObject,getFrame());
	}
	public static void warn(Object errorObject, Component parent) {
		System.out.println("warning message " + errorObject);

		if (allowPopups())
			JOptionPane.showMessageDialog(parent,errorObject, Messages.getContextString("Title.ProjectLibreWarning"),JOptionPane.WARNING_MESSAGE);
	}

	public static void error(Object errorObject) {
		if (allowPopups())
			error(errorObject,getFrame());
	}
	public static void error(Object errorObject, Component parent) {
		System.out.println("error message " + errorObject);

		if (allowPopups())
			JOptionPane.showMessageDialog(parent,errorObject, Messages.getContextString("Title.ProjectLibreError"),JOptionPane.ERROR_MESSAGE);
	}
	public static int confirmYesNo(Object messageObject) {
		if (!allowPopups())
			return JOptionPane.NO_OPTION;
		return JOptionPane.showConfirmDialog(getFrame(),
		        messageObject,
		        Messages.getContextString("Text.ApplicationTitle"),
	            JOptionPane.YES_NO_OPTION);
	}
	public static int confirm(Object messageObject) {
		if (!allowPopups())
			return JOptionPane.NO_OPTION;
		int result = JOptionPane.showConfirmDialog(getFrame(),
		        messageObject,
		        Messages.getContextString("Text.ApplicationTitle"),
	            JOptionPane.YES_NO_CANCEL_OPTION);
		if (result == JOptionPane.CLOSED_OPTION)
			result = JOptionPane.CANCEL_OPTION;
		return result;
	}
	public static boolean okCancel(Object messageObject) {
		if (!allowPopups())
			return true;

		return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(getFrame(),
		        messageObject,
		        Messages.getContextString("Text.ApplicationTitle"),
	            JOptionPane.OK_CANCEL_OPTION);
	}

	public static String renameProject(final String name,Set projectNames,boolean saveAs){
		try {
			return (String)Class.forName(GRAPHIC_MANAGER).getMethod("doRenameProjectDialog",new Class[]{String.class,Set.class,boolean.class}).invoke(getGraphicManager(),new Object[]{name,projectNames,saveAs});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	private static final String GRAPHIC_MANAGER="com.projectlibre1.pm.graphic.frames.GraphicManager";
	public static Frame getFrame(){
		try {
		    return (Frame)Class.forName(GRAPHIC_MANAGER).getMethod("getFrameInstance",null).invoke(null,null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static Object getGraphicManager(){
		try {
		    return Class.forName(GRAPHIC_MANAGER).getMethod("getInstance",null).invoke(null,null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static boolean allowPopups() {
		return Environment.isClientSide() && !Environment.isBatchMode();
	}
	public static Object getGraphicManagerMethod(String method) {
		try {
			return Class.forName(GRAPHIC_MANAGER).getMethod(method,null).invoke(null,null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static void setGraphicManagerMethod(String method,Object value) {
		try {
			Class.forName(GRAPHIC_MANAGER).getMethod(method,new Class[] {Object.class}).invoke(null,new Object[] {value});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void warnWithOnceOption(Object object,String preference) {
		warnWithOnceOption(object,preference,null);
	}
	public static void warnWithOnceOption(Object object,String preference,Component parentComponent) {
		boolean warned =  Preferences.userNodeForPackage(Alert.class).getBoolean(preference,false);
		if (warned)
			return;
		JOptionPane pane = new JOptionPane(object);
		String title=Messages.getContextString("Text.ApplicationTitle");
		JDialog dialog = pane.createDialog(parentComponent,title);
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		JCheckBox notAgain = new JCheckBox(Messages.getString("Text.doNotShowAgain"));
		p.add(notAgain);
		pane.add(p);
		Dimension d=dialog.getSize();
		d.height+=40; // for extra height of checkbox
		dialog.setSize(d);
		dialog.setVisible(true);
		if (notAgain.isSelected())
			Preferences.userNodeForPackage(Alert.class).putBoolean(preference,true);


	}

}
