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

Attribution Information: Attribution Copyright Notice: Copyright (c) 2006, 2007
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import net.sf.mpxj.AccrueType;
import net.sf.mpxj.ConstraintType;
import net.sf.mpxj.DateRange;
import net.sf.mpxj.Day;
import net.sf.mpxj.DayType;
import net.sf.mpxj.EarnedValueMethod;
import net.sf.mpxj.FieldContainer;
import net.sf.mpxj.MPXJException;
import net.sf.mpxj.Priority;
import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectCalendarException;
import net.sf.mpxj.ProjectCalendarHours;
import net.sf.mpxj.ProjectProperties;
import net.sf.mpxj.Resource;
import net.sf.mpxj.ResourceAssignment;
import net.sf.mpxj.Task;
import net.sf.mpxj.TaskType;
import net.sf.mpxj.TimeUnit;
import net.sf.mpxj.WorkContour;
import net.sf.mpxj.mspdi.DatatypeConverter;

import com.projity.configuration.Configuration;
import com.projity.contrib.util.Log;
import com.projity.contrib.util.LogFactory;
import com.projity.datatype.Duration;
import com.projity.datatype.Rate;
import com.projity.exchange.ImportedCalendarService;
import com.projity.field.CustomFields;
import com.projity.grouping.core.VoidNodeImpl;
import com.projity.options.CalendarOption;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.calendar.WorkDay;
import com.projity.pm.calendar.WorkRange;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.calendar.WorkingHours;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.task.NormalTask;
import com.projity.pm.task.Project;
import com.projity.util.DateTime;
import com.projity.util.MathUtils;
/**
 *
 */
public class MPXConverter {
	private static Log log = LogFactory.getLog(MPXConverter.class);

	public static int nameFieldWidth = Configuration.getFieldFromId("Field.name").getTextWidth();


	public static void toMPXOptions(ProjectProperties projectHeader) {

		CalendarOption calendarOption = CalendarOption.getInstance();
//		projectHeader.setDefaultHoursInDay(new Float(calendarOption.getHoursPerDay()));
		projectHeader.setMinutesPerDay(new Integer((int) (60 * calendarOption.getHoursPerDay())));
//		projectHeader.setDefaultHoursInWeek(new Float(calendarOption.getHoursPerWeek()));
		projectHeader.setMinutesPerWeek(new Integer((int) (60 * calendarOption.getHoursPerWeek())));

		projectHeader.setDaysPerMonth(new Integer((int) Math.round(calendarOption.getDaysPerMonth())));

		projectHeader.setDefaultStartTime(calendarOption.getDefaultStartTime().getTime());
		projectHeader.setDefaultEndTime(calendarOption.getDefaultEndTime().getTime());
	}

	public static void toMPXProject(Project project,ProjectProperties projectHeader) {
		WorkCalendar baseCalendar=project.getBaseCalendar();
		projectHeader.setDefaultCalendarName(baseCalendar.getName()); // use unique id for name - this is a hack
		projectHeader.setName(project.getName());
		projectHeader.setProjectTitle(project.getName()); //TODO separate title and name
		projectHeader.setComments(project.getNotes());
		projectHeader.setManager(project.getManager());
		projectHeader.setComments(removeInvalidChars(project.getNotes()));
		projectHeader.setStartDate(DateTime.fromGmt(new Date(project.getStartDate())));
		projectHeader.setFinishDate(DateTime.fromGmt(new Date(project.getFinishDate())));
		projectHeader.setDefaultStartTime(CalendarOption.getInstance().getDefaultStartTime().getTime());
		projectHeader.setDefaultEndTime(CalendarOption.getInstance().getDefaultEndTime().getTime());

	}


