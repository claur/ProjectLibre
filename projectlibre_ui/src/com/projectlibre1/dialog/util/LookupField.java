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
package com.projectlibre1.dialog.util;


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.projectlibre1.dialog.LookupDialog;
import com.projectlibre1.pm.graphic.IconManager;
import com.projectlibre1.field.Field;
import com.projectlibre1.strings.Messages;

public class LookupField extends JPanel {
	JLabel display;
	JButton button;
	String value;
	public LookupField(Field field,Object value) {
		super();
		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		this.value = (String)value;
		add(button =createLookupButton(field)); //TODO setting value?
		button.setAlignmentY(Component.CENTER_ALIGNMENT);
		button.setAlignmentX(Component.LEFT_ALIGNMENT);

//		button.setHorizontalAlignment(SwingConstants.LEFT);
		add(display = new JLabel());
		setText(this.value);

	}
	public void setText(String value) {
		if (value == null)
			display.setText(""); //$NON-NLS-1$
		else {
			int index = value.indexOf("\\") + 1; //$NON-NLS-1$
			String label = value.substring(index); // gets the part after the slash, or all if no slash
			display.setText(label);
		}
	}

  	private JButton createLookupButton(final Field f) {
  		JButton lookup= new JButton();
  		lookup.setToolTipText(Messages.getString("LookupField.LookupAValue")); //$NON-NLS-1$
		ImageIcon icon = IconManager.getIcon("menu.find"); //$NON-NLS-1$
		lookup.setIcon(icon);
		lookup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//route message to main frame
				String keyAndValuePair[]=LookupDialog.getKeyAndValue(f);
				if (keyAndValuePair == null)
					return; // no change
				if (keyAndValuePair[0] == null)
					value = null;
				else
					value = keyAndValuePair[0] + "\\" + keyAndValuePair[1]; //$NON-NLS-1$
				setText(value);
				fire();
				
			}});
  		return lookup;
  	}
	public String getValue() {
		return value;
	}
	public String getText() {
		return value;
	}
	public JLabel getDisplay() {
		return display;
	}
	public void setDisplay(JLabel display) {
		this.display = display;
	}
    protected javax.swing.event.EventListenerList listenerList =
        new javax.swing.event.EventListenerList();

    // This methods allows classes to register for ObjectEvents
    public void addChangeListener(ChangeListener listener) {
        listenerList.add(ChangeListener.class, listener);
    }

    // This methods allows classes to unregister for ObjectEvents
    public void removeChangeListener(ChangeListener listener) {
        listenerList.remove(ChangeListener.class, listener);
    }
    private void fire() {
    	ChangeEvent evt = new ChangeEvent(this);
        Object[] listeners = listenerList.getListenerList();
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i]==ChangeListener.class) {
                ((ChangeListener)listeners[i+1]).stateChanged(evt);
            }
        }
    }
	
}
