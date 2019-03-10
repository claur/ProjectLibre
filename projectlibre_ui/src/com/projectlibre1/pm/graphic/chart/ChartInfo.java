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
package com.projectlibre1.pm.graphic.chart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;

import com.projectlibre1.pm.graphic.model.cache.NodeModelCache;
import com.projectlibre1.pm.graphic.model.event.CacheListener;
import com.projectlibre1.pm.graphic.model.event.CompositeCacheEvent;
import com.projectlibre1.pm.graphic.spreadsheet.selection.event.SelectionNodeEvent;
import com.projectlibre1.pm.graphic.spreadsheet.selection.event.SelectionNodeListener;
import com.projectlibre1.pm.graphic.timescale.CoordinatesConverter;
import com.projectlibre1.pm.graphic.views.ChartView;
import com.projectlibre1.document.ObjectEvent;
import com.projectlibre1.graphic.configuration.SpreadSheetFieldArray;
import com.projectlibre1.grouping.core.model.NodeModel;
import com.projectlibre1.pm.resource.Resource;
import com.projectlibre1.pm.scheduling.ScheduleEvent;
import com.projectlibre1.pm.scheduling.ScheduleEventListener;
import com.projectlibre1.pm.task.Project;
import com.projectlibre1.timescale.TimeScaleEvent;
import com.projectlibre1.timescale.TimeScaleListener;
import com.projectlibre1.workspace.SavableToWorkspace;
import com.projectlibre1.workspace.WorkspaceSetting;

/**
 * This class serves as a moderator between the ChartPanel and the ChartLegend
 */
public class ChartInfo implements Serializable, SelectionNodeListener, ScheduleEventListener, TimeScaleListener, ObjectEvent.Listener, CacheListener, SavableToWorkspace{
	private static final long serialVersionUID = -6593093924980192805L;
	Project project;
	NodeModel nodeModel;
	ChartView chartView;
	
	boolean simple;
	List selectedObjects = new ArrayList();
	ChartModel model;
	ChartLegend chartLegend;
	JFreeChart chart;
	CoordinatesConverter coord;
	List tasks;
	List resources;
	boolean cumulative = false;
	boolean histogram = true;
	boolean selectedOnTop = true;
	boolean work=true;
	Object traces[] = {};	
	TimeChartPanel chartPanel;
	AxisPanel axisPanel;
	NodeModelCache cache = null; // for resources
	private boolean restoring = false;
	/**
	 * 
	 */
	public ChartInfo() {
		super();
	}

	/**
	 * @return Returns the chartView.
	 */
	public ChartView getChartView() {
		return chartView;
	}
	/**
	 * @param chartView The chartView to set.
	 */
	public void setChartView(ChartView chartView) {
		this.chartView = chartView;
	}
	/**
	 * @return Returns the coord.
	 */
	public CoordinatesConverter getCoord() {
		return coord;
	}
	/**
	 * @param coord The coord to set.
	 */
	public void setCoord(CoordinatesConverter coord) {
		if (this.coord != null)
			this.coord.removeTimeScaleListener(this);
		this.coord = coord;
		model = new ChartModel(coord);
		coord.addTimeScaleListener(this);
	}
	/**
	 * @return Returns the cumulative.
	 */
	public boolean isCumulative() {
		return cumulative;
	}

	public boolean isSelectedOnTop() {
		return selectedOnTop;
	}

