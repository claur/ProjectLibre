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
package com.projity.grouping.core;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.iterators.EmptyIterator;
import org.apache.commons.collections.iterators.EmptyListIterator;

import com.projity.grouping.core.model.NodeModelUtil;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.resource.Resource;
import com.projity.pm.task.Task;
import com.projity.server.data.DataObject;

/**
 * Bridge of the bridge pattern
 */
public class NodeBridge extends DefaultMutableTreeNode implements Node{
	//protected Object impl;
	protected boolean virtual = false;
	protected boolean voidNode=false;
	protected boolean root=false;
	protected transient boolean lazyParent = false; // for subprojects
	/**
	 * Use NodeFactory instead
	 */
	NodeBridge(Object impl) {
		//this.impl = impl;
		setImpl(impl);
	}
	/**
	 * Use NodeFactory instead
	 */
	NodeBridge(Object impl, boolean virtual) {
		this(impl);
		this.virtual = virtual;
	}
	/**
	 * @see com.projity.grouping.core.Node#isVirtual()
	 */
	public boolean isVirtual() {
		return virtual;
	}
	/**
	 * @param virtual
	 *            The virtual to set.
	 */
	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}

	public boolean isVoid() {
		return voidNode;
	}
	public void setVoid(boolean voidNode) {
		this.voidNode = voidNode;
	}
	public boolean isRoot() {
		return root;
	}
	public void setRoot(boolean root) {
		this.root = root;
	}
	/**
	 * @see com.projity.analysis.core.Node#getType()
	 */
	public Class getType() throws NodeException {
		Object impl=getUserObject();
		if (impl == null)
			throw new NodeException("No Implementation");
		return impl.getClass();
	}
	/**
	 * @see com.projity.analysis.core.Node#accept(com.projity.analysis.core.NodeVisitor)
	 */
	public void accept(NodeVisitor visitor) {
		visitor.execute(this);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		Object impl=getUserObject();
		if (impl == null)
			return "null";
		return impl.toString();
	}
	/**
	 * @return Returns the impl.
	 */
	public Object getImpl() {
		return getUserObject();
	}
	public void setImpl(Object impl) {
		virtual=(impl instanceof GroupNodeImpl);
		voidNode=(impl instanceof VoidNodeImpl);
		setUserObject(impl);
	}

	static ListIterator emptyListIterator(){
		return new ListIterator(){
			public boolean hasNext() {
				return false;
			}
			public Object next() {
				return null;
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
			public void add(Object o) {
			}
			public boolean hasPrevious() {
				return false;
			}
			public int nextIndex() {
				return 0;
			}
			public Object previous() {
				return null;
			}
			public int previousIndex() {
				return -1;
			}
			public void set(Object o) {
			}
		};
	}

    public ListIterator childrenIterator(){
    	return (children==null)?emptyListIterator():children.listIterator();
    }
    public ListIterator childrenIterator(int i){
    	return (children==null)?emptyListIterator():children.listIterator(i);
    }
    public List getChildren(){
    	return children;
    }


    public boolean isIndentable(int value){
    	if (!(value==1||value==-1)) return false;
    	return !root&&!voidNode&&!virtual&&!(getImpl() instanceof Assignment);
    }


    public boolean canBeChildOf(Node p){
    	if (p.isVoid())
    		return false;
    	return NodeModelUtil.canBeChildOf(p,this);
    }

	public boolean isDirty() {
		Object impl=getImpl();
		if (impl instanceof DataObject) return ((DataObject)impl).isDirty();
		else return false;
	}
	public void setDirty(boolean dirty) {
		//System.out.println("NodeBridge _setDirty("+dirty+")");
		Object impl=getImpl();
		if (impl instanceof DataObject) ((DataObject)impl).setDirty(dirty);
	}
	public final boolean isLazyParent() {
		return getImpl() instanceof LazyParent;
	}

	public final boolean isValidLazyParent() {
		return getImpl() instanceof LazyParent
		&& ((LazyParent)getImpl()).isValid();
	}

	public boolean hasNumber(){
		Object impl=getImpl();
		return  impl instanceof Task || impl instanceof Resource;
	}

	protected int subprojectLevel;
	public int getSubprojectLevel() {
		return subprojectLevel;
	}
	public void setSubprojectLevel(int subprojectLevel) {
		this.subprojectLevel = subprojectLevel;
	}
	public boolean isInSubproject(){
		return getSubprojectLevel()>0;
	}
//	public void setInSubproject(boolean inSubproject){
//		setSubprojectLevel(inSubproject?1:0);
//	}


}