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
package com.projity.timescale;

import java.util.Calendar;

import com.projity.pm.time.HasStartAndEnd;
import com.projity.util.DateTime;

/**
 *
 */
public class TimeIterator implements HasStartAndEnd {
	//protected int calendarField=Calendar.DAY_OF_WEEK;
	//protected int calendarIncrement=1;
	protected long startTime;
	protected long endTime;
	protected Calendar calendar1;
	protected Calendar calendar2;
	protected TimeScale scale;
	private boolean hasNext=true;
	private long next2=-1;
	private boolean useLargeScale;
	/**
	 * @param startTime
	 * @param endTime
	 */
	public TimeIterator(long startTime, long endTime,TimeScale scale,long startReference) {
		this(startTime,endTime,scale,startReference,false);
	}
	public TimeIterator(long startTime, long endTime,TimeScale scale,long startReference,boolean useLargeScale) {
		//System.out.println("TimeIterator: "+CalendarUtil.toString(startTime)+","+CalendarUtil.toString(endTime)+","+CalendarUtil.toString(startReference));
		this.useLargeScale=useLargeScale;
		long s;
		long e;
		if (startTime<=endTime){
			s=startTime;
			e=endTime;
		}else{
			//this case appears with clip rectangles
			e=startTime;
			s=endTime;
		}
		long startRef=startReference;
		this.startTime = s;
		this.endTime = (s==e)?(e+1):e;
		this.scale=scale;
		
		if (useLargeScale){
			calendar2=DateTime.calendarInstance();
			
			calendar2=DateTime.calendarInstance();
			calendar2.setTimeInMillis(s);
			if (startRef==-1) scale.floor2(calendar2);
			else{
				scale.floor2(calendar2,startRef);
			}
			
			this.startTime=calendar2.getTimeInMillis();
		}else{
			calendar1=DateTime.calendarInstance();
			
			calendar1=DateTime.calendarInstance();
			calendar1.setTimeInMillis(s);
			if (startRef==-1) scale.floor1(calendar1);
			else{
				scale.floor1(calendar1,startRef);
			}

			calendar2=DateTime.calendarInstance();
			calendar2.setTimeInMillis(s);
			
			scale.floor2(calendar2,startRef);
			
			this.startTime=calendar1.getTimeInMillis();
		}
	}
	public TimeIterator(double startTime, double endTime,TimeScale scale,long startReference) {
		this(startTime,endTime,scale,startReference,false);
	}
	public TimeIterator(double startTime, double endTime,TimeScale scale,long startReference, boolean useLargeScale) {
		this(CalendarUtil.toLongTime(startTime),CalendarUtil.toLongTime(endTime),scale,startReference,useLargeScale);
	}
	public boolean hasNext(){
		return hasNext;
	}
	public TimeInterval next(){
		if (!hasNext) return null;
		if (useLargeScale){
			long begin2=calendar2.getTimeInMillis();
			scale.increment2(calendar2);
			long end2=calendar2.getTimeInMillis();
			if (end2>=endTime) hasNext=false;
			//System.out.println("large begin2="+CalendarUtil.toString(begin2));
			String text2=scale.getText2(begin2);
			
			return new TimeInterval(begin2,end2,text2,-1L,-1L,null);
		}else{
			long begin1=calendar1.getTimeInMillis();
			scale.increment1(calendar1);
			long end1=calendar1.getTimeInMillis();
			if (end1>=endTime) hasNext=false;
			//System.out.println("begin1="+CalendarUtil.toString(begin1));
			String text1=scale.getText1(begin1);
			
			long begin2=-1;
			long end2=-1;
			String text2=null;
			if (next2==-1||begin1>=next2){
				begin2=calendar2.getTimeInMillis();
				scale.increment2(calendar2);
				end2=calendar2.getTimeInMillis();
				next2=end2;
				//System.out.println("begin2="+CalendarUtil.toString(begin2));
				text2=scale.getText2(begin2);
			}
			return new TimeInterval(begin1,end1,text1,begin2,end2,text2);
			
		}
	}
	
	
	
	/**
	 * @return Returns the endTime.
	 */
	public long getEnd() {
		return endTime;
	}
	/**
	 * @return Returns the startTime.
	 */
	public long getStart() {
		return startTime;
	}
}
