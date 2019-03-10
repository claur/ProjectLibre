/*******************************************************************************
 * The contents of this file are subject to the Common Public Attribution License 
 * Version 1.0 (the "License"); you may not use this file except in compliance with 
 * the License. You may obtain a copy of the License at 
 * http://www.projectlibre.com/license . The License is based on the Mozilla Public 
 * License Version 1.1 but Sections 14 and 15 have been added to cover use of 
 * software over a computer network and provide for limited attribution for the 
 * Original Developer. In addition, Exhibit A has been modified to be consistent 
 * with Exhibit B. 
 *
 * Software distributed under the License is distributed on an "AS IS" basis, 
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the 
 * specific language governing rights and limitations under the License. The 
 * Original Code is ProjectLibre. The Original Developer is the Initial Developer 
 * and is ProjectLibre Inc. All portions of the code written by ProjectLibre are 
 * Copyright (c) 2012-2019. All Rights Reserved. All portions of the code written by 
 * ProjectLibre are Copyright (c) 2012-2019. All Rights Reserved. Contributor 
 * ProjectLibre, Inc.
 *
 * Alternatively, the contents of this file may be used under the terms of the 
 * ProjectLibre End-User License Agreement (the ProjectLibre License) in which case 
 * the provisions of the ProjectLibre License are applicable instead of those above. 
 * If you wish to allow use of your version of this file only under the terms of the 
 * ProjectLibre License and not to allow others to use your version of this file 
 * under the CPAL, indicate your decision by deleting the provisions above and 
 * replace them with the notice and other provisions required by the ProjectLibre 
 * License. If you do not delete the provisions above, a recipient may use your 
 * version of this file under either the CPAL or the ProjectLibre Licenses. 
 *
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text of the notices 
 * in the Source Code files of the Original Code. You should use the text of this 
 * Exhibit A rather than the text found in the Original Code Source Code for Your 
 * Modifications.] 
 *
 * EXHIBIT B. Attribution Information for ProjectLibre required
 *
 * Attribution Copyright Notice: Copyright (c) 2012-2019, ProjectLibre, Inc.
 * Attribution Phrase (not exceeding 10 words): 
 * ProjectLibre, open source project management software.
 * Attribution URL: http://www.projectlibre.com
 * Graphic Image as provided in the Covered Code as file: projectlibre-logo.png with 
 * alternatives listed on http://www.projectlibre.com/logo 
 *
 * Display of Attribution Information is required in Larger Works which are defined 
 * in the CPAL as a work which combines Covered Code or portions thereof with code 
 * not governed by the terms of the CPAL. However, in addition to the other notice 
 * obligations, all copies of the Covered Code in Executable and Source Code form 
 * distributed must, as a form of attribution of the original author, include on 
 * each user interface screen the "ProjectLibre" logo visible to all users. 
 * The ProjectLibre logo should be located horizontally aligned with the menu bar 
 * and left justified on the top left of the screen adjacent to the File menu. The 
 * logo must be at least 144 x 31 pixels. When users click on the "ProjectLibre" 
 * logo it must direct them back to http://www.projectlibre.com. 
 *******************************************************************************/
package com.projectlibre1.pm.assignment.timesheet;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.projectlibre1.datatype.Rate;
import com.projectlibre1.field.CanBeDirty;
import com.projectlibre1.field.FieldContext;
import com.projectlibre1.functor.IntervalConsumer;
import com.projectlibre1.graphic.configuration.HasCssStyle;
import com.projectlibre1.pm.assignment.Assignment;
import com.projectlibre1.pm.assignment.AssignmentSpecificFields;
import com.projectlibre1.pm.assignment.TimeDistributedFields;
import com.projectlibre1.pm.calendar.WorkCalendar;
import com.projectlibre1.pm.scheduling.Schedule;
import com.projectlibre1.pm.scheduling.ScheduleInterval;
import com.projectlibre1.pm.task.HasNotes;
import com.projectlibre1.pm.task.Project;
import com.projectlibre1.util.DateTime;

public class TimesheetAssignment implements Schedule, AssignmentSpecificFields, TimeDistributedFields, UpdatesFromTimesheet, HasCssStyle, CanBeDirty, Comparable, HasNotes {
	Assignment assignment;
	String taskName;
	String projectName;
	long projectUniqueId;
	long taskUniqueId;
	long resourceUniqueId;
	boolean alreadyTimesheet = false;
	Collection parentsNames = null;
	transient boolean dirty = false;
	
	transient String description;  //used when sending data to partner
	private String notes;
	public final boolean isAlreadyTimesheet() {
		return alreadyTimesheet;
	}

	public final void setAlreadyTimesheet(boolean alreadyTimesheet) {
		this.alreadyTimesheet = alreadyTimesheet;
	}

	public final long getResourceUniqueId() {
		return resourceUniqueId;
	}

	public final long getTaskUniqueId() {
		return taskUniqueId;
	}

