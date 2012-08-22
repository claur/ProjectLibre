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
package com.projity.pm.graphic.chart;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicListUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

import org.apache.commons.collections.Closure;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.grouping.core.NodeList;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.transform.CommonTransformFactory;
import com.projity.grouping.core.transform.ViewConfiguration;
import com.projity.menu.MenuActionConstants;
import com.projity.pm.assignment.HasAssignmentsImpl;
import com.projity.pm.assignment.HasTimeDistributedData;
import com.projity.pm.graphic.IconManager;
import com.projity.pm.graphic.model.cache.GraphicNode;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeEvent;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeListener;
import com.projity.pm.graphic.swing.Util;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.task.Project;
import com.projity.strings.Messages;
import com.projity.toolbar.TransformComboBox;
import com.projity.toolbar.TransformComboBoxModel;
import com.projity.util.Environment;
import com.projity.workspace.SavableToWorkspace;
import com.projity.workspace.WorkspaceSetting;

/**
 *
 */
public class ChartLegend  implements SelectionNodeListener, Serializable , SavableToWorkspace{
	private static final long serialVersionUID = 5098599798868391983L;

	JTree tree;
//	JList traces;

	JCheckBox selectedOnTop;
	JCheckBox cumulative;
	JCheckBox histogram;
	JRadioButton cost;
	JRadioButton work;
	JScrollPane treeScrollPane = null;
	JScrollPane tracesScrollPane = null;
	boolean selecting = false;
	JList workTraces;
	JList costTraces;
	
	boolean simple;
	ChartInfo chartInfo;
	List selectedObjects = new ArrayList();
	List selectedResourcesFromTasks = new ArrayList();
	List selectedResourcesOnTree = new ArrayList();
	TransformComboBox filterComboBox = null;
	JList tracesList;

	public ChartLegend(ChartInfo chartInfo) {
		super();
		this.chartInfo = chartInfo;
		this.simple = chartInfo.isSimple();
	}
	
	void rebuildTree() {
//		System.out.println("rebuilding tree");
		initTree();
//		((AbstractMutableNodeHierarchy)chartInfo.getCache().getReference().getModel().getHierarchy()).dump();
	}
	
	private void initTree() {
//		tree = new JTree(chartInfo.getNodeModel());
		tree = new JTree(chartInfo.getCache());
		tree.setExpandsSelectedPaths(true);
		final JTree finalTree = tree;
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent evt) {
				if (selecting)
					return;
				TreePath[] paths = ((JTree)evt.getSource()).getSelectionPaths(); //evt.getPaths();
				chartInfo.updateChart(selectedObjects,pathsToList(paths));
			}
			
		});
		tree.setCellRenderer(new TreeRenderer());
		if (treeScrollPane == null)
			treeScrollPane = new JScrollPane(tree);
		else
			treeScrollPane.getViewport().add(tree);
	}
	
	JList getListInstance(boolean cost) {
		final JList list = new JList() { // do not want to update the UI. see below also
			private static final long serialVersionUID = 1L;
			public void updateUI() {
				if (!Environment.isNewLook())
					super.updateUI();
			}
		};
		if (Environment.isNewLook()) // The PLAF can override the custom renderer. This avoids that
			list.setUI(new BasicListUI());
		list.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setCellRenderer( new ListRenderer());
		setListFields(list,cost);
		if (!simple) {
			list.setSelectedIndex(0); // start off with first choice selected			
			list.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (chartInfo.isRestoring()) // don't want to listen if updating from workspace
						return;
				    if (e.getValueIsAdjusting() == false) {
				    	chartInfo.setTraces(list.getSelectedValues());
				    }
				}
			});
		}
			
		return list;
	}
	
	public void setControlValues() {
		if (simple) {
			selectedOnTop.setSelected(chartInfo.isSelectedOnTop()); // start off as histogram
		} else {
			cumulative.setSelected(chartInfo.isCumulative()); // start off as histogram
			histogram.setSelected(chartInfo.isHistogram()); // start off as histogram
			work.setSelected(chartInfo.isWork());
			cost.setSelected(!chartInfo.isWork());
			Util.setSelectedValues(tracesList,chartInfo.traces);
		}
		
	}
	void initControls() {
		chartInfo.setAxisPanel(new AxisPanel(chartInfo));
		filterComboBox=new TransformComboBox(null,MenuActionConstants.ACTION_CHOOSE_FILTER,TransformComboBoxModel.FILTER);
		filterComboBox.setView(ViewConfiguration.getView(MenuActionConstants.ACTION_CHARTS));
		filterComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (chartInfo.isRestoring())
					return;
		        TransformComboBox combo = (TransformComboBox) e.getSource();
				CommonTransformFactory factory = (CommonTransformFactory)combo.getSelectedItem();
				((TransformComboBoxModel)combo.getModel()).changeTransform(factory);
			}});
		
		initTree();
		Object[] fields = getFields(false);
		
		workTraces = getListInstance(false);
		tracesList = workTraces; // start off work
		tracesScrollPane = new JScrollPane(workTraces);
		workTraces.setVisibleRowCount(Environment.getStandAlone()?HasTimeDistributedData.tracesCount:HasTimeDistributedData.serverTracesCount);

