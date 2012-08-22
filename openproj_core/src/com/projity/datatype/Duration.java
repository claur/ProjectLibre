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
package com.projity.datatype;

import com.projity.options.CalendarOption;
import com.projity.options.ScheduleOption;
import com.projity.util.MathUtils;

/**
 * Instead of creating a true Duration type, I use unused bits of the long which stores durations.
 * The idea is that the algorithms will run faster because there is no object churn and fewer function
 * calls.
 */
public class Duration extends Number implements Comparable {
	private static final long serialVersionUID = 1489291902577173002L;
	private long encodedMillis;
	protected boolean work = false;
	
	public static final long MAX_DURATION = 10*365*8*60*60*1000L; // biggest possible value
	private Duration(Double doubleObject) {
		this(doubleObject == null ? 0L : doubleObject.longValue());
	}
	public Duration(long encodedMillis) {
		this.encodedMillis = encodedMillis;
	}
	
	public String toString() {
		return DurationFormat.getInstance().format(this);
	}
	
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0 instanceof Duration)
			return encodedMillis == ((Duration)arg0).encodedMillis;
		else
			return false;
	}

	public int compareTo(Object arg0) {
		if (arg0 == null)
			 throw new NullPointerException();
		if (!(arg0 instanceof Duration))
			throw new ClassCastException();
		
		return MathUtils.signum(getValue(encodedMillis) - getValue(((Duration)arg0).getEncodedMillis())); 
	}
		
	public long getEncodedMillis() {
		return encodedMillis;
	}
	public void setEncodedMillis(long encodedMillis) {
		this.encodedMillis = encodedMillis;
	}
	
	public static final Duration ZERO = new Duration(0); 
	public void setWork(boolean work) {
		this.work = work;
//TODO figure out why lines below don't work when formatting
//		if (work)
//			encodedMillis = Duration.setAsTimeUnit(encodedMillis,ScheduleOption.getInstance().getWorkUnit());
	}
	
	public boolean isWork() {
		return work;
	}
	
	private static int SHIFT = 57; // 6 bits are used: bits 62-57 (bit 63 is the sign bit and is not used
	private static long ESTIMATED_BIT 		= 0x20L << SHIFT; // 1<< 62
	private static long ELAPSED_BIT 		= 0x10L << SHIFT;
	private static long PERCENT_BIT			= 0x0fL << SHIFT;	
	private static long YEARS_BIT 			= 0x0eL << SHIFT;
	private static long MONTHS_BIT 			= 0x0dL << SHIFT;
	private static long WEEKS_BIT 			= 0x0cL << SHIFT;
	private static long DAYS_BIT			= 0x0bL << SHIFT;	
	private static long HOURS_BIT			= 0x0aL << SHIFT;
	private static long MINUTES_BIT			= 0x09L << SHIFT;
	private static long SECONDS_BIT			= 0x08L << SHIFT;
	private static long NON_TEMPORAL_BIT	= 0x07L << SHIFT;
	
	
	private static long UNITS_MASK 		= 0x0fL << SHIFT;
	private static long ELAPSED_AND_UNITS_MASK = (0x1fL << SHIFT);	
	private static long MILLIS_MASK 	= ~(0x3fL << SHIFT);
	
	
	public static long clear(long duration) {
		return duration & ELAPSED_AND_UNITS_MASK;
	}
	
	public static boolean isZero(long duration) {
		return millis(duration) == 0L;
	}
	
	private static long getBits(long flags, long duration) {
		if (duration < 0)
			duration = -duration;
		return (duration & flags);			
	}
	
	private static long setBits(long flags, long duration) {
		boolean negative = (duration < 0);
		if (negative)
			duration = -duration; 
		duration |= flags;
		if (negative)
			duration = -duration;
		return duration; 					
	}
	private static long clearBits(long flags, long duration) {
		boolean negative = (duration < 0);
		if (negative)
			duration = -duration; 
		duration &= ~flags;
		if (negative)
			duration = -duration;
		return duration; 					
	}
	
	private static long setBits(long duration, long clearThese, long setThese) {
		boolean negative = (duration < 0);
		if (negative)
			duration = -duration; 
		long unitsOnly = duration & (~clearThese);
		duration = unitsOnly | setThese;
		if (negative)
			duration = - duration;
		return duration;
	}

	public static boolean isElapsed(long duration) {
		return getBits(ELAPSED_BIT,duration) == ELAPSED_BIT;
	}

	public static long setAsElapsed(long duration) {
		return setBits(duration,ELAPSED_BIT,ELAPSED_BIT);
	}

	public static boolean isEstimated(long duration) {
		return getBits(ESTIMATED_BIT,duration) == ESTIMATED_BIT;
	}

	public static long setAsEstimated(long duration, boolean estimated) {
		if (estimated)
			return setBits(ESTIMATED_BIT,duration);
		else
			return clearBits(ESTIMATED_BIT,duration);
	}
	
	public static boolean hasUnits(long duration) {
		return getBits(UNITS_MASK,duration) != 0;
	}
	
	public static long millis(long duration) {
		boolean negative = (duration < 0);
		if (negative)
			duration = -duration; 
		duration &= MILLIS_MASK;
		if (negative)
			duration = -duration; 
		return duration;
	}
	
