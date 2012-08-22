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
package com.projity.pm.task;



import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.apache.commons.collections.Closure;

import com.projity.algorithm.ReverseQuery;
import com.projity.algorithm.TimeIteratorGenerator;
import com.projity.algorithm.buffer.CalculatedValues;
import com.projity.association.AssociationList;
import com.projity.field.FieldContext;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.HasAssignments;
import com.projity.pm.assignment.HasAssignmentsImpl;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.costing.Accrual;
import com.projity.pm.criticalpath.TaskSchedule;
import com.projity.pm.resource.Resource;
import com.projity.pm.snapshot.DataSnapshot;
/**
 *
 */
public class TaskSnapshot implements DataSnapshot, HasAssignments, Cloneable {
	TaskSchedule currentSchedule;
	HasAssignments hasAssignments = null;
	double fixedCost = 0;
	int fixedCostAccrual = Accrual.END;
	boolean ignoreResourceCalendar = false;


	public long getEarliestAssignmentStart() {
		return hasAssignments.getEarliestAssignmentStart();
	}

	public boolean hasActiveAssignment(long start, long end) {
		return hasAssignments.hasActiveAssignment(start, end);
	}

	/**
	 * @param modified
	 */
	public void updateAssignment(Assignment modified) {
		hasAssignments.updateAssignment(modified);
	}

	/**
	 * @return Returns the taskSchedule.
	 */
	public TaskSchedule getCurrentSchedule() {
		return currentSchedule;
	}
	/**
	 * @param currentSchedule The taskSchedule to set.
	 */
	public void setCurrentSchedule(TaskSchedule currentSchedule) {
		this.currentSchedule = currentSchedule;
	}
	public HasAssignments getHasAssignments() {
		if (hasAssignments == null) // lazy instantiation
			hasAssignments = new HasAssignmentsImpl();
		return hasAssignments;
	}
	/**
	 * 
	 */
	public TaskSnapshot() {
	}


	public TaskSnapshot(Collection details) {
		hasAssignments=new HasAssignmentsImpl(details);
	}


