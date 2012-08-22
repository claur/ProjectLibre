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
package com.projity.interval;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelDataFactory;
import com.projity.strings.Messages;
import com.projity.util.DateTime;

/**
 *
 */
public abstract class ValueObjectForIntervalTable implements NodeModelDataFactory, Serializable, Cloneable {
	static final long serialVersionUID = 7728399282882L;
	protected ArrayList valueObjects = new ArrayList();
	protected String name;
	public List getList() {
		return Collections.unmodifiableList(valueObjects);
	}
	public ArrayList getValueObjects(){ //serialization
		return valueObjects;
	}
	public ValueObjectForIntervalTable() {
		
	}
	public ValueObjectForIntervalTable(String name,ArrayList valueObjects) { //serialization
		this.name=name;
		this.valueObjects=valueObjects;
	}
	public ValueObjectForIntervalTable(String name) {
		this.name = name;
		valueObjects.add(createValueObject(ValueObjectForInterval.NA_TIME)); // put in default one
	}
	
	protected abstract ValueObjectForInterval createValueObject(long date);
	
	/**
	 * A factory method returning a new value at a given date
	 * @param start
	 * @return
	 * @throws InvalidValueObjectForIntervalException
	 */	
	public ValueObjectForInterval newValueObject(long start) throws InvalidValueObjectForIntervalException {
		ValueObjectForInterval newOne = createValueObject(start);

		int index = Collections.binarySearch(valueObjects, newOne, newOne); // find where to insert
		if (index < 0) { // if doesn't already exist
			ValueObjectForInterval previous = (ValueObjectForInterval)valueObjects.get(-index-2); // get previous element
			valueObjects.add(-index-1, newOne); // add new in place
			newOne.setEnd( previous.getEnd()); //set new one's end to prevous end
			previous.setEnd(start); // set previous end to this start
		} else { // not allowed to make duplicate, so send back error
			throw new InvalidValueObjectForIntervalException(Messages.getString("ValueObjectForIntervalTable.ThatEffectiveDateIsAlreadyInTheTable")); //$NON-NLS-1$
		}
		return newOne;
	}
	
	public long getEnd() {
		long end = 0;
		Iterator i = valueObjects.iterator();
		while (i.hasNext()) {
			end = Math.max(end,((ValueObjectForInterval)i.next()).getEnd());
		}
		return end;
	}
	
	/**
	 * Adjust the start date of a value object.  Assure that it is in valid range, and adjust previous element's end as well as this one's start
	 * @param newStart
	 * @param valueObject
	 * @throws InvalidValueObjectForIntervalException
	 */
	public void adjustStart(long newStart, ValueObjectForInterval valueObject) throws InvalidValueObjectForIntervalException  {
		int index = valueObjects.indexOf(valueObject);
		if (index == 0)
			return;
		ValueObjectForInterval previous = (ValueObjectForInterval) valueObjects.get(index -1);
		if (newStart <= previous.getStart())
			throw new InvalidValueObjectForIntervalException(Messages.getString("ValueObjectForIntervalTable.ThisDateMustBeAfter")); //$NON-NLS-1$
		if (newStart >= valueObject.getEnd()) // see if this would disappear
			throw new InvalidValueObjectForIntervalException(Messages.getString("ValueObjectForIntervalTable.ThisDateMustBeBefore")); //$NON-NLS-1$
				
		previous.setEnd(newStart);
		valueObject.setStart(newStart);
	}
	
	
	public long getStart() {
		long start = DateTime.getMaxDate().getTime();
		Iterator i = valueObjects.iterator();
		while (i.hasNext()) {
			start = Math.min(start,((ValueObjectForInterval)i.next()).getStart());
		}
		return start;
	}
		
/**
 * Remove an entry from the table
 * @param interval object
 * @throws InvalidValueObjectForIntervalException if it's the first element
 */
	public void remove(ValueObjectForInterval removeMe) throws InvalidValueObjectForIntervalException {
		if (removeMe.isFirst()) // don't allow removal of first value
			throw new InvalidValueObjectForIntervalException(Messages.getString("ValueObjectForIntervalTable.YouCannotRemoveTheFirst"));			 //$NON-NLS-1$
		int index = valueObjects.indexOf(removeMe);
		ValueObjectForInterval previous = (ValueObjectForInterval) valueObjects.get(index-1); // set previous end to this end
		previous.setEnd(removeMe.getEnd());
		valueObjects.remove(removeMe);
	}
	
	
	private int findActiveIndex(long date) {
		ValueObjectForInterval find = createValueObject(date);		
		int index = Collections.binarySearch(valueObjects, find,find); // find it
		if (index < 0) // binary search is weird.  The element before is -index - 2
			index = -index-2; // gets index of element before
		return index;
	}
	
