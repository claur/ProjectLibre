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
package com.projity.pm.criticalpath;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.projity.datatype.Duration;
import com.projity.grouping.core.Node;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.task.NormalTask;
import com.projity.pm.task.SubProj;
import com.projity.pm.task.Task;


public final class TaskSchedule implements Cloneable {
	public static final int CURRENT = 0;
	public static final int EARLY = -1;
	public static final int LATE = 1;
	
	
	//Persisted fields
	private double percentComplete = 0D;
	
	 // these are calculated, but are persisted anyway for reporting
	private long rawDuration;
	private long start;
	private long finish;
	

	// Calculated fields that are transient
//TODO don't bother serializing these.  When I make them transient, the program hangs
	private Task task;
	private int type;
	private boolean forward = true;
	private long dependencyDate = Dependency.NEEDS_CALCULATION;
	private long remainingDependencyDate = 0;
	
	public TaskSchedule() {
		
	}
	public TaskSchedule(Task task, int type) {
		init(task,type);
		start = 0;
		finish = 0;
	}	
	public void init(Task task, int type) {
		this.task = task;
		this.type = type;
		if (type == EARLY)
			forward = true;
		else if (type == LATE)
			forward = false;
		dependencyDate = Dependency.NEEDS_CALCULATION;
		invalidate();
	}
	
	public void initSerialized(Task task, int type) {
		this.task = task;
		this.type = type;
		if (type == EARLY)
			forward = true;
		else if (type == LATE)
			forward = false;
	}		
	public void setTask(Task task) {
		this.task = task;
	}
	public void serialize(ObjectOutputStream s) throws IOException {
	    s.writeDouble(percentComplete);
	    s.writeLong(rawDuration);
	    s.writeLong(start);
	    s.writeLong(finish);
	}
	
	//call init to complete initialization
	public static TaskSchedule deserialize(ObjectInputStream s) throws IOException, ClassNotFoundException  {
	    TaskSchedule t=new TaskSchedule();
	    t.setPercentComplete(s.readDouble());
	    t.setRawDuration(s.readLong());
	    t.setStart(s.readLong());
	    t.setFinish(s.readLong());
	    return t;
	}
	
	
	
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
	public Object cloneWithTask(Task task) {
		TaskSchedule ts=(TaskSchedule)clone();
		ts.setTask(task);
		ts.invalidate();
		return ts;
	}

	
//
//	void copyDatesFrom(TaskSchedule from) {
//		start = from.start;
//		finish = from.finish;
//		if (task.isWbsParent())
//			rawDuration = from.rawDuration;
//		dependencyDate = from.dependencyDate;
//		remainingDependencyDate = from.remainingDependencyDate;
//	}
	
	public long getBegin() {
		return forward ? start : -finish;
	}
	public long getEnd() {
		return forward ? finish : -start;
	}
	public long getWindowBegin() {
		return forward ? task.getWindowEarlyStart() : -task.getWindowLateFinish();
	}
	public long getWindowEnd() {
		return forward ? task.getWindowEarlyFinish() : -task.getWindowLateStart();
	}
	public void setBegin(long begin) {
		if (forward)
			start = begin;
		else
			finish = -begin;
	}
	public void setEnd(long end) {
		if (forward)
			finish = end;
		else
			start = -end;
	}
	public long getBeginDependency() {
		if (dependencyDate == Dependency.NEEDS_CALCULATION)
			dependencyDate = calcDependencyDate();
			
		return dependencyDate;
	}
	public final void invalidate() {
		if (task != null && task.isSubproject() && !((SubProj)task).isValidAndOpen()) // until they are open, don't touch subprojects
			return;
		start = 0;
		finish = 0;
		dependencyDate = Dependency.NEEDS_CALCULATION;
	}
	public void copyDatesAfterClone(TaskSchedule from) {
		start = from.start;
		finish = from.finish;
		dependencyDate = from.dependencyDate;
	}
	final void invalidateDependencyDate() {
		dependencyDate = Dependency.NEEDS_CALCULATION;
	}
	/**
	 * @return Returns the percentComplete.
	 */
	public final double getPercentComplete() {
		return percentComplete;
	}
	/**
	 * @param percentComplete The percentComplete to set.
	 */
	public final void setPercentComplete(double percentComplete) {
		this.percentComplete = percentComplete;
	}
	/**
	 * @return Returns the rawDuration.
	 */
	public final long getRawDuration() {
		return rawDuration;
	}
	/**
	 * @param rawDuration The rawDuration to set.
	 */
	public final void setRawDuration(long rawDuration) {
		this.rawDuration = rawDuration;
		
	}
		
