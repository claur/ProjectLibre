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
package com.projity.dialog;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.association.AssociationList;
import com.projity.configuration.Settings;
import com.projity.dialog.util.FieldComponentMap;
import com.projity.grouping.core.model.NodeModel;
import com.projity.help.HelpUtil;
import com.projity.menu.MenuActionConstants;
import com.projity.pm.costing.CostRateTable;
import com.projity.pm.graphic.frames.DocumentFrame;
import com.projity.pm.graphic.frames.DocumentSelectedEvent;
import com.projity.pm.graphic.frames.GraphicManager;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.spreadsheet.SpreadSheet;
import com.projity.pm.graphic.spreadsheet.SpreadSheetModel;
import com.projity.pm.graphic.spreadsheet.SpreadSheetUtils;
import com.projity.pm.graphic.views.UsageDetailView;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.strings.Messages;

/**
 *
 */
public class ResourceInformationDialog extends InformationDialog {
	private static final long serialVersionUID = 1L;

	public static ResourceInformationDialog getInstance(Frame owner, Resource resource) {
		return new ResourceInformationDialog(owner, resource);
	}

	private ResourceInformationDialog(Frame owner, Resource resource) {
		super(owner, Messages.getString("ResourceInformationDialog.ResourceInformation")); //$NON-NLS-1$
		setObjectClass(Resource.class);
		setObject(resource);
		addDocHelp("Resource_Information_Dialog");
	}
 
	private JTabbedPane resourceTabbedPane;
	private int notesTabIndex;
	private JLabel availabilityLabel;
	private JButton changeWorkingTimeButton;

	public JComponent createContentPanel() {	
		FormLayout layout = new FormLayout("400dlu:grow","fill:275dlu:grow"); //$NON-NLS-1$ //$NON-NLS-2$
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		resourceTabbedPane= new JTabbedPane();
		JComponent generalTab=createGeneralPanel();
		JComponent costsTab=createCostsPanel();
		JComponent availabilityTab=createAvailabilityPanel();

		resourceTabbedPane.addTab(Messages.getString("ResourceInformationDialog.General"),generalTab); //$NON-NLS-1$
		resourceTabbedPane.addTab(Messages.getString("ResourceInformationDialog.Costs"),costsTab); //$NON-NLS-1$
		resourceTabbedPane.addTab(Messages.getString("ResourceInformationDialog.ResourceAvailability"),availabilityTab); //$NON-NLS-1$
		resourceTabbedPane.addTab(Messages.getString("ResourceInformationDialog.Tasks"),createTasksPanel()); //$NON-NLS-1$
		String notes = Messages.getString("ResourceInformationDialog.Notes"); //$NON-NLS-1$
		resourceTabbedPane.addTab(notes,createNotesPanel());
		notesTabIndex =resourceTabbedPane.indexOfTab(notes);
		
		builder.add(resourceTabbedPane);
		mainComponent = resourceTabbedPane;
		return builder.getPanel();
	}
	

	public void showNotes() {
		resourceTabbedPane.setSelectedIndex(notesTabIndex);
		
	}

	
	public JComponent createGeneralPanel(){
		FieldComponentMap map = createMap();

		FormLayout layout = new FormLayout(
        "p, 3dlu, 160dlu, 3dlu, p, 3dlu, p:grow", //$NON-NLS-1$
		  "p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,0dlu,p"); //$NON-NLS-1$
	
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		map.append(builder,"Field.name"); //$NON-NLS-1$
		map.append(builder,"Field.initials"); //$NON-NLS-1$
	   	builder.nextLine(2);
		map.append(builder,"Field.emailAddress"); //$NON-NLS-1$
		map.append(builder,"Field.group"); //$NON-NLS-1$
		builder.nextLine(2);	
		map.append(builder,"Field.rbsCode"); //$NON-NLS-1$
		map.append(builder,"Field.generic"); //$NON-NLS-1$
		map.append(builder,"Field.inactive"); //$NON-NLS-1$
		builder.nextLine(2);
		map.append(builder,"Field.resourceType"); //$NON-NLS-1$
		map.appendSometimesReadOnly(builder,"Field.materialLabel"); //$NON-NLS-1$
		builder.nextLine(2);
		builder.addLabel(map.getLabel("Field.baseCalendar")+":"); //$NON-NLS-1$ //$NON-NLS-2$
		builder.nextColumn(2);
		changeWorkingTimeButton=getChangeWorkingTimeButton();
		builder.append(pairedComponents(map,"Field.baseCalendar",0,changeWorkingTimeButton)); //$NON-NLS-1$
	   	return builder.getPanel();
		  
	}

