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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.undo.UndoableEditSupport;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import com.projity.algorithm.ReverseQuery;
import com.projity.association.InvalidAssociationException;
import com.projity.configuration.CircularDependencyException;
import com.projity.configuration.Dictionary;
import com.projity.configuration.FieldDictionary;
import com.projity.configuration.Settings;
import com.projity.datatype.Duration;
import com.projity.datatype.Hyperlink;
import com.projity.datatype.ImageLink;
import com.projity.document.Document;
import com.projity.document.ObjectEvent;
import com.projity.document.ObjectEventManager;
import com.projity.document.ObjectSelectionEventManager;
import com.projity.field.Field;
import com.projity.field.FieldContext;
import com.projity.field.HasExtraFields;
import com.projity.functor.IntervalConsumer;
import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.graphic.configuration.SpreadSheetCategories;
import com.projity.graphic.configuration.SpreadSheetFieldArray;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeException;
import com.projity.grouping.core.NodeFactory;
import com.projity.grouping.core.NodeList;
import com.projity.grouping.core.NodeVisitor;
import com.projity.grouping.core.OutlineCollection;
import com.projity.grouping.core.OutlineCollectionImpl;
import com.projity.grouping.core.event.HierarchyEvent;
import com.projity.grouping.core.event.HierarchyListener;
import com.projity.grouping.core.hierarchy.NodeHierarchy;
import com.projity.grouping.core.model.AssignmentNodeModel;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelDataFactory;
import com.projity.grouping.core.model.NodeModelFactory;
import com.projity.grouping.core.transform.filtering.NotAssignmentFilter;
import com.projity.options.CalendarOption;
import com.projity.options.TimesheetOption;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.HasTimeDistributedData;
import com.projity.pm.assignment.TimeDistributedDataConsolidator;
import com.projity.pm.assignment.TimeDistributedFields;
import com.projity.pm.assignment.timesheet.TimesheetHelper;
import com.projity.pm.assignment.timesheet.UpdatesFromTimesheet;
import com.projity.pm.calendar.CalendarService;
import com.projity.pm.calendar.HasBaseCalendar;
import com.projity.pm.calendar.HasCalendar;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.costing.EarnedValueCalculator;
import com.projity.pm.costing.EarnedValueFields;
import com.projity.pm.costing.EarnedValueValues;
import com.projity.pm.costing.ExpenseType;
import com.projity.pm.costing.HasExpenseType;
import com.projity.pm.criticalpath.CriticalPath;
import com.projity.pm.criticalpath.HasSentinels;
import com.projity.pm.criticalpath.SchedulingAlgorithm;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.dependency.DependencyService;
import com.projity.pm.key.HasId;
import com.projity.pm.key.HasKey;
import com.projity.pm.key.HasKeyImpl;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourcePool;
import com.projity.pm.scheduling.BarClosure;
import com.projity.pm.scheduling.ConstraintType;
import com.projity.pm.scheduling.Schedule;
import com.projity.pm.scheduling.ScheduleEvent;
import com.projity.pm.scheduling.ScheduleEventListener;
import com.projity.pm.scheduling.ScheduleEventManager;
import com.projity.pm.scheduling.ScheduleInterval;
import com.projity.pm.scheduling.ScheduleUtil;
import com.projity.pm.snapshot.BaselineScheduleFields;
import com.projity.pm.snapshot.Snapshottable;
import com.projity.pm.snapshot.SnapshottableImpl;
import com.projity.pm.time.MutableHasStartAndEnd;
import com.projity.print.PrintSettings;
import com.projity.server.access.ErrorLogger;
import com.projity.server.data.DataObject;
import com.projity.server.data.DistributionComparator;
import com.projity.server.data.DistributionConverter;
import com.projity.server.data.DistributionData;
import com.projity.session.FileHelper;
import com.projity.strings.Messages;
import com.projity.transaction.MultipleTransactionManager;
import com.projity.undo.ClearSnapshotEdit;
import com.projity.undo.DataFactoryUndoController;
import com.projity.undo.SaveSnapshotEdit;
import com.projity.util.Alert;
import com.projity.util.DateTime;
import com.projity.util.Environment;
import com.projity.workspace.SavableToWorkspace;
import com.projity.workspace.WorkspaceSetting;
/**
 * Project class
 */
public class Project implements Document, BelongsToDocument, HasKey, HasPriority, MutableHasStartAndEnd, ProjectSpecificFields, HasNotes, HasBaseCalendar, HasCalendar, NodeModelDataFactory, HierarchyListener, HasTimeDistributedData, TimeDistributedFields, EarnedValueValues, EarnedValueFields, DataObject, HasSentinels, BaselineScheduleFields, Schedule,UpdatesFromTimesheet, HasExtraFields, HasExpenseType, SavableToWorkspace {
	static final long serialVersionUID = 17283790404932L;
	private long statusDate = 0;
	private String manager="";
	private transient HasKeyImpl hasKey;
	private String notes="";
	private transient LinkedList tasks = new LinkedList();
	private transient ResourcePool resourcePool = null;
	//private transient Schedule schedule = null; //used?
	private transient SchedulingAlgorithm schedulingAlgorithm = null;
	private transient boolean initialized = false;
	private transient ScheduleEventManager scheduleEventManager = new ScheduleEventManager();
	private transient MultipleTransactionManager multipleTransactionManager = new MultipleTransactionManager();
	private transient ObjectEventManager objectEventManager = new ObjectEventManager();
	private transient ObjectSelectionEventManager objectSelectionEventManager = new ObjectSelectionEventManager();

	private transient int taskIdCounter = 0;
	private transient boolean isGroupDirty = false;
	private transient boolean isDirty = false;
	private transient boolean readOnly = false;
	private transient SubprojectHandler subprojectHandler;
	public transient static Project lastDeserialized = null;
	long start;
	long end;
	long duration;
	boolean forward = true;
	int priority = 500;
	long currentDate = 0;
	private Map extraFields = null;
	private double risk = 0.0D;
	private double netPresentValue = 0.0D;
	private int benefit = 0;
	transient int projectStatus = ProjectStatus.PLANNING; // exposed in database
	transient int projectType = ProjectType.OTHER; // exposed in database
	transient int expenseType = ExpenseType.NONE;// exposed in database
	transient String group;// exposed in database
	transient String division;// exposed in database

	private transient boolean openedAsSubproject = false;
	private transient Hyperlink documentFolderUrl = null;
	private transient long earliestStartingTask = 0L; // used for subprojects
	private transient long latestFinishingTask = 0L; // used for subprojects
	private static Project dummy = null;


	private transient NodeModel taskModel = null;
	private transient NodeModel resourceModel = null;
	private transient Object taskCache = null;
	private transient Object resourceCache = null;
	private transient List<Task> repaired = null;
	private transient Date creationDate,lastModificationDate;

	public NodeModel getTaskModel() {
		if (taskModel == null)
			taskModel=NodeModelFactory.createTaskModel(this);
		return taskModel;
	}

	public NodeModel getResourceModel() {
		if (resourceModel == null)
			resourceModel=NodeModelFactory.createResourceModel(this);
		return resourceModel;
	}


	public Object getResourceCache() {
		return resourceCache;
	}

	public void setResourceCache(Object resourceCache) {
		this.resourceCache = resourceCache;
	}

	public Object getTaskCache() {
		return taskCache;
	}

	public void setTaskCache(Object taskCache) {
		this.taskCache = taskCache;
	}

	private Project(boolean local) {
		super();
		// get the appropriate subproject handler
		initSubprojectHandler();
		hasKey =new HasKeyImpl(local,this);
		setWorkCalendar(CalendarService.getInstance().getDefaultInstance());

		start = CalendarOption.getInstance().makeValidStart(DateTime.midnightToday(), true);
		start = getEffectiveWorkCalendar().adjustInsideCalendar(start,false);
		end = start;
		calendarOption = CalendarOption.getDefaultInstance();
	}


	private Project(ResourcePool resourcePool,DataFactoryUndoController undo) {
		this(resourcePool.isLocal());
		this.resourcePool = resourcePool;
		undoController=undo;
	}

	public void dispose() {
		System.out.println("disposing project " + this);
	}
	public static Project getDummy() {
		if (dummy == null)
			dummy = new Project(true);
		return dummy;
	}

	public static Project createProject(ResourcePool resourcePool,DataFactoryUndoController undo) {
		Project project=new Project(resourcePool,undo);
		project.initializeProject();
		project.setUndoController(undo); //undo not properly initialized in new Project(resourcePool,undo)
		return project;
	}

	public void initializeOutlines(){
		int count=Settings.numHierarchies();
		for (int i=0;i<count;i++){
			NodeModel model=taskOutlines.getOutline(i);
			if (model==null) continue;
			if (model instanceof AssignmentNodeModel){
				AssignmentNodeModel aModel=(AssignmentNodeModel)model;
				aModel.setContainsLeftObjects(true);
				aModel.setDocument(this);
			}
			model.setUndoController(undoController);
		}
		initializeDefaultOutline();
	}

	public void disconnectOutlines(){
		int count=Settings.numHierarchies();
		for (int i=0;i<count;i++){
			NodeModel model=taskOutlines.getOutline(i);
			if (model instanceof AssignmentNodeModel){
				AssignmentNodeModel aModel=(AssignmentNodeModel)model;
				aModel.setDocument(null); //remove ObjectListener
			}
		}
		disconnectDefaultOutline();
	}


	public long getStartConstraint() {
		long result;
	    long constraint = getReferringSubprojectTaskDependencyDate();
	   	if (constraint > getStart())
	   		result = getEffectiveWorkCalendar().adjustInsideCalendar(constraint,false);
	   	else
	   		result = getStart();
	   	return result;
	}

