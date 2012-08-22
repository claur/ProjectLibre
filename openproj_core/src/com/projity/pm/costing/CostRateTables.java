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
package com.projity.pm.costing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.projity.configuration.Settings;
import com.projity.datatype.Rate;
import com.projity.field.FieldContext;
import com.projity.interval.InvalidValueObjectForIntervalException;

/**
 * 
 */
public class CostRateTables implements Cost, Serializable, Cloneable {
	public static final int DEFAULT = 0;
	protected CostRateTable[] costRateTableArray;
	String[] names = null;
	
	String getName(int index) {
		if (names == null) {
			names = Settings.COST_RATE_NAMES.split(";");
		}
		return names[index];
	}
	public CostRateTable getCostRateTable(int index) {
		if (costRateTableArray[index] == null)
			costRateTableArray[index] = new CostRateTable(getName(index));
		return costRateTableArray[index];
	}
	
	public Object clone(){ 
		try {
			CostRateTables c=(CostRateTables)super.clone();
			if (names!=null) c.names=new String[names.length];
			else for (int i=0;i<names.length;i++){
				c.names[i]=(names[i]==null)?null:new String(names[i]);
			}
			if (costRateTableArray!=null){
				c.costRateTableArray=new CostRateTable[costRateTableArray.length];
				for (int i=0;i<costRateTableArray.length;i++){
					c.costRateTableArray[i]=(costRateTableArray[i]==null)?null:(CostRateTable)costRateTableArray[i].clone();
					if (c.costRateTableArray[i]!=null) c.costRateTableArray[i].initAfterCloning();
				}
			}
			return c;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	
	public void setCostRateTable(int index, CostRateTable t) {
		costRateTableArray[index] = t;
	}
	
	private CostRate getCurrent() {
		return (CostRate)costRateTableArray[DEFAULT].findCurrent();
	}
	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Rate#getCostPerUse()
	 */
	public double getCostPerUse() {
		return getCurrent().getCostPerUse();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Rate#getOvertimeRate()
	 */
	public Rate getOvertimeRate() {
		return getCurrent().getOvertimeRate();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Rate#getStandardRate()
	 */
	public Rate getStandardRate() {
		return getCurrent().getStandardRate();
	}



	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#setCostPerUse(double)
	 */
	public void setCostPerUse(double costPerUse) {
		getCurrent().setCostPerUse(costPerUse);		
	}



	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#setOvertimeRate(double)
	 */
	public void setOvertimeRate(Rate overtimeRate) {
		getCurrent().setOvertimeRate(overtimeRate);		
	}



	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#setStandardRate(double)
	 */
	public void setStandardRate(Rate standardRate) {
		getCurrent().setStandardRate(standardRate);
	}
	
	
	/**
	 * 
	 * 
	 */
	public CostRateTables() {
		super();
		costRateTableArray = new CostRateTable[Settings.NUM_COST_RATES]; // initialize array
		costRateTableArray[DEFAULT] = new CostRateTable(getName(DEFAULT)); //add default element
//		java.util.GregorianCalendar start1 = new java.util.GregorianCalendar(2003,java.util.GregorianCalendar.JANUARY,4,0,0);
//		java.util.GregorianCalendar start2 = new java.util.GregorianCalendar(2005,java.util.GregorianCalendar.JANUARY,7,0,0);	
//		try {
//			CostRate test;
//			test = costRateTableArray[DEFAULT].newRate(start1.getTimeInMillis());
//			test.setStandardRate(100.0/(1000*60*60*8));
//			test.setOvertimeRate(110.0/(1000*60*60*8));
//			test.setCostPerUse(450);
//			test = costRateTableArray[DEFAULT].newRate(start2.getTimeInMillis());
//			test.setStandardRate(13);
//			test.setOvertimeRate(1300);
//		} catch (InvalidCostRateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}
	public long getEffectiveDate() {
		return getCurrent().getEffectiveDate();
	}
	public void setEffectiveDate(long effectiveDate) throws InvalidValueObjectForIntervalException {
		getCurrent().setEffectiveDate(effectiveDate);
	}
	public boolean isReadOnlyEffectiveDate(FieldContext fieldContext) {
		return getCurrent().isReadOnlyEffectiveDate(fieldContext);
	}
	
	public void serialize(ObjectOutputStream s) throws IOException {
	    s.writeObject(names);
	    
	    ArrayList[] costRates=new ArrayList[costRateTableArray.length];
	    for (int i=0;i<costRates.length;i++){
	    	costRates[i]=(costRateTableArray[i]==null)?null:costRateTableArray[i].getValueObjects();
	    }
	    s.writeObject(costRates);
	}
	
	public static CostRateTables deserialize(ObjectInputStream s) throws IOException, ClassNotFoundException  {
		CostRateTables t=new CostRateTables();
		t.names=(String[])s.readObject();
		ArrayList[] costRates=(ArrayList[])s.readObject();
		t.costRateTableArray=new CostRateTable[costRates.length];
	    for (int i=0;i<costRates.length;i++){
	    	t.costRateTableArray[i]=(costRates[i]==null)?null:new CostRateTable(t.names[i],costRates[i]);
	    }
		return t;
	}
	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#fieldHideOvertimeRate(com.projity.field.FieldContext)
	 */
	public boolean fieldHideOvertimeRate(FieldContext fieldContext) {
		return getCurrent().fieldHideOvertimeRate(fieldContext);
	}


}