	private final boolean isLate() {
		return type == LATE;
	}
	public final long getDependencyDate() {
		return dependencyDate;
	}
	public final void setDependencyDate(long dependencyDate) {
		this.dependencyDate = dependencyDate;
	}
	public final long getFinish() {
		return finish;
	}
	public final void setFinish(long finish) {
		this.finish = finish;
	}
	public final long getStart() {
		return start;
	}
	public final void setStart(long start) {
		this.start = start;
	}
	public TaskSchedule getOppositeSchedule() {
		return task.getSchedule(forward ? LATE : EARLY);
	}
	
	/**
	 * @return Returns the forward.
	 */
	public final boolean isForward() {
		return forward;
	}
	public void setForward(boolean forward) {
		if (this.forward != forward) {
			this.forward = forward;
			long s = start;
			start = -finish;
			finish = -s;
			dependencyDate = -dependencyDate;
			remainingDependencyDate = -remainingDependencyDate;
			
		}
	}
	/**
	 * @param begin
	 */
	public final void setRemainingDependencyDate(long remainingDependencyDate) {
		this.remainingDependencyDate = remainingDependencyDate;
	}
	
	/**
	 * @return Returns the remainingDependencyDate.
	 */
	public final long getRemainingDependencyDate() {
		return remainingDependencyDate;
	}
	
/**
 * Calculate a task's dates and see if the critical path changes.  This means either: The task is currently critical, or the task becomes critical.
 * This function is useful in seeing whether a backward pass is necessary.  The backward pass is only necessary when the CP is modified.
 * @param honorRequiredDates
 * @param boundary
 * @return
 */	
	final boolean affectsCriticalPath(CalculationContext context) {
		if (task.isOrWasCritical())
			return true;
		
		calcStartAndFinish(context); // for parents, it will examine all children
		long newEnd = getEnd();
		long oppositeEnd = -getOppositeSchedule().getBegin();
		
//		System.out.println("Affects" + (oppositeEnd < newEnd) + " opposite " + new Date(oppositeEnd) + " new" + new Date(newEnd));
		return (oppositeEnd < newEnd);
	}

	
	final void calcDates(CalculationContext context) {
		long oldBegin = getBegin();
		long oldEnd = getEnd();
		long newBegin = 0L;
		long newEnd=0L;
		final long needsCalculation =Dependency.NEEDS_CALCULATION;
		boolean unopenedSubproject = task.isSubproject() && !((SubProj)task).isValidAndOpen();
		boolean external = task.isExternal();
		if (!external && !unopenedSubproject) {
			if (context.taskReferenceType == PredecessorTaskList.TaskReference.PARENT_END) {
				assignDatesFromChildren(context);
			} else {
				calcStartAndFinish(context); // for parents, it will examine all children
			}
			newBegin = getBegin();
			newEnd = getEnd();
			boolean reverseScheduled = task.isReverseScheduled();
			
			// if not just calculating early dates, check if reverse scheduled
			if (!context.earlyOnly && reverseScheduled) {
				TaskSchedule oppositeSchedule = getOppositeSchedule();
				newBegin = -oppositeSchedule.getEnd();
				newEnd = -oppositeSchedule.getBegin();
			}
			
			if (context.assign && !unopenedSubproject) {
				TaskSchedule currentSchedule = task.getCurrentSchedule();
				if (newBegin < 0) {
					currentSchedule.setStart(-newEnd);
					currentSchedule.setFinish(-newBegin);
					currentSchedule.setRemainingDependencyDate(-remainingDependencyDate);
				} else {
					currentSchedule.setStart(newBegin);
					currentSchedule.setFinish(newEnd);
					currentSchedule.setRemainingDependencyDate(remainingDependencyDate);
	
				}
				currentSchedule.setDependencyDate(dependencyDate);
				// for parents, set current schedule's duration
				if (context.taskReferenceType == PredecessorTaskList.TaskReference.PARENT_END) {
					//TODO this only needs to be done if advancement changed on a task
					currentSchedule.updateDurationFromDates(); // calculate duration based on parent start/end
					((NormalTask)(currentSchedule.task)).assignActualDatesFromChildren();
				}
	//			System.out.println(task.getName() + " Set current " + new Date(currentSchedule.getStart()) + " " + new Date(currentSchedule.getFinish()));
	
			}
		} else if (external) { // external
			TaskSchedule currentSchedule = task.getCurrentSchedule();
			newBegin = currentSchedule.getBegin();
			newEnd = currentSchedule.getEnd();
			oldBegin = 0;
			
		}
		
		if (oldBegin == newBegin && oldEnd == newEnd) {
//			System.out.println("no change");
			if (!unopenedSubproject)
				return;
		}		
		if (unopenedSubproject) { // need to put back old dates because we want the reverse pass to work right
			newBegin = oldBegin;
			newEnd = oldEnd;
		}
		Collection list = task.getDependencyList(!forward);
		Task parent = task.getWbsParentTask();
		TaskSchedule parentSchedule = null;
		long parentEnd = 0;
		if (parent != null) {
			parentSchedule = parent.getSchedule(type);
			parentEnd = parentSchedule.getEnd();
		}
			
		if (context.taskReferenceType == PredecessorTaskList.TaskReference.PARENT_BEGIN) {
			if (oldBegin != newBegin) { // if parent start (finish) changed, then all of its children need to me marked
				
				flagChildren();
				setDependencyDate(newBegin); //This fixes a problem in incorrect propagation of constraints to children hk 16/8/05

				
//				 make sure that in second pass over this, the schedule will change so it will be marked in backward pass.  However, we don't want to lose
//				information - specificially whether this task affects its parent's task.  In case it does, it is marked with a special value (Dependency.NEEDS_CALCULATION)
//				This is a bit of a hack, but it's for optimization purposes.				

				if (parentEnd == oldEnd)
					setEnd(needsCalculation);
				else
					setEnd(0); 
			}
			return;
		}

		Dependency dependency;
		
		if (list.isEmpty()) {
			if (!task.isExternal() && task != context.sentinel) { // When the task is the sentinel, do nothing, otherwise find dependency and update it
				dependency = (Dependency) context.sentinel.getDependencyList(forward).find(forward,task); // find sentinel's dependency concerning this task
				
				if (dependency != null) { // tasks in a subproject won't have a sentinel dependency
					dependency.calcDependencyDate(forward,newBegin,newEnd,false); // calculate it to store off value
					context.sentinel.setCalculationStateCount(context.stateCount); // need to process successor(predecessor) later on in pass
					context.sentinel.getSchedule(type).setDependencyDate(needsCalculation); //sentinel needs dependencies calculated - I assume more than one
				}
			}
		} else {
//			Go Thru Successors (Predecessors) and calculate a dependency date for them and mark them for further treatment.  There is an optimization here:
//			If the successor(pred) task only has one predecessor(succ), then just set its dependency date instead of calculating it.  This avoids reprocessing
//			the predecessor(successor) list of that task later on.  Since in most cases, a task has only one predecessor, this saves time.
			for (Iterator d = list.iterator(); d.hasNext();) {
				Task dependencyTask;
				TaskSchedule dependencyTaskSchedule;
				
				dependency = (Dependency) d.next();
				if (dependency.isDisabled())
					continue;
				dependencyTask = (Task) dependency.getTask(!forward);				// get the successor(pred) task
				dependencyTaskSchedule = dependencyTask.getSchedule(type);
				// if this task is the only predecessor(successor) for the successor(predecessor) task, avoid long calculation and just calculate the date, otherwise
				// flag the task for later calculation
				long dependencyCount = dependencyTask.getDependencyList(forward).size();

				long dep = newBegin; // by default (if no preds for example)
				if (dependencyCount > 0) {
					boolean useSooner = !dependencyTask.isWbsParent() && dependencyTask.hasDuration();
					dep = dependency.calcDependencyDate(forward,newBegin,newEnd,useSooner); // calculate it and store off value
					if (dependencyCount > 1) // can't just set date directly because more than one
						dep = needsCalculation; // it will need to be calculated later
				}
				dependencyTaskSchedule.setDependencyDate(dep);
				dependencyTask.setCalculationStateCount(context.stateCount); // need to process successor(predecessor) later on in pass
			}
		}
		
		// mark parent also if it is affected and isn't marked already
		if (parent != null && parent.getCalculationStateCount() != context.stateCount) {
			long parentBegin = parentSchedule.getBegin();
			if (oldEnd == 0 || oldEnd ==Dependency.NEEDS_CALCULATION) // in case a parent itself modifies its parent or this task has been invalidated (if it is the task modified)
				parent.setCalculationStateCount(context.stateCount); // mark its parent
			else if (	(oldEnd != newEnd && (newEnd > parentEnd  || oldEnd == parentEnd))  // if this task previously determined the parent end date, the parent will need to be recalculated
			  ||	(oldBegin != newBegin && ( newBegin < parentBegin || oldBegin == parentBegin))) { // if this task previously determined parent start date
				parent.setCalculationStateCount(context.stateCount); // mark parent
			}
		}
		if (context.pass == 1) // only mark during first pass.
			task.setCalculationStateCount(context.stateCount+1); // signal that backward pass needs to be done on this
	}

