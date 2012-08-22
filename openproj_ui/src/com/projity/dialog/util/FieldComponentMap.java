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
package com.projity.dialog.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.projity.configuration.Configuration;
import com.projity.dialog.FieldDialog;
import com.projity.field.Field;
import com.projity.field.ObjectRef;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelDataFactory;
import com.projity.grouping.core.model.WalkersNodeModel;
import com.projity.help.HelpUtil;
import com.projity.pm.task.BelongsToDocument;
/**
 *
 */
public class FieldComponentMap implements ObjectRef {
	private Object object;
	private Node node = null;
	private NodeModel nodeModel = null;
	private Collection collection = null;
	private HashMap map = new HashMap();
	private FieldDialog fieldDialog;
	private NodeModelDataFactory dataFactory;

	public FieldComponentMap(Object object) {
		this.object = object;
		setDataFactoryFromObject(object);
	}
	
	public FieldComponentMap(Node node, NodeModel nodeModel) {
		this.node = node;
		this.nodeModel = nodeModel;
		dataFactory=nodeModel.getDataFactory();
	}

	public FieldComponentMap(Collection collection) {
		this.collection = collection;
		if (collection!=null&& collection.size()>0){
			setDataFactoryFromObject(collection.iterator().next());
		}
	}
	
	private void setDataFactoryFromObject(Object object){
		if (object instanceof BelongsToDocument) dataFactory=(NodeModelDataFactory)((BelongsToDocument)object).getDocument();
	}
	
	public JComponent getComponent(String fieldId, int flag) {
		JComponent component = (JComponent) map.get(fieldId);
		if (component == null) {
			Field field = Configuration.getFieldFromId(fieldId);
			component = ComponentFactory.componentFor(field,this, flag);
			map.put(fieldId,component);
		}
		return component;
	}
	public String getLabel(String fieldId) {
		Field field = Configuration.getFieldFromId(fieldId);
		return field.getName();
	}
	
	// updates all components
	public void updateAll() {
		Iterator i = map.keySet().iterator();
		String fieldId;
		Field field;
		JComponent component;
		while (i.hasNext()) {
			fieldId = (String)i.next();
			field = Configuration.getFieldFromId(fieldId);
			component = getComponent(fieldId, 0); // argument 0 shouldn't matter because exists already
			ComponentFactory.updateValueOfComponent(component,field,this);
		}
	}
	
	public JComponent append(DefaultFormBuilder builder, String fieldId) {
		return appendField(builder,fieldId,0);
	}

	public void append(DefaultFormBuilder builder, Collection fields) {
		Iterator i = fields.iterator();
		while (i.hasNext()) {
			appendField(builder,((Field)i.next()).getId(),0);
			builder.nextLine(2);
		}
	}

	public JComponent appendReadOnly(DefaultFormBuilder builder, String fieldId) {
		return appendField(builder,fieldId,ComponentFactory.READ_ONLY);
	}

	public JComponent appendSometimesReadOnly(DefaultFormBuilder builder, String fieldId) {
		return appendField(builder,fieldId,ComponentFactory.SOMETIMES_READ_ONLY);
	}

	public JComponent appendField(DefaultFormBuilder builder, String fieldId, int flag) {
		Field field = Configuration.getFieldFromId(fieldId);
		if (field == null)
			return null;
		JComponent component = getComponent(fieldId, flag);
		if (component instanceof JCheckBox) // checkboxes already have a label to the right
			builder.append(component);
		else 
			builder.append(getLabel(fieldId)+":",component);
		String fieldDoc = field.getHelp();
		if (fieldDoc != null)
			HelpUtil.addDocHelp(component,fieldDoc);
		return component;
	}
	
	public JComponent append(DefaultFormBuilder builder, String fieldId, int span) {
		Field field = Configuration.getFieldFromId(fieldId);
		if (field == null)
			return null;
		JComponent component = getComponent(fieldId,0);
		boolean isCheckbox = component instanceof JCheckBox;
		CellConstraints cc = new CellConstraints().xyw(builder.getColumn() + (isCheckbox ? 0 : 2), builder.getRow(), span);
		if (component instanceof JCheckBox) {// checkboxes already have a label to the right
			builder.add(component,cc);
		} else {
			builder.addLabel(getLabel(fieldId)+":");
			builder.nextColumn(2);
			builder.add(component,cc);
			builder.nextColumn(1);
		}
		String fieldDoc = field.getHelp();
		if (fieldDoc != null)
			HelpUtil.addDocHelp(component,fieldDoc);
		return component;
	}
	
	public Node getNode() {
		return node;
	}
	public WalkersNodeModel getNodeModel() {
		return nodeModel;
	}
	public Object getObject() {
		return object;
	}
	public void setObject(Object object) {
		this.object = object;
	}
	/**
	 * @return Returns the collection.
	 */
	public Collection getCollection() {
		return collection;
	}
	/**
	 * @param collection The collection to set.
	 */
	public void setCollection(Collection collection) {
		this.collection = collection;
	}
	
	public void setFieldDialog(FieldDialog fieldDialog) {
		this.fieldDialog = fieldDialog;
	}
	
	public NodeModelDataFactory getDataFactory(){
		return dataFactory;
	}
}
