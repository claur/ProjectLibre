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

import java.util.ArrayList;
import java.util.LinkedList;

import com.projity.datatype.DurationFormat;
import com.projity.pm.time.MutableInterval;

/**
 * Abstract base class for work and cost contours
 * @stereotype strategy 
 */
public abstract class AbstractContour implements Cloneable{
	protected AbstractContourBucket[] contourBuckets = null;	
	protected double maxUnits = 0;
	public abstract int getType();
	public abstract boolean isPersonal();
	public abstract long calcTotalWork(long assignmentDuration);
	public AbstractContour adjustDuration(long newDuration, long actualDuration) {return this;} // only personal contours will treat this
	public AbstractContour adjustUnits(double multiplier, long startingFrom) {return this;} // only personal contours will treat this
	public AbstractContour contourAdjustWork(double multiplier, long actualDuration) {return this;}// only personal contours will treat this
	public AbstractContourBucket[] getContourBuckets() {
		return contourBuckets;
	}
	public abstract String getName();

	public int numBuckets() {
		if (contourBuckets == null)
			return 0;
		return contourBuckets.length;
	}
	
	private long calcSumBucketDuration(long assignmentDuration, boolean excludeNonWorkBuckets) {
		long duration = 0;
		for (int i=0; i < contourBuckets.length; i++) {
			if (contourBuckets[i] == null)
				System.out.println(toString(assignmentDuration));
			if (!excludeNonWorkBuckets || contourBuckets[i].getUnits() != 0.0) // do not add in durations for time off if excluding nonwork buckets
				duration += contourBuckets[i].getBucketDuration(assignmentDuration);
		}		
		return duration;
	}
	
	public long calcTotalBucketDuration(long assignmentDuration) {
		return calcSumBucketDuration(assignmentDuration,false);
	}
	
	public long calcWorkingBucketDuration(long assignmentDuration) {
		return calcSumBucketDuration(assignmentDuration,true);
	}
	
	protected double calcMaxUnits() {
		double units = 0.0;
		for (int i=0; i < contourBuckets.length; i++) {
			if (contourBuckets[i] != null) // in case called from constructor and array is unitialized
				units = Math.max(units,contourBuckets[i].getUnits());
		}
		return units;
	}
	
	
	/**
	 * Returns an array list containing elements of bucket array 
	 * @return
	 */
	public ArrayList toArrayList() {
		if (contourBuckets == null)
			return null;

		ArrayList list = new ArrayList(contourBuckets.length);
		for (int i=0; i < contourBuckets.length; i++) {
			list.add(contourBuckets[i]);
		}
		return list;
	}
	
	/**
	 * @return Returns the maxUnits.
	 */
	public double getMaxUnits() {
		return maxUnits;
	}

	public AbstractContour(AbstractContourBucket contourBuckets[]) {
		this.contourBuckets = contourBuckets;
		maxUnits = calcMaxUnits();
	}

/**
 * Returns a linked list of buckets that fall between two duration points.  Used when copying planned to actuals.
 * @param start
 * @param end
 * @param assignmentDuration
 * @return
 */
	public LinkedList bucketsBetweenDurations(long start, long end, long assignmentDuration) {
		LinkedList list = new LinkedList();
		AbstractContourBucket bucket = null;
		long currentEnd = 0;
		long currentStart = 0;
		AbstractContourBucket newBucket;
		for (int i=0; i < contourBuckets.length; i++) {
			bucket = contourBuckets[i];
			currentStart = currentEnd;
			currentEnd += bucket.getBucketDuration(assignmentDuration);
			if (currentEnd <= start) // if not at start yet, keep going
				continue;
			if (currentStart >= end) // if past end, stop
				break;
			// Add a new bucket that falls both within this bucket and the between range
			long newBucketDuration = Math.min(end,currentEnd) - Math.max(start,currentStart);
			list.add(PersonalContourBucket.getInstance(newBucketDuration,bucket.getUnits()));
		}
 		return list;
	}

	public String toString(long assignmentDuration) {
		StringBuffer result = new StringBuffer();
		if (contourBuckets == null)
			return null;
		for (int i=0; i < contourBuckets.length; i++) {
			result.append("bucket[" + i + "]");
			if (contourBuckets[i] == null)
				result.append(" NULL!");
			else
				result.append(" duration=" + DurationFormat.format(contourBuckets[i].getBucketDuration(assignmentDuration)) + " units " +  contourBuckets[i].getUnits() +"\n");
		}
		return result.toString();
	}
	
	public String toString() {
		return toString(0);
	}
	/**
	 * @param end
	 * @param extendDuration
	 * @return
	 */
	public abstract AbstractContour extend(long end, long extendDuration);
	/**
	 * @param startOffset
	 * @param extendDuration
	 * @return
	 */
	public abstract AbstractContour extendBefore(long startOffset, long extendDuration);
	
	public abstract MutableInterval getRangeThatIntervalCanBeMoved(long start, long end);	
	public AbstractContour removeFillerAfter(long atDuration) {
		return this;
	}
	
	/**
	 * Remove any starting empty bucket from the contour and return the duration of that bucket
	 * @return
	 */
	public long extractDelay() {
		return 0;
	}
	
	public Object clone() {
		try {
			AbstractContour c=(AbstractContour)super.clone();
			if (contourBuckets!=null){
				c.contourBuckets=new AbstractContourBucket[contourBuckets.length];
				for (int i=0;i<contourBuckets.length;i++){
					c.contourBuckets[i]=(AbstractContourBucket)contourBuckets[i].clone();
				}
			}
			return c;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
	
	public double getLastBucketUnits() {
		return contourBuckets[contourBuckets.length -1].getUnits();
	}


}
