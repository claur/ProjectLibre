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
package com.projity.pm.graphic.spreadsheet.renderer;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.projity.configuration.Configuration;
import com.projity.field.Field;
import com.projity.graphic.configuration.HasTaskIndicators;
import com.projity.pm.graphic.IconManager;
import com.projity.pm.task.NormalTask;
import com.projity.pm.task.SubProj;
import com.projity.pm.task.Task;
import com.projity.strings.Messages;
/**
 *  
 */
public class TaskIndicatorsComponent extends IndicatorsComponent{
	private static final long serialVersionUID = 192992920101L;
	protected JLabel calendar;
	protected  JLabel constraint;
	protected JLabel invalidCalendar;
	protected JLabel notes;
	protected JLabel completed;
	protected JLabel empty;
	protected JLabel missedDeadline;
	protected JLabel parentAssignment;
	protected JLabel subproject;
	protected JLabel invalidProject;
	protected JLabel delegated;
	protected JLabel delegatedMe;
	Field constraintTypeField = Configuration.getFieldFromId("Field.constraintType"); //$NON-NLS-1$
	Field constraintDateField = Configuration.getFieldFromId("Field.constraintDate"); //$NON-NLS-1$
	Field deadlineField = Configuration.getFieldFromId("Field.deadline"); //$NON-NLS-1$
	Field finish = Configuration.getFieldFromId("Field.finish"); //$NON-NLS-1$

	public boolean acceptValue(Object value){
		return acceptTask(value);
	}
	public static boolean acceptTask(Object value){
		return value instanceof HasTaskIndicators;
	}

	
	public void init() {
		//empty = new JLabel("");
		calendar = new JLabel(" ",IconManager.getIcon("indicator.calendar"),  JLabel.RIGHT); //$NON-NLS-1$ //$NON-NLS-2$
		calendar.setOpaque(false);
		constraint = new JLabel(" ", IconManager.getIcon("indicator.constraint"),  JLabel.RIGHT); //$NON-NLS-1$ //$NON-NLS-2$
		constraint.setOpaque(false);
		invalidCalendar = new JLabel(" ", IconManager.getIcon("indicator.invalidCalendar"),  JLabel.RIGHT); //$NON-NLS-1$ //$NON-NLS-2$
		invalidCalendar.setOpaque(false);
		notes = new JLabel(" ",IconManager.getIcon("indicator.note"),  JLabel.RIGHT); //$NON-NLS-1$ //$NON-NLS-2$
		notes.setOpaque(false);
		parentAssignment = new JLabel(" ",IconManager.getIcon("indicator.parentAssignment"),  JLabel.RIGHT); //$NON-NLS-1$ //$NON-NLS-2$
		parentAssignment.setOpaque(false);
		completed = new JLabel(" ",IconManager.getIcon("indicator.completed"),  JLabel.RIGHT); //$NON-NLS-1$ //$NON-NLS-2$
		completed.setOpaque(false);
		missedDeadline = new JLabel(" ",IconManager.getIcon("indicator.missedDeadline"),  JLabel.RIGHT); //$NON-NLS-1$ //$NON-NLS-2$
		missedDeadline.setOpaque(false);
		subproject = new JLabel(" ",IconManager.getIcon("indicator.subproject"),  JLabel.RIGHT); //$NON-NLS-1$ //$NON-NLS-2$
		subproject.setOpaque(false);
		invalidProject = new JLabel(" ",IconManager.getIcon("indicator.invalidProject"),  JLabel.RIGHT); //$NON-NLS-1$ //$NON-NLS-2$
		invalidProject.setOpaque(false);
		delegated = new JLabel(" ",IconManager.getIcon("indicator.delegated"),  JLabel.RIGHT); //$NON-NLS-1$ //$NON-NLS-2$
		delegated.setOpaque(false);
		delegatedMe = new JLabel(" ",IconManager.getIcon("indicator.delegatedMe"),  JLabel.RIGHT); //$NON-NLS-1$ //$NON-NLS-2$
		delegatedMe.setOpaque(false);
	}
	
	
	
