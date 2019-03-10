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
package com.projectlibre1.pm.graphic.model.cache;

import java.util.List;
import java.util.ListIterator;

import javax.swing.tree.TreeModel;

import com.projectlibre1.pm.graphic.model.event.CacheListener;
import com.projectlibre1.association.InvalidAssociationException;
import com.projectlibre1.grouping.core.Node;
import com.projectlibre1.grouping.core.model.NodeModel;
import com.projectlibre1.grouping.core.model.WalkersNodeModel;
/**
 * This class lies between the SpreadSheet and the SpreadSheetModel.
 * It holds the states directly linked to the view.
 * The collapsed state and level of the nodes here.
 * The level is not a view state but it is calculated and cached for performance purposes.
 */

public interface NodeModelCache extends TreeModel{
	public static final int ASSIGNMENT_TYPE=1;
	public static final int TASK_TYPE=2;
	public static final int RESOURCE_TYPE=4;
	public static final int PROJECT_TYPE=8;
	
	public void setType(int type);
	public int getType();

	public NodeModel getModel();
	public WalkersNodeModel getWalkersModel();
	public void setModel(NodeModel model);
	
	public Object getElementAt(int i);
	public ListIterator getIterator();
	public ListIterator getIterator(int i);
//	public static interface CacheClosure{
//		public void execute(GraphicNode node,int deltaLevel);
//	}
	//public void forEach(CacheClosure c);
	public int getMaxLevel();
	public List getElementsAt(int[] i);
	public List getNodesAt(int[] i);
	public int getRowAt(Object obj);
	public Object getEdgeElementAt(int i);
	public ListIterator getEdgesIterator();
	public ListIterator getEdgesIterator(int i);
	public int getSize();
	public int getEdgesSize();
	public GraphicNode getParent(GraphicNode node);
	public List getChildren(GraphicNode node);

	public Object getGraphicNode(Object base);
	public Object getGraphicDependency(Object base);

	
	//public boolean isSummary(GraphicNode node);
	
	public void changeCollapsedState(GraphicNode node);
	public void expandNodes(List nodes, boolean expand);
	
	public void newNode(GraphicNode node);
	
	public void deleteNodes(List nodes);
	public void indentNodes(List nodes);
	public void outdentNodes(List nodes);
	public void cutNodes(List gnodes);
	public void copyNodes(List gnodes);
	public void pasteNodes(Node parent,List nodes,int position);
	public void addNodes(Node sibling,List nodes);

	
	public void createDependency(GraphicNode startNode,GraphicNode endNode) throws InvalidAssociationException;
	public void createHierarchyDependency(GraphicNode startNode,GraphicNode endNode) throws InvalidAssociationException;

	/**
	 * Returns the parent/previous,position identification of the void node at row
	 * Apply this to a void node row only
	 * @param row
	 * @return
	 */
//	public NodeHierarchyVoidLocation getVoidNodeInfoObject(GraphicNode node);
	
	
	
	public void addNodeModelListener(CacheListener l);
	public void removeNodeModelListener(CacheListener l);
	public CacheListener[] getNodeModelListeners();
	
	public void close();
	
	
	public int getLevel(GraphicNode node);
	public int getPertLevel(GraphicNode node);
	public void setPertLevel(GraphicNode node,int level);
	
	
	public void update(); //test only
	
	public ReferenceNodeModelCache getReference();

    public VisibleDependencies getVisibleDependencies();
    public VisibleNodes getVisibleNodes();
    
    public boolean isReceiveEvents();
    public void setReceiveEvents(boolean receiveEvents);
	
}
