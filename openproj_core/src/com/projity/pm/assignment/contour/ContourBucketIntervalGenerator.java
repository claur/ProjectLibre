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
package com.projity.pm.assignment.contour;
import com.projity.algorithm.IntervalGenerator;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.task.Project;

/**
 * Generator which goes through a work contour.  Will also treat gap due to a dependency
 */
public class ContourBucketIntervalGenerator implements IntervalGenerator {
	protected final int BEFORE_START = -1; 
	protected final int AFTER_END = -2;
	protected long consumedDuration = 0; // how much used up of planned duration - see DoubleContourBucketIntervalGenerator
	protected int index = BEFORE_START;
	long durationLeftUntilRemainingStartDependency = Long.MAX_VALUE;
	long start = 0;
	long end;
	long assignmentDuration;
	long assignmentActualDuration=0;
	private AbstractContourBucket[] contourBuckets = null;
	WorkCalendar workCalendar;
	long splitAtDuration = Long.MAX_VALUE;
	long splitDuration = 0;
	long remainingSplitBucketDuration = 0;
	AbstractContourBucket specialBucket = null;
	boolean didSplit = false;
	public static ContourBucketIntervalGenerator getInstance(Assignment assignment, Object type) {
		return new ContourBucketIntervalGenerator(assignment,type);		
	}
	
	protected ContourBucketIntervalGenerator(Assignment assignment, Object type) {
		workCalendar = assignment.getEffectiveWorkCalendar();
		contourBuckets = assignment.getContour(type);

		assignmentDuration = assignment.getDurationMillis();
		assignmentActualDuration = assignment.getActualDuration();
		long assignmentStart = assignment.getStart();
		end = assignmentStart; // treat 0th bucket as all time before contour begins
		
		Project project = assignment.getTask().getProject();
		if (project != null && !project.isForward()) //TODO need to figure out what to do if completion in reverse scheduled...
			return;
		
		if (assignment.getDependencyStart() > assignmentStart && assignment.getPercentComplete() > 0.0D) { // if a split caused by remaining work being pushed out by a dependency
			durationLeftUntilRemainingStartDependency = workCalendar.compare(assignment.getDependencyStart(),assignmentStart,false);
			if (durationLeftUntilRemainingStartDependency > 0) {
				if (durationLeftUntilRemainingStartDependency > assignmentActualDuration) {
					splitAtDuration = assignmentActualDuration;
					splitDuration = durationLeftUntilRemainingStartDependency - assignmentActualDuration;
				}
			}
		}
	}
	
	public long currentEnd() {
		return end;
	}
	
	public long currentStart() {
		return start;
	}
	
	/* (non-Javadoc)
	 * @see com.projity.algorithm.IntervalGenerator#current()
	 */
	public Object current() {
		AbstractContourBucket bucket = null;
		if (specialBucket != null)
			bucket = specialBucket;
		else if (index >= 0) // -1 is be
			bucket = contourBuckets[index];
		return bucket;
	}

