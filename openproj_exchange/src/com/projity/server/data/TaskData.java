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
package com.projity.server.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 */
public class TaskData extends SerializedDataObject {
	static final long serialVersionUID = 37382828283746L;
	public static final int CREATION_STATUS_NORMAL = 0;
	public static final int CREATION_STATUS_TIMESHEET = 1;

	protected CalendarData calendar;
    protected Collection assignments;
    protected Collection predecessors;
    protected TaskData parentTask;
    protected long childPosition;
    protected long parentTaskId=-1L;
    protected long calendarId=-1;
    protected boolean external = false;
    protected long projectId = 0L;
    protected long subprojectId = 0L; // for subproject tasks, what project they represent
    protected Map subprojectFieldValues = null;
    protected boolean timesheetCreated = false;
    protected String notes;
    protected transient Map attributes;

// this code is to set fields which are exposed in database
//    protected long start;
//	  protected long finish;
//    protected long baselineStart;
//    protected long baselineFinish;
//    protected long completedThrough;
//    protected double percentComplete;



    public static final SerializedDataObjectFactory FACTORY=new SerializedDataObjectFactory(){
        public SerializedDataObject createSerializedDataObject(){
            return new TaskData();
        }
    };

    public Collection getAssignments() {
        return assignments;
    }
    public void setAssignments(Collection assignments) {
        this.assignments = assignments;
    }
    public CalendarData getCalendar() {
        return calendar;
    }
    public void setCalendar(CalendarData calendar) {
        this.calendar = calendar;
        setCalendarId((calendar==null)?-1L:calendar.getUniqueId());
    }

    /*public Collection getPredecessors() {
        return predecessors;
    }
    public void setPredecessors(Collection predecessors) {
        this.predecessors = predecessors;
    }*/
    public Collection getPredecessors() {
        return predecessors;
    }
    public void setPredecessors(Collection predecessors) {
        this.predecessors = predecessors;
    }
	/**
	 * @return Returns the childPosition.
	 */
	public long getChildPosition() {
		return childPosition;
	}
	/**
	 * @param childPosition The childPosition to set.
	 */
	public void setChildPosition(long childPosition) {
		this.childPosition = childPosition;
	}
	/**
	 * @return Returns the parentTask.
	 */
	public TaskData getParentTask() {
		return parentTask;
	}
	/**
	 * @param parentTask The parentTask to set.
	 */
	public void setParentTask(TaskData parentTask) {
		this.parentTask = parentTask;
        setParentTaskId((parentTask==null)?-1L:parentTask.getUniqueId());
	}


    public long getParentTaskId() {
		return parentTaskId;
	}
	public void setParentTaskId(long parentTaskId) {
		this.parentTaskId = parentTaskId;
	}
	public int getType(){
        return DataObjectConstants.TASK_TYPE;
    }
    public long getCalendarId() {
		return calendarId;
	}
	public void setCalendarId(long calendarId) {
		this.calendarId = calendarId;
	}

	public void emtpy(){
    	super.emtpy();
    	calendar=null;
    	assignments=null;
    	predecessors=null;
    	parentTask=null;
    	external = false;
    }

	//syncronizer
	public void addAssignment(AssignmentData assignmentData){
		if (assignments==null) assignments=new ArrayList();
		assignmentData.setTask(this);
		assignments.add(assignmentData);
	}
	public void addPredecessor(LinkData linkData){
		if (predecessors==null) predecessors=new ArrayList();
		linkData.setSuccessor(this);
		predecessors.add(linkData);
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
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	public final boolean isSubproject() {
		return subprojectId != 0;
	}
	public final void setSubprojectId(long subprojectId) {
		this.subprojectId = subprojectId;
	}
	public final long getSubprojectId() {
		return subprojectId;
	}
	public final Map getSubprojectFieldValues() {
		return subprojectFieldValues;
	}
	public final void setSubprojectFieldValues(Map subprojectFieldValues) {
		this.subprojectFieldValues = subprojectFieldValues;
	}
	public boolean isTimesheetCreated() {
		return timesheetCreated;
	}
	public void setTimesheetCreated(boolean timesheetCreated) {
		this.timesheetCreated = timesheetCreated;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public Map getAttributes() {
		return attributes;
	}
	public void setAttributes(Map attributes) {
		this.attributes = attributes;
	}

// this code is to set fields which are exposed in database
//    public long getStart() {
//		return start;
//	}
//	public void setStart(long start) {
//		this.start = start;
//	}
//	public long getFinish() {
//		return finish;
//	}
//	public void setFinish(long finish) {
//		this.finish = finish;
//	}
//	public long getBaselineStart() {
//		return baselineStart;
//	}
//	public void setBaselineStart(long baselineStart) {
//		this.baselineStart = baselineStart;
//	}
//	public long getBaselineFinish() {
//		return baselineFinish;
//	}
//	public void setBaselineFinish(long baselineFinish) {
//		this.baselineFinish = baselineFinish;
//	}
//	public long getCompletedThrough() {
//		return completedThrough;
//	}
//	public void setCompletedThrough(long completedThrough) {
//		this.completedThrough = completedThrough;
//	}
//	public double getPercentComplete() {
//		return percentComplete;
//	}
//	public void setPercentComplete(double percentComplete) {
//		this.percentComplete = percentComplete;
//	}

}
