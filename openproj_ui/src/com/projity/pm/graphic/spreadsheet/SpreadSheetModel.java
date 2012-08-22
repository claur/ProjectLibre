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
package com.projity.pm.graphic.spreadsheet;

import java.util.LinkedList;

import com.projity.association.InvalidAssociationException;
import com.projity.datatype.Duration;
import com.projity.field.Field;
import com.projity.field.FieldParseException;
import com.projity.graphic.configuration.ActionList;
import com.projity.graphic.configuration.CellStyle;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.model.NodeModel;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.dependency.DependencyService;
import com.projity.pm.dependency.DependencyType;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.spreadsheet.common.CommonSpreadSheetModel;
import com.projity.util.ClassUtils;
import com.projity.util.Environment;

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