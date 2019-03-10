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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.projectlibre1.util.ClassUtils;

/**
 * This class represents a set of choices where the list of choices and the finder are specified via reflection.
 * It is used by Fields
 */
public class DynamicSelect extends Select implements Finder {
	/**
	 * 
	 */
	public DynamicSelect() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Object[] getKeyArrayWithoutNull() {
		try {
			return (Object[]) listMethod.invoke(null, null);
		} catch (Exception e) {
			Field.log.error("error calling keyArrayFromMethod for:" + listMethod);
			return null;
		}
	}
	
	public List getValueListWithoutNull(){
		throw new RuntimeException ("Not implemented");
	}
	
	private Method listMethod = null;
	private Method finderMethod = null;
	public void setList(String methodName) {
		listMethod = ClassUtils.staticVoidMethodFromFullName(methodName);
		if (listMethod == null)
			Field.log.error("invalid method in select:" + methodName);
	}
	public void setFinder(String finderName) {
		finderMethod = ClassUtils.staticMethodFromFullName(finderName, new Class[] {String.class});
		if (finderMethod == null)
			Field.log.error("invalid method in select:" + finderName);
	}

	/* (non-Javadoc)
	 * @see com.projectlibre1.field.Select#get(java.lang.Object)
	 */
	public Object getValue(Object arg0) throws InvalidChoiceException  {
		if (arg0 == null)
			return null;
		String name = arg0.toString();
		if (StringUtils.isEmpty(name))
			return null;
		Object result = find(name,null);
		if (result == null && (!isAllowNull() || name != EMPTY))
			throw new InvalidChoiceException(ObjectUtils.toString(name));
		return result;
	}

	/* (non-Javadoc)
	 * @see com.projectlibre1.field.Select#getKey(java.lang.Object)
	 */
	public Object getKey(Object arg0) {
		return ObjectUtils.toString(arg0);
	}

	/* (non-Javadoc)
	 * @see com.projectlibre1.field.Finder#find(java.lang.Object)
	 */
	public Object find(Object key, Collection container) {
		String name = (String)key;
		try {
			return finderMethod.invoke(null, new Object[] {name});
		} catch (Exception e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.projectlibre1.field.Select#isStatic()
	 */
	public boolean isStatic() {
		return false;
	}

}
