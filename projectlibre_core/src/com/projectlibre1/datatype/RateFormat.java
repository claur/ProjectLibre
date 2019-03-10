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
package com.projectlibre1.datatype;

import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;

import com.projectlibre1.configuration.Settings;
import com.projectlibre1.options.ScheduleOption;
import com.projectlibre1.util.ClassUtils;

/**
 *
 */
public class RateFormat extends Format implements TimeUnit {
	private static NumberFormat MONEY_FORMAT = Money.getMoneyFormatInstance(); //NumberFormat.getCurrencyInstance();
	private static NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();
	private static NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance();
	
	private static RateFormat moneyInstance = null;
	private static RateFormat instance = null;
	private static RateFormat percentInstance = null;
	private static RateFormat nonTemporalInstance = null;
	private String timeUnitLabel;
	private boolean money;
	public boolean percent;
	private boolean temporal;
	public static RateFormat getInstance(Object object, boolean money, boolean percent, boolean temporal) {
		if (instance == null) {
			instance = new RateFormat(null,false,false,true);
			moneyInstance = new RateFormat(null,true,false,true);
			percentInstance = new RateFormat(null,false,true,true);
			nonTemporalInstance = new RateFormat(null,false,false,false);
		}
		String timeUnit = unitLabelOfObject(object);
		if (percent)
			return percentInstance;
		if (timeUnit != null)
			return new RateFormat(timeUnit, money,false,temporal);
		if (money)
			return moneyInstance;
		else if (temporal)
			return instance;
		else
			return nonTemporalInstance;
	}
	
	public static String unitLabelOfObject(Object object) {
		String result = null;
		if (object != null) {
			if (object instanceof String)
				result = (String)object;
			else if (object instanceof CanSupplyRateUnit) {
				result = ((CanSupplyRateUnit)object).getTimeUnitLabel();
				if (result == null)
					result = "";
			}
		}
		return result;
	}
	/**
	 * 
	 */
	private RateFormat(String timeUnitLabel, boolean money, boolean percent, boolean temporal) {
		super();
		this.money = money;
		this.percent = percent;
		this.timeUnitLabel = timeUnitLabel;
		this.temporal = temporal;
	}

	/* (non-Javadoc)
	 * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
	 */
	public Object parseObject(String rateString, ParsePosition pos) {
		if (rateString.length() == 0)
			return null;
		
		if (rateString.charAt(pos.getIndex()) == '+') // if string begins with + sign, ignore it
			pos.setIndex(pos.getIndex()+1);
				
		Number numberResult = null;
		if (percent) {
			numberResult = PERCENT_FORMAT.parse(rateString,pos);
			if (numberResult == null) {
				numberResult = NUMBER_FORMAT.parse(rateString,pos);
				if (numberResult != null)
					numberResult = new Double(numberResult.doubleValue() / 100.0D);
			}
			if (numberResult == null)
				return null;
			return new Rate(numberResult.doubleValue(),TimeUnit.PERCENT);
		}

		if (money) 
			numberResult = MONEY_FORMAT.parse(rateString, pos);
		if (numberResult == null)
			numberResult = NUMBER_FORMAT.parse(rateString, pos);
		if (numberResult == null)
			return null;
		double rate = numberResult.doubleValue();
		String durationPart = rateString.substring(pos.getIndex());
		durationPart = durationPart.trim();
		
		// at this point, we have the number and are now focusing on the suffix
		int type = TimeUnit.NON_TEMPORAL;
		int slashIndex = durationPart.indexOf(Settings.SLASH);
		if (slashIndex == -1) { // if no slash
			if (timeUnitLabel == null && temporal) // temporal types use default
				type = getDefaultType();
		} else {
			durationPart = "1" + durationPart.substring(slashIndex+1,durationPart.length());// replace the slash with a 1
			Duration duration = (Duration) DurationFormat.getInstance().parseObject(durationPart, new ParsePosition(0));
			if (duration == null)
				return null;
			type = Duration.getEffectiveType(duration.getEncodedMillis());
		}
		rate /= Duration.timeUnitFactor(type);
		return new Rate(rate,type);
	}

	/* (non-Javadoc)
	 * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
	 */
	public StringBuffer format(Object rateObject, StringBuffer toAppendTo, FieldPosition pos) {

		if (rateObject == null)
			return toAppendTo;
		else if (rateObject == ClassUtils.defaultRate)
			return toAppendTo; // do nothing
		else if (rateObject == ClassUtils.defaultUnitlessRate)
			return toAppendTo;
		
		Rate rate = (Rate)rateObject;
		double rateValue = rate.getValue();
		int type = rate.getTimeUnit();
		if (type == TimeUnit.NONE && temporal) // if no unit, use default
			type = getDefaultType();

		if (percent) {
			PERCENT_FORMAT.format(new Double(rateValue),toAppendTo,pos);
		} else {
			rateValue *= Duration.timeUnitFactor(type);
			if (money) {
				MONEY_FORMAT.format(new Double(rateValue),toAppendTo,pos);
			} else {
				NumberFormat.getInstance().format(new Double(rateValue),toAppendTo,pos);
				if (timeUnitLabel != null && !timeUnitLabel.equals(""))
					toAppendTo.append(" " + timeUnitLabel);
			}
	
			if (type != TimeUnit.NON_TEMPORAL) { // if value is expressed in duration
				toAppendTo.append(Settings.SLASH);
				String unit = DurationFormat.formatTypeUnit(type);
				toAppendTo.append(unit);
			} 
		}
		return toAppendTo;
	}
	
	private int getDefaultType() {
		return ScheduleOption.getInstance().getRateEnteredIn();
	}
}
