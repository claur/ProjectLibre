/*
 * Created on Sep 10, 2007
 *
 * Copyright 2004, Projity Inc.
 */
package com.projity.help;

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

import com.projity.configuration.Settings;
import com.projity.field.Field;
import com.projity.pm.graphic.IconManager;
import com.projity.pm.graphic.spreadsheet.SpreadSheet;
import com.projity.pm.graphic.spreadsheet.SpreadSheetModel;
import com.projity.util.BrowserControl;
import com.projity.util.Environment;

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
