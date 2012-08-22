/*
The contents of this file are subject to the Common Public Attribution License 
Version 1.0 (the "License"); you may not use this file except in compliance with 
the License. You may obtain a copy of the License at 
http://www.projectlibre.com/license . The License is based on the Mozilla Public 
License Version 1.1 but Sections 14 and 15 have been added to cover use of 
software over a computer network and provide for limited attribution for the 
Original Developer. In addition, Exhibit A has been modified to be consistent 
with Exhibit B. 

Software distributed under the License is distributed on an "AS IS" basis, 
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the 
specific language governing rights and limitations under the License. The 
Original Code is OpenProj and ProjectLibre.
The Original Developer is the Initial Developer and is both Projity, Inc and 
ProjectLibre Inc. All portions of the code written by Projity are Copyright (c) 
2006, 2008. All Rights Reserved. All portions of the code written by ProjectLibre 
are Copyright (c) 2012. All Rights Reserved. Contributors Projity, Inc. and 
ProjectLibre, Inc.

Alternatively, the contents of this file may be used under the terms of the 
ProjectLibre End-User License Agreement (the ProjectLibre License) in which case 
the provisions of the ProjectLibre License are applicable instead of those above. 
If you wish to allow use of your version of this file only under the terms of the 
ProjectLibre License and not to allow others to use your version of this file under 
the CPAL, indicate your decision by deleting the provisions above and replace them 
with the notice and other provisions required by the Project Libre License. If you 
do not delete the provisions above, a recipient may use your version of this file 
under either the CPAL or the ProjectLibre Licenses. 


[NOTE: The text of this Exhibit A may differ slightly from the text of the notices 
in the Source Code files of the Original Code. You should use the text of this 
Exhibit A rather than the text found in the Original Code Source Code for Your 
Modifications.] 
EXHIBIT B. Attribution Information both ProjectLibre and OpenProj required

Attribution Copyright Notice: Copyright (c) 2012, ProjectLibre, Inc.
Attribution Phrase (not exceeding 10 words): ProjectLibre, the updated version of 
OpenProj
Attribution URL: http://www.projectlibre.com
Graphic Image as provided in the Covered Code as file: projectlibre-logo.png with 
alternatives listed on http://www.projectlibre.com/logo 

Display of Attribution Information is required in Larger Works which are defined in 
the CPAL as a work which combines Covered Code or portions thereof with code not 
governed by the terms of the CPAL. However, in addition to the other notice 
obligations, all copies of the Covered Code in Executable and Source Code form 
distributed must, as a form of attribution of the original author, include on each 
user interface screen the "OpenProj"  and “ProjectLibre” logos visible to all users. 
The OpenProj logo should be located horizontally aligned with the menu bar and left 
justified on the top left of the screen adjacent to the File menu. The logo must be 
at least 100 x 25 pixels. When users click on the "OpenProj" logo it must direct them 
back to http://www.projity.com. The ProjectLibre logo should be located horizontally 
aligned immediately above the OpenProj logo and left justified in alignment with the 
OpenProj logo. The logo must be at least 144 x 31 pixels. When users click on the 
"ProjectLibre" logo it must direct them back to http://www.projectlibre.com.

Attribution Copyright Notice: Copyright (c) 2006, 2008 Projity, Inc.
Attribution Phrase (not exceeding 10 words): Powered by OpenProj, an open source 
solution from Projity
Attribution URL: http://www.projity.com
Graphic Image as provided in the Covered Code as file: openproj_logo.png with 
alternatives listed on http://www.projity.com/logo 

Display of Attribution Information is required in Larger Works which are defined in 
the CPAL as a work which combines Covered Code or portions thereof with code not 
governed by the terms of the CPAL. However, in addition to the other notice 
obligations, all copies of the Covered Code in Executable and Source Code form 
distributed must, as a form of attribution of the original author, include on each 
user interface screen the "OpenProj" and “ProjectLibre” logos visible to all users. 
The OpenProj logo should be located horizontally aligned with the menu bar and left 
justified on the top left of the screen adjacent to the File menu.  The logo must be 
at least 100 x 25 pixels. When users click on the "OpenProj" logo it must direct them 
back to http://www.projity.com.
*/
package com.projectlibre.core.hierarchy;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.projectlibre.core.nodes.DefaultNode;
import com.projectlibre.core.nodes.Node;

/**
 * @author Laurent Chretienneau
 *
 */
public class DefaultHierarchy implements Hierarchy{
	protected HierarchyNode root;
	protected Map<Node,HierarchyNode> reverseIndex=new HashMap<Node, HierarchyNode>();
	public DefaultHierarchy(){
		root=new DefaultHierarchyNode(new DefaultNode());
	}
	
	//find
	protected void addIndexEntry(HierarchyNode node){
		reverseIndex.put(node.getNode(),node);
	}
	protected HierarchyNode removeIndexEntry(Node node){
		return reverseIndex.remove(node);
	}

	@Override
	public HierarchyNode findHierarchyNode(Node node) {
		return reverseIndex.get(node);		
	}
	
	
	//add
	@Override
	public void add(Node node) {
		root.add(node);
	}	

	@Override
	public void add(Node node, Node parent) {
		HierarchyNode hparent;
		if (parent==null)
			hparent=root;
		else hparent=findHierarchyNode(parent);
		add(node,hparent);
	}

	@Override
	public void add(Node node, HierarchyNode parentHierarchyNode) {
		if (parentHierarchyNode==null)
			parentHierarchyNode=root;
		addIndexEntry((parentHierarchyNode==null ? root : parentHierarchyNode).add(node));
	}
	
	
	//visit
	@Override
	public void visit(Visitor visitor) {
		visit(visitor,VisitType.PRE_ORDER);
	}

	@Override
	public void visit(Visitor visitor,VisitType visitType) {
		visit(visitor,visitType,root);
	}

	@Override
	public void visit(Visitor visitor, VisitType visitType, HierarchyNode parent) {
		visitChildren(visitor,parent,visitType);
	}
	
	protected void visitChildren(Visitor visitor, HierarchyNode parent, VisitType visitType){ //iterative post-order more efficient than recursive version
		if (parent==null)
			parent=root;
		
		HierarchyNode c=parent;
		Stack<Integer> position=new Stack<Integer>();
		int pos;
		do {
			//down to the lower left leaf
			while(c.hasChildren()){  //visit down
				c=c.getChildren().getFirst();
				if (visitType==VisitType.PRE_ORDER) visitor.visit(c);
				position.push(1);
			}
			//up and right
			while (c!=parent){ //visit up
				if (visitType==VisitType.POST_ORDER) visitor.visit(c); 
				c=c.getParent();
				pos=position.pop();
				if (pos<c.getChildrenCount()){
					c=c.getChildren().get(pos);
					if (visitType==VisitType.PRE_ORDER) visitor.visit(c); 
					position.push(pos+1);
					break;
				}
			}

		} while (c!=parent);
	}

	
}
