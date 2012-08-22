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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.SwingUtilities;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.TruePredicate;

import com.projity.association.AssociationFormatParameters;
import com.projity.association.AssociationList;
import com.projity.association.AssociationListFormat;
import com.projity.configuration.CircularDependencyException;
import com.projity.configuration.Configuration;
import com.projity.configuration.Settings;
import com.projity.datatype.Duration;
import com.projity.document.Document;
import com.projity.document.ObjectEvent;
import com.projity.field.CustomFields;
import com.projity.field.CustomFieldsImpl;
import com.projity.field.Field;
import com.projity.field.FieldContext;
import com.projity.field.FieldParseException;
import com.projity.functor.CollectionVisitor;
import com.projity.functor.ObjectVisitor;
import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeList;
import com.projity.grouping.core.OutlineCollection;
import com.projity.grouping.core.hierarchy.BelongsToHierarchy;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.summaries.DivisionSummaryVisitor;
import com.projity.grouping.core.summaries.LeafWalker;
import com.projity.options.CalculationOption;
import com.projity.options.CalendarOption;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.HasTimeDistributedData;
import com.projity.pm.assignment.timesheet.UpdatesFromTimesheet;
import com.projity.pm.calendar.HasCalendar;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.costing.EarnedValueMethodType;
import com.projity.pm.costing.ExpenseType;
import com.projity.pm.costing.HasExpenseType;
import com.projity.pm.criticalpath.PredecessorTaskList;
import com.projity.pm.criticalpath.ScheduleWindow;
import com.projity.pm.criticalpath.TaskSchedule;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.dependency.DependencyFormat;
import com.projity.pm.dependency.DependencyService;
import com.projity.pm.dependency.DependencyType;
import com.projity.pm.dependency.HasDependencies;
import com.projity.pm.dependency.HasDependenciesImpl;
import com.projity.pm.key.HasKey;
import com.projity.pm.key.HasKeyImpl;
import com.projity.pm.resource.Resource;
import com.projity.pm.scheduling.CanBeLeveled;
import com.projity.pm.scheduling.ConstraintType;
import com.projity.pm.scheduling.Schedule;
import com.projity.pm.scheduling.ScheduleUtil;
import com.projity.pm.snapshot.Snapshottable;
import com.projity.pm.snapshot.SnapshottableImpl;
import com.projity.server.access.ErrorLogger;
import com.projity.server.data.DataObject;
import com.projity.strings.Messages;
import com.projity.util.Alert;
import com.projity.util.DateTime;
import com.projity.util.Environment;

/**
 * @stereotype thing
 */
public abstract class Task implements HasKey, HasNotes, HasCalendar, HasDependencies, Schedule, ScheduleWindow,  Snapshottable, HasTimeDistributedData, HasPriority, CustomFields, BelongsToDocument, BelongsToHierarchy, DataObject, CanBeLeveled,UpdatesFromTimesheet,HasExpenseType,TaskLinkReference {
	static final long serialVersionUID = 786665335611L;
	protected static final double COMPLETE_THRESHOLD = 0.9999D;
    /**
     * @link aggregation
     * @supplierCardinality 0..*
     */
    /*# com.projity.pm.assignment.Assignment lnkAssignment; */
	transient Project project;
	transient Project owningProject;
	transient TaskSchedule currentSchedule = null;

	protected transient int debugDependencyOrder = -1;
	protected transient TaskSchedule earlySchedule;
	protected transient TaskSchedule lateSchedule;
	protected transient Snapshottable snapshots;
	protected transient HasDependencies dependencies;
	protected String notes = "";
	protected String wbs = "";
	protected boolean markTaskAsMilestone = false;
	protected transient CustomFieldsImpl customFields = new CustomFieldsImpl();
	protected transient HasKeyImpl hasKey;
	protected transient boolean external = false;
	protected transient long projectId = 0;
	double physicalPercentComplete = 0.0;

	protected long windowEarlyStart = 0;
	protected long windowEarlyFinish = 0;
	protected long windowLateStart = 0;
	protected long windowLateFinish = 0;

	protected long actualStart = 0;

	protected long levelingDelay = 0L;
	protected transient int calculationStateCount = 0;
	protected transient boolean markerStatus = false;
	protected int earnedValueMethod = EarnedValueMethodType.PERCENT_COMPLETE;
	protected static Field startFieldInstance = null;

	protected int constraintType = ConstraintType.ASAP;
	protected long deadline = 0;
	protected int expenseType = ExpenseType.NONE;
	protected transient boolean inSubproject = false;
	protected transient long lastSavedStart = 0L;
	protected transient long lastSavedFinish = 0L;
	protected transient boolean dirty;
	protected transient long lastSavedParentId = -1L;
	protected transient long lastSavedPosistion = 0L;


	public static Field getStartField() {
		if (startFieldInstance == null)
			startFieldInstance = Configuration.getFieldFromId("Field.start");
		return startFieldInstance;
	}
	/**
	 * @return Returns the taskSchedule.
	 */
	public TaskSchedule getCurrentSchedule() {
		return currentSchedule;
	}
	private static Field endFieldInstance = null;
	public static Field getEndField() {
		if (endFieldInstance == null)
			endFieldInstance = Configuration.getFieldFromId("Field.finish");
		return endFieldInstance;
	}

	public Task() {
		this(true);
	}
	public Task(boolean local) {
		hasKey = new HasKeyImpl(local,this);
		currentSchedule = new TaskSchedule();
		initializeTransientTaskObjects();
	}

	public boolean isReadOnly() {
		return isExternal() || isSubproject() || (getOwningProject() != null && getOwningProject().isReadOnly());
	}
	protected void initializeTransientTaskObjects() {
		currentSchedule.initSerialized(this,TaskSchedule.CURRENT);
		earlySchedule = new TaskSchedule(this,TaskSchedule.EARLY);
		lateSchedule = new TaskSchedule(this,TaskSchedule.LATE);
	    snapshots = new SnapshottableImpl(Settings.numBaselines());
	    dependencies = new HasDependenciesImpl(this);

		createSnapshot(CURRENT);
		((TaskSnapshot)getCurrentSnapshot()).setCurrentSchedule(currentSchedule); // put the current schedule in the snapshot
		setLastSavedStart(currentSchedule.getStart());
		setLastSavedFinish(currentSchedule.getFinish());
	}
	protected void initializeTransientTaskObjectsAfterDeserialization() {
		earlySchedule = new TaskSchedule(this,TaskSchedule.EARLY);
		lateSchedule = new TaskSchedule(this,TaskSchedule.LATE);
	    dependencies = new HasDependenciesImpl(this);

	    currentSchedule=((TaskSnapshot)getCurrentSnapshot()).getCurrentSchedule();
		currentSchedule.initSerialized(this,TaskSchedule.CURRENT);

		setLastSavedStart(currentSchedule.getStart());
		setLastSavedFinish(currentSchedule.getFinish());
//		validateConstraints();
	}
	public TaskSnapshot getBaselineSnapshot() {
		return (TaskSnapshot) getSnapshot(CalculationOption.getInstance().getEarnedValueBaselineId());
	}

