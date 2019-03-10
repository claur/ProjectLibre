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
package com.projectlibre1.pm.assignment.contour;

import java.io.Serializable;

import com.projectlibre1.configuration.CalculationPreference;
/**
 * @stereotype mi-detail
 * 
 * An allocation biclet represents the finest grained detail for an assignment.  The amount of work is determined
 * by a value of effort during a duration.
 * I do not store the absolute time value of the start/end because the allocation bucket can be shifted.
 * This class is immutable.
 * 
 * The basic formula Work = Units * Duration applies.  See class SchedulingRule
 * 
 */

public class PersonalContourBucket extends AbstractContourBucket implements Serializable{
	static final long serialVersionUID = 99779271628737L;
    private long duration = 0;

	/**
	 * @return Returns the duration.
	 */
	public long getDuration() {
		return duration;
	}

	public long getBucketDuration(long assignmentDuration) {
		return duration;
	}
	
	protected PersonalContourBucket(long duration, double units) {
		this.duration = duration;
		this.units = units;
	}
	
	public static PersonalContourBucket getInstance(long duration, double units) {
		if (duration < 0)
			System.out.println("bug negative bucket"); //TODO get rid of in production
		return new PersonalContourBucket(duration,units);
	}
	private PersonalContourBucket(AbstractContourBucket standard, long assignmentDuration) {
		this.duration = standard.getBucketDuration(assignmentDuration);
		this.units = standard.getUnits();
	}
	public double getEffectiveUnits(double assignmentUnits) {
		return units;
	}	
	/**
	 * Copy constructor. Class is immutable
	 * @param from
	 */	
	public PersonalContourBucket(PersonalContourBucket from) {
		this(from.duration, from.units);
	}

	/**
	 * @return Returns the work.
	 */
	public long calcWork() {
		return (long) (units * duration);
	}

	
	public String toString() {
		return "[duration] " + (duration / (1000*60*60)) + "h"
		      + "\n[units] " + units;
	}
	
	public double weightedSum() {
		return units * duration;
	}
	
	/**
	 * Returns a new bucket which has its units multiplied by the multiplier
	 * @param multiplier
	 * @return A new bucket.  Objects of this class are immutable
	 */	
	public PersonalContourBucket adjustUnits(double multiplier) {
		return new PersonalContourBucket(duration,units * multiplier);
	}
	
	/**
	 * Returns a new bucket which has its duration multiplied by the multiplier and its units divided by it
	 * @param multiplier
	 * @return A new bucket.  Objects of this class are immutable
	 */
	 public PersonalContourBucket adjustWork(double multiplier) {
		if (!CalculationPreference.getActive().isNonWorkContourPeriodsStayFixedLength() || units != 0) { // in the case where units are 0, don't touch the bucket
			return new PersonalContourBucket((long) (duration * multiplier), units / multiplier);
		} else {
			return this;
		}
	}
	
	/**
	 * Returns a new bucket which has its duration increased decreased by the offset
	 * @param offset (positive to increase duration, negative to decrease it)
	 * @return A new bucket.  Objects of this class are immutable
	 */
	public PersonalContourBucket adjustDuration(long offset) {
		return new PersonalContourBucket(duration + offset, units);
	}

	public static PersonalContourBucket getInstance(AbstractContourBucket standard, long assignmentDuration) {
		return new PersonalContourBucket(standard, assignmentDuration);
	}
	
	public Object clone() {
			return super.clone();
	}

}
