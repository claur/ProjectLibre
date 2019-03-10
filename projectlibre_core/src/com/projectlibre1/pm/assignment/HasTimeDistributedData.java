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
package com.projectlibre1.pm.assignment;

import java.util.Collection;

import org.apache.commons.collections.Closure;

import com.projectlibre1.algorithm.ReverseQuery;
import com.projectlibre1.pm.calendar.WorkCalendar;

/**
 * Interface for classes having time distributed data
 */
public interface HasTimeDistributedData extends TimeDistributedConstants {
	public static final long NO_VALUE_LONG = 0L;
	public static final double NO_VALUE_DOUBLE = 0.0D;
	
	public void buildReverseQuery(ReverseQuery reverseQuery);	
	public void forEachWorkingInterval(Closure visitor, boolean mergeWorking, WorkCalendar workCalendar);
	public double cost(long start, long end);
	public double actualCost(long start, long end);
	public double actualFixedCost(long start, long end);
	public double fixedCost(long start, long end);
	public double baselineCost(long start, long end);
	public long work(long start, long end);
	public long baselineWork(long start, long end);
	public long actualWork(long start, long end);
	public long remainingWork(long start, long end);
	public boolean isLabor();
	public Collection childrenToRollup();
	
	public static Object histogramTypes[] = {
		SELECTED
		,THIS_PROJECT
		,AVAILABILITY
	};

	public static Object reverseHistogramTypes[] = {
		THIS_PROJECT
		,SELECTED
		,AVAILABILITY
	};
	public static Object serverHistogramTypes[] = {
		SELECTED
		,THIS_PROJECT
		,OTHER_PROJECTS
		,AVAILABILITY
	};

	public static Object serverReverseHistogramTypes[] = {
		OTHER_PROJECTS
		,THIS_PROJECT
		,SELECTED
		,AVAILABILITY
	};
	
	public static int tracesCount=3;
	public static int serverTracesCount=4;
	
			

	public static Object workTypes[] = {
			WORK,
			ACTUAL_WORK,
			REMAINING_WORK,
			BASELINE_WORK,
			BASELINE1_WORK,
			BASELINE2_WORK,
			BASELINE3_WORK,
			BASELINE4_WORK,
			BASELINE5_WORK,
			BASELINE6_WORK,
			BASELINE7_WORK,
			BASELINE8_WORK,
			BASELINE9_WORK,
			BASELINE10_WORK
	};
	public static Object costTypes[] = {
			COST,
			ACTUAL_COST,
			FIXED_COST,
			ACTUAL_FIXED_COST,
			REMAINING_COST,
			BASELINE_COST,
			ACWP,
			BCWP,
			BCWS,
			BASELINE1_COST,
			BASELINE2_COST,
			BASELINE3_COST,
			BASELINE4_COST,
			BASELINE5_COST,
			BASELINE6_COST,
			BASELINE7_COST,
			BASELINE8_COST,
			BASELINE9_COST,
			BASELINE10_COST,
	};
	public static Object baselineWorkTypes[] = {
			BASELINE_WORK,
			BASELINE1_WORK,
			BASELINE2_WORK,
			BASELINE3_WORK,
			BASELINE4_WORK,
			BASELINE5_WORK,
			BASELINE6_WORK,
			BASELINE7_WORK,
			BASELINE8_WORK,
			BASELINE9_WORK,
			BASELINE10_WORK
	};
}
