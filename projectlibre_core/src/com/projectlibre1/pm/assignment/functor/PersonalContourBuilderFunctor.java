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
package com.projectlibre1.pm.assignment.functor;

import java.util.Collection;

import com.projectlibre1.algorithm.CollectionIntervalGenerator;
import com.projectlibre1.algorithm.IntervalValue;
import com.projectlibre1.pm.assignment.Assignment;
import com.projectlibre1.pm.assignment.contour.AbstractContourBucket;
import com.projectlibre1.pm.assignment.contour.ContourBucketIntervalGenerator;
import com.projectlibre1.pm.assignment.contour.PersonalContourBucket;
import com.projectlibre1.pm.calendar.WorkCalendar;
import com.projectlibre1.pm.time.HasStartAndEnd;

/**
 * This functor adds buckets to a collection.  If an entire bucket is used, it is added as is, otherwise a new one is made.
 */
public class PersonalContourBuilderFunctor extends AssignmentFieldFunctor{
	private Collection collection;
	private long assignmentWork;
	private CollectionIntervalGenerator replacementGenerator;
	private long activeDate = 0;
	AbstractContourBucket previous = null;
	public static PersonalContourBuilderFunctor getInstance(Assignment assignment, WorkCalendar workCalendar, com.projectlibre1.pm.assignment.contour.ContourBucketIntervalGenerator contourBucketIntervalGenerator, CollectionIntervalGenerator replacementGenerator, Collection collection) {
		return new PersonalContourBuilderFunctor(assignment, workCalendar, contourBucketIntervalGenerator, replacementGenerator, collection);
	}

	private PersonalContourBuilderFunctor(Assignment assignment, WorkCalendar workCalendar, ContourBucketIntervalGenerator contourBucketIntervalGenerator, CollectionIntervalGenerator replacementGenerator, Collection collection) {
		super(assignment, workCalendar, contourBucketIntervalGenerator);
		assignmentWork = assignment.calcWork(); //TODO verify if need work or duration
		this.replacementGenerator = replacementGenerator;
		this.collection = collection;
	}

	/**
	 * Add buckets to the collection.  The new interval has priority over the existing contour.  Buckets
	 * are re-used if they are identical.
	 */
	public void execute(Object object) {
		HasStartAndEnd interval = (HasStartAndEnd)object;
		
		if (interval.getStart() == 0) // ignore degenerate range 
			return;
		if (interval.getStart() == interval.getEnd())
			return;

		AbstractContourBucket bucket = null;
		long intervalDuration = 0;
//		System.out.println("--interval " + new java.util.Date(interval.getStart()) + " - " + new java.util.Date(interval.getEnd()));

		// if beginning a replacement interval
		if (replacementGenerator.isCurrentActive() && replacementGenerator.currentStart() == interval.getStart()) {
			intervalDuration = workCalendar.compare(replacementGenerator.getEnd(),replacementGenerator.getStart(), false); // get duration of new region
			
			// if inserting during a non-working time, need to adjust assignment calendar
			if (intervalDuration == 0) {
				assignment.addCalendarTime(interval.getStart(),interval.getEnd());
			}

			//need to shift start to make room for new ones 
			if (interval.getStart() < assignment.getStart()) {
				assignment.setStart(interval.getStart());
			}
			
			IntervalValue replacementIntervalValue = (IntervalValue)replacementGenerator.current();
			bucket = PersonalContourBucket.getInstance(intervalDuration,replacementIntervalValue.getValue()); // make a new bucket
			activeDate = replacementGenerator.currentEnd(); // ignore everything in the future until active date
	
		} else if (interval.getStart() >= activeDate) { // use contour bucket
			intervalDuration = workCalendar.compare(interval.getEnd(),interval.getStart(), false);
			if (intervalDuration == 0) // don't treat degenerate cased
				return;
			if (contourBucketIntervalGenerator.current() == null) { // if not active, then insert dead time
				bucket =PersonalContourBucket.getInstance(intervalDuration,0); // make a new non-workingbucket
			} else {
				bucket = (AbstractContourBucket) contourBucketIntervalGenerator.current();
				if (intervalDuration != bucket.getBucketDuration(assignmentWork)) // try to use existing bucket 
					bucket = PersonalContourBucket.getInstance(intervalDuration,bucket.getUnits()); // make a new bucket
				
			}
		}
		if (bucket == null) // if no bucket, then do nothing
			return;

		// merge with previous if units are identical
		if (previous != null && previous.getUnits() == bucket.getUnits()) {
			collection.remove(previous);
			bucket = PersonalContourBucket.getInstance(bucket.getBucketDuration(assignmentWork) + previous.getBucketDuration(assignmentWork),previous.getUnits());
		}

		collection.add(bucket);
		previous = bucket; // for merge
	}
}
