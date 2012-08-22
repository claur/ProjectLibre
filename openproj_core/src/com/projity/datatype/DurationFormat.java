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

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.projity.options.EditOption;
import com.projity.options.ScheduleOption;
import com.projity.strings.Messages;
import java.text.Format;

/**
 * Instead of creating a true Duration type, I use unused bits of the long which
 * stores durations. The idea is that the algorithms will run faster because
 * there is no object churn and fewer function calls.
 */
public class DurationFormat extends Format {
	private boolean showPlusSign = false;
	private boolean isWork = false;
	private boolean canBeNonTemporal = false;
	private static Format instance = null;
	public static Format getInstance() {
		if (instance == null)
			instance = new DurationFormat(false);
		return instance;
	}
	private static Format signedInstance = null;
	public static Format getSignedInstance() {
		if (signedInstance == null)
			signedInstance = new DurationFormat(true);
		return signedInstance;
	}
	private static Format workInstance = null;
	public static Format getWorkInstance() {
		if (workInstance == null) {
			workInstance = new DurationFormat(false);
			((DurationFormat)workInstance).isWork = true;
		}
		return workInstance;
	}
	
	private static Format nonTemporalWorkInstance = null;
	public static Format getNonTemporalWorkInstance() {
		if (nonTemporalWorkInstance == null) {
			nonTemporalWorkInstance = new DurationFormat(false);
			((DurationFormat)nonTemporalWorkInstance).isWork = true;
			((DurationFormat)nonTemporalWorkInstance).canBeNonTemporal = true;
		}
		return nonTemporalWorkInstance;
	}
	
	// these strings are themselves parts of string ids in properties file and as such must be hard coded as below
	private static String[] types = {"minute", "hour", "day", "week", "month",
			"year", "percent", "eminute", "ehour", "eday", "eweek", "emonth",
			"eyear", "epercent"};
	
	private static final int SINGULAR = 0;
	private static final int PLURAL = 1;
	private static final String multiple[] = {".singular", ".plural"};
	private static int TYPE_COUNT = types.length;
	private static int NAME_COUNT = 4;
	private static String[][][] typesArray = new String[NAME_COUNT][multiple.length][TYPE_COUNT];
	private static Pattern[] pattern = new Pattern[TYPE_COUNT];
	private static String estimatedSymbol = Messages.getString("Units.estimatedSymbol");
	
	
	//private constructor initializes values.
	private DurationFormat(boolean showPlusSign) {
		this.showPlusSign = showPlusSign;
		String estimated = Messages.getString("Units.estimatedSymbolRegex"); // Like ?
		
		// a bunch of init code which reads the possible  values for durations from localized messages
		for (int i = 0; i < TYPE_COUNT; i++) {
			String singularNames=null;
			String pluralNames=null;
			for (int j = 0; j < multiple.length; j++) {
				String names = new String(Messages.getString("Units."
						+ types[i] + multiple[j]));
				if (j==SINGULAR) singularNames=names;
				if (j==PLURAL) pluralNames=names;
				String[] units = names.split("\\|"); // index into the names list, getting string
				//split has a big memory cost so names are pre-splited 
				for (int k = 0; k < NAME_COUNT; k++) typesArray[k][j][i]=units[k];
			}
			// The pattern represents the following:
			// Singular type OR Plural type, optinally followed by white space and the estimated symbol (?)
			// Two groups are saved: Group 1 is the type, Group 2 is the estimated symbol
			pattern[i] = Pattern.compile("((?:" + singularNames+ ")"
					+ "|(?:" + pluralNames + "))?" 
					+ "(\\s*" + estimated + "?)");
		}
	}
	
	
	private static NumberFormat DECIMAL_FORMAT = NumberFormat.getNumberInstance();
	

