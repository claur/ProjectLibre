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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.projity.util.DateTime;
import com.projity.util.Environment;

/**
 *
 */
public class TimeScale {
	protected int minWidth;
	protected int tableMinWidth;
	protected int normalMinWidth;
	protected long minDuration;
	protected int number1;
	protected int number2;
	protected int calendarField1;
	protected int calendarField2;
	protected double ratio=-1.0;
	protected String pattern1;
	protected String pattern2;
	protected int trunc1=-1;
	protected int trunc2=-1;
	protected boolean upperCase1=false;
	protected boolean upperCase2=false;

//	private Date recycledDate=new Date();
	private ExtendedDateFormat recycledDateFormat;

	/**
	 * 
	 */
	public TimeScale() {
		recycledDateFormat = DateTime.extendedUtcDateFormatInstance();
	}
	
	private Calendar tmp=DateTime.calendarInstance();
	public long floor1(long t){
		tmp.setTimeInMillis(t);
		floor(tmp,calendarField1,number1,-1);
		return tmp.getTimeInMillis();
	}
	public long ceil1(long t){
		tmp.setTimeInMillis(t);
		ceil(tmp,calendarField1,number1,-1);
		return tmp.getTimeInMillis();
	}
	
	public void floor1(Calendar calendar){
		floor(calendar,calendarField1,number1,-1);
	}
	public void floor1(Calendar calendar,long startReference){
		
		floor(calendar,calendarField1,number1,startReference);
	}
	public void floor2(Calendar calendar){
		floor(calendar,calendarField2,number2,-1);
	}
	public void floor2(Calendar calendar,long startReference){
		floor(calendar,calendarField2,number2,startReference);
	}
	public void ceil1(Calendar calendar){
		ceil(calendar,calendarField1,number1,-1);
	}
	public void ceil1(Calendar calendar,long startReference){
		
		ceil(calendar,calendarField1,number1,startReference);
	}
	public void ceil2(Calendar calendar){
		ceil(calendar,calendarField2,number2,-1);
	}
	public void ceil2(Calendar calendar,long startReference){
		ceil(calendar,calendarField2,number2,startReference);
	}
	
	protected void floor(Calendar calendar,int calendarField,int number,long startReference){
		CalendarUtil.floor(calendar,calendarField,number);
//		if (number>1){
//			long ref=calendar.getTimeInMillis();
//			if (calendarField==2&&number==3){
//				System.out.println("floor: "+CalendarUtil.toString(calendar)+", ref="+CalendarUtil.toString(ref)+", startReference="+CalendarUtil.toString(startReference));
//			}
//			if (startReference==-1) calendar.set(calendarField,calendar.getActualMinimum(calendarField));
//			else calendar.setTimeInMillis(startReference);
//			if (calendarField==2&&number==3){
//				System.out.println("floor#2: "+CalendarUtil.toString(calendar));
//			}
//			while(calendar.getTimeInMillis()<=ref){
//				calendar.add(calendarField,number);
//				if (calendarField==2&&number==3){
//					System.out.println("floor#3: "+CalendarUtil.toString(calendar));
//				}
//
//			}
//			if (calendarField==2&&number==3){
//				System.out.println("floor#3.9: "+CalendarUtil.toString(calendar));
//			}
//			calendar.add(calendarField,-number);
//			if (calendarField==2&&number==3){
//				System.out.println("floor#4: "+CalendarUtil.toString(calendar));
//			}
//
//		}
	}
	protected void ceil(Calendar calendar,int calendarField,int number,long startReference){
		Calendar ref=DateTime.calendarInstance();
		ref.setTimeInMillis(calendar.getTimeInMillis());
		floor(ref,calendarField,number,startReference);
		if (ref.getTimeInMillis()==calendar.getTimeInMillis()) return;
		floor(calendar,calendarField,number,startReference);
		calendar.add(calendarField,number);
	}
	public void increment1(Calendar calendar){
		calendar.add(calendarField1,number1);
	}
	public void increment2(Calendar calendar){
		calendar.add(calendarField2,number2);
	}
	
	
	
