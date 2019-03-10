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
package com.projectlibre1.exchange;

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

		public void setUnassignedResource(Object projectlibreUnassignedResource) {
			this.unassignedResource = projectlibreUnassignedResource;
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
					Object value=PropertyUtils.getProperty(resource,mergeField.getProjectLibreName());
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
			protected String importName,projectlibreName,displayName;

			public MergeField(String importName, String projectlibreName, String displayName) {
				super();
				// TODO Auto-generated constructor stub
				this.importName = importName;
				this.projectlibreName = projectlibreName;
				this.displayName = displayName;
			}

			public String getImportName() {
				return importName;
			}

			public void setImportName(String importName) {
				this.importName = importName;
			}

			public String getProjectLibreName() {
				return projectlibreName;
			}

			public void setProjectLibreName(String projectlibreName) {
				this.projectlibreName = projectlibreName;
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
