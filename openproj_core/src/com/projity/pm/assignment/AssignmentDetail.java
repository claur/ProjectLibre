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



import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;

import com.projity.configuration.CircularDependencyException;
import com.projity.datatype.Duration;
import com.projity.datatype.Rate;
import com.projity.datatype.TimeUnit;
import com.projity.document.Document;
import com.projity.functor.IntervalConsumer;
import com.projity.options.CalendarOption;
import com.projity.pm.assignment.contour.AbstractContour;
import com.projity.pm.assignment.contour.AbstractContourBucket;
import com.projity.pm.assignment.contour.ContourTypes;
import com.projity.pm.assignment.contour.PersonalContour;
import com.projity.pm.assignment.contour.StandardContour;
import com.projity.pm.calendar.CalendarService;
import com.projity.pm.calendar.HasCalendar;
import com.projity.pm.calendar.InvalidCalendarIntersectionException;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.costing.CostRateTables;
import com.projity.pm.criticalpath.TaskSchedule;
import com.projity.pm.resource.Resource;
import com.projity.pm.scheduling.Delayable;
import com.projity.pm.scheduling.DelayableImpl;
import com.projity.pm.scheduling.Schedule;
import com.projity.pm.scheduling.ScheduleInterval;
import com.projity.pm.scheduling.ScheduleUtil;
import com.projity.pm.task.NormalTask;
import com.projity.pm.task.Task;
import com.projity.pm.time.MutableInterval;
import com.projity.strings.Messages;
import com.projity.util.Alert;
import com.projity.util.DateTime;
/**
 * Class representing the non-scheduling part of resource assignments.  It is immutable
 * @stereotype thing 
 */
public final class AssignmentDetail implements Schedule, HasCalendar, Cloneable, Serializable, ContourTypes {// , Schedule {
	static final long serialVersionUID = 867792734923243L;
	transient Rate rate = new Rate(1.0D,TimeUnit.PERCENT); // units can never be 0!!!
 	double percentComplete = 0;
 	long duration = 0;
	//TODO There is some stuff to figure out regarding contouring remaining work.  If you change the contour, Project applies
	// the entire contour to the remaining work.  If you keep completing and change the contour again, project keeps the
	// different contours active to where they were applied.  The weird thing is that if you uncomplete the task, the work still
	// shows the various contours, even if the current contour is flat.  This is a bug.
 	
 	
	private int workContourType; // these are only used for serializing the contour.  They are not "up to date" otherwise
	private int costContourType;
 	private transient AbstractContour workContour = StandardContour.FLAT_CONTOUR;
	private transient AbstractContour costContour = StandardContour.FLAT_CONTOUR;

	private WorkingCalendar actualExceptionsCalendar = null; // for when user enters actual work during non-work time of calendar
	private transient WorkingCalendar intersectionCalendar = null;
	private transient TaskSchedule taskSchedule = null;

	private transient Resource resource;
	private transient Task task;
	private transient Delayable delayable;
	private int requestDemandType = RequestDemandType.NONE;
	private int costRateIndex = CostRateTables.DEFAULT;
	private long overtimeWork = 0; // allowed overtime that is evenly distributed across contour
	
	private WorkingCalendar baselineCalendar = null; // only applies if this is a baseline
	/**
	 * @return Returns the percentComplete.
	 */
	public double getPercentComplete() {
		return percentComplete;
	}
	/**
	 * @param percentComplete The percentComplete to set.
	 */
	public void setPercentComplete(double percentComplete) {
		this.percentComplete = percentComplete;
		if (getTask() != null) // in case of called from web
			((NormalTask)getTask()).adjustActualStartFromAssignments();
	}
	/**
	 * @return Returns the duration.
	 */
	public long getDuration() {
		return duration;
	}
	
	void recalculateDuration() {
		duration = workContour.calcTotalBucketDuration(0);
	}
	/**
	 * @param taskSchedule The taskSchedule to set.
	 */
	void setTaskSchedule(TaskSchedule taskSchedule) {
		this.taskSchedule = taskSchedule;
	}
/**
 * Set just the units and the resource.  This is the case when replacing a resource with another,
 * such as the default resource assignment being replaced with a true one
 * @param units
 * @param resource
 */	
	void replaceResourceAndUnits(double units, Resource resource) {
		this.resource = resource;
		setUnits(units);
	}
	
	/**
	 * Construct an assignment.  The arguments are those that are presented in the assign resource dialog
	 * @param task
	 * @param resource
	 * @param units
	 * @param requestDemandType  -Normally empty
	 */	
	AssignmentDetail(Task task,
					Resource resource,
					double units,
					int requestDemandType,
					long delay) {
		this.task = task;
		this.resource = resource;
		setUnits(units);
		this.requestDemandType = requestDemandType;
		this.delayable = new DelayableImpl(delay,0);
	}
	
