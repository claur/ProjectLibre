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
package com.projity.exchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFrame;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * Used to merge resources found in an imported file
 */
public abstract class ResourceMappingForm {
		protected List importedResources;
		protected Vector resources;
		protected List selectedResources;
		protected boolean local=false,master=false;
		protected int accessControlType;
		protected JFrame owner;
		
		public static final MergeField NO_MERGE=new MergeField(null,null,"");
		protected Vector mergeFields=new Vector();
		protected MergeField mergeField;
		protected Object unassignedResource;
		
		public ResourceMappingForm(){
			selectedResources=new ArrayList();
			mergeFields.add(NO_MERGE);
			mergeField=NO_MERGE;
			
		}
		
		public List getSelectedResources() {
			return selectedResources;
		}

		public void setSelectedResources(List selectedResources) {
			this.selectedResources = selectedResources;
		}

		public List getImportedResources() {
			return importedResources;
		}

		public void setImportedResources(List importedResources) {
			this.importedResources = importedResources;
		}

		public Vector getResources() {
			return resources;
		}

		public void setResources(Vector resources) {
			this.resources = resources;
		}

		public boolean isLocal() {
			return local;
		}

		public void setLocal(boolean local) {
			this.local = local;
		}

		public boolean isMaster() {
			return master;
		}

		public void setMaster(boolean master) {
			this.master = master;
		}

		public JFrame getOwner() {
			return owner;
		}

		public void setOwner(JFrame owner) {
			this.owner = owner;
		}
		
		
		
		public int getAccessControlType() {
			return accessControlType;
		}

		public void setAccessControlType(int accessControlType) {
			this.accessControlType = accessControlType;
		}

		public abstract boolean execute();
		
		
		
		
		public Object getUnassignedResource() {
			return unassignedResource;
		}

		public void setUnassignedResource(Object projityUnassignedResource) {
			this.unassignedResource = projityUnassignedResource;
		}

		public MergeField getMergeField() {
			return mergeField;
		}

		public void setMergeField(MergeField mergeField) {
			this.mergeField = mergeField;
			Map mergeFieldMap=new HashMap();
			HashSet notMergedValues=new HashSet();
			Object resource;
			if (mergeField!=NO_MERGE)
			for (Iterator i=resources.iterator();i.hasNext();){
				resource=i.next();
				try {
					Object value=PropertyUtils.getProperty(resource,mergeField.getProjityName());
					if (notMergedValues.contains(value)) continue;
					if (mergeFieldMap.containsKey(value)){ //not duplicates
						mergeFieldMap.remove(value);
						notMergedValues.add(value);
					}else mergeFieldMap.put(value,resource);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			selectedResources.clear();
			Object value;
			for (Iterator i=importedResources.iterator();i.hasNext();){
				resource=i.next();
				if (mergeField==NO_MERGE) selectedResources.add(unassignedResource);
				else{
					try {
						value=PropertyUtils.getProperty(resource,mergeField.getImportName());
						if (value==null||!mergeFieldMap.containsKey(value)) selectedResources.add(unassignedResource);
						else selectedResources.add(mergeFieldMap.get(value));
					} catch (Exception e) {selectedResources.add(unassignedResource);}
				}
				
			}
		}

		public void addMergeField(MergeField mergeField){
			mergeFields.add(mergeField);
		}
		
		public Vector getMergeFields(){
			return mergeFields;
		}
		
		public void selectMergeField(MergeField mergeField){
		}
		
		
		public static class MergeField{
			protected String importName,projityName,displayName;

			public MergeField(String importName, String projityName, String displayName) {
				super();
				// TODO Auto-generated constructor stub
				this.importName = importName;
				this.projityName = projityName;
				this.displayName = displayName;
			}

			public String getImportName() {
				return importName;
			}

			public void setImportName(String importName) {
				this.importName = importName;
			}

			public String getProjityName() {
				return projityName;
			}

			public void setProjityName(String projityName) {
				this.projityName = projityName;
			}
			
			public String getDisplayName() {
				return displayName;
			}

			public void setDisplayName(String displayName) {
				this.displayName = displayName;
			}

			public String toString(){
				return displayName;
			}
			
		}
		
}
