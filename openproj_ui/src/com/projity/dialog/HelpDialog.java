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

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.configuration.Settings;
import com.projity.pm.graphic.IconManager;
import com.projity.pm.graphic.frames.GraphicManager;
import com.projity.strings.Messages;
import com.projity.util.BrowserControl;
import com.projity.util.Environment;
import com.projity.util.VersionUtils;

public final class HelpDialog extends AbstractDialog {
	private static final long serialVersionUID = 1L;
//	private static final String helpUrl = "http://www.projectlibre.com/doc.html"; //$NON-NLS-1$
	private static final String helpUrl = "http://www.projectlibre.com/projectlibre-documentation"; //$NON-NLS-1$
//	private static final String videosUrl = Settings.SITE_HOME + "/demos.html"; //$NON-NLS-1$
	private static final String videosUrl = Settings.SITE_HOME; //$NON-NLS-1$
	public static final String donateUrl = "http://www.projectlibre.com";
//	public static final String donateUrl = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=paypal%40projity%2ecom&item_name=OpenProj%20Donation&no_shipping=0&no_note=1&tax=0&currency_code=USD&lc=US&bn=PP%2dDonationsBF&charset=UTF%2d8";
	JButton link;
    JButton videos;
    JButton tipOfTheDay;
    JButton license;
	private JPanel donatePanel;
	public static HelpDialog getInstance(Frame owner) {
		return new HelpDialog(owner);
	}

	private HelpDialog(Frame owner) {
		super(owner, Messages.getString("HelpDialog.About") + " " +Messages.getContextString("Text.ApplicationTitle"), true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected void initComponents() {
		link = new JButton(Messages.getString("HelpDialog.GoToOnlineHelp")); //$NON-NLS-1$
		link.setEnabled(true);
		link.setToolTipText(helpUrl);
		link.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				BrowserControl.displayURL(helpUrl);
			}
		});		
//		videos = new JButton(Messages.getString("HelpDialog.WatchHowToVideos")); //$NON-NLS-1$
//		videos.setEnabled(true);
//		videos.setToolTipText(helpUrl);
//		videos.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent arg0) {
//				BrowserControl.displayURL(videosUrl);
//			}
//		});		
		if (Environment.isOpenProj()) {
			tipOfTheDay = new JButton(Messages.getString("HelpDialog.ShowTipsOfTheDay")); //$NON-NLS-1$
			tipOfTheDay.setEnabled(true);
			tipOfTheDay.setToolTipText(Messages.getString("HelpDialog.ShowTipsOfTheDay")); //$NON-NLS-1$
			tipOfTheDay.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					TipOfTheDay.showDialog(HelpDialog.this.getOwner(),true);
				}
			});		
		}
		license = new JButton(Messages.getString("HelpDialog.ShowLicense")); //$NON-NLS-1$
		license.setEnabled(true);
		license.setToolTipText(Messages.getString("HelpDialog.ShowLicense")); //$NON-NLS-1$
		license.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				LicenseDialog.showDialog(GraphicManager.getFrameInstance(),true);
			}
		});		


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
		// Separating the component initialization and configuration
		// from the layout code makes both parts easier to read.
		//TODO set minimum size
		FormLayout layout = new FormLayout("120px,180px,120px" , // cols //$NON-NLS-1$
			
				"p, 6dlu,  p,6dlu,p,6dlu,p,6dlu,p,6dlu,p"); // rows //$NON-NLS-1$

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		JLabel logo = new JLabel(IconManager.getIcon("logo.ProjectLibre"));
		logo.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				BrowserControl.displayURL("http://www.projectlibre.com");//$NON-NLS-1$
			}});
		builder.nextColumn();
		builder.append(logo); 
		builder.nextLine(2);
		builder.nextColumn();
		builder.append(link);
//		builder.nextLine(2);
//		builder.append(videos);
		if (Environment.isOpenProj()) {
			builder.nextLine(2);
			builder.nextColumn();
			builder.append(tipOfTheDay);
		}
		builder.nextLine(2);
		builder.nextColumn();
		builder.append(license);
		
		builder.nextLine(2);
		String version=VersionUtils.getVersion();
		builder.addLabel(Messages.getContextString("Text.ShortTitle")+" "+"Version "+(version==null?"Unknown":version),cc.xyw(1,  9, 3));
		builder.nextLine(2);
		builder.addLabel(Messages.getString("AboutDialog.copyright"),cc.xyw(1,  11, 3));

		
		if (false || Environment.isOpenProj()) { // removed donation link
			JPanel p = new JPanel();
			p.add(builder.getPanel());
	//		p.add(makeDonatePanel(false));
			return p;
		}
		else 
			return builder.getPanel();
	}
	protected boolean hasCloseButton() {
		return true;
	}

	protected boolean hasOkAndCancelButtons() {
		return false;
	}

	public static JPanel makeDonatePanel(boolean border) {
		FormLayout layout = new FormLayout("250px" , // cols //$NON-NLS-1$
		"p, 6dlu,  p"); // rows //$NON-NLS-1$
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		if (border)
			builder.setDefaultDialogBorder();

		JLabel msg = new JLabel(Messages.getString("Text.donateMessage"));
		
		builder.append(msg);
		builder.nextLine(2);
		ImageIcon icon = IconManager.getIcon("paypal.donate");
		JButton donate = new JButton(icon);
		donate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		builder.append(donate);
		donate.setEnabled(true);
		donate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				BrowserControl.displayURL(donateUrl);
			}
		});		

		return builder.getPanel();
	}
}
