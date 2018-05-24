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

Attribution Information: Attribution Copyright Notice: Copyright (c) 2006, 2007
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
package com.projity.pm.graphic.frames;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import org.apache.commons.collections.Closure;

import com.projity.association.InvalidAssociationException;
import com.projity.command.UpdateProjectCommand;
import com.projity.configuration.Configuration;
import com.projity.dialog.BaselineDialog;
import com.projity.dialog.DelegateTaskDialog;
import com.projity.dialog.FindDialog;
import com.projity.dialog.UpdateProjectDialogBox;
import com.projity.dialog.UpdateTaskDialog;
import com.projity.dialog.calendar.ChangeWorkingTimeDialogBox;
import com.projity.document.ObjectEvent;
import com.projity.document.ObjectSelectionEvent;
import com.projity.document.ObjectSelectionListener;
import com.projity.field.Field;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeList;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.transform.ViewTransformer;
import com.projity.grouping.core.transform.filtering.NodeFilter;
import com.projity.grouping.core.transform.filtering.NotAssignmentFilter;
import com.projity.grouping.core.transform.filtering.ResourceInTeamFilter;
import com.projity.job.JobQueue;
import com.projity.menu.MenuActionConstants;
import com.projity.menu.MenuActionsMap;
import com.projity.menu.MenuManager;
import com.projity.pm.calendar.CalendarService;
import com.projity.pm.calendar.HasCalendar;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.dependency.DependencyService;
import com.projity.pm.graphic.IconManager;
import com.projity.pm.graphic.frames.workspace.NamedFrame;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.model.cache.NodeModelCacheFactory;
import com.projity.pm.graphic.model.cache.ReferenceNodeModelCache;
import com.projity.pm.graphic.spreadsheet.SpreadSheet;
import com.projity.pm.graphic.spreadsheet.SpreadSheetModel;
import com.projity.pm.graphic.spreadsheet.common.CommonSpreadSheet;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeEvent;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeListener;
import com.projity.pm.graphic.timescale.CoordinatesConverter;
import com.projity.pm.graphic.views.BaseView;
import com.projity.pm.graphic.views.ChartView;
import com.projity.pm.graphic.views.GanttView;
import com.projity.pm.graphic.views.MainView;
import com.projity.pm.graphic.views.PertView;
import com.projity.pm.graphic.views.ProjectView;
import com.projity.pm.graphic.views.ResourceView;
import com.projity.pm.graphic.views.Searchable;
import com.projity.pm.graphic.views.TreeView;
import com.projity.pm.graphic.views.UsageDetailView;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.task.Portfolio;
import com.projity.pm.task.Project;
import com.projity.pm.task.ProjectEvent;
import com.projity.pm.task.ProjectFactory;
import com.projity.pm.task.ProjectListener;
import com.projity.pm.task.Task;
import com.projity.preference.GlobalPreferences;
import com.projity.session.LoadOptions;
import com.projity.toolbar.FilterToolBarManager;
import com.projity.undo.UndoController;
import com.projity.util.Alert;
import com.projity.util.ArrayUtils;
import com.projity.util.ClassUtils;
import com.projity.util.DataUtils;
import com.projity.util.Environment;
import com.projity.workspace.SavableToWorkspace;
import com.projity.workspace.WorkspaceSetting;

/**
 *
 */

