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
package com.projity.graphic.configuration;

import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.digester.Digester;

import com.projity.configuration.Configuration;
import com.projity.configuration.Dictionary;

import com.projity.configuration.NamedItem;
import com.projity.field.Field;
import com.projity.pm.assignment.TimeDistributedHelper;
import com.projity.strings.Messages;
import com.projity.workspace.SavableToWorkspace;
import com.projity.workspace.WorkspaceSetting;

/**
 *
 */
public class SpreadSheetFieldArray extends ArrayList implements NamedItem, Cloneable, WorkspaceSetting {
	private static final long serialVersionUID = 6310711336308730391L;
	transient Map map = new LinkedHashMap();
	transient boolean userCreated = false;
	ArrayList<Integer> widths = null;//new ArrayList<Integer>();
	public Object clone() {
		return super.clone();
	}

	public SpreadSheetFieldArray() {

	}

	public SpreadSheetFieldArray makeUserDefinedCopy() {
		SpreadSheetFieldArray newOne = (SpreadSheetFieldArray) clone();
		newOne.setId(null); // it's user defined
		newOne.setName(Dictionary.generateUniqueName(this));
		newOne.userCreated = true;
		return newOne;
	}
	public SpreadSheetFieldArray makeEditableVersion() {
		SpreadSheetFieldArray f = this;
		if (!f.isUserDefined()) {
			f = f.makeUserDefinedCopy();
			Dictionary.add(f);
		}
		return f;
	}
	public SpreadSheetFieldArray insertField(int position,Field field) {
		SpreadSheetFieldArray f = makeEditableVersion();
		f.add(position,field);
		//f.widths.add(field.getColumnWidth());
		return f;
	}

	public SpreadSheetFieldArray removeField(int position) {
		SpreadSheetFieldArray f = makeEditableVersion();
		f.remove(position);
		//f.widths.remove(position);
		return f;
	}
	public SpreadSheetFieldArray move(int oldPosition, int newPosition) {
		SpreadSheetFieldArray f = makeEditableVersion();
		Field field = (Field) f.remove(oldPosition);
		//Integer w = f.widths.remove(oldPosition);
		SpreadSheetFieldArray result = f.insertField(newPosition,field);
		//result.widths.set(newPosition,w);
		return result;


	}

//	public void setWidth(int column, int width) {
//		widths.set(column,width);
//	}
	/**
	 * Equality is based on name, not on contents
	 */
	public boolean equals(Object arg0) {
		if (! (arg0 instanceof SpreadSheetFieldArray))
			return false;
		return name == ((SpreadSheetFieldArray)arg0).getName();
	}
	private String name = null;
	private String category;
	private String cellStyleId;
	private String actionListId;
	private String id = null;

    public String getCellStyleId() {
        return cellStyleId;
    }
    public void setCellStyleId(String cellStyleId) {
        this.cellStyleId = cellStyleId;
    }
    public CellStyle getCellStyle(){
        CellStyles cellStyles=CellStyles.getInstance();
        if (cellStyleId==null||cellStyleId.length()==0)
            return cellStyles.getDefaultStyle();
        CellStyle style=cellStyles.getStyle(cellStyleId);
        if (style==null) style=cellStyles.getDefaultStyle();
        return style;
    }

    public String getActionListId() {
        return actionListId;
    }
    public void setActionListId(String actionListId) {
        this.actionListId = actionListId;
    }
    public ActionList getActionList(){
        ActionLists actionLists=ActionLists.getInstance();
        if (actionListId==null||actionListId.length()==0)
            return actionLists.getDefaultActionList();
        ActionList actionList=actionLists.getActionList(actionListId);
        if (actionList==null) actionList=actionLists.getDefaultActionList();
        return actionList;
    }

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	public void setId(String messageId) {
		this.id = messageId;
		if (name == null)
			setName(Messages.getString(messageId));
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return getName();
	}
	/**
	 * @return Returns the category.
	 */
	public String getCategory() {
		return category;
	}
	/**
	 * @param category The category to set.
	 */
	public void setCategory(String category) {
		this.category = category;
	}


	public boolean isUserDefined() {
		return id == null;
	}


	public void addField(String fieldId) {
		Field field = Configuration.getFieldFromId(fieldId);
		if (field != null) {
			if (mapFieldTo != null) {
				map.put(fieldId,mapFieldTo);
				mapFieldTo = null;
			}

			add(field);
			//widths.add(field.getColumnWidth());
		} else {
//			System.out.println("field is null in SpreadSheetFieldArray addField : ");
		}
	}
	public void removeField(String fieldId) {
		if (fieldId==null) return;
		map.remove(fieldId);
		for (int i = 0; i < size(); i++) {
			Field field=(Field)get(i);
			if (fieldId.equals(field.getId())) {
				remove(i);
				//widths.remove(i);
			}

		}
	}