/**
	Decimals are stored in lower order bytes as a float
*/
	public static float getPercentAsDecimal(long duration) {
		int right = (int) (duration & 0xFFFFFFFF);
		return Float.intBitsToFloat(right);
	} 
	
	public static long setPercentAsDecimal(long duration, float percentAsDecimal) {
		duration &= (0xFFFFFFFF00000000L); // clear out low int
		duration |= (Float.floatToIntBits(percentAsDecimal));
		return duration;
	}
	
	public static boolean isPercent(long duration) {
		return getBits(UNITS_MASK,duration) == PERCENT_BIT;
	}

	public static long setAsPercent(long duration) {
		return setBits(duration, ELAPSED_AND_UNITS_MASK, PERCENT_BIT);	
	}
			
	public static long setAsElapsedPercent(long duration) {
		return setBits(duration, ELAPSED_AND_UNITS_MASK, ELAPSED_BIT | PERCENT_BIT);
	}

	public static long setAsNonTemporal(long duration) {
		return setBits(duration, ELAPSED_AND_UNITS_MASK, NON_TEMPORAL_BIT);
	}

	public static long setAsSeconds(long duration) {
		return setBits(duration, ELAPSED_AND_UNITS_MASK, SECONDS_BIT);
	}

	public static long setAsElapsedSeconds(long duration) {
		return setBits(duration, ELAPSED_AND_UNITS_MASK, ELAPSED_BIT | SECONDS_BIT);		
	}

	public static long setAsMinutes(long duration) {
		return setBits(duration, ELAPSED_AND_UNITS_MASK, MINUTES_BIT);
	}

	public static long setAsElapsedMinutes(long duration) {
		return setBits(duration, ELAPSED_AND_UNITS_MASK, ELAPSED_BIT | MINUTES_BIT);		
	}


	public static long setAsHours(long duration) {
		return setBits(duration, ELAPSED_AND_UNITS_MASK, HOURS_BIT);		
	}

	public static long setAsElapsedHours(long duration) {
		return setBits(duration, ELAPSED_AND_UNITS_MASK, ELAPSED_BIT | SECONDS_BIT);
	}

	public static long setAsDays(long duration) {
		return setBits(duration, ELAPSED_AND_UNITS_MASK, DAYS_BIT);		
	}

	public static long setAsElapsedDays(long duration) {
		return setBits(duration, ELAPSED_AND_UNITS_MASK, ELAPSED_BIT | DAYS_BIT);
	}

	public static long setAsWeeks(long duration) {
		return setBits(duration, ELAPSED_AND_UNITS_MASK, WEEKS_BIT);		
	}

	public static long setAsElapsedWeeks(long duration) {
		return setBits(duration, ELAPSED_AND_UNITS_MASK, ELAPSED_BIT | WEEKS_BIT);
	}
	
	public static long setAsMonths(long duration) {
		return setBits(duration, ELAPSED_AND_UNITS_MASK, MONTHS_BIT);
	}

	public static long setAsElapsedMonths(long duration) {
		return setBits(duration, ELAPSED_AND_UNITS_MASK, ELAPSED_BIT | MONTHS_BIT);
	}
	
	public static long setAsYears(long duration) {
		return setBits(duration, ELAPSED_AND_UNITS_MASK, YEARS_BIT);
	}

	public static long setAsElapsedYears(long duration) {
		return setBits(duration, ELAPSED_AND_UNITS_MASK, ELAPSED_BIT | YEARS_BIT);
	}
	