	public boolean hasNext() {
		return index < contourBuckets.length-1;
	}
	boolean didFirstPart = false;
	/* (non-Javadoc)
	 * @see com.projity.algorithm.IntervalGenerator#next(com.projity.algorithm.ProblemSpace)
	 */
	public boolean evaluate(Object obj) {
		index++;
		if (index == contourBuckets.length)
			return false;
		start = workCalendar.add(end,0,false); // move to next working time, skipping non calendar time
		specialBucket = null;
		long bucketDuration = contourBuckets[index].getBucketDuration(assignmentDuration);
		consumedDuration += bucketDuration;

		if (consumedDuration >= splitAtDuration) {
			
			remainingSplitBucketDuration = consumedDuration - splitAtDuration; // for latter half of bucket
			
			bucketDuration -= remainingSplitBucketDuration;
			if (bucketDuration > 0) {
				specialBucket = PersonalContourBucket.getInstance(bucketDuration,contourBuckets[index].getUnits());
				index--; // need to repeat the last bucket
//				System.out.println("before split bucket" + DurationFormat.format(bucketDuration) + " " + this);
			} else {
//				System.out.println("before split -nothing before");
			}
			splitAtDuration = Long.MAX_VALUE;// we don't want to treat the start split or this again
			didFirstPart = true;
		} 
		if (specialBucket == null && didFirstPart) {
		
			if (didSplit == false) {
				bucketDuration = splitDuration;
//				System.out.println("in split" + DurationFormat.format(bucketDuration)+ " " + this);
				specialBucket = FillerContourBucket.getInstance(splitDuration);
				index--;
				didSplit = true;
			} else {
				bucketDuration = remainingSplitBucketDuration;
//				System.out.println("after split bucket" + DurationFormat.format(bucketDuration));
				double units =contourBuckets[index].getUnits();

				if (bucketDuration > 0)	
					specialBucket = PersonalContourBucket.getInstance(remainingSplitBucketDuration,units);
				remainingSplitBucketDuration = 0;
				didFirstPart = false;
			}
		}
//System.out.println("before " + new java.util.Date(end)  + " after " + new java.util.Date(workCalendar.add(end,bucketDuration,true)));
		end = workCalendar.add(end,bucketDuration,true);

		return true;
	}

//	/* (non-Javadoc)
//	 * @see com.projity.algorithm.IntervalGenerator#next(com.projity.algorithm.ProblemSpace)
//	 */
//	public boolean evaluate2(Object obj) {
//		index++;
//		if (index == contourBuckets.length)
//			return false;
//		start = workCalendar.add(end,0,false); // move to next working time, skipping non calendar time
//		specialBucket = null;
//		long bucketDuration = contourBuckets[index].getBucketDuration(assignmentDuration);
//		consumedDuration += bucketDuration;
//
//		if (consumedDuration > splitAtDuration) {
//System.out.println("consumedDuration > splitAtDuration");
//			remainingSplitBucketDuration = consumedDuration - splitAtDuration; // for latter half of bucket
//			consumedDuration = splitAtDuration - bucketDuration; // for next pass we want consumed==splitAt
//			
//			bucketDuration -= remainingSplitBucketDuration;
//			
//			specialBucket = PersonalContourBucket.getInstance(bucketDuration,contourBuckets[index].getUnits());
//			index--; // need to repeat the last bucket
//
//		} else if (consumedDuration == splitAtDuration) { // need to do dead time
//			System.out.println("consumedDuration == splitAtDuration");
//
//			bucketDuration = splitDuration;
//			specialBucket = FillerContourBucket.getInstance(bucketDuration);
//
//			index--;// need to repeat the last bucket
//			splitAtDuration = Long.MAX_VALUE; // we don't want to treat the start split or this again
//		} else if (remainingSplitBucketDuration > 0) {
//			System.out.println("remainingSplitBucketDuration > 0");
//			
//			bucketDuration = remainingSplitBucketDuration;
//			double units =contourBuckets[index].getUnits();
//// Turn off this handling.  It seems as if we don't want it after all			
////			if (units == 0) // split occurs during off time - reduce buckets duration to eliminate any off time that occurs during split
////				bucketDuration = Math.max(0,bucketDuration-splitDuration);// need to skip dead time during split duration
//
//			if (bucketDuration > 0)	
//				specialBucket = PersonalContourBucket.getInstance(remainingSplitBucketDuration,units);
//			remainingSplitBucketDuration = 0;
//		}
////		consumedDuration += bucketDuration;
//	
//		end = workCalendar.add(end,bucketDuration,true);
//
//		return true;
//	}

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

	/* (non-Javadoc)
	 * @see com.projity.algorithm.IntervalGenerator#isActive()
	 */
	public boolean isCurrentActive() {
		AbstractContourBucket cur = (AbstractContourBucket) current();
		return (cur != null) && (cur.getUnits() != 0.0);
	}

	/**
	 * @return Returns the workCalendar.
	 */
	public WorkCalendar getWorkCalendar() {
		return workCalendar;
	}

	/* (non-Javadoc)
	 * @see com.projity.algorithm.IntervalGenerator#canBeShared()
	 */
	public boolean canBeShared() {
		return false;
	}

}
