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
user interface screen the "OpenProj"  and "ProjectLibre" logos visible to all users. 
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
user interface screen the "OpenProj" and "ProjectLibre" logos visible to all users. 
The OpenProj logo should be located horizontally aligned with the menu bar and left 
justified on the top left of the screen adjacent to the File menu. The logo must be 
at least 100 x 25 pixels. When users click on the "OpenProj" logo it must direct them 
back to http://www.projity.com.
*/
package com.projectlibre.core.fields;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.projectlibre.core.configuration.Configuration;
import org.projectlibre.core.dictionary.DictionaryCategory;
import org.projectlibre.core.dictionary.HasStringId;

import com.projectlibre.pm.tasks.Task;

/**
 * @author Laurent Chretienneau
 *
 */
public class FieldUtil {
	protected static Logger log = Logger.getLogger("FieldUtil");
	public static void convertFields(HasFields hasFields, Class<?> inClass, Object inObject, String[] fieldNames, boolean from){
		for (int i=0;i<fieldNames.length;){
			convertFieldSeries(hasFields, inClass, inObject, fieldNames[i++], -1, -1, fieldNames[i++], -1, -1, -1, fieldNames[i++], from);
		}
	}
	
	protected static void convertFieldSeries(HasFields hasFields, Class<?> inClass, Object inObject, String fieldName1, int startIndex1, int endIndex1, String fieldName2, int startIndex2, int endIndex2, int index, String converterName, boolean from){
//		if (fieldName2.startsWith("customCost")){
//			log.info("convertFieldSeries("+hasFields+", "+fieldName1+", "+startIndex1+", "+endIndex1+", "+fieldName2+", "+startIndex2+", "+endIndex2+", "+index+")");
//		}
		String[] elements1=fieldName1.split(":");
		if (elements1.length==3){
			fieldName1=elements1[0];
			startIndex1=Integer.parseInt(elements1[1]);
			endIndex1=Integer.parseInt(elements1[2]);
			int len=endIndex1-startIndex1+1;
			if (index==-1){
				for (int i=0;i<len;i++)
					convertFieldSeries(hasFields, inClass, inObject, fieldName1, startIndex1, endIndex1, fieldName2, startIndex2, endIndex2, i, converterName, from);
			}else convertFieldSeries(hasFields, inClass, inObject, fieldName1, startIndex1, endIndex1, fieldName2, startIndex2, endIndex2, index, converterName, from);
			return;
		}
		String[] elements2=fieldName2.split(":");
		if (elements2.length==3){
			fieldName2=elements2[0];
			startIndex2=Integer.parseInt(elements2[1]);
			endIndex2=Integer.parseInt(elements2[2]);
			int len=endIndex2-startIndex2+1;
			if (index==-1){
				for (int i=0;i<len;i++)
					convertFieldSeries(hasFields, inClass, inObject, fieldName1, startIndex1, endIndex1, fieldName2, startIndex2, endIndex2, i, converterName, from);
			}else convertFieldSeries(hasFields, inClass, inObject, fieldName1, startIndex1, endIndex1, fieldName2, startIndex2, endIndex2, index, converterName, from);
			return;
		}

		elements1=fieldName1.split(",");
		if (elements1.length==3){
			fieldName1=elements1[0];
			startIndex1=Integer.parseInt(elements1[1]);
			endIndex1=Integer.parseInt(elements1[2]);
			int len=endIndex1-startIndex1+1;
			if (index==-1){
				for (int i=0;i<len;i++)
					convertFieldSeries(hasFields, inClass, inObject, fieldName1, startIndex1, endIndex1, fieldName2, startIndex2, endIndex2, i, converterName, from);
			}else convertFieldSeries(hasFields, inClass, inObject, fieldName1, startIndex1, endIndex1, fieldName2, startIndex2, endIndex2, index, converterName, from);
			return;
		}
		elements2=fieldName2.split(",");
		if (elements2.length==3){
			fieldName2=elements2[0];
			startIndex2=Integer.parseInt(elements2[1]);
			endIndex2=Integer.parseInt(elements2[2]);
			int len=endIndex2-startIndex2+1;
			if (index==-1){
				for (int i=0;i<len;i++)
					convertFieldSeries(hasFields, inClass, inObject, fieldName1, startIndex1, endIndex1, fieldName2, startIndex2, endIndex2, i, converterName, from);
			}else convertFieldSeries(hasFields, inClass, inObject, fieldName1, startIndex1, endIndex1, fieldName2, startIndex2, endIndex2, index, converterName, from);
			return;
		}
		
		convertField(hasFields, inClass, inObject, fieldName1, index==-1? -1: index+startIndex1, fieldName2, index==-1? -1: index+startIndex2, converterName, from);
	}
	
