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

import java.util.HashMap;

import com.projectlibre1.configuration.Configuration;
import com.projectlibre1.field.Field;
import com.projectlibre1.pm.snapshot.Snapshottable;

public class TimeDistributedHelper {
	private static HashMap baselineMapper = new HashMap();
	static {
		baselineMapper.put(HasTimeDistributedData.WORK, Snapshottable.CURRENT);
		baselineMapper.put(HasTimeDistributedData.ACTUAL_WORK, Snapshottable.CURRENT);
		baselineMapper.put(HasTimeDistributedData.REMAINING_WORK, Snapshottable.CURRENT);
		baselineMapper.put(HasTimeDistributedData.BASELINE_WORK, Snapshottable.BASELINE);
		baselineMapper.put(HasTimeDistributedData.BASELINE1_WORK, Snapshottable.BASELINE_1);
		baselineMapper.put(HasTimeDistributedData.BASELINE2_WORK, Snapshottable.BASELINE_2);
		baselineMapper.put(HasTimeDistributedData.BASELINE3_WORK, Snapshottable.BASELINE_3);
		baselineMapper.put(HasTimeDistributedData.BASELINE4_WORK, Snapshottable.BASELINE_4);
		baselineMapper.put(HasTimeDistributedData.BASELINE5_WORK, Snapshottable.BASELINE_5);
		baselineMapper.put(HasTimeDistributedData.BASELINE6_WORK, Snapshottable.BASELINE_6);
		baselineMapper.put(HasTimeDistributedData.BASELINE7_WORK, Snapshottable.BASELINE_7);
		baselineMapper.put(HasTimeDistributedData.BASELINE8_WORK, Snapshottable.BASELINE_8);
		baselineMapper.put(HasTimeDistributedData.BASELINE9_WORK, Snapshottable.BASELINE_9);
		baselineMapper.put(HasTimeDistributedData.BASELINE10_WORK, Snapshottable.BASELINE_10);

		baselineMapper.put(HasTimeDistributedData.COST, Snapshottable.CURRENT);
		baselineMapper.put(HasTimeDistributedData.ACTUAL_COST, Snapshottable.CURRENT);
		baselineMapper.put(HasTimeDistributedData.REMAINING_COST, Snapshottable.CURRENT);
		baselineMapper.put(HasTimeDistributedData.BASELINE_COST, Snapshottable.BASELINE);
		baselineMapper.put(HasTimeDistributedData.BASELINE1_COST, Snapshottable.BASELINE_1);
		baselineMapper.put(HasTimeDistributedData.BASELINE2_COST, Snapshottable.BASELINE_2);
		baselineMapper.put(HasTimeDistributedData.BASELINE3_COST, Snapshottable.BASELINE_3);
		baselineMapper.put(HasTimeDistributedData.BASELINE4_COST, Snapshottable.BASELINE_4);
		baselineMapper.put(HasTimeDistributedData.BASELINE5_COST, Snapshottable.BASELINE_5);
		baselineMapper.put(HasTimeDistributedData.BASELINE6_COST, Snapshottable.BASELINE_6);
		baselineMapper.put(HasTimeDistributedData.BASELINE7_COST, Snapshottable.BASELINE_7);
		baselineMapper.put(HasTimeDistributedData.BASELINE8_COST, Snapshottable.BASELINE_8);
		baselineMapper.put(HasTimeDistributedData.BASELINE9_COST, Snapshottable.BASELINE_9);
		baselineMapper.put(HasTimeDistributedData.BASELINE10_COST, Snapshottable.BASELINE_10);

	}
	public static Object baselineForData(Object data) {
		return baselineMapper.get(data);
	}
	public static boolean isWork(Object data) {
		if (data instanceof Field) {
			return ((Field)data).isWork();
		}
		if (data instanceof Number) {
			int type = ((Number)data).intValue();
			if (type > 0 &&  type <= 4)
				return true;
			return (type - 16) %6 == 0; // See TimeDistributedTypeMapper
		}
		return false;
	}
	public static boolean isCost(Object data) {
		if (data instanceof Field) {
			return ((Field)data).isMoney();
		}
		return false;
	}
	public static String getIdForObject(Object obj) {
		if (obj instanceof String)
			return (String) obj;
		else
			return ((Field)obj).getId();
	}
	public static Object getObjectFromId(String id) {
		Object result = null;
		if (id.equals(TimeDistributedConstants.REMAINING_WORK))	
			result = TimeDistributedConstants.REMAINING_WORK;
		else if (id.equals(TimeDistributedConstants.REMAINING_COST))	
			result = TimeDistributedConstants.REMAINING_COST;
		else 
			result =Configuration.getFieldFromId(id);
		if (result == null)
			System.out.println("error no object for id " + id);
		return result;
	}
}
