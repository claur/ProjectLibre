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
package com.projectlibre1.script.object;

import java.util.ArrayList;
import java.util.List;

import com.projectlibre1.datatype.Duration;
import com.projectlibre1.datatype.TimeUnit;
import com.projectlibre1.field.FieldContext;
import com.projectlibre1.pm.assignment.TimeDistributedFields;
import com.projectlibre1.pm.key.HasId;
import com.projectlibre1.pm.key.HasName;
import com.projectlibre1.pm.scheduling.TimeSheetSchedule;
import com.projectlibre1.pm.time.MutableHasStartAndEnd;
import com.projectlibre1.server.data.ExtendedDistributionData;

public class DistributionHolder implements HasId,HasName,MutableHasStartAndEnd,TimeDistributedFields,TimeSheetSchedule{
	protected long id,uniqueId;
	protected String name;
	protected long start,end,c;
	protected List<ExtendedDistributionData> dist;
	protected List<DistributionHolder> children;
	protected long parentId;
	protected Object extension;
	public List<ExtendedDistributionData> getDist() {
		return dist;
	}
	public void setDist(List<ExtendedDistributionData> dist) {
		this.dist = dist;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getUniqueId() {
		return uniqueId;
	}
	public void setUniqueId(long uniqueId) {
		this.uniqueId = uniqueId;
	}
	public String getName() {
		return name;
	}
	public String getName(FieldContext context){
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getEnd() {
		return end;
	}
	public void setEnd(long end) {
		this.end = end;
	}
	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
	public List<DistributionHolder> getChildren() {
		return children;
	}
	public void setChildren(List<DistributionHolder> children) {
		this.children = children;
	}
	public void addChild(DistributionHolder child){
		if (children==null) children=new ArrayList<DistributionHolder>();
		children.add(child);
	}
	public Object getExtension() {
		return extension;
	}
	public void setExtension(Object extension) {
		this.extension = extension;
	}
	public long getParentId() {
		return parentId;
	}
	public void setParentId(long parentId) {
		this.parentId = parentId;
	}
	public long getC() {
		return c;
	}
	public void setC(long c) {
		this.c = c;
	}
	
	
	//TimeDistributedFields
	protected double work,actualWork,cost,actualCost;
	public double getActualCost() {
		return actualCost;
	}
	public void setActualCost(double actualCost) {
		this.actualCost = actualCost;
	}
	public double getActualWork() {
		return actualWork;
	}
	public void setActualWork(double actualWork) {
		this.actualWork = actualWork;
	}
	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
	public double getWork() {
		return work;
	}
	public void setWork(double work) {
		this.work = work;
	}

	public String dumpTimeDistributedFields(){
		return "work="+work+", actualWork="+actualWork+", cost="+cost+", ="+actualCost+", ="+actualCost;
	}
	
	
	public boolean fieldHideActualCost(FieldContext fieldContext) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean fieldHideActualFixedCost(FieldContext fieldContext) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean fieldHideActualWork(FieldContext fieldContext) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean fieldHideBaselineCost(int numBaseline, FieldContext fieldContext) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean fieldHideBaselineWork(int numBaseline, FieldContext fieldContext) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean fieldHideCost(FieldContext fieldContext) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean fieldHideWork(FieldContext fieldContext) {
		// TODO Auto-generated method stub
		return false;
	}
	public double getActualCost(FieldContext fieldContext) {
		return getActualCost();
	}
	public double getActualFixedCost(FieldContext fieldContext) {
		// TODO Auto-generated method stub
		return 0;
	}
	public long getActualWork(FieldContext fieldContext) {
		return Duration.getInstance(Math.round(getActualWork()),TimeUnit.DAYS);
	}
	public double getBaselineCost(int numBaseline, FieldContext fieldContext) {
		// TODO Auto-generated method stub
		return 0;
	}
	public long getBaselineWork(int numBaseline, FieldContext fieldContext) {
		// TODO Auto-generated method stub
		return 0;
	}
	public double getCost(FieldContext fieldContext) {
		 return getCost();
	}
	public double getFixedCost(FieldContext fieldContext) {
		// TODO Auto-generated method stub
		return 0;
	}
	public double getRemainingCost(FieldContext fieldContext) {
		// TODO Auto-generated method stub
		return 0;
	}
	public long getRemainingWork(FieldContext fieldContext) {
		// TODO Auto-generated method stub
		return 0;
	}
	public long getWork(FieldContext fieldContext) {
		return Duration.getInstance(Math.round(getWork()),TimeUnit.DAYS);
	}
	public boolean isReadOnlyActualWork(FieldContext fieldContext) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean isReadOnlyFixedCost(FieldContext fieldContext) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean isReadOnlyRemainingWork(FieldContext fieldContext) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean isReadOnlyWork(FieldContext fieldContext) {
		// TODO Auto-generated method stub
		return false;
	}
	public void setActualWork(long actualWork, FieldContext fieldContext) {
		setActualWork(actualWork);
	}
	public void setFixedCost(double fixedCost, FieldContext fieldContext) {
		// TODO Auto-generated method stub
		
	}
	public void setRemainingWork(long remainingWork, FieldContext fieldContext) {
		// TODO Auto-generated method stub
		
	}
	public void setWork(long work, FieldContext fieldContext) {
		// TODO Auto-generated method stub
		
	}
	//TimeSheetSchedule
	
	
	public double getPercentComplete() {
		return work==0?0.0:actualWork/work;
	}
	public long getRemainingDuration() {
		// TODO Auto-generated method stub
		return 0;
	}
	public boolean isComplete() {
		// TODO Auto-generated method stub
		return false;
	}
	public void setComplete(boolean complete) {
		// TODO Auto-generated method stub
		
	}
	public void setPercentComplete(double percentComplete) {
		// TODO Auto-generated method stub
		
	}
	public void setRemainingDuration(long remainingDuration) {
		// TODO Auto-generated method stub
		
	}
	
	//Schedule?
	
	
	
	
	
}
