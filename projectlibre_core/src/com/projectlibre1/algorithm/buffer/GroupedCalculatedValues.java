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
package com.projectlibre1.algorithm.buffer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import org.apache.commons.lang.time.DateUtils;

import com.projectlibre1.pm.calendar.WorkCalendar;

/**
 * Calculated values that are grouped by time buckets, such as a histogram
 */
public class GroupedCalculatedValues implements CalculatedValues, Serializable {
	static final long serialVersionUID = 8900927827L;
	ArrayList values = new ArrayList(); //(x,y pairs) //TODO a set would be better because this is often sparse
	double yScale;
	private static final Double ZERO = new Double(0.0D);
//	boolean dayByDay;
	/**
	 * 
	 */
	public GroupedCalculatedValues(double yScale) {
		super();
		this.yScale = yScale;
	}
	
	public GroupedCalculatedValues() {
		this(1.0D);
	}

	public int size() {
		return values.size();
	}
	public void set(final int index, final long date, final long endDate, final double value, final WorkCalendar assignmentCalendar) {
		if (date == 0)
			return;
		Point point;
		if (index > values.size()-1) {
//			System.out.println("add index " + index + new java.util.Date(date) + " - " + new java.util.Date(endDate) + " value " + value);
			values.add(index, new Point(date,value));
		} else {
			point = (Point) values.get(index);
			if (point == null) {
				values.set(index, new Point(date,value));
//				System.out.println("add indexb " + index + new java.util.Date(date) + " - " + new java.util.Date(endDate) + " value " + value);
			} else {
				point.addValue(value);
//				System.out.println("add value " + index + new java.util.Date(date) + " - " + new java.util.Date(endDate) + " value " + value);
			}
		}
	}

	public Long getDate(int index) {
		Point point = (Point) values.get(index);
		if (point == null)
			return null;
		return new Long(point.date);
	}
	
	public void setValue(int index, double value) {
		Point point = (Point) values.get(index);
		if (point == null)
			return;
		point.value = value;
		
	}
	final public double getUnscaledValue(int index) {
		if (values.isEmpty()) {
			System.out.println("empty values in GroupedCalculatedValues");
			return 0;
		} else if (index >= values.size()) {
			System.out.println("index out of bounds in GroupedCalculatedValues " + index);
			return 0;
		}
		Point point = (Point) values.get(index);
		if (point == null)
			return 0;
		return point.value;
	}
	
	public Double getValue(int index) {
		if (values.isEmpty()) {
			System.out.println("empty values in GroupedCalculatedValues");
			return ZERO;
		} else if (index >= values.size()) {
			System.out.println("index out of bounds in GroupedCalculatedValues " + index);
			return ZERO;
		}
		Point point = (Point) values.get(index);
		if (point == null)
			return null;
		return new Double(point.value / yScale);
	}
	
	
	
	public void makeSeries(boolean cumulative, SeriesCallback callback) {
		Long[] d = new Long[values.size()];
		Double[] v = new Double[values.size()];
		Point point=null;
		//long lastDate=-10L;
		double sum = 0;
		//int deltai=0;
		for (int i = 0; i < values.size(); i++) {
			point = (Point) values.get(i);
//			lc hack to enable day by day values
//			if (dayByDay&&point.date-lastDate>DateUtils.MILLIS_PER_DAY+12*DateUtils.MILLIS_PER_HOUR){
//				if (lastDate>0L&&point.date-lastDate>2*DateUtils.MILLIS_PER_DAY+12*DateUtils.MILLIS_PER_HOUR)
//					callback.add(i+deltai++,lastDate+DateUtils.MILLIS_PER_DAY,0.0);
//				callback.add(i+deltai++,point.date-DateUtils.MILLIS_PER_DAY,0.0);
//			}
			callback.add(i/*+deltai*/,point.date,point.value + (cumulative ? sum : 0));
			sum += point.value;
//			lastDate=point.date;
		}
//		if (dayByDay&&point!=null) callback.add(values.size()+deltai,point.date+DateUtils.MILLIS_PER_DAY,0.0);
	}	
	
