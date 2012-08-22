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
package com.projity.pm.dependency;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import org.apache.commons.collections.Predicate;

import com.projity.association.InvalidAssociationException;
import com.projity.pm.task.SubProj;
import com.projity.pm.task.Task;
import com.projity.strings.Messages;
import com.projity.undo.DataFactoryUndoController;
import com.projity.undo.DependencyCreationEdit;
import com.projity.undo.DependencyDeletionEdit;
import com.projity.undo.DependencySetFieldsEdit;
import com.projity.util.Alert;
import com.projity.util.ClassUtils;

/**
 * Manages the creation and deleting of dependencies as well as events
 */
public class DependencyService {
	private static DependencyService instance = null;
	public static DependencyService getInstance() {
		if (instance == null)
			instance = new DependencyService();
		return instance;
	}

	public Dependency newDependency(HasDependencies predecessor, HasDependencies successor, int dependencyType, long lead, Object eventSource) throws InvalidAssociationException {
		if (predecessor == successor)
			throw new InvalidAssociationException(Messages.getString("Message.cantLinkToSelf"));
		Task successorTask = (Task)successor;
		if (successorTask.isExternal())
			throw new InvalidAssociationException(Messages.getString("Message.cantLinkToExternal"));
		if (successorTask.isSubproject() && !((SubProj)successorTask).isWritable())
			throw new InvalidAssociationException(Messages.getString("Message.cantLinkToClosedSubproject"));

		Dependency dependency = Dependency.getInstance(predecessor, successor, dependencyType, lead);
		dependency.testValid(false); // throws if exception
		connect(dependency,eventSource);
		dependency.setDirty(true);
		return dependency;
	}
	//for deserialization
	public void initDependency(Dependency dependency,HasDependencies predecessor, HasDependencies successor, Object eventSource) throws InvalidAssociationException {
		dependency.setPredecessor(predecessor);
		dependency.setSuccessor(successor);
		if (!dependency.isDisabled()) // allow for calling a second time once invalidated
			dependency.testValid(false); // throws if exception
		connect(dependency,eventSource);
	}

	public void addStartSentinelDependency(HasDependencies sentinel, HasDependencies successor) {
		Dependency dependency = Dependency.getInstance(sentinel, successor, DependencyType.SS,0);
		sentinel.getSuccessorList().add(dependency);
		//		System.out.println("adding start sentinel dependency task is " + successor);
	}
	public void addEndSentinelDependency(HasDependencies sentinel, HasDependencies predecessor) {
		Dependency dependency = Dependency.getInstance(predecessor, sentinel, DependencyType.FS,0);
		sentinel.getPredecessorList().add(dependency);
	//	System.out.println("adding end sentinel dependency task is " + predecessor);
	}

	public boolean removeEndSentinel(HasDependencies sentinel, HasDependencies task) {
		Dependency dependency;
		dependency = (Dependency) sentinel.getPredecessorList().findLeft(task);
		if (dependency != null) {
			sentinel.getPredecessorList().remove(dependency);
			return true;
	//		System.out.println("removing end sentinel dependency task is " + dependency.getPredecessor());
		}
		return false;
	}
	public boolean removeStartSentinel(HasDependencies sentinel, HasDependencies task) {
		Dependency dependency;
		dependency = (Dependency) sentinel.getSuccessorList().findRight(task);
		if (dependency != null) {
			sentinel.getSuccessorList().remove(dependency);
			return true;
	//		System.out.println("removing start sentinel dependency task is " + dependency.getSuccessor());
		}
		return false;
	}

	public void connect(Dependency dependency, Object eventSource) {
		dependency.getPredecessor().getSuccessorList().add(dependency);
		dependency.getSuccessor().getPredecessorList().add(dependency);
		updateSentinels(dependency);
		if (eventSource != null) {
			dependency.fireCreateEvent(eventSource);
		}
		dependency.setDirty(true);

		UndoableEditSupport undoableEditSupport=getUndoableEditSupport(dependency);
		if (undoableEditSupport!=null&&eventSource!=null&&!(eventSource instanceof UndoableEdit)){
			undoableEditSupport.postEdit(new DependencyCreationEdit(dependency,eventSource));
		}
	}

	public void fireTaskPredecessors(Collection list) {
		Iterator i = list.iterator();
		while (i.hasNext()) {
			Iterator j =((Task)i.next()).getPredecessorList().iterator();
			while (j.hasNext())
				((Dependency)j.next()).fireCreateEvent(this);
		}
	}

	public void remove(Dependency dependency, Object eventSource,boolean undo) {
		dependency.setDirty(true); //for setGroupDirty()
		dependency.getPredecessor().getSuccessorList().remove(dependency);
		dependency.getSuccessor().getPredecessorList().remove(dependency);
		updateSentinels(dependency);

		if (eventSource != null)
			dependency.fireDeleteEvent(eventSource);

		UndoableEditSupport undoableEditSupport=getUndoableEditSupport(dependency);
		if (undo && undoableEditSupport!=null&&!(eventSource instanceof UndoableEdit)){
			undoableEditSupport.postEdit(new DependencyDeletionEdit(dependency,eventSource));
		}


	}
	public void setFields(Dependency dependency, long lag, int type,Object eventSource) throws InvalidAssociationException{

//		if (eventSource != null)
//			dependency.getDocument().getObjectEventManager().fireUpdateEvent(eventSource,dependency);
		long oldLag=dependency.getLag();
		int oldType=dependency.getDependencyType();
		dependency.setLag(lag);
		dependency.setDependencyType(type);
		dependency.setDirty(true);

		UndoableEditSupport undoableEditSupport=getUndoableEditSupport(dependency);
		if (undoableEditSupport!=null&&!(eventSource instanceof UndoableEdit)){
			undoableEditSupport.postEdit(new DependencySetFieldsEdit(dependency,oldLag,oldType,eventSource));
		}

	}

