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
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import com.projity.association.AssociationList;
import com.projity.configuration.Settings;
import com.projity.grouping.core.LazyParent;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeBridge;
import com.projity.grouping.core.NodeFactory;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelUtil;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.dependency.DependencyService;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.resource.ResourcePool;
import com.projity.pm.snapshot.Snapshottable;
import com.projity.pm.task.Project;
import com.projity.pm.task.SubProj;
import com.projity.pm.task.Task;
import com.projity.pm.task.TaskLinkReference;
import com.projity.pm.task.TaskSnapshot;
import com.projity.undo.NodeIndentEdit;
import com.projity.undo.NodeUndoInfo;
import com.projity.util.Alert;
import com.projity.util.Environment;

/**
 * A map that holds the parent-children relationship.  Also implements TreeModel so it can be used to generate
 * trees, such as in outline cells or popup trees.
 */
public class MutableNodeHierarchy extends AbstractMutableNodeHierarchy{
    public static int DEFAULT_NB_END_VOID_NODES=50;
    public static int DEFAULT_NB_MULTIPROJECT_END_VOID_NODES=1;

    private final Node root=NodeFactory.getInstance().createRootNode();



    protected int nbEndVoidNodes = DEFAULT_NB_END_VOID_NODES;
    protected int nbMultiprojectEndVoidNodes = DEFAULT_NB_MULTIPROJECT_END_VOID_NODES;
    public MutableNodeHierarchy() {
    }


    public static boolean isEvent(int actionType){
    	return (actionType&NodeModel.EVENT)==NodeModel.EVENT;
    }
    public static boolean isUndo(int actionType){
    	return (actionType&NodeModel.UNDO)==NodeModel.UNDO;
    }
//    private void setInSubproject(Node parent,Node node){
//    	if (NodeModelUtil.nodeIsSubproject(parent))
//    		node.setInSubproject(true);
//    	else
//    		node.setInSubproject(parent.isInSubproject());
//    }

    private void setSubprojectLevel(Node node,int level){
    	node.setSubprojectLevel(level);
    	int subprojectLevel=getChildrenSubprojectLevel(node);
    	for (Enumeration e=node.children();e.hasMoreElements();){
    		Node child=(Node)e.nextElement();
    		setSubprojectLevel(child, subprojectLevel);
    	}

    }

    private int getChildrenSubprojectLevel(Node parent){
    	if (parent==null||parent.isRoot()) return 0;
    	if (NodeModelUtil.nodeIsSubproject(parent))
    		return parent.getSubprojectLevel()+1;
    	else
    		return parent.getSubprojectLevel();
    }

    public void add(Node parent,List children,int position,int actionType){
    	Node p=(parent==null)?root:parent;
//    	ArrayList trees=new ArrayList();
//    	extractParents(children,trees);
    	if (/*trees*/children.size()==0) return;

    	int subprojectLevel=getChildrenSubprojectLevel(parent);

    	int childCount=p.getChildCount();
    	if (position>childCount){
    		NodeFactory nodeFactory=NodeFactory.getInstance();
    		for (int i=childCount;i<position;i++){
    			Node node=nodeFactory.createVoidNode();
    			setSubprojectLevel(node,subprojectLevel);
    			p.add(node);
    		}
    	}

    	int j=position;
       	for (Iterator i=/*trees*/children.iterator();i.hasNext();){
       		Node node=(Node)i.next();
        	//if (node.getImpl() instanceof Task) System.out.println("ADD parent="+parent+":"+(parent==null?"X":parent.isInSubproject())+", node="+node+":"+node.isInSubproject());
			setSubprojectLevel(node,subprojectLevel);
			//if (node.getImpl() instanceof Task) System.out.println("ADD node in sub="+node.isInSubproject());
    		if (position==-1) p.add(node);
			else p.insert(node,j++);
       	}
    	if (isEvent(actionType)){
    		renumber();
    		fireNodesInserted(this,addDescendants(children/*trees*/));
    	}
    }

