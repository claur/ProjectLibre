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
package com.projity.grouping.core.hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import java.util.Vector;

import javax.swing.event.EventListenerList;
import javax.swing.tree.TreeNode;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;

import com.projity.grouping.core.LazyParent;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeBridge;
import com.projity.grouping.core.event.HierarchyEvent;
import com.projity.grouping.core.event.HierarchyListener;
import com.projity.pm.key.HasKey;

/**
 *
 */
public abstract class AbstractMutableNodeHierarchy implements NodeHierarchy{
	public abstract Object getRoot();
	public Object getChild(Object parent, int index) {
		return ((Node)parent).getChildAt(index);
	}
	public int getChildCount(Object parent) {
		return ((Node)parent).getChildCount();
	}
	public int getIndexOfChild(Object parent, Object child) {
		return ((Node)parent).getIndex((Node)child);
	}
    public int getIndexOfNode(Node key, boolean skipVoid) {
    	return getIndexOfNode((Node)getRoot(),key,new Counter(),skipVoid);
    }
  
	public Iterator iterator(Node rootNode){
		NodeBridge r;
		if (rootNode!=null && rootNode instanceof NodeBridge) r=(NodeBridge)rootNode;
		else r=(NodeBridge)getRoot();
		return IteratorUtils.asIterator(r.preorderEnumeration());
	}
	public Iterator iterator(){
		return iterator(null);
	}
	
    final class ShallowPreorderInterator implements Iterator<TreeNode> {
    	protected Stack stack;
    	protected int maxLevel;
    	protected Stack<Integer> levelStack;

    	public ShallowPreorderInterator(TreeNode rootNode,int maxLevel,boolean returnRoot) {
    	    super();
    	    Vector v = new Vector(1);
    	    v.addElement(rootNode);	// PENDING: don't really need a vector
    	    stack = new Stack();
    	    stack.push(v.elements());
    	    levelStack=new Stack<Integer>();
    	    levelStack.push(0);
    	    this.maxLevel=maxLevel;
    	    if (!returnRoot&&hasNext()) next(); 
    	}

    	public boolean hasNext() {
    	    return (!stack.empty() &&
    		    ((Enumeration)stack.peek()).hasMoreElements());
    	}

    	public TreeNode next() {
    	    Enumeration	enumer = (Enumeration)stack.peek();
    	    int level=levelStack.peek();
    	    TreeNode	node = (TreeNode)enumer.nextElement();
    	    Enumeration	children = level==maxLevel?null:node.children();

    	    if (!enumer.hasMoreElements()) {
    		stack.pop();
    		levelStack.pop();
    	    }
    	    if (children!=null&&children.hasMoreElements()) {
    		stack.push(children);
    		levelStack.push(level+1);
    	    }
    	    return node;
    	}
		public void remove() {
			throw new UnsupportedOperationException("Remove not supported");
		}
    }

	
	public Iterator shallowIterator(int maxLevel,boolean returnRoot){
		return new ShallowPreorderInterator((TreeNode)getRoot(),maxLevel,returnRoot);
	}
	

    
    private class Counter { // an int object that is mutable
    	int count = 0;
    }
    
    private int getIndexOfNode(Node node, Node key, Counter counter, boolean skipVoid) {
    	if (key == node)
    		return counter.count;
    	if (!skipVoid || !node.isVirtual())
    		counter.count++;
    	Collection children = getChildren(node);
    	if (children == null)
    		return -1;
    	Iterator i = children.iterator();
    	int found = -1;
    	while (i.hasNext()) {
    		if ((found = getIndexOfNode(key,(Node)i.next(),counter,skipVoid)) != -1)
    			break;
    	}
    	return found;
    	
    }
    public void visitAll(Closure visitor) {
    	visitAll(null,visitor);
    }
    public void visitAll(Node parent, Closure visitor) {
    	if (parent != null)
    		visitor.execute(parent);
    	Collection children = getChildren(parent);
    	if (children != null) {
        	Iterator i = children.iterator();
        	while (i.hasNext()) {
        		visitAll((Node)i.next(),visitor);
        	}
    	}
    }
    //doesn't visit parent
    public void visitAllLevelOrder(Node parent, boolean skipLazyParents, Closure visitor) {
    	visitAllLevelOrder(true,parent,skipLazyParents,visitor);
    }

