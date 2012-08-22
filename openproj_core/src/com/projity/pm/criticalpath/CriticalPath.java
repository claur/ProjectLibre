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
package com.projity.pm.criticalpath;

import java.util.Iterator;
import java.util.ListIterator;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.projity.configuration.Configuration;
import com.projity.document.Document;
import com.projity.document.ObjectEvent;
import com.projity.field.Field;
import com.projity.options.ScheduleOption;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.dependency.DependencyService;
import com.projity.pm.scheduling.ConstraintType;
import com.projity.pm.scheduling.ScheduleEvent;
import com.projity.pm.task.BelongsToDocument;
import com.projity.pm.task.NormalTask;
import com.projity.pm.task.Project;
import com.projity.pm.task.SubProj;
import com.projity.pm.task.Task;
import com.projity.strings.Messages;
import com.projity.transaction.MultipleTransaction;
import com.projity.util.DateTime;
import com.projity.util.Environment;
/**
 * The critical path calculation
 */
public class CriticalPath implements SchedulingAlgorithm {
	PredecessorTaskList predecessorTaskList = new PredecessorTaskList(this);
	private CriticalPathFields fieldUpdater = null;
	NormalTask finishSentinel;
	NormalTask startSentinel;
	Project project;
	boolean suspendUpdates = false; // flag to suspend updates when multiple objects are treated
	boolean needsReset = false; // flag to indicate that a reset is pending during suspended updates
	long earliestStart;
	long latestFinish;
	private boolean criticalPathJustChanged = false;
	private static Task traceTask;
	TaskSchedule.CalculationContext context;
	private static CriticalPath lastInstance;
	private static Field constraintTypeField = Configuration.getFieldFromId("Field.constraintType");
	public CriticalPath(Project project) {
		this.project = project;
		project.setSchedulingAlgorithm(this); 
//		project.addObjectListener(this);
//		project.getMultipleTransactionManager().addListener(this);
		fieldUpdater = CriticalPathFields.getInstance(this,project);

		startSentinel = new NormalTask(true,project); //local
		startSentinel.setDuration(0);
		startSentinel.setName("<Start>"); // name doesn't matter - useful for debugging purposes

		finishSentinel = new NormalTask(true,project); //local
		finishSentinel.setDuration(0);
		finishSentinel.setName("<End>"); // name doesn't matter - useful for debugging purposes


		setForward(isForward());
		setProjectBoundaries();
		initEarliestAndLatest();
	}
	private void setProjectBoundaries() {
		// update sentinels based on read in project
		
		if (isForward())  {
			startSentinel.setWindowEarlyStart(project.getStartConstraint());
			finishSentinel.setWindowLateFinish(0); //no end constraint
		} else {
			startSentinel.setWindowEarlyStart(0); // no start constraint
			finishSentinel.setWindowLateFinish(project.getEnd());
		}
		predecessorTaskList.getList().add(0,new PredecessorTaskList.TaskReference(startSentinel));
		predecessorTaskList.getList().add(new PredecessorTaskList.TaskReference(finishSentinel));
	}

	public void initialize(Object object) {
		project = (Project) object;
		predecessorTaskList.getList().clear(); // get rid of sentinels that are in lis
		predecessorTaskList.addAll(project.getTasks());
		initSentinelsFromTasks();
		setProjectBoundaries(); // put back sentinels
		
		calculate(false);
	}
	
	
/**
 * Initialize the sentinels so that the start sentinel has all start tasks as successors, and the end sentinel has all end tasks as predecessors
 *
 */
	private void initSentinelsFromTasks() {
		Iterator i = predecessorTaskList.listIterator();
		Task task;
		while (i.hasNext()) {
			task = ((PredecessorTaskList.TaskReference)i.next()).getTask();
			if (task.getPredecessorList().size() == 0)
				addStartSentinelDependency(task);
			if (task.getSuccessorList().size() == 0)
				addEndSentinelDependency(task);
		}
//		System.out.println("start sentinel successors");
//		startSentinel.getSuccessorList().dump(false);
//		System.out.println("end sentinel preds");
//		finishSentinel.getPredecessorList().dump(true);
		
	}
	
	public String getName() {
		return Messages.getString("Text.forwardScheduled");
	}
	private boolean isHonorRequiredDates() {
		return ScheduleOption.getInstance().isHonorRequiredDates();
	}

