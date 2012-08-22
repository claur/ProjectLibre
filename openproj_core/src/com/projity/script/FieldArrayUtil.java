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

Attribution Information: Attribution Copyright Notice: Copyright � 2006, 2007
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
package com.projity.script;

import java.util.Iterator;

import com.projity.configuration.Dictionary;
import com.projity.field.Field;
import com.projity.graphic.configuration.SpreadSheetFieldArray;
import com.projity.strings.Messages;

public class FieldArrayUtil {
	public static final String HIDDEN_SUFFIX="_Hidden";
	public static final String SERVER_SUFFIX="_Server";

	public static final String taskFieldArrayCategory="taskSpreadsheet";
	public static final String resourceFieldArrayCategory="resourceSpreadsheet";
	public static final String portfolioFieldArrayCategory="portfolioSpreadsheet";
	public static final String projectFieldArrayCategory="projectSpreadsheet";
	public static final String projectFieldArrayCategoryHidden="projectSpreadsheet"+HIDDEN_SUFFIX;
	public static final String timesheetFieldArrayCategory="timesheetSpreadsheet";


	public static SpreadSheetFieldArray removeNonWebFields(SpreadSheetFieldArray in) {
		SpreadSheetFieldArray out= (SpreadSheetFieldArray)in.clone();
		Iterator i = out.iterator();
		while (i.hasNext()) {
			Field f = (Field)i.next();
			if (f.getId().equals("Field.indicators") || /*f.isGraphical() ||*/
					(projectFieldArrayCategory.equals(out.getCategory())&&"Field.id".equals(f.getId())))
				i.remove();

		}
		return out;
	}

	public static SpreadSheetFieldArray getFieldArray(int type, String id){
		if (id==null) return null;
		String category=typetoCategory(type);
//		System.out.println("getFieldArray type="+type+", id="+id+", category="+category);
		Object o=getFromId(category, id);
		if (o==null) {
			o=getFromId(category+SERVER_SUFFIX,id);
		}
		if (o == null)
			return null;
		else return removeNonWebFields((SpreadSheetFieldArray)o);
	}
	public static SpreadSheetFieldArray getHiddenFieldArray(int type, String id){
		if (id==null) return null;
		String category=typetoHiddenCategory(type);
//		System.out.println("getHiddenFieldArray type="+type+", id="+id+", category="+category);
		Object o=getFromId(category, id);
		if (o==null) return null;
		else return removeNonWebFields((SpreadSheetFieldArray)o);
	}

//	public static SpreadSheetFieldArray createFieldArray(String category, String id){
//		Object o=getFromId(category, id);
//		if (o==null) return null;
//		if (o instanceof SpreadSheetFieldArray) return removeNonWebFields((SpreadSheetFieldArray)o);
//		else return null;
//	}


//	public static String getDefaultConfigObjectId(String category){
//		if (portfolioFieldArrayCategory.equals(category)) return "Spreadsheet.Project.portfolio";//"Spreadsheet.Portfolio.properties";
//		if (taskFieldArrayCategory.equals(category)) return "Spreadsheet.Task.entry";
//		if (resourceFieldArrayCategory.equals(category)) return "Spreadsheet.Resource.entryWorkResources";
//		if (projectFieldArrayCategory.equals(category)) return "Spreadsheet.Portfolio.properties";
//		if ((projectFieldArrayCategoryHidden).equals(category)) return "Spreadsheet.Project.Hidden";
//		if (timesheetFieldArrayCategory.equals(category)) return "Spreadsheet.Timesheet.Default";
//
//		else return null;
//	}



//	public static SpreadSheetFieldArray getFieldArray(int type, String id) {
//		String category = typetoCategory(type);
////		System.out.println("getFieldArray type="+type+", category="+category+", id="+id);
////		if (id == null)
////			id = getDefaultConfigObjectId(category);
////		System.out.println("getFieldArray type="+type+", category="+category+", id="+id);
//		return removeNonWebFields((SpreadSheetFieldArray) getFromId(category,id));
//
//	}

//	public static String getDefaultConfigObjectId(int type) {
//		return getDefaultConfigObjectId(typetoCategory(type));
//	}
	public static String typetoCategory(int type){
		if (type==ScriptRunner.TASK) return taskFieldArrayCategory;
		else if (type==ScriptRunner.RESOURCE) return resourceFieldArrayCategory;
		else if (type==ScriptRunner.PROJECT) return projectFieldArrayCategory;
		//else if (type==ScriptRunner.PORTFOLIO) return  portfolioFieldArrayCategory;
		else if (type==ScriptRunner.ASSIGNMENT) return timesheetFieldArrayCategory;

		else return null;
	}
	public static String typetoHiddenCategory(int type){
		if (type==ScriptRunner.PROJECT) return projectFieldArrayCategoryHidden;

		else return null;
	}
//	public static int categoryToType(String ca){
//		if (type==ScriptRunner.TASK) return taskFieldArrayCategory;
//		else if (type==ScriptRunner.RESOURCE) return resourceFieldArrayCategory;
//		else if (type==ScriptRunner.PROJECT) return projectFieldArrayCategory;
//		else if (type==ScriptRunner.PORTFOLIO) return portfolioFieldArrayCategory;
//		else if (type==ScriptRunner.ASSIGNMENT) return timesheetFieldArrayCategory;
//
//		else return null;
//	}
//	public static String typetoCategory(int type,boolean hidden){
//		if (hidden) return typetoCategory(type)+HIDDEN_SUFFIX;
//		else return typetoCategory(type);
//	}



	//more general than SpreadSheetFieldArray.getFromId, useful?
	private static final Object getFromId(String category, String id) {
//		System.out.println("getFromId category="+category+", id="+id);
		Object result = Dictionary.get(category, Messages.getString(id));
		if (result == null)
			result = Dictionary.get(category, id);
		return result;
	}


}
