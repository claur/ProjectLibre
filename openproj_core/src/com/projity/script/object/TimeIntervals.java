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
package com.projity.script.object;

import java.io.Serializable;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;

import org.apache.commons.lang.time.DateUtils;

import com.projity.timescale.CalendarUtil;


public class TimeIntervals implements Serializable,Cloneable{
	static final long serialVersionUID = 18828392223063L;

	public static final int DAY=1;
	public static final int WEEK=2;
	public static final int MONTH=3;
	public static final int QUARTER=4;
	public static final int YEAR=5;
	public static final int ETERNITY=1000000;

	public static final int MIN_SCALE=1;
	public static final int MAX_SCALE=2;//3;

	protected static final int DEFAULT_WINDOW_COUNT=3;
	protected static final int WINDOW_INTERVALS=50;

	protected LinkedList<TimeWindow> win=new LinkedList<TimeWindow>();
	protected int scale=MIN_SCALE;
	protected int translation;
	protected int winId;
	protected float center;
	protected LinkedList<TimeWindow> history=new LinkedList<TimeWindow>();
	protected long start,end;

	public int getScale() {
		return scale;
	}
	public void setScale(int scale) {
		if (scale>=MIN_SCALE&&scale<=MAX_SCALE)
		this.scale = scale;
	}

	public LinkedList<TimeWindow> getWin() {
		return win;
	}

	public long getStart(){
		if (win.size()==0) return 0;
		else return win.getFirst().getS();
	}
	public long getEnd(){
		if (win.size()==0) return Long.MAX_VALUE;
		else return win.getLast().getE();
	}
	public TimeWindow getCenterWin(){
		int count=win.size();
		if (count==0) return null;
		int i=count/2;
		if (count%2==1) return win.get(i);
		else{
			TimeWindow t=new TimeWindow();
			t.setS(win.get(i-1).calculateCenter());
			t.setE(win.get(i).calculateCenter());
			return t;
		}

	}

	public int getTranslation() {
		return translation;
	}
	public void setTranslation(int translation) {
		this.translation = translation;
	}

	public float getCenter() {
		return center;
	}
	public void setCenter(float center) {
		this.center = center;
	}

	public void update() {
		if (win.size()>0) update(win.getFirst().getS(),end);
	}
	public void update(long start,long end) {
		update(start,end,DEFAULT_WINDOW_COUNT);
	}
	public void update(long start,long end,int winCount) { //winCount positive in this case
//		System.out.println("TimeIntervals.update");
		this.start=start;
		this.end=end;
		win.clear();
		history.clear();
		generateWindows(scale,start,0,Long.MAX_VALUE,winCount,win);
		indexWindows(winId,win);
		history.addAll(win);
	}

	protected static void indexWindows(int winId,LinkedList<TimeWindow> win){
		int i=winId;
		for (TimeWindow w:win) w.setId(i++);
	}

	public TimeIntervals translate(int winCount) { //TODO case winCount<0

//		for (TimeWindow w : history) System.out.println("history0: "+w);
//		for (TimeWindow w : win) System.out.println("win0: "+w);

		//for (TimeWindow w : history) System.out.println("id="+w.getId());
		TimeIntervals t=new TimeIntervals();
		t.setScale(scale);
		LinkedList<TimeWindow> twin=t.getWin();
		if (winCount==0||win.size()==0) return t; //or null
		if (winCount>0){
			t.winId=winId+win.size();
			int lastId=t.winId-1+winCount;
			int maxHistoryId=Math.min(history.getLast().getId(),lastId);
			int i=t.winId;
			if (i<=maxHistoryId){
				ListIterator<TimeWindow> it=history.listIterator();
				TimeWindow w;
				while (it.hasNext()){
					w=it.next();
					if (w.getId()==t.winId){
						it.previous();
						break;
					}
				}
				for(;i<=maxHistoryId&&it.hasNext();i++){
					w=it.next();
					twin.add(w);
//					System.out.println("Found in history: "+w);
				}
			}
			LinkedList<TimeWindow> newWin=new LinkedList<TimeWindow>();
			generateWindows(scale,(twin.size()>0?twin:win).getLast().getE(),start,end,lastId-i+1,newWin);
			t.indexWindows(t.winId+t.getWin().size(),newWin);
//			for (TimeWindow w : newWin) System.out.println("New window: "+w);
			t.getWin().addAll(newWin);
			history.addAll(newWin);
		}else{
			t.winId=winId-1;
			int lastId=t.winId+1+winCount;
			int minHistoryId=Math.max(history.getFirst().getId(),lastId);
			int i=t.winId;
			if (i>=minHistoryId){
				ListIterator<TimeWindow> it=history.listIterator(history.size()-1);
				TimeWindow w;
				while (it.hasPrevious()){
					w=it.previous();
					if (w.getId()==t.winId){
						it.next();
						break;
					}
				}
				for(;i>=minHistoryId;i--){
					w=it.previous();
					twin.addFirst(w);
//					System.out.println("Found in history: "+w);
				}
			}
//			System.out.println("winId="+winId+", t.winId="+t.winId+", lastId="+lastId+", i="+i+" minHistoryId="+minHistoryId);
			LinkedList<TimeWindow> newWin=new LinkedList<TimeWindow>();
			generateWindows(scale,(twin.size()>0?twin:win).getFirst().getS(),start,end,lastId-i-1,newWin);
			t.indexWindows(lastId,newWin);
//			for (TimeWindow w : newWin) System.out.println("New window: "+w);
			t.getWin().addAll(0,newWin);
			history.addAll(0,newWin);
		}

		int translation=0;
		for (TimeWindow w : t.getWin()){
			if (winCount>0){
				win.removeFirst();
				win.addLast(w);
				translation++;
			}else{
				win.removeLast();
				win.addFirst(w);
				translation--;
			}
		}
		winId=winId+translation;
		t.setTranslation(translation);

//		for (TimeWindow w : history) System.out.println("history1: "+w);
//		for (TimeWindow w : win) System.out.println("win1: "+w);
//		for (TimeWindow w : twin) System.out.println("t.win1: "+w);

		return t;
	}






