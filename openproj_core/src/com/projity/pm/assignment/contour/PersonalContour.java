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
import java.util.Collection;

import com.projity.pm.time.MutableInterval;
import com.projity.strings.Messages;

/**
 * @stereotype strategy 
 */
public class PersonalContour extends AbstractContour {
	private static final String name = Messages.getString("PersonalContour.personal"); //$NON-NLS-1$
	public boolean isPersonal() {return true;}
	/**
	 * Calculates total work 
	 */
	public long calcTotalWork(long assignmentDuration) {
		long work = 0;
		for (int i=0; i < contourBuckets.length; i++)
			work += ((PersonalContourBucket)contourBuckets[i]).calcWork();
		return work;
	}
	

	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.contour.AbstractContour#getType()
	 */
	public int getType() {
		return ContourTypes.CONTOURED;
	}
	/**
	 * 
	 */
	private PersonalContour(AbstractContourBucket contourBuckets[]) {
		super(contourBuckets);
	}

	public static PersonalContour makePersonal(AbstractContour contour, long assignmentDuration) {
		if (contour.isPersonal())
			return (PersonalContour) contour;
		AbstractContourBucket newBuckets[] = new AbstractContourBucket[contour.contourBuckets.length];
		StandardContourBucket bucket;
		for (int i=0; i < contour.contourBuckets.length; i++) {
			bucket = (StandardContourBucket)contour.contourBuckets[i];
			newBuckets[i] = PersonalContourBucket.getInstance(bucket.getBucketDuration(assignmentDuration), bucket.getUnits());
		}
		PersonalContour newOne = getInstance(newBuckets);
		return newOne;
	}

	public static PersonalContour constructUsingSizeOf(AbstractContour from) {
		PersonalContour newOne = getInstance(new AbstractContourBucket[from.contourBuckets.length]);
		newOne.maxUnits = newOne.calcMaxUnits();
		return newOne;
	}
	
	public static PersonalContour getInstance(Collection list) {
		AbstractContourBucket newBuckets[] = new AbstractContourBucket[list.size()];
		list.toArray(newBuckets);
		PersonalContour newContour = PersonalContour.getInstance(newBuckets);
		newContour.validate();
		return newContour;
	}
	
	public void validate() {
		for (int i = 0; i < this.contourBuckets.length; i++)
			if (contourBuckets[i].getBucketDuration(0) < 0 || contourBuckets[i].getUnits()< 0)
				System.out.println("error neg bucket!\n" + this.toString(0));
	}
	
	public static PersonalContour getInstance(AbstractContourBucket[] contourBuckets) {
		return new PersonalContour(contourBuckets);
	}
	
/**
 * Add empty duration bucket either before or after
 * @param from
 * @param duration
 * @param after
 * @return
 */	public static PersonalContour addEmptyBucket(AbstractContour from, long duration, boolean after) {
		int oldLength = from.contourBuckets.length;
		PersonalContour newContour = getInstance(new AbstractContourBucket[oldLength+1]);
		
		System.arraycopy(from.contourBuckets,0,newContour.contourBuckets,after ? 0 : 1,oldLength); // replace old with new and truncate to correct length
		newContour.contourBuckets[after ? oldLength : 0] = PersonalContourBucket.getInstance(duration,0.0);
		return newContour;
	}
	
 
 /**
	 * Set the duration of the personal contour. This implies either truncating the bucket array or changing the last bucket
	 * to accommodate the new duration. Note that only the last bucket will be modified.  The number of buckets will never increase.
	 * Since the buckets themselves are immutable, the last element will most likely be replaced.
	 * @param newDuration: The new duration to set to.
	 */	
	public AbstractContour adjustDuration(long newDuration, long actualDuration) {
		//TODO incorporate actual duration
		
		
		PersonalContour newContour = constructUsingSizeOf(this);
		
		PersonalContourBucket bucket = null;
		for (int i=0; i < contourBuckets.length; i++) {
			bucket = (PersonalContourBucket)contourBuckets[i];
			newContour.contourBuckets[i] = bucket;
			newDuration -= bucket.getDuration();
			if (newDuration <= 0) {
				newContour.contourBuckets[i] = bucket.adjustDuration(newDuration);// adding a negative value
				if (i < contourBuckets.length -1 ) // if shortening number of buckets
					System.arraycopy(newContour.contourBuckets,0,newContour.contourBuckets,0,i+1); // replace old with new and truncate to correct length
		
				AbstractContour result = newContour.makePacked(); // pack so as to get rid of any trailing empty buckets
				return result;
			}
		}
		// extend last bucket to account for duration
		newContour.contourBuckets[contourBuckets.length-1] = bucket.adjustDuration(newDuration);
		return newContour;
	}

//	private AbstractContour makeFlatIfPossible() {
//		if (contourBuckets.length == 1)
//			contourBuckets[0].units
//	}

