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
package com.projity.pm.dependency;

import java.text.FieldPosition;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Collection;

import com.projity.association.AssociationFormat;
import com.projity.association.AssociationFormatParameters;
import com.projity.configuration.Settings;
import com.projity.datatype.Duration;
import com.projity.datatype.DurationFormat;
import com.projity.field.FieldParseException;
import com.projity.options.GeneralOption;
import com.projity.pm.task.Task;
import com.projity.strings.Messages;


public class DependencyFormat extends AssociationFormat {
	public static DependencyFormat getInstance(AssociationFormatParameters parameters) {
		return new DependencyFormat(parameters);
	}
	
	private DependencyFormat(AssociationFormatParameters parameters) {
		super(parameters);
	}
	
	
	private String getErrorMessage(String text) {
		String errorMessage = MessageFormat.format(Messages.getString("Message.invalidDependency.mf"),
			new Object[] {parameters.isLeftAssociation() ?
					Messages.getString("Text.predecessor") : 
					Messages.getString("Text.successor")});
		return errorMessage;
	}
	private static NumberFormat integerFormat = NumberFormat
	.getIntegerInstance();

	private Object doParse(String string, ParsePosition pos) throws ParseException {
		Long number = (Long) integerFormat.parseObject(string, pos);
		if (number == null)
			throw new ParseException(getErrorMessage(string), pos.getIndex());
		
		Object found = null;
		Collection container = getContainer(parameters.isLeftAssociation());
		if (container != null)
			found = parameters.getIdField().find(number,container);

		if (found == null) { //TODO this should probably be moved to finder

			if (GeneralOption.getInstance().isAutomaticallyAddNewResourcesAndTasks()) {
				found = createNewObject(parameters.isLeftAssociation());
				try {
					parameters.getIdField().setText(found,number.toString(),null);
				} catch (FieldParseException e) {
					throw new ParseException(e.getMessage(), 0); //TODO don't know about this - can it happen?
				}
			} else {
				throw new ParseException(getErrorMessage(string), pos.getIndex());
			}
		}
		Integer type = (Integer) DependencyType.Format.getInstance().parseObject(string, pos);
		
		if (type == null)
			throw new ParseException(getErrorMessage(string), pos.getIndex());

		Duration duration;
		String durationPart = string.substring(pos.getIndex()).trim();

		if (durationPart.length() == 0) { // if a duration was entered, use it, otherwise 0
			duration = Duration.ZERO;
		} else {
			duration = (Duration) DurationFormat.getInstance().parseObject(string, pos);
			if (duration == null)
				throw new ParseException(getErrorMessage(string), pos.getIndex());
		}
		return Dependency.getInstance(	parameters.isLeftAssociation() ? (HasDependencies)found : (HasDependencies)parameters.getThisObject(),
										parameters.isLeftAssociation() ? (HasDependencies)parameters.getThisObject() : (HasDependencies)found,
									  	type.intValue(),
										duration.getEncodedMillis());
		
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
	 * convert to text.  The format is either, 123, 123FF, or 123FS-1d 
	 */
	public StringBuffer format(Object dependencyObject,	StringBuffer string, FieldPosition fieldPos) {
		Dependency dependency = (Dependency)dependencyObject;
		Task task = (Task) ((parameters.isLeftAssociation()) ? dependency.getPredecessor() : dependency.getSuccessor());
		string.append(parameters.getIdField().getValue(task,null));
		boolean hasLag = !Duration.isZero(dependency.getLag());

		StringBuffer details = new StringBuffer();
		if (!DependencyType.isDefault(dependency.getDependencyType()) || hasLag)
			details.append(DependencyType.mapValueToString( new Integer(dependency.getDependencyType())));

		Duration duration = new Duration(dependency.getLag()); // use duration format to format duration
		if (hasLag) {
			details.append(DurationFormat.getSignedInstance().format(duration));
		}
		if (details.length() != 0) {
			if (parameters.isEncloseInBrackets())
				string.append(Settings.LEFT_BRACKET);
			string.append(details);
			if (parameters.isEncloseInBrackets())
				string.append(Settings.RIGHT_BRACKET);
		}
		return string;
	}

	/* (non-Javadoc)
	 * @see com.projity.association.AssociationFormat#getContainerOfLeft(java.lang.Object)
	 */
	protected Collection getContainer(boolean left) {
		return ((Task) parameters.getThisObject()).getProject().getTasks();
	}
	protected Object createNewObject(boolean left) {
		return ((Task) parameters.getThisObject()).getProject().newNormalTaskInstance(); //TODO this should not search only in current
	}


}