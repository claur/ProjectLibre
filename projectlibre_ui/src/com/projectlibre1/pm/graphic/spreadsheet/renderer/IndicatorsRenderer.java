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
package com.projectlibre1.pm.graphic.spreadsheet.renderer;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.projectlibre1.pm.graphic.IconManager;
import com.projectlibre1.pm.graphic.model.cache.GraphicNode;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheetParams;
import com.projectlibre1.field.Field;

public class IndicatorsRenderer extends DefaultTableCellRenderer implements OfflineRenderer{
	private static final long serialVersionUID = 190987129201L;
	protected static JLabel cellHeader; //TODO static OK?
	protected IndicatorsComponent indicatorsComponent;
	
	public IndicatorsRenderer() {
		super();
	}
	
	
	
	public Component getTableCellRendererComponent (JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column){
		if(indicatorsComponent==null){
			if (TaskIndicatorsComponent.acceptTask(value)) indicatorsComponent=new TaskIndicatorsComponent();
			else if (ResourceIndicatorsComponent.acceptResource(value)) indicatorsComponent=new ResourceIndicatorsComponent();
		}
		if (indicatorsComponent!=null&&indicatorsComponent.acceptValue(value)) {
			JComponent label;
			label = new JPanel();
			//label=(JComponent)super.getTableCellRendererComponent(table,"",isSelected,hasFocus,row,column);
			//indicatorsComponent.setLook(label,isSelected,hasFocus);
			if (table!=null) label.setBackground(table.getBackground());
			label.setLayout(new BoxLayout(label, BoxLayout.X_AXIS));
			StringBuffer text = new StringBuffer();
			
			// I would like to also show a gif next to the text as MS does.  unfortunately, this is not doable
			// with the html tag, since there is no way to refrence the image from the jar (darn)
			// This could be accomplished via a custom tooltip UI.  See http://www.javareference.com/jrexamples/viewexample.jsp?id=83
			
			indicatorsComponent.setIndicators(value, label, text, isSelected, hasFocus);
			
			if (text.length() == 0){
				if (table==null){
					this.setText("");
					return this;
				}else return super.getTableCellRendererComponent(table,"",isSelected,hasFocus,row,column);//empty;
			}
			text.insert(0,"<html>");
			text.append("</html>");
			label.setToolTipText(text.toString());
			return label;
		} else {
			if (table==null){
				this.setText("");
				return this;
			}else return super.getTableCellRendererComponent(table,"",isSelected,hasFocus,row,column);//empty;
		}
	}
	
	public static JLabel getCellHeader() {
		if (cellHeader == null)
			cellHeader = new JLabel(IconManager.getIcon("infomation.icon"));
		return cellHeader;
			
	}
	
	public Component getComponent(Object value, GraphicNode node,Field field,SpreadSheetParams params){
		Component component=getTableCellRendererComponent(null, value, false, false, -1, -1);
		return component;
	}

}
