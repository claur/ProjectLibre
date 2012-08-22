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
package com.projity.configuration;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.digester.Digester;

import com.projity.contrib.util.Log;
import com.projity.contrib.util.LogFactory;
import com.projity.field.Field;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.task.NormalTask;
import com.projity.pm.task.Project;
import com.projity.strings.Messages;
import com.projity.util.ClassUtils;
import com.projity.util.Environment;

/**
 * Dictionary of all fields
 */
public class FieldDictionary {
	private static Log log = LogFactory.getLog(FieldDictionary.class);
	private HashedMap map = new HashedMap();
	private HashMap actionsMap = new HashMap();
	public void addField(Field field) {
		if (field.isServer()&&Environment.getStandAlone()) return;
		field.setClass(clazz);
		if (field.build() || true) {
			if (field.isIndexed()) {
				for (int i=0; i < field.getIndexes(); i++) {
					Field indexField = field.createIndexedField(i);
					log.debug("adding indexfield " + clazz.getName() + "." +  indexField.getName() + " id " + indexField.getId() + " field "+ indexField);
					map.put(indexField.getId(),indexField);					
					
				}
			} else {
				log.debug("adding field " + clazz.getName() + "." +  field.getName() + " " + field);
				map.put(field.getId(),field);
				if (field.getAction() != null)
					actionsMap.put(field.getAction(),field);

			}
		} else {
			log.warn("Field not added" + field.getId());
		}
	}
	
	public Field getActionField(String action) {
		return (Field) actionsMap.get(action);
	}
	
	public static FieldDictionary getInstance() {
		return Configuration.getInstance().getFieldDictionary();
	}
	
	public Field getFieldFromId(String id) {
		return (Field) map.get(id);
	}
	
