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

Attribution Information: Attribution Copyright Notice: Copyright © 2006, 2007 
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
package com.projity.dialog;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.pm.graphic.IconManager;
import com.projity.strings.Messages;
import com.projity.util.ClassLoaderUtils;

public final class LoginDialog extends AbstractDialog {
	private static final long serialVersionUID = 1L;

	private LoginForm form;

	// use property utils to copy to project like struts
	JTextField login;
	JPasswordField password;
	JCheckBox storeCredentials;
	JCheckBox useMenus;
	
	private JFrame standaloneFrame = null;
	
	private static String getDialogTitle() {
		return Messages.getContextString("Text.ApplicationTitle"); //$NON-NLS-1$
	}
	public static LoginForm doLogin(Frame owner,URL serverUrl) {
		
		if (owner == null) {
			final JFrame standaloneFrame = new JFrame(getDialogTitle());
			standaloneFrame.setIconImage(IconManager.getImage("application.icon")); //$NON-NLS-1$
			standaloneFrame.addWindowListener(new WindowListener() {
				public void windowOpened(WindowEvent arg0) {}
				public void windowClosing(WindowEvent arg0) {}
				public void windowClosed(WindowEvent arg0) {}
				public void windowIconified(WindowEvent arg0) {}
				public void windowDeiconified(WindowEvent arg0) {}
				public void windowDeactivated(WindowEvent arg0) {}
				public void windowActivated(WindowEvent arg0) {
					Window w[] = standaloneFrame.getOwnedWindows();
					for (int i = 0; i < w.length; i++)
						w[i].toFront();
				}
			});
			owner = standaloneFrame;
		}

		LoginForm form=null;
		if (serverUrl!=null)
		try {
			Object ps=ClassLoaderUtils.getLocalClassLoader().loadClass("javax.jnlp.ServiceManager").getMethod("lookup",new Class[]{String.class}).invoke(null,new Object[]{"javax.jnlp.PersistenceService"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//			Object ps=Class.forName("javax.jnlp.ServiceManager").getMethod("lookup",new Class[]{String.class}).invoke(null,new Object[]{"javax.jnlp.PersistenceService"});
			Object contents=ps.getClass().getMethod("get",new Class[]{URL.class}).invoke(ps,new Object[]{serverUrl}); //$NON-NLS-1$
			ObjectInputStream in=new ObjectInputStream((InputStream)ClassLoaderUtils.getLocalClassLoader().loadClass("javax.jnlp.FileContents").getMethod("getInputStream",null).invoke(contents,null)); //$NON-NLS-1$ //$NON-NLS-2$
//			ObjectInputStream in=new ObjectInputStream((InputStream)Class.forName("javax.jnlp.FileContents").getMethod("getInputStream",null).invoke(contents,null));
			form=(LoginForm)in.readObject();
			in.close();
		} catch (Exception e) {}
		
		
		LoginDialog dlg = getInstance(owner,form);
		
		// make sure dialog shows
		dlg.requestFocus();
// Because setAlwaysOnTop is not in JDK 1.4, I added treatment as per http://www.codecomments.com/archive250-2004-12-347421.html		-HK 25/2/05
//		dlg.setAlwaysOnTop(true); // this is not in JDK 1.4!!!
		
		dlg.doModal();
		
		if (serverUrl!=null)
		try {			
			Object ps=Class.forName("javax.jnlp.ServiceManager").getMethod("lookup",new Class[]{String.class}).invoke(null,new Object[]{"javax.jnlp.PersistenceService"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (dlg.form.isStoreCredentials()){
				
				if (form==null) ps.getClass().getMethod("create",new Class[]{URL.class,long.class}).invoke(ps,new Object[]{serverUrl,new Long(1000)}); //$NON-NLS-1$
				Object contents=ps.getClass().getMethod("get",new Class[]{URL.class}).invoke(ps,new Object[]{serverUrl}); //$NON-NLS-1$
				ObjectOutputStream out=new ObjectOutputStream((OutputStream)Class.forName("javax.jnlp.FileContents").getMethod("getOutputStream",new Class[]{boolean.class}).invoke(contents,new Object[]{new Boolean(true)})); //$NON-NLS-1$ //$NON-NLS-2$
				out.writeObject(dlg.form);
				out.close();
			} else if (form!=null) ps.getClass().getMethod("delete",new Class[]{URL.class}).invoke(ps,new Object[]{serverUrl}); //$NON-NLS-1$
			
		} catch (Exception e) {}
		
		
		
		return dlg.form;
	}
	
	public static LoginDialog getInstance(Frame owner,LoginForm form) {
		return new LoginDialog(owner,form);
	}

	private LoginDialog(Frame owner,LoginForm form) {
		super(owner, getDialogTitle(), true);
		this.form = (form==null)?new LoginForm():form;
		
		addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent arg0) {}
			public void windowClosing(WindowEvent arg0) {}
			public void windowClosed(WindowEvent arg0) {}
			public void windowIconified(WindowEvent arg0) {}
			public void windowDeiconified(WindowEvent arg0) {}
			public void windowDeactivated(WindowEvent arg0) {}
			public void windowActivated(WindowEvent arg0) {
				toFront();
			}
		});
	}

	// Component Creation and Initialization **********************************

	/**
	 * Creates, intializes and configures the UI components. Real applications
	 * may further bind the components to underlying models.
	 */
	protected void initControls() {
		login = new JTextField();
		password = new JPasswordField();
		storeCredentials=new JCheckBox();
		useMenus=new JCheckBox();
		bind(true);
	}

	protected boolean bind(boolean get) {
		if (get) {
			login.setText(form.getLogin());
			password.setText(form.getPassword());
			storeCredentials.setSelected(form.isStoreCredentials());
			useMenus.setSelected(form.isUseMenus());
		} else {
			if (login.getText().trim().length() == 0 || password.getPassword().length == 0) // prevent empty fields
				return false; 
			form.setLogin(login.getText());
			form.setPassword(new String(password.getPassword())); 
			form.setStoreCredentials(storeCredentials.isSelected());
			form.setUseMenus(useMenus.isSelected());
		}
		return true;
	}

	// Building *************************************************************

	/**
	 * Builds the panel. Initializes and configures components first, then
	 * creates a FormLayout, configures the layout, creates a builder, sets a
	 * border, and finally adds the components.
	 * 
	 * @return the built panel
	 */

	public JComponent createContentPanel() {
		// Separating the component initialization and configuration
		// from the layout code makes both parts easier to read.
		initControls();
		//TODO set minimum size
		FormLayout layout = new FormLayout("default, 3dlu, 120dlu:grow", // cols //$NON-NLS-1$
				"p, 3dlu,p,3dlu,p,3dlu,p"); // rows //$NON-NLS-1$

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		builder.append(Messages.getString("LoginDialog.Login"), login); //$NON-NLS-1$
		builder.nextLine(2);
		builder.append(Messages.getString("LoginDialog.Password"), password); //$NON-NLS-1$
		builder.nextLine(2);
//		builder.append(useMenus);
//		builder.append(Messages.getString("LoginDialog.UseOfficeLook")); //$NON-NLS-1$
		builder.nextLine(2);
		builder.append(storeCredentials);
		builder.append(Messages.getString("LoginDialog.RememberMe")); //$NON-NLS-1$
		
		return builder.getPanel();
	}
	/**
	 * @return Returns the form.
	 */
	public LoginForm getForm() {
		return form;
	}
	protected void onCancel() {
		getForm().setCancelled(true);
		super.onCancel();
	}
	
	

}
