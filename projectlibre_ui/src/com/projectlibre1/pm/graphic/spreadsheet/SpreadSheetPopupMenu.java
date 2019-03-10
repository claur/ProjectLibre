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
package com.projectlibre1.pm.graphic.spreadsheet;

import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;

import com.projectlibre1.menu.MenuActionConstants;
import com.projectlibre1.pm.graphic.IconManager;
import com.projectlibre1.util.Environment;

/**
 *
 */
public class SpreadSheetPopupMenu extends JPopupMenu {
	   protected int row;
	    protected int col;
	    protected final SpreadSheet spreadSheet;
	    /**
	     * 
	     */
	    public SpreadSheetPopupMenu(SpreadSheet spreadSheet) {
	        super();
	        this.spreadSheet=spreadSheet;
	        
	        //setLabel("");
	        setBorder(new BevelBorder(BevelBorder.RAISED));
	        
	        final SpreadSheet sp=spreadSheet;
			AbstractAction action;
			
			//Normal spreadsheet
			//NodeListTransferHandler.registerWith(this);
			String[] actions=spreadSheet.getActionList();
			if (actions!=null)
			for (int i=0;i<actions.length;i++){
				add(spreadSheet.prepareAction(actions[i]),getMenuAction(actions[i]));
			}
		}
	    
	    private Map menuActionMap=null;
	    protected String getMenuAction(String action){
	    	if (menuActionMap==null){
	    		menuActionMap=new HashMap();
	    		if (Environment.isNewLook()) {
		    		menuActionMap.put(MenuActionConstants.ACTION_NEW,"menu24.insertTask");
		    		menuActionMap.put(MenuActionConstants.ACTION_DELETE,"menu24.delete");
		    		menuActionMap.put(MenuActionConstants.ACTION_INDENT,"menu24.indent");
		    		menuActionMap.put(MenuActionConstants.ACTION_OUTDENT,"menu24.outdent");
		    		menuActionMap.put(MenuActionConstants.ACTION_CUT,"menu24.cut");
		    		menuActionMap.put(MenuActionConstants.ACTION_COPY,"menu24.copy");
		    		menuActionMap.put(MenuActionConstants.ACTION_PASTE,"menu24.paste");
		    		menuActionMap.put(MenuActionConstants.ACTION_EXPAND,"menu24.expand");
		    		menuActionMap.put(MenuActionConstants.ACTION_COLLAPSE,"menu24.collapse");
	    		} else {
		    		menuActionMap.put(MenuActionConstants.ACTION_NEW,"menu.insertTask");
		    		menuActionMap.put(MenuActionConstants.ACTION_DELETE,"menu.delete");
		    		menuActionMap.put(MenuActionConstants.ACTION_INDENT,"menu.rightArrow");
		    		menuActionMap.put(MenuActionConstants.ACTION_OUTDENT,"menu.leftArrow");
		    		menuActionMap.put(MenuActionConstants.ACTION_CUT,"menu.cut");
		    		menuActionMap.put(MenuActionConstants.ACTION_COPY,"menu.copy");
		    		menuActionMap.put(MenuActionConstants.ACTION_PASTE,"menu.paste");
		    		menuActionMap.put(MenuActionConstants.ACTION_EXPAND,"menu.expand");
		    		menuActionMap.put(MenuActionConstants.ACTION_COLLAPSE,"menu.collapse");
	    		}
	    	}
	    	return (String)menuActionMap.get(action);
	    }
	    
	    private void add(Action action, String iconName) {
	    	JMenuItem menuItem = new JMenuItem(action);
	    	menuItem.setIcon(IconManager.getIcon(iconName));
	    	add(menuItem);
	    }

	    /**
	     * @return Returns the col.
	     */
	    public int getCol() {
	        return col;
	    }
	    /**
	     * @param col The col to set.
	     */
	    public void setCol(int col) {
	        this.col = col;
	    }
	    /**
	     * @return Returns the row.
	     */
	    public int getRow() {
	        return row;
	    }
	    /**
	     * @param row The row to set.
	     */
	    public void setRow(int row) {
	        this.row = row;
	    }
    
    

		public SpreadSheet getSpreadSheet() {
			return spreadSheet;
		}
}
