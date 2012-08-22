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
package com.projity.pm.assignment;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.projity.association.AssociationFormat;
import com.projity.association.AssociationFormatParameters;
import com.projity.configuration.Settings;
import com.projity.datatype.Rate;
import com.projity.datatype.RateFormat;
import com.projity.datatype.TimeUnit;
import com.projity.field.FieldParseException;
import com.projity.options.GeneralOption;
import com.projity.pm.task.Task;
import com.projity.pm.resource.Resource;
import com.projity.strings.Messages;


public class AssignmentFormat extends AssociationFormat {
	public static AssignmentFormat getInstance(AssociationFormatParameters parameters) {
		return new AssignmentFormat(parameters);
	}
	
	private AssignmentFormat(AssociationFormatParameters parameters) {
		super(parameters);
	}
	

	private String getErrorMessage(String text) {
		return Messages.getString("Message.invalidAssignments");
	}

	private static NumberFormat percentFormat = NumberFormat.getPercentInstance();
	
	private static String typePatternString =  	
		 	"\\s*" // optional whitespace before 
			+ "(" // group1 
				+ "[^" + Messages.getString("Symbol.leftBracketRegex") + "]+" // anything, aside from bracket
			+ ")" // end of group1	
			+ "(?:" // non grouping
				+ Messages.getString("Symbol.leftBracketRegex") // open bracket
				+ "(" // group 2
					+ "\\d+" // one or more digits
					+ ".*" // room here for option percent
				+ ")" // group 2					
				+ Messages.getString("Symbol.rightBracketRegex") // clost bracket
			+ ")?"
		+ "\\s*" // optional white space
		 ;
	
	
	private static Pattern pattern = Pattern.compile(typePatternString);
	
	private Object doParse(String string, ParsePosition pos) throws ParseException {
		Matcher matcher = pattern.matcher(string.substring(pos.getIndex()));
		if (!matcher.matches())
			throw new ParseException(getErrorMessage(string), pos.getIndex());
		
		// group 1 is resource name
		// group 2 is percent

		
		Object found = parameters.getIdField().find(matcher.group(1),getContainer(parameters.isLeftAssociation()));
 
		if (found == null) {
			if (GeneralOption.getInstance().isAutomaticallyAddNewResourcesAndTasks()) {
				found = createNewObject(parameters.isLeftAssociation());
				
				if (found == null) // if couldn't create, such as trying to create a task on resource pool
					throw new ParseException(getErrorMessage(string), pos.getIndex());	
				try {
					parameters.getIdField().setText(found,matcher.group(1),null);
				} catch (FieldParseException e) {
					throw new ParseException(e.getMessage(), 0); //TODO don't know about this - can it happen?
				}
			} else {
				throw new ParseException(getErrorMessage(string), pos.getIndex());
			}
		}
		
		double percent = 1.0D;
		Resource resource = (Resource)(parameters.isLeftAssociation() ? found : parameters.getThisObject());
		Rate rate = null;
		if (matcher.group(2) != null) { // if text was empty use default
			if (!getParameters().isAllowDetailsEntry())
				throw new ParseException(Messages.getString("Message.cannotEnterUnits"),0);
			RateFormat format = resource.getRateFormat();
			rate = (Rate) format.parseObject(matcher.group(2));
			percent = rate.getValue();
//			Number percentNumber;
//			if (resource.isLabor())
//				percentNumber = percentFormat.parse(matcher.group(2)+ Settings.PERCENT); // force a percent sign at the end for labor.  If there are two, it is ignored
//			else //TODO allow parsing values like 3/d for material resources
//				percentNumber = NumberFormat.getInstance().parse(matcher.group(2));
//			
//			if (percentNumber == null)
//				throw new ParseException(getErrorMessage(string), pos.getIndex());
//			percent = percentNumber.doubleValue();
		} else if (resource.isMaterial()) {
			rate = new Rate(1,TimeUnit.NON_TEMPORAL);
		}
		Assignment ass = Assignment.getInstance((Task) (parameters.isLeftAssociation() ? parameters.getThisObject() : found),
										resource,
									  	percent,
										0);
		if (rate != null)
			ass.detail.setRate(rate);
		return ass;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.text.Format#parseObject(java.lang.String,
	 *      java.text.ParsePosition)
	 */
	public Object parseObject(String string, ParsePosition pos) {
		try {
			return doParse(string, pos);
		} catch (ParseException e) {
			parameters.setError(e.getMessage());
			return null;
		}
	}

	/**
	 * convert to text.  The format is either, John or John[50%] 
	 */
	public StringBuffer format(Object assignmentObject,	StringBuffer string, FieldPosition fieldPos) {
		Assignment assignment = (Assignment)assignmentObject;
		Object showObject = ((parameters.isLeftAssociation()) ? (Object)assignment.getResource() : (Object)assignment.getTask());
		string.append(parameters.getIdField().getValue(showObject,null));
		if (parameters.isEncloseInBrackets()) {
			double units = assignment.getUnits();
			if (units != 1D) {
				string.append(Settings.LEFT_BRACKET);
				string.append(assignment.getRateFormat().format(assignment.getRate()));
				string.append(Settings.RIGHT_BRACKET);
			}
		}
		return string;
	}

	/* (non-Javadoc)
	 * @see com.projity.association.AssociationFormat#getContainer(boolean)
	 */
	protected Collection getContainer(boolean left) {
		if (left)
			return ((Task) parameters.getThisObject()).getProject().getResourcePool().getResourceList();
		else
			return null; // TODO if we implement projet-specific resource pools, we can handle this
	}

	protected Object createNewObject(boolean left) {
		if (left)
			return ((Task) parameters.getThisObject()).getProject().getResourcePool().newResourceInstance();
		else
			return null; // TODO if we implement projet-specific resource pools, we can handle this
	}
	
}