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
package com.projectlibre1.pm.assignment;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.projectlibre1.configuration.Configuration;
import com.projectlibre1.datatype.CanSupplyRateUnit;
import com.projectlibre1.datatype.Rate;
import com.projectlibre1.datatype.TimeUnit;
import com.projectlibre1.document.Document;
import com.projectlibre1.field.Field;
import com.projectlibre1.pm.key.HasKey;
import com.projectlibre1.pm.task.BelongsToDocument;
import com.projectlibre1.pm.task.Task;
import com.projectlibre1.util.ClassUtils;

/**
 * Used to enter new assignments in dialog
 */
public class AssignmentEntry implements HasRequestDemandType, BelongsToDocument,CanSupplyRateUnit {
	HasAssignments resource;
	ArrayList assignments;
	Document document;

	private static Field rateFieldInstance = null;
	public static Field getRateField() {
		if (rateFieldInstance == null)
			rateFieldInstance = Configuration.getFieldFromId("Field.assignmentEntryRate");
		return rateFieldInstance;
	}


	/**
	 * 
	 */
	public AssignmentEntry(HasAssignments resource, ArrayList assignments, Document document) {
		this.resource = resource;
		this.assignments = assignments;
		this.document = document;
	}
	
	public String getName() {
		return ((HasKey) resource).getName();
	}
	
	public void setRequestDemandType(int requestDemandType) {
		if (!isAssigned()) //requestDemand type only settable if already assigned 
			return;
		Iterator i = assignments.iterator();
		Assignment assignment;
		while (i.hasNext()) {
			assignment = (Assignment)i.next();
			assignment.setRequestDemandType(requestDemandType);
		}
	}

	public int getRequestDemandType() {
		Integer commonRequestDemandType = (Integer)Assignment.getRequestDemandTypeField().getCommonValue(assignments,false,false);
		if (commonRequestDemandType == null)
			return RequestDemandType.NONE;
		else
			return commonRequestDemandType.intValue();
	}
	
	private boolean isAssignmentListEmpty() {
		return (assignments == null) || assignments.size() == 0;
	}
	
	public Rate getRate() {
		Rate commonValue = (Rate) Assignment.getRateField().getCommonValue(assignments,false,false);
		if (commonValue == null) {
			if (isAssignmentListEmpty()) {
				if (getResource().isLabor())
					return ClassUtils.defaultRate;
				else
					return ClassUtils.defaultUnitlessRate;
			} else
				return ClassUtils.RATE_MULTIPLE_VALUES;
		}
		else {
			return commonValue;
		}
	}
	
	public void setRate(Rate rate) throws ParseException {
		Iterator i = assignments.iterator();
		Assignment assignment;
		int timeUnit = rate.getTimeUnit();
		while (i.hasNext()) {
			assignment = (Assignment)i.next();
			if (timeUnit != TimeUnit.NON_TEMPORAL)
				if (assignment.isLabor()) {
					assignment.adjustRemainingUnits(rate.getValue(), 0, true, false);
					assignment.forceUnits(rate.getValue());
				} else
					assignment.setRate(rate);
			else
				assignment.forceUnits(rate.getValue());
			assignment.setRateUnit(timeUnit);
			Assignment.getRateField().fireEvent(assignment, this,null); // update assignment rows
		}
	}
	
	public boolean isAssigned() {
		return assignments != null && assignments.size() > 0;
	}

	public int getAssignmentCount() {
		if (assignments == null)
			return 0;
		return assignments.size();
	}
	
	private void addAssignment(Assignment assignment) {
		if (assignments == null)
			assignments = new ArrayList();
		assignments.add(assignment);
	}
	
	/**
	 * Given a list of tasks, the assignments list will be filled to contain those assignments which
	 * refer to a task in the taskList
	 * @param taskList
	 */
	public void setAssignmentsFromTaskList(List taskList) {
		assignments = null;
		Iterator t = taskList.iterator();
		Task task;
		Object current;
		while (t.hasNext()) {
			current = t.next();
			if (!(current instanceof Task))
				continue;
			task = (Task)current;
			Assignment assignment = resource.findAssignment(task);
			if (assignment != null)
				addAssignment(assignment);
		}
		
	}
	
	/**
	 * @return Returns the resource.
	 */
	public HasAssignments getResource() {
		return resource;
	}


	/* (non-Javadoc)
	 * @see com.projectlibre1.pm.task.BelongsToDocument#getDocument()
	 */
	public Document getDocument() {
		return document;
	}


	/* (non-Javadoc)
	 * @see com.projectlibre1.datatype.CanSupplyRateUnit#getTimeUnit()
	 */
	public String getTimeUnitLabel() {
		return ((CanSupplyRateUnit)resource).getTimeUnitLabel();
	}
	
	public boolean isMaterial() {
		return ((CanSupplyRateUnit)resource).isMaterial();
	}
	

	
}
