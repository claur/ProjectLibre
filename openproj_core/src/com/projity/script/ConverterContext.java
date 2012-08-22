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

import com.projity.graphic.configuration.SpreadSheetFieldArray;
import com.projity.grouping.core.transform.TransformList;
import com.projity.grouping.core.transform.filtering.NodeFilter;
import com.projity.script.object.TimeIntervals;
import com.projity.strings.Messages;



public class  ConverterContext implements Cloneable{
	public static final int ALL=0;
	public static final int CHANGE=1;
	public static final int SCALE=2;
	public static final int TRANSLATE=3;

	protected int type;
	protected String id,name;
	protected String fieldArrayId;
	protected String hiddenFieldArrayId;
	protected String filterId;
	protected String groupFieldId,sortFieldId;
	protected String roles;
	protected boolean distribution;
	protected int summaryLevel=-1;
	protected long s=Long.MAX_VALUE,e=0;
	protected int scale=TimeIntervals.WEEK;

	protected int timeType;

	protected int actionType;
	protected long winS=0,winE=Long.MAX_VALUE;



	public int getActionType() {
		return actionType;
	}
	public void setActionType(int actionType) {
		this.actionType = actionType;
	}

	public ConverterContext(){

	}

	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}


	public String getName() {
		if (name == null)
			name = Messages.getString(id);
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFieldArrayId() {
		return fieldArrayId;
	}
	public void setFieldArrayId(String fieldArrayId) {
		this.fieldArrayId = fieldArrayId;
	}



	public String getHiddenFieldArrayId() {
		return hiddenFieldArrayId;
	}
	public void setHiddenFieldArrayId(String hiddenFieldArrayId) {
		this.hiddenFieldArrayId = hiddenFieldArrayId;
	}

	public String getFilterId() {
		return filterId;
	}
	public void setFilterId(String filterId) {
		this.filterId = filterId;
	}
	public String getGroupFieldId() {
		return groupFieldId;
	}
	public void setGroupFieldId(String groupFieldId) {
		this.groupFieldId = groupFieldId;
	}
	public String getSortFieldId() {
		return sortFieldId;
	}
	public void setSortFieldId(String sortFieldId) {
		this.sortFieldId = sortFieldId;
	}


	public int getSummaryLevel() {
		return summaryLevel;
	}
	public void setSummaryLevel(int summaryLevel) {
		this.summaryLevel = summaryLevel;
	}




	public String getRoles() {
		return roles;
	}
	public void setRoles(String roles) {
		this.roles = roles;
	}

	public long getE() {
		return e;
	}

	public void setE(long e) {
		this.e = e;
	}

	public long getS() {
		return s;
	}

	public void setS(long s) {
		this.s = s;
	}


	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public long getWinE() {
		return winE;
	}

	public void setWinE(long winE) {
		this.winE = winE;
	}

	public long getWinS() {
		return winS;
	}

	public void setWinS(long winS) {
		this.winS = winS;
	}



//	public GlobalCache getCache(){
//		return (GlobalCache)confiObjectStore;
//	}

//	public ConfigObjectStore getConfiObjectStore() {
//		return confiObjectStore;
//	}



//	public void setConfiObjectStore(ConfigObjectStore confiObjectStore) {
//		this.confiObjectStore = confiObjectStore;
//	}
//
//	public IdentifiedObject getConfigObject(String category) {
//		return confiObjectStore==null?null:confiObjectStore.getConfigObject(category);
//	}


//	public SpreadSheetFieldArray getFieldArray(String category) {
//		return FieldArrayUtil.createFieldArray(category,fieldArrayId==null?FieldArrayUtil.getDefaultConfigObjectId(category):fieldArrayId);
//	}

	protected transient SpreadSheetFieldArray fieldArray;
	protected transient boolean fieldArrayInitialized;
	public SpreadSheetFieldArray retrieveFieldArray(){ //to "retrieve" avoid "get"
		if (fieldArrayInitialized) return fieldArray;
		fieldArrayInitialized=true;
		if (fieldArrayId==null) return null;
		fieldArray=FieldArrayUtil.getFieldArray(type, fieldArrayId);
		return fieldArray;
	}

	protected transient SpreadSheetFieldArray hiddenFieldArray;
	protected transient boolean hiddenFieldArrayInitialized;
	public SpreadSheetFieldArray retrieveHiddenFieldArray(){ //to "retrieve" avoid "get"
		if (hiddenFieldArrayInitialized) return hiddenFieldArray;
		hiddenFieldArrayInitialized=true;
		if (hiddenFieldArrayId==null) return null;
		hiddenFieldArray=FieldArrayUtil.getHiddenFieldArray(type, hiddenFieldArrayId);
		return hiddenFieldArray;
	}

	protected transient NodeFilter filter;
	protected transient boolean filterInitialized;
	public NodeFilter retrieveFilter(){ //to "retrieve" avoid "get"
		if (filterInitialized) return filter;
		filterInitialized=true;
		if (filterId==null) return null;
		filter=(NodeFilter)TransformList.getInstance("report_filters").getTransform(filterId);
		if (filter==null) filter=(NodeFilter)TransformList.getInstance("user_filters").getTransform(filterId);
		if (filter==null) filter=(NodeFilter)TransformList.getInstance("hidden_filters").getTransform(filterId);
		//check user and hidden filters groups too
//		if (filterId==null)
//			return null;
//		filter=filterFromList("report_filters",filterId);
//		if (filter==null)
//			filter=filterFromList("report_user",filterId);
//		if (filter==null)
//			filter=filterFromList("Filters.user",filterId);
//		if (filter==null)
//			filter=filterFromList("Filters.hidden",filterId);

		return filter;
	}

//	private NodeFilter filterFromList(String filterListId,String filterId) {
//		TransformList list = TransformList.getInstance(filterListId);
//		if (list != null)
//			return (NodeFilter)list.getTransform(filterId);
//		else
//			return null;
//	}

	public String toString(){
		return "{"+
			"id="+id+", "+
			"type="+type+", "+
			"fieldArrayId="+fieldArrayId+", "+
			"hiddenFieldArrayId="+hiddenFieldArrayId+", "+
			"filterId="+filterId+", "+
			"}";
	}


	public Object clone(){
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}

	}
	public boolean isDistribution() {
		return distribution;
	}
	public void setDistribution(boolean distribution) {
		this.distribution = distribution;
	}
	public int getTimeType() {
		return timeType;
	}
	public void setTimeType(int timeType) {
		this.timeType = timeType;
	}



}
