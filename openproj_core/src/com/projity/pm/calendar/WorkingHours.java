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
package com.projity.pm.calendar;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


import com.projity.configuration.Settings;
import com.projity.strings.Messages;
import com.projity.util.DateTime;

/**
 * 
 */
public class WorkingHours implements Cloneable, Serializable {
	static final long serialVersionUID = 83888849333431L;
	//private static Log log = LogFactory.getLog(WorkingHours.class);
	private static long MS_PER_MINUTE = 60000L;
	/**
	 * milliseconds of work time in the day
	 */
	long duration = 0;

	WorkRange workRange[] = new WorkRange[Settings.CALENDAR_INTERVALS];
	private static GregorianCalendar helper = DateTime.calendarInstance();;
	
	
	public Object clone() {
		WorkingHours newOne = new WorkingHours();
		for (int i = 0; i < workRange.length; i++) {
			if (workRange[i] == null)
				newOne.workRange[i] = null;
			else
				newOne.workRange[i] = (WorkRange) workRange[i].clone();
		}
		newOne.duration = duration;
		return newOne;
	}
	
/** Create a new working hours by intersecting this one's ranges with another's.  Not that it's possible that the result has
 * up to twice as many ranges as normal in a worst case
 * @param other
 * @return
 */	
	WorkingHours intersectWith(WorkingHours other) {
		int thisIndex = 0;
		int otherIndex = 0;
		
		long start;
		long end;
		WorkRange thisRange;
		WorkRange otherRange;
		ArrayList list = new ArrayList();
		for(;;) {
			// check boundary conditions.  if one of the working hours is exhausted, then no more intersection
			if (thisIndex == workRange.length)
				break;
			if (otherIndex == other.workRange.length)
				break;
			
			thisRange = workRange[thisIndex];
			otherRange = other.workRange[otherIndex];
			if (thisRange == null || otherRange == null)
				break;
			
			
			// the start is always the maximum of the current ranges
			start = Math.max(thisRange.getStart(), otherRange.getStart());
			
			// the end is the minimum of the current ranges
			if (thisRange.getEnd() < otherRange.getEnd()) {
				end = thisRange.getEnd();
				thisIndex++;
			} else {
				end = otherRange.getEnd();
				otherIndex++;
			}
			if (end > start) //if the range is not degenerate, then there is an overlap
				try {
					list.add(new WorkRange(start,end));
				} catch (WorkRangeException e) {
					// this should never happen
					e.printStackTrace();
				}
		}
		// make a new working hours and use the work ranges that were generated
		WorkingHours result = new WorkingHours();
		result.workRange = new WorkRange[list.size()];
		list.toArray(result.workRange);
		result.initialize();
		return result;
		
	}
	
