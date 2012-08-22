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
package com.projity.graphic.configuration;

import groovy.lang.GroovyClassLoader;

import com.projity.field.InvalidFormulaException;
import com.projity.strings.Messages;

/**
 *
 */
public class CellStyleFactory {
	protected String formulaText;
	protected String formulaClass;
 	protected String id = null;
 	protected String name = null;
	protected String type = null; 
	protected String format = null; 
	
//	static Log log = LogFactory.getLog(CellStyleFactory.class);
	private static int count=0;
	
	
	
	public CellStyle getCellStyle() throws InvalidFormulaException{
		if (formulaClass!=null){
			try {
				return (CellStyle)Class.forName(formulaClass).newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}else if (formulaText!=null){
		    StringBuffer classText=new StringBuffer();
		    classText.append("package com.projity.graphic.configuration;\n");
		    classText.append("import com.projity.pm.graphic.model.cache.GraphicNode;\n");
		    classText.append("public class CellStyle").append(count++).append(" implements CellStyle{\n");
		    classText.append("\tpublic CellFormat getCellFormat(Object _nodeObject){\n\t\tGraphicNode ")
		    	.append(type).append("=(GraphicNode)_nodeObject;\n\t\tCellFormat ")
		    	.append(format).append("=new CellFormat();\n")
		    	.append(formulaText).
		    	append("\n\t\treturn ").append(format).append(";\n\t}\n");
		    classText.append("}\n");
	//		GroovyClassLoader loader = new GroovyClassLoader(ClassLoaderUtils.getLocalClassLoader());
			GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader());
	//		GroovyClassLoader loader = new GroovyClassLoader(new TracingClassLoader(getClass().getClassLoader()));
			try {
				Class groovyClass = loader.parseClass(classText.toString());
				CellStyle style= (CellStyle)groovyClass.newInstance();
				return style;
			} catch (Exception e) {
				throw new InvalidFormulaException(e);
			}
		}else return null;
	}
	
    public String getFormulaText() {
        return formulaText;
    }
    public void setFormulaText(String formulaText) {
        this.formulaText = formulaText;
    }
	
     public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
		setName(Messages.getString(id));
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getFormat() {
        return format;
    }
    public void setFormat(String format) {
        this.format = format;
    }

	public String getFormulaClass() {
		return formulaClass;
	}

	public void setFormulaClass(String formulaClass) {
		this.formulaClass = formulaClass;
	}
}
