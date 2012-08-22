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
package com.projity.configuration;

import java.util.Collection;

import org.apache.commons.digester.Digester;

import com.projity.field.Field;
import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.strings.Messages;
import com.projity.timescale.TimeScaleManager;
import com.projity.util.ClassUtils;

/**
 * Main access to objects described in configuration files
 */
public class Configuration implements ProvidesDigesterEvents {
	FieldDictionary fieldDictionary = null;
	TimeScaleManager timeScales = null;
	GraphicConfiguration graphicConfiguation = null;
	ScriptConfiguration scriptConfiguration = null;
	
	private static Configuration instance = null;
	public static synchronized Configuration getInstance() {
		if (instance == null) {
			instance = new  Configuration();
			String [] files = Messages.getMetaString("ConfigurationFiles").split(";");
			for (String file : files) 
				ConfigurationReader.read(file, instance) ;
			instance.setDonePopulating(); // makes its hash table fast if using a FastHashMap
		}
		return instance;
	}
	public Configuration() {
	}
	
	public void setDonePopulating() {
		fieldDictionary.setDonePopulating(); // makes its hash table fast if using a FastHashMap
		
	}
	/**
	 * @return Returns the fieldDictionary.
	 */
	public FieldDictionary getFieldDictionary() {
		return fieldDictionary;
	}
	/**
	 * @param fieldDictionary The fieldDictionary to set.
	 */
	public void setFieldDictionary(FieldDictionary fieldDictionary) {
		this.fieldDictionary = fieldDictionary;
	}
	
	public static Field getFieldFromId(String id) {
		return getInstance().getFieldDictionary().getFieldFromId(id);
	}
	public static final Field getFieldFromShortId(String id) {
		return getFieldFromId("Field."+id);
	}
	
	public static Collection getAllFields() {
		return getInstance().getFieldDictionary().getAllFields();
	}
	/**
	 * @return Returns the timeScales.
	 */
	public TimeScaleManager getTimeScales() {
		return timeScales;
	}
	/**
	 * @param timeScales The timeScales to set.
	 */
	public void setTimeScales(TimeScaleManager timeScales) {
		this.timeScales = timeScales;
	}
	
	public void setIntConstant(String name, int value) {
		ClassUtils.setStaticField(name,value);
	}
	
	public void setStringConstant(String name, String value) {
		ClassUtils.setStaticField(name,value);
	}
	/**
	 * @return Returns the graphicConfiguation.
	 */
	public GraphicConfiguration getGraphicConfiguation() {
		return graphicConfiguation;
	}
	/**
	 * @param graphicConfiguation The graphicConfiguation to set.
	 */
	public void setGraphicConfiguation(GraphicConfiguration graphicConfiguation) {
		this.graphicConfiguation = graphicConfiguation;
	}
	
	public ScriptConfiguration getScriptConfiguration() {
		return scriptConfiguration;
	}
	public void setScriptConfiguration(ScriptConfiguration scriptConfiguration) {
		this.scriptConfiguration = scriptConfiguration;
	}
	
	private void addGlobalDigesterEvents(Digester dg){
		dg.addCallMethod("configuration/constants/int","setIntConstant", 2, new Class[] {String.class, Integer.class});
		dg.addCallParam("configuration/constants/int/name",0);
		dg.addCallParam("configuration/constants/int/value",1);

		dg.addCallMethod("configuration/constants/String","setStringConstant", 2, new Class[] {String.class, String.class});
		dg.addCallParam("configuration/constants/String/name",0);
		dg.addCallParam("configuration/constants/String/value",1);
	}
	
	public void addDigesterEvents(Digester dg) {
		addGlobalDigesterEvents(dg);
		FieldDictionary.addDigesterEvents(dg);
		//set time scale's zoom levels
		TimeScaleManager.addDigesterEvents(dg);

		ScriptConfiguration.addDigesterEvents(dg);

		//graphic config
		GraphicConfiguration.addDigesterEvents(dg);
	}

}
