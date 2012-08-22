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

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.projity.company.ApplicationUser;
import com.projity.configuration.Configuration;
import com.projity.datatype.ImageLink;
import com.projity.field.DelegatesFields;
import com.projity.field.Field;
import com.projity.field.FieldContext;
import com.projity.pm.costing.EarnedValueCalculator;
import com.projity.pm.costing.EarnedValueIndicatorFields;
import com.projity.pm.key.HasName;
import com.projity.session.SessionFactory;

/**
 *
 */
public class ProjectData extends DocumentData implements HasName,DelegatesFields,EarnedValueIndicatorFields,Comparable {
	static final long serialVersionUID = 722537477839L;
	//web
	public static final long GANTT=1L;
	public static final long NETWORK=16L;
	public static final long SVG=32L;
	public static final long PNG=64L;
	public static final long PDF=128L;
	//database
	public static final long GANTT_SVG=GANTT|SVG;
	public static final long GANTT_PDF=GANTT|PDF;
	public static final long GANTT_PNG=GANTT|PNG;
	public static final long NETWORK_SVG=NETWORK|SVG;
	public static final long NETWORK_PDF=NETWORK|PDF;
	public static final long NETWORK_PNG=NETWORK|PNG;


    protected CalendarData calendar;
    protected Collection resources;
    protected Collection tasks;
    protected long calendarId=-1;
    protected long lockedById;
    protected String lockedByName;
    protected long idleTime,allowedIdleTime;
    protected Date creationDate,lastModificationDate;
    protected Collection distributions;
    protected Map fieldValues;
    protected Map extraFields;
    protected Collection referringSubprojectTasks;
    protected long availableImages=GANTT_SVG|GANTT_PNG|NETWORK_SVG|NETWORK_PNG;
    protected String group;
    protected String division;
    protected int expenseType;
    protected int projectType;
    protected int projectStatus;
    protected int accessControlPolicy;
    protected float version=1.2f;
    protected long[] unchangedTasks;
    protected long[] unchangedLinks;
    protected boolean incrementalDistributions;
	//protected transient long externalId=-1L;
	protected transient Map attributes;

    public static final SerializedDataObjectFactory FACTORY=new SerializedDataObjectFactory(){
        public SerializedDataObject createSerializedDataObject(){
            return new ProjectData();
        }
    };

    public CalendarData getCalendar() {
        return calendar;
    }
    public void setCalendar(CalendarData calendar) {
        this.calendar = calendar;
        setCalendarId((calendar==null)?-1L:calendar.getUniqueId());
    }
    public Collection getResources() {
        return resources;
    }
    public void setResources(Collection resources) {
        this.resources = resources;
    }
    public Collection getTasks() {
        return tasks;
    }
    public void setTasks(Collection tasks) {
        this.tasks = tasks;
    }

    public int getType(){
        return DataObjectConstants.PROJECT_TYPE;
    }
	public long getCalendarId() {
		return calendarId;
	}
	public void setCalendarId(long calendarId) {
		this.calendarId = calendarId;
	}