	private AssignmentDetail() { // used in cloning
		
	}
	/**
	 * Accessor for units (value of work)
	 * @return units
	 */
	double getUnits() {
		return rate.getValue();
	}
	
	void setUnits(double units) {
		rate.setValue(units);
	}

	/**
	 * Calculate the overtime value from the overtime work field.  MSProject distributes
	 * overtime uniformly over the working days of the assignment.
	 * @return overtime value 
	 */
	double calcOvertimeUnits() {
		long workingDuration = calcWorkingDuration(); //TODO this should be stored
		if (workingDuration == 0) // take care of degenerate case
			return 0.0;
		return ((double)overtimeWork) / workingDuration;
	}

	void setOvertimeWork(long overtimeWork) {
		this.overtimeWork = overtimeWork;
	}
	
	 
	/**
	 * Calculate the total work by multiplying units by the calculated duration
	 * @return total work.
	*/
	long calcWork() {
		//hk
		return (long) (getUnits() * calcWorkingDuration());
	}

	public String toString() {
		return super.toString();
//		return "[start] " + new java.util.Date(getStart())
//		      + "\n[end] " +  new java.util.Date(getEnd())
//			  +"\n[units] " + getUnits()// in hours
//			  +"\n[work] " + getWork() / (1000*60*60); // in hours
	}

	/**
	 * @return Returns the contour.
	 */
	AbstractContour getWorkContour() {
		return workContour;
	}

	/**
	 * @param contour The contour to set.
	 * TODO get rid of this
	 */
	public void debugSetWorkContour(AbstractContour contour) {
		this.workContour = contour;
	}

	/**
	 * Accessor for the assignment's delay
	 * @return delay
	 */
	public long getDelay(){
		return delayable.getDelay();
	}

	void setDelay(long delay) {
//		if (delay > 0)
//			System.out.println("delay " + new java.util.Date(getStart()) + new java.util.Date(getTaskStart()));

		delayable = new DelayableImpl(delay,delayable.getLevelingDelay());
	}
	
	public long getLevelingDelay(){
		return delayable.getLevelingDelay();
	}

	void setLevelingDelay(long levelingDelay){
		delayable = new DelayableImpl(delayable.getDelay(),levelingDelay);
	}

	public long calcTotalDelay() {
		return delayable.calcTotalDelay();
	}


	void setContour(Object type, Collection bucketList) {
		AbstractContourBucket[] contour = new AbstractContourBucket[bucketList.size()];
		bucketList.toArray(contour);
		setContour(type,contour);
		
	}
	void setContour(Object type, AbstractContourBucket[] buckets) {
		if (type == HasTimeDistributedData.WORK) {
			workContour  = PersonalContour.getInstance(buckets);
		} else if (type == HasTimeDistributedData.COST) {
			costContour  = PersonalContour.getInstance(buckets);
		}
	}
	
	/**
	 * @return Returns the requestDemandType.
	 */
	public int getRequestDemandType() {
		return requestDemandType;
	}
	
	void setRequestDemandType(int requestDemandType) {
		this.requestDemandType = requestDemandType;

	}
	void setWorkContour(AbstractContour contour) {
		this.workContour = contour;
	}
	/**
	 * @param duration The duration to set.
	 */
	void adjustRemainingDuration(long newRemainingDuration) {
		if (newRemainingDuration < 0)
			newRemainingDuration = 0; // just in case
		if (getUnits() == 0) // take care of degenerate case
			newRemainingDuration = 0;

		long actualDuration = getActualDuration();
		long d = newRemainingDuration + actualDuration;
		if (actualDuration > 0 && !workContour.isPersonal()) // because the remaining might not have same contour
			workContour = PersonalContour.makePersonal(workContour,getDuration()); //use previous duration
		workContour = workContour.adjustDuration(d, actualDuration); // allow a personal contour to adjust itself
		d = workContour.calcTotalBucketDuration(d); // it is possible that the contour is shorter because we have eliminated a bucket at the end which is after empty time.  The empty time must be removed too
		setDuration(d);
	}