	private TaskSnapshot createSnapshot(Object snapshotId) {
		TaskSnapshot newOne = new TaskSnapshot();
		setSnapshot(snapshotId, newOne);
		return newOne;
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.scheduling.Schedule#getHasCalendar()
	 */
	public HasCalendar getHasCalendar() {
		return this;
	}

	public Assignment getBaselineAssignment(Resource resource) {
		TaskSnapshot baseline = getBaselineSnapshot();
		if (baseline == null)
			return null;
		return getBaselineSnapshot().findAssignment(resource);
	}

	public Assignment getBaselineAssignment(Resource resource,Object snapshot, boolean createIfDoesntExist) {
		TaskSnapshot baselineSnapshot = (TaskSnapshot) getSnapshot(snapshot);
		if (baselineSnapshot == null) {
			if (createIfDoesntExist)
				baselineSnapshot = createSnapshot(snapshot);
			else
				return null;
		}
		Assignment assignment = baselineSnapshot.findAssignment(resource);

		if (assignment == null && createIfDoesntExist) {
			assignment = Assignment.getInstance(this,resource,1.0,0);
			baselineSnapshot.addAssignment(assignment);
			TaskSchedule baselineSchedule = new TaskSchedule(this,TaskSchedule.CURRENT);
			//baselineSnapshot.set
			baselineSnapshot.setCurrentSchedule(baselineSchedule);
			assignment.setTaskSchedule(baselineSchedule);
			assignment.convertToBaselineAssignment(true);
		}


		return assignment;
	}


	/**
	 * @return Returns the project.
	 */
	public Project getProject() {
		return project;
	}
	public Document getDocument() {
		return getProject();
	}

	public WorkCalendar getProjectCalendar() {
		return project.getWorkCalendar();
	}
	public void connectToProject() {
		project.connectTask(this);
	}

	protected boolean isInitialized() {
		return getProject() != null && getProject().isInitialized();
	}


	/**
	 * @param project The project to set.
	 */
	public void setProject(Project project) {
		this.project = project;
	}

	public static Closure forProject(Closure visitor) {
		return new ObjectVisitor(visitor) {
			protected final Object getObject(Object caller) {
				return ((Task)caller).getProject();
			}
		};
	}
//	Don't know if need this or not.  In any case, need to work out hierarchy treatment.
//	public NodeHierarchy getNodeHierarchy() {
//		//TODO use correct view ?
//		return Document.getTestInstance().getContextManager().getGlobalContext().getTaskContext(View.UNNAMED).getModel().getHierarchy();
//	}
//
//
	public static Closure forParent(Closure visitor) {
		return new ObjectVisitor(visitor) {
			protected final Object getObject(Object caller) {
				return ((Task)caller).getProject().getTaskOutline().getHierarchy().getParent((Node)caller);
			}
		};
	}


	public static Closure forAllChildren(Closure visitor, Predicate filter) {
		return new CollectionVisitor(visitor,filter) {
			protected Collection getCollection(Object caller) {
				return ((Task)caller).getProject().getTaskOutline().getHierarchy().getChildren((Node)caller);
			}
		};
	}

	public static Closure forAllChildren(Closure visitor) {
		return forAllChildren(visitor, TruePredicate.INSTANCE);
	}

	/**
	 * @return Returns the notes.
	 */
	public String getNotes() {
		return notes;
	}
	/**
	 * @param notes The notes to set.
	 */
	public void setNotes(String notes) {
		this.notes = notes;

	}
	public double getCustomCost(int i) {
		return customFields.getCustomCost(i);
	}
	public long getCustomDate(int i) {
		return customFields.getCustomDate(i);
	}
	public long getCustomDuration(int i) {
		return customFields.getCustomDuration(i);
	}
	public long getCustomFinish(int i) {
		return customFields.getCustomFinish(i);
	}
	public boolean getCustomFlag(int i) {
		return customFields.getCustomFlag(i);
	}
	public double getCustomNumber(int i) {
		return customFields.getCustomNumber(i);
	}
	public long getCustomStart(int i) {
		return customFields.getCustomStart(i);
	}
	public String getCustomText(int i) {
		return customFields.getCustomText(i);
	}
	public void setCustomCost(int i, double cost) {
		customFields.setCustomCost(i, cost);
	}
	public void setCustomDate(int i, long date) {
		customFields.setCustomDate(i, date);
	}
	public void setCustomDuration(int i, long duration) {
		customFields.setCustomDuration(i, duration);
	}
	public void setCustomFinish(int i, long finish) {
		customFields.setCustomFinish(i, finish);
	}
	public void setCustomFlag(int i, boolean flag) {
		customFields.setCustomFlag(i, flag);
	}
	public void setCustomNumber(int i, double number) {
		customFields.setCustomNumber(i, number);
	}
	public void setCustomStart(int i, long start) {
		customFields.setCustomStart(i, start);
	}
	public void setCustomText(int i, String text) {
		customFields.setCustomText(i, text);
	}


	/************************************************************************************************
	 * Task Schedule and methods
	 *************************************************************************************************/

		/**
		 * @return start from task schedule
		 */
		public long getStart() {
			return currentSchedule.getStart();
		}
		/**
		 * Set the dependency start, not the task schedule start.  Only the CP can do that
		 */
		public void setStart(long start,FieldContext fieldContext) {
			if (getActualStart() != 0) // you can't change the start date of an in progress task
				return;
			start = CalendarOption.getInstance().makeValidStart(start, false);
			if (start != getStart() && !Environment.isImporting()) {
				long projectStart=getProject().getStart();
				if (projectStart > start) {
					if (!Alert.okCancel(Messages.getString("Message.allowTaskStartBeforeProjectStart")))
						return;
	                setScheduleConstraint(ConstraintType.SNLT, start);

				} else {
                   setScheduleConstraint(ConstraintType.SNET, start);
				}
			}
		}
		public void setStart(long start) {
			setStart(start,null);
		}

		/**
		 * @return
		 */
		public long getEnd() {
			return currentSchedule.getFinish();
		}


		/**
		 * @param end
		 */
		public void setEnd(long end) {
			setEnd(end,null);
		}
		public void setEnd(long end,FieldContext fieldContext) {
			if (end != getEnd()) { // only set if it changes
				getCurrentSchedule().setEnd(end);
			}
		}

		/**
		 * @return Returns the dependencyStart.
		 */
		public long getDependencyStart() {
			return currentSchedule.getRemainingDependencyDate();
		}
		/**
		 * @param dependencyStart The dependencyStart to set.
		 */
		public void setDependencyStart(long dependencyStart) {
			currentSchedule.setRemainingDependencyDate(dependencyStart);
		}

		/**
		 * Sets the duration without controls that setDuration performs
		 *
		 * @param duration
		 */
		public abstract void setRawDuration(long duration);

		public long getRawDuration() {
			return currentSchedule.getRawDuration();
		}


		public void clearDuration() {
			setRawDuration(0);
		}

	/**
	 * @return
	 */
	public WorkCalendar getEffectiveWorkCalendar() {
		return null; // TODO figure out if this belong shere
	}

	public boolean isMilestone() {
		return false;
	}
	public boolean isSubproject() {
		return false;
	}
	transient Collection wbsChildrenNodes = null;
	transient Task wbsParentTask = null;
	transient Resource delegatedTo = null;

	/**
	 * @return Returns the wbsChildrenNodes.
	 */
	public Collection getWbsChildrenNodes() {
		return wbsChildrenNodes;
	}
	/**
	 * @param wbsChildrenNodes The wbsChildrenNodes to set.
	 */
	public void setWbsChildrenNodes(Collection wbsChildrenNodes) {
//System.out.println(this + " setWbsChildrenNodes " + wbsChildrenNodes);
		this.wbsChildrenNodes = wbsChildrenNodes;
	}

	public List getWbsChildrenTasks() {
		List children = (List) getWbsChildrenNodes();
		return NodeList.nodeListToImplList(children);
	}
	/**
	 * @return Returns the wbsParent.
	 */
	public Task getWbsParentTask() {
		return wbsParentTask;
	}
	/**
	 * @param wbsParent The wbsParent to set.
	 */
	public void setWbsParent(Task wbsParentTask) {
		this.wbsParentTask = wbsParentTask;
	}


	/**
	 * See if a this task is a child, grandchild... of another
	 * @param potentialParentTask - task to see if parent
	 * @return true if the task descends from potentialParentTask
	 */
	public boolean wbsDescendentOf(Task potentialParentTask) {
		if (this == potentialParentTask)
			return true;
		Task parentTask = getWbsParentTask();
		if (parentTask == null)
			return false;
		return parentTask.wbsDescendentOf(potentialParentTask);

	}

	public boolean isParent() {
		return isWbsParent();
	}
	/* (non-Javadoc)
	 * @see com.projity.pm.task.TaskSpecificFields#isParent()
	 */
	public boolean isWbsParent() {
		if (wbsChildrenNodes == null || wbsChildrenNodes.size() == 0) {//a task has at least one assignment
//			System.out.println(this + " is not a wbs parent " + wbsChildrenNodes);
			return false;
		}

		Iterator i = wbsChildrenNodes.iterator();
		Object current;
		while (i.hasNext()) {
			current = ((Node) i.next()).getImpl();
			if (current instanceof Task) {
				return true;
			}
		}
		return false;
	}

	public String getWbsParentName() {
		if (wbsParentTask == null)
			return "";
		return wbsParentTask.getName();
	}


	//arranges a task in predecessor/parent order in a colleciton
	public void arrangeTask(Collection addTo, boolean markerStatus, int depth) {
		if (this.markerStatus == markerStatus) // if task has been added, don't treat it again
			return;

		if (Environment.isImporting() && depth >= 1000) // in case circular link in imported project - TODO this is not a perfect solution
			throw new RuntimeException(CircularDependencyException.RUNTIME_EXCEPTION_TEXT);
		// Arrange my parent

		// Arrange my predecessors
		Iterator i = getPredecessorList().iterator();
		Task predecessor;
		Dependency dep;

		Task parent = getWbsParentTask();
		if (parent != null) {
			parent.arrangeTask(addTo,markerStatus,depth+1);
		}
		//May 2 2008. this seems to be right place for setting marker status
		// If it is after adding this task below, you can get into an endless loop
		// If it is put at the top, you can get into situations where the PARENT_END is added before all children are added
		this.markerStatus = markerStatus;

		while (i.hasNext()) {
			dep = (Dependency)i.next();
			if (dep.isDisabled())
				continue;
			predecessor = (Task) dep.getPredecessor();
			predecessor.arrangeTask(addTo,markerStatus,depth+1);
			predecessor.arrangeChildren(addTo,markerStatus,depth);
		}


		// Process current task
		PredecessorTaskList.TaskReference taskReference = new PredecessorTaskList.TaskReference(this);
		addTo.add(taskReference);

		//This was the old place for the above		this.markerStatus = markerStatus;  Move it back if bugs happen



		// Arrange my children
		if (isWbsParent()) {
			taskReference.setParentBegin();
			arrangeChildren(addTo,markerStatus,depth);
			taskReference =  new PredecessorTaskList.TaskReference(this);
			taskReference.setParentEnd();
			addTo.add(taskReference);
		}
	}

	private void arrangeChildren(Collection addTo, boolean markerStatus, int depth) {

		//note that it is possible that this is called for non parents

		Collection children = getWbsChildrenNodes(); // I depend on my predecessors children
		if (children != null) {
			Iterator p = children.iterator();
			Object current;
			Task child;
			while (p.hasNext()) {
				current = ((Node)p.next()).getImpl();
				if (! (current instanceof Task))
					continue;

				child = (Task)current;
				child.arrangeTask(addTo,markerStatus,depth+1);
			}
		}
	}



	/**
 * This rather complex function determines whether one task depends on another.  Because of the rules for parent tasks,
 * the algorithm is rather complicated.  Basically:
 * - A (parent) task depends on its childrens predecessors, as these are potentially equivalent
 * - A task depends on its parents predecessors - in the case of a link to its parent task, the links applies to this task too
 * - A task depends on its parent, of course
 * - A task depends on its predecessors (obviously)
 * - A task depends on its predecessors children
 *
 * @param other Task to compare to
 * @param set - A set used to prevent treating same task twice
 * @return true if linking to other would cause a circular link
 */
	private boolean dependsOn(Task other, Task me,HashSet set, String taskNames) {
		// To avoid infinite loops which can occur under certain circumstances, use a set to prevent looking up twice
		if (set.contains(this))
			return false;
		set.add(this);




		// Here is the primary exit point.  We have arrived back at the other node, so it is circular
		if (this == other) {
			if (taskNames != null)
				System.out.println("Circular: \n" + taskNames);
			return true;
		}



		if (taskNames != null)
			taskNames += (getId() +": " + getName() + '\n');


		Task predecessor;
		Dependency dep;

		Collection children;
		Iterator i;

		i = getPredecessorList().iterator();
		while (i.hasNext()) {
			dep = (Dependency)i.next();
			if (dep.isDisabled())
				continue;
			predecessor = (Task)dep.getPredecessor(); // I depend on my predecessors
			if (predecessor.dependsOn(other,me,set,taskNames == null ? null : taskNames + "Pred-"))
				return true;
		}


		//parent
		Task parent = getWbsParentTask();
		if (other.getWbsParentTask() != parent) { // only do parents if they are different
//			if (!this.isAncestorOrDescendent(other))
			if (parent != null) {
	//			if ( !other.wbsDescendentOf(parent))
					if (parent.dependsOn(other,me,set,taskNames == null? null: taskNames + "Parent- " ))
						return true;

			}
		}

		children = getWbsChildrenNodes();
		Task child;
		Object current;
		Iterator j;


		// I depend on my children's preds
		if (children != null) {
			i = children.iterator();
			while (i.hasNext()) {
				current = ((Node)i.next()).getImpl();
				if (! (current instanceof Task))
					continue;
				child = (Task)current;

				j = child.getPredecessorList().iterator();
				while (j.hasNext()) {
					dep = (Dependency)j.next();
					if (dep.isDisabled())
						continue;
					predecessor = (Task)dep.getPredecessor(); // I depend on my predecessors
					if (predecessor.wbsDescendentOf(this)) {// skip if already belongs to parent thru an ancestry relation
						continue;
					}

					if (predecessor.dependsOn(other,me,set,taskNames == null ? null : taskNames + "pred-child " + child.getId()))
						return true;
				}

			}

		}


		return false;
	}

//	/**
//	 * @param other
//	 * @return
//	 */
//	boolean isPredecessorOfDescendent(Task other) {
//		if (this == other)
//			return true;
//		Collection children = getWbsChildrenNodes();
//		if (children == null)
//			return false;
//		Task child;
//		Object current;
//		Iterator i = children.iterator();
//		Iterator j;
//		Dependency dep;
//		Task predecessor;
//		while (i.hasNext()) {
//			current = ((Node)i.next()).getImpl();
//			if (! (current instanceof Task))
//				continue;
//			child = (Task)current;
////			if ( child.wbsDescendentOf(other) ) //|| other.wbsDescendentOf(this))
////				continue;
//
//			j = child.getPredecessorList().iterator();
//			while (j.hasNext()) {
//				dep = (Dependency)j.next();
//				if (dep.isDisabled())
//					continue;
//				predecessor = (Task)dep.getPredecessor(); // I depend on my predecessors
//				if (predecessor.isPredecessorOfDescendent(other))
//					return true;
//			}
//		}
//		return false;
//	}
	public boolean isAncestorOrDescendent(Task other) {
		return (wbsDescendentOf(other) || other.wbsDescendentOf(this));
	}
	public boolean dependsOn(HasDependencies other) {
		Task task = (Task)other;
		if (isAncestorOrDescendent(task))
			return true;


		HashSet set = new HashSet();
		return dependsOn((Task)other,this,set,"");
	}

	/**
	 * @return
	 */
	public Date getCreated() {
		return hasKey.getCreated();
	}
	/**
	 * @return
	 */
	public long getId() {
		return hasKey.getId();
	}
	/**
	 * @param id
	 */
	public void setId(long id) {
		hasKey.setId(id);
	}
	/**
	 * @param created
	 */
	public void setCreated(Date created) {
		hasKey.setCreated(created);
	}

	public String getPredecessors() {
		return AssociationListFormat.getInstance(DependencyFormat.getInstance(AssociationFormatParameters.getInstance(this,true,Configuration.getFieldFromId("Field.id"),false, true)))
			.format(getPredecessorList());
	}
	public String getSuccessors() {
		return AssociationListFormat.getInstance(DependencyFormat.getInstance(AssociationFormatParameters.getInstance(this,false,Configuration.getFieldFromId("Field.id"),false, true)))
			.format(getSuccessorList());
	}

	public String getUniqueIdPredecessors() {
		return AssociationListFormat.getInstance(DependencyFormat.getInstance(AssociationFormatParameters.getInstance(this,true,Configuration.getFieldFromId("Field.uniqueId"),false, true)))
			.format(getPredecessorList());
	}
	public String getUniqueIdSuccessors() {
		return AssociationListFormat.getInstance(DependencyFormat.getInstance(AssociationFormatParameters.getInstance(this,false,Configuration.getFieldFromId("Field.uniqueId"),false, true)))
			.format(getSuccessorList());
	}

	public String getWbsPredecessors() {
		return AssociationListFormat.getInstance(DependencyFormat.getInstance(AssociationFormatParameters.getInstance(this,true,Configuration.getFieldFromId("Field.wbs"),true, true)))
			.format(getPredecessorList());
	}

	public String getWbsSuccessors() {
		return AssociationListFormat.getInstance(DependencyFormat.getInstance(AssociationFormatParameters.getInstance(this,false,Configuration.getFieldFromId("Field.wbs"),true, true)))
			.format(getSuccessorList());
	}

	public void setPredecessors(String predecessors) throws FieldParseException {
		getPredecessorList().setAssociations(predecessors,DependencyFormat.getInstance(AssociationFormatParameters.getInstance(this,true,Configuration.getFieldFromId("Field.id"),false, true)));
	}

	public void setSuccessors(String successors) throws FieldParseException {
		getSuccessorList().setAssociations(successors,DependencyFormat.getInstance(AssociationFormatParameters.getInstance(this,false,Configuration.getFieldFromId("Field.id"),false, true)));
	}

	public void setUniqueIdPredecessors(String predecessors) throws FieldParseException {
		getPredecessorList().setAssociations(predecessors,DependencyFormat.getInstance(AssociationFormatParameters.getInstance(this,true,Configuration.getFieldFromId("Field.uniqueId"),false, true)));
	}

	public void setUniqueIdSuccessors(String successors) throws FieldParseException {
		getSuccessorList().setAssociations(successors,DependencyFormat.getInstance(AssociationFormatParameters.getInstance(this,false,Configuration.getFieldFromId("Field.uniqueId"),false, true)));
	}

	/**
	 * @return Returns the wbs.
	 */
	public String getWbs() {
		return wbs;
	}
	/**
	 * @param wbs The wbs to set.
	 */
	public void setWbs(String wbs) {
		this.wbs = wbs;
	}
	/**
	 * @return
	 */
	public long getUniqueId() {
		return hasKey.getUniqueId();
	}

	/**
	 * @param id
	 */
	public void setUniqueId(long id) {
		hasKey.setUniqueId(id);
	}
	/**
	 * @return
	 */
	public String getName() {
		return hasKey.getName();
	}
	/**
	 * @param name
	 */
	public void setName(String name) {
		hasKey.setName(name);
	}


	public String toString() {
		if (getName() == null)
			return "<null name>";
		return getName();
	}

	public String getName(FieldContext context) {
		return hasKey.getName(context);
	}

	public boolean isNormal() {
		return false;
	}
	public boolean isCritical() {
		return false;
	}
	/** if task is currently critical or will become critical after CP */
	public boolean isOrWasCritical() {
		if (isCritical())
			return true;
		if (currentSchedule.isForward())
			return getEnd() >= getLateFinish();
		else // reverse schedule
			return getStart() <= getEarlyStart();
	}

	public boolean isSummary() {
		return isWbsParent(); //TODO need to somehow hook into view and see if parent in view's node model.  yuck!
	}
	public boolean isAssignment() {
		return false;
	}


	/**
	 * @return Returns the physicalPercentComplete.
	 */
	public double getPhysicalPercentComplete() {
		return physicalPercentComplete;
	}
	/**
	 * @param physicalPercentComplete The physicalPercentComplete to set.
	 */
	public void setPhysicalPercentComplete(double physicalPercentComplete) {
		this.physicalPercentComplete = physicalPercentComplete;
	}

	public void markTaskAsNeedingRecalculation() {
		int nextStateCount = project.getCalculationStateCount()+1;
		setCalculationStateCount(nextStateCount);
	}

	/**
	 * Flags all tasks which depend on this one for scheduling as dirty
	 * @param doSelf TODO
	 *
	 */
	void markAllDependentTasksAsNeedingRecalculation(boolean doSelf) {
		if (doSelf)
			markTaskAsNeedingRecalculation();


		Iterator succ = getSuccessorList().iterator();
		if (!succ.hasNext()) {
			getProject().getSchedulingAlgorithm().markBoundsAsDirty();
			//TODO what about reverse schedulded?
		} else {
			Task successor;
			// mark successors as dirty
			while (succ.hasNext()) {
				successor = (Task)((Dependency)succ.next()).getSuccessor();
				successor.markTaskAsNeedingRecalculation();
			}
		}
		// mark parent as dirty
		Task parent = getWbsParentTask();
		while (parent != null) {
			parent.markTaskAsNeedingRecalculation();
			parent = parent.getWbsParentTask();
		}

		// mark children as dirty
		Collection children = getWbsChildrenNodes();
		if (children != null) {
			Iterator i = children.iterator();
			Object child;
			while (i.hasNext()) {
				child = ((Node) i.next()).getImpl();
				if (! (child instanceof Task))
					continue;
				((Task)child).markTaskAsNeedingRecalculation();
			}
		}
	}


	void cleanUp(Object eventSource,boolean deep,boolean undo,boolean cleanDependencies) {
		//if (!cleanDependencies) return; //TODO was for undo of paste of linked tasks, but doesn't work
		markAllDependentTasksAsNeedingRecalculation(false);

		// remove sentinel dependencies if any
		project.removeStartSentinelDependency(this);
		project.removeEndSentinelDependency(this);

		// remove all links to or from
		LinkedList toRemove=new LinkedList(); //fix
		DependencyService.getInstance().remove(getPredecessorList(),toRemove);
		for(Iterator j=toRemove.iterator();j.hasNext();){
			DependencyService.getInstance().remove((Dependency)j.next(),eventSource,undo); //fix
		}
		toRemove.clear();
		DependencyService.getInstance().remove(getSuccessorList(),toRemove);
		for(Iterator j=toRemove.iterator();j.hasNext();){
			DependencyService.getInstance().remove((Dependency)j.next(),eventSource,undo); //fix
		}

	}

	public void recalculate(Object eventSource) {
		((Project)getDocument()).updateScheduling(eventSource,this,ObjectEvent.UPDATE,getEndField());
		//getDocument().getObjectEventManager().fireUpdateEvent(eventSource,this,getEndField());
	}
	public void recalculateLater(final Object eventSource) {
		markTaskAsNeedingRecalculation(); // task needs to be recalculated

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	recalculate(eventSource);
            }}
        );
	}


	/**
	 * @return
	 */
	public long getActualStart() {
		return actualStart;
//		if (currentSchedule.getPercentComplete() == 0.0D && getPercentComplete() == 0)
//			return 0;
//		return getStart();
	}

	public abstract void setActualStart(long actualStart);


	public long getActualFinish() {
		if (getPercentComplete() == 1.0)
			return getEnd();
		return 0;
	}
	/**
	 * @param actualFinish
	 */
	public void setActualFinish(long actualFinish) {
		long old = getActualFinish();
		if (actualFinish == old)
			return;
		setEnd(actualFinish);
		setPercentComplete(1.0);
	}
	public boolean isZeroDuration() {
		return Duration.millis(getRawDuration()) == 0;
	}

