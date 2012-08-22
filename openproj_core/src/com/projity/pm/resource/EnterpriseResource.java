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
package com.projity.pm.resource;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.Closure;

import com.projity.algorithm.ReverseQuery;
import com.projity.algorithm.TimeIteratorGenerator;
import com.projity.algorithm.buffer.CalculatedValues;
import com.projity.algorithm.buffer.GroupedCalculatedValues;
import com.projity.association.AssociationList;
import com.projity.company.ApplicationUser;
import com.projity.configuration.CircularDependencyException;
import com.projity.datatype.ImageLink;
import com.projity.datatype.Rate;
import com.projity.datatype.RateFormat;
import com.projity.datatype.TimeUnit;
import com.projity.document.Document;
import com.projity.field.CustomFields;
import com.projity.field.CustomFieldsImpl;
import com.projity.field.FieldContext;
import com.projity.interval.InvalidValueObjectForIntervalException;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.HasAssignments;
import com.projity.pm.assignment.HasAssignmentsImpl;
import com.projity.pm.assignment.timesheet.TimesheetHelper;
import com.projity.pm.availability.AvailabilityTable;
import com.projity.pm.calendar.CalendarService;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.costing.Accrual;
import com.projity.pm.costing.CostRateTable;
import com.projity.pm.costing.CostRateTables;
import com.projity.pm.costing.EarnedValueCalculator;
import com.projity.pm.key.HasKeyImpl;
import com.projity.pm.task.Task;
import com.projity.strings.Messages;
import com.projity.util.Environment;

/**
 * A global resource that belongs to the enterprise resource pool
 */
public class EnterpriseResource implements Resource {
	static final long serialVersionUID = 273977742329L;
	private static Resource UNASSIGNED = null;
	public static final int UNASSIGNED_ID = -65535; // correponds to MSDI


	public long getEarliestAssignmentStart() {
		return hasAssignments.getEarliestAssignmentStart();
	}

	public boolean hasActiveAssignment(long start, long end) {
		return hasAssignments.hasActiveAssignment(start, end);
	}

	public EnterpriseResource(ResourcePool resourcePool) {
		this(resourcePool==null||resourcePool.isLocal(),resourcePool);
	}
	public EnterpriseResource(boolean local,ResourcePool resourcePool) {
		hasKey = new HasKeyImpl(local,this);
		this.resourcePool = resourcePool;
		if (resourcePool != null) {
			workCalendar = WorkingCalendar.getInstanceBasedOn(resourcePool.getDefaultCalendar());
			workCalendar.setName("");
		}
	}

	/**
	 * @return
	 */
	public static Resource getUnassignedInstance() {
		if (UNASSIGNED == null) {
			UNASSIGNED = new EnterpriseResource(null); //local
			UNASSIGNED.setName(Messages.getString("Text.Unassigned"));
			UNASSIGNED.setUniqueId(UNASSIGNED_ID);
		}
		return UNASSIGNED;
	}

	public boolean isDefault(){
		return getUniqueId()==UNASSIGNED_ID;
	}




	transient HasAssignments hasAssignments = new HasAssignmentsImpl();
	private transient HasKeyImpl hasKey;
	/* (non-Javadoc)
	 * @see com.projity.pm.resource.Resource#getResourceType()
	 */
	protected transient ResourcePool resourcePool;

	protected String notes = "";
	protected String group = "";
	protected String initials = "";
	protected String phonetics = "";
	protected String rbsCode = "";
	protected String emailAddress="";
	protected String materialLabel="";
	protected String userAccount="";
	protected int resourceType = ResourceType.WORK;
	protected transient CostRateTables costRateTables = new CostRateTables();
	protected double maximumUnits = 1.0D;
	protected boolean generic = false;
	protected boolean inactive = false;
	protected transient CustomFieldsImpl customFields = new CustomFieldsImpl();
	protected long externalId=-1;

	public long getExternalId() {
		return externalId;
	}

	public void setExternalId(long externalId) {
		this.externalId = externalId;
	}

	public int getResourceType() {
		return resourceType;
	}

	public String toString() {
		return getName();
	}