	/**
	 * Gets all buckets before a duration, cutting a bucket in half if needed.  Will return empty list if at start
	 * @param atDuration
	 * @param newBucket
	 * @return
	 */	
	private ArrayList bucketsBeforeDuration(long atDuration) {
		ArrayList newList = new ArrayList();
		boolean inserted = false;
		PersonalContourBucket bucket = null;
		long cursorDuration = 0;
		for (int i=0; i < contourBuckets.length; i++) {
			bucket = (PersonalContourBucket)contourBuckets[i];
			cursorDuration += bucket.getDuration();
			if (cursorDuration < atDuration) { // if already treated or before start, just use existing one
				newList.add(bucket);
			} else {
				long fractionedBucketDuration = atDuration - (cursorDuration - bucket.getDuration());
				if (fractionedBucketDuration > 0) {
					if (bucket.isFiller())
						newList.add(FillerContourBucket.getInstance(fractionedBucketDuration));
					else
						newList.add(PersonalContourBucket.getInstance(fractionedBucketDuration,bucket.getUnits()));
				}
				break;
			}
		}
		return newList;
	}		

	private ArrayList bucketsAfterDuration(long atDuration, boolean excludeFiller) {
		ArrayList newList = new ArrayList();
		boolean inserted = false;
		PersonalContourBucket bucket = null;
		long cursorDuration = 0;
		for (int i=0; i < contourBuckets.length; i++) {
			bucket = (PersonalContourBucket)contourBuckets[i];
			cursorDuration += bucket.getDuration();
			if (excludeFiller && bucket.isFiller()) // see if should skip filler buckets
				continue;
			if (cursorDuration > atDuration) { // if already treated or before start, just use existing one
				if (cursorDuration - bucket.getDuration() >= atDuration) {
					newList.add(bucket); // if start of this bucket is past atDuration, just add it
				} else {
					long fractionedBucketDuration = cursorDuration - atDuration;
					if (fractionedBucketDuration > 0)
						newList.add(PersonalContourBucket.getInstance(fractionedBucketDuration,bucket.getUnits()));
				}
			}
		}
		return newList;
	}	
	
	/**
	 * Removes any buckets having filler status after a given date.  This is used in the obscure case where we uncomplete
	 * work and we want to eliminate gaps that exist due to dependency dates.  These gaps should not be considered part of the contour.
	 * @param atDuration
	 * @return
	 */
	public AbstractContour removeFillerAfter(long atDuration) {
		ArrayList newList = bucketsBeforeDuration(atDuration);
		newList.addAll(bucketsAfterDuration(atDuration,false)); // exclude filler
		return getInstance(newList).makePacked();
	}
/**
 * Given an interval as start and end, determine the range which this interval can move in.
 * @param start
 * @param end
 * @return an interval which is a superset of the start,end interval
 */	public MutableInterval getRangeThatIntervalCanBeMoved(long start, long end) {
		ArrayList tempList = bucketsBeforeDuration(start);
		long startConstraint = start;
		long endConstraint = Long.MAX_VALUE; // by default unbounded 
		if (tempList.size() > 0) {
			PersonalContourBucket bucket = (PersonalContourBucket) tempList.get(tempList.size()-1);
			if (bucket.getUnits() == 0)
				startConstraint = start - bucket.getDuration();
		}
		tempList.clear();
		tempList = bucketsAfterDuration(end, false);
		if (tempList.size() > 0) {
			PersonalContourBucket bucket = (PersonalContourBucket) tempList.get(0);
			if (bucket.getUnits() == 0)
				endConstraint = end + bucket.getDuration();
			else
				endConstraint = end;
		}
		return new MutableInterval(startConstraint,endConstraint);

	}
	
