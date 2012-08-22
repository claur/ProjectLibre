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
package com.projity.pm.assignment.functor;

import java.util.Collection;

import com.projity.algorithm.CollectionIntervalGenerator;
import com.projity.algorithm.IntervalValue;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.contour.AbstractContourBucket;
import com.projity.pm.assignment.contour.ContourBucketIntervalGenerator;
import com.projity.pm.assignment.contour.PersonalContourBucket;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.time.HasStartAndEnd;

/**
 * This functor adds buckets to a collection.  If an entire bucket is used, it is added as is, otherwise a new one is made.
 */
public class PersonalContourBuilderFunctor extends AssignmentFieldFunctor{
	private Collection collection;
	private long assignmentWork;
	private CollectionIntervalGenerator replacementGenerator;
	private long activeDate = 0;
	AbstractContourBucket previous = null;
	public static PersonalContourBuilderFunctor getInstance(Assignment assignment, WorkCalendar workCalendar, com.projity.pm.assignment.contour.ContourBucketIntervalGenerator contourBucketIntervalGenerator, CollectionIntervalGenerator replacementGenerator, Collection collection) {
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
