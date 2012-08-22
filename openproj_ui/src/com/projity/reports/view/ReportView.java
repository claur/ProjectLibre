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
package com.projity.reports.view;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import com.projity.configuration.Dictionary;
import com.projity.configuration.ReportDefinition;
import com.projity.configuration.ScriptConfiguration;
import com.projity.contrib.ClassResolverFilter;
import com.projity.field.Field;
import com.projity.graphic.configuration.SpreadSheetFieldArray;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.transform.filtering.PredicatedNodeFilterIterator;
import com.projity.help.HelpUtil;
import com.projity.pm.graphic.frames.DocumentFrame;
import com.projity.pm.graphic.frames.GraphicManager;
import com.projity.pm.graphic.model.cache.GeneralFilteredIterator;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.model.cache.NodeModelCacheFactory;
import com.projity.pm.graphic.model.cache.ReferenceNodeModelCache;
import com.projity.pm.graphic.model.event.CacheListener;
import com.projity.pm.graphic.model.event.CompositeCacheEvent;
import com.projity.pm.graphic.spreadsheet.SpreadSheet;
import com.projity.pm.graphic.timescale.CoordinatesConverter;
import com.projity.pm.graphic.views.BaseView;
import com.projity.pm.task.Project;
import com.projity.reports.adapter.DataSource;
import com.projity.reports.adapter.DataSourceProvider;
import com.projity.reports.adapter.ReportUtil;
import com.projity.reports.adapter.ReportViewer;
import com.projity.strings.Messages;
import com.projity.undo.UndoController;
import com.projity.util.Environment;
import com.projity.workspace.WorkspaceSetting;

/**
 *
 */