	/**
	 * Finds the Rate/Availability which is on or before a date
	 * @param date
	 * @return
	 */
	public ValueObjectForInterval findActive(long date) {
		return (ValueObjectForInterval) valueObjects.get(findActiveIndex(date));
	}
	
	public ValueObjectForInterval findCurrent() {
		return findActive(System.currentTimeMillis());
	}
	public String getName() {
		return name;
	}

	/**
	 * Create a new entry one year later
	 */		
	public Object createUnvalidatedObject(NodeModel nodeModel, Object parent) {
		long baseDate = DateTime.midnightToday();
		ValueObjectForInterval last = (ValueObjectForInterval) valueObjects.get(valueObjects.size()-1); // get last one
		baseDate = Math.max(baseDate,last.getStart()); // latest of today or last entry
		GregorianCalendar cal = DateTime.calendarInstance();
		cal.setTimeInMillis(baseDate);
		cal.roll(GregorianCalendar.YEAR,true); // one year later than last one's start or today
		long date = cal.getTimeInMillis();
		try {
			return newValueObject(date);
		} catch (InvalidValueObjectForIntervalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); // should not ever happen
			return null;
		}
	}
	public void addUnvalidatedObject(Object object,NodeModel nodeModel, Object parent) {
		
	}
	public NodeModelDataFactory getFactoryToUseForChildOfParent(Object impl) {
		return this;
	}
	
	
	public void rollbackUnvalidated(NodeModel nodeModel, Object object) {
//		try {
			remove(object,nodeModel,false,true,true);
//		} catch (NodeException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}


	public void remove(Object toRemove, NodeModel nodeModel,boolean deep,boolean undo,boolean removeDependencies){
		try {
			remove((ValueObjectForInterval) toRemove);
		} catch (InvalidValueObjectForIntervalException e) {
			return;
//			Alert.error(e.getMessage());
//			throw new NodeException(e);
		}

	}
	public void validateObject(Object newlyCreated, NodeModel nodeModel,
		Object eventSource, Object hierarchyInfo,boolean isNew) {
	}
//	public void fireCreated(Object newlyCreated){}

	public void serialize(ObjectOutputStream s) throws IOException {
	    s.writeObject(name);
	    s.writeObject(valueObjects);
	}
	
	
	protected static ValueObjectForIntervalTable deserialize(ObjectInputStream s,ValueObjectForIntervalTable v) throws IOException, ClassNotFoundException  {
		v.name=(String)s.readObject();
		v.valueObjects=(ArrayList)s.readObject();
		return v;
	}
	
	public Object clone(){ 
		try {
			ValueObjectForIntervalTable v=(ValueObjectForIntervalTable)super.clone();
			v.name=(name==null)?null:new String(name);
			ArrayList newList=new ArrayList();
			for (Iterator i=valueObjects.iterator();i.hasNext();){
				newList.add(((ValueObjectForInterval)i.next()).clone());
			}
			v.valueObjects=newList;
			return v;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
	
	public void initAfterCloning(){
		for (Iterator i=valueObjects.iterator();i.hasNext();){
			((ValueObjectForInterval)i.next()).setTable(this);
		}
		
	}

}