   	public void visitAllLevelOrder(boolean first, Node parent, boolean skipLazyParents, Closure visitor) {
   		// when saving a project, we do not want to save the children of subproject tasks, except when 
   		// saving a suproject itself, in which case, the root element will be a subproject task and first will be true
    	if (!first && skipLazyParents && parent != null && parent.getImpl() instanceof LazyParent) 
    		return;
    	Collection children = getChildren(parent);
    	if (children != null) {
        	Iterator i = children.iterator();
        	while (i.hasNext()) {
        		visitor.execute(i.next());
        	}
        	i=children.iterator();
        	while (i.hasNext()) {
        		visitAllLevelOrder(false,(Node)i.next(),skipLazyParents,visitor);
        	}
    	}
    }
   	
    public void visitAll(Node parent, boolean skipLazyParents, Closure visitor) {
    	visitAll(true,parent,skipLazyParents,visitor);
    }
   	public void visitAll(boolean first, Node parent, boolean skipLazyParents, Closure visitor) {
   		// when saving a project, we do not want to save the children of subproject tasks, except when 
   		// saving a suproject itself, in which case, the root element will be a subproject task and first will be true
    	if (!first && skipLazyParents && parent != null && parent.getImpl() instanceof LazyParent) 
    		return;
    	Collection children = getChildren(parent);
    	if (children != null) {
        	Iterator i = children.iterator();
        	i=children.iterator();
        	while (i.hasNext()) {
        		Node node=(Node)i.next();
        		visitor.execute(node);
        		visitAll(false,node,skipLazyParents,visitor);
        	}
    	}
    }
   	
    public void visitLeaves(Node node, Closure visitor) {
    	if (node.isLeaf()) visitor.execute(node);
    	else for (Enumeration e=node.children();e.hasMoreElements();){
    		visitLeaves((Node)e.nextElement(), visitor);
    	}
    }


/**
 * Get next non virtual node
 */    
    public Node getNext(Node current) {
    	Node node = current;
    	while (true) {
    		node = getNext(node,true);
    		if (node == null || !node.isVirtual())
    			break;
    	}
    	return node;
    }
    
    private Node getNext(Node current, boolean doChildren) {
    	List children;
    	if (doChildren) { // if haven't visited children yet
    		children = getChildren(current);
       		if (children != null && children.size() > 0) // if parent, next is first child
    			return (Node)children.get(0);
    	}
       	if (current == null) // null parent has no parent
       		return null;
   		Node parent =getParent(current);
		children = getChildren(parent);
		Iterator i = children.iterator();
        while (i.hasNext()) { // get next element after this one.  If it is the last then try its parent
        	if (i.next() == current) {
        		if (i.hasNext())
        			return (Node)i.next();
        		else
        			return getNext(parent,false);
        	}
        }
        return null;
    }
    public Node getPrevious(Node current) {
    	Node node = current;
    	while (true) {
    		node = getPrevious(node,true);
    		if (node == null || !node.isVirtual())
    			break;
    	}
    	return node;
    }
    
    private Node getPrevious(Node current, boolean doChildren) {
       	if (current == null) // null parent has no parent
       		return null;
    	List children;

    	Node parent =getParent(current);
		children = getChildren(parent);
    	if (doChildren) { // if haven't visited children yet
			ListIterator i = children.listIterator(children.size()); // reverse iterator
	        while (i.hasPrevious()) { // get next element after this one.  If it is the last then try its parent
	        	if (i.previous() == current) {
	        		if (i.hasPrevious())
	        			return getPrevious((Node)i.previous(),false);
	        		else
	        			return parent;
	        	}
	        }
    	}

		children = getChildren(current);
   		if (children != null && children.size() > 0) // if parent, previous is last child
			return getPrevious((Node)children.get(children.size()-1),doChildren);
   		
   		return current;
    }

	
    public void dump() {
    	dump(null,"",new Closure(){
    		public void execute(Object obj) {
    			System.out.println((String)obj);
    		}
    	});
   }
    public void dump(final StringBuffer buf) {
    	dump(null,"",new Closure(){
    		public void execute(Object obj) {
    			buf.append((String)obj).append('\n');
    		}
    	});
   }
    
    private void dump(Node parent, String indent,Closure c) {
    	if (parent != null)
    		c.execute(indent + ">"+parent.toString());
    	Collection children = getChildren(parent);
    	if (children != null) {
        	Iterator i = children.iterator();
        	while (i.hasNext()) {
        		dump((Node)i.next(),indent+"--",c);
        	}
    	}
    }
	
    
	public abstract Object clone();
	
	
	
	
	
	protected EventListenerList hierarchyListenerList = new EventListenerList();