	public void initialize(final boolean subproject,boolean updateDistribution) {
	    initialized = true;
	    repairTasks();
	    if (!subproject)
	    	schedulingAlgorithm.initialize(this);
	    if (getStart() == 0L) {
	    	System.out.println("no start so using earliest");
	    	SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					recalculate();
			    	setStart(getEarliestStartingTaskOrStart());
				}});
	    }
	    initializeDefaultOutline();
	    if (TimesheetOption.getInstance().isAutomaticallyIntegrateTimecardData())
	    	applyTimesheet(TimesheetOption.getInstance().getTimesheetFieldArray());
	    setAllTasksAsUnchangedFromPersisted(false);

	    if (updateDistribution) updateDistributionMap();
	}

/**
 * This will set the start and end date of a project to the earliest starting task and the latest finishing
 * Its purpose is for use in handling subprojects, when we'd like the subproject's external constraints to determine its start and end, and also
 * have it the subproject show up with correct start and end dates when shown upopened in another project
 *
 */
	void setEarliestAndLatestDatesFromSchedule() {
		Iterator i = tasks.iterator();
		long s = Long.MAX_VALUE;
		long e = 0;
		while (i.hasNext()) {
			Task t = (Task)i.next();
			if (t.isExternal() || t.getOwningProject() != this)
				continue;
			s = Math.min(s,t.getStart());
			e = Math.max(e,t.getEnd());
		}
		if (s != Long.MAX_VALUE)
			earliestStartingTask = s;
		else
			earliestStartingTask = getStart();

		if (e != 0 )
			latestFinishingTask = e;
		else
			latestFinishingTask = getEnd();
	}





	public void initializeProject(){
		setSchedulingAlgorithm(new CriticalPath(this));
		/**TODO fix calendar handling  should be created by factory*/

		initializeOutlines();

	}

	public void disconnect(){
	    disconnectOutlines();
	    removeObjectListener(getSchedulingAlgorithm());
	    schedulingAlgorithm = null; // help with gc
	}


	private void initializeDefaultOutline() {
		taskOutlines.getDefaultOutline().getHierarchy().addHierarchyListener(this);
	}
	private void disconnectDefaultOutline() {
		taskOutlines.getDefaultOutline().getHierarchy().removeHierarchyListener(this);
	}


	public NormalTask newNormalTaskInstance() {
		return newNormalTaskInstance(true);
	}

	public void initializeId(Task task) {
		long id = ++taskIdCounter;
		task.setId(id); //starts at 1TODO check for duplicates -
		//task.setUniqueId(id); //TODO use a GUID generator
	}


	public NormalTask newNormalTaskInstance(boolean userCreated) {
		NormalTask newOne = new NormalTask(this);

		add(newOne);
		initializeId(newOne);

		if (userCreated)
			objectEventManager.fireCreateEvent(this,newOne);
		return newOne;
	}


	public void setLocalParent(Task child, Task parent) {
		Node childNode = getTaskModel().search(child);
		Node parentNode = parent == null ? null : getTaskModel().search(parent);
		setLocalParent(childNode,parentNode);
	}

	public void setLocalParent(Node childNode, Node parentNode) {
		Task child = (Task) childNode.getImpl();
		Task parent = (Task) (parentNode == null ? null : parentNode.getImpl());
		if (child.getWbsParentTask() == parent)
			return;
		Node oldParentNode = getTaskModel().search(child.getWbsParentTask());
		if (oldParentNode != null)
			oldParentNode.getChildren().remove(childNode);
		ArrayList temp = new ArrayList();
		temp.add(childNode);
		getTaskModel().move(parentNode, temp, -1,NodeModel.NORMAL);
		setDefaultRelationship(parentNode,childNode);
	}

	public Node createLocalTaskNode(Node parentNode) {
		NormalTask task=new NormalTask(this);
		Node childNode = NodeFactory.getInstance().createNode(task); // get a node for this task
		connectTask(task);
		addToDefaultOutline(parentNode,childNode);
		getSchedulingAlgorithm().addObject(task);
		return childNode;
	}



//	public Task cloneTask(Task from) {
//		//TODO this does not copy fields correctly
//		NormalTask newOne = (NormalTask) from.clone();
//		add(newOne);
//		initializeId(newOne);
//		newOne.setWbsParent(from.getWbsParentTask());
//		Node node = NodeFactory.getInstance().createNode(newOne);
//		getTaskModel().addBefore(getTaskModel().search(from),node,NodeModel.NORMAL);
//		objectEventManager.fireCreateEvent(this,newOne);
//		return newOne;
//
//	}


	/**
	 * Used when creating a task on spreadsheet that may not be valid
	 * @return
	 */
	public NormalTask newStandaloneNormalTaskInstance() {
		NormalTask newOne = new NormalTask(this);

		newOne.getCurrentSchedule().setStart(getWorkCalendar().adjustInsideCalendar(newOne.getCurrentSchedule().getStart(),false));
		initializeId(newOne);
//		newOne.initializeDates();
		return newOne;
	}
	public NormalTask createScriptedTask() {
		NormalTask task = newStandaloneNormalTaskInstance();
		connectTask(task); // put task in project list
		taskOutlines.addToAll(task, null); // update all node models
		task.markTaskAsNeedingRecalculation();
		updateScheduling(this, task, ObjectEvent.CREATE);
		return task;
	}

   public void connectTask(Task task) {
	   if (!isOpenedAsSubproject() || !tasks.contains(task)) // tasks were being added twice.
		   add(task);

		if (task.getOwningProject() == null)
			task.setOwningProject(this);
		if (task.getProjectId() == 0) {
			task.setProjectId(getUniqueId());
		}
		Project masterProject = (Project) task.getMasterDocument();
		if (masterProject == this) {
			task.setProject(this);
				// initially, the task has no predecessors or successors, so signify that it is both a starting point and an ending point
			if (task.getSuccessorList().size() == 0)
				addEndSentinelDependency(task);
			if (task.getPredecessorList().size() == 0)
				addStartSentinelDependency(task);
		} else {
			masterProject.add(task);
			task.setProject(masterProject);
		}
	}


	/**
	 * @return Returns the statusDate.
	 */
	public long getStatusDate() {
		if (statusDate == 0) // if date not set, then use last instant of this day incude all of today
			return workCalendar.adjustInsideCalendar(DateTime.midnightTomorrow() -1,true);

		return statusDate;
	}

	/**
	 * @param statusDate The statusDate to set.
	 */
	public void setStatusDate(long statusDate) {
		statusDate = DateTime.midnightNextDay(statusDate) -1; // last instant of today
		statusDate = workCalendar.adjustInsideCalendar(statusDate, true);
		this.statusDate = statusDate;
	}
	


	public final int getPriority() {
		return priority;
	}
	public final void setPriority(int priority) {
		this.priority = priority;
	}
	public void add(Task task) {
		tasks.add(task);
	}
	/**
	 * @return Returns the tasks.
	 */
	public LinkedList getTasks() {
		return tasks;
	}

