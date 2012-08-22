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

Attribution Information: Attribution Copyright Notice: Copyright � 2006, 2007
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.projity.association.AssociationList;
import com.projity.document.Document;
import com.projity.field.Field;
import com.projity.field.FieldContext;
import com.projity.field.FieldParseException;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeBridge;
import com.projity.grouping.core.NodeFactory;
import com.projity.grouping.core.VoidNodeImpl;
import com.projity.grouping.core.hierarchy.HierarchyUtils;
import com.projity.grouping.core.hierarchy.MutableNodeHierarchy;
import com.projity.grouping.core.hierarchy.NodeHierarchy;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.AssignmentService;
import com.projity.pm.assignment.HasAssignments;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.dependency.HasDependencies;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.task.NormalTask;
import com.projity.pm.task.Task;
import com.projity.pm.task.TaskLinkReference;
import com.projity.pm.task.TaskLinkReferenceImpl;
import com.projity.undo.ModelFieldEdit;
import com.projity.undo.NodeCreationEdit;
import com.projity.undo.NodeDeletionEdit;
import com.projity.undo.NodeImplChangeAndValueSetEdit;
import com.projity.undo.NodeImplChangeEdit;
import com.projity.undo.NodePasteEdit;
import com.projity.undo.NodeUndoInfo;
import com.projity.undo.UndoController;
import com.projity.util.Environment;

/**
 *
 */
public class DefaultNodeModel implements NodeModel {

	protected NodeHierarchy hierarchy;
	protected NodeModelDataFactory dataFactory = null;

	/**
	 *
	 */
	public DefaultNodeModel() {
		hierarchy=new MutableNodeHierarchy();
	}

	public DefaultNodeModel(NodeModelDataFactory dataFactory) {
		this();
		this.dataFactory = dataFactory;
	}
	//for clone()
	DefaultNodeModel(NodeHierarchy hierarchy, NodeModelDataFactory dataFactory) {
		this.hierarchy=hierarchy;
		this.dataFactory = dataFactory;
	}

	public void addBefore(LinkedList siblings,Node newNode,int actionType){
		Node previous,next,parent;
		boolean firstChild;
		if (siblings.size()==0){
			return;
		}else if (siblings.size()==1){
			previous=null;
			next=(Node)siblings.removeLast();
			parent=null;
			firstChild=true;
		}else{
			previous=(Node)siblings.removeFirst();  //no need to clone, list used only here, see CommonSpreadSheetModel
			next=(Node)siblings.removeLast();
			parent=(Node)next.getParent();
			firstChild=(parent==previous);
			if (firstChild) parent=previous;
			else parent=(Node)previous.getParent();
			remove(siblings, NodeModel.SILENT);
		}
		siblings.add(newNode);
		add(parent,siblings,(firstChild)?0:(parent.getIndex(previous)+1),NodeModel.SILENT);

		getDataFactory().setGroupDirty(true);
		//TODO need undo here
	}

	public void addBefore(Node sibling,Node newNode,int actionType){
		Node parent=(Node)sibling.getParent();
		add(parent,newNode,parent.getIndex(sibling),actionType);
	}
	public void addBefore(Node sibling,List newNodes,int actionType){
		Node parent=(Node)sibling.getParent();
		add(parent,newNodes,parent.getIndex(sibling),actionType);
	}
	public void add(Node parent,Node child,int actionType){
		add(parent,child,-1,actionType);
	}
	public void add(Node parent,Node child,int position,int actionType){
		ArrayList children=new ArrayList();
		children.add(child);
		add(parent,children,position,actionType);
		//hierarchy.add(parent,child,position,actionType);
	}
	public void add(Node parent,List children,int actionType){
		add(parent,children,-1,actionType);
		//hierarchy.add(parent,children,actionType);
	}
	public void add(Node parent,List children,int position,int actionType){
		hierarchy.add(parent,children,position,actionType);
		//Undo
		if (isUndo(actionType)) postEdit(new NodeCreationEdit(this,parent,children,position));

	}


	public void add(Node child,int actionType){
		add((Node)hierarchy.getRoot(),child,actionType);
	}

