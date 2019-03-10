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
package com.projectlibre1.graphic.configuration;

import org.apache.commons.collections.Predicate;

import com.projectlibre1.configuration.Dictionary;
import com.projectlibre1.field.InvalidFormulaException;
import com.projectlibre1.scripting.Formula;
import com.projectlibre1.scripting.FormulaFactory;
import com.projectlibre1.strings.Messages;

/**
 *
 */
public class BarStyle implements Predicate {
//	static Log log = LogFactory.getLog(BarStyle.class);
	public static final String FORMULA_PREFIX = "BarStyle";
	String name = null;
	String id = null;
	String formulaText = null;
	String formulaClass = null;
	String barFormatName = null;
	String type = null; // type is actually only used to construct formula
	String formatId = null;
	boolean link=false;
	boolean annotation=false;
	boolean calendar=false;
	boolean horizontalGrid = false;
	
	public boolean isHorizontalGrid() {
		return horizontalGrid;
	}

	public void setHorizontalGrid(boolean horizontalGrid) {
		this.horizontalGrid = horizontalGrid;
	}

	private BarFormat barFormat = null;
	private Formula formula = null;
	BarStyles belongsTo;

	boolean active = true;
	public BarStyle() {}
	
	public boolean evaluate(Object object) {
		if (!active)
			return false;
		try {
			if (formula == null)
				return true;
			return ((Boolean) formula.evaluate(object)).booleanValue();
		} catch (InvalidFormulaException e) {

//			log.warn("Error evaluating formula in BarMappingRow" + name);
			return false;
		}
	}

	/**
	 * @return Returns the barFormat.
	 */
	public String getBarFormatName() {
		return barFormatName;
	}
	/**
	 * @param barFormat The barFormat to set.
	 */
	public void setFormatId(String formatId) {
		this.formatId = formatId;
		String name = Messages.getString(formatId);
		barFormat = (BarFormat) Dictionary.get(BarFormat.category,name);
	}
	/**
	 * @return Returns the formula.
	 */
	public String getFormulaText() {
		return formulaText;
	}
	
	void build() {
		if (formulaText == null&&formulaClass == null)
			formulaClass="com.projectlibre1.scripting.formulas.TrueFormula";
		if (formulaClass!=null)
			formula=FormulaFactory.addNormal(formulaClass,name);
		else if (formulaText!=null)
			formula = FormulaFactory.addScripted(FORMULA_PREFIX + belongsTo.getName(),name,type,formulaText);
		String idName = Messages.getString(formatId);
		barFormat = (BarFormat) Dictionary.get(BarFormat.category,idName);
	}
	/**
	 * @param formula The formula to set.
	 */
	public void setFormulaText(String formulaText) {
		this.formulaText = formulaText;
	}
	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		this.id = id;
		setName(Messages.getString(id));
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return Returns the barFormat.
	 */
	public BarFormat getBarFormat() {
		return barFormat;
	}
	/**
	 * @return Returns the active.
	 */
	public boolean isActive() {
		return active;
	}
	/**
	 * @param active The active to set.
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
	
	public boolean isLink() {
		return link;
	}
	public void setLink(boolean link) {
		this.link = link;
	}

	public boolean isAnnotation() {
		return annotation;
	}

	public void setAnnotation(boolean annotation) {
		this.annotation = annotation;
	}

	public boolean isCalendar() {
		return calendar;
	}

	public void setCalendar(boolean calendar) {
		this.calendar = calendar;
	}

	public void setBelongsTo(BarStyles styles) {
		this.belongsTo = styles;
	}

	public String getFormulaClass() {
		return formulaClass;
	}

	public void setFormulaClass(String formulaClass) {
		this.formulaClass = formulaClass;
	}
	
	
	
}
