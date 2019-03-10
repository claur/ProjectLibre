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

import java.util.LinkedList;

import com.projectlibre1.pm.graphic.model.cache.NodeModelCache;
import com.projectlibre1.pm.graphic.spreadsheet.common.CommonSpreadSheetModel;
import com.projectlibre1.association.InvalidAssociationException;
import com.projectlibre1.datatype.Duration;
import com.projectlibre1.field.Field;
import com.projectlibre1.field.FieldParseException;
import com.projectlibre1.graphic.configuration.ActionList;
import com.projectlibre1.graphic.configuration.CellStyle;
import com.projectlibre1.grouping.core.Node;
import com.projectlibre1.grouping.core.model.NodeModel;
import com.projectlibre1.pm.dependency.Dependency;
import com.projectlibre1.pm.dependency.DependencyService;
import com.projectlibre1.pm.dependency.DependencyType;
import com.projectlibre1.util.ClassUtils;
import com.projectlibre1.util.Environment;

/**
 * 
 */
public class SpreadSheetModel extends CommonSpreadSheetModel {
	protected boolean readOnly;
	/**
	 * 
	 */
	public SpreadSheetModel(NodeModelCache cache, SpreadSheetColumnModel colModel, CellStyle cellStyle, ActionList actionList) {
		super(cache, colModel, cellStyle, actionList);
	}

	public int getColumnCount() {
		return colModel.getFieldColumnCount();
	}

	public Field getFieldInColumn(int col) {
		return SpreadSheetUtils.getFieldInColumn(col,colModel);
		//return colModel.getFieldInColumn(col);
	}

	public Field getFieldInNonTranslatedColumn(int col) {
		return colModel.getFieldInNonTranslatedColumn(col);
	}

	public Object getValueAt(int row, int col) {
		return SpreadSheetUtils.getValueAt(row,col,getRowMultiple(),cache,colModel,fieldContext);
	}

	public String getColumnName(int col) {
		if (col == 0)
			return "";
		return getFieldInColumn(col).getName();

	}

	public void setValueAt(Object value, int row, int col) {
		if (isReadOnly()) return;
		if (col == 0)
			return;
		Field field=getFieldInColumn(col);
		boolean roleField="Field.userRole".equals(field.getId()); //an exception for roles //TODO get rid of this
		NodeModel nodeModel=getCache().getModel();
		if (!nodeModel.isLocal()&&!nodeModel.isMaster()&&!Environment.getStandAlone()&&!roleField) return;
		
		
		// System.out.println("Field " + getFieldInColumn(col) +
		// "setValueAt("+value+","+row+","+col+")");

		Object oldValue = getValueAt(row, col);
		// if (oldValue==null&&(value==null||"".equals(value))) return;
		if (oldValue == null && ("".equals(value)))
			return;

		Node rowNode = getNodeInRow(row);
		//Field field = getFieldInColumn(col);

		try {
			if (rowNode.isVoid()) {
				if (value == null) { // null means parse error, so generate error here
					getCache().getModel().setFieldValue(field, rowNode, this, value, fieldContext, NodeModel.NORMAL);
				} else{
					//boolean previousIsParent=false;
					LinkedList previousNodes=getPreviousVisibleNodesFromRow(row);
					if (previousNodes!=null){
						Node nextSibling=getNextNonVoidSiblingFromRow(row);
						if(nextSibling!=null&&nextSibling.getParent()==previousNodes.getFirst()) previousNodes=null;
					}
					getCache().getModel()
							.replaceImplAndSetFieldValue(rowNode, previousNodes, getFieldInColumn(col), this, value, fieldContext, NodeModel.NORMAL);
			
				}
			} else if (rowNode.getImpl() instanceof Dependency) { // dependencies
																	// need
																	// specific
																	// handling
																	// at least
																	// for undo
				// TODO this code is a hack and does not belong here.
				Dependency dependency = (Dependency) rowNode.getImpl();
				DependencyService dependencyService = DependencyService.getInstance();
				try {
					Duration duration = (Duration) ((col == 4) ? value : getValueAt(row, 4)); // TODO
																								// can
																								// not
																								// assume
																								// column
																								// positions
					int type = ((Number) DependencyType.mapStringToValue((String) ((col == 3) ? value : getValueAt(row, 3)))).intValue();

					dependencyService.setFields(dependency, duration.getEncodedMillis(), type, this);
					dependencyService.update(dependency, this);
				} catch (InvalidAssociationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else {
				getCache().getModel().setFieldValue(field, rowNode, this, value, fieldContext, NodeModel.NORMAL);
			}
		} catch (FieldParseException e) {
			throw new RuntimeException(e); // exceptions will be treated by the spreadsheet, not the model, because there is a popup.  Because this method doesn't have an exception, a runtime exception will be caught by the spreadsheet
		}
	}

	public boolean isRowEditable(int row) {
		if (isReadOnly()) return false;
		NodeModel nodeModel=getCache().getModel();
		//if (!nodeModel.isLocal()&&!nodeModel.isMaster()&&!Environment.getStandAlone()) return false;
		Node node = getNodeInRow(row);
		if (node.isVoid())
			return true;
		return !ClassUtils.isObjectReadOnly(node.getImpl());
	}
	
	public boolean isCellEditable(int row, int col) {
		if (isReadOnly()) return false;
		if (col == 0)
			return false;
		Field field=getFieldInColumn(col);
		if (field.getLookupTypes() != null)
			return false;
		Node node = getNodeInRow(row);
		NodeModel nodeModel=getCache().getModel();
// 		if (!nodeModel.isLocal()&&!nodeModel.isMaster()&&!Environment.getStandAlone()) return false;
		
		if (node.isVoid()&&!(nodeModel.isLocal()||nodeModel.isMaster())&&"Field.userRole".equals(field.getId()))
			return false;

		if (node.isVoid())
			return true;
		return !field.isReadOnly(node, getCache().getWalkersModel(), null);
	}

	private int findFieldColumn(Field field) {
		return colModel.findFieldColumn(field);
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	


}