	public JComponent createAvailabilityPanel() {
		FieldComponentMap map = createMap();
		
		FormLayout layout = new FormLayout("p:grow","p,3dlu,p,3dlu,fill:150dlu:grow"); //$NON-NLS-1$ //$NON-NLS-2$

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		builder.add(createHeaderFieldsPanel(map),cc.xyw(builder.getColumn(), builder
				.getRow(), 1));
		builder.nextLine(2);
		builder.add(createAvailabilitySpreadsheet(),cc.xyw(builder.getColumn(), builder
				.getRow(), 1));
		
		return builder.getPanel();	
	}

	public JComponent createTasksPanel() {
		FieldComponentMap map = createMap();
		
		FormLayout layout = new FormLayout("p:grow","p,3dlu,p,3dlu,fill:150dlu:grow"); //$NON-NLS-1$ //$NON-NLS-2$

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		builder.add(createHeaderFieldsPanel(map),cc.xyw(builder.getColumn(), builder
				.getRow(), 1));
		builder.nextLine(2);
		builder.append(Messages.getString("ResourceInformationDialog.AssignedToTasks")); // not using assigned button //$NON-NLS-1$
		builder.nextLine(2);
		builder.add(createAssignmentSpreadsheet(),cc.xyw(builder.getColumn(), builder
				.getRow(), 1));
		JComponent panel = builder.getPanel();
		HelpUtil.addDocHelp(panel,"Assign_Resources");
		
		return panel;	
	}

	protected SpreadSheet assignmentSpreadSheet;
	private JScrollPane assignmentPane;
    protected JScrollPane createAssignmentSpreadsheet() {
        assignmentSpreadSheet = SpreadSheetUtils.createFilteredSpreadsheet(GraphicManager.getInstance(this).getCurrentFrame()
        							,true
									,"View.ResourceInformation.Assignments" //$NON-NLS-1$
									,UsageDetailView.taskAssignmentSpreadsheetCategory
									,"Spreadsheet.Assignment.taskUsage" //$NON-NLS-1$
									,false
									//, 0 
									,new String[]{MenuActionConstants.ACTION_DELETE}/*,new int[] {SpreadSheet.DELETE}*/);
        assignmentSpreadSheet.setCanModifyColumns(true);
        assignmentSpreadSheet.setCanSelectFieldArray(true);
        assignmentSpreadSheet.setActions(new String[]{MenuActionConstants.ACTION_DELETE});

        updateAssignmentSpreadsheet();
		assignmentPane = SpreadSheetUtils.makeSpreadsheetScrollPane(assignmentSpreadSheet);
		return assignmentPane;
    }
    protected void updateAssignmentSpreadsheet() {
    	SpreadSheetUtils.updateFilteredSpreadsheet(assignmentSpreadSheet,(object==null)?new AssociationList():((Resource)object).getAssignments());
    }
	
	
	private SpreadSheet availabilitySpreadsheet;
	JScrollPane availabilityPane;
	
	protected JScrollPane createAvailabilitySpreadsheet() {
		availabilitySpreadsheet = new SpreadSheet() {
	   		protected void doPostExceptionTreatment() { // this is a bit of a hack.
	   			updateAvailabilitySpreadsheet(); 
	   			requestFocus();
	   		}

	   	};

		availabilitySpreadsheet.setSpreadSheetCategory("availabilitySpreadsheet"); //$NON-NLS-1$
    	availabilitySpreadsheet.setCanSelectFieldArray(false);
    	//availabilitySpreadsheet.setActions(new String[]{SpreadSheet.DELETE});

//    	availabilitySpreadsheet.setHasRowPopup(false);
    	ResourceImpl resourceImpl=(ResourceImpl)object;
    	SpreadSheetUtils.createCollectionSpreadSheet(availabilitySpreadsheet
				,(object==null)?new LinkedList():resourceImpl.getAvailabilityTable().getList()
				//,(object==null)?null:((Resource)object).getDocument()
				,"View.ResourceInformation.Availability" //$NON-NLS-1$
				,"availabilitySpreadsheet" //$NON-NLS-1$
				,"Spreadsheet.availability" //$NON-NLS-1$
				,false
				,(object==null)?null:((ResourceImpl)object).getAvailabilityTable()
				,1
//				,resourceImpl.getGlobalResource().isLocal()
//				,resourceImpl.getGlobalResource().isMaster()
				);
    	availabilityPane = SpreadSheetUtils.makeSpreadsheetScrollPane(availabilitySpreadsheet);
		return availabilityPane;
    }
	
