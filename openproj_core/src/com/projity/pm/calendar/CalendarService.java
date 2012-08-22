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
package com.projity.pm.calendar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.projity.configuration.Settings;
import com.projity.document.Document;
import com.projity.pm.time.HasStartAndEnd;
import com.projity.strings.Messages;
import com.projity.timescale.CalendarUtil;

/**
 * Facade for manipulating calendars via a dialog or web interface
 */
public class CalendarService {
	private static CalendarService instance = null;
	ArrayList baseCalendars = new ArrayList();
	ArrayList derivedCalendars = new ArrayList();
	ArrayList assignmentCalendars = new ArrayList();

	public static CalendarService getInstance() {
		if (instance == null)
			instance = new CalendarService();
		return instance;
	}
	/**
	 * Gets the name of a calendar
	 *
	 * @param cal
	 *            Calendar whose name to return
	 * @return name of calendar or "" if cal is null
	 */
	public String getCalendarName(WorkingCalendar cal) {
		if (cal == null)
			return "";
		return cal.getName();
	}


	/**
	 * Get the base calendar for a given calendar
	 *
	 * @param cal
	 *            whose based to get
	 * @return base calendar or null if cal has no base
	 */
	public WorkingCalendar getBaseCalendar(WorkingCalendar cal) {
		return (WorkingCalendar) cal.getBaseCalendar();
	}

	/**
	 * Get a descriptor for the day (its hours and modified status). The
	 * priority order what day is return is: 1. Derived calendar month day
	 * exception 2. Derived calendar week day exception 3. Base calendar special
	 * month day 4. Base calendar week day
	 *
	 * @param workingCalendar
	 *            The calendar to use
	 * @param date
	 *            Midnight on the day in question (see DateTime.midnightToday())
	 * @return DayDescriptor with modified info and work day
	 */
	public DayDescriptor getDay(WorkingCalendar workingCalendar, long date) {
		return workingCalendar.getMonthDayDescriptor(date);
	}
	/**
	 *
	 * @param workingCalendar
	 * @param intervals
	 * @return Common day or null
	 */
	public DayDescriptor getDay(WorkingCalendar workingCalendar, Set intervals, boolean selectedWeekDays[]){
		CalendarUtil.DayIterator days=new CalendarUtil.DayIterator();
	    DayDescriptor common=null;
	    DayDescriptor current;

		for (int i = 0; i < 7; i++) {
			if (selectedWeekDays[i]) {
				current = workingCalendar.getWeekDayDescriptor(i+1);
		        common = mergeWithCommon(current,common);
		        if (common == null)
		        	return null;
		    }
		}

	    for (Iterator i=intervals.iterator();i.hasNext();){
		    days.setInterval((HasStartAndEnd)i.next());
		    while(days.hasMoreDays()){
		        current=workingCalendar.getMonthDayDescriptor(days.nextDay());
		        common = mergeWithCommon(current,common);
		        if (common == null)
		        	return null;
		    }
		}
	    return common;
	}

	private DayDescriptor mergeWithCommon(DayDescriptor current, DayDescriptor common) {
        if (common==null){
            common=current;
            return common;
        }
        if (current.isModified()!=common.isModified()||
                current.isWorking()!=common.isWorking())
            return null;
        // compare working hours
        if (current.isModified()&&current.isWorking()){
            // to avoid breaking code with redefining equals in DayDescriptor...
            if (current.getWorkingHours()==null&&common.getWorkingHours()==null)
                return null;
            if ((current.getWorkingHours()==null&&common.getWorkingHours()!=null)||
                    (current.getWorkingHours()!=null&&common.getWorkingHours()==null))
                return null;
            List currentIntervals=current.getWorkingHours().getIntervals();
            List commonIntervals=common.getWorkingHours().getIntervals();
            if (commonIntervals.size()!=currentIntervals.size()) return null;
            Iterator com=commonIntervals.iterator();
            Iterator cur=currentIntervals.iterator();
            HasStartAndEnd comInterval,curInterval;
            while (com.hasNext()){
                comInterval=(HasStartAndEnd)com.next();
                curInterval=(HasStartAndEnd)cur.next();
                if (curInterval==null&&comInterval==null) continue;
                if (curInterval==null&&comInterval!=null) return null;
                if (curInterval!=null&&comInterval==null) return null;
                if (comInterval.getStart()!=curInterval.getStart()||comInterval.getEnd()!=curInterval.getEnd())
                    return null;
            }
        }
        return common;
	}
	/**
	 * Get a descriptor for the week day (its hours and modified status). If the
	 * derived calendar does not have a special weekday defined, then the base
	 * calendar weekday is used
	 *
	 * @param workingCalendar
	 *            The calendar to use
	 * @param dayNum
	 *            Corresponds to constants in java.util.Calendar SUNDAY=1,
	 *            MONDAY=2... SATURDAY=7
	 * @return DayDescriptor with modified info and work day
	 */
	public DayDescriptor getWeekDay(WorkingCalendar workingCalendar, int dayNum) {
		return workingCalendar.getWeekDayDescriptor(dayNum);
	}

