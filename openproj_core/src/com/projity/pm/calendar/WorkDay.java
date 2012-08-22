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

import com.projity.util.MathUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
public class WorkDay extends CalendarEvent implements Comparable, Cloneable,Serializable {

	static final long serialVersionUID = 28283927181117L;
	// These values can serve as sentinels to simplify algorithms
	public static final WorkDay MINIMUM = new WorkDay(0);
	public static final WorkDay MAXIMUM = new WorkDay(Long.MAX_VALUE);
	/**
	 * @param fromDate
	 * @param toDate
	 */
	public WorkDay(long fromDate, long toDate) {
		super(fromDate, toDate);
	}
	public Object clone() {
		WorkDay newOne = null;
		try {
			newOne = (WorkDay) super.clone();
			newOne.workingHours = (WorkingHours) workingHours.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newOne;
	}

	/**
	 * @param date
	 */
	public WorkDay(long date) {
		this(date,date);
	}

	public WorkDay() {
		this(0,0);
	}

/**
 * Intersect a day with another one returning the result
 * @param other
 * @return
 */	
	WorkDay intersectWith(WorkDay other) {
		WorkDay result = new WorkDay(Math.max(getStart(),other.getStart()),Math.min(getStart(),other.getStart()));
		result.setWorkingHours(workingHours.intersectWith(other.getWorkingHours()));
		return result;
		
	}

	WorkingHours workingHours = new WorkingHours();

	void initialize() {
		workingHours.initialize();
	}
	/**
	 * @return Returns the workingHours.
	 */
	public WorkingHours getWorkingHours() {
		return workingHours;
	}

	/**
	 * @param workingHours The workingHours to set.
	 */
	public void setWorkingHours(WorkingHours workingHours) {
		this.workingHours = workingHours;
	}
	
	
	public boolean hasSameWorkHours(WorkDay d) {
		if (workingHours == null) {
			if (d == null || d.workingHours == null)
				return true;
			else
				return d.hasSameWorkHours(this);
		}
		if (d == null)
			return false;
		return workingHours.equals(d.workingHours);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object e) {
		if (! (e instanceof WorkDay))
			return false;
		return (getStart() == ((WorkDay)e).getStart());
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object event1, Object event2) {
		
		if (event2 instanceof Date) // if comparing to a date
			return MathUtils.signum(((WorkDay)event1).getStart() - ((Date)event2).getTime()); 

		if (event2 instanceof Calendar) // if comparing to a date
			return MathUtils.signum(((WorkDay)event1).getStart() - ((Calendar)event2).getTimeInMillis()); 
		
		if (! (event1 instanceof WorkDay) || ! (event2 instanceof WorkDay))
			return 0;
		
		return MathUtils.signum(((WorkDay)event1).getStart() - ((WorkDay)event2).getStart());
	}
	
    public long getDuration() {
    	return workingHours.getDuration();
    }

    public boolean isWorking() {
    	return getDuration() > 0;
    }
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object to) {
		if (to instanceof WorkDay) // if comparing to a date
			return MathUtils.signum((getStart() - ((WorkDay)to).getStart()));

		if (to instanceof Date) // if comparing to a date
			return MathUtils.signum(getStart() - ((Date)to).getTime()); 

		if (to instanceof Calendar) // if comparing to a date
			return MathUtils.signum(getStart() - ((Calendar)to).getTimeInMillis()); 
		
		throw new ClassCastException("Cant compare" + to + " to a WorkDay");
	}

	public String toString() {
		return "work day " + new Date(start) + " " + hashCode() + "\n" + workingHours.toString() + "\n";
	}
	
	private static WorkDay defaultWorkDay = null;
	
	public static WorkDay getDefaultWorkDay() {
		if (defaultWorkDay == null) {
			defaultWorkDay = new WorkDay();
			defaultWorkDay.setWorkingHours(WorkingHours.getDefault());
		}
		return defaultWorkDay;
	}
	private static WorkDay nonStopWorkDay = null;
	
	public static WorkDay getNonStopWorkDay() {
		if (nonStopWorkDay == null) {
			nonStopWorkDay = new WorkDay();
			nonStopWorkDay.setWorkingHours(WorkingHours.getNonStop());
		}
		return nonStopWorkDay;
	}

	private static WorkDay nonWorkingWorkDay = null;
	
	public static WorkDay getNonWorkingDay() {
		if (nonWorkingWorkDay == null) {
			nonWorkingWorkDay = new WorkDay();
		}
		return nonWorkingWorkDay;
	}
	
}