	/**
	 * @return Returns the histogram.
	 */
	public boolean isHistogram() {
		return histogram;
	}
	/**
	 * @param histogram The histogram to set.
	 */
	public void setHistogram(boolean histogram) {
		this.histogram = histogram;
		updateChart(tasks,resources);		
	}
	/**
	 * @return Returns the model.
	 */
	public ChartModel getModel() {
		return model;
	}
	/**
	 * @param model The model to set.
	 */
	public void setModel(ChartModel model) {
		this.model = model;
	}
	/**
	 * @return Returns the resources.
	 */
	public List getResources() {
		return resources;
	}
	/**
	 * @param resources The resources to set.
	 */
	public void setResources(List resources) {
		this.resources = resources;
	}
	/**
	 * @return Returns the selectedObjects.
	 */
	public List getSelectedObjects() {
		return selectedObjects;
	}
	/**
	 * @param selectedObjects The selectedObjects to set.
	 */
	public void setSelectedObjects(List selectedObjects) {
		this.selectedObjects = selectedObjects;
	}
	/**
	 * @return Returns the simple.
	 */
	public boolean isSimple() {
		return simple;
	}
	/**
	 * @param simple The simple to set.
	 */
	public void setSimple(boolean simple) {
		this.simple = simple;
	}
	/**
	 * @return Returns the tasks.
	 */
	public List getTasks() {
		return tasks;
	}
	/**
	 * @param tasks The tasks to set.
	 */
	public void setTasks(List tasks) {
		this.tasks = tasks;
	}
	/**
	 * @return Returns the traces.
	 */
	public Object[] getTraces() {
		return traces;
	}
	/**
	 * @return Returns the chart.
	 */
	public JFreeChart getChart() {
		return chart;
	}
	/**
	 * @param chart The chart to set.
	 */
	public JFreeChart setChart(JFreeChart chart) {
		this.chart = chart;
		return chart;
	}
	/**
	 * @return Returns the chartPanel.
	 */
	public TimeChartPanel getChartPanel() {
		return chartPanel;
	}
	/**
	 * @param chartPanel The chartPanel to set.
	 */
	public void setChartPanel(TimeChartPanel chartPanel) {
		this.chartPanel = chartPanel;
	}
	
	
	/**
	 * @return Returns the project.
	 */
	public Project getProject() {
		return project;
	}
	/**
	 * @param project The project to set.
	 */
	public void setProject(Project project) {
		if (this.project != null) {
			this.project.removeScheduleListener(this);
			this.project.getResourcePool().removeObjectListener(this);
		}
		this.project = project;
		project.addScheduleListener(this);
		project.getResourcePool().addObjectListener(this);
		nodeModel = project.getResourcePool().getResourceOutline();		
	}
	

		
	/**
	 * @return Returns the nodeModel.
	 */
	public NodeModel getNodeModel() {
		return nodeModel;
	}
	/**
	 * @param nodeModel The nodeModel to set.
	 */
	public void setNodeModel(NodeModel nodeModel) {
		this.nodeModel = nodeModel;
	}
	public void selectionChanged(SelectionNodeEvent e) {
		if (!isVisible())
			return;
		chartLegend.selectionChanged(e); // pass it along
	}
	
	/**
	 * @return Returns the chartLegend.
	 */
	public ChartLegend getChartLegend() {
		return chartLegend;
	}
	/**
	 * @param chartLegend The chartLegend to set.
	 */
	public void setChartLegend(ChartLegend chartLegend) {
		this.chartLegend = chartLegend;
	}

	public void setTraces(Object[] traces) {
		if (traces.length == 0) // will happen if changing from cost to work.  Don't change it
			return;
		this.traces = traces;
		if (chartPanel != null) // the very first time on histogram we need to set traces before chart panel is created
			updateChart(tasks,resources);
	}	
	public void timeScaleChanged(TimeScaleEvent e) {
		if (!isVisible())
			return;
		
		updateChart(tasks,resources);
	}	
	/* (non-Javadoc)
	 * @see com.projectlibre1.pm.scheduling.ScheduleEventListener#scheduleChanged(com.projectlibre1.pm.scheduling.ScheduleEvent)
	 */
	public void scheduleChanged(ScheduleEvent evt) {
		if (!isVisible())
			return;
		
		updateChart(tasks,resources);
	}
	public void setCumulative(boolean cumulative) {
		this.cumulative = cumulative;
		updateChart(tasks,resources);
	}
	public void setSelectedOnTop(boolean multiproject) {
		this.selectedOnTop = multiproject;
	}
	
	public double getFooterHeight() {
		return chartPanel.getNonPlotHeight();
	}
	public double getHeaderHeight() {
		return chartView.getHeaderComponentHeight();
	}
	
