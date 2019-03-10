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

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Factory;

import com.projectlibre1.pm.time.HasStartAndEnd;


/**
 * This class applies an action visitor closure over an interval and select clauses
 */
public class Query implements Factory, HasStartAndEnd {
	long start;
	long end;
	boolean hasGroupBy = false;

	private LinkedList selectFromClauses = new LinkedList();
	private IntervalGenerator groupByGenerator = null;
	private Closure actionVisitor = null;	

	/**
	 * The constructor is empty.  The query is built by chaining together parts of statement 
	 */
	private Query() {}

	public static Query getInstance() {
		return new Query();
	}
	
	public Query selectFrom(SelectFrom selectFrom) {
		selectFromClauses.add(selectFrom);
		return this;
	}
	
	public Query selectFrom(LinkedList selectFromClauses) {
		this.selectFromClauses = selectFromClauses;
		return this;
	}
	
	
	public Query groupBy(IntervalGenerator groupByGenerator) {
		if (groupByGenerator == null)
			return this;
		hasGroupBy = true;
		this.groupByGenerator = groupByGenerator;
		return this;
	}
	
	public Query action(Closure actionVisitor) {
		this.actionVisitor = actionVisitor;
		return this;
	}

	public IntervalGenerator[] execute() {
		if (groupByGenerator == null) 
			groupByGenerator = RangeIntervalGenerator.continuous();
		create();
		return null; //TODO return array of intervalgenerators
	}


	
	
	
	/**
	 * This is the main calculation function.  It will go thru all elements of the group by generator (if any) and
	 * call back the action visitor.
	 * Eventually, it will be capable of returning a generator which itself can be used in a subsequent query
	 */
	public Object create() {
		SelectFrom clause;
		do {

			// set range of this element
			start = groupByGenerator.currentStart();
			end = groupByGenerator.currentEnd();
//			System.out.println("query dates " + new java.util.Date(start) + " - " + new java.util.Date(end));		
			Iterator i = selectFromClauses.iterator();
			while (i.hasNext()) { // go thru select from clauses until they are used up
				clause = (SelectFrom) i.next();
				clause.initializeCalculations();
				if (!clause.calculate(start,end)) // if clause is used up, remove it so it won't be treated again
					i.remove();
			}
			
			if (start != 0L && actionVisitor != null)
				actionVisitor.execute(this);
			

			// in case where there is no specified group by, should stop when no more things to treat
			if (!hasGroupBy && selectFromClauses.isEmpty())
				break;
			
		} while (groupByGenerator.evaluate(this));
		return null;	//TODO add support for returning a generator
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
	/**
	 * @return Returns the groupByGenerator.
	 */
	public IntervalGenerator getGroupByGenerator() {
		return groupByGenerator;
	}
	
	public Object currentGroupByObject() {
		return groupByGenerator.current();
	}

	/**
	 * @return Returns the actionVisitor.
	 */
	public Closure getActionVisitor() {
		return actionVisitor;
	}

}

