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
package com.projectlibre1.grouping.core.transform;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.event.EventListenerList;

import org.apache.commons.collections.Closure;

import com.projectlibre1.grouping.core.transform.filtering.NodeFilter;
import com.projectlibre1.grouping.core.transform.grouping.NodeGrouper;
import com.projectlibre1.grouping.core.transform.sorting.NodeSorter;
import com.projectlibre1.grouping.core.transform.transformer.NodeTransformer;

/**
 *
 */
public class ViewTransformer{
	public static final String FILTER_NONE_ID="Filter.None";
	public static final String SORTER_NONE_ID="Sorter.None";
	public static final String GROUPER_NONE_ID="Grouper.None";


	protected List filters=null;
	protected List sorters=null;
	protected List groupers=null;

    protected NodeFilter hiddenFilter;
    protected NodeFilter userFilter;
    protected NodeSorter hiddenSorter;
    protected NodeTransformer transformer;
    protected NodeSorter userSorter;
    protected NodeGrouper hiddenGrouper;
    protected NodeGrouper userGrouper;

    protected String hiddenFilterId;
    protected String userFilterId=FILTER_NONE_ID;
    protected String hiddenSorterId;
    protected String userSorterId=SORTER_NONE_ID;
    protected String hiddenGrouperId;
    protected String userGrouperId=GROUPER_NONE_ID;
    protected String transformerId;

    protected boolean hiddenFilterIdDirty=false;
    protected boolean userFilterIdDirty=false;
    protected boolean hiddenSorterIdDirty=false;
    protected boolean userSorterIdDirty=false;
    protected boolean hiddenGrouperIdDirty=false;
    protected boolean userGrouperIdDirty=false;
    protected boolean transformerIdDirty=false;




    public List getFilterList() {
        return filters;
    }
    public void setFilters(String slist) {
        StringTokenizer st=new StringTokenizer(slist,";, \t");
        filters=new ArrayList();
        while (st.hasMoreTokens()) filters.add(st.nextToken());
    }
    public List getSorterList() {
        return sorters;
    }
    public void setSorters(String slist) {
        StringTokenizer st=new StringTokenizer(slist,";, \t");
        sorters=new ArrayList();
        while (st.hasMoreTokens()) sorters.add(st.nextToken());
    }
    public List getGrouperList() {
        return groupers;
    }
    public void setGroupers(String slist) {
        StringTokenizer st=new StringTokenizer(slist,";, \t");
        groupers=new ArrayList();
        while (st.hasMoreTokens()) groupers.add(st.nextToken());
    }


    private Closure redefinition=new Closure(){
        public void execute(Object o){
            fireTransformerChanged(o);
        }
    };

    public void setFilterId(TransformId id) {
        if (id.isHidden()){
        	hiddenFilterId=id.getId();
        	hiddenFilterIdDirty=true;
        }
        else{
        	userFilterId=id.getId();
        	userFilterIdDirty=true;
        }
    }
    public void setSorterId(TransformId id) {
        if (id.isHidden()){
        	hiddenSorterId=id.getId();
        	hiddenSorterIdDirty=true;
        }
        else{
        	userSorterId=id.getId();
        	userSorterIdDirty=true;
        }
    }
    public void setGrouperId(TransformId id) {
        if (id.isHidden()){
        	hiddenGrouperId=id.getId();
        	hiddenGrouperIdDirty=true;
        }
        else{
        	userGrouperId=id.getId();
        	userGrouperIdDirty=true;
        }
    }
    public void setTransformerId(TransformId id) {
        transformerId=id.getId();
        transformerIdDirty=true;
    }

	public String getHiddenFilterId() {
		return hiddenFilterId;
	}
	public void setHiddenFilterId(String hiddenFilterId) {
		this.hiddenFilterId = hiddenFilterId;
		hiddenFilterIdDirty=true;
		fireTransformerChanged(this);
	}
	public String getHiddenGrouperId() {
		return hiddenGrouperId;
	}
	public void setHiddenGrouperId(String hiddenGrouperId) {
		this.hiddenGrouperId = hiddenGrouperId;
		hiddenGrouperIdDirty=true;
		fireTransformerChanged(this);
	}
	public String getHiddenSorterId() {
		return hiddenSorterId;
	}
	public void setHiddenSorterId(String hiddenSorterId) {
		this.hiddenSorterId = hiddenSorterId;
		hiddenSorterIdDirty=true;
		fireTransformerChanged(this);
	}
	public String getUserFilterId() {
		return userFilterId;
	}
	public void setUserFilterId(String userFilterId) {
		this.userFilterId = userFilterId;
		userFilterIdDirty=true;
		fireTransformerChanged(this);
	}
	public String getUserGrouperId() {
		return userGrouperId;
	}
	public void setUserGrouperId(String userGrouperId) {
		this.userGrouperId = userGrouperId;
		userGrouperIdDirty=true;
		fireTransformerChanged(this);
	}
	public String getUserSorterId() {
		return userSorterId;
	}
	public void setUserSorterId(String userSorterId) {
		this.userSorterId = userSorterId;
		userSorterIdDirty=true;
		fireTransformerChanged(this);
	}
	public String getTransformerId() {
		return transformerId;
	}
	public void setTransformerId(String transformerId) {
		this.transformerId = transformerId;
		transformerIdDirty=true;
		fireTransformerChanged(this);
	}

