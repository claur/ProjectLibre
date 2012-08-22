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
package com.projity.pm.graphic.pert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.projity.pm.graphic.model.cache.GraphicDependency;
import com.projity.pm.graphic.model.cache.GraphicNode;
import com.projity.pm.graphic.model.cache.NodeModelCache;

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
