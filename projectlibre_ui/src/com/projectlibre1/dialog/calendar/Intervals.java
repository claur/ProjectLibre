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
package com.projectlibre1.dialog.calendar;

import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jdesktop.swing.calendar.DateSpan;

import com.projectlibre1.contrib.calendar.ContribIntervals;
import com.projectlibre1.pm.time.HasStartAndEnd;
import com.projectlibre1.util.DateTime;

/**
 *
 */
public class Intervals extends TreeSet implements HasStartAndEnd{

	/**
	 *
	 */
	public Intervals(ContribIntervals c) {
		super(new Comparator(){
			public int compare(Object o1, Object o2) {
				HasStartAndEnd d1=(HasStartAndEnd)o1; //Only want to compare DateSpan no need to use instanceof
				HasStartAndEnd d2=(HasStartAndEnd)o2;
				if (d1.getStart()<d2.getStart()||(d1.getStart()==d2.getStart()&&d1.getEnd()<d2.getEnd())) return -1;
				else if (d1.getStart()>d2.getStart()||(d1.getStart()==d2.getStart()&&d1.getEnd()>d2.getEnd())) return 1;
				else return 0;
			}
		});
		if (c!=null)
			for (Iterator i=c.iterator();i.hasNext();){
				DateSpan d=(DateSpan)i.next();
				if (super.add(new CalendarInterval(d.getStart(),d.getEnd())));
			}
	}


	protected HasStartAndEnd createInterval(long start,long end) {
		return new CalendarInterval(start,end);
	}
	protected HasStartAndEnd mergeIntervals(HasStartAndEnd i1,HasStartAndEnd i2) {
		return new CalendarInterval(Math.min(i1.getStart(),i2.getStart()),Math.max(i1.getEnd(),i2.getEnd()));
	}


	public boolean add(Object o) {
		HasStartAndEnd toAdd=(HasStartAndEnd)o;
		SortedSet set=headSet(o);
		if (set.size()>0){
			HasStartAndEnd interval=(HasStartAndEnd)set.last();
			if (interval.getEnd()>=toAdd.getStart())
				toAdd=mergeIntervals(toAdd,interval);
		}

		set=tailSet(o);
		if (set.size()>0){
			HasStartAndEnd interval=(HasStartAndEnd)set.first();
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
		return (size()==0)?-1:((HasStartAndEnd)last()).getEnd();
	}
	public long getStart() {
		return (size()==0)?-1:((HasStartAndEnd)first()).getStart();
	}

	public boolean containsDate(long date){
		for (Iterator i=iterator();i.hasNext();){ //a more optimized version can be found
			HasStartAndEnd interval=(HasStartAndEnd)i.next();
			if (interval.getStart()<=date&&date<=interval.getEnd()) return true;
		}
		return false;
	}

	void eliminateWeekdayDuplicates(boolean weekDays[]) {
		Calendar cal = DateTime.calendarInstance();
		for (Iterator i=iterator();i.hasNext();){ //a more optimized version can be found
			HasStartAndEnd interval=(HasStartAndEnd)i.next();
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
}
