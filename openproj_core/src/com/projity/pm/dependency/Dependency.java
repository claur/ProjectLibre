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

import com.projity.association.Association;
import com.projity.association.InvalidAssociationException;
import com.projity.datatype.Duration;
import com.projity.datatype.DurationFormat;
import com.projity.document.Document;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.criticalpath.ScheduleWindow;
import com.projity.pm.task.BelongsToDocument;
import com.projity.pm.task.HasProject;
import com.projity.pm.task.Project;
import com.projity.pm.task.Task;
import com.projity.server.data.DataObject;
import com.projity.strings.Messages;

public class Dependency implements Association, BelongsToDocument, DataObject {
	static final long serialVersionUID = 283794049292031L;
	private int dependencyType;
	private transient HasDependencies predecessor;
	private transient HasDependencies successor;
	private transient boolean disabled = false;
	private long lag;

	private transient long earlyDate;
	private transient long lateDate;
	public static final long NEEDS_CALCULATION = -1;

	public static Dependency getInstance(HasDependencies predecessor,
			HasDependencies successor) {
		return getInstance(predecessor,successor,DependencyType.FS,0);
	}

	public static Dependency getInstance(HasDependencies predecessor,
			HasDependencies successor, int dependencyType, long lead) {
		return new Dependency(predecessor, successor, dependencyType, lead);
	}

	private Dependency(HasDependencies predecessor, HasDependencies successor,
			int dependencyType, long lead) {
		this.predecessor = predecessor;
		this.successor = successor;
		this.dependencyType = dependencyType;
		this.lag = lead;
	}

	public void updateDependencyLists() {
		predecessor.getSuccessorList().add(this);
		successor.getPredecessorList().add(this);
	}


	public HasDependencies getPredecessor() {
		return predecessor;
	}
	public HasDependencies getSuccessor() {
		return successor;
	}


	public long getLag() {
		return lag;
	}

	public void setLag(long lead) {
		this.lag = lead;
	}

	/**
	 * Copy the fields lag and type
	 *
	 */
	public void copyPrincipalFieldsFrom(Association from) {
		this.lag = ((Dependency)from).lag;
		this.dependencyType = ((Dependency)from).dependencyType;

	}

	/**
	 * @return Returns the calendar.
	 */
	public final WorkCalendar getEffectiveWorkCalendar() {
		return predecessor.getHasCalendar().getEffectiveWorkCalendar(); // use
																		// the
																		// predecessor's
																		// calendar
	}

	/**
	 * @param predecessor
	 *            The predecessor to set.
	 */
	public void setPredecessor(HasDependencies predecessor) {
		this.predecessor = predecessor;
	}

    public void setSuccessor(HasDependencies successor) {
        this.successor = successor;
    }
	/**
	 * @return Returns the dependencyType.
	 */
	public int getDependencyType() {
		return dependencyType;
	}

	/**
	 * @param dependencyType
	 *            The dependencyType to set.
	 * @throws InvalidAssociationException
	 */
	public void setDependencyType(int dependencyType) throws InvalidAssociationException {
		if (((Task)getSuccessor()).isWbsParent()) {
			if (dependencyType == DependencyType.FF ||
					dependencyType == DependencyType.SF) {
				throw new InvalidAssociationException(Messages.getString("Message.parentSuccessorCannotHaveFinishLink"));
			}
		}

		this.dependencyType = dependencyType;
	}

	boolean isCircular() {
		return predecessor.dependsOn(successor);
	}

	boolean isLinkToParent() {
		return ((Task)successor).wbsDescendentOf((Task) predecessor);
	}

	boolean isLinkToChild() {
		return ((Task)predecessor).wbsDescendentOf((Task) successor);
	}

    public void testValid(boolean allowDuplicate) throws InvalidAssociationException {
    	if (isLinkToParent() || isLinkToChild())
    		throw new InvalidAssociationException(Messages.getString("Message.cannotLinkToSummary"));

    	if (isCircular())
    		throw new InvalidAssociationException(Messages.getString("Message.circularDependency"));


    	if (!allowDuplicate && predecessor.getSuccessorList().findRight(successor) != null)
    		throw new InvalidAssociationException(Messages.getString("Message.cannotLinkTwice"));
    }


	public Object getLeft() {
		return predecessor;
	}

	/* (non-Javadoc)
	 * @see com.projity.util.Association#getRight()
	 */
	public Object getRight() {
		return successor;
	}

	public void doAddService(Object eventSource) {
		DependencyService.getInstance().connect((Dependency)this,eventSource);
	}

