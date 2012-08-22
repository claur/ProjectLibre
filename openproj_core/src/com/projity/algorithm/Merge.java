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

import java.util.Comparator;
import java.util.Date;

import org.apache.commons.collections.Closure;

/**
 * Merge is a functor which groups time intervals together and calls a vistor object on the resulting merged intervals
 * You can specify a comparator as well to determine whether two intervals are mergeable
 */
public class Merge implements Closure {
	boolean started = false;
	long currentStart;
	long currentEnd;
	Object currentObject = null;
	Closure callBack;
	Comparator comparator = null;
//	boolean ignoreZeroValueIntervals = false;
	
	public static Merge getInstance(Closure callBack) {
		return new Merge(callBack);
	}

	public static Merge getInstance(Closure callBack, Comparator comparator) {
		return new Merge(callBack, comparator);
	}

	private Merge(Closure callBack) {
		this.callBack = callBack;
		initializeDates();
	}
	
	private Merge(Closure callBack, Comparator comparator) {
		this(callBack);
		this.comparator = comparator;
	}
	
	public void setCallBack(Closure callBack) {
		this.callBack = callBack;
	}
	
	private void initializeDates() {
		currentStart = Long.MAX_VALUE;
		currentEnd = Long.MIN_VALUE;
	}
	
	private void treatCurrentInterval() {
		double value = 0.0D;
		if (currentObject != null)
			value = ((DoubleValue)currentObject).getValue();
		else {
			// System.out.println("Merge.treatCurrentInterval currentObject is null - using 0.0 for value");
		}
//		if (!ignoreZeroValueIntervals || value != 0.0D)
		callBack.execute(IntervalValue.getInstance(currentStart, currentEnd, value)); // finish previous
		started = false;
		initializeDates();
	}
	/* 
	 * Execution of the functor.
	 * This takes care of merging and calling the visitor.
	 */
	public void execute(Object obj) {
		Query query = (Query)obj;
		IntervalGenerator generator = query.getGroupByGenerator();
		if (generator.isCurrentActive()) {
			if (started && comparator != null && comparator.compare(currentObject,generator.current()) != 0)  // if comparator doesnt match
				treatCurrentInterval();

			// starting
			currentObject = generator.current();
			started = true;
			
			currentStart = Math.min(currentStart,query.getStart()) ;			
			currentEnd = Math.max(currentEnd,query.getEnd());
//System.out.println("in Merge" + new Date(currentStart) + " " + new Date(currentEnd));			
			if (!generator.hasNext()) // if no more intervals, then terminate this one
				treatCurrentInterval();
		} else {
			if (generator.current() != null) {
				// ending
				started = false;
				treatCurrentInterval();				
			}
		}
	}
	
	public class MergedInterval {
		public MergedInterval(long start, long end, Object template) {
			this.start = start;
			this.end = end;
			this.template = template;
		}
		long start;
		long end;
		Object template;
		/**
		 * @return Returns the end.
		 */
		public long getEnd() {
			return end;
		}

		/**
		 * @return Returns the start.
		 */
		public long getStart() {
			return start;
		}

		/**
		 * @return Returns the template.
		 */
		public Object getTemplate() {
			return template;
		}
		
		public String toString() {
			return new Date(start) + "-" + new Date(end) + template;
		}

	}
}
