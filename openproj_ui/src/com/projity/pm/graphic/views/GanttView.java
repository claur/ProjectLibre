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
package com.projity.pm.graphic.views;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.projity.configuration.Configuration;
import com.projity.configuration.Dictionary;
import com.projity.configuration.Settings;
import com.projity.field.FieldContext;
import com.projity.graphic.configuration.BarStyles;
import com.projity.graphic.configuration.CellStyle;
import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.graphic.configuration.SpreadSheetCategories;
import com.projity.graphic.configuration.SpreadSheetFieldArray;
import com.projity.grouping.core.model.NodeModel;
import com.projity.help.HelpUtil;
import com.projity.menu.MenuActionConstants;
import com.projity.menu.MenuManager;
import com.projity.pm.graphic.frames.DocumentFrame;
import com.projity.pm.graphic.gantt.Gantt;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.model.cache.NodeModelCacheFactory;
import com.projity.pm.graphic.model.cache.ReferenceNodeModelCache;
import com.projity.pm.graphic.spreadsheet.SpreadSheet;
import com.projity.pm.graphic.spreadsheet.SpreadSheetModel;
import com.projity.pm.graphic.spreadsheet.SpreadSheetUtils;
import com.projity.pm.graphic.timescale.CoordinatesConverter;
import com.projity.pm.graphic.timescale.ScaledScrollPane;
import com.projity.pm.graphic.views.synchro.Synchronizer;
import com.projity.pm.scheduling.ScheduleEvent;
import com.projity.pm.scheduling.ScheduleEventListener;
import com.projity.pm.snapshot.Snapshottable;
import com.projity.pm.task.Project;
import com.projity.pm.task.Task;
import com.projity.pm.task.TaskSnapshot;
import com.projity.pm.time.HasStartAndEnd;
import com.projity.strings.Messages;
import com.projity.undo.UndoController;
import com.projity.workspace.WorkspaceSetting;

/**
 *
 */
public class GanttView extends SplittedView implements BaseView, ScheduleEventListener{
	/**
	 *
	 */
	private static final long serialVersionUID = 514828655690086836L;
	protected SpreadSheet spreadSheet;
	protected Gantt gantt;
    protected SortedSet baseLines=new TreeSet();

    protected ScaledScrollPane ganttScrollPane;
	protected NodeModel model;
	protected NodeModelCache cache;
	protected CoordinatesConverter coord;
	private Project project;
	DocumentFrame documentFrame;
	FieldContext fieldContext;
	CellStyle cellStyle;
	private boolean tracking = false;
	public static final String spreadsheetCategory=taskSpreadsheetCategory;



	/**
	 * @param project
	 * @param manager
	 *
	 */
	public GanttView(DocumentFrame documentFrame, MenuManager manager, Synchronizer synchronizer) {
		super(synchronizer);
		this.documentFrame = documentFrame;
		this.project = documentFrame.getProject();
		HelpUtil.addDocHelp(this,"Gantt_Chart");
		setNeedVoidBar(true);
		//setScaled(true);
	}
	public void init(ReferenceNodeModelCache cache, NodeModel model,CoordinatesConverter coord){
		this.coord=coord;
		this.cache=NodeModelCacheFactory.getInstance().createFilteredCache((ReferenceNodeModelCache)cache,getViewName(),null);

		fieldContext = new FieldContext();
		fieldContext.setLeftAssociation(true);
		/*cellStyle=new CellStyle(){
			CellFormat cellProperties=new CellFormat();
			public CellFormat getCellProperties(GraphicNode node){
				cellProperties.setBold(node.isSummary());
				cellProperties.setItalic(node.isAssignment());
				//cellProperties.setBackground((node.isAssignment())?"NORMAL_LIGHT_YELLOW":"NORMAL_YELLOW");
				cellProperties.setCompositeIcon(node.isComposite());
				return cellProperties;
			}

		};*/
		super.init();
		updateHeight(project);
		updateSize();

		//sync the height of spreadsheet and gantt
		leftScrollPane.getViewport().addChangeListener(new ChangeListener(){
			private Dimension olddl=null;
			public void stateChanged(ChangeEvent e){
				Dimension dl=leftScrollPane.getViewport().getViewSize();
				if (dl.equals(olddl)) return;
				olddl=dl;
//				Dimension dr=rightScrollPane.getViewport().getViewSize();
//				((Gantt)rightScrollPane.getViewport().getView()).setPreferredSize(new Dimension((int)dr.getWidth(),(int)dl.getHeight()));
//				rightScrollPane.getViewport().revalidate();
				((Gantt)rightScrollPane.getViewport().getView()).setPreferredSize(new Dimension(rightScrollPane.getViewport().getViewSize().width,dl.height));
			}
		});


		//TODO automatic scrolling to add as an option
//		spreadSheet.getRowHeader().getSelectionModel().addListSelectionListener(new ListSelectionListener(){
//			public void valueChanged(ListSelectionEvent e) {
//				if (!e.getValueIsAdjusting()&&spreadSheet.getRowHeader().getSelectedRowCount()==1){
//					List impls=spreadSheet.getSelectedNodesImpl();
//					if (impls.size()!=1) return;
//					Object impl=impls.get(0);
//					if (!(impl instanceof HasStartAndEnd)) return;
//					HasStartAndEnd interval=(HasStartAndEnd)impl;
//					gantt.scrollToTask(interval, true);
//				}
//			}
//		});


		cache.update();

		//Call this last to be sure everything is initialized
		//gantt.insertCacheData(); //useless?

	}
	public void cleanUp() {
		super.cleanUp();
		coord.removeTimeScaleListener(ganttScrollPane);
		project.removeScheduleListener(this);
		spreadSheet.cleanUp();
		gantt.cleanUp();
		spreadSheet=null;
		gantt=null;
	    baseLines=null;
	    ganttScrollPane=null;
		model=null;
		cache=null;
		coord=null;
		project=null;
		documentFrame=null;
		fieldContext=null;
		cellStyle=null;
	}

