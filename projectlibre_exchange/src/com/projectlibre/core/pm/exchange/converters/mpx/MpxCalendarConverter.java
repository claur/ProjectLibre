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
package com.projectlibre.core.pm.exchange.converters.mpx;

import com.projectlibre.pm.calendar.CalendarId;
import com.projectlibre.pm.calendar.DayType;
import com.projectlibre.pm.calendar.WorkCalendar;
import com.projectlibre.pm.calendar.WorkCalendarException;
import com.projectlibre.pm.calendar.WorkDay;
import com.projectlibre.pm.calendar.WorkWeek;

import net.sf.mpxj.Day;
import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectCalendarException;
import net.sf.mpxj.ProjectCalendarHours;

/**
 * @author Laurent Chretienneau
 *
 */
public class MpxCalendarConverter {
	public void from(ProjectCalendar mpxCalendar, WorkCalendar calendar, MpxImportState state){	
		calendar.setName(mpxCalendar.getName());
		state.getCalendarManager().fixBaseCalendar(calendar, state.getProjectTitle());
		calendar.setId(new CalendarId(mpxCalendar.getUniqueID()));
		
		
		//base calendar
		WorkCalendar standardCalendar = state.getCalendarManager().getStandardBaseCalendar();
		ProjectCalendar mpxBaseCalendar=null;
		WorkCalendar baseCalendar=null;
		if (mpxCalendar.isDerived()) {
			mpxBaseCalendar = mpxCalendar.getParent();
			if (mpxBaseCalendar==null){
				mpxBaseCalendar=state.getMpxStandardBaseCalendar();
			}
			baseCalendar=state.getCalendarManager().getCalendar(new CalendarId(mpxBaseCalendar.getUniqueID()));
			if (baseCalendar == null) 
				baseCalendar = standardCalendar;
			calendar.setBase(baseCalendar);
		}
		
		
		//work weeks
		WorkWeek week=new WorkWeek();
		calendar.setWeek(week);
		MpxRangeConverter rangeConverter=new MpxRangeConverter();
		for (int i=0; i<7; i++) {
			Day mpxDayId=Day.getInstance(i+1);
			ProjectCalendarHours mpxDay=mpxCalendar.getCalendarHours(mpxDayId);
			net.sf.mpxj.DayType mpxDayType=mpxCalendar.getWorkingDay(mpxDayId);
			WorkDay day=null;
			if (mpxDay == null) {
				if (mpxCalendar.isDerived() &&
							mpxBaseCalendar!=null){
					if (mpxDayType==net.sf.mpxj.DayType.DEFAULT)
						day=calendar.getBase().getWeek().getDay(i);
//						day = WorkDay.getDefaultDay();
					else if (mpxBaseCalendar.isWorkingDay(mpxDayId)) //TODO correct?
						day = WorkDay.getNonWorkingDay();
				}
			} else {
				day=new WorkDay(DayType.getInstance(mpxDayType.getValue()));
				rangeConverter.from(mpxDay,day);
			}
			week.setDay(i,day);
		}
		
		//exceptions
		MpxExceptionConverter exceptionConverter=new MpxExceptionConverter();
		for (ProjectCalendarException mpxException : mpxCalendar.getCalendarExceptions()){
			WorkCalendarException exception=new WorkCalendarException();
			exceptionConverter.from(mpxException,exception);
			calendar.addException(exception);
		}
	}

}
