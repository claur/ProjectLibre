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
package com.projity.association;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.projity.field.FieldParseException;


/**
 * Container for managing lists of associated elements, such as Dependency or Assignment
 */
public class AssociationList implements List {

    protected LinkedList list;
    
    
    public AssociationList() {
    	list = new LinkedList();
    }
    
    public AssociationList(AssociationList from) {
    	this();
    	list.addAll(from.list);
    }
    public boolean add(Association association) {
    	Association found = AssociationList.findAssociation(list,association.getLeft(),association.getRight(),null);
    	if (found != null) // if already in list
    		return false;
        return list.add(association);
    }
    
    private static Object getObject(Association association, boolean leftObject) {
    	return leftObject ? association.getLeft() : association.getRight();
    }

    public Association find(boolean leftObject, Object object) {
    	Association association;
        for ( Iterator i = list.iterator(); i.hasNext();) {
        	association = (Association)i.next();
            if (getObject(association,leftObject) == object)
                return association;
        }
        return null;
    }

    public Association findLeft(Object left) {
    	return find(true,left);
    }

    public Association findRight(Object right) {
    	return find(false,right);
    }
    
    public static Association findAssociation(LinkedList findInList, Object left, Object right, Association exclude) {
    	Association association;
        for ( Iterator i = findInList.iterator(); i.hasNext();) {
        	association = (Association)i.next();
        	if (association == exclude)
        		continue;
            if (association.getLeft() == left && association.getRight() == right)
                return association;
        }
        return null;
    }
    
    public static List extractDistinct(List list, boolean leftObject) {
    	ArrayList result = new ArrayList();
    	Association association;
    	Object object;
        for ( Iterator i = list.iterator(); i.hasNext();) {
        	association = (Association)i.next();
        	object = getObject(association,leftObject);
        	if (!result.contains(object)) // if not already in list, add it
        		result.add(object);
        }
        return result;
    }
    

    
    
    protected void testValid(boolean allowDuplicate) throws InvalidAssociationException {
    	Association association;
        for ( Iterator i = list.iterator(); i.hasNext();) {
        	association = (Association)i.next();
        	association.testValid(allowDuplicate); //throws if exception
        }	
    }    
    
    public void replaceAll(Object object, boolean leftObject) {
       	Association association;
        for ( Iterator i = list.iterator(); i.hasNext();) {
        	association = (Association)i.next();
        	association.replace(object,leftObject);
        }
    }
	public AssociationList setAssociations(String associations, AssociationFormat associationFormat) throws FieldParseException {
		AssociationListFormat format = AssociationListFormat.getInstance(associationFormat);
		AssociationList result = (AssociationList)format.parseObject(associations,new ParsePosition(0));
		if (result == null) {
			System.out.println(associationFormat.getParameters().getError());
			throw new FieldParseException(associationFormat.getParameters().getError());
		}
		LinkedList oldList = list; // (LinkedList) list.clone(); // make a copy of original list since we'll be modifying real list
		LinkedList newList = result.list; 
		
		// validate each element in new list
		try {
			result.testValid(true);
		} catch (InvalidAssociationException e) {
//			newList = oldList;
			
        	System.out.println(e.getMessage());
			throw new FieldParseException(e.getMessage());				
        }	

		Association association;
		Iterator i;
		
		// check for duplicates
        for (i = newList.iterator(); i.hasNext();) {
        	association = (Association)i.next();
        	// if duplicate
        	if (AssociationList.findAssociation(newList,association.getLeft(),association.getRight(),association) != null) {
//        		newList = oldList;
        		throw new FieldParseException("Duplicate association"); //TODO better message
        	}
        }		
		
		// At this point, the newList is valid, so now merge
        
		
		// Go through old list figuring out which elements were removed and updating those that are modified.
        Association oldAssociation;
        Association newAssociation;
        LinkedList removed = new LinkedList();
        LinkedList modified = new LinkedList();
        for (i = oldList.iterator(); i.hasNext();) {
        	oldAssociation = (Association)i.next();
        	if (oldAssociation.isDefault()) // don't treat default association.  It will be removed by later code if needed
        		continue;
        	newAssociation = AssociationList.findAssociation(newList,oldAssociation.getLeft(),oldAssociation.getRight(),null);
        	if (newAssociation == null) { 
        		removed.add(oldAssociation);
        	} else {
        		if (associationFormat.getParameters().isAllowDetailsEntry()) // some fields don't allow you to enter details. In which case, ignore values
        			modified.add(oldAssociation); // for later use?
        			oldAssociation.copyPrincipalFieldsFrom(newAssociation);
        		//TODO fire update event?
        	}
        }
        
        // Remove ones that were eliminated
        for (i = removed.iterator(); i.hasNext();) {
        	((Association)i.next()).doRemoveService(this); // will remove from real list
        }
        
        

        
        // Get a list of added elements
        LinkedList added = new LinkedList();
        for (i = newList.iterator(); i.hasNext();) {
        	association = (Association)i.next();
        	if (association.isDefault()) // don't treat default association.  It will be added by later code if needed
        		continue;
        	
        	// see if new one (not in modified list)
        	if (AssociationList.findAssociation(modified,association.getLeft(),association.getRight(),null) == null) {
        		added.add(association);
        	}
        }		
        
        // Add new ones
        for (i = added.iterator(); i.hasNext();) {
        	((Association)i.next()).doAddService(this); // will remove from real list
        }
        
        // Signal update of modified ones
        for (i = modified.iterator(); i.hasNext();) {
        	((Association)i.next()).doUpdateService(this); // will send update message
        }        
        // sort the resulting list
        // TODO sort inverse lists too
        Collections.sort(list,new AssociationComparator(associationFormat.getParameters().getIdField()));
		return result;
	}
    
    
	/**
	 * @return
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * @param arg0
	 * @return
	 */
	public boolean remove(Object arg0) {
		return list.remove(arg0);
	}

