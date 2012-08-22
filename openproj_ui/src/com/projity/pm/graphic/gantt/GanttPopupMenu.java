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
package com.projity.pm.graphic.gantt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;

import com.projity.graphic.configuration.BarStyle;
import com.projity.grouping.core.transform.TransformList;
import com.projity.grouping.core.transform.filtering.BaseFilter;
import com.projity.pm.graphic.graph.GraphInteractor;
import com.projity.pm.graphic.graph.GraphModel;
import com.projity.pm.graphic.graph.GraphPopupMenu;
import com.projity.strings.Messages;


/**
 *
 */
public class GanttPopupMenu extends GraphPopupMenu{
	private static final long serialVersionUID = -5006500626139949187L;


	private class BarMenuAction extends JRadioButtonMenuItem implements ActionListener {
		private static final long serialVersionUID = 8168153384233811506L;
		BarStyle style;
    	
    	BarMenuAction(final BarStyle style) {
    		super(style.getName());
    		this.style = style;
    		setSelected(style.isActive());
    		addActionListener(this);
    	}
    	public void actionPerformed(ActionEvent arg0) {
    	    style.setActive(isSelected());
    	    ((GraphModel)interactor.getGraph().getModel()).updateAll(true);
    	}
    }
    
    private class AssignmentsMenuAction extends JRadioButtonMenuItem implements ActionListener {
		private static final long serialVersionUID = 3480838269288912755L;
		BaseFilter filter,filterOffline;
    	
    	AssignmentsMenuAction() {
    		super(Messages.getString("Gantt.Popup.showAssignments"));
    		filter=(BaseFilter)TransformList.getInstance("hidden_filters").getTransform("Filter.Gantt");
    		filterOffline=(BaseFilter)TransformList.getInstance("hidden_filters").getTransform("Filter.OfflineGantt");
    		setSelected(filter.isShowAssignments());
    		addActionListener(this);
    	}
        public void actionPerformed(ActionEvent e) {
            filter.setShowAssignments(isSelected());
            filterOffline.setShowAssignments(isSelected());
            ((GraphModel)interactor.getGraph().getModel()).getCache().update();
        }
    }
   
    private class SplitModeMenuAction extends AbstractAction {
    	/**
		 * 
		 */
		private static final long serialVersionUID = -8615889754474230400L;
		SplitModeMenuAction() {
    		super(Messages.getString("Gantt.Popup.splitMode"));
    	}
        public void actionPerformed(ActionEvent e) {
            ((GanttInteractor)interactor).setSplitMode();
        }
    }
    
    
    public GanttPopupMenu(final GraphInteractor interactor) {
        super(interactor);
    }
    
	
/**
 * Because the styles may change, rebuild the menu each time
 *
 */
	protected void init() {
    	removeAll();
    	add(new SplitModeMenuAction());
    	add(new AssignmentsMenuAction());
        final JMenu bars=new JMenu(Messages.getString("Gantt.Popup.barStylesMenu"));
        final JMenu annotations=new JMenu(Messages.getString("Gantt.Popup.annotationStylesMenu"));
		CollectionUtils.forAllDo(interactor.getGraph().getBarStyles().getRows(), new Closure() {
			public void execute(Object arg0) {
				BarStyle barStyle = (BarStyle)arg0;
				BarMenuAction menuAction =new BarMenuAction(barStyle); 
				if (barStyle.isLink()) // move the show links item to the main menu
					add(menuAction);
				else if (barStyle.isCalendar()) // move the show links item to the main menu
					add(menuAction);
				else if (barStyle.isHorizontalGrid()) // move the show links item to the main menu
					add(menuAction);
				else if (barStyle.isAnnotation())
					annotations.add(menuAction);
				else 
					bars.add(menuAction);
				
			}
		});
        add(bars);
        add(annotations);
    	
    }

}