	private int getFreshCalculationStateCount() {
		return predecessorTaskList.getFreshCalculationStateCount();
	}
	private int getNextCalculationStateCount() {
		return predecessorTaskList.getNextCalculationStateCount();
	}
	public int getCalculationStateCount() {
		return predecessorTaskList.getCalculationStateCount();
	}
	private Task getBeginSentinel(boolean forward) {
		return forward ? startSentinel : finishSentinel;
	}
	private Task getEndSentinel(boolean forward) {
		return forward ? finishSentinel : startSentinel;
	}

	
	/**
	 * Run the critical path.  There are three possibilities:
	 * 1) The task that is modified does not affect the Critical Path.  In this case, only a single pass is performed and dates are set
	 * 2) The CP is modified, but the project contains no ALAP tasks.  In which case early and current dates are set in the first pass, and late in the second
	 * 3) The CP is modified and the project has ALAP tasks.  In which case, after both forward and backward passes are perfomed, a third pass sets current dates
	 * @param startTask
	 */
	private void fastCalc(Task startTask) {
		lastInstance =this;
		Task beginSentinel = getBeginSentinel(isForward());
		Task endSentinel = getEndSentinel(isForward());

		long firstBoundary = isForward() ? project.getStartConstraint() : -project.getEnd();
		boolean hasReverseScheduledTasks = predecessorTaskList.hasReverseScheduledTasks();

		context = new TaskSchedule.CalculationContext();
		context.stateCount = getNextCalculationStateCount();
		context.honorRequiredDates = isHonorRequiredDates();
		context.forward = isForward();
		context.boundary = firstBoundary;
		context.sentinel = endSentinel;
		context.earlyOnly = false;
		context.assign = false;
		context.scheduleType = isForward() ? TaskSchedule.EARLY : TaskSchedule.LATE;;
		context.pass = 0;
		boolean affectsCriticalPath = (startTask == beginSentinel) || startTask.getSchedule(context.scheduleType).affectsCriticalPath(context);
		
		boolean worstCase = affectsCriticalPath && hasReverseScheduledTasks;
		context.earlyOnly = worstCase;
		context.assign = true;
		context.pass = 1;
		criticalPathJustChanged = affectsCriticalPath;
		doPass(startTask,context); // always assign in first pass.  Dates may change in third pass
		
		if (affectsCriticalPath) {
			context.stateCount = getNextCalculationStateCount(); // backward pass treats next increment
			context.sentinel = endSentinel;
			long secondBoundary = -endSentinel.getSchedule(context.scheduleType).getBegin(); // sent bounds of end sentinel for backward pass
			context.boundary = secondBoundary;
			context.sentinel = beginSentinel;
			context.forward = !context.forward;
			context.assign = false;
			context.scheduleType = -context.scheduleType;
			context.pass++;
			doPass(null,context);

			//set project fields
			project.setStart(startSentinel.getEarlyStart());
			project.setEnd(finishSentinel.getEarlyFinish());
			
			if (hasReverseScheduledTasks) {
				context.stateCount = getNextCalculationStateCount(); // backward pass treats next increment
				context.forward = !context.forward;
				context.boundary = firstBoundary;
				context.sentinel = endSentinel;
				context.earlyOnly = false;
				context.assign = true;
				context.scheduleType = -context.scheduleType;
				context.pass++;
				doPass(null,context);
				
			}
			
		}
		getFreshCalculationStateCount(); // For next time;
	}
	
