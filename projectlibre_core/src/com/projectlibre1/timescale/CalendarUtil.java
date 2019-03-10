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
package com.projectlibre1.timescale;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import com.projectlibre1.pm.time.HasStartAndEnd;
import com.projectlibre1.util.DateTime;


public class CalendarUtil {
	public static final long SECOND_ms=1000;
	public static final long MINUTE_ms=60*SECOND_ms;
	public static final long HOUR_ms=60*MINUTE_ms;
	public static final long DAY_ms=24*HOUR_ms;
	public static final long WEEK_ms=DateTime.calendarInstance().getLeastMaximum(Calendar.DAY_OF_WEEK)*DAY_ms;
	public static final long MONTH_ms=DateTime.calendarInstance().getLeastMaximum(Calendar.DAY_OF_MONTH)*DAY_ms;
	public static final long YEAR_ms=DateTime.calendarInstance().getLeastMaximum(Calendar.DAY_OF_YEAR)*DAY_ms;
	

	private CalendarUtil() {
	}

	public static Date min(Date date, Date date1) {
		return date.before(date1) ? date : date1;
	}

	public static Date max(Date date, Date date1) {
		return date.after(date1) ? date : date1;
	}

	public static int compareEra(Calendar calendar, Calendar calendar1) {
		if(calendar.getClass() != calendar1.getClass())
			throw new IllegalArgumentException("Cannot compare calendars of dissimilar classes: " + calendar + ", " + calendar1);
		else
			return calendar.get(Calendar.ERA) - calendar1.get(Calendar.ERA);
	}

	public static int compareYear(Calendar calendar, Calendar calendar1) {
		int i = compareEra(calendar, calendar1);
		if(i != 0)
			return i;
		else
			return calendar.get(Calendar.YEAR) - calendar1.get(Calendar.YEAR);
	}

	public static int compareMonth(Calendar calendar, Calendar calendar1) {
		int i = compareYear(calendar, calendar1);
		if(i != 0)
			return i;
		else
			return calendar.get(Calendar.MONTH) - calendar1.get(Calendar.MONTH);
	}

	public static int compareDay(Calendar calendar, Calendar calendar1) {
		int i = compareYear(calendar, calendar1);
		if(i != 0)
			return i;
		else
			return calendar.get(Calendar.DAY_OF_YEAR) - calendar1.get(Calendar.DAY_OF_YEAR);
	}

	
		
	
	public static void secondFloor(Calendar calendar) {
		calendar.set(Calendar.MILLISECOND, 0);
	}

	public static void minuteFloor(Calendar calendar) {
		secondFloor(calendar);
		calendar.set(Calendar.SECOND, 0);
	}
	public static void minuteFloor(Calendar calendar,int number) {
		secondFloor(calendar);
		calendar.set(Calendar.SECOND, 0);
		if (number>1){
			int minutes=calendar.get(Calendar.SECOND);
			calendar.set(Calendar.SECOND,(minutes/number)*number);
		}
	}

	public static void hourFloor(Calendar calendar) {
		minuteFloor(calendar);
		calendar.set(Calendar.MINUTE, 0);
	}
	public static void hourFloor(Calendar calendar,int number) {
		minuteFloor(calendar);
		calendar.set(Calendar.MINUTE, 0);
		if (number>1){
			int hours=calendar.get(Calendar.HOUR);
			calendar.set(Calendar.HOUR,(hours/number)*number);
		}
	}

	public static void dayFloor(Calendar calendar) {
		hourFloor(calendar);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
	}

	public static void weekFloor(Calendar calendar) {
		int i = calendar.get(Calendar.YEAR);
		int k = calendar.get(Calendar.DAY_OF_YEAR);
		int l = calendar.get(Calendar.DAY_OF_WEEK);
		int i1 = calendar.getFirstDayOfWeek();
		int j1 = l - i1;
		if(j1 > 0)
			k -= j1;
		else
		if(j1 < 0)
			k -= 7 + j1;
		calendar.clear();
		boolean flag = calendar.isLenient();
		if(!flag)
			calendar.setLenient(true);
		calendar.set(Calendar.YEAR, i);
		calendar.set(Calendar.DAY_OF_YEAR, k);
		if(!flag) {
			int j = calendar.get(Calendar.YEAR);
			calendar.setLenient(false);
		}
	}

