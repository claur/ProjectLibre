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
package com.projectlibre1.pm.key;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import com.projectlibre1.field.FieldContext;
import com.projectlibre1.server.data.DataObject;

/**
 *
 */
public class HasKeyImpl extends HasUniqueIdImpl implements HasKey{
	private static final long serialVersionUID = 739020202L;
	private static final int MAX_NAME_LENGTH = 255;
	long id = 0L;
	Date created = new Date();
	String name = "";

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	public String getName(FieldContext fieldContext) {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		if (name != null && name.length() > MAX_NAME_LENGTH) {
			System.out.println("Truncating long name from " + name.length() + " chars to " + MAX_NAME_LENGTH + " chars");
			name = name.substring(0, MAX_NAME_LENGTH);
		}
		this.name = name;
	}

	/**
	 *
	 */
	public HasKeyImpl(DataObject hasUniqueId,long uniqueId) {
		super(hasUniqueId,uniqueId);
	}
	public HasKeyImpl(boolean local,DataObject hasUniqueId) {
		super(local,hasUniqueId);
	}



	/* (non-Javadoc)
	 * @see com.projectlibre1.pm.task.HasKey#getId()
	 */
	public long getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see com.projectlibre1.pm.task.HasKey#setId(int)
	 */
	public void setId(long id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see com.projectlibre1.pm.task.HasKey#getCreationDate()
	 */
	public Date getCreated() {
		return created;
	}


	/**
	 * @param created The created to set.
	 */
	public void setCreated(Date created) {
		this.created = created;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (! (other instanceof HasKey))
			return false;
		return uniqueId == ((HasKey)other).getUniqueId();
	}


	public void serialize(ObjectOutputStream s) throws IOException {
	    s.writeLong(getUniqueId());
	    s.writeLong(getId());
	}

	//call init to complete initialization
	public static HasKeyImpl deserialize(ObjectInputStream s,DataObject hasUniqueId) throws IOException, ClassNotFoundException  {
		HasKeyImpl hasKey=new HasKeyImpl(hasUniqueId,s.readLong());
	    //hasKey.setUniqueId(s.readLong());
	    hasKey.setId(s.readLong());
//	    hasKey.setNew(false);
	    return hasKey;
	}

//	public boolean isNew() {
//		return CommonDataObject.isLocal(getUniqueId());
//	}

	//because it implements DataObject, should implement a different interface
	private transient boolean dirty;
	public boolean isDirty() {
		return dirty;
	}
	public void setDirty(boolean dirty) {
		//System.out.println("HasKeyImpl _setDirty("+dirty+"): "+getName());
		this.dirty = dirty;
	}


}
