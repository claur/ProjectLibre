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
package com.projity.menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.ButtonFactory;
import org.apache.batik.util.gui.resource.RibbonFactory;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

import com.projectlibre.ui.ribbon.CustomRibbonBandGenerator;
import com.projity.pm.graphic.TabbedNavigation;
import com.projity.strings.DirectoryClassLoader;
import com.projity.util.ClassLoaderUtils;
import com.projity.util.Environment;

/**
 *
 */
public class MenuManager {
	private static final String MENU_BUNDLE = "com.projity.menu.menu";
	private static final String MENU_INTERNAL_BUNDLE = "com.projity.menu.menuInternal";
	private static final String MENU_BUNDLE_CONF_DIR = "menu";
	public static final String STANDARD_MENU ="StandardMenuBar";
	public static final String MAC_STANDARD_MENU ="MacStandardMenuBar";
	public static final String SERVER_STANDARD_MENU ="ServerStandardMenuBar";
	public static final String SF_MENU ="SFMenuBar";
	public static final String STANDARD_TOOL_BAR ="StandardToolBar";
	public static final String MAC_STANDARD_TOOL_BAR ="MacStandardToolBar";
	public static final String FILE_TOOL_BAR ="FileToolBar";
	public static final String BIG_TOOL_BAR ="BigToolBar";
	public static final String VIEW_TOOL_BAR ="ViewToolBar";
	public static final String VIEW_TOOL_BAR_WITH_NO_SUB_VIEW_OPTION ="ViewToolBarNoSubView";
	public static final String RIBBON_VIEW_BAR ="RibbonViewToolBar";
	public static final String PRINT_PREVIEW_TOOL_BAR ="PrintPreviewToolBar";

	public static final String STANDARD_RIBBON = "StandardRibbon";

	//private static MenuManager instance = null;
	static ResourceBundle[] bundles;
	/*static*/ ExtMenuFactory menuFactory;
	ExtToolBarFactory toolBarFactory;
	ExtRibbonFactory ribbonFactory;
	ActionMap rootActionMap;

	LinkedList tabbedNavigations = new LinkedList();

	public void add(TabbedNavigation t) {
		tabbedNavigations.add(t);
	}
	private MenuManager(ActionMap rootActionMap) {
		this.rootActionMap = rootActionMap;
		ResourceBundle internalBundle=null,bundle=null;
		if (bundle==null){
			try{
				DirectoryClassLoader dir=new DirectoryClassLoader();
				if (dir.isValid()){
					bundle=ResourceBundle.getBundle(MENU_BUNDLE_CONF_DIR,Locale.getDefault(),dir);
				}
			}catch(Exception e){}
			if (internalBundle==null) internalBundle =  ResourceBundle.getBundle(MENU_INTERNAL_BUNDLE,Locale.getDefault(),ClassLoaderUtils.getLocalClassLoader());
			if (bundle==null) bundle =  ResourceBundle.getBundle(MENU_BUNDLE,Locale.getDefault(),ClassLoaderUtils.getLocalClassLoader());
			bundles=new ResourceBundle[]{internalBundle,bundle};
		}
		menuFactory = new ExtMenuFactory(rootActionMap,bundles);
		toolBarFactory = new ExtToolBarFactory(rootActionMap,bundles);
		if (Environment.isRibbonUI()) ribbonFactory = new ExtRibbonFactory(rootActionMap,bundles);
	}

	public static MenuManager getInstance(ActionMap rootActionMap) {
//		if (instance == null)
//			instance = new MenuManager(rootActionMap);
//		return instance;
		return new MenuManager(rootActionMap);
	}

	public JMenuBar getMenu(String name) {
		return menuFactory.createJMenuBar(name);
	}

	public JPopupMenu getPopupMenu(String name) {
		return menuFactory.createJPopupMenuBar(name);
	}

	public static String getMenuString(String key) {
    	MissingResourceException exception=null;
    	String s=null;
    	for (ResourceBundle bundle : bundles){
    		try {
				s=bundle.getString(key);
				exception=null;
			} catch (MissingResourceException e) {
				exception=e;
				continue;
			}
    		if (s!=null) break;
    	}
    	if (exception!=null) throw exception;
    	return s;
	}
	public String getString(String key) {
		return menuFactory.getString(key);
	}

    public String getStringOrNull(String key) {
    	try {
    	   return getString(key);
    	} catch (MissingResourceException e) {
    		return null;
    	}
    }