	public void addHierarchyListener(HierarchyListener l) {
		hierarchyListenerList.add(HierarchyListener.class, l);
	}
	public void removeHierarchyListener(HierarchyListener l) {
		hierarchyListenerList.remove(HierarchyListener.class, l);
	}
	public HierarchyListener[] getHierarchyListeners() {
		return (HierarchyListener[]) hierarchyListenerList.getListeners(HierarchyListener.class);
	}
    public EventListener[] getHierarchyListeners(Class listenerType) { 
    	return hierarchyListenerList.getListeners(listenerType); 
    }
    
 	protected void fireStructureChanged(Object source) {
		Object[] listeners = hierarchyListenerList.getListenerList();
		HierarchyEvent e = null;
//		for (int i = listeners.length - 2; i >= 0; i -= 2) {
		for (int i = 0; i < listeners.length; i += 2) {
			if (listeners[i] == HierarchyListener.class) {
				if (e == null) {
					e = new HierarchyEvent(source, 
							HierarchyEvent.STRUCTURE_CHANGED, null);
				}
				((HierarchyListener) listeners[i + 1]).structureChanged(e);
		
			}
		}
	}
	protected void fireNodesChanged(Object source, Object[] nodes,Object[] oldNodes,Object flag) {
		Object[] listeners = hierarchyListenerList.getListenerList();
		HierarchyEvent e = null;
		for (int i = 0; i < listeners.length; i += 2) {
			if (listeners[i] == HierarchyListener.class) {
				if (e == null) {
					e = new HierarchyEvent(source, 
							HierarchyEvent.NODES_CHANGED, nodes,oldNodes,flag);
				}
				((HierarchyListener) listeners[i + 1]).nodesChanged(e);
		
			}
		}
	}
	protected void fireNodesInserted(Object source, Object[] nodes,Object[] oldNodes,Object flag) {
		Object[] listeners = hierarchyListenerList.getListenerList();
		HierarchyEvent e = null;
		for (int i = 0; i < listeners.length; i += 2) {
			if (listeners[i] == HierarchyListener.class) {
				if (e == null) {
					e = new HierarchyEvent(source, 
							HierarchyEvent.NODES_INSERTED, nodes,oldNodes,flag);
				}
				((HierarchyListener) listeners[i + 1]).nodesInserted(e);
		
			}
		}
	}
	protected void fireNodesRemoved(Object source, Object[] nodes,Object[] oldNodes,Object flag) {
		Object[] listeners = hierarchyListenerList.getListenerList();
		HierarchyEvent e = null;
		for (int i = 0; i < listeners.length; i += 2) {
			if (listeners[i] == HierarchyListener.class) {
				if (e == null) {
					e = new HierarchyEvent(source, 
							HierarchyEvent.NODES_REMOVED, nodes,oldNodes,flag);
				}
				((HierarchyListener) listeners[i + 1]).nodesRemoved(e);
		
			}
		}
	}
	protected void fireNodesChanged(Object source, Object[] nodes) {
//		System.out.println("Hierarchy="+hashCode()+", fireNodesChanged, nodes="+nodes);
//		dump();
	    fireNodesChanged(source,nodes,null,null);
	}
	protected void fireNodesInserted(Object source, Object[] nodes) {
//		System.out.println("Hierarchy="+hashCode()+", fireNodesInserted, nodes="+nodes);
//		dump();
	    fireNodesInserted(source,nodes,null,null);
	}
	protected void fireNodesRemoved(Object source, Object[] nodes) {
//		System.out.println("Hierarchy="+hashCode()+", fireNodesRemoved, nodes="+nodes);
//		dump();
	    fireNodesRemoved(source,nodes,null,null);
	}
	/**
	 * Convenience method to convert hierarchy to a list of nodes in depth-first order.
	 * @return
	 */
	public List toList(final boolean isNode, final Predicate filter) {
		final ArrayList list = new ArrayList();
    	visitAll(new Closure(){
			public void execute(Object node) {
				if (filter != null  && !filter.evaluate(((Node) node).getImpl()))
					return;
				if (isNode) 
					list.add(node);
				else
					list.add(((Node) node).getImpl());
			}});
    	return list;
    }
	
	
	public void renumber(){
		visitAll(new Closure(){
			private int index=0;
			public void execute(Object o) {
				Node node=(Node)o;
				if (node.hasNumber()){
					HasKey impl=(HasKey)node.getImpl();
					if (impl.getId()!=++index){
						impl.setId(index);
					}
				}
			}
		});
	}
	
	
	protected int updateLevel=0;
	protected synchronized void beginUpdate(){
		updateLevel++;
	}
	protected synchronized void endUpdate(){
		updateLevel--;
	}
	protected synchronized int getUpdateLevel(){
		return updateLevel;
	}


}
