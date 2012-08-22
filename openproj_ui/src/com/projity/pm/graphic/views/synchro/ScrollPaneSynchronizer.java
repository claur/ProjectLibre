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
package com.projity.pm.graphic.views.synchro;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 
 */
public class ScrollPaneSynchronizer {
	public static final int HORIZONTAL = JSplitPane.VERTICAL_SPLIT;

	public static final int VERTICAL = JSplitPane.HORIZONTAL_SPLIT;

	protected JScrollPane scrollPane1;

	protected JScrollPane scrollPane2;

	protected int orientation;

	protected ChangeListener listener = null;

	protected int defaultScrollBarPolicy1;

	protected int defaultScrollBarPolicy2;
	
	protected boolean bottomBarActivated=true;
	protected boolean bottomBarEnabled=false;
	/**
	 * @param scrollPane1
	 * @param scrollPane2
	 * @param position
	 */
	public ScrollPaneSynchronizer(JScrollPane scrollPane1,
			JScrollPane scrollPane2, int orientation) {
		this.scrollPane1 = scrollPane1;
		this.scrollPane2 = scrollPane2;
		this.orientation = orientation;
	}

	/**
	 * @return Returns the bottomBarActivated.
	 */
	public boolean isBottomBarActivated() {
		return bottomBarActivated;
	}
	/**
	 * @param bottomBarActivated The bottomBarActivated to set.
	 */
	public void setBottomBarActivated(boolean bottomBarActivated) {
		this.bottomBarActivated = bottomBarActivated;
	}
	/**
	 * @return Returns the bottomBarEnabled.
	 */
	public boolean isBottomBarEnabled() {
		return bottomBarEnabled;
	}
	/**
	 * @param bottomBarEnabled The bottomBarEnabled to set.
	 */
	public void setBottomBarEnabled(boolean bottomBarEnabled) {
		this.bottomBarEnabled = bottomBarEnabled;
	}
	public void activateSynchro() {
		if (listener == null) {
			if (orientation == HORIZONTAL) {
				defaultScrollBarPolicy1 = scrollPane1
						.getVerticalScrollBarPolicy();
				defaultScrollBarPolicy2 = scrollPane2
						.getVerticalScrollBarPolicy();
				scrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				scrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				scrollPane1
						.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
				scrollPane2
						.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

				scrollPane1.getViewport().addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						JViewport vp1 = scrollPane1.getViewport();
						JViewport vp2 = scrollPane2.getViewport();
						Point p1 = vp1.getViewPosition();
						Point p2 = vp2.getViewPosition();
						p2.setLocation((int) p2.getX(), (int) p1.getY());
						vp2.setViewPosition(p2);
						vp2.revalidate();
					}
				});

				scrollPane2.getViewport().addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						JViewport vp1 = scrollPane1.getViewport();
						JViewport vp2 = scrollPane2.getViewport();
						Point p1 = vp1.getViewPosition();
						Point p2 = vp2.getViewPosition();
						p1.setLocation((int) p1.getX(), (int) p2.getY());
						vp1.setViewPosition(p1);
						vp1.revalidate();
					}
				});

			} else if (orientation == VERTICAL) {
				defaultScrollBarPolicy1 = scrollPane1
						.getHorizontalScrollBarPolicy();
				defaultScrollBarPolicy2 = scrollPane2
						.getHorizontalScrollBarPolicy();
				scrollPane1
						.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				scrollPane2
						.setHorizontalScrollBarPolicy((bottomBarActivated)?JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS:JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				scrollPane2.getHorizontalScrollBar().setEnabled(bottomBarEnabled);
				
				listener = new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						JViewport vp1 = scrollPane1.getViewport();
						JViewport vp2 = scrollPane2.getViewport();
						
						Point p1 = vp1.getViewPosition();
						Point p2 = vp2.getViewPosition();
						p2.setLocation((int) p1.getX(), (int) p2.getY());
						vp2.setViewPosition(p2);
						
						//test
						Dimension d1 = vp1.getViewSize();
						Dimension d2 = vp2.getViewSize();
						d2.setSize((int) d1.getWidth(), (int) d2.getHeight());
						
						vp2.setViewSize(d2);
						((JComponent)vp2.getView()).setPreferredSize(d2);
						
						vp2.revalidate();
					}
				};
				scrollPane1.getViewport().addChangeListener(listener);

			}
		}
	}

	public void deactivateSynchro() {
		if (listener != null) {
			scrollPane2.getViewport().removeChangeListener(listener);
			if (orientation == HORIZONTAL) {
				scrollPane1.setVerticalScrollBarPolicy(defaultScrollBarPolicy1);
				scrollPane2.setVerticalScrollBarPolicy(defaultScrollBarPolicy2);
			} else if (orientation == VERTICAL) {
				scrollPane1
						.setHorizontalScrollBarPolicy(defaultScrollBarPolicy1);
				scrollPane2
						.setHorizontalScrollBarPolicy(defaultScrollBarPolicy2);
				scrollPane2.getHorizontalScrollBar().setEnabled(true);
			}
		}
	}

    /**
     * @return Returns the orientation.
     */
    public int getOrientation() {
        return orientation;
    }
    /**
     * @param orientation The orientation to set.
     */
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
    /**
     * @return Returns the scrollPane1.
     */
    public JScrollPane getScrollPane1() {
        return scrollPane1;
    }
    /**
     * @param scrollPane1 The scrollPane1 to set.
     */
    public void setScrollPane1(JScrollPane scrollPane1) {
        this.scrollPane1 = scrollPane1;
    }
    /**
     * @return Returns the scrollPane2.
     */
    public JScrollPane getScrollPane2() {
        return scrollPane2;
    }
    /**
     * @param scrollPane2 The scrollPane2 to set.
     */
    public void setScrollPane2(JScrollPane scrollPane2) {
        this.scrollPane2 = scrollPane2;
    }
}