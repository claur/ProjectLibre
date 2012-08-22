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

import com.projity.configuration.Configuration;
import com.projity.strings.Messages;

public interface TimeDistributedConstants {
	public static final Object PERCENT_ALLOC = Messages.getString("Field.percentAlloc");
	public static final Object OVERALLOCATED = Messages.getString("Text.Overallocated");
	public static final Object SELECTED = Messages.getString("Text.Selected");
	public static final Object OTHER_PROJECTS = Messages.getString("Text.OtherProjects");
	public static final Object THIS_PROJECT = Messages.getString("Text.ThisProject");
	
	public static final Object AVAILABILITY = Configuration.getFieldFromId("Field.resourceAvailability");
	
	public static final Object WORK = Configuration.getFieldFromId("Field.work");
	public static final Object ACTUAL_WORK = Configuration.getFieldFromId("Field.actualWork");
	public static final Object REMAINING_WORK = Messages.getString("Field.remainingWork");	
	//TODO add overtime work
	public static final Object BASELINE_WORK = Configuration.getFieldFromId("Field.baselineWork");
	
	public static final Object COST = Configuration.getFieldFromId("Field.cost");
	public static final Object ACTUAL_COST = Configuration.getFieldFromId("Field.actualCost");
	public static final Object FIXED_COST = Configuration.getFieldFromId("Field.fixedCost");
	public static final Object ACTUAL_FIXED_COST = Configuration.getFieldFromId("Field.actualFixedCost");
	public static final Object REMAINING_COST = Messages.getString("Field.remainingCost");	
	public static final Object BASELINE_COST = Configuration.getFieldFromId("Field.baselineCost");
	public static final Object ACWP = Configuration.getFieldFromId("Field.acwp");	
	public static final Object BCWP = Configuration.getFieldFromId("Field.bcwp");
	public static final Object BCWS = Configuration.getFieldFromId("Field.bcws");
	public static final Object BASELINE1_COST = Configuration.getFieldFromId("Field.baseline1Cost");
	public static final Object BASELINE2_COST = Configuration.getFieldFromId("Field.baseline2Cost");
	public static final Object BASELINE3_COST = Configuration.getFieldFromId("Field.baseline3Cost");
	public static final Object BASELINE4_COST = Configuration.getFieldFromId("Field.baseline4Cost");
	public static final Object BASELINE5_COST = Configuration.getFieldFromId("Field.baseline5Cost");
	public static final Object BASELINE6_COST = Configuration.getFieldFromId("Field.baseline6Cost");
	public static final Object BASELINE7_COST = Configuration.getFieldFromId("Field.baseline7Cost");
	public static final Object BASELINE8_COST = Configuration.getFieldFromId("Field.baseline8Cost");
	public static final Object BASELINE9_COST = Configuration.getFieldFromId("Field.baseline9Cost");
	public static final Object BASELINE10_COST = Configuration.getFieldFromId("Field.baseline10Cost");

	public static final Object BASELINE1_WORK = Configuration.getFieldFromId("Field.baseline1Work");
	public static final Object BASELINE2_WORK = Configuration.getFieldFromId("Field.baseline2Work");
	public static final Object BASELINE3_WORK = Configuration.getFieldFromId("Field.baseline3Work");
	public static final Object BASELINE4_WORK = Configuration.getFieldFromId("Field.baseline4Work");
	public static final Object BASELINE5_WORK = Configuration.getFieldFromId("Field.baseline5Work");
	public static final Object BASELINE6_WORK = Configuration.getFieldFromId("Field.baseline6Work");
	public static final Object BASELINE7_WORK = Configuration.getFieldFromId("Field.baseline7Work");
	public static final Object BASELINE8_WORK = Configuration.getFieldFromId("Field.baseline8Work");
	public static final Object BASELINE9_WORK = Configuration.getFieldFromId("Field.baseline9Work");
	public static final Object BASELINE10_WORK = Configuration.getFieldFromId("Field.baseline10Work");

}