    protected void updateAvailabilitySpreadsheet() {
    	SpreadSheetUtils.updateCollectionSpreadSheet(availabilitySpreadsheet
				,(object==null)?new LinkedList():((ResourceImpl)object).getAvailabilityTable().getList()
				,(object==null)?null:((ResourceImpl)object).getAvailabilityTable(),
				1);
    	if (object!=null){
    		ResourceImpl resourceImpl=(ResourceImpl)object;
    		NodeModel model=((SpreadSheetModel)availabilitySpreadsheet.getModel()).getCache().getModel();
//    		availabilitySpreadsheet.clearActions();
    		availabilitySpreadsheet.setReadOnly(resourceImpl.isReadOnly());
    		availabilitySpreadsheet.setEnabled(!resourceImpl.isReadOnly());

    		model.setMaster(resourceImpl.getGlobalResource().isMaster());
    		model.setLocal(resourceImpl.getGlobalResource().isLocal());
    		//TODO instead of doing this availabilityTable and Availability can contain a read-only field
    	}
    	
    }
	
	private SpreadSheet costTableSpreadsheets[] = new SpreadSheet[Settings.NUM_COST_RATES];
	
	protected SpreadSheet createCostTableSpreadsheet(CostRateTable costRateTable) {
	   	SpreadSheet ss = new SpreadSheet() {
	   		protected void doPostExceptionTreatment() {
	   			updateCostTableSpreadsheets();
	   			requestFocus();
	   		}

	   	};
	   	//ss.setActions(new String[]{SpreadSheet.DELETE});

		ss.setSpreadSheetCategory("costRates"); //$NON-NLS-1$
		ss.setCanSelectFieldArray(false);
//    	ss.setHasRowPopup(false);

    	return ss;

    }
	
    protected void createCostTableSpreadsheets() {
    	ResourceImpl resourceImpl=(ResourceImpl)object;
    	for (int i = 0; i < Settings.NUM_COST_RATES; i++) {
        	SpreadSheetUtils.createCollectionSpreadSheet(costTableSpreadsheets[i]
					,(object==null)?new LinkedList():resourceImpl.getCostRateTable(i).getList()
					//,(object==null)?null:((Resource)object).getDocument()
					,"View.ResourceInformation.CostRate" //$NON-NLS-1$
					,"costRates" //$NON-NLS-1$
					,"Spreadsheet.costRates" //$NON-NLS-1$
					,false
					,(object==null)?null:((Resource)object).getCostRateTable(i)
					, 1
//					,resourceImpl.getGlobalResource().isLocal()
//					,resourceImpl.getGlobalResource().isMaster()
					);
     	}
    }
    protected void updateCostTableSpreadsheets() {
    	for (int i = 0; i < Settings.NUM_COST_RATES; i++) {
        	SpreadSheetUtils.updateCollectionSpreadSheet(costTableSpreadsheets[i]
					,(object==null)?new LinkedList():((Resource)object).getCostRateTable(i).getList()
					,(object==null)?null:((Resource)object).getCostRateTable(i)
					, 1);
        	if (object!=null){
        		ResourceImpl resourceImpl=(ResourceImpl)object;
        		NodeModel model=((SpreadSheetModel)costTableSpreadsheets[i].getModel()).getCache().getModel();
//        		costTableSpreadsheets[i].clearActions();
        		costTableSpreadsheets[i].setReadOnly(resourceImpl.isReadOnly());
        		costTableSpreadsheets[i].setEnabled(!resourceImpl.isReadOnly());
        		model.setMaster(resourceImpl.getGlobalResource().isMaster());
        		model.setLocal(resourceImpl.getGlobalResource().isLocal());
        		//TODO instead of doing this availabilityTable and Availability can contain a read-only field
        	}
     	}
    }

