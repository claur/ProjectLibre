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
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.configuration.Configuration;
import com.projity.document.ObjectEvent;
import com.projity.field.Field;
import com.projity.pm.graphic.IconManager;
import com.projity.pm.graphic.frames.DocumentFrame;
import com.projity.pm.graphic.frames.DocumentSelectedEvent;
import com.projity.pm.graphic.spreadsheet.common.CommonSpreadSheet;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeEvent;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeListener;
import com.projity.pm.graphic.views.SearchContext;
import com.projity.pm.graphic.views.Searchable;
import com.projity.pm.task.Project;
import com.projity.strings.Messages;
import com.projity.util.Alert;
import com.projity.util.BrowserControl;
import com.projity.util.ClassLoaderUtils;
import com.projity.util.VersionUtils;

public final class FindDialog extends AbstractDialog implements ObjectEvent.Listener,DocumentSelectedEvent.Listener{
	private static final long serialVersionUID = 1L;
	JComboBox combo;
	JTextField search  = new JTextField(30);
	DocumentFrame documentFrame;
	SearchContext context;
	JButton next;
	JButton previous;
	private static FindDialog instance = null;
	public static FindDialog getInstance(DocumentFrame owner,Searchable searchable, Field current) {
		return new FindDialog(owner,searchable,current);
	}


	private Searchable searchable;

	private FindDialog(DocumentFrame documentFrame, Searchable searchable, Field field) {
		super(documentFrame.getGraphicManager().getFrame(), Messages.getString("LookupDialog.Find"), false); //$NON-NLS-1$ //$NON-NLS-2$
		DocumentSelectedEvent.addListener(this);
		init(searchable,field);

	}

	public void init(Searchable searchable, Field field) {
		this.searchable=searchable;
		context = searchable.createSearchContext();
		if (field != null)
			context.setField(field);
		ArrayList l = new ArrayList();
		l.addAll(searchable.getAvailableFields());
		Collections.sort(l);
		ComboBoxModel m = new DefaultComboBoxModel(l.toArray());
		if (combo == null)
			combo = new JComboBox(m);
		else
			combo.setModel(m);

		bind(true);
		updateFindButtonState();
		search.requestFocus();
		//combo.invalidate();

	}

	public void onOk() {
		bind(false);
		if (!searchable.findNext(context))
			Alert.warn("No more matches");
	}

	protected boolean bind(boolean get) {
		if (get) {
			if (context.getField() != null)
				combo.setSelectedItem(context.getField());
			if (search != null)
				search.setText("");

		} else {
			context.setField((Field)combo.getSelectedItem());
			context.setSearchValue(search.getText());

		}
		return true;
	}

	// Building *************************************************************

	/**
	 * Builds the panel. Initializes and configures components first, then
	 * creates a FormLayout, configures the layout, creates a builder, sets a
	 * border, and finally adds the components.
	 *
	 * @return the built panel
	 */

	public JComponent createContentPanel() {
//		initControls();
		// Separating the component initialization and configuration
		// from the layout code makes both parts easier to read.
		//TODO set minimum size
		FormLayout layout = new FormLayout("default, 3dlu, default, 3dlu, default", // cols //$NON-NLS-1$
				"p, 3dlu, p"); // rows //$NON-NLS-1$



		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();



		builder.append(Messages.getString("LookupDialog.Find"),search); //$NON-NLS-1$
		builder.nextLine(2);
		builder.append(Messages.getString("Text.Field"),combo); //$NON-NLS-1$
		return builder.getPanel();
	}

	public void setDocumentFrame(DocumentFrame documentFrame) {
		if (this.documentFrame != null)
		    this.documentFrame.getProject().removeObjectListener(this);
		this.documentFrame = documentFrame;
		documentFrame.getProject().addObjectListener(this);
	}

	public void documentSelected(DocumentSelectedEvent evt) {
		setVisible(false);
	}
	public void objectChanged(ObjectEvent objectEvent) {
//		if (objectEvent.getObject() instanceof Project)
//			setVisible(false);
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = new ButtonPanel();
		next = new JButton(Messages.getString("LookupDialog.Find"),IconManager.getIcon("image.down"));
		previous = new JButton(Messages.getString("LookupDialog.Find"),IconManager.getIcon("image.up"));
		buttonPanel.add(next);
		buttonPanel.add(previous);
		updateFindButtonState();
		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				context.setForward(true);
				onOk();
			}});
		previous.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				context.setForward(false);
				onOk();
			}});
		// TODO Auto-generated method stub
		search.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				updateFindButtonState();			}

			public void keyTyped(KeyEvent e) {
			}});
		return buttonPanel;
	}

	private void updateFindButtonState() {
		if (next == null)
			return;
		boolean empty = search.getText().length() == 0;
		next.setEnabled(!empty);
		previous.setEnabled(!empty);

	}
	@Override
	protected boolean hasOkAndCancelButtons() {
		return false;
	}

}
