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
package com.projectlibre1.server.data.linker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Closure;

import com.projectlibre1.grouping.core.Node;
import com.projectlibre1.grouping.core.hierarchy.NodeHierarchy;
import com.projectlibre1.pm.assignment.Assignment;

/**
 *
 */
public abstract class Linker {
	//protected boolean globalIdsOnly=true;
	protected Map transformationMap=new HashMap();
	protected Collection transformed=new ArrayList();
	protected Iterator iterator=null;
	protected Object parent;
	protected Object transformedParent;
	protected Object[] args=null;
	protected boolean incremental;

	public Map getTransformationMap() {
		return transformationMap;
	}
	public Collection getTransformed() {
		return transformed;
	}
//	public boolean isGlobalIdsOnly() {
//		return globalIdsOnly;
//	}
//	public void setGlobalIdsOnly(boolean globalIdsOnly) {
//		this.globalIdsOnly = globalIdsOnly;
//	}

	public boolean isIncremental() {
		return incremental;
	}
	public void setIncremental(boolean incremental) {
		this.incremental = incremental;
	}

	public Object getParent() {
		return parent;
	}
	public void setParent(Object parent) {
		this.parent = parent;
	}
	public Object getTransformedParent() {
		return transformedParent;
	}
	public void setTransformedParent(Object transformedParent) {
		this.transformedParent = transformedParent;
	}

	public Object[] getArgs() {
		return args;
	}
	public void setArgs(Object[] args) {
		this.args = args;
	}
	public boolean hasNext(){return (iterator==null)?false:iterator.hasNext();}
	public void addTransformedObjects() throws Exception{
		while(hasNext()){
			Object obj=executeNext();
			if (obj!=null){
				Object trans=addTransformedObjects(obj);
				if (trans!=null) transformed.add(trans);
			}
		}
		executeFinally();
	}
	public void init(){
		transformationMap.clear();
		transformed.clear();
		initIterator();
	}

	//private int lastIndex;
    public void addOutline(Node root){
    	final Set endVoids=new HashSet();
    	//lastIndex=0;
        getHierarchy().visitAll(root, true,new Closure(){
        	//int tmpIndex=0;
        	public void execute(Object arg){
        		Node node=(Node)arg;
        		Object nodeImpl=node.getImpl();
        		if (!(nodeImpl instanceof Assignment)){
//        			if (!node.isVoid()) lastIndex=tmpIndex;
//        			tmpIndex++;
        			if (node.isVoid()) endVoids.add(node);
        			else endVoids.clear();
        		}
        	}
        });

        getHierarchy().visitAllLevelOrder(root, true,new Closure(){
        	Node thisParent=null;
        	long position=0;
        	//int index=0;
        	public void execute(Object arg){
        		//if (index++>lastIndex) return;
        		Node node=(Node)arg;
        		if (endVoids.contains(node)) return;
        		Object nodeImpl=node.getImpl();
        		if (!(nodeImpl instanceof Assignment)){
        			Node currentParent=getHierarchy().getParent(node);
        			if (currentParent!=null&&currentParent.isRoot()) currentParent=null; //for compatibility
        			if (thisParent!=currentParent){
        				thisParent=currentParent;
        				position=0;
        			}
        			if (node.isVoid()||addOutlineElement(nodeImpl,(thisParent==null)?null:thisParent.getImpl(),position))
        				position++; //skip voids but increment position
        		}
        	}
        });
    }
//    public void addOutline(Node root){
//        getHierarchy().visitAllLevelOrder(root, true,new Closure(){
//        	Node thisParent=null;
//        	long position=0;
//        	public void execute(Object arg){
//        		Node node=(Node)arg;
//        		Object nodeImpl=node.getImpl();
//        		if (!(nodeImpl instanceof Assignment)&&!node.isVoid()){
//        			Node currentParent=getHierarchy().getParent(node);
//        			if (currentParent!=null&&currentParent.isRoot()) currentParent=null; //for compatibility
//        			if (thisParent!=currentParent){
//        				thisParent=currentParent;
//        				position=0;
//        			}
//        			if (addOutlineElement(nodeImpl,(thisParent==null)?null:thisParent.getImpl(),position))
//        				position++;
//        		}
//        	}
//        });
//    }






	protected void initIterator(){}
	public Object executeNext(){return null;}
	public void executeFinally(){}
    public abstract Object addTransformedObjects(Object child) throws Exception;
    public NodeHierarchy getHierarchy(){return null;}
    public boolean addOutlineElement(Object outlineChild,Object outlineParent,long position){return true;}
}
