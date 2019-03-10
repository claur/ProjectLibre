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
package com.projectlibre1.pm.costing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.projectlibre1.configuration.Settings;
import com.projectlibre1.datatype.Rate;
import com.projectlibre1.field.FieldContext;
import com.projectlibre1.interval.InvalidValueObjectForIntervalException;

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
	 * @see com.projectlibre1.pm.costing.Rate#getCostPerUse()
	 */
	public double getCostPerUse() {
		return getCurrent().getCostPerUse();
	}

	/* (non-Javadoc)
	 * @see com.projectlibre1.pm.costing.Rate#getOvertimeRate()
	 */
	public Rate getOvertimeRate() {
		return getCurrent().getOvertimeRate();
	}

	/* (non-Javadoc)
	 * @see com.projectlibre1.pm.costing.Rate#getStandardRate()
	 */
	public Rate getStandardRate() {
		return getCurrent().getStandardRate();
	}



	/* (non-Javadoc)
	 * @see com.projectlibre1.pm.costing.Cost#setCostPerUse(double)
	 */
	public void setCostPerUse(double costPerUse) {
		getCurrent().setCostPerUse(costPerUse);		
	}



	/* (non-Javadoc)
	 * @see com.projectlibre1.pm.costing.Cost#setOvertimeRate(double)
	 */
	public void setOvertimeRate(Rate overtimeRate) {
		getCurrent().setOvertimeRate(overtimeRate);		
	}



	/* (non-Javadoc)
	 * @see com.projectlibre1.pm.costing.Cost#setStandardRate(double)
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
	 * @see com.projectlibre1.pm.costing.Cost#fieldHideOvertimeRate(com.projectlibre1.field.FieldContext)
	 */
	public boolean fieldHideOvertimeRate(FieldContext fieldContext) {
		return getCurrent().fieldHideOvertimeRate(fieldContext);
	}


}