	public Node newNode(Node parent,int position,int actionType){
		//check if position is correct
		Node node;
		int p=position;
		int i=0;
		for (Enumeration e=parent.children();e.hasMoreElements();i++){
			node=(Node)e.nextElement();
			if (i==p){
				if (node.getImpl() instanceof Assignment) p++;
				else{
					Node newNode=NodeFactory.getInstance().createVoidNode();
					add(parent,newNode,p,NodeModel.NORMAL);
					return newNode;
				}
			}
		}
		Node newNode=NodeFactory.getInstance().createVoidNode();
		add(parent,newNode,-1,NodeModel.NORMAL);
		return newNode;

	}

	public void paste(Node parent,List nodes,int position,int actionType){
		//nodes=copy(nodes,NodeModel.SILENT); //make an other copy, in case it is copied more than one time
		//done in transfert handler

		hierarchy.paste(parent,nodes,position,this,actionType);
		//Undo
		if (isUndo(actionType)) postEdit(new NodePasteEdit(this,parent,nodes,position));
	}

	public boolean isAncestor(Node parent, Node child) {
		if (child == null)
			return false;
		if (parent == child)
			return true;
		return isAncestor(parent,getParent(child));
	}

	public boolean isAncestorOrDescendant(Node one, Node two) {
		return isAncestor(one,two) || isAncestor(two,one);
	}

	public boolean testAncestorOrDescendant(Node one, List nodes) {
		Iterator i = nodes.iterator();
		while (i.hasNext()) {
			if (isAncestorOrDescendant(one,(Node)i.next()))
				return false;
		}
		return true;
	}

	public void move(Node parent,List nodes,int position,int actionType){
		if (!testAncestorOrDescendant(parent,nodes)) // don't allow circular
			return;
		List cutNodes=cut(nodes,false,actionType);
		//List cutNodes=cut(nodes,actionType&UNDO); //TODO fixes bug 225 but it's probably breaking undo
		paste(parent,cutNodes,position,actionType);
	}


	/**
	 * Convenience method to add a collection of objects (not nodes) to the node model
	 * @param parent
	 * @param collection
	 */
	public void addImplCollection(Node parent, Collection collection,int actionType) {
		Iterator i = collection.iterator();
		Node child;
		while (i.hasNext()) {
			child = NodeFactory.getInstance().createNode(i.next());
			add(parent,child,actionType);
		}

	}

