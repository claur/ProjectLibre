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
package com.projity.grouping.core.model;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.Transformer;

import com.projity.document.Document;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeFactory;
import com.projity.grouping.core.hierarchy.MutableNodeHierarchy;
import com.projity.pm.task.Project;


/**
 *
 */
public class NodeModelFactory {

	protected static NodeModelFactory instance=null;
	protected NodeModelFactory() {
	}
	public static NodeModelFactory getInstance(){
		if (instance==null) instance=new NodeModelFactory();
		return instance;
	}
	
	public NodeModel createNodeModel(){
		return new DefaultNodeModel();
	}

	public NodeModel createNodeModel(NodeModelDataFactory dataFactory){
		if (dataFactory!=null&&dataFactory.containsAssignments()) return new AssignmentNodeModel(dataFactory);
		else return new DefaultNodeModel(dataFactory);
	}

//	public NodeModel createAssignmentNodeModel(NodeModelDataFactory dataFactory){
//		return new AssignmentNodeModel(dataFactory);
//	}
	

	public NodeModel createAssignmentNodeModel(DefaultNodeModel model,Document document,boolean containsLeftObjects){
		return new AssignmentNodeModel(/*(ArrayList)model.getList().clone(),*/(MutableNodeHierarchy)model.getHierarchy().clone(), model.getDataFactory(),document,containsLeftObjects);
	}

	public NodeModel createNodeModelFromCollection(Collection collection,NodeModelDataFactory dataFactory) {
		NodeModel nodeModel = createNodeModel(dataFactory);
		nodeModel.addImplCollection(null,collection,NodeModel.SILENT);
		return nodeModel;
	}
	public void updateNodeModelFromCollection(NodeModel nodeModel,Collection collection,NodeModelDataFactory dataFactory,int nbEndVoidNodes) {
		nodeModel.removeAll(NodeModel.SILENT);
		nodeModel.setDataFactory(dataFactory);
		nodeModel.addImplCollection(null,collection,NodeModel.SILENT);
		nodeModel.getHierarchy().setNbEndVoidNodes(nbEndVoidNodes);
		nodeModel.getHierarchy().checkEndVoidNodes(nbEndVoidNodes); // fixed bug 145
		nodeModel.getHierarchy().fireUpdate();
	}

	/**
	 * Creates a node model from a given one such that the relationships are the same, as the source hierarchy, though
	 * the nodes and their impls are different.  The impls are created by applying the transformer
	 * @param source
	 * @param transformer
	 * @return
	 */
	public NodeModel replicate(NodeModel source, Transformer transformer) {
		NodeModel newModel = getInstance().createNodeModel();
		replicate(source,null,null,newModel,transformer);
		return newModel;
	}
	
    private void replicate(NodeModel source, Node sourceParentNode, Node newParentNode, NodeModel newModel, Transformer transformer) {
    	Collection children = source.getHierarchy().getChildren(sourceParentNode);        	
    	if (children != null) {
        	Iterator i = children.iterator();
        	while (i.hasNext()) {
        		Node sourceNode = (Node)i.next();
        		Object newImpl = transformer.transform(sourceNode.getImpl()); // make a new object from source
        		if (newImpl==null) continue;
        		Node newNode = NodeFactory.getInstance().createNode(newImpl); // make a new node
        		newModel.add(newParentNode,newNode,NodeModel.SILENT);
        		replicate(source,sourceNode,newNode,newModel,transformer);
        	}
    	}
    	
    }
    
    
    //for DocumentFrame and svg export
	public static NodeModel createTaskModel(Project project) {
		NodeModel taskModel = project.getTaskOutline();
		if (taskModel instanceof AssignmentNodeModel)
			((AssignmentNodeModel) taskModel).addAssignments();
		return taskModel;
	}

	public static NodeModel createResourceModel(Project project) {
		NodeModel resourceModel = project.getResourcePool().getResourceOutline();
		if (resourceModel instanceof AssignmentNodeModel) {
			//the bug is fixed elsewhere
//			if (!resourceModel.hasChildren(null)) // if it is currently empty - fixes bug about adding a second assignment when the view is first shown
				((AssignmentNodeModel) resourceModel).addAssignments();
		}
		return resourceModel;
	}

    	
	
}