	public String mapFieldTo;
	//root node needs to be Dictionary
	public static void addDigesterEvents(Digester digester){
		digester.addObjectCreate("*/spreadsheet", "com.projity.graphic.configuration.SpreadSheetFieldArray");
	    digester.addSetProperties("*/spreadsheet");
		digester.addSetNext("*/spreadsheet", "add", "com.projity.configuration.NamedItem");
	    digester.addSetProperties("*/spreadsheet/columns/column");
		digester.addCallMethod("*/spreadsheet/columns/column", "addField", 	0);

	}
	/**
	 * @return
	 */
	public Object next() {
		// TODO Auto-generated method stub
		return null;
	}

	public static final SpreadSheetFieldArray getFromId(String category, String id) {
		SpreadSheetFieldArray result = (SpreadSheetFieldArray) Dictionary.get(category, Messages.getString(id));
		if (result == null)
			result = (SpreadSheetFieldArray) Dictionary.get(category, id);
		return result;
	}

	public final String getMapFieldTo() {
		return mapFieldTo;
	}

	public final void setMapFieldTo(String mapFieldTo) {
		this.mapFieldTo = mapFieldTo;
	}

	public final String getMappedValue(String key) {
		return (String) map.get(key);
	}

	public static Object[] toIdArray(Object[] fieldArray) {
		Object[] result = new Object[fieldArray.length];
		for (int i = 0; i < fieldArray.length; i++)
			result[i] = TimeDistributedHelper.getIdForObject(fieldArray[i]);
		return result;
	}

	public static Object[] fromIdArray(Object[] fieldArray) {
		Object[] result = new Object[fieldArray.length];
		for (int i = 0; i < fieldArray.length; i++)
			result[i] = TimeDistributedHelper.getObjectFromId((String) fieldArray[i]);
		return result;
	}

	public static Collection toIdArray(Collection fieldArray) {
		ArrayList result = new ArrayList(fieldArray.size());
		Iterator i = fieldArray.iterator();
		while (i.hasNext()) {
			result.add(TimeDistributedHelper.getIdForObject(i.next()));
		}
		return result;
	}
	public static Collection fromIdArray(Collection fieldArray) {
		ArrayList result = new ArrayList(fieldArray.size());
		Iterator i = fieldArray.iterator();
		while (i.hasNext()) {
			result.add(TimeDistributedHelper.getObjectFromId((String) i.next()));
		}
		return result;
	}

	public boolean isUserCreated() {
		return userCreated;
	}

	public void setUserCreated(boolean userCreated) {
		this.userCreated = userCreated;
	}

	public int getWidth(int column) {
		return (widths!=null&&column>=0&&column<widths.size())?widths.get(column):-1;
	}




	public WorkspaceSetting createWorkspace(int context) {
		Workspace ws = new Workspace();
		ws.fields.addAll(toIdArray(this));
		if (widths!=null) ws.widths.addAll(widths);
		return ws;
	}

	public void restoreWorkspace(WorkspaceSetting w, int context) {
		Workspace ws = (Workspace) w;
		addAll(fromIdArray(ws.fields));
		if (ws.version>0.0f&&ws.widths!=null&&ws.widths.size()>0){
			widths=new ArrayList<Integer>(ws.widths.size());
			widths.addAll(ws.widths);
		}
	}
	public static class Workspace implements WorkspaceSetting {
		private static final long serialVersionUID = -4517935309304612237L;
		ArrayList<Integer> widths = new ArrayList<Integer>();
		ArrayList fields = new ArrayList();
		float version=1.0f;
	}

	public ArrayList<Integer> getWidths() {
		return widths;
	}

	public void setWidths(ArrayList<Integer> widths) {
		this.widths = widths;
	}

	public static SpreadSheetFieldArray restore(WorkspaceSetting spreadsheetWorkspace,String name,int context){
		SpreadSheetFieldArray fieldArray = new SpreadSheetFieldArray();
		fieldArray.setCategory(SpreadSheetCategories.taskSpreadsheetCategory);
		fieldArray.restoreWorkspace(spreadsheetWorkspace, context);
		fieldArray.setName(name);
		Dictionary.add(fieldArray);
		return fieldArray;
	}

}
