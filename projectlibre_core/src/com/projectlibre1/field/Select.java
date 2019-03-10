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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * abastract Base class for selection lists
 */
public abstract class Select implements Map {

	private String name;
	private boolean allowNull = false;
	protected boolean sortKeys = false;
	public static final String EMPTY=" ";
	public abstract boolean isStatic();
	/**
	 * @param arg0
	 * @return
	 */
	public abstract Object getValue(Object arg0) throws InvalidChoiceException;

	/**
	 * @param arg0
	 * @return
	 */
	public abstract Object getKey(Object arg0);

	public abstract Object[] getKeyArrayWithoutNull();

	public Object[] getKeyArray() {
		Object[] result = getKeyArrayWithoutNull();
		if (result == null || !allowNull)
			return result;
		// if a null element should be added, add it at front
		Object[] resultWithNull = new Object[result.length+1];
		System.arraycopy(result,0,resultWithNull,1,result.length);
		resultWithNull[0] = EMPTY;
		return resultWithNull;
	}

	public abstract List getValueListWithoutNull();
	
	public List getValueList() {
		List result = getValueListWithoutNull();
		if (result == null || !allowNull)
			return result;
		// if a null element should be added, add it at front
		List resultWithNull=new ArrayList(result.size()+1);
		resultWithNull.add(null);
		return resultWithNull;
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

	public static class InvalidChoiceException extends Exception {
		/**
		 * 
		 */
		public InvalidChoiceException() {
			super();
			// TODO Auto-generated constructor stub
		}

		/**
		 * @param arg0
		 */
		public InvalidChoiceException(String arg0) {
			super(arg0);
			// TODO Auto-generated constructor stub
		}

		/**
		 * @param arg0
		 */
		public InvalidChoiceException(Throwable arg0) {
			super(arg0);
			// TODO Auto-generated constructor stub
		}

		/**
		 * @param arg0
		 * @param arg1
		 */
		public InvalidChoiceException(String arg0, Throwable arg1) {
			super(arg0, arg1);
			// TODO Auto-generated constructor stub
		}

	}

	/* (non-Javadoc)
	 * @see java.util.Map#size()
	 */
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#values()
	 */
	public Collection values() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(Map arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.util.Map#entrySet()
	 */
	public Set entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#keySet()
	 */
	public Set keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object arg0) {
		try {
			return getValue(arg0);
		} catch (InvalidChoiceException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public Object remove(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	public Object put(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * @return Returns the allowNull.
	 */
	public boolean isAllowNull() {
		return allowNull;
	}
	/**
	 * @param allowNull The allowNull to set.
	 */
	public void setAllowNull(boolean allowNull) {
		this.allowNull = allowNull;
	}
	public static String toConfigurationXMLOptions(LinkedHashMap map, String keyPrefix) {
//		MapIterator i = map.i();
		Iterator i = map.keySet().iterator();
		StringBuffer buf = new StringBuffer();
		HashSet duplicateSet = new HashSet(); // don't allow duplicate keys
		while (i.hasNext()) {
			String key = (String) i.next();
			// notion of key and value is switched
			String value = (String)map.get(key);
			int dupCount = 2;
			String newKey = key;
			while (duplicateSet.contains(newKey)) {
				newKey = key + "-" + dupCount++;
			}
			key = newKey;
			duplicateSet.add(key);
			if (key == null || key.length() == 0)
				continue;
			if (value == null || value.length() == 0)
				continue;
			key = keyPrefix + key;
//			String key = "<html>" + keyPrefix + ": " + "<b>" + i.getValue() +"</b></html>";
			buf.append(SelectOption.toConfigurationXML(key, value));
		}
		return buf.toString();
	}
	public final boolean isSortKeys() {
		return sortKeys;
	}
	public final void setSortKeys(boolean sortKeys) {
		this.sortKeys = sortKeys;
	}
	public String documentOptions() {
		StringBuffer result = new StringBuffer();
		for (Object key : getKeyArrayWithoutNull()) {
			if (result.length() > 0)
				result.append(", ");
			result.append(get(key)).append("=").append(key);
		}
		return result.toString();
	}

} 
