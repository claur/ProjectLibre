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
package com.projity.pm.graphic.spreadsheet.common;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.DefaultTableColumnModel;

import com.projity.graphic.configuration.shape.Colors;
import com.projity.menu.MenuActionConstants;
import com.projity.pm.graphic.spreadsheet.SpreadSheet;
import com.projity.pm.graphic.spreadsheet.SpreadSheetPopupMenu;

/**
 *
 */
public class SpreadSheetRowHeader extends JTable {
	protected CommonSpreadSheet table;
	//protected SpreadSheetPopupMenu popup=null;
	public SpreadSheetRowHeader(CommonSpreadSheet table) {
		super();
		setGridColor(Colors.GRAY);
		this.table=table;
		if (table instanceof SpreadSheet){
			final SpreadSheet spreadSheet=(SpreadSheet)table;
			
			getActionMap().put("cut",new AbstractAction(){
				public void actionPerformed(java.awt.event.ActionEvent e) {
					spreadSheet.prepareAction(MenuActionConstants.ACTION_CUT).actionPerformed(new ActionEvent(spreadSheet,e.getID(),e.getActionCommand()));
				}
			});
			getActionMap().put("copy",new AbstractAction(){
				public void actionPerformed(java.awt.event.ActionEvent e) {
					spreadSheet.prepareAction(MenuActionConstants.ACTION_COPY).actionPerformed(new ActionEvent(spreadSheet,e.getID(),e.getActionCommand()));
				}
			});
			getActionMap().put("paste",new AbstractAction(){
				public void actionPerformed(java.awt.event.ActionEvent e) {
					spreadSheet.prepareAction(MenuActionConstants.ACTION_PASTE).actionPerformed(new ActionEvent(spreadSheet,e.getID(),e.getActionCommand()));
				}
			});
			
		}
		this.setUI(new BasicTableUI());
		
	}
	
	protected SpreadSheetPopupMenu getPopup(){
//		if (popup==null){
//			SpreadSheet spreadSheet=(SpreadSheet)table;
//			popup = spreadSheet.hasRowPopup() ? new SpreadSheetPopupMenu(spreadSheet) : null;
//		}
//		return popup;
		return ((SpreadSheet)table).getPopup();
	}
//	public void clearPopup(){
//		popup=null;
//	}
	
	public void setModel(CommonSpreadSheetModel spreadSheetModel, DefaultTableColumnModel spreadSheetColumnModel) {
	    setModel(spreadSheetModel);
	    setColumnModel(spreadSheetColumnModel);
	    
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		if (table instanceof SpreadSheet){
			final SpreadSheet spreadSheet=(SpreadSheet)table;
			addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				SpreadSheetPopupMenu popup=getPopup();
				if (SwingUtilities.isLeftMouseButton(e)){
					if (e.getClickCount()==2){
						((SpreadSheet)table).doDoubleClick(0,0);
//						Component comp=SpreadSheetRowHeader.this;
//						while(!((comp=comp.getParent()) instanceof MainFrame));
//						MainFrame mainFrame=(MainFrame)comp;
//						mainFrame.doInformationDialog(false);
//
					}
				}else if (popup!=null&&SwingUtilities.isRightMouseButton(e)){ //e.isPopupTrigger() can be used too
					Point p = e.getPoint();
					int row = rowAtPoint(p);
					int col = columnAtPoint(p);
					table.selection.getRowSelection().addSelectionInterval(row, row);
					
					popup.setRow(row);
					popup.setCol(0);
					popup.show(SpreadSheetRowHeader.this,e.getX(),e.getY());
				}
			}
		});
		}

	}	

	public void changeSelection(int rowIndex, int columnIndex, boolean toggle,
			boolean extend) {
		changeSelection(rowIndex,columnIndex,toggle,extend,true);
	}
	public void changeSelection(int rowIndex, int columnIndex, boolean toggle,
			boolean extend,boolean forwards) {
		boolean clearTable=(getSelectedRowCount()==0);
		super.changeSelection(rowIndex, columnIndex, toggle, extend);
		if (forwards){
			table.finishCurrentOperations();
			if (clearTable) table.changeSelection(rowIndex, columnIndex, false, false,false);
			else table.changeSelection(rowIndex, columnIndex, toggle, extend,false);
			table.getSelection().getColumnSelection().addSelectionInterval(0,table.getColumnCount()-1);
		}
	}
	
	public CommonSpreadSheet getSpreadSheet(){
		return table;
	}
	public void updateUI() {
		this.setUI(new BasicTableUI());
		
	}
}