	public void doRemoveService(Object eventSource) {
		DependencyService.getInstance().remove((Dependency)this,eventSource,true);
	}
	public void doUpdateService(Object eventSource) {
		DependencyService.getInstance().update((Dependency)this,eventSource);
	}

	public boolean isDefault() {
		return false;
	}

	public Document getDocument() {
		return ((BelongsToDocument)getSuccessor()).getDocument();
	}

	public boolean refersToDocument(Document document) {
		return ((Task)getSuccessor()).getMasterDocument() == document ||
				((Task)getPredecessor()).getMasterDocument() == document;

	}

	public void fireCreateEvent(Object eventSource) {
		((Task)getSuccessor()).getMasterDocument().getObjectEventManager().fireCreateEvent(eventSource,this);
//		if (isExternal())
//			((Task)getPredecessor()).getMasterDocument().getObjectEventManager().fireCreateEvent(eventSource,this);
	}
	public void fireUpdateEvent(Object eventSource) {
		((Task)getSuccessor()).getMasterDocument().getObjectEventManager().fireUpdateEvent(eventSource,this);
//		if (isExternal())
//			((Task)getPredecessor()).getMasterDocument().getObjectEventManager().fireUpdateEvent(eventSource,this);
	}
	public void fireDeleteEvent(Object eventSource) {
		((Task)getSuccessor()).getMasterDocument().getObjectEventManager().fireDeleteEvent(eventSource,this);
//		if (isExternal())
//			((Task)getPredecessor()).getMasterDocument().getObjectEventManager().fireDeleteEvent(eventSource,this);
	}


	public boolean isExternal() {
		return ((Task)getSuccessor()).getProjectId() != ((Task)getPredecessor()).getProjectId();
	}
	public boolean isCrossProject() {
		return isExternal(); // || ((Task)predecessor).isExternal() || ((Task)successor).isExternal();
	}
	public String toString() {
		return "[predecessor]" + predecessor + " [successor]" + successor;
	}

	public String getPredecessorName() {
		return ((Task)predecessor).getName();
	}

	public String getSuccessorName() {
		return ((Task)successor).getName();
	}

	public String getQualifiedPredecessorName() {
		if (isExternal())
			return ((Task)predecessor).getTaskAndProjectName();
		else
			return predecessor.toString();
	}

	public String getQualifiedSuccessorName() {
		if (isExternal())
			return ((Task)successor).getTaskAndProjectName();
		else
			return successor.toString();
	}

	public long getPredecessorId() {
		return ((Task)predecessor).getUniqueId();
	}

	public long getSuccessorId() {
		return ((Task)successor).getUniqueId();
	}
	public long getPredecessorIdNumber() {
		return ((Task)predecessor).getId();
	}

	public long getSuccessorIdNumber() {
		return ((Task)successor).getId();
	}

