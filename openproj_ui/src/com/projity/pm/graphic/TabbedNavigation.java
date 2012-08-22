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
package com.projity.pm.graphic;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JToolTip;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.batik.util.gui.resource.ButtonFactory;
import org.apache.batik.util.gui.resource.JToolbarSeparator;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.help.HelpUtil;
import com.projity.menu.HyperLinkToolTip;
import com.projity.menu.MenuActionConstants;
import com.projity.menu.MenuManager;
import com.projity.pm.graphic.frames.DocumentFrame;
import com.projity.pm.graphic.frames.GraphicManager;
import com.projity.strings.Messages;
import com.projity.util.Environment;

public class TabbedNavigation implements MenuActionConstants, Serializable {
	private static final long serialVersionUID = -270788624568075685L;
	ExtTabbedPane tabbedPane;
	MenuManager menuManager;
	ArrayList actions = new ArrayList();
	int oldSelected = -1;
	DocumentFrame currentFrame;
	private JToolBar currentBar = null;
	private static int eventNum = 0;
	private JPopupMenu trackingPopup = null;
	private int resourceTabCount = 0;
	private static Color backgroundSelected = new Color(125,157,230);
	//UIManager.getColor("ProgressBar.selectionBackground");
    private Color backgroundUnselected=UIManager.getColor("TabbedPane.unselectedBackground");

	private ArrayList<JButton> trackingButtons = new ArrayList<JButton>();

