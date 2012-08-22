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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Predicate;

import com.projity.algorithm.ReverseQuery;
import com.projity.algorithm.TimeIteratorGenerator;
import com.projity.algorithm.buffer.CalculatedValues;
import com.projity.association.AssociationList;
import com.projity.company.ApplicationUser;
import com.projity.company.UserUtil;
import com.projity.configuration.CircularDependencyException;
import com.projity.configuration.Configuration;
import com.projity.configuration.FieldDictionary;
import com.projity.datatype.ImageLink;
import com.projity.datatype.Rate;
import com.projity.datatype.RateFormat;
import com.projity.document.Document;
import com.projity.field.CustomFields;
import com.projity.field.Field;
import com.projity.field.FieldContext;
import com.projity.graphic.configuration.HasIndicators;
import com.projity.graphic.configuration.HasResourceIndicators;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.model.NodeModel;
import com.projity.interval.InvalidValueObjectForIntervalException;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.availability.AvailabilityTable;
import com.projity.pm.availability.HasAvailability;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.costing.CostRateTable;
import com.projity.pm.key.HasKey;
import com.projity.pm.task.AccessControlPolicy;
import com.projity.pm.task.Project;
import com.projity.pm.task.Task;
import com.projity.util.Environment;
/**
 * This class is used to hold resources assigned to a project.  Currently everything delegates to the global resource's values.
 * In the future, some fields such as costs and availability should be overridable on he porject level.
 */
public class ResourceImpl implements Resource, HasAvailability, HasResourceIndicators {
	static final long serialVersionUID = 9485792329492L;
	private static ResourceImpl UNASSIGNED = null;
	static HashSet readOnlyUserFields = null;

	private HashSet getReadOnlyUserFields() {
		if (readOnlyUserFields == null) {
			readOnlyUserFields = new HashSet();
			readOnlyUserFields.add(Configuration.getFieldFromId("Field.name"));
	//		readOnlyUserFields.add(Configuration.getFieldFromId("Field.emailAddress"));
		}
		return readOnlyUserFields;
	}
	public long getEarliestAssignmentStart() {
		return globalResource.getEarliestAssignmentStart();
	}
	public boolean hasActiveAssignment(long start, long end) {
		return globalResource.hasActiveAssignment(start, end);
	}
	/**
	 * @return
	 */
	public static Resource getUnassignedInstance() {
		if (UNASSIGNED == null) {
			UNASSIGNED = new ResourceImpl((EnterpriseResource) EnterpriseResource.getUnassignedInstance());
		}
		return UNASSIGNED;
	}

	transient EnterpriseResource globalResource = null;
	private int bookingType = BookingType.COMMITTED;

//Methods not in EnterpriseResources
	public int getBookingType() {
		return bookingType;
	}
	public void setBookingType(int bookingType) {
		this.bookingType = bookingType;
	}
	/**
	 * @return Returns the availabilityTable.
	 */
	public AvailabilityTable getAvailabilityTable() {
		return globalResource.getAvailabilityTable();
	}
	public void setAvailableFrom(long availableFrom) throws InvalidValueObjectForIntervalException {
		getAvailabilityTable().setAvailableFrom(availableFrom);
	}
	public void setAvailableTo(long availableTo) {
		getAvailabilityTable().setAvailableTo(availableTo);
	}
	public boolean isReadOnlyAvailableFrom(FieldContext fieldContext) {
		return true;
	}
	public boolean isReadOnlyAvailableTo(FieldContext fieldContext) {
		return true;
	}

	public long getAvailableFrom() {
		return getAvailabilityTable().getAvailableFrom();
	}
	public long getAvailableTo() {
		return getAvailabilityTable().getAvailableTo();
	}
	public double getMaximumUnits() {
		return getAvailabilityTable().getMaximumUnits();
//		return globalResource.getMaximumUnits();
	}
	public void setMaximumUnits(double maxUnits) {
		getAvailabilityTable().setMaximumUnits(maxUnits);
		//		globalResource.setMaximumUnits(maxUnits);
	}


	public ResourceImpl() {
	}

	/**
	 * @param pool
	 */
	public ResourceImpl(EnterpriseResource globalResource) {
		this.globalResource = globalResource;
	}


// Delegated methods

