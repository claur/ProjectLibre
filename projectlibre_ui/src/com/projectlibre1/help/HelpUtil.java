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
package com.projectlibre1.help;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.table.JTableHeader;

import com.projectlibre1.pm.graphic.IconManager;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheet;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheetModel;
import com.projectlibre1.configuration.Settings;
import com.projectlibre1.field.Field;
import com.projectlibre1.util.BrowserControl;
import com.projectlibre1.util.Environment;

public class HelpUtil implements KeyEventDispatcher {
	private  HashMap<Component,String> map = new HashMap<Component,String>();
	private static HelpUtil instance = null;
	private static final int DELAY_BETWEEN_HELPS = 5000;
	private long lastHelpTime = 0L;
	public boolean noHelp = false;
	
	private synchronized static HelpUtil getInstance() {
		if (instance == null)
			instance = new HelpUtil();
		return instance;
	}
	private HelpUtil() {
		noHelp=Environment.getOs()==Environment.MAC&&Environment.isApplet();
		if (noHelp) System.out.print("no help");
		if (noHelp) return;
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
				
	}
	public static String helpTipImg = "<img src=\"" + IconManager.class.getResource("/toolbarButtonGraphics/general/Information16.gif")
	+ "\"> F1";
	// a tooltip with a help icon and F1
	public static String helpTipHtml = "<html><img src=\"" + IconManager.class.getResource("/toolbarButtonGraphics/general/Information16.gif")
	+ "\"> F1</html>";
	public static void addHelp(Component c, String address) {
		if (getInstance().noHelp) return;
		if (c instanceof JComponent && ((JComponent)c).getToolTipText() == null)
			((JComponent)c).setToolTipText(helpTipHtml);
		getInstance().map.put(c,address);
	}
	public static String getHelpURL(String address) {
		return Settings.HELP_HOME+address;		
	}
	public static void addDocHelp(Component c, String address) {
		addHelp(c,getHelpURL(address));
	}

	public static String findHelp(Component c) {
		if (getInstance().noHelp) return null;
		String result = null;
		while (c != null) {
			if ((result = getInstance().map.get(c)) != null)
				break;
			c = c.getParent();
		}
		return result;
	}
	public static boolean doHelp(Component c) {
		if (getInstance().noHelp) return false;
		String help = findHelp(c);
		if (help != null) {
			long time = System.currentTimeMillis();
			boolean doHelp = time - getInstance().lastHelpTime > DELAY_BETWEEN_HELPS;
			getInstance().lastHelpTime = time;
		
			if (doHelp) { // prevents doubles which arrive for some reason
				BrowserControl.displayURL(help);
				return true;
			}
		} 
		return false;
	}
	public static boolean doHelp() {
		if (getInstance().noHelp) return false;
		Point pt = MouseInfo.getPointerInfo().getLocation();
		Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

		while (owner != null) { // get dialog or frame which is focus owner
			if (owner instanceof JDialog || owner instanceof Frame) {
				break;
			}
			owner = owner.getParent();
		}
		// Get component at point
		Component c = null;
		Point loc = owner.getLocationOnScreen();
		Point offsetPoint = new Point(pt.x- loc.x, pt.y-loc.y);
		c = ((Container)owner).findComponentAt(offsetPoint);

		if (c == null)
			c = owner;
		
		// do help for most detailed item
		while(c != null) {
			if (c instanceof JTableHeader) { // if clicked on row header, see if there is help for the field
				JTableHeader th = (JTableHeader)c;
				loc = c.getLocationOnScreen();
				offsetPoint = new Point(pt.x- loc.x, pt.y-loc.y);	
				int col =  th.columnAtPoint(offsetPoint);
				SpreadSheet ss = (SpreadSheet)th.getTable();
				Field f = ((SpreadSheetModel)ss.getModel()).getFieldInColumn(col+1);
				if (f.getHelp()!=null) {
					BrowserControl.displayURL(Settings.HELP_HOME+f.getHelp());
					return true;
				}
			}
			if (doHelp(c)) 
				return true;
			c = c.getParent();
		}
		return false;

	}

	public boolean dispatchKeyEvent(KeyEvent e) {
		if (noHelp) return false;
		if (e.getKeyCode() == KeyEvent.VK_F1 && !e.isConsumed())
			if (doHelp()) {
				e.consume();
				return true;
			}
		return false;
	}
}
