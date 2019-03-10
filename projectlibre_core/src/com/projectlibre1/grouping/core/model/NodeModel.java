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
package com.projectlibre1.grouping.core.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.TreeModel;
import javax.swing.undo.UndoableEditSupport;

import com.projectlibre1.field.Field;
import com.projectlibre1.field.FieldContext;
import com.projectlibre1.field.FieldParseException;
import com.projectlibre1.grouping.core.Node;
import com.projectlibre1.grouping.core.hierarchy.NodeHierarchy;
import com.projectlibre1.undo.UndoController;

/**
 *
 */
public interface NodeModel extends TreeModel, WalkersNodeModel{
	public static int EVENT=1;
	public static int UNDO=2;

	public static int SILENT=0;
	public static int NORMAL=3;

	//Node structure modification
	public void add(Node child,int actionType);
	public void add(Node parent,Node child,int position,int actionType);
	public void add(Node parent,Node child,int actionType);
	public void add(Node parent,List children,int actionType);
	public void add(Node parent,List children,int position,int actionType);
	public void addBefore(LinkedList siblings,Node newNode,int actionType);
	public void addBefore(Node sibling,Node newNode,int actionType);
	public void addBefore(Node sibling,List newNodes,int actionType);
	public void addImplCollection(Node parent, Collection collection,int actionType);
	public Node newNode(Node parent,int position,int actionType);

	public void paste(Node parent,List nodes,int position,int actionType);

	public void remove(Node node,int actionType);
	public void remove(List nodes,int actionType);
	public void remove(Node node,int actionType,boolean removeDependencies);
	public void remove(List nodes,int actionType,boolean removeDependencies);
	public void removeAll(int actionType);
	//internal
	public boolean removeApartFromHierarchy(Node node,boolean cleanAssignment,int actionType,boolean removeDependencies);
	boolean confirmRemove(List nodes);
	public List cut(List nodes,int actionType);
	public List copy(List nodes,int actionType);

	public void move(Node parent,List nodes,int position,int actionType);


	//Node implementation or field modifications
	public void setFieldValue(Field field, Node node, Object eventSource, Object value, FieldContext context,int actionType) throws FieldParseException;
	public Node replaceImplAndSetFieldValue(Node node, LinkedList previous, Object newImpl, Field field, Object eventSource, Object value, FieldContext context,int actionType) throws FieldParseException;
	public Node replaceImplAndSetFieldValue(Node node, LinkedList previous, Field field, Object eventSource, Object value, FieldContext context,int actionType) throws FieldParseException;
	public Node replaceImpl(Node node, Object nodeImpl, Object eventSource,int actionType);



	public boolean hasChildren(Node node);
	public boolean isSummary(Node node);

	public Iterator iterator();
	public Iterator iterator(Node rootNode);
	public Iterator shallowIterator(int maxLevel,boolean returnRoot);
	public NodeHierarchy getHierarchy();
	public void setHierarchy(NodeHierarchy hierarchy);

	public Object clone();
	public Node search(Object key, Comparator c);
	public Node search(Object key);



	public NodeModelDataFactory getDataFactory();
	public void setDataFactory(NodeModelDataFactory dataFactory);

	//shortcut used by walkers
	public List getChildren(Node parent);
	public Node getParent(Node child);

	public void setUndoController(UndoController undoController);
	public UndoController getUndoController();
	public UndoableEditSupport getUndoableEditSupport();

	public boolean isLocal();
	public void setLocal(boolean local);
	public boolean isMaster();
	public void setMaster(boolean master);

}
