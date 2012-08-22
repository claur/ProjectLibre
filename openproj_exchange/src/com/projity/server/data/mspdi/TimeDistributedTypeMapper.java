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
package com.projity.server.data.mspdi;

import java.math.BigInteger;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

import com.projity.exchange.TimeDistributedTypeMapperConstants;
import com.projity.pm.assignment.HasTimeDistributedData;

/**
 *
From pjxml help file
The type of timephased data: 
1 Assignment Remaining Work  
2 Assignment Actual Work  
3 Assignment Actual Overtime Work 
4 Assignment Baseline Work  
5 Assignment Baseline Cost 
6 Assignment Actual Cost  
7 Resource Baseline Work 
8 Resource Baseline Cost  
9 Task Baseline Work 
10 Task Baseline Cost  
11 Task Percent Complete 
16 Assignment Baseline 1 Work  
17 Assignment Baseline 1 Cost  
18 Task Baseline 1 Work 
19 Task Baseline 1 Cost  
20 Resource Baseline 1 Work 
21 Resource Baseline 1 Cost  
22 Assignment Baseline 2 Work 
23 Assignment Baseline 2 Cost  
24 Task Baseline 2 Work 
25 Task Baseline 2 Cost  
26 Resource Baseline 2 Work 
27 Resource Baseline 2 Cost  
28 Assignment Baseline 3 Work 
29 Assignment Baseline 3 Cost  
30 Task Baseline 3 Work 
31 Task Baseline 3 Cost  
32 Resource Baseline 3 Work 
33 Resource Baseline 3 Cost  
34 Assignment Baseline 4 Work 
35 Assignment Baseline 4 Cost  
36 Task Baseline 4 Work 
37 Task Baseline 4 Cost  
38 Resource Baseline 4 Work 
39 Resource Baseline 4 Cost  
40 Assignment Baseline 5 Work 
41 Assignment Baseline 5 Cost  
42 Task Baseline 5 Work 
43 Task Baseline 5 Cost  
44 Resource Baseline 5 Work 
45 Resource Baseline 5 Cost  
46 Assignment Baseline 6 Work 
47 Assignment Baseline 6 Cost  
48 Task Baseline 6 Work 
49 Task Baseline 6 Cost  
50 Resource Baseline 6 Work 
51 Resource Baseline 6 Cost  
52 Assignment Baseline 7 Work 
53 Assignment Baseline 7 Cost  
54 Task Baseline 7 Work 
55 Task Baseline 7 Cost  
56 Resource Baseline 7 Work 
57 Resource Baseline 7 Cost  
58 Assignment Baseline 8 Work 
59 Assignment Baseline 8 Cost  
60 Task Baseline 8 Work 
61 Task Baseline 8 Cost  
62 Resource Baseline 8 Work 
63 Resource Baseline 8 Cost  
64 Assignment Baseline 9 Work 
65 Assignment Baseline 9 Cost  
66 Task Baseline 9 Work 
67 Task Baseline 9 Cost 
68 Resource Baseline 9 Work  
69 Resource Baseline 9 Cost 
70 Assignment Baseline 10 Work  
71 Assignment Baseline 10 Cost 
72 Task Baseline 10 Work  
73 Task Baseline 10 Cost 
74 Resource Baseline 10 Work  
75 Resource Baseline 10 Cost 
76 Physical Percent Complete 

Projity additions
100 Timecard unvalidated
101 Timecard validated

 */
public class TimeDistributedTypeMapper extends TimeDistributedTypeMapperConstants{

	
	public static Object getProjityField(BigInteger mpxValue) {
		return map.get(mpxValue);
	}
	public static int getMpxValueForField(Object field) {
		return ((Number)map.getKey(field)).intValue();
	}
	
	public static int getBaselineNumber(int mpxValue) {
		if (mpxValue <=10)
			return 0;
		return (mpxValue - 10) / 6;
	}

	public static boolean isCurrent(int mpxValue) {
		return mpxValue == ASSIGNMENT_REMAINING_WORK
				|| mpxValue == ASSIGNMENT_ACTUAL_WORK
				|| mpxValue == ASSIGNMENT_ACTUAL_OVERTIME_WORK;
	}
	
	private static BidiMap map  = new DualHashBidiMap();
	static {
		map.put(BigInteger.valueOf(1),HasTimeDistributedData.REMAINING_WORK);
		map.put(BigInteger.valueOf(2),HasTimeDistributedData.ACTUAL_WORK);
	//	map.put(BigInteger.valueOf(3),HasTimeDistributedData.OVERTIME_WORK);

		map.put(BigInteger.valueOf(4),HasTimeDistributedData.BASELINE_WORK);
		map.put(BigInteger.valueOf(5),HasTimeDistributedData.BASELINE_COST);
		map.put(BigInteger.valueOf(6),HasTimeDistributedData.ACTUAL_COST);
		
		map.put(BigInteger.valueOf(16),HasTimeDistributedData.BASELINE1_WORK);
		map.put(BigInteger.valueOf(17),HasTimeDistributedData.BASELINE1_COST);
		map.put(BigInteger.valueOf(22),HasTimeDistributedData.BASELINE2_WORK);
		map.put(BigInteger.valueOf(23),HasTimeDistributedData.BASELINE2_COST);
		map.put(BigInteger.valueOf(28),HasTimeDistributedData.BASELINE3_WORK);
		map.put(BigInteger.valueOf(29),HasTimeDistributedData.BASELINE3_COST);
		map.put(BigInteger.valueOf(34),HasTimeDistributedData.BASELINE4_WORK);
		map.put(BigInteger.valueOf(35),HasTimeDistributedData.BASELINE4_COST);
		map.put(BigInteger.valueOf(40),HasTimeDistributedData.BASELINE5_WORK);
		map.put(BigInteger.valueOf(41),HasTimeDistributedData.BASELINE5_COST);
		map.put(BigInteger.valueOf(46),HasTimeDistributedData.BASELINE6_WORK);
		map.put(BigInteger.valueOf(47),HasTimeDistributedData.BASELINE6_COST);
		map.put(BigInteger.valueOf(52),HasTimeDistributedData.BASELINE7_WORK);
		map.put(BigInteger.valueOf(53),HasTimeDistributedData.BASELINE7_COST);
		map.put(BigInteger.valueOf(58),HasTimeDistributedData.BASELINE8_WORK);
		map.put(BigInteger.valueOf(59),HasTimeDistributedData.BASELINE8_COST);
		map.put(BigInteger.valueOf(64),HasTimeDistributedData.BASELINE9_WORK);
		map.put(BigInteger.valueOf(65),HasTimeDistributedData.BASELINE9_COST);
		map.put(BigInteger.valueOf(70),HasTimeDistributedData.BASELINE10_WORK);
		map.put(BigInteger.valueOf(71),HasTimeDistributedData.BASELINE10_COST);
	}
	
	
}
