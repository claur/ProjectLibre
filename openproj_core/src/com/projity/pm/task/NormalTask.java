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
package com.projity.pm.task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.collections.Closure;

import com.projity.algorithm.ReverseQuery;
import com.projity.algorithm.TimeIteratorGenerator;
import com.projity.algorithm.buffer.CalculatedValues;
import com.projity.association.AssociationFormatParameters;
import com.projity.association.AssociationList;
import com.projity.association.AssociationListFormat;
import com.projity.configuration.Configuration;
import com.projity.configuration.Settings;
import com.projity.datatype.Duration;
import com.projity.datatype.ImageLink;
import com.projity.datatype.TimeUnit;
import com.projity.document.Document;
import com.projity.field.CustomFieldsImpl;
import com.projity.field.FieldContext;
import com.projity.field.FieldParseException;
import com.projity.functor.IntervalConsumer;
import com.projity.functor.NumberClosure;
import com.projity.functor.ObjectVisitor;
import com.projity.graphic.configuration.HasIndicators;
import com.projity.graphic.configuration.HasTaskIndicators;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.summaries.DeepChildWalker;
import com.projity.options.CalculationOption;
import com.projity.options.CalendarOption;
import com.projity.options.ScheduleOption;
import com.projity.pm.assignment.Allocation;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.AssignmentFormat;
import com.projity.pm.assignment.AssignmentService;
import com.projity.pm.assignment.HasAssignments;
import com.projity.pm.assignment.TimeDistributedFields;
import com.projity.pm.assignment.timesheet.TimesheetHelper;
import com.projity.pm.calendar.CalendarService;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.costing.Accrual;
import com.projity.pm.costing.EarnedValueCalculator;
import com.projity.pm.costing.EarnedValueFields;
import com.projity.pm.costing.EarnedValueValues;
import com.projity.pm.criticalpath.TaskSchedule;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.key.HasKeyImpl;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.scheduling.BarClosure;
import com.projity.pm.scheduling.ConstraintType;
import com.projity.pm.scheduling.Schedule;
import com.projity.pm.scheduling.ScheduleEvent;
import com.projity.pm.scheduling.ScheduleInterval;
import com.projity.pm.scheduling.ScheduleUtil;
import com.projity.pm.scheduling.SchedulingFields;
import com.projity.pm.scheduling.SchedulingRule;
import com.projity.pm.scheduling.SchedulingType;
import com.projity.pm.snapshot.BaselineScheduleFields;
import com.projity.pm.snapshot.DataSnapshot;
import com.projity.pm.snapshot.SnapshottableImpl;
import com.projity.server.access.ErrorLogger;
import com.projity.strings.Messages;
import com.projity.util.DateTime;

/**
 * @stereotype thing
 */