//	public static Closure forAllTasks(Closure visitor, Predicate filter) {
//		return new CollectionVisitor(visitor,filter) {
//			protected Collection getCollection(Object arg0) {
//				return ((Project)arg0).getTasks();
//			}
//		};
//	}
//
//	public int testCount() {
//		ReflectionPredicate taskPredicate;
//		try {
//			taskPredicate = ReflectionPredicate.getInstance(NormalTask.class.getMethod("isVirtual",null));
//			Closure t = Project.forAllTasks(NormalTask.forAllAssignments(PrintString.INSTANCE),taskPredicate);
//			t.execute(this);
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return 0;
//	}



	/**
	 * @return Returns the resourcePool.
	 */
	public ResourcePool getResourcePool() {
		return resourcePool;
	}
    public void setResourcePool(ResourcePool resourcePool) {
        this.resourcePool = resourcePool;
    }

	/* (non-Javadoc)
	 * @see com.projity.grouping.core.Node#accept(com.projity.grouping.core.NodeVisitor)
	 */
	public void accept(NodeVisitor visitor) {
		visitor.execute(this);
	}

	/* (non-Javadoc)
	 * @see com.projity.grouping.core.Node#getType()
	 */
	public Class getType() throws NodeException {
		return getClass();
	}

	/* (non-Javadoc)
	 * @see com.projity.grouping.core.Node#isVirtual()
	 */
	public boolean isVirtual() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.projity.grouping.core.Node#setVirtual(boolean)
	 */
	public void setVirtual(boolean virtual) {
	}

	private transient OutlineCollection taskOutlines = new OutlineCollectionImpl(Settings.numHierarchies(),this);

	public NodeModel getTaskOutline() {
		return taskOutlines.getOutline();
	}
	public NodeModel getTaskOutline(int outlineNumber) {
		return taskOutlines.getOutline(outlineNumber);
	}

	public void addToDefaultOutline(Node parentNode, Node childNode) {
		taskOutlines.addToDefaultOutline(parentNode,childNode);
		if (parentNode == null)
			return;
		setDefaultRelationship(parentNode,childNode);
	}
	public void addToDefaultOutline(Node parentNode, Node childNode, int position,boolean event) {
		taskOutlines.addToDefaultOutline(parentNode,childNode,position,event);
		if (parentNode == null||childNode.isVoid())
			return;
		setDefaultRelationship(parentNode,childNode);
	}
	public OutlineCollection getTaskOutlines() {
		return taskOutlines;
	}
	private void setDefaultRelationship(Node parentNode, Node childNode) {
		Task childTask = (Task)childNode.getImpl();
		if (parentNode == null) {
			childTask.setWbsParent(null);
		} else {
			Task parentTask = (Task)parentNode.getImpl();
			childTask.setWbsParent(parentTask);
			if (parentTask != null)
				parentTask.setWbsChildrenNodes(taskOutlines.getDefaultOutline().getHierarchy().getChildren(parentNode));
		}
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}
	/**
	 * @return
	 */
	public long getDuration() {
		return getEffectiveWorkCalendar().compare(end,start,false);
	}


	WorkCalendar workCalendar = null;
	/**
	 * @return
	 */
	public WorkCalendar getWorkCalendar() {
		return workCalendar;
	}

	/**
	 * @param workCalendar
	 */
	public void setWorkCalendar(WorkCalendar workCalendar) {
		if (this.workCalendar != null)
			((WorkingCalendar)this.workCalendar).removeObjectUsing(this);
		this.workCalendar = workCalendar;
		((WorkingCalendar)this.workCalendar).addObjectUsing(this);
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.time.HasCalendar#getEffectiveWorkCalendar()
	 */
	public WorkCalendar getEffectiveWorkCalendar() {
		return workCalendar;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.scheduling.Schedule#getHasCalendar()
	 */
	public HasCalendar getHasCalendar() {
		return this;
	}
	/**
	 * @param end
	 */
	public void setEnd(long end) {
		this.end = end;
//		setEndConstraint(end);
	}
	/**
	 * @param start
	 */
	public void setStart(long start) {
//		System.out.println("setting project " + this + " start " + new Date(start) + " previous " + new Date(this.start));
//if (this.getName().equals("xxx"))
//	System.out.println("bah");
		this.start = start;
	}



	/**
	 * Quick function to find a task by id.  Should probably replaced with hash table
	 * @param idObject
	 * @param project
	 * @return
	 */
	public static Task findTaskById(Object idObject, Collection taskList) {
		Iterator i = taskList.iterator();
		int id = ((Number)idObject).intValue();
		Task task;
		while (i.hasNext()) {
			task = (Task)i.next();
			if (task.getId() == id)
				return task;
		}
		return null;
	}

//	public static Task findTaskByUniqueId(Object idObject, Collection taskList) {
//		Iterator i = taskList.iterator();
//		int id = ((Number)idObject).intValue();
//		Task task;
//		while (i.hasNext()) {
//			task = (Task)i.next();
//			if (task.getUniqueId() == id)
//				return task;
//		}
//		return null;
//	}

	public Task findByUniqueId(long id) {
		Iterator i = getTaskOutlineIterator();
		Task task;
		while (i.hasNext()) {
			task = (Task)i.next();
			if (task.getUniqueId() == id)
				return task;
		}
		return null;
	}
//	public Task findByUniqueId(long id) {
//		return findTaskByUniqueId(new Long(id), getTasks());
//	}
	/**
	 * @return Returns the initialized.
	 */
	public boolean isInitialized() {
		return initialized;
	}
	/**
	 * @return Returns the manager.
	 */
	public String getManager() {
		return manager;
	}
	/**
	 * @param manager The manager to set.
	 */
	public void setManager(String manager) {
		this.manager = manager;
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
	 * @return
	 */
	public String getName() {
		return hasKey.getName();
	}
	/**
	 * @return
	 */
	public long getUniqueId() {
		return hasKey.getUniqueId();
	}
//	public void setNew(boolean isNew) {
//		hasKey.setNew(isNew);
//	}
	/**
	 * @param created
	 */
	public void setCreated(Date created) {
		hasKey.setCreated(created);
	}
	/**
	 * @param id
	 */
	public void setId(long id) {
		hasKey.setId(id);
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		if (name == null || name.length() == 0)
			return;
		String oldName=getName();
		hasKey.setName(name);
		if ((oldName==null&&name!=null)||(!oldName.equals(name))) fireNameChanged(this, oldName);
		if (getWorkCalendar() == null)
			System.out.println("error work calendar is null on project");
	}

	protected transient EventListenerList projectListenerList = new EventListenerList();

	public void addProjectListener(ProjectListener l) {
		projectListenerList.add(ProjectListener.class, l);
	}
	public void removeProjectListener(ProjectListener l) {
		projectListenerList.remove(ProjectListener.class, l);
	}
	public ProjectListener[] getProjectListeners() {
		return (ProjectListener[]) projectListenerList.getListeners(ProjectListener.class);
	}
    public EventListener[] getProjectListeners(Class listenerType) {
    	return projectListenerList.getListeners(listenerType);
    }

 	protected void fireNameChanged(Object source,String oldName) {
		Object[] listeners = projectListenerList.getListenerList();
		ProjectEvent e = null;
		for (int i = 0; i < listeners.length; i += 2) {
			if (listeners[i] == ProjectListener.class) {
				if (e == null) {
					e = new ProjectEvent(source,
							ProjectEvent.NAME_CHANGED, this,oldName);
				}
				((ProjectListener) listeners[i + 1]).nameChanged(e);

			}
		}
	}
 	protected void fireGroupDirtyChanged(Object source,boolean oldName) {
		Object[] listeners = projectListenerList.getListenerList();
		ProjectEvent e = null;
		for (int i = 0; i < listeners.length; i += 2) {
			if (listeners[i] == ProjectListener.class) {
				if (e == null) {
					e = new ProjectEvent(source,
							ProjectEvent.GROUP_DIRTY_CHANGED, this,new Boolean(oldName));
				}
				((ProjectListener) listeners[i + 1]).groupDirtyChanged(e);

			}
		}
	}




	/**
	 * @param id
	 */
	public void setUniqueId(long id) {
		hasKey.setUniqueId(id);
	}


	//NodeModelDataFactory
	/* (non-Javadoc)
	 * @see com.projity.grouping.core.DataFactory#createNode(java.lang.Object, java.lang.Object)
	 */
	public Object createUnvalidatedObject(NodeModel nodeModel, Object parent) {
		NormalTask task = newStandaloneNormalTaskInstance();
		task.setWbsParent((Task)parent);
		return task;
	}
	public void addUnvalidatedObject(Object object, NodeModel nodeModel, Object parent) {
		if (!(object instanceof NormalTask)) return;// avoids VoidNodes
		NormalTask task=(NormalTask)object;
		//task.getCurrentSchedule().setStart(getWorkCalendar().adjustInsideCalendar(task.getCurrentSchedule().getStart(),false));
		task.setWbsParent((Task)parent);
		task.setInSubproject(task.liesInSubproject());

	}
	public NodeModelDataFactory getFactoryToUseForChildOfParent(Object parent) {
		if (parent == null || !(parent instanceof Task))
			return this;
		return((Task)parent).getEnclosingProject();
	}

	/* (non-Javadoc)
	 * @see com.projity.grouping.core.DataFactory#validateObject(java.lang.Object)
	 */
	public void validateObject(Object newlyCreated, NodeModel nodeModel, Object eventSource, Object hierarchyInfo,boolean isNew) {
		if (!(newlyCreated instanceof Task)) return;// avoids VoidNodes
		Task newTask = (Task)newlyCreated;
		newTask.setProject((Project) getSchedulingAlgorithm().getMasterDocument());
		newTask.setOwningProject(this);


		connectTask(newTask); // put task in project list
		taskOutlines.addToAll(newlyCreated,nodeModel); // update all node models except the one passed in
		Task parentTask = newTask.getWbsParentTask();
		Node parentNode = (parentTask == null) ? null : nodeModel.search(newTask.getWbsParentTask());
		Node childNode =  nodeModel.search(newTask);
		setDefaultRelationship(parentNode,childNode);
		newTask.markTaskAsNeedingRecalculation();
		updateScheduling(this,newlyCreated,ObjectEvent.CREATE);
		//objectEventManager.fireCreateEvent(eventSource,newlyCreated,hierarchyInfo);

	}
	public void updateScheduling(Object source,Object newlyCreated,int type){
		ObjectEvent evt = ObjectEvent.getInstance(source,newlyCreated,type,null);
		getSchedulingAlgorithm().objectChanged(evt);
		evt.recycle();
	}
	public void updateScheduling(Object source,Object newlyCreated,int type,Field field){
		ObjectEvent evt = ObjectEvent.getInstance(source,newlyCreated,type,null);
		evt.setField(field);
		getSchedulingAlgorithm().objectChanged(evt);
		evt.recycle();
	}

	/* (non-Javadoc)
	 * @see com.projity.grouping.core.DataFactory#remove(java.lang.Object)
	 */
	public void remove(Object toRemove, NodeModel nodeModel,boolean deep,boolean undo,boolean cleanDependencies){
		Object eventSource=nodeModel;
		if (! (toRemove instanceof Task))
			return; // avoid VoidNodes
		Task task = (Task)toRemove;
		Project owningProject = task.getOwningProject();
		if (owningProject != this) {
			owningProject.taskOutlines.removeFromAll(toRemove,null);
			owningProject.tasks.remove(task);
		}
		task.cleanUp(/*null*/eventSource,deep,undo,cleanDependencies); //lc
		tasks.remove(task);
		taskOutlines.removeFromAll(task,nodeModel); // update all node models except the one passed in

		if (task.isSubproject()) { // remove subproject from portfolio so it won't be saved - fixes bug with it being saved empty
			Project sub = ((SubProj)task).getSubproject();
			ProjectFactory.getInstance().removeProject(sub, false, false, true);
		}
		objectEventManager.fireDeleteEvent(eventSource,task);
	}


	/* (non-Javadoc)
	 * @see com.projity.grouping.core.DataFactory#remove(java.lang.Object)
	 */
	public void removeExternal(Task toRemove) {
//		removeStartSentinelDependency(toRemove);
//		removeEndSentinelDependency(toRemove);
		tasks.remove(toRemove);
		taskOutlines.removeFromAll(toRemove,null); // update all node models except the one passed in
		objectEventManager.fireDeleteEvent(this,toRemove);
	}



	public void saveCurrentToSnapshot(Object snapshotId, boolean entireProject, List selection, boolean undo) {
		if (entireProject) forTasks(new SnapshottableImpl.SaveCurrentToSnapshotClosure(snapshotId));
		else CollectionUtils.forAllDo(selection, new SnapshottableImpl.SaveCurrentToSnapshotClosure(snapshotId));

		fireBaselineChanged(this, null, (Integer)snapshotId,true);

		if (undo){
			UndoableEditSupport undoableEditSupport=getUndoController().getEditSupport();
			if (undoableEditSupport!=null){
				undoableEditSupport.postEdit(new SaveSnapshotEdit(this,snapshotId,entireProject,selection));
			}
		}

	}

	public void restoreSnapshot(Object snapshotId, boolean entireProject, List selection, Collection snapshotDetails) {
		Iterator i;
		if (entireProject) i=getTaskOutlineIterator();
		else{
			if(selection==null) return;
			i=selection.iterator();
		}

		Iterator j=snapshotDetails.iterator();
		while (i.hasNext()){
			NormalTask t=(NormalTask)i.next();
			t.restoreSnapshot(snapshotId,j.next());
		}
		fireBaselineChanged(this, null, (Integer)snapshotId,true);
	}

	public void clearSnapshot(final Object snapshotId, boolean entireProject, List selection, boolean undo) {
		Iterator i;
		if (entireProject) i=getTaskOutlineIterator();
		else i=selection==null?null:selection.iterator();

		final Collection snapshotDetails;
		final boolean[] foundSnapshot=new boolean[1]; //no undo edit of there is no snapshot
		if (undo&& i!=null && i.hasNext()){
			snapshotDetails=new ArrayList();
			while (i.hasNext()){
				NormalTask t=(NormalTask)i.next();
				TaskBackup taskBackup=(TaskBackup)t.backupDetail(snapshotId);
				if (taskBackup.snapshot!=null) foundSnapshot[0]=true;
				snapshotDetails.add(taskBackup);
			}
		} else snapshotDetails=null;

		if (entireProject) forTasks(new SnapshottableImpl.ClearSnapshotClosure(snapshotId));
		else CollectionUtils.forAllDo(selection, new SnapshottableImpl.ClearSnapshotClosure(snapshotId));
//		FieldEvent.fire(this, Configuration.getFieldFromId("Field.baseline" + snapshotId + "Cost"), null);
		fireBaselineChanged(this, null, (Integer)snapshotId,false);

		if (foundSnapshot[0]){
			UndoableEditSupport undoableEditSupport=getUndoController().getEditSupport();
			if (undoableEditSupport!=null){
				undoableEditSupport.postEdit(new ClearSnapshotEdit(this,snapshotId,entireProject,selection,snapshotDetails));
			}
		}

	}
	/**
	 * @param context
	 * @return
	 */
	public String getName(FieldContext context) {
		return hasKey.getName(context);
	}

	public String toString() {
		return getName();
	}
	/**
	 * @param listener
	 */
	public void addScheduleListener(ScheduleEventListener listener) {
		scheduleEventManager.addListener(listener);
	}
	/**
	 * @param listener
	 */
	public void removeScheduleListener(ScheduleEventListener listener) {
		scheduleEventManager.removeListener(listener);
	}

	/**
	 * @param listener
	 */
	public void addObjectListener(ObjectEvent.Listener listener) {
		objectEventManager.addListener(listener);
	}
	/**
	 * @param listener
	 */
	public void removeObjectListener(ObjectEvent.Listener listener) {
		objectEventManager.removeListener(listener);
	}

	public ObjectEventManager getObjectEventManager() {
		return objectEventManager;
	}


	public void fireScheduleChanged(Object source, String type) {
		scheduleEventManager.fire(source,type);
	}
	public void fireScheduleChanged(Object source, String type, Object object) {
		scheduleEventManager.fire(source,type,object);
	}
	public void fireBaselineChanged(Object source, Object object, Integer baselineNumber, boolean save) {
		scheduleEventManager.fireBaselineChanged(source, null, baselineNumber, save);
	}




	/* (non-Javadoc)
	 * @see com.projity.pm.task.BelongsToDocument#getDocument()
	 */
	public Document getDocument() {
		return this;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#buildReverseQuery(com.projity.algorithm.ReverseQuery)
	 */
	public void buildReverseQuery(ReverseQuery reverseQuery) {
		Iterator i = tasks.iterator();
		while (i.hasNext()) {
			((Task)i.next()).buildReverseQuery(reverseQuery);
		}
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#forEachWorkingInterval(org.apache.commons.collections.Closure, boolean)
	 */
	public void forEachWorkingInterval(Closure visitor, boolean mergeWorking, WorkCalendar workCalendar) {
		Iterator i = tasks.iterator();
		while (i.hasNext()) {
			((Task)i.next()).forEachWorkingInterval(visitor,mergeWorking, workCalendar);
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
	 * @see com.projity.pm.assignment.HasTimeDistributedData#cost(long, long)
	 */
	public double cost(long start, long end) {
		return TimeDistributedDataConsolidator.cost(start,end,childrenToRollup());
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#actualCost(long, long)
	 */
	public double actualCost(long start, long end) {
		return TimeDistributedDataConsolidator.actualCost(start,end,childrenToRollup());
	}

	public double fixedCost(long start, long end) {
		return TimeDistributedDataConsolidator.fixedCost(start,end,childrenToRollup());
	}

	public double actualFixedCost(long start, long end) {
		return TimeDistributedDataConsolidator.actualFixedCost(start,end,childrenToRollup());
	}

	public boolean fieldHideActualFixedCost(FieldContext fieldContext) {
		return false;
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
	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#actualWork(long, long)
	 */
	public long remainingWork(long start, long end) {
		return TimeDistributedDataConsolidator.remainingWork(start,end,childrenToRollup(),true);
	}
	boolean isInRange(long start, long finish) {
		long s = getStart();
		return (finish > s && start < getEnd());
	}

	private boolean isFieldHidden(FieldContext fieldContext) {
		return fieldContext != null && !isInRange(fieldContext.getStart(),fieldContext.getEnd());
	}

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
		return false; //TODO implement
	}
	public boolean fieldHideBaselineWork(int numBaseline,FieldContext fieldContext) {
		return false; //TODO implement
	}
	public boolean fieldHideAcwp(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}
	public boolean fieldHideBac(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}
	public boolean fieldHideBcwp(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
	}
	public boolean fieldHideBcws(FieldContext fieldContext) {
		return isFieldHidden(fieldContext);
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
		return getFixedCost(fieldContext) +
		cost(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getFixedCost(FieldContext fieldContext) {
		return fixedCost(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getActualFixedCost(FieldContext fieldContext) {
		return actualFixedCost(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}

	public long getWork(FieldContext fieldContext) {
		return work(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getActualCost(FieldContext fieldContext) {
		return getActualFixedCost(fieldContext) +
			actualCost(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public long getActualWork(FieldContext fieldContext) {
		return actualWork(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public long getRemainingWork(FieldContext fieldContext) {
		return remainingWork(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getRemainingCost(FieldContext fieldContext) {
		return getCost(fieldContext) - getActualCost(fieldContext);
	}

	public double getBaselineCost(FieldContext fieldContext) {
		return baselineCost(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public long getBaselineWork(FieldContext fieldContext) {
		return baselineWork(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}

	public void nodeRemoved(HierarchyEvent e) {

	}


	public double getAcwp(FieldContext fieldContext) {
		return acwp(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getBac(FieldContext fieldContext) {
		return bac(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getBcwp(FieldContext fieldContext) {
		return bcwp(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getBcws(FieldContext fieldContext) {
		return bcws(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getCv(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().cv(this,FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getSv(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().sv(this,FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getEac(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().eac(this,FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getVac(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().vac(this,FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getCpi(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().cpi(this,FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getSpi(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().spi(this,FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getCsi(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().csi(this,FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getCvPercent(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().cvPercent(this,FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getSvPercent(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().svPercent(this,FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getTcpi(FieldContext fieldContext) {
		return EarnedValueCalculator.getInstance().tcpi(this,FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#childrenToRollup()
	 */
	public Collection childrenToRollup() {
		return tasks;
	}
	public String getSchedulingMethod() {
		return schedulingAlgorithm.getName();
	}
	//TODO other baselines
	public double getBaselineCost(int numBaseline, FieldContext fieldContext) {
		return baselineCost(FieldContext.start(fieldContext),FieldContext.end(fieldContext));	}
	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.TimeDistributedFields#getBaselineWork(int, com.projity.field.FieldContext)
	 */
	public long getBaselineWork(int numBaseline, FieldContext fieldContext) {
		return baselineWork(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	/**
	 * @param initialized The initialized to set.
	 */
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}




	public void nodesChanged(HierarchyEvent e) {
//	    System.out.println("Node changed ...");
//	    if ("NO_PROJECT_UPDATE".equals(e.getFlag())) return;
		Node node, previousParentNode, newParentNode;
		Task task, previousParentTask, newParentTask;
		int count=e.getNodes().length;
		if (count==0) return;
		for (int i=0;i<count;i++){
			node=(Node)e.getNodes()[i];
			if (!(node.getImpl() instanceof Task)) continue;
			task=(Task) node.getImpl();

			//TODO verify that this is ok when pasting for bug 426
//			task.setProject((Project) getSchedulingAlgorithm().getMasterDocument());
//			task.setOwningProject(this);
			//moved to validateObject

			previousParentTask=task.getWbsParentTask();
			previousParentNode=taskOutlines.getDefaultOutline().search(previousParentTask);

			// refresh the previous parent's children
			if (previousParentTask!=null) {
				previousParentTask.markAllDependentTasksAsNeedingRecalculation(true); // flag this and dependent tasks as dirty
				previousParentTask.setWbsChildrenNodes(taskOutlines.getDefaultOutline().getHierarchy().getChildren(previousParentNode));
			}

			// refresh the new parent's children
			NodeHierarchy hierarchy=taskOutlines.getDefaultOutline().getHierarchy();
			newParentNode = hierarchy.getParent(node);
			newParentTask = null;
			if (newParentNode!=hierarchy.getRoot()) {
				newParentTask=(Task)newParentNode.getImpl();
				newParentTask.setWbsChildrenNodes(taskOutlines.getDefaultOutline().getHierarchy().getChildren(newParentNode));
				newParentTask.restrictToValidConstraintType();
		//		newParentTask.setParentDuration(); //hk
				newParentTask.markAllDependentTasksAsNeedingRecalculation(true); // flag this and dependent tasks as dirty
			}

			//refresh this node to point to new parent
			task.setWbsParent(newParentTask);

			final Task _newParentTask = newParentTask;
			final Object eventSource = e.getSource();

			// recursively remove all dependencies between new parent and any children, grandchildren, etc.
			taskOutlines.getDefaultOutline().getHierarchy().visitAll(newParentNode, new Closure() {
				public void execute(Object arg) {
					Node node=(Node)arg;
					if (!(node.getImpl() instanceof Task)) return;
					Task task = (Task) node.getImpl();
					DependencyService.getInstance().removeAnyDependencies(task, _newParentTask,eventSource);
				}
			});
		}
		if (!e.isVoid()) { // if the event was not the promotion of a void node
			// The critical path needs resetting because its internal list must be rebuilt as it depends on hierarchy
//			objectEventManager.fireUpdateEvent(e.getSource(),this);
			e.consume();
			updateScheduling(e.getSource(),this,ObjectEvent.CREATE); // will cause critical path to reset and to run and send schedule events
		}
	}
	public void nodesInserted(HierarchyEvent e) {
		nodesChanged(e);
	}
	public void nodesRemoved(HierarchyEvent e) {
	}
    public void structureChanged(HierarchyEvent e) {
    }

    public List getRootNodes(List tasks){
        List roots=new LinkedList();
        for (Iterator i=tasks.iterator();i.hasNext();){
            Task task=(Task)i.next();
            if (task.getWbsParentTask()==null) roots.add(taskOutlines.getDefaultOutline().search(task));
        }
        return roots;
    }

    public void dump(Collection tasks,String indent){
        if (tasks!=null)
        for (Iterator i=tasks.iterator();i.hasNext();){
            Node node=(Node)i.next();
            Task task=(Task)node.getImpl();
            System.out.println(indent+task.getWbsParentTask()+"->"+task);
            dump(task.getWbsChildrenNodes(),indent+"-");
        }
    }

/*
	public void clearDuration() {
		schedule.clearDuration();
	}
	public long getDurationActive() {
		return schedule.getDurationActive();
	}
	public long getDurationSpan() {
		return schedule.getDurationSpan();
	}
	public double getPercentComplete() {
		return schedule.getPercentComplete();
	}
	public void setDurationActive(long durationActive) {
		schedule.setDurationActive(durationActive);
	}
	public void setDurationSpan(long durationSpan) {
		schedule.setDurationSpan(durationSpan);
	}
	public void setPercentComplete(double percentComplete) {
		schedule.setPercentComplete(percentComplete);
	}
	public long getDependencyStart() {
		return schedule.getDependencyStart();
	}
	public void setDependencyStart(long dependencyStart) {
		schedule.setDependencyStart(dependencyStart);
	}
*/

	private transient BarClosure barClosureInstance = new BarClosure();
	public void moveInterval(Object eventSource, long start, long end, ScheduleInterval oldInterval) {
		if (start != oldInterval.getStart())
			setStart(start); // allow for changing start of subproject.  need to add  test for actuals
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.scheduling.Schedule#consumeIntervals(com.projity.functor.IntervalConsumer)
	 */
	public void consumeIntervals(IntervalConsumer consumer) {
		consumer.consumeInterval(new ScheduleInterval(getStart(),getEnd()));
	}


	public boolean equals(Object obj){
	    if (obj instanceof DataObject){
	        return getName().equals(((DataObject)obj).getName());
	    }
	    return false;
	}
	Workspace workspace;
	private void writeObject(ObjectOutputStream s) throws IOException {
		workspace = (Workspace) createWorkspace(SavableToWorkspace.PERSIST);
	    s.defaultWriteObject();
	    hasKey.serialize(s);
	}
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException  {
	    s.defaultReadObject();
	    hasKey=HasKeyImpl.deserialize(s,this);
	    //initUndo();
	    tasks = new LinkedList();
		objectEventManager = new ObjectEventManager();
		objectSelectionEventManager = new ObjectSelectionEventManager();
		scheduleEventManager = new ScheduleEventManager();
		multipleTransactionManager = new MultipleTransactionManager();
		projectListenerList=new EventListenerList();
	    taskOutlines=new OutlineCollectionImpl(Settings.numHierarchies(),this);
	    barClosureInstance = new BarClosure();


	}

	private void initSubprojectHandler() {
		try {
			subprojectHandler = (SubprojectHandler) Class.forName(Messages.getMetaString("SubprojectHandler")).getConstructor(new Class[]{Project.class}).newInstance(this);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SubprojectHandler not valid in meta.properties");
			System.exit(-1);
		}
	}
	public void postDeserialization(){
		lastDeserialized = this;
		initSubprojectHandler();	    //this is created transiently
	    setSchedulingAlgorithm(new CriticalPath(this)); // Critical path needs objectEventManager

	    int count=Settings.numHierarchies();
		for (int i=0;i<count;i++){
			NodeModel model=taskOutlines.getOutline(i);
			if (model==null) continue;
			if (model instanceof AssignmentNodeModel){
				AssignmentNodeModel aModel=(AssignmentNodeModel)model;
				aModel.setContainsLeftObjects(true);
				aModel.setDocument(this);
			}
			model.setUndoController(undoController);
		}

		//setEnd(getStart());
		//setInitialized(true);
		//initialize();
		initializeDefaultOutline();
		setInitialized(true);
		setGroupDirty(false);
	    if (workspace != null)
	    	restoreWorkspace(workspace, SavableToWorkspace.PERSIST);
	    if (calendarOption == null)
	    	calendarOption = CalendarOption.getDefaultInstance();

	}

	public void addEndSentinelDependency(Task task) {
		if (!task.isInSubproject())  // subprojects have fixed dates, and their children do not depend on master project's sentinels
			schedulingAlgorithm.addEndSentinelDependency(task);
	}
	public boolean removeEndSentinelDependency(Task task) {
		if (!task.isInSubproject())  // subprojects have fixed dates, and their children do not depend on master project's sentinels
			return schedulingAlgorithm.removeEndSentinelDependency(task);
		return false;
	}
	public void addStartSentinelDependency(Task task) {
		if (!task.isInSubproject())  // subprojects have fixed dates, and their children do not depend on master project's sentinels
			schedulingAlgorithm.addStartSentinelDependency(task);
	}
	public boolean removeStartSentinelDependency(Task task) {
		if (!task.isInSubproject())  // subprojects have fixed dates, and their children do not depend on master project's sentinels
			return schedulingAlgorithm.removeStartSentinelDependency(task);
		return false;
	}

	/**
	 * @param date
	 */
	public void setEndConstraint(long date) {
		schedulingAlgorithm.setEndConstraint(date);
	}
	/**
	 * @param date
	 */
	public void setStartConstraint(long date) {
//		System.out.println("setStartConstraint " + new Date(date));
		schedulingAlgorithm.setStartConstraint(date);
	}
	/**
	 * @return Returns the forward.
	 */
	public boolean isForward() {
		return forward;
	}
	/**
	 * @param forward The forward to set.
	 */
	public void setForward(boolean forward) {
		if (forward == this.forward)
			return;
		this.forward = forward;
		Iterator i = tasks.iterator();
		// update all of the tasks schedules to reflect their scheduling direction
		while (i.hasNext()) {
			((Task)i.next()).setForward(forward);
		}
		markAllTasksAsNeedingRecalculation(false);
		schedulingAlgorithm.setForward(forward);
		schedulingAlgorithm.reset();
		schedulingAlgorithm.calculate(true);
	}

	public void fireUpdateEvent(Object source, Object object) {
		if (isInitialized())
			objectEventManager.fireUpdateEvent(source,object);
	}

	int getCalculationStateCount() {
		if (schedulingAlgorithm == null)
			return 0;
		return schedulingAlgorithm.getCalculationStateCount();
	}
	/**
	 * @return Returns the multipleTransactionManager.
	 */
	public final MultipleTransactionManager getMultipleTransactionManager() {
		return multipleTransactionManager;
	}

	/* (non-Javadoc)
	 * @see com.projity.document.Document#fireMultipleTransaction(int, boolean)
	 */
	public int fireMultipleTransaction(int id, boolean begin) {
		return multipleTransactionManager.fire(this,id,begin);
	}


	private void repairTasks() {
		Iterator i = tasks.iterator();
		NormalTask task;
		while (i.hasNext()) {
			task = (NormalTask)i.next();
			if (task.validateConstraints())
				addRepaired(task);
			if (task.getAssignments().isEmpty()) {
				Assignment ass = task.addDefaultAssignment();
				ass.setDirty(true);
				task.setDirty(true);
				ErrorLogger.logOnce("NoAssignment","Repaired task with no assignments",null);
				System.out.println("added default ass for " + task);
				addRepaired(task);
			}
		}
	}
/**
 * When opening a project or just after saving, need to put all tasks back to their undirty state.
 * This means the task is considered as being untouched since the last save.
 *
 */
	public void setAllTasksAsUnchangedFromPersisted(boolean justSaved) {
		getTaskOutline().getHierarchy().visitAll(new Closure(){
			int id=1;
			public void execute(Object o) {
				Node node=(Node)o;
				if (node.getImpl() instanceof NormalTask){
					NormalTask task=(NormalTask)node.getImpl();
					task.setDirty(false);
					task.setLastSavedStart(task.getStart()); //
					task.setLastSavedFinish(task.getEnd());
					Iterator j = task.getAssignments().iterator();
					while (j.hasNext())
						((Assignment)j.next()).setDirty(false);


					j=task.getDependencyList(true).iterator();
					while (j.hasNext())
						((Dependency)j.next()).setDirty(false);

					Node parent=(Node)node.getParent();
					if (parent==null||parent.isRoot()) task.setLastSavedParentId(-1L);
					else task.setLastSavedParentId(((Task)parent.getImpl()).getUniqueId());
					task.setLastSavedPosistion(parent.getIndex(node));



				}
			}
		});


	    // in case any tasks were repaired (rare), mark them as dirty
		if (!justSaved && repaired != null) {
		    Iterator<Task> i = repaired.iterator();
		    while (i.hasNext()) {
		    	NormalTask t = (NormalTask)i.next();
		    	t.setTaskAssignementAndPredsDirty();
		    }
		    repaired = null;
		}
	}
	void addRepaired(Task t) {
		if (repaired == null)
			repaired = new LinkedList<Task>();
		repaired.add(t);
	}
	public void markAllTasksAsNeedingRecalculation(boolean invalidateSchedules) {
		int nextStateCount = getCalculationStateCount()+1;
		Iterator i = tasks.iterator();
		Task task;
		// update all of the tasks schedules to reflect their scheduling direction
		while (i.hasNext()) {
			task = (Task)i.next();
			task.setCalculationStateCount(nextStateCount);
			if (invalidateSchedules)
				task.invalidateSchedules();
		}
		getSchedulingAlgorithm().initEarliestAndLatest();
	}
	public void setAllChildrenDirty(boolean dirty) { // used when changing field dirties all tasks
		Iterator i = tasks.iterator();
		Task task;
		while (i.hasNext()) {
			task = (Task)i.next();
			task.setDirty(dirty);
		}
	}
	public void setAllDirty() {
		setDirty(true);
		setGroupDirty(true);
		Iterator i = tasks.iterator();
		NormalTask task;
		// update all of the tasks schedules to reflect their scheduling direction
		while (i.hasNext()) {
			task = (NormalTask)i.next();
			task.setDirty(false);
			Iterator j = task.getAssignments().iterator();
			while (j.hasNext())
				((Assignment)j.next()).setDirty(true);
			j=task.getDependencyList(true).iterator();
			while (j.hasNext())
				((Dependency)j.next()).setDirty(true);
		}
	}

	int getDefaultConstraintType(){
		if (isForward())
			return ConstraintType.ASAP;
		else
			return ConstraintType.ALAP;
	}

	/* (non-Javadoc)
	 * @see com.projity.document.Document#getDefaultCalendar()
	 */
	public WorkCalendar getDefaultCalendar() {
		return getWorkCalendar();
	}

	// these fields are not modifiable
	public void setWork(long work, FieldContext fieldContext) {
		//do nothing
	}
	public void setRemainingWork(long work, FieldContext fieldContext) {
		//do nothing
	}
	public void setActualWork(long work, FieldContext fieldContext) {
		//do nothing
	}
    public SchedulingAlgorithm getSchedulingAlgorithm() {
        return schedulingAlgorithm;
    }
	public void setSchedulingAlgorithm(SchedulingAlgorithm schedulingAlgorithm) {
		if (this.schedulingAlgorithm != null) {
			removeObjectListener(this.schedulingAlgorithm);
			getMultipleTransactionManager().removeListener(this.schedulingAlgorithm);
		}
		this.schedulingAlgorithm = schedulingAlgorithm;
		addObjectListener(schedulingAlgorithm);
		getMultipleTransactionManager().addListener(schedulingAlgorithm);
	}
	public boolean isReadOnlyWork(FieldContext fieldContext) {
		return true;
	}
	public boolean isReadOnlyActualWork(FieldContext fieldContext) {
		return true;
	}
	public boolean isReadOnlyRemainingWork(FieldContext fieldContext) {
		return true;
	}

	public void setFixedCost(double fixedCost, FieldContext fieldContext) {
	}

	public boolean isReadOnlyFixedCost(FieldContext fieldContext) {
		return true;
	}

//	public boolean isNew() {
//		return hasKey.isNew();
//	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#isLabor()
	 */
	public boolean isLabor() {
		return true;
	}

	public final long getStartDate() {
		return getStart();
	}

	public final void setStartDate(long start) {
		start = getEffectiveWorkCalendar().adjustInsideCalendar(start,false);
		setStart(start);
		getSchedulingAlgorithm().setStartConstraint(start);
	}

	public final boolean isReadOnlyStartDate(FieldContext fieldContext) {
		return getSchedulingAlgorithm() == null || !getSchedulingAlgorithm().isForward();
	}

	public final long getFinishDate() {
		return getEnd();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.task.ProjectSpecificFields#setFinishDate(long)
	 */
	public void setFinishDate(long finish) {
		finish = getEffectiveWorkCalendar().adjustInsideCalendar(finish,true);
		setEnd(finish);
		getSchedulingAlgorithm().setEndConstraint(finish);
	}

	public boolean isReadOnlyFinishDate(FieldContext fieldContext) {
		return getSchedulingAlgorithm() == null || getSchedulingAlgorithm().isForward();
	}
	public final long getCurrentDate() {
		return currentDate;
	}
	public final void setCurrentDate(long currentDate) {
		this.currentDate = currentDate;
	}

	public long getBaselineStart(int numBaseline) {
		NormalTask task;
		long result = Long.MAX_VALUE;
		long val;
        for (Iterator i=tasks.iterator();i.hasNext();){
            task=(NormalTask)i.next();
            val = task.getBaselineStart(numBaseline);
            if (val != 0 && val < result)
            	result = val;
        }
        if (result == Long.MAX_VALUE)
        	result = 0;
        return result;

	}

	public long getBaselineFinish(int numBaseline) {
		NormalTask task;
		long result = 0;
		long val;
        for (Iterator i=tasks.iterator();i.hasNext();){
            task=(NormalTask)i.next();
            val = task.getBaselineFinish(numBaseline);
            if (val > result)
            	result = val;
        }
        return result;
	}

	public long getBaselineDuration(int numBaseline) {
		// note that I am using the current calendar no matter what.
		return getEffectiveWorkCalendar().compare(getBaselineFinish(numBaseline),getBaselineStart(numBaseline),false);
	}

	public long getActualStart() {
		Task task;
		long result = Long.MAX_VALUE;
		long val;
        for (Iterator i=tasks.iterator();i.hasNext();){
            task=(Task)i.next();
            val = task.getActualStart();
            if (val != 0 && val < result)
            	result = val;
        }
        if (result == Long.MAX_VALUE)
        	result = 0;
        return result;
	}

	public void setActualStart(long actualStart) {
	}

	public long getActualFinish() {
		Task task;
		long result = 0;
		long val;
        for (Iterator i=tasks.iterator();i.hasNext();){
            task=(Task)i.next();
            val = task.getActualFinish();
            if (val == 0)
            	break;
            if (val > result)
            	result = val;
        }
        return result;
	}

	public long getStop() {
		Task task;
		long result = 0;
		long val;
        for (Iterator i=tasks.iterator();i.hasNext();){
            task=(Task)i.next();
            val = task.getStop();
            if (val == 0)
            	return 0;
            if (val > result)
            	result = val;
        }
        return result;
	}

	public long getEarliestStop() {
		Task task;
		long result = Long.MAX_VALUE;
		long val;
        for (Iterator i=tasks.iterator();i.hasNext();){
            task=(Task)i.next();
            val = task.getEarliestStop();
            if (val < result)
            	result = val;
            if (val == 0)
            	break;
        }
        if (result == Long.MAX_VALUE)
        	result = 0;
        return result;
	}

	public void setActualFinish(long actualFinish) {
	}

	public long getActualDuration() {
		long stop = getStop();
		if (stop == 0)
			return 0;
		return getEffectiveWorkCalendar().compare(stop,getStartDate(),false);
	}

	public void setActualDuration(long actualDuration) {
	}

	public long getRemainingDuration() {
		long stop = getStop();
		if (stop == 0)
			stop = getStartDate();
		return getEffectiveWorkCalendar().compare(getFinishDate(),stop,false);
	}

	//TODO to avoid risks of breaking obfuscation, Project will implement all of Schedule for now
	public void setRemainingDuration(long remainingDuration) {
	}
	public double getPercentComplete() {
		Task task;
		long actual = 0L;
		long total = 0L;
        for (Iterator i=tasks.iterator();i.hasNext();){
            task=(Task)i.next();
            actual += Duration.millis(task.getActualDuration());
            total += Duration.millis(task.getDuration());
        }
        if (total == 0L)
        	return 0D;
        else
        	return ((double)actual) / total;
	}
	public void setPercentComplete(double percentComplete) {
	}

	public void setDuration(long duration) {
	}
	public long getElapsedDuration() {
		return Math.round(getEffectiveWorkCalendar().compare(getEnd(), getStart(),true) * CalendarOption.getInstance().getFractionOfDayThatIsWorking());
	}

	public long getDependencyStart() {
		return 0;
	}

	public void setDependencyStart(long dependencyStart) {
	}

	public long getResume() {
		return 0;
	}

	public void setResume(long resume) {
	}

	public void setStop(long stop) {
	}

	public void clearDuration() {
	}

	public void moveRemainingToDate(long date) {
	}

	public void moveInterval(Object eventSource, long start, long end, ScheduleInterval oldInterval, boolean isChild) {
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.scheduling.Schedule#split(java.lang.Object, long, long)
	 */
	public void split(Object eventSource, long from, long to) {
		// TODO Auto-generated method stub

	}
	public final boolean isDirty() {
		return isDirty;
	}
	public final void setDirty(boolean dirty) {
		//System.out.println("Project _setDirty("+dirty+"): "+getName());
		this.isDirty = isDirty;
		if (isDirty)
			setGroupDirty(true);
	}

	public final boolean isGroupDirty() {
		return isGroupDirty;
	}
	public final void setGroupDirty(boolean isGroupDirty) {
		boolean old=this.isGroupDirty;
		this.isGroupDirty = isGroupDirty;
		if (old!=isGroupDirty){
//			System.out.println("Project["+hashCode()+"].setGroupDirty("+isGroupDirty+")");
			fireGroupDirtyChanged(this, old);
		}
	}

	//Undo
	protected transient DataFactoryUndoController undoController;
//	protected void initUndo(){
//		undoController=new DataFactoryUndoController(this);
//	}
	public DataFactoryUndoController getUndoController() {
		return undoController;
	}


	public void setUndoController(DataFactoryUndoController undoController) {
		this.undoController = undoController;
		if (undoController!=null) undoController.setDataFactory(this);
	}

	public static Predicate instanceofPredicate() {
		return new Predicate() {
			public boolean evaluate(Object arg0) {
				return arg0 instanceof Project;
			}};
	}


	public void addPastedTask(Task task) {
		getSchedulingAlgorithm().addObject(task);
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.HasCalendar#invalidateCalendar()
	 */
	public Document invalidateCalendar() {
		markAllTasksAsNeedingRecalculation(false);
		return this;
	}

	public WorkCalendar getBaseCalendar() {
		return getWorkCalendar();
	}

	public void setBaseCalendar(WorkCalendar baseCalendar) throws CircularDependencyException {
		setWorkCalendar(baseCalendar);
	}

	public boolean fieldHideBaseCalendar(FieldContext fieldContext) {
		return false;
	}

	public boolean isJustModified(){
		return true; //Not used
	}

	public void setComplete(boolean complete) {
		ScheduleUtil.setComplete(this,complete);
	}
	public boolean isComplete() {
		return getPercentComplete() == 1.0D;
	}


	protected boolean master=false;
	public boolean isMaster() {
		return master;
	}

	public void setMaster(boolean master) {
		this.master = master;
	}

	public boolean isLocal() {
		return hasKey.isLocal();
	}

	public void setLocal(boolean local) {
		hasKey.setLocal(local);
	}

	public boolean isSavable() {
		return Environment.isOpenProj() || (!isLocal() && !isReadOnly());
	}


	protected transient boolean temporaryLocal;

	public boolean isTemporaryLocal() {
		return temporaryLocal;
	}

	public void setTemporaryLocal(boolean temporaryLocal) {
		this.temporaryLocal = temporaryLocal;
	}

	public boolean isLockable() {
		return !(temporaryLocal||isLocal());
	}

	public boolean applyTimesheet(Collection fieldArray) {
		return applyTimesheet(fieldArray,System.currentTimeMillis());
	}
	public boolean applyTimesheet(Collection fieldArray, long timesheetUpdateDate) {
		return TimesheetHelper.applyTimesheet(getTasks(),fieldArray,timesheetUpdateDate);
	}
	public long getLastTimesheetUpdate() {
		return TimesheetHelper.getLastTimesheetUpdate(getTasks());
	}

	public boolean isPendingTimesheetUpdate() {
		return TimesheetHelper.isPendingTimesheetUpdate(getTasks());
	}

	public int getTimesheetStatus() {
		return TimesheetHelper.getTimesheetStatus(getTasks());
	}
	public String getTimesheetStatusName() {
		return TimesheetHelper.getTimesheetStatusName(getTimesheetStatus());
	}

	public void rollbackUnvalidated(NodeModel nodeModel, Object object) {
	}

	public void initOutline(NodeModel nodeModel){}

	public final long getCompletedThrough	() {
		long start = getStart();
		if (start == 0)
			return 0;
		long actualDuration = DateTime.closestDate(getDuration() * getPercentComplete());
		return getEffectiveWorkCalendar().add(start,actualDuration,true);
	}
	public final void setCompletedThrough(long completedThrough) {
		// do nothing
	}

	public final boolean isOpenedAsSubproject() {
		return openedAsSubproject;
	}

	public final void setOpenedAsSubproject(boolean openedAsSubproject) {
		this.openedAsSubproject = openedAsSubproject;
	}

	private transient ExternalTaskManager externalTaskManager = null;
	private ExternalTaskManager getExternalTaskManager() {
		if (externalTaskManager == null)
			externalTaskManager = new ExternalTaskManager();
		return externalTaskManager;
	}
	public void addExternalTask(Task task) {
		getExternalTaskManager().add(task);
		task.markTaskAsNeedingRecalculation();
	}
	public void handleExternalTasks(Project project, boolean opening, boolean saving) {
		getExternalTaskManager().handleExternalTasks(project,opening, saving);
		project.getExternalTaskManager().handleExternalTasks(this, opening,saving);

	}
	public boolean needsSaving() {
		return (isSavable() && isGroupDirty()/*||getResourcePool().isGroupDirty()*/);
	}
	public long getEarliestStartingTask() {
		return earliestStartingTask;
	}
	public long getEarliestStartingTaskOrStart() {
		if (isOpenedAsSubproject())
			return earliestStartingTask;
		long early = ((CriticalPath)getSchedulingAlgorithm()).getEarliestStart();
		if (early == 0) {
			early = getStart();
			System.out.println("0 earliest start for project. Forward = " + isForward() + " using proj start " + new Date(early));
		}

		return early;
	}
	public long getLatestFinishingTask() {
		if (isOpenedAsSubproject())
			return latestFinishingTask;
		long late = ((CriticalPath)getSchedulingAlgorithm()).getLatestFinish();
		if (late == 0 || late == Long.MAX_VALUE) {
			late = getEnd();
			System.out.println("" + late + " latest finish for project. Forward = " + isForward() + " using proj end " + new Date(end));
		}

		return late;
	}

	public final Map getExtraFields() {
		if (extraFields == null)
			extraFields = new HashMap();
		return extraFields;
	}

	public final void setExtraFields(Map extraFields) {
		this.extraFields = extraFields;
	}

	public final Hyperlink getDocumentFolderUrl() {
		return documentFolderUrl;
	}

	public final void setDocumentFolderUrl(Hyperlink documentFolderUrl) {
		this.documentFolderUrl = documentFolderUrl;
	}

	public final boolean isReadOnly() {
		return readOnly;
	}

	public final void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public final Collection getReferringSubprojectTasks() {
		return subprojectHandler.getReferringSubprojectTasks();
	}

	public final void setReferringSubprojectTasks(Collection referringSubprojectTasks) {
		subprojectHandler.setReferringSubprojectTasks(referringSubprojectTasks);
	}

	public final Task getContainingSubprojectTask() {
		return subprojectHandler.getContainingSubprojectTask();
	}

	public final void setContainingSubprojectTask(Task subprojectTask) {
		this.subprojectHandler.setContainingSubprojectTask(subprojectTask);
	}

	public long getFinishOffset() {
		return EarnedValueCalculator.getInstance().getFinishOffset(this);
	}

	public long getStartOffset() {
		return EarnedValueCalculator.getInstance().getStartOffset(this);
	}

	public double getRisk() {
		return risk;
	}

	public void setRisk(double risk) {
		this.risk = risk;
	}

	public int getProjectType() {
		return projectType;
	}

	public void setProjectType(int projectType) {
		this.projectType = projectType;
	}

	public int getExpenseType() {
		return expenseType;
	}

	public void setExpenseType(int budgetType) {
		this.expenseType = budgetType;
	}

	public int getEffectiveExpenseType() {
		return getExpenseType();
	}

	public String getDivision() {
		return division;
	}

	public void setDivision(String division) {
		this.division = division;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public int getProjectStatus() {
		return projectStatus;
	}

	public void setProjectStatus(int projectStatus) {
		this.projectStatus = projectStatus;
	}
	public ImageLink getBudgetStatusIndicator() {
		return EarnedValueCalculator.getInstance().getBudgetStatusIndicator(getCpi(null));
	}

	public ImageLink getScheduleStatusIndicator() {
		return EarnedValueCalculator.getInstance().getBudgetStatusIndicator(getSpi(null));
	}

	public Object backupDetail() {
		// TODO Auto-generated method stub
		return null;
	}

	public void restoreDetail(Object source,Object detail,boolean isChild) {
		// TODO Auto-generated method stub

	}

	public boolean containsAssignments(){return true;}


	public void beginUndoUpdate(){
		if (undoController!=null) undoController.beginUpdate();
	}
	public void endUndoUpdate(){
		if (undoController!=null) undoController.endUpdate();
	}

	public boolean renumber(boolean localOnly){
		boolean r=false;
		long uniqueId=getUniqueId();
        for (Iterator i=getTaskOutlineIterator();i.hasNext();){
            NormalTask task=(NormalTask)i.next(); //ResourceImpl to have the EnterpriseResource link
            if (task.getProjectId() != uniqueId) // skip if in another project
            	continue;

            r|=task.renumber(localOnly);
        }
		r|=hasKey.renumber(localOnly);
		if (!r) return false;
		uniqueId=getUniqueId();
        for (Iterator i=getTaskOutlineIterator();i.hasNext();){
            NormalTask task=(NormalTask)i.next();
            task.setProjectId(uniqueId);
        }
        return true;
	}

	protected transient int accessControlPolicy;
	public int getAccessControlPolicy() {
		return accessControlPolicy;
	}

	public void setAccessControlPolicy(int accessControlPolicy) {
		this.accessControlPolicy = accessControlPolicy;
	}


	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastModificationDate() {
		return lastModificationDate;
	}

	public void setLastModificationDate(Date lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}

/**
 * Automatically link all siblings at all levels
 * A condition can be applied. The condition tests the task and sees whether it can be a successor task ornot
 * @param parent - should be null if whole project
 */
	public void linkAllSiblings(Node parent, Predicate canBeSuccessorCondition, Object eventSource) {
		List<Node> children = getTaskModel().getChildren(parent);

		if (children == null)
			return;
		try {
			DependencyService.getInstance().connect(NodeList.nodeListToImplList(children, NotAssignmentFilter.getInstance()),eventSource,canBeSuccessorCondition);
		} catch (InvalidAssociationException e) {
			e.printStackTrace();
		}
		for (Node n : children) // recursively do children
			linkAllSiblings(n,canBeSuccessorCondition,eventSource);

	}
	public List<NormalTask> getRootTasks() {
		List<Node> children = getTaskModel().getChildren(null);
		return 	NodeList.nodeListToImplList(children);
	}

	public List<Resource> getRootResources() {
		List<Node> children = getResourceModel().getChildren(null);
		return 	NodeList.nodeListToImplList(children);
	}

	public boolean isCriticalPathJustChanged() {
		return ((CriticalPath)getSchedulingAlgorithm()).isCriticalPathJustChanged();
	}

	public int getBenefit() {
		return benefit;
	}

	public void setBenefit(int benefit) {
		this.benefit = benefit;
	}

	public double getNetPresentValue() {
		return netPresentValue;
	}

	public void setNetPresentValue(double netPresentValue) {
		this.netPresentValue = netPresentValue;
	}

	protected transient String fileName;
	public String getFileName(){
		return fileName;
	}
	public String getGuessedFileName(){
		if (fileName!=null) return fileName;
		String name=getName();
		if (name==null) return null;
		return getName()+"."+FileHelper.getFileExtension(fileType);
	}
//	public String getDefaultExtension(){
//		return Environment.getStandAlone()?FileHelper.DEFAULT_FILE_EXTENSION:"xml";
//	}
	public void setFileName(String fileName){
		this.fileName=fileName;
		if (fileName!=null){
			setFileType(FileHelper.getFileType(fileName));
		}
	}
	public String getTitle(){
		return getName()+(fileName==null?"":(" - "+fileName));
	}

	protected transient int fileType=FileHelper.PROJITY_FILE_TYPE;

	public int getFileType() {
		return fileType;
	}

	public void setFileType(int fileType) {
		this.fileType = fileType;
	}

	public void setBoundsAfterReadProject() {
		getSchedulingAlgorithm().setEarliestAndLatest(getStart(), getEnd());
		fireScheduleChanged(this,ScheduleEvent.SCHEDULE);
	}

	public void setAllTasksInSubproject(boolean b, Project masterProject) {
		// TODO Auto-generated method stub

	}

	public void setAllNodesInSubproject(boolean b) {
		// TODO Auto-generated method stub

	}

	public SubprojectHandler getSubprojectHandler() {
		return subprojectHandler;
	}

	public long getReferringSubprojectTaskDependencyDate() {
		return subprojectHandler.getReferringSubprojectTaskDependencyDate();
	}

	public String getSubprojectOf() {
		return subprojectHandler.getSubprojectOf();
	}
	public void resetRoles(boolean publicRoles) {
		try {
			Class.forName(Messages.getMetaString("ProjectRoleManager")).getDeclaredMethod("resetRoles", new Class[] {Project.class, Boolean.class}).invoke(null, new Object[] {this,publicRoles});
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ProjectRoleManager not valid in meta.properties");
			System.exit(-1);
		}
	}

	public class Workspace implements WorkspaceSetting {
		private static final long serialVersionUID = 6909144693873463556L;
		WorkspaceSetting spreadsheetWorkspace;
		HashMap fieldAliasMap;
		PrintSettings printSettings;
		CalendarOption calendarOption;

	}
	public transient SpreadSheetFieldArray fieldArray = null;
	public transient PrintSettings printSettings = null;
	public transient PrintSettings tmpSettings = null;
	public transient CalendarOption calendarOption = null;

	public PrintSettings getPrintSettings(int context) {
		return context==SavableToWorkspace.PERSIST?printSettings:tmpSettings;
	}

	public void setPrintSettings(PrintSettings printSettings) {
		this.printSettings = printSettings;//==null?null:(PrintSettings)printSettings.clone();
		setGroupDirty(true);
	}
//	public void savePrintSettings() {
//		if (printSettings!=null){
//			this.printSettings = (PrintSettings)tmpSettings.clone();
//			printSettings.updateWorkspace();
//			setGroupDirty(true);
//		}
//	}

	public CalendarOption getCalendarOption() {
		return calendarOption;
	}

	public void setCalendarOption(CalendarOption calendarOption) {
		this.calendarOption = calendarOption;
		setGroupDirty(true);
	}

	public PrintSettings getTmpSettings() {
		return tmpSettings;
	}

	public void setTmpSettings(PrintSettings tmpSettings) {
		this.tmpSettings = tmpSettings;
	}

	public void restoreWorkspace(WorkspaceSetting w, int context) {
		Workspace ws = (Workspace) w;
		if (ws.spreadsheetWorkspace != null) {
			fieldArray=SpreadSheetFieldArray.restore(ws.spreadsheetWorkspace, getName(), context);
		}
		if (ws.printSettings!=null){
			printSettings=ws.printSettings;
			if (printSettings!=null){
				printSettings.init();
				tmpSettings=(PrintSettings)printSettings.clone();
			}

		}

		if (ws.fieldAliasMap != null)
			FieldDictionary.setAliasMap(ws.fieldAliasMap);
		if (ws.calendarOption != null) {
			calendarOption = ws.calendarOption;
			CalendarOption.setInstance(calendarOption);
		}

		//	Alert.setGraphicManagerMethod("setCurrentFieldArray",f);
	}
	public WorkspaceSetting createWorkspace(int context) {
		Workspace ws = new Workspace();
		if (Environment.isClientSide()){
			fieldArray = (SpreadSheetFieldArray) Alert.getGraphicManagerMethod("getCurrentFieldArray");
			if (fieldArray != null)
				ws.spreadsheetWorkspace = fieldArray.createWorkspace(context);
			if (printSettings!=null){
				ws.printSettings=printSettings;
				printSettings.updateWorkspace();
			}
			if (calendarOption != null)
				ws.calendarOption = calendarOption;
		}
		ws.fieldAliasMap = FieldDictionary.getAliasMap();
		return ws;
	}

	public SpreadSheetFieldArray getFieldArray() {
		return fieldArray;
	}

	public void forTasks(Closure c){
		for (Iterator i=getTaskOutlineIterator();i.hasNext();){
			c.execute(i.next());
		}
	}

	private class TaskIterator implements Iterator<Task>{
		private Iterator iterator;
		private Task next=null;
		private Task nextElement(){
	        Node node=null;
	        while(iterator.hasNext() && !((node=(Node)iterator.next()).getImpl() instanceof Task));
	        if (node!=null && node.getImpl() instanceof Task) next=(Task)node.getImpl();
	        else next=null;
	        return next;
		}

		TaskIterator(){
			iterator=getTaskOutline().iterator(getTaskOutlineRoot());
			nextElement();
		}
		public boolean hasNext() {
			return next!=null;
		}
		public Task next() {
			Task n=next;
			nextElement();
			return n;
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	public Iterator<Task> getTaskOutlineIterator(){
		return new TaskIterator();
	}

	public Node getTaskOutlineRoot(){
    	if (isOpenedAsSubproject()) { // when doing subprojects, we must treat the suproject parent as the root node
    		if (getTasks().size() > 0) {
	    		NormalTask t = (NormalTask) getTasks().get(0); // get any task in the project
	   			return t.getEnclosingSubprojectNode(); // this will fetch the enclosing subproject task which will be the hierarchy root
    		}
    	}
		return null;
	}

	private transient TreeMap<DistributionData,DistributionData> distributionMap;
	private transient TreeMap<DistributionData,DistributionData> newDistributionMap;
	public TreeMap<DistributionData,DistributionData> getDistributionMap() {
		return distributionMap;
	}

	public void setDistributionMap(TreeMap<DistributionData,DistributionData> distributionMap) {
		this.distributionMap = distributionMap;
	}

	public TreeMap<DistributionData, DistributionData> getNewDistributionMap() {
		return newDistributionMap;
	}

	public void setNewDistributionMap(TreeMap<DistributionData, DistributionData> newDistributionMap) {
		this.newDistributionMap = newDistributionMap;
	}

    public void updateDistributionMap(){
    	long t=System.currentTimeMillis();
    	List dist=(new DistributionConverter()).createDistributionData(this,false);
    	if (dist==null) return;
    	TreeMap<DistributionData, DistributionData> distMap=new TreeMap<DistributionData, DistributionData>(new DistributionComparator());
    	setDistributionMap(distMap);
    	long projectId=getUniqueId();
	    for (Iterator i=dist.iterator();i.hasNext();){
	    	DistributionData d=(DistributionData)i.next();
	    	if (d.getProjectId()==projectId) distMap.put(d,d);
	    }
	    System.out.println("DistributionMap: "+dist.size()+" elements, updated in "+(System.currentTimeMillis()-t)+" ms");
    }
    public void validateNewDistributionMap(){
    	if (distributionMap!=null) distributionMap.clear();
    	setDistributionMap(getNewDistributionMap());
    	setNewDistributionMap(null);
    	setForceNonIncrementalDistributions(false);
    }

    protected transient boolean forceNonIncremental;

	public boolean isForceNonIncremental() {
		return forceNonIncremental;
	}

	public void setForceNonIncremental(boolean forceNonIncremental) {
		this.forceNonIncremental = forceNonIncremental;
	}

	protected transient boolean forceNonIncrementalDistributions;

	public boolean isForceNonIncrementalDistributions() {
		return forceNonIncrementalDistributions;
	}

	public void setForceNonIncrementalDistributions(
			boolean forceNonIncrementalDistributions) {
		this.forceNonIncrementalDistributions = forceNonIncrementalDistributions;
	}

	public static final float CURRENT_VERSION=1.2f;
	protected float version=CURRENT_VERSION;

	public float getVersion() {
		return version;
	}

	public void setVersion(float version) {
		this.version = version;
	}

	public void recalculate() {
		markAllTasksAsNeedingRecalculation(true);
		schedulingAlgorithm.reset();
		schedulingAlgorithm.calculate(true);
	}

	public ObjectSelectionEventManager getObjectSelectionEventManager() {
		return objectSelectionEventManager;
	}

	public int getRowHeight(SortedSet baseLines){
        for (Iterator i=getTaskOutlineIterator();i.hasNext();){
            Task task=(Task)i.next();
            int current=Snapshottable.CURRENT.intValue();
            for (int s=0;s<Settings.numGanttBaselines();s++){
                if (s==current) continue;
                TaskSnapshot snapshot=(TaskSnapshot)task.getSnapshot(new Integer(s));
                if (snapshot!=null) baseLines.add(new Integer(s));
            }
        }
		int num=(baseLines.size()==0)?0:(((Integer)baseLines.last()).intValue()+1);
		int rowHeight=GraphicConfiguration.getInstance().getRowHeight()
				+num*GraphicConfiguration.getInstance().getBaselineHeight();
		return rowHeight;
	}


}