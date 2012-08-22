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
package com.projity.datatype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import com.projity.strings.Messages;

/**
 * 
 */
public class Money extends BigDecimal {
	private static final long serialVersionUID = -8182666966278921881L;
	private static NumberFormat moneyFormat = null;
	private static DecimalFormat moneyCompactFormat = null;
	
	public static NumberFormat getMoneyFormatInstance() {
		if (moneyFormat == null) {
			moneyFormat = NumberFormat.getCurrencyInstance();
			moneyFormat.setGroupingUsed(false);
		}
		return moneyFormat;
	}
	public static NumberFormat getMoneyCompactFormatInstance() {
		if (moneyCompactFormat == null) {
			moneyCompactFormat = (DecimalFormat)NumberFormat.getCurrencyInstance();
			moneyCompactFormat.setGroupingUsed(false);
			moneyCompactFormat.setMaximumFractionDigits(0);
		}
		return moneyCompactFormat;
	}
	
	public static NumberFormat getFormat(boolean compact) {
		return compact ? getMoneyFormatInstance() : getMoneyCompactFormatInstance();
	}
	
	public static Money getInstance(double arg0) {
		return new Money(arg0);
	}

	/**
	 * @param arg0
	 */
	private Money(double arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public Money(String arg0) throws NumberFormatException{
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public Money(BigInteger arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public Money(BigInteger arg0, int arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}
	
	public double getPrimitiveTypeValue()
	{
		return doubleValue();
	}
	
	public static String formatCurrency(double value,boolean compact){
		if (compact){
			if (value<100) return normalCurrencyFormat(value,Math.floor(value)==value);
			else if (value<10000){ 
				value=Math.floor(value);
				return normalCurrencyFormat(value,true);
			}else if (value<100000){
				value=value/1000;
				return normalCurrencyFormat(value,Math.floor(value)==value)+Messages.getString("Text.thousandsAbbreviation"); //$NON-NLS-1$
			}else if (value<1000000){
				value=value/1000;
				return normalCurrencyFormat(value,true)+Messages.getString("Text.thousandsAbbreviation"); //$NON-NLS-1$
			}else if (value<100000000){
				value=value/1000000;
				return normalCurrencyFormat(value,Math.floor(value)==value)+Messages.getString("Text.millionsAbbreviation"); //$NON-NLS-1$
			}else{
				value=value/1000000;
				return normalCurrencyFormat(value,true)+Messages.getString("Text.millionsAbbreviation"); //$NON-NLS-1$
			}
		}else return normalCurrencyFormat(value, false);
	}
	
	public static String normalCurrencyFormat(double value,boolean compact){
		return compact?Money.getMoneyCompactFormatInstance().format(value):Money.getMoneyFormatInstance().format(value);
	}
}