	public void remove(Node node,int actionType){
		remove(node, actionType, true,true);
	}
	public void remove(Node node,int actionType,boolean removeDependencies){
		remove(node, actionType, true,removeDependencies);
	}
	public void remove(Node node,int actionType,boolean filterAssignments,boolean removeDependencies){
		ArrayList nodes=new ArrayList();
		nodes.add(node);
		remove(nodes,actionType,filterAssignments,removeDependencies);
		//hierarchy.remove(node,this,actionType);
		//it calls back removeApartFromHierarchy for each node to remove
	}
	public void remove(List nodes,int actionType){
		remove(nodes, actionType, true);
	}
	public void remove(List nodes,int actionType,boolean removeDependencies){
		remove(nodes, actionType, true,removeDependencies);
	}
	public void remove(List nodes,int actionType,boolean filterAssignments,boolean removeDependencies){
		if (undoController!=null&&isUndo(actionType)){
			undoController.getEditSupport().beginUpdate();
		}
		try {
			ArrayList roots=new ArrayList();
			HierarchyUtils.extractParents(nodes, roots);
			if (filterAssignments)
				for (Iterator i=roots.iterator();i.hasNext();){
					Node node=(Node)i.next();
					if (node.getImpl() instanceof Assignment){
						i.remove();
						//AssignmentService.getInstance().remove(node, this, true);
					}
				}
			boolean containsSubprojects=false;
			List parents=new ArrayList();
			List positions=new ArrayList();
			NodeBridge node,parent;
			for (Iterator i=roots.iterator();i.hasNext();){
				node=(NodeBridge)i.next();
				if (NodeModelUtil.nodeIsSubproject(node))
					containsSubprojects=true;
				parent=(NodeBridge)node.getParent();
				parents.add(parent);
				positions.add(new Integer(parent.getIndex(node)));
			}
			if (!confirmRemove(roots))
				return;

			if (undoController!=null&&isUndo(actionType)){
				if (containsSubprojects) undoController.clear();
			}

			hierarchy.remove(roots,this,actionType,removeDependencies);
			//it calls back removeApartFromHierarchy for each node to remove
			hierarchy.checkEndVoidNodes(actionType);

			//Undo
			if (undoController!=null){
				if (!containsSubprojects&&isUndo(actionType)){
					postEdit(new NodeDeletionEdit(this,parents,roots,positions));
				}
			}
		} finally{
			if (undoController!=null){
				if (isUndo(actionType)){
					undoController.getEditSupport().endUpdate();
				}
			}
		}
	}
	public void removeAll(int actionType){
		hierarchy.removeAll(this,actionType);
	}
	public boolean removeApartFromHierarchy(Node node,boolean cleanAssignment,int actionType,boolean removeDependencies){
		if (!isEvent(actionType))
			return true;
//		try {
//			beginUpdate();
			if (node.getImpl() instanceof Assignment){
				Assignment assignment=(Assignment)node.getImpl();
//				if (cleanAssignment)
					AssignmentService.getInstance().remove(assignment,cleanAssignment,this,isUndo(actionType)); //LC 8/4/2006 - hk 7/8/2006 changed null to this so event will be fired
//				else if (assignment.getResource()!=ResourceImpl.getUnassignedInstance()){
//					assignment.getResource().removeAssignment(assignment);
//				}


			//AssignmentService.getInstance().remove((Assignment)node.getImpl(),this);
			}else if (dataFactory!=null&&!node.isVoid())
				dataFactory.remove(node.getImpl(),this,false,isUndo(actionType),removeDependencies); //TODO make this work properly with subproject
//		} finally {
//			endUpdate();
//		}
		return true;
	}

	public List cut(List nodes,int actionType){
		return cut(nodes,true,actionType);
	}
	public List cut(List nodes,boolean clone,int actionType){
		List newNodes=copy(nodes,clone,actionType);
		remove(nodes,actionType);
		return newNodes;
//		ArrayList parentNodes=new ArrayList(nodes.size());
//		HierarchyUtils.extractParents(nodes,parentNodes);
//		remove(parentNodes,actionType);
//		//TODO check parent is null
//		return parentNodes;
	}


	public List copy(List nodes,int actionType){
		return copy(nodes,true,actionType);
	}