	private void flagChildren() {
		int stateCount = task.getCalculationStateCount();
		Collection children = task.getWbsChildrenNodes();
		if (children == null)
			return;
		Object current;
		Iterator i = children.iterator();
		while (i.hasNext()) {
			current = ((Node) i.next()).getImpl();
			if (! (current instanceof Task))
				continue;
			((Task) current).setCalculationStateCount(stateCount); // mark parent
		}
		
	}
	
	private void updateDurationFromDates() {
		setRawDuration(task.getEffectiveWorkCalendar().compare(getFinish(), getStart(),false));
		
	}
	
	public void assignDatesFromChildren(CalculationContext context) {
		Collection children = task.getWbsChildrenNodes();
		if (children == null)
			return;
		long begin = Long.MAX_VALUE;
		long end = Long.MIN_VALUE;

		Iterator i = children.iterator();
		NormalTask child;
		Object current;
		TaskSchedule childSchedule;
		boolean estimated = false;
		int t = type;
		if (context !=  null && context.pass == 3) 
			t = CURRENT;
//System.out.println("assign from children top ass" + assign + " " + this);		
		while (i.hasNext()) {
			current = ((Node) i.next()).getImpl();
			if (! (current instanceof NormalTask))
				continue;
			
			child = (NormalTask) current;
			estimated |= child.isEstimated();

				

			//			if (context !=  null && context.pass == 3 && child.isReverseScheduled()) {
//				
//				childSchedule = child.getSchedule(-type);
//				System.out.println("reverse " + child + " " + childSchedule);				
//			} else
				childSchedule = child.getSchedule(t);
//			if (assign && child.isReverseScheduled())
//				childSchedule = childSchedule.getOppositeSchedule();

			long childBegin = childSchedule.getBegin();
			
			if (childBegin != 0)
				begin = Math.min(begin,childBegin);
			long childEnd = childSchedule.getEnd();
			if (childEnd != 0)
				end = Math.max(end,childEnd);
		}
		
		if (begin != Long.MAX_VALUE && begin != 0) // in case of invalid subproject having no children
			setBegin(begin);
		else {
			return;
		}
		if (end != Long.MIN_VALUE && end != 0)
			setEnd(end);
		else {
			return;//		System.out.println("begin is " + new Date(begin) + " end " + new Date(end));
		}
		long duration = task.getEffectiveWorkCalendar().compare(end,begin,false);
		duration = Duration.setAsEstimated(duration,estimated);
		((NormalTask)task).setEstimated(estimated);
		setRawDuration(duration);
	}	
	
/**
 * Calculate the date which predecessors(successors) push this task to start by looping thru all of its predecesors(succ) and choosing the max value
 * @return max date
 */
	long calcDependencyDate() {
		long result = 0;
		Dependency dependency;
		long current;
		Collection list = task.getDependencyList(forward);
		for (Iterator i = list.iterator(); i.hasNext();) {
			dependency = (Dependency) i.next();
			if (dependency.isDisabled())
				continue;
			current = dependency.getDate(forward);
			if (result == 0)
				result = current;
			else
				result = Math.max(result,current);
		}
		setDependencyDate(result);
		return result;
	}

	
	/**
	 * During the forward pass, begin is early dates, during backward pass, it is late dates.
	 * When reverse scheduling, the backward pass is executed first, then the forward.
	 * The late schedule uses a trick: dates are returned as negative values.  This lets me to use the same min/max code.  Also
	 * the calendar code knows to reverse durations when the date is negative.
	 * @param boundary 
	 * @param honorRequiredDates
	 * @param early
	 * @return
	 */
	private void calcStartAndFinish(CalculationContext context) {
		long begin = getBeginDependency();
		Task parent = task.getWbsParentTask();
		
		//boolean useSooner = (forward != task.hasDuration()); // shortcut: if forward and is milestone, use sooner, otherwise later.  And conversely, if reverse and isn't milestone, use sooner, othewise later
		boolean useSooner = !task.hasDuration();
		
		if (parent != null) {
			TaskSchedule parentSchedule = parent.getSchedule(type);
			long parentDependency = parentSchedule.getBeginDependency();
			long parentWindow = parentSchedule.getWindowBegin();
			if (parentDependency == 0 || (parentWindow != 0 && parentWindow > parentDependency))
				parentDependency = parentWindow;
			// in case where parent determines start time, make sure that this
			// task starts either at day end if milesetone, or at next working
			// day otherwise if parent is at day end
			if (parentDependency != 0 && (begin == 0 || parentDependency > begin)) {
				begin = task.getEffectiveWorkCalendar().add(parentDependency, 0, useSooner);
			}
		}
		if (task.isInSubproject())
			begin = Math.max(begin,context.forward ? task.getOwningProject().getStartConstraint() : -task.getOwningProject().getEnd());

		// Soft constraints
		long windowBegin = getWindowBegin();
		
		// Make sure the task starts after its early start window date. This
		// is a soft constraint during forward pass.
		
		// For SNET
		if (windowBegin != 0) {
			if (begin == 0)
				begin = windowBegin;
			else if (windowBegin < begin) {
				if (task.startsBeforeProject()) // case of task starting before project start but has SNET constraint
					begin = windowBegin;
			} else
				begin = windowBegin;
		}
		
		// For FNET
		long windowEnd = getWindowEnd();
		if (windowEnd != 0) {
			if (begin == 0)
				begin = Long.MIN_VALUE;
			begin = Math.max(begin, task.calcOffsetFrom(windowEnd,windowEnd,false, false, useSooner));
//			System.out.println("Applying FNET " + task + " " + d(windowEnd) + " begin is now " + d(begin));
		}

		// Hard constraints
		if (context.honorRequiredDates) {
			// If honoring required dates, check the hard constraint that the
			// task is finished by its late finish date.
			// Note that currently late finish has priority over early start.
			long oppositeEnd = -getOppositeSchedule().getWindowBegin(); // For FNLT
			if (oppositeEnd != 0) {
				if (begin == 0)
					begin = Long.MAX_VALUE;
				begin = Math.min(begin, task.calcOffsetFrom(oppositeEnd,dependencyDate,false, false, useSooner));
//				System.out.println("Applying FNLT " + task + " " + d(oppositeEnd) + " begin is now " + d(begin));
			}
			// For SNLT
			long oppositeBegin = -getOppositeSchedule().getWindowEnd();
			if (oppositeBegin != 0) {
				if (begin == 0)
					begin = Long.MAX_VALUE;
				begin = Math.min(begin, oppositeBegin);
//				System.out.println("Applying SNLT " + task + " " + d(oppositeBegin) + " begin is now " + d(begin));
			}
		}
		
		
		if (begin == 0) {
			if (!task.isWbsParent()) // if no constraints at all
				begin = context.boundary;
		}
		if (task.isSubproject()) {// subprojects can't start before their project start
			SubProj subProj = (SubProj)task;
			if (!subProj.isValidAndOpen())
				return;
			if (task.getPredecessorList().size() == 0 && task.getConstraintDate() == 0)
				return;
			begin = Math.max(begin, context.forward ? subProj.getSubproject().getStartConstraint() : -subProj.getSubproject().getEnd());
		}
		
		long levelingDelay = task.getLevelingDelay();
		
		if (Duration.millis(levelingDelay) != 0)
			begin = task.getEffectiveWorkCalendar().add(begin,levelingDelay,useSooner);
		
		long remainingBegin = begin;
		
		if (forward == context.forward)
			setRemainingDependencyDate(remainingBegin);  // the date which predecessors push the task to start at.  Actuals can override this
		
		if (context.forward) {
			long actualStart = task.getActualStart();
			if (actualStart != 0)
				begin = actualStart;
		}
		setBegin(begin); 		
		long end = ((Task)task).calcOffsetFrom(begin,remainingBegin,true, true, true);
		setEnd(end);
		
	}
	
	public void dump() {
		System.out.println("Task " + task + " schedule " + type + " start " + new Date(start) + " finish " + new Date(finish));
	}
	public String toString() { 
		return "Task " + task + " schedule " + type + " begin " + new Date(getBegin()) +
		" end "  +new Date(getEnd()) +
		 " start " + new Date(start) +  " finish " + new Date(finish);
		 
	
	}
	
	/** 
	 * Function used for tracing dates when debugging
	 * @param l date either postive or negative
	 * @return a date using the absolute value of the input
	 */
	public static Date d(long l) {
		return new Date(Math.abs(l));
	}
/**
 * Structure used to store variables related to the pass
 */
	static class CalculationContext {
		int stateCount;
		boolean forward;
		boolean honorRequiredDates;
		Task sentinel;
		int taskReferenceType;
		long boundary;
		boolean earlyOnly;
		boolean assign;
		int scheduleType;
		int pass;
		
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}
}