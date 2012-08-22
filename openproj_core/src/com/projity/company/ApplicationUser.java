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
package com.projity.company;

/**
 * Information about the person running the application
 */
public interface ApplicationUser {
	public static final long MAX_IDLE_TIME=3600000L;
	public static final long INFINITE_IDLE_TIME=Long.MAX_VALUE;

	//roles
	//public static final int INTERNAL = 4;
	public static final int TEAM_MEMBER = 8;
	public static final int PROJECT_MANAGER = 16;
	public static final int TEAM_RESOURCE = 32;

	//licenses
	public static final int INACTIVE = 0;
	public static final int POWER_USER = 1;
	public static final int LITE_USER = 2;
	//options
	public static final int ADMINISTRATOR = 1;
	public static final int EXTERNAL = 2; //license option
	
//	public static final int PORTFOLIO_MANANGER = 13;
//	public static final int RESOURCE_MANANGER = 14;
//	public static final int TEAM_LEAD = 7;
//	public static final int EXECUTIVE = 8;
//	public static final int HUMAN_RESOURCES = 9;
//	public static final int SUBSCRIBER = 10;
//	public static final int SUBCONTRACTOR = 12; // no costs

	public static final String ADMINISTRATOR_NAME = "Administrator";
	public static final String TEAM_MEMBER_NAME = "TeamMember";
	public static final String PROJECT_MANAGER_NAME = "ProjectManager";
	public static final String INTERNAL_NAME = "Internal";

	public static final String LITE_USER_NAME = "LiteUser";
	public static final String POWER_USER_NAME = "PowerUser";
	
	public static final String EXTERNAL_NAME = "External";

	boolean isAdministrator();
	boolean isExternal();

	long getUniqueId();

	long getResourceId();

	String getName();
}