	public void setBarStyles(String styleName) {
		if (gantt == null)
			return;
		gantt.setBarStyles((BarStyles) Dictionary.get(BarStyles.category,styleName));
	}

    protected JScrollPane createLeftScrollPane() {
        spreadSheet = new SpreadSheet();
        spreadSheet.setName(project.getName());
		spreadSheet.setSpreadSheetCategory(spreadsheetCategory); // for columns.  Must do first
		SpreadSheetFieldArray fields=getFields();
		if (project.getFieldArray() != null) {
			fields = project.getFieldArray();
		}
		spreadSheet.setCache(cache,fields,fields.getCellStyle(),fields.getActionList());
		if (project.getFieldArray() != null)
			spreadSheet.setFieldArrayWithWidths(fields);
		((SpreadSheetModel)spreadSheet.getModel()).setFieldContext(fieldContext);
		project.removeScheduleListener(this); // in case was already attached and recreating (applet)
		project.addScheduleListener(this);
		if (project.isReadOnly())
			spreadSheet.setReadOnly(true);

		return SpreadSheetUtils.makeSpreadsheetScrollPane(spreadSheet);
   }
   protected JScrollPane createRightScrollPane() {
		gantt=new Gantt(project,"Gantt");
		gantt.setCache(cache);
		gantt.setBarStyles((BarStyles) Dictionary.get(BarStyles.category,"standard"));
		ganttScrollPane=new ScaledScrollPane(gantt,coord,documentFrame,spreadSheet.getRowHeight());
		return ganttScrollPane;
    }

   public void activateEmptyRowHeader(boolean activate){
    ganttScrollPane.activateEmptyRowHeader(activate);
   }

	//spreadsheet fields
	private static SpreadSheetFieldArray getFields() {
		return (SpreadSheetFieldArray) Dictionary.get(spreadsheetCategory,Messages.getString("Spreadsheet.Task.entry")); //TODO don't hardcode
	}
	/**
	 *
	 * @param name
	 * @return old field array
	 */
	public ArrayList setColumns(String name){
		ArrayList old = spreadSheet.getFieldArray();
		setColumns((ArrayList) Dictionary.get(spreadsheetCategory,Messages.getString(name)));
		return old;
	}
	public void setColumns(ArrayList fields){
		spreadSheet.setFieldArray(fields);
	}
	/**
	 * @return Returns the spreadSheet.
	 */
	public SpreadSheet getSpreadSheet() {
		return spreadSheet;
	}



	public void updateHeight(Integer snapshotId, boolean add){
		if (add)
			baseLines.add(snapshotId);
		else baseLines.remove(snapshotId);
		int num=(baseLines.size()==0)?0:(((Integer)baseLines.last()).intValue()+1);
		int rowHeight=GraphicConfiguration.getInstance().getRowHeight()
				+num*GraphicConfiguration.getInstance().getBaselineHeight();
		spreadSheet.setRowHeight(rowHeight);
		gantt.setRowHeight(rowHeight);
	}

