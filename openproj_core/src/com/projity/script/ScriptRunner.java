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
package com.projity.script;

import java.util.List;

import com.projity.script.object.LiteField;
import com.projity.script.object.LiteProject;
import com.projity.script.object.LiteResource;
import com.projity.script.object.LiteTask;
import com.projity.script.object.ReportData;


/**
 * Will be subclassed for client and server (ajax) versions
 *
 * This will be used for:
 * AJAX communication to server
 * Real-time collaboration
 * and later on
 * as an API (with the advantage of being a sandbox - only can access this package
 * Macros
 * Logging
 * Journaling
 * Possibly using journaling as a way of saving projects instead of sending all data?
 * Should this be tied to undo?
 * And wrapped by bsh or groovy for formulas and filters
 * Need to add exception handling where needed
 *
 */
public interface ScriptRunner {
	public static final int TASK=1;
	public static final int RESOURCE=2;
	public static final int PROJECT=3;
	public static final int ASSIGNMENT=4;
	//public static final int PORTFOLIO=6;

	public static final int PROJECT_DESCRIPTOR=100;
	public static final int PROJECT_DESCRIPTOR_AS_TASK=101;
	public static final int RESOURCE_AS_TASK=102;
	public static final int RESOURCE_USAGE=103;
	public static final int USER=200;

	public static final long PORTFOLIO_PROJECT_ID=100000L;
	public static final long RESOURCE_USAGE_PROJECT_ID=200000L;
	public static final long RESOURCE_ASSIGNMENT_PROJECT_ID=1000000000000000L;


	public List getProjectDescriptors()  throws Exception;
	public List getUsers()  throws Exception;


	public LiteProject createProject(String name) throws Exception;
	public void saveProject(long projectId) throws Exception;
	public void saveProjectAs(long projectId,String name) throws Exception;
	public void closeProject(long projectId) throws Exception;
	public void closeProject(Long ids[]) throws Exception;

	/**
	 * Returns all the tasks of a project.
	 * Opens the project if it's not already opened
	 * @param projectId
	 * @return the content of the project
	 */
	public LiteProject getProject(long projectId,ConverterContext context) throws Exception;
	//public LiteProject getProject(long projectId) throws Exception;

	public List getContexts(int type) throws Exception;
	public Object[] getCharts(int type)  throws Exception;

//	public LiteResourcePool getResourcePool(long projectId) throws Exception;
//	public LiteProject getPortfolio() throws Exception;
//	public LiteProject getResourcePoolWithUsage() throws Exception;
//	/**
//	 * Returns all the tasks of a project.
//	 * Opens the project if it's not already opened
//	 * @param projectId
//	 * @param explorationMaxLevel 1: returns only the parent tasks, 2: returns the parent tasks and their children ...
//	 * @return the content of the project
//	 */
//	public LiteProject getProject(long projectId,int explorationMaxLevel) throws Exception;
//	public LiteResourcePool getResourcePool(long projectId,int explorationMaxLevel) throws Exception;

	public LiteProject setValue(long projectId, String fieldId, int type, long id, String value,boolean returnChanges) throws Exception;
	/**
	 *
	 * @param projectId
	 * @param type
	 * @param previousId
	 * @param returnChanges
	 * @return a project with the new task only if returnChanges is true
	 * @throws Exception
	 */
	public LiteProject insertBefore(long projectId, int type, long previousId, boolean returnChanges) throws Exception;
	public List<Long> remove(long projectId, int type, long id, boolean returnRemovedIds) throws Exception;

	public LiteTask getTask(long projectId,long id) throws Exception;
	public List<LiteTask> getChildrenTasks(long projectId,long id) throws Exception;
	public LiteResource getResource(long projectId,long id) throws Exception;
	public List<LiteResource> getChildrenResource(long projectId,long id) throws Exception;

	public LiteProject link(long projectId,Long ids[],int type) throws Exception;
	public LiteProject unlink(long projectId,Long ids[]) throws Exception;
	public LiteProject indent(long projectId,Long ids[]) throws Exception;
	public LiteProject outdent(long projectId,Long ids[]) throws Exception;

	public LiteProject setInterval(long projectId, long id, long newStart, long newEnd, long oldStart, long oldEnd, boolean returnChanges) throws Exception;
	public LiteProject setCompleted(long projectId, long id, long completed, boolean returnChanges) throws Exception;


	public void setFieldArray(long projectId,int type,String fieldArrayId) throws Exception;
	public List<String> getFieldArrays(long projectId,int type) throws Exception;
	public List<LiteField> getFieldArray(ConverterContext ctx) throws Exception;
	public List<LiteField> getDefaultFieldArray(long projectId) throws Exception;
	public Object zoomTimeScale(long projectId,int type,int amount,float center,boolean returnChange) throws Exception;
	public Object translateWindow(long projectId,int type,int amount,float center,boolean returnChange) throws Exception;



//	// methods for finding existing proxy objects
//	Project project(long id);
//	Task task(long id);
//	Resource resource(long id);
//	Field field(String name);
//	Field fieldFromId(String id);
//
//	// Factory methods
//	Project createProject();
//	Task createTask(Project project);
//	Resource createResource();
//
//	void saveProject(); // I put these methods here and not in project since they are impl dependent
//	void saveProjectAs(String newName);
//	void closeProject();
//
//	void assignResource(Task task, Resource resource);
//	void link(Task pred,Task succ);
//
//	// also have node versions?
//	void setText(Field field, Scriptable obj, String textValue);
//	String getText(Field field, Scriptable obj);
//	String getValue(Field field, Scriptable obj);
//
//	void select(Collection objects);
//	// Selection specific methods
//	void indent();
//	void outdent();
//	void fold();
//	void unfold();
//	void link(); // selected
//	void setTextOnSelection(Field field, String textValue); // for things like update task where multiple objects ar changd
//
//	void undo();
//	void redo();
//	// cut, copy, paste ?

	public ReportData getReport(String reportId, String fieldArrayId) throws Exception;

	public String ping(String message) throws Exception;
	public void close() throws Exception;;
}