public class DocumentFrame extends NamedFrame implements
		SelectionNodeListener, UndoableEditListener, MenuActionConstants, ObjectEvent.Listener, ProjectListener, SavableToWorkspace, ObjectSelectionListener {
	private static final long serialVersionUID = 2075764134837908178L;
	protected MainView mainView;
	protected GanttView ganttView;
	protected UsageDetailView taskUsageDetailView;
	protected UsageDetailView resourceUsageDetailView;
	protected PertView pertView;
	protected TreeView wbsView;
	protected TreeView rbsView;
	protected ChartView chartView;
	protected ChartView histogramView;
	protected ResourceView resourceView;
	protected ProjectView projectView;
	protected UsageDetailView taskUsageView;
	protected UsageDetailView resourceUsageView;
	protected BaseView reportView;
	private static ArrayList ganttColumns = null; // static is ok?
	private FindDialog findDialog = null;
	protected CoordinatesConverter coord;
	protected Project project;
	protected GraphicManager graphicManager;
	protected MenuManager menuManager;
	MenuActionsMap actionsMap = null;
	BaseView activeTopView = null;
	BaseView activeBottomView = null;

	// keep state of pushed buttons so i can reset them when a view is reactivated
	String lastTopButton = null;
	String lastBottomButton = ACTION_NO_SUB_WINDOW;
	Workspace workspace;
	FilterToolBarManager filterToolBarManager = null;
	JobQueue jobQueue = null;
	private JRadioButtonMenuItem menuItem = null;
	public GraphicManager getGraphicManager() {
		return graphicManager;
	}

	protected NodeModel getTaskModel() {
		return project.getTaskModel();
	}

	protected NodeModel getResourceModel() {
		return project.getResourceModel();
	}

	public JobQueue getJobQueue(){
		if (jobQueue==null){
			jobQueue=new JobQueue("GraphicManager",true);
		}
		return jobQueue;
	}

	public ReferenceNodeModelCache getTaskNodeModelCache() {
		ReferenceNodeModelCache taskCache = (ReferenceNodeModelCache) project.getTaskCache();
		if (taskCache == null) {
			taskCache =NodeModelCacheFactory.createTaskNodeModelCache(project, getTaskModel());
			project.setTaskCache(taskCache);
		}
		return taskCache;
	}
	public ReferenceNodeModelCache getResourceNodeModelCache() {
		ReferenceNodeModelCache resourceCache = (ReferenceNodeModelCache) project.getResourceCache();
		if (resourceCache == null) {
			resourceCache =NodeModelCacheFactory.createResourceNodeModelCache(project.getResourcePool(), getResourceModel());
			project.setResourceCache(resourceCache);
		}
		return resourceCache;
	}


	public ReferenceNodeModelCache getReferenceCache(boolean task) {
		ReferenceNodeModelCache cache = (task) ? getTaskNodeModelCache()
				: getResourceNodeModelCache();
		return cache;
	}

	public NodeModelCache createCache(boolean task, String viewName) {
		return NodeModelCacheFactory.getInstance().createFilteredCache(
				getReferenceCache(task), viewName,null);
	}



	public DocumentFrame(GraphicManager parentFrame, final Project project,String id) {
		super(id, IconManager.getHalfSizedIcon("view.gantt"));
		//TODO make another constructor that does lazy loading of project from project id. it would be for restore workspace

		this.graphicManager = parentFrame;
		this.menuManager = graphicManager.getMenuManager();
		filterToolBarManager = Environment.isNewLook() ? FilterToolBarManager.create(menuManager) : graphicManager.getFilterToolBarManager();

		this.project = project;
		coord = new CoordinatesConverter(project);

		project.addObjectListener(this); // for project name changes
		getGraphicManager().getPreferences().addObjectListener(this);

		project.getObjectSelectionEventManager().addListener(this);
		setPreferredSize(new Dimension(800, 600));
		setMainView(true);

	}
	private void setMainView(boolean activate) {
		if (mainView != null)
			remove(mainView); // any previous
		mainView = new MainView();
		mainView.setBorder(null);
		setLayout(new GridLayout(1,1)); // fill up all of the space always
		add(mainView);

//		toolBarListener = new ToolBarListener();
//		registerToolBarActions();


		// wait until everything is initialized before activating the gantt view
		if (activate) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (true || GraphicManager.getLastWorkspace() == null) { // if there was a workspace, it will be restored
						if (isEditingResourcePool()) {
							activateResourceView();
							getGraphicManager().setAllButResourceDisabled(true);

						} else {
							activateGanttView();
							getGraphicManager().setAllButResourceDisabled(false);
						}
					}
					getGraphicManager().setButtonState(null,project); // disable buttons at first
				}});
		}

	}

	List getSelectedNodes(boolean excludeReadOnly) {
		CommonSpreadSheet spreadSheet = getTopSpreadSheet();
		if (spreadSheet == null)
			return null;
		List nodes = spreadSheet.getSelectedNodes();
		if (nodes.size() == 0)
			return null;
		if (excludeReadOnly) {
			Iterator i = nodes.iterator();
			while (i.hasNext()) {
				Node n = (Node)i.next();
				Object obj = n.getImpl();
				if (ClassUtils.isObjectReadOnly(obj))
					i.remove();
			}
		}

		return nodes;
	}

	Object getSelectedImpl() {
		CommonSpreadSheet spreadSheet = getTopSpreadSheet();
		if (spreadSheet == null)
			return null;
		return spreadSheet.getCurrentRowImpl();
	}

	boolean doBaselineDialog(boolean save) {
		finishAnyOperations();

		BaselineDialog baselineDialog = BaselineDialog.getInstance(getGraphicManager(),
				null, save,hasAtLeastOneTaskSelected());
		if (!baselineDialog.doModal())
			return false;
		Integer baselineNumber = new Integer(baselineDialog.getForm()
				.getBaselineNumber());
		boolean entireProject = baselineDialog.getForm().isEntireProject();
		List selection = entireProject ? null : getSelectedImpls(true);
		if (save)
			getProject().saveCurrentToSnapshot(baselineNumber, entireProject,
					selection,true);
		else
			getProject()
					.clearSnapshot(baselineNumber, entireProject, selection,true);
//		getProject().fireBaselineChanged(baselineDialog, null, baselineNumber,
//				save);
		return true;
	}

