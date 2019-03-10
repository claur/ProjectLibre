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
 * Copyright (c) 2012. All Rights Reserved. All portions of the code written by 
 * ProjectLibre are Copyright (c) 2012. All Rights Reserved. Contributor 
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
 * Attribution Copyright Notice: Copyright (c) 2012, ProjectLibre, Inc.
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
package com.projectlibre1.server.data;

import java.util.Date;
import java.util.Map;

import com.projectlibre1.pm.assignment.timesheet.AssignmentWorkflowState;
import com.projectlibre1.pm.assignment.timesheet.TimesheetStatus;
import com.projectlibre1.pm.snapshot.Snapshottable;

/**
 *
 */
public class AssignmentData extends SerializedDataObject {
	static final long serialVersionUID = 798773653651L;

    protected TaskData task;
    protected EnterpriseResourceData resource;
    protected int snapshotId=Snapshottable.CURRENT.intValue();
    protected long taskId=-1L;

    protected Date cachedStart = null;
    protected Date cachedEnd = null;
    protected int timesheetStatus = TimesheetStatus.NO_DATA;
    protected Date lastTimesheetUpdate = null;
    protected int workflowState = AssignmentWorkflowState.UNDEFINED;
    protected double percentComplete;
    protected long duration;
    protected transient Map attributes;

    public static final SerializedDataObjectFactory FACTORY=new SerializedDataObjectFactory(){
        public SerializedDataObject createSerializedDataObject(){
            return new AssignmentData();
        }
    };

    public EnterpriseResourceData getResource() {
        return resource;
    }
    public void setResource(EnterpriseResourceData resource) {
        this.resource = resource;
        setResourceId((resource==null)?-1L:resource.getUniqueId());
    }
    public TaskData getTask() {
        return task;
    }
    public void setTask(TaskData task) {
        this.task = task;
        setTaskId(task.getUniqueId());
    }




    public long getTaskId() {
		return taskId;
	}
	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}
    public long getResourceId() {
		return getUniqueId();
	}
	public void setResourceId(long resourceId) {
		setUniqueId(resourceId);
	}

	public int getSnapshotId() {
        return snapshotId;
    }
    public void setSnapshotId(int snapshotId) {
        this.snapshotId = snapshotId;
    }

    public int getType(){
        return DataObjectConstants.ASSIGNMENT_TYPE;
    }

    public void emtpy(){
    	super.emtpy();
    	resource=null;
    	task=null;
    	cachedStart = null;
    	cachedEnd = null;
    	timesheetStatus = TimesheetStatus.NO_DATA;
    	lastTimesheetUpdate = null;
    	workflowState = AssignmentWorkflowState.UNDEFINED;
    }


	public boolean equals(Object obj){
		if (!super.equals(obj)) return false;
		if (obj instanceof AssignmentData){
			AssignmentData data=(AssignmentData)obj;
			return data.getTaskId()==getTaskId()&&data.getSnapshotId()==data.getSnapshotId();
		}else return false;
	}
	public final Date getCachedStart() {
		return cachedStart;
	}
	public final void setCachedStart(Date start) {
		this.cachedStart = start;
	}
	public final Date getCachedEnd() {
		return cachedEnd;
	}
	public final void setCachedEnd(Date end) {
		this.cachedEnd = end;
	}
	public final Date getLastTimesheetUpdate() {
		return lastTimesheetUpdate;
	}
	public final void setLastTimesheetUpdate(Date lastTimesheetUpdate) {
		this.lastTimesheetUpdate = lastTimesheetUpdate;
	}
	public final int getTimesheetStatus() {
		return timesheetStatus;
	}
	public final void setTimesheetStatus(int timesheetStatus) {
		this.timesheetStatus = timesheetStatus;
	}
	public final int getWorkflowState() {
		return workflowState;
	}
	public final void setWorkflowState(int workflowState) {
		this.workflowState = workflowState;
	}

	public final boolean isNoDuration() {
		return cachedEnd.equals(cachedStart);
	}

    public double getPercentComplete() {
		return percentComplete;
	}
	public void setPercentComplete(double percentComplete) {
		this.percentComplete = percentComplete;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public Map getAttributes() {
		return attributes;
	}
	public void setAttributes(Map attributes) {
		this.attributes = attributes;
	}
	public void renumber(IDGenerator idGenerator){
    	super.renumber(idGenerator);
    	setTaskId(idGenerator.getId(getTaskId()));
    }

}