	protected static void convertField(HasFields hasFields, Class<?> inClass, Object inObject, String fieldName1, int index1, String fieldName2, int index2, String converterName, boolean from){ 
		if (index1!=-1)
		 fieldName1+=index1;
		
		//index1 is ignored
		try {
			if (from) {
//				if (index2!=-1)
//					fieldName2+=index1;

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
				hasFields.setPropertyValue(fieldName1, value);
			} else {
				//get value
				Object value=hasFields.getPropertyValue(fieldName1);
				
				if (value==null || 
						((value instanceof Boolean) && ((Boolean)value)==false) )
					return; //skip null values, it will be considered as not set

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
				else {
					value=method.invoke(inObject, new Object[]{index2, value});
				}
				

				
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
	
	protected static String toGetterMethodName(String s){
		return "get" + s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	protected static String toSetterMethodName(String s){
		return "set" + s.substring(0, 1).toUpperCase() + s.substring(1);
	}
	
	protected static Field[] getDeclaredFields(Class<?> classe){
		Field[] fields=classe.getDeclaredFields();
		return fields;
	}
		
	
	public static com.projectlibre.core.fields.Field getField(String fieldId, String[] categories){
		com.projectlibre.core.fields.Field field=null;
		for (String category : categories){
			field=(com.projectlibre.core.fields.Field)Configuration.getInstance().getDictionary().get(
					new DictionaryCategory(com.projectlibre.core.fields.Field.class, category),
					fieldId);
			if (field!=null)
				return field;
		}
		return field;
	}
	
	public static Map<String,com.projectlibre.core.fields.Field> getFields(String[] categories){
		Map<String,com.projectlibre.core.fields.Field> map=new HashMap<String, com.projectlibre.core.fields.Field>();
		for (String category : categories){
			Map<String,HasStringId> m=Configuration.getInstance().getDictionary().get(
					new DictionaryCategory(com.projectlibre.core.fields.Field.class, category));
			if (m!=null)
				for (String key : m.keySet()){
					com.projectlibre.core.fields.Field existingField=map.get(key);
					if (existingField==null)
						map.put(key, (com.projectlibre.core.fields.Field)m.get(key));
				}
		}
		return map;
	}

	
	public static String[] getCategories(Class<?> cl){
		Set<String> categorySet=new HashSet<String>();
		List<String> categories=new LinkedList<String>();
		addCategories(cl, categories, categorySet);
		return categories.toArray(new String[categories.size()]);
		
	}
	
	private static void addCategories(Class<?> cl, List<String> categories, Set<String> categorySet){
		categorySet.add(cl.getName());
		categories.add(cl.getName());
		Class<?> superClass=cl.getSuperclass();
		Class<?>[] interfaces=cl.getInterfaces();
		if (superClass!=null &&
				!categorySet.contains(superClass.getName())){
			addCategories(superClass,categories,categorySet);

		}
		for (Class<?> i : interfaces){
			if (!categorySet.contains(i.getName())){
				addCategories(i,categories,categorySet);
			}
		}
	}
		
	

}