	public static Closure forAllAssignments(Closure visitor) {
		return EnterpriseResource.forAllAssignments(visitor);
	}
	public double actualCost(long start, long end) {
		return globalResource.actualCost(start, end);
	}
	public long actualWork(long start, long end) {
		return globalResource.actualWork(start, end);
	}
	public double acwp(long start, long end) {
		return globalResource.acwp(start, end);
	}
	public void addAssignment(Assignment assignment) {
		globalResource.addAssignment(assignment);
		addInTeam();
	}
	/*public void addDefaultAssignment() {
		globalResource.addDefaultAssignment();
	}*/
	public double bac(long start, long end) {
		return globalResource.bac(start, end);
	}
	public double baselineCost(long start, long end) {
		return globalResource.baselineCost(start, end);
	}
	public long baselineWork(long start, long end) {
		return globalResource.baselineWork(start, end);
	}
	public double bcwp(long start, long end) {
		return globalResource.bcwp(start, end);
	}
	public double bcws(long start, long end) {
		return globalResource.bcws(start, end);
	}
	public void buildReverseQuery(ReverseQuery reverseQuery) {
		globalResource.buildReverseQuery(reverseQuery);
	}
	public long calcActiveAssignmentDuration(WorkCalendar workCalendar) {
		return globalResource.calcActiveAssignmentDuration(workCalendar);
	}
	public void calcDataBetween(Object type, TimeIteratorGenerator generator,
			CalculatedValues values) {
		globalResource.calcDataBetween(type, generator, values);
	}
	public Collection childrenToRollup() {
		return globalResource.childrenToRollup();
	}
	public double cost(long start, long end) {
		return globalResource.cost(start, end);
	}
	public boolean fieldHideActualCost(FieldContext fieldContext) {
		return globalResource.fieldHideActualCost(fieldContext);
	}
	public boolean fieldHideActualWork(FieldContext fieldContext) {
		return globalResource.fieldHideActualWork(fieldContext);
	}
	public boolean fieldHideAcwp(FieldContext fieldContext) {
		return globalResource.fieldHideAcwp(fieldContext);
	}
	public boolean fieldHideBac(FieldContext fieldContext) {
		return globalResource.fieldHideBac(fieldContext);
	}
	public boolean fieldHideBaselineCost(int numBaseline,FieldContext fieldContext) {
		return globalResource.fieldHideBaselineCost(numBaseline,fieldContext);
	}
	public boolean fieldHideBaselineWork(int numBaseline,FieldContext fieldContext) {
		return globalResource.fieldHideBaselineWork(numBaseline,fieldContext);
	}
	public boolean fieldHideBcwp(FieldContext fieldContext) {
		return globalResource.fieldHideBcwp(fieldContext);
	}
	public boolean fieldHideBcws(FieldContext fieldContext) {
		return globalResource.fieldHideBcws(fieldContext);
	}
	public boolean fieldHideCost(FieldContext fieldContext) {
		return globalResource.fieldHideCost(fieldContext);
	}
	public boolean fieldHideCpi(FieldContext fieldContext) {
		return globalResource.fieldHideCpi(fieldContext);
	}
	public boolean fieldHideCv(FieldContext fieldContext) {
		return globalResource.fieldHideCv(fieldContext);
	}
	public boolean fieldHideCvPercent(FieldContext fieldContext) {
		return globalResource.fieldHideCvPercent(fieldContext);
	}
	public boolean fieldHideEac(FieldContext fieldContext) {
		return globalResource.fieldHideEac(fieldContext);
	}
	public boolean fieldHideSpi(FieldContext fieldContext) {
		return globalResource.fieldHideSpi(fieldContext);
	}
	public boolean fieldHideSv(FieldContext fieldContext) {
		return globalResource.fieldHideSv(fieldContext);
	}
	public boolean fieldHideSvPercent(FieldContext fieldContext) {
		return globalResource.fieldHideSvPercent(fieldContext);
	}
	public boolean fieldHideTcpi(FieldContext fieldContext) {
		return globalResource.fieldHideTcpi(fieldContext);
	}
	public boolean fieldHideVac(FieldContext fieldContext) {
		return globalResource.fieldHideVac(fieldContext);
	}
	public boolean fieldHideWork(FieldContext fieldContext) {
		return globalResource.fieldHideWork(fieldContext);
	}
	public Assignment findAssignment(Resource resource) {
		return globalResource.findAssignment(resource);
	}
	public Assignment findAssignment(Task task) {
		return globalResource.findAssignment(task);
	}
	public void forEachWorkingInterval(Closure visitor, boolean mergeWorking,
			WorkCalendar workCalendar) {
		globalResource.forEachWorkingInterval(visitor, mergeWorking,
				workCalendar);
	}
	public int getAccrueAt() {
		return globalResource.getAccrueAt();
	}
	public double getActualCost(FieldContext fieldContext) {
		return globalResource.getActualCost(fieldContext);
	}
	public long getActualWork(FieldContext fieldContext) {
		return globalResource.getActualWork(fieldContext);
	}
	public double getAcwp(FieldContext fieldContext) {
		return globalResource.getAcwp(fieldContext);
	}
	public AssociationList getAssignments() {
		return globalResource.getAssignments();
	}
	public double getBac(FieldContext fieldContext) {
		return globalResource.getBac(fieldContext);
	}
	public double getBaselineCost(int numBaseline, FieldContext fieldContext) {
		return globalResource.getBaselineCost(numBaseline, fieldContext);
	}
	public long getBaselineWork(int numBaseline, FieldContext fieldContext) {
		return globalResource.getBaselineWork(numBaseline, fieldContext);
	}
	public double getBcwp(FieldContext fieldContext) {
		return globalResource.getBcwp(fieldContext);
	}
	public double getBcws(FieldContext fieldContext) {
		return globalResource.getBcws(fieldContext);
	}
	public double getCost(FieldContext fieldContext) {
		return globalResource.getCost(fieldContext);
	}
	public double getCostPerUse() {
		return globalResource.getCostPerUse();
	}
	public CostRateTable getCostRateTable(int costRateIndex) {
		return globalResource.getCostRateTable(costRateIndex);
	}
	public double getCpi(FieldContext fieldContext) {
		return globalResource.getCpi(fieldContext);
	}
	public Date getCreated() {
		return globalResource.getCreated();
	}
	public double getCv(FieldContext fieldContext) {
		return globalResource.getCv(fieldContext);
	}
	public double getCvPercent(FieldContext fieldContext) {
		return globalResource.getCvPercent(fieldContext);
	}
	public Document getDocument() {
		return globalResource.getDocument();
	}
	public double getEac(FieldContext fieldContext) {
		return globalResource.getEac(fieldContext);
	}
	public WorkCalendar getEffectiveWorkCalendar() {
		return globalResource.getEffectiveWorkCalendar();
	}
	public String getGroup() {
		return globalResource.getGroup();
	}
	public long getId() {
		return globalResource.getId();
	}
	public long getExternalId() {
		return globalResource.getExternalId();
	}
	public String getInitials() {
		return globalResource.getInitials();
	}
	public String getName() {
		return globalResource.getName();
	}
	public String getName(FieldContext context) {
		return globalResource.getName(context);
	}
	public String getNotes() {
		return globalResource.getNotes();
	}
	public Rate getOvertimeRate() {
		return globalResource.getOvertimeRate();
	}
	public String getPhonetics() {
		return globalResource.getPhonetics();
	}
	public String getRbsCode() {
		return globalResource.getRbsCode();
	}
	public double getRemainingOvertimeCost() {
		return globalResource.getRemainingOvertimeCost();
	}
	public long getRemainingWork(FieldContext fieldContext) {
		return globalResource.getRemainingWork(fieldContext);
	}
	public ResourcePool getResourcePool() {
		return globalResource.getResourcePool();
	}
	public int getResourceType() {
		return globalResource.getResourceType();
	}
	public int getSchedulingType() {
		return globalResource.getSchedulingType();
	}
	public double getSpi(FieldContext fieldContext) {
		return globalResource.getSpi(fieldContext);
	}
	public double getCsi(FieldContext fieldContext) {
		return globalResource.getCsi(fieldContext);
	}
	public Rate getStandardRate() {
		return globalResource.getStandardRate();
	}
	public double getSv(FieldContext fieldContext) {
		return globalResource.getSv(fieldContext);
	}
	public double getSvPercent(FieldContext fieldContext) {
		return globalResource.getSvPercent(fieldContext);
	}
	public double getTcpi(FieldContext fieldContext) {
		return globalResource.getTcpi(fieldContext);
	}
	public long getUniqueId() {
		return globalResource.getUniqueId();
	}
	public double getVac(FieldContext fieldContext) {
		return globalResource.getVac(fieldContext);
	}
	public long getWork(FieldContext fieldContext) {
		return globalResource.getWork(fieldContext);
	}
	public WorkCalendar getWorkCalendar() {
		return globalResource.getWorkCalendar();
	}
	public int hashCode() {
		return globalResource.hashCode();
	}
	public boolean isAssignment() {
		return globalResource.isAssignment();
	}
	public boolean isEffortDriven() {
		return globalResource.isEffortDriven();
	}
	public boolean isReadOnlyEffortDriven(FieldContext fieldContext) {
		return globalResource.isReadOnlyEffortDriven(fieldContext);
	}
	public long remainingWork(long start, long end) {
		return globalResource.remainingWork(start, end);
	}
	public void removeAssignment(Assignment assignment) {
		globalResource.removeAssignment(assignment);
	}
	public void setAccrueAt(int accrueAt) {
		globalResource.setAccrueAt(accrueAt);
	}
	public void setCostPerUse(double costPerUse) {
		globalResource.setCostPerUse(costPerUse);
	}
	public void setCreated(Date created) {
		globalResource.setCreated(created);
	}
	public void setEffortDriven(boolean effortDriven) {
		globalResource.setEffortDriven(effortDriven);
	}
	public void setGroup(String group) {
		globalResource.setGroup(group);
	}
	public void setId(long id) {
	    if (globalResource!=null) globalResource.setId(id);
	}
	public void setExternalId(long id) {
	    if (globalResource!=null) globalResource.setExternalId(id);
	}
	public void setInitials(String initials) {
		globalResource.setInitials(initials);
	}
	public void setName(String name) {
		if (globalResource!=null) globalResource.setName(name);
	}
	public void setNotes(String notes) {
		globalResource.setNotes(notes);
	}
	public void setOvertimeRate(Rate overtimeRate) {
		globalResource.setOvertimeRate(overtimeRate);
	}
	public void setPhonetics(String phonetics) {
		globalResource.setPhonetics(phonetics);
	}
	public void setRbsCode(String wbsCode) {
		globalResource.setRbsCode(wbsCode);
	}
	public void setResourceType(int resourceType) {
		globalResource.setResourceType(resourceType);
	}
	public void setSchedulingType(int schedulingType) {
		globalResource.setSchedulingType(schedulingType);
	}
	public void setStandardRate(Rate standardRate) {
		globalResource.setStandardRate(standardRate);
	}
	public void setUniqueId(long id) {
		if (globalResource!=null) globalResource.setUniqueId(id);
	}
	public void setWorkCalendar(WorkCalendar workCalendar) {
		globalResource.setWorkCalendar(workCalendar);
	}
	public String toString() {
		return globalResource.toString();
	}
	public void updateAssignment(Assignment modified) {
		globalResource.updateAssignment(modified);
	}
	public long work(long start, long end) {
		return globalResource.work(start, end);
	}
	public String getEmailAddress() {
		return globalResource.getEmailAddress();
	}
	public void setEmailAddress(String emailAddress) {
		globalResource.setEmailAddress(emailAddress);
	}
	public String getMaterialLabel() {
		return globalResource.getMaterialLabel();
	}
	public void setMaterialLabel(String materialLabel) {
		globalResource.setMaterialLabel(materialLabel);
	}
	public boolean isReadOnlyMaterialLabel(FieldContext fieldContext) {
		return globalResource.isReadOnlyMaterialLabel(fieldContext);
	}
	public String getUserAccount() {
		return globalResource.getUserAccount();
	}
	public final void setUserAccount(String userAccount) {
		globalResource.setUserAccount(userAccount);
	}

