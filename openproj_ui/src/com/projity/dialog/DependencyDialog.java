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
package com.projity.dialog;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.text.ParseException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.association.InvalidAssociationException;
import com.projity.configuration.Configuration;
import com.projity.datatype.Duration;
import com.projity.datatype.DurationFormat;
import com.projity.field.Field;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.dependency.DependencyService;
import com.projity.pm.dependency.DependencyType;
import com.projity.pm.task.NormalTask;
import com.projity.strings.Messages;
import com.projity.util.Alert;

/**
 *  
 */
public class DependencyDialog extends AbstractDialog {
	private static final long serialVersionUID = 1L;
	protected JLabel preLabel, sucLabel;
	protected JComboBox typeCombo;
	protected JTextField lagTextField;
	protected JButton removeButton = new JButton();

	Field dependencyTypeField = Configuration.getFieldFromId("Field.dependencyType");
	boolean remove=false;
	Dependency dependency;
	
	public static boolean doDialog(DependencyDialog dialog, Dependency dependency) {
		dialog.setDependency(dependency);
		boolean result;
		if (result = dialog.doModal()) {
			if (dialog.remove) {
				DependencyService.getInstance().remove(dependency,dialog,true);
			} else {
				DependencyService.getInstance().update(dependency,dialog);
			}
		}
		dialog.dependency = null;
		return result;
	}
	public DependencyDialog(Frame frame, Dependency dependency) {
		super(frame, Messages.getString("Text.TaskDependency"), true);
		initControls();
		setDependency(dependency);
		addDocHelp("Task_Dependency_Dialog");
	}

	public void setDependency(Dependency dependency) {
		remove= false;
		this.dependency = dependency;
		if (dependency.isExternal()) {
			if (dependency.isDisabled())
				setTitle(Messages.getString("Text.DisabledExternalTaskDependency"));
			else
				setTitle(Messages.getString("Text.ExternalTaskDependency"));
		} else {
			setTitle(Messages.getString("Text.TaskDependency"));
		}
		bind(true);
		
	}
	protected void initComponents() {
		if (contentPanel != null) // if already shown once
			return;
		super.initComponents();
	}
	protected void initControls() {
		preLabel = new JLabel();
		sucLabel = new JLabel();
		Object[] options = dependencyTypeField.getOptions(null);
		typeCombo = new JComboBox(options);
		lagTextField = new JTextField();
		bind(true);
	}
	
	public ButtonPanel createButtonPanel() {
		AbstractAction action = new AbstractAction(Messages.getString("Text.Remove")) {
			public void actionPerformed(ActionEvent e) {
				delete();
			}
		};
		removeButton.setAction(action);
		removeButton.setEnabled(!isReadOnly());
		createOkCancelButtons();
		ButtonPanel buttonPanel = new ButtonPanel();
		buttonPanel.addButton(removeButton);
		buttonPanel.addButton(ok);
		buttonPanel.addButton(cancel);
		buttonPanel.add(getHelpButton());
		return buttonPanel;
	}   	

	public JComponent createContentPanel() {
		// Separating the component initialization and configuration
		// from the layout code makes both parts easier to read.
		initControls();
		//TODO set minimum size
		FormLayout layout = new FormLayout(
				"50dlu,3dlu,50dlu,3dlu,50dlu,3dlu,50dlu", // cols
				"p,3dlu,p,3dlu,p,3dlu,p,3dlu"); // rows

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		builder.append(Messages.getString("Text.From") + ":");
		builder.add(preLabel,cc.xyw(builder.getColumn(), builder
				.getRow(), 5)); 
		builder.nextLine(2);
		builder.append(Messages.getString("Text.To") + ":");
		builder.add(sucLabel,cc.xyw(builder.getColumn(), builder
				.getRow(), 5)); 
		
		builder.nextLine(2);
		builder.append(Messages.getString("Text.Type") + ":", typeCombo);

		builder.addLabel(Messages.getString("Text.Lag") + ":");
		builder.nextColumn(2);
		builder.add(lagTextField);

		return builder.getPanel();
	}

//	void ok() {
//		Integer type = DependencyType.mapStringToValue((String) typeCombo
//				.getSelectedItem());
//		model.modifyEdge(edge, type.intValue(), -1);
//		edge = null;
//		setDialogResult(RESULT_AFFIRMED);
//		setVisible(false);
//	}
//

	void delete() {
		remove = true;
		setDialogResult(JOptionPane.OK_OPTION);
		setVisible(false);
	}

	protected boolean bind(boolean get) {
		if (dependency == null)
			return false;
		if (get) {
			preLabel.setText(dependency.getQualifiedPredecessorName());
			sucLabel.setText(dependency.getQualifiedSuccessorName());
			String stype = DependencyType.mapValueToString(new Integer(dependency.getDependencyType()));
			typeCombo.setSelectedItem(stype);
			lagTextField.setText(DurationFormat.format(dependency.getLag()));
			
			boolean readOnly = ((NormalTask)dependency.getPredecessor()).getProject().isReadOnly();
			typeCombo.setEnabled(!readOnly);
			lagTextField.setEnabled(!readOnly);
			removeButton.setEnabled(!readOnly);
		} else {
			try {
				Duration duration = (Duration) DurationFormat.getInstance().parseObject(lagTextField.getText());
				int type = ((Number)DependencyType.mapStringToValue(typeCombo.getSelectedItem().toString())).intValue();
				
				
//				dependency.setLag(duration.getEncodedMillis());
//				dependency.setDependencyType(type);
				DependencyService.getInstance().setFields(dependency,duration.getEncodedMillis(),type,this);
			} catch (ParseException e) {
				Alert.warn(Messages.getString("Message.invalidDuration"),this);
				return false;
			} catch (InvalidAssociationException e) {
				Alert.warn(e.getMessage(),this);
				return false;
			}
		}
		return true;
	}
	private boolean isReadOnly() {
		return ((NormalTask)dependency.getPredecessor()).isReadOnly();
	}
//	public void init(GanttModel model, GanttEdge edge) {
//		this.edge = edge;
//		this.model = model;
//		Dependency dep = edge.getDependency();
//		Task preTask = (Task) dep.getPredecessor();
//		preLabel.setText(preTask.getName());
//		Task sucTask = (Task) dep.getSuccessor();
//		sucLabel.setText(sucTask.getName());
//		int type = dep.getDependencyType();
//		String stype = DependencyType.mapValueToString(new Integer(type));
//		typeCombo.setSelectedItem(stype);
//	}

	/**
	 * @return Returns the remove.
	 */
	public boolean isRemove() {
		return remove;
	}
}