	public void setResourceType(int resourceType) {
		if (resourceType == this.resourceType)
			return;
		boolean oldIsLabor = isLabor();
		this.resourceType = resourceType;

		// if resource type changes to/from labor, then initialize rates
		if (oldIsLabor != isLabor()) {
			setStandardRate(new Rate());
			setOvertimeRate(new Rate());

			if (!isLabor()) { // Non labor resources have no time unit
				getStandardRate().setTimeUnit(TimeUnit.NON_TEMPORAL);
				getOvertimeRate().setTimeUnit(TimeUnit.NON_TEMPORAL);
			}
		}
	}


	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Rate#getCostPerUse()
	 */
	public double getCostPerUse() {
		return costRateTables.getCostPerUse();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Rate#getOvertimeRate()
	 */
	public Rate getOvertimeRate() {
		return costRateTables.getOvertimeRate();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Rate#getStandardRate()
	 */
	public Rate getStandardRate() {
		return costRateTables.getStandardRate();
	}



	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#setCostPerUse(double)
	 */
	public void setCostPerUse(double costPerUse) {
		costRateTables.setCostPerUse(costPerUse);
	}



	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#setOvertimeRate(double)
	 */
	public void setOvertimeRate(Rate overtimeRate) {
		if (!isLabor())
			overtimeRate.makeUnitless();
		costRateTables.setOvertimeRate(overtimeRate);
	}



	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#setStandardRate(double)
	 */
	public void setStandardRate(Rate standardRate) {
		if (!isLabor())
			standardRate.makeUnitless();
		costRateTables.setStandardRate(standardRate);
	}


	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#getEffectiveDate()
	 */
	public long getEffectiveDate() {
		return costRateTables.getEffectiveDate();
	}
	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#setEffectiveDate(long)
	 */
	public void setEffectiveDate(long effectiveDate) throws InvalidValueObjectForIntervalException {
		costRateTables.setEffectiveDate(effectiveDate);
	}
	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#isReadOnlyEffectiveDate()
	 */
	public boolean isReadOnlyEffectiveDate(FieldContext fieldContext) {
		return costRateTables.isReadOnlyEffectiveDate(fieldContext);
	}

	/**
	 * @return Returns the costRateTable.
	 */
	public CostRateTable getCostRateTable(int costRateIndex) {
		return costRateTables.getCostRateTable(costRateIndex);
	}


	protected int accrueAt = Accrual.PRORATED;
	/* (non-Javadoc)
	 * @see com.projity.pm.resource.Resource#getAccrueAt()
	 */
	public int getAccrueAt() {
		return accrueAt;
	}



	/**
	 * @param accrueAt The accrueAt to set.
	 */
	public void setAccrueAt(int accrueAt) {
		this.accrueAt = accrueAt;
	}

	/**
	 * @param assignment
	 */
	public void addAssignment(Assignment assignment) {
		hasAssignments.addAssignment(assignment);
	}
	/*public void addDefaultAssignment() {
		hasAssignments.addAssignment(newDefaultAssignment());
	}
	private Assignment newDefaultAssignment() {
		return Assignment.getInstance(NormalTask
				.getUnassignedInstance(),this, 1.0, 0);
	}*/

	/**
	 * @param reverseQuery
	 */
	public void buildReverseQuery(ReverseQuery reverseQuery) {
		hasAssignments.buildReverseQuery(reverseQuery);
	}




	/**
	 * @param resource
	 * @return
	 */
	public Assignment findAssignment(Resource resource) {
		return hasAssignments.findAssignment(resource);
	}

	/**
	 * @param task
	 * @return
	 */
	public Assignment findAssignment(Task task) {
		return hasAssignments.findAssignment(task);
	}

	/**
	 * @return Returns the maxUnits.
	 */
	public double getMaximumUnits() {
		return maximumUnits;
	}
	/**
	 * @param maxUnits The maxUnits to set.
	 */
	public void setMaximumUnits(double maxUnits) {
		this.maximumUnits = maxUnits;
	}
	/**
	 * @return
	 */
	public AssociationList getAssignments() {
		return hasAssignments.getAssignments();
	}

	/**
	 * This is unused
	 * @return
	 */
	public int getSchedulingType() {
		return hasAssignments.getSchedulingType();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return hasAssignments.hashCode();
	}

	/**
	 * This is unused - a resource is not effort driven
	 * @return
	 */
	public boolean isEffortDriven() {
		return false;
	}

	/**
	 * @param assignment
	 */
	public void removeAssignment(Assignment assignment) {
		hasAssignments.removeAssignment(assignment);
	}

	/**
	 * @param effortDriven
	 */
	public void setEffortDriven(boolean effortDriven) {
		hasAssignments.setEffortDriven(effortDriven);
	}

	/**
	 * @param schedulingType
	 */
	public void setSchedulingType(int schedulingType) {
		hasAssignments.setSchedulingType(schedulingType);
	}

	/**
	 * @param modified
	 */
	public void updateAssignment(Assignment modified) {
		hasAssignments.updateAssignment(modified);
	}


	/**
	 * @param visitor
	 * @return
	 */
	public static Closure forAllAssignments(Closure visitor) {
		return HasAssignmentsImpl.forAllAssignments(visitor);
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
	 * @param visitor
	 * @param mergeWorking
	 */
	public void forEachWorkingInterval(Closure visitor, boolean mergeWorking, WorkCalendar workCalendar) {
		hasAssignments.forEachWorkingInterval(visitor, mergeWorking, workCalendar);
	}



	protected WorkCalendar workCalendar = null;

	public void setWorkCalendar(WorkCalendar workCalendar) {
		this.workCalendar = workCalendar;
	}



	/* (non-Javadoc)
	 * @see com.projity.pm.time.HasCalendar#getWorkCalendar()
	 */
	public WorkCalendar getWorkCalendar() {
		return workCalendar;
	}



	/* (non-Javadoc)
	 * @see com.projity.pm.time.HasCalendar#getEffectiveWorkCalendar()
	 */
	public WorkCalendar getEffectiveWorkCalendar() {
		return workCalendar; // can be null
	}



	/**
	 * @return
	 */
	public boolean isReadOnlyEffortDriven(FieldContext fieldContext) {
		return hasAssignments.isReadOnlyEffortDriven(fieldContext);
	}
	/**
	 * @return Returns the group.
	 */
	public String getGroup() {
		return group;
	}
	/**
	 * @param group The group to set.
	 */
	public void setGroup(String group) {
		this.group = group;
	}
	/**
	 * @return Returns the initials.
	 */
	public String getInitials() {
		return initials;
	}
	/**
	 * @param initials The initials to set.
	 */
	public void setInitials(String initials) {
		this.initials = initials;
		if (getName() == null) // for the case where the resource is created by entering initials, set name too
			setName(initials);
	}
	/**
	 * @return Returns the phonetics.
	 */
	public String getPhonetics() {
		return phonetics;
	}
	/**
	 * @param phonetics The phonetics to set.
	 */
	public void setPhonetics(String phonetics) {
		this.phonetics = phonetics;
	}



	/* (non-Javadoc)
	 * @see com.projity.pm.resource.ResourceSpecificFields#getRemainingOvertimeCost()
	 */
	public double getRemainingOvertimeCost() {
		// TODO implement this
		return -1;
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
		hasKey.setName(name);
		// set initials too to first character of name if initials is empty
		if (getInitials() == null || getInitials().length() == 0) {
			if (name != null && name.length() > 0)
				setInitials(name.substring(0,1));
		}
		if (workCalendar != null)
			workCalendar.setName(name);

	}
	/**
	 * @param id
	 */
	public void setUniqueId(long id) {
		hasKey.setUniqueId(id);
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public double actualCost(long start, long end) {
		return hasAssignments.actualCost(start, end);
	}
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public long actualWork(long start, long end) {
		return hasAssignments.actualWork(start, end);
	}
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public long remainingWork(long start, long end) {
		return hasAssignments.remainingWork(start, end);
	}


	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public double acwp(long start, long end) {
		return hasAssignments.acwp(start, end);
	}
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public double bac(long start, long end) {
		return hasAssignments.bac(start, end);
	}
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public double bcwp(long start, long end) {
		return hasAssignments.bcwp(start, end);
	}
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public double bcws(long start, long end) {
		return hasAssignments.bcws(start, end);
	}
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public double cost(long start, long end) {
		return hasAssignments.cost(start, end);
	}
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public long work(long start, long end) {
		return hasAssignments.work(start, end);
	}
	/**
	 * @param context
	 * @return
	 */
	public String getName(FieldContext context) {
		return hasKey.getName(context);
	}
	/**
	 * @param type
	 * @param generator
	 * @param values
	 */
	public void calcDataBetween(Object type, TimeIteratorGenerator generator,
			CalculatedValues values) {
		hasAssignments.calcDataBetween(type, generator, values);
	}
	/**
	 * @return Returns the rbsCode.
	 */
	public String getRbsCode() {
		return rbsCode;
	}
	/**
	 * @param rbsCode The rbsCode to set.
	 */
	public void setRbsCode(String rbsCode) {
		this.rbsCode = rbsCode;
	}
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
	 * @see com.projity.pm.task.BelongsToDocument#getDocument()
	 */
	public Document getDocument() {
		return resourcePool;
	}
	/**
	 * @return
	 */
	public Collection childrenToRollup() {
		return hasAssignments.childrenToRollup();
	}
	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#actualWork(long, long)
	 */
	public double getCost(FieldContext fieldContext) {
		return cost(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public long getWork(FieldContext fieldContext) {
		return work(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	public double getActualCost(FieldContext fieldContext) {
		return actualCost(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
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

	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public double baselineCost(long start, long end) {
		return hasAssignments.baselineCost(start, end);
	}
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public long baselineWork(long start, long end) {
		return hasAssignments.baselineWork(start, end);
	}
	public double getBaselineCost(int numBaseline, FieldContext fieldContext) {
		return baselineCost(FieldContext.start(fieldContext),FieldContext.end(fieldContext));	}
	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.TimeDistributedFields#getBaselineWork(int, com.projity.field.FieldContext)
	 */
	public long getBaselineWork(int numBaseline, FieldContext fieldContext) {
		return baselineWork(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	}
	private boolean isFieldHidden(FieldContext fieldContext) {
		return false;
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
	/**
	 * @param workCalendar
	 * @return
	 */
	public long calcActiveAssignmentDuration(WorkCalendar workCalendar) {
		return hasAssignments.calcActiveAssignmentDuration(workCalendar);
	}


	public boolean isAssignment() { //for filters
		return false;
	}


	/**
	 * @return Returns the emailAddress.
	 */
	public String getEmailAddress() {
		return emailAddress;
	}
	/**
	 * @param emailAddress The emailAddress to set.
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	/**
	 * @return Returns the materialLabel.
	 */
	public String getMaterialLabel() {
		return materialLabel;
	}
	/**
	 * @param materialLabel The materialLabel to set.
	 */
	public void setMaterialLabel(String materialLabel) {
		this.materialLabel = materialLabel;
	}
	public boolean isLabor() {
		return resourceType == ResourceType.WORK; // work resources are time based

	}
	public boolean isReadOnlyMaterialLabel(FieldContext fieldContext) {
		return isLabor();
	}

	public String getUserAccount() {
		return userAccount;
	}
	public final void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}

	/**
	 * @return Returns the active.
	 */
	public boolean isInactive() {
		return inactive;
	}
	/**
	 * @param inactive The active to set.
	 */
	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}
	/**
	 * @return Returns the generic.
	 */
	public boolean isGeneric() {
		return generic;
	}
	/**
	 * @param generic The generic to set.
	 */
	public void setGeneric(boolean generic) {
		this.generic = generic;
	}

	private static short DEFAULT_VERSION=2;
	private short version=DEFAULT_VERSION;

	public short getVersion() {
		return version;
	}
	private void writeObject(ObjectOutputStream s) throws IOException {
	    s.defaultWriteObject();
	    hasKey.serialize(s);
	    costRateTables.serialize(s);
	    customFields.serialize(s);
    	s.writeInt(hasAssignments.getSchedulingType());
    	s.writeBoolean(hasAssignments.isEffortDriven());
	    availabilityTable.serialize(s);
	}
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException  {
	    s.defaultReadObject();
	    hasKey=HasKeyImpl.deserialize(s,this);
	    costRateTables=CostRateTables.deserialize(s);
	    try {
	    	customFields=CustomFieldsImpl.deserialize(s);
	    } catch (java.io.OptionalDataException e) {
	    	// to ensure compatibilty with old files
	    	customFields = new CustomFieldsImpl();
	    }
		hasAssignments = new HasAssignmentsImpl();
	    if (version>=2){
	    	hasAssignments.setSchedulingType(s.readInt());
	    	hasAssignments.setEffortDriven(s.readBoolean());
		    availabilityTable=AvailabilityTable.deserialize(s);
	    }else availabilityTable=new AvailabilityTable(null);
	    version=DEFAULT_VERSION;
	}

	public Object clone(){
		try {
			EnterpriseResource resource=(EnterpriseResource)super.clone();
			resource.hasKey=new HasKeyImpl(isLocal()&&Environment.getStandAlone(),resource);
			resource.setName(new String(getName()));
			if (notes!=null) resource.notes = new String(notes);
			if (group!=null)resource.group = new String(group);
			if (group!=null)resource.initials = new String(initials);
			if (phonetics!=null)resource.phonetics = new String(phonetics);
			if (rbsCode!=null)resource.rbsCode = new String(rbsCode);
			if (emailAddress!=null)resource.emailAddress = new String(emailAddress);
			if (materialLabel!=null)resource.materialLabel = new String(materialLabel);
			if (userAccount != null)
			   resource.userAccount = new String(userAccount);

			resource.costRateTables=(CostRateTables)costRateTables.clone();
			resource.hasAssignments=(HasAssignments)((HasAssignmentsImpl)hasAssignments).cloneWithResource(resource);
			resource.customFields=(CustomFieldsImpl)customFields.clone();

			resource.availabilityTable=(AvailabilityTable)availabilityTable.clone();
			resource.availabilityTable.initAfterCloning();



			return resource;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
	public void cleanClone(){
		resourcePool=null;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.resource.ResourceSpecificFields#getBaseCalendar()
	 */
	public WorkCalendar getBaseCalendar() {
		if (getWorkCalendar() == null)
			return null;
		return (WorkingCalendar) ((WorkingCalendar)getWorkCalendar()).getBaseCalendar();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.resource.ResourceSpecificFields#setBaseCalendar(com.projity.pm.calendar.WorkingCalendar)
	 */
	public void setBaseCalendar(WorkCalendar baseCalendar) throws CircularDependencyException {
		WorkCalendar old = getWorkCalendar();
		if (old == null)
			return;

		CalendarService.getInstance().reassignCalendar(this,old,baseCalendar);

		((WorkingCalendar)getWorkCalendar()).changeBaseCalendar(baseCalendar);
		invalidateAssignmentCalendars(); // assignments intersection calendars need to be recalculated

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
	public boolean isReadOnlyWork(FieldContext fieldContext) {
		return true;
	}
	public boolean isReadOnlyActualWork(FieldContext fieldContext) {
		return true;
	}
	public boolean isReadOnlyRemainingWork(FieldContext fieldContext) {
		return true;
	}
	public double getActualFixedCost(FieldContext fieldContext) {
		return 0;
	}
	public boolean fieldHideActualFixedCost(FieldContext fieldContext) {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#actualFixedCost(long, long)
	 */
	public double fixedCost(long start, long end) {
		return 0;
	}

	public double actualFixedCost(long start, long end) {
		return 0;
	}

	public double getFixedCost(FieldContext fieldContext) {
		return 0;
	}

	public void setFixedCost(double fixedCost, FieldContext fieldContext) {
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.TimeDistributedFields#isReadOnlyFixedCost(com.projity.field.FieldContext)
	 */
	public boolean isReadOnlyFixedCost(FieldContext fieldContext) {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.projity.datatype.CanSupplyRateUnit#getTimeUnit()
	 */
	public String getTimeUnitLabel() {
		if (getResourceType() == ResourceType.WORK)
			return null;
		return getMaterialLabel();
	}
	public boolean fieldHideOvertimeRate(FieldContext fieldContext) {
		return !isLabor();
	}

	public boolean fieldHideBaseCalendar(FieldContext fieldContext) {
		return !isLabor();
	}

//	public boolean isNew() {
//		return hasKey.isNew();
//	}
	public boolean hasLaborAssignment() {
			return isLabor() && !getAssignments().isEmpty();
	}
	public void invalidateAssignmentCalendars() {
		hasAssignments.invalidateAssignmentCalendars();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.HasCalendar#invalidateCalendar()
	 */
	public Document invalidateCalendar() {
		invalidateAssignmentCalendars();
		return getResourcePool();
	}

	public boolean isWork() {
		return getResourceType() == ResourceType.WORK;
	}

	public boolean isMaterial() {
		return getResourceType() == ResourceType.MATERIAL;
	}

	public boolean isMe() {
		if (userAccount==null) return false;
		return userAccount.equals(Environment.getLogin());
	}

	public boolean isParent() {
		// currently the model contains ResourceImpls and not enterprise resources
		return false;
	}

	public long getParentId(int outlineNumber) {
		// currently the model contains ResourceImpls and not enterprise resources
		return 0;
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
	public CustomFields getCustomFields() {
		return customFields;
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

	private transient boolean dirty;
	public boolean isDirty() {
		return dirty;
	}
	public void setDirty(boolean dirty) {
		//System.out.println("EnterpriseResource _setDirty("+dirty+"): "+getName());
		this.dirty = dirty;
	}

	public boolean isReadOnly() {
		return !master && !isLocal() && !Environment.getStandAlone();
	}

	protected transient boolean master;


	public boolean isLocal() {
		return hasKey.isLocal();
	}

	public void setLocal(boolean local) {
		hasKey.setLocal(local);
	}

	public boolean isMaster() {
		return master;
	}

	public void setMaster(boolean master) {
		this.master = master;
	}

	protected transient GroupedCalculatedValues globalWorkVector;
	public GroupedCalculatedValues getGlobalWorkVector() {
		return globalWorkVector;
	}
	public void setGlobalWorkVector(GroupedCalculatedValues globalWorkVector) {
		this.globalWorkVector = globalWorkVector;
	}


	public long getFinishOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getStartOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	public RateFormat getRateFormat(){
		return RateFormat.getInstance(getTimeUnitLabel(), false, isLabor(), isLabor());
	}
	public String getResourceName(){
		return getName();
	}

	public ImageLink getBudgetStatusIndicator() {
		return EarnedValueCalculator.getInstance().getBudgetStatusIndicator(getCpi(null));
	}

	public ImageLink getScheduleStatusIndicator() {
		return EarnedValueCalculator.getInstance().getBudgetStatusIndicator(getSpi(null));
	}

	public boolean isUser() {
		return userAccount != null && userAccount.length() > 0;
	}

	public boolean renumber(boolean localOnly){
		return hasKey.renumber(localOnly);
	}

	public boolean isAssignedToSomeProject() {
		if (hasAssignments.getAssignments().size() > 0)
			return true;
		if (globalWorkVector == null || globalWorkVector.size() == 0) // note that this doesn't mean there isn't baseline info assigned
			return false;
		return true;
	}

	private transient Set<Integer> authorizedRoles;

	public Set<Integer> getAuthorizedRoles() {
		return authorizedRoles;
	}
	public void setAuthorizedRoles(Set<Integer> authorizedRoles) {
		this.authorizedRoles = authorizedRoles;
	}
	public void filterRoles(List keys,List values){
		if (authorizedRoles==null) return;
		Iterator k=keys.iterator();
		Iterator<Integer> v=((List<Integer>)values).iterator();
		Object inactiveKey=null;
		while (v.hasNext()) {
			Object key=k.next();
			int r=v.next();
			if (r==ApplicationUser.INACTIVE) inactiveKey=key;
			if ((r==ApplicationUser.INACTIVE&&getAssignments().size()>0)||
					!authorizedRoles.contains(r)) k.remove();
		}
		if (keys.size()==0) keys.add(inactiveKey); //occurs when an user becomes "inactive"
	}
	private transient int defaultRole;

	public int getDefaultRole() {
		return defaultRole;
	}
	public void setDefaultRole(int defaultRole) {
		this.defaultRole = defaultRole;
	}

	private transient int license;
	private transient int licenseOptions;

	public int getLicense(){
		return license;
	}
	public void setLicense(int license) {
		this.license = license;
	}

	public int getLicenseOptions() {
		return licenseOptions;
	}

	public void setLicenseOptions(int licenseOptions) {
		this.licenseOptions = licenseOptions;
	}

	public boolean isInactiveLicense(){
		return license==ApplicationUser.INACTIVE;
	}

	public boolean isExternal(){
		return (licenseOptions&ApplicationUser.EXTERNAL)==ApplicationUser.EXTERNAL;
	}

	public boolean isAdministrator(){
		return (licenseOptions&ApplicationUser.ADMINISTRATOR)==ApplicationUser.ADMINISTRATOR;
	}



	private transient AvailabilityTable availabilityTable = new AvailabilityTable(null);
	public AvailabilityTable getAvailabilityTable() {
		//TODO implement this somehow. need to figure out relationship to ResourceImpl version
		// Do projects have their own availability tables or not?
		// TODO Auto-generated method stub
		return availabilityTable;
	}


	protected transient Object serverMeta;


	public Object getServerMeta() {
		return serverMeta;
	}

	public void setServerMeta(Object serverMeta) {
		this.serverMeta = serverMeta;
	}


}
