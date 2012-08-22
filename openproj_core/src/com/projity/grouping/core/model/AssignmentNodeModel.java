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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.undo.UndoableEditSupport;

import org.apache.commons.collections.Closure;

import com.projity.document.Document;
import com.projity.document.ObjectEvent;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeFactory;
import com.projity.grouping.core.hierarchy.HierarchyUtils;
import com.projity.grouping.core.hierarchy.MutableNodeHierarchy;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.AssignmentService;
import com.projity.pm.assignment.HasAssignments;
import com.projity.pm.task.NormalTask;
import com.projity.pm.task.Project;
import com.projity.undo.AssignmentCreationEdit;
import com.projity.undo.AssignmentDeletionEdit;

/**
 *
 */
public class AssignmentNodeModel extends DefaultNodeModel implements ObjectEvent.Listener{
	protected Document document;
	protected boolean containsLeftObjects;


	public AssignmentNodeModel(Document document, boolean containsLeftObjects) {
		super();
		this.containsLeftObjects=containsLeftObjects;
		setDocument(document);
	}
	public AssignmentNodeModel(NodeModelDataFactory dataFactory) {
		this(dataFactory,null,false);
	}
	public AssignmentNodeModel(NodeModelDataFactory dataFactory,Document document, boolean containsLeftObjects) {
		super(dataFactory);
		this.containsLeftObjects=containsLeftObjects;
		setDocument(document);
	}
	AssignmentNodeModel(/*ArrayList list,*/ MutableNodeHierarchy hierarchy,
			NodeModelDataFactory dataFactory,Document document, boolean containsLeftObjects) {
		super(/*list,*/ hierarchy, dataFactory);
		this.containsLeftObjects=containsLeftObjects;
		setDocument(document);
	}


	public void objectChanged(ObjectEvent objectEvent) {
		if (objectEvent.getObject() instanceof Assignment) {
			Assignment assignment = ((Assignment)objectEvent.getObject());
			if (assignment.isDefault()) return;
			if (assignment.getDocument(containsLeftObjects) == document) { //TODO check if it's correct
				if (objectEvent.isCreate()) {
					Object parentObject=containsLeftObjects ? assignment.getLeft() : assignment.getRight();
							Node parent=search(parentObject);
					if (parent != null){ // the new assignment has to be added
						Node child=null;
						if (objectEvent.getInfo()!=null && dataFactory instanceof Project ){
							//don't want a node shared by Projet and ResourcePool
							if (objectEvent.getInfo().getNode()!=null) child=objectEvent.getInfo().getNode();
							else child = NodeFactory.getInstance().createNode(assignment);
						}
						else{
							//search if assignment already exists in hierarchy
							for (Enumeration e=parent.children();e.hasMoreElements();){
								Node c=(Node)e.nextElement();
								if (c.getImpl()==assignment){
									child=c;
									break;
								}
							}
							if (child==null) child = NodeFactory.getInstance().createNode(assignment);
						}
						int position=0;
						for (Enumeration e=parent.children();e.hasMoreElements();position++){
							if (!(((Node)e.nextElement()).getImpl() instanceof Assignment))
								break;
						}
						add(parent,child,position,EVENT);

						if ((objectEvent.getInfo()==null||(objectEvent.getInfo()!=null&&objectEvent.getInfo().isUndo()))&& dataFactory instanceof Project){
							UndoableEditSupport undoableEditSupport=getUndoableEditSupport();
							if (undoableEditSupport!=null){
								undoableEditSupport.postEdit(new AssignmentCreationEdit(child));
							}
						}

					}



				} else if (objectEvent.isDelete()) {
					Node node=search(assignment);
					if (node != null){
						remove(node,EVENT,false,false);
						if ((objectEvent.getInfo()==null||(objectEvent.getInfo()!=null&&objectEvent.getInfo().isUndo()))&& dataFactory instanceof Project){
							UndoableEditSupport undoableEditSupport=getUndoableEditSupport();
							if (undoableEditSupport!=null){
								undoableEditSupport.postEdit(new AssignmentDeletionEdit(node));
							}
						}
					}


				} else { //update
				}
			}
		}
	}

