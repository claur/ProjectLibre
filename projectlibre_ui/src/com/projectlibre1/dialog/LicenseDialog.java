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
package com.projectlibre1.dialog;

import java.awt.Dimension;
import java.awt.Frame;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.util.Alert;
import com.projectlibre1.util.BrowserControl;
import com.projectlibre1.util.Environment;

public final class LicenseDialog extends AbstractDialog {
	private static final long serialVersionUID = 1L;
	JEditorPane license = null;
	JEditorPane thirdParty = null;
	private boolean init = false;
	private static boolean validated = Preferences.userNodeForPackage(LicenseDialog.class).getBoolean("validatedLicense",false); //$NON-NLS-1$
	private static boolean resetData;
	public static boolean showDialog(Frame owner, boolean force) {
		resetData=!force;
		if (!Environment.isProjectLibre() && !force)
			return false;
		if (!validated || force) {
			LicenseDialog dlg = new LicenseDialog(owner);
			if (!validated)
				dlg.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // force user to click a button
			dlg.doModal();
			return true;
		}
		if (Environment.isProjectLibre())
			System.setProperty("projectlibre.validation", Preferences.userNodeForPackage(LicenseDialog.class).get("licenseValidationDate","0"));
		return false;
	}

	private LicenseDialog(Frame owner) {
		super(owner, Messages.getContextString("Text.ApplicationTitle") + " "+Messages.getString("LicenseDialog.License"), true); //$NON-NLS-1$ //$NON-NLS-2$
		if (!Environment.isProjectLibre())
			validated = true; // POD validation is on web
	}


	private JEditorPane createEditorPane(URL url,final int height) {
		JEditorPane pane = null;
		try {
			pane = new JEditorPane(url) {
			       public Dimension getPreferredSize() {
			    	   return new Dimension(600,height); //TODO there are issues with the size, so i just make it correct here
			          }
			};
		} catch (Exception e) {
			if (!validated) {
				Alert.error(Messages.getString("LicenseDialog.CouldNotLoadExiting")); //$NON-NLS-1$
				System.exit(-1);
			} else {
				Alert.error(Messages.getString("LicenseDialog.CouldNotLoadLater")); //$NON-NLS-1$
				return null;

			}
		}

		pane.setEditable(false);
		pane.setAutoscrolls(true);
		pane.setBounds(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
		pane.addHyperlinkListener(new HyperlinkListener(){
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType()== HyperlinkEvent.EventType.ACTIVATED)
					BrowserControl.displayURL(e.getURL().toExternalForm());
			}});
		return pane;
	}
	protected void initComponents() {
		if (init)
			return;
		init = true;
		if (Environment.isProjectLibre()) {
			license = createEditorPane(getClass().getClassLoader().getResource("license/index.html"),7500); //$NON-NLS-1$
			thirdParty = createEditorPane(getClass().getClassLoader().getResource("license/third-party/index.html"),1200); //$NON-NLS-1$
		} else {
			try {
				thirdParty = createEditorPane(new URL("http://projectlibre.com/license/third-party/index.html"),1200); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} //$NON-NLS-1$
			
		}
		
		
		super.initComponents();
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
		FormLayout layout = new FormLayout("700px", // cols //$NON-NLS-1$
				"600px"); // rows //$NON-NLS-1$

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		if (!Environment.isProjectLibre()) {
			builder.append(new JScrollPane(thirdParty));
		} else {
			JTabbedPane tabbed= new JTabbedPane();
			tabbed.addTab(Messages.getString("LicenseDialog.License"),new JScrollPane(license));
			tabbed.addTab(Messages.getString("LicenseDialog.ThirdParty"),new JScrollPane(thirdParty));
			builder.append(tabbed);
		}

		JComponent result =  builder.getPanel();
		return result;
	}


	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel bp = super.createButtonPanel();
		if (!validated)
			ok.setText(Messages.getString("ButtonText.IAccept")); //$NON-NLS-1$
		return bp;
	}

	@Override
	protected boolean hasCloseButton() {
		return validated;
	}

	@Override
	protected void onCancel() {
		if (!validated)
			System.exit(-1);
		super.onCancel();
	}

	@Override
	public void onOk() {
		validated = true;
		if (resetData){//About dialog shouldn't reset data
			Preferences.userNodeForPackage(LicenseDialog.class).put("licenseValidationDate",System.currentTimeMillis()+"."+(new Random()).nextInt(1000)); //$NON-NLS-1$
			Preferences.userNodeForPackage(LicenseDialog.class).putBoolean("validatedLicense",validated); //$NON-NLS-1$
			System.setProperty("projectlibre.validation", Preferences.userNodeForPackage(LicenseDialog.class).get("licenseValidationDate","0"));
		}
		super.onOk();
	}

}