	/**
	 * @param units The units to set.
	 */
	void adjustRemainingUnits(double newUnits) {
		workContour = workContour.adjustUnits(newUnits/getUnits(), getActualDuration()); // allow a personal contour to adjust itself
		setUnits(newUnits);
	}

//this version has bugs. I have reverted to the older version below
	
//	void adjustRemainingWork(double multiplier) {
//		long dur;
//		boolean fixedDuration = ((NormalTask)getTask()).getSchedulingRule() == FixedDuration.getInstance();
//		if (!getResource().isLabor())
//			System.out.println("mater " + multiplier);
//		AbstractContour newContour = workContour;
//		if (fixedDuration)
//			newContour = workContour.adjustUnits(multiplier, getActualDuration());
//		else
//			newContour = workContour.contourAdjustWork(multiplier, getActualDuration());
//		if (workContour != newContour) { // adjust the work contour - the case of a personal contour
//			workContour = newContour; // if contour was changed
//			dur =workContour.calcTotalBucketDuration(getDuration()); // cannot perform simple multiplication of duration because non-work periods are NOT multiplied
//		} else {
//			dur = (long) (getActualDuration() + getRemainingDuration() * multiplier);
//		}
//		if (fixedDuration)
//			setUnits(getUnits() * multiplier);
//		else
//			setDuration(dur);
//	}
	
	void adjustRemainingWork(double multiplier) {
		long dur;
		
		AbstractContour newContour = workContour.contourAdjustWork(multiplier, getActualDuration());
		if (workContour != newContour) { // adjust the work contour - the case of a personal contour
			workContour = newContour; // if contour was changed
			dur =workContour.calcTotalBucketDuration(getDuration()); // cannot perform simple multiplication of duration because non-work periods are NOT multiplied
		} else {
			dur = (long) (getActualDuration() + getRemainingDuration() * multiplier);
		}
		setDuration(dur);
		setUnits(getUnits() / multiplier);
	}


	/**
	 * MSProject displays duration as only the working duration except in fixed duration tasks.
	 * @return duration with non-work periods excluded
	 */
	long calcWorkingDuration() {
		return workContour.calcWorkingBucketDuration(getDuration());
	}


	/**
	 * Allow setting of working duration. MSProject displays working duration (excludes non-work intervals) except when
	 * task type is fixed duration, in which case it shows duration with non-work intervals
	 * @param newWorkingDuration
	 */
	void adjustWorkingDuration(long newWorkingDuration) {
		adjustRemainingDuration(getDuration() + (newWorkingDuration - calcWorkingDuration()));
	}

	
	/**
	 * @return Returns the schedule.
	 */
	TaskSchedule getTaskSchedule() {
		if (taskSchedule == null)
			return getTask().getCurrentSchedule();
		return taskSchedule;
	}
	
