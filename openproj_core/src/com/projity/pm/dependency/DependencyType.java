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
import java.text.ParsePosition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.projity.configuration.Configuration;
import com.projity.field.Field;
import com.projity.strings.Messages;



/**
 * @stereotype enumeration 
 */
public class DependencyType {
    public static final int FF = 0;
    public static final int FS = 1;
    public static final int SF = 2;
    public static final int SS = 3;
    
    public static final Integer defaultValue = new Integer(FS);
    private static Field dependencyFieldInstance = null;
    private static Field getDependencyField() {
    	if (dependencyFieldInstance == null)
    		dependencyFieldInstance = Configuration.getFieldFromId("Field.dependencyType");
    	return dependencyFieldInstance;
    }
    
	public static Integer mapStringToValue(String textValue) {
		Integer i = (Integer) getDependencyField().mapStringToValue(textValue);
		if (i == null)
			i = (Integer) getDependencyField().mapStringToValue(textValue.toUpperCase());
		return i;
	}

	public static String mapValueToString(Integer value) {
		return  getDependencyField().mapValueToString(value);
	}
	
	public static boolean isDefault(int value) {
		return value == defaultValue.intValue();
	}
	
	//any better way?
	public static String toLongString(int type){
	    switch (type) {
        case FF:
            return Messages.getString("DependencyType.longFF");
        case SF:
            return Messages.getString("DependencyType.longSF");
        case FS:
            return Messages.getString("DependencyType.longFS");
        case SS:
            return Messages.getString("DependencyType.longSS");
        default:
            return null;
        }
	}
	
	/**
	 * We will capture into two groups: group 2 is the text of the dependency.  group 1 is the dependency  with optional white space
	 * We will use group1 to determine the length of the parsed text.
	 */
	private static String typePatternString =  	
         "(" // group1
		 	+ "\\s*" // optional whitespace before 
			+ "(" // group2 
				+ Messages.getString("DependencyType.SS") 
				 + "|" + Messages.getString("DependencyType.SF")
				 + "|" + Messages.getString("DependencyType.FS")
				 + "|" + Messages.getString("DependencyType.FF")
				+  "|" + Messages.getString("DependencyType.SS").toLowerCase() 
				 + "|" + Messages.getString("DependencyType.SF").toLowerCase()
				 + "|" + Messages.getString("DependencyType.FS").toLowerCase()
				 + "|" + Messages.getString("DependencyType.FF").toLowerCase()
  		    + ")?"  // End group 2: SS,SF,FS,FF or nothing
			+ "\\s*" // optional white space
		 + ")" // end group 1
		 + ".*" // anything else
		 ;
	
	
	private static Pattern pattern = Pattern.compile(typePatternString);
	
	private static Format formatInstance = null;
	public static class Format extends java.text.Format {
		
		public static Format getInstance() {
			if (formatInstance == null)
				formatInstance = new Format();
			return formatInstance;
		}
		private Format() {
		}
		public Object parseObject(String string, ParsePosition pos) {
			int index = pos.getIndex();
			Matcher matcher = pattern.matcher(string.substring(index));
			if (!matcher.matches()) {
				return null;
			}
			if (matcher.group(2) == null) {// if text was empty use default
				return defaultValue;
			}
			pos.setIndex(pos.getIndex() + matcher.group(1).length());
			return DependencyType.mapStringToValue(matcher.group(2));
		}

		public StringBuffer format(Object type, StringBuffer toAppendTo, FieldPosition pos) {
			String typeName = mapValueToString((Integer)type);
			toAppendTo.append(typeName);
			return toAppendTo;
		}
		
	}	
}
