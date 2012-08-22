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
package com.projity.pm.graphic.spreadsheet.time;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.projity.field.Field;
import com.projity.graphic.configuration.ActionList;
import com.projity.graphic.configuration.CellStyle;
import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.graphic.configuration.shape.Colors;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.model.event.CompositeCacheEvent;
import com.projity.pm.graphic.spreadsheet.common.CommonSpreadSheet;
import com.projity.pm.graphic.spreadsheet.common.CommonSpreadSheetModel;
import com.projity.pm.graphic.spreadsheet.selection.SpreadSheetListSelectionModel;
import com.projity.pm.graphic.spreadsheet.selection.SpreadSheetSelectionModel;
import com.projity.pm.graphic.timescale.CoordinatesConverter;
import com.projity.pm.graphic.timescale.ScaledComponent;
import com.projity.pm.task.Project;
import com.projity.timescale.TimeScaleListener;

/**
 *
 */
public class TimeSpreadSheet extends CommonSpreadSheet implements ScaledComponent{
	protected Project project;
	protected ArrayList fieldArray;
	public TimeSpreadSheet(Project project) {
		super();
		setTableHeader(null);
		this.project=project;
	}
	
	public void setCache(NodeModelCache cache,ArrayList fieldArray, CellStyle cellStyle, ActionList actionList){
		TimeSpreadSheetModel model=new TimeSpreadSheetModel(cache,fieldArray,cellStyle,actionList);
		setModel(model,
				new TimeSpreadSheetColumnModel(this));	    
	}
	
	public void setFieldArray(ArrayList fieldArray){
		((TimeSpreadSheetModel)getModel()).setFieldArray(fieldArray);
		((TimeSpreadSheetModel)getModel()).resetSelectedFieldArray();

	
	}
	
	
	public void setSelectedFieldArray(ArrayList fieldArray){
		((TimeSpreadSheetModel)getModel()).setSelectedFieldArray(fieldArray);
	}
	public final ArrayList getSelectedFieldArray() {
		return ((TimeSpreadSheetModel)getModel()).getSelectedFieldArray();
	}
	public void selectFieldArray(Field field){
		((TimeSpreadSheetModel)getModel()).selectFieldArray(field);
	}

	
	
	public void setModel(CommonSpreadSheetModel spreadSheetModel,
			DefaultTableColumnModel spreadSheetColumnModel) {
		TableModel oldModel=getModel();
	    setModel(spreadSheetModel);
	    setColumnModel(spreadSheetColumnModel);
	    
	    selection = new SpreadSheetSelectionModel(this);
		selection.setRowSelection(new SpreadSheetListSelectionModel(selection,
				true));
		selection.setColumnSelection(new SpreadSheetListSelectionModel(
				selection, false));
		setSelectionModel(selection.getRowSelection());
		//createDefaultColumnsFromModel(); done outside
		getColumnModel().setSelectionModel(selection.getColumnSelection());
		
		registerEditors(true);
		initRowHeader(spreadSheetModel);
		initModel();
		initListeners();
	    if (oldModel!=spreadSheetModel&&oldModel instanceof CommonSpreadSheetModel) ((CommonSpreadSheetModel)getModel()).getCache().removeNodeModelListener(this);
	    spreadSheetModel.getCache().addNodeModelListener(this);
	}
	public void cleanUp() {
		((TimeSpreadSheetModel)getModel()).getCache().removeNodeModelListener(this);
     	getCoord().removeTimeScaleListener((TimeScaleListener) getColumnModel());
     	getCoord().removeTimeScaleListener((TimeScaleListener) getColumnModel());
		super.cleanUp();
	}

	protected void initRowHeader(CommonSpreadSheetModel spreadSheetModel){
		rowHeader.setModel(spreadSheetModel,new TimeSpreadSheetRowHeaderColumnModel());
		rowHeader.createDefaultColumnsFromModel();

		GraphicConfiguration config=GraphicConfiguration.getInstance();
		rowHeader.setRowHeight(config.getRowHeight());

	}
	
	public Project getProject() {
		return project;
	}
	
    public CoordinatesConverter getCoord() {
    	TimeSpreadSheetModel model=(TimeSpreadSheetModel)getModel();
        return model.getCoord();
    }
     public void setCoord(CoordinatesConverter coord) {
     	TimeSpreadSheetModel model=(TimeSpreadSheetModel)getModel();
        model.setCoord(coord);
     	TimeSpreadSheetColumnModel columnModel=(TimeSpreadSheetColumnModel)getColumnModel();
     	columnModel.setCoord(coord);
    }
     
 	/*protected void registerEditors(){
		GraphicConfiguration config=GraphicConfiguration.getInstance();
		
		//Modify here to register a custom editor
		//all the types used have to be registered here
		setAdaptedRenderer(Duration.class,new TimeSimpleRenderer());
		setAdaptedEditor(Duration.class,new TimeSimpleEditor(Duration.class));
	}*/
    
     

     public void createDefaultColumnsFromModel() {
     	TableColumnModel columnModel=getColumnModel();
        if (columnModel!=null&&columnModel instanceof TimeSpreadSheetColumnModel){
        	((TimeSpreadSheetColumnModel)columnModel).updateColumns();
        }
    }
     
     public Class getRowClass(int row) {
        return ((TimeSpreadSheetModel)getModel()).getRowClass(row);
    }
     
     public TableCellRenderer getCellRenderer(int row, int column) {
     	if (getModel() instanceof TimeSpreadSheetModel)
            return getDefaultRenderer(getRowClass(row));
     	else return super.getCellRenderer(row,column);
     }
     
     public TableCellEditor getCellEditor(int row, int column) {
     	if (getModel() instanceof TimeSpreadSheetModel)
            return getDefaultEditor(getRowClass(row));
     	else return super.getCellEditor(row,column);
     }
    
    

     public void graphicNodesCompositeEvent(CompositeCacheEvent compositeEvent){
    	 super.graphicNodesCompositeEvent(compositeEvent);
    	 setPreferredSize(new Dimension(getPreferredSize().width,getRowHeight()*getRowCount()));
    	 //TODO why is it needed?
     }	
     
     
 	public Component prepareRenderer(TableCellRenderer renderer, int row,
			int column) {
		Component component =  super.prepareRenderer(renderer, row, column);
		if (!getModel().isCellEditable(row, column+1))
			component.setBackground(Colors.LIGHT_GRAY);						
		return component;
	}
	          
     
    
     
}
