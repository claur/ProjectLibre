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
package com.projectlibre1.pm.graphic.spreadsheet.common;

import java.util.Date;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.projectlibre1.pm.graphic.spreadsheet.editor.DateEditor;
import com.projectlibre1.pm.graphic.spreadsheet.editor.SimpleEditor;
import com.projectlibre1.pm.graphic.spreadsheet.editor.SpreadSheetCellEditorAdapter;
import com.projectlibre1.pm.graphic.spreadsheet.renderer.DateRenderer;
import com.projectlibre1.pm.graphic.spreadsheet.renderer.OfflineCapableBooleanRenderer;
import com.projectlibre1.pm.graphic.spreadsheet.renderer.SimpleRenderer;
import com.projectlibre1.pm.graphic.spreadsheet.renderer.SpreadSheetCellRendererAdapter;
import com.projectlibre1.datatype.Duration;
import com.projectlibre1.datatype.Money;
import com.projectlibre1.datatype.Work;
import com.projectlibre1.graphic.configuration.GraphicConfiguration;

/**
 *
 */
public class CommonTable extends JTable {

    /**
     * 
     */
    public CommonTable() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param numRows
     * @param numColumns
     */
    public CommonTable(int numRows, int numColumns) {
        super(numRows, numColumns);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param dm
     */
    public CommonTable(TableModel dm) {
        super(dm);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param rowData
     * @param columnNames
     */
    public CommonTable(Object[][] rowData, Object[] columnNames) {
        super(rowData, columnNames);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param rowData
     * @param columnNames
     */
    public CommonTable(Vector rowData, Vector columnNames) {
        super(rowData, columnNames);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param dm
     * @param cm
     */
    public CommonTable(TableModel dm, TableColumnModel cm) {
        super(dm, cm);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param dm
     * @param cm
     * @param sm
     */
    public CommonTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
        super(dm, cm, sm);
        // TODO Auto-generated constructor stub
    }
    public boolean editorsRegistered;
	protected void registerEditors(){
		registerEditors(false);
	}
	protected void registerEditors(boolean compact){
		if (editorsRegistered) return;
		GraphicConfiguration config=GraphicConfiguration.getInstance();
		
		//Modify here to register a custom editor
		//all the types used have to be registered here
		setAdaptedRenderer(String.class,new SimpleRenderer());
		setAdaptedEditor(String.class,new SimpleEditor(String.class));
		
		setAdaptedRenderer(Integer.class,new SimpleRenderer());
		setAdaptedEditor(Integer.class,new SimpleEditor(Integer.class));
		
		setAdaptedRenderer(Double.class,new SimpleRenderer());
		setAdaptedEditor(Double.class,new SimpleEditor(Double.class));
		
       setAdaptedEditor(Date.class, new DateEditor());
//       setAdaptedRenderer(Date.class, new DateRendererDecorator( new SimpleRenderer(), format)); // format will be used
       setAdaptedRenderer(Date.class,new DateRenderer());
       
       setAdaptedRenderer(Boolean.class,new OfflineCapableBooleanRenderer());
		//setAdaptedRenderer(Boolean.class,null);
		setAdaptedEditor(Boolean.class,null);
		
		setAdaptedRenderer(Work.class,new SimpleRenderer(compact));
		setAdaptedEditor(Work.class,new SimpleEditor(Work.class));

		setAdaptedRenderer(Duration.class,new SimpleRenderer());
		setAdaptedEditor(Duration.class,new SimpleEditor(Duration.class));
//		setDefaultEditor(Duration.class,new DefaultCellEditor(new JTextField()));
		setAdaptedRenderer(Money.class,new SimpleRenderer(compact));
		setAdaptedEditor(Money.class,new SimpleEditor(Money.class));
		
		editorsRegistered=true;
	}
	protected void setAdaptedRenderer(Class columnClass,TableCellRenderer renderer) {
		setDefaultRenderer(columnClass,new SpreadSheetCellRendererAdapter(
				(renderer==null)?getDefaultRenderer(columnClass):renderer));
	}
	protected void setAdaptedEditor(Class columnClass,TableCellEditor editor) {
		setDefaultEditor(columnClass, new SpreadSheetCellEditorAdapter(
				(editor==null)?getDefaultEditor(columnClass):editor));
	}

	public TableCellRenderer getDefaultRenderer(Class arg0) {
		// TODO Auto-generated method stub
		return super.getDefaultRenderer(arg0);
	}
	
	

}