    public void paste(Node parent,List children,int position,NodeModel model,int actionType){
    	Node p=(parent==null)?root:parent;

    	Project project=null;
    	ResourcePool resourcePool=null;
    	if (model.getDataFactory() instanceof Project)
        	project=(Project)model.getDataFactory();
    	else if(model.getDataFactory() instanceof ResourcePool)
        	resourcePool=(ResourcePool)model.getDataFactory();

    	int subprojectLevel=getChildrenSubprojectLevel(parent);


//    	ArrayList trees=new ArrayList();
//    	HierarchyUtils.extractParents(children,trees);

    	int childCount=p.getChildCount();
    	if (position>childCount){
    		NodeFactory nodeFactory=NodeFactory.getInstance();
    		Node node=nodeFactory.createVoidNode();
    		for (int i=childCount;i<position;i++){
    			setSubprojectLevel(node,subprojectLevel);
    			p.add(node);
    		}
    	}



    	int j=position;
       	for (Iterator i=/*trees*/children.iterator();i.hasNext();){
       		Node node=(Node)i.next();
       		if ((project!=null && node.getImpl() instanceof Task)||
       				(resourcePool!=null && node.getImpl() instanceof Resource)||
       				node.isVoid()){
	        	//if (node.getImpl() instanceof Task) System.out.println("PASTE parent="+parent+":"+(parent==null?"X":parent.isInSubproject())+", node="+node+":"+node.isInSubproject());
    			setSubprojectLevel(node,subprojectLevel);
				//if (node.getImpl() instanceof Task) System.out.println("PASTE node in sub="+node.isInSubproject());
	    		if (position==-1) p.add(node);
			else p.insert(node,j++);
       		}
       	}
       	Node[] descendants=addDescendants(/*trees*/children);


       	ArrayList<Dependency> dependencies=new ArrayList<Dependency>();

    	boolean doTransaction = model.getDocument() != null && descendants.length > 0;
    	int transactionId = 0;
    	if (doTransaction)
    		transactionId = model.getDocument().fireMultipleTransaction(0,true);


    	ArrayList<Node> insertedNodes=new ArrayList<Node>(descendants.length);

    	if (project!=null){
    		HashMap<Long,Resource> resourceMap=new HashMap<Long,Resource>();
    		for (Resource r: (Collection<Resource>)project.getResourcePool().getResourceList())
    			resourceMap.put(r.getUniqueId(),r);

    		HashMap<Long,Task> taskMap=null;
    		if (Environment.isKeepExternalLinks()){
    			taskMap=new HashMap<Long,Task>();
    			for (Task t: (Collection<Task>)project.getTasks()) //use model instead?
    				taskMap.put(t.getUniqueId(),t);
    		}

    		Project owningProject;
    		if (parent!=null && parent.getImpl() instanceof Task){
    			Task task=(Task)parent.getImpl();
    			if (task.isSubproject()) owningProject=((SubProj)task).getSubproject();
    			else owningProject=task.getOwningProject();
    		}else owningProject=(Project)model.getDataFactory();

        	for (int i=0;i<descendants.length;i++){
        		if (descendants[i].getImpl() instanceof Task){
        			Task task=(Task)descendants[i].getImpl();

        			Node parentSubproject=getParentSubproject((Node)descendants[i].getParent());
        			if (parentSubproject!=null) owningProject=((SubProj)parentSubproject.getImpl()).getSubproject();
        			if (!task.isExternal()) // fixes  subproject bug with external links
        				task.setProjectId(owningProject.getUniqueId()); //useful?
        			owningProject.validateObject(task,model,this,null,false);

        			Set<Dependency> depsSet=new HashSet<Dependency>();
    				List pdeps=task.getDependencyList(true);
    				if (pdeps!=null&&pdeps.size()>0){
    					if (Environment.isKeepExternalLinks()){
    						for (Iterator k=pdeps.iterator();k.hasNext();){
    							Dependency d=(Dependency)k.next();
    							if (!(d.getPredecessor() instanceof Task)){
    								TaskLinkReference ref=(TaskLinkReference)d.getPredecessor();
    								Task t=taskMap.get(ref.getUniqueId());
    								if (t==null){
    									k.remove();
    									continue;
    								} else{
    									d.setPredecessor(t);
    									t.getSuccessorList().add(d);
    									//DependencyService.getInstance().updateSentinels(d);
    									//DependencyService.getInstance().connect(d, this);
    								}
    							}
    							depsSet.add(d);
    						}
   						}
    				}
    				List sdeps=task.getDependencyList(false);
    				if (sdeps!=null&&sdeps.size()>0){
    					if (Environment.isKeepExternalLinks()){
    						for (Iterator k=sdeps.iterator();k.hasNext();){
    							Dependency d=(Dependency)k.next();
    							if (!(d.getSuccessor() instanceof Task)){
    								TaskLinkReference ref=(TaskLinkReference)d.getSuccessor();
    								Task t=taskMap.get(ref.getUniqueId());
    								if (t==null){
    									k.remove();
    									continue;
    								} else{
    									d.setSuccessor(t);
    									t.getPredecessorList().add(d);
    									//DependencyService.getInstance().updateSentinels(d);
    									//DependencyService.getInstance().connect(d, this);
    								}
    							}
    							depsSet.add(d);
    						}
   						}
    				}
					dependencies.addAll(depsSet);

        			//check assignments, if resource not present change it to unassigned

        			for (int s=0;s<Settings.numBaselines();s++){
        				TaskSnapshot snapshot=(TaskSnapshot)task.getSnapshot(new Integer(s));
        		        if (snapshot==null) continue;
    			        AssociationList snapshotAssignments=snapshot.getHasAssignments().getAssignments();
    			        if (snapshotAssignments.size()>0){
//    			        	ArrayList<Assignment> assignmentsToLink=new ArrayList<Assignment>();
    			            for (Iterator a=snapshotAssignments.listIterator();a.hasNext();){
    			                Assignment assignment=(Assignment)a.next();
    			                Resource resource=assignment.getResource();
            					if (resource==ResourceImpl.getUnassignedInstance()) continue;
            					Resource destResource=resourceMap.get(resource.getUniqueId());
            					if (destResource!=null){
                					if (Snapshottable.CURRENT.equals(s)){
                						if (destResource!=resource){ // use destination resource
                							resource=destResource;
                							assignment.getDetail().setResource(resource);
                						}
//                						assignmentsToLink.add(assignment);
                						resource.addAssignment(assignment);
                						NodeUndoInfo undo=new NodeUndoInfo(false);
                						((ResourcePool)assignment.getResource().getDocument()).getObjectEventManager().fireCreateEvent(this,assignment,undo);
                					}
            					}else{
            						assignment.getDetail().setResource(ResourceImpl.getUnassignedInstance());
            					}
    			            }
//    			            for (Assignment assignment: assignmentsToLink){
//    			            	AssignmentService.getInstance().remove(assignmentsToLink, this,false);
//    			            	AssignmentService.getInstance().connect(assignment, this);
//    			            }


    			        }
        			}

    	    		project.initializeId(task);
    	    		project.addPastedTask(task);

    	    		insertedNodes.add(descendants[i]);
        		}
        	}
    	}else if (resourcePool!=null){
        	for (int i=0;i<descendants.length;i++){
        		if (descendants[i].getImpl() instanceof Resource){
        			Resource resource=(Resource)descendants[i].getImpl();
        			model.getDataFactory().validateObject(resource,model,this,null,false);
        			resourcePool.initializeId(resource);
    	    		insertedNodes.add(descendants[i]);
        		}
        	}
    	}

    	 // added april 16 2008 in the hopes it cures the "Calendar value too large for accurate calculations" bug
    	if (project != null)
    		project.getSchedulingAlgorithm().markBoundsAsDirty();

        if (doTransaction)
        	model.getDocument().fireMultipleTransaction(transactionId,false);


       	if (isEvent(actionType)){
       		renumber();
       		fireNodesInserted(this,insertedNodes.toArray(new Node[insertedNodes.size()]));

       		//not necessary in case of subproject paste
       		for (Dependency dependency : dependencies){
       			DependencyService.getInstance().updateSentinels(dependency); //needed?
				dependency.fireCreateEvent(this);
       		}
       	}
    }