	public void update(Dependency dependency, Object eventSource) {
		if (eventSource != null)
			dependency.fireUpdateEvent(eventSource);
		dependency.setDirty(true);
	}

// update the starting and ending sentinels of the project - the sentinels keep track of which
//	tasks have no preds or no successors
	public void updateSentinels(Dependency dependency) {
		Task predecessor = (Task) dependency.getPredecessor();
		Task successor = (Task) dependency.getSuccessor();
		predecessor.updateEndSentinel();
		successor.updateStartSentinel();
	}

	/**
	 * Connect tasks sequentially.
	 * Circularities will be tested, and an exception thrown if any circularity would occur
	 *
	 * @param tasks
	 * @param eventSource
	 * @throws InvalidAssociationException
	 */
	public void connect(List tasks, Object eventSource, Predicate canBeSuccessorCondition) throws InvalidAssociationException {
		ArrayList newDependencies = new ArrayList();
		// try making new dependencies between all items earlier to all items later, thereby checking all possible circularities
		HasDependencies pred;
		HasDependencies succ;
		Object temp;
		for (int i = 0; i < tasks.size()-1; i++) {
			temp = tasks.get(i);
			if (!(temp instanceof HasDependencies))
				continue;
			pred = (HasDependencies)temp;
			for (int j = i+1; j < tasks.size(); j++) {
				temp = tasks.get(j);
				if (!(temp instanceof HasDependencies))
					continue;
				succ = (HasDependencies)temp;
				if (canBeSuccessorCondition != null && !canBeSuccessorCondition.evaluate(succ)) // allow exclusion of certain nodes that we don't want to be successors
					continue;
				if (succ.getPredecessorList().findLeft(pred) != null) // if dependency already exists, skip it
					continue;
				if (ClassUtils.isObjectReadOnly(succ))
					continue;
				Dependency test = Dependency.getInstance(pred,succ,DependencyType.FS,0); // make a new one
				test.testValid(false); // test for circularity, throws if bad
				if (j == i+1) // only add sequential ones
					newDependencies.add(test);
			}
		}
		Iterator d = newDependencies.iterator();
		while (d.hasNext()) {
			connect((Dependency)d.next(),eventSource);
		}


	}
	/**
	 * Remove all dependencies between all tasks in an array
	 * @param tasks
	 * @param eventSource
	 */
	public void removeAnyDependencies(List tasks, Object eventSource) {
		HasDependencies pred;
		HasDependencies succ;
		Object temp;
		for (int i = 0; i < tasks.size()-1; i++) {
			temp = tasks.get(i);
			if (!(temp instanceof HasDependencies))
				continue;
			pred = (HasDependencies)temp;
			for (int j = i+1; j < tasks.size(); j++) {
				temp = tasks.get(j);
				if (!(temp instanceof HasDependencies))
					continue;
				succ = (HasDependencies)temp;
				removeAnyDependencies(pred,succ,eventSource);
			}
		}
	}
	public void removeAnyDependencies(HasDependencies first, HasDependencies second, Object eventSource) {
		Dependency dependency;
		if (first == null || second == null)
			return;
		if ((dependency = (Dependency) first.getPredecessorList().findLeft(second)) != null)
			remove(dependency,eventSource,true);
		if ((dependency = (Dependency) second.getPredecessorList().findLeft(first)) != null)
			remove(dependency,eventSource,true);
		if ((dependency = (Dependency) first.getSuccessorList().findRight(second)) != null)
			remove(dependency,eventSource,true);
		if ((dependency = (Dependency) second.getSuccessorList().findRight(first)) != null)
			remove(dependency,eventSource,true);
	}

	public void remove(Collection dependencyList, Object eventSource) {
		Dependency dependency;
		Iterator i = dependencyList.iterator();
		while (i.hasNext()) {
			dependency = (Dependency)i.next();
			remove(dependency,eventSource,true);
		}
	}

	//fix
	public void remove(Collection dependencyList, Collection toRemove) {
		Iterator i = dependencyList.iterator();
		while (i.hasNext())
			toRemove.add(i.next());
	}




	//undo
	public UndoableEditSupport getUndoableEditSupport(Dependency dependency) {
		if (dependency.getPredecessor()==null)
			return null;
		else {
			DataFactoryUndoController c = ((Task)dependency.getPredecessor()).getProject().getUndoController();
			if (c == null)
				return null;
			return c.getEditSupport();
		}
	}

	public static String getCircularCrossProjectLinkMessage(Object predecessor, Object successor) {
		return MessageFormat.format(Messages.getString("Message.crossProjectCircularDependency.mf"),new Object[] {predecessor,successor});
	}
	/**
	 * Warn that a cross project link is disabled. This is invoked later to give time for the gantt to redraw first
	 * @param predecessor
	 * @param successor
	 */
	public static void warnCircularCrossProjectLinkMessage(final Object predecessor, final Object successor) {
		if (Alert.allowPopups()) {
			SwingUtilities.invokeLater( new Runnable() {
				public void run() {
		        	Alert.warn(getCircularCrossProjectLinkMessage(predecessor, successor));
				}});
		}
	}
}