	public void update(){
		fireTransformerChanged(this);
	}



    private CommonTransform getTransform(String listName,String id){
    	TransformList list=TransformList.getInstance(listName);
    	if (list==null) return null;
    	CommonTransform transform=(CommonTransform)list.getTransform(id);
    	if (transform!=null) transform.askForParameters();
    	return transform;
    }

    public NodeFilter getHiddenFilter() {
        if (hiddenFilterIdDirty){
        	hiddenFilter=(NodeFilter)getTransform("hidden_filters",hiddenFilterId);
        	hiddenFilter.setRedefinitionCallBack(redefinition);
        	hiddenFilterIdDirty=false;
        }
        return hiddenFilter;
    }
    public void setHiddenFilter(NodeFilter hiddenFilter) {
        this.hiddenFilter = hiddenFilter;
    }
    public NodeGrouper getHiddenGrouper() {
        if (hiddenGrouperIdDirty){
        	hiddenGrouper=(NodeGrouper)getTransform("hidden_groupers",hiddenGrouperId);
        	hiddenGrouper.setRedefinitionCallBack(redefinition);
        	hiddenGrouperIdDirty=false;
        }
       return hiddenGrouper;
    }
    public void setHiddenGrouper(NodeGrouper hiddenGrouper) {
        this.hiddenGrouper = hiddenGrouper;
    }
    public NodeSorter getHiddenSorter() {
        if (hiddenSorterIdDirty){
        	hiddenSorter=(NodeSorter)getTransform("hidden_sorters",hiddenSorterId);
        	hiddenSorter.setRedefinitionCallBack(redefinition);
        	hiddenSorterIdDirty=false;
        }
       return hiddenSorter;
    }
    public void setHiddenSorter(NodeSorter hiddenSorter) {
        this.hiddenSorter = hiddenSorter;
    }
    public NodeFilter getUserFilter() {
        if (userFilterIdDirty){
        	userFilter=(NodeFilter)getTransform("user_filters",userFilterId);
        	userFilterIdDirty=false;
        }
        return userFilter;
    }
    public void setUserFilter(NodeFilter userFilter) {
        this.userFilter = userFilter;
    }
    public NodeGrouper getUserGrouper() {
       if (userGrouperIdDirty){
       		userGrouper=(NodeGrouper)getTransform("user_groupers",userGrouperId);
       		userGrouperIdDirty=false;
       }
       return userGrouper;
    }
    public void setUserGrouper(NodeGrouper userGrouper) {
        this.userGrouper = userGrouper;
    }
    public NodeSorter getUserSorter() {
        if (userSorterIdDirty){
        	userSorter=(NodeSorter)getTransform("user_sorters",userSorterId);
        	userSorterIdDirty=false;
        }
        return userSorter;
    }
    public void setUserSorter(NodeSorter userSorter) {
        this.userSorter = userSorter;
    }
    public NodeTransformer getTransformer() {
        if (transformerIdDirty){
            transformer=(NodeTransformer)getTransform("transformers",transformerId);
        	//hiddenFilter.setRedefinitionCallBack(redefinition);
           transformerIdDirty=false;
        }
        return transformer;
    }
    public void settransformer(NodeTransformer transformer) {
        this.transformer = transformer;
    }

    public boolean isShowSummary(){
    	if (!isShowSummary(getHiddenFilter())) return false;
       	if (!isShowSummary(getUserFilter())) return false;
       	if (!isShowSummary(getHiddenSorter())) return false;
       	if (!isShowSummary(getUserSorter())) return false;
       	if (!isShowSummary(getHiddenGrouper())) return false;
       	if (!isShowSummary(getUserGrouper())) return false;
    	return true;
    }
    private boolean isShowSummary(CommonTransform t){return (t==null)?true:t.isShowSummary();}