	private Class clazz;
	public void setClassName(String className) {
		//System.out.println("						<include name=\""+className+"\"/>");
		try {
			clazz = ClassUtils.forName(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void populateListWithFieldsOfType(List list, Class clazz) {
		populateListWithFieldsOfType(list,new Class[] {clazz});
	}

	/** Fill a collection with all fields that are applicable to one or more types
	 * specified by clazz.  The collection is sorted alpha-numerically by field name.
	 * Lists by type should probably just be cached in static variables.
	 * @param collection - collection to fill
	 * @param clazz - array of class types
	 */
	public void populateListWithFieldsOfType(List list, Class[] clazz) {
		MapIterator i = map.mapIterator();
		while (i.hasNext()) {
			Object key = i.next();
			Field field = (Field) i.getValue();
			if (field.isApplicable(clazz))
				list.add(field);
		 }
		Collections.sort(list);
	}
	
	Collection getAllFields() {
 		return map.values();
	}

	private LinkedList taskFields = new LinkedList();
	private LinkedList resourceFields = new LinkedList();
	private LinkedList assignmentFields = new LinkedList();
	private LinkedList dependencyFields = new LinkedList();
	private LinkedList projectFields = new LinkedList();
	private LinkedList taskAndAssignmentFields = new LinkedList();	
	private LinkedList resourceAndAssignmentFields = new LinkedList();	
	
	void setDonePopulating() { 
// in case we use a FastHashMap do this:		map.setFast(true);
		taskFields = new LinkedList();
		resourceFields = new LinkedList();
		assignmentFields = new LinkedList();
		dependencyFields = new LinkedList();
		projectFields = new LinkedList();
		taskAndAssignmentFields = new LinkedList();	
		resourceAndAssignmentFields = new LinkedList();	

		populateListWithFieldsOfType(taskFields,NormalTask.class);
		populateListWithFieldsOfType(resourceFields,ResourceImpl.class);
		populateListWithFieldsOfType(assignmentFields,Assignment.class);
		populateListWithFieldsOfType(dependencyFields,Dependency.class);
		populateListWithFieldsOfType(projectFields,Project.class);
		populateListWithFieldsOfType(taskAndAssignmentFields,new Class[] {NormalTask.class,Assignment.class});		
		populateListWithFieldsOfType(resourceAndAssignmentFields,new Class[] {Resource.class,Assignment.class});		
	}

	/**
	 * @return Returns the assignmentFields.
	 */
	public LinkedList getAssignmentFields() {
		return assignmentFields;
	}
	/**
	 * @return Returns the dependencyFields.
	 */
	public LinkedList getDependencyFields() {
		return dependencyFields;
	}
	/**
	 * @return Returns the projectFields.
	 */
	public LinkedList getProjectFields() {
		return projectFields;
	}
	/**
	 * @return Returns the resourceFields.
	 */
	public LinkedList getResourceFields() {
		return resourceFields;
	}
	/**
	 * @return Returns the taskAndAssignmentFields.
	 */
	public LinkedList getTaskAndAssignmentFields() {
		return taskAndAssignmentFields;
	}
	/**
	 * @return Returns the taskFields.
	 */
	public LinkedList getTaskFields() {
		return taskFields;
	}
	/**
	 * @return Returns the resourceAndAssignmentFields.
	 */
	public LinkedList getResourceAndAssignmentFields() {
		return resourceAndAssignmentFields;
	}

/**
 * Extract fields that have extra status, and also optionally that have validOnOjbectCreate status
 */
	public static LinkedList extractExtraFields(Collection from, final boolean mustBeValidOnObjectCreate) {
		LinkedList result = new LinkedList();
		CollectionUtils.select(from, new Predicate() {
			public boolean evaluate(Object arg0) {
				Field f = (Field)arg0;
				return f.isExtra() && (!mustBeValidOnObjectCreate || f.isValidOnObjectCreate());
			}},result);
		return result;
	}

	public static void addDigesterEvents(Digester digester){
//		digester.addObjectCreate("*/fieldDictionary", "com.projity.configuration.FieldDictionary");
		digester.addFactoryCreate("*/fieldDictionary", "com.projity.configuration.FieldDictionaryFactory");
		digester.addSetNext("*/fieldDictionary", "setFieldDictionary", "com.projity.configuration.FieldDictionary");	//TODO can we do this more easily
	    digester.addSetProperties("*/fieldDictionary/class","name","className"); // object is field dictionary
		digester.addObjectCreate("*/fieldDictionary/class/field", "com.projity.field.Field");
		digester.addSetProperties("*/fieldDictionary/class/field");
		digester.addSetNext("*/fieldDictionary/class/field", "addField", "com.projity.field.Field");

		digester.addObjectCreate("*/field/select", "com.projity.field.StaticSelect"); // create a select
		digester.addSetProperties("*/field/select"); // set name of select
		digester.addSetNext("*/field/select", "setSelect", "com.projity.field.StaticSelect"); // attach to field
		

		digester.addObjectCreate("*/field/choice", "com.projity.field.DynamicSelect"); // create a cohice
		digester.addSetProperties("*/field/choice"); // set name of choice, finder and list methods
		digester.addSetNext("*/field/choice", "setSelect", "com.projity.field.DynamicSelect"); // attach to field
		
		digester.addObjectCreate("*/field/select/option","com.projity.field.SelectOption"); // create an option when seeing one
		digester.addSetProperties("*/field/select/option"); // get key and value properties
		digester.addSetNext("*/field/select/option","addOption","com.projity.field.SelectOption"); // add option to select

		digester.addObjectCreate("*/field/range","com.projity.field.Range"); // create an option when seeing one
		digester.addSetProperties("*/field/range"); // get key and value properties
		digester.addSetNext("*/field/range","setRange","com.projity.field.Range"); // add option to select

		//non intrusive method to reduce role options, otherwise Select should be modified to depend on a specific object
		digester.addObjectCreate("*/field/filter","com.projity.field.OptionsFilter"); 
		digester.addSetProperties("*/field/filter"); 
		digester.addSetNext("*/field/filter","setFilter","com.projity.field.OptionsFilter");

		String fieldAccessibleClass = Messages.getMetaString("FieldAccessible");
		digester.addObjectCreate("*/field/permission",fieldAccessibleClass); 
		digester.addSetProperties("*/field/permission"); 
		digester.addSetNext("*/field/permission","setAccessControl","com.projity.field.FieldAccessible");

	}
	private static void tabbedStringToHtmlRow(StringBuffer result,String colString, boolean header) {
		result.append("<tr>");
		String [] cols= colString.split("\t");
		for (String col : cols)
			result.append(header ? "<th>" : "<td>").append(col).append(header ? "</th>" : "</td>");
		result.append("</tr>");
	}

	private static void fieldsToHtmlTable(final StringBuffer result,String title,Collection fields) {
		result.append("<p><b>").append(title).append("</b><br />");
		result.append("<table border='1'>");
		tabbedStringToHtmlRow(result,Field.getMetadataStringHeader(),true);
		CollectionUtils.forAllDo(FieldDictionary.getInstance().getProjectFields(), new Closure() {
			public void execute(Object arg0) {
				tabbedStringToHtmlRow(result,((Field)arg0).getMetadataString(),false);
			}}
		);
		result.append("</table>");
		result.append("</p>");
	}
	public static void generateFieldDoc(String fileName) {
		StringBuffer result = new StringBuffer();
		result.append("<html><body>");
		fieldsToHtmlTable(result,"Project Fields",FieldDictionary.getInstance().getProjectFields());
		fieldsToHtmlTable(result,"Resource Fields",FieldDictionary.getInstance().getProjectFields());
		fieldsToHtmlTable(result,"Task Fields",FieldDictionary.getInstance().getProjectFields());
		fieldsToHtmlTable(result,"Assignment Fields",FieldDictionary.getInstance().getProjectFields());
		fieldsToHtmlTable(result,"Dependency Fields",FieldDictionary.getInstance().getProjectFields());
		result.append("</body></html>");
		
		try {
			new FileOutputStream(fileName).write(result.toString().getBytes());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String args[]) {
		generateFieldDoc("d:/pod/fields.html");
	}
	
	public static HashMap getAliasMap() {
		HashMap aliasMap = new HashMap();
		MapIterator i = getInstance().map.mapIterator();
		while (i.hasNext()) {
			Object key = i.next();
			Field field = (Field) i.getValue();
			if (field.getAlias() != null)
				aliasMap.put(field.getId(), field.getAlias());
		}
		return aliasMap;
	}

	public static void setAliasMap(HashMap aliasMap) {
		if (aliasMap == null)
			return;
		Iterator i = aliasMap.keySet().iterator();
		while (i.hasNext()) {
			String fieldId  = (String) i.next();
			Field f = Configuration.getFieldFromId(fieldId);
			if (f != null)
				f.setAlias((String) aliasMap.get(fieldId));
		}
	}

}