	//DataObject
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }
    public void setName(String name) {
        // TODO Auto-generated method stub

    }
    public long getUniqueId() {
        // TODO Auto-generated method stub
        return 0;
    }
    public void setUniqueId(long id) {
        // TODO Auto-generated method stub

    }
    public String getUniqueIdString() {
    	return getPredecessorId() + "." + getSuccessorId();
    }
    transient boolean newId=true;
    public boolean isNew(){
    	return newId;
    }
    public void setNew(boolean newId){
    	this.newId=newId;
    }
	/**
	 * Method to calculate the lead in millis from the lead value stored in the
	 * dependency. In normal cases, it just extracts the milliseconds, but if
	 * the lead is a percentage, then it is calculated based on predecessor
	 * duration. If the value is expressed as elapsed %, then the flag elapsed
	 * is applied to the resulting value. In this case, the rule is that the
	 * lead itself has an elapsed duration which is calculated based on the
	 * tasks (non-elapsed) duration.
	 *
	 * @param dependency
	 * @return lead in milliseconds with elapsed flag set if the dependency is
	 *         elapsed
	 */
	public long getLeadValue() {
		long leadWithUnits = getLag();
		if (Duration.isPercent(leadWithUnits)) {
			long lead = Duration.millis(leadWithUnits);
			float fraction = Duration.getPercentAsDecimal(lead);
			if (Duration.isElapsed(leadWithUnits)) {
				leadWithUnits = (long) (((Task) getPredecessor()).getElapsedDuration() * fraction);
				leadWithUnits = Duration.setAsElapsed(leadWithUnits); // put in elapsed  flag
			} else {
				leadWithUnits = (long) (((Task) getPredecessor()).getDuration() * fraction);
			}
		}
		return leadWithUnits;
	}

	//gets either predecessor or successor
	public HasDependencies getTask(boolean pred) {
		return pred ? predecessor : successor;
	}

	public long calcDependencyDate(boolean forward, long begin, long end, boolean hasDuration) {
		return forward
				? calcForwardDependencyDate(begin,end,hasDuration)
				: calcReverseDependencyDate(begin,end,hasDuration);
	}

	/**
	 * Calc the date that this dependency will cause its successor task (if forward scheduling) or predecessor (if reverse scheduling)
	 * @param begin
	 * @param end
	 * @param duration
	 * @return
	 */
	public long calcForwardDependencyDate(long begin, long end, boolean hasDuration) {
		if (disabled)
			return earlyDate;
		long t = 0;

		boolean canStartAtDayEnd = !hasDuration; // to handle the milestone case
		switch (dependencyType) {
			case DependencyType.FS:
				t = end;
				break;
			case DependencyType.SS:
				t = begin;
				break;
			case DependencyType.FF:
				t = ((ScheduleWindow)successor).calcOffsetFrom(end,end,false,false, canStartAtDayEnd);
				break;
			case DependencyType.SF:
				t = ((ScheduleWindow)successor).calcOffsetFrom(begin,begin,false,false, canStartAtDayEnd);
			break;
		}
		earlyDate = getEffectiveWorkCalendar().add(t,getLeadValue(), canStartAtDayEnd);
		return earlyDate;
	}
	/**
	 * get the latest finish time for the predecessor. The current
	 * ScheduleWindow in the backward pass of cp algo is the predecessor. It is
	 * possible that the milestone handling code needs more work
	 * begin is actually the late finish and end is early finish
	 */
	public long calcReverseDependencyDate(long begin, long end , boolean hasDuration) {
		if (disabled)
			return lateDate;
		long t = 0;
		boolean cannotFinishAtDayStart = !hasDuration; // to handle the milestone case
		switch (getDependencyType()) {
			case DependencyType.FS:
				t = end;
				break;
			case DependencyType.SS:
				t = ((ScheduleWindow)getPredecessor()).calcOffsetFrom(end,end,false,false, cannotFinishAtDayStart);
				break;
			case DependencyType.FF:
				t = begin;
				break;
			case DependencyType.SF:
				t = ((ScheduleWindow)getPredecessor()).calcOffsetFrom(begin,begin,false,false, cannotFinishAtDayStart);
			break;
		}
		lateDate = getEffectiveWorkCalendar().add(t, getLeadValue(), cannotFinishAtDayStart);
		return lateDate;
	}

	public long getDate(boolean early) {
		return early ? earlyDate : lateDate;
	}

	public void setDate(boolean early, long date) {
		System.out.println(this + "setting date to " + new java.util.Date(date));
		if (early)
			earlyDate = date;
		else
			lateDate = date;
	}

	public String htmlString() {
	    StringBuffer s=new StringBuffer();
	    s.append("<html><body>");
	    s.append(Messages.getString("Gantt.tooltip.link")).append(": ");
	    s.append(DependencyType.toLongString(getDependencyType())).append(" ");
	    s.append(DurationFormat.format(getLag())).append("<br>");
	    s.append(Messages.getString("Gantt.tooltip.from")).append(": ");
	    s.append(getQualifiedPredecessorName()).append("<br>");
	    s.append(Messages.getString("Gantt.tooltip.to")).append(": ");
	    s.append(getQualifiedSuccessorName()).append("<br>");
	    s.append("</body></html>");
	    return s.toString();

	}

	//because it implements DataObject, should implement a different interface
	private transient boolean dirty;
	public boolean isDirty() {
		return dirty;
	}
	public void setDirty(boolean dirty) {
		//System.out.println("Dependency _setDirty("+dirty+"): "+getName());
		this.dirty = dirty;
		if (dirty&&predecessor!=null){
			Project project=((HasProject)predecessor).getProject();
			if (project!=null) project.setGroupDirty(true);
		}
	}

	public Document getMasterDocument() {
		return ((Task)getSuccessor()).getMasterDocument();
	}

	public void replace(Object newOne, boolean leftObject) {
		if (leftObject)
			setPredecessor((HasDependencies) newOne);
		else
			setSuccessor((HasDependencies) newOne);
	}

	public final boolean isDisabled() {
		return disabled;
	}

	public final void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

}