	public boolean isInactive() {
		return globalResource.isInactive();
	}
	public boolean isGeneric() {
		return globalResource.isGeneric();
	}
	public void setInactive(boolean inactive) {
		globalResource.setInactive(inactive);
	}
	public void setGeneric(boolean generic) {
		globalResource.setGeneric(generic);
	}
	/**
	 * @return
	 */
	public long getEffectiveDate() {
		return globalResource.getEffectiveDate();
	}
	/**
	 * @return
	 */
	public boolean isReadOnlyEffectiveDate(FieldContext fieldContext) {
		return globalResource.isReadOnlyEffectiveDate(fieldContext);
	}
	/**
	 * @param effectiveDate
	 */
	public void setEffectiveDate(long effectiveDate) throws InvalidValueObjectForIntervalException{
		globalResource.setEffectiveDate(effectiveDate);
	}

	public boolean isDefault(){
	    return this==UNASSIGNED;
	}

    public EnterpriseResource getGlobalResource() {
        return globalResource;
    }
    public void setGlobalResource(EnterpriseResource globalResource) {
        this.globalResource = globalResource;
    }


	private void writeObject(ObjectOutputStream s) throws IOException {
	    s.defaultWriteObject();
	}
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException  {
	    s.defaultReadObject();
	}