	public static int generateWindows(int scale, long ref,long start,long end,int winCount,LinkedList<TimeWindow> windows) {
		TimeWindow win,lastWin=null;
		if (winCount>0){
			for (int i=0;i<=winCount;i++){
				win=generateWindow(ref, scale, 1);
				//if (win.getS()>end) return i;
				if (lastWin!=null){
					lastWin.setE(win.getS());
					windows.add(lastWin);
				}
				ref=win.getE();
				lastWin=win;
			}
		}else{
			for (int i=0;i>=winCount;i--){
				win=generateWindow(ref, scale, -1);
				//if (win.getE()<start) return i;
				if (lastWin!=null){
					lastWin.setS(win.getE());
					windows.addFirst(lastWin);
				}
				ref=win.getS();
				lastWin=win;
			}
		}
		return winCount;
	}

	//not idempotent, need history to undo
	public static TimeWindow generateWindow(long start,int scale,int sign) {
		int timeType,timeType2=0,number2;
		int timeIncrement=1,timeIncrement2=1;
		switch (scale) {
		case TimeIntervals.DAY:
			timeType=Calendar.DAY_OF_MONTH;
			timeType2=Calendar.WEEK_OF_YEAR;
			break;
		case TimeIntervals.WEEK:
			timeType=Calendar.WEEK_OF_YEAR;
			timeType2=Calendar.MONTH;
			break;
		case TimeIntervals.MONTH:
			timeType=Calendar.MONTH;
			timeType2=Calendar.MONTH;
			timeIncrement2=3;
			break;
		case TimeIntervals.QUARTER:
			timeType=Calendar.MONTH;
			timeType2=Calendar.YEAR;
			timeIncrement=3;
			break;
		case TimeIntervals.YEAR:
			timeType=Calendar.YEAR;
			timeType2=Calendar.YEAR;
			break;
		default:
			return null;
		}

		Calendar c=Calendar.getInstance(DateUtils.UTC_TIME_ZONE, Locale.US);//DateTime.calendarInstance();
		c.setTimeInMillis(start);

		//adapt start
		floorCal(scale, c);
		long s1=c.getTimeInMillis();
		floorCal(scale+1, c);
		long s2=c.getTimeInMillis();

		c.setTimeInMillis(s1);
		long s;
		while ((s=c.getTimeInMillis())>=s2){ //can occur with week, month scale
			s1=s;
			c.add(timeType, -timeIncrement);
		}

		//set approximative end
		c.setTimeInMillis(s1);
		c.add(timeType,sign*timeIncrement*WINDOW_INTERVALS);
		TimeWindow win=new TimeWindow();
		if (sign>0) win.setS(s1);
		else win.setE(s1);
		if (sign>0) win.setE(c.getTimeInMillis());
		else win.setS(c.getTimeInMillis());
		return win;
	}



	private static void floorCal(int scale,Calendar c){
		switch (scale) {
		case TimeIntervals.DAY:
			CalendarUtil.dayFloor(c);
			break;
		case TimeIntervals.WEEK:
			CalendarUtil.weekFloor(c);
			break;
		case TimeIntervals.MONTH:
			CalendarUtil.monthFloor(c);
			break;
		case TimeIntervals.QUARTER:
			CalendarUtil.monthFloor(c);
			c.set(Calendar.MONTH,(c.get(Calendar.MONTH)/3)*3);
			break;
		case TimeIntervals.YEAR:
			CalendarUtil.yearFloor(c);
			break;
		}
	}


	public Object clone(){
		try {
			TimeIntervals t=(TimeIntervals)super.clone();
			t.win=new LinkedList<TimeWindow>();
			t.history=new LinkedList<TimeWindow>();
			for (TimeWindow w:win) t.win.add(w);
			for (TimeWindow w:history) t.history.add(w);
			return t;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}

	}


}
