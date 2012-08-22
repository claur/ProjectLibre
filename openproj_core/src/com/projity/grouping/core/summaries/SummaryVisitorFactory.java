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
package com.projity.grouping.core.summaries;

import java.util.Date;
import java.util.Hashtable;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

import com.projity.datatype.DurationFormat;
import com.projity.strings.Messages;


/**
 *
 */
public class SummaryVisitorFactory implements SummaryNames {
	
	public static SummaryVisitor getInstance(int type, Class clazz, boolean forceDeep) {
		//TODO implement this	
	    
		if (type==SAME){
		    return new ShallowChildWalker(new Same());
		}

	    
		if (clazz == Boolean.class) {
			switch (type) {
				case OR:
					return new LeafWalker(new Maximum());
				case AND:
					return new LeafWalker(new Minimum());
			}
		} else {
	
			switch (type) {
				case MAXIMUM:
					return new LeafWalker(new Maximum());
				case MINIMUM:
					return new LeafWalker(new Minimum());
				case COUNT_ALL:
					return new DeepChildWalker(new Count(),true);
				case SUM:
					if (forceDeep)
						return new DeepChildWalker(new Sum(),true);
					else
						return new LeafWalker(new Sum());
				case AVERAGE:
					return new LeafWalker(new Average());
				case AVERAGE_FIRST_SUBLEVEL:
					return new ShallowChildWalker(new Average());
				case COUNT_FIRST_SUBLEVEL:				
					return new ShallowChildWalker(new Count());
				case LIST:
					return new LeafWalker(new ConcatTextSummaryVisitor());
				case NONE:
				case THIS:
				default:	
			}
		}
		return null;
	}


	private static BidiMap COST_SUMMARY_MAP = new DualHashBidiMap();
	static {
		COST_SUMMARY_MAP.put(Messages.getString("Summary.None"), new Integer(NONE));
		COST_SUMMARY_MAP.put(Messages.getString("Summary.Average"), new Integer(AVERAGE));
		COST_SUMMARY_MAP.put(Messages.getString("Summary.AverageFirstSublevel"), new Integer(AVERAGE_FIRST_SUBLEVEL));		
		COST_SUMMARY_MAP.put(Messages.getString("Summary.Maximum"), new Integer(MAXIMUM));
		COST_SUMMARY_MAP.put(Messages.getString("Summary.Minimum"), new Integer(MINIMUM));
		COST_SUMMARY_MAP.put(Messages.getString("Summary.Sum"), new Integer(SUM));
	}

	private static BidiMap DATE_SUMMARY_MAP = new DualHashBidiMap();
	static {
		DATE_SUMMARY_MAP.put(Messages.getString("Summary.None"), new Integer(NONE));		
		DATE_SUMMARY_MAP.put(Messages.getString("Summary.Maximum"), new Integer(MAXIMUM));
		DATE_SUMMARY_MAP.put(Messages.getString("Summary.Minimum"), new Integer(MINIMUM));		
	}

	private static BidiMap DURATION_SUMMARY_MAP = COST_SUMMARY_MAP;
	
	private static BidiMap FLAG_SUMMARY_MAP = new DualHashBidiMap();
	static {
		FLAG_SUMMARY_MAP.put(Messages.getString("Summary.None"), new Integer(NONE));		
		FLAG_SUMMARY_MAP.put(Messages.getString("Summary.OR"), new Integer(OR));
		FLAG_SUMMARY_MAP.put(Messages.getString("Summary.AND"), new Integer(AND));		
	}

	private static BidiMap NUMBER_SUMMARY_MAP = new DualHashBidiMap();
	static {
		NUMBER_SUMMARY_MAP.put(Messages.getString("Summary.None"), new Integer(NONE));		
		NUMBER_SUMMARY_MAP.put(Messages.getString("Summary.Average"), new Integer(AVERAGE));
		NUMBER_SUMMARY_MAP.put(Messages.getString("Summary.AverageFirstSublevel"), new Integer(AVERAGE_FIRST_SUBLEVEL));		
		NUMBER_SUMMARY_MAP.put(Messages.getString("Summary.CountAll"), new Integer(COUNT_ALL));		
		NUMBER_SUMMARY_MAP.put(Messages.getString("Summary.CountFirstSublevel"), new Integer(COUNT_FIRST_SUBLEVEL));
		NUMBER_SUMMARY_MAP.put(Messages.getString("Summary.CountNonsummaries"), new Integer(COUNT_NONSUMMARIES));		
		NUMBER_SUMMARY_MAP.put(Messages.getString("Summary.Maximum"), new Integer(MAXIMUM));
		NUMBER_SUMMARY_MAP.put(Messages.getString("Summary.Minimum"), new Integer(MINIMUM));
		NUMBER_SUMMARY_MAP.put(Messages.getString("Summary.Sum"), new Integer(SUM));
	}
	
	private static BidiMap TEXT_SUMMARY_MAP = new DualHashBidiMap();
	static {
		TEXT_SUMMARY_MAP.put(Messages.getString("Summary.None"), new Integer(NONE));		
		TEXT_SUMMARY_MAP.put(Messages.getString("Summary.List"), new Integer(LIST));
	}
	
	private static Hashtable ALL_SUMMARY_MAP = new Hashtable();
	static {
		ALL_SUMMARY_MAP.put("None", new Integer(NONE));		
		ALL_SUMMARY_MAP.put("This", new Integer(THIS));		
		ALL_SUMMARY_MAP.put("List", new Integer(LIST));		
		ALL_SUMMARY_MAP.put("Average", new Integer(AVERAGE));
		ALL_SUMMARY_MAP.put("AverageFirstSublevel", new Integer(AVERAGE_FIRST_SUBLEVEL));		
		ALL_SUMMARY_MAP.put("CountAll", new Integer(COUNT_ALL));		
		ALL_SUMMARY_MAP.put("CountFirstSublevel", new Integer(COUNT_FIRST_SUBLEVEL));
		ALL_SUMMARY_MAP.put("CountNonsummaries", new Integer(COUNT_NONSUMMARIES));		
		ALL_SUMMARY_MAP.put("Maximum", new Integer(MAXIMUM));
		ALL_SUMMARY_MAP.put("Minimum", new Integer(MINIMUM));
		ALL_SUMMARY_MAP.put("Sum", new Integer(SUM));
		ALL_SUMMARY_MAP.put("OR", new Integer(OR));
		ALL_SUMMARY_MAP.put("AND", new Integer(AND));		

		ALL_SUMMARY_MAP.put("Same", new Integer(SAME));		
}
 
/**
 * Used when reading in config file to transform a summary name into an id
 * @param name
 * @return
 */	public static int getSummaryId(String name) {
		Integer id = (Integer) ALL_SUMMARY_MAP.get(name);
		if (id == null)
			return NONE;
		return id.intValue();
	}
	
	public static BidiMap getMap(Class clazz, boolean cost) {
		if (clazz == Double.class) {
			return cost ? COST_SUMMARY_MAP : NUMBER_SUMMARY_MAP;
		} else if (clazz == Date.class) {
			return DATE_SUMMARY_MAP;
		} else if (clazz == DurationFormat.class) {
			return DURATION_SUMMARY_MAP;
		} else if (clazz == String.class) {
			return TEXT_SUMMARY_MAP;
		} else if (clazz == Boolean.class) {
			return FLAG_SUMMARY_MAP;
		}
			
		return null;

	}  


}