//	void doEnterpriseResourcesDialog() {
//		finishAnyOperations();
//		EnterpriseResourcesDialog enterpriseResourceDialog = EnterpriseResourcesDialog
//				.getInstance(getMainFrame());
//		enterpriseResourceDialog.pack();
//		enterpriseResourceDialog.setModal(false);
//		enterpriseResourceDialog.setLocationRelativeTo(null);//to center on
//															 // screen
//		enterpriseResourceDialog.show();
//	}


	void doChangeWorkingTimeDialog(boolean restrict) {
		finishAnyOperations();
		Object rowObject = getSelectedImpl();
		WorkingCalendar wc = null;
		List documentCalendars = null;
		if (rowObject instanceof HasCalendar) {
			wc = (WorkingCalendar) ((HasCalendar) rowObject).getWorkCalendar();
			if (rowObject instanceof ResourceImpl) {
				documentCalendars = ((ResourceImpl) rowObject)
						.getResourcePool().extractCalendars();
			}
		}
		if (wc == null)
			wc = (WorkingCalendar) getProject().getWorkCalendar();
		CalendarService service = CalendarService.getInstance();


		ChangeWorkingTimeDialogBox dlg = ChangeWorkingTimeDialogBox
				.getInstance(getGraphicManager().getFrame(), project,wc, documentCalendars,restrict,this.getUndoController());
		dlg.doModal();
	}


	void doLevelResourcesDialog() {
//		ResourceLevelingDialogBox.getInstance(getGraphicManager().getFrame(), null).doModal();
	}


	void doDelegateTasksDialog() {
		finishAnyOperations();
		List nodes = getSelectedNodes(true); //nodes, not impls!
		if (nodes == null)
			return;

		DelegateTaskDialog dlg = DelegateTaskDialog.getInstance(getGraphicManager().getFrame(),
				nodes);
		dlg.setLocationRelativeTo(null);//to center on screen
		dlg.doModal();

	}

	void doUpdateTasksDialog() {
		finishAnyOperations();
		List nodes = getSelectedNodes(true); //nodes, not impls!
		if (nodes == null)
			return;

		UpdateTaskDialog dlg = UpdateTaskDialog.getInstance(getGraphicManager().getFrame(),
				nodes);
		dlg.setLocationRelativeTo(null);//to center on screen
		dlg.doModal();

	}

	void doUpdateProjectDialog() {
		finishAnyOperations();
		UpdateProjectDialogBox dlg = UpdateProjectDialogBox.getInstance(
				getGraphicManager().getFrame(), null,hasAtLeastOneTaskSelected());
		if (dlg.doModal()) {
			UpdateProjectCommand cmd = new UpdateProjectCommand(project, dlg
					.getForm().getActiveDate().getTime(), dlg.getForm()
					.getUpdate().booleanValue(), dlg.getForm().getProgress()
					.booleanValue());
			forTasksDo(cmd, dlg.getForm().getEntireProject().booleanValue());
			cmd = null;
		}

	}

	void doDefineCodeDialog() {
		finishAnyOperations();
//		OutlineCodeDefinitionDialogBox.getInstance(getGraphicManager().getFrame(), null).doModal();
	}

	void doRecurringTaskDialog() {
		finishAnyOperations();
//		RecurringTaskInformationDialogBox.getInstance(getGraphicManager().getFrame(), null)
//				.doModal();
	}

	void doBarDialog() {
		finishAnyOperations();
//		ShapeBarDialogBox.getInstance(getGraphicManager().getFrame(), null).doModal();
	}

	void doSortDialog() {
		finishAnyOperations();
//		SortDialogBox.getInstance(getGraphicManager().getFrame(), null).doModal();
	}

	void doGroupDialog() {
		finishAnyOperations();
//		GroupDefinitionDialogBox.getInstance(getGraphicManager().getFrame(), null).doModal();
	}


	public void doLinkTasks() {
		finishAnyOperations();
		try {
			List list = NodeList.nodeListToImplList(getSelectedNodes(false), NotAssignmentFilter.getInstance());
			DependencyService.getInstance().connect(list,this,null);
			//DependencyService.getInstance().connect(list,this);
		} catch (InvalidAssociationException e) {
			Alert.error(e.getMessage(),this);
		}
	}
	public void doUnlinkTasks() {
		finishAnyOperations();
		List list = NodeList.nodeListToImplList(getSelectedNodes(false), NotAssignmentFilter.getInstance());


		DependencyService.getInstance().removeAnyDependencies(list,this);
	}
	public void doUndoRedo(boolean isUndo) {
		if (!isActive())
			return;
		finishAnyOperations();
		UndoController undoController=getUndoController();
		if (undoController!=null){
			if (isUndo)
				undoController.undo();
			else
				undoController.redo();
			refreshUndoButtons();
		}
	}
	public void doZoomIn() {
		if (activeTopView != null)
			activeTopView.zoomIn();
	}
	public void doZoomOut() {
		if (activeTopView != null)
			activeTopView.zoomOut();
	}
	public void doScrollToTask() {
		if (activeTopView != null)
			activeTopView.scrollToTask();
	}

	public boolean canZoomIn() {
		return activeTopView==null?false:activeTopView.canZoomIn();
	}
	public boolean canZoomOut() {
		return activeTopView==null?false:activeTopView.canZoomOut();
	}
	public boolean canScrollToTask() {
		return activeTopView==null?false:activeTopView.canScrollToTask();
	}
	public int getScale() {
		return activeTopView==null?-1:activeTopView.getScale();
	}

	public void doOutdent() {
		SpreadSheet ss = getActiveSpreadSheet();
		if (ss !=null)
			ss.executeAction(MenuActionConstants.ACTION_OUTDENT);
	}
	public void doExpand() {
		SpreadSheet ss = getActiveSpreadSheet();
		if (ss !=null)
			ss.executeAction(MenuActionConstants.ACTION_EXPAND);
	}
	public void doCollapse() {
		SpreadSheet ss = getActiveSpreadSheet();
		if (ss !=null)
			ss.executeAction(MenuActionConstants.ACTION_COLLAPSE);
	}

	public void doIndent() {
		SpreadSheet ss = getActiveSpreadSheet();
		if (ss !=null)
			ss.executeAction(MenuActionConstants.ACTION_INDENT);
	}
	public void doDelete() {
		SpreadSheet ss = getActiveSpreadSheet();
		if (ss !=null)
			ss.executeAction(MenuActionConstants.ACTION_DELETE);
	}

	public void doCut() {
		SpreadSheet ss = getActiveSpreadSheet();
		if (ss !=null)
			ss.prepareAction(MenuActionConstants.ACTION_CUT).actionPerformed(new ActionEvent(ss,0,null));
			//NodeListTransferHandler.getCutAction(ss).actionPerformed(new ActionEvent(this,0,null));
			//ss.executeAction(SpreadSheet.CUT);
	}
	public void doCopy() {
		SpreadSheet ss = getActiveSpreadSheet();
		if (ss !=null)
			ss.prepareAction(MenuActionConstants.ACTION_COPY).actionPerformed(new ActionEvent(ss,0,null));
			//NodeListTransferHandler.getCopyAction(ss).actionPerformed(new ActionEvent(this,0,null));
			//ss.executeAction(SpreadSheet.COPY);
	}
	public void doPaste() {
		SpreadSheet ss = getActiveSpreadSheet();
		if (ss !=null)
			ss.prepareAction(MenuActionConstants.ACTION_PASTE).actionPerformed(new ActionEvent(ss,0,null));
			//NodeListTransferHandler.getPasteAction(ss).actionPerformed(new ActionEvent(this,0,null));
			//ss.executeAction(SpreadSheet.PASTE);
	}


	public GanttView getGanttView() {
		if (ganttView == null) {
			ganttView = new GanttView(this, graphicManager.getMenuManager(),mainView.getSynchronizer());
			ganttView.init(getTaskNodeModelCache(), getTaskModel(), coord);
			restoreWorkspaceFor(ganttView);
		}
		return ganttView;
	}

	public UsageDetailView getTaskUsageDetailView() {
		if (taskUsageDetailView == null) {
			taskUsageDetailView = new UsageDetailView(this, graphicManager
					.getMenuManager(),mainView.getSynchronizer());
			taskUsageDetailView.init(getTaskNodeModelCache(), true, coord,
					false,  ACTION_TASK_USAGE_DETAIL,addTransformerInitializationClosure());
			restoreWorkspaceFor(taskUsageDetailView);
		}
		return taskUsageDetailView;
	}

	public UsageDetailView getResourceUsageDetailView() {
		if (resourceUsageDetailView == null) {
			resourceUsageDetailView = new UsageDetailView(this, graphicManager
					.getMenuManager(),mainView.getSynchronizer());
			resourceUsageDetailView.init(getResourceNodeModelCache(), false,
					coord, false, ACTION_RESOURCE_USAGE_DETAIL,addTransformerInitializationClosure());
			restoreWorkspaceFor(resourceUsageDetailView);
		}
		return resourceUsageDetailView;
	}

	public PertView getPertView() {
		if (pertView == null) {
			pertView = new PertView(this, graphicManager.getMenuManager());
			pertView.init(getTaskNodeModelCache(), getTaskModel());
			restoreWorkspaceFor(pertView);
		}
		return pertView;
	}

	public TreeView getWBSView() {
		if (wbsView == null) {
			wbsView = new TreeView(this, graphicManager.getMenuManager());
			wbsView.init(getTaskNodeModelCache(), getTaskModel(), ACTION_WBS,addTransformerInitializationClosure());
			restoreWorkspaceFor(wbsView);
		}
		return wbsView;
	}

	public TreeView getRBSView() {
		if (rbsView == null) {
			rbsView = new TreeView(this, graphicManager.getMenuManager());
			rbsView.init(getResourceNodeModelCache(), getResourceModel(),
							ACTION_RBS,addTransformerInitializationClosure());
			restoreWorkspaceFor(rbsView);
		}
		return rbsView;
	}

	public BaseView getReportView() {
		try {
			if (reportView == null) {
				Class clazz=Class.forName("com.projity.reports.view.ReportView");
				reportView=(BaseView)clazz.getConstructor(new Class[]{DocumentFrame.class}).newInstance(new Object[]{this});
				clazz.getMethod("init", new Class[]{CoordinatesConverter.class}).invoke(reportView, new Object[]{coord});
				if (reportView!=null) restoreWorkspaceFor(reportView);
			}
		} catch (Exception e) {
			e.printStackTrace();
			reportView=null;
		}
		return reportView;
	}

	public ChartView getChartView() {
		if (chartView == null) {
			chartView = new ChartView(this, false, graphicManager.getMenuManager(),mainView.getSynchronizer(),addTransformerInitializationClosure());
			chartView.init(coord);
			restoreWorkspaceFor(chartView);
		}
		return chartView;
	}

	public ChartView getHistogramView() {
		if (histogramView == null) {
			histogramView = new ChartView(this, true, graphicManager
					.getMenuManager(),mainView.getSynchronizer(),addTransformerInitializationClosure());
			histogramView.init(coord);
			restoreWorkspaceFor(histogramView);
		}
		return histogramView;
	}

	public ResourceView getResourceView() {
		if (resourceView == null) {
			resourceView = new ResourceView(getResourceNodeModelCache(),
					getResourceModel(), project.getResourcePool(),!Environment.isOpenProj() &&project.isReadOnly(),graphicManager.isEditingMasterProject());
			restoreWorkspaceFor(resourceView);
		}
		return resourceView;
	}

	public ProjectView getProjectView() {
		if (projectView == null) {
			Portfolio portfolio = getGraphicManager().getProjectFactory().getPortfolio();
			projectView = new ProjectView(portfolio.getNodeModel(), portfolio);
			restoreWorkspaceFor(projectView);
		}
		return projectView;
	}

	public UsageDetailView getTaskUsageView() {
		if (taskUsageView == null) {
			taskUsageView = new UsageDetailView(this, graphicManager
					.getMenuManager(),mainView.getSynchronizer());
			taskUsageView.init(getTaskNodeModelCache(), true, coord, true,
					ACTION_TASK_USAGE,addTransformerInitializationClosure());
			restoreWorkspaceFor(taskUsageView);
		}
		return taskUsageView;
	}

	public UsageDetailView getResourceUsageView() {
		if (resourceUsageView == null) {
			resourceUsageView = new UsageDetailView(this, graphicManager
					.getMenuManager(),mainView.getSynchronizer());
			resourceUsageView.init(getResourceNodeModelCache(), false, coord,
					true, ACTION_RESOURCE_USAGE,addTransformerInitializationClosure());
			restoreWorkspaceFor(resourceUsageView);
		}
		return resourceUsageView;
	}

	public void toggleMinWidth() {
		boolean normalMinWidth = (activeTopView == null || activeTopView.hasNormalMinWidth())
			&& (activeBottomView == null || activeBottomView.hasNormalMinWidth());
		coord.toggleMinWidth(normalMinWidth);
	}


	public boolean activateView(String viewName) {
		BaseView topView = null;
		BaseView bottomView = null;
		boolean top = true;
		if (viewName.equals(ACTION_GANTT)) {
			activateGanttView();
			return top;
		} else if (viewName.equals(ACTION_TRACKING_GANTT)) {
			activateTrackingGanttView();
			return top;
		} else if (viewName.equals(ACTION_TASK_USAGE_DETAIL))
			topView = getTaskUsageDetailView();
		else if (viewName.equals(ACTION_RESOURCE_USAGE_DETAIL))
			topView = getResourceUsageDetailView();
		else if (viewName.equals(ACTION_NETWORK))
			topView = getPertView();
		else if (viewName.equals(ACTION_WBS))
			topView = getWBSView();
		else if (viewName.equals(ACTION_RBS))
			topView = getRBSView();
		else if (viewName.equals(ACTION_REPORT))
			topView = getReportView();
		else if (viewName.equals(ACTION_RESOURCES))
			topView = getResourceView();
		else if (viewName.equals(ACTION_PROJECTS))
			topView = getProjectView();
		else if (viewName.equals(ACTION_HISTOGRAM)) {
//			if (activeBottomView != getHistogramView())
				 bottomView = getHistogramView();
//			else
//				deactivateBottomView();
		}
		else if (viewName.equals(ACTION_CHARTS))
			bottomView = getChartView();
		else if (viewName.equals(ACTION_TASK_USAGE))
			bottomView = getTaskUsageView();
		else if (viewName.equals(ACTION_RESOURCE_USAGE))
			bottomView = getResourceUsageView();
		else if (viewName.equals(ACTION_NO_SUB_WINDOW))
			deactivateBottomView();

		if (topView != null) {
			activateTopView(topView,viewName);
		}

		if (bottomView != null) {
			boolean clickNew = true;
			if (!Environment.isNewLook()) {
				clickNew = bottomView != activeBottomView; // if clicked on a non pressed button
				deactivateBottomView();
			}
			if (clickNew)
				activateBottomView(bottomView,viewName);
			top = false;
		}
		return top;
	}
	private void activateTopView(BaseView view,String viewName) {
		showWaitCursor(true);
		if (findDialog != null)
			findDialog.setVisible(false);
		CommonSpreadSheet ss = getTopSpreadSheet();
		if (ss != null)
			ss.removeSelectionNodeListener(this);
		deactivateTopView();
		activeTopView = view;
		mainView.setTop((Component)view);
		view.onActivate(true);
		ss = view.getSpreadSheet();
		if (ss != null)
			ss.addSelectionNodeListener(this);

		toggleMinWidth();
		menuManager.setActionSelected(viewName,true);
		lastTopButton = viewName;
		setComboBoxesViewName(view.getViewName());
		getGraphicManager().setTaskInformation(view.showsTasks(),view.showsResources());
		refreshUndoButtons();
		getGraphicManager().setEnabledDocumentMenuActions(true);
		showWaitCursor(false);
//this doesn't have any effect		setFrameIcon(menuManager.getToolButtonFromId(viewName).getIcon());

	}

	void deactivateTopView() {
		if (activeTopView == null)
			return;
		if (lastTopButton != null)
			menuManager.setActionSelected(lastTopButton,false);
		// deactivate current ss listener
		CommonSpreadSheet ss = getTopSpreadSheet();
		if (ss != null)
			ss.removeSelectionNodeListener(this);

		mainView.removeTop();
		activeTopView.onActivate(false);
		activeTopView = null;
		toggleMinWidth();
		getGraphicManager().setTaskInformation(false, false);
		refreshUndoButtons();
		getGraphicManager().setEnabledDocumentMenuActions(false);
	}

	public void activateResourceView() {
		activateTopView(getResourceView(),ACTION_RESOURCES);
	}

	public void activateGanttView() {
		if (ganttColumns != null)
			getGanttView().setColumns(ganttColumns);
		getGanttView().setBarStyles("standard");
		getGanttView().setTracking(false);
		activateTopView(getGanttView(),ACTION_GANTT);
	}

	public ArrayList getGanttColumns() {
		return ganttColumns;
	}
	public void activateTrackingGanttView() {
		getGanttView().setBarStyles("Tracking");
		getGanttView().setTracking(true);
		ganttColumns = getGanttView().setColumns("Spreadsheet.Task.tracking");
		activateTopView(getGanttView(),ACTION_TRACKING_GANTT);
	}

	public void activateBottomView(BaseView view,String viewName) {
		boolean same = viewName.equals(lastBottomButton);
		if (same)
			return;
		if (viewName == ACTION_NO_SUB_WINDOW)
			deactivateBottomView();
		else {
			mainView.removeBottom();
		}
		activeBottomView = view;
		view.onActivate(true);
		lastBottomButton = viewName;
		mainView.setBottom((Component) view);
		toggleMinWidth();
		menuManager.setActionSelected(viewName,true);
		refreshUndoButtons();

		if (lastSelectionEvent!=null && view != null && view instanceof SelectionNodeListener)
			((SelectionNodeListener) view).selectionChanged(lastSelectionEvent);

	}

	public void deactivateBottomView() {
		if (activeBottomView == null)
			return;
		menuManager.setActionSelected(ACTION_NO_SUB_WINDOW,true);
		if (lastBottomButton != null)
			menuManager.setActionSelected(lastBottomButton,false);
		activeTopView.onActivate(false);
		lastBottomButton = ACTION_NO_SUB_WINDOW;
		mainView.removeBottom();
		activeBottomView = null;
		toggleMinWidth();
		refreshUndoButtons();
	}


	/**
	 * @return Returns the mainView.
	 */
	public MainView getMainView() {
		return mainView;
	}

	/**
	 * @return Returns the project.
	 */
	public Project getProject() {
		return project;
	}

	/**
	 * @return Returns the topSpreadSheet.
	 */
	public SpreadSheet getTopSpreadSheet() {
		CommonSpreadSheet ss = null;
		if (activeTopView != null)
			ss = activeTopView.getSpreadSheet();
		return (SpreadSheet) ss;
	}

	/**
	 * @return Returns the topSpreadSheet.
	 */
	public SpreadSheet getActiveSpreadSheet() {
		SpreadSheet ss = getTopSpreadSheet();
		if (ss == null) {
			if (activeBottomView != null)
				ss = activeBottomView.getSpreadSheet();

		}
		return ss;
	}


	protected SelectionNodeEvent lastSelectionEvent=null;
	/**
	 * React to selection changed events and forward them on to any bottom
	 * window
	 */
	public void selectionChanged(SelectionNodeEvent e) {
		lastSelectionEvent=e;
		Component bottom = mainView.getBottomComponent();
		if (bottom != null && bottom instanceof SelectionNodeListener)
			((SelectionNodeListener) bottom).selectionChanged(e);
		graphicManager.selectionChanged(e);
	}

	/**
	 * @return Returns the menuManager.
	 */
	public MenuManager getMenuManager() {
		return menuManager;
	}

	private boolean hasAtLeastOneTaskSelected() {
		return DataUtils.nodeListContainsImplOfType(getSelectedNodes(true), Task.class);
	}
	private void forTasksDo(Closure closure, boolean all) {
		DataUtils.forAllDo(closure, all, project.getTaskOutlineIterator(),
				getSelectedNodes(true), Task.class);
	}

	public void undoableEditHappened(UndoableEditEvent e) {
		refreshUndoButtons();
	}

	protected UndoController currentUndoController=null;
	public UndoController getUndoController(){
		return currentUndoController;
	}

	void refreshViewButtons(boolean enable) {
		if (enable)
			refreshUndoButtons();
		if (lastTopButton != null)
			menuManager.setActionSelected(lastTopButton,enable);
		if (lastBottomButton != null)
			menuManager.setActionSelected(lastBottomButton,enable);

	}
	public void refreshUndoButtons() {
		UndoController undoController = null;
		if (activeTopView != null)
			undoController = activeTopView.getUndoController();

		if (undoController!=currentUndoController){
			if (currentUndoController!=null)
				currentUndoController.getEditSupport().removeUndoableEditListener(this);
			if (undoController!=null)
				undoController.getEditSupport().addUndoableEditListener(this);
			currentUndoController=undoController;
		}

		boolean canUndo = false;
		boolean canRedo = false;
//		String undoText = "";
//		String redoText = "";

		if (undoController != null){
			canUndo = undoController.canUndo();
			canRedo = undoController.canRedo();
//			undoText = undoController.getUndoManager().getUndoPresentationName();
//			redoText = undoController.getUndoManager().getRedoPresentationName();
		}
		menuManager.setActionEnabled(ACTION_UNDO,canUndo);
		menuManager.setActionEnabled(ACTION_REDO,canRedo);

	}

	public Node addNodeForImpl(Object impl) {
		return addNodeForImpl(impl,NodeModel.NORMAL);
	}
	public Node addNodeForImpl(Object impl,int eventType) {
		SpreadSheet spreadSheet = (SpreadSheet) getTopSpreadSheet();
		if (impl == null) {
			spreadSheet.executeAction(MenuActionConstants.ACTION_NEW);
			return null;
		} else {
			return spreadSheet.addNodeForImpl(impl,eventType);
		}
	}

