/*
The contents of this file are subject to the Common Public Attribution License 
Version 1.0 (the "License"); you may not use this file except in compliance with 
the License. You may obtain a copy of the License at 
http://www.projectlibre.com/license . The License is based on the Mozilla Public 
License Version 1.1 but Sections 14 and 15 have been added to cover use of 
software over a computer network and provide for limited attribution for the 
Original Developer. In addition, Exhibit A has been modified to be consistent 
with Exhibit B. 

Software distributed under the License is distributed on an "AS IS" basis, 
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the 
specific language governing rights and limitations under the License. The 
Original Code is OpenProj and ProjectLibre.
The Original Developer is the Initial Developer and is both Projity, Inc and 
ProjectLibre Inc. All portions of the code written by Projity are Copyright (c) 
2006, 2008. All Rights Reserved. All portions of the code written by ProjectLibre 
are Copyright (c) 2012. All Rights Reserved. Contributors Projity, Inc. and 
ProjectLibre, Inc.

Alternatively, the contents of this file may be used under the terms of the 
ProjectLibre End-User License Agreement (the ProjectLibre License) in which case 
the provisions of the ProjectLibre License are applicable instead of those above. 
If you wish to allow use of your version of this file only under the terms of the 
ProjectLibre License and not to allow others to use your version of this file under 
the CPAL, indicate your decision by deleting the provisions above and replace them 
with the notice and other provisions required by the Project Libre License. If you 
do not delete the provisions above, a recipient may use your version of this file 
under either the CPAL or the ProjectLibre Licenses. 


[NOTE: The text of this Exhibit A may differ slightly from the text of the notices 
in the Source Code files of the Original Code. You should use the text of this 
Exhibit A rather than the text found in the Original Code Source Code for Your 
Modifications.] 
EXHIBIT B. Attribution Information both ProjectLibre and OpenProj required

Attribution Copyright Notice: Copyright (c) 2012, ProjectLibre, Inc.
Attribution Phrase (not exceeding 10 words): ProjectLibre, the updated version of 
OpenProj
Attribution URL: http://www.projectlibre.com
Graphic Image as provided in the Covered Code as file: projectlibre-logo.png with 
alternatives listed on http://www.projectlibre.com/logo 

Display of Attribution Information is required in Larger Works which are defined in 
the CPAL as a work which combines Covered Code or portions thereof with code not 
governed by the terms of the CPAL. However, in addition to the other notice 
obligations, all copies of the Covered Code in Executable and Source Code form 
distributed must, as a form of attribution of the original author, include on each 
user interface screen the "OpenProj"  and “ProjectLibre” logos visible to all users. 
The OpenProj logo should be located horizontally aligned with the menu bar and left 
justified on the top left of the screen adjacent to the File menu. The logo must be 
at least 100 x 25 pixels. When users click on the "OpenProj" logo it must direct them 
back to http://www.projity.com. The ProjectLibre logo should be located horizontally 
aligned immediately above the OpenProj logo and left justified in alignment with the 
OpenProj logo. The logo must be at least 144 x 31 pixels. When users click on the 
"ProjectLibre" logo it must direct them back to http://www.projectlibre.com.

Attribution Copyright Notice: Copyright (c) 2006, 2008 Projity, Inc.
Attribution Phrase (not exceeding 10 words): Powered by OpenProj, an open source 
solution from Projity
Attribution URL: http://www.projity.com
Graphic Image as provided in the Covered Code as file: openproj_logo.png with 
alternatives listed on http://www.projity.com/logo 

Display of Attribution Information is required in Larger Works which are defined in 
the CPAL as a work which combines Covered Code or portions thereof with code not 
governed by the terms of the CPAL. However, in addition to the other notice 
obligations, all copies of the Covered Code in Executable and Source Code form 
distributed must, as a form of attribution of the original author, include on each 
user interface screen the "OpenProj" and “ProjectLibre” logos visible to all users. 
The OpenProj logo should be located horizontally aligned with the menu bar and left 
justified on the top left of the screen adjacent to the File menu.  The logo must be 
at least 100 x 25 pixels. When users click on the "OpenProj" logo it must direct them 
back to http://www.projity.com.
*/
package com.projectlibre.core.fields;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * @author Laurent Chretienneau
 *
 */
public class FieldUtil {
	protected static Logger log = Logger.getLogger("FieldUtil");
	public static void convertFields(HasFields hasFields, Class<?> inClass, Object inObject, String[] fieldNames, boolean from){
		log.info("convertFields hasFields="+hasFields);
		for (int i=0;i<fieldNames.length;){
			System.out.println("fieldNames: " + fieldNames[i] +" "+ fieldNames[i+1] + " " + fieldNames[i+2]);
			convertFieldSeries(hasFields, inClass, inObject, fieldNames[i++], -1, fieldNames[i++], -1, fieldNames[i++], from);
		}
	}
	
