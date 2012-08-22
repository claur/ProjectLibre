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
package com.projity.algorithm;

import java.util.LinkedList;
import com.projity.pm.assignment.HasTimeDistributedData;
import com.projity.pm.assignment.functor.AssignmentFieldClosureCollection;
import com.projity.pm.assignment.functor.DateAtValueFunctor;
import com.projity.pm.time.HasStartAndEnd;

/**
 * A reverse query is one which calculates a date given a value instead of vice-versa.
 */
public class ReverseQuery {
	private Object type;
	private LinkedList fieldSet = new LinkedList();
	private LinkedList selectFromSet = new LinkedList();
	private LinkedList groupBySet = new LinkedList();
	private HasTimeDistributedData root;
	private double valueToFind;
	private boolean allowDefaultAssignments = false;

	/**
	 * 
	 */
	private ReverseQuery(Object type,HasTimeDistributedData root, double valueToFind) {
		super();
		this.type = type;
		this.root = root;
		this.valueToFind = valueToFind;
	}
	
	public static long getDateAtValue(Object type,HasTimeDistributedData root, double valueToFind, boolean allowDefaultAssignments) {
		ReverseQuery reverseQuery = getInstance(type, root, valueToFind);
		reverseQuery.allowDefaultAssignments = allowDefaultAssignments;
		return reverseQuery.getDate();
	}


	public static ReverseQuery getInstance(Object type, HasTimeDistributedData root, double valueToFind) {
		return new ReverseQuery(type, root, valueToFind);
	}

	/**
	 * @return Returns the type.
	 */
	public Object getType() {
		return type;
	}

	public void addField(Object field) {
		if (field != null)
			fieldSet.add(field);
	}
	
	public void addSelectFrom(Object selectFrom) {
		if (selectFrom != null)
			selectFromSet.add(selectFrom);
	}
	
	public void addGroupBy(Object groupBy) {
		if (groupBy != null)
			groupBySet.add(groupBy);
	}


	/* (non-Javadoc)
	 * @see com.projity.algorithm.DoubleValue#getValue()
	 */
	public long getDate() {
		root.buildReverseQuery(this);
		Query query = Query.getInstance();
		DateAtValueFunctor dateAtValue = DateAtValueFunctor.getInstance(valueToFind, AssignmentFieldClosureCollection.getInstance(fieldSet));	
		
		IntervalGenerator interval = null;
		if (!groupBySet.isEmpty())
			interval = IntervalGeneratorSet.getInstance(groupBySet);
		query.selectFrom(selectFromSet)
			 .groupBy(interval)
			 .action(dateAtValue)
			 .execute();
		return dateAtValue.getDate();
	}


//	public static void iterate(Object type,HasTimeDistributedData root) {
//		ReverseQuery reverseQuery = getInstance(type, root, 0);
//		reverseQuery.iterate();
//	}
//
//
//	private void iterate() {
//		root.buildReverseQuery(this);
//		Query query = Query.getInstance();
//		PrintValueFunctor functor = PrintValueFunctor.getInstance(ClosureCollectionSum.getInstance(AssignmentFieldClosureCollection.getInstance(fieldSet)));
//		query.selectFrom(selectFromSet)
//			 .groupBy(IntervalGeneratorSet.getInstance(groupBySet))
//			 .action(functor)
//			 .execute();
//	}
	
	public final boolean isAllowDefaultAssignments() {
		return allowDefaultAssignments;
	}
	public final void setAllowDefaultAssignments(boolean allowDefaultAssignments) {
		this.allowDefaultAssignments = allowDefaultAssignments;
	}
}
