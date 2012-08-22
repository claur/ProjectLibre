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

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.commons.collections.Closure;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.pm.graphic.model.cache.GraphicDependency;
import com.projity.pm.key.HasName;
import com.projity.strings.Messages;

/**
 *  
 */
public class XbsDependencyDialog extends AbstractDialog {
	private static final long serialVersionUID = 1L;
	protected JLabel preLabel, sucLabel;
	protected JButton removeButton;
	boolean remove=false;
	GraphicDependency dependency;
	
	public static boolean doDialog(XbsDependencyDialog dialog, GraphicDependency dependency,Closure removeClosure) {
		dialog.setDependency(dependency);
		boolean result;
		if (result = dialog.doModal()) {
			if (dialog.remove) 
				removeClosure.execute(null);
		}
		dialog.dependency = null;
		return result;
	}
	public XbsDependencyDialog(Frame frame, GraphicDependency dependency) {
		super(frame, Messages.getString("Text.HierarchicalRelation"), true);
		initControls();
		setDependency(dependency);
	}

	public void setDependency(GraphicDependency dependency) {
		remove= false;
		this.dependency = dependency;
		setTitle(Messages.getString("Text.HierarchicalRelation"));
		bind(true);
		
	}
	protected void initControls() {
		preLabel = new JLabel();
		sucLabel = new JLabel();
		bind(true);
	}
	
	public ButtonPanel createButtonPanel() {
		AbstractAction action = new AbstractAction(Messages.getString("Text.Remove")) {
			public void actionPerformed(ActionEvent e) {
				delete();
			}
		};
		removeButton = new JButton(action);
		
		createOkCancelButtons();
		ButtonPanel buttonPanel = new ButtonPanel();
		buttonPanel.addButton(removeButton);
		//buttonPanel.addButton(ok);
		buttonPanel.addButton(cancel);
		return buttonPanel;
	}   	

	public JComponent createContentPanel() {
		// Separating the component initialization and configuration
		// from the layout code makes both parts easier to read.
		initControls();
		//TODO set minimum size
		FormLayout layout = new FormLayout(
				"50dlu,3dlu,50dlu,3dlu,50dlu,3dlu,50dlu", // cols
				"p,3dlu,p,3dlu,p,3dlu"); // rows

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
		
		return builder.getPanel();
	}


	void delete() {
		remove = true;
		setDialogResult(JOptionPane.OK_OPTION);
		setVisible(false);
	}

	protected boolean bind(boolean get) {
		if (dependency == null)
			return false;
		if (get) {
			preLabel.setText(((HasName)dependency.getPredecessor().getNode().getImpl()).getName());
			sucLabel.setText(((HasName)dependency.getSuccessor().getNode().getImpl()).getName());
		} else {
		}
		return true;
	}

	/**
	 * @return Returns the remove.
	 */
	public boolean isRemove() {
		return remove;
	}
}