	/* (non-Javadoc)
	 * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
	 */
	public Object parseObject(String durationString, ParsePosition pos) {
		Object result = null;
		if (durationString.length() == 0)
			return null;
		
		if (durationString.charAt(pos.getIndex()) == '+') // if string begins with + sign, ignore it
			pos.setIndex(pos.getIndex()+1);
				
				
		Number numberResult = DECIMAL_FORMAT.parse(durationString, pos);
		if (numberResult == null)
			return null;
		String durationPart = durationString.substring(pos.getIndex());
		durationPart = durationPart.trim();
		Matcher matcher;
		for (int i = 0; i < TYPE_COUNT; i++) { // find hte appropriate units
			matcher = pattern[i].matcher(durationPart);
			if (matcher.matches()) {
				int timeUnit = (matcher.group(1) != null) ? i : TimeUnit.NONE; // first group is units.  If no units, then it will match, but should use default: NONE
				double value = numberResult.doubleValue();
				if (timeUnit == TimeUnit.PERCENT || timeUnit == TimeUnit.ELAPSED_PERCENT)
					value /= 100.0;
				if (timeUnit == TimeUnit.NONE && isWork) {
					if (canBeNonTemporal)
						timeUnit = TimeUnit.NON_TEMPORAL;
					else
						timeUnit = ScheduleOption.getInstance().getWorkUnit(); // use default work unit if work and nothing entered
				}
				long longResult = Duration.getInstance(value,timeUnit);
				if (Duration.millis(longResult) > Duration.MAX_DURATION) // check for too big
					return null;
				if (matcher.group(2).length() != 0) { // second group is estimated				
					longResult = Duration.setAsEstimated(longResult,true);
				}

				result = new Duration(longResult);
				
				return result;
			}
		}
		
		return null;
	}
	/* (non-Javadoc)
	 * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
	 */
	public StringBuffer format(Object durationObject, StringBuffer toAppendTo, FieldPosition pos) {
		long duration = ((Duration)durationObject).getEncodedMillis();
		if (((Duration)durationObject).isWork() && Duration.getType(duration) != TimeUnit.NON_TEMPORAL) {
			duration = Duration.setAsTimeUnit(duration,ScheduleOption.getInstance().getWorkUnit());
		}
		
		double value = Duration.getValue(duration);
		int type = Duration.getEffectiveType(duration);
		if (value > 0D && showPlusSign)
			toAppendTo.append("+");
		boolean isPercent = Duration.isPercent(duration);
		if (isPercent)
			value *= 100.0;
		DECIMAL_FORMAT.format(value,toAppendTo,pos);
		
		String unit = formatTypeUnit(type
				,(Math.abs(value) == 1.0)
				,EditOption.getInstance().isAddSpaceBeforeLabel()
				,Duration.isPercent(duration)
				,Duration.isEstimated(duration)
		        ,EditOption.getInstance().getViewAs(type));
		toAppendTo.append(unit);
		return toAppendTo;
	}
	
	public String formatCompact(Object durationObject) {
		StringBuffer toAppendTo = new StringBuffer();
		long duration = ((Duration)durationObject).getEncodedMillis();
		if (((Duration)durationObject).isWork() && Duration.getType(duration) != TimeUnit.NON_TEMPORAL) {
			duration = Duration.setAsTimeUnit(duration,ScheduleOption.getInstance().getWorkUnit());
		}
		
		double value = Duration.getValue(duration);
		int type = Duration.getEffectiveType(duration);
		if (value > 0D && showPlusSign)
			toAppendTo.append("+");
		boolean isPercent = Duration.isPercent(duration);
		if (isPercent)
			value *= 100.0;
		toAppendTo.append(DECIMAL_FORMAT.format(value));
		
		String unit = formatTypeUnit(type
				,(Math.abs(value) == 1.0)
				,false
				,Duration.isPercent(duration)
				,Duration.isEstimated(duration)
				,3);
		toAppendTo.append(unit);
		return toAppendTo.toString();
	}

	public static String formatTypeUnit(int type, boolean isSingular, boolean addSpace, boolean isPercent, boolean isEstimated, int displayIndex) {
		StringBuffer toAppendTo = new StringBuffer();
		if (type == TimeUnit.NON_TEMPORAL)
			return "";
		if (addSpace && !isPercent) {
			toAppendTo.append(" ");
		}
		String unit = typesArray[displayIndex][isSingular ? SINGULAR : PLURAL][type]; // get either singular or plural names list
		toAppendTo.append(unit);
		if (isEstimated)
			toAppendTo.append(estimatedSymbol);
		return toAppendTo.toString();
	}	
	
	public static String formatTypeUnit(int type) {
		DurationFormat.getInstance(); // make sure it is initialized
		return formatTypeUnit(type,true,false,false,false,EditOption.getInstance().getViewAs(type));
	}
	
	public static String format(long millis) {
		return getInstance().format(new Duration(millis)).toString();
	}
	public static String formatCompact(long millis) {
		return ((DurationFormat)getInstance()).formatCompact(new Duration(millis)).toString();
	}
	public static String formatWork(long millis) {
		return getWorkInstance().format(new Work(millis)).toString();
	}
	public static String formatWork(Object millis) {
		if (millis!=null&&millis instanceof Long) return formatWork(((Long)millis).longValue());
		return getWorkInstance().format(millis);
	}
}