    private Node getParentSubproject(Node node){
    	if (node.isRoot()) return null;
    	else if (node.getImpl() instanceof SubProj) return node;
    	else return getParentSubproject((Node)node.getParent());
    }

//    public void add(Node parent,Node child,int actionType){
//    	add(parent,child,-1,actionType);
//    }
//    public void add(Node parent,List children,int actionType){
//    	add(parent,children,-1,actionType);
//    }
//
//    public void add(Node parent,Node child,int position,int actionType){
//    	LinkedList children=new LinkedList();
//    	children.add(child);
//    	add(parent,children,position,actionType);
//    }

    public void cleanVoidChildren(){
    	cleanVoidChildren(root);
    }
    private void cleanVoidChildren(Node node){
    	for (Iterator i=node.childrenIterator();i.hasNext();){
    		Node child=(Node)i.next();
    		if (child.isVoid()) i.remove();
    		else cleanVoidChildren(child);
    	}
    }


//utility
    public static void addDescendants(Node node,List descendants){
    	for (Enumeration e=((NodeBridge)node).preorderEnumeration();e.hasMoreElements();)
    		descendants.add(e.nextElement());
    }
    //nodes are roots of trees
    public static  void addDescendants(List nodes,List descendants){
    	for (Iterator i=nodes.iterator();i.hasNext();)
    		addDescendants((Node)i.next(),descendants);
    }

