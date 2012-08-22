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
package com.projity.grouping.core.transform.sorting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.Closure;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.projity.configuration.FieldDictionary;
import com.projity.field.Field;
import com.projity.field.FieldConverter;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.transform.CommonTransform;
import com.projity.grouping.core.transform.HierarchicObject;

/**
 *
 */
public class NodeSorter extends CommonTransform implements Comparator{
	protected boolean showSummary = true;
	protected boolean showEmptyLines = true;
	protected boolean showEndEmptyLines = true;
	protected List fields = null;

	protected NodeModel model;
	public NodeModel getModel() {
		return model;
	}
	public void setModel(NodeModel model) {
		this.model = model;
	}
	
	
	
    public boolean isShowEmptyLines() {
        return showEmptyLines;
    }
    public void setShowEmptyLines(boolean showEmptyLines) {
        this.showEmptyLines = showEmptyLines;
    }
    
    public boolean isShowEndEmptyLines() {
		return showEndEmptyLines;
	}
	public void setShowEndEmptyLines(boolean showEndEmptyLines) {
		this.showEndEmptyLines = showEndEmptyLines;
	}
	
    public boolean isShowSummary() {
        return showSummary;
    }
    public void setShowSummary(boolean showSummary) {
        this.showSummary = showSummary;
    }
    
	protected boolean showEmptySummaries = true;
	public boolean isShowEmptySummaries() {
		return showEmptySummaries;
	}
	public void setShowEmptySummaries(boolean showEmptySummaries) {
		this.showEmptySummaries = showEmptySummaries;
	}
	protected boolean showAssignments = true;
    public boolean isShowAssignments() {
		return showAssignments;
	}
	public void setShowAssignments(boolean showAssignments) {
		this.showAssignments = showAssignments;
	}
	
	protected boolean preserveHierarchy = true;
	public boolean isPreserveHierarchy() {
		return preserveHierarchy;
	}
	public void setPreserveHierarchy(boolean preserveHierarchy) {
		this.preserveHierarchy = preserveHierarchy;
	}
	
	public List sortList(List list,boolean preserverHierarchy){
	    return sortList(list,this,preserverHierarchy);
	}
	public List sortList(List list,Comparator comparator,boolean preserveHierarchy){
		Collections.sort(list,comparator);
		if (preserveHierarchy)
		for (Iterator i=list.iterator();i.hasNext();){
			HierarchicObject child=(HierarchicObject)i.next();
			if (child.getChildren().size()>0) sortList(child.getChildren(), comparator,true);
		}
		return list;
	}

public List getList() {
	try {
		return (List) pool.borrowObject();
	} catch (Exception e) {
		return null;
	}
}

public void recycleList(List list) {
	try {
		list.clear();
		pool.returnObject(list);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

private GenericObjectPool pool = new GenericObjectPool(new ListFactory());

private class ListFactory extends BasePoolableObjectFactory {
	public Object makeObject() { //claur
		return new ArrayList();
	}

	public void activateObject(Object arg0){ //claur{
//		Stack stack = (Stack)arg0;
//		stack.clear();
	}
}



















    public void setRedefinitionCallBack(Closure callback){}
	
    
    //used by label formula
    protected String getFieldName(String fieldId){
		Field field=FieldDictionary.getInstance().getFieldFromId(fieldId);
		return field.getName();
    }
   /* protected Object getFieldValue(String fieldId,Node node){
		Field field=FieldDictionary.getInstance().getFieldFromId(fieldId);
		return field.getValue(node,model,null);
    }*/
    
    protected String toString(Object value){
        return FieldConverter.toString(value,value.getClass(),null);
    }
    protected String toString(String fieldId,Object value){
		Field field=FieldDictionary.getInstance().getFieldFromId(fieldId);
		if (field.hasOptions()) {
			return field.convertValueToStringUsingOptions(value);
		} //test, use getValue instead
		else return toString(value);
    } 
    
    public String getGroupName(Object impl){
        return "";
    }
    public String getGroupName(String fieldId,Object object){
        return getFieldName(fieldId)+": "+object;
    }
    public String getStringGroupName(String label,Object object){
        return label+": "+object;
    }

    protected ListIterator currentSorter=null;
    public int compare(Object o1, Object o2) {
        NodeSorter sorter;
        List sorters=getSubTransforms();
        if (sorters!=null){
            currentSorter=sorters.listIterator();
	        while (currentSorter.hasNext()){
	            sorter=(NodeSorter)currentSorter.next();
	            int r=sorter.compare(o1,o2);
	            if (r!=0){
	                currentSorter.previous();
	                return r;
	            }
	        }
        }
        return 0;
    }
    public ListIterator getCurrentSorter(){
        if (currentSorter!=null) return currentSorter;
        if (getSubTransforms()==null) return null;
        else return getSubTransforms().listIterator();
    }

    
}