	public static void toMpxCalendar(WorkingCalendar workCalendar,ProjectCalendar mpx) {
		mpx.setName(workCalendar.getName());
//		mpx.setUniqueID((int) workCalendar.getId()); // TODO watch out for int overrun

		WorkingCalendar wc = workCalendar;
		if (workCalendar.isBaseCalendar())
			wc = (WorkingCalendar) workCalendar.getBaseCalendar();
		for (int i = 0; i < 7; i++) {// MPX days go from SUNDAY=1 to SATURDAY=7
			WorkDay day= workCalendar.isBaseCalendar() ? workCalendar.getDerivedWeekDay(i) : workCalendar.getWeekDay(i);
			ProjectCalendarHours mpxDay = null;
			Day d = Day.getInstance(i+1);
			if (day == null) {
				mpx.setWorkingDay(d,DayType.DEFAULT); // claur
			} else {
				mpx.setWorkingDay(d,day.isWorking());
				if (day.isWorking()) {
					mpxDay = mpx.addCalendarHours(Day.getInstance(i+1));
					toMpxCalendarDay(day,mpxDay);
				}
			}
		}

		WorkDay[] workDays=workCalendar.getExceptionDays();
		if (workDays!=null)
			for (int i=0;i<workDays.length;i++){
				if (workDays[i]==null||workDays[i].getStart()==0L||workDays[i].getStart()==Long.MAX_VALUE)
					continue;
				Date start = new Date(workDays[i].getStart());
				GregorianCalendar cal = DateTime.calendarInstance();
				// days go from 00:00 to 23:59
				cal.setTime(start);
				cal.set(Calendar.HOUR,23);
				cal.set(Calendar.MINUTE,59);
				ProjectCalendarException exception=mpx.addCalendarException(start,DateTime.fromGmt(cal.getTime())); //claur
				
				toMpxExceptionDay(workDays[i],exception);
				//exception.setWorking(workDays[i].isWorking()); //claur exception is working once it has at least one range
			}
		if (!workCalendar.isBaseCalendar()){
			//claur fix to avoid default hours for bases calendar
			// base calendars have standard calendar for parent
			WorkCalendar baseCalendar=workCalendar.getBaseCalendar();
			mpx.setParent(ImportedCalendarService.getInstance().findExportedCalendar(baseCalendar)); //claur
		}


		//mpx.setUniqueID((int)workCalendar.getUniqueId());
	}
	public static  void toMpxCalendarDay(WorkDay day,ProjectCalendarHours mpxDay) {
		if  (day==null)
			return;
		WorkingHours workingHours=day.getWorkingHours();

		for (WorkRange range:(List<WorkRange>)workingHours.getIntervals()) { //claur
			if (range!=null)
				mpxDay.addRange(new DateRange(DateTime.fromGmt(range.getNormalizedStartTime()),DateTime.fromGmt(range.getNormalizedEndTime())));
		}
	}

	public static  void toMpxExceptionDay(WorkDay day,ProjectCalendarException mpxDay) {
		if  (day==null)
			return;
		WorkingHours workingHours=day.getWorkingHours();
		WorkRange range;

		range=workingHours.getInterval(0);
		if (range!=null){
			mpxDay.addRange(new DateRange(DateTime.fromGmt(range.getNormalizedStartTime()),DateTime.fromGmt(range.getNormalizedEndTime()))); //claur
		}

		range=workingHours.getInterval(1);
		if (range!=null){
			mpxDay.addRange(new DateRange(DateTime.fromGmt(range.getNormalizedStartTime()),DateTime.fromGmt(range.getNormalizedEndTime())));//claur
		}

		range=workingHours.getInterval(2);
		if (range!=null){
			mpxDay.addRange(new DateRange(DateTime.fromGmt(range.getNormalizedStartTime()),DateTime.fromGmt(range.getNormalizedEndTime())));//claur
		}
	}


	public static void toMPXResource(ResourceImpl projityResource,Resource mpxResource) {
		mpxResource.setName(removeInvalidChars(projityResource.getName()));
		mpxResource.setNotes(removeInvalidChars(projityResource.getNotes()));
		mpxResource.setAccrueAt(AccrueType.getInstance(projityResource.getAccrueAt()));
		mpxResource
				.setCostPerUse(new Double(projityResource.getCostPerUse()));
		mpxResource.setStandardRate(toMPXRate(projityResource
				.getStandardRate()));
		mpxResource.setOvertimeRate(toMPXRate(projityResource
				.getOvertimeRate()));
		//TODO set calendar
		mpxResource.setGroup(projityResource.getGroup());
		mpxResource.setEmailAddress(projityResource.getEmailAddress());
		mpxResource.setIsGeneric(projityResource.isGeneric()); // fix for 2024492

		mpxResource.setInitials(projityResource.getInitials());
		mpxResource.setID((int)projityResource.getId());
		long uid = projityResource.getExternalId(); // try using external id of one set
		if (uid <= 0)
			uid = projityResource.getId();
		mpxResource.setUniqueID((int)uid); // note using id and not unique id
		mpxResource.setMaxUnits(projityResource.getMaximumUnits()*100);

		WorkingCalendar projityCalendar = (WorkingCalendar)projityResource.getWorkCalendar();
		if (projityCalendar != null) { // there should be a calendar, except for the unassigned instance
			ProjectCalendar mpxCalendar = null;
			try {
				mpxCalendar = mpxResource.addResourceCalendar();
			} catch (MPXJException e) {
				e.printStackTrace();
				return;
			}
				toMpxCalendar(projityCalendar,mpxCalendar);
		}
		//TODO The follwing only work because the UID of the resource is the id and not the unique id. A big unique id value  overflows the UID element of the custom field.  It works
		// here because the id is small
		toMpxCustomFields(projityResource.getCustomFields(),mpxResource, CustomFieldsMapper.getInstance().resourceMaps);


	}