    //warning: modify nodes list
    private static Node[] addDescendants(List nodes){
    	ArrayList descendants=new ArrayList();
       	for (ListIterator i=nodes.listIterator();i.hasNext();){
       		Node node=(Node)i.next();
       		extractSameProjectBranch(node,descendants);
//       		boolean rootNode=true;
//        	for (Enumeration e=((NodeBridge)node).preorderEnumeration();e.hasMoreElements();rootNode=false){
//        		Node current=(Node)e.nextElement();
//        		if (!rootNode) descendants.add(current);
//        	}
       	}
    	Node[] descendantsArray=(Node[]) descendants.toArray(new Node[descendants.size()]);
    	return descendantsArray;
    }

    private static void extractSameProjectBranch(Node parent,ArrayList descendants){
//    	if (parent.getImpl() instanceof Subproject){
//    		((NodeBridge)parent).removeAllChildren();
//    		Subproject subproject=(Subproject)parent.getImpl();
//    		//subproject.setProject(null);
//
//    	}else{
    	descendants.add(parent);
	    	for (Enumeration e=parent.children();e.hasMoreElements();){
	    		Node current=(Node)e.nextElement();
	    		extractSameProjectBranch(current,descendants);
	    	}
//    	}
    }



 /**
  * Remove nodes.  This will wrap the call in a multiple transaction if there are many calls so as
  * not to recalculate each time.  In case end void nodes were removed, they will be put back
  *
  */
    public void remove(List nodes,NodeModel model,int actionType,boolean removeDependencies){
        if (nodes!=null) {

        	boolean doTransaction = model.getDocument() != null && nodes.size() > 0 && isEvent(actionType);
        	int transactionId = 0;
        	if (doTransaction)
        		transactionId = model.getDocument().fireMultipleTransaction(0,true);
 	        ArrayList removed=new ArrayList();
 		    for (Iterator i=nodes.iterator();i.hasNext();){
 		        	LinkedList toRemove=new LinkedList();
 		            removeSubTree((Node)i.next(),model,toRemove,actionType, removeDependencies);
 		            removed.addAll(toRemove);
	        }
	    	if (isEvent(actionType)){
	    		renumber();
	    		fireNodesRemoved(this,removed.toArray());
	    	}
	        if (doTransaction)
	        	model.getDocument().fireMultipleTransaction(transactionId,false);
        }
    }
//    public void remove(Node node,NodeModel model,int actionType){
//    	LinkedList removed=new LinkedList();
//    	removeNoEvent(node,model,removed,actionType);
//    	fireNodesRemoved(this,removed.toArray());
//    }

//    private void removeNoEvent(Node node,NodeModel model,LinkedList toRemove,int actionType){
//
//    	System.out.println("removeNoEvent("+node+")");
//    	//if (!isEvent(actionType)) return;
//    	//node.removeFromParent();
//        Node current;
//        int badCount = 0;
//        LinkedList enumeratedNodes=new LinkedList();
//    	for (Enumeration e=((NodeBridge)node).postorderEnumeration();e.hasMoreElements();){
//    		enumeratedNodes.add(e.nextElement());
//    	}
//		System.out.println("removeApartFromHierarchy("+enumeratedNodes+")");
//    	for (Iterator i=enumeratedNodes.iterator();i.hasNext();){
//    		current=(Node)i.next();
//            if (model.removeApartFromHierarchy(current,actionType))
//            	toRemove.add(current);
//            else
//            	badCount++;
//    	}
////    	for (Enumeration e=((NodeBridge)node).postorderEnumeration();e.hasMoreElements();){
////    		current=(Node)e.nextElement();
////    		System.out.println("removeApartFromHierarchy("+current+")");
////            if (model.removeApartFromHierarchy(current,actionType))
////            	toRemove.add(current);
////            else
////            	badCount++;
////    	}
////    	if (badCount == 0) // if no errors, the
//        	node.removeFromParent();
//
//
//
////         	if (undo){
////				//Undo
////	        	NodeHierarchyVoidLocation location=new NodeHierarchyVoidLocation(new NodeHierarchyLocation(parent,previous),1);
////				UndoableEditSupport undoableEditSupport=model.getUndoableEditSupport();
////				if (undoableEditSupport!=null){
////					undoableEditSupport.postEdit(new NodeDeletionEdit(model,location,node));
////				}
////         	}
//
//
//    	//fireNodesRemoved(this,toRemove.toArray());
//  }