	public void updateChart(List tasks, List resources) {
		this.tasks = tasks;
		this.resources = resources;
		if (isSimple())
			model.computeHistogram(getProject(), tasks, resources,traces);
		else
			model.computeValues(tasks,resources, cumulative, traces, histogram);
		chart = chartPanel.buildChart();
		setChart(chart);
		chartPanel.updateChart();
		axisPanel.setAxis(getChart().getXYPlot().getRangeAxis());
		axisPanel.repaint();
	}
	
	/**
	 * @return Returns the axisPanel.
	 */
	public AxisPanel getAxisPanel() {
		return axisPanel;
	}
	/**
	 * @param axisPanel The axisPanel to set.
	 */
	public void setAxisPanel(AxisPanel axisPanel) {
		this.axisPanel = axisPanel;
	}

	/* (non-Javadoc)
	 * @see com.projectlibre1.field.ObjectEvent.Listener#objectChanged(com.projectlibre1.field.ObjectEvent)
	 */
	public void objectChanged(ObjectEvent objectEvent) {
		if (!isVisible())
			return;

		if (objectEvent.getObject() instanceof Resource) {
			chartLegend.rebuildTree(); // take into account different resources
			updateChart(tasks,resources);
		}
	}
	
	boolean isVisible() {
		return chartView.isVisible();
	}

	public void setCache(NodeModelCache cache) {
		if (this.cache == null)
			cache.removeNodeModelListener(this);
		this.cache = cache;
		cache.update();
		cache.addNodeModelListener(this);
	}

	public final NodeModelCache getCache() {
		return cache;
	}

	public void graphicNodesCompositeEvent(CompositeCacheEvent e) {
		if (!isVisible())
			return;
		chartLegend.rebuildTree();
	}

	public void setCumulativeCostMode() {
		cumulative = true;
		histogram = false;
	}
	public boolean isWork() {
		return work;
	}

	public void setWork(boolean work) {
		this.work = work;
	}

	public void restoreWorkspace(WorkspaceSetting w, int context) {
		Workspace ws = (Workspace) w;
		cumulative = ws.cumulative;
		histogram = ws.histogram;
		selectedOnTop = ws.selectedOnTop;
		work = ws.work;
		chartPanel.setVerticalScrolling(ws.verticalScroll);
		chartPanel.verticalScrollingItem.setSelected(ws.verticalScroll);
		if (!simple)
			setTraces(SpreadSheetFieldArray.fromIdArray(ws.traces));
	}
	public WorkspaceSetting createWorkspace(int context) {
		Workspace ws = new Workspace();
		ws.cumulative = cumulative;
		ws.histogram = histogram;
		ws.selectedOnTop = selectedOnTop;
		ws.work = work;
		ws.verticalScroll = chartPanel.isVerticalScrolling();
		if (!simple)
			ws.traces = SpreadSheetFieldArray.toIdArray(traces);
		return ws;
	}

	public static class Workspace implements WorkspaceSetting { 
		private static final long serialVersionUID = -1369065811123053002L;
		boolean cumulative;
		boolean histogram;
		boolean selectedOnTop;
		boolean work;
		Object[] traces;
		boolean verticalScroll;
		public boolean isCumulative() {
			return cumulative;
		}
		public void setCumulative(boolean cumulative) {
			this.cumulative = cumulative;
		}
		public boolean isHistogram() {
			return histogram;
		}
		public void setHistogram(boolean histogram) {
			this.histogram = histogram;
		}
		public boolean isSelectedOnTop() {
			return selectedOnTop;
		}
		public void setSelectedOnTop(boolean selectedOnTop) {
			this.selectedOnTop = selectedOnTop;
		}
		public boolean isWork() {
			return work;
		}
		public void setWork(boolean work) {
			this.work = work;
		}
		public Object[] getTraces() {
			return traces;
		}
		public void setTraces(Object[] traces) {
			this.traces = traces;
		}
		public boolean isVerticalScroll() {
			return verticalScroll;
		}
		public void setVerticalScroll(boolean verticalScroll) {
			this.verticalScroll = verticalScroll;
		}
	}

	public boolean isRestoring() {
		return restoring;
	}

	public void setRestoring(boolean restoring) {
		this.restoring = restoring;
	}

}