	public PersonalContour setInterval(long startDuration, long endDuration, double units) {
		ArrayList newList = new ArrayList();
		newList.addAll(bucketsBeforeDuration(startDuration));
		newList.add(PersonalContourBucket.getInstance(endDuration - startDuration, units));
		newList.addAll(bucketsAfterDuration(endDuration, false));
		return getInstance(newList).makePacked();
	}
	
	
/**
 * Inserts a bucket into the contour.  If the bucket is past the end, it is not inserted 
 * @param atDuration
 * @param newBucket
 * @return
 */	public PersonalContour insertBucket(long atDuration, AbstractContourBucket newBucket) {
		ArrayList newList = new ArrayList();
		boolean inserted = false;
		PersonalContourBucket bucket = null;
		long cursorDuration = 0;
		for (int i=0; i < contourBuckets.length; i++) {
			bucket = (PersonalContourBucket)contourBuckets[i];
			cursorDuration += bucket.getDuration();
			if (inserted || cursorDuration < atDuration) { // if already treated or before start, just use existing one
				newList.add(bucket);
			} else {
				long d = cursorDuration - atDuration;
				newList.add(PersonalContourBucket.getInstance(d,bucket.getUnits()));
				newList.add(newBucket); // do this bucket after
				newList.add(PersonalContourBucket.getInstance(bucket.getDuration()-d,bucket.getUnits()));				
				inserted = true;
			}
		}
		return getInstance(newList).makePacked();
	}	
	
// 	public PersonalContour setContiguousDuration(long startOfRegion, long duration) {
// 		
// 		//not yet implemented
//		ArrayList newList = new ArrayList();
//		boolean inserted = false;
//		PersonalContourBucket bucket = null;
//		long cursorDuration = 0;
//		long regionDuration = 0;
//		for (int i=0; i < contourBuckets.length; i++) {
//			bucket = (PersonalContourBucket)contourBuckets[i];
//			cursorDuration += bucket.getDuration();
//			if (inserted || cursorDuration < startOfRegion) { // if already treated or before start, just use existing one
//				newList.add(bucket);
//			} else { // start of region
//				regionDuration += bucket.getDuration();
//				if (regionDuration < duration)
//					newList.add(bucket);
//				
//				
//				
//				long d = cursorDuration - startOfRegion;
//				if (d > 0)
//					newList.add(PersonalContourBucket.getInstance(d,bucket.getUnits()));
//				// at region now
//				//newList.add(newBucket); // do this bucket after
//				newList.add(PersonalContourBucket.getInstance(bucket.getDuration()-d,bucket.getUnits()));				
//				inserted = true;
//			}
//		}
//		return getInstance(newList).makePacked();
// 		
// 	}
 
/**
 * Extend a bucket by a duration
 * @param atDuration - point on bucket to extend
 * @param extendDuration - amount to extend - can be negative to contract
 * @return - new contour
 */	public PersonalContour extendBucket(long atDuration, long extendDuration) {
		PersonalContour newContour = constructUsingSizeOf(this);
		PersonalContourBucket bucket = null;
		boolean extended = false;
		long cursorDuration = 0;
		for (int i=0; i < contourBuckets.length; i++) {
			bucket = (PersonalContourBucket)contourBuckets[i];
			newContour.contourBuckets[i] = bucket; // use existing bucket by default
			cursorDuration += bucket.getDuration();
			if (!extended && cursorDuration >= atDuration) { // if the current bucket overlaps the point
				if (bucket.getUnits() != 0) { // if it is a working bucket, then skip it
					extended = true;
					newContour.contourBuckets[i] = PersonalContourBucket.getInstance(Math.max(0,bucket.getDuration() + extendDuration),bucket.getUnits());
				}
			}
		}
		return newContour.makePacked();		
	}	

/**
 * Shift an interval (usually a bar) to the right or left
 * @param start - start of interval (expressed as an offset from the start) 
 * @param end - end of interval (expressed as an offset from the start)
 * @param shiftDuration (if positive, shifting right, negative means shifting left)
 * @return
 */	public PersonalContour shift(long start, long end, long shiftDuration) {
		if (shiftDuration == 0)
			return this;
		ArrayList newList = new ArrayList();
		if (shiftDuration > 0) {
			// we are shifting to right, so remove a period corresponding to shiftDuration immediately after the interval
			newList.addAll(bucketsBeforeDuration(end));
			newList.addAll(bucketsAfterDuration(end+shiftDuration, false));
			
			PersonalContour temp = getInstance(newList).makePacked();
			newList.clear();
			
			//Now add in a non-work period before the start
			newList.addAll(temp.bucketsBeforeDuration(start));
			newList.add(PersonalContourBucket.getInstance(shiftDuration,0));
			newList.addAll(temp.bucketsAfterDuration(start, false)); 
		} else {
			// we will be shifting to the left, so add padding after the interval so later bars will stay fixed
			newList.addAll(bucketsBeforeDuration(end));
			newList.add(PersonalContourBucket.getInstance(-shiftDuration,0)); // note shift duration is negative
			newList.addAll(bucketsAfterDuration(end, false));

			PersonalContour temp = getInstance(newList).makePacked();
			newList.clear();
			// Now remove the period corresponding to shiftDuration before the bar
			newList.addAll(temp.bucketsBeforeDuration(start+shiftDuration)); // note shift duration is negative
			newList.addAll(temp.bucketsAfterDuration(start, false));
		}
		PersonalContour result =getInstance(newList).makePacked(); 
		return result;
	}	