	protected void finalize() throws Throwable {
		super.finalize();
	   	document.removeObjectListener(this);
	}





	public boolean isContainsLeftObjects() {
		return containsLeftObjects;
	}
	public void setContainsLeftObjects(boolean containsLeftObjects) {
		this.containsLeftObjects = containsLeftObjects;
	}

	public Document getDocument() {
		return document;
	}
	public void setDocument(Document document) {
		if (this.document!=null) this.document.removeObjectListener(this);
		this.document = document;
		if (document!=null) document.addObjectListener(this);
	}


	public void addAssignments(){
		addAssignments(iterator());
	}

	public void addAssignments(Iterator i){
		Iterator j;
		Node parent;
		Node child;
		Map assignments=new HashMap();
		while (i.hasNext()) { // go thru tasks or resources
			parent = (Node)i.next();
			if (! (parent.getImpl() instanceof HasAssignments)) {
				continue; //TODO currently getting voidNodeImpl's.  This should go away when fixed
			}
			HasAssignments hasAssignments = (HasAssignments)parent.getImpl();
			for (j = hasAssignments.getAssignments().iterator();j.hasNext();) {
				Assignment assignment = (Assignment)j.next();
				if (assignment.isDefault()) continue;
				child = NodeFactory.getInstance().createNode(assignment);
				assignments.put(child,parent);
			}
		}
		boolean found;
		for (Iterator k=assignments.keySet().iterator();k.hasNext();){
			child=(Node)k.next();
			parent=(Node)assignments.get(child);

			//search if assignment already exists in hierarchy
			//fixes bug about adding a second assignment when the view is first shown
			found=false;
			for (Enumeration e=parent.children();e.hasMoreElements();){
				Node c=(Node)e.nextElement();
				if (c.getImpl()==child.getImpl()){
					child=c;
					found=true;
					break;
				}
			}

			if (!found) add(parent,child,SILENT);
		}
	}

	public boolean confirmRemove(List nodes) {
		return true;
// This code is commented out since the user was getting prompted multiple times.  With Undo, it's less important
//		if (Environment.isBatchMode())
//			return true;
//		Iterator i = nodes.iterator();
//		Object impl;
//		boolean hasActuals = false;
//		while (i.hasNext()) {
//			impl = ((Node)i.next()).getImpl();
//			if (impl instanceof Schedule) {
//				if (((Schedule)impl).getPercentComplete() > 0.0D) {
//					hasActuals = true;
//					break;
//				}
//			}
//		}
//		if (hasActuals)
//			return Alert.okCancel(Messages.getString("Message.allowDeleteActuals"));
//		else
//			return true;
	}



	public void paste(Node parent,List nodes,int position,int actionType){
		super.paste(parent, nodes, position, actionType);
		ArrayList roots=new ArrayList();
		HierarchyUtils.extractParents(nodes, roots);
		final List freeAssignments=new ArrayList();
		for (Iterator i=roots.iterator();i.hasNext();)
			hierarchy.visitLeaves((Node)i.next(), new Closure(){
				public void execute(Object o) {
					Node node=(Node)o;
					if (node.getImpl() instanceof Assignment){
						Assignment assignment=(Assignment)node.getImpl();
						Node parent=(Node)node.getParent();
						if (parent.getImpl() instanceof NormalTask){
							NormalTask task=(NormalTask)parent.getImpl();
							if (task.findAssignment(assignment.getResource())==null){
								freeAssignments.add(node);
							}
						}
					}
				}
			});
		for (Iterator i=freeAssignments.iterator();i.hasNext();){
			Node node=(Node)i.next();
			node.removeFromParent();
			System.out.println("restoring assignment: "+node.getImpl());
			AssignmentService.getInstance().connect(node,this,isUndo(actionType));
		}

	}



}