	public String getText1(long t){
		tmp.setTimeInMillis(t);
		recycledDateFormat.applyPattern(pattern1);
		String r=recycledDateFormat.format(tmp.getTime());
		if (trunc1>=0) {
			// patch for Chinese week display
			if (Environment.isChinese()) {
				int len = r.length();
				if (len>2)
					return r.substring(len-trunc1);
			}
			r=r.substring(0,trunc1);
		}
		if (upperCase1) r=r.toUpperCase();
		return r;
	}
	public String getText2(long t){
		tmp.setTimeInMillis(t);
		recycledDateFormat.applyPattern(pattern2);
		String r=recycledDateFormat.format(tmp.getTime());
		if (trunc2>=0) r=r.substring(0,trunc2);
		if (upperCase2) r=r.toUpperCase();
		return r;
	}
	
	
	
	public void toggleWidth(boolean normal){
		if ((minWidth==normalMinWidth)!=normal){
			if (normal) minWidth=normalMinWidth;
			else minWidth=tableMinWidth;
			updateRatio();
		}
	}
	
	public double getRatio() {
		if (ratio==-1.0) updateRatio();
		return ratio;
	}
	
	private void updateRatio(){
		minDuration=CalendarUtil.getMinDuration(calendarField1)*number1;
		ratio=((double)minWidth)/((double)minDuration);
	}
	
	public double toTime(double x){
		return x/getRatio();
	}
	
	public double toX(double t){
		return t*getRatio();
	}
	
	/**
	 * @param calendarField1 The calendarField1 to set.
	 */
	public void setCalendarField1(int calendarField1) {
		this.calendarField1 = calendarField1;
	}
	/**
	 * @param calendarField2 The calendarField2 to set.
	 */
	public void setCalendarField2(int calendarField2) {
		this.calendarField2 = calendarField2;
	}
	/**
	 * @param minWidth The minWidth to set.
	 */
	public void setNormalMinWidth(int normalMinWidth) {
		this.normalMinWidth = normalMinWidth;
		minWidth=normalMinWidth;
	}
	/**
	 * @param minWidth The minWidth to set.
	 */
	public void setNormalMinWidthChinese(int normalMinWidth) {
		if (Environment.isChinese()){ //overrides because it's called after setNormalMinWidth()
			this.normalMinWidth = normalMinWidth;
			minWidth=normalMinWidth;
		}
	}
	/**
	 * @param number1 The number1 to set.
	 */
	public void setNumber1(int number1) {
		this.number1 = number1;
	}
	/**
	 * @param number2 The number2 to set.
	 */
	public void setNumber2(int number2) {
		this.number2 = number2;
	}
	/**
	 * @param pattern1 The pattern1 to set.
	 */
	public void setPattern1(String pattern1) {
		this.pattern1 = pattern1;
	}
	/**
	 * @param pattern2 The pattern2 to set.
	 */
	public void setPattern2(String pattern2) {
		this.pattern2 = pattern2;
	}
	/**
	 * @param trunc1 The trunc1 to set.
	 */
	public void setTrunc1(int trunc1) {
		this.trunc1 = trunc1;
	}
	/**
	 * @param trunc2 The trunc2 to set.
	 */
	public void setTrunc2(int trunc2) {
		this.trunc2 = trunc2;
	}
	/**
	 * @param uppercase1 The uppercase1 to set.
	 */
	public void setUpperCase1(boolean upperCase1) {
		this.upperCase1 = upperCase1;
	}
	/**
	 * @param uppercase2 The uppercase2 to set.
	 */
	public void setUpperCase2(boolean upperCase2) {
		this.upperCase2 = upperCase2;
	}
	
	
	
	public void setTableMinWidth(int tableMinWidth) {
		this.tableMinWidth = tableMinWidth;
	}
	
	
	public Object clone(){
		TimeScale t=new TimeScale();
		t.minWidth=minWidth;
		t.tableMinWidth=tableMinWidth;
		t.normalMinWidth=normalMinWidth;
		t.minDuration=minDuration;
		t.number1=number1;
		t.number2=number2;
		t.calendarField1=calendarField1;
		t.calendarField2=calendarField2;
		t.ratio=ratio;
		t.pattern1=pattern1;
		t.pattern2=pattern2;
		t.trunc1=trunc1;
		t.trunc2=trunc2;
		t.upperCase1=upperCase1;
		t.upperCase2=upperCase2;
		return t;
	}
	
	/**
	 * Get millis between smallest intervals
	 * @return
	 */	
		public long getIntervalDuration() {
			return CalendarUtil.getMinDuration(calendarField1)*number1;
		}
	public int getMinWidth() {
		return minWidth;
	}
}
