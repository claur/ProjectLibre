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
package com.projity.field;

import java.text.MessageFormat;

import com.projity.strings.Messages;

/**
 * Stores min and max values as well as optional error message
 * You can set min, max or both
 */
public class Range {
	private static final String messageErrorRangeMinimum = Messages.getString("Message.errorRangeMinimum.mf");
	private static final String messageErrorRangeMaximum = Messages.getString("Message.errorRangeMaximum.mf");
	private static final String messagePercentErrorRangeMinimum = Messages.getString("Message.errorPercentRangeMinimum.mf");
	private static final String messagePercentErrorRangeMaximum = Messages.getString("Message.errorPercentRangeMaximum.mf");
	

	double minimum = 0.0;
	double maximum = Double.MAX_VALUE;
	double step = 0;
	String errorMessage = null;
	
		/**
	 * @return Returns the maximum.
	 */
	public double getMaximum() {
		return maximum;
	}
	/**
	 * @param maximum The maximum to set.
	 */
	public void setMaximum(double maximum) {
		this.maximum = maximum;
	}
	/**
	 * @return Returns the minimum.
	 */
	public double getMinimum() {
		return minimum;
	}
	/**
	 * @param minimum The minimum to set.
	 */
	public void setMinimum(double minimum) {
		this.minimum = minimum;
	}
	public void validate(Object objectValue, Field field) throws FieldParseException {
		if (!(objectValue instanceof Number))
			return;
		Number value = (Number)objectValue;
		String error = null;
		if (minimum > ((Number)value).doubleValue()) {
			error = field.isPercent() ? messagePercentErrorRangeMinimum : messageErrorRangeMinimum;
		} else if (maximum < ((Number)value).doubleValue()) {
			error = field.isPercent() ? messagePercentErrorRangeMaximum : messageErrorRangeMaximum;
		}
		if (error != null) {
			throw new FieldParseException(getFormattedError(value,field, error));
		}
	}

	private String getFormattedError(Number value, Field field, String defaultMessage) {
		String message = (errorMessage == null) ? defaultMessage : errorMessage;
		return MessageFormat.format( message, new Object[] { new Double(minimum), new Double(maximum), value, field.getName()});
	}	
	
	/**
	 * @return Returns the errorMessage.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	/**
	 * @param errorMessage The errorMessage to set. The message must be formated.
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	/**
	 * @return Returns the step.
	 */
	public final double getStep() {
		return step;
	}
	/**
	 * @param step The step to set.
	 */
	public final void setStep(double step) {
		this.step = step;
	}
}

