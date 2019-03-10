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
package com.projectlibre1.grouping.core.hierarchy;

import java.util.Comparator;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import com.projectlibre1.grouping.core.Node;
import com.projectlibre1.grouping.core.event.HierarchyEvent;
import com.projectlibre1.grouping.core.event.HierarchyListener;
import com.projectlibre1.grouping.core.model.NodeModel;
import com.projectlibre1.grouping.core.transform.filtering.NodeFilter;

/**
 *
 */
public class FilteredNodeHierarchy extends AbstractMutableNodeHierarchy implements HierarchyListener {
	protected NodeHierarchy hierarchy;
	protected NodeFilter filter;
	
	public FilteredNodeHierarchy(NodeHierarchy hierarchy) {
		this.hierarchy=hierarchy;
	}

	
	
	
	public NodeFilter getFilter() {
		return filter;
	}
	public void setFilter(NodeFilter filter) {
		this.filter = filter;
		fireStructureChanged(this);
	}
	public NodeHierarchy getHierarchy() {
		return hierarchy;
	}
	public void setHierarchy(NodeHierarchy hierarchy) {
		this.hierarchy = hierarchy;
		fireStructureChanged(this);
	}
	
//    public void cleanNullChildren(){
//    	hierarchy.cleanNullChildren();
//    }
	
	
	
//	public void add(Node parent, Node child, int actionType) {
//		hierarchy.add(parent, child, actionType);
//	}
//    public void add(Node parent,Node child,int position,int actionType){
//		hierarchy.add(parent, child, position,actionType);
//    }
//	public void add(Node parent, List children, int actionType) {
//		hierarchy.add(parent, children, actionType);
//	}
   public void add(Node parent,List children,int position,int actionType){
		hierarchy.add(parent, children, position,actionType);
    }
   public void paste(Node parent,List children,int position, NodeModel model, int actionType){
	hierarchy.paste(parent, children, position, model, actionType);
}
    public void cleanVoidChildren(){
    	hierarchy.cleanVoidChildren();
    }

	public void checkEndVoidNodes(int actionType) {
		hierarchy.checkEndVoidNodes(actionType);
	}
	public void checkEndVoidNodes(boolean subproject,int actionType){
		hierarchy.checkEndVoidNodes(subproject,actionType);
	}
//	public int deleteVoidNodesAfter(NodeHierarchyLocation location) {
//		return hierarchy.deleteVoidNodesAfter(location);
//	}
//	public int deleteVoidNodesAfter(NodeHierarchyLocation location, int n,
//			boolean event) {
//		return hierarchy.deleteVoidNodesAfter(location, n, event);
//	}
	public int getLevel(Node node) {
		return hierarchy.getLevel(node);
	}
	public Object getRoot() {
		return hierarchy.getRoot();
	}
	public void indent(List nodes, int deltaLevel, NodeModel nodeModel,int actionType) {
		hierarchy.indent(nodes, deltaLevel, nodeModel,actionType);
	}
//	public void indent(Node node, int deltaLevel,int actionType) {
//		hierarchy.indent(node, deltaLevel,actionType);
//	}
//	public int insertVoidNodesAfter(NodeHierarchyLocation location, int n,
//			boolean event) {
//		return hierarchy.insertVoidNodesAfter(location, n, event);
//	}
	public boolean isLeaf(Object node) {
		return hierarchy.isLeaf(node);
	}
	public boolean isSummary(Node node) {
		return hierarchy.isSummary(node);
	}
//	public void promoteVoidNode(NodeHierarchyVoidLocation info,
//			Object newNodeImpl) {
//		hierarchy.promoteVoidNode(info, newNodeImpl);
//	}
//	public void remove(Node node, NodeModel model, int actionType) {
//		hierarchy.remove(node, model, actionType);
//	}
	public void remove(List nodes, NodeModel model, int actionType,boolean removeDependencies) {
		hierarchy.remove(nodes, model, actionType,removeDependencies);
	}
//    public void move(Node node,Node newParent){
//        hierarchy.move(node,newParent);
//    }
    public void move(Node node,Node newParent, int actionType){
        hierarchy.move(node,newParent,actionType);
    }
//
//    public void move(List nodes,Node newParent){
//        hierarchy.move(nodes,newParent);
//    }
//	public void replaceVoidNode(Node child, NodeHierarchyVoidLocation info,
//			boolean event) {
//		hierarchy.replaceVoidNode(child, info, event);
//	}
	public void setNbEndVoidNodes(int nbEndVoidNodes) {
		hierarchy.setNbEndVoidNodes(nbEndVoidNodes);
	}
	public int getNbEndVoidNodes() {
		return hierarchy.getNbEndVoidNodes();
	}
	public Node getParent(Node child) {
		return hierarchy.getParent(child);
	}
//	public int getVoidNodesCountAfter(NodeHierarchyLocation location) {
//		return hierarchy.getVoidNodesCountAfter(location);
//	}
//	public ArrayList getVoidNodes(NodeHierarchyLocation location) {
//		return hierarchy.getVoidNodes(location);
//	}
	public void valueForPathChanged(TreePath path, Object newValue) {
 		//TODO works ?
		hierarchy.valueForPathChanged(path, newValue);
	}
	public void addTreeModelListener(TreeModelListener l) {
		hierarchy.addTreeModelListener(l);
	}
	public void removeTreeModelListener(TreeModelListener l) {
		hierarchy.removeTreeModelListener(l);
	}
//	public Map getVoidNodesMap(){
//	    return hierarchy.getVoidNodesMap();
//	}
	
	public void removeAll(NodeModel model, int actionType) {
		hierarchy.removeAll(model,actionType);
	}
	
	
	public List getChildren(Node parent) {
		return filter.filterList(hierarchy.getChildren(parent));
	}
	public Node search(Object key, Comparator c) {
		Node node=hierarchy.search(key, c);
		if (node==null)  return null;
		return (filter.evaluate(node))?node:null;
	}
	
	
	
	public Object clone(){
		FilteredNodeHierarchy newHierarchy=new FilteredNodeHierarchy(hierarchy);
		newHierarchy.setFilter(filter);
		return newHierarchy;
	}
	

	
	public void nodesChanged(HierarchyEvent e) {
		Object[] nodes=filter.filterArray(e.getNodes());
		fireNodesChanged(e.getSource(),nodes,e.getNodes(),e.getFlag());
	}
	public void nodesInserted(HierarchyEvent e) {
		Object[] nodes=filter.filterArray(e.getNodes());
		fireNodesInserted(e.getSource(),nodes,e.getNodes(),e.getFlag());
	}
	public void nodesRemoved(HierarchyEvent e) {
		Object[] nodes=filter.filterArray(e.getNodes());
		fireNodesRemoved(e.getSource(),nodes,e.getNodes(),e.getFlag());
	}
	public void structureChanged(HierarchyEvent e) {
		Object[] nodes=filter.filterArray(e.getNodes());
		fireStructureChanged(e.getSource());
	}
	
    public void fireUpdate(){
    	hierarchy.fireUpdate();
    }
    public void fireUpdate(Node[] nodes){
    	hierarchy.fireUpdate(nodes);
    }
    public void fireInsertion(Node[] nodes){
    	hierarchy.fireInsertion(nodes);
    }
    public void fireRemoval(Node[] nodes){
    	hierarchy.fireRemoval(nodes);
    }
	
	
	
	
	
	
	
	


}
