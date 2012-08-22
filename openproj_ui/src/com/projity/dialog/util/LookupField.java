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
package com.projity.dialog.util;


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

import com.projity.dialog.LookupDialog;
import com.projity.field.Field;
import com.projity.pm.graphic.IconManager;
import com.projity.strings.Messages;

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
