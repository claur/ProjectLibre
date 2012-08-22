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
package com.projity.pm.assignment;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.TruePredicate;

import com.projity.algorithm.ReverseQuery;
import com.projity.algorithm.TimeIteratorGenerator;
import com.projity.algorithm.buffer.CalculatedValues;
import com.projity.algorithm.buffer.IntervalCallback;
import com.projity.algorithm.buffer.NonGroupedCalculatedValues;
import com.projity.association.AssociationList;
import com.projity.field.FieldContext;
import com.projity.functor.CollectionVisitor;
import com.projity.options.ScheduleOption;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.criticalpath.TaskSchedule;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.scheduling.SchedulingType;
import com.projity.pm.task.NormalTask;
import com.projity.pm.task.Task;
import com.projity.pm.time.HasStartAndEnd;
import com.projity.pm.time.MutableInterval;

/**
 * Implementation of class which contains assignments
 */
public class HasAssignmentsImpl implements HasAssignments, HasTimeDistributedData, Serializable, Cloneable{
	//private static Log log = LogFactory.getLog(HasAssignmentsImpl.class);
	transient AssociationList assignments;

//TODO scheduling rule and effort driven don't make sense for resources, so make them go away?
	int schedulingRule = ScheduleOption.getInstance().getSchedulingRule();
	boolean effortDriven = ScheduleOption.getInstance().isEffortDriven();

	public HasAssignmentsImpl() {
		assignments = new AssociationList();
	}

	public boolean isReadOnlyEffortDriven(FieldContext fieldContext) {
		return getSchedulingType() == SchedulingType.FIXED_WORK;
	}

	/**
	 * Copy constructor: It does a deep copy of assignments
	 * @param from
	 */
	private HasAssignmentsImpl(HasAssignmentsImpl from) {
		this();
		Iterator i = from.assignments.iterator();
		while (i.hasNext()) {
			assignments.add(new Assignment((Assignment)i.next()));
		}
	}
	public HasAssignmentsImpl(Collection details) {
		this();
		Iterator i = details.iterator();
		while (i.hasNext()) {
			assignments.add(new Assignment((AssignmentDetail)i.next()));
		}
	}

	/**
	 * @param schedule
	 * @return
	 */
//	public HasAssignments cloneWithSchedule(TaskSchedule currentSchedule) {
//		return cloneWithSchedule(currentSchedule,null);
//	}
//	public HasAssignments cloneWithSchedule(TaskSchedule currentSchedule,Collection details) {
//		HasAssignmentsImpl newOne;
//		if (details==null) newOne= new HasAssignmentsImpl(this);
//		else newOne= new HasAssignmentsImpl(details);
//		newOne.setScheduleForAssignments(currentSchedule);
//		return newOne;
//	}
	public HasAssignments cloneWithSchedule(TaskSchedule currentSchedule) {
		HasAssignmentsImpl newOne= new HasAssignmentsImpl(this);
		newOne.setScheduleForAssignments(currentSchedule);
		return newOne;
	}

	private void setScheduleForAssignments(TaskSchedule currentSchedule) {
		Iterator i = assignments.iterator();
		Assignment assignment;
		while (i.hasNext()) {
			assignment = (Assignment)i.next();
			assignment.setTaskSchedule(currentSchedule);
			assignment.convertToBaselineAssignment(false);
		}
	}

