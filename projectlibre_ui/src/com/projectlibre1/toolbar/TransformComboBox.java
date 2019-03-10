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
package com.projectlibre1.toolbar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JComboBox;
import javax.swing.JToolTip;
import javax.swing.UIManager;

import org.apache.batik.util.gui.resource.ButtonFactory;

import com.projectlibre1.menu.HyperLinkToolTip;
import com.projectlibre1.menu.MenuManager;
import com.projectlibre1.grouping.core.transform.CommonTransformFactory;
import com.projectlibre1.grouping.core.transform.ViewConfiguration;

/**
 * 
 */
public class TransformComboBox extends JComboBox {
	protected int type;
	public TransformComboBox(MenuManager menuManager, String command,int type) {
		super(new TransformComboBoxModel(type));
		setActionCommand(command);
		this.type=type;
		setMaximumSize(new Dimension(150,Integer.MAX_VALUE));
		String tip;
		if (menuManager != null) { // for the combox on top of the screen, use the properties file
			String text = menuManager.getString(command+ButtonFactory.TOOLTIP_SUFFIX);
			String help = menuManager.getStringOrNull(command+ButtonFactory.HELP_SUFFIX);
			String demo = menuManager.getStringOrNull(command+ButtonFactory.DEMO_SUFFIX);
			String doc = menuManager.getStringOrNull(command+ButtonFactory.DOC_SUFFIX);
			tip = HyperLinkToolTip.helpTipText(text, help, demo, doc);
		} else { // for the one in the histogram, just show filter text
			tip =((TransformComboBoxModel)getModel()).getTipText();
		}
		setToolTipText(tip);
	}
	public void setView(ViewConfiguration view){
		((TransformComboBoxModel)getModel()).setView(view);
		setSelectedItem(getModel().getSelectedItem());//JComboBox local state update
	}
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
	
	public void paintComponent(Graphics graphics) {
		boolean none =  (getSelectedIndex() <= 0);
		setForeground(none ? UIManager.getColor("ComboBox.foreground") : Color.RED); //$NON-NLS-1$
		super.paintComponent(graphics);
	}
	public void transformBasedOnValue() {
		CommonTransformFactory factory = (CommonTransformFactory)getSelectedItem();
		((TransformComboBoxModel)getModel()).changeTransform(factory);
	}

}
