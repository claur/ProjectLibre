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
package com.projectlibre1.grouping.core.transform;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Transformer;

import com.projectlibre1.field.InvalidFormulaException;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.util.Environment;

/**
 *
 */
public abstract class CommonTransformFactory extends CommonTransform{
 	protected String id = null;
 	protected String name = null;
	protected String formulaText = null;
	protected boolean showSummary = true;
	protected boolean showEmptyLines=true;
	protected boolean showEndEmptyLines=true;
	protected String definition=null;
	protected String arguments=null;
	protected Transformer composition=null;
	protected boolean server;
	
	public abstract CommonTransform getTransform() throws InvalidFormulaException;
	
	public CommonTransform getTransformFromDefinition()  throws InvalidFormulaException{
	    if (definition!=null){
	        try {
	            /*if (arguments==null)
	                return (CommonTransform)Class.forName(definition).newInstance();
	            else*/ return (CommonTransform)Class.forName(definition).
	            	getConstructor(new Class[]{String.class}).newInstance(new Object[]{arguments});
            } catch (Exception e) {
                throw new InvalidFormulaException(e);
            }
	    }
	    return null;

	}
	
	
    public String getFormulaText() {
        return formulaText;
    }
    public void setFormulaText(String formulaText) {
        this.formulaText = formulaText;
    }
    public boolean isShowSummary() {
        return showSummary;
    }
    public void setShowSummary(boolean showSummary) {
        this.showSummary = showSummary;
    }

    public String getDefinition() {
        return definition;
    }
    public void setDefinition(String definition) {
        this.definition = definition;
    }
    
    public String getArguments() {
        return arguments;
    }
    public void setArguments(String arguments) {
        this.arguments = arguments;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
        if (name == null)
        	setName(Messages.getString(id));
    }
	public void setNameId(String id) {
		this.name = Messages.getString(id);
	}
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    
    public boolean isShowEmptyLines() {
        return showEmptyLines;
    }
    public void setShowEmptyLines(boolean showEmptyLines) {
        this.showEmptyLines = showEmptyLines;
    }

    public boolean isShowEndEmptyLines() {
		return showEndEmptyLines;
	}
	public void setShowEndEmptyLines(boolean showEndEmptyLines) {
		this.showEndEmptyLines = showEndEmptyLines;
	}
	
	protected boolean showEmptySummaries = true;
	public boolean isShowEmptySummaries() {
		return showEmptySummaries;
	}
	public void setShowEmptySummaries(boolean showEmptySummaries) {
		this.showEmptySummaries = showEmptySummaries;
	}
	
	protected boolean showAssignments = true;
    public boolean isShowAssignments() {
		return showAssignments;
	}
	public void setShowAssignments(boolean showAssignments) {
		this.showAssignments = showAssignments;
	}
	
	protected boolean preserveHierarchy = true;
	public boolean isPreserveHierarchy() {
		return preserveHierarchy;
	}
	public void setPreserveHierarchy(boolean preserveHierarchy) {
		this.preserveHierarchy = preserveHierarchy;
	}

    

	public boolean isServer() {
		return server;
	}

	public void setServer(boolean server) {
		this.server = server;
	}

	public Transformer getComposition() {
        return composition;
    }
    public void setComposition(Transformer composition) {
        this.composition = composition;
    }
    
    
	protected void setProperties(CommonTransform t) throws InvalidFormulaException{
	    t.setShowEmptyLines(isShowEmptyLines());
	    t.setShowEndEmptyLines(isShowEndEmptyLines());
	    t.setShowSummary(isShowSummary());
	    t.setShowEmptySummaries(isShowEmptySummaries());
	    t.setShowAssignments(isShowAssignments());
	    t.setPreserveHierarchy(isPreserveHierarchy());
	    if (subTransforms!=null){
	        ArrayList sub=new ArrayList();
	        for (Iterator i=subTransforms.iterator();i.hasNext();)
	            sub.add(((CommonTransformFactory)i.next()).getTransform());
	        t.setSubTransforms(sub);
	    }
	    t.setParameters(getParameters());
	    t.setParametersMap(getParametersMap());
	}
	
	
	public String toString(){
		return getName();
	}

    public void setRedefinitionCallBack(Closure callback){}
    
    
    public void addFactory(CommonTransformFactory factory){
    	if (factory.isServer()&&Environment.getStandAlone()) return;
        if (subTransforms==null) subTransforms=new ArrayList();
        subTransforms.add(factory);
    }
    
    
}
