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
package com.projectlibre.core.time;

import java.util.HashMap;
import java.util.Map;


// 

/**
 * @author Laurent Chretienneau
 * 
 * contants from mpxj with 2 additional ones from ProjectLibre
 */
public enum TimeUnit{
	
	/**
	 * projectlibre specific to indicate the value is not temporal
	 */
	NON_TEMPORAL(-2,""), //-2 in ProjectLibre

	/**
	 * projectlibre specific to indicate no value entered
	 */
	NONE(-1,""), //-1 in ProjectLibre
	
	/**
	 * Constant representing Minutes.
	 */
	MINUTES(0, "m"),

	/**
	 * Constant representing Hours.
	 */
	HOURS(1, "h"),

	/**
	 * Constant representing Days.
	 */
	DAYS(2, "d"),

	/**
	 * Constant representing Weeks.
	 */
	WEEKS(3, "w"),

	/**
	 * Constant representing Months.
	 */
	MONTHS(4, "mo"),

	/**
	 * Constant representing Percent.
	 */
	PERCENT(5, "%"),

	/**
	 * Constant representing Years.
	 */
	YEARS(6, "y"),

	/**
	 * Constant representing Elapsed Minutes.
	 */
	ELAPSED_MINUTES(7, "em"),

	/**
	 * Constant representing Elapsed Hours.
	 */
	ELAPSED_HOURS(8, "eh"),

	/**
	 * Constant representing Elapsed Days.
	 */
	ELAPSED_DAYS(9, "ed"),

	/**
	 * Constant representing Elapsed Weeks.
	 */
	ELAPSED_WEEKS(10, "ew"),

	/**
	 * Constant representing Elapsed Months.
	 */
	ELAPSED_MONTHS(11, "emo"),

	/**
	 * Constant representing Elapsed Years.
	 */
	ELAPSED_YEARS(12, "ey"),

	/**
	 * Constant representing Elapsed Percent.
	 */
	ELAPSED_PERCENT(13, "e%");


	protected int id;
	protected String symbol;
	protected static Map<Integer,TimeUnit> reverseMap;
	
	private TimeUnit(int id, String symbol) {
		this.id=id;
		this.symbol=symbol;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public static TimeUnit getInstance(int id){
		//not using values()[id] because type can be negative
		if (reverseMap==null){
			reverseMap=new HashMap<Integer,TimeUnit>();
			for (TimeUnit unit : values())
				reverseMap.put(unit.getId(),unit);
		}
		return reverseMap.get(id);
	}	
	

}