	JTabbedPane costTabbedPane;
	
	public JComponent createCostsPanel(){
		FieldComponentMap map = createMap();
		FormLayout layout = new FormLayout(
		        "50dlu,3dlu,50dlu,3dlu,p:grow", //$NON-NLS-1$
	    		  "p,3dlu,p,3dlu,fill:p:grow,3dlu,p,3dlu,p"); //$NON-NLS-1$
	
	
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		builder.add(createHeaderFieldsPanel(map),cc.xyw(builder.getColumn(), builder
				.getRow(), 5));
		
		costTabbedPane= new JTabbedPane();
		CostRateTable costRateTable;
		for (int i = 0; i < 5; i++) {
			costRateTable = ((Resource)object).getCostRateTable(i);
			costTableSpreadsheets[i] = createCostTableSpreadsheet(costRateTable);
			costTableSpreadsheets[i].setPreferredScrollableViewportSize(new Dimension(500, 200));
			JScrollPane ssPane = SpreadSheet.createScrollPaneForTable(costTableSpreadsheets[i]);
			costTabbedPane.addTab(costRateTable.getName(),ssPane);
		}
		createCostTableSpreadsheets();
		builder.nextLine(2);
		builder.addSeparator(Messages.getString("Text.CostRateTables")); //$NON-NLS-1$
		builder.nextLine(2);
		builder.add(costTabbedPane,cc.xyw(builder.getColumn(), builder
				.getRow(), 5));
		builder.nextLine(2);
		map.append(builder,"Field.accrueAt"); //$NON-NLS-1$
	   	return builder.getPanel();
		  
	}
	

	/* (non-Javadoc)
	 * @see com.projity.dialog.InformationDialog#createHeaderFieldsPanel(com.projity.dialog.util.FieldComponentMap)
	 */
	protected JComponent createHeaderFieldsPanel(FieldComponentMap map) {
		FormLayout layout = new FormLayout(
        "p, 3dlu, 160dlu", //$NON-NLS-1$
		  "p"); //$NON-NLS-1$
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		map.append(builder,"Field.name"); //$NON-NLS-1$
		return builder.getPanel();
	}
	
	protected void setVisibleAndEnabledState() {
		super.setVisibleAndEnabledState();
		boolean showing = (object != null && object instanceof Resource);
		assignmentPane.setVisible(showing);
		costTabbedPane.setVisible(showing);
		boolean isLaborResource = showing && ((Resource)object).isLabor();
		availabilityPane.setVisible(isLaborResource);
	}
	public void updateAll() {
		activateListeners();
		super.updateAll();
		updateAssignmentSpreadsheet();
		updateCostTableSpreadsheets();
		updateAvailabilitySpreadsheet();
		changeWorkingTimeButton.setEnabled(getObject() != null && !((ResourceImpl)getObject()).isReadOnly());

	}
	public void documentSelected(DocumentSelectedEvent evt) {
		if (assignmentSpreadSheet==null) return;
        DocumentFrame df=evt.getCurrent();
        if (df!=null){
        	NodeModelCache cache = df.createCache(true,Messages.getString("View.TaskInformation.Assignments")); //$NON-NLS-1$
			assignmentSpreadSheet.setCache(cache);
        }
	}
	
	
	
	protected void activateListeners() {
		super.activateListeners();
		for (int i=0;i<costTableSpreadsheets.length;i++) costTableSpreadsheets[i].getCache().setReceiveEvents(true);
		availabilitySpreadsheet.getCache().setReceiveEvents(true);
		//assignmentSpreadSheet.getCache().setReceiveEvents(true);
	}

	protected void desactivateListeners() {
		super.desactivateListeners();
		for (int i=0;i<costTableSpreadsheets.length;i++) costTableSpreadsheets[i].getCache().setReceiveEvents(true);
		availabilitySpreadsheet.getCache().setReceiveEvents(false);
		//assignmentSpreadSheet.getCache().setReceiveEvents(false);
		//causes an update problem of the filtered cache
	}

	
}