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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * A named item belonging to a category that represents a list of values
 */
public class NamedList implements List, NamedItem {
	private List list = new LinkedList();
	private String name;
	private String category;
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	

	
	public int hashCode() {
		return list.hashCode();
	}
	public Object set(int arg0, Object arg1) {
		return list.set(arg0, arg1);
	}
	public int lastIndexOf(Object arg0) {
		return list.lastIndexOf(arg0);
	}
	public ListIterator listIterator() {
		return list.listIterator();
	}
	public boolean add(Object arg0) {
		return list.add(arg0);
	}
	public boolean addAll(int arg0, Collection arg1) {
		return list.addAll(arg0, arg1);
	}
	public void add(int arg0, Object arg1) {
		list.add(arg0, arg1);
	}
	public String toString() {
		return list.toString();
	}
	public Object[] toArray() {
		return list.toArray();
	}
	public Object remove(int arg0) {
		return list.remove(arg0);
	}
	public boolean addAll(Collection arg0) {
		return list.addAll(arg0);
	}
	public boolean retainAll(Collection arg0) {
		return list.retainAll(arg0);
	}
	public boolean contains(Object arg0) {
		return list.contains(arg0);
	}
	public void clear() {
		list.clear();
	}
	public boolean containsAll(Collection arg0) {
		return list.containsAll(arg0);
	}
	public Object get(int arg0) {
		return list.get(arg0);
	}
	public int size() {
		return list.size();
	}
	public boolean removeAll(Collection arg0) {
		return list.removeAll(arg0);
	}
	public ListIterator listIterator(int arg0) {
		return list.listIterator(arg0);
	}
	public boolean isEmpty() {
		return list.isEmpty();
	}
	public boolean equals(Object arg0) {
		return list.equals(arg0);
	}
	public boolean remove(Object arg0) {
		return list.remove(arg0);
	}
	public Iterator iterator() {
		return list.iterator();
	}
	public List subList(int arg0, int arg1) {
		return list.subList(arg0, arg1);
	}
	public Object[] toArray(Object[] arg0) {
		return list.toArray(arg0);
	}
	public int indexOf(Object arg0) {
		return list.indexOf(arg0);
	}
}
