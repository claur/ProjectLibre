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

import com.projity.configuration.CalculationPreference;
import com.projity.datatype.ImageLink;
import com.projity.pm.calendar.HasCalendar;
import com.projity.pm.snapshot.BaselineScheduleFields;
import com.projity.pm.snapshot.Snapshottable;
import com.projity.pm.time.HasStartAndEnd;
import com.projity.strings.Messages;
import com.projity.util.DateTime;

/**
 * Implements the earned value calculation algorithms.  Currently, standard calculations
 * are implemented, with TCPI being calculated like Project 2003 (it was different in earlier versions).
 * Other instances can be created to implement variaions, such as Primavera's
 */
public class EarnedValueCalculator {
	private static final long defaultStart = 0;
	private static final long defaultEnd = DateTime.getMaxDate().getTime();
	private static EarnedValueCalculator instance = null;
	
	private double getDivideByZeroValue() {
		return CalculationPreference.getActive().getEarnedValueDivideByZeroValue();
	}
	public double acwp(EarnedValueValues ev) {
		return ev.acwp(defaultStart,defaultEnd); 
	}
	public double bac(EarnedValueValues ev) {
		return ev.bac(defaultStart,defaultEnd); 
	}
	public double bcwp(EarnedValueValues ev) {
		return ev.acwp(defaultStart,defaultEnd); 
	}
	public double bcws(EarnedValueValues ev) {
		return ev.bcws(defaultStart,defaultEnd); 
	}
	
	public static EarnedValueCalculator getInstance() {
		if (instance == null)
			instance = new EarnedValueCalculator();
		return instance;
	}
	private EarnedValueCalculator() {}
	
	public double cv(EarnedValueValues ev, long start, long end) {
//		return ev.acwp(start,end) - ev.bcwp(start,end);
		return ev.bcwp(start,end) - ev.acwp(start,end);
	}
	
	public double cv(EarnedValueValues ev) {
		return cv(ev,defaultStart,defaultEnd); 
	}
	
	public double sv(EarnedValueValues ev, long start, long end) {
		return ev.bcwp(start,end) - ev.bcws(start,end);
	}

	public double sv(EarnedValueValues ev) {
		return sv(ev,defaultStart,defaultEnd); 
	}
	
	public double eac(EarnedValueValues ev, long start, long end) {
		double bcwp = ev.bcwp(start,end);
		if (bcwp == 0.0D) {
			if (getDivideByZeroValue() == 0)// prevent divide by 0
				return 0;
			bcwp = ev.cost(start,end); // use cost for bcwp in case no bcwp
		}
		double acwp = ev.acwp(start,end);
		if (acwp == 0) {
			if (getDivideByZeroValue() != 0)
				return ev.cost(start,end); // use cost eac
		}
		return acwp + acwp * (ev.bac(start,end) - bcwp) / bcwp;
	}

	public double eac(EarnedValueValues ev) {
		return eac(ev,defaultStart,defaultEnd); 
	}
	
	public double vac(EarnedValueValues ev, long start, long end) {
		return ev.bac(start,end) - eac(ev,start,end);
	}

	public double vac(EarnedValueValues ev) {
		return vac(ev,defaultStart,defaultEnd); 
	}

	public double cpi(EarnedValueValues ev, long start, long end) {
		double acwp = ev.acwp(start,end);
		if (acwp == 0.0D) // prevent divide by 0
			return getDivideByZeroValue();
		return ev.bcwp(start,end) / acwp;
	}

	public double cpi(EarnedValueValues ev) {
		return cpi(ev,defaultStart,defaultEnd); 
	}

	public double spi(EarnedValueValues ev, long start, long end) {
		double bcws = ev.bcws(start,end);
		if (bcws == 0.0D) // prevent divide by 0
			return getDivideByZeroValue();
		return ev.bcwp(start,end) / bcws;
	}