	private void doPass(Task startTask, TaskSchedule.CalculationContext context) {
		if (startTask != null) {
			startTask.getSchedule(context.scheduleType).invalidate();
			startTask.setCalculationStateCount(getCalculationStateCount());
		}

		PredecessorTaskList.TaskReference taskReference;
		boolean forward = context.forward;
		ListIterator i = forward ? predecessorTaskList.listIterator() : predecessorTaskList.reverseIterator();
		Task task;
		TaskSchedule schedule;

//		int count = 0;
//		long z = System.currentTimeMillis();
		boolean projectForward = project.isForward();
		while (forward ? i.hasNext() : i.hasPrevious()) {
			taskReference = (PredecessorTaskList.TaskReference)(forward ? i.next() : i.previous());
			traceTask = task = taskReference.getTask();
			context.taskReferenceType = taskReference.getType();
			schedule = task.getSchedule(context.scheduleType);
			if (!forward)
				context.taskReferenceType = -taskReference.getType();
				
			if (task.isReverseScheduled()) {//  reverse scheduled must always be calculated
				schedule.invalidate();
				task.setCalculationStateCount(context.stateCount);
			}
			if (task.getCalculationStateCount() >= context.stateCount) {
				schedule.calcDates(context);
				if (context.assign && (projectForward || !task.isWbsParent())) { // in reverse scheduling, I see some parents have 0 or 1 as their dates. This is a workaround.
					if (schedule.getBegin() != 0L && !isSentinel(task))
						earliestStart = Math.min(earliestStart, schedule.getStart());
					if (schedule.getEnd() != 0 && !isSentinel(task))
						latestFinish = Math.max(latestFinish, schedule.getFinish());
				}
				
//				schedule.dump();
			}
		}
//		System.out.println("pass forward=" + forward + " tasks:" + count + " time " + (System.currentTimeMillis() -z) + " ms");
	}

	public void calculate(boolean update) {
		calculate(update,null);
	}
	