	public AbstractContour extend(long end, long extendDuration) {
		if (extendDuration == 0)
			return this;
		PersonalContour result;
		ArrayList newList = new ArrayList();
		if (extendDuration > 0) {
			newList.addAll(bucketsBeforeDuration(end));
			newList.addAll(bucketsAfterDuration(end+extendDuration, false));
			PersonalContour temp = getInstance(newList).makePacked();
			result = temp.extendBucket(end,extendDuration); // extend interval
		} else {
			newList.addAll(bucketsBeforeDuration(end+extendDuration));
			newList.add(PersonalContourBucket.getInstance(-extendDuration,0)); // whitespace replaces area removed by shortening
			newList.addAll(bucketsAfterDuration(end, false));
			result = getInstance(newList).makePacked();
		}
		return result;
	}

	public AbstractContour extendBefore(long start, long extendDuration) {
		if (extendDuration == 0)
			return this;
		PersonalContour result;
		ArrayList newList = new ArrayList();
		PersonalContour temp = extendBucket(start,-extendDuration); // extend at point
		if (extendDuration < 0) {
			newList.addAll(temp.bucketsBeforeDuration(start+extendDuration)); // add all up to new start - extend duration is neg
			newList.addAll(temp.bucketsAfterDuration(start, false));
		} else {
			newList.addAll(temp.bucketsBeforeDuration(start));
			newList.add(PersonalContourBucket.getInstance(extendDuration,0)); // whitespace replaces area removed by shortening
			newList.addAll(temp.bucketsAfterDuration(start, false));

		}
		result = getInstance(newList).makePacked();		
		return result;
	}	
/**
 * Return an optimized contour that has no superflous info
 * @return
 */	private PersonalContour makePacked() {
		ArrayList newList = new ArrayList();
		PersonalContourBucket previous = null;
		PersonalContourBucket bucket = null;
		// go thru each bucket
		for (int i=0; i < contourBuckets.length; i++) {
			bucket = (PersonalContourBucket) contourBuckets[i];
			if (bucket == null) {
				System.out.println("null bucket " + i + " in make packed - skipping");
				continue;
			}
			if (previous != null && previous.getUnits() == bucket.getUnits()  && previous.isFiller() == bucket.isFiller()) { // if matches previous bucket, make new bucket
				previous = PersonalContourBucket.getInstance(previous.getDuration() + bucket.getDuration(),bucket.getUnits());
			} else {
				if (previous != null) {
					if (previous.getDuration() > 0) {// ignore 0 length buckets
						newList.add(previous); // doesnt match, so add it
					}
				}
				previous = bucket;
			}
			
		}
		if (previous != null && previous.getDuration() > 0 && previous.getUnits() != 0.0) // ignore 0 length buckets or ones with 0 units at end
			newList.add(previous); // add the last one - it will not have been added yet
		if (newList.isEmpty())
			newList.add(PersonalContourBucket.getInstance(0,1.0)); // add empty bucket with no duration
		return getInstance(newList);
		
	}
	
 	public AbstractContour convertToFlatIfPossible() {
 		if (getContourBuckets().length > 1)
 			return this;
 		else
 			return StandardContour.FLAT_CONTOUR;
 	}
	/**
	 * Set the units of the personal contour by multiplying each bucket's units by a multipier.
	 * Since buckets are immutable, each element is copied to a new one
	 * The multiplier is newRate / oldRate
	 * @param multiplier: The new value to set to.
	 */	
//	public AbstractContour adjustUnitsOld(double multiplier, long actualDuration) {
//		PersonalContour newContour = constructUsingSizeOf(this);		
//		for (int i=0; i < contourBuckets.length; i++) {
//			newContour.contourBuckets[i] = ((PersonalContourBucket)contourBuckets[i]).adjustUnits(multiplier);
//		}
//		newContour.maxUnits = newContour.calcMaxUnits();
//		return newContour;		
//	}