	public final Assignment getAssignment() {
		return assignment;
	}

	public final void setAssignment(Assignment assignment) {
		this.assignment = assignment;
	}

	public TimesheetAssignment(String projectName, String taskName, long projectUniqueId, long taskUniqueId, long resourceUniqueId,Assignment assignment, String notes) {
		this.projectName = projectName;
		this.taskName = taskName;
		this.projectUniqueId = projectUniqueId;
		this.taskUniqueId = taskUniqueId;
		this.resourceUniqueId = resourceUniqueId;
		this.assignment = assignment;
		this.notes = notes;
	}
	
	public long getDuration() {
		return assignment.getDuration();
	}
	public long getEnd() {
		return assignment.getEnd();
	}
	public long getStart() {
		return assignment.getStart();
	}
	public long getActualDuration() {
		return assignment.getActualDuration();
	}
	public long getActualFinish() {
		return assignment.getActualFinish();
	}
	public long getActualStart() {
		return assignment.getActualStart();
	}
	public double getPercentComplete() {
		return assignment.getPercentComplete();
	}
	public long getRemainingDuration() {
		return assignment.getRemainingDuration();
	}
	public long getResume() {
		return assignment.getResume();
	}
	public long getStop() {
		return assignment.getStop();
	}
	public final String getProjectName() {
		return projectName;
	}
	public final void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public final String getTaskName() {
		return taskName;
	}
	public final void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public TimesheetAssignment(Assignment assignment) {
		this.assignment = assignment;
	}
	public int getWorkContourType() {
		return assignment.getWorkContourType();
	}
	public void setWorkContourType(int workContourType) {
	}
	public long getResourceAvailability() {
		return assignment.getResourceAvailability();
	}
	public String getTaskId() {
		return taskUniqueId+"";
	}
	public String getResourceName() {
		return null;
	}
	public String getResourceId() {
		return resourceUniqueId+"";
	}
	public Rate getRate() {
		return assignment.getRate();
	}
	public void setRate(Rate rate) {
		assignment.setRate(rate);
	}
	
	
	public double getCost(FieldContext fieldContext) {
		return assignment.getCost();
	}
	public long getWork(FieldContext fieldContext) {
		return assignment.getWork(fieldContext);
	}
	public void setWork(long work, FieldContext fieldContext) {
		assignment.setWork(work,fieldContext);
	}
	public boolean isReadOnlyWork(FieldContext fieldContext) {
		return false;
	}
	public double getActualCost(FieldContext fieldContext) {
		return assignment.getActualCost(fieldContext);
	}
	public long getActualWork(FieldContext fieldContext) {
		return assignment.getActualWork(fieldContext);
	}
	public void setActualWork(long actualWork, FieldContext fieldContext) {
		assignment.setActualWork(actualWork,fieldContext);

	}
	public boolean isReadOnlyActualWork(FieldContext fieldContext) {
		return false;
	}
	public long getRemainingWork(FieldContext fieldContext) {
		return assignment.getRemainingWork(fieldContext);
	}
	public void setRemainingWork(long remainingWork, FieldContext fieldContext) {
		assignment.setRemainingWork(remainingWork,fieldContext);
	}
	public boolean isReadOnlyRemainingWork(FieldContext fieldContext) {
		return false;
	}
	public double getBaselineCost(int numBaseline, FieldContext fieldContext) {
		return assignment.getBaselineCost(numBaseline,fieldContext);
	}
	public long getBaselineWork(int numBaseline, FieldContext fieldContext) {
		return assignment.getBaselineWork(numBaseline,fieldContext);
	}
	public boolean fieldHideCost(FieldContext fieldContext) {
		return false;
	}
	public boolean fieldHideWork(FieldContext fieldContext) {
		return false;
	}
	public boolean fieldHideBaselineCost(int numBaseline, FieldContext fieldContext) {
		return false;
	}
	public boolean fieldHideBaselineWork(int numBaseline, FieldContext fieldContext) {
		return false;
	}
	public boolean fieldHideActualCost(FieldContext fieldContext) {
		return false;
	}
	public boolean fieldHideActualWork(FieldContext fieldContext) {
		return false;
	}
	public double getFixedCost(FieldContext fieldContext) {
		return assignment.getFixedCost(fieldContext);
	}
	public void setFixedCost(double fixedCost, FieldContext fieldContext) {
		assignment.setFixedCost(fixedCost,fieldContext);
	}
	public boolean isReadOnlyFixedCost(FieldContext fieldContext) {
		return false;
	}
	public double getActualFixedCost(FieldContext fieldContext) {
		return assignment.getActualFixedCost(fieldContext);
	}
	public boolean fieldHideActualFixedCost(FieldContext fieldContext) {
		return false;
	}
	public double getRemainingCost(FieldContext fieldContext) {
		return assignment.getRemainingCost(fieldContext);
	}
	public void setActualStart(long actualStart) {
		assignment.setActualStart(actualStart);
		
	}
	public void setActualFinish(long actualFinish) {
		assignment.setActualFinish(actualFinish);
	}
	public void setActualDuration(long actualDuration) {
		assignment.setActualDuration(actualDuration);
	}
	public void setRemainingDuration(long remainingDuration) {
		assignment.setRemainingDuration(remainingDuration);
	}
	public void setPercentComplete(double percentComplete) {
		assignment.setPercentComplete(percentComplete);
	}
	public void setDuration(long duration) {
		assignment.setDuration(duration);
	}
	public long getElapsedDuration() {
		return assignment.getElapsedDuration();
	}
	public long getDependencyStart() {
		return assignment.getDependencyStart();
	}
	public void setDependencyStart(long dependencyStart) {
		assignment.setDependencyStart(dependencyStart);
	}
	public void setResume(long resume) {
		assignment.setResume(resume);
	}
	public void setStop(long stop) {
		assignment.setStop(stop);
	}
	public void clearDuration() {
		assignment.clearDuration();
	}
	public void moveRemainingToDate(long date) {
		assignment.moveRemainingToDate(date);
	}
	public void moveInterval(Object eventSource, long start, long end, ScheduleInterval oldInterval, boolean isChild) {
		assignment.moveInterval(eventSource,start,end,oldInterval,isChild);
	}
	public void consumeIntervals(IntervalConsumer consumer) {
		assignment.consumeIntervals(consumer);
	}
	public WorkCalendar getEffectiveWorkCalendar() {
		return assignment.getEffectiveWorkCalendar();
	}
	public void split(Object eventSource, long from, long to) {
		assignment.split(eventSource,from,to);
	}
	public boolean isJustModified() {
		return assignment.isJustModified();
	}
	public void setStart(long start) {
		assignment.setStart(start);
	}
	public void setEnd(long end) {
		assignment.setEnd(end);
	}
	public boolean isComplete() {
		return assignment.isComplete();
	}
	public void setComplete(boolean complete) {
		assignment.setComplete(complete);
	}