	private class ExtTabbedPane extends JTabbedPane {
		private static final long serialVersionUID = 7993870683783896098L;
		ExtTabbedPane() {
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			final Font normal = new Font("Verdana", 1, 11);
			setFont(normal);
			setBorder(BorderFactory.createEmptyBorder());

			addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					int i = getSelectedIndex();
					AbstractAction ac = null;
					if (oldSelected != -1) {
						JComponent old = (JComponent) tabbedPane.getComponentAt(oldSelected);
						if (old instanceof JToolBar)
							removeFilterToolBar((JToolBar)old);
						ac = (AbstractAction)actions.get(i);
						ac.actionPerformed(new ActionEvent(this,eventNum++,"click"));
						tabbedPane.setForegroundAt(oldSelected, Color.BLACK);
						tabbedPane.setBackgroundAt(oldSelected, backgroundUnselected);
						JComponent selectedComponent = (JComponent) tabbedPane.getSelectedComponent();
						if (selectedComponent instanceof JToolBar)
							addFilterToolBar((JToolBar)selectedComponent);
					}
					tabbedPane.setForegroundAt(i, Color.WHITE);
					tabbedPane.setBackgroundAt(i, backgroundSelected);
					oldSelected = i;
				}

			});
			GraphicManager.getInstance().getLafManager().setUI(this);
		}

		public void updateUI() {
			// ignore it since i set it explicitly in ctor and would give exception when clickin on tab due to fade issue above
		}

	}
	public JComponent createContentPanel(MenuManager menuManager,JToolBar toolbar, int group, int tabPlacement, boolean addFilterButtons) {
		this.menuManager = menuManager;
		menuManager.add(this);
		boolean top = (tabPlacement == JTabbedPane.TOP);
		int height = top ? 45 : 15;
		FormLayout layout = new FormLayout("p:grow",  "fill:" + height + "dlu:grow");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		CellConstraints cc = new CellConstraints();
		builder.setBorder(BorderFactory.createEmptyBorder());
		tabbedPane= new ExtTabbedPane();
		tabbedPane.setTabPlacement(tabPlacement);
		int tabCount = 0;
		int groupCount = 0;
		for (int i=0; i < toolbar.getComponentCount(); i++) {
			Object obj = toolbar.getComponent(i);
			if (obj instanceof JToolbarSeparator)
				groupCount++;
			if (! (obj instanceof AbstractButton))
				continue;
			if (group != -1 && group != groupCount)
				continue;
			AbstractButton b = (AbstractButton) obj;
			Action action = b.getAction();
			if (action == menuManager.getActionFromId(ACTION_TRACKING_GANTT))
				continue;
			if (action == menuManager.getActionFromId(ACTION_PROJECTS))
				continue;
			JComponent component;
			if (top)
				component = createSubPanel(action, addFilterButtons);
			else
				component = dummy();
			component.setBorder(BorderFactory.createEmptyBorder());
			if (Environment.isNewLook() && !Environment.isNewLaf()) component.setOpaque(false);
			String text = HyperLinkToolTip.extractTip(b.getToolTipText());
			tabbedPane.addTab(text,component);
			if (action == menuManager.getActionFromId(ACTION_RESOURCES))
				this.resourceTabCount = tabCount;
			tabbedPane.setToolTipTextAt(tabCount, text); // don't use version with F1
			actions.add(action);
			tabCount++;
		}
		builder.add(tabbedPane);
		JComponent c = builder.getPanel();
		c.setBorder(BorderFactory.createEmptyBorder());

		return c;
	}

	public void setAllButResourceDisabled(boolean disable) {
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			if (i == resourceTabCount)
				continue;
			tabbedPane.setEnabledAt(i,!disable);
		}

	}
	private JPanel dummy() {
		JPanel dummy = new JPanel();
		dummy.setSize(dummy.getSize().width,0);

		return dummy;
	}


	private JComponent createSubPanel(Action action, boolean addFilterButtons) {
		String toolBarName = null;
		boolean taskMenu = false;
		if (action == menuManager.getActionFromId(ACTION_GANTT)) {
			toolBarName = "GanttToolBar";
			taskMenu = true;
		} else if (action == menuManager.getActionFromId(ACTION_TRACKING_GANTT)) {
			toolBarName = "TrackingGanttToolBar";
			taskMenu = true;
		} else if (action == menuManager.getActionFromId(ACTION_TASK_USAGE_DETAIL)) {
			toolBarName = "TaskUsageDetailToolBar";
		} else if (action == menuManager.getActionFromId(ACTION_RESOURCE_USAGE_DETAIL)) {
			toolBarName = "ResourceUsageDetailToolBar";
		} else if (action == menuManager.getActionFromId(ACTION_NETWORK))
			toolBarName = "NetworkToolBar";
		else if (action == menuManager.getActionFromId(ACTION_WBS))
			toolBarName = "WBSToolBar";
		else if (action == menuManager.getActionFromId(ACTION_RBS))
			toolBarName = "RBSToolBar";
		else if (action == menuManager.getActionFromId(ACTION_RESOURCES))
			toolBarName = "ResourceToolBar";
		else if (action == menuManager.getActionFromId(ACTION_REPORT))
			toolBarName = "ReportToolBar";
		if (toolBarName == null)
			return dummy();
		JToolBar toolBar = menuManager.getToolBar(toolBarName);
		toolBar.setFloatable(false);
		if (taskMenu) {
			toolBar.addSeparator(new Dimension(20, 20));
			final JComponent tracking = tracking();
			toolBar.add(tracking);

			HelpUtil.addDocHelp(tracking,"Tracking_Menu");

//			toolBar.add(menuManager.getMenu(MenuManager.SF_MENU));
		}
		toolBar.addSeparator(new Dimension(40, 20));
//		String viewName = menuManager.getStringFromAction(action);
//		if (addFilterButtons)
//			FilterToolBarManager.getInstance().addButtons(toolBar,menuManager,viewName);


		return toolBar;
	}

	private JComponent tracking() {
		final JButton p = new JButton(Messages.getString("Spreadsheet.Task.tracking"), IconManager.getIcon("print.down")) {
			public Point getToolTipLocation(MouseEvent event) { // the tip MUST be touching the button if html because you can click on links
				if (getToolTipText().startsWith("<html>"))
					return new Point(0, getHeight()-2);
				else
					return super.getToolTipLocation(event);
			}

			public JToolTip createToolTip() {
				if (getToolTipText().startsWith("<html>")) {
					JToolTip tip = new HyperLinkToolTip();
					tip.setComponent(this);
					return tip;
				} else {
					return super.createToolTip();
				}
			}

		};


		p.setHorizontalTextPosition(AbstractButton.LEADING);
		String name = "SFTracking";
		String s = menuManager.getString(name + ButtonFactory.TOOLTIP_SUFFIX);
		if (s != null) {
			String help = menuManager.getStringOrNull(name+ButtonFactory.HELP_SUFFIX);
			String demo = menuManager.getStringOrNull(name+ButtonFactory.DEMO_SUFFIX);
			String doc = menuManager.getStringOrNull(name+ButtonFactory.DOC_SUFFIX);
			if (doc != null)
				s = HyperLinkToolTip.helpTipText(s,help,demo, doc);
			p.setToolTipText(s);
		}
		p.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (trackingPopup == null)
				   trackingPopup = menuManager.getPopupMenu("SFTracking");
				trackingPopup.show(p,0,p.getHeight());
			}});

		trackingButtons.add(p);
		return p;

	}
	public void setActivatedView(String viewId, boolean enable) {
		if (!enable)
			return;
		int index = indexOfViewId(viewId);
		if (index != -1 && index != tabbedPane.getSelectedIndex()) {
			tabbedPane.setSelectedIndex(index);
		}
	}
	private void dumpTabNames() {
		for (int i=0; i < tabbedPane.getTabCount(); i++)
			System.out.println("tab " + i + " title: " + tabbedPane.getTitleAt(i) + "action " + actions.get(i).hashCode());
	}

	private int indexOfViewId(String viewId) {
		Object action = menuManager.getActionFromId(viewId);
		for (int i=0; i < tabbedPane.getTabCount(); i++)
			if (actions.get(i) == action)
				return i;
		return -1;
	}


	private void addFilterToolBar(JToolBar bar) {
		if (currentFrame != null) {
			currentBar  = bar;
			currentFrame.getFilterToolBarManager().addButtons(bar);
		}
	}
	private void removeFilterToolBar(JToolBar bar) {
		if (currentFrame != null) {
			currentFrame.getFilterToolBarManager().removeButtons(bar);
		}
	}

	public void setCurrentFrame(DocumentFrame currentFrame) {
		if (this.currentFrame != null)
			removeFilterToolBar(currentBar);
		this.currentFrame = currentFrame;
		initFilterToolBar();
	}
	public void initFilterToolBar() {
		JComponent selectedComponent = (JComponent) tabbedPane.getSelectedComponent();
		if (selectedComponent instanceof JToolBar)
			addFilterToolBar((JToolBar)selectedComponent);
	}

	public void setTrackingEnabled(boolean enabled) {
		for (JButton but: trackingButtons) {
			but.setEnabled(enabled);
		}
	}
}
