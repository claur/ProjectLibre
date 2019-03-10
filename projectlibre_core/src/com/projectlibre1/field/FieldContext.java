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
package com.projectlibre1.field;

import com.projectlibre1.pm.time.HasStartAndEnd;
import com.projectlibre1.util.DateTime;
/**
 * This class holds context specific information necessary for interacting with field data.
 */
public class FieldContext implements HasStartAndEnd {
	public static final long defaultStart = 0;
	public static final long defaultEnd = DateTime.getMaxDate().getTime();
	
	private boolean parseOnly;
	private boolean noUpdate;
	private boolean noDirty = false;
	private HasStartAndEnd interval = null;
	private boolean leftAssociation = true;
	private boolean round=false; //for start date when pasting from a string clipboard
	private boolean scripting = false;
	private boolean compact = false;
	private boolean forceValue = false;
	
	private static FieldContext noUpdateInstance = null;
	private static FieldContext scriptingInstance = null;
	
	public static FieldContext DEFAULT_CONTEXT= new FieldContext();
	public static FieldContext getNoUpdateInstance() {
		if (noUpdateInstance == null) {
			noUpdateInstance = new FieldContext();
			noUpdateInstance.setNoUpdate(true);
		}
		return noUpdateInstance;	
	}
	public static FieldContext getScriptingInstance() {
		if (scriptingInstance == null) {
			scriptingInstance = new FieldContext();
			scriptingInstance.setScripting(true);
		}
		return scriptingInstance;	
	}
	private static FieldContext noDirtyInstance = null;
	
	public static FieldContext getNoDirtyInstance() {
		if (noDirtyInstance == null) {
			noDirtyInstance = new FieldContext();
			noDirtyInstance.setNoDirty(true);
		}
		return noDirtyInstance;	
	}

	/**
	 * @return Returns parseOnly flag which indicates that the text should be parsed, errors thrown if necessary, but never set values
	 */
	public boolean isParseOnly() {
		return parseOnly;
	}
	/**
	 * @param parseOnly The parseOnly to set.
	 */
	public void setParseOnly(boolean parseOnly) {
		this.parseOnly = parseOnly;
	}
	/**
	 * @return Returns noUpdate flag which indicates that the field should be set but no update message sent
	 */
	public boolean isNoUpdate() {
		return noUpdate;
	}
	/**
	 * @param noUpdate The noUpdate to set.
	 */
	public void setNoUpdate(boolean noUpdate) {
		this.noUpdate = noUpdate;
	}
	
	/**
	 * @return Returns the interval.
	 */
	public HasStartAndEnd getInterval() {
		return interval;
	}
	/**
	 * @param interval The interval to set.
	 */
	public void setInterval(HasStartAndEnd interval) {
		this.interval = interval;
	}
	
	public static boolean hasInterval(FieldContext context) {
		if (context == null)
			return false;
		if (context.getInterval() ==  null)
			return false;
		return true;
	}
	
	
	public static boolean isParseOnly(FieldContext context) {
		if (context == null)
			return false;
		return context.isParseOnly();
	}

	public static boolean isNoUpdate(FieldContext context) {
		if (context == null)
			return false;
		return context.isNoUpdate();
	}
	
	public static boolean isScripting(FieldContext context) {
		if (context == null)
			return false;
		return context.isScripting();
	}
	public static boolean isForceValue(FieldContext context) {
		if (context == null)
			return false;
		return context.isForceValue();
	}	
	/**
	 * @return
	 */
	public long getEnd() {
		if (interval == null)
			return defaultEnd;
		return interval.getEnd();
	}
	/**
	 * @return
	 */
	public long getStart() {
		if (interval == null)
			return defaultStart;
		return interval.getStart();
	}
	
	public static long start(FieldContext context) {
		if (context == null)
			return defaultStart;
		return context.getStart();
	}
	public static long end(FieldContext context) {
		if (context == null)
			return defaultEnd;
		return context.getEnd();
	}
	
	public static boolean isScalar(long start, long end) { // see if range is all time
		return start == defaultStart && end == defaultEnd;
	}
	/**
	 * @return Returns the leftAssociation.
	 */
	public boolean isLeftAssociation() {
		return leftAssociation;
	}
	/**
	 * @param leftAssociation The leftAssociation to set.
	 */
	public void setLeftAssociation(boolean leftAssociation) {
		this.leftAssociation = leftAssociation;
	}

	public final boolean isNoDirty() {
		return noDirty;
	}

	public final void setNoDirty(boolean noDirty) {
		this.noDirty = noDirty;
	}

	public boolean isRound() {
		return round;
	}

	public void setRound(boolean round) {
		this.round = round;
	}
	public boolean isScripting() {
		return scripting;
	}
	public void setScripting(boolean scripting) {
		this.scripting = scripting;
	}
	public boolean isCompact() {
		return compact;
	}
	public void setCompact(boolean compact) {
		this.compact = compact;
	}
	public boolean isForceValue() {
		return forceValue;
	}
	public void setForceValue(boolean forceValue) {
		this.forceValue = forceValue;
	}
	
	
}
