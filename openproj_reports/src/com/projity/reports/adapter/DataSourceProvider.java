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
import java.util.HashMap;
import java.util.Iterator;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataSourceProvider;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRTextElement;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignReportFont;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;

import org.apache.commons.collections.Predicate;

import com.projity.configuration.Configuration;
import com.projity.datatype.Duration;
import com.projity.datatype.Money;
import com.projity.datatype.Rate;
import com.projity.field.Field;
import com.projity.grouping.core.model.WalkersNodeModel;
import com.projity.grouping.core.transform.filtering.PredicatedNodeFilterIterator;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.task.Project;
import com.projity.pm.task.Task;
import com.projity.strings.Messages;
import com.projity.timescale.TimeInterval;
import com.projity.timescale.TimeIterator;

/**
 *
 */
public class DataSourceProvider implements JRDataSourceProvider {
	public static final int PROJECT = 0; // just the current project's fields
	public static final int PROJECTS_TREE = 1;
	public static final int PROJECTS_FLAT = 2;
	public static final int TASKS_TREE = 3;
	public static final int TASKS_FLAT = 4;
	public static final int RESOURCES_TREE = 5;
	public static final int RESOURCES_FLAT = 6;
	public static final int ASSIGNMENTS = 7;
	public static final int PREDECESSORS = 8;
	public static final int SUCCESSORS = 9;
	public static final int TASKS_ASSIGNMENTS_TREE = 10;
	public static final int RESOURCES_ASSIGNMENTS_TREE = 11;
	public static final int TASKS_ASSIGNMENTS_FLAT = 12;
	public static final int RESOURCES_ASSIGNMENTS_FLAT = 13;
	public static final int ASSIGNMENTS_PROJECT_BASED = 14;
	public static final int ASSIGNMENTS_RESOURCE_BASED = 15;
	
	
	public static final String REPORT_VIEW=Messages.getString("View.Report");
	
	public static final String PROJECT_REPORT_VIEW=Messages.getString("View.ProjectReport");
	public static final String TASK_REPORT_VIEW=Messages.getString("View.TaskReport");
	public static final String RESOURCE_REPORT_VIEW=Messages.getString("View.ResourceReport");
	
	public static final String COLLECTION_TYPE_PROPERTY="collectionType";
	public static final String OUTLINE_PROPERTY="outline";
	public static final String TIME_BASED="timeBased";
	private static DataSourceProvider instance = null;
	private JRField[] reportFields = null;
	public static DataSourceProvider getInstance() {
		if (instance == null)
			instance = new DataSourceProvider();
		return instance;
	}
	
	HashMap map = new HashMap();
	
	
	/* (non-Javadoc)
	 * @see net.sf.jasperreports.engine.JRDataSourceProvider#supportsGetFieldsOperation()
	 */
	public boolean supportsGetFieldsOperation() {
		return true;
	}
	private void initFields() {
		Collection allFields = Configuration.getAllFields();
		reportFields = new JRField[allFields.size()];
		int index = 0;
		Iterator i = allFields.iterator();
		JRDesignField newOne;
		Field field;
		JRField f;
		while (i.hasNext()) {
			field = (Field)i.next();
			newOne = new JRDesignField();
			newOne.setName(field.getId());
			newOne.setDescription(field.getName());
			newOne.setValueClass(field.getClazz());
			newOne.setValueClassName(field.getDisplayType().getName()); //TODO what should this be?
			map.put(newOne,field);
		}
	}
	public JRField[] getFields(JasperReport arg0) throws JRException, UnsupportedOperationException {
		if (reportFields == null)
			initFields();
		return reportFields;
	}

	

/**
 * JasperAssistant version
 */
	public JRDataSource create(JasperReport arg0) throws JRException {
		DataSource dataSource = new DataSource();
		return dataSource;
	}
	
	/**
	 * get the view corresponding to a report.  This is used primarily to set filter and sort combos
	 * @param report
	 * @return
	 */
	public static String getViewName(JasperReport report) {
		String collectionType = report.getProperty(COLLECTION_TYPE_PROPERTY);
		int type = Integer.parseInt(collectionType);
		switch (type) {
			case PROJECT: 
				return REPORT_VIEW;
			case PROJECTS_TREE:
			case PROJECTS_FLAT:
				return PROJECT_REPORT_VIEW;
			case TASKS_TREE:
			case TASKS_FLAT:
			case TASKS_ASSIGNMENTS_TREE:
			case TASKS_ASSIGNMENTS_FLAT:
			case ASSIGNMENTS_PROJECT_BASED:
				return TASK_REPORT_VIEW;
			case RESOURCES_TREE:
			case RESOURCES_FLAT:
			case RESOURCES_ASSIGNMENTS_TREE:
			case RESOURCES_ASSIGNMENTS_FLAT:
			case ASSIGNMENTS_RESOURCE_BASED:
				return RESOURCE_REPORT_VIEW;
			default:
				return null;
		}

	}
	