	public void setIndicators(Object value,JComponent label,StringBuffer text,boolean isSelected, boolean hasFocus){
		HasTaskIndicators indicators = (HasTaskIndicators)value;

		if (indicators.getWorkCalendar() != null) {
			setLook(calendar,isSelected,hasFocus);
			label.add(calendar);
			text.append(Messages.getString("TaskIndicatorsComponent.TheCalendar") + indicators.getWorkCalendar().getName() + Messages.getString("TaskIndicatorsComponent.isAssignedToTheTask")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		long constraintDate = indicators.getConstraintDate();
		if (constraintDate != 0) {
			setLook(constraint,isSelected,hasFocus);
			label.add(constraint);
			text.append(Messages.getString("TaskIndicatorsComponent.ThisTaskHasA") + constraintTypeField.getText(indicators,null) + Messages.getString("TaskIndicatorsComponent.constraintOn") + constraintDateField.getText(indicators,null)+"<br>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		if (indicators.isInvalidIntersectionCalendar()) {
			setLook(invalidCalendar,isSelected,hasFocus);
			label.add(invalidCalendar);
			text.append(Messages.getString("TaskIndicatorsComponent.TheIntersection")); //$NON-NLS-1$
		}
		String note = indicators.getNotes();
		if (note != null && note.length() > 0) {
			setLook(notes,isSelected,hasFocus);
			label.add(notes);
			text.append(Messages.getString("TaskIndicatorsComponent.Notes") + note + "'<br>"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (indicators.isComplete()) {
			setLook(completed,isSelected,hasFocus);
			label.add(completed);
			text.append(Messages.getString("TaskIndicatorsComponent.TheTaskWasCompletedOn") + finish.getText(indicators,null)+"<br>"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if (indicators.isParentWithAssignments()) {
			setLook(parentAssignment,isSelected,hasFocus);
			label.add(parentAssignment);
			text.append(Messages.getString("TaskIndicatorsComponent.ThisParentTaskHasResources") + ((NormalTask)indicators).getResourceNames()+"<br>"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if (indicators instanceof Task) {
			Task t = (Task)indicators;
			if (t.isSubproject()) {
				SubProj s = (SubProj)t;
				if (s.isValid()) {
					setLook(subproject,isSelected,hasFocus);
					label.add(subproject);
					text.append(Messages.getString("TaskIndicatorsComponent.ThisTasksRepresentsThe") + (s.isSubprojectOpen() ? Messages.getString("TaskIndicatorsComponent.opened") : Messages.getString("TaskIndicatorsComponent.unopened")) + Messages.getString("TaskIndicatorsComponent.subproject") + ((Task)s).getName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				} else {
					setLook(invalidProject,isSelected,hasFocus);
					label.add(invalidProject);
					text.append(Messages.getString("TaskIndicatorsComponent.ThisSubprojectIsNotValid")); //$NON-NLS-1$
				}
			}
			
			
			if (t.isMissedDeadline()) {
				setLook(missedDeadline,isSelected,hasFocus);
				label.add(missedDeadline);
				text.append(Messages.getString("TaskIndicatorsComponent.ThisTaskFinishesOn") + finish.getText(indicators,null) +  //$NON-NLS-1$
						Messages.getString("TaskIndicatorsComponent.whichIsAfterItsDeadline") + deadlineField.getText(indicators,null)); //$NON-NLS-1$
			}
			if (t.getDelegatedTo() != null) {
				if (t.isDelegatedToUser()) {
					setLook(delegatedMe,isSelected,hasFocus);
					label.add(delegatedMe);
					text.append(Messages.getString("TaskIndicatorsComponent.ThisTaskHasBeenDelegatedToYou")); //$NON-NLS-1$

				} else {
					setLook(delegated,isSelected,hasFocus);
					label.add(delegated);
					text.append(Messages.getString("TaskIndicatorsComponent.ThisTaskHasBeenDelegatedTo") + t.getDelegatedTo().getName()); //$NON-NLS-1$
				}
			}
		}
	}
	
	
}