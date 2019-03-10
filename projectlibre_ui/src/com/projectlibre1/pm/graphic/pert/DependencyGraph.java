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
package com.projectlibre1.pm.graphic.pert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.projectlibre1.pm.graphic.model.cache.GraphicDependency;
import com.projectlibre1.pm.graphic.model.cache.GraphicNode;
import com.projectlibre1.pm.graphic.model.cache.NodeModelCache;

/**
 *
 */
public class DependencyGraph{
	protected HashMap nodeMap=new HashMap();
	protected NodeModelCache cache;
	
	public void setCache(NodeModelCache cache){
		this.cache=cache;
		nodeMap.clear();
	}
	
	public void insertDependency(GraphicDependency dependency){
		//System.out.println("insertDependency");
	    GraphicNode preValue=(GraphicNode)dependency.getPredecessor();
	    GraphicNode sucValue=(GraphicNode)dependency.getSuccessor();
	    Node pre=(Node)nodeMap.get(preValue);
	    if (pre==null){
	        pre=new Node(preValue);
	        nodeMap.put(preValue,pre);
	    }
	    Node suc=(Node)nodeMap.get(sucValue);
	    if (suc==null){
	        suc=new Node(sucValue);
	        nodeMap.put(sucValue,suc);
	    }
	    
	    pre.addSuccessor(suc);
	    suc.addPredecessor(pre);
	}
	public void removeDependency(GraphicDependency dependency){
		//System.out.println("removeDependency");
	    GraphicNode preValue=(GraphicNode)dependency.getPredecessor();
	    GraphicNode sucValue=(GraphicNode)dependency.getSuccessor();
	    Node pre=(Node)nodeMap.get(preValue);
	    Node suc=(Node)nodeMap.get(sucValue);
	    if (pre==null||suc==null)return;
	    
	    pre.removeSuccessor(suc);
	    suc.removePredecessor(pre);
	    if (pre.isolated()) nodeMap.remove(pre.getValue());
	    if (suc.isolated()) nodeMap.remove(suc.getValue());
	}
	
	public void insertDependencies(List dependencies){
	    for (Iterator i=dependencies.iterator();i.hasNext();) insertDependency((GraphicDependency)i.next());
	}
	public void removeDependencies(List dependencies){
	    for (Iterator i=dependencies.iterator();i.hasNext();) removeDependency((GraphicDependency)i.next());
	}
	
	
	public void updatePertLevels(){
//		System.out.println("updatePertLevels");
	    for (Iterator i=cache.getIterator();i.hasNext();){
	        resetCachePertLevel((GraphicNode)i.next());
	    }
	    
	    Set predecessors=new HashSet();
	    Set successors=new HashSet();
	    for (Iterator i=nodeMap.values().iterator();i.hasNext();){
	        Node node=(Node)i.next();
	        GraphicNode gnode=(GraphicNode)node.getValue();
	        //resetCachePertLevel(gnode);
	        if (node.getPredecessors().size()==0) predecessors.add(node);
	    }
	    
	    while (predecessors.size()>0){
	        updateSuccessorsPertLevel(predecessors,successors);
	        Set tmp=predecessors;
	        predecessors=successors;
	        successors=tmp;
	        successors.clear();
	    }
	}
	
	
	private void updateSuccessorsPertLevel(Set predecessors,Set successors){
	    for (Iterator i=predecessors.iterator();i.hasNext();){
	        Node pre=(Node)i.next();
	        GraphicNode gpre=(GraphicNode)pre.getValue();
	        for (Iterator j=pre.getSuccessors().iterator();j.hasNext();){
	            Node suc=(Node)j.next();
	            successors.add(suc);
		        GraphicNode gsuc=(GraphicNode)suc.getValue();
		        correctPertLevel(gpre,gsuc);
	        }
	    }
	}
	
	private void resetCachePertLevel(GraphicNode gnode){
	    cache.setPertLevel(gnode,cache.getLevel(gnode));
	}
	private void correctPertLevel(GraphicNode gpre,GraphicNode gsuc){
        if (cache.getPertLevel(gsuc)<=cache.getPertLevel(gpre)){
            cache.setPertLevel(gsuc,cache.getPertLevel(gpre)+1);
        }
	}
	
	
	
	public class Node{
	    protected Object value;
	    protected List predecessors;
	    protected List successors;
	    public Node(Object value){
	        this.value=value;
	        predecessors=new LinkedList();
	        successors=new LinkedList();
	    }
        public Object getValue() {
            return value;
        }
        public void setValue(Object value) {
            this.value = value;
        }
        
        public void addSuccessor(Node successor){
            successors.add(successor);
        }
        public void removeSuccessor(Node successor){
            successors.remove(successor);
        }
        public List getSuccessors(){
            return successors;
        }
        
        public void addPredecessor(Node predecessor){
            predecessors.add(predecessor);
        }
        public void removePredecessor(Node predecessor){
            predecessors.remove(predecessor);
        }
        public List getPredecessors(){
            return predecessors;
        }
        
        public boolean isolated(){
            return predecessors.size()==0&&successors.size()==0;
        }
        
	}

}