	public static DataSource createDataSource(JasperReport report, Project project, PredicatedNodeFilterIterator cacheIterator, WalkersNodeModel walkersNodeModel) throws JRException {
		DataSource dataSource = new DataSource();
		boolean timeBased = report.getProperty(TIME_BASED) != null;
		String collectionType = report.getProperty(COLLECTION_TYPE_PROPERTY);
		if (collectionType == null)
			throw new JRException("must specify collectionType property in report definition");
		int type = Integer.parseInt(collectionType);
		int outline = 0;
		String outlineNumber = report.getProperty(OUTLINE_PROPERTY);
		if (outlineNumber != null)
			outline = Integer.parseInt(outlineNumber);
		
		
		Predicate predicate = null;
		boolean tree = false;
		switch (type) {
			case PROJECT: 
				break;
			case PROJECTS_TREE:
				predicate = Project.instanceofPredicate();
				tree = true;
				break;
			case PROJECTS_FLAT:
				predicate = Project.instanceofPredicate();
				break;
			case TASKS_TREE:
				predicate = Task.instanceofPredicate();
				tree = true;
				break;
			case TASKS_FLAT:
				predicate = Task.instanceofPredicate();
				break;
			case RESOURCES_TREE:
				predicate = ResourceImpl.instanceofPredicate();
				tree = true;
				break;
			case RESOURCES_FLAT:
				predicate = ResourceImpl.instanceofPredicate();
				break;
			case TASKS_ASSIGNMENTS_TREE:
				tree = true;
				break;
			case RESOURCES_ASSIGNMENTS_TREE:
				tree = true;
				break;
			case TASKS_ASSIGNMENTS_FLAT:
				break;
			case RESOURCES_ASSIGNMENTS_FLAT:
				break;
			case ASSIGNMENTS_PROJECT_BASED:
				predicate = Assignment.instanceofPredicate();
				break;
			case ASSIGNMENTS_RESOURCE_BASED:
				predicate = Assignment.instanceofPredicate();
				break;
			case ASSIGNMENTS:
			case PREDECESSORS:
			case SUCCESSORS:
				throw new JRException("Supreport collection type " + type + " not yet implemented");
		}
		dataSource.setTimeBased(timeBased);
		dataSource.setProject(project);
		dataSource.setIterator(cacheIterator);
		dataSource.setNodeModel(walkersNodeModel);
		dataSource.setNodeBased(tree);
		dataSource.setPredicate(predicate);
		return dataSource;
	}
	
//	private static NodeModel getResourceModel(Project project, int outlineNumber) {
//		NodeModel resourceModel = project.getResourcePool().getResourceOutline(outlineNumber);
//		if (resourceModel instanceof AssignmentNodeModel) {
//			((AssignmentNodeModel)resourceModel).addAssignments();
//		}
//		return resourceModel;
//	}

	/* (non-Javadoc)
	 * @see net.sf.jasperreports.engine.JRDataSourceProvider#dispose(net.sf.jasperreports.engine.JRDataSource)
	 */
	public void dispose(JRDataSource arg0) throws JRException {
		map.clear();
	}

	public static Object fieldValueConverterToPrimitiveType(Field field,Object fieldValue) {
		if (fieldValue == null)
			return null;
		if (field.isRate()) {
			return new Double(((Rate)fieldValue).getValue());
		} else if(field.isMoney()) {
			return new Double(((Money)fieldValue).doubleValue());
		} else if (field.isDurationOrWork()) {
			return new Long (((Duration)fieldValue).longValue());
		} else {
			return fieldValue;
		}
	}
	
	public static JasperDesign addTimescale(JasperDesign design, TimeIterator iterator, Class fieldType) {
		
		// extract field name
		String baseFieldName = design.getProperty("timeBasedField");
		
		// get column header start position
		JRBand pageHeader = design.getPageHeader();
		JRElement[] elements = pageHeader.getElements();
		int maxX = 0;
		int maxY = 0;
		for(int i = 0; i < elements.length; i++)
		{
			maxX = (elements[i].getX() + elements[i].getWidth() > maxX)?(elements[i].getX() + elements[i].getWidth()):maxX;
			maxY = (elements[i].getY() > maxY)?elements[i].getY():maxY;
		}

		JRDesignReportFont normalFont = new JRDesignReportFont();
		normalFont.setFontName("Arial");
		normalFont.setSize(10);
		normalFont.setPdfFontName("Helvetica");
		
		
		try {
			while(iterator.hasNext()) {
				
				// build field name from start & end
				TimeInterval interval = iterator.next();
				String fieldName = "TIME";
				fieldName += interval.getStart();
				fieldName += "_";
				fieldName += interval.getEnd();
				fieldName += "_" + baseFieldName;
				System.out.println("time based field is " + fieldName);
				// add fields
				JRDesignField field = new JRDesignField();
				field.setName(fieldName);
				field.setValueClass(fieldType);
				design.addField(field);
			
				System.out.println("column header is " + interval.getText2());
				
				// add columns
				JRDesignBand columnHeader = (JRDesignBand) design.getColumnHeader();
	
				JRDesignStaticText staticText = new JRDesignStaticText();
				staticText.setX(maxX);
				staticText.setY(0);
				staticText.setWidth(80);
				staticText.setHeight(15);
				staticText.setTextAlignment(JRTextElement.TEXT_ALIGN_RIGHT);
				staticText.setFont(normalFont);
				staticText.setText(interval.getText1());
				staticText.setPrintWhenDetailOverflows(true);
				columnHeader.addElement(staticText);

				// add textFields
				JRDesignBand detailBand = (JRDesignBand) design.getDetail();
	
				JRDesignTextField textField = new JRDesignTextField();
				textField.setX(maxX);
				textField.setY(0);
				textField.setWidth(80);
				textField.setHeight(15);
				textField.setTextAlignment(JRTextElement.TEXT_ALIGN_RIGHT);
				textField.setFont(normalFont);
	//			textField.setFont((JRReportFont)fonts.get("normalFont"));
				JRDesignExpression expression = new JRDesignExpression();
				expression.setValueClass(fieldType);
				expression.setText("$F{" + fieldName + "}");
				textField.setExpression(expression);
				textField.setPrintWhenDetailOverflows(true);
				detailBand.addElement(textField);
				maxX += 80;
			}

		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return design;
      
//        detailBand.addElement();
        
		
	}
}