    public String getLockedByName() {
		return lockedByName;
	}
	public void setLockedByName(String lockedByName) {
		this.lockedByName = lockedByName;
	}
    public long getLockedById() {
		return lockedById;
	}
	public void setLockedById(long lockedById) {
		this.lockedById = lockedById;
	}
	public long getIdleTime() {
		return idleTime;
	}
	public void setIdleTime(long idleTime) {
		this.idleTime = idleTime;
	}
	public long getAllowedIdleTime() {
		return allowedIdleTime;
	}
	public void setAllowedIdleTime(long allowedIdleTime) {
		this.allowedIdleTime = allowedIdleTime;
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
	public Collection getDistributions() {
		return distributions;
	}
	public void setDistributions(Collection distributions) {
		this.distributions = distributions;
	}
	public void emtpy(){
    	super.emtpy();
    	calendar=null;
    	resources=null;
    	tasks=null;
    }

    public boolean canBeUsed(){
    	if (lockedById<=0) return true;
    	ApplicationUser user=SessionFactory.getInstance().getSession(false).getUser();
    	if (user==null/*for offline gantt*/||lockedById==user.getUniqueId()) return true;
    	return (idleTime>allowedIdleTime);
    }
    public boolean isLocked() {
    	return lockedByName != null && lockedByName.length() > 0;
    }

    public String getLockerInfo(){
		String lockerName=getLockedByName();
		if (lockerName==null) return null;
		if (getIdleTime()>allowedIdleTime)
			lockerName+="("+"Idle: "+(getIdleTime()/60000)+"min)";
		return lockerName;

    }
	public final Map getFieldValues() {
		return fieldValues;
	}
	public final void setFieldValues(Map fieldValues) {
		this.fieldValues = fieldValues;
	}
	public final Map getExtraFields() {
		return extraFields;
	}
	public final void setExtraFields(Map extraFields) {
		this.extraFields = extraFields;
	}
	public final Collection getReferringSubprojectTasks() {
		return referringSubprojectTasks;
	}
	public final void setReferringSubprojectTasks(Collection referringSubprojectTasks) {
		this.referringSubprojectTasks = referringSubprojectTasks;
	}

	public long getAvailableImages() {
		return availableImages;
	}
	public void setAvailableImages(long availableImages) {
		this.availableImages = availableImages;
	}


	public Object getDelegatedFieldValue(Field field) {
		if (fieldValues == null)
			return null;
		return fieldValues.get(field.getId());
	}
	public boolean delegates(Field field) {
		if (field == getGanttSnapshotField()
			|| field == getNetworkSnapshotField()
			|| field.getId().equals("Field.creationDate")
			|| field.getId().equals("Field.lastModificationDate")
			|| field.getId().equals("Field.lockedByName")
			|| field.getId().equals("Field.locked")
			|| field.getId().equals("Field.name")
			|| field.getId().equals("Field.scheduleStatusIndicator")
			|| field.getId().equals("Field.statusIndicator")
			|| field.getId().equals("Field.budgetStatusIndicator"))


			return false;
		return true;
	}


	private static Field ganttSnapshotFieldInstance = null;
	public static Field getGanttSnapshotField() {
		if (ganttSnapshotFieldInstance == null)
			ganttSnapshotFieldInstance = Configuration.getFieldFromId("Field.ganttSnapshot");
		return ganttSnapshotFieldInstance;
	}
	public ImageLink getGanttSnapshot() {
		return new ImageLink("Gantt Snapshot"
				,"gantt"
				,((availableImages&GANTT_SVG)==GANTT_SVG)?"/img/littleGantt.jpg":""
//				,"http://www.projity.com/web/img/littleGantt.jpg"
				,"application.icon"
				,""+getUniqueId(),true);

	}

	private static Field networkSnapshotFieldInstance = null;
	public static Field getNetworkSnapshotField() {
		if (networkSnapshotFieldInstance == null)
			networkSnapshotFieldInstance = Configuration.getFieldFromId("Field.networkSnapshot");
		return networkSnapshotFieldInstance;
	}
	public ImageLink getNetworkSnapshot() {
		return new ImageLink("Network Snapshot"
				,"network"
				,((availableImages&NETWORK_SVG)==NETWORK_SVG)?"/img/littleNetwork.png":""
//				,"http://www.projity.com/web/img/littleNetwork.png"
				,"network.icon"
				,""+getUniqueId(),true);

	}

	public ImageLink getScheduleStatusIndicator() {
		Double spi = (Double)fieldValues.get("Field.spi");
		if (spi == null)
			spi = new Double(0.0D);
		return EarnedValueCalculator.getInstance().getScheduleStatusIndicator(spi.doubleValue());
	}
	public ImageLink getBudgetStatusIndicator() {
		Double cpi = (Double)fieldValues.get("Field.cpi");
		if (cpi == null)
			cpi = new Double(0.0D);
		return EarnedValueCalculator.getInstance().getScheduleStatusIndicator(cpi.doubleValue());
	}
	public ImageLink getStatusIndicator() {


		Double csi = (Double)fieldValues.get("Field.csi");
		if (csi == null) {
			Double spi = (Double)fieldValues.get("Field.spi");
			Double cpi = (Double)fieldValues.get("Field.cpi");
			if (spi == null || cpi == null)
				csi = new Double(0.0D);
			else
				csi = new Double(spi.doubleValue() * cpi.doubleValue());
		}
		return EarnedValueCalculator.getInstance().getStatusIndicator(csi.doubleValue());
	}

	public String getDivision() {
		return division;
	}
	public void setDivision(String division) {
		this.division = division;
	}
	public int getExpenseType() {
		return expenseType;
	}
	public void setExpenseType(int expenseType) {
		this.expenseType = expenseType;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public int getProjectType() {
		return projectType;
	}
	public void setProjectType(int projectType) {
		this.projectType = projectType;
	}
	public int getProjectStatus() {
		return projectStatus;
	}
	public void setProjectStatus(int projectStatus) {
		this.projectStatus = projectStatus;
	}
	public String getName(FieldContext context) {
		return getName();
	}
	public int compareTo(Object o) {
		return getName().compareTo(((HasName)o).getName());
	}
	public int getAccessControlPolicy() {
		return accessControlPolicy;
	}
	public void setAccessControlPolicy(int accessControlPolicy) {
		this.accessControlPolicy = accessControlPolicy;
	}
	public float getVersion() {
		return version;
	}

	public void setVersion(float version) {
		this.version = version;
	}
	public long[] getUnchangedTasks() {
		return unchangedTasks;
	}
	public void setUnchangedTasks(long[] unchangedTasks) {
		this.unchangedTasks = unchangedTasks;
	}
	public long[] getUnchangedLinks() {
		return unchangedLinks;
	}
	public void setUnchangedLinks(long[] unchangedLinks) {
		this.unchangedLinks = unchangedLinks;
	}
	public boolean isIncrementalDistributions() {
		return incrementalDistributions;
	}
	public void setIncrementalDistributions(boolean incrementalDistributions) {
		this.incrementalDistributions = incrementalDistributions;
	}

//	public long getExternalId() {
//		return externalId;
//	}
//
//	public void setExternalId(long externalId) {
//		this.externalId = externalId;
//	}
	public Map getAttributes() {
		return attributes;
	}
	public void setAttributes(Map attributes) {
		this.attributes = attributes;
	}


}