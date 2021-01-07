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
package com.projectlibre1.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;

import com.projectlibre1.strings.Messages;

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
    
    public static String LANGUAGES = "default"; //defaults are set in configuration.xml
 }
