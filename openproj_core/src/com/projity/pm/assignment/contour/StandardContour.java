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
package com.projity.pm.assignment.contour;

import java.util.ArrayList;

import com.projity.configuration.Configuration;
import com.projity.pm.time.ImmutableInterval;
import com.projity.pm.time.MutableInterval;

/**
 * A standard contour represents a work distribution.  There are several predined ones, which correspond to a stepping function.
 * Because the function is the same for each type, instances of this class are final static. 
 * @stereotype strategy
 */
public class StandardContour extends AbstractContour implements ContourTypes {
	private double meanUnits = 0.0;
	private int type;
	
	public static StandardContour getInstance(int type, AbstractContourBucket[] contourBuckets) {
		return new StandardContour(type, contourBuckets);
	}
	public boolean isPersonal() {return false;}	
	/**
	 * @return Returns the meanUnits.
	 */
	public double getMeanUnits() {
		return meanUnits;
	}	
	
	public String getName() {
		return Configuration.getInstance().getFieldDictionary().getFieldFromId("Field.workContour").convertIdToString(new Integer(type));
	}

	
	/* 
	 *  (non-Javadoc)
	 * @see com.projity.pm.assignment.contour.Contour#calcTotalWork(long)
	 */
	public long calcTotalWork(long assignmentDuration) {
		return (long) (meanUnits * assignmentDuration);
	}
	
    private StandardContour(int type, AbstractContourBucket[] contourBuckets) {
    	super(contourBuckets);
    	this.type = type;
    	meanUnits = calcWeightedSum();
    }
    
    private double calcWeightedSum() {
    	double sum = 0;
    	for (int i=0; i < contourBuckets.length; i++)
    		sum += contourBuckets[i].weightedSum();
    	return sum;
    }

    public static final StandardContour FLAT_CONTOUR = getInstance(ContourTypes.FLAT, new StandardContourBucket[] { // mean is 1.0
					  new StandardContourBucket(1.0, 1.0)
	});
 
    public static final StandardContour BACK_LOADED_CONTOUR = getInstance(ContourTypes.BACK_LOADED, new StandardContourBucket[] { // mean is 0.6
					  		new StandardContourBucket(0.1, 0.1), // 10% charge for first 10%
					  		new StandardContourBucket(0.15, 0.1), // 15% charge for next 10%
							new StandardContourBucket(0.25, 0.1), // 25% charge for next 10%
							new StandardContourBucket(0.5, 0.2), // 50% charge for next 20%
							new StandardContourBucket(0.75, 0.2), // 75% charge for next 20%
							new StandardContourBucket(1.0, 0.3) // 100% charge last 30%
	});

    public static final StandardContour FRONT_LOADED_CONTOUR = getInstance(ContourTypes.FRONT_LOADED, new StandardContourBucket[] { // mean is 0.6
					  		new StandardContourBucket(1.0, 0.3), // 100% charge first 30%
					  		new StandardContourBucket(0.75, 0.2), // 75% charge for next 20%
							new StandardContourBucket(0.5, 0.2), // 50% charge for next 20%
							new StandardContourBucket(0.25, 0.1), // 25% charge for next 10%
							new StandardContourBucket(0.15, 0.1), // 15% charge for next 10%
							new StandardContourBucket(0.1, 0.1) // 10% charge for last 10%					  
	});

    public static final StandardContour DOUBLE_PEAK_CONTOUR = getInstance(ContourTypes.DOUBLE_PEAK, new StandardContourBucket[] { // mean is 0.5
					  		new StandardContourBucket(0.25, 0.1), // 25% charge first 10%
					  		new StandardContourBucket(0.5, 0.1), // 50% charge for next 10%
							new StandardContourBucket(1.0, 0.1), // 100% charge for next 10%
							new StandardContourBucket(0.5, 0.1), // 50% charge for next 10%			
							new StandardContourBucket(0.25, 0.2), // 25% charge next 20%			
							new StandardContourBucket(0.5, 0.1), // 50% charge for next 10%
							new StandardContourBucket(1.0, 0.1), // 100% charge for next 10%
							new StandardContourBucket(0.5, 0.1), // 50% charge for next 10%			
							new StandardContourBucket(0.25, 0.1), // 25% charge last 10%			
	});
    