    private void removeSubTree(Node node,NodeModel model,LinkedList toRemove,int actionType,boolean removeDependencies){
//    	System.out.println("removeSubTree");
		if (getUpdateLevel()==0){
			//boolean singleRemoval=!(node.getImpl() instanceof Assignment);
	    	try {
				node.removeFromParent();
	    		/*if (singleRemoval)*/ beginUpdate();
//				System.out.println("removeNoEvent("+node+")");
				Node current;
				int badCount = 0;
				LinkedList enumeratedNodes=new LinkedList();
				for (Enumeration e=((NodeBridge)node).postorderEnumeration();e.hasMoreElements();){
					enumeratedNodes.add(e.nextElement());
				}
//				System.out.println("removeApartFromHierarchy("+enumeratedNodes+")");
				for (Iterator i=enumeratedNodes.iterator();i.hasNext();){
					current=(Node)i.next();
				    if (model.removeApartFromHierarchy(current,false,actionType,removeDependencies))
				    	toRemove.add(current);
				    else
				    	badCount++;
				}
				node.removeFromParent();
			} finally{
				/*if (singleRemoval)*/ endUpdate();
			}
		}
   }


	public void removeAll(NodeModel model,int actionType){
		remove(buildList(),model,actionType,true);
	}

    public void move(Node node,Node newParent,int actionType){
		setSubprojectLevel(node,getChildrenSubprojectLevel(newParent));
    	newParent.add(node);
    	ArrayList change=new ArrayList();
    	for (Enumeration e=((NodeBridge)node).preorderEnumeration();e.hasMoreElements();)
    		change.add(e.nextElement());

    	if (isEvent(actionType)) fireNodesChanged(this,change.toArray());
    }



//indentation
//    public void indent(Node node,int deltaLevel,int actionType){
//    	internalIndent(node,deltaLevel,actionType);
//    }
    public void indent(List nodes,int deltaLevel, NodeModel model,int actionType){
    	boolean doTransaction = model.getDocument() != null;
    	int transactionId = 0;
    	if (doTransaction)
    		transactionId = model.getDocument().fireMultipleTransaction(0,true);
        List changedParents=internalIndent(nodes,deltaLevel,actionType);
       	if (doTransaction)
    		model.getDocument().fireMultipleTransaction(transactionId,false);
		if (model.getUndoableEditSupport()!=null&isUndo(actionType)&&changedParents!=null&&changedParents.size()>0){
			model.getUndoableEditSupport().postEdit(new NodeIndentEdit(model,changedParents,deltaLevel));
		}
    }


