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
package com.projity.script.object;

import java.util.ArrayList;
import java.util.List;

import com.projity.datatype.Duration;
import com.projity.datatype.TimeUnit;
import com.projity.field.FieldContext;
import com.projity.pm.assignment.TimeDistributedFields;
import com.projity.pm.key.HasId;
import com.projity.pm.key.HasName;
import com.projity.pm.scheduling.TimeSheetSchedule;
import com.projity.pm.time.MutableHasStartAndEnd;
import com.projity.server.data.ExtendedDistributionData;

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
