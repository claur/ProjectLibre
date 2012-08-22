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
package com.projity.toolbar;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JToolBar;

import com.projity.grouping.core.transform.ViewConfiguration;
import com.projity.help.HelpUtil;
import com.projity.menu.MenuActionConstants;
import com.projity.menu.MenuManager;

public class FilterToolBarManager implements MenuActionConstants{
	private MenuManager menuManager;
	JToolBar toolBar = null;
	public static FilterToolBarManager create(MenuManager menuManager) {
		return new FilterToolBarManager(menuManager);
	}
	//best place to this?
	protected TransformComboBox filtersComboBox;
	protected TransformComboBox sortersComboBox;
	protected TransformComboBox groupersComboBox;
	protected Component separator1,separator2,separator3,filler;
	

	private FilterToolBarManager(MenuManager menuManager) {
		this.menuManager = menuManager;
		filtersComboBox=new TransformComboBox(menuManager,ACTION_CHOOSE_FILTER,TransformComboBoxModel.FILTER);
		sortersComboBox=new TransformComboBox(menuManager,ACTION_CHOOSE_SORT,TransformComboBoxModel.SORTER);
		groupersComboBox=new TransformComboBox(menuManager,ACTION_CHOOSE_GROUP,TransformComboBoxModel.GROUPER);
//		Border defaultBorder = BorderFactory.createEmptyBorder(8,8,8,8);
//		filtersComboBox.setBorder(defaultBorder);
//		sortersComboBox.setBorder(defaultBorder);
//		groupersComboBox.setBorder(defaultBorder);
		setComboSize(filtersComboBox);
		setComboSize(sortersComboBox);
		setComboSize(groupersComboBox);
		
		separator1=Box.createRigidArea(new Dimension(16,16));
		separator2=Box.createRigidArea(new Dimension(16,16));
		separator3=Box.createRigidArea(new Dimension(20,20));
		filler=new Box.Filler(new Dimension(0,0),new Dimension(0,0),new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
		HelpUtil.addDocHelp(filtersComboBox,"Filters");
		HelpUtil.addDocHelp(sortersComboBox,"Sorts");
		HelpUtil.addDocHelp(groupersComboBox,"Grouping");
				
	}
	
	private void setComboSize(TransformComboBox combo){
		Dimension size = new Dimension(150,28);
		combo.setMinimumSize(size);
		combo.setMaximumSize(size);
		combo.setPreferredSize(size);
	}
	
	public void addButtons(JComponent toolBar) {
		toolBar.add(filler);
		toolBar.add(filtersComboBox,"Center");
		toolBar.add(separator1);
		toolBar.add(sortersComboBox,"Center");
		toolBar.add(separator2);
		toolBar.add(groupersComboBox,"Center");
		toolBar.add(separator3);
		filtersComboBox.addActionListener(menuManager.getActionFromId(filtersComboBox.getActionCommand()));
		sortersComboBox.addActionListener(menuManager.getActionFromId(sortersComboBox.getActionCommand()));
		groupersComboBox.addActionListener(menuManager.getActionFromId(groupersComboBox.getActionCommand()));
	}
	public void addButtonsInRibbonBand(JComponent component) {
		component.add(filtersComboBox);
		component.add(sortersComboBox);
		component.add(groupersComboBox);
		filtersComboBox.addActionListener(menuManager.getActionFromId(filtersComboBox.getActionCommand()));
		sortersComboBox.addActionListener(menuManager.getActionFromId(sortersComboBox.getActionCommand()));
		groupersComboBox.addActionListener(menuManager.getActionFromId(groupersComboBox.getActionCommand()));
	}

	public void removeButtons(JComponent bar) {
		if (bar != null) {
			bar.remove(filler);
			bar.remove(filtersComboBox);
			bar.remove(separator1);
			bar.remove(sortersComboBox);
			bar.remove(separator2);
			bar.remove(groupersComboBox);
			bar.remove(separator3);
		}
		if (filtersComboBox != null)
			filtersComboBox.removeActionListener(menuManager.getActionFromId(filtersComboBox.getActionCommand()));
		if (sortersComboBox != null)
			sortersComboBox.removeActionListener(menuManager.getActionFromId(sortersComboBox.getActionCommand()));
		if (groupersComboBox != null)
			groupersComboBox.removeActionListener(menuManager.getActionFromId(groupersComboBox.getActionCommand()));
	}
	
	
	
	public void setComboBoxesViewName(String viewName){
		ViewConfiguration view=ViewConfiguration.getView(viewName);
		filtersComboBox.setView(view);
		sortersComboBox.setView(view);
		groupersComboBox.setView(view);
	}
	
	public void setEnabled(boolean enable) {
		filtersComboBox.setEnabled(enable);
		sortersComboBox.setEnabled(enable);
		groupersComboBox.setEnabled(enable);
	}
	
	public void transformBasedOnValue() {
		filtersComboBox.transformBasedOnValue();
		sortersComboBox.transformBasedOnValue();
		groupersComboBox.transformBasedOnValue();
	}
	public void clear() {
		filtersComboBox.setSelectedIndex(0);
		sortersComboBox.setSelectedIndex(0);
		groupersComboBox.setSelectedIndex(0);
	}

}
