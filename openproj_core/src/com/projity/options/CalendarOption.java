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
package com.projity.options;
import java.util.GregorianCalendar;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.projity.datatype.Duration;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.util.DateTime;

/**
 *
 */
public class CalendarOption implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6714103946319228798L;
	private static CalendarOption instance = null;
	private static CalendarOption defaultInstance = null;
	
	public final boolean isAddedCalendarTimeIsNonStop() {
		return addedCalendarTimeIsNonStop;
	}
	public final void setAddedCalendarTimeIsNonStop(boolean addedCalendarTimeIsNonStop) {
		this.addedCalendarTimeIsNonStop = addedCalendarTimeIsNonStop;
	}
	public static CalendarOption getInstance() {
		if (instance == null)
			instance = new CalendarOption();
		return instance;
	}
	public static CalendarOption getNewInstance() {
		return new CalendarOption();
	}
	public static CalendarOption getDefaultInstance() {
		if (defaultInstance == null)
			defaultInstance = new CalendarOption();
		return defaultInstance;
	}
	// allow setting of options so that default options can be toggled on and off for import export
	public static final void setInstance(CalendarOption instance) {
		CalendarOption.instance = instance;
	}
	private CalendarOption() {
		// note that the mpxj library uses the default values when importing or exporting
		defaultStartTime.set(GregorianCalendar.HOUR_OF_DAY,defaultStartHour);
		defaultStartTime.set(GregorianCalendar.MINUTE,0);
		defaultStartTime.set(GregorianCalendar.SECOND,0);
		defaultStartTime.set(GregorianCalendar.MILLISECOND,0);
		defaultEndTime.set(GregorianCalendar.HOUR_OF_DAY,defaultEndHour);
		defaultEndTime.set(GregorianCalendar.MINUTE,0);
		defaultEndTime.set(GregorianCalendar.SECOND,0);
		defaultEndTime.set(GregorianCalendar.MILLISECOND,0);
		
	}
	
	int weekStartsOn = GregorianCalendar.SUNDAY;
	int fiscalYearStartsIn = GregorianCalendar.JANUARY;
	int defaultStartHour = 8;
	int defaultEndHour = 17;
 
	GregorianCalendar defaultStartTime = DateTime.calendarInstance();
	GregorianCalendar defaultEndTime = DateTime.calendarInstance();
	double hoursPerDay = 8.0D;
	double hoursPerWeek = 40.0D;
	double daysPerMonth = 20.0D;
	
	public double getFractionOfDayThatIsWorking() {
		return hoursPerDay / 24.0;
	}
	long defaultDuration = Duration.setAsEstimated((long) (WorkCalendar.MILLIS_IN_HOUR * hoursPerDay),true);
	// when typing values on a non work day, the day is added to the assignment calendar.  If it is non stop, then a 24 hour exception
	// is used (MSP does this). If false, a default day is used.  The MSP behaviour is the default.  I don't think it is all that logical
	// so i provide the option to use a default day instead

	boolean addedCalendarTimeIsNonStop = false; 
	/**
	 * @return Returns the daysPerMonth.
	 */
	public double getDaysPerMonth() {
		return daysPerMonth;
	}
	/**
	 * @param daysPerMonth The daysPerMonth to set.
	 */
	public void setDaysPerMonth(double daysPerMonth) {
		this.daysPerMonth = daysPerMonth;
	}

	public long getMillisPerDay() {
		return (long) (WorkCalendar.MILLIS_IN_HOUR * hoursPerDay);
	}
	/**
	 * @return Returns the fiscalYearStartsIn.
	 */
	public int getFiscalYearStartsIn() {
		return fiscalYearStartsIn;
	}
	/**
	 * @param fiscalYearStartsIn The fiscalYearStartsIn to set.
	 */
	public void setFiscalYearStartsIn(int fiscalYearStartsIn) {
		this.fiscalYearStartsIn = fiscalYearStartsIn;
	}
	/**
	 * @return Returns the hoursPerDay.
	 */
	public double getHoursPerDay() {
		return hoursPerDay;
	}
	/**
	 * @param hoursPerDay The hoursPerDay to set.
	 */
	public void setHoursPerDay(double hoursPerDay) {
		this.hoursPerDay = hoursPerDay;
		defaultDuration = Duration.setAsEstimated((long) (WorkCalendar.MILLIS_IN_HOUR * hoursPerDay),true);

	}
	/**
	 * @return Returns the hoursPerWeek.
	 */
	public double getHoursPerWeek() {
		return hoursPerWeek;
	}
	/**
	 * @param hoursPerWeek The hoursPerWeek to set.
	 */
	public void setHoursPerWeek(double hoursPerWeek) {
		this.hoursPerWeek = hoursPerWeek;
	}
	/**
	 * @return Returns the weekStartsOn.
	 */
	public int getWeekStartsOn() {
		return weekStartsOn;
	}
	/**
	 * @param weekStartsOn The weekStartsOn to set.
	 */
	public void setWeekStartsOn(int weekStartsOn) {
		this.weekStartsOn = weekStartsOn;
	}
	
	public double hoursPerMonth() {
		return hoursPerDay * daysPerMonth;
	}
	/**
	 * @return Returns the defaultEndTime.
	 */
	public GregorianCalendar getDefaultEndTime() {
		return defaultEndTime;
	}
	/**
	 * @param defaultEndTime The defaultEndTime to set.
	 */
	public void setDefaultEndTime(GregorianCalendar defaultEndTime) {
		this.defaultEndTime = defaultEndTime;
	}
	/**
	 * @return Returns the defaultStartTime.
	 */
	public GregorianCalendar getDefaultStartTime() {
		return defaultStartTime;
	}
	/**
	 * @param defaultStartTime The defaultStartTime to set.
	 */
	public void setDefaultStartTime(GregorianCalendar defaultStartTime) {
		this.defaultStartTime = defaultStartTime;
	}
	
	public long makeValidStart(long start, boolean force) {
		
		start = DateTime.minuteFloor(start);
		GregorianCalendar cal = DateTime.calendarInstance();
		cal.setTimeInMillis(start);
		int year = cal.get(GregorianCalendar.YEAR);
		int dayOfYear = cal.get(GregorianCalendar.DAY_OF_YEAR);
		if (force || cal.get(GregorianCalendar.HOUR_OF_DAY) == 0 && cal.get(GregorianCalendar.MINUTE) == 0) {
			cal.set(GregorianCalendar.HOUR_OF_DAY,getDefaultStartTime().get(GregorianCalendar.HOUR_OF_DAY));
			cal.set(GregorianCalendar.MINUTE,getDefaultStartTime().get(GregorianCalendar.MINUTE));
			cal.set(GregorianCalendar.YEAR,year);
			cal.set(GregorianCalendar.DAY_OF_YEAR,dayOfYear);
		}
		return cal.getTimeInMillis();
	}
		
	public long makeValidEnd(long end, boolean force) {
		end =DateTime.minuteFloor(end);		
		GregorianCalendar cal = DateTime.calendarInstance();
		cal.setTimeInMillis(end);
		if (force || cal.get(GregorianCalendar.HOUR_OF_DAY) == 0 && cal.get(GregorianCalendar.MINUTE) == 0) {
			cal.set(GregorianCalendar.HOUR_OF_DAY,getDefaultEndTime().get(GregorianCalendar.HOUR_OF_DAY));
			cal.set(GregorianCalendar.MINUTE,getDefaultEndTime().get(GregorianCalendar.MINUTE));
		}
		return cal.getTimeInMillis();
	}
	
	
	
	/**
	 * @return Returns the defaultDuration.
	 */
	public long getDefaultDuration() {
		return defaultDuration;
	}
	/**
	 * @param defaultDuration The defaultDuration to set.
	 */
	public void setDefaultDuration(long defaultDuration) {
		this.defaultDuration = defaultDuration;
	}
	public final int getDefaultEndHour() {
		return defaultEndHour;
	}
	public final void setDefaultEndHour(int defaultEndHour) {
		this.defaultEndHour = defaultEndHour;
	}
	public final int getDefaultStartHour() {
		return defaultStartHour;
	}
	public final void setDefaultStartHour(int defaultStartHour) {
		this.defaultStartHour = defaultStartHour;
	}
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