	//nodes have to be ordered from first to last
    private List internalIndent(List nodes,int deltaLevel,int actionType){
        if (deltaLevel!=1&&deltaLevel!=-1) return null;

        //Indent only parents
        LinkedList nodesToChange=new LinkedList();
        HierarchyUtils.extractParents(nodes,nodesToChange);

        List modifiedVoids=new ArrayList();

        //exclude Assignments and VoidNodes
        if (deltaLevel>0){
	        for (ListIterator i=nodesToChange.listIterator();i.hasNext();){
	        	if (!internalIndent((Node)i.next(),deltaLevel,actionType&NodeModel.UNDO,modifiedVoids))
	        		i.remove();
	        	for (Iterator j=modifiedVoids.iterator();j.hasNext();){
	        		i.add(j.next());
	        	}
	        	modifiedVoids.clear();
	        }
        }else{
	        for (ListIterator i=nodesToChange.listIterator(nodesToChange.size());i.hasPrevious();){
	        	if (!internalIndent((Node)i.previous(),deltaLevel,actionType&NodeModel.UNDO,modifiedVoids))
	        		i.remove();
	        	for (Iterator j=modifiedVoids.iterator();j.hasNext();){
	        		i.add(j.next());
	        	}
	        	modifiedVoids.clear();
	        }
        }

		if (isEvent(actionType)&&nodesToChange.size()>0)
			fireNodesChanged(this,nodesToChange.toArray());
		return nodesToChange;
    }


   private boolean internalIndent(Node node,int deltaLevel,int actionType,List modifiedVoids){ //only +1 -1
    	if (node==null||node==root||!node.isIndentable(deltaLevel)) return false;
   		if (deltaLevel==1){ //indent
       		Node parent=getParent(node);
        	int index=parent.getIndex(node);
        	if (index==0) return false;
        	Node sibling;
        	Node previous=null;

        	for (ListIterator i=parent.childrenIterator(index);i.hasPrevious();){
        		sibling=(Node)i.previous();
        		if (node.canBeChildOf(sibling)){
        			previous=sibling;
        			break;
        		} else if (sibling.isVoid()){
        			modifiedVoids.add(sibling);
        		}
        	}
        	if (previous==null||previous.getImpl() instanceof Assignment){

        		return false;
        	}
        	for (Iterator i=modifiedVoids.iterator();i.hasNext();){
        		previous.add((Node)i.next());
        	}
        	previous.add(node);
    		if (isEvent(actionType)) fireNodesChanged(this,new Node[]{node});
    	}else if (deltaLevel==-1){ //outdent
       		Node parent=getParent(node);
       		if (parent==null || parent==root) return false;
       		if (parent.isLazyParent()) // don't allow outdenting of subprojects' children
       			return false;
       		Node grandParent=getParent(parent);

       		//voids
       		int index=parent.getIndex(node);
       		if (index>0){
       			Node sibling;
            	for (ListIterator i=parent.childrenIterator(index);i.hasPrevious();){
            		sibling=(Node)i.previous();
            		if (sibling.isVoid()){
            			modifiedVoids.add(sibling);
            		} else break;
            	}
       		}

       		index=grandParent.getIndex(parent)+1;
      		grandParent.insert(node,index);
        	for (Iterator i=modifiedVoids.iterator();i.hasNext();){
          		grandParent.insert((Node)i.next(),index);
        	}
    		if (isEvent(actionType)) fireNodesChanged(this,new Node[]{node});
    	}
    	return true;
    }