	public AbstractContour adjustUnits(double multiplier, long startingFrom) {
		ArrayList newList = new ArrayList();
		newList.addAll(bucketsBeforeDuration(startingFrom));
		ArrayList remainingBuckets = bucketsAfterDuration(startingFrom, false);
		for (int i=0; i < remainingBuckets.size(); i++) {
			newList.add(((PersonalContourBucket)remainingBuckets.get(i)).adjustUnits(multiplier));
		}
		PersonalContour newContour = getInstance(newList).makePacked();
		newContour.maxUnits = newContour.calcMaxUnits();
		return newContour;
	}
		
	
/**
 * Replace every element of the array with another that has its work adjusted
 */
	public AbstractContour contourAdjustWork(double multiplier, long actualDuration) {
		if (actualDuration == 0) {
			PersonalContour newContour = constructUsingSizeOf(this);
			for (int i=0; i < contourBuckets.length; i++) {
				newContour.contourBuckets[i] = ((PersonalContourBucket)contourBuckets[i]).adjustWork(multiplier);
			}
			newContour.makePacked();
			newContour.maxUnits = newContour.calcMaxUnits();		
			return newContour;
		}

		ArrayList newList = bucketsBeforeDuration(actualDuration);
		ArrayList after = bucketsAfterDuration(actualDuration,false);
		for (int i = 0; i < after.size(); i++) {
			newList.add(((PersonalContourBucket)after.get(i)).adjustWork(multiplier));
		}
		AbstractContour result =  getInstance(newList).makePacked();
		result.maxUnits = result.calcMaxUnits();
		return result;
	}	
	

/**
 * Make a personal contour from a standard one.  This is used when the user explicity changes to a personal contour, or if he types
 * in values in the spreadsheet.
 * It essentially clones a contour to a personal contour.
 * @param standard
 * @param assignmentDuration
 */
	public PersonalContour(AbstractContour standard, long assignmentDuration) {
		super(new PersonalContourBucket[standard.numBuckets()]);
		for (int i = 0; i < contourBuckets.length; i++) 
			contourBuckets[i] = PersonalContourBucket.getInstance(standard.getContourBuckets()[i], assignmentDuration);
		maxUnits = calcMaxUnits();
	}


//	public AbstractContour setValue(long assignmentStart, long setStart, long setEnd, long setDuration, double value) {
//		LinkedList newList = new LinkedList();
//		
//		PersonalContourBucket bucket = null;
//		long start = assignmentStart;
//		long end;
//		for (int i=0; i < contourBuckets.length; i++) {
//			bucket = (PersonalContourBucket)contourBuckets[i];
//
//			if (setStart <= start) {
//				newList.add(new PersonalContourBucket(setDuration, value));
//				setStart = Long.MAX_VALUE;
//				end = setEnd;
//			}
//			if (start + bucket.getDuration())
//			
//				end = start + bucket.getDuration();
//				
//			}
//			
//			newContour.contourBuckets[i] = bucket;
//			newDuration -= bucket.getDuration();
//			if (newDuration <= 0) {
//				newContour.contourBuckets[i] = bucket.adjustDuration(newDuration);// adding a negative value
//				if (i < contourBuckets.length -1 ) // if shortening number of buckets
//					System.arraycopy(newContour.contourBuckets,0,newContour.contourBuckets,0,i+1); // replace old with new and truncate to correct length
//				return newContour;
//			}
//		}
//		// extend last bucket to account for duration
//		newContour.contourBuckets[contourBuckets.length] = bucket.adjustDuration(newDuration);
//		return newContour;
//	}

	/**
	 * Remove any starting empty buckets from the contour and return the duration of those buckets
	 * @return
	 */
	public long extractDelay() {
		long delay = 0;
		if (contourBuckets.length < 2) 
			return 0;

		while (contourBuckets[0].getUnits() == 0) {
			delay += contourBuckets[0].getBucketDuration(0);
			AbstractContourBucket newBuckets[] = new AbstractContourBucket[contourBuckets.length - 1];
			System.arraycopy(contourBuckets,1,newBuckets,0,newBuckets.length);
			contourBuckets = newBuckets;
		}
		return delay;
	}
	
	/** Remove a blank bucket, if any, that starts or occurs at a duration.
	 * This is used when updating progress to assure that a task resumes on a certain date
	 * @param atDuration
	 * @return
	 */
	public AbstractContour removeEmptyBucketAtDuration(long atDuration) {
		ArrayList afterList = bucketsAfterDuration(atDuration,false);
		// if nothing after or starts with a non null bucket
		if (afterList.isEmpty() || ((AbstractContourBucket)afterList.get(0)).getUnits() != 0)
			return this;
		afterList.remove(0); // remove blank bucket
		ArrayList newList = bucketsBeforeDuration(atDuration);
		newList.addAll(afterList); // exclude filler
		return getInstance(newList).makePacked();

	}
	public Object clone() {
		return super.clone();
	}
}


