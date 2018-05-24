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
package com.projity.dialog;

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
import com.projity.strings.Messages;
import com.projity.util.Alert;
import com.projity.util.BrowserControl;
import com.projity.util.Environment;

public final class LicenseDialog extends AbstractDialog {
	private static final long serialVersionUID = 1L;
	JEditorPane license = null;
	JEditorPane thirdParty = null;
	private boolean init = false;
	private static boolean validated = Preferences.userNodeForPackage(LicenseDialog.class).getBoolean("validatedLicense",false); //$NON-NLS-1$
	private static boolean resetData;
	public static boolean showDialog(Frame owner, boolean force) {
		resetData=!force;
		if (!Environment.isOpenProj() && !force)
			return false;
		if (!validated || force) {
			LicenseDialog dlg = new LicenseDialog(owner);
			if (!validated)
				dlg.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // force user to click a button
			dlg.doModal();
			return true;
		}
		if (Environment.isOpenProj())
			System.setProperty("projectlibre.validation", Preferences.userNodeForPackage(LicenseDialog.class).get("licenseValidationDate","0"));
		return false;
	}

	private LicenseDialog(Frame owner) {
		super(owner, Messages.getContextString("Text.ApplicationTitle") + " "+Messages.getString("LicenseDialog.License"), true); //$NON-NLS-1$ //$NON-NLS-2$
		if (!Environment.isOpenProj())
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
		if (Environment.isOpenProj()) {
			license = createEditorPane(getClass().getClassLoader().getResource("license/index.html"),7500); //$NON-NLS-1$
			thirdParty = createEditorPane(getClass().getClassLoader().getResource("license/third-party/index.html"),1200); //$NON-NLS-1$
		} else {
			try {
				thirdParty = createEditorPane(new URL("http://projity.com/license/third-party/index.html"),1200); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} //$NON-NLS-1$
			
		}
		
		
		//		license = createEditorPane("http://www.projity.com/license/index.html",7100); //$NON-NLS-1$
//		thirdParty = createEditorPane("http://www.projity.com/license/third-party/index.html",800); //$NON-NLS-1$
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
		if (!Environment.isOpenProj()) {
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