	public void makeRectilinearSeries(SeriesCallback callback) {
		double previous = 0.0D;
		Point point;
		for (int i = 0; i < values.size(); i++) {
			point = (Point) values.get(i);
			callback.add(2*i,point.getDate(),previous);
			previous = point.getValue();
			callback.add(2*i+1,point.getDate(),previous);
		}
	}
	
	
/**
 * Transforms values into cumulative values or back to non cumulative
 *
 */	public void makeCumulative(boolean cumulative) {
		double sum = 0;
		Point point;
		for (int i = 0; i < values.size(); i++) {
			point = (Point) values.get(i);
			if (cumulative) {
				sum += point.value;
				point.value = sum;
			} else {
				point.value -= sum;
				sum += point.value;
			}
		}
		
	}
 
 	public void dump() {
		for (int i = 0; i < values.size(); i++)
			System.out.println(i + " " +  new java.util.Date(getDate(i).longValue()) +" "+ getValue(i));
 	}
	
 	public ListIterator iterator(int index){
 		return values.listIterator(index);
 	}
 	public static GroupedCalculatedValues union(GroupedCalculatedValues values1,GroupedCalculatedValues values2){
 		GroupedCalculatedValues c1,c2;
 		if (values1.size()>=values2.size()){
 			c1=values1;
 			c2=values2;
 		}else{
 			c1=values2;
 			c2=values1;
 		}
 		GroupedCalculatedValues c=new GroupedCalculatedValues();
 		ListIterator i1=c1.values.listIterator();
 		ListIterator i2=c2.values.listIterator();
	 	Point p1,p2=null;
 		while(i1.hasNext()){
 			p1=(Point)i1.next();
 	 		while(i2.hasNext()){
 	 			p2=(Point)i2.next();
 	 			if (p2.date<p1.date){
 	 				c.values.add(p2);
 	 			}else if (p2.date>p1.date){
 	 				i2.previous();
 	 				break;
 	 			}else break;
 	 		}
 	 		if (p2!=null&&p1.date==p2.date) c.values.add(new Point(p1.date,p1.value+p2.value));
 	 		else c.values.add(p1);
 		}
 		while (i2.hasNext()){
 			c.values.add((Point)i2.next());
 		}
 		return c;
 	}
 	
 	public void mergeIn(GroupedCalculatedValues add){
 		ListIterator baseIterator = values.listIterator();
		ListIterator addIterator = add.values.listIterator();
 		Point basePoint = baseIterator.hasNext() ? (Point)baseIterator.next() : null;
 		long start = basePoint.date;
 		Point previousAddPoint = null;
 		Point addPoint = addIterator.hasNext() ? (Point)addIterator.next() : null;
 		while (basePoint != null && addPoint != null) {
 			//TODO handle overlaps
 			if (basePoint.compareTo(addPoint) >= 0) {
 				if (addPoint.date >= start) {
 					basePoint.value += addPoint.value;
 					if (basePoint.date == start && previousAddPoint != null) { // if first time
 						double proratedAmount = 
 							((double)addPoint.date - start)
							/ (addPoint.date - previousAddPoint.date);
 						if (proratedAmount > 0)
 							basePoint.value += (previousAddPoint.value * proratedAmount);
 					}
 				}
 				previousAddPoint = addPoint;
 	 			addPoint = addIterator.hasNext() ? (Point)addIterator.next() : null;
 	 			continue;
 			}
 			
 			
 			if (baseIterator.hasNext()) {
 				basePoint = (Point)baseIterator.next();
 			} else { 
 				if (previousAddPoint != null) {// handle end boundary
 					double proratedAmount = 
 							((double)(basePoint.date - previousAddPoint.date)) 
 							/ (addPoint.date - previousAddPoint.date);
 					if (proratedAmount > 0)
 						basePoint.value += (addPoint.value * proratedAmount);
 				}
 				basePoint = null;
 			}
 		}
 	}
 	public GroupedCalculatedValues dayByDayConvert(){
 		GroupedCalculatedValues c=new GroupedCalculatedValues();
		//c.setDayByDay(true);
 		for (Iterator i=values.iterator();i.hasNext();){
 			Point p=(Point)i.next();
 			c.values.add(new Point(p.date,p.value*DateUtils.MILLIS_PER_HOUR));
 		}
 		return c;
 	}

	public final ArrayList getValues() {
		return values;
	}
	
}