	public Project getProject() {
		return assignment.getOwningProject();
	}

	public long getTimesheetFinish() {
		return assignment.getTimesheetFinish();
	}

	public long getTimesheetStart() {
		return assignment.getTimesheetStart();
	}

	public long getLastTimesheetUpdate() {
		return assignment.getLastTimesheetUpdate();
	}

	public boolean applyTimesheet(Collection fieldArray, long timesheetUpdateDate) {
		return false;
	}

	public boolean isPendingTimesheetUpdate() {
		return assignment.isPendingTimesheetUpdate();
	}

	public int getTimesheetStatus() {
		return assignment.getTimesheetStatus();
	}

	public String getTimesheetStatusName() {
		return assignment.getTimesheetStatusName();
	}

	public String getCssStyleClass() {
		return getTimesheetStatusName();
	}

	public void setHierarchy(Collection parentsNames) {
		this.parentsNames = parentsNames;
	}

	public Collection getHierarchy() {
		return parentsNames;
	}
	public boolean isIntegratedOrComplete() {
		if (getTimesheetStatus() == TimesheetStatus.INTEGRATED)
			return true;
		if (getTimesheetStatus() == TimesheetStatus.VALIDATED) // validated timesheets must always be shown in dialog till integrated
			return false;
		return isComplete();
	}

	public final boolean isDirty() {
		return dirty;
	}

	public final void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public long getCachedStart() {
		Date d = assignment.getCachedStart();
		if (d == null)
			return 0;
		d =  DateTime.fromGmt(d);
		return d.getTime();
	}

	public long getCachedEnd() {
		Date d = assignment.getCachedEnd();
		if (d == null)
			return 0;
		return DateTime.gmt(d);
	//	return d.getTime();
	}
	
	public int compareTo(Object arg0) {
		return com.projectlibre1.util.MathUtils.signum(getCachedStart() - ((TimesheetAssignment)arg0).getCachedStart());
	}
	
	public long getReadOnlyDuration() {
		return getDuration();
	}

	public final long getEarliestStop() {
		return assignment.getEarliestStop();
	}
	public final long getCompletedThrough() {
		return assignment.getCompletedThrough();
	}

	public void setCompletedThrough(long completedThrough) {
		assignment.setCompletedThrough(completedThrough);
	}

	public final String getDescription() {
		return description;
	}

	public final void setDescription(String description) {
		this.description = description;
	}
	public String toExternalId() { 
		return taskUniqueId + "/" + resourceUniqueId;
	}

	public Object backupDetail() {
		// TODO Auto-generated method stub
		return null;
	}

	public void restoreDetail(Object source,Object detail,boolean isChild) {
		// TODO Auto-generated method stub
		
	}

	public long getProjectUniqueId() {
		return projectUniqueId;
	}

	public void setProjectUniqueId(long projectUniqueId) {
		this.projectUniqueId = projectUniqueId;
	}
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes=notes;
	}
	
	

}