	public List copy(List nodes,boolean clone,int actionType){
		ArrayList parentNodes=new ArrayList(nodes.size());
		HierarchyUtils.extractParents(nodes,parentNodes);
		if (!clone) return parentNodes;
		Set assignedNodes=new HashSet();
		Map implMap=new HashMap();
		Set<Dependency> predecessors=new HashSet<Dependency>();
		Set<Dependency> successors=new HashSet<Dependency>();
		for (ListIterator i=parentNodes.listIterator();i.hasNext();){
			Node parent=(Node)i.next();
			Node newParent=cloneNode(parent,null,implMap,predecessors,successors);
			cloneBranch(parent,newParent,assignedNodes,implMap,predecessors,successors);
			i.remove();
			i.add(newParent);
		}

		//rebuild dependencies
		if (Environment.isKeepExternalLinks()){
			for (Dependency dependency : successors) {
				TaskLinkReference pt=(TaskLinkReference)dependency.getPredecessor();
				TaskLinkReference st=(TaskLinkReference)dependency.getSuccessor();

				HasDependencies predecessor=(Task)implMap.get(pt);
				HasDependencies successor=(Task)implMap.get(st);

				if (predecessor==null) predecessor=new TaskLinkReferenceImpl(pt.getUniqueId(),pt.getProject());
				if (successor==null) successor=new TaskLinkReferenceImpl(st.getUniqueId(),st.getProject());
				Dependency d=Dependency.getInstance(predecessor, successor, dependency.getDependencyType(), dependency.getLag());
				d.setDirty(true);
				predecessor.getDependencyList(false).add(d);
				successor.getDependencyList(true).add(d);
				predecessors.remove(d);
			}
			for (Dependency dependency : predecessors) {
				TaskLinkReference pt=(TaskLinkReference)dependency.getPredecessor();
				TaskLinkReference st=(TaskLinkReference)dependency.getSuccessor();

				TaskLinkReference predecessor=(TaskLinkReference)implMap.get(pt);
				TaskLinkReference successor=(TaskLinkReference)implMap.get(st);

				if (predecessor==null) predecessor=new TaskLinkReferenceImpl(pt.getUniqueId(),pt.getProject());
				if (successor==null) successor=new TaskLinkReferenceImpl(st.getUniqueId(),st.getProject());
				Dependency d=Dependency.getInstance(predecessor, successor, dependency.getDependencyType(), dependency.getLag());
				d.setDirty(true);
				predecessor.getDependencyList(false).add(d);
				successor.getDependencyList(true).add(d);
				//successors.remove(d);
			}

		}else{
			for (Dependency dependency : predecessors) {
				if (successors.contains(dependency)){
					Task predecessor=(Task)implMap.get(dependency.getPredecessor());
					Task successor=(Task)implMap.get(dependency.getSuccessor());
					if (predecessor!=null&&successor!=null){
						Dependency d=Dependency.getInstance(predecessor, successor, dependency.getDependencyType(), dependency.getLag());
						d.setDirty(true);
						//Serializer.connectDependency(dependency, predecessor, successor);
						predecessor.getDependencyList(false).add(d);
						successor.getDependencyList(true).add(d);
					}
				}
			}

		}

		for (Iterator i=assignedNodes.iterator();i.hasNext();){
			addAssignments((Node)i.next());
		}

		for (ListIterator i=parentNodes.listIterator();i.hasNext();){
			Node node=(Node)i.next();
			cleanBranch(node);
		}

		return parentNodes;
	}
	private void cloneBranch(Node parent,Node newParent,Set assignedNodes,Map implMap,Set<Dependency> predecessors,Set<Dependency> successors){
		for (Iterator i=parent.childrenIterator();i.hasNext();){
				Node child=(Node)i.next();
				if (child.getImpl() instanceof Assignment){
					assignedNodes.add(newParent);
				}else{
					Node newChild=cloneNode(child,newParent,implMap,predecessors,successors);
					cloneBranch(child,newChild,assignedNodes,implMap,predecessors,successors);
				}
		}
	}
	private Node cloneNode(Node oldNode,Node newParent,Map implMap,Set<Dependency> predecessors,Set<Dependency> successors){
		Object oldNodeImpl=oldNode.getImpl();
		Object newNodeImpl=cloneNodeImpl(oldNodeImpl);
		implMap.put(oldNodeImpl, newNodeImpl);
		if (oldNodeImpl instanceof Task){
			Task t=(Task)oldNodeImpl;
			predecessors.addAll(t.getDependencyList(true));
			successors.addAll(t.getDependencyList(false));
		}
		Object parentImpl = (newParent==null)?null:newParent.getImpl();
		NodeModelDataFactory factory = getFactory(parentImpl);

		factory.addUnvalidatedObject(newNodeImpl,this,parentImpl);

		Node newNode=NodeFactory.getInstance().createNode(newNodeImpl);
		if (newParent!=null) newParent.add(newNode);
		if (parentImpl != null&& parentImpl instanceof Task)
			((Task)parentImpl).setWbsChildrenNodes(getHierarchy().getChildren(newParent)); //rebuild children task's wbs cache
		return newNode;
	}
	private Object cloneNodeImpl(Object impl){
				if (impl instanceof VoidNodeImpl){
					return new VoidNodeImpl();
				}else if (impl instanceof NormalTask){
					return ((NormalTask)impl).clone();
				}else if (impl instanceof ResourceImpl){
					return ((ResourceImpl)impl).clone();
				}//TOTO assignments
		return null;
	}

