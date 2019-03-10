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
package com.projectlibre1.algorithm;

import java.util.Collection;
import java.util.LinkedList;
import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.ChainedClosure;
import org.apache.commons.collections.functors.FalsePredicate;
import org.apache.commons.collections.functors.TruePredicate;

import com.projectlibre1.pm.time.HasStartAndEnd;

/**
 * The part of a query that will apply visitors over generators
 */
public class SelectFrom implements HasStartAndEnd {
	long start;
	long end;
	boolean finished = false;
	private IntervalGenerator generator = null;	
	boolean mustProcessAll = false;
	Closure fieldVisitors = null;
	CalculationVisitor[] fieldVisitorArray = null;
	Predicate wherePredicate = TruePredicate.INSTANCE;
	
	public static LinkedList selectFromListInstance() {
		return new LinkedList();	
	}

	public static LinkedList listInstance(SelectFrom a) {
		LinkedList list = new LinkedList();
		list.add(a);
		return list;
	}

	public static LinkedList listInstance(SelectFrom a, SelectFrom b) {
		LinkedList list = new LinkedList();
		list.add(a);
		list.add(b);		
		return list;
	}

	public static LinkedList listInstance(SelectFrom a, SelectFrom b, SelectFrom c) {
		LinkedList list = new LinkedList();
		list.add(a);
		list.add(b);		
		list.add(c);		
		return list;
	}

	public static LinkedList listInstance(SelectFrom a, SelectFrom b, SelectFrom c, SelectFrom d) {
		LinkedList list = new LinkedList();
		list.add(a);
		list.add(b);		
		list.add(c);
		list.add(d);				
		return list;
	}
	
	protected IntervalGeneratorSet fromGenerators = null;	
	
	public Collection getFromIntervalGenerators() {
		return fromGenerators.getGenerators();
	}
	
	public SelectFrom select(CalculationVisitor[] fieldVisitorArray) {
		this.fieldVisitorArray = fieldVisitorArray;
		this.fieldVisitors = new ChainedClosure(fieldVisitorArray);
		return this;
	}
	
	public SelectFrom select(CalculationVisitor fieldVisitor) {
		if (fieldVisitorArray != null) { // if already something there, add to it
			CalculationVisitor[] newArray =new CalculationVisitor[fieldVisitorArray.length +1];
			System.arraycopy(fieldVisitorArray,0,newArray,0,fieldVisitorArray.length);
			newArray[fieldVisitorArray.length] = fieldVisitor; // add new one to end
			return select(newArray);
		}
		fieldVisitorArray = new CalculationVisitor[] {fieldVisitor}; //make one element array
		this.fieldVisitors = fieldVisitor; // no need to make chained closure since only one element
		return this;
	}
	
	public SelectFrom all() {
		mustProcessAll = true; // must be set after from generators are set!
		return this;
	}
	
	public SelectFrom from(LinkedList fromGeneratorList) {
		if (fromGenerators == null) 
			this.fromGenerators = IntervalGeneratorSet.getInstance(fromGeneratorList);
		else
			this.fromGenerators.getGenerators().addAll(fromGeneratorList);
		return this;
	}
	

	public SelectFrom from(IntervalGenerator fromGenerator) {
		if (fromGenerators == null) 
			this.fromGenerators = IntervalGeneratorSet.getInstance(fromGenerator);
		else
			fromGenerators.getGenerators().add(fromGenerator);
		return this;
	}

	public SelectFrom where(Predicate wherePredicate) {
		this.wherePredicate = wherePredicate;
		return this;
	}
	
	public SelectFrom whereInRange(long start, long end) {
		if (start <= end) { // if non backwards range 
			// If there is already a range, intersect with it
			if (wherePredicate != null && wherePredicate instanceof DateInRangePredicate) {
				DateInRangePredicate range = (DateInRangePredicate)wherePredicate;
				range.limitTo(start,end);
				start = range.getStart();
				end = range.getEnd();
			} else {
				wherePredicate = DateInRangePredicate.getInstance(start,end);
			}
			from(RangeIntervalGenerator.betweenInstance(start,end)); // add a generator assuring the endpoints are treated corrctly
		} else { // take care in cases where range is invalid
			wherePredicate = FalsePredicate.INSTANCE;
		}

		return this;
	}
	
	/**
	 * Initializes all calculation totals for active field visitors.  This will set all non-cumulative ones to 0s
	 * Cumulative ones are not initialized
	 *
	 */
	public void initializeCalculations() {
		if (fieldVisitorArray == null)
			return;
		for (int i =0; i < fieldVisitorArray.length; i++ )
			fieldVisitorArray[i].initialize();
	}
	
	/**
	 * Put fields back to their 0 state. This is used when the clause is used up.  Cumulative fields as well.
	 *
	 */	
	public void resetCalculations() {
		if (fieldVisitorArray == null)
			return;
		for (int i =0; i < fieldVisitorArray.length; i++ )
			fieldVisitorArray[i].reset();
	}
	
	/**
	 * Calculate values in a range of times by calling each visitor on subranges until the range is complete.
	 * @param groupByStart start of calculation range.  currently unused!
	 * @param groupByEnd end of calculation range
	 * @return true if all of the from generators are still active, false if one of them has been used up.
	 */
	public boolean calculate(long groupByStart, long groupByEnd) {
		if (finished) {// if the last item of a generator was processed in previous call
			resetCalculations(); // since it is no longer active, should always return 0s from now on
			return false;
		}
		while (true) {
			if (generator == null) // will be null on first call, and after the previously active generator has been evaluated
				generator = fromGenerators.earliestEndingGenerator();

			if (generator == null) { //could be case if there are no from generatros at all TODO is this test needed?
				finished = true;
				break;
			}

			start = Math.max(start,generator.currentStart()); // if current generator was interrupted by ending a range, we need to start at point left off
			end = Math.min(groupByEnd,generator.currentEnd());
			if (end >= start) { // in cases where a clause starts in the middle, such as remaining work, end may be less than start at first
//	System.out.println("SelectFrom start" + new java.util.Date(start) + " end " + new java.util.Date(end) + " " + generator);			
				// evaluate fields
				boolean whereConditionMet = wherePredicate.evaluate(this);
				if (fieldVisitors != null) { 
					for (int i = 0; i < fieldVisitorArray.length; i++) {
						// if we are in the calculation range, or if the functor is cumulative
						if (whereConditionMet || fieldVisitorArray[i].isCumulative()) {
							fieldVisitorArray[i].execute(this);
						}
					}
				}
			}
			start = end; // for next iteration, shift start to current end
			
			if (end == groupByEnd) // at end of groupBy. 
				break;

			if (!generator.evaluate(this)) {
				if (mustProcessAll) { // if all froms must be treated
					fromGenerators.remove(generator);
					finished = fromGenerators.isEmpty(); // any left?
				} else {
					finished = true; // The next time calculate is called, it should return false
				}
				if (finished)
					break;
			}
			generator = null; // The current generator has been finished.  Will need to find earliest next time
		}
		return true;
	}
	
	/**
	 * 
	 */
	private SelectFrom() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Factory method
	 * @return
	 */
	public static SelectFrom getInstance() {
		return new SelectFrom();
	}
	/**
	 * @return Returns the end.
	 */
	public long getEnd() {
		return end;
	}

	/**
	 * @return Returns the start.
	 */
	public long getStart() {
		return start;
	}

	public String toString() {
		return "Select From where is " + wherePredicate;
	}
	public static final SelectFrom[] NOTHING = new SelectFrom[] {};
	
}
