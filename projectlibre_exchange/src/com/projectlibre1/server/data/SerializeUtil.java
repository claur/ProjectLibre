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
 * Copyright (c) 2012. All Rights Reserved. All portions of the code written by 
 * ProjectLibre are Copyright (c) 2012. All Rights Reserved. Contributor 
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
 * Attribution Copyright Notice: Copyright (c) 2012, ProjectLibre, Inc.
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
package com.projectlibre1.server.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.projectlibre1.pm.calendar.WorkCalendar;
import com.projectlibre1.pm.resource.EnterpriseResource;
import com.projectlibre1.pm.resource.ResourceImpl;
import com.projectlibre1.session.Session;

/**
 *
 */
public class SerializeUtil {
    public static final boolean ZIP=true;


    public static byte[] serializeToByteArray(Object data) throws IOException {
        ByteArrayOutputStream bout=new ByteArrayOutputStream();
        ObjectOutputStream out=new ObjectOutputStream(bout);
        out.writeObject(data);
        out.close();
        bout.close();
    	return bout.toByteArray();
    }

    public static Object deserializeFromByteArray(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bin=new ByteArrayInputStream(bytes);
        ObjectInputStream in=new ObjectInputStream(bin);
        return in.readObject();
    }

    public static SerializedDataObject serialize(DataObject data, SerializedDataObjectFactory factory) throws IOException{
        SerializedDataObject r=factory.createSerializedDataObject();
        r.setUniqueId(data.getUniqueId());
        r.setName(data.getName());
        r.setDirty(data.isDirty());

        ByteArrayOutputStream bout=new ByteArrayOutputStream();
        ObjectOutputStream out;
        if (ZIP&&((data instanceof WorkCalendar)||(data instanceof EnterpriseResource)||(data instanceof ResourceImpl))){ //other are too small to be zipped
        	ZipOutputStream zout=new ZipOutputStream(bout);
        	zout.putNextEntry(new ZipEntry("Serialized"));
        	out=new ObjectOutputStream(zout);
        } else out=new ObjectOutputStream(bout);
        out.writeObject(data);
        out.close();
        bout.close();

        r.setSerialized(bout.toByteArray());
        //System.out.println(data+" size="+r.getSerialized().length);
        return r;
    }

    public static DataObject deserialize(SerializedDataObject sdata,Session session) throws IOException,ClassNotFoundException{
    	ByteArrayInputStream bin=new ByteArrayInputStream(sdata.getSerialized());
        ObjectInputStream in;
		if (ZIP&&(sdata.getType()==DataObjectConstants.CALENDAR_TYPE||sdata.getType()==DataObjectConstants.ENTERPRISE_RESOURCE_TYPE||sdata.getType()==DataObjectConstants.RESOURCE_TYPE)){
        	ZipInputStream zin=new ZipInputStream(bin);
            zin.getNextEntry();
            in=new ObjectInputStream(zin);
        } else in=new ObjectInputStream(bin);
        DataObject data=(DataObject)in.readObject();
        data.setUniqueId(session==null?sdata.getUniqueId():session.getId());
        data.setName(sdata.getName());
        return data;
    }

}