	private void cleanBranch(Node parent){
		for (Iterator i=parent.childrenIterator();i.hasNext();){
				Node child=(Node)i.next();
				cleanNodeImpl(child.getImpl());
				cleanBranch(child);
		}
	}
	private void cleanNodeImpl(Object impl){
		if (impl instanceof NormalTask){
			((NormalTask)impl).cleanClone();
		}else if (impl instanceof ResourceImpl){
			((ResourceImpl)impl).cleanClone();
		}
	}

	private void addAssignments(Node node){
		if (node.getImpl() instanceof HasAssignments){
			AssociationList assignments=((HasAssignments)node.getImpl()).getAssignments();
			if (assignments==null) return;
			for (ListIterator i=assignments.listIterator(assignments.size());i.hasPrevious();){
				Assignment assignment=(Assignment)i.previous();
				if (assignment.isDefault()) continue;
				Node assignmentNode=NodeFactory.getInstance().createNode(assignment);
				node.insert(assignmentNode,0);
			}
		}
	}







	public Object clone(){
		return new DefaultNodeModel((NodeHierarchy)hierarchy.clone(), dataFactory);
	}

	public Iterator iterator(){
		return hierarchy.iterator();
	}
	public Iterator iterator(Node rootNode){
		return hierarchy.iterator(rootNode);
	}
	public Iterator shallowIterator(int maxLevel,boolean returnRoot){
		return hierarchy.shallowIterator(maxLevel,returnRoot);
	}
	/**
	 * @return Returns the hierarchy.
	 */
	public NodeHierarchy getHierarchy() {
		return hierarchy;
	}



