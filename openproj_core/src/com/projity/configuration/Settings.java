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

Attribution Information: Attribution Copyright Notice: Copyright (c) 2006, 2007 
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
package com.projity.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;

import com.projity.strings.Messages;

/**
 * Stores global constants related to the application settings. Some are also read in from config
 */
public class Settings {
	public static final boolean CLUSTERED=true;
	public static final String CLUSTER_NODES="jnp://localhost:1100";
	public static final String SITE_HOME = "http://www.projectlibre.com";
//	public static final String HELP_HOME = "http://projectlibre.org/wiki/index.php?title=";
	public static final String HELP_HOME = "http://www.projectlibre.com/projectlibre-documentation";
	public static final String WEB_APP= "web";
	public static final String WEB_HOME = SITE_HOME + "/" + WEB_APP;
	
	public static int NUM_ARRAY_BASELINES = 11; // should get set from config file 
	public static int NUM_COST_RATES = 5;
	public static String COST_RATE_NAMES = "A;B;C;D;E";
	public static int NUM_HIERARCHIES = 11;
	public static int CALENDAR_INTERVALS = 5;
	public static int numBaselines() {return NUM_ARRAY_BASELINES + 2;}// "baseline" + basline1-10 + timesheet
	public static int numHierarchies() {return NUM_HIERARCHIES;}// "wbs" + hierachy 1-10
	public static int numGanttBaselines() {return NUM_ARRAY_BASELINES + 1;}// "baseline" + basline1-10
	public static String LIST_SEPARATOR = Messages.getString("Symbol.listSeparator"); //; for example
	public static String LEFT_BRACKET = Messages.getString("Symbol.leftBracket"); //[ for example
	public static String RIGHT_BRACKET = Messages.getString("Symbol.rightBracket"); //] for example	
	public static String PERCENT = Messages.getString("Symbol.percent"); //] for example
	public static String SLASH = Messages.getString("Symbol.slash"); // / for example
	public static String ELLIPSIS = Messages.getString("Symbol.ellipsis"); // / for example ...
	public static int STRING_LIST_LIMIT = 20; // number of items to put in a string list.  If exceeded, will display a message saying all can't be displayed.  See class StringList
    public static boolean SHOW_HELP_LINKS = true;
    public static String VERSION_TYPE_STANDALONE="standalone";
    public static String VERSION_TYPE_SERVER="server";
 }
