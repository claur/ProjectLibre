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
package com.projectlibre1.document;

import java.util.EventListener;
import java.util.EventObject;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.projectlibre1.field.Field;
import com.projectlibre1.undo.NodeUndoInfo;

/**
 *
 */
public class ObjectEvent extends EventObject {
    
	public Object getObject() {
		return object;
	}

	public boolean isUpdate() {
		return (eventType == UPDATE);
	}
	
	public boolean isDelete() {
		return (eventType == DELETE);
	}
	public boolean isCreate() {
		return (eventType == CREATE);
	}
	public int getType(){
		return eventType;
	}
	
    public NodeUndoInfo getInfo() {
        return info;
    }
	
	/**
	 * @param object The object to set.
	 */
	public void setObject(Object object) {
		this.object = object;
	}

    private static Object NULL_SOURCE = new Object();
	public static final int CREATE = 1;
	public static final int UPDATE = 0;
	public static final int DELETE = -1;
	
	private Object object;
	private int eventType;
	private NodeUndoInfo info;
	private Field field = null;
	
	public static ObjectEvent getInstance(Object source) {
		ObjectEvent objectEvent;
		try {
			objectEvent = (ObjectEvent) pool.borrowObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		objectEvent.source = source;
		return objectEvent;
	}
	
	public static ObjectEvent getInstance(Object source, Object object, int eventType, NodeUndoInfo info) {
			ObjectEvent objectEvent = getInstance(source);
			objectEvent.object = object;
			objectEvent.eventType = eventType;
			objectEvent.info = info;
			return objectEvent;
	}
	
	public void recycle() {
		try {
			pool.returnObject(this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void reset() {
		source = null;
		eventType = 0;
		object = null;	
	}
	
	private ObjectEvent() {
		super(NULL_SOURCE); // it needs a source.  Will be modified later
		reset();
	}
	
	
	
	private static GenericObjectPool pool = new GenericObjectPool(new ObjectEventFactory());
	
	private static class ObjectEventFactory extends BasePoolableObjectFactory {
		public Object makeObject() { //claur
			return new ObjectEvent();
		}

		public void activateObject(Object arg0){ //claur
			ObjectEvent objectEvent = (ObjectEvent)arg0;
			objectEvent.reset();

		}
	}
	public interface Listener extends EventListener {
		public void objectChanged(ObjectEvent objectEvent);
	}	
	/**
	 * @return Returns the field.
	 */
	public Field getField() {
		return field;
	}
	/**
	 * @param field The field to set.
	 */
	public void setField(Field field) {
		this.field = field;
	}
}