	/**
	 * @param arg0
	 */
	public void addFirst(Object arg0) {
		list.addFirst(arg0);
	}

	/**
	 * @return
	 */
	public Iterator iterator() {
		return list.iterator();
	}

	/**
	 * @return Returns the list.
	 */
	public LinkedList getList() {
		return list;
	}
	/**
	 * @param arg0
	 * @param arg1
	 */
	public void add(int arg0, Object arg1) {
		list.add(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @return
	 */
	public boolean add(Object arg0) {
		return list.add(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public boolean addAll(int arg0, Collection arg1) {
		return list.addAll(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @return
	 */
	public boolean addAll(Collection arg0) {
		return list.addAll(arg0);
	}
	/**
	 * @param arg0
	 */
	public void addLast(Object arg0) {
		list.addLast(arg0);
	}
	/**
	 * 
	 */
	public void clear() {
		list.clear();
	}
	/**
	 * @param arg0
	 * @return
	 */
	public boolean contains(Object arg0) {
		return list.contains(arg0);
	}
	/**
	 * @param arg0
	 * @return
	 */
	public boolean containsAll(Collection arg0) {
		return list.containsAll(arg0);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0) {
		return list.equals(arg0);
	}
	/**
	 * @param arg0
	 * @return
	 */
	public Object get(int arg0) {
		return list.get(arg0);
	}
	/**
	 * @return
	 */
	public Object getFirst() {
		return list.getFirst();
	}
	/**
	 * @return
	 */
	public Object getLast() {
		return list.getLast();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return list.hashCode();
	}
	/**
	 * @param arg0
	 * @return
	 */
	public int indexOf(Object arg0) {
		return list.indexOf(arg0);
	}
	/**
	 * @param arg0
	 * @return
	 */
	public int lastIndexOf(Object arg0) {
		return list.lastIndexOf(arg0);
	}
	/**
	 * @return
	 */
	public ListIterator listIterator() {
		return list.listIterator();
	}
	/**
	 * @param arg0
	 * @return
	 */
	public ListIterator listIterator(int arg0) {
		return list.listIterator(arg0);
	}
	/**
	 * @param arg0
	 * @return
	 */
	public Object remove(int arg0) {
		return list.remove(arg0);
	}
	/**
	 * @param arg0
	 * @return
	 */
	public boolean removeAll(Collection arg0) {
		return list.removeAll(arg0);
	}
	/**
	 * @return
	 */
	public Object removeFirst() {
		return list.removeFirst();
	}
	/**
	 * @return
	 */
	public Object removeLast() {
		return list.removeLast();
	}
	/**
	 * @param arg0
	 * @return
	 */
	public boolean retainAll(Collection arg0) {
		return list.retainAll(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public Object set(int arg0, Object arg1) {
		return list.set(arg0, arg1);
	}
	/**
	 * @return
	 */
	public int size() {
		return list.size();
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public List subList(int arg0, int arg1) {
		return list.subList(arg0, arg1);
	}
	/**
	 * @return
	 */
	public Object[] toArray() {
		return list.toArray();
	}
	/**
	 * @param arg0
	 * @return
	 */
	public Object[] toArray(Object[] arg0) {
		return list.toArray(arg0);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return list.toString();
	}
	
	public void dump(boolean leftObject) {
	   	Association association;
        for ( Iterator i = list.iterator(); i.hasNext();) {
        	association = (Association)i.next();
            System.out.println(getObject(association,leftObject));
        }
	}
}
