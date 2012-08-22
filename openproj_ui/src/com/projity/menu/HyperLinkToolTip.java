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
package com.projity.menu;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JToolTip;
import javax.swing.LookAndFeel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.ToolTipUI;

import com.projity.configuration.Settings;
import com.projity.help.HelpUtil;
import com.projity.strings.Messages;
import com.projity.util.BrowserControl;
 
/**
 * A tooltip that contains hyperlinks that are clickable.
 * Modified from a comment in this thread http://forum.java.sun.com/thread.jspa?threadID=592163&messageID=3095280
 * Note that a tooltip disappears when the mouse leaves the button. You must make sure to align the tooltip
 * directly touching the button to avoid this.
 * 
 */
public class HyperLinkToolTip extends JToolTip {
	private static final long serialVersionUID = 1L;
	private JEditorPane theEditorPane;
 
	public HyperLinkToolTip() {
		setLayout(new BorderLayout());
		LookAndFeel.installBorder(this, "ToolTip.border"); //$NON-NLS-1$
		LookAndFeel.installColors(this, "ToolTip.background", "ToolTip.foreground"); //$NON-NLS-1$ //$NON-NLS-2$
		theEditorPane = new JEditorPane();
		theEditorPane.setContentType("text/html"); //$NON-NLS-1$
		theEditorPane.setEditable(false);
		theEditorPane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					BrowserControl.displayURL(e.getURL().toExternalForm());				}
			}
		});
		add(theEditorPane);
	}
 
	public void setTipText(String tipText) {
		theEditorPane.setText(tipText);
	}
 
	public void updateUI() {
		setUI(new ToolTipUI() {});
	}
	
	private static final String htmlPrefix = "<html><font face=\"Dialog\" size=\"2\">";//$NON-NLS-1$
	
	public static String helpTipText(String text,String helpLink, String demoLink, String docLink) {
		StringBuffer result = new StringBuffer();
		result.append(htmlPrefix); 
		result.append(text);
//		if (Settings.SHOW_HELP_LINKS && helpLink != null) {
//			result.append("<br><a href=\""); //$NON-NLS-1$
//			result.append(helpLink);
//			result.append(Messages.getString("HyperLinkToolTip.SeeOnlineHelp")); //$NON-NLS-1$
//		}
//		if (demoLink != null) {
//			result.append("<br><a href=\""); //$NON-NLS-1$
//			result.append(demoLink);
//			result.append(Messages.getString("HyperLinkToolTip.SeeOnlineDemo")); //$NON-NLS-1$
//		}
		if (docLink != null)
			result.append("<br>").append(HelpUtil.helpTipImg);
		result.append("</font></html>"); //$NON-NLS-1$
		return result.toString();	
		
	}
	
	public static String extractTip(String htmlTip) {
		if (!htmlTip.startsWith("<html>"))
			return htmlTip;
		String t = htmlTip.substring(htmlPrefix.length());
		t = t.substring(0,t.indexOf('<'));
		return t;
		
	}
 
}