/**
 * Use the time unit of another duration if this duration has none
 * @param duration input duraiton
 * @param durationWithUnit duration that has time unit
 * @return the original duration if it had a time unit, otherwise the duration with the time unit of durationWithUnit
 */	
	public static long useTimeUnitOfInNone(long duration, long durationWithUnit) {
		if (getType(duration) == TimeUnit.NONE)
			return setAsTimeUnit(duration,getType(durationWithUnit));
		return duration;
	}
	
	public static long setAsTimeUnit(long duration, int type) {
		switch (type) {
			case TimeUnit.PERCENT :
				return setAsPercent(duration);
			case TimeUnit.ELAPSED_PERCENT :
				return setAsElapsedPercent(duration);
			case TimeUnit.NON_TEMPORAL :
				return setAsNonTemporal(duration);
			case TimeUnit.MINUTES :
				return setAsMinutes(duration);
			case TimeUnit.ELAPSED_MINUTES :
				return setAsElapsedMinutes(duration);
			case TimeUnit.HOURS :
				return setAsHours(duration);
			case TimeUnit.ELAPSED_HOURS :
				return setAsElapsedHours(duration);
			case TimeUnit.DAYS :
				return setAsDays(duration);
			case TimeUnit.ELAPSED_DAYS :
				long x = setAsDays(duration);
				long y = setAsElapsedDays(duration);
				
				return setAsElapsedDays(duration);
			case TimeUnit.WEEKS :
				return setAsWeeks(duration);
			case TimeUnit.ELAPSED_WEEKS :
				return setAsElapsedWeeks(duration);
			case TimeUnit.MONTHS :
				return setAsMonths(duration);
			case TimeUnit.ELAPSED_MONTHS :
				return setAsElapsedMonths(duration);
			case TimeUnit.YEARS :
				return setAsYears(duration);
			case TimeUnit.ELAPSED_YEARS :
				return setAsElapsedYears(duration);
			// 	don't bother for NONE
		}
		return duration;
	}	

	public static double timeUnitFactor(int type) {
		double result = 1.0;
		if (type == TimeUnit.NONE)
			type = ScheduleOption.getInstance().getDurationEnteredIn();
		
		switch (type) {
			case TimeUnit.NON_TEMPORAL:
				return 1.0D;
			case TimeUnit.MINUTES :
			case TimeUnit.ELAPSED_MINUTES :
				return 60.0 * 1000;
			case TimeUnit.HOURS :
			case TimeUnit.ELAPSED_HOURS :
				return 60.0 * 60 * 1000;
			case TimeUnit.DAYS :
				return CalendarOption.getInstance().getHoursPerDay() * 60.0 * 60 * 1000;
			case TimeUnit.ELAPSED_DAYS :
				return 24.0 * 60 * 60 * 1000;
			case TimeUnit.WEEKS :
				return CalendarOption.getInstance().getHoursPerWeek() * 60.0 * 60 * 1000;
			case TimeUnit.ELAPSED_WEEKS :
				return 7.0 * 24 * 60 * 60 * 1000;
			case TimeUnit.MONTHS :
				return CalendarOption.getInstance().hoursPerMonth() * 60.0 * 60 * 1000;
			case TimeUnit.ELAPSED_MONTHS : 
				return 30.0 * 24 * 60 * 60 * 1000;
			case TimeUnit.YEARS :
				return 365 * CalendarOption.getInstance().getHoursPerDay() * 60
						* 60 * 1000;
			case TimeUnit.ELAPSED_YEARS :
				return 365 * 24 * 60 * 60 * 1000;
		}
		return result;
	}
	
	public static long getInstance(double value, int type) {
		long result = 0;
		if (type == TimeUnit.PERCENT || type == TimeUnit.ELAPSED_PERCENT) { // percentages are treated specially
			result = Duration.setPercentAsDecimal(result,(float) value);
		} else {
			result = Math.round(value * timeUnitFactor(type));
		}
		result = setAsTimeUnit(result,type);
		return result;
	}
	
	public static double getValue(long duration) {
		int type = getEffectiveType(duration);
		if (type == TimeUnit.PERCENT || type == TimeUnit.ELAPSED_PERCENT) { // percentages are treated specially
			return getPercentAsDecimal(duration);
		} else {
			return ((double)millis(duration)) / timeUnitFactor(type);
			//TODO confirm no rounding error above
			
		}
	}
	public static int getType(long duration) {
		long unitBits = getBits(ELAPSED_AND_UNITS_MASK,duration);
		if (unitBits == PERCENT_BIT)
			return TimeUnit.PERCENT;
		else if (unitBits == (ELAPSED_BIT | PERCENT_BIT))
			return TimeUnit.ELAPSED_PERCENT;
		else if (unitBits == YEARS_BIT)
			return TimeUnit.YEARS;
		else if (unitBits == (ELAPSED_BIT | YEARS_BIT))
			return TimeUnit.ELAPSED_YEARS;
		else if (unitBits == MONTHS_BIT)
			return TimeUnit.MONTHS;
		else if (unitBits == (ELAPSED_BIT | MONTHS_BIT))
			return TimeUnit.ELAPSED_MONTHS;
		else if (unitBits == WEEKS_BIT)
			return TimeUnit.WEEKS;
		else if (unitBits == (ELAPSED_BIT | WEEKS_BIT))
			return TimeUnit.ELAPSED_WEEKS;
		else if (unitBits == DAYS_BIT)
			return TimeUnit.DAYS;
		else if (unitBits == (ELAPSED_BIT | DAYS_BIT))
			return TimeUnit.ELAPSED_DAYS;
		else if (unitBits == HOURS_BIT)
			return TimeUnit.HOURS;
		else if (unitBits == (ELAPSED_BIT | HOURS_BIT))
			return TimeUnit.ELAPSED_HOURS;
		else if (unitBits == MINUTES_BIT)
			return TimeUnit.MINUTES;
		else if (unitBits == (ELAPSED_BIT | MINUTES_BIT))
			return TimeUnit.ELAPSED_MINUTES;
		else if (unitBits == NON_TEMPORAL_BIT)
			return TimeUnit.NON_TEMPORAL;
		return TimeUnit.NONE;
	}
	
	// same as get type but converts NONE to current preference
	public static int getEffectiveType(long duration) {
		int type = getType(duration);
		if (type == TimeUnit.NONE)
			type = ScheduleOption.getInstance().getDurationEnteredIn();
		return type;
	}

	public static Duration getInstanceFromDouble(Double doubleObject) {
		return new Duration(doubleObject);
	}
	/* (non-Javadoc)
	 * @see java.lang.Number#doubleValue()
	 */
	public double doubleValue() {
		return millis(encodedMillis);
	}

	/* (non-Javadoc)
	 * @see java.lang.Number#floatValue()
	 */
	public float floatValue() {
		return millis(encodedMillis);
	}

	/* (non-Javadoc)
	 * @see java.lang.Number#intValue()
	 */
	public int intValue() {
		return (int) millis(encodedMillis);
	}

	/* (non-Javadoc)
	 * @see java.lang.Number#longValue()
	 */
	public long longValue() {
		return millis(encodedMillis);
	}

	public double getAsHours() {
		return doubleValue() / timeUnitFactor(TimeUnit.HOURS);
	}
	public double getAsDays() {
		return doubleValue() / timeUnitFactor(TimeUnit.DAYS);
	}
}