/**
 * sees if currently selected row belongs to main project. used to see if can insert a subproject. subprojects can
 * only be inserted into master project
 * @return
 */	public boolean isCurrentRowInMainProject() {
        CommonSpreadSheet spreadSheet=getTopSpreadSheet();
        if (spreadSheet == null)
        	return true;
	    int row = spreadSheet.getCurrentRow();
	    if (row == -1)
	    	return true;
		Node current = spreadSheet.getCurrentRowNode();
        SpreadSheetModel model=(SpreadSheetModel)spreadSheet.getModel();
 		LinkedList previousNodes=model.getPreviousVisibleNodesFromRow(row);
		if (previousNodes == null)
			return true;
		previousNodes.add(current); // treat current node first since going backwards
		ListIterator i = previousNodes.listIterator(previousNodes.size());
		while (i.hasPrevious()) {
			Object o = ((Node)i.previous()).getImpl();
			if (o instanceof Task) {
				if (((Task)o).isInSubproject())
					return false;
				return project == ((Task)o).getOwningProject();
			}
		}

		return true;
	}

	public List getSelectedImpls(boolean excludeReadOnly) {
		return NodeList.nodeListToImplList(getSelectedNodes(excludeReadOnly));
	}


	public void finishAnyOperations() {
		if (!isActive())
			return;
		CommonSpreadSheet topSpreadSheet=getTopSpreadSheet();
		if (topSpreadSheet!=null)
			topSpreadSheet.finishCurrentOperations();
	}

	public void setComboBoxesViewName(String view) {
		if (!Environment.isPlugin()) filterToolBarManager.setComboBoxesViewName(view);
	}

	public void showWaitCursor(boolean show) {
		getGraphicManager().showWaitCursor(show);
	}

	public void objectChanged(ObjectEvent objectEvent) { // rename tab if project changes
		if (objectEvent.getObject() instanceof Project && objectEvent.isUpdate()) { //Only for projects
			if (objectEvent.getField() == Configuration.getFieldFromId("Field.name")) {
				getGraphicManager().setTabNameAndTitle(this,project);
			}
//			else if (objectEvent.getField() == Configuration.getFieldFromId("Field.showProjectResourcesOnly")) {
//				for (ResourceInTeamFilter filter : resourcesInTeamFilters) {
//					filter.setFilterTeam(project.isShowProjectResourcesOnly());
//				}
//			}

		}
		else if (objectEvent.getObject() instanceof GlobalPreferences && objectEvent.isUpdate()) {
			//if (objectEvent.getField() == Configuration.getFieldFromId("Field.showProjectResourcesOnly")) {
				for (ResourceInTeamFilter filter : resourcesInTeamFilters) {
					filter.setFilterTeam(getGraphicManager().getPreferences().isShowProjectResourcesOnly());
				}
				ResourceInTeamFilter filter=getGraphicManager().getAssignmentDialogTransformerInitializationClosure();
				if (filter!=null){
					filter.setFilterTeam(getGraphicManager().getPreferences().isShowProjectResourcesOnly());
				}
			//}

		}

	}

	private ArrayList<ResourceInTeamFilter> resourcesInTeamFilters=new ArrayList<ResourceInTeamFilter>();

	public Closure addTransformerInitializationClosure(){
		return new Closure(){
			public void execute(Object arg) {
				ViewTransformer transformer=(ViewTransformer)arg;
		        NodeFilter hiddenFilter=transformer.getHiddenFilter();
		        if (hiddenFilter!=null&& hiddenFilter instanceof ResourceInTeamFilter){
		        	ResourceInTeamFilter rf=(ResourceInTeamFilter)hiddenFilter;
		        	rf.setFilterTeam(getGraphicManager().getPreferences().isShowProjectResourcesOnly());
		        	resourcesInTeamFilters.add(rf);
		        }
			}
		};
	}

	void onClose() {
		project = null; // get rid of reference
	}

	boolean isPrintable() {
		return activeTopView != null && activeTopView.isPrintable();
	}

	final CoordinatesConverter getCoord() {
		return coord;
	}

	public void nameChanged(final ProjectEvent e) {
		setTabNameAndTitle(e.getProject());
	}
	public void groupDirtyChanged(final ProjectEvent e) {
		setTabNameAndTitle(e.getProject());
		getGraphicManager().refreshSaveStatus(false);
	}
    void setTabNameAndTitle(Project project) {
    	boolean show = isShowTitleBar();

   		getGraphicManager().setTitle(false);
		setTabTitle(project.getName());
		setShowTitleBar(show);
//		if (parentFrame.getCurrentFrame() == this)
//			parentFrame.setTitle(Messages.getString("Text.ApplicationTitle") + " - " + project.getName());
    }
    String getTopViewId() {
    	if (lastTopButton == null)
    		return ACTION_GANTT;
    	return lastTopButton;
    }


	protected void finalize() throws Throwable {
		System.out.println("~~~~~~~~~~~~~~~~ DocumentFrame.finalize()");
		super.finalize();
	}

	public void cleanUp() {
		System.out.println("Document Frame Cleanup");
		if (project != null) {
			project.removeProjectListener(this);
			project.removeObjectListener(this);
			project.removeScheduleListener(coord);
		}
		if (getUndoController() != null &&  getUndoController().getEditSupport() != null)
			getUndoController().getEditSupport().removeUndoableEditListener(this);
		if (coord != null)
			coord.removeTimeScaleListener(mainView);
    	forAllViews(new Closure() {
			public void execute(Object v) {
				if (v != null)
					((BaseView)v).cleanUp();
			}});
    	resetViews();
    	if (jobQueue != null)
    		jobQueue.cancel();
    	jobQueue =null;
		project = null;
		coord = null;
		resetViews();
	}

	void resetViews() {
		ganttView = null;
		taskUsageDetailView = null;
		resourceUsageDetailView = null;
		pertView = null;
		wbsView = null;
		rbsView = null;
		chartView = null;
		histogramView = null;
		resourceView = null;
		projectView = null;
		taskUsageView = null;
		resourceUsageView = null;
		reportView = null;
		activeTopView = null;
		activeBottomView = null;
	}
	private void forAllViews(Closure c) {
		Object[] views = {
			ganttView,
			taskUsageDetailView,
			resourceUsageDetailView,
			pertView,
			wbsView,
			rbsView,
			chartView,
			histogramView,
			resourceView,
			projectView,
			taskUsageView,
			resourceUsageView,
			reportView
		};
		ArrayUtils.forAllDo(views,c);
	}

	public void restoreWorkspace(WorkspaceSetting w, int context) {
		Workspace ws = (Workspace)w;
		workspace = ws;
		setMainView(false);
		if (project == null) {
			project = ProjectFactory.getInstance().findFromId(ws.projectId);
			if (project == null){
				LoadOptions opt=new LoadOptions();
				opt.setId(ws.projectId);
				opt.setSync(true);
				project = ProjectFactory.getInstance().openProject(opt);
			}
		}
		coord.restoreWorkspace(ws.getCoord(), context);

		activateView(ws.topViewName);
		activateView(ws.bottomViewName);
		mainView.restoreWorkspace(ws.mainView, context);

	}

	private WorkspaceSetting restoreWorkspaceFor(BaseView view) {
		WorkspaceSetting ws = getWorkspaceFor(view);
		if (ws != null)
			view.restoreWorkspace(ws, SavableToWorkspace.VIEW);
		return ws;
	}
	private WorkspaceSetting getWorkspaceFor(BaseView view) {
		if (workspace == null || workspace.views == null)
			return null;
		return (WorkspaceSetting) workspace.views.get(view.getViewName());
	}

	private void saveViewWorkspace(Workspace ws, String name, BaseView view) {
		if (view != null)
			ws.views.put(name, view.createWorkspace(SavableToWorkspace.VIEW));
	}
	public WorkspaceSetting createWorkspace(int context) {
		Workspace ws = new Workspace();
		ws.projectId = getProject().getUniqueId();
		ws.topViewName = getTopViewId();
		ws.bottomViewName = lastBottomButton;
		ws.coord = coord.createWorkspace(context);
		ws.mainView = mainView.createWorkspace(context);
		ws.saveViewWorkspace(ACTION_GANTT,ganttView);
		ws.saveViewWorkspace(ACTION_TASK_USAGE_DETAIL,taskUsageDetailView);
		ws.saveViewWorkspace(ACTION_RESOURCE_USAGE_DETAIL,resourceUsageDetailView);
		ws.saveViewWorkspace(ACTION_NETWORK,pertView);
		ws.saveViewWorkspace(ACTION_WBS,wbsView);
		ws.saveViewWorkspace(ACTION_RBS,rbsView);
		ws.saveViewWorkspace(ACTION_CHARTS,chartView);
		ws.saveViewWorkspace(ACTION_HISTOGRAM,histogramView);
		ws.saveViewWorkspace(ACTION_RESOURCES,resourceView);
		ws.saveViewWorkspace(ACTION_PROJECTS,projectView);
		ws.saveViewWorkspace(ACTION_TASK_USAGE,taskUsageView);
		ws.saveViewWorkspace(ACTION_RESOURCE_USAGE,resourceUsageView);
		ws.saveViewWorkspace(ACTION_REPORT,reportView);
		return ws;
	}

	public static class Workspace implements WorkspaceSetting  {
		private static final long serialVersionUID = 8836549717587108911L;
		//TODO store the current filter, grouper, sorter for each view
		long projectId;
		String topViewName;
		String bottomViewName;
		WorkspaceSetting coord;
		WorkspaceSetting mainView;
		HashMap views;
		public void saveViewWorkspace(String name, BaseView view) {
			if (views  == null)
				views = new HashMap();
			if (view != null)
				views.put(name, view.createWorkspace(SavableToWorkspace.VIEW));
		}
		public final String getBottomViewName() {
			return bottomViewName;
		}
		public final void setBottomViewName(String bottomViewName) {
			this.bottomViewName = bottomViewName;
		}
		public final String getTopViewName() {
			return topViewName;
		}
		public final void setTopViewName(String topViewName) {
			this.topViewName = topViewName;
		}
		public final long getProjectId() {
			return projectId;
		}
		public final void setProjectId(long projectId) {
			this.projectId = projectId;
		}
		public WorkspaceSetting getCoord() {
			return coord;
		}
		public void setCoord(WorkspaceSetting coord) {
			this.coord = coord;
		}
		public HashMap getViews() {
			return views;
		}
		public void setViews(HashMap views) {
			this.views = views;
		}
		public WorkspaceSetting getMainView() {
			return mainView;
		}
		public void setMainView(WorkspaceSetting mainView) {
			this.mainView = mainView;
		}
	}

	public FilterToolBarManager getFilterToolBarManager() {
		return filterToolBarManager;
	}

	public BaseView getActiveBottomView() {
		return activeBottomView;
	}

	public BaseView getActiveTopView() {
		return activeTopView;
	}

	public void setMenuItem(JRadioButtonMenuItem mi) {
		this.menuItem = mi;

	}

	public JRadioButtonMenuItem getMenuItem() {
		return menuItem;
	}
	public void setActive(boolean active) {
		super.setActive(active);
		if (isEditingResourcePool()) {
			getGraphicManager().setAllButResourceDisabled(true);

		} else {
			getGraphicManager().setAllButResourceDisabled(false);
		}

	}

	boolean isEditingResourcePool() {
		return (project.isMaster()&&!project.isLocal());

	}
	public void doFind(Searchable searchable, Field field) {
		if (!isActive())
			return;

    	if (findDialog == null) {
    		findDialog = FindDialog.getInstance(this,searchable,field);
    		findDialog.pack();
    		findDialog.setModal(false);
    	} else {
    		findDialog.init(searchable,field);
    	}
    	findDialog.setLocationRelativeTo(this);//to center on screen
        findDialog.setVisible(true);

	}

	public void objectSelected(ObjectSelectionEvent e) {
		CommonSpreadSheet spreadSheet = getTopSpreadSheet();
		spreadSheet.selectObject(e.getObject());
		doScrollToTask();
	}
}
