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
package com.projity.pm.assignment;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.projity.configuration.Configuration;
import com.projity.datatype.CanSupplyRateUnit;
import com.projity.datatype.Rate;
import com.projity.datatype.TimeUnit;
import com.projity.document.Document;
import com.projity.field.Field;
import com.projity.pm.key.HasKey;
import com.projity.pm.task.BelongsToDocument;
import com.projity.pm.task.Task;
import com.projity.util.ClassUtils;

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
	 * @see com.projity.pm.task.BelongsToDocument#getDocument()
	 */
	public Document getDocument() {
		return document;
	}


	/* (non-Javadoc)
	 * @see com.projity.datatype.CanSupplyRateUnit#getTimeUnit()
	 */
	public String getTimeUnitLabel() {
		return ((CanSupplyRateUnit)resource).getTimeUnitLabel();
	}
	
	public boolean isMaterial() {
		return ((CanSupplyRateUnit)resource).isMaterial();
	}
	

	
}
