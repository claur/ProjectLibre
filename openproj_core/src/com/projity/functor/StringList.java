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
package com.projity.functor;
import java.text.MessageFormat;
import java.util.Collection;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.StringValueTransformer;

import com.projity.configuration.Settings;
/**
 *
 */
public class StringList implements Closure {
	private StringBuffer buffer= new StringBuffer();
	private String separator = Settings.LIST_SEPARATOR;
	private Transformer transformer = null;
	
	private StringList() {
		this(StringValueTransformer.INSTANCE);
	}
	
	private StringList(Transformer transformer) {
		this.transformer = transformer;
	}
	
	/**
	 * @param separator The separator to set.
	 */
	public void setSeparator(String separator) {
		this.separator = separator;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.collections.Closure#execute(java.lang.Object)
	 */
	public void execute(Object object) {
		if (object != null) {
			if (buffer.length() > 0)
				buffer.append(separator);
			buffer.append(transformer.transform(object));
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return buffer.toString();
	}

	/**
	 * Utility function to dump out a collection separated by ,
	 * @param collection
	 * @return
	 */
	public static String list(Collection collection) {
		return list(collection,StringValueTransformer.INSTANCE);
	}
	public static String commaSeparatedList(Collection collection) {
		StringList l = getInstance(StringValueTransformer.INSTANCE);
		l.setSeparator(",");
		CollectionUtils.forAllDo(collection,l);
		return l.toString();
	}
	public static String brSeparatedList(Collection collection) {
		StringList l = getInstance(StringValueTransformer.INSTANCE);
		l.setSeparator("<br>");
		CollectionUtils.forAllDo(collection,l);
		return l.toString();
	}

	/** for generating prepared statements */
	public static String commaQuestionMarkString(Collection collection) {
		return StringList.list(collection,",", new Transformer(){
		public Object transform(Object arg0) {
			return "?";
		}});
	}

	public static String list(Collection collection, Transformer transformer) {
		StringList l = getInstance(transformer);
		CollectionUtils.forAllDo(collection,l);
		return l.toString();
	}	
	public static String list(Collection collection, String separator, Transformer transformer) {
		StringList l = getInstance(transformer);
		l.setSeparator(separator);
		CollectionUtils.forAllDo(collection,l);
		return l.toString();
	}	
	
	public static String listWithMaxAndMessage(Collection collection, int maxInList, String message, Transformer transformer) {
		if (collection.size() > maxInList)
			return MessageFormat.format( message, new Object[] { new Integer(collection.size())});
		return list(collection,transformer);
	}
	/**
	 * Utility function to dump out a collection on separate lines
	 * @param collection
	 * @return
	 */
	public static String rows(java.util.Collection collection) {
		StringList l = getInstance();
		l.setSeparator("\n");
		CollectionUtils.forAllDo(collection,l);
		return l.toString();
	}

	public static StringList getInstance() {
		return new StringList();
	}

	public static StringList getInstance(Transformer transformer) {
		return new StringList(transformer);
	}	

/**
 * Repates the String val a number of times. Couldn't find an SDK or commons method for this.
 * @param val
 * @param times
 * @return
 */	public static String repeat(String val, int times) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<times; i++)
			buf.append(val);
		return buf.toString();
	}
}