/**
 * Percent complete is calculated based on assignments
 */
	public double getPercentComplete() {
		boolean parent = isWbsParent();
		DivisionSummaryVisitor divisionClosure = ScheduleUtil.percentCompleteClosureInstance(parent);
		Project proj = (Project) (getMasterDocument() == null ? getProject() : getMasterDocument());
		NodeModel nodeModel = proj.getTaskOutline();
		if (isWbsParent()) {
			try {
			   LeafWalker.recursivelyTreatBranch(nodeModel,this, divisionClosure);
			} catch (NullPointerException n ){
				ErrorLogger.logOnce("getPercentComplete","getPercentComplete() Task: " + this + " Project " + project,n);
				return 0;  // better this than crashing
			}
		} else {
			CollectionUtils.forAllDo(((NormalTask)this).getAssignments(),divisionClosure);
		}
		double val = divisionClosure.getValue();
		if (val >=COMPLETE_THRESHOLD) // adjust for rounding
			val = 1.0D;
		return val;
	}

	public boolean inProgress() {
		double percentComplete = getPercentComplete();
		return (percentComplete > 0.0D
				&& percentComplete < 1.0D);
	}

	private long calcTimeToStartFromNow() {
		return getStart() - System.currentTimeMillis();

	}
	private static long millisInWeek = 1000L*60L*60L*24L*7L;
	public boolean isStartingWithinOneWeek() {
		if (getPercentComplete() > 0.0D)
			return false;
		long diff = calcTimeToStartFromNow();
		return diff > 0 && diff < millisInWeek;
	}
	public boolean isStartingWithinTwoWeeks() {
		if (getPercentComplete() > 0.0D)
			return false;
		long diff = calcTimeToStartFromNow();
		return diff > 0 && diff < 2L*millisInWeek;
	}
	public boolean isLateStarting() {
		if (getPercentComplete() > 0.0D)
			return false;
		long diff = calcTimeToStartFromNow();
		return diff < 0;
	}

	public boolean isComplete() {
		return getPercentComplete() >= 1.0D;
	}
	public boolean isUnstarted() {
		return getPercentComplete() == 0.0D;
	}

	public long getDurationMillis() {
		return Duration.millis(getDuration());
	}

	/**
	 * @return
	 */