	synchronized private static long getHoursAndMinutes(Date date) {
		helper.setTime(date);
		// the date needs to be normalized for GMT.  because can wrap around, use modulus
		long minutes = 60L *  (24 + helper.get(GregorianCalendar.HOUR_OF_DAY)) + helper.get(GregorianCalendar.MINUTE) - date.getTimezoneOffset();
		minutes = minutes % (24 * 60);
		return 60000L * minutes;
	}
	synchronized public static long hourTime(int hour) {
		helper.setTimeInMillis(0);
		helper.set(GregorianCalendar.HOUR_OF_DAY,hour);
		return helper.getTimeInMillis();
	}

	
	/**
	 * Used by importing, not the dialog box
	 * @param number
	 * @param start
	 * @param end
	 * @return
	 */public boolean setInterval(int number, Date start, Date end) {
		if (start == null || end == null)
			return false;
		
		try {
			setInterval(number,getHoursAndMinutes(start),getHoursAndMinutes(end));
			return true;
		} catch (WorkRangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	/**
	 * Set an interval.  Start and end must be on 1/1/70 and must have their hours set using a GregorianCalendar
	 * to avoid daylight savings issues.
	 * @param number - Range number 0 up to Settings.CALENDAR_INTERVALS -1.  Currently range is 0-4
	 * @param start - start time.  A value of -1 signifies that this range is null
	 * @param end - end time.  If end time is 0, it is treated as midnight the next day
	 * @throws WorkRangeException
	 */
	public void setInterval(int number, long start, long end) throws WorkRangeException {
		if (start == -1 || end == -1)
			workRange[number] = null;
		else {
			GregorianCalendar cal = DateTime.calendarInstance();
			cal.setTimeInMillis(end);
			if (cal.get(GregorianCalendar.HOUR_OF_DAY) == 0 && cal.get(GregorianCalendar.MINUTE) == 0) { // test for midnight end next day
				cal.add(GregorianCalendar.HOUR_OF_DAY,24);
				end = cal.getTimeInMillis();
			}
			workRange[number] = new WorkRange(start,end);			
		}
			
		initialize();
	}
	
	
	public WorkRange getInterval(int number){
	    return workRange[number];
	}
	
	public List getIntervals(){
	    return Arrays.asList(workRange);
	}
	
	
	/**
	 * 
	 */
	public WorkingHours() {
		super();
		for (int i = 0; i < workRange.length; i++)
			workRange[i] = null;
	}
	
	/**
	 * Validate that the work ranges do not overlap
	 * @param range
	 * @return
	 */
	void validate() throws WorkRangeException {
		boolean foundNull = false;
		
		// check for gaps first
		for (int i = 0; i < workRange.length - 1; i++) {
			if (workRange[i] == null) {
				foundNull = true;
			} else {
				if (foundNull)
					throw new WorkRangeException(Messages.getString("WorkRangeException.RangeIncomplete"));
			}
			
		}
		for (int i = 0; i < workRange.length - 1; i++) {
			for (int j = i+1; j < workRange.length; j++) {
				if (workRange[i] != null && !workRange[i].isBefore(workRange[j]))
					throw new WorkRangeException(Messages.getString("WorkRangeException.RangesMustBeOrdered"));
			}
		}
		initialize();
	}
	
	/**
	 * @return Returns the workTime.
	 */
	public long getDuration() {
		return duration;
	}

	
	void initialize() {
		duration = 0;
		for (int i =0; i <workRange.length; i++) {
			if (workRange[i] != null)
				duration += workRange[i].calcWorkingHours();
		}
	}
 	
	public void setNonWorking() {
		duration = 0;
	}


	/**
	 * Calculates the time of day when there is still x time left to do
	 * @param duration The x time of work left after the return value
	 * @return Time of day 
	 */
	public long calcTimeAtRemainingWork(long duration) {
		long work = 0;
		for (int i = workRange.length-1; i >=0; i--) {
			if (workRange[i] != null) {
				work += workRange[i].calcWorkingHours();
				if (work >= duration) {
					return (workRange[i].getStart() + (work - duration));
				}
			}
		}
//		log.error("calcTimeAtRemainingWork didn't finish");
		return -1; // error, return day start
	}	


	/**
	 * @param duration
	 * @return
	 */
	public long calcTimeAtWork(long duration) {
		long work = 0;
		for (int i =0; i <workRange.length; i++) {
			if (workRange[i] != null) {
				work += workRange[i].calcWorkingHours();
				if (work >= duration) {
					return (workRange[i].getEnd() - (work - duration));
				}
			}
		}
//		log.error("calcTimeAtWork didn't finish");
		return -1;//24L*60*60*1000; // error return midnight next day
	}	
	

	/**
	 * Calculate how much work time is remaining after the given time. 
	 * @param time
	 * @return
	 */
	public long calcWorkTimeAfter(long time) {
		long work = 0;
		for (int i =0; i <workRange.length; i++) {
			if (workRange[i] != null) {
				if (workRange[i].getEnd() > time)
					work += (workRange[i].getEnd() - Math.max(time,workRange[i].getStart()));
			}
		}
		return work;
	}
	
	/**
	 * @param date
	 * @return
	 */
	public long calcWorkTimeBefore(long time) {
		return duration - calcWorkTimeAfter(time);
	}
	
	public long calcWorkTime(long time, boolean after) {
		return after ? calcWorkTimeAfter(time) : calcWorkTimeBefore(time);
	}

	public String toString() {
		String result = "WorkingHours\n";
		for (int i =0; i <workRange.length; i++) {
			if (workRange[i] != null) {
				if (result.length() != 0)
					result += "\n";
				result +="Range " + i + " - " + workRange[i]; 
			}
		}
		return result;
	}
	private static WorkingHours defaultWorkingHours = null;
	
	public static WorkingHours getDefault() {
		if (defaultWorkingHours == null) {
			defaultWorkingHours = new WorkingHours();
			try {
				Calendar cal = DateTime.calendarInstance();
				cal.setTimeInMillis(0);
				cal.set(Calendar.HOUR_OF_DAY,8);
				long start = cal.getTimeInMillis();
				cal.set(Calendar.HOUR_OF_DAY,12);
				long end = cal.getTimeInMillis();
				
				defaultWorkingHours.setInterval(0,start, end);
				
				cal.set(Calendar.HOUR_OF_DAY,13);
				start = cal.getTimeInMillis();
				cal.set(Calendar.HOUR_OF_DAY,17);
				end = cal.getTimeInMillis();
				defaultWorkingHours.setInterval(1,start, end);
				
				
				
			} catch (WorkRangeException e) {
				e.printStackTrace();
			}
		}
		return defaultWorkingHours;
	}
	private static WorkingHours nonStopWorkingHours = null;
	
	public static WorkingHours getNonStop() {
		if (nonStopWorkingHours == null) {
			nonStopWorkingHours = new WorkingHours();
			try {
				Calendar cal = DateTime.calendarInstance();
				cal.setTimeInMillis(0);
				cal.set(Calendar.HOUR_OF_DAY,0);
				long start = cal.getTimeInMillis();
				
				nonStopWorkingHours.setInterval(0,start, start);
				
				
			} catch (WorkRangeException e) {
				e.printStackTrace();
			}
		}
		return nonStopWorkingHours;
	}

	
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof WorkingHours))
			return false;
		if (this == arg0)
			return true;
		WorkingHours to = (WorkingHours)arg0;
		for (int i = 0; i < workRange.length; i++) {
			if (workRange[i] != null) {
				if (!workRange[i].equals(to.workRange[i]))
					return false;
			} else if (to.workRange[i] != null) {
					return false;
			}
		}
		return true;
	}
	
	public boolean hasHours() {
		duration = 0;
		for (int i =0; i <workRange.length; i++) {
			if (workRange[i] != null)
				return true;
		}
		return false;
	}
}
