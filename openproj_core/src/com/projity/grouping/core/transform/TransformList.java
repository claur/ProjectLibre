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
package com.projity.grouping.core.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;

import com.projity.configuration.Dictionary;
import com.projity.configuration.NamedItem;
import com.projity.contrib.util.Log;
import com.projity.contrib.util.LogFactory;
import com.projity.field.InvalidFormulaException;
import com.projity.grouping.core.transform.filtering.NodeFilter;
import com.projity.grouping.core.transform.filtering.NotVoidFilter;
import com.projity.strings.Messages;
import com.projity.util.Environment;

/**
 * 
 */
public class TransformList implements NamedItem {
	static Log log = LogFactory.getLog(TransformList.class);
	public static final String category="TransformCategory";
	public String getCategory() {
		return category;
	}
	
	String name = null;
	String id = null;
	Map elementMap = new HashMap();
	Map factoryMap = new HashMap();
	List factories = new ArrayList();

	public TransformList() {}
	
	
	public void addFactory(CommonTransformFactory factory) {
    	if (factory.isServer()&&Environment.getStandAlone()) return;
		factories.add(factory);
		factoryMap.put(factory.getId(),factory);
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public void setId(String id) {
		this.id = id;
		setName(Messages.getString(id));
	}	
	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	public Object getTransform(String id) {
	    Object transform=null;
	    if (elementMap.containsKey(id))
		    transform=elementMap.get(id);
	    else{
            CommonTransformFactory factory=getFactory(id);
            if (factory==null) return null;
            try {
                    transform=factory.getTransform();
                    elementMap.put(id,transform);
                } catch (InvalidFormulaException e) {
        			log.error("Formula not set: invalid formula text: " +factory.getFormulaText());
                }
	    }
	    return transform;
	}
	
	public CommonTransformFactory getFactory(String id) {
		return (CommonTransformFactory)factoryMap.get(id);
	}
	public List getFactories() {
		return factories;
	}
	public List getFactories(ViewConfiguration view,String type) {
	    List authorizedList;
	    if (view==null){
	        authorizedList=new ArrayList();
	        if ("user_filters".equals(type)) authorizedList.add(ViewTransformer.FILTER_NONE_ID);
	        else if ("user_sorters".equals(type)) authorizedList.add(ViewTransformer.SORTER_NONE_ID);
	        else if ("user_groupers".equals(type)) authorizedList.add(ViewTransformer.GROUPER_NONE_ID);
	    }else{
	        if ("user_filters".equals(type)) authorizedList=view.getTransform().getFilterList();
	        else if ("user_sorters".equals(type)) authorizedList=view.getTransform().getSorterList();
	        else authorizedList=view.getTransform().getGrouperList();
	        if (authorizedList==null) return factories;
	    }
	    List filtered=new ArrayList();
	    CommonTransformFactory f;
	    for (Iterator i=factories.iterator();i.hasNext();){
	        f=(CommonTransformFactory)i.next();
	        if (authorizedList.contains(f.getId())) filtered.add(f);
	    }
		return filtered;
	}
	
	public static void addDigesterEvents(Digester digester){
		//filters
	    digester.addObjectCreate("*/transform/filters", "com.projity.grouping.core.transform.TransformList");
	    digester.addSetProperties("*/transform/filters");
		digester.addSetNext("*/transform/filters", "add", "com.projity.configuration.NamedItem");

		digester.addObjectCreate("*/filter", "com.projity.grouping.core.transform.filtering.NodeFilterFactory");
	    digester.addSetProperties("*/filter");
	    digester.addCallMethod("*/filter/formulaText","setFormulaText",0);
	    digester.addSetNext("*/filter", "addFactory", "com.projity.grouping.core.transform.filtering.NodeFilterFactory");

	    
	    //sorters
		digester.addObjectCreate("*/transform/sorters", "com.projity.grouping.core.transform.TransformList");
	    digester.addSetProperties("*/transform/sorters");
		digester.addSetNext("*/transform/sorters", "add", "com.projity.configuration.NamedItem");
	    
	    digester.addObjectCreate("*/sorter", "com.projity.grouping.core.transform.sorting.NodeSorterFactory");
	    digester.addSetProperties("*/sorter");
	    digester.addCallMethod("*/sorter/formulaText","setFormulaText",0);
	    digester.addCallMethod("*/sorter/groupNameFormula","setGroupNameFormula",0);
	    digester.addSetNext("*/sorter", "addFactory", "com.projity.grouping.core.transform.sorting.NodeSorterFactory");
	    	    
	    //groupers
		digester.addObjectCreate("*/transform/groupers", "com.projity.grouping.core.transform.TransformList");
	    digester.addSetProperties("*/transform/groupers");
		digester.addSetNext("*/transform/groupers", "add", "com.projity.configuration.NamedItem");
	    
	    digester.addObjectCreate("*/grouper", "com.projity.grouping.core.transform.grouping.NodeGrouper");
	    digester.addSetProperties("*/grouper");
	    digester.addSetNext("*/grouper", "addFactory", "com.projity.grouping.core.transform.grouping.NodeGrouper");
	    
	    digester.addObjectCreate("*/grouper/group", "com.projity.grouping.core.transform.grouping.NodeGroup");
	    digester.addSetProperties("*/grouper/group");
	    digester.addSetNext("*/grouper/group", "addGroup", "com.projity.grouping.core.transform.grouping.NodeGroup");

	    
	    digester.addObjectCreate("*/param", "com.projity.grouping.core.transform.TransformParameter");
	    digester.addSetProperties("*/param");
	    digester.addSetNext("*/param", "addParameter", "com.projity.grouping.core.transform.TransformParameter");
	    
	    
		//transformers
	    digester.addObjectCreate("*/transform/transformers", "com.projity.grouping.core.transform.TransformList");
	    digester.addSetProperties("*/transform/transformers");
		digester.addSetNext("*/transform/transformers", "add", "com.projity.configuration.NamedItem");

		digester.addObjectCreate("*/transformer", "com.projity.grouping.core.transform.transformer.NodeTransformerFactory");
	    digester.addSetProperties("*/transformer");
	    digester.addCallMethod("*/transformer/formulaText","setFormulaText",0);
	    digester.addSetNext("*/transformer", "addFactory", "com.projity.grouping.core.transform.transformer.NodeTransformerFactory");

	
	}
	
	
	
	
	
	
	public static TransformList getInstance(String name){
	    return (TransformList)Dictionary.get(category,name);
	}
	
	
	
	
	
	/*public static NodeFilter getNotAssignmentFilter(){
	    return (NodeFilter)getInstance("hidden_filters").getTransform("Filter.NotAssignment");
	}*/
	
	public static NodeFilter getNotVoidFilter(){
	    return NotVoidFilter.getInstance();
	}
	
	public static NodeFilter getTrueFilter(){
	    return (NodeFilter)getInstance("user_filters").getTransform("Filter.True");
	}
	
	
	
}
