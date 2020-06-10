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
import com.projectlibre1.pm.graphic.IconManager;
import com.projectlibre1.pm.graphic.frames.GraphicManager;
import com.projectlibre1.configuration.Settings;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.util.BrowserControl;
import com.projectlibre1.util.Environment;
import com.projectlibre1.util.VersionUtils;

public final class HelpDialog extends AbstractDialog {
	private static final long serialVersionUID = 1L;
	private static final String helpUrl = "https://www.projectlibre.com/projectlibre-documentation"; //$NON-NLS-1$
	private static final String helpLoginUrl = "https://www.projectlibre.com/user/login";
	private static final String videosUrl = Settings.SITE_HOME; //$NON-NLS-1$
	public static final String donateUrl = "https://www.projectlibre.com";
	JButton link;
	JButton registerToHelp;
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
		
		
		registerToHelp = new JButton(Messages.getString("HelpDialog.RegisterToOnlineHelp")); //$NON-NLS-1$
		registerToHelp.setEnabled(true);
		registerToHelp.setToolTipText(helpUrl);
		registerToHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				BrowserControl.displayURL(helpLoginUrl);
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
		if (Environment.isProjectLibre()) {
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
			
				"p, 6dlu, p, 6dlu, p, 1dlu, p, 6dlu, p, 6dlu, p, 6dlu, p, 10dlu, p, 6dlu, p, 6dlu, p"); // rows //$NON-NLS-1$

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
		builder.nextLine(2);
		builder.nextColumn();
		builder.addLabel(Messages.getString("HelpDialog.RegisterToOnlineHelp1"),cc.xyw(1,  5, 3));
		builder.nextLine(2);
		builder.nextColumn();
		builder.addLabel(Messages.getString("HelpDialog.RegisterToOnlineHelp2"),cc.xyw(1,  7, 3));
		builder.nextLine(2);
		builder.nextColumn();
		builder.append(registerToHelp);

//		builder.nextLine(2);
//		builder.append(videos);
		if (Environment.isProjectLibre()) {
			builder.nextLine(2);
			builder.nextColumn();
			builder.append(tipOfTheDay);
		}
		builder.nextLine(2);
		builder.nextColumn();
		builder.append(license);
		
		builder.nextLine(2);
		String version=VersionUtils.getVersion();
		builder.addLabel(Messages.getContextString("Text.ShortTitle")+" "+"Version "+(version==null?"Unknown":version),cc.xyw(1,  15, 3));
		builder.nextLine(2);
		builder.addLabel(Messages.getString("AboutDialog.copyright"),cc.xyw(1,  17, 3));

		
		if (false || Environment.isProjectLibre()) { // removed donation link
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