   public void fireUpdate(){
		fireStructureChanged(this);
   }
   public void fireUpdate(Node[] nodes){
   	fireNodesChanged(this,nodes);
   }
   public void fireInsertion(Node[] nodes){
   	fireNodesInserted(this,nodes);
   }
   public void fireRemoval(Node[] nodes){
   	fireNodesRemoved(this,nodes);
   	}














    public Node getParent(Node child){
    	if (child==null) return null;
        return (Node)child.getParent();
    }
    public List getChildren(Node parent){
           return (parent==null)?root.getChildren():parent.getChildren();
    }

    public int getLevel(Node node){
    	int level=0;
    	for (Node current=node;current!=null;current=getParent(current)) level++;
    	return level-1;
    }


    private List buildList() {
         return buildList(null);
   }
    private List buildList(Node parent) {
        List list=new ArrayList();
        buildList(parent,list);
        return list;
   }
    private void buildList(Node parent, List list) {
    	Node p=(parent==null)?root:parent;
        if (p!=root) list.add(p);
    	Collection children=getChildren(p);
    	if (children!=null) {
        	for (Iterator i=children.iterator();i.hasNext();) {
        		buildList((Node)i.next(),list);
        	}
    	}
    }
//
//    private Iterator iterator(){
//        return buildList(null).iterator();
//    }

    private Node searchPrevious(Node node){
        if (node==null||node==root) return null;
        Node previous=getParent(node);
    	Collection children=getChildren(previous);
    	if (children == null)
    		return null;
        for (Iterator i=children.iterator();i.hasNext();) {
        	Node currentNode=(Node)i.next();
        	if (currentNode.equals(node)) return previous;
        	previous=currentNode;
        }
        return null;
    }

    private Node searchLast(Node node){
        Node current=(node==null)?root:node;
        while (!isLeaf(current)){
            List children=(List)getChildren(current);
            current=(Node)children.get(children.size()-1);
        }
        return current;
    }
    private Node searchLast(int level){
        Node current=null;
        for (int l=0;!isLeaf(current)&&l<level;l++){
            List children=(List)getChildren(current);
            current=(Node)children.get(children.size()-1);
        }
        return current;
    }

	private boolean contains(Object node){
	    //return parents.containsKey(node)||children.containsKey(node);
		Alert.error("contains not implemented");
		return false;
	}


    public Object clone(){
    		Alert.error("clone not implemented");
    		return null;
            //return new MutableNodeHierarchy((HashMap)parents.clone(),(MultiHashMap)children.clone(),(HashMap)voidNodes.clone());
    }








    public Node search(Object key, Comparator c) {
 //   	System.out.println("search("+key+", "+c+")");
    	return search(root,key,c);
    }

    private Node search(Node node, Object key, Comparator c) {
    	if (c.compare((node==null)?root:node,key) == 0)
    		return node;
    	Collection children = getChildren(node);
    	if (children == null)
    		return null;
    	Iterator i = children.iterator();
    	while (i.hasNext()) {
    		Node found = search((Node)i.next(),key,c);
    		if (found != null)
    			return found;
    	}
    	return null;
    }














	public boolean isSummary(Node node){
		List children=getChildren(node);
		if (children==null) return false;
		for (Iterator i=children.iterator();i.hasNext();){
			if (!(((Node)i.next()).getImpl() instanceof Assignment))
				return true;
		}
	    return false;
	}


// Below is TreeModel implementation
    public Object getRoot() {
		return root;
	}

	public boolean isLeaf(Object node) {
    	Collection children = getChildren((Node)node);
    	return (children == null || children.size() == 0);
	}

    protected transient EventListenerList listenerList = new EventListenerList();