	public void initEarliestAndLatest() {
		long date = project.getStartConstraint();
		if (date == 0)
			date = DateTime.midnightToday(); // this repairs empty start bug
		earliestStart = latestFinish = date;
	}
	private void _calculate(boolean update, Task task) {
		long t = System.currentTimeMillis();
		if (predecessorTaskList.getList().size() < 3) {// if no tasks, nothing to calculate.  This is needed to avoid a null pointer execption because of sentinels not having any preds/succs
			if (isForward())
				project.setEnd(project.getStartConstraint());
			else
				project.setStart(project.getEnd());
			return;

		}
		if (task == null) {
			task = getBeginSentinel(isForward());
		}
		fastCalc(task);
		if (update)
			fireScheduleChanged();
	}
	private void calculate(final boolean update, final Task task) {
		if (suspendUpdates)
			return;
		_calculate(update,task);
		// instead of calculating immediately, we can perhaps delay the calculation till the end of all other updates.  This may
		// cause problems in other cases where an immediate update is required, so I am commenting it out for now. See bug 225
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				_calculate(update,task);
//			}
//		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.projity.pm.criticalpath.SchedulingAlgorithm#getDefaultTaskConstraintType()
	 */
	public int getDefaultTaskConstraintType() {
		return ConstraintType.ASAP;
	}


		
	private void fireScheduleChanged() {
		((Project) project).fireScheduleChanged(this, ScheduleEvent.SCHEDULE);
	}

	private boolean updating = false;
	private synchronized CriticalPathFields getOrClearUpdater(boolean get) {
		if (get) {
			if (updating == true){
				System.out.println("interrupting update thread");
				fieldUpdater.interrupt(); // interrupt existing thread
				fieldUpdater = CriticalPathFields.getInstance(this,project); // make new one
			}
			updating = true;
			return fieldUpdater;
		} else {
			updating = false;
			return null;

		}
	}
//
//	public void scheduleTask(NormalTask task) {
//		calcEarlyStartAndFinish(task, task.getProject().getStart());
//		calcLateStartAndLateFinish(task, task.getProject().getEnd(), false);
//		task.calcStartAndFinish();
//	}


	
	
	/**
	 * Respond to object create/delete events
	 */
	public void objectChanged(ObjectEvent objectEvent) {
		if (!project.isInitialized() && !Environment.isImporting()) {
			System.out.println("Error - Message received when Project is not init" + project);
			return;
		}
		if (objectEvent.getSource() == this)
			return;
		Object changedObject = objectEvent.getObject();
		Task task = null;
		if (changedObject instanceof Task) {
			if (objectEvent.isCreate()) {
				predecessorTaskList
						.arrangeTask((Task) changedObject);
				return; // let the hierarchy event that follow run the CP
			} else if (objectEvent.isDelete()) {
				Task removedTask = (Task) changedObject;
				predecessorTaskList.removeTask(removedTask);
				reset(); // Fix of bug 91 31/8/05.  This ensures the ancestors of this task that are no longer parents will be replaced as single entries in pred list
			} else if (objectEvent.isUpdate()) {
				task = (Task)changedObject;
				Field field = objectEvent.getField();
				if (field != null && !fieldUpdater.inputContains(field))
					return;
				if (field == constraintTypeField) {
					reset();
					task.invalidateSchedules();
					task.markTaskAsNeedingRecalculation();
				}
			}
			calculate(true,task);

		} else if (changedObject instanceof Dependency) { // dependency added or
														  // removed
			Dependency dependency = (Dependency) changedObject;
			if (!dependency.refersToDocument(project))
				return;
			if (!objectEvent.isUpdate()) {
				reset(); // refresh predecssor list - the whold thing may change drastically no matter what the link because of parents
			}
			task = (Task)dependency.getPredecessor();
			Task successor = (Task) dependency.getSuccessor(); // the successor needs to be scheduled
			
			// to fix a bug, I am invalidating both early and late schedules
			task.invalidateSchedules();
			task.markTaskAsNeedingRecalculation();
			if (successor.isSubproject()) { // special case for subprojects - need to reset all
				SubProj sub = (SubProj)successor;
				if (sub.isSubprojectOpen())
					sub.getSubproject().markAllTasksAsNeedingRecalculation(true);
			} 
			successor.invalidateSchedules();
			successor.markTaskAsNeedingRecalculation();
			
//			The line below fixes a bug with nested parents of the sort pred->grand par sib1->sib2. Of course, it means most of the code above is redundant (except for subproject stuff)
			project.markAllTasksAsNeedingRecalculation(true); 
			calculate(true,null); // Run both passes, since the CP might be modified and it's hard to tell if so
		} else if (changedObject == project) { // if whole project changed, such
											   // as hierarchy event
			reset();
			calculate(true,null);
		} else if (changedObject instanceof WorkingCalendar) { // if whole project changed, such
			//TODO for now just invalidating all projects, eventually be smarter
			project.markAllTasksAsNeedingRecalculation(false);
			calculate(true,null);
		} else if (changedObject instanceof Assignment) {
			Assignment assignment = (Assignment)changedObject;
			task = assignment.getTask();
			if (task.getProject().getSchedulingAlgorithm() != this)
				return;
//			if (((NormalTask)task).isEffortDriven())
			calculate(true,task);
		} else if (changedObject instanceof BelongsToDocument){ // for other things, such as assignment entry
			if (((BelongsToDocument)changedObject).getDocument() instanceof Project) {
				Project proj = (Project)((BelongsToDocument)changedObject).getDocument();
				if (proj.getSchedulingAlgorithm() != this)
					return;
			}
			Field field = objectEvent.getField();
			if (field != null && fieldUpdater.inputContains(field))
				calculate(true,null);
		}
	}

	public void reset() {
		if (suspendUpdates) {
			needsReset = true;
			return;
		}
		
		needsReset = false;
		initEarliestAndLatest();
		predecessorTaskList.rearrangeAll();
	}

	public void addEndSentinelDependency(Task task) {
		if (task.getOwningProject() == project && !task.isExternal())
			DependencyService.getInstance().addEndSentinelDependency(finishSentinel,task);
	}
	public boolean removeEndSentinelDependency(Task task) {
		if (task.getOwningProject() == project && !task.isExternal())
			return DependencyService.getInstance().removeEndSentinel(finishSentinel,task);
		return false;
	}
	public void addStartSentinelDependency(Task task) {
		if (task.getOwningProject() == project && !task.isExternal())
			DependencyService.getInstance().addStartSentinelDependency(startSentinel,task);
	}
	public boolean removeStartSentinelDependency(Task task) {
		if (task.getOwningProject() == project && !task.isExternal())
			return DependencyService.getInstance().removeStartSentinel(startSentinel,task);
		return false;
	}
	
	public long getStartConstraint() {
		return startSentinel.getConstraintDate();
	}
	/* (non-Javadoc)
	 * @see com.projity.pm.criticalpath.HasSentinels#setStartConstraint(long)
	 */
	public void setStartConstraint(long date) {
		startSentinel.setScheduleConstraint(ConstraintType.SNET, date);
		markBoundsAsDirty();
		}
	/* (non-Javadoc)
	 * @see com.projity.pm.criticalpath.HasSentinels#setEndConstraint(long)
	 */
	public void setEndConstraint(long date) {
		finishSentinel.setScheduleConstraint(ConstraintType.FNLT, date);
		markBoundsAsDirty();
	}
	public void markBoundsAsDirty() {
		startSentinel.markTaskAsNeedingRecalculation();
		finishSentinel.markTaskAsNeedingRecalculation();
		
		// mark all tasks without preds or without succs as dirty 
		// the purpose of this is to handle cases where a task that determines the project bounds is deleted.
		
		Iterator i = startSentinel.getSuccessorList().iterator();
		Task task;
		while (i.hasNext()) {
			task = ((Task)((Dependency)i.next()).getTask(false));
			task.invalidateSchedules();
			task.markTaskAsNeedingRecalculation();
		}

		i = finishSentinel.getPredecessorList().iterator();
		while (i.hasNext()) {
			task = ((Task)((Dependency)i.next()).getTask(true));
			task.invalidateSchedules();
			task.markTaskAsNeedingRecalculation();
		}
		
	}
	/**
	 * @return
	 */
	public boolean isForward() {
		return project.isForward();
	}
	
	public void setForward(boolean forward) {
		if (forward) {
			setStartConstraint(project.getStartConstraint());
			finishSentinel.setRawConstraintType(ConstraintType.ASAP);
		}
		else {
			setEndConstraint(project.getEnd());
			startSentinel.setRawConstraintType(ConstraintType.ASAP);
		}
		startSentinel.setForward(forward);
		finishSentinel.setForward(forward);
	}
	/* (non-Javadoc)
	 * @see com.projity.transaction.MultipleTransaction.Listener#multipleTransaction(com.projity.transaction.MultipleTransaction)
	 */
	public void multipleTransaction(MultipleTransaction objectEvent) {
		if (objectEvent.isFinalEnd()) {
			suspendUpdates = false;
			if (needsReset)
				reset();
			calculate(true,null);
		} else {
			suspendUpdates = true;
		}
		
	}
	/**
	 * @return
	 */
	public boolean getMarkerStatus() {
		return predecessorTaskList.getMarkerStatus();
	}
	/**
	 * To add a new object such as when pasting
	 */
	public void addObject(Object task) {
		NormalTask newTask  = (NormalTask)task;
		if (newTask.getSuccessorList().isEmpty()) { // if pred has no successors, tell end sentinel about it
			addEndSentinelDependency(newTask);
		} else { // make sure not in sentinel's list
			removeEndSentinelDependency(newTask);
		}
		if (newTask.getPredecessorList().isEmpty()) { // if pred has no successors, tell end sentinel about it
			addStartSentinelDependency(newTask);
		} else { // make sure not in sentinel's list
			removeStartSentinelDependency(newTask);
		}
		
		
		newTask.markTaskAsNeedingRecalculation();
		predecessorTaskList.arrangeTask(newTask);
	}
	
	public void addSubproject(Task subproject) {
		predecessorTaskList.addSubproject(subproject);
	}
	/* (non-Javadoc)
	 * @see com.projity.pm.criticalpath.SchedulingAlgorithm#getDocument()
	 */
	public Document getMasterDocument() {
		return project;
	}
	
	public void dumpPredecessorList() {
		predecessorTaskList.dump();
	}
	public final long getEarliestStart() {
		return earliestStart;
	}
	public final long getLatestFinish() {
		return latestFinish;
	}
	private boolean isSentinel(Task task) {
		return task == startSentinel || task == finishSentinel;
	}
	public final Project getProject() {
		return project;
	}
	public boolean isCriticalPathJustChanged() {
		return criticalPathJustChanged;
	}
	public CriticalPathFields getFieldUpdater() {
		return fieldUpdater;
	}
	public void setEarliestAndLatest(long earliest, long latest) {
		this.earliestStart = earliest;
		this.latestFinish = latest;
	}
	public int[] findTaskPosition(Task t) { // for debugging
		return predecessorTaskList.findTaskPosition(t);
	}
	
	public static String getTrace() {
		StringBuffer buf = new StringBuffer();
		buf.append(ToStringBuilder.reflectionToString(lastInstance));
		buf.append("\nProject: " +  lastInstance.project + " Task: " + traceTask + " reverse=" + traceTask.isReverseScheduled() + " parent ="+traceTask.isParent());
		return buf.toString();
	}
	
}