    public static final StandardContour EARLY_PEAK_CONTOUR = getInstance(ContourTypes.EARLY_PEAK, new StandardContourBucket[] { // mean is 0.5	
					  		new StandardContourBucket(0.25, 0.1), // 25% charge first 10%
					  		new StandardContourBucket(0.5, 0.1), // 50% charge for next 10%
							new StandardContourBucket(1.0, 0.2), // 100% charge for next 20%
							new StandardContourBucket(0.75, 0.1), // 75% charge for next 10%			
							new StandardContourBucket(0.5, 0.2), // 50% charge next 20%			
							new StandardContourBucket(0.25, 0.1), // 25% charge for next 10%
							new StandardContourBucket(0.15, 0.1), // 15% charge for next 10%
							new StandardContourBucket(0.1, 0.1), // 10% charge for last 10%			

	});

    public static final StandardContour LATE_PEAK_CONTOUR = getInstance(ContourTypes.LATE_PEAK, new StandardContourBucket[] { // mean is 0.5
					  		new StandardContourBucket(0.1, 0.1), // 10% charge for first 10%			
					  		new StandardContourBucket(0.15, 0.1), // 15% charge for next 10%
							new StandardContourBucket(0.25, 0.1), // 25% charge for next 10%
							new StandardContourBucket(0.5, 0.2), // 50% charge next 20%			
							new StandardContourBucket(0.75, 0.1), // 75% charge for next 10%			
							new StandardContourBucket(1.0, 0.2), // 100% charge for next 20%
							new StandardContourBucket(0.5, 0.1), // 50% charge for next 10%																		
							new StandardContourBucket(0.25, 0.1) // 25% charge last 10%
	});

    public static final StandardContour BELL_CONTOUR = getInstance(ContourTypes.BELL, new StandardContourBucket[] { // mean is 0.5	
					  		new StandardContourBucket(0.1, 0.1), // 10% charge for first 10%			
					  		new StandardContourBucket(0.2, 0.1), // 20% charge for next 10%
							new StandardContourBucket(0.4, 0.1), // 40% charge for next 10%
							new StandardContourBucket(0.8, 0.1), // 80% charge next 10%			
							new StandardContourBucket(1.0, 0.2), // 100% charge for next 20%		
							new StandardContourBucket(0.8, 0.1), // 80% charge next 10%			
							new StandardContourBucket(0.4, 0.1), // 40% charge for next 10%
							new StandardContourBucket(0.2, 0.1), // 20% charge for next 10%			
							new StandardContourBucket(0.1, 0.1) // 10% charge for last 10%									
	});

    public static final StandardContour PLATEAU_CONTOUR = getInstance(ContourTypes.PLATEAU, new StandardContourBucket[] { // mean is 0.7
					  		new StandardContourBucket(0.25, 0.1), // 25% charge for first 10%			
					  		new StandardContourBucket(0.5, 0.1), // 50% charge for next 10%
							new StandardContourBucket(0.75, 0.1), // 75% charge for next 10%
							new StandardContourBucket(1.0, 0.4), // 100% charge next 40%		
							new StandardContourBucket(0.75, 0.1), // 75% charge for next 10%				
							new StandardContourBucket(0.5, 0.1), // 50% charge for next 10%			
							new StandardContourBucket(0.25, 0.1), // 25% charge for last 10%						
	});
    

    public static StandardContour getStandardContour(int type){
    	switch (type) {
			case FLAT: return FLAT_CONTOUR;
			case BACK_LOADED: return BACK_LOADED_CONTOUR;
			case FRONT_LOADED: return FRONT_LOADED_CONTOUR;
			case DOUBLE_PEAK: return DOUBLE_PEAK_CONTOUR;
			case EARLY_PEAK: return EARLY_PEAK_CONTOUR;
			case LATE_PEAK: return LATE_PEAK_CONTOUR;
			case BELL: return BELL_CONTOUR;
			case PLATEAU: return PLATEAU_CONTOUR;
			default: return null;
		}
    }


//	public Object clone() throws CloneNotSupportedException {
//		return this; //since this is immutable, no need to clone it
//	}
	public Object clone() {
		return super.clone();
}

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}
	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.contour.AbstractContour#extend(long, long)
	 */
	public AbstractContour extend(long end, long extendDuration) {
		return this;
	}
	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.contour.AbstractContour#extendBefore(long, long)
	 */
	public AbstractContour extendBefore(long startOffset, long extendDuration) {
		return this;
	}
	
	public MutableInterval getRangeThatIntervalCanBeMoved(long start, long end) {
		return new MutableInterval(start,Long.MAX_VALUE); // by default unbounded 
	}	
}
