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
package com.projity.pm.calendar;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.projity.datatype.Duration;
import com.projity.pm.criticalpath.CriticalPath;
import com.projity.server.access.ErrorLogger;
import com.projity.util.DateTime;

/**
 * This class holds specific calendar informatin either for a base calendar or a concrete one, as well as date math functions
 */
public class CalendarDefinition implements WorkCalendar, Cloneable {
	static final long serialVersionUID = 73883742020831L;
	TreeSet dayExceptions = null;
	WorkDay[] exceptions = null;
	WorkWeek week = new WorkWeek();
	protected long id=-1L;

	/**
	 *
	 */
	public CalendarDefinition() {
		super();
		dayExceptions = new TreeSet();
		// TODO Auto-generated constructor stub
	}

	public CalendarDefinition(CalendarDefinition base, CalendarDefinition differences) {
		if (base == null) {
			week = new WorkWeek();
		} else {
			week = (WorkWeek)  base.week.clone(); // copy the week days
		}
		week.addDaysFrom(differences.week); // Now replace any special weekdays

		dayExceptions = (TreeSet) differences.dayExceptions.clone(); // copy from differences
		if (base != null)
			dayExceptions.addAll( base.dayExceptions); // add in base days. If day is already present it will not be added
		addSentinelsAndMakeArray();

		if (!testValid())
			System.out.println("calendar is invalid " + this.getName());
	}

	public boolean testValid() {
		if (week == null)
			return false;
		for (int i = 0; i < 7; i++)
			if (week.getWeekDay(i) == null)
				return false;
		return true;

	}
	void addSentinelsAndMakeArray() {
		// Add endpoint sentinels.  This facilitates algorithms which will no longer need to check for boundary conditions
		dayExceptions.add(WorkDay.MINIMUM);
		dayExceptions.add(WorkDay.MAXIMUM);
		exceptions = new WorkDay[dayExceptions.size()];
		dayExceptions.toArray(exceptions);

	}
	public WorkDay[] getExceptions() {
		return exceptions;
	}

	public WorkDay getWeekDay(int d) {
		return week.getWeekDay(d);
	}
	void addOrReplaceException(WorkDay exceptionDay) {
		dayExceptions.remove(exceptionDay); // remove any existing
		dayExceptions.add(exceptionDay);
		exceptions = new WorkDay[dayExceptions.size()];
		dayExceptions.toArray(exceptions);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		CalendarDefinition newOne = (CalendarDefinition) super.clone();
		newOne.week = (WorkWeek) week.clone();
		newOne.dayExceptions = new TreeSet();

		Iterator i = dayExceptions.iterator();
		while (i.hasNext())
			newOne.dayExceptions.add(((WorkDay)i.next()).clone());
		return newOne;
	}


	/**
	 * This method adjusts the given time to a working time in the calendar.
	 * The algorithm just subtracts a tick and adds it back for sooner or vice versa for later
	 * @param date
	 * @param useSooner
	 * @return
	 */
	public long adjustInsideCalendar(long date, boolean useSooner) {
		long result;
		if (date < 0) {
			date = -date;
			useSooner = !useSooner;
		}
		if (useSooner) {
			long backOne = add(date,-MILLIS_IN_MINUTE,useSooner);
			result =add(backOne,MILLIS_IN_MINUTE,useSooner);
		} else {
			long aheadOne = add(date,MILLIS_IN_MINUTE,useSooner);
			result =add(aheadOne,-MILLIS_IN_MINUTE,useSooner);
		}
		return result;
	}