	public String getActionStringFromId(String id) {
		String result = menuFactory.getActionStringFromId(id);
		if (result == null)
			System.out.println("Invalid item: " + id + " it must be a menu item in the menu properties, even if it's only shown in a toolbar");
		return result;
	}

    public Action getActionFromId(String id) {
		return menuFactory.getActionFromId(id);
    }
    public String getStringFromAction(Action action) {
		return menuFactory.getStringFromAction(action);
    }

	public JMenuItem getMenuItemFromId(String id) {
		return menuFactory.getMenuItemFromId(id);
	}
	public ArrayList getToolButtonsFromId(String id) {
		return ribbonFactory==null?toolBarFactory.getButtonsFromId(id):ribbonFactory.getButtonsFromId(id);
	}

	public final ExtToolBarFactory getToolBarFactory() {
		return toolBarFactory;
	}
	public final ExtRibbonFactory getRibbonFactory() {
		return ribbonFactory;
	}
	public JToolBar getToolBar(String name) {
		return toolBarFactory.createJToolBar(name);
	}
	public void initComponent(String name, JComponent component) {
		toolBarFactory.initJComponent(name,component);
	}
	public Collection<RibbonTask> getRibbon(String name, CustomRibbonBandGenerator customBandsGenerator) {
		return ribbonFactory.createRibbon(name, customBandsGenerator);
	}
	public Collection<AbstractCommandButton> getTaskBar(String name) {
		return ribbonFactory.createTaskBar(name);
	}

	public void setActionEnabled(String id, boolean enable) {
		Collection buttons = getToolButtonsFromId(id);
		if (buttons != null) {
			Iterator i = buttons.iterator();
			while (i.hasNext()) {
				AbstractButton button = (AbstractButton)i.next();
				if (button != null)
					button.setEnabled(enable);
			}
		}
		JMenuItem menuItem = menuFactory.getMenuItemFromId(id);
		if (menuItem != null)
			menuItem.setEnabled(enable);
	}
	public void setActionVisible(String id, boolean enable) {
		Collection buttons = getToolButtonsFromId(id);
		if (buttons != null) {
			Iterator i = buttons.iterator();
			while (i.hasNext()) {
				AbstractButton button = (AbstractButton)i.next();
				if (button != null)
					button.setVisible(enable);
			}
		}
		JMenuItem menuItem = menuFactory.getMenuItemFromId(id);
		if (menuItem != null)
			menuItem.setVisible(enable);
	}
	public void setActionSelected(String id, boolean enable) {
		Collection buttons = getToolButtonsFromId(id);
		if (buttons != null) {
			Iterator i = buttons.iterator();
			while (i.hasNext()) {
				AbstractButton button = (AbstractButton)i.next();
				if (button != null) {
					button.setSelected(enable);
					if (button instanceof JToggleButton) {
					//	button.setBackground(enable ? Color.GRAY : ExtButtonFactory.BACKGROUND_COLOR);
					}
				}
			}
		}
		JMenuItem menuItem = menuFactory.getMenuItemFromId(id);
		if (menuItem != null)
			menuItem.setSelected(enable);
		Iterator i = tabbedNavigations.iterator();
		while (i.hasNext())
			((TabbedNavigation)i.next()).setActivatedView(id, enable);

	}
	public void setText(String id, String text) {
		Collection buttons = getToolButtonsFromId(id);
		if (buttons != null) {
			Iterator i = buttons.iterator();
			while (i.hasNext()) {
				AbstractButton button = (AbstractButton)i.next();
				if (button != null)
					button.setToolTipText(text);
			}
		}
		JMenuItem menuItem = menuFactory.getMenuItemFromId(id);
		if (menuItem != null)
			menuItem.setText(text);
	}
	
	
    public String getTextForId(String id) {
    	return menuFactory.getTextForId(id);
    }
    public String getFullTipText(String name) {
		String s = getStringOrNull(name + ButtonFactory.TOOLTIP_SUFFIX);
		if (s != null) {
			String help = getStringOrNull(name+ButtonFactory.HELP_SUFFIX);
			String demo = getStringOrNull(name+ButtonFactory.DEMO_SUFFIX);
			String doc = getStringOrNull(name+ButtonFactory.DOC_SUFFIX);

			if (doc != null)
				s = HyperLinkToolTip.helpTipText(s,help,demo, doc);
		}
		return s;
    }

}