    /**
     * Adds a listener for the TreeModelEvent posted after the tree changes.
     *
     * @see     #removeTreeModelListener
     * @param   l       the listener to add
     */
    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    /**
     * Removes a listener previously added with <B>addTreeModelListener()</B>.
     *
     * @see     #addTreeModelListener
     * @param   l       the listener to remove
     */
    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
    	Node aNode = (Node)path.getLastPathComponent();
    	//TODO do we need to treat this?
	}

	protected boolean checkSubprojectEndVoidNodes(Node parent,List inserted){
		Node node;
		int count=0;
		boolean found=false;
		for (Enumeration e=parent.children();e.hasMoreElements();){
			node=(Node)e.nextElement();
			if (checkSubprojectEndVoidNodes(node,inserted)) found=true;
	    	if (NodeModelUtil.nodeIsSubproject(parent)) {
				if (node.isVoid()) count++;
				else count=0;
			}
		}
		if (NodeModelUtil.nodeIsSubproject(parent)){
			int nbEndVoids=nbMultiprojectEndVoidNodes;
			if (parent.getImpl() instanceof SubProj){
				Project s=((SubProj)parent.getImpl()).getSubproject();
				if (s!=null&&s.isReadOnly()) nbEndVoids=0; //don't add end void nodes for read-only subprojects
			}
			if ( count<nbEndVoids){
				LazyParent sub=(LazyParent)parent.getImpl();
				if (sub.isDataFetched()){
					int subprojectLevel=getChildrenSubprojectLevel(parent);
					for (int i=0;i<nbMultiprojectEndVoidNodes-count;i++){
						node=NodeFactory.getInstance().createVoidNode();
						setSubprojectLevel(node,subprojectLevel);
						//node.setInSubproject(true);
						parent.add(node);
						inserted.add(node);
					}
				}
			}
		}
		return found;
	}


	public void checkEndVoidNodes(int actionType){
		checkEndVoidNodes(false,actionType);
	}
	/**
	 * Check if void nodes have to be added to respect nbEndVoidNodes
	 * @param event
	 */
	public void checkEndVoidNodes(boolean subproject,int actionType){
		ArrayList inserted=new ArrayList();

		if (!subproject) checkSubprojectEndVoidNodes(root,inserted);
		int nbEndVoids=subproject?nbMultiprojectEndVoidNodes:nbEndVoidNodes;
		//int nbEndVoids=nbEndVoidNodes;
		int count=0;
		Node node;
		for (ListIterator i=root.childrenIterator(root.getChildCount());i.hasPrevious();){
			node=(Node)i.previous();
			if (node.isVoid()) count++;
			else break;
		}
		if (count<nbEndVoids){

			for (int i=0;i<nbEndVoids-count;i++){
				node=NodeFactory.getInstance().createVoidNode();
				root.add(node);
				inserted.add(node);
			}
		} else if (count > nbEndVoids) { // remove void nodes if they shouldt be there
			int removeCount = count -  nbEndVoids;
			for (ListIterator i=root.childrenIterator(root.getChildCount());i.hasPrevious();){
				node=(Node)i.previous();
				if (node.isVoid()) {
					i.remove();
					removeCount--;
					if (removeCount == 0)
						break;
				} else {
					break;
				}
			}

		}
		if (isEvent(actionType)&&inserted.size()>0) fireNodesInserted(this,inserted.toArray()/*null*/);
	}

	/**
	 * @return Returns the nbEndVoidNodes.
	 */
	public int getNbEndVoidNodes() {
		return nbEndVoidNodes;
	}
	/**
	 * @param nbEndVoidNodes The nbEndVoidNodes to set.  If -1, then use default
	 */
	public void setNbEndVoidNodes(int nbEndVoidNodes) {
		if (nbEndVoidNodes == -1)
			nbEndVoidNodes = DEFAULT_NB_END_VOID_NODES;
		this.nbEndVoidNodes = nbEndVoidNodes;
	}


}
