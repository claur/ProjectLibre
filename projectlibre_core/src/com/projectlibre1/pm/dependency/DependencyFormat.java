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
package com.projectlibre1.pm.dependency;

import java.text.FieldPosition;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Collection;

import com.projectlibre1.association.AssociationFormat;
import com.projectlibre1.association.AssociationFormatParameters;
import com.projectlibre1.configuration.Settings;
import com.projectlibre1.datatype.Duration;
import com.projectlibre1.datatype.DurationFormat;
import com.projectlibre1.field.FieldParseException;
import com.projectlibre1.options.GeneralOption;
import com.projectlibre1.pm.task.Task;
import com.projectlibre1.strings.Messages;


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
	 * @see com.projectlibre1.association.AssociationFormat#getContainerOfLeft(java.lang.Object)
	 */
	protected Collection getContainer(boolean left) {
		return ((Task) parameters.getThisObject()).getProject().getTasks();
	}
	protected Object createNewObject(boolean left) {
		return ((Task) parameters.getThisObject()).getProject().newNormalTaskInstance(); //TODO this should not search only in current
	}


}