    public boolean isPreserveHierarchy(){
    	if (!isPreserveHierarchy(getHiddenFilter())) return false;
       	if (!isPreserveHierarchy(getUserFilter())) return false;
       	if (!isPreserveHierarchy(getHiddenSorter())) return false;
       	if (!isPreserveHierarchy(getUserSorter())) return false;
       	if (!isPreserveHierarchy(getHiddenGrouper())) return false;
       	if (!isPreserveHierarchy(getUserGrouper())) return false;
    	return true;
    }
    private boolean isPreserveHierarchy(CommonTransform t){return (t==null)?true:t.isPreserveHierarchy();}

    public boolean isShowAssignments(){
    	if (!isShowAssignments(getHiddenFilter())) return false;
       	if (!isShowAssignments(getUserFilter())) return false;
       	if (!isShowAssignments(getHiddenSorter())) return false;
       	if (!isShowAssignments(getUserSorter())) return false;
       	if (!isShowAssignments(getHiddenGrouper())) return false;
       	if (!isShowAssignments(getUserGrouper())) return false;
    	return true;
    }
    private boolean isShowAssignments(CommonTransform t){return (t==null)?true:t.isShowAssignments();}

    public boolean isShowEmptyLines(){
    	if (!isNoneSorter()) return false;
    	if (!isNoneGrouper()) return false;
    	if (!isShowEmptyLines(getHiddenFilter())) return false;
       	if (!isShowEmptyLines(getUserFilter())) return false;
//       	if (!isShowEmptyLines(getHiddenSorter())) return false;
//       	if (!isShowEmptyLines(getUserSorter())) return false;
//       	if (!isShowEmptyLines(getHiddenGrouper())) return false;
//       	if (!isShowEmptyLines(getUserGrouper())) return false;
    	return true;
    }
    private boolean isShowEmptyLines(CommonTransform t){return (t==null)?true:t.isShowEmptyLines();}

    public boolean isShowEndEmptyLines(){
    	if (!isNoneSorter()) return false;
    	if (!isNoneGrouper()) return false;
    	if (!isShowEndEmptyLines(getHiddenFilter())) return false;
       	if (!isShowEndEmptyLines(getUserFilter())) return false;
//    	if (!isShowEndEmptyLines(getHiddenSorter())) return false;
//       	if (!isShowEndEmptyLines(getUserSorter())) return false;
    	return true;
    }
    private boolean isShowEndEmptyLines(CommonTransform t){return (t==null)?true:t.isShowEndEmptyLines();}

    public boolean isShowEmptySummaries(){
    	if (!isShowEmptySummaries(getHiddenFilter())) return false;
       	if (!isShowEmptySummaries(getUserFilter())) return false;
//       	if (!isShowEmptyLines(getHiddenSorter())) return false;
//       	if (!isShowEmptyLines(getUserSorter())) return false;
//       	if (!isShowEmptyLines(getHiddenGrouper())) return false;
//       	if (!isShowEmptyLines(getUserGrouper())) return false;
    	return true;
    }
    private boolean isShowEmptySummaries(CommonTransform t){return (t==null)?true:t.isShowEmptySummaries();}

//    public boolean isShowBadBranches(){
//    	if (!isShowBadBranches(getHiddenFilter())) return false;
//       	if (!isShowBadBranches(getUserFilter())) return false;
//    	return true;
//    }
//    private boolean isShowBadBranches(CommonTransform t){return (t==null)?true:t.isShowBadBranches();}


    public boolean isTreatAssignmentsAsTasks(){
    	return false;
    }




    public boolean isNoneFilter(){
    	return userFilterId==null||FILTER_NONE_ID.equals(userFilterId);
    }
    public boolean isNoneSorter(){
    	return userSorterId==null||SORTER_NONE_ID.equals(userSorterId);
    }
    public boolean isNoneGrouper(){
    	return userGrouperId==null||GROUPER_NONE_ID.equals(userGrouperId);
    }






	protected EventListenerList listenerList = new EventListenerList();

	public void addViewTransformerListener(ViewTransformerListener l) {
		listenerList.add(ViewTransformerListener.class, l);
	}
	public void removeViewTransformerListener(ViewTransformerListener l) {
		listenerList.remove(ViewTransformerListener.class, l);
	}
	public ViewTransformerListener[] getTimeScaleListeners() {
		return (ViewTransformerListener[]) listenerList.getListeners(ViewTransformerListener.class);
	}
	protected void fireTransformerChanged(Object source) {
		Object[] listeners = listenerList.getListenerList();
		ViewTransformerEvent e = null;
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ViewTransformerListener.class) {
				if (e == null) {
					e = new ViewTransformerEvent(source);
				}
				((ViewTransformerListener) listeners[i + 1]).transformerChanged(e);
			}
		}
	}
    public EventListener[] getListeners(Class listenerType) {
    	return listenerList.getListeners(listenerType);
       }














}