	public Object clone(){
		try {
			ResourceImpl resource=(ResourceImpl)super.clone();
			resource.globalResource=(EnterpriseResource)globalResource.clone();
			return resource;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
	public void cleanClone(){
		globalResource.cleanClone();
	}



	/**
	 * @return
	 */
	public WorkCalendar getBaseCalendar() {
		return globalResource.getBaseCalendar();
	}
	/**
	 * @param baseCalendar
	 * @throws CircularDependencyException
	 */
	public void setBaseCalendar(WorkCalendar baseCalendar) throws CircularDependencyException {
		globalResource.setBaseCalendar(baseCalendar);
	}
	public boolean isReadOnlyWork(FieldContext fieldContext) {
		return globalResource.isReadOnlyWork(fieldContext);
	}
	public void setWork(long work, FieldContext fieldContext) {
		globalResource.setWork(work, fieldContext);
	}
	public boolean isReadOnlyActualWork(FieldContext fieldContext) {
		return globalResource.isReadOnlyActualWork(fieldContext);
	}
	public boolean isReadOnlyRemainingWork(FieldContext fieldContext) {
		return globalResource.isReadOnlyRemainingWork(fieldContext);
	}
	public void setActualWork(long actualWork, FieldContext fieldContext) {
		globalResource.setActualWork(actualWork, fieldContext);
	}
	public void setRemainingWork(long remainingWork, FieldContext fieldContext) {
		globalResource.setRemainingWork(remainingWork, fieldContext);
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
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public double actualFixedCost(long start, long end) {
		return globalResource.actualFixedCost(start, end);
	}
	/**
	 * @param fieldContext
	 * @return
	 */
	public double getFixedCost(FieldContext fieldContext) {
		return globalResource.getFixedCost(fieldContext);
	}
	/**
	 * @param fieldContext
	 * @return
	 */
	public boolean isReadOnlyFixedCost(FieldContext fieldContext) {
		return globalResource.isReadOnlyFixedCost(fieldContext);
	}

	public boolean isReadOnly(){
		return false; //roles
		//return globalResource.isReadOnly();
	}

	/**
	 * @param fixedCost
	 * @param fieldContext
	 */
	public void setFixedCost(double fixedCost, FieldContext fieldContext) {
		globalResource.setFixedCost(fixedCost, fieldContext);
	}
	public String getTimeUnitLabel() {
		return globalResource.getTimeUnitLabel();
	}
	public boolean fieldHideOvertimeRate(FieldContext fieldContext) {
		return globalResource.fieldHideOvertimeRate(fieldContext);
	}
	/* (non-Javadoc)
	 * @see com.projity.pm.availability.HasAvailability#fieldHideMaximumUnits(com.projity.field.FieldContext)
	 */
	public boolean fieldHideMaximumUnits(FieldContext fieldContext) {
		return fieldHideOvertimeRate(fieldContext);
	}
	public boolean fieldHideBaseCalendar(FieldContext fieldContext) {
		return globalResource.fieldHideBaseCalendar(fieldContext);
	}
	public boolean isLabor() {
		return globalResource.isLabor();
	}
//	public boolean isNew() { //TODO does this have its own ID and not the global resource id?
//		return globalResource.isNew();
//	}
//	public void setNew(boolean isNew) {
//		globalResource.setNew(isNew);
//	}
	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#hasLaborAssignment()
	 */
	public boolean hasLaborAssignment() {
		return globalResource.hasLaborAssignment();
	}
	public double getRemainingCost(FieldContext fieldContext) {
		return globalResource.getRemainingCost(fieldContext);
	}

	public static Predicate instanceofPredicate() {
		return new Predicate() {
			public boolean evaluate(Object arg0) {
				return arg0 instanceof Resource;
			}};
	}
	public void invalidateAssignmentCalendars() {
		globalResource.invalidateAssignmentCalendars();
	}
	public Document invalidateCalendar() {
		return globalResource.invalidateCalendar();
	}
	public boolean isMaterial() {
		return globalResource.isMaterial();
	}
	public boolean isMe() {
		return globalResource.isMe();
	}
	public boolean isWork() {
		return globalResource.isWork();
	}
	public long getParentId(int outlineNumber) {
		NodeModel model= getResourcePool().getResourceOutline(outlineNumber);
		if (model == null)
			return 0;
		Node node = model.getParent(model.search(this));
		Object impl = node.getImpl();
		if (impl != null && impl instanceof HasKey)
			return ((HasKey)impl).getId();
		return 0;
	}

	public boolean isParent() {
		NodeModel model= getResourcePool().getResourceOutline();
		return model.hasChildren(model.search(this));
	}
	public double getCustomCost(int i) {
		return globalResource.getCustomCost(i);
	}
	public long getCustomDate(int i) {
		return globalResource.getCustomDate(i);
	}
	public long getCustomDuration(int i) {
		return globalResource.getCustomDuration(i);
	}
	public CustomFields getCustomFields() {
		return globalResource.getCustomFields();
	}
	public long getCustomFinish(int i) {
		return globalResource.getCustomFinish(i);
	}
	public boolean getCustomFlag(int i) {
		return globalResource.getCustomFlag(i);
	}
	public double getCustomNumber(int i) {
		return globalResource.getCustomNumber(i);
	}
	public long getCustomStart(int i) {
		return globalResource.getCustomStart(i);
	}
	public String getCustomText(int i) {
		return globalResource.getCustomText(i);
	}
	public void setCustomCost(int i, double cost) {
		globalResource.setCustomCost(i, cost);
	}
	public void setCustomDate(int i, long date) {
		globalResource.setCustomDate(i, date);
	}
	public void setCustomDuration(int i, long duration) {
		globalResource.setCustomDuration(i, duration);
	}
	public void setCustomFinish(int i, long finish) {
		globalResource.setCustomFinish(i, finish);
	}
	public void setCustomFlag(int i, boolean flag) {
		globalResource.setCustomFlag(i, flag);
	}
	public void setCustomNumber(int i, double number) {
		globalResource.setCustomNumber(i, number);
	}
	public void setCustomStart(int i, long start) {
		globalResource.setCustomStart(i, start);
	}
	public void setCustomText(int i, String text) {
		globalResource.setCustomText(i, text);
	}
	public boolean applyTimesheet(Collection fieldArray, long timesheetUpdateDate) {
		return globalResource.applyTimesheet(fieldArray, timesheetUpdateDate);
	}
	public long getLastTimesheetUpdate() {
		return globalResource.getLastTimesheetUpdate();
	}
	public int getTimesheetStatus() {
		return globalResource.getTimesheetStatus();
	}
	public boolean isPendingTimesheetUpdate() {
		return globalResource.isPendingTimesheetUpdate();
	}
	public String getTimesheetStatusName() {
		return globalResource.getTimesheetStatusName();
	}

	private transient boolean dirty;
	public boolean isDirty() {
		return dirty;
	}
	public void setDirty(boolean dirty) {
		//System.out.println("ResourceImpl _setDirty("+dirty+"): "+getName());
		this.dirty = dirty;
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
		return globalResource.getRateFormat();
	}
	public String getResourceName(){
		return globalResource.getResourceName();
	}
	public ImageLink getBudgetStatusIndicator() {
		return globalResource.getBudgetStatusIndicator();
	}
	public ImageLink getScheduleStatusIndicator() {
		return globalResource.getScheduleStatusIndicator();
	}

	public boolean inProgress(){
		for (Iterator i=getAssignments().iterator();i.hasNext();){
			Assignment a=(Assignment)i.next();
			if (a.inProgress()) return true;
		}
		return false;
	}

	public boolean isUnstarted(){
		for (Iterator i=getAssignments().iterator();i.hasNext();){
			Assignment a=(Assignment)i.next();
			if (a.isUnstarted()) return true;
		}
		return false;
	}
	public boolean isComplete(){
		for (Iterator i=getAssignments().iterator();i.hasNext();){
			Assignment a=(Assignment)i.next();
			if (!a.isComplete()) return false;
		}
		return true;
	}

	public boolean isReadOnly(Field f) {
		// roles
		boolean roleField="Field.userRole".equals(f.getId());
		//if (roleField&&!isUser()) return true;
		if (!roleField&&globalResource.isReadOnly()) return true;

		if (!isUser())
			return false;
		return getReadOnlyUserFields().contains(f);
	}

	public boolean isUser() {
		return globalResource.isUser();
	}

	public boolean renumber(boolean localOnly){
		return globalResource.renumber(localOnly);
	}
	public boolean isAssignedToSomeProject() {
		return globalResource.isAssignedToSomeProject();
	}



	private transient int role;

	public int getRole() {
		return role;
	}
	public void setRole(int role) {
		if (this.role!=role){
			int defaultRole=getDefaultRole();
			if (globalResource!=null){
				if((!isExternal()&&role!=defaultRole)||(isExternal()&&role!=ApplicationUser.INACTIVE)){
					ResourcePool resourcePool=getResourcePool();
					if (resourcePool!=null){
						Collection projects=resourcePool.getProjects();
						if (projects!=null&&projects.size()>0){
							Project project=(Project)projects.iterator().next();
							Field field=FieldDictionary.getInstance().getFieldFromId("Field.accessControlPolicy");
							if (field != null)
								field.setValue(project, project, AccessControlPolicy.RESTRICTED);
						}
					}
				}
			}
			this.role = role;
			//System.out.println("New role for "+getName()+": "+role);
		}
	}


	public int getExtendedRole(){ //more information for field combo
		return UserUtil.toExtendedRole(role,  isUser());
	}
	public void setExtendedRole(int role){
		setRole(UserUtil.toNormalRole(role));
	}
	public boolean isReadOnlyExtendedRole(FieldContext fieldContext) { // moved out of spreadsheet model
		if (Environment.getStandAlone())
			return true;
		return Environment.getUser().getResourceId() == getUniqueId(); // prevents a user from losing access to his project
	}

	public int getLicense(){
		return globalResource.getLicense();
	}


	public Set<Integer> getAuthorizedRoles() {
		return globalResource.getAuthorizedRoles();
	}
	public void setAuthorizedRoles(Set<Integer> authorizedRoles) {
		globalResource.setAuthorizedRoles(authorizedRoles);
	}
	public void filterRoles(List keys,List values){
		globalResource.filterRoles(keys, values);
	}

	public int getDefaultRole() {
		return globalResource.getDefaultRole();
	}
	public void setDefaultRole(int defaultRole) {
		globalResource.setDefaultRole(defaultRole);
	}


	public HasIndicators getIndicators() {
		return this;
	}


	public boolean isInTeam(){
		return getRole()!=ApplicationUser.INACTIVE || getAssignments().size()>0;
	}

	public boolean isInactiveLicense(){
		return globalResource.isInactiveLicense();
	}

	public boolean isExternal(){
		return globalResource.isExternal();
	}

	public boolean isAdministrator(){
		return globalResource.isAdministrator();
	}

	public boolean isRoleAllowed(int role){
		if (role==ApplicationUser.INACTIVE&&getAssignments().size()>0) return false;
		return true;
	}

	public void addInTeam(){
		if (!isRoleAllowed(getRole())) setRole(isUser()?ApplicationUser.TEAM_MEMBER:ApplicationUser.TEAM_RESOURCE);
	}

	public void setServerMeta(Object o){
		globalResource.setServerMeta(o);
	}

	public Object getServerMeta(){
		return globalResource.getServerMeta();
	}
	public boolean isLocal() {
		return globalResource.isLocal();
	}
	public void setLocal(boolean local) {
		globalResource.setLocal(local);
	}

}