	/**
	 * Clear out any month day exceptions for the given date
	 *
	 * @param workingCalendar
	 * @param date
	 */
	public void makeDefaultDay(WorkingCalendar workingCalendar, long date) {
		workingCalendar.makeDefaultDay(date);
	}

	/**
	 *
	 * @param workingCalendar
	 * @param intervals
	 *            with no overlap
	 * @return
	 */
	public void makeDefaultDays(WorkingCalendar workingCalendar, Set intervals,boolean[] selectedDays) {
		for (int i = 0; i < 7; i++) {
			if (selectedDays[i]) {
				makeDefaultWeekDay(workingCalendar, i+1);
			}
		}


		CalendarUtil.DayIterator days=new CalendarUtil.DayIterator();
	    for (Iterator i=intervals.iterator();i.hasNext();){
		    days.setInterval((HasStartAndEnd)i.next());
		    while(days.hasMoreDays())
		        workingCalendar.makeDefaultDay(days.nextDay());
		}
	}

	/**
	 * Clear out any week day exceptions for the week day
	 *
	 * @param workingCalendar
	 * @param dayNum
	 *            Corresponds to constants in java.util.Calendar SUNDAY=1,
	 *            MONDAY=2... SATURDAY=7
	 */
	public void makeDefaultWeekDay(WorkingCalendar workingCalendar, int dayNum) {
		workingCalendar.makeDefaultWeekDay(dayNum);
	}

	/**
	 * Set the working hours for a day. Working hours should be filled as
	 * follows: An existing WorkingHours should be cloned before modification in
	 * case the modification causes an exception. WorkingHours is modified by
	 * calls to WorkingHours.setInterval(int number, long start, long end) See
	 * comments in this method as to what values to use (must be on the date
	 * 1/1/70 or -1 if null)
	 *
	 * @param workingCalendar -
	 *            Calendar whose day to set
	 * @param date -
	 *            date of the exception
	 * @param workingHours -
	 *            contains the work ranges
	 * @throws WorkRangeException -
	 *             If invalid ranges, an exception is thrown
	 */
	public void setDayWorkingHours(WorkingCalendar workingCalendar, long date, WorkingHours workingHours) throws WorkRangeException{
		if (workingHours.getDuration() == 0)
			setDayNonWorking(workingCalendar,date);
		else
			workingCalendar.setDayWorkingHours(date,workingHours);
	}

	public void setDaysWorkingHours(WorkingCalendar workingCalendar, Set intervals, boolean selectedDays[], WorkingHours workingHours)  throws WorkRangeException, InvalidCalendarException{
		for (int i = 0; i < 7; i++) {
			if (selectedDays[i]) {
				setWeekDayWorkingHours(workingCalendar, i+1,workingHours);
			}
		}

		CalendarUtil.DayIterator days=new CalendarUtil.DayIterator();
	    for (Iterator i=intervals.iterator();i.hasNext();){
		    days.setInterval((HasStartAndEnd)i.next());
		    while(days.hasMoreDays())
		        workingCalendar.setDayWorkingHours(days.nextDay(),workingHours);
		}
	}


	/**
	 * Set the working hours for a week day
	 *
	 * @param workingCalendar -
	 *            Calendar whose week day to set
	 * @param dayNum
	 *            Corresponds to constants in java.util.Calendar SUNDAY=1,
	 *            MONDAY=2... SATURDAY=7
	 * @param workingHours -
	 *            contains the work ranges
	 * @throws WorkRangeException -
	 *             If invalid ranges, an exception is thrown
	 * @throws InvalidCalendarException
	 */
	public void setWeekDayWorkingHours(WorkingCalendar workingCalendar, int dayNum, WorkingHours workingHours) throws WorkRangeException, InvalidCalendarException{
		if (workingHours.getDuration() == 0)
			setWeekDayNonWorking(workingCalendar,dayNum);
		else
			workingCalendar.setWeekDayWorkingHours(dayNum,workingHours);
	}

	/**
	 * Set a month day as a non working day
	 *
	 * @param workingCalendar
	 * @param date
	 *            what date is not working
	 */
	public void setDayNonWorking(WorkingCalendar workingCalendar, long date) {
		workingCalendar.setDayNonWorking(date);

	}

	public void setDaysNonWorking(WorkingCalendar workingCalendar, Set intervals, boolean[] selectedDays) throws InvalidCalendarException{
		for (int i = 0; i < 7; i++) {
			if (selectedDays[i]) {
				setWeekDayNonWorking(workingCalendar,i+1);
			}
		}
		CalendarUtil.DayIterator days=new CalendarUtil.DayIterator();
	    for (Iterator i=intervals.iterator();i.hasNext();){
		    days.setInterval((HasStartAndEnd)i.next());
		    while(days.hasMoreDays())
		        workingCalendar.setDayNonWorking(days.nextDay());
		}
	}



	/**
	 * Sets a week day as a non working day
	 *
	 * @param workingCalendar
	 * @param dayNum
	 *            Corresponds to constants in java.util.Calendar SUNDAY=1,
	 *            MONDAY=2... SATURDAY=7
	 */
	public void setWeekDayNonWorking(WorkingCalendar workingCalendar, int dayNum) throws InvalidCalendarException {
		int nonWorkingDays = 0;
		for (int i = 0; i < 7; i++) {
			if (!getWeekDay(workingCalendar,i+1).isWorking())
				nonWorkingDays++;
		}
		if (nonWorkingDays >= 6)
			throw new InvalidCalendarException(Messages.getString("Message.errorCalendarMustHaveWorkingDay"));
		workingCalendar.setWeekDayNonWorking(dayNum);
	}

	/**
	 * Assigns an existing calendar with the contents of another. Used when
	 * validating the calendar dialog to copy the days back into the non-scratch
	 * calendar
	 *
	 * @param destination
	 *            Where to copy data
	 * @param source
	 *            Where to copy from
	 */
	public void assignCalendar(WorkingCalendar destination, WorkingCalendar source) {
		destination.assignFrom(source);
	}

	/**
	 * Make a temporary copy of a calendar. Used in the calendar dialog - A
	 * scratch copy should be modified, and only upon validation should the
	 * calendar data be copied back into the original calendar via a call to
	 * assignCalendar
	 *
	 * @param from
	 * @return
	 */
	public WorkingCalendar makeScratchCopy(WorkingCalendar from) {
		return from.makeScratchCopy();
	}


/**
 * For debugging - returns a toString() for a calendar
 *
 * @param cal
 * @return String representation of calendar for debugging
 */
	public String dump(WorkingCalendar cal) {
		return cal.dump();
	}

	public WorkingCalendar getStandardBasedInstance() {
		WorkingCalendar wc = WorkingCalendar.getStandardBasedInstance();
		add(wc);
		return wc;
	}

	public WorkingCalendar getStandardInstance() {
		WorkingCalendar wc = WorkingCalendar.getStandardInstance();
		return wc;
	}

	public WorkingCalendar getDefaultInstance() {
		WorkingCalendar wc = WorkingCalendar.getDefaultInstance();
		return wc;
	}

	public void saveAndUpdate(WorkingCalendar workingCalendar) {
		invalidate(workingCalendar);
	}

	public void invalidate(WorkingCalendar cal) {
		Iterator i = cal.getObjectsUsing().iterator();
		HasCalendar hasCal;
		HashSet documents = new HashSet();
		while (i.hasNext()) {
			hasCal = (HasCalendar)i.next();
			documents.add(hasCal.invalidateCalendar());
		}
		Iterator d = documents.iterator();
		while (d.hasNext()) {
			Document doc = (Document)d.next();
			doc.fireUpdateEvent(this,doc);
		}
	}


//	HashMap importedCalendarMap = new HashMap();
//	public void addImportedCalendar(WorkingCalendar cal, MPXCalendar mpxCal) {
//		importedCalendarMap.put(mpxCal,cal);
//		if (cal.isBaseCalendar()) {
//			if (findBaseCalendar(cal.getName(),true) != null)
//				return;
//// cal.setName(cal.getName() + PLACE_HOLDER_NAME);
//		}
//		add(cal);
//	}
//
//	public WorkCalendar findImportedCalendar(MPXCalendar mpxCal) {
//		return (WorkCalendar) importedCalendarMap.get(mpxCal);
//	}
//	public MPXCalendar findImportedMPXCalendar(String name) {
//		Iterator i = importedCalendarMap.keySet().iterator();
//		MPXCalendar cal;
//		while (i.hasNext()) {
//			cal = (MPXCalendar)i.next();
//			if (cal.getName().equals(name))
//				return cal;
//		}
//		return null;
//	}
//
//	HashMap exportedCalendarMap = new HashMap();
//	public void addExportedCalendar(MPXCalendar mpxCal,WorkingCalendar cal) {
//		exportedCalendarMap.put(cal,mpxCal);
//	}
//
//	public MPXCalendar findExportedCalendar(WorkCalendar cal) {
//		return (MPXCalendar) exportedCalendarMap.get(cal);
//	}