	public static void toMpxCustomFields(CustomFields projityFields,FieldContainer mpx, CustomFieldsMapper.Maps maps) {
		for (int i = 0; i < maps.costMap.length; i++) {
			double cost = projityFields.getCustomCost(i);
			if (cost != 0.0D)
				mpx.set(maps.costMap[i],new Double(cost));
		}
		for (int i = 0; i < maps.dateMap.length; i++) {
			long d = projityFields.getCustomDate(i);
			if (d != 0)
				mpx.set(maps.dateMap[i],new Date(d));
		}
		for (int i = 0; i < maps.durationMap.length; i++) {
			long d = projityFields.getCustomDuration(i);
			if (Duration.millis(d) != 0)
				mpx.set(maps.durationMap[i],toMPXDuration(d));
		}
		for (int i = 0; i < maps.finishMap.length; i++) {
			long d = projityFields.getCustomFinish(i);
			if (d != 0)
				mpx.set(maps.finishMap[i],new Date(d));
		}
		for (int i = 0; i < maps.flagMap.length; i++) {
			boolean b = projityFields.getCustomFlag(i);
			if (b == true)
				mpx.set(maps.flagMap[i],Boolean.TRUE);
		}
		for (int i = 0; i < maps.numberMap.length; i++) {
			double n = projityFields.getCustomNumber(i);
			if (n != 0.0D)
				mpx.set(maps.numberMap[i],new Double(n));
		}
		for (int i = 0; i < maps.startMap.length; i++) {
			long d = projityFields.getCustomStart(i);
			if (d != 0)
				mpx.set(maps.startMap[i],new Date(d));
		}
		for (int i = 0; i < maps.textMap.length; i++) {
			String s = projityFields.getCustomText(i);
			if (s != null) {
				mpx.set(maps.textMap[i],MPXConverter.removeInvalidChars(s));
			}
		}
	}

	public static void toMPXAssignment(Assignment assignment, ResourceAssignment mpxAssignment) {
//		long work = assignment.isDefault() ? 0 : assignment.getWork(null); // microsoft considers no work on default assignments
		long work = assignment.getWork(null); // microsoft considers no work on default assignments
    	mpxAssignment.setWork(MPXConverter.toMPXDuration(work));
    	mpxAssignment.setUnits(MathUtils.roundToDecentPrecision(assignment.getUnits()*100.0D));
    	mpxAssignment.setRemainingWork(MPXConverter.toMPXDuration(assignment.getRemainingWork())); //2007
    	long delay = Duration.millis(assignment.getDelay());
    	if (delay != 0) {
    		// mpxj uses default options when dealing with assignment delay
    		CalendarOption oldOptions = CalendarOption.getInstance();
    		CalendarOption.setInstance(CalendarOption.getDefaultInstance());

        	mpxAssignment.setDelay(MPXConverter.toMPXDuration(assignment.getDelay()));
            CalendarOption.setInstance(oldOptions);
    	}

    	long levelingDelay = Duration.millis(assignment.getLevelingDelay());
    	if (levelingDelay != 0) {
    		// mpxj uses default options when dealing with assignment delay
    		CalendarOption oldOptions = CalendarOption.getInstance();
    		CalendarOption.setInstance(CalendarOption.getDefaultInstance());

        	mpxAssignment.setDelay(MPXConverter.toMPXDuration(assignment.getLevelingDelay()));
            CalendarOption.setInstance(oldOptions);
    	}


    	mpxAssignment.setWorkContour(WorkContour.getInstance(assignment.getWorkContourType()));


	}
private static int autoId = 0;