	//very deep copy of assignments contrary to copy constructor which doesn't clone assigments' detail
	public HasAssignments deepCloneWithTask(Task task) { //TODO doesn't
		HasAssignmentsImpl newOne = (HasAssignmentsImpl)cloneWithTask(task);
		return newOne;
	}
	public Object clone(){
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
	public Object cloneWithTask(Task task){
			HasAssignmentsImpl clone=(HasAssignmentsImpl)clone();
			clone.assignments=new AssociationList();

			//TODO doesn't work when it's copied between projects
			Iterator i = assignments.iterator();
			while (i.hasNext()) {
				clone.assignments.add(((Assignment)i.next()).cloneWithTask(task));
//				clone.assignments.add(((Assignment)i.next()).cloneWithResourceAndTask(ResourceImpl.getUnassignedInstance(),task));
				//break;
			}


//			Iterator i = assignments.iterator();
//			while (i.hasNext()) {
//				clone.assignments.add(((Assignment)i.next()).cloneWithTask(task));
//			}

			return clone;
	}
	public Object cloneWithResource(Resource resource){
		HasAssignmentsImpl clone=(HasAssignmentsImpl)clone();
		clone.assignments=new AssociationList();
		Iterator i = assignments.iterator();
		while (i.hasNext()) {
			clone.assignments.add(((Assignment)i.next()).cloneWithResource(resource));
		}

		return clone;
}


	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#getAssignments()
	 */
	public AssociationList getAssignments() {
		return assignments;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#addAssignment(com.projity.pm.assignment.Assignment)
	 */
	public void addAssignment(Assignment assignment) {
		assignments.add(assignment);
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#removeAssignment(com.projity.pm.assignment.Assignment)
	 */
	public void removeAssignment(Assignment assignment) {
		assignments.remove(assignment);
	}

	/**
	 * Finds an assignment given a resource
	 */
	public Assignment findAssignment(Resource resource) {
		Iterator i = assignments.iterator();
		Assignment result = null;
		while (i.hasNext()) {
			result = (Assignment)i.next();
			if (result.getResource() == resource)
				return result;
		}
		return null;
	}

	/**
	 * Finds an assignment given a task
	 */
	public Assignment findAssignment(Task task) {
		Iterator i = assignments.iterator();
		Assignment result = null;
		while (i.hasNext()) {
			result = (Assignment)i.next();
			if (result.getTask() == task)
				return result;
		}
		return null;
	}



	/**
	 * @return Returns the schedulingRule.
	 */
	public int getSchedulingType() {
		return schedulingRule;
	}

	/**
	 * @param schedulingType The schedulingRule to set.
	 */
	public void setSchedulingType(int schedulingType) {
		this.schedulingRule = schedulingType;
	}

	/**
	 * @return Returns the effortDriven.
	 */
	public boolean isEffortDriven() {
		return effortDriven;
	}

	/**
	 * @param effortDriven The effortDriven to set.
	 */
	public void setEffortDriven(boolean effortDriven) {
		this.effortDriven = effortDriven;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#collectIntervalGenerators(java.lang.Object, java.util.Collection)
	 */
	public void buildReverseQuery(ReverseQuery reverseQuery) {
		Iterator i = assignments.iterator();
		Assignment assignment;
		while (i.hasNext()) {
			assignment = (Assignment) i.next();
			if (assignment.isDefault() && !reverseQuery.isAllowDefaultAssignments())
				continue;
			assignment.buildReverseQuery(reverseQuery);
		}
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#updateAssignment(com.projity.pm.assignment.Assignment)
	 */
	public void updateAssignment(Assignment modified) {
		ListIterator i = assignments.listIterator();
		Assignment current = null;
		while (i.hasNext()) {
			current = (Assignment)i.next();
			if (current.getTask() == modified.getTask() && current.getResource() == modified.getResource()) {
				i.set(modified); // replace current with new one
				break;
			}
		}
	}

	public static Closure forAllAssignments(Closure visitor, Predicate filter) {
		return new CollectionVisitor(visitor,filter) {
			protected final Collection getCollection(Object arg0) {
				return ((HasAssignments)arg0).getAssignments();
			}
		};
	}
	public static Closure forAllAssignments(Closure visitor) {
		return forAllAssignments(visitor,TruePredicate.INSTANCE);
	}


	public void forEachInterval(Closure visitor, Object type, WorkCalendar workCalendar) {
		NonGroupedCalculatedValues calculatedValues = new NonGroupedCalculatedValues(false,0);
		ListIterator i = assignments.listIterator();
		Assignment assignment = null;

		while (i.hasNext()) { // add in all child groups
			assignment = (Assignment)i.next();
			barCallback.setWorkCalendar(assignment.getEffectiveWorkCalendar()); // use this assignments cal because it might work on off calendar time
			assignment.calcDataBetween(type,null,calculatedValues);
		}
		calculatedValues.makeContiguousNonZero(barCallback,workCalendar);
		//calculatedValues.dump();
	}
	public void forEachWorkingInterval(final Closure visitor, boolean mergeWorking, WorkCalendar workCalendar) {
		barCallback.initialize(workCalendar,visitor,true);
		forEachInterval(visitor,ACTUAL_WORK, workCalendar);
/* if the splitting should be at latest bar use this code
		barCallback.finish();
		barCallback.initialize(workCalendar,visitor,false);
*/
		barCallback.initialize(workCalendar,visitor,true);
		forEachInterval(visitor,REMAINING_WORK, workCalendar);
	}

	private static BarSeriesCallback barCallback = new BarSeriesCallback();
	private static class BarSeriesCallback implements IntervalCallback {
		long barStart = 0;
		WorkCalendar workCalendar;
		MutableInterval interval = new MutableInterval(0,0);
		Closure visitor;
		long previousEnd = 0;
		private void executeVisitor(long start, long end) {
			start = Math.max(start,previousEnd); // prevent overlap in case of multiple assignments that do not have same advancement
			if (start > end)
				return;

			interval.setStart(start);
			interval.setEnd(end);

			previousEnd = end;
//System.out.println("bar " + new Date(start) + " " + new Date(end));
			visitor.execute(interval);
			barStart = 0;
		}
		public void setWorkCalendar(WorkCalendar workCalendar) {
			this.workCalendar = workCalendar;

		}
		private static double ALMOST_ZERO = 0.00001;
		public void add(int index, long start, long end, double value) {
			if (value <= ALMOST_ZERO) { // because of rounding errors, treat 0 as something very small
				if (workCalendar.compare(end,start,false) == 0)
					return;
				if (barStart > 0) {
					start = workCalendar.adjustInsideCalendar(start,true);
					executeVisitor(barStart,start);
				}
			} else {
				if (barStart == 0) {
//hk					barStart = start;
					barStart = workCalendar.adjustInsideCalendar(start,false);
				}
				if (index == 0) {// last bar, must draw
					end = workCalendar.adjustInsideCalendar(end,true);
					executeVisitor(barStart,end);
//					System.out.println("last bar " + new Date(start) + " " + new Date(end));
				}
			}
		}

		private void initialize(WorkCalendar workCalendar, Closure visitor, boolean firstTime) {
			if (firstTime)
				previousEnd = 0;
			this.workCalendar = workCalendar;
			this.visitor = visitor;
		}

		private void finish() {
			if (barStart != 0) {
//				System.out.println("finishing bar " + new Date(barStart) + " " + new Date(previousEnd));
				executeVisitor(barStart,previousEnd);
			}
			barStart = 0;
		}
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.EarnedValueValues#acwp(long, long)
	 */
	public double acwp(long start, long end) {
		return TimeDistributedDataConsolidator.acwp(start,end,childrenToRollup());
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.EarnedValueValues#bac(long, long)
	 */
	public double bac(long start, long end) {
		return TimeDistributedDataConsolidator.bac(start,end,childrenToRollup());
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.EarnedValueValues#bcwp(long, long)
	 */
	public double bcwp(long start, long end) {
		return TimeDistributedDataConsolidator.bcwp(start,end,childrenToRollup());
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.EarnedValueValues#bcws(long, long)
	 */
	public double bcws(long start, long end) {
		return TimeDistributedDataConsolidator.bcws(start,end,childrenToRollup());
	}


	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#cost(long, long)
	 */
	public double cost(long start, long end) {
		return TimeDistributedDataConsolidator.cost(start,end,childrenToRollup());
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#cost(long, long)
	 */
	public double baselineCost(long start, long end) {
		return TimeDistributedDataConsolidator.baselineCost(start,end,childrenToRollup());
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#cost(long, long)
	 */
	public long baselineWork(long start, long end) {
		return TimeDistributedDataConsolidator.baselineWork(start,end,childrenToRollup(),true);
	}


	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#actualCost(long, long)
	 */
	public double actualCost(long start, long end) {
		return TimeDistributedDataConsolidator.actualCost(start,end,childrenToRollup());
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#work(long, long)
	 */
	public long work(long start, long end) {
		return TimeDistributedDataConsolidator.work(start,end,childrenToRollup(),true);
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#actualWork(long, long)
	 */
	public long actualWork(long start, long end) {
		return TimeDistributedDataConsolidator.actualWork(start,end,childrenToRollup(),true);
	}

	public long remainingWork(long start, long end) {
		return TimeDistributedDataConsolidator.remainingWork(start,end,childrenToRollup(),true);
	}

	public void calcDataBetween(Object type, TimeIteratorGenerator generator, CalculatedValues values) {
		Iterator i = getAssignments().iterator();
		while (i.hasNext()) {
			((Assignment)i.next()).calcDataBetween(type,generator,values);
		}

	}

    public static List extractOppositeList(List list, boolean leftObject) {
    	Iterator i = list.iterator();
    	ArrayList assignments = new ArrayList();
    	while (i.hasNext()) { // go thru tasks or resources
    		Object object = i.next();
			if (! (object instanceof HasAssignments))
				continue; //TODO currently getting voidNodeImpl's.  This should go away when fixed
			HasAssignments hasAssignments = (HasAssignments)object;
			assignments.addAll(hasAssignments.getAssignments());
		}
		return AssociationList.extractDistinct(assignments,leftObject);
    }

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#childrenToRollup()
	 */
	public Collection childrenToRollup() {
		return assignments;
	}



	/**
	 *
	 */
	private class AssignmentDurationSummer implements Closure {
		private long sum;
		private WorkCalendar workCalendar;
		AssignmentDurationSummer(WorkCalendar workCalendar) {
			this.workCalendar =workCalendar;
			sum = 0;
		}
		public void execute(Object arg0) {
			HasStartAndEnd interval = (HasStartAndEnd)arg0;
			sum += workCalendar.compare(interval.getEnd(), interval.getStart(),false);
		}
		public long getSum() {
			return sum;
		}
	}

	/**
	 * Compute the sum of active assignment durations.  If there are multiple assignments, then
	 * the calendar time of the union of active periods is used, otherwise, if just one assignment
	 * (which could be the default assignment), use the assignment duration
	 * @param workCalendar
	 * @return
	 */
	public long calcActiveAssignmentDuration(WorkCalendar workCalendar) {
		AssociationList assignments =getAssignments();
		// Most of the time there is just one assignment. If that's the case, use the assignment duration
		if (assignments.size() == 1)
			return ((Assignment)assignments.getFirst()).getDurationMillis();
		AssignmentDurationSummer summer = new AssignmentDurationSummer(workCalendar);
		forEachWorkingInterval(summer,false,workCalendar);
		return summer.getSum();

	}


	private void writeObject(ObjectOutputStream s) throws IOException {
	    s.defaultWriteObject();
	}
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException  {
	    s.defaultReadObject();
	    assignments = new AssociationList();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#fixedCost(long, long)
	 */
	public double fixedCost(long start, long end) {
		// TODO Auto-generated method stub
		return 0;
	}
	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#actualFixedCost(long, long)
	 */
	public double actualFixedCost(long start, long end) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#isLabor()
	 */
	public boolean isLabor() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#hasLaborAssignment()
	 */
	public boolean hasLaborAssignment() {
		Iterator i = assignments.iterator();
		while (i.hasNext()) {
			if (((Assignment)i.next()).isLabor())
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#invalidateAssignmentCalendars()
	 */
	public void invalidateAssignmentCalendars() {
		Iterator i = assignments.iterator();
		while (i.hasNext()) {
			((Assignment)i.next()).invalidateAssignmentCalendar();
		}
	}

	public boolean hasActiveAssignment(long start, long end) {
		Iterator i = assignments.iterator();
		Assignment assignment;
		while (i.hasNext()) {
			assignment = (Assignment) i.next();
			if (assignment.isActiveBetween(start, end))
				return true;
		}
		return false;
	}

	public long getEarliestAssignmentStart() {
		long result = Long.MAX_VALUE;
		Iterator i = assignments.iterator();
		while (i.hasNext()) {
			result = Math.min(result,((Assignment)i.next()).getStart());
		}
		return result;
	}

}