	public double spi(EarnedValueValues ev) {
		return spi(ev,defaultStart,defaultEnd); 
	}
	public double csi(EarnedValueValues ev, long start, long end) {
		return spi(ev,start,end) * cpi(ev,start,end);
	}
	public double csi(EarnedValueValues ev) {
		return spi(ev) * cpi(ev);
	}
	public double cvPercent(EarnedValueValues ev, long start, long end) {
		double bcwp = ev.bcwp(start,end);
		if (bcwp == 0.0D) // prevent divide by 0
			return getDivideByZeroValue();

		return (bcwp - ev.acwp(start,end)) / bcwp;
	}
		
	public double cvPercent(EarnedValueValues ev) {
		return cvPercent(ev,defaultStart,defaultEnd); 
	}
	
	public double svPercent(EarnedValueValues ev, long start, long end) {
		double bcws = ev.bcws(start,end);
		if (bcws == 0.0D) // prevent divide by 0
			return getDivideByZeroValue();
		return (ev.bcwp(start,end) - bcws) / bcws;
	}	

	public double svPercent(EarnedValueValues ev) {
		return svPercent(ev,defaultStart,defaultEnd); 
	}
	
	public double tcpi(EarnedValueValues ev, long start, long end) {
		double bac = ev.bac(start,end);
		double acwp = ev.acwp(start,end);
		if (bac == acwp) // prevent divide by 0
			return getDivideByZeroValue();
		
		return (bac - ev.bcwp(start,end)) / (bac - acwp);
	}	

	public double tcpi(EarnedValueValues ev) {
		return tcpi(ev,defaultStart,defaultEnd); 
	}

	public double bcwr(EarnedValueValues ev, long start, long end) {
		return ev.bac(start,end) - ev.bcwp(start,end);
	}

	public double bcwr(EarnedValueValues ev) {
		return bcwr(ev,defaultStart,defaultEnd); 
	}
	public long getStartOffset(EarnedValueValues ev) {
		int numBaseline = Snapshottable.BASELINE.intValue(); // TODO use EV baseline?
		if (!(ev instanceof HasStartAndEnd))
			return 0L;
		if (!(ev instanceof BaselineScheduleFields))
			return 0L;
		if (!(ev instanceof HasCalendar))
			return 0L;
		long baselineStart = ((BaselineScheduleFields)ev).getBaselineStart(numBaseline);
		if (baselineStart == 0)
			return 0L;
		long start = ((HasStartAndEnd)ev).getStart();
		return ((HasCalendar)ev).getEffectiveWorkCalendar().compare(start,baselineStart, false);
	}
	public long getFinishOffset(EarnedValueValues ev) {
		int numBaseline = Snapshottable.BASELINE.intValue(); // TODO use EV baseline?
		if (!(ev instanceof HasStartAndEnd))
			return 0L;
		if (!(ev instanceof BaselineScheduleFields))
			return 0L;
		if (!(ev instanceof HasCalendar))
			return 0L;
		long baselineFinish = ((BaselineScheduleFields)ev).getBaselineFinish(numBaseline);
		if (baselineFinish == 0)
			return 0L;
		long finish = ((HasStartAndEnd)ev).getEnd();
		return ((HasCalendar)ev).getEffectiveWorkCalendar().compare(finish,baselineFinish, false);
		
	}
	private static final String NO_BASELINE = "There is no Earned Value data"; //$NON-NLS-1$
	public ImageLink getScheduleStatusIndicator(double spi) {
		
		return ImageLink.trafficLight(spi == 0.0D ? NO_BASELINE : Messages.getString("EarnedValueCalculator.SPI") + "="+spi,spi, 1.0D, 0.9D); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public ImageLink getBudgetStatusIndicator(double cpi) {
		return ImageLink.trafficLight(cpi == 0.0D ? NO_BASELINE : Messages.getString("EarnedValueCalculator.CPI") + "="+cpi,cpi, 1.0D, 0.9D); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public ImageLink getStatusIndicator(double csi) {
		return ImageLink.trafficLight(csi == 0.0D ? NO_BASELINE : Messages.getString("EarnedValueCalculator.CSI") + "="+csi,csi, 1.0D, 0.81D); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
}