	/**
	 * Algorithm to add a duration to a date.  This code MUST be very fast as it is the most executed code in the program.
	 * The time required by the algorithm is determined by the number of exceptions encountered and not the duration itself.
	 * To handle reverse scheduling, the date can be negative.  In this case, the date is converted to a positive value, but the duration
	 * is negated.
	 */
	public long add(long date, long duration, boolean useSooner) {
		if (date == 0) // don't bother treating null dates since they will never be valid for calculations
			return 0;
		long result = date;
		boolean forward = true;
		boolean negative = date < 0;
		boolean elapsed = Duration.isElapsed(duration);
		duration = Duration.millis(duration);

		if (negative) {
			date = -date;
			duration = -duration;
			useSooner = !useSooner;
			if (duration == 0)
				forward = false;
		}




		if (elapsed) { // elapsed times do not use calendars, though the result must fall within working time
			result = adjustInsideCalendar(date + duration,useSooner);
		} else {
			if (duration < 0) {
				forward = false;
				duration = -duration;
			}
			//TODO move current day into iterator for speed
			CalendarIterator iterator = CalendarIteratorFactory.getInstance(); // use object pool for speed
			long currentDay = iterator.dayOf(date);
			iterator.initialize(this,forward,currentDay);
			WorkingHours current = iterator.getNext(currentDay);
			duration -= current.calcWorkTime(iterator.timeOf(date),forward);// handle the first day

			long numWeeks;

	/*
	 * First, do a "rough tuning" to get within a week of destination day.  This part of the algorithm will
	 * see how many weeks there are in the duration, subtract off the normal working time for a week for each week.
	 * and position the day correctly.  It then adjusts the duration based on any exception days during those weeks.
	 * It is possible, if there are many exceptions, that after adjusting for exception days, there are still weeks of
	 * work left.  That is why this is called in a loop.
	 */


			int weekTries = 0;			// in rare cases, the exception value can increase, so abort if so
			long weekDuration = week.getDuration();
			while ((numWeeks = (duration / weekDuration)) != 0) {
				if (weekTries++ == 4) // most likely it's increasing. give up and do remaining day by day
					break;
				currentDay = iterator.nextDay(currentDay); // move to next day, first is done
				currentDay = iterator.moveNumberOfDays((int) (WorkWeek.DAYS_IN_WEEK * (forward ? numWeeks : -numWeeks)),currentDay);
				duration -= (numWeeks * weekDuration); // subtract off fixed duration
				duration -= iterator.exceptionDurationDifference(currentDay); // subtract off difference.

				if (duration <= 0) { // if exceptions cause too much duration, then go back in other direction
					iterator.reverseDirection();
					duration = -duration;
					forward = !forward; // todo is this necessary?
				}
				else //TODO verify that this should be in else.
					currentDay = iterator.prevDay(currentDay); // move back a day for fine tuning which adds it back

			}
//
//
	/*
	 * This part of the algorithm is the fine tuning.  It does through the remaining deays and treats them one by one.
	 * Because of the week treatment above, this is guaranteed to go through 6 days at the most.
	 */		while (duration >= 0) { // add in days until we go exactly on the spot or past it
				if (duration == 0 && (forward == useSooner))
					break;
				currentDay = iterator.nextDay(currentDay);
				current = iterator.getNext(currentDay);
				duration -= current.getDuration(); // use exception day
			}
			// Handle the last day
	 		long time = -1;
	 		while (true)  {
				if (forward) {
					time = current.calcTimeAtRemainingWork(-duration);
				}else
					time = current.calcTimeAtWork(-duration);
				if (time != -1)
					break;
				currentDay = iterator.nextDay(currentDay);
				current = iterator.getNext(currentDay);
	 		};
			result = currentDay + time;

	 		CalendarIteratorFactory.recycle(iterator); //No longer using iterator, return it to pool
		}

		// if input was negative time, return a negative value
		if (negative)
			result = -result;
		return result;
	}