	public static void monthFloor(Calendar calendar) {
		dayFloor(calendar);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
	}
	public static void monthFloor(Calendar calendar,int number) { //number param for quarter and half
		dayFloor(calendar);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		if (number>1){
			int month=calendar.get(Calendar.MONTH);
			calendar.set(Calendar.MONTH,(month/number)*number);
		}
	}

	public static void yearFloor(Calendar calendar) {
		monthFloor(calendar);
		calendar.set(Calendar.MONTH, 0);
	}
	
	public static void floor(Calendar calendar,int field){
		floor(calendar, field,1);
	}
	public static void floor(Calendar calendar,int field,int number){
		switch (field) {
			case Calendar.YEAR :yearFloor(calendar);			
				break;
			case Calendar.MONTH :monthFloor(calendar,number);			
				break;
			case Calendar.WEEK_OF_YEAR :weekFloor(calendar);			
				break;
			case Calendar.DAY_OF_WEEK :dayFloor(calendar);			
				break;
			case Calendar.DAY_OF_MONTH :dayFloor(calendar);			
				break;
			case Calendar.HOUR_OF_DAY :hourFloor(calendar,number);			
				break;
			case Calendar.MINUTE :minuteFloor(calendar,number);			
				break;
			case Calendar.SECOND :secondFloor(calendar);			
				break;
		}
	}
	
	public static long getMinDuration(int field){
		switch (field) {
			case Calendar.YEAR : return YEAR_ms;			
			case Calendar.MONTH : return MONTH_ms;
			case Calendar.WEEK_OF_YEAR : return WEEK_ms;
			case Calendar.DAY_OF_WEEK : return DAY_ms;
			case Calendar.DAY_OF_MONTH : return DAY_ms;
			case Calendar.HOUR_OF_DAY : return HOUR_ms;
			case Calendar.MINUTE : return MINUTE_ms;
			case Calendar.SECOND : return SECOND_ms;
		}
		return -1;
		
	}
	
	/**
	 * Don't use in loops. DateFormat and Date have to be reused
	 * @param t
	 * @return
	 */
	public static String toString(long t){
		Calendar calendar=DateTime.calendarInstance();
		calendar.setTimeInMillis(t);
		return toString(calendar);
	}
	public static String toString(Calendar calendar){
		DateFormat df = DateFormat.getDateTimeInstance();
		return df.format(calendar.getTime());
	}
	
	public static long toLongTime(double t){
		return Math.round(t/1000)*1000;
	}
	public static long roundTime(double dt,Calendar tmp){
		long t=toLongTime(dt);
		tmp.setTimeInMillis(t);
		int sec=tmp.get(Calendar.SECOND);
		return (sec>30)?t+(60-sec):t-sec;
	}
	public static void roundTime(Calendar c){
		c.setTimeInMillis(roundTime(c.getTimeInMillis(),c));
	}
	
	public static class DayIterator{
	    private Calendar calendar;
	    protected long end;
	    public void setInterval(HasStartAndEnd interval){
	        calendar=DateTime.calendarInstance();
	        calendar.setTimeInMillis(interval.getEnd());
	        dayFloor(calendar);
	        end=calendar.getTimeInMillis();
	        calendar.setTimeInMillis(interval.getStart());
	        dayFloor(calendar);
	    }
	    public boolean hasMoreDays(){
	        return calendar.getTimeInMillis()<=end;
	    }
	    public long nextDay(){
	        long current=calendar.getTimeInMillis();
	        calendar.add(Calendar.DATE,1);
	        return current;
	    }
	}
	

	
	
}