//&&&&&
//	public long getActualDuration() {
//		long stop = getStop();
//		if (stop == 0)
//			return 0;
//		return getEffectiveWorkCalendar().compare(stop,getStart(),false);
//	}

/**
 * Actual duration is % complete * duration for all tasks including parents
 */
	public long getActualDuration() {
		long duration = getDuration();
		long result = Math.round(getPercentComplete() * Duration.millis(duration));
		return Duration.useTimeUnitOfInNone(result, duration);
	}

	/**
	 * @param actualDuration
	 */
	public void setActualDuration(long actualDuration) {
		actualDuration = DateTime.closestDate(Duration.millis(actualDuration));

		if (actualDuration == Duration.millis(getActualDuration()))
			return;
		long stop = getEffectiveWorkCalendar().add(getStart(),actualDuration,true);
		setStop(stop);
	}
	/**
	 * @return
	 */
	public long getRemainingDuration() {
		long actualDuration = getActualDuration();
		long result = getDurationMillis() - Duration.millis(actualDuration);
		return Duration.useTimeUnitOfInNone(result, actualDuration);
	}
	/**
	 * @param remainingDuration
	 */
	public void setRemainingDuration(long remainingDuration) {
		remainingDuration = DateTime.closestDate(Duration.millis(remainingDuration));
		setActualDuration(getDurationMillis() - remainingDuration);
	}


	public Object clone(){
		try {
			Task task=(Task)super.clone();
			_cloneTo(task);
//				Handle wbs outside
			return task;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
	public void cleanClone(){
		owningProject=null;
		project=null;
	}

	private void _cloneTo(Task task){
		task.hasKey=new HasKeyImpl(isLocal()&&Environment.getStandAlone(),task);
		task.setName(new String(getName()));
		task.setRawDuration(getRawDuration());

		task.earlySchedule=(TaskSchedule)earlySchedule.cloneWithTask(task);
		task.lateSchedule=(TaskSchedule)lateSchedule.cloneWithTask(task);
		task.customFields=(CustomFieldsImpl)customFields.clone();
		task.snapshots=(Snapshottable)((SnapshottableImpl)snapshots).cloneWithTask(task);
		task.currentSchedule= ((TaskSnapshot)task.getCurrentSnapshot()).getCurrentSchedule();
		task.notes=new String(notes);
		task.wbs=new String(wbs);
		task.wbsChildrenNodes=null;
		task.wbsParentTask=null;

		task.dependencies=new HasDependenciesImpl(task);

		task.currentSchedule.copyDatesAfterClone(currentSchedule);
		task.setDirty(true);
	}
	public void cloneTo(Task task){
		task.project=project;
		task.owningProject=owningProject;

		task.debugDependencyOrder=debugDependencyOrder;
		task.markTaskAsMilestone = markTaskAsMilestone;
		task.external = external;
		task.projectId = projectId;
		task.physicalPercentComplete = physicalPercentComplete;

		task.windowEarlyStart = windowEarlyStart;
		task.windowEarlyFinish = windowEarlyFinish;
		task.windowLateStart = windowLateStart;
		task.windowLateFinish = windowLateFinish;

		task.actualStart = actualStart;

		task.levelingDelay = levelingDelay;
		task.calculationStateCount = calculationStateCount;
		task.markerStatus = markerStatus;
		task.earnedValueMethod = earnedValueMethod;
		task.startFieldInstance = startFieldInstance;

		task.constraintType = constraintType;
		task.deadline = deadline;
		task.expenseType = expenseType;
		task.inSubproject = inSubproject;
		task.lastSavedStart = lastSavedStart;
		task.lastSavedFinish = lastSavedFinish;
		task.dirty=dirty;

		task.delegatedTo = delegatedTo;

		_cloneTo(task);
	}


	/**
	 * @return Returns the splitDuration.
	 */
	public long getSplitDuration() {
		long r = getResume();
		if (r == 0)
			return 0;
		return getEffectiveWorkCalendar().compare(r,getStop(),false);
	}
/******************************************************************************
 * Getters and setters
 ****************************************************************************/

	/**
	 * @return Returns the constraintType.
	 */
	public int getConstraintType() {
//		if (constraintType != ConstraintType.ASAP && constraintType != ConstraintType.ALAP) {
//			if (getConstraintDate() < 1000000) {
//				System.out.println("fixing bad constraint date: " + this + "  " + getId() );
//				clearDateConstraints();
//				constraintType = ConstraintType.ASAP;
//			}
//		}
//
//
//

		return constraintType;
	}

	/**
	 * @param constraintType The constraintType to set.
	 */
	public void setConstraintType(int constraintType) throws FieldParseException {
		int newConstraintType = makeValidConstraintType(constraintType); // limit to valid options
		if (newConstraintType != constraintType)
			throw new FieldParseException(Messages.getString("Message.parentConstraintType"));
		setRawConstraintType(constraintType);
		long d = getConstraintDate(); // save off old date, it will be reused
		if (!isDatelessConstraintType() && d < 5* WorkCalendar.MILLIS_IN_DAY) // if the constraint requires a date and there is none, set a date to today. added a bit of margin 5/5/08
			setConstraintDate(getEffectiveWorkCalendar().adjustInsideCalendar(DateTime.midnightToday(),false));

	}

	public void setRawConstraintType(int constraintType) {
		long d = getConstraintDate(); // save off old date, it will be reused
		clearDateConstraints(); // get rid of all constraints
		setScheduleConstraint(constraintType,d); // set new constraint with old date

	}
	/**
	 * @return Returns the windowEarlyStart.
	 */
	public long getWindowEarlyStart() {
		return windowEarlyStart;
	}

	/**
	 * @param windowEarlyStart The windowEarlyStart to set.
	 */
	public void setWindowEarlyStart(long windowEarlyStart) {
		this.windowEarlyStart = windowEarlyStart;
	}

	/**
	 * @return Returns the windowEarlyFinish.
	 */
	public long getWindowEarlyFinish() {
		return windowEarlyFinish;
	}

	/**
	 * @param windowEarlyFinish The windowEarlyFinish to set.
	 */
	public void setWindowEarlyFinish(long windowEarlyFinish) {
		this.windowEarlyFinish = windowEarlyFinish;
	}
	/**
	 * @return Returns the windowLateFinish.
	 */
	public long getWindowLateFinish() {
		return windowLateFinish;
	}

	/**
	 * @param windowLateFinish The windowLateFinish to set.
	 */
	public void setWindowLateFinish(long windowLateFinish) {
		this.windowLateFinish = windowLateFinish;
	}

	/**
	 * @return Returns the windowLateStart.
	 */
	public long getWindowLateStart() {
		return windowLateStart;
	}

	/**
	 * @param windowLateStart The windowLateStart to set.
	 */
	public void setWindowLateStart(long windowLateStart) {
		this.windowLateStart = windowLateStart;
	}


	/**
	 * @param mustStart The date the task must start on.
	 */
	public void setMustStartOn(long mustStart) {
		setWindowEarlyStart(mustStart);
		setWindowLateStart(mustStart);
	}

	/**
	 * @param mustStart The date the task must finish on.
	 */
	public void setMustFinishOn(long mustFinish) {
		setWindowEarlyFinish(mustFinish);
		setWindowLateFinish(mustFinish);
	}

	/**
	 * Set constraint FNET
	 * @param date
	 */
	public void setFinishNoEarlierThan(long date) {
		setWindowEarlyFinish(date);
	}

	/**
	 * Set constraint FNLT
	 * @param date
	 */
	public void setFinishNoLaterThan(long date) {
		setWindowLateFinish(date);
		setWindowEarlyFinish(0);
	}

	/**
	 * Set constraint SNET
	 * @param date
	 */
	public void setStartNoEarlierThan(long date) {
		setWindowEarlyStart(date);
		setWindowLateStart(0);
	}

	/**
	 * Set constraint SNLT
	 * @param date
	 */
	public void setStartNoLaterThan(long date) {
		setWindowLateStart(date);
		setWindowEarlyStart(0);
	}



	/**
	 * @return Returns the earlyFinish.
	 */
	public long getEarlyFinish() {
		return earlySchedule.getFinish();
	}

	/**
	 * @return Returns the earlyStart.
	 */
	public long getEarlyStart() {
		return earlySchedule.getStart();
	}


	/**
	 * @return Returns the lateFinish.
	 */
	public long getLateFinish() {
		return lateSchedule.getFinish();
	}


	/**
	 * @return Returns the lateStart.
	 */
	public long getLateStart() {
		return lateSchedule.getStart();
	}

	/**
	 * Is this task reverse scheduled:  Is it ALAP in forward scheduling or ASAP in reverse?
	 * @return
	 */
	public final boolean isReverseScheduled() {
		if (project.isForward())
			return constraintType == ConstraintType.ALAP;
		else
			return constraintType == ConstraintType.ASAP;
	}

	protected final boolean isDatelessConstraintType() {
		return constraintType == ConstraintType.ALAP
		||     constraintType == ConstraintType.ASAP;

	}
/*********************************************************************************
 * Constraints
***********************************************************************************/
	private void clearDateConstraints() {
		setWindowEarlyStart(0);
		setWindowLateStart(0);
		setWindowEarlyFinish(0);
		setWindowLateFinish(0);
	}

	boolean validateConstraints() {
		long little = WorkCalendar.MILLIS_IN_DAY  * 10;
		if (!isDatelessConstraintType()) {
			if (windowEarlyStart < little
				&& windowEarlyFinish <little
				&& windowLateStart <little
				&& windowLateFinish < little) {
				setConstraintDate(0L);
				ErrorLogger.log("Repairing invalid constraints " + getId() + " " + getName()); // under unknown circumstances, constraints are becoming invalid. this repairs them.
				setDirty(true);
				return true;

			}

		}
		return false;
	}

//	public Object[] fieldOptionsScheduleConstraint() {
//		if (isWbsParent()) {
//			Configuration.getFieldFromId("Field.scheduleConstraint");
//
//
//		} else {
//			return null; // use default
//		}
//
//	}

	public void setScheduleConstraint(int constraintType, long date) {
		this.constraintType = constraintType;
		if (constraintType == ConstraintType.FNET) {
			setFinishNoEarlierThan(date);
		} else if (constraintType == ConstraintType.FNLT) {
			setFinishNoLaterThan(date);
		} else if(constraintType == ConstraintType.SNET) {
			setStartNoEarlierThan(date);
		} else if(constraintType == ConstraintType.SNLT) {
			setStartNoLaterThan(date);
		} else if(constraintType == ConstraintType.MSO) {
			setMustStartOn(date);
		} else if(constraintType == ConstraintType.MFO) {
			setMustFinishOn(date);
		} else {
			clearDateConstraints();
		}

	}
	public void setScheduleConstraintAndUpdate(int constraintType, long date) {
		setScheduleConstraint(constraintType,date);
		getDocument().getObjectEventManager().fireUpdateEvent(this,this,Configuration.getFieldFromId("Field.constraintType"));
	}
	public void setConstraintDate(long date) {
		if (date == 0) {
			clearDateConstraints();
			constraintType = getProject() == null ? ConstraintType.ASAP : getProject().getDefaultConstraintType();
		} else {
			date = getEffectiveWorkCalendar().adjustInsideCalendar(date,false); // make date valid

		}
		setScheduleConstraint(constraintType,date);

	}
	public long getConstraintDate() {
		if (constraintType == ConstraintType.FNET) {
			return getWindowEarlyFinish();
		} else if (constraintType == ConstraintType.FNLT) {
			return getWindowLateFinish();
		} else if(constraintType == ConstraintType.SNET) {
			return getWindowEarlyStart();
		} else if(constraintType == ConstraintType.SNLT) {
			return getWindowLateStart();
		} else if(constraintType == ConstraintType.MSO) {
			return getWindowEarlyStart();
		} else if(constraintType == ConstraintType.MFO) {
			return getWindowEarlyFinish();
		} else {
			return 0;
		}
	}

	public boolean isReadOnlyConstraintDate(FieldContext fieldContext) {
		return getConstraintType() == ConstraintType.ALAP
			|| getConstraintType() == ConstraintType.ASAP;
	}

/**************************************************************************
 * Critical path methods for slack
***************************************************************************/
	public final long getTotalSlack() {
		return getEffectiveWorkCalendar().compare(getLateFinish(),getEarlyFinish(), false);
	}

	/** The amount of excess time an activity has between its Early Start and Late Start dates. */
	public final long getStartSlack() {
		return getEffectiveWorkCalendar().compare(getLateStart(),getEarlyStart(), false);
	}

	/** The amount of excess time an activity has between its Early Finish and Late Finish dates. */
	public final long getFinishSlack() {
		return getEffectiveWorkCalendar().compare(getLateFinish(),getEarlyFinish(), false);  // note that this is same as total float
	}

	/**
	 * Used to calculate the free slack on a given dependency
	 *
	 * @param dependency
	 * @return
	 */
	private static long calcFreeSlack(Dependency dependency) {
		ScheduleWindow predecessor = (ScheduleWindow) dependency
				.getPredecessor();
		ScheduleWindow successor = (ScheduleWindow) dependency.getSuccessor();
		long t = 0;
		WorkCalendar cal = dependency.getEffectiveWorkCalendar();
		if (dependency.getDependencyType() == DependencyType.FS) {
			t = cal.compare(cal.add(successor.getEarlyStart(),
					-dependency.getLeadValue(), true), predecessor
					.getEarlyFinish(), false);
		} else if (dependency.getDependencyType() == DependencyType.FF) {
			t = cal.compare(cal.add(successor.getEarlyFinish(),
					-dependency.getLeadValue(), true), predecessor
					.getEarlyFinish(), false);
		} else if (dependency.getDependencyType() == DependencyType.SS) {
			t = cal.compare(cal.add(successor.getEarlyStart(),
					-dependency.getLeadValue(), true), predecessor
					.getEarlyStart(), false);
		} else if (dependency.getDependencyType() == DependencyType.SF) {
			t = cal.compare(cal.add(successor.getEarlyFinish(),
					-dependency.getLeadValue(), true), predecessor
					.getEarlyStart(), false);
		}
		return t;
	}

	/**
	 * Used in calculating free slack. Free Float = For FS: Early Start (next
	 * activity) � Lag (if any) � Early Finish*
	 *
	 * @param dependencyList
	 * @param duration
	 * @return
	 */
	public long getFreeSlack() {
		long least = getTotalSlack(); // free slack is at most the total slack
		Dependency dependency;
		for (Iterator i = getSuccessorList().iterator(); i
				.hasNext();) {
			dependency = (Dependency) i.next();
			least = Math.min(least, calcFreeSlack(dependency));
		}
		return least;
	}

/**********************************************************************************
 * Access to Dependencies
 **********************************************************************************/

	public AssociationList getPredecessorList() {
		return dependencies.getPredecessorList();
	}
	public AssociationList getSuccessorList() {
		return dependencies.getSuccessorList();
	}

	public AssociationList getDependencyList(boolean pred) {
		return dependencies.getDependencyList(pred);
	}


/************************************************************************************
 * Critical Path stuff
 **************************************************************************************/

	/* (non-Javadoc)
	 * @see com.projity.pm.criticalpath.ScheduleWindow#calcOffsetFrom(long, boolean, boolean)
	 */
	public long calcOffsetFrom(long startDate, long dependencyDate, boolean ahead, boolean remainingOnly, boolean useSooner) {
		throw new RuntimeException("calcOffsetFrom should not be called in ScheduleWindow");
	}



	public long getElapsedDuration() {
		return Math.round(getEffectiveWorkCalendar().compare(getEnd(), getStart(),true) * CalendarOption.getInstance().getFractionOfDayThatIsWorking());
	}

	public long getDeadline() {
		return deadline;
	}
	public void setDeadline(long deadline) {
		if (deadline != 0L)
			deadline = CalendarOption.getInstance().makeValidEnd(deadline, false);
		this.deadline = deadline;
	}
	public boolean isMarkTaskAsMilestone() {
		return markTaskAsMilestone;
	}
	public void setMarkTaskAsMilestone(boolean markTaskAsMilestone) {
		this.markTaskAsMilestone = markTaskAsMilestone;
	}
	public int getEarnedValueMethod() {
		return earnedValueMethod;
	}
	public void setEarnedValueMethod(int earnedValueMethod) {
		this.earnedValueMethod = earnedValueMethod;
	}

	/** Update a task from the Update Project dialog.  There are several options:
	 *
	 * @param date Date to either update completion to, or to move remaining work to
	 * @param updateWorkAsCompleteThrough If true, then update % complete, if false, then move remaining
	 *  to the date
	 * @param setFractionalPercentComplete If true, then allow setting of % complete for uncompleted tasks,
	 *  otherwise, if a task is not completed, it's current completion will not be modified
	 * @return True if task was updated, false if unchanged
	 */
	public boolean updateProjectTask(long date, boolean updateWorkAsCompleteThrough, boolean setFractionalPercentComplete) {
		long start = getStart();
		long end = getEnd();
		long completedDate = getStop();
		boolean updated = false;
		if (updateWorkAsCompleteThrough) {
			if (setFractionalPercentComplete) {
				if (completedDate != end) {// if task is not finished, adjust its completion.  This may actually reduce % complete
					setStop(date);
					updated = true;
				}
			} else if (date >= end) { // if date is equal or later to end date of task, set its percent complete to 100%, otherwise do nothing
				setPercentComplete(1.0);
				updated = true;
			}
		} else {
			if (date > start) { // move remaining after status date
				moveRemainingToDate(date);
				updated = true;
			}
		}
		if (updated) {
			recalculate(this);
			setDirty(true);
		}
		return updated;
	}

	/**
	 * @return Returns the calculationStateCount.
	 */
	public final int getCalculationStateCount() {
		return calculationStateCount;
	}
	/**
	 * @param calculationStateCount The calculationStateCount to set.
	 */
	public final void setCalculationStateCount(int calculationStateCount) {
//		if (this.calculationStateCount == calculationStateCount) return;
//		System.out.println("setCalculationStateCount");
//		setDirty(true);
		this.calculationStateCount = calculationStateCount;
	}

	public void updateEndSentinel() {
		if (getSuccessorList().isEmpty()) { // if pred has no successors, tell end sentinel about it
			project.addEndSentinelDependency(this);
		} else { // make sure not in sentinel's list
			project.removeEndSentinelDependency(this);
		}
	}
	public void updateStartSentinel() {
		if (getPredecessorList().isEmpty()) { // if pred has no successors, tell end sentinel about it
			project.addStartSentinelDependency(this);
		} else { // make sure not in sentinel's list
			project.removeStartSentinelDependency(this);
		}
	}

	public final TaskSchedule getEarlySchedule() {
		return earlySchedule;
	}
	public final TaskSchedule getLateSchedule() {
		return lateSchedule;
	}

	public final TaskSchedule getSchedule(int scheduleType) {
		if (scheduleType == TaskSchedule.CURRENT)
			return currentSchedule;
		else if (scheduleType == TaskSchedule.EARLY)
			return earlySchedule;
		else
			return lateSchedule;
	}

/**
 * Used for debugging primarily - says if a task was just modified by last recalculation - either has same count or is one less
 * @return
 */	public boolean isJustModified() {
	 	if (project.getSchedulingAlgorithm() == null)
	 		return false;
		return calculationStateCount + PredecessorTaskList.CALCULATION_STATUS_STEP >= project.getSchedulingAlgorithm().getCalculationStateCount();
	}

 	public abstract boolean hasDuration();
	/**
	 * @param markerStatus The markerStatus to set.
	 */
	public final void setMarkerStatus(boolean markerStatus) {
		this.markerStatus = markerStatus;
	}
	/**
	 * Set the task to be forward or rerverse scheduled
	 * @param forward
	 */
	public void setForward(boolean forward) {
		getCurrentSchedule().setForward(forward);
		restrictToValidConstraintType();

	}
	/**
	 * Parent tasks have only 3 possible consraint types ASAP/ALAP (depending on forward or reverse scheduling), SNET, and FNLT
	 */
	void restrictToValidConstraintType() {
		setRawConstraintType(makeValidConstraintType(getConstraintType()));
	}

	protected int makeValidConstraintType(int type) {
		if (isWbsParent()) { // parents have a limited choice of constraint types
			if (type == ConstraintType.FNLT
				|| type == ConstraintType.SNET)
				return type;
			else
				return project.getDefaultConstraintType();
		}
		return type;

	}
//	public boolean isNew() {
//		return hasKey.isNew();
//	}

	public static Predicate instanceofPredicate() {
		return new Predicate() {
			public boolean evaluate(Object arg0) {
				return arg0 instanceof Task;
			}};
	}

	public void invalidateSchedules() {
		earlySchedule.invalidate();
		lateSchedule.invalidate();
	}

	public Document getMasterDocument() {
		if (getProject().getSchedulingAlgorithm() == null)
			return null;
		return getProject().getSchedulingAlgorithm().getMasterDocument();
	}
	public String getTaskAndProjectName() {
		Project p = getOwningProject();
		if (p == null)
			p = getProject();
		return getName() + " (" + p.getName() + ")";
	}
	public String getSubprojectFile() {
		return null;
	}

	public boolean fieldHideSubprojectFile(FieldContext fieldContext) {
		return true;
	}

	public void setSubprojectFile(String sub) {
	}

	public boolean isSubprojectReadOnly() {
		return false;
	}

	public boolean fieldHideSubprojectReadOnly(FieldContext fieldContext) {
		return true;
	}

	public long getParentId(int outlineNumber) {
		NodeModel model= project.getTaskOutline(outlineNumber);
		if (model == null)
			return 0;
		Node node = model.getParent(model.search(this));
		Object impl = node.getImpl();
		if (impl != null && impl instanceof HasKey)
			return ((HasKey)impl).getId();
		return 0;
	}

	public int getOutlineLevel(int outlineNumber) {
		NodeModel model= project.getTaskOutline(outlineNumber);
		if (model == null)
			return 0;
		Node node = model.getParent(model.search(this));
		return model.getHierarchy().getLevel(node);
	}
	public int getOutlineLevel() {
		return getOutlineLevel(OutlineCollection.DEFAULT_OUTLINE);
	}

	public final int getDebugDependencyOrder() {
		return debugDependencyOrder;
	}
	public final void setDebugDependencyOrder(int debugDependencyOrder) {
		this.debugDependencyOrder = debugDependencyOrder;
	}
	public final long getLevelingDelay() {
		return levelingDelay;
	}
	public final void setLevelingDelay(long levelingDelay) {
		this.levelingDelay = levelingDelay;
	}
	public CustomFields getCustomFields() {
		return customFields;
	}
	public void updateCachedDuration() {
		setRawDuration(getDuration());
	}

	public boolean isDirty() {
		return dirty ||getStart() != getLastSavedStart() || getEnd() != getLastSavedFinish();
	}
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
		if (!dirty){
			setLastSavedStart(currentSchedule.getStart());
			setLastSavedFinish(currentSchedule.getFinish());
		}
		if (dirty&&project!=null) project.setGroupDirty(true);
	}

	public boolean isMissedDeadline() {
		if (deadline == 0)
			return false;
		else
			return deadline < getEnd();
	}
	public final boolean isExternal() {
		return external;
	}
	public final void setExternal(boolean external) {
		this.external = external;
	}
	public final long getProjectId() {
		return projectId;
	}
	public final void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	public Project getRootProject() {
		Task parent = getWbsParentTask();
		if (parent == null)
			return getProject();
		return parent.getRootProject();
	}
	public Project getEnclosingProject() {
		if (isSubproject())
			return ((SubProj)this).getSubproject();
		Task parent = getWbsParentTask();
		if (parent == null)
			return getProject();
		return parent.getEnclosingProject();
	}
	public SubProj getEnclosingSubproject() {
		Task parent = getWbsParentTask();

		if (parent == null)
			return null;
		if (parent.isSubproject())
			return (SubProj)parent;
		return parent.getEnclosingSubproject();
	}
	/**
	 * Will return a node in the master project that holds he subproject
	 * @return
	 */
	public Node getEnclosingSubprojectNode() {
		SubProj s = getEnclosingSubproject();
		if (s == null)
			return null;
		return ((Task)s).getProject().getTaskOutline().search(s);
	}

	public boolean liesInSubproject() {
		Task parent = getWbsParentTask();
		if (parent == null)
			return false;
		if (parent.isSubproject())
			return true;
		return parent.liesInSubproject();
	}
	public final boolean isInSubproject() {
		return inSubproject;
	}
	public final void setInSubproject(boolean inSubproject) {
		this.inSubproject = inSubproject;
	}
	public final Project getOwningProject() {
		return owningProject;
	}
	public final void setOwningProject(Project owningProject) {
		this.owningProject = owningProject;
	}

	public void copyScheduleTo(Task to) {
		to.getCurrentSchedule().setStart(getCurrentSchedule().getStart());
		to.getCurrentSchedule().setFinish(getCurrentSchedule().getFinish());
		to.setRawDuration(getDuration());
	}
	/**
	 * Set all schedules to a fixed start and end
	 * @param start
	 * @param finish
	 */
	protected void setAllSchedules(long start, long finish) {
		getCurrentSchedule().setStart(start);
	    getCurrentSchedule().setFinish(finish);
		getEarlySchedule().setStart(start);
		getEarlySchedule().setFinish(finish);
		getLateSchedule().setStart(start);
		getLateSchedule().setFinish(finish);
	}
	public void setAllSchedulesToCurrentDates() {
		long start = getCurrentSchedule().getStart();
		long finish = getCurrentSchedule().getFinish();
		setAllSchedules(start,finish);

	}
	public boolean isAssignable() {
		return !isReadOnly();
	}
	public final long getLastSavedFinish() {
		return lastSavedFinish;
	}
	public final void setLastSavedFinish(long currendFinish) {
		this.lastSavedFinish = currendFinish;
	}
	public final long getLastSavedStart() {
		return lastSavedStart;
	}
	public final void setLastSavedStart(long currentStart) {
		this.lastSavedStart = currentStart;
	}
	public final long getLastSavedParentId() {
		return lastSavedParentId;
	}
	public final void setLastSavedParentId(long lastSavedParentId) {
		this.lastSavedParentId = lastSavedParentId;
	}
	public final long getLastSavedPosistion() {
		return lastSavedPosistion;
	}
	public final void setLastSavedPosistion(long lastSavedPosistion) {
		this.lastSavedPosistion = lastSavedPosistion;
	}

	public int getExpenseType() {
		return expenseType;
	}

	public void setExpenseType(int budgetType) {
		this.expenseType = budgetType;
	}

	public int getEffectiveExpenseType() {
		int result = expenseType;
		if (result == ExpenseType.NONE) {
			Task parent = getWbsParentTask();
			if (parent != null)
				result = parent.getEffectiveExpenseType();
		}
		if (result == ExpenseType.NONE)
			result = getOwningProject().getEffectiveExpenseType();
		return result;
	}

	public boolean startsBeforeProject() { // special case for SNLT tasks that start before project
		return (getConstraintType() == ConstraintType.SNLT  // this was changed from SNET - MSP mistakenly displays SNET for these tasks
				&& getPredecessorList().isEmpty()
				&& getConstraintDate() < getOwningProject().getStart());
	}

/**
 * Get value of delegateTo, or if null, recursively get parent's value
 * @return
 */
	public Resource getDelegatedTo() {
		if (delegatedTo == null) {
			Task parent = getWbsParentTask();
			if (parent != null)
				return parent.getDelegatedTo();
		}
		return delegatedTo;
	}
	public void setDelegatedTo(Resource delegatedTo) {
		Resource old = this.delegatedTo;
		this.delegatedTo = delegatedTo;
//		if (old == null) {
//			SwingUtilities.invokeLater(new Runnable() {
//
//				public void run() {
//					Task newOne = getOwningProject().cloneTask(Task.this);
//				}});
//		}
	}
	public boolean isDelegatedToUser() {
		Resource del = getDelegatedTo();
		return del != null && Environment.getLogin().equals(del.getUserAccount());
	}
	public String getDelegatedToName() {
		Resource del = getDelegatedTo();
		return del == null ? "" : del.getName();
	}

	public void forSnapshotsAssignments(Closure c,boolean onlyCurrent){
		if (onlyCurrent) forSnapshotsAssignments(c,-1);
		else{
		    for (int s=0;s<Settings.numBaselines();s++){
		    	forSnapshotsAssignments(c, s);
		    }
		}
	}

	public void forSnapshotsAssignments(Closure c,int s){
		TaskSnapshot snapshot;
		if (s==-1) snapshot=(TaskSnapshot)getCurrentSnapshot();
		else snapshot=(TaskSnapshot)getSnapshot(new Integer(s));
        if (snapshot==null) return;
        AssociationList snapshotAssignments=snapshot.getHasAssignments().getAssignments();
        if (snapshotAssignments.size()>0){
            for (Iterator j=snapshotAssignments.iterator();j.hasNext();){
                Assignment assignment=(Assignment)j.next();
                c.execute(assignment);
            }
        }
	}
	public void forSnapshots(Closure c){
	}
	public void forSnapshots(Closure c,int s){
	}

/**
 * Get a task's position in the predecessor-ordered list of the critical path.  Parent tasks will have a pair indicating the parent start and parent end
 * @return
 */	public String getPredecessorOrder() {
		int pos[] = ((com.projity.pm.criticalpath.CriticalPath)project.getSchedulingAlgorithm()).findTaskPosition(this);
		String result = "" + pos[0];
		if (pos.length ==2)
			result += ", " + pos[1];
		return result;
	}




}