public class NormalTask extends Task implements Allocation, TaskSpecificFields,
		SchedulingFields, HasAssignments, EarnedValueValues,
		EarnedValueFields, TimeDistributedFields, BaselineScheduleFields,
		HasTaskIndicators {
	static final long serialVersionUID = 273898992929L;


//	Schedule schedule = null;


	boolean estimated = true;
	int priority = 500;
	public NormalTask(Project project) {
		this(project.isLocal(),project);
	}
	public NormalTask(boolean local,Project project) {
		super(local);
		this.project = project;
		initializeDates();
		addDefaultAssignment();

	}
	public NormalTask() {
		super();
	}

	/**
	 * Used when creating a task to set initial date and duration conditions
	 *
	 */
	void initializeDates() {
		setRawConstraintType(project == null ? ConstraintType.ASAP : project.getDefaultConstraintType());
		long duration = CalendarOption.getInstance().getDefaultDuration(); //MS uses 1 day estimated
		setRawDuration(duration);
		setWorkCalendar(null);

		// initialize start and end to avoid 0 dates in calculations
		long start = project.getStart();
		currentSchedule.setStart(start);
		currentSchedule.setFinish(start);

		if (ScheduleOption.getInstance().isNewTasksStartToday())
			setWindowEarlyStart(CalendarOption.getInstance()
					.makeValidStart(DateTime.midnightToday(), true));
	}

	/**
	 * This constructor is used to create dummy tasks, such as the UNASSIGNED
	 * instance. We do not want to perform standard initialization on it.
	 *
	 * @param dummy
	 */
	private NormalTask(boolean dummy) {
		super(true);
	}

	private static NormalTask UNASSIGNED = null;

	public static NormalTask getUnassignedInstance() {
		if (UNASSIGNED == null) {
			UNASSIGNED = new NormalTask(true);
			UNASSIGNED.setName(Messages.getString("Text.Unassigned"));
		}
		return UNASSIGNED;
	}

	private Assignment newDefaultAssignment() {
		return Assignment.getInstance(this, ResourceImpl
				.getUnassignedInstance(), 1.0, 0);
	}
	public boolean isNormal() {
		return !isSummary() && !isMilestone() && !isExternal();
	}


	public boolean isCritical() {
		if (currentSchedule.isForward())
			return getEarlyFinish() >= getLateFinish(); //TODO hook into preference
		else // reverse schedule
			return getLateStart() <= getEarlyStart();
	}

	public boolean isMilestone() {
	//	return !hasDuration();
		return Duration.millis(getRawDuration()) == 0 || isMarkTaskAsMilestone();
	}
	public double getPercentComplete() {
		if (isZeroDuration()) { // special case for completion on milestones
			int count = 0;
			double pc = 0;
			Assignment ass;
			Iterator i =getAssignments().iterator();
			while (i.hasNext()) {
				ass = ((Assignment)i.next());
				pc += ass.getPercentComplete();
				count++;
			}
			if (count == 0) // shouldn't happen
				return 0;
			return pc / count;
		} else {
			return super.getPercentComplete();
		}
	}

/****************************************************************************************
 * Schedule
 ****************************************************************************************/
	/**
	 * @return
	 */
	public long getDuration() {
		
		long duration;
		if (isWbsParent() || isExternal() || isSubproject()) {
			long raw = getRawDuration();
			if (raw >=0)
				duration = Duration.millis(raw);
			else {
				project.addRepaired(this);
				ErrorLogger.logOnce("raw parent", "repaired bad raw duration" + this,null);
				duration = 0;
			}
		} else {
			AssociationList assignments =getAssignments();
			if (assignments.size() == 1) {
				duration = ((Assignment)assignments.getFirst()).getDurationMillis();
			} else {
				Iterator i = assignments.iterator();
				long end = 0;
				// get the latest ending assignment
				while (i.hasNext()) {
					end = Math.max(end,((Assignment)i.next()).getEnd());
				}
				// duration is calendar time between assignment end and task start
				duration = getEffectiveWorkCalendar().compare(end,getStart(),false);
			}
		}
		duration = Duration.setAsEstimated(duration,estimated);
		return duration;

//		return calcActiveAssignmentDuration(getEffectiveWorkCalendar());
	}

	/** Quickly check to see if a task has a duration without actually calculating it
	 *
	 * @return true if duration > 0
	 */
	public boolean hasDuration() {
		if (isWbsParent()) {
			return getRawDuration() != 0;
		} else {
			AssociationList assignments =getAssignments();
			if (assignments.size() == 1)
				return ((Assignment)assignments.getFirst()).hasDuration();
			Iterator i = assignments.iterator();
			while (i.hasNext()) {
				if (((Assignment)i.next()).hasDuration())
					return true;
			}
		}
		return false;

	}




	/**
	 * @param duration
	 */
	public void setDuration(long duration) {
		setRawDuration(duration); // set the schedule duration, primariy for use when reading a file
		estimated = Duration.isEstimated(duration);
		duration = Duration.millis(duration);
		long actualDurationMillis = Duration.millis(getActualDuration());
		if (duration < actualDurationMillis) // if reducing duration to shorter than the current actual duration
			setPercentComplete(1);
		if (!isWbsParent()) {
			long remainingDuration = duration - actualDurationMillis;
			getSchedulingRule().adjustRemainingDuration(this, remainingDuration, true);
		}
		updateCachedDuration();
	}

/********************************************************************************
 * Calendars
 ***********************************************************************************/

	private WorkCalendar workCalendar = null;

	/**
	 * @return
	 */
	public WorkCalendar getWorkCalendar() {
		return workCalendar;
	}

	public WorkCalendar getEffectiveWorkCalendar() {
		if (workCalendar == null) {
			if (getProject() == null) {
				System.out.println("------No project in getting calendar for task " + getUniqueId() + " " + getName());
				return CalendarService.getInstance().getDefaultInstance();
			}
			return getProject().getEffectiveWorkCalendar();
		}
		return workCalendar;
	}

	/**
	 * @param workCalendar
	 */
	public void setWorkCalendar(WorkCalendar workCalendar) {
		this.workCalendar = workCalendar;
	}


	/**
	 * @return
	 */
	public DataSnapshot getCurrentSnapshot() {
		return snapshots.getCurrentSnapshot();
	}

	/**
	 * @param i
	 * @return
	 */
	public DataSnapshot getSnapshot(Object snapshotId) {
		return snapshots.getSnapshot(snapshotId);
	}

	/**
	 * @param i
	 */
	public void saveCurrentToSnapshot(Object snapshotId) {
		setSnapshot(snapshotId, cloneSnapshot(getSnapshot(CURRENT)));
		markTaskAsNeedingRecalculation(); // for redraw purpooses, not for recalc.
		setDirty(true);
	}
	public void restoreSnapshot(Object snapshotId,Object b) {
		TaskBackup backup=(TaskBackup)b;
		if (backup.snapshot==null) return;
		TaskSnapshot snapshot=(TaskSnapshot)((TaskSnapshot) getSnapshot(CURRENT)).clone();
		//snapshot.setCurrentSchedule(getCurrentSchedule());
		restoreDetail(this, backup, true,snapshot);
		setSnapshot(snapshotId, snapshot);
		markTaskAsNeedingRecalculation(); // for redraw purpooses, not for recalc.
		setDirty(true);
	}

	/**
	 * @param snapshot
	 */
	public void setCurrentSnapshot(DataSnapshot snapshot) {

		snapshots.setCurrentSnapshot(snapshot);
	}

	/**
	 * @param i
	 * @param snapshot
	 */
	public void setSnapshot(Object snapshotId, DataSnapshot snapshot) {
		snapshots.setSnapshot(snapshotId, snapshot);
	}

	/**
	 * @param i
	 */
	public void clearSnapshot(Object snapshotId) {
		snapshots.clearSnapshot(snapshotId);
		markTaskAsNeedingRecalculation(); // for redraw purpooses, not for recalc.
		setDirty(true);
	}

	public boolean hasRealAssignments() {
		return (null == findAssignment(ResourceImpl.getUnassignedInstance()));
	}
	/**
	 * @return
	 */
	public AssociationList getAssignments() {
		return ((TaskSnapshot) getCurrentSnapshot()).getAssignments();
	}

	public AssociationList getRealAssignments() {
		if (hasRealAssignments())
			return getAssignments();
		else
			return new AssociationList(); //empty list
	}

	public boolean isAssignedToMe(){
		for (Iterator i=getAssignments().iterator();i.hasNext();){
			Assignment a=(Assignment)i.next();
			if (a.isMine()) return true;
		}
		return false;
	}

	/**
	 * Add an assignment to the task.  A task always has at least one assignment, whether or not
	 * it has any true assignments.  This is because a default assignment is always present.  This
	 * greatly facilitates other calculations.  This method takes care to either create or delete
	 * the default assignment.
	 *
	 * @param assignment
	 */
	public Assignment addDefaultAssignment(){
		Assignment ass = newDefaultAssignment();
		addAssignment(ass);
		return ass;
	}
	public void addAssignment(Assignment assignment) {
		//project.beginUndoUpdate();
		boolean recalculateDuration = !assignment.isDefault()
				&& assignment.isInitialized() && assignment.isLabor();
		Assignment defaultAssignment = findAssignment(ResourceImpl
				.getUnassignedInstance());

		if (!assignment.isDefault()) {
			// get rid of any default
			if (defaultAssignment != null ) { //Remove any default assignment
 				assignment.usePropertiesOf(defaultAssignment); // the new assignment must take on properties of the default assignment
				AssignmentService.getInstance().remove(defaultAssignment, null,true);
			} else {
				// if the task is started already, then only apply to remaining duration.  This means added delay to new assignment
				if (getActualStart() != 0L)
					assignment.setDelay(Duration.millis(getActualDuration()));
				assignment.adjustRemainingDuration(Duration.millis(getRemainingDuration()),false);
			}
		} else {
			if (defaultAssignment != null) //Remove any default assignment.  This happens importing if the imported task just has no assignments
				AssignmentService.getInstance().remove(defaultAssignment, null,true);

			// use default task duration for the default assignment duraiton
			assignment.setDuration(getRawDuration());
		}

		// must calculate these two values before adding assignment!
		double mostLoadedAssignmentUnits = getMostLoadedAssignmentUnits();
		// Get details of current assignments before change
		double assignedRate = getRemainingUnits();

		// add assignment
		((TaskSnapshot) getCurrentSnapshot()).addAssignment(assignment);

		if (!assignment.isInitialized()) // if reading in, then don't recalc duration
			return;

		// if effort driven then set duration
		if (recalculateDuration && isEffortDriven()) {
			if (assignedRate != 0) {//
				if (getSchedulingType() == SchedulingType.FIXED_DURATION) // fixed duration effort driven has complicated rule - a new assignment is weighted the same as the most loaded assignment, unless that assignment is over 100%
					assignment.adjustRemainingUnits(Math.min(1.0,mostLoadedAssignmentUnits), 1, false, false);
				double newRemainingUnits = assignedRate + assignment.getRemainingLaborUnits();

				getSchedulingRule().adjustRemainingUnits(this,
						newRemainingUnits, assignedRate,
						true, true); // conserve total units
			}
		}
		setDirty(true);
		//project.endUndoUpdate();

	}


	/**
	 * @param assignment
	 */
	public void removeAssignment(Assignment assignment) {
		//project.beginUndoUpdate();
		boolean recalculateDuration = !assignment.isDefault()
				&& assignment.isInitialized(); // && assignment.isLabor();
		// Get details of current assignments before change

		double assignedRate = getRemainingUnits();
		((TaskSnapshot) getCurrentSnapshot()).removeAssignment(assignment);

		if (!assignment.isDefault()) {

			if (recalculateDuration && isEffortDriven()) {
				double newUnits = assignedRate - assignment.getLaborUnits();
				if (newUnits != 0) {
					getSchedulingRule().adjustRemainingUnits(this,
							newUnits, assignedRate,
							true, true); // conserve total units
				}
			}
			if (getAssignments().isEmpty()) {
				Assignment newDefault = newDefaultAssignment();
				newDefault.usePropertiesOf(assignment); // the default assignment must take on properties of the removed assignment
				AssignmentService.getInstance().connect(newDefault, null);
			}
		}
		setDirty(true);
		//project.endUndoUpdate();

	}


	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.snapshot.Snapshottable#cloneSnapshot(com.projity.pm.snapshot.DataSnapshot)
	 */

	public DataSnapshot cloneSnapshot(DataSnapshot snapshot) {
		return (DataSnapshot) ((TaskSnapshot) snapshot).clone();
	}

	public TaskSnapshot getBaselineSnapshot() {
		return (TaskSnapshot) getSnapshot(CalculationOption.getInstance()
				.getEarnedValueBaselineId());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.assignment.HasTimeDistributedData#buildComplexQuery(com.projity.algorithm.ComplexQuery)
	 */
	public void buildReverseQuery(ReverseQuery reverseQuery) {
		//Do this ones assignments
		((TaskSnapshot) getCurrentSnapshot()).buildReverseQuery(reverseQuery);
		Collection children = getWbsChildrenNodes();
		Object current;
		if (children != null) { //  do for all children as well
			Iterator i = children.iterator();
			Task child;
			while (i.hasNext()) {
				current = ((Node) i.next()).getImpl();
				if (! (current instanceof NormalTask))
					continue;
				child = (Task)current;
				child.buildReverseQuery(reverseQuery);
			}
		}

	}

	public long getBaselineStartOrZero() {
		TaskSnapshot baseline = getBaselineSnapshot();
		if (baseline == null)
			return 0L;
		return getBaselineStart();
	}

	public long getBaselineFinishOrZero() {
		TaskSnapshot baseline = getBaselineSnapshot();
		if (baseline == null)
			return 0L;
		return getBaselineFinish();
	}
	
	public long getBaselineStart() {
		TaskSnapshot baseline = getBaselineSnapshot();
		if (baseline == null)
			return getStart();

		return baseline.getCurrentSchedule().getStart();

	}
	public long getBaselineFinish() {
		TaskSnapshot baseline = getBaselineSnapshot();
		if (baseline == null)
			return getEnd();

		return baseline.getCurrentSchedule().getFinish();

	}

	public int getSchedulingType() {
		return ((TaskSnapshot) getCurrentSnapshot()).getSchedulingType();
	}

	public void setSchedulingType(int schedulingType) {
		((TaskSnapshot) getCurrentSnapshot()).setSchedulingType(schedulingType);
	}

	public boolean isEffortDriven() {
		return ((TaskSnapshot) getCurrentSnapshot()).isEffortDriven();
	}

	public void setEffortDriven(boolean effortDriven) {
		((TaskSnapshot) getCurrentSnapshot()).setEffortDriven(effortDriven);

	}

	public boolean isReadOnlyEffortDriven(FieldContext fieldContext) {
		return ((TaskSnapshot) getCurrentSnapshot()).isReadOnlyEffortDriven(fieldContext);
	}

	public static Closure forAllAssignments(Closure visitor) {
		return new ObjectVisitor(visitor) {
			protected Object getObject(Object arg0) {
				return ((TaskSnapshot) ((Task) arg0).getCurrentSnapshot())
						.getHasAssignments();
			}
		};
	}

	public double getFixedCost() {
		return ((TaskSnapshot) getCurrentSnapshot()).getFixedCost();
	}
	public void setFixedCost(double fixedCost) {
		((TaskSnapshot) getCurrentSnapshot()).setFixedCost(fixedCost);
	}

	/**
	 * @return Returns the fixedCostAccrual.
	 */
	public final int getFixedCostAccrual() {
		return ((TaskSnapshot) getCurrentSnapshot()).getFixedCostAccrual();
	}
	/**
	 * @param fixedCostAccrual The fixedCostAccrual to set.
	 */
	public final void setFixedCostAccrual(int fixedCostAccrual) {
		((TaskSnapshot) getCurrentSnapshot()).setFixedCostAccrual(fixedCostAccrual);
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Assignment findAssignment(Resource resource) {
		return ((TaskSnapshot) getCurrentSnapshot()).findAssignment(resource);
	}

	public Assignment findAssignment(Task task) {
		return ((TaskSnapshot) getCurrentSnapshot()).findAssignment(task);
	}

	public void updateAssignment(Assignment modified) {
		((TaskSnapshot) getCurrentSnapshot()).updateAssignment(modified);

	}

	public void forEachWorkingInterval(Closure visitor, boolean mergeWorking, WorkCalendar workCalendarToUse) {
		((TaskSnapshot) getCurrentSnapshot()).forEachWorkingInterval(visitor,
				mergeWorking, workCalendarToUse);
	}

	/**
	 * @return
	 */
	public boolean isEstimated() {
		return estimated;
	}

/**
 * Set estimated status of flag. First level parents will have their status set by the CP.  Higher levels
 * will need to be set recursively. Note that a parent will only be asked to updated its estimated
 * status if one of its children has had its estimated status change.
 */
	public void setEstimated(boolean estimated) {
		boolean changed = this.estimated != estimated;
		this.estimated = estimated;
		if (changed && isWbsParent()) { // only deal with parents already since CP handles children and sets first parent level
			NormalTask parent = (NormalTask) this.getWbsParentTask();
			if (parent != null)
				parent.updateEstimatedStatus();
		}

	}

	private void updateEstimatedStatus() {
		Collection children = getWbsChildrenNodes();
		Iterator i = children.iterator();
		Object current;
		NormalTask child;
		boolean childEstimated = false;
		while (i.hasNext()) {
			current = ((Node) i.next()).getImpl();
			if (! (current instanceof NormalTask))
				continue;
			child = (NormalTask) current;
			childEstimated |= child.isEstimated();
		}
		setEstimated(childEstimated);
	}

	/**
	 * set actual start and competion date for parents
	 *
	 */
	protected void assignParentActualDatesFromChildren() {
		NormalTask parent = this;
		while ((parent = (NormalTask) parent.getWbsParentTask()) != null)
			parent.assignActualDatesFromChildren();

	}

	/**
	 * Assigns the actual start and completed date fields of parents based on
	 * children values
	 *
	 */
	public void assignActualDatesFromChildren() {
		long computedActualStart = Long.MAX_VALUE;
		long stop = 0;
		Collection children = getWbsChildrenNodes();
		Iterator i = children.iterator();
		Task child;
		long currentActualStart;
		long oldActualDuration = Duration.millis(getActualDuration());
		Object current;
		while (i.hasNext()) {
			current = ((Node) i.next()).getImpl();
			if (! (current instanceof NormalTask))
				continue;
			child = (NormalTask) current;
			if (!child.inProgress())
				continue;
			if ((currentActualStart = child.getActualStart()) != 0) // if any task has actual start, use the earliest value
				computedActualStart = Math.min(computedActualStart, currentActualStart);

			stop = Math.max(stop, child.getStop());
		}

		long actualDuration = 0;
		if (computedActualStart != Long.MAX_VALUE && stop != 0)
			actualDuration = getEffectiveWorkCalendar().compare(stop,getStart(),false);
		if (computedActualStart != Long.MAX_VALUE)
			setActualStartNoEvent(computedActualStart);
		else
			setActualStartNoEvent(0L);
		
		if (actualDuration != oldActualDuration) {
			double percentComplete =((double)actualDuration) / getDurationMillis();
			currentSchedule.setPercentComplete(percentComplete);
			markTaskAsNeedingRecalculation(); // so it redraws
		}
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.task.TaskSpecificFields#taskCalendar()
	 */
	public WorkCalendar getTaskCalendar() {
		return getWorkCalendar();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.task.TaskSpecificFields#setTaskCalendar(java.lang.String)
	 */
	public void setTaskCalendar(WorkCalendar taskCalendar) {
		if (workCalendar == taskCalendar)
			return;
		CalendarService.getInstance().reassignCalendar(this,workCalendar,taskCalendar);
		setWorkCalendar(taskCalendar);
		invalidateAssignmentCalendars(); // assignments intersection calendars need to be recalculated
	}

	public long getBaselineStart(int numBaseline) {
		TaskSnapshot snapshot = ((TaskSnapshot) getSnapshot(new Integer(
				numBaseline)));
		if (snapshot == null)
			return 0;
		return snapshot.getCurrentSchedule().getStart();
	}

	public long getBaselineFinish(int numBaseline) {
		TaskSnapshot snapshot = ((TaskSnapshot) getSnapshot(new Integer(
				numBaseline)));
		if (snapshot == null)
			return 0;
		return snapshot.getCurrentSchedule().getEnd();
	}

	public long getBaselineDuration(int numBaseline) {
		TaskSnapshot snapshot = ((TaskSnapshot) getSnapshot(new Integer(
				numBaseline)));
		if (snapshot == null)
			return 0;
		return snapshot.getCurrentSchedule().getRawDuration();
	}

	public double getBaselineCost(int numBaseline, long start, long end) {
		TaskSnapshot snapshot = ((TaskSnapshot) getSnapshot(new Integer(
				numBaseline)));
		if (snapshot == null)
			return 0;
		return snapshot.cost(start, end);
	}

	public double getBaselineWork(int numBaseline, long start, long end) {
		TaskSnapshot snapshot = ((TaskSnapshot) getSnapshot(new Integer(
				numBaseline)));
		if (snapshot == null)
			return 0;
		return snapshot.work(start, end);
	}

	//	public long getWork() {
	//		DoubleSum sumFunctor = new DoubleSum() {
	//
	//			protected double getValueForElement(Object object) {
	//				return ((Assignment)object).calcAll(Assignment.WORK);
	//			}};
	//
	//		CollectionUtils.forAllDo(getAssignments(),sumFunctor);
	//		return (long) sumFunctor.getValue();
	//	}



	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.task.TaskSpecificFields#getResourceInitials()
	 */
	public String getResourceInitials() {
		return AssociationListFormat.getInstance(
				AssignmentFormat
						.getInstance(AssociationFormatParameters.getInstance(
								this, true, Configuration
										.getFieldFromId("Field.initials"),
								false, false))).format(getAssignments());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.task.TaskSpecificFields#setResourceInitials()
	 */
	public void setResourceInitials(String resourceInitials)
			throws FieldParseException {
		getAssignments().setAssociations(
				resourceInitials,
				AssignmentFormat
						.getInstance(AssociationFormatParameters.getInstance(
								this, true, Configuration
										.getFieldFromId("Field.initials"),
								false, false)));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.task.TaskSpecificFields#getResourcePhonetics()
	 */
	public String getResourcePhonetics() {
		return AssociationListFormat.getInstance(
				AssignmentFormat
						.getInstance(AssociationFormatParameters.getInstance(
								this, true, Configuration
										.getFieldFromId("Field.phonetics"),
								false, true))).format(getAssignments());

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.task.TaskSpecificFields#getResourceGroup()
	 */
	public String getResourceGroup() {
		return AssociationListFormat.getInstance(
				AssignmentFormat.getInstance(AssociationFormatParameters
						.getInstance(this, true, Configuration
								.getFieldFromId("Field.group"), false, false)))
				.format(getAssignments());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.task.TaskSpecificFields#getResourceNames()
	 */
	public String getResourceNames() {
		return AssociationListFormat.getInstance(
				AssignmentFormat.getInstance(AssociationFormatParameters
						.getInstance(this, true, Configuration
								.getFieldFromId("Field.name"), true, true)))
				.format(getAssignments());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.task.TaskSpecificFields#setResourceNames(java.lang.String)
	 */
	public void setResourceNames(String resourceNames)
			throws FieldParseException {
		getAssignments().setAssociations(
				resourceNames,
				AssignmentFormat.getInstance(AssociationFormatParameters
						.getInstance(this, true, Configuration
								.getFieldFromId("Field.name"), true, true)));

	}

	public double getUnits() {
		if (getAssignments().isEmpty())
			return 0;
		long duration = getDurationMillis();
		if (duration == 0.0)
			return 1.0D; // degeneratate case
		if (!isInitialized()) // the case when reading a file, don't boether to
							  // calculate
			return 1.0;
		long work = calcWork();
		if (work == 0) // degenerate case with no work yet
			return 1.0;
		return ((double) work) / duration;
	}

	public double getRemainingUnits() {
		if (getAssignments().isEmpty())
			return 0;
		long duration = Duration.millis(getRemainingDuration());
		if (duration == 0.0)
			return 1.0D; // degeneratate case
		if (!isInitialized()) // the case when reading a file, don't boether to
							  // calculate
			return 1.0;
		long work = getRemainingWork(null);
//		if (work == 0) // degenerate case with no work yet
//			return 1.0;
		return ((double) work) / duration;

	}


	public void setWork(long work, FieldContext context) {

		if (FieldContext.hasInterval(context)) {
			Iterator i = getAssignments().iterator();
			while (i.hasNext()) {
				Assignment assignment = (Assignment) i.next();
				assignment.setWork(work,context);
			}
		} else {
			setWork(work);
		}
	}

	public void setWork(long work) {
		work = Duration.millis(work);
		if (hasLaborAssignment() && work < 60000) {
			work *= Duration.timeUnitFactor(TimeUnit.HOURS);
		}
		long remainingWork = work - getActualWork(null);
		getSchedulingRule().adjustRemainingWork(this, remainingWork, true);
	}

	public long calcWork() {
		if (!hasRealAssignments()) // avoid treating dummy assignment
			return 0;

		return getWork(null);
	}

/**
 * The the most highly loaded assignment's units.  This is used for calculating new assignment's
 * units for fixed-duration effort-driven tasks
 * @return
 */
	public double getMostLoadedAssignmentUnits() {
		double result = 0;
		Iterator i = getAssignments().iterator();
		while (i.hasNext())
			result = Math.max(result,((Assignment) i.next()).getLaborUnits());

		return result;
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.assignment.Allocation#adjustDuration(long)
	 */
	public void adjustRemainingDuration(long newDuration, boolean doChildren) {
//~~		setRawDuration(newDuration); // keep units
//hk		long newRemainingDuration = Duration.millis(newDuration) - getActualDuration(); // assignments dont treqt
		long newRemainingDuration = Duration.millis(newDuration); // - getActualDuration(); // assignments dont treqt
													// units
		Iterator i = getAssignments().iterator();
		while (i.hasNext())
			((Assignment) i.next()).adjustRemainingDurationIfWorkingAtTaskEnd(newRemainingDuration);

	}

	/**
	 * Called when an assignment value is modified. We want the task details to
	 * be modified without changing the assignment details
	 *
	 * @param deltaAdded
	 */
	public void adjustUnitsDelta(double deltaAdded) {
		getSchedulingRule().adjustRemainingUnits(this, getRemainingUnits() + deltaAdded,
				getRemainingUnits(), false, false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.assignment.Allocation#adjustUnits(double)
	 */
	public void adjustRemainingUnits(double newRemainingUnits, double oldRemainingUnits, boolean doChildren, boolean conserveTotalUnits) {

		if (!doChildren)
			return;
		double multiplier = 1;
		if (conserveTotalUnits) {
			multiplier= oldRemainingUnits / newRemainingUnits;
		}

		double u = newRemainingUnits;
		double remaining = getRemainingUnits();
		double factor= u/remaining;
		Iterator i = getAssignments().iterator();
		while (i.hasNext()) {
			Assignment assignment = (Assignment) i.next();
			double r = assignment.getLaborUnits();
//			if (!assignment.isLabor())
//				continue;
			if (conserveTotalUnits)
				getSchedulingRule().adjustRemainingUnits(assignment, assignment.getRemainingLaborUnits() * multiplier, assignment.getRemainingLaborUnits(), false, false);
			else {
				getSchedulingRule().adjustRemainingUnits(assignment,factor*r,r, false, false);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.assignment.Allocation#adjustWork(double)
	 */
	public void adjustRemainingWork(double multiplier, boolean doChildren) {
//		long newDuration = (long) (getDurationMillis() * multiplier);
//~~		setRawDuration(newDuration);
		//need to always do children regardless of doChildren flag
		Iterator i = getAssignments().iterator();
		while (i.hasNext()) {
			Assignment assignment = (Assignment) i.next();
			if (!assignment.isLabor())
				continue;
			getSchedulingRule().adjustRemainingWork(assignment,(long) (assignment.getRemainingWork()*multiplier),false);
		}
	}

	/**
	 * Gets a (singleton) instance of the scheduling rule to use for the task
	 *
	 * @return scheduling rule to use in adjust...() calculations
	 */
	public SchedulingRule getSchedulingRule() {
		return SchedulingType.getSchedulingRuleInstance(getSchedulingType());

	}

	public boolean isReadOnlyUnits(FieldContext fieldContext) {
		return true;
	}
	public long getCompletedThrough() {
		long start = getStart();
		if (start == 0)
			return 0;
		long actualDuration = DateTime.closestDate(getDurationMillis() * getPercentComplete());
		return getEffectiveWorkCalendar().add(start,actualDuration,true);
	}



	/**
	 * Stop is the earliest completion date of the assignments
	 * @return
	 */
	public long getStop() {
//		if (isWbsParent( )) {
//			long start = getStart();
//			if (start == 0)
//				return 0;
//			long actualDuration = DateTime.closestDate(getDurationMillis() * getPercentComplete());
//			return getEffectiveWorkCalendar().add(start,actualDuration,true);
//		}
		return getEarliestStop();
		//&&&&&
//		long stop = 0;
//		Assignment assignment;
//		Iterator i = getAssignments().iterator();
//		while (i.hasNext()) {
//			assignment = (Assignment)i.next();
//			stop = Math.max(stop,assignment.getStop());
//		}
//		return stop;
	}

	//Used when an assignment advancement changes
	public void adjustActualStartFromAssignments() {
		Assignment assignment;
		Iterator i = getAssignments().iterator();
		long start = 0L;
		while (i.hasNext()) {
			assignment = (Assignment)i.next();
			if (assignment.getPercentComplete() > 0.0D ) {
				start= getStart();
				break;
			}
		}
//		System.out.println("adjusting actual start to " + new java.util.Date(start));
		setActualStart(start);
		assignParentActualDatesFromChildren();

	}

	/**
	 * @param stop
	 */
	public void setStop(long stop) {
		if (stop == getStop())
			return;
		stop = DateTime.closestDate(stop);
		stop = Math.min(stop,getEnd());

		Iterator i = getAssignments().iterator();
		Assignment assignment;
		long computedActualStart = Long.MAX_VALUE;
		long assignmentActualStart;
		while (i.hasNext()) {
			assignment = (Assignment)i.next();
			assignment.setStop(stop);
			assignmentActualStart = assignment.getActualStart();
			if (assignmentActualStart != 0 && assignmentActualStart < computedActualStart)
				computedActualStart = assignmentActualStart;
		}
		if (computedActualStart == Long.MAX_VALUE)
			computedActualStart = 0;
		setActualStart(computedActualStart);
		assignParentActualDatesFromChildren();

		// if % complete went down to 0, then the plan changed and need to recalculate all.
		if (computedActualStart == 0) {
			getDocument().getObjectEventManager().fireUpdateEvent(this, this,
					Configuration.getFieldFromId("Field.start"));
		} else {
			//TODO duplicate event
			//TODO in the case of progress update this event is useless since critical path runs after.
			getProject().fireScheduleChanged(this, ScheduleEvent.ACTUAL, this);
		}

	}
	/**
	 * @return
	 */
	public long getResume() {
		long resume = Long.MAX_VALUE;
		Assignment assignment;
		Iterator i = getAssignments().iterator();
		while (i.hasNext()) {
			assignment = (Assignment)i.next();
			resume = Math.min(resume,assignment.getResume());
		}
		return resume;
	}
	/**
	 * @param resume
	 */
	public void setResume(long resume) {
		Assignment assignment;
		Iterator i = getAssignments().iterator();
		while (i.hasNext()) {
			assignment = (Assignment)i.next();
			assignment.setResume(resume);
		}
	}




	private void setStopNoExtend(long stop) {
		//TODO figure out
		long start = getStart();
		if (stop < start) {// don't allow completion before start
			setActualDuration(0);
			stop = start;
		} else {
			long duration = getEffectiveWorkCalendar().compare(stop,
					start, false);
			duration = Math.min(duration, getDurationMillis()); // don't ever
																// change finish
			setActualDuration(duration);
		}
//		scheduleWindow.setStop(stop);
	}

	/***************************************************************************
	 * TimeDistributedData T********
	 **************************************************************************/
	/**
	 * For parent tasks, we don't want to count their one day of work
	 */
	private boolean isParentWithoutAssignments() {
		return (isWbsParent() && !hasRealAssignments());
	}

	public double cost(long start, long end) {
		if (isParentWithoutAssignments())
			return 0.0D;
		return ((TaskSnapshot) getCurrentSnapshot()).cost(start, end);
	}

	public long work(long start, long end) {
		if (isParentWithoutAssignments())
			return 0L;
		return ((TaskSnapshot) getCurrentSnapshot()).work(start, end);
	}

	public double actualCost(long start, long end) {
		if (isParentWithoutAssignments())
			return 0.0D;

		return ((TaskSnapshot) getCurrentSnapshot()).actualCost(start, end);
	}

	public long actualWork(long start, long end) {
		if (isParentWithoutAssignments())
			return 0L;
		return ((TaskSnapshot) getCurrentSnapshot()).actualWork(start, end);
	}
	public long remainingWork(long start, long end) {
		if (isParentWithoutAssignments())
			return 0L;
		return ((TaskSnapshot) getCurrentSnapshot()).remainingWork(start, end);
	}

	public double baselineCost(long start, long end) {
		if (getBaselineSnapshot() == null)
			return 0;

		return getBaselineSnapshot().cost(start, end);
	}

	public long baselineWork(long start, long end) {
		if (getBaselineSnapshot() == null)
			return 0;
		return getBaselineSnapshot().work(start, end);
	}

	/***************************************************************************
	 * EarnedValueValues
	 **************************************************************************/

	public double acwp(long start, long end) {
		return ((TaskSnapshot) getCurrentSnapshot()).acwp(start, end);
	}

	public double bac(long start, long end) {
		return ((TaskSnapshot) getCurrentSnapshot()).bac(start, end);
	}

	public double bcwp(long start, long end) {
		return ((TaskSnapshot) getCurrentSnapshot()).bcwp(start, end);
	}

	public double bcws(long start, long end) {
		return ((TaskSnapshot) getCurrentSnapshot()).bcws(start, end);
	}

	boolean isInRange(long start, long finish) {
		long s = getStart();
		return (finish > s && start < getEnd());
	}

	private boolean isFieldHidden(FieldContext fieldContext) {
		return fieldContext != null && !isInRange(fieldContext.getStart(),fieldContext.getEnd());
	}

	private boolean isBaselineFieldHidden(int numBaseline,FieldContext fieldContext) {
		TaskSnapshot baseline = (TaskSnapshot) getSnapshot(new Integer(numBaseline));
		if (baseline == null)
			return true;

		 if (fieldContext == null) // the baseline exists, but no time range
			 return false;
		 return (fieldContext.getStart() >= baseline.getCurrentSchedule().getFinish() || fieldContext.getEnd() <= baseline.getCurrentSchedule().getStart());
	}

	private boolean isEarnedValueFieldHidden(FieldContext fieldContext) {
		if (isFieldHidden(fieldContext))
			return true;
		if (fieldContext == null)
			return false;
		return project.getStatusDate() < fieldContext.getStart();
	}

	/***************************************************************************
	 * Time Distributed Fields
	 **************************************************************************/
	public boolean fieldHideCost(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}
	public boolean fieldHideWork(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}
	public boolean fieldHideActualCost(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}
	public boolean fieldHideActualWork(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}
	public boolean fieldHideBaselineCost(int numBaseline,FieldContext fieldContext) {
		return isBaselineFieldHidden(numBaseline,fieldContext);
	}
	public boolean fieldHideBaselineWork(int numBaseline,FieldContext fieldContext) {
		return isBaselineFieldHidden(numBaseline,fieldContext);
	}
	public boolean fieldHideAcwp(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}
	public boolean fieldHideBac(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}
	public boolean fieldHideBcwp(FieldContext fieldContext) {
		return isEarnedValueFieldHidden(fieldContext);
	}
	public boolean fieldHideBcws(FieldContext fieldContext) {
		return isEarnedValueFieldHidden(fieldContext);
	}
	public boolean fieldHideCv(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}
	public boolean fieldHideSv(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}
	public boolean fieldHideEac(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}
	public boolean fieldHideVac(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}
	public boolean fieldHideCpi(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}
	public boolean fieldHideSpi(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}
	public boolean fieldHideCvPercent(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}
	public boolean fieldHideSvPercent(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}
	public boolean fieldHideTcpi(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}

	public double getCost(FieldContext fieldContext) {
		return getFixedCost(fieldContext)
				+ cost(FieldContext.start(fieldContext), FieldContext
						.end(fieldContext));
	}

	public long getWork(FieldContext fieldContext) {
		return work(FieldContext.start(fieldContext), FieldContext
				.end(fieldContext));
	}
	public double getActualFixedCost(FieldContext fieldContext) {
		return fixedCost(FieldContext.start(fieldContext), Math.min(getStop(),FieldContext // only up to completion
				.end(fieldContext)));
	}
	public double getFixedCost(FieldContext fieldContext) {
		if (!FieldContext.hasInterval(fieldContext))
			return ((TaskSnapshot) getCurrentSnapshot()).getFixedCost();

		return fixedCost(FieldContext.start(fieldContext), FieldContext
				.end(fieldContext));
	}

	public double actualFixedCost(long start, long end) {
		return fixedCost(start,Math.min(getStop(),end));
	}

	/** Calculate the fixed cost for the task given its accrual type and percent complete
	 */
	public double fixedCost(long start, long end) {
		long taskStart = getStart();
		long taskEnd = getEnd();
		double fixed = 0.0;
		double fixedCost = getFixedCost();
		if (getFixedCostAccrual() == Accrual.START) {
			if (taskStart >= start && taskStart <= end) // if task starts in this range
				fixed = fixedCost;
		} else if (getFixedCostAccrual() == Accrual.PRORATED) {
			// find overlapping actual time
			start = Math.max(start,taskStart);
			end = Math.min(end,taskEnd);
			if (start < end) { // if valid range
				long overlappingDuration = getEffectiveWorkCalendar().compare(end,start,false);
				double fraction = ((double)overlappingDuration) / getDurationMillis();
				fixed = fixedCost * fraction;
			}
		} else  { // END accrual by default
			if (taskEnd >= start && taskEnd <= end) // if task ends in this range
				fixed = fixedCost;
		}
		return fixed;
	}
	public boolean fieldHideActualFixedCost(FieldContext fieldContext) {
		return false;
	}



	public double getActualCost(FieldContext fieldContext) {
		return getActualFixedCost(fieldContext) + actualCost(FieldContext.start(fieldContext), FieldContext
				.end(fieldContext));
	}

	public long getActualWork(FieldContext fieldContext) {
		return actualWork(FieldContext.start(fieldContext), FieldContext
				.end(fieldContext));
	}
	public long getRemainingWork(FieldContext fieldContext) {
		return remainingWork(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public long getRemainingWork() {
		return getRemainingWork(null);
	}
	public double getRemainingCost(FieldContext fieldContext) {
		return getCost(fieldContext) - getActualCost(fieldContext);
	}


	//Baseline versions
	public double getBaselineCost(int numBaseline, FieldContext fieldContext) {
		TaskSnapshot snapshot = (TaskSnapshot) getSnapshot(new Integer(
				numBaseline));
		if (snapshot == null)
			return 0.0D;
		return ((TaskSnapshot) getSnapshot(new Integer(numBaseline))).cost(
				FieldContext.start(fieldContext), FieldContext
						.end(fieldContext));
	}

	public long getBaselineWork(int numBaseline, FieldContext fieldContext) {
		TaskSnapshot snapshot = (TaskSnapshot) getSnapshot(new Integer(
				numBaseline));
		if (snapshot == null)
			return 0L;
		return ((TaskSnapshot) getSnapshot(new Integer(numBaseline))).work(
				FieldContext.start(fieldContext), FieldContext
						.end(fieldContext));
	}

	/***************************************************************************
	 * Earned Value Fields
	 **************************************************************************/
	public double getAcwp(FieldContext fieldContext) {
		return acwp(FieldContext.start(fieldContext), FieldContext
				.end(fieldContext));
	}

	public double getBac(FieldContext fieldContext) {
		return bac(FieldContext.start(fieldContext), FieldContext
				.end(fieldContext));
	}

	public double getBcwp(FieldContext fieldContext) {
		return bcwp(FieldContext.start(fieldContext), FieldContext
				.end(fieldContext));
	}

	public double getBcws(FieldContext fieldContext) {
		return bcws(FieldContext.start(fieldContext), FieldContext
				.end(fieldContext));
	}

	public double getCv(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().cv(this,
				FieldContext.start(fieldContext),
				FieldContext.end(fieldContext));
	}

	public double getSv(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().sv(this,
				FieldContext.start(fieldContext),
				FieldContext.end(fieldContext));
	}

	public double getEac(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().eac(this,
				FieldContext.start(fieldContext),
				FieldContext.end(fieldContext));
	}

	public double getVac(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().vac(this,
				FieldContext.start(fieldContext),
				FieldContext.end(fieldContext));
	}

	public double getCpi(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().cpi(this,
				FieldContext.start(fieldContext),
				FieldContext.end(fieldContext));
	}

	public double getSpi(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().spi(this,
				FieldContext.start(fieldContext),
				FieldContext.end(fieldContext));
	}
	public double getCsi(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().csi(this,FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}

	public double getCvPercent(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().cvPercent(this,
				FieldContext.start(fieldContext),
				FieldContext.end(fieldContext));
	}

	public double getSvPercent(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().svPercent(this,
				FieldContext.start(fieldContext),
				FieldContext.end(fieldContext));
	}

	public double getTcpi(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().tcpi(this,
				FieldContext.start(fieldContext),
				FieldContext.end(fieldContext));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.assignment.HasAssignments#calcDataBetween(java.lang.Object,
	 *      com.projity.algorithm.TimeIteratorGenerator,
	 *      com.projity.algorithm.CalculatedValues)
	 */
	public void calcDataBetween(Object type, TimeIteratorGenerator generator,
			CalculatedValues values) {
		((TaskSnapshot) getCurrentSnapshot()).calcDataBetween(type, generator,
				values);

	}


	public void setPercentComplete(double percentComplete) {
		if (percentComplete > 1.0) {
			System.out.println("percent complete more than 100%");
			percentComplete = 1.0;
		} else if (percentComplete < 0){
			System.out.println("percent complete less than 0%");
			percentComplete = 0.0;
		}
		if (isZeroDuration()) { // special case for completion on milestones
			final double pc = percentComplete;
			Iterator i= getAssignments().iterator();
			while (i.hasNext()) {
				((Assignment)i.next()).setPercentComplete(pc);
			}
		} else {
			long actualDuration = DateTime.closestDate(getDurationMillis() * percentComplete);
			setActualDuration(actualDuration);
			long stop = getEffectiveWorkCalendar().add(getStart(), actualDuration, false);
			DeepChildWalker.recursivelyTreatBranch(getProject().getTaskOutline(),
					this, new NumberClosure(stop) {
						public void execute(Object arg0) {
							if (arg0 == null) {
								return;
							}
							Object nodeObject = ((Node) arg0).getImpl();
							if (nodeObject instanceof NormalTask) { // do not treat assignments
								NormalTask task = ((NormalTask)nodeObject);
								task.setStop(Math.min(longValue(),task.getEnd())); // do within range of task
							}
						}
					});
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.task.TaskSpecificFields#getPercentWorkComplete()
	 */
	public double getPercentWorkComplete() {
		//		NodeModel nodeModel = getProject().getTaskOutline();
		//		Node node = nodeModel.search(this);
		//		Number value = (Number)
		// Configuration.getFieldFromId("Field.work").getValue(node,nodeModel,null);
		//		if (value.doubleValue() == 0)
		//			return 0;
		//		Number actualValue = (Number)
		// Configuration.getFieldFromId("Field.actualWork").getValue(node,nodeModel,null);
		//		return actualValue.doubleValue() / value.doubleValue();
		long work = calcSummedWork();
		if (work == 0)
			return 0;
		else
			return ((double) calcSummedActualWork()) / work;
	}

	private long calcSummedWork() {
		NodeModel nodeModel = getProject().getTaskOutline();
		Node node = nodeModel.search(this);
		if (node == null)
			return 0;
		Number value = (Number) Configuration.getFieldFromId("Field.work")
				.getValue(node, nodeModel, null);
		return value.longValue();
	}

	private long calcSummedActualWork() {
		NodeModel nodeModel = getProject().getTaskOutline();
		Node node = nodeModel.search(this);
		Number value = (Number) Configuration
				.getFieldFromId("Field.actualWork").getValue(node, nodeModel,
						null);
		return value.longValue();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.task.TaskSpecificFields#setPercentWorkComplete(double)
	 */
	public void setPercentWorkComplete(double percentWorkComplete) {
		if (percentWorkComplete < 0)
			percentWorkComplete = 0;
		if (percentWorkComplete > 1)
			percentWorkComplete = 1;
		double workValue = percentWorkComplete * calcSummedWork();
		//		System.out.println("work value is " +
		// DurationFormat.format((long)workValue) +" get work null is " +
		// DurationFormat.format(calcSummedWork()));

		long date = ReverseQuery.getDateAtValue(WORK, this, workValue, true); // allow use of default assignments
		DeepChildWalker.recursivelyTreatBranch(getProject().getTaskOutline(),
				this, new NumberClosure(date) {
					public void execute(Object arg0) {
						if (arg0 == null)
							return;
						Object nodeObject = ((Node) arg0).getImpl();
						if (nodeObject instanceof NormalTask) // do not treat assignments
							((NormalTask)nodeObject).setStopNoExtend(getLongValue());
					}
				});

	}

//	/*
//	 * (non-Javadoc)
//	 *
//	 * @see com.projity.pm.criticalpath.ScheduleWindow#getScheduleChildren()
//	 */
//	public Collection getScheduleChildren() {
//		return getWbsChildrenNodes();
//	}

	/**
	 * Cleans up all links to and form this task and removes all assignments,
	 * including baseline ones.
	 *
	 * @param eventSource -
	 *            if not null, then events will be sent indicating the removal
	 *            of links and assignments
	 */
	void cleanUp(Object eventSource,boolean deep,boolean undo,boolean cleanDependencies) {
		super.cleanUp(eventSource,deep,undo,cleanDependencies); // gets rid of dependencies

		// for all snapshots
		if (deep){
			TaskSnapshot snapshot;
			for (int i = 0; i < Settings.numBaselines(); i++) {
				Integer snapshotId = new Integer(i);
				snapshot = (TaskSnapshot) getSnapshot(snapshotId);
				if (snapshot != null) {
					// send events only for current snapshot
					Object useEventSource = (getCurrentSnapshot() == snapshot) ? eventSource
							: null;

					LinkedList toRemove = new LinkedList(); //fix
					AssignmentService.getInstance().remove(
							snapshot.getAssignments(), toRemove);
					AssignmentService.getInstance().remove(toRemove, useEventSource,false);

					if (snapshot != getCurrentSnapshot())
						getProject().fireBaselineChanged(eventSource, this,
								snapshotId, false);
				}

			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.assignment.HasTimeDistributedData#childrenToRollup()
	 */
	public Collection childrenToRollup() {
		return ((TaskSnapshot) getCurrentSnapshot()).getHasAssignments()
				.childrenToRollup();
	}

	// some functions useful for API
	public double getCost() {
		return getCost(null);
	}

	public double getBaselineCost() {
		return getBaselineCost(0, null);
	}

	public double getBaselineCost(int number) {
		return getBaselineCost(number, null);
	}

	public double getWork() {
		return getWork(null);
	}

	public double getBaselineWork() {
		return getBaselineWork(0, null);
	}

	public double getBaselineWork(int number) {
		return getBaselineWork(number, null);
	}


/**
 *  Useful for drawing bars
 */
	public long getTotalSlackStart() {
		return (getConstraintType() == ConstraintType.ALAP) ? getEarlyStart() : getEarlyFinish();

	}
/**
 *  Useful for drawing bars
 */	public long getTotalSlackEnd() {
		return (getConstraintType() == ConstraintType.ALAP) ? getLateStart() : getLateFinish();
	}

	/**
	 * Offset the given date by the duration of the remaining duration.
	 */
	public long calcOffsetFrom(long startDate, long dependencyDate, boolean ahead, boolean remainingOnly, boolean useSooner) {

		//		This is a task based implementation- for parents dont use their assignments
		if (isWbsParent()) {
			long d = remainingOnly ? Duration.millis(getRemainingDuration()) : getDurationMillis();
			if (!ahead)
				d = -d;
			return getEffectiveWorkCalendar().add(startDate,d,useSooner);
		}
//
//
//		This is an assignment based implementation

		Iterator i = getAssignments().iterator();
		long result;
		Assignment assignment;
		if (startDate < 0)
			result = ahead ? Long.MIN_VALUE : 0;
		else
			result = ahead ? 0 : Long.MAX_VALUE;
		while (i.hasNext()) {
			assignment = (Assignment)i.next();
			long offsetDate = assignment.calcOffsetFrom(startDate,dependencyDate,ahead,remainingOnly,useSooner);
			result = ahead ? Math.max(result,offsetDate) : Math.min(result,offsetDate);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#calcActiveAssignmentDuration(com.projity.pm.calendar.WorkCalendar)
	 */
	public long calcActiveAssignmentDuration(WorkCalendar workCalendarToUse) {
		return ((TaskSnapshot) getCurrentSnapshot()).calcActiveAssignmentDuration(workCalendarToUse);
	}

/**
 * Move remaining work to date - used when doing a project update for this task
 */
	public void moveRemainingToDate(long date) {
		date = getEffectiveWorkCalendar().adjustInsideCalendar(date,false);
		if (getActualStart() == 0L)
			setStart(date); // if not started, change start
		else if (inProgress()) {
			Iterator i = getAssignments().iterator();
			Assignment assignment;
			while (i.hasNext()) {
				assignment = (Assignment)i.next();
				assignment.moveRemainingToDate(date);
			}
		} // do nothing for completed tasks
	}
	/* (non-Javadoc)
	 * @see com.projity.pm.scheduling.Schedule#moveInterval(java.lang.Object, long, long, com.projity.pm.scheduling.ScheduleInterval)
	 */
	public void moveInterval(Object eventSource, long start, long end, ScheduleInterval oldInterval, boolean isChild) {
		WorkCalendar cal = getEffectiveWorkCalendar();
		start = cal.adjustInsideCalendar(start,false);
		boolean shifting = cal.compare(start,oldInterval.getStart(),false) != 0;
		long assignmentStart = getEarliestAssignmentStart();
		long amountFromStart = cal.compare(oldInterval.getStart(),assignmentStart,false); // possible that they are not the same but there is no working time between them
		if (shifting && amountFromStart == 0L) { // see if first bar shifted -The first bar is drawn from the first assignment and not from the task start.
			// To figure out the new task start, see how much the shift of this bar is, then apply that difference to the task start
			long shift = cal.compare(start,assignmentStart,false);
			long newTaskStart = cal.add(getStart(),shift,false);
			setStart(newTaskStart);
		} else {
			long amount =cal.compare(end,oldInterval.getEnd(),false);
			if (amount == 0L) // skip if nothing moved
				return;

			Iterator i = getAssignments().iterator();
			Assignment assignment;
			while (i.hasNext()) {
				assignment = (Assignment)i.next();
				assignment.moveInterval(eventSource,start,end,oldInterval, true);
			}
		}
		setRawDuration(getDurationMillis()); // this fixes all sorts of pbs

		recalculate(eventSource); // need to recalculate
		assignParentActualDatesFromChildren();


//		//Undo
//		UndoableEditSupport undoableEditSupport=getProject().getUndoController().getEditSupport();
//		if (undoableEditSupport!=null&&!(eventSource instanceof UndoableEdit)){
//			undoableEditSupport.postEdit(new ScheduleEdit(this,new ScheduleInterval(start,end),oldInterval,isChild,eventSource));
//		}


	}


	/* (non-Javadoc)
	 * @see com.projity.pm.scheduling.Schedule#split(java.lang.Object, long, long)
	 */
	public void split(Object eventSource, long from, long to) {
		from = getEffectiveWorkCalendar().adjustInsideCalendar(from,false);
		to = getEffectiveWorkCalendar().adjustInsideCalendar(to,false);

		if (from == to) { // if from is same as two, split one day
			to = getEffectiveWorkCalendar().add(from,CalendarOption.getInstance().getMillisPerDay(),false);
		}

		Iterator i = getAssignments().iterator();
		Assignment assignment;
		while (i.hasNext()) {
			assignment = (Assignment)i.next();
			assignment.split(eventSource,from,to);
		}
		recalculate(eventSource); // need to recalculate
		assignParentActualDatesFromChildren();

	}

	protected transient static BarClosure barClosureInstance = new BarClosure();
	public void consumeIntervals(IntervalConsumer consumer) {
		if (isWbsParent() || isSubproject()) { //TODO this shouldn't be needed since default assignment should be ok.  See why
			consumer.consumeInterval(new ScheduleInterval(getStart(),getEnd()));
			return;
		}
		barClosureInstance.initialize(consumer,this);
		forEachWorkingInterval(barClosureInstance,true, getEffectiveWorkCalendar());

		// Below is a hack to prevent hanging on void node promotion
		if (barClosureInstance.getCount() == 0) { // if no bars drawn
			consumer.consumeInterval(new ScheduleInterval(getStart(),getEnd()));
		}
	}


/**
 * Overloads task setEnd but calls it
 */	public void setEnd(long end) {
 		long start = getStart();
 		if (start == 0) { // if the end date is entered on a new line creating the task need to set the start correctly
 			start = CalendarOption.getInstance().makeValidStart(DateTime.midnightToday(),true);
 			getCurrentSchedule().setStart(start);
 		}
 		end = CalendarOption.getInstance().makeValidEnd(end, true);
 		if (end < start)
 			end = start;
		long oldEnd = getEnd();
		if (end != oldEnd) {
			super.setEnd(end);
			Iterator i = getAssignments().iterator();
			Assignment assignment;
			while (i.hasNext()) {
				assignment = (Assignment)i.next();
				assignment.setEnd(end);
			}
//			System.out.println("Old End"  + new Date(oldEnd) + " input end " + new Date(end )+ " resulting End " + new Date(getEnd()) + " duration " + DurationFormat.format(getDuration()));
			setRawDuration(getDurationMillis());
		}
		assignParentActualDatesFromChildren();
	}


	/**
	 * @param actualStart
	 */
	public void setActualStart(long actualStart) {
		actualStart = getEffectiveWorkCalendar().adjustInsideCalendar(actualStart, false); //TODO not good if it starts off calendar

		setActualStartNoEvent(actualStart);
		markTaskAsNeedingRecalculation();
		getProject().fireScheduleChanged(this, ScheduleEvent.ACTUAL, this);
	}

	public void setActualStartNoEvent(long actualStart) {
		long old = getActualStart();
		if (actualStart == old)
			return;
//		if (actualStart != 0) {
//			if (getPercentComplete() == 0) {
//				currentSchedule.setPercentComplete(INSTANT_COMPLETION);
//				setPercentComplete(INSTANT_COMPLETION);
//			}
//			currentSchedule.setStart(actualStart);
//		}
		this.actualStart = actualStart;
		assignParentActualDatesFromChildren();

	}

	/* (non-Javadoc)
	 * @see com.projity.pm.task.TaskSpecificFields#isIgnoreResourceCalendar()
	 */
	public boolean isIgnoreResourceCalendar() {
		return ((TaskSnapshot) getCurrentSnapshot()).isIgnoreResourceCalendar();
	}


	/* (non-Javadoc)
	 * @see com.projity.pm.task.TaskSpecificFields#setIgnoreResourceCalendar(boolean)
	 */
	public void setIgnoreResourceCalendar(boolean ignoreResourceCalendar) {
		((TaskSnapshot) getCurrentSnapshot()).setIgnoreResourceCalendar(ignoreResourceCalendar);
	}

	public boolean isDefault(){
	    return this==UNASSIGNED;
	}

	private static short DEFAULT_VERSION=2;
	private short version=DEFAULT_VERSION;

	public short getVersion() {
		return version;
	}
/* The serialization version must be private. This lets subclasses call this code */
	protected void doWriteObject(ObjectOutputStream s) throws IOException {
	    s.defaultWriteObject();
	    hasKey.serialize(s);
	    customFields.serialize(s);
	    if (version<1) currentSchedule.serialize(s);
	    else{
	    	int sCount=0;
            for (int i=0;i<Settings.numBaselines();i++){
                TaskSnapshot snapshot=(TaskSnapshot)getSnapshot(new Integer(i));
                if (snapshot!=null) sCount++;
    	    }
            s.writeInt(sCount);
            for (int i=0;i<Settings.numBaselines();i++){
                TaskSnapshot snapshot=(TaskSnapshot)getSnapshot(new Integer(i));
                if (snapshot==null) continue;
                s.writeInt(i);
                snapshot.serialize(s);
            }
	    }
	}
	private void writeObject(ObjectOutputStream s) throws IOException {
		doWriteObject(s);
	}
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException  {
	    s.defaultReadObject();

	    hasKey=HasKeyImpl.deserialize(s,this);
	    customFields=CustomFieldsImpl.deserialize(s);
	    if (version<1)currentSchedule=TaskSchedule.deserialize(s);
	    else{
	    	snapshots = new SnapshottableImpl(Settings.numBaselines());
	    	int sCount=s.readInt();
            for (int i=0;i<sCount;i++){
            	int snapshotId=s.readInt();
                TaskSnapshot snapshot=TaskSnapshot.deserialize(s,this);
                setSnapshot(new Integer(snapshotId), snapshot);
            }
	    }

	    if(version<1) super.initializeTransientTaskObjects();
	    else super.initializeTransientTaskObjectsAfterDeserialization();
//	    barClosureInstance = new BarClosure();
//	    This shouldn't be called -hk 4/feb/05
//	    initializeDates();

	    version=DEFAULT_VERSION;
	}
	public Object clone(){
		Task task=(Task)super.clone();
//		task.barClosureInstance = new BarClosure();



		return task;
	}
	public void cloneTo(Task task){
		if (task instanceof NormalTask){
			NormalTask n=(NormalTask)task;
			n.estimated=estimated;
			n.priority = priority;
			n.version=version;
			n.workCalendar = workCalendar;
		}

		super.cloneTo(task);
	}

	public void serialize(ObjectOutputStream s) throws IOException {}

	public boolean isReadOnlyWork(FieldContext fieldContext) {
		if (!hasLaborAssignment())
			return true;
		if (fieldContext == null)
			return false;
		return !hasActiveAssignment(fieldContext.getStart(), fieldContext.getEnd());
	}

	public void setActualWork(long actualWork, FieldContext context) {

		if (FieldContext.hasInterval(context)) {
			Iterator i = getAssignments().iterator();
			while (i.hasNext()) {
				Assignment assignment = (Assignment) i.next();
				assignment.setActualWork(actualWork,context);
			}
		} else {
			long workValue = Duration.millis(actualWork);
			if (workValue == 0L) {
				setPercentComplete(0);
			} else {
				long  date = ReverseQuery.getDateAtValue(WORK, this, workValue, false);
				setStop(date);
			}
		}
	}

	public boolean isReadOnlyActualWork(FieldContext fieldContext) {
		return false;
	}

	public void setRemainingWork(long remainingWork, FieldContext fieldContext) {
		setActualWork(getWork(fieldContext) - Duration.millis(remainingWork), fieldContext);
	}

	public boolean isReadOnlyRemainingWork(FieldContext fieldContext) {
		return isReadOnlyWork(fieldContext);
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.TimeDistributedFields#setFixedCost(double, com.projity.field.FieldContext)
	 */
	public void setFixedCost(double fixedCost, FieldContext fieldContext) {
		if (!FieldContext.hasInterval(fieldContext))
			setFixedCost(fixedCost);
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.TimeDistributedFields#isReadOnlyFixedCost(com.projity.field.FieldContext)
	 */
	public boolean isReadOnlyFixedCost(FieldContext fieldContext) {
		return FieldContext.hasInterval(fieldContext);
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#isLabor()
	 */
	public boolean isLabor() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#hasLaborAssignment()
	 */
	public boolean hasLaborAssignment() {
		return ((TaskSnapshot) getCurrentSnapshot()).hasLaborAssignment();
	}

	public void setRawDuration(long duration) {
		currentSchedule.setRawDuration(duration);
	}


	public void setParentDuration() {
		if (!isWbsParent())
			return;
		currentSchedule.assignDatesFromChildren(null);
		long duration = getDurationMillis();
		getSchedulingRule().adjustRemainingDuration(this, duration - Duration.millis(getActualDuration()), true);
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#invalidateAssignmentCalendars()
	 */
	public void invalidateAssignmentCalendars() {
		((TaskSnapshot) getCurrentSnapshot()).invalidateAssignmentCalendars();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.HasCalendar#invalidateCalendar()
	 */
	public Document invalidateCalendar() {
		invalidateAssignmentCalendars();
		markTaskAsNeedingRecalculation();
		return getProject();
	}

	public boolean hasActiveAssignment(long start, long end) {
		return ((TaskSnapshot) getCurrentSnapshot()).hasActiveAssignment(start, end);
	}


	public boolean isInvalidIntersectionCalendar() {
		Iterator i = getAssignments().iterator();
		while (i.hasNext()) {
			if (((Assignment)i.next()).isInvalidIntersectionCalendar())
				return true;
		}
		return false;
	}

	public HasIndicators getIndicators() {
		return this;
	}

	public long getEarliestAssignmentStart() {
		return ((TaskSnapshot) getCurrentSnapshot()).getEarliestAssignmentStart();
	}

	public boolean isParentWithAssignments() {
		return isWbsParent() && hasRealAssignments();
	}

	public void setComplete(boolean complete) {
		ScheduleUtil.setComplete(this,complete);
	}

	public boolean applyTimesheet(Collection fieldArray, long timesheetUpdateDate) {
		return TimesheetHelper.applyTimesheet(getAssignments(),fieldArray,timesheetUpdateDate);
	}

	public long getLastTimesheetUpdate() {
		return TimesheetHelper.getLastTimesheetUpdate(getAssignments());
	}

	public boolean isPendingTimesheetUpdate() {
		return TimesheetHelper.isPendingTimesheetUpdate(getAssignments());
	}

	public int getTimesheetStatus() {
		return TimesheetHelper.getTimesheetStatus(getAssignments());
	}

	public String getTimesheetStatusName() {
		return TimesheetHelper.getTimesheetStatusName(getTimesheetStatus());
	}

	public final long getEarliestStop() {
		long stop = Long.MAX_VALUE;
		Schedule s;
		Object nodeImpl;
		if (isWbsParent()) {
			Collection children = getWbsChildrenNodes();
			Iterator i = children.iterator();
			while (i.hasNext()) {
				Object x = i.next();
				if (!(x instanceof Node))
					continue;
				nodeImpl = ((Node)x).getImpl();
				if (! (nodeImpl  instanceof Schedule))
					continue;
				s = (Schedule)nodeImpl;
				stop = Math.min(stop,s.getEarliestStop());
			}
		} else {
			Iterator i = getAssignments().iterator();
			while (i.hasNext()) {
				Assignment ass = (Assignment)i.next();
				stop = Math.min(stop,ass.getEarliestStop());
			}
		}
		return stop;
	}

	public void setCompletedThrough(long completedThrough) {
		completedThrough = DateTime.closestDate(completedThrough);
		completedThrough = Math.min(completedThrough,getEnd());
		if (completedThrough == getCompletedThrough())
			return;

		Iterator i = getAssignments().iterator();
		Assignment assignment;
		long computedActualStart = Long.MAX_VALUE;
		long assignmentActualStart;
		while (i.hasNext()) {
			assignment = (Assignment)i.next();
			assignment.setCompletedThrough(completedThrough);
			assignmentActualStart = assignment.getActualStart();
			if (assignmentActualStart != 0 && assignmentActualStart < computedActualStart)
				computedActualStart = assignmentActualStart;
		}
		if (computedActualStart == Long.MAX_VALUE)
			computedActualStart = 0;
		setActualStart(computedActualStart);
		assignParentActualDatesFromChildren();

		// if % complete went down to 0, then the plan changed and need to recalculate all.
		if (computedActualStart == 0) {
			getDocument().getObjectEventManager().fireUpdateEvent(this, this,
					Configuration.getFieldFromId("Field.start"));
		} else {
			//TODO duplicate event
			//TODO in the case of progress update this event is useless since critical path runs after.
			getProject().fireScheduleChanged(this, ScheduleEvent.ACTUAL, this);
		}
	}
	public long getFinishOffset() {
		return EarnedValueCalculator.getInstance().getFinishOffset(this);
	}

	public long getStartOffset() {
		return EarnedValueCalculator.getInstance().getStartOffset(this);
	}
	public ImageLink getBudgetStatusIndicator() {
		return EarnedValueCalculator.getInstance().getBudgetStatusIndicator(getCpi(null));
	}

	public ImageLink getScheduleStatusIndicator() {
		return EarnedValueCalculator.getInstance().getBudgetStatusIndicator(getSpi(null));
	}
	public Object backupDetail(){
		return backupDetail(null);
	}
	public Object backupDetail(Object snapshotId) {
		TaskSnapshot snapshot=(TaskSnapshot)((snapshotId==null)?getCurrentSnapshot():getSnapshot(snapshotId));
		TaskSnapshotBackup snapshotBackup=TaskSnapshotBackup.backup(snapshot,/*snapshotId!=null*/true);
		TaskBackup backup=new TaskBackup();
		backup.snapshot=snapshotBackup;
		backup.windowEarlyFinish=windowEarlyFinish;
		backup.windowEarlyStart=windowEarlyStart;
		backup.windowLateFinish=windowLateFinish;
		backup.windowLateStart=windowLateStart;
		backup.actualStart=actualStart;
		//TODO Backup other fields?
		return backup;
	}

	public void restoreDetail(Object source,Object backup,boolean isChild) {
		restoreDetail(source, backup, isChild,(TaskSnapshot)getCurrentSnapshot());
	}
	public void restoreDetail(Object source,Object backup,boolean isChild,TaskSnapshot snapshot) {
		TaskBackup b=(TaskBackup)backup;
		windowEarlyFinish=b.windowEarlyFinish;
		windowEarlyStart=b.windowEarlyStart;
		windowLateFinish=b.windowLateFinish;
		windowLateStart=b.windowLateStart;
		actualStart=b.actualStart;
		TaskSnapshotBackup.restore(snapshot, b.snapshot);
		if (!isChild) recalculate(source); //to send update event
	}

	private static abstract class ResultClosure implements Closure{
		boolean result=false;
	}
	public boolean renumber(final boolean localOnly){
		ResultClosure c=new ResultClosure(){
			public void execute(Object arg0) {
                result|=((Assignment)arg0).renumber(localOnly);
			}
		};
		boolean r=c.result;
		forSnapshotsAssignments(c, true);
		return r|hasKey.renumber(localOnly);
	}


	public boolean isLocal() {
		return hasKey.isLocal();
	}

	public void setLocal(boolean local) {
		hasKey.setLocal(local);
	}
	public boolean isSlipped() {
		long bf = getBaselineFinish();
		return bf != 0 && getEnd() > bf;
	}
	public void setTaskAssignementAndPredsDirty() {
		setDirty(true);
		Iterator a = getAssignments().iterator();
		while (a.hasNext())
			((Assignment)a.next()).setDirty(true);
		Iterator d=getDependencyList(true).iterator();
		while (d.hasNext())
			((Dependency)d.next()).setDirty(false);

	}
	//			task.setDirty(false);
//	task.setLastSavedStart(task.getStart()); //
//	task.setLastSavedFinish(task.getEnd());
//	Iterator j = task.getAssignments().iterator();
//	while (j.hasNext())
//		((Assignment)j.next()).setDirty(false);
//	j=task.getDependencyList(true).iterator();
//	while (j.hasNext())
//		((Dependency)j.next()).setDirty(false);
//}
	
	
	//claur import shortcuts
	public void setCurrentScheduleStart(long start){
		getCurrentSchedule().setStart(start);
	}
	
	public void setCurrentScheduleFinish(long finish){
		getCurrentSchedule().setFinish(finish);
	}

}