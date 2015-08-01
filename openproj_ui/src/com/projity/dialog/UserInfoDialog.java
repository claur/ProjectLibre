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
package com.projity.dialog;

import java.awt.Font;
import java.awt.Frame;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.strings.Messages;
import com.projity.util.Environment;

public final class UserInfoDialog extends AbstractDialog {
	private static final long serialVersionUID = 1L;
	JTextField email = null;
	private boolean init = false;
	private static boolean validated = (Preferences.userNodeForPackage(UserInfoDialog.class).get("userEmail",null)!=null); //$NON-NLS-1$
	public static boolean showDialog(Frame owner, boolean force) {
		if (!Environment.isOpenProj())
			return false;
		System.setProperty("projectlibre.userEmail", Preferences.userNodeForPackage(UserInfoDialog.class).get("userEmail",""));
		if (!validated || force) {
			UserInfoDialog dlg = new UserInfoDialog(owner);
//			if (!validated)
//				dlg.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // force user to click a button
			dlg.doModal();
			return true;
		}
		return false;
	}

	private UserInfoDialog(Frame owner) {
		super(owner, Messages.getContextString("Text.ApplicationTitle") + " " + Messages.getString("LicenseDialog.CustomerInformation"), true); //$NON-NLS-1$ //$NON-NLS-2$
	}


	protected void initComponents() {
		if (init)
			return;
		init = true;
		email=new JTextField(30);
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
		FormLayout layout = new FormLayout("p,3dlu,p,3dlu", // cols //$NON-NLS-1$
				"p,10dlu,p,10dlu"); // rows //$NON-NLS-1$

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		CellConstraints cc = new CellConstraints();
		JEditorPane l=new JEditorPane("text/html",Messages.getString("LicenseDialog.Email"));
		l.setEditable(false);
		l.setOpaque(false);
		l.setFont(l.getFont().deriveFont(Font.PLAIN));
		JLabel emailLabel=new JLabel(Messages.getString("LicenseDialog.EmailLabel")+":");
		//emailLabel.setFont(emailLabel.getFont().deriveFont(Font.PLAIN));
		builder.add(l,cc.xyw(1,1, 4));
		builder.nextLine(2);
		builder.append(emailLabel,email);
		builder.nextLine(2);

		JComponent result =  builder.getPanel();
		return result;
	}



	@Override
	protected void onCancel() {
		setEmail("");
		super.onCancel();
	}

	@Override
	public void onOk() {
		setEmail(email.getText());
		super.onOk();
	}

	private void setEmail(String email){
		validated = true;
		Preferences.userNodeForPackage(UserInfoDialog.class).put("userEmail",email); //$NON-NLS-1$
		System.setProperty("projectlibre.userEmail", Preferences.userNodeForPackage(UserInfoDialog.class).get("userEmail",""));

	}

}
