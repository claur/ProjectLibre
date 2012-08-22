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

Attribution Information: Attribution Copyright Notice: Copyright � 2006, 2007 
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
package com.projity.util;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Transformer;

import com.projity.functor.StringList;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.TypedNodeIterator;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.key.HasKey;
import com.projity.pm.resource.Resource;
import com.projity.pm.task.Task;

/**
 * Utility functions for data manipulation
 */
public class DataUtils {
	
	public static Object extractObjectOfClass(Object object, Class objectClass) {
		if (object instanceof Assignment) {// if clicked on an assignment, set task
			if (objectClass == Task.class)
				object = ((Assignment)object).getTask();
			else if (objectClass == Resource.class)
				object = ((Assignment)object).getResource();
		}
		// assure type is treated by this dialog
		if (objectClass == Task.class  && !(object instanceof Task))
			return null;
		if (objectClass == Resource.class  && !(object instanceof Resource))
			return null;
		return object;
	}
	
	public static void extractObjectsOfClassFromNodeList(Collection result, Collection nodeList, Class objectClass) {
		result.clear();
		Iterator i = nodeList.iterator();
		Object nodeObject;
		
		while (i.hasNext()) {
			nodeObject = ((Node)i.next()).getImpl();
			nodeObject = DataUtils.extractObjectOfClass(nodeObject,objectClass);
			if (nodeObject != null) {
				if (!result.contains(nodeObject)) // only add if not already in there
					result.add(nodeObject);
			}
				
		}
	}
	
	public static void forAllDo(Iterator i, Closure closure) {
		while (i.hasNext())
			closure.execute(i.next());
	}

	/**
	 * Apply a closure to one of the collections: if all is true, then use the allList, otherwise
	 * use the nodeList, and extract only the impls of the type clazz (or subclasses of clazz)
	 * @param closure
	 * @param all
	 * @param allList
	 * @param nodeList
	 * @param clazz
	 */
	public static void forAllDo(Closure closure, boolean all, Collection allList, Collection nodeList, Class clazz) {
		forAllDo(closure, all, allList.iterator(), nodeList, clazz);
	}
	public static void forAllDo(Closure closure, boolean all, Iterator allIterator, Collection nodeList, Class clazz) {
		Iterator i = all ? allIterator : TypedNodeIterator.getInstance(nodeList,clazz);
		forAllDo(i,closure);
	}
	public static boolean nodeListContainsImplOfType(Collection nodeList, Class clazz) {
		if (nodeList == null)
			return false;
		Iterator i = TypedNodeIterator.getInstance(nodeList,clazz);
		return i.hasNext();
	}

	public static String stringList(Collection collection) {
		return StringList.list(collection, new Transformer() {
			public Object transform(Object arg0) {
				return ""+ ((HasKey)arg0).getId();
			}});		
	}

	public static String stringListWithMaxAndMessage(Collection collection, int maxInList, String message) {
		return StringList.listWithMaxAndMessage(collection, maxInList,message, new Transformer() {
			public Object transform(Object arg0) {
				return ""+ ((HasKey)arg0).getId();
			}});		
	}

}
