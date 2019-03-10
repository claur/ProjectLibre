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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.Closure;

import com.projectlibre1.grouping.core.Node;
import com.projectlibre1.grouping.core.hierarchy.AbstractMutableNodeHierarchy;
import com.projectlibre1.grouping.core.summaries.DeepChildWalker;
import com.projectlibre1.pm.assignment.Assignment;
import com.projectlibre1.pm.key.HasId;
import com.projectlibre1.pm.task.Task;

public class NodeModelUtil {
	static class NonAssignmentEnumerator implements Closure {
		int count = 0;

		public void execute(Object node) {
			if (node == null)
				return;
			Object impl = ((Node) node).getImpl();
			if (impl != null && !(impl instanceof Assignment)) {
				((HasId) impl).setId(++count);
			}
		}

	}
    public static boolean nodeIsSubproject(Node node) {
   		Object impl =node.getImpl();
    	return impl instanceof Task && ((Task)impl).isSubproject(); 
    }

	public static void enumerateNonAssignments(NodeModel model) {
		DeepChildWalker.recursivelyTreatBranch(model, null, new NonAssignmentEnumerator());
	}

	public static void dump(NodeModel model) {
		((AbstractMutableNodeHierarchy) model.getHierarchy()).dump();
	}

	public static void dumpTask(NodeModel nodeModel) {
		dumpTask(nodeModel, null, "");
	}

	private static void dumpTask(NodeModel nodeModel, Node parent, String indent) {
		if (parent != null)
			System.out.println(indent + ">" + parent.toString());
		Collection children = nodeModel.getChildren(parent);
		if (children != null) {
			Iterator i = children.iterator();
			while (i.hasNext()) {
				Node n = (Node) i.next();
				Object impl = n.getImpl();
				if (impl instanceof Task) {
					if (((Task) impl).getWbsParentTask() != (parent == null ? null : parent.getImpl()))
						System.out.println("cached hierarchy error - child " + impl + " cached parent" + ((Task) impl).getWbsParentTask()
								+ " parent " + parent.getImpl());
				}
				dumpTask(nodeModel, n, indent + "--");
			}
		}
	}

	public static List extractNodeList(NodeModel nodeModel, Node root) {
		ArrayList l = new ArrayList();
		extractNodeList(nodeModel, root, l);
		return l;
	}

	private static void extractNodeList(NodeModel nodeModel, Node parent, Collection result) {
		if (parent != null)
			result.add(parent);
		Collection children = nodeModel.getChildren(parent);
		if (children != null) {
			Iterator i = children.iterator();
			while (i.hasNext()) {
				Node n = (Node) i.next();
				extractNodeList(nodeModel, n, result);
			}
		}
	}

	public static void cacheWbs(NodeModel nodeModel, Node parentNode) {
		Object parentImpl = parentNode.getImpl();
		List children = nodeModel.getChildren(parentNode);
		if (parentImpl instanceof Task && children != null && children.size() > 0) {
			Task parent = (Task) parentImpl;
			parent.setWbsChildrenNodes(children); // cached values
			Node child;
			for (Iterator i = children.iterator(); i.hasNext();) {
				child = (Node) i.next();
				Object impl = child.getImpl();
				if (impl instanceof Task) {
					((Task) impl).setWbsParent(parent); // set cached wbs parent
														// too
					cacheWbs(nodeModel, child);
				}
			}
		}
	}

	public static boolean canBeChildOf(Node parent, Node child) {
		Object parentImpl = parent.getImpl();
		Object childImpl = child.getImpl();
		if (nodeIsSubproject(parent))
			return false;
		if (parentImpl instanceof Task && childImpl instanceof Task) {
			return ((Task)parentImpl).getOwningProject() == ((Task)childImpl).getOwningProject();
		}
		return true;
	}

}