	public Object clone() {
		TaskSnapshot newOne = null;
		try {
			newOne = (TaskSnapshot) super.clone();
			newOne.currentSchedule = (TaskSchedule) currentSchedule.clone();
			newOne.hasAssignments = (HasAssignments) ((HasAssignmentsImpl)hasAssignments).cloneWithSchedule(newOne.currentSchedule);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		return newOne;
	}
	public Object deepCloneWithTask(Task task) {
		
		TaskSnapshot newOne = null;
		try {
			newOne = (TaskSnapshot) super.clone();
			newOne.currentSchedule = (TaskSchedule) currentSchedule.cloneWithTask(task);
			newOne.hasAssignments = (HasAssignments) ((HasAssignmentsImpl)hasAssignments).deepCloneWithTask(task);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		return newOne;
	}
	/**
	 * @param assignment
	 */
	public void addAssignment(Assignment assignment) {
		getHasAssignments().addAssignment(assignment);
	}

	/**
	 * @param resource
	 * @return
	 */
	public Assignment findAssignment(Resource resource) {
		return getHasAssignments().findAssignment(resource);
	}

	/**
	 * @param task
	 * @return
	 */
	public Assignment findAssignment(Task task) {
		return getHasAssignments().findAssignment(task);
	}

	/**
	 * @param assignment
	 */
	public void removeAssignment(Assignment assignment) {
		getHasAssignments().removeAssignment(assignment);
	}


	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#getAssignments()
	 */
	public AssociationList getAssignments() {
		return getHasAssignments().getAssignments();
	}

	/**
	 * @return
	 */
	public int getSchedulingType() {
		return getHasAssignments().getSchedulingType();
	}

	/**
	 * @param schedulingType
	 */
	public void setSchedulingType(int schedulingType) {
		getHasAssignments().setSchedulingType(schedulingType);
	}

	/**
	 * @return
	 */
	public boolean isEffortDriven() {
		return getHasAssignments().isEffortDriven();
	}

	/**
	 * @param effortDriven
	 */
	public void setEffortDriven(boolean effortDriven) {
		getHasAssignments().setEffortDriven(effortDriven);
	}


	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#buildComplexQuery(com.projity.algorithm.ComplexQuery)
	 */
	public void buildReverseQuery(ReverseQuery reverseQuery) {
		getHasAssignments().buildReverseQuery(reverseQuery);
	}


	/**
	 * @param visitor
	 * @return
	 */
	public static Closure forAllAssignments(Closure visitor) {
		return HasAssignmentsImpl.forAllAssignments(visitor);
	}
	/**
	 * @param visitor
	 * @param mergeWorking
	 */
	public void forEachWorkingInterval(Closure visitor, boolean mergeWorking, WorkCalendar workCalendar) {
		hasAssignments.forEachWorkingInterval(visitor, mergeWorking, workCalendar);
	}
	/**
	 * @return
	 */
	public boolean isReadOnlyEffortDriven(FieldContext fieldContext) {
		return hasAssignments.isReadOnlyEffortDriven(fieldContext);
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public double actualCost(long start, long end) {
		return hasAssignments.actualCost(start, end);
	}
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public long actualWork(long start, long end) {
		return hasAssignments.actualWork(start, end);
	}
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public long remainingWork(long start, long end) {
		return hasAssignments.remainingWork(start, end);
	}	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public double acwp(long start, long end) {
		return hasAssignments.acwp(start, end);
	}
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public double bac(long start, long end) {
		return hasAssignments.bac(start, end);
	}
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public double bcwp(long start, long end) {
		return hasAssignments.bcwp(start, end);
	}
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public double bcws(long start, long end) {
		return hasAssignments.bcws(start, end);
	}
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public double cost(long start, long end) {
		return hasAssignments.cost(start, end);
	}
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public long work(long start, long end) {
		return hasAssignments.work(start, end);
	}
	/**
	 * @param type
	 * @param generator
	 * @param values
	 */
	public void calcDataBetween(Object type, TimeIteratorGenerator generator,
			CalculatedValues values) {
		hasAssignments.calcDataBetween(type, generator, values);
	}
	/**
	 * @return
	 */
	public Collection childrenToRollup() {
		return hasAssignments.childrenToRollup();
	}
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public double baselineCost(long start, long end) {
		return hasAssignments.baselineCost(start, end);
	}
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public long baselineWork(long start, long end) {
		return hasAssignments.baselineWork(start, end);
	}
	/**
	 * @param workCalendar
	 * @return
	 */
	public long calcActiveAssignmentDuration(WorkCalendar workCalendar) {
		return hasAssignments.calcActiveAssignmentDuration(workCalendar);
	}

	public double fixedCost(long start, long end) {
		return 0;
	}
	public double actualFixedCost(long start, long end) {
		return 0;
	}
	/**
	 * @return Returns the fixedCost.
	 */
	public final double getFixedCost() {
		return fixedCost;
	}
	/**
	 * @param fixedCost The fixedCost to set.
	 */
	public final void setFixedCost(double fixedCost) {
		this.fixedCost = fixedCost;
	}
	/**
	 * @return Returns the fixedCostAccrual.
	 */
	public final int getFixedCostAccrual() {
		return fixedCostAccrual;
	}
	/**
	 * @param fixedCostAccrual The fixedCostAccrual to set.
	 */
	public final void setFixedCostAccrual(int fixedCostAccrual) {
		this.fixedCostAccrual = fixedCostAccrual;
	}
	/**
	 * @return Returns the ignoreResourceCalendar.
	 */
	public final boolean isIgnoreResourceCalendar() {
		return ignoreResourceCalendar;
	}
	/**
	 * @param ignoreResourceCalendar The ignoreResourceCalendar to set.
	 */
	public final void setIgnoreResourceCalendar(boolean ignoreResourceCalendar) {
		this.ignoreResourceCalendar = ignoreResourceCalendar;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#isLabor()
	 */
	public boolean isLabor() {
		return true;
	}
	public boolean hasLaborAssignment() {
		return hasAssignments.hasLaborAssignment();
	}
	public void invalidateAssignmentCalendars() {
		hasAssignments.invalidateAssignmentCalendars();
	}
	
	
	
	public void serialize(ObjectOutputStream s) throws IOException {
		currentSchedule.serialize(s);
		//s.writeObject(hasAssignments);
		s.writeDouble(fixedCost);
		s.writeInt(fixedCostAccrual);
		s.writeBoolean(ignoreResourceCalendar);
	    s.writeInt(hasAssignments.getSchedulingType());
	    s.writeBoolean(hasAssignments.isEffortDriven());
	}
	
	//call init to complete initialization
	public static TaskSnapshot deserialize(ObjectInputStream s,NormalTask hasAssignments) throws IOException, ClassNotFoundException  {
	    TaskSnapshot t=new TaskSnapshot();
	    TaskSchedule schedule=TaskSchedule.deserialize(s);
	    schedule.setTask(hasAssignments);
	    t.setCurrentSchedule(schedule);
	    t.hasAssignments=new HasAssignmentsImpl();//(HasAssignments)s.readObject();
	    
	    t.setFixedCost(s.readDouble());
	    t.setFixedCostAccrual(s.readInt());
	    t.setIgnoreResourceCalendar(s.readBoolean());
	   
	    if (hasAssignments.getVersion()>=2){
	    	t.hasAssignments.setSchedulingType(s.readInt());
	    	t.hasAssignments.setEffortDriven(s.readBoolean());
	    }
	    return t;
	}

}