	protected static void convertFieldSeries(HasFields hasFields, Class<?> inClass, Object inObject, String fieldName1, int index1, String fieldName2, int index2, String converterName, boolean from){
		String[] elements1=fieldName1.split(":");
		if (elements1.length==3){
			String fieldNameS=elements1[0];
			int startIndex=Integer.parseInt(elements1[1]);
			int endIndex=Integer.parseInt(elements1[2]);
			for (int i=startIndex;i<endIndex;i++)
				convertFieldSeries(hasFields, inClass, inObject, fieldNameS+1, -1, fieldName2, index2, converterName, from);
			return;
		}
		String[] elements2=fieldName2.split(":");
		if (elements2.length==3){
			String fieldNameS=elements2[0];
			int startIndex=Integer.parseInt(elements2[1]);
			int endIndex=Integer.parseInt(elements2[2]);
			for (int i=startIndex;i<endIndex;i++)
				convertFieldSeries(hasFields, inClass, inObject, fieldName1, index1, fieldNameS+i, -1, converterName, from);
			return;
		}

		elements1=fieldName1.split(",");
		if (elements1.length==3){
			String fieldNameS=elements1[0];
			int startIndex=Integer.parseInt(elements1[1]);
			int endIndex=Integer.parseInt(elements1[2]);
			for (int i=startIndex;i<endIndex;i++)
				convertFieldSeries(hasFields, inClass, inObject, fieldNameS, i, fieldName2, index2, converterName, from);
			return;
		}
		elements2=fieldName2.split(",");
		if (elements2.length==3){
			String fieldNameS=elements2[0];
			int startIndex=Integer.parseInt(elements2[1]);
			int endIndex=Integer.parseInt(elements2[2]);
			for (int i=startIndex;i<endIndex;i++)
				convertFieldSeries(hasFields, inClass, inObject, fieldName1, index1, fieldNameS, i, converterName, from);
			return;
		}
		
		convertField(hasFields, inClass, inObject, fieldName1, index1, fieldName2, index2, converterName, from);
	}
	
	protected static void convertField(HasFields hasFields, Class<?> inClass, Object inObject, String fieldName1, int index1, String fieldName2, int index2, String converterName, boolean from){ 
		//index1 is ignored
		try {
			if (from) {
				//get value
				Object value;
				if (index2==-1){
					Method m=inClass.getMethod(toGetterMethodName(fieldName2), (Class<?>[])null);
					value=m.invoke(inObject, (Object[])null);
				}else{
					Method m=inClass.getMethod(toGetterMethodName(fieldName2), new Class<?>[]{int.class});
					value=m.invoke(inObject, new Object[]{index2});
				}
				
				if (value==null) return; //skip null values, it will be considered as not set
				
				//convert
				if (converterName!=null){
					if (value==null) return;
					FieldTypeConverter converter = (FieldTypeConverter)Class.forName(converterName).newInstance();
					value=converter.convert(value, from);
				}
				
				//set
				hasFields.set(fieldName1, value);
			} else {
				//get value
				Object value=hasFields.get(fieldName1);
				
				if (value==null) return; //skip null values, it will be considered as not set

				//convert
				if (converterName!=null){
					if (value==null) return;
					FieldTypeConverter converter = (FieldTypeConverter)Class.forName(converterName).newInstance();
					value=converter.convert(value, from);
				}
				
				//set
				Method method=null;
				Method methods[]=inClass.getMethods();
				String methodToFind=toSetterMethodName(fieldName2);
				for (Method m : methods){
					if (m.getName().equals(methodToFind)){ //TODO check types too
						Class<?>[] parameterTypes=m.getParameterTypes();
						if ((index2==-1&&parameterTypes.length==1) ||
								(index2!=-1&&parameterTypes.length==2&&"int".equals(parameterTypes[0].getName()))){
							method=m;
							break;

						}
					}
				}
				if (method==null){
					log.info("Method not found: "+hasFields.getClass()+" "+methodToFind);
					return;
				}
				if (index2==-1)
					value=method.invoke(inObject, new Object[]{value});
				else value=method.invoke(inObject, new Object[]{index2, value});
				

				
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String toGetterMethodName(String s){
		return "get" + s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static String toSetterMethodName(String s){
		return "set" + s.substring(0, 1).toUpperCase() + s.substring(1);
	}
	
	public static Field[] getDeclaredFields(Class<?> classe){
		Field[] fields=classe.getDeclaredFields();
		return fields;
	}
}
