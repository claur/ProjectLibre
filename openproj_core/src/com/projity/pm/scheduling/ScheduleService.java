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
package com.projity.pm.scheduling;

import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.projity.configuration.Configuration;
import com.projity.field.Field;
import com.projity.functor.IntervalConsumer;
import com.projity.undo.FieldEdit;
import com.projity.undo.ScheduleEdit;
import com.projity.undo.SplitEdit;
import com.projity.util.ClassUtils;
import com.projity.util.DateTime;

/**
 * Singleton service for manipulating a schedule, such as by gantt chart modifications
 */
public class ScheduleService {
	private boolean consuming = false;

	private static Field completedFieldInstance = null;
	public static Field getCompletedField() {
		if (completedFieldInstance == null)
			completedFieldInstance = Configuration.getFieldFromId("Field.stop");
		return completedFieldInstance;
	}

	private static ScheduleService instance = null;

	/**
	 * @return Returns the singleton instance.
	 */
	public static ScheduleService getInstance() {
		if (instance == null)
			instance = new ScheduleService();
		return instance;
	}
	
	
	/**
	 * Private constructor 
	 */
	private ScheduleService() {
		super();
	}
	
	public long getCompleted(Schedule schedule) {
		// this is used for drawing completion on the gantt also. see GanttUI
		return schedule.getCompletedThrough();
	}
	
	public void setCompleted(Object eventSource, Schedule schedule, long completed,UndoableEditSupport undoableEditSupport) {
		if (isReadOnly(schedule))
			return;
		Field completedField=getCompletedField();
		Object oldValue=completedField.getValue(schedule);
		if (oldValue==null) oldValue=new Long(schedule.getActualStart());
		Object value=new Long(completed);
		completedField.setValue(schedule,eventSource,value);
		if (undoableEditSupport!=null&&!(eventSource instanceof UndoableEdit)){
			undoableEditSupport.postEdit(new FieldEdit(completedField,schedule,value,oldValue,eventSource,null));
		}
	}
	
	public static boolean isReadOnly(Schedule schedule){
		return ClassUtils.isObjectReadOnly(schedule);
	}
	
	/**
	 * Set the start or the end of the schedule and fire field event which will cause the critical path to run.  The method
	 * checks to see which of the two - start or end, was modified and only updates the modified one
	 * @param eventSource - the object which is the event source, such as GanttModel
	 * @param schedule - the task or assignment
	 * @param start - start date millis
	 * @param end - end date millis	 * 
	 * @param oldStart is the prior start for the bar.  It will be used to identify what bar changed
	 */
	public void setInterval(Object eventSource, Schedule schedule, long start, long end, ScheduleInterval interval,UndoableEditSupport undoableEditSupport) {
		if (isReadOnly(schedule))
			return;
		Object detailBackup=null;
		start = DateTime.hourFloor(start);
		end = DateTime.hourFloor(end);
		if (interval.getStart() == start && interval.getEnd() == end) // if no move do nothing
			return;
		if (undoableEditSupport!=null&&!(eventSource instanceof UndoableEdit)){
			detailBackup=schedule.backupDetail();
		}
		
		schedule.moveInterval(eventSource,start,end,interval, false);
		//Undo
		if (detailBackup!=null){
			undoableEditSupport.postEdit(new ScheduleEdit(schedule,detailBackup,start,end,interval,false,eventSource));
		}

	}

	/**
	 * Split a task/assignment by adding a nonworking interval.  If there is actual work during the split,
	 * only the nonworking part will be moved.  Unlike other products, we don't let you move actuals.
	 * @param eventSource- the object which is the event source, such as GanttModel
	 * @param schedule - the task or assignment
	 * @param from - beginning of nonwork interval
	 * @param to - end of nonwork interval
	 */
	public void split(Object eventSource, Schedule schedule, long from, long to,UndoableEditSupport undoableEditSupport) {
		if (isReadOnly(schedule))
			return;
		Object detailBackup=null;
		if (undoableEditSupport!=null&&!(eventSource instanceof UndoableEdit)){
			detailBackup=schedule.backupDetail();
		}
		schedule.split(eventSource,DateTime.hourFloor(from),DateTime.hourFloor(to));
		//Undo
		if (detailBackup!=null){
			undoableEditSupport.postEdit(new SplitEdit(schedule,detailBackup,from,to,eventSource));
		}
	}
	
	/**
	 * Calls back the consumer for each interval in the schedule.  Currently in only treats splits due to
	 * stop/resume. In the future it will also call back for splits in the work contour itself
	 * @param schedule
	 * @param consumer
	 */
	public void consumeIntervals(Schedule schedule, IntervalConsumer consumer) {
		if (consuming)
			return;
		consuming = true;
		schedule.consumeIntervals(consumer);
		consuming = false;
	}
}
