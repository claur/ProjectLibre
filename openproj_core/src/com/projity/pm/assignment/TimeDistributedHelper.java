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
package com.projity.pm.assignment;

import java.util.HashMap;

import com.projity.configuration.Configuration;
import com.projity.field.Field;
import com.projity.pm.snapshot.Snapshottable;

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