public class ReportView extends JPanel implements BaseView, CacheListener {
	private static final long serialVersionUID = 5457040745964404658L;
	protected JPanel report;
	protected Project project;
	private ReportViewer viewer = null;
	private DocumentFrame documentFrame;
	private boolean xmlFile = true;
	private ReportDefinition reportDefinition = null;
	JLabel reportLabel;
	JComboBox reportChoice;
	JLabel columnsLabel;
	JComboBox columnsChoice;
	CoordinatesConverter coord;
	SpreadSheetFieldArray fieldArray = null;
	BorderLayout layout = new java.awt.BorderLayout();
	NodeModelCache cache;
	private boolean initializing;
	boolean dirty = true;
	NodeModelCache taskCache = null;
	NodeModelCache resourceCache = null;
	private String viewName = DataSourceProvider.TASK_REPORT_VIEW;// initial report is task based
	private Closure transformerClosure;
	/**
	 * 
	 */
	public ReportView(DocumentFrame documentFrame) {
		super();
		this.documentFrame = documentFrame;
		this.project = documentFrame.getProject();
		HelpUtil.addDocHelp(this,"Report_View");
		//Note that the hook to the filter/sort/group combos is done in makeViewer according to the report type
		if (!Environment.getStandAlone()){
			try{
				Class.forName("bsh.BshClassManager").getMethod("setClassResolverFilter", new Class[]{ClassResolverFilter.class}).invoke(null, new Object[]{ScriptConfiguration.getInstance()});
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		//BshClassManager.setClassResolverFilter(ScriptConfiguration.getInstance());
		transformerClosure=documentFrame.addTransformerInitializationClosure();
	}

	public void cleanUp() {
	    if (cache != null)
	    	cache.removeNodeModelListener(this);
		report = null;
		project = null;
		viewer = null;
		documentFrame = null;
		reportDefinition = null;
		reportLabel = null;
		reportChoice = null;
		columnsLabel = null;
		columnsChoice = null;
		coord = null;
		fieldArray = null;
		layout = null;
		cache = null;
		taskCache = null;
		resourceCache = null;
		viewName = null;
	}
	private NodeModelCache newFilteredCache(ReferenceNodeModelCache cache, String viewName) {
		NodeModelCache c = NodeModelCacheFactory.getInstance().createFilteredCache(cache,viewName,transformerClosure);
		c.update();
		return c;
	}
	

	private NodeModel updateCacheForView(String viewName) {
		if (viewName.equals(DataSourceProvider.TASK_REPORT_VIEW)) {
			if (taskCache == null)
				taskCache = newFilteredCache((ReferenceNodeModelCache)documentFrame.getTaskNodeModelCache(),viewName);
			cache = taskCache;
		} else if (viewName.equals(DataSourceProvider.RESOURCE_REPORT_VIEW)) {
			if (resourceCache == null)
				resourceCache = newFilteredCache((ReferenceNodeModelCache)documentFrame.getResourceNodeModelCache(),viewName);
			cache = resourceCache;
		} else if (viewName.equals(DataSourceProvider.PROJECT_REPORT_VIEW)) {
			cache = null;
			return GraphicManager.getInstance(this).getProjectFactory().getPortfolio().getNodeModel();
		}
	    return cache.getModel();

	}
	
	private void showReport() {
		documentFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			makeViewer();
		} catch (JRException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		documentFrame.setCursor(Cursor.getDefaultCursor());
		
	}
	private void makeViewer() throws JRException{
		if (!dirty)
			return;
		
		documentFrame.showWaitCursor(true);

		if (cache != null) { // remove old listener
		    cache.removeNodeModelListener(this);
		}
				   
        DataSource dataSource;

        SpreadSheetFieldArray fa = null;
        if (fieldArray != null) {
        	fa =(SpreadSheetFieldArray) fieldArray.clone();
        	CollectionUtils.filter(fa,new Predicate() {
        		public boolean evaluate(Object arg0) {
        			return !((Field)arg0).isGraphical(); // get rid of fields that can't be shown
        		}});
        }
        JasperReport report = ReportUtil.getReport(reportDefinition, coord.getProjectTimeIterator(), fa);

        viewName = DataSourceProvider.getViewName(report);
        //System.out.println("viewName="+viewName);
        documentFrame.setComboBoxesViewName(viewName); 

        NodeModel model = null;
        PredicatedNodeFilterIterator iterator;
        if (viewName == DataSourceProvider.REPORT_VIEW) { // special case to just use project
        	cache = null;
			ArrayList list = new ArrayList();
			list.add(project);
        	iterator = GeneralFilteredIterator.instance(list.iterator());
        } else {
        	model = updateCacheForView(viewName);
            if (cache == null){
            	iterator = GeneralFilteredIterator.instance(model.iterator());
            	//for (Iterator i=GeneralFilteredIterator.instance(model.iterator());i.hasNext();) System.out.println("Report model iterator: "+i.next());
            }else{ 
            	iterator = GeneralFilteredIterator.instance(cache.getIterator());
            	//for (Iterator i=GeneralFilteredIterator.instance(cache.getIterator());i.hasNext();) System.out.println("Report cache iterator: "+i.next());
            }
        }
        dataSource = DataSourceProvider.createDataSource(report,project,iterator,model);

        
        // projet name is used as report's title
        // and passed as a parameter
        HashMap params = new HashMap();
        params.put("projectName", project.getName()); //$NON-NLS-1$
        
		JasperPrint jasperPrint = JasperFillManager.fillReport(report, params, dataSource);
		if (viewer != null) {
			viewer.changeReport(jasperPrint);
		} else {
			viewer =  new ReportViewer(jasperPrint);
			add(viewer,BorderLayout.CENTER);
		}
		// add new listener
		if (cache != null) {
		    cache.addNodeModelListener(this);
		}
		dirty = false;
		
		documentFrame.showWaitCursor(false);

	}
	
	private JPanel header() {
		JPanel panel = new JPanel();
		panel.add(reportLabel);
		panel.add(reportChoice);
		panel.add(columnsLabel);
		panel.add(columnsChoice);
		return panel;
	}
	
	private void initColumns() {
		String ssFields = reportDefinition.getMainSpreadsheetCategory();
		if (ssFields == null || ssFields.equals("assignmentSpreadsheet")) { //$NON-NLS-1$
			columnsChoice.setVisible(false);
			columnsLabel.setVisible(false);
			return;
		}
		columnsChoice.setVisible(true);
		columnsLabel.setVisible(true);
		DefaultComboBoxModel model = new DefaultComboBoxModel(Dictionary.getAll(ssFields));
		columnsChoice.setModel(model);
		columnsChoice.setSelectedItem(fieldArray);
	}
	public void init(CoordinatesConverter coord)  {
		initializing = true;
		this.coord = coord;
		setLayout(layout);
		reportLabel = new JLabel(Messages.getString("ReportView.Report")); //$NON-NLS-1$
		reportChoice = new JComboBox(ReportUtil.getReportDefinitions());
		reportChoice.setSelectedIndex(0);
		reportDefinition = (ReportDefinition) reportChoice.getSelectedItem();
		fieldArray = reportDefinition.getMainFieldArray();
		reportChoice.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				reportDefinition = (ReportDefinition) ((JComboBox)arg0.getSource()).getSelectedItem();
				fieldArray = reportDefinition.getMainFieldArray();
				dirty = true;
				showReport();
				initColumns();
			}});
		