	/**
	 * @param hierarchy The hierarchy to set.
	 */
	public void setHierarchy(NodeHierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	/* (non-Javadoc)
	 * @see com.projity.grouping.core.NodeModel#hasChildren(com.projity.grouping.core.Node)
	 */
	public boolean hasChildren(Node node) {
		return !hierarchy.isLeaf(node);
	}
	public boolean isSummary(Node node){
		return hierarchy.isSummary(node);
	}
	/**
	 * @param key
	 * @param c
	 * @return
	 */
	public Node search(Object key, Comparator c) {
		return hierarchy.search(key, c);
	}



	private static ImplComparator implComparatorInstance = null;
	public static ImplComparator getImplComparatorInstance() {
		if (implComparatorInstance == null)
			implComparatorInstance = new ImplComparator();
		return implComparatorInstance;
	}

	public static class ImplComparator implements Comparator {
		ImplComparator() {}
		public int compare(Object node, Object impl) {
			if (((Node)node).getImpl() == impl)
				return 0;
			else
				return 1;
		}
	}

	public Node search(Object key) {
		//TODO consider using a hashtable instead of searching like this
		return search(key,getImplComparatorInstance());
	}


	// Below is for tree model
	/**
	 * @param arg0
	 */
	public void addTreeModelListener(TreeModelListener arg0) {
		hierarchy.addTreeModelListener(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public Object getChild(Object arg0, int arg1) {
		return hierarchy.getChild(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @return
	 */
	public int getChildCount(Object arg0) {
		return hierarchy.getChildCount(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public int getIndexOfChild(Object arg0, Object arg1) {
		return hierarchy.getIndexOfChild(arg0, arg1);
	}
	/**
	 * @return
	 */
	public Object getRoot() {
		return hierarchy.getRoot();
	}
	/**
	 * @param arg0
	 * @return
	 */
	public boolean isLeaf(Object arg0) {
		return hierarchy.isLeaf(arg0);
	}
	/**
	 * @param arg0
	 */
	public void removeTreeModelListener(TreeModelListener arg0) {
		hierarchy.removeTreeModelListener(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 */
	public void valueForPathChanged(TreePath arg0, Object arg1) {
		hierarchy.valueForPathChanged(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see com.projity.grouping.core.NodeModel#setFieldValue(com.projity.field.Field, com.projity.grouping.core.Node, java.lang.Object, java.lang.Object, com.projity.field.FieldContext)
	 */
	public void setFieldValue(Field field, Node node, Object eventSource, Object value, FieldContext context,int actionType) throws FieldParseException {
		Object oldValue=field.getValue(node,this,context);

//		// this prevents the field from sending an update message.  However, ideally the field will send the message and the hiearchy event wont
//		if (context != null)
//			context.setUserObject(FieldContext.getNoUpdateInstance());


		field.setValue(node, this,eventSource, value, context);

//		No longer sending update event
//		if (isEvent(actionType)) hierarchy.fireUpdate(new Node[]{node});
		//TODO treat the ObjectEvent instead

		//Undo
		if (isUndo(actionType)) postEdit(new ModelFieldEdit(this,field,node,eventSource,value,oldValue,context));

	}

	public Node replaceImplAndSetFieldValue(Node node, LinkedList previous, Field field, Object eventSource, Object value,FieldContext context,int actionType) throws FieldParseException {
		//the line following a subproject is connected to the main project
		if (previous!=null&&previous.size()>0){
			Node p=(Node)previous.getFirst();
			if (p!=null&&p.isInSubproject()&&node.getSubprojectLevel()<p.getSubprojectLevel()){
				while (node.getSubprojectLevel()<p.getSubprojectLevel()) p=(Node)p.getParent();
				LinkedList newPrevious=new LinkedList();
				newPrevious.add(p);
				Node vn,pvn;
				for (Iterator i=previous.iterator();i.hasNext();){
					vn=(Node)i.next();
					pvn=(Node)vn.getParent();
					while(pvn!=null&&pvn!=p) pvn=(Node)pvn.getParent();
					if (pvn!=p) newPrevious.add(vn);
				}
				Object parentImpl = p.getImpl();
				NodeModelDataFactory factory = getFactory(parentImpl);
				return replaceImplAndSetFieldValue(node,newPrevious,factory.createUnvalidatedObject(this, parentImpl),field,eventSource,value,context,actionType);

			}
//			if (p!=null&&p.getImpl() instanceof NormalTask){
//				Task task=(Task)p.getImpl();
//				boolean subprojectParent=false;
//				while (task.getOwningProject()!=task.getProject()){
//					Node pParent=(Node)p.getParent();
//					if (pParent.getIndex(p)==pParent.getChildCount()-1){
//						p=pParent;
//						subprojectParent=true;
//					}else{
//						subprojectParent=false;
//						break;
//					}
//				}
//				if (subprojectParent){
//					LinkedList newPrevious=(LinkedList)previous.clone();
//					newPrevious.set(0, p);
//					Object parentImpl = p.getImpl();
//					NodeModelDataFactory factory = getFactory(parentImpl);
//					return replaceImplAndSetFieldValue(node,newPrevious,factory.createUnvalidatedObject(this, parentImpl),field,eventSource,value,context,actionType);
//
//				}
//			}

		}

		Node parent=(Node)node.getParent();
		Object parentImpl = (parent==getHierarchy().getRoot())?null:parent.getImpl();
		NodeModelDataFactory factory = getFactory(parentImpl);
		return replaceImplAndSetFieldValue(node,previous,factory.createUnvalidatedObject(this, parentImpl),field,eventSource,value,context,actionType);
	}

	private NodeModelDataFactory getFactory(Object parentImpl) {
		if (parentImpl == null)
			return dataFactory;
		else
			return dataFactory.getFactoryToUseForChildOfParent(parentImpl);
	}
	public Node replaceImplAndSetFieldValue(Node node, LinkedList previous, Object newImpl, Field field, Object eventSource, Object value,FieldContext context,int actionType) throws FieldParseException {
		List previousPosition=null;
		//move in hierarchy
		if (previous!=null){
			LinkedList p=(LinkedList)previous.clone();
			Node sibling=(Node)p.removeFirst();
			Node parent=(Node)sibling.getParent();
			p.add(node);
			if (getUndoableEditSupport()!=null&isUndo(actionType)){
				previousPosition=new ArrayList(p.size());
				for (Iterator i=p.iterator();i.hasNext();){
					Node n=(Node)i.next();
					previousPosition.add(new NodeImplChangeAndValueSetEdit.Position((Node)n.getParent(),n,n.getParent().getIndex(n)));
				}
			}
			remove(p, NodeModel.SILENT);
			add(parent,p,parent.getIndex(sibling)+1,NodeModel.SILENT);
			//TODO need undo here
		}



		Node parent=(Node)node.getParent();

		Object parentImpl = (parent==getHierarchy().getRoot())?null:parent.getImpl();
		NodeModelDataFactory factory = getFactory(parentImpl);
		factory.addUnvalidatedObject(newImpl,this, parentImpl);
		Object oldImpl=node.getImpl();
		node.setImpl(newImpl);
		try {
			field.setValue(node, this,null, value, context); // will throw if error
		} catch (FieldParseException e) {
			factory.rollbackUnvalidated(this, newImpl); // in some cases, such as ValueObjectForInterval, some cleanup is needed
			throw e;
		}
		// if no exception was thrown, then validate the object and hook it into model
		factory.validateObject(newImpl, this, eventSource, null,true);

		hierarchy.renumber();

//		dataFactory.fireCreated(newImpl);
		hierarchy.checkEndVoidNodes(actionType^NodeModel.EVENT);
		getHierarchy().fireInsertion(new Node[]{node}); //TODO Cause critical path to run twice

		//Undo
		if (isUndo(actionType)) postEdit(new NodeImplChangeAndValueSetEdit(this,node,previous,previousPosition,oldImpl,field,value,context,eventSource));
		return node;
	}
	public Node replaceImpl(Node node,Object newImpl, Object eventSource,int actionType){
		Node parent = getParent(node);
		Object parentImpl = (parent==getHierarchy().getRoot())?null:parent.getImpl();
		NodeModelDataFactory factory = getFactory(parentImpl);

		factory.addUnvalidatedObject(newImpl,this, parentImpl);
		Object oldImpl=node.getImpl();
		node.setImpl(newImpl);
		factory.validateObject(newImpl, this, eventSource,null,false);

		hierarchy.renumber();

//		dataFactory.fireCreated(newImpl);
		getHierarchy().fireRemoval(new Node[]{node}); //TODO Cause critical path to run twice

		hierarchy.checkEndVoidNodes(actionType);
		//Undo
		if (isUndo(actionType)) postEdit(new NodeImplChangeEdit(this,node,oldImpl,eventSource));
		return node;
	}


	public NodeModelDataFactory getDataFactory() {
		return dataFactory;
	}
	/**
	 * @param dataFactory The dataFactory to set.
	 */
	public void setDataFactory(NodeModelDataFactory dataFactory) {
		this.dataFactory = dataFactory;
	}

	public List getChildren(Node parent){
		return getHierarchy().getChildren(parent);
	}
	public Node getParent(Node child){
		return getHierarchy().getParent(child);
	}

    public Document getDocument() {
        return null;
    }

    public static boolean isEvent(int actionType){
    	return (actionType&NodeModel.EVENT)==NodeModel.EVENT;
    }
    public static boolean isUndo(int actionType){
    	return (actionType&NodeModel.UNDO)==NodeModel.UNDO;
    }


    protected UndoController undoController;



	public UndoController getUndoController() {
		return undoController;
	}

	public void setUndoController(UndoController undoController) {
		this.undoController = undoController;
	}

	public UndoableEditSupport getUndoableEditSupport() {
		if (undoController==null) return null;
		return undoController.getEditSupport();
	}
//	public void setUndoableEditSupport(UndoableEditSupport undoableEditSupport) {
//		this.undoableEditSupport = undoableEditSupport;
//	}

	public void postEdit(UndoableEdit edit){
		if (getUndoableEditSupport()!=null){
			getUndoableEditSupport().postEdit(edit);
		}

	}

	public boolean confirmRemove(List nodes) {
		return true;
	}


	protected boolean local,master=true;

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




//	protected int updateLevel=0;
//	protected synchronized void beginUpdate(){
//		updateLevel++;
//	}
//	protected synchronized void endUpdate(){
//		updateLevel--;
//	}
//	protected synchronized int getUpdateLevel(){
//		return updateLevel;
//	}


}
