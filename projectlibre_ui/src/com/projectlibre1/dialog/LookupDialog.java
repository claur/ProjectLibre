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
package com.projectlibre1.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicListUI;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projectlibre1.pm.graphic.frames.MainFrameFactory;
import com.projectlibre1.field.Field;
import com.projectlibre1.session.Session;
import com.projectlibre1.session.SessionFactory;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.util.Alert;
import com.projectlibre1.util.Environment;

public final class LookupDialog extends AbstractDialog {
	private static final long serialVersionUID = 1L;
	// use property utils to copy to project like struts
	Field field;
	JComboBox types;
	JList results;
	JScrollPane resultsPane;
	JTextField match;
	JButton find;
	JButton removeButton;
	LinkedHashMap resultMap;
	String key;
	String value;
	public static String[] getKeyAndValue(Field f) {
		LookupDialog dlg = new LookupDialog(f);
		dlg.initControls();
		if (dlg.doModal()) {
			return new String[] {dlg.key,dlg.value};
		}
		return null;
		
	}
	public ButtonPanel createButtonPanel() {
		AbstractAction action = new AbstractAction(Messages.getString("Text.Remove")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				remove();
			}
		};
		removeButton = new JButton(action);
		
		createOkCancelButtons();
		ButtonPanel buttonPanel = new ButtonPanel();
		buttonPanel.addButton(removeButton);
		buttonPanel.addButton(ok);
		buttonPanel.addButton(cancel);
		return buttonPanel;
	}   	
	private void remove() {
		value= null;
		key = null;
		super.onOk();
	}

	@Override
	public void onOk() {
		int index = results.getSelectedIndex();
		if (index != -1) {
			value = (String) results.getSelectedValue();
			key = keyForValue(value);
		}
		super.onOk();
	}

	private String keyForValue(String value) {
		Iterator i = resultMap.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry entry =(Map.Entry)i.next();
			if (value == entry.getValue())
				return (String)entry.getKey();
		}
		return null;
	}
	private LookupDialog(Field field) {
		super(MainFrameFactory.getMainFrame(), Messages.getString("LookupDialog.LookupAnObject"),true); //$NON-NLS-1$
		this.field = field;
	}
	protected boolean initialOkEnabledState() {
		return false;
	}

	/**
	 * Creates, intializes and configures the UI components. Real applications
	 * may further bind the components to underlying models.
	 */
	protected void initControls() {
		types = new JComboBox(field.getLookupTypes().split(";")); //$NON-NLS-1$
		results = new JList();
		results.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
//		results.setVisibleRowCount(15);
		resultsPane = new JScrollPane(results);
		if (Environment.isNewLook()) // The PLAF can override the custom renderer. This avoids that
			results.setUI(new BasicListUI());

		match = new JTextField();
		match.setToolTipText(Messages.getString("LookupDialog.EnterPartOfTheName")); //$NON-NLS-1$
		find = new JButton(Messages.getString("LookupDialog.Find")); //$NON-NLS-1$
		find.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Session session = SessionFactory.getInstance().getSession(false);
				if (session != null)
					try {
						resultMap = (LinkedHashMap)SessionFactory.call(session,"queryLike",new Class[]{String.class,String.class},new Object[]{ types.getSelectedItem(), match.getText()});
						results= new ActionJList(resultMap.values().toArray());
						((ActionJList)results).addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e) {
								onOk();
							}});
						if (resultMap.isEmpty()) {
							resultsPane.getViewport().add(new JLabel(Messages.getString("LookupDialog.NoMatchesFound"))); //$NON-NLS-1$
							resultsPane.setEnabled(false);
							ok.setEnabled(false);
						} else {
							resultsPane.setEnabled(true);
							resultsPane.getViewport().add(results);
							ok.setEnabled(true);
						}
					} catch (Exception e1) {
						Alert.error(Messages.getString("LookupDialog.UnableToContactServer")); //$NON-NLS-1$
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				// TODO Auto-generated method stub
				
			}});
	}


	// Component Creation and Initialization **********************************



	// Building *************************************************************

	/**
	 * Builds the panel. Initializes and configures components first, then
	 * creates a FormLayout, configures the layout, creates a builder, sets a
	 * border, and finally adds the components.
	 * 
	 * @return the built panel
	 */

	public JComponent createContentPanel() {
		// Separating the component initialization and configuration
		// from the layout code makes both parts easier to read.
		initControls();
		//TODO set minimum size
		FormLayout layout = new FormLayout("p, 3dlu, p,20dlu,p,3dlu,160dlu:grow,3dlu,p", // cols //$NON-NLS-1$
				"p, 3dlu, p,3dlu,fill:default:grow"); // rows //$NON-NLS-1$

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		builder.append(Messages.getString("LookupDialog.Type"),types); //$NON-NLS-1$
		builder.append(Messages.getString("LookupDialog.Find") + ":",match); //$NON-NLS-1$
		builder.append(find);
		builder.nextLine(2);
		builder.append(Messages.getString("LookupDialog.Results")); //$NON-NLS-1$
		builder.nextLine(2);
		builder.add(resultsPane,cc.xyw(builder.getColumn(), builder.getRow(), 9));
		return builder.getPanel();
	}


}