	TaskSchedule getTaskScheduleOfAssignment() {
		return taskSchedule;
	}
	
	
	void convertToBaselineAssignment(boolean useDefaultCalendar) {
		try {
			if (useDefaultCalendar)
				baselineCalendar = CalendarService.getInstance().getDefaultInstance();
			else
				baselineCalendar = (WorkingCalendar) getEffectiveWorkCalendar().clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	boolean isBaseline() {
		return baselineCalendar != null;
	}
	
	public WorkCalendar getEffectiveWorkCalendar() {
		if (actualExceptionsCalendar != null)
			return actualExceptionsCalendar;
		if (baselineCalendar != null)
			return baselineCalendar;
		Resource resource = getResource();
		Task task = getTask();
		if (((NormalTask)task).isIgnoreResourceCalendar() || isInvalidIntersectionCalendar() || resource.getEffectiveWorkCalendar() == null)
			return task.getEffectiveWorkCalendar();
		// if no task calendar, or calendar is invalid due to empty intersection of task and resource calendars
		if (task.getWorkCalendar() == null)
			return resource.getEffectiveWorkCalendar();
		
		if (intersectionCalendar == null) {
			try {
				intersectionCalendar = ((WorkingCalendar)task.getEffectiveWorkCalendar()).intersectWith((WorkingCalendar)resource.getEffectiveWorkCalendar());
			} catch (InvalidCalendarIntersectionException e) {
				intersectionCalendar = WorkingCalendar.INVALID_INTERSECTION_CALENDAR;
				Alert.error(Messages.getString("Message.invalidIntersection"));
				return task.getEffectiveWorkCalendar();
			}
		}
		return intersectionCalendar;
		// need to use intersection work calendar
	}
	
	public boolean isInvalidIntersectionCalendar() {
		return intersectionCalendar == WorkingCalendar.INVALID_INTERSECTION_CALENDAR;
	}

	/**
	 * @return Returns the task.
	 */
	Task getTask() {
		return task;
	}

/**
 * The assignment start is calculated like this:
 * Get the task start. 
 * If the assignment is not started, use the task's dependency date if it's later than the start
 * Add in any delay
 */
	public long getStart() {
		long delay = this.calcTotalDelay();
		TaskSchedule ts = getTaskSchedule();
		long s = ts.getStart();
		if (percentComplete == 0) { // if no completion, use task dependency date if needed
			long d = ts.getDependencyDate();
			if (d > s && !task.startsBeforeProject())
				s = d;
		}
		if (delay > 0)
			s = getEffectiveWorkCalendar().add(s,delay,false); // use later time
		//TODO check if above should use task calendar or assignment calendar
		return s; 
	}
	
	
/**
 * Sets the assignment start. Since the assigment start is an offset (usually 0) from task start, setting a later start means
 * setting a delay for the assignment.  Also, any levelling delay is cleared
 */
	public void setStart(long start) {
		if (start > getTaskStart()) {
			long delay = getEffectiveWorkCalendar().compare(start,getTaskStart(),false);
			setLevelingDelay(0);
			setDelay(delay);
		}
	}
	
	long getSplitDuration() {
		long dependencyStart = getDependencyStart();
		if (dependencyStart == 0 || getDependencyStart() == getTaskStart())
			return 0;
		
		long remainingStart = Math.max(getTaskStart(),getStop());
		return Math.max(0,getEffectiveWorkCalendar().compare(getDependencyStart(),remainingStart,false));
	}
	

//		// if need to split
//		if (actualDuration >0 && actualDuration != durationMillis) {
//			long start = getStart();
//			long split = getTaskSchedule().getResume();
//			if (split > start) {
//				long splitDuration = task.getEffectiveWorkCalendar().compare(split,start,false); //TODO move this elsewhere
//				finish = getEffectiveWorkCalendar().add(finish,splitDuration,true);
//		}
//		}
//		return finish;

	
	long getFinish() {
		return getEnd();

//		// if need to split
//		if (actualDuration >0 && actualDuration != durationMillis) {
//			long start = getStart();
//			long split = getTaskSchedule().getResume();
//			if (split > start) {
//				long splitDuration = task.getEffectiveWorkCalendar().compare(split,start,false); //TODO move this elsewhere
//				finish = getEffectiveWorkCalendar().add(finish,splitDuration,true);
//		}
//		}
//		return finish;
	}


	/**
	 * @return Returns the resource.
	 */
	Resource getResource() {
		return resource;
	}
	
	AbstractContourBucket[] getContour(Object type) {
		return ((type == HasTimeDistributedData.COST) ? costContour : workContour).getContourBuckets();
	}
	/**
	 * @return Returns the costContour.
	 */
	AbstractContour getCostContour() {
		return costContour;
	}

	/**
	 * @return Returns the costRateIndex.
	 */
	int getCostRateIndex() {
		return costRateIndex;
	}
	
	void setCostRateIndex(int costRateIndex) {
		this.costRateIndex = costRateIndex;
	}

	Assignment getBaselineAssignment() {
		if (task == null) // case when just loading assignment in timesheet
			return null;
		return task.getBaselineAssignment(resource);
	}

	Assignment getBaselineAssignment(Object baseline, boolean createIfDoestExist) {
		if (task == null) // case when just loading assignment in timesheet
			return null;
		return task.getBaselineAssignment(resource,baseline, createIfDoestExist);
	}
	
	long effectiveBaselineStart() {
		Assignment baselineAssignment  = getBaselineAssignment();
		if (baselineAssignment == null)
			return getStart();
		return baselineAssignment.getStart();
	}

	long effectiveBaselineStart(Object baseline) {
		Assignment baselineAssignment  = getBaselineAssignment(baseline, false);
		if (baselineAssignment == null)
			return getStart();
		return baselineAssignment.getStart();
	}
	long effectiveBaselineFinish(Object baseline) {
		Assignment baselineAssignment  = getBaselineAssignment(baseline, false);
		if (baselineAssignment == null)
			return getFinish();
		return baselineAssignment.getFinish();
	}
	
//	private void adjustActualDurationAndContour(long newActualDuration, long stop, long splitDuration) {
//		long oldActualDuration = actualDuration;
//		newActualDuration = Duration.millis(newActualDuration);
//		if (newActualDuration == oldActualDuration) // if no change do nothing
//			return;
//		actualDuration = newActualDuration; // need to set in now since it mayb be used below inside bucketsBetweenDurations
//		
//		if (newActualDuration == 0) { // if setting to 0 actuals
//			return;
//		}
//		
//		if (oldActualDuration > newActualDuration) {// trim off end
//			actualWorkContour = actualWorkContour.adjustDuration(newActualDuration); // simple, just truncate it.
//		} else {
//System.out.println("Stop "+ new Date(stop) + " duration" + DurationFormat.format(stopResumeDuration));			
//			ArrayList list;
//			if (actualWorkContour != null) // see if there are actuals
//				list = actualWorkContour.toArrayList(); // copy current actual contour to an array list
//			else // in case there are no actuals yet
//				list = new ArrayList();
//			if (actualDuration > durationMillis) { // if made longer than planned duration, adjust planned duration
//				durationMillis = actualDuration;
//				workContour = workContour.adjustDuration(durationMillis);
//			}
//			list.addAll(workContour.bucketsBetweenDurations(oldActualDuration, newActualDuration, durationMillis)); // add from work contour
//			actualWorkContour = PersonalContour.getInstance(list); // set new contour that combines the two above
//			//insert gap for stop/resume
//System.out.println("contour before\n" + actualWorkContour.toString(newActualDuration));
//			if (stopResumeDuration > 0) {
//				actualWorkContour = ((PersonalContour)actualWorkContour).insertBucket(stop,PersonalContourBucket.getInstance(stopResumeDuration,0.0));
//				System.out.println("contour after\n" + actualWorkContour.toString(newActualDuration));
//			}
////			System.out.println("new actual contour" + actualWorkContour.toString(newActualDuration));
//		}
//	}

	


	
	/** returns the amount of effort that the resource as available to work on the assignment 
	 * 
	 * @return
	 */
	long getResourceAvailability() {
		WorkCalendar cal = resource.getEffectiveWorkCalendar();
		if (cal == null)
			cal = task.getOwningProject().getEffectiveWorkCalendar();
		//TODO implement time-scaled availability
		return (long) resource.getMaximumUnits() * cal.compare(getFinish(),getStart(),false);
	}
	
	
	void shift(long start, long end, long shiftDuration) {
		PersonalContour newContour =PersonalContour.makePersonal(workContour,getDuration());
		newContour = newContour.shift(start,end,shiftDuration);
		workContour =newContour;
		if (workContour.isPersonal())
			duration = workContour.calcTotalBucketDuration(duration);
		checkForNegativeDuration();
	}

	// in rare circumstances duration would go negative. prevent this
	private void checkForNegativeDuration() {
		if (duration < 0) {
			if (getDelay() > 0) // clear out any delay
				setDelay(0);
			duration = 0;
		}
		
	}
	void extend(long end, long extendDuration) {
		workContour = workContour.extend(end,extendDuration);
		if (workContour.isPersonal())
			duration = workContour.calcTotalBucketDuration(duration);
		else
			duration += extendDuration;
		checkForNegativeDuration();
	}
	/**
	 * @param startOffset
	 * @param extendDuration
	 */
	public void extendBefore(long startOffset, long extendDuration) {
		workContour = workContour.extendBefore(startOffset,extendDuration);
		if (workContour.isPersonal())
			duration = workContour.calcTotalBucketDuration(duration);
		else
			duration -= extendDuration;
		checkForNegativeDuration();
		
	}
	
	/* (non-Javadoc)
	 * @see com.projity.pm.scheduling.Schedule#split(java.lang.Object, long, long)
	 */
	public void split(Object eventSource, long from, long to) {
		System.out.println("split should not be called for AssignmentDetail");
	}

	
	public void removeEmptyBucketAtDuration(long atDuration) {
		if (workContour.isPersonal()) {
			workContour = ((PersonalContour)workContour).removeEmptyBucketAtDuration(atDuration);
		}
	}
	
	
	MutableInterval getRangeThatIntervalCanBeMoved(long start, long end) {
		return workContour.getRangeThatIntervalCanBeMoved(start,end);
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.HasCalendar#setWorkCalendar(com.projity.pm.calendar.WorkCalendar)
	 */
	public void setWorkCalendar(WorkCalendar workCalendar) {
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.HasCalendar#getWorkCalendar()
	 */
	public WorkCalendar getWorkCalendar() {
		return getEffectiveWorkCalendar();
	}
	
/**
 * Use the start dependency in the task schedule
 */	public long getDependencyStart() {
		return getTaskSchedule().getRemainingDependencyDate();
	}	
 
 	long getTaskStart() {
 		return getTaskSchedule().getStart();
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
    public void setDuration(long duration) {
    	long stop = getStop(); // need to preserve completion
    	if (Duration.millis(duration) < 0)
    		duration = 0; // just in case
    	this.duration = duration;
    	setStop(stop);
    }
    public long getActualFinish() {
    	if (getPercentComplete() < 1.0)
    		return 0;
    	return getEnd();
//    	return getEffectiveWorkCalendar().add(getStop(),getRemainingDuration(),false);
    }
    /**
     * Sets the actual finish.  This entails setting the end, the actual start (in case it's not set yet), the duration
     * and the actual duration.
     */
	public void setActualFinish(long actualFinish) {
		setEnd(actualFinish);
		setPercentComplete(1.0D);
	}    
 	
	public long getEnd() {
		WorkCalendar cal = getEffectiveWorkCalendar();
		long finish = cal.add(getStart(),getDuration(),true);
		if (getPercentComplete() > 0.0 && getPercentComplete() < 1.0) {
			long dependencyStart = getDependencyStart();
			if (dependencyStart > getTaskStart()) {
				long splitDuration  = cal.compare(dependencyStart,getTaskStart(),false);
				finish = cal.add(finish,splitDuration,true);
			}
		}
		return finish;
	}
	
	public void setEnd(long end) {
		long remaining = getRemainingDuration();
		long oldEnd = getEnd();
		if (oldEnd == end)
			return;
		long stop = getStop();
		if (stop > end)
			end = stop;
		long durationDifference = getEffectiveWorkCalendar().compare(end,oldEnd,false);
//		if (stop != 0 && end > stop) {
//			durationDifference -= getSplitDuration();
//			System.out.println("split duration is " + DurationFormat.format(getSplitDuration()));
//		}
//System.out.println("duration difference " + DurationFormat.format(durationDifference));
//System.out.println("old end " + new Date(oldEnd) + " end " + new Date(end));
		long newRemaining = remaining + durationDifference;
		if (newRemaining < 0) // test to avoid negative duration
			newRemaining = 0L;
		adjustRemainingDuration(newRemaining);
		
		
//		long splitDuration = 0; //getSplitDuration();
//		durationActive += durationDifference + splitDuration;
//		durationSpan += durationDifference + splitDuration;
		
		setStop(stop);
//		long splitDuration = getSplitDuration();
//		
//		if (splitDuration >= 0) {
//			durationSpan = durationActive + splitDuration;
//		}
			
	}
	
	public long getActualStart() {
		if (percentComplete == 0)
			return 0;
		return getStart();
	}

	public void setActualStart(long actualStart) {
		setStart(actualStart);
		if (percentComplete == 0)
			setPercentComplete(INSTANT_COMPLETION);
	}
	/**
	 * @return Returns the actualDuration.
	 */
	public long getActualDuration() {
		return  DateTime.closestDate(getDuration() * getPercentComplete());
	}
	/**
	 * @param actualDuration The actualDuration to set. Will fix actual start if needed
	 */
	public void setActualDuration(long actualDuration) {
		if (getDuration() > 0)
			setPercentComplete(((double)actualDuration)/getDuration());
	}

	public void clearDuration() {
		setDuration(0);
	}
	/* (non-Javadoc)
	 * @see com.projity.pm.scheduling.Schedule#moveInterval(java.lang.Object, long, long, com.projity.pm.scheduling.ScheduleInterval)
	 */
	public void moveInterval(Object eventSource, long start, long end, ScheduleInterval oldInterval, boolean isChild) {
		throw new RuntimeException("cannot call moveInterval for an AssignmentDetail");
	}

	public void moveRemainingToDate(long date) {
		throw new RuntimeException("cannot call moveRemainingToDate for an AssignmentDetail");
	}
	
	
	/* (non-Javadoc)
	 * @see com.projity.pm.scheduling.Schedule#consumeIntervals(com.projity.functor.IntervalConsumer)
	 */
	public void consumeIntervals(IntervalConsumer consumer) {
		throw new RuntimeException("cannot call consumeIntervals for an AssignmentDetail");
	}

	public long getElapsedDuration() {
		return Math.round(getEffectiveWorkCalendar().compare(getEnd(), getStart(),true) * CalendarOption.getInstance().getFractionOfDayThatIsWorking());
	}
	public long getRemainingDuration() {
		return DateTime.closestDate( (getDuration() * (1.0D - percentComplete)));
	}

	public void setRemainingDuration(long remainingDuration) {
//		if (getDuration() > 0) {
//			double actual = (percentComplete * getDuration());
//			long total = (long) (actual + remainingDuration);
//			setDuration(total);
//			if (actual != 0.0D)
//				setPercentComplete(actual / total);
//			
//		}
			setPercentComplete(1.0D - ((double)remainingDuration)/getDuration());
	}
	
	/**
	 *  returns calculated completed date.  If there is no advancement, the date is start date
	 * @return
	 */
	public long getStop() {
		long stop = 0;
		if (percentComplete > 0.0 && percentComplete != INSTANT_COMPLETION) {
			stop = getEffectiveWorkCalendar().add(getStart(),getActualDuration(),true); // use earlier date
		}
		return stop;
	}

	public void setDependencyStart(long dependencyStart) {
		throw new RuntimeException("cannot call setDependencyStart for an AssignmentDetail");	
	}
	
/**
 * Remove any filler periods in the contour after a certain date. This is used in the special case
 * of uncompleting a task.  There might be filler periods in the contour due to a task dependency
 * that pushed out remaining work that was subsequently completed.  When uncompleting, the remaining
 * work needs to have this filler period removed, otherwise there will be an extra delay.
 * @param stop Stop date
 */	void removeFillerAfter(long stop) {
		long stopDuration = getEffectiveWorkCalendar().compare(stop,getStart(),false);
		workContour = workContour.removeFillerAfter(stopDuration);
	}
	
	public void setStop(long stop) {
		if (stop < getStart()) // if before start ignore
			return;
		if (percentComplete == 1.0 && stop >= getActualFinish()) // adjust to be no later than actual finish
			return;
		
//		System.out.println("setting stop to  " + new java.util.Date(stop));

		if (stop <= getStart()) { // if getting rid of completion
			setPercentComplete(0);
		} else {
			long actualDuration = getEffectiveWorkCalendar().compare(stop,getStart(),false);
//			if (getDependencyStart() >= getStop() && getDependencyStart() < stop) {// if setting stop incorporates split due to dependency
//				actualDuration -= getSplitDuration();
//			}
		//	duration = getWorkContour().calcTotalBucketDuration(0);
			
			long d = getDuration();
			if (d != 0)
				setPercentComplete(((double)actualDuration) / d);
		}
	}
	/* (non-Javadoc)
	 * @see com.projity.pm.scheduling.Schedule#getResume()
	 */
	public long getResume() {
		long resume = 0;
		
		if (percentComplete > 0.0)
			resume = getEffectiveWorkCalendar().add(getStart(),getActualDuration(),false); // use later date
		resume = Math.max(resume,getDependencyStart());
		if (workContour.isPersonal()) {
			
		}

		return resume;
	}
	/* (non-Javadoc)
	 * @see com.projity.pm.scheduling.Schedule#setResume(long)
	 */
	public void setResume(long resume) {
		long oldResume = getResume();
		long shift = getEffectiveWorkCalendar().compare(resume,oldResume,false);
		if (shift == 0)
			return;
		
		if (resume > oldResume) {

		}
//
//		setStop(stop);
		
	}

	long extractDelay() {
		return workContour.extractDelay();
	}
	
	
	
	private static short DEFAULT_VERSION=1;
	private short version=DEFAULT_VERSION;
	
	private void writeObject(ObjectOutputStream s) throws IOException {
		workContourType = workContour.getType();
		costContourType = costContour.getType();
	    s.defaultWriteObject();
	    rate.serialize(s);
	    //delayable
	    s.writeLong(delayable.getDelay());
	    s.writeLong(delayable.getLevelingDelay());
	    //personnal contours
	    if (workContourType==CONTOURED)
	    	s.writeObject(workContour.getContourBuckets());
	    if (costContourType==CONTOURED)
	    	s.writeObject(costContour.getContourBuckets());
	    
	    if (version>=1){
	    	s.writeBoolean(taskSchedule==null);
	    	if (taskSchedule!=null) taskSchedule.serialize(s);
	    }
	}
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException  {
	    s.defaultReadObject();
	    rate=Rate.deserialize(s);
	    //delayable
	    delayable=new DelayableImpl(s.readLong(),s.readLong());
	    //personnal contours
	    if (workContourType==CONTOURED){
//	    	try{
	    		workContour  = PersonalContour.getInstance((AbstractContourBucket[])s.readObject());
//	    	}catch(Exception e){
//	    		workContour = StandardContour.getStandardContour(workContourType);
//	    	}
	    } else workContour = StandardContour.getStandardContour(workContourType);
	    if (costContourType==CONTOURED){
//	    	try{
		    	costContour  = PersonalContour.getInstance((AbstractContourBucket[])s.readObject());
//	    	}catch(Exception e){
//	    		costContour = StandardContour.getStandardContour(costContourType);
//	    	}
	    } else costContour = StandardContour.getStandardContour(costContourType);
	    
	    if (version>=1){
	    	boolean nullSchedule=s.readBoolean();
	    	if (!nullSchedule){
	    		taskSchedule=TaskSchedule.deserialize(s);
	    		taskSchedule.setTask(task);
	    	}
	    }
	    version=DEFAULT_VERSION;
	    	
	}
	public Object clone() {
//		try {
			/*
			private transient Rate rate = new Rate(1.0D); // units can never be 0!!!
		 	double percentComplete = 0;
		 	long duration = 0;

		    //	private Schedule schedule = new ScheduleImpl(this);
			
//			private long durationMillis; // This includes non-working duration (if personal contour).  See also calcWorkingDuration()
//			long actualDuration = 0;
			
			//TODO There is some stuff to figure out regarding contouring remaining work.  If you change the contour, Project applies
			// the entire contour to the remaining work.  If you keep completing and change the contour again, project keeps the
			// different contours active to where they were applied.  The weird thing is that if you uncomplete the task, the work still
			// shows the various contours, even if the current contour is flat.  This is a bug.
		 	
		 	
			private int workContourType; // these are only used for serializing the contour.  They are not "up to date" otherwise
			private int costContourType;
		 	private transient AbstractContour workContour = StandardContour.FLAT_CONTOUR;
			private transient AbstractContour costContour = StandardContour.FLAT_CONTOUR;

			private transient WorkCalendar actualExceptionsCalendar; // for when user enters actual work during non-work time of calendar
			private transient WorkingCalendar intersectionCalendar = null;
			private transient TaskSchedule taskSchedule = null;

			private transient Resource resource;
			private transient Task task;
			private transient Delayable delayable;
			private int requestDemandType = RequestDemandType.NONE;
			private int costRateIndex = CostRateTables.DEFAULT;
			private long overtimeWork = 0; // allowed overtime that is evenly distributed across contour
			
			private WorkingCalendar baselineCalendar = null; // only applies if this is a baseline
			*/
			AssignmentDetail a = new AssignmentDetail();
			a.rate=(Rate)rate.clone();
			a.percentComplete = percentComplete;
			a.duration = duration;
			a.workContour = (AbstractContour)workContour.clone();
			a.costContour = costContour; //(AbstractContour)costContour.clone();
			a.actualExceptionsCalendar = actualExceptionsCalendar; //(WorkCalendar) actualExceptionsCalendar.clone();
			a.intersectionCalendar = intersectionCalendar; // (WorkingCalendar) intersectionCalendar.clone();
			a.taskSchedule = taskSchedule; // copy not clone
			a.resource = resource;
			a.task = task;
			a.delayable=(Delayable)((DelayableImpl)delayable).clone();
			a.requestDemandType = requestDemandType;
			a.costRateIndex = costRateIndex;
			a.overtimeWork = overtimeWork;
			a.baselineCalendar = baselineCalendar;
			return a;
//		}
//		catch (CloneNotSupportedException e) {
//			throw new InternalError();
//		}
	}


	
    public long getOvertimeWork() {
        return overtimeWork;
    }
    public void setResource(Resource resource) {
        this.resource = resource;
    }
    public void setTask(Task task) {
        this.task = task;
    }
    
    boolean hasDuration() {
    	return duration != 0;
    }
	public final boolean isTemporal() {
		return rate.getTimeUnit() != TimeUnit.NON_TEMPORAL;
	}
	/**
	 * @param timeUnit
	 */
	void setRateUnit(int timeUnit) {
		rate.setTimeUnit(timeUnit);
	}
	public final Rate getRate() {
		return rate;
	}
	public final void setRate(Rate rate) {
		this.rate = rate;
	}
	public void invalidateAssignmentCalendar() {
		if (intersectionCalendar != null)
			intersectionCalendar = null;
		if (actualExceptionsCalendar != null)
			actualExceptionsCalendar.invalidate();
	}
	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.HasCalendar#invalidateCalendar()
	 */
	public Document invalidateCalendar() {
		return task.getOwningProject();
		
	}
	
	
	public boolean isJustModified(){
		Task task=getTask();
		if (task==null) return false;
		else return task.isJustModified();
	}
	public void addCalendarTime(long start, long end) {
		if (actualExceptionsCalendar == null) {
			WorkCalendar base = getEffectiveWorkCalendar();
			actualExceptionsCalendar = CalendarService.getInstance().getStandardBasedInstance();
			try {
				actualExceptionsCalendar.setBaseCalendar(base);
			} catch (CircularDependencyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		actualExceptionsCalendar.addCalendarTime(start, end);
	}

	public void setComplete(boolean complete) {
		ScheduleUtil.setComplete(this,complete);
	}
	public boolean isComplete() {
		return getPercentComplete() == 1.0D;
	}
	public final long getEarliestStop() {
		return getStop();
	}
	public final long getCompletedThrough() {
		return getStop();
	}
	public void setCompletedThrough(long completedThrough) {
		setStop(completedThrough);
	}

	
	public Object backupDetail() {
		return clone();
	}
	public void restoreDetail(Object source,Object detail,boolean isChild) {
	}

}