	/**
	 * Get difference of two dates: laterDate - earlierDate according to calendar
	 */
	public long compare(long laterDate, long earlierDate, boolean elapsed) {
		boolean negative = laterDate < 0;
		if (negative) {
			laterDate = -laterDate;
			earlierDate = -earlierDate;
		}

		if (elapsed) { // if the desired duration is elapsed time, then just to a simple subtraction
			return laterDate - earlierDate;
		}


		// if later is before earlier swap the dates.  The value of swap is tested later and sign is reversed if it is used
		long swap = 0;
		if (laterDate < earlierDate) {
			swap = earlierDate;
			earlierDate = laterDate;
			laterDate = swap;

		}
		if (earlierDate == 0) // degenerate case.  A 0 date means undefined, so don't process it
			return laterDate;

		CalendarIterator iterator = CalendarIteratorFactory.getInstance(); // use object pool for speed
		long earlierDay = iterator.dayOf(earlierDate);
		long laterDay = iterator.dayOf(laterDate);
		long currentDay;
		iterator.initialize(this,true,earlierDay);
		WorkingHours current = iterator.getNext(earlierDay);
		long duration = 0;


		// Algo starts here
		// treat start day
		duration += current.calcWorkTimeAfter(iterator.timeOf(earlierDate));
		currentDay = iterator.nextDay(earlierDay); // move to next day, first is done

		/*
		 * First add in weeks, adjusting for exception days
		 */
		long numWeeks = (iterator.dayOf(laterDate) - currentDay) / WorkWeek.MS_IN_WEEK ;
		if (numWeeks != 0) {
			currentDay = iterator.moveNumberOfDays((int) (WorkWeek.DAYS_IN_WEEK * numWeeks),currentDay);
			duration += numWeeks * week.getDuration(); // add on normal working duration
			duration += iterator.exceptionDurationDifference(currentDay); // add difference.
		}

		// treat remaining middle days  (no more than 6) and the end day
		for (; currentDay <= laterDay; currentDay = iterator.nextDay(currentDay)) {
			current = iterator.getNext(currentDay);
			duration += current.getDuration();
		}

		// subtract out part of the end day that is later then laterDate
		duration -= current.calcWorkTimeAfter(iterator.timeOf(laterDate));

		CalendarIteratorFactory.recycle(iterator);
		if (negative)
			duration = -duration;
		return (swap == 0) ? duration : - duration; // swap == 0 implies that no swap was done since early date had to be minimum
	}


/**
 * This class manages a pool of calendar iterators.
 *
 */	private static class CalendarIteratorFactory extends BasePoolableObjectFactory {
		private static GenericObjectPool pool =  new GenericObjectPool(new CalendarIteratorFactory());
		public Object makeObject(){ //claur
			return new CalendarIterator();
		}
		public static CalendarIterator getInstance() {
			try {
				return (CalendarIterator) pool.borrowObject();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public static void recycle(CalendarIterator object) {
			try {
				pool.returnObject(object);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

/**
 * This class is an iterator which is used to return week days or exception days
 *
 */
 	private static class CalendarIterator {
		WorkDay[] exceptions;
		WorkWeek week;
		Calendar scratchDate; // will get reused since this class is recycled

		long exceptionDay;
		int i;
		boolean forward;
		int step;


		private CalendarIterator() {
			scratchDate = DateTime.calendarInstance(); // will get reused since this class is recycled
		}
		/**
		 *
		 */
		private void reverseDirection() {
			if (forward) {
				i -=1;
			} else {
				i += 1;
			}
			step = -step;
			exceptionDay = exceptions[i].getStart();
			forward = !forward;
		}

		private static SimpleDateFormat f = DateTime.dateFormatInstance();


		public long dayOf(long date) {
			scratchDate.setTimeInMillis(date);
			scratchDate.set(Calendar.HOUR_OF_DAY,0);
			scratchDate.set(Calendar.MINUTE,0);
			scratchDate.set(Calendar.SECOND,0); // Fixed rounding bug as we now go to seconds 8/2/07
//			scratchDate.set(Calendar.MILLISECOND,0);
			return scratchDate.getTimeInMillis();
		}

		public long timeOf(long date) {
			return date - dayOf(date);
		}


		private void initialize(CalendarDefinition cal, boolean forward, long day) {
			exceptions = cal.exceptions;
			week = cal.week;
			this.forward = forward;
			scratchDate.setTimeInMillis(day);
			try {
				DateUtils.truncate(scratchDate,Calendar.DATE);
			} catch (Exception e) {
				ErrorLogger.logOnce("hugedate", "date value is garbage " + scratchDate + "\n" + CriticalPath.getTrace(), e);
			}
			step = (forward) ? 1 : -1;
			i = Arrays.binarySearch(exceptions, scratchDate);
			if (i < 0) {// First day not found
				i = -i-1; // set index for the future
				if (!forward)
					i -= 1;
			}
			exceptionDay = exceptions[i].getStart();

		}

		public String dump() {
			String result = "CalendarIterator ";
			result += "weekdays\n";
			for (int i = 0; i < 7; i++) {
				result += "day[" + i +"]" + week.getWeekDay(i) +  "\n";
			}
			result += "There are " + exceptions.length + " exceptions\n";
			for (int j = 0; j < exceptions.length; j++) {
				result += "exception" + exceptions[j].toString();
			}
			return result;

		}
		
		private WorkingHours getNext(long day) {
			WorkDay workDay;
			if (day == exceptionDay) {
				workDay = exceptions[i]; // move index, save off new value for exception day
				i += step;
				if (i < 0 || i == exceptions.length) {//TODO
					System.out.println("invalid calendar iterator - index is negative or past bounds. avoiding");
					ErrorLogger.logOnce("CalendarIterator","invalid calendar iterator i=" + i + "\n" + CriticalPath.getTrace(),null);
				} else
					exceptionDay = exceptions[i].getStart(); // move index, save off new value for exception day
			} else {
				workDay = week.getWeekDay(dayOfWeek(day));
			}
			
			if (workDay==null)
				workDay=WorkDay.getDefaultWorkDay();
			
			return workDay.getWorkingHours();
		}

		private long exceptionDurationDifference(long endDay) {
			long difference = 0;
			if (exceptions.length == 2) // skip sentinels
				return 0;
			while ((forward && exceptionDay < endDay) || (!forward && exceptionDay > endDay)) {
				difference -= week.getWeekDay(dayOfWeek(exceptionDay)).getDuration();
				difference += exceptions[i].getDuration();
				i += step;
				if (i < 0 || i >= exceptions.length) {
//					System.out.println("error");
					break; // added april 30 2008 hk
				}
				exceptionDay = exceptions[i].getStart();
			}
			return difference;

		}

		private int dayOfWeek(long day) {
			scratchDate.setTimeInMillis(day);
			return scratchDate.get(Calendar.DAY_OF_WEEK) -1 ;

		}
		private long moveNumberOfDays(int numberOfDays, long fromDay) {
			scratchDate.setTimeInMillis(fromDay);
			scratchDate.add(Calendar.DATE,numberOfDays);
			return scratchDate.getTimeInMillis();
		}

		private long nextDay(long day) {
			scratchDate.setTimeInMillis(day);
			scratchDate.add(Calendar.DATE,forward ? 1 : -1);
			return scratchDate.getTimeInMillis();
		}
		private long prevDay(long day) {
			scratchDate.setTimeInMillis(day);
			scratchDate.add(Calendar.DATE,forward ? -1 : 1);
			return scratchDate.getTimeInMillis();
		}

	}

	/* (non-Javadoc)
	 * @see com.projity.configuration.NamedItem#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.projity.configuration.NamedItem#getCategory()
	 */
	public String getCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.time.WorkCalendar#setName(java.lang.String)
	 */
	public void setName(String name) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.WorkCalendar#getConcreteInstance()
	 */
	public CalendarDefinition getConcreteInstance() {
		return this; // doesn't make sense to call this
	}
	public static final int getDayOfWeek(long date) {
		Calendar scratchDate = DateTime.calendarInstance();
		scratchDate.setTimeInMillis(date);
		return scratchDate.get(Calendar.DAY_OF_WEEK) -1 ;
	}


	public final WorkDay getWorkDay(long date) {
		WorkDay workDay = null;
		int i = Arrays.binarySearch(getConcreteInstance().exceptions, new Date(date));
		if (i >= 0) {
			workDay =  exceptions[i];
		} else {
			workDay = week.getWeekDay(getDayOfWeek(date));
		}
		return workDay;
	}

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getUniqueId() {
        return id;
    }
    public void setUniqueId(long id) {
        this.id = id;
    }
    transient boolean newId=true;
    public boolean isNew(){
    	return newId;
    }
    public void setNew(boolean newId){
    	this.newId=newId;
    }
	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.WorkCalendar#getBaseCalendar()
	 */
	public WorkCalendar getBaseCalendar() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.WorkCalendar#dependsOn(com.projity.pm.calendar.WorkCalendar)
	 */
	public boolean dependsOn(WorkCalendar cal) {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.WorkCalendar#invalidate()
	 */
	public void invalidate() {
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.WorkCalendar#isInvalid()
	 */
	public boolean isInvalid() {
		return false;
	}


	CalendarDefinition intersectWith(CalendarDefinition other) throws InvalidCalendarIntersectionException {
		CalendarDefinition result = new CalendarDefinition();
		result.week = week.intersectWith(other.week);

		WorkDay exceptionDay;
		// merge exceptions
		for (int i = 0; i < exceptions.length; i++) {
			exceptionDay = exceptions[i];
			result.dayExceptions.add(exceptionDay.intersectWith(other.getWorkDay(exceptionDay.getStart())));
		}
		for (int i = 0; i < other.exceptions.length; i++) {
			exceptionDay = other.exceptions[i];
			result.dayExceptions.add(exceptionDay.intersectWith(getWorkDay(exceptionDay.getStart())));
		}
		result.addSentinelsAndMakeArray();
		return result;
	}

	private transient boolean dirty;
	public boolean isDirty() {
		return dirty;
	}
	public void setDirty(boolean dirty) {
		//System.out.println("CalendarDefinition _setDirty("+dirty+"): "+getName());
		this.dirty = dirty;
	}

	public String dump() {
		String result = "Calendar " + getName() + "\n";
		result += "weekdays\n";
		for (int i = 0; i < 7; i++) {
			result += "day[" + i +"]" + getWeekDay(i) +  "\n";
		}
		result += "There are " + exceptions.length + " exceptions\n";
		for (int j = 0; j < exceptions.length; j++) {
			result += "exception" + exceptions[j].toString();
		}
		return result;

	}

}
