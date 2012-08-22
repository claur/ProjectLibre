/*
 * (C) Copyright 2006-2008, by Projity Inc. and Contributors.
 * http://www.projity.com
 *
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 *
*/
package com.projity.contrib.calendar;

import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.time.DateUtils;
import org.jdesktop.swing.calendar.DateSpan;

/**
 *
 */
public class ContribIntervals extends TreeSet{

	/**
	 *
	 */
	public ContribIntervals() {
		super(new Comparator(){
			public int compare(Object o1, Object o2) {
				DateSpan d1=(DateSpan)o1; //Only want to compare DateSpan no need to use instanceof
				DateSpan d2=(DateSpan)o2;
				if (d1.getStart()<d2.getStart()||(d1.getStart()==d2.getStart()&&d1.getEnd()<d2.getEnd())) return -1;
				else if (d1.getStart()>d2.getStart()||(d1.getStart()==d2.getStart()&&d1.getEnd()>d2.getEnd())) return 1;
				else return 0;
			}
		});
	}
	public ContribIntervals(Comparator c) {
		super(c);
	}


	protected DateSpan createInterval(long start,long end) {
		return new DateSpan(start,end);
	}
	protected DateSpan mergeIntervals(DateSpan i1,DateSpan i2) {
		return new DateSpan(Math.min(i1.getStart(),i2.getStart()),Math.max(i1.getEnd(),i2.getEnd()));
	}


	public boolean add(Object o) {
		DateSpan toAdd=(DateSpan)o;
		SortedSet set=headSet(o);
		if (set.size()>0){
			DateSpan interval=(DateSpan)set.last();
			if (interval.getEnd()>=toAdd.getStart())
				toAdd=mergeIntervals(toAdd,interval);
		}

		set=tailSet(o);
		if (set.size()>0){
			DateSpan interval=(DateSpan)set.first();
			if (toAdd.getEnd()>=interval.getStart())
				toAdd=mergeIntervals(toAdd,interval);
		}
		return super.add(toAdd);
	}

	public boolean addAll(Collection c) {
		if (c==null) return false;
		boolean added=false;
		for (Iterator i=c.iterator();i.hasNext();){
			if (super.add(i.next())) added=true;
		}
		return added;
	}

	public long getEnd() {
		return (size()==0)?-1:((DateSpan)last()).getEnd();
	}
	public long getStart() {
		return (size()==0)?-1:((DateSpan)first()).getStart();
	}

	public boolean containsDate(long date){
		for (Iterator i=iterator();i.hasNext();){ //a more optimized version can be found
			DateSpan interval=(DateSpan)i.next();
			if (interval.getStart()<=date&&date<=interval.getEnd()) return true;
		}
		return false;
	}

	void eliminateWeekdayDuplicates(boolean weekDays[]) {
		Calendar cal = calendarInstance();
		for (Iterator i=iterator();i.hasNext();){ //a more optimized version can be found
			DateSpan interval=(DateSpan)i.next();
			cal.setTimeInMillis(interval.getStart());
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) -1;

			// remove any days which correspond to a selected week day
			for (int d = 0; d < 7; d++) {
				if (weekDays[d] && d == dayOfWeek) {
					i.remove();
				}
			}
		}
	}

	/*public void xorAdd(HasStartAndEnd o,Closure removeFunctor,Closure addFunctor){
		HasStartAndEnd toAdd=o;
		SortedSet set=headSet(o);
		if (set.size()>0){
			HasStartAndEnd previous=(HasStartAndEnd)set.last();
			if (previous.getEnd()>=toAdd.getStart()){
				remove(previous);
				if (previous.getEnd()==toAdd.getStart()){
					toAdd=createInterval(previous.getStart(),toAdd.getEnd());
				}else{
					if (previous.getStart()<toAdd.getStart())
						super.add(createInterval(previous.getStart(),toAdd.getStart()));
					removeFunctor.execute(createInterval(toAdd.getStart(),previous.getEnd()));
					toAdd=createInterval(previous.getEnd(),toAdd.getEnd());
				}
			}
		}

		set=tailSet(o);
		if (set.size()>0){
			HasStartAndEnd next=(HasStartAndEnd)set.first();
			if (next.getStart()<=toAdd.getEnd()){
				remove(next);
				if (next.getStart()==toAdd.getEnd()){
					createInterval(toAdd.getStart(),next.getEnd());
				}else{
					if (next.getEnd()>toAdd.getEnd())
						super.add(createInterval(toAdd.getEnd(),next.getEnd()));
					removeFunctor.execute(createInterval(next.getStart(),toAdd.getEnd()));
					toAdd=createInterval(toAdd.getStart(),next.getStart());
				}
			}
		}

		if (toAdd.getStart()<=toAdd.getEnd()){
			super.add(toAdd);
			addFunctor.execute(toAdd);
		}

	}*/


	public static GregorianCalendar calendarInstance() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeZone(DateUtils.UTC_TIME_ZONE);
		return cal;
	}

}
