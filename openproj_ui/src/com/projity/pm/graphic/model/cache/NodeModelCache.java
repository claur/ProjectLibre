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
package com.projity.pm.graphic.model.cache;

import java.util.List;
import java.util.ListIterator;

import javax.swing.tree.TreeModel;

import com.projity.association.InvalidAssociationException;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.WalkersNodeModel;
import com.projity.pm.graphic.model.event.CacheListener;
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