//		final ViewTransformer transformer=ViewConfiguration.getView(MenuActionConstants.ACTION_CHARTS).getTransform();
//		final ResourceInTeamFilter hiddenFilter=(ResourceInTeamFilter)transformer.getHiddenFilter();		
//		teamResources= new JCheckBox(Messages.getString("Text.ShowTeamResourcesOnly"));
//		teamResources.addItemListener(new ItemListener() {
//			public void itemStateChanged(ItemEvent e) {
//				hiddenFilter.setFilterTeam(e.getStateChange() == ItemEvent.SELECTED);
//				transformer.update();
//			}
//		});
//		teamResources.setSelected(hiddenFilter.isFilterTeam());
		
		
		if (simple) {
			chartInfo.setTraces(fields);
			tree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION); // allow only 1 for histogram
			
			selectedOnTop = new JCheckBox(Messages.getString("Text.ShowSelectedOnTop")); //$NON-NLS-1$
			selectedOnTop.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {

					chartInfo.setSelectedOnTop(e.getStateChange() == ItemEvent.SELECTED);
					Object[] traces = getFields(false);
			    	chartInfo.setTraces(traces);
					workTraces = getListInstance(false);
					tracesScrollPane.getViewport().add(workTraces);
					workTraces.setVisibleRowCount(Environment.getStandAlone()?HasTimeDistributedData.tracesCount:HasTimeDistributedData.serverTracesCount);
				}
			});
			selectedOnTop.setSelected(chartInfo.isSelectedOnTop()); // start off as histogram
			
			return;
		}

		costTraces = getListInstance(true);		
		cumulative = new JCheckBox(Messages.getString("Text.Cumulative")); //$NON-NLS-1$
		cumulative.setSelected(chartInfo.isCumulative()); // start off as histogram
		cumulative.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				chartInfo.setCumulative(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		histogram = new JCheckBox(Messages.getString("Text.Histogram")); //$NON-NLS-1$
		histogram.setSelected(chartInfo.isHistogram()); // start off as histogram
		histogram.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				boolean histogramSelected = e.getStateChange() == ItemEvent.SELECTED;
				chartInfo.setHistogram(histogramSelected);
				if (histogramSelected) {
					workTraces.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION); // allow only 1 for histogram
					costTraces.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION); // allow only 1 for histogram
				} else {
					workTraces.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // allow many
					costTraces.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // allow only 1 for histogram					
				}
			}
		});

		work = new JRadioButton(Messages.getString("Text.work")); //$NON-NLS-1$
		work.setSelected(chartInfo.isWork());
		cost = new JRadioButton(Messages.getString("Text.cost")); //$NON-NLS-1$


		ItemListener costWork = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				boolean isCost = e.getSource() == cost;
				chartInfo.setWork(!isCost);
				tracesList = isCost ? costTraces : workTraces;
				tracesScrollPane.getViewport().add(tracesList);
				if (!chartInfo.isRestoring())
					chartInfo.setTraces(tracesList.getSelectedValues());
			}
		}; 
		cost.addItemListener(costWork);
		work.addItemListener(costWork);
		ButtonGroup group = new ButtonGroup();
		group.add(cost);
		group.add(work);

		// by default, always select first item
		chartInfo.setTraces(new Object[] {fields[0]});
	}


	
	//	 This class is a custom renderer based
	//	 on DefaultTreeCellRenderer
	class TreeRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;
		private Icon activeIcon = IconManager.getIcon("greenCircle");	 //$NON-NLS-1$
		
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			// Allow the original renderer to set up the label
			Component c = super.getTreeCellRendererComponent(tree, value,
					selected, expanded, leaf, row, hasFocus);

			if (selectedResourcesFromTasks.contains( ((GraphicNode)value).getNode().getImpl() )) {
				setIcon(activeIcon);
			}

			return c;
		}
	} 	
	
	class ListRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = 1L;
	   public ListRenderer () {
	       // Don't paint behind the component
	       setOpaque(true);
	   }

	   public Component getListCellRendererComponent(JList list,
	         Object value, // value to display
	         int index,    // cell index
	         boolean iss,  // is selected
	         boolean chf) { // cell has focus?
	   		setText(value.toString());
	   		if (iss) {
	   			Color color = ChartHelper.getColorForField(value);
	   			setBackground(color);
	   			if (color.getRed() + color.getGreen() + color.getBlue() < 450) // draw dark with white foreground
	   				setForeground(Color.white);
	   			else
		   			setForeground(list.getForeground());
	   				
	   		} else {
	   			setBackground(list.getBackground());
	   			setForeground(list.getForeground());
	   		}
	         // Set a border if the 
	         //list item is selected
	        if (iss) {
	            setBorder(BorderFactory.createLineBorder(
	              Color.black, 1));
	        } else {
	            setBorder(BorderFactory.createLineBorder(
	             list.getBackground(), 1));
	        }

	   		
	   		return this;
	   }
	}
	
	private List pathsToList(TreePath[] paths) {
		List list = new ArrayList();
		if (paths != null) {
			for (int i=0; i < paths.length; i++) {
				list.add(((GraphicNode)paths[i].getLastPathComponent()).getNode().getImpl());
			}
		}
		return list;
	}
	
	
	public JComponent createContentPanel() {
		// Separating the component initialization and configuration
		// from the layout code makes both parts easier to read.
		initControls();
		FormLayout layout = new FormLayout("p:grow, 3dlu,100dlu:grow,5dlu, default, 5dlu", // cols //$NON-NLS-1$
				"p, 3dlu, p, 3dlu, p, 3dlu, " + (simple ? "" : "fill:") + "p:grow, 5dlu"); // rows		 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setBorder(BorderFactory.createEmptyBorder(0, 5 ,0 ,0)); 
		CellConstraints cc = new CellConstraints();
		
		builder.addLabel(Messages.getString("ChartLegend.ResourceFilter"),		cc.xy  (1,1)); //$NON-NLS-1$
		builder.add(filterComboBox,		cc.xy  (3,1));
		builder.add(treeScrollPane,	cc.xywh(1,3,3,5));
		builder.add(tracesScrollPane		,cc.xy  (5,7));
		if (simple){
			builder.add(selectedOnTop,				cc.xy  (5,1));
		}else{
			builder.add(cumulative,				cc.xy  (5,1));
			builder.add(histogram,				cc.xy  (5,3));		
			builder.add(workCostRadioPanel(),			cc.xy  (5,5));
		}
		return builder.getPanel();
	}
	
	public Component workCostRadioPanel() {
		JPanel panel = new JPanel();
		panel.add(work);
		panel.add(cost);
		return panel;
	}
 	private Object[] getFields(boolean cost) {
 		if (simple) {
 			return chartInfo.isSelectedOnTop() ? (Environment.getStandAlone()?HasTimeDistributedData.histogramTypes:HasTimeDistributedData.serverHistogramTypes)  : (Environment.getStandAlone()?HasTimeDistributedData.reverseHistogramTypes:HasTimeDistributedData.serverReverseHistogramTypes);
 		}
 		return cost ? HasTimeDistributedData.costTypes : HasTimeDistributedData.workTypes;
 	}
	
	private void setListFields(JList list, boolean cost) {
		Object[] items = getFields(cost);
		list.setListData(items);
		list.setVisibleRowCount(items.length);
		if (simple) {
			list.setSelectionInterval(0,items.length);
			list.setEnabled(false); // in simple mode, no selection allowed
		}
	}

	private List getListFromNodeList(List nodes) {
		List implList =  NodeList.nodeListToImplList(nodes);
		// normally it is tasks or resources, but if project, make sure its works too
		if (implList.isEmpty() || !(implList.get(0) instanceof Project)) 
			return implList;
	
		final List resultList = new ArrayList();
		Iterator i = implList.iterator();
		while (i.hasNext()) {
			((Project)i.next()).forTasks(new Closure(){
				public void execute(Object arg0) {
					resultList.add(arg0);
				}
			});
			//resultList.addAll( ((Project)i.next()).getTasks());
		}
		return resultList;
	}
	public void selectionChanged(SelectionNodeEvent e) {
		
		if (!chartInfo.isVisible())
			return;
		
		List nodes = e.getNodes();
		selectedObjects = getListFromNodeList(nodes);
		List resList = extractResources(selectedObjects);
		if (resList.isEmpty())
			selectedResourcesFromTasks = HasAssignmentsImpl.extractOppositeList(selectedObjects,false);
		else {
			selectedResourcesFromTasks = resList; // top view is resource list
			selectedObjects = null;
		}
		setTreeSelection(selectedResourcesFromTasks);
		chartInfo.updateChart(selectedObjects,selectedResourcesOnTree);
	}
	
	private List extractResources(List list) {
		ArrayList resList = new ArrayList();
		Iterator i = list.iterator();
		Object obj;
		while (i.hasNext()) {
			obj = i.next();
			if (obj instanceof Resource)
				resList.add(obj);
		}
		return resList;
	}
	private void setTreeSelection(List resources) {
		selecting = true;
		selectedResourcesOnTree.clear();
		selectedResourcesOnTree.addAll(resources);
		selectedResourcesOnTree.remove(ResourceImpl.getUnassignedInstance());
		int[] sel = new int[selectedResourcesOnTree.size()]; // if simple can only select 1
		Object resource;
		NodeModel nodeModel = chartInfo.getNodeModel();
		
		int topRow = Integer.MAX_VALUE;
		Object topResource = null;
		for (int i = 0; i < selectedResourcesOnTree.size(); i++) {
			resource = selectedResourcesOnTree.get(i);
			int row = nodeModel.getHierarchy().getIndexOfNode(nodeModel.search(resource),false);
			sel[i] = row;
			if (row < topRow) {
				topRow = row;
				topResource = resource;
			}
		}
		tree.clearSelection();		
		tree.setExpandsSelectedPaths(true);		
		if (simple && topResource != null) {
			tree.setSelectionRow(topRow);
			selectedResourcesOnTree.clear();
			selectedResourcesOnTree.add(topResource);
		} else {
			tree.setSelectionRows(sel);
		}
		tree.setExpandsSelectedPaths(true);
		tree.repaint();
		selecting = false;
	}

	public WorkspaceSetting createWorkspace(int context) {
		Workspace ws = new Workspace();
		ws.treeViewPosition = treeScrollPane.getViewport().getViewPosition();
		ws.tracesViewPosition = tracesScrollPane.getViewport().getViewPosition();
		return ws;
	}

	public void restoreWorkspace(WorkspaceSetting w, int context) {
		Workspace ws = (Workspace) w;
		treeScrollPane.getViewport().setViewPosition(ws.treeViewPosition);
		tracesScrollPane.getViewport().setViewPosition(ws.tracesViewPosition);
	}
	public static class Workspace implements WorkspaceSetting {
		private static final long serialVersionUID = -8581691622116505516L; 
		Point treeViewPosition;
		Point tracesViewPosition;
		public Point getTracesViewPosition() {
			return tracesViewPosition;
		}
		public void setTracesViewPosition(Point tracesViewPosition) {
			this.tracesViewPosition = tracesViewPosition;
		}
		public Point getTreeViewPosition() {
			return treeViewPosition;
		}
		public void setTreeViewPosition(Point treeViewPosition) {
			this.treeViewPosition = treeViewPosition;
		}
	}	
}