		columnsLabel= new JLabel(Messages.getString("ReportView.Columns")); //$NON-NLS-1$
		columnsChoice = new JComboBox();
		initColumns();
		columnsChoice.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				fieldArray = (SpreadSheetFieldArray) ((JComboBox)arg0.getSource()).getSelectedItem();
				dirty = true;
				showReport();
			}});
		add(header(), BorderLayout.PAGE_START);
		showReport();
		initializing = false;
	}

	public void graphicNodesCompositeEvent(CompositeCacheEvent compositeEvent){
		if (initializing)
			return;
		dirty = true;
		if (!isShowing())
			return;
		if (!isVisible()) { // set it as dirty and recalculate when shown because it's expensive to recalc report if not shown
			return;
		}
		showReport();
	}
	

	public UndoController getUndoController() {
		return null;
	}

	public void zoomIn() {
		viewer.zoomIn();
	}

	public void zoomOut() {
		viewer.zoomOut();
	}
	public boolean canZoomIn() {
		return true;
	}
	public boolean canZoomOut() {
		return true;
	}
	public int getScale() {
		return -1;
	}
	public SpreadSheet getSpreadSheet() {
		return null;
	}
	public boolean hasNormalMinWidth() {
		return true;
	}
	public String getViewName() {
		return viewName;
	}
	public boolean showsTasks() {
		return false;
	}
	public boolean showsResources() {
		return false;
	}
	/**
	 * Because it can be expensive to recalc a report, if the report is not visible, it is only recalced when made visible
	 */
	public void onActivate(boolean activate) {
		if (activate)
			showReport();
	}
	public boolean isPrintable() {
		return false;
	}

	public void restoreWorkspace(WorkspaceSetting w, int context) {
		Workspace ws = (Workspace)w;
		if (ws.reportName != null) {
			ReportDefinition def = ReportUtil.getFromName(ws.reportName);
			if (def != null)
				reportChoice.setSelectedItem(def);
		}
		if (ws.fieldArrayName != null) {
			SpreadSheetFieldArray s = (SpreadSheetFieldArray) Dictionary.get(reportDefinition.getMainSpreadsheetCategory(),ws.fieldArrayName);
			if (s != null)
				columnsChoice.setSelectedItem(s);
		}
	}
	public WorkspaceSetting createWorkspace(int context) {
		Workspace ws = new Workspace();
		if (reportDefinition != null)
			ws.reportName = reportDefinition.getName();
		if (fieldArray != null)
			ws.fieldArrayName = fieldArray.toString();
		return ws;
	}

	public static class Workspace implements WorkspaceSetting  {
		private static final long serialVersionUID = -7768176701769503845L;
		//TODO Zoom not set - a bit of a pain to do
		String fieldArrayName = null;
		String reportName = null;
		public String getFieldArrayName() {
			return fieldArrayName;
		}
		public void setFieldArrayName(String fieldArrayName) {
			this.fieldArrayName = fieldArrayName;
		}
		public String getReportName() {
			return reportName;
		}
		public void setReportName(String reportName) {
			this.reportName = reportName;
		}
	}

	public boolean canScrollToTask() {
		// TODO Auto-generated method stub
		return false;
	}

	public void scrollToTask() {
		// TODO Auto-generated method stub
		
	}
	
	public NodeModelCache getCache(){
		return cache;
	}
	
	
}