	public void updateHeight(Project project){
	    baseLines.clear();
	    int rowHeight=project.getRowHeight(baseLines);
//        for (Iterator i=project.getTaskOutlineIterator();i.hasNext();){
//            Task task=(Task)i.next();
//            int current=Snapshottable.CURRENT.intValue();
//            for (int s=0;s<Settings.numGanttBaselines();s++){
//                if (s==current) continue;
//                TaskSnapshot snapshot=(TaskSnapshot)task.getSnapshot(new Integer(s));
//                if (snapshot!=null) baseLines.add(new Integer(s));
//            }
//        }
//		int num=(baseLines.size()==0)?0:(((Integer)baseLines.last()).intValue()+1);
//		int rowHeight=GraphicConfiguration.getInstance().getRowHeight()
//				+num*GraphicConfiguration.getInstance().getBaselineHeight();
		spreadSheet.setRowHeight(rowHeight);
		gantt.setRowHeight(rowHeight);
	}

	public void scheduleChanged(ScheduleEvent evt) {
		if (evt.getType() == ScheduleEvent.SCHEDULE) {
			//gantt.updateSize(); //done throught cache
		} else if (evt.getType() == ScheduleEvent.ACTUAL) {
		} else if (evt.getType() == ScheduleEvent.BASELINE){
		    updateHeight(evt.getSnapshot(),evt.isSaveSnapshot());
		    //Warning: listeners order is important.
		    //This one must be before GanttModel one which calls updateAll after the height is setted
		}
	}

	public void updateSize(){
		gantt.updateSize();
	}
	public UndoController getUndoController() {
		return project.getUndoController();
	}
	public void zoomIn() {
		coord.zoomIn();
	}
	public void zoomOut() {
		coord.zoomOut();
	}
	public boolean canZoomIn() {
		return coord.canZoomIn();
	}
	public boolean canZoomOut() {
		return coord.canZoomOut();
	}
	public int getScale() {
		return coord.getTimescaleManager().getCurrentScaleIndex();
	}
	public boolean hasNormalMinWidth() {
		return true;
	}
	public String getViewName() {
		return MenuActionConstants.ACTION_GANTT;
	}
	public boolean showsTasks() {
		return true;
	}
	public boolean showsResources() {
		return false;
	}
	public void onActivate(boolean activate) {
	}



	public Gantt getGantt() {
		return gantt;
	}
	public boolean isPrintable() {
		return true;
	}

	public void reinitialize() { // applet
		createLeftScrollPane();
	}
	public void restoreWorkspace(WorkspaceSetting w, int context) {
		Workspace ws = (Workspace) w;
		spreadSheet.restoreWorkspace(ws.spreadSheet, context);
		ganttScrollPane.restoreWorkspace(ws.scrollPane, context);
		setDividerLocation(ws.dividerLocation);
	}
	public WorkspaceSetting createWorkspace(int context) {
		Workspace ws = new Workspace();
		ws.spreadSheet = spreadSheet.createWorkspace(context);
		ws.scrollPane = ganttScrollPane.createWorkspace(context);
		ws.dividerLocation = getDividerLocation();
		return ws;
	}

	public static class Workspace implements WorkspaceSetting {
		private static final long serialVersionUID = -407561451956813994L;
		WorkspaceSetting spreadSheet;
		WorkspaceSetting scrollPane;
		int dividerLocation;
		public WorkspaceSetting getSpreadSheet() {
			return spreadSheet;
		}
		public void setSpreadSheet(WorkspaceSetting spreadSheet) {
			this.spreadSheet = spreadSheet;
		}
		public WorkspaceSetting getScrollPane() {
			return scrollPane;
		}
		public void setScrollPane(WorkspaceSetting scrollPane) {
			this.scrollPane = scrollPane;
		}
		public int getDividerLocation() {
			return dividerLocation;
		}
		public void setDividerLocation(int dividerLocation) {
			this.dividerLocation = dividerLocation;
		}
	}

	public boolean canScrollToTask() {
		return true;
	}
	public void scrollToTask() {
		List impls=spreadSheet.getSelectedNodesImpl();
		if (impls.size()==0) return;
		Object impl=impls.get(0);
		if (!(impl instanceof HasStartAndEnd)) return;
		HasStartAndEnd interval=(HasStartAndEnd)impl;
		gantt.scrollToTask(interval, false);
	}

	public NodeModelCache getCache(){
		return cache;
	}
	public boolean isTracking() {
		return tracking;
	}
	public void setTracking(boolean tracking) {
		this.tracking = tracking;
		HelpUtil.addDocHelp(this,tracking ? "Tracking_Gantt_Chart":"Gantt_Chart");
	}

}