	public void add(WorkingCalendar cal) {
		if (cal.isBaseCalendar()) {
			if (!baseCalendars.contains(cal)) {
				boolean found = findBaseCalendar(cal.getName()) != null;
				if (found)
					return;
				baseCalendars.add(cal);
			}
		} else {
			if (!baseCalendars.contains(cal))
				derivedCalendars.add(cal);
		}
	}
//
//
//	public void invalidate(WorkingCalendar cal) {
//		HashSet set = new HashSet();
//		cal.invalidate();
//		if (cal.isBaseCalendar()) {
//			Iterator i = derivedCalendars.iterator();
//			WorkingCalendar cur;
//			while (i.hasNext()) {
//				cur = (WorkingCalendar)i.next();
//				if (cur.getBaseCalendar() == cal) {
//					cur.invalidate();
//					set.add(cur.getDocument());
//				}
//			}
//		} else {
//			set.add(cal.getDocument());
//		}
//
//		// update all documents with modified calendars
//		Iterator i = set.iterator();
//		Document document;
//		while (i.hasNext()) {
//			document = (Document)i.next();
//			if (document == null)
//				continue;
//			document.fireUpdateEvent(null,cal); // TODO should use correct cal
//												// but for now docs update on
//												// all
//		}
//	}
//
	public final ArrayList getBaseCalendars() {
		return baseCalendars;
	}
	public final ArrayList getDerivedCalendars() {
		return derivedCalendars;
	}

	public static final Object[] allBaseCalendars() {
		return getInstance().getBaseCalendars().toArray();
	}
	public static final WorkCalendar findBaseCalendar(String name) {
		return getInstance().findBaseCalendar(name,false);
	}
	public WorkCalendar findBaseCalendar(String name, boolean importing) {
		if (name == null)
			return null;
		importing = false; // don't do the treatment - if a calendar is found
							// with same name, use it
		// previously, I would create a new calendar for the project one, but
		// this led to an explosion of calendars on repeated imports and exports
		if (importing)
			name = name + PLACE_HOLDER_NAME;
		Iterator i = getInstance().getBaseCalendars().iterator();
		WorkingCalendar current;
		while (i.hasNext()) {
			current = (WorkingCalendar)i.next();
			if (current!=null&&current.getName()!=null&&current.getName().equals(name))
				return current;
		}
		return null;
	}
	public WorkCalendar findBaseCalendar(long id) {
		Iterator i = getInstance().getBaseCalendars().iterator();
		WorkingCalendar current;
		while (i.hasNext()) {
			current = (WorkingCalendar)i.next();
			if (current.getUniqueId() == id)
				return current;
		}
		return null;
	}

//	public WorkCalendar findDocumentCalendar(String name, Document document) {
//		if (name == null)
//			return null;
//		Iterator i = getInstance().getDerivedCalendars().iterator();
//		WorkingCalendar current;
//		while (i.hasNext()) {
//			current = (WorkingCalendar)i.next();
//			if (document == current.getDocument() && current.getName().equals(name))
//				return current;
//		}
//		return null;
//	}

	private static final String PLACE_HOLDER_NAME = "____~";
	public void renameImportedBaseCalendars(String documentName) {
		Iterator i = getInstance().getBaseCalendars().iterator();
		WorkingCalendar current;
		String currentName;
		while (i.hasNext()) {
			current = (WorkingCalendar)i.next();
			currentName = current.getName();
			int spot = currentName.indexOf(PLACE_HOLDER_NAME);
			if (spot == -1)
				continue;
			currentName = toImportedName(currentName.substring(0,spot),documentName);
			current.setName(currentName);
		}

	}

	private static String toImportedName(String calName, String documentName) {
		return calName + Settings.LEFT_BRACKET + documentName + Settings.RIGHT_BRACKET;
	}

	public void reassignCalendar(HasCalendar object, WorkCalendar oldCal, WorkCalendar newCal) {
		if (oldCal == newCal)
			return;
		if (oldCal != null && oldCal instanceof WorkingCalendar)
			((WorkingCalendar)oldCal).removeObjectUsing(object);
		if (newCal != null && newCal instanceof WorkingCalendar)
			((WorkingCalendar)newCal).addObjectUsing(object);
	}

}
