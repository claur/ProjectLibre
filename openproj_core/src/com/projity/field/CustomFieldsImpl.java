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
package com.projity.field;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;


/**
 * Custom fields and their bounds.  The bounds are set from config file by corresponding fields
 */
public class CustomFieldsImpl implements CustomFields, Serializable, Cloneable {
	public static int NUM_COST = 10;
	public static int NUM_DATE = 10;
	public static int NUM_DURATION = 10;
	public static int NUM_FINISH = 10;
	public static int NUM_FLAG = 20;
	public static int NUM_NUMBER = 20;
	public static int NUM_START = 10;	
	public static int NUM_TEXT = 30;
	
	
	protected double cost[];
	protected long date[];
	protected long duration[];
	protected long finish[];
	protected boolean flag[];
	protected double number[];
	protected long start[];
	protected String text[];
				  
	public void serialize(ObjectOutputStream s) throws IOException {
	    s.writeObject(cost);
	    s.writeObject(date);
	    s.writeObject(duration);
	    s.writeObject(finish);
	    s.writeObject(flag);
	    s.writeObject(number);
	    s.writeObject(start);
	    s.writeObject(text);
	}
	
	//call init to complete initialization
	public static CustomFieldsImpl deserialize(ObjectInputStream s) throws IOException, ClassNotFoundException  {
		CustomFieldsImpl c=new CustomFieldsImpl();
	    c.cost=(double[])s.readObject();
	    c.date=(long[])s.readObject();
	    c.duration=(long[])s.readObject();
	    c.finish=(long[])s.readObject();
	    c.flag=(boolean[])s.readObject();
	    c.number=(double[])s.readObject();
	    c.start=(long[])s.readObject();
	    c.text=(String[])s.readObject();
	    return c;
	}
	
	public Object clone(){
		CustomFieldsImpl cf=new CustomFieldsImpl();
		
		if (cost==null) cf.cost=null;
		else{
			cf.cost=new double[cost.length];
			System.arraycopy(cost,0,cf.cost,0,cost.length);
		}
		if (date==null) cf.date=null;
		else{
			cf.date=new long[date.length];
			System.arraycopy(date,0,cf.date,0,date.length);
		}	
		if (duration==null) cf.duration=null;
		else{
			cf.duration=new long[duration.length];
			System.arraycopy(duration,0,cf.duration,0,duration.length);
		}
		if (finish==null) cf.finish=null;
		else{
			cf.finish=new long[finish.length];
			System.arraycopy(finish,0,cf.finish,0,finish.length);
		}		
		if (flag==null) cf.flag=null;
		else{
			cf.flag=new boolean[flag.length];
			System.arraycopy(flag,0,cf.flag,0,flag.length);
		}
		if (number==null) cf.number=null;
		else{
			cf.number=new double[number.length];
			System.arraycopy(number,0,cf.number,0,number.length);
		}
		if (start==null) cf.start=null;
		else{
			cf.start=new long[start.length];
			System.arraycopy(start,0,cf.start,0,start.length);
		}
		if (text==null) cf.text=null;
		else{
			cf.text=new String[text.length];
			for (int i=0;i<text.length;i++)
				cf.text[i]=text[i]==null?null:new String(text[i]);
		}
		return cf;
	}
	
	public CustomFieldsImpl() {
	}
	
	public double getCustomCost(int i) {
		if (cost == null)
			return 0.0;
		if (cost.length!=NUM_COST)
			cost=Arrays.copyOf(cost, NUM_COST);
		return cost[i];
	}
	public void setCustomCost(int i, double cost) {
		if (this.cost == null)
			this.cost = new double[NUM_COST];
		if (this.cost.length!=NUM_COST)
			this.cost=Arrays.copyOf(this.cost, NUM_COST);
		this.cost[i] = cost;
	}
	public long getCustomDate(int i) {
		if (date == null)
			return 0;
		if (date.length!=NUM_DATE)
			date=Arrays.copyOf(date, NUM_DATE);
		return date[i];
	}
	public void setCustomDate(int i, long date) {
		if (this.date == null)
			this.date = new long[NUM_DATE];
		if (this.date.length!=NUM_DATE)
			this.date=Arrays.copyOf(this.date, NUM_DATE);
		this.date[i] = date;
	}
	public long getCustomDuration(int i) {
		if (duration == null)
			return 0;
		if (duration.length!=NUM_DURATION)
			duration=Arrays.copyOf(duration, NUM_DURATION);
		return duration[i];
	}
	public void setCustomDuration(int i, long duration) {
		if (this.duration == null)
			this.duration = new long[NUM_DATE];
		if (this.duration.length!=NUM_DURATION)
			this.duration=Arrays.copyOf(this.duration, NUM_DURATION);
		this.duration[i] = duration;
	}
	public long getCustomFinish(int i) {
		if (finish == null)
			return 0;
		if (finish.length!=NUM_FINISH)
			finish=Arrays.copyOf(finish, NUM_FINISH);
		return finish[i];
	}
	public void setCustomFinish(int i, long finish) {
		if (this.finish == null)
			this.finish = new long[NUM_FINISH];
		if (this.finish.length!=NUM_FINISH)
			this.finish=Arrays.copyOf(this.finish, NUM_FINISH);
		this.finish[i] = finish;
	}
	public boolean getCustomFlag(int i) {
		if (flag == null)
			return false;
		if (flag.length!=NUM_FLAG)
			flag=Arrays.copyOf(flag, NUM_FLAG);
		return flag[i];
	}
	public void setCustomFlag(int i, boolean flag) {
		if (this.flag == null)
			this.flag = new boolean[NUM_FLAG];
		if (this.flag.length!=NUM_FLAG)
			this.flag=Arrays.copyOf(this.flag, NUM_FLAG);
		this.flag[i] = flag;
	}
	public double getCustomNumber(int i) {
		if (number == null)
			return 0.0D;
		if (number.length!=NUM_NUMBER)
			number=Arrays.copyOf(number, NUM_NUMBER);
		return number[i];
	}
	public void setCustomNumber(int i, double number) {
		if (this.number == null)
			this.number = new double[NUM_NUMBER];
		if (this.number.length!=NUM_NUMBER)
			this.number=Arrays.copyOf(this.number, NUM_NUMBER);
		this.number[i] = number;
	}
	public long getCustomStart(int i) {
		if (start == null || i>= start.length)
			return 0;
		if (start.length!=NUM_START)
			start=Arrays.copyOf(start, NUM_START);
		return start[i];
	}
	public void setCustomStart(int i, long start) {
		if (this.start == null)
			this.start = new long[NUM_START];
		if (this.start.length!=NUM_START)
			this.start=Arrays.copyOf(this.start, NUM_START);
		this.start[i] = start;
	}
	public String getCustomText(int i) {
		if (text == null)
			return null;
		if (text.length!=NUM_TEXT)
			text=Arrays.copyOf(text, NUM_TEXT);
		return text[i];
	}
	public void setCustomText(int i, String text) {
		if (this.text == null)
			this.text = new String[NUM_TEXT];
		if (this.text.length!=NUM_TEXT)
			this.text=Arrays.copyOf(this.text, NUM_TEXT);
		this.text[i] = text;
	}
	
}
