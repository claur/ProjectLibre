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
package com.projity.reports.adapter;

import java.util.Collection;
import java.util.Iterator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.apache.commons.collections.Predicate;

import com.projity.field.Field;
import com.projity.field.FieldContext;
import com.projity.field.ObjectRef;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.model.NodeModelDataFactory;
import com.projity.grouping.core.model.WalkersNodeModel;
import com.projity.grouping.core.transform.filtering.PredicatedNodeFilterIterator;
import com.projity.pm.task.Project;
import com.projity.pm.time.MutableInterval;

/**
 * A Data Source for Jasper Reports
 */
public class DataSource implements JRDataSource, ObjectRef {

	private WalkersNodeModel nodeModel = null;
	private FieldContext context = null;
	private MutableInterval interval = null;
	private Iterator iterator;
	
	boolean nodeBased = false;
	
	public void setIterator(Iterator iterator) {
		this.iterator = iterator;
	}
	public void setPredicate(Predicate predicate) {
		((PredicatedNodeFilterIterator)iterator).setPredicate(predicate);
	}
	public void setNodeBased(boolean nodeBased) {
		this.nodeBased = nodeBased;
		if (iterator instanceof PredicatedNodeFilterIterator)
			((PredicatedNodeFilterIterator)iterator).setNodeBased(nodeBased);
	}

	public WalkersNodeModel getNodeModel() {
		return nodeModel;
	}
	
	public void setNodeModel(WalkersNodeModel nodeModel) {
		this.nodeModel = nodeModel;
	}

	private boolean isNodeBased() {
		return nodeBased;
	}

	Object currentObject = null;
	
	Project project = null;
	DecoratedField cleanField = null;
	
	public DataSource() {
	}
	
	
	
	public void setTimeBased(boolean timeBased) {
		if (timeBased) {
			// initialize interval treatment
			context = new FieldContext();
			interval = new MutableInterval(0, 0);
			context.setInterval(interval);
		}
		
	}
	
	public void setProject(Project project) {
		this.project = project;
	}
	
	/** (non-Javadoc)
	 * @see net.sf.jasperreports.engine.JRDataSource#next()
	 */
	public boolean next() throws JRException {
		if (!iterator.hasNext()) {
			currentObject = null;
			return false;
		}

		currentObject = iterator.next();
		return true;
	}
	/* (non-Javadoc)
	 * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
	 */
	public Object getFieldValue(JRField jrField) throws JRException {
		// check if asked field must be converted to a String (field name begins whith Text_)
		cleanField = new DecoratedField(jrField);
		
		// get Projity equiv. field
		Field field = cleanField.fieldForReportField();
		if(cleanField.isTimeBased()) {
			setInterval(cleanField.getStart(), cleanField.getEnd());
		}
		Object result;
		if(cleanField.isTextField()) {
			result = field.getText(this, context);
		} else {
			// convert Projity type into jasper accepted types	
			result = DataSourceProvider.fieldValueConverterToPrimitiveType(field,field.getValue(this,context));
		}
		return result;
	}

	public Node getNode() {
		if (isNodeBased()) {
			return (Node)currentObject;
		}
		return null;
	}

	public Object getObject() {
		return currentObject;
	}
	public void setInterval(long start, long end) {
		interval.setStart(start);
		interval.setEnd(end);
	}
	/* (non-Javadoc)
	 * @see com.projity.field.ObjectRef#getCollection()
	 */
	public Collection getCollection() {
		return null;
	}
	public NodeModelDataFactory getDataFactory() {
		// TODO Auto-generated method stub
		return null;
	}




}