	public static String removeInvalidChars(String in) { // had case of user with newlines in task names
		if (in == null)
			return null;
		StringBuffer inBuf = new StringBuffer(in);
		for (int i = 0; i <inBuf.length(); i++) {
			char c = inBuf.charAt(i);
			if (c == '\r' || c == '\n' || c == '\t') // using escape chars of the form &#x0000; is not good - they show up in MSP literally. MSP doesn't seem to support newlines anyway
				inBuf.setCharAt(i,' ');
		}
		return inBuf.toString();

	}
	public static  void toMPXTask(NormalTask projityTask, Task mpxTask) {
		mpxTask.setName(removeInvalidChars(projityTask.getName()));
		if (projityTask.getWbs() != null)
			mpxTask.setWBS(removeInvalidChars(projityTask.getWbs()));
		mpxTask.setNotes(removeInvalidChars(projityTask.getNotes()));
		mpxTask.setID((int)projityTask.getId());
		mpxTask.setUniqueID((int)projityTask.getId()); // note using id for unique id
		mpxTask.setCreateDate(projityTask.getCreated());
		mpxTask.setDuration(toMPXDuration(projityTask.getDuration())); // set duration without controls
		mpxTask.setStart(DateTime.fromGmt(new Date(projityTask.getStart())));
		mpxTask.setFinish(DateTime.fromGmt(new Date(projityTask.getEnd())));
		mpxTask.setCritical(new Boolean(projityTask.isCritical()));
		mpxTask.setEstimated(projityTask.isEstimated());
		mpxTask.setEffortDriven(projityTask.isEffortDriven());
		mpxTask.setType(TaskType.getInstance(projityTask.getSchedulingType()));
		mpxTask.setConstraintType(ConstraintType.getInstance(projityTask.getConstraintType()));
		mpxTask.setConstraintDate(DateTime.fromGmt(new Date(projityTask.getConstraintDate())));
		mpxTask.setPriority(Priority.getInstance(projityTask.getPriority()));
		mpxTask.setFixedCost(projityTask.getFixedCost());
		mpxTask.setFixedCostAccrual(AccrueType.getInstance(projityTask.getFixedCostAccrual()));
		mpxTask.setMilestone(projityTask.isMarkTaskAsMilestone());
//		mpxTask.setPercentageComplete(projityTask.getPercentComplete()/100.0D);
		mpxTask.setLevelingDelay(toMPXDuration(projityTask.getLevelingDelay()));
		if (projityTask.getDeadline() != 0)
			mpxTask.setDeadline(DateTime.fromGmt(new Date(projityTask.getDeadline())));
		mpxTask.setEarnedValueMethod(EarnedValueMethod.getInstance(projityTask.getEarnedValueMethod()));
		mpxTask.setIgnoreResourceCalendar(projityTask.isIgnoreResourceCalendar());

		//2007
		mpxTask.setTotalSlack(toMPXDuration(projityTask.getTotalSlack()));
		mpxTask.setRemainingDuration(toMPXDuration(projityTask.getRemainingDuration()));
		if (projityTask.getStop() != 0)
			mpxTask.setStop(DateTime.fromGmt(new Date(projityTask.getStop())));
//		if (projityTask.getResume() != 0)
//			mpxTask.setResume(DateTime.fromGmt(new Date(projityTask.getResume())));

		WorkCalendar cal = projityTask.getWorkCalendar();

		if (cal != null)
			mpxTask.setCalendar(ImportedCalendarService.getInstance().findExportedCalendar(cal));

//	Not needed - it will be set when hierarchy is done		mpxTask.setOutlineLevel(new Integer(projityTask.getOutlineLevel()));

		toMpxCustomFields(projityTask.getCustomFields(),mpxTask, CustomFieldsMapper.getInstance().taskMaps);
	}

	public static void toMPXVoid(VoidNodeImpl projityVoid, Task mpxTask) {
		mpxTask.setID((int)projityVoid.getId());
		mpxTask.setUniqueID((int)projityVoid.getId());
		mpxTask.setNull(true);
		// below is for mpxj 2007. These values need to be set
		mpxTask.setCritical(false);
		mpxTask.setTotalSlack(toMPXDuration(0));

	}







	public static net.sf.mpxj.Rate toMPXRate(Rate rate) {
		double value = rate.getValue() * Duration.timeUnitFactor(rate.getTimeUnit());
		return new net.sf.mpxj.Rate(value,TimeUnit.getInstance(rate.getTimeUnit()));
	}

	public static net.sf.mpxj.Duration toMPXDuration(long duration) {
		return net.sf.mpxj.Duration.getInstance(Duration.getValue(duration),TimeUnit.getInstance(Duration.getType(duration)));
		//TODO put the correct formula
	}
	public static final String dateToXMLString(long time) {
	    Calendar date = DatatypeConverter.printDate(new Date(time));
	  //TODO claur - find replacement. Not working anymore and not good anyway
//	    String result = com.sun.msv.datatype.xsd.DateTimeType.theInstance.serializeJavaObject(date, null); 
	    String result=(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")).format(date.getTime()); 
	    //claur
	    //"yyyy-MM-dd'T'HH:mm:ssZ" return a non compatible format +0200 instead of +02:00 for example. colon is missing. So adding this manually...
	    String timezone = (new SimpleDateFormat("Z")).format(date.getTime());
	    result+=timezone.substring(0, 3) + ":" + timezone.substring(3);
		return result;
	}
	private static String truncName(String name) {
		if (name == null)
			return null;
		if (name.length() > nameFieldWidth)
			name = name.substring(0,nameFieldWidth);
		return name;
	}
}
