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
package com.projectlibre1.pm.calendar;

import java.io.Serializable;

import com.projectlibre1.contrib.util.Log;
import com.projectlibre1.contrib.util.LogFactory;
import com.projectlibre1.strings.Messages;

public class WorkWeek implements Cloneable,Serializable {
	static final long serialVersionUID = 2555674567677L;
    public static final int DAYS_IN_WEEK = 7;
	public static final long MS_IN_WEEK = DAYS_IN_WEEK * 24L*60*60*1000;
    WorkDay workDay[] = new WorkDay[DAYS_IN_WEEK];
    static Log log = LogFactory.getLog(WorkWeek.class);

    private static String WEEKDAY_MASK = new String(Messages.getString("Calendar.WeekdayBitMaskFromSundayToSaturday"));
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
    	log.info("WorkWeek.clone, "+this);
		WorkWeek newOne = new WorkWeek();
		for (int i = 0; i < DAYS_IN_WEEK; i++) {
			if (workDay[i] == null)
				newOne.workDay[i] = null;
			else
				newOne.workDay[i] = (WorkDay) workDay[i].clone();
		}
		return newOne;
	}

	public WorkWeek(WorkDay[] days) {
    	log.info("WorkWeek(), days="+days);
		this.workDay = (WorkDay[]) days.clone();
        updateWorkingDuration();
	}
	
    public WorkWeek() {
    	log.info("WorkWeek()");
        for (int i = 0; i < DAYS_IN_WEEK; i++)
            workDay[i] = null;
    }
    public WorkDay getWeekDay(int dayNum) {
//    	if (dayNum < 0)
//    		System.out.println("day num is " + dayNum);    	
    	return workDay[dayNum];
    }
    
    WorkWeek intersectWith(WorkWeek other) throws InvalidCalendarIntersectionException {
    	WorkWeek result = new WorkWeek();
        for (int i = 0; i < DAYS_IN_WEEK; i++)
            result.workDay[i] = workDay[i].intersectWith(other.getWeekDay(i));
        result.updateWorkingDuration();
        if (result.getDuration() == 0) // a calendar cannot have no working time for its work week
        	throw new InvalidCalendarIntersectionException();
        return result;
    	
    }
    public void setWeekDay(int dayNum, WorkDay day) {
    	log.info("WorkWeek.setWeekDay, "+this+", dayNum="+dayNum+", day="+day);
    	if (day != null)
    		day.initialize();
    	workDay[dayNum] = day;
        updateWorkingDuration();    	
    }
    
    public void setWeekDays(WorkDay day) {
    	for (int i = 0; i < DAYS_IN_WEEK; i++) {
    		if (WEEKDAY_MASK.charAt(i) == '1') {
    			setWeekDay(i,day);
    		}
    	}
        updateWorkingDuration();    	
    }
    public void setWeekends(WorkDay day) {
    	for (int i = 0; i < DAYS_IN_WEEK; i++) {
    		if (WEEKDAY_MASK.charAt(i) == '0') {
    			setWeekDay(i,day);
    		}
    	}
        updateWorkingDuration();    	
    }
    
    public void addDaysFrom(WorkWeek from) {
    	log.info("WorkWeek.addDaysFrom, "+this+", from="+from);
        for (int i = 0; i < DAYS_IN_WEEK; i++) {
        	if (from.workDay[i] != null)
        		workDay[i] = from.workDay[i];
        	if (workDay[i]==null){
        		System.out.println();
        	}
        	else workDay[i].initialize(); // calc hours - fixes bug in importing project 2007 files
        }
        updateWorkingDuration();
    }
    
    private long workingDuration = 0;

    void updateWorkingDuration() {
    	workingDuration = 0;
    	for (int i = 0; i < DAYS_IN_WEEK; i++) {
    		if (workDay[i] != null)
    			workingDuration += workDay[i].getDuration();
    	}
    }

    public final long getDuration() {
    	return workingDuration;
    }
}
