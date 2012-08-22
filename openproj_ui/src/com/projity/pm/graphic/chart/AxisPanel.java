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
package com.projity.pm.graphic.chart;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.ui.RectangleEdge;

/**
 * An axis panel that shows a right justified JFreeChart axis associated with a chart
 */
public class AxisPanel extends JPanel {
	private static final long serialVersionUID = -43845080081033994L;

	private ValueAxis axis;
	
	private ChartInfo chartInfo;
	AxisPanel(ChartInfo chartInfo) {
		this.chartInfo = chartInfo;
		setBorder (UIManager.getBorder ("TableHeader.cellBorder"));
	}
	/**
	 * @param axis The axis to set.
	 */
	void setAxis(ValueAxis axis) {
		this.axis = axis;
	}
	
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics); // have to do this first for background
		if (axis == null)		
			return; // no axis, don't paint it
		
		Rectangle plotArea = new Rectangle(getSize()); // use full panel size
		RectangleEdge edge = RectangleEdge.LEFT; // plot is on the right
		
		axis.setVisible(true); // this enables this axis to draw.  Later on the axis is made invisible so the chart axis won't draw
		
		Dimension d = getSize();
		int cursor = d.width; // set cursor to full width
		int footerOffset = (int) Math.round(chartInfo.getFooterHeight());
		int headerOffset = 0;//(int) Math.round(chartInfo.getHeaderHeight());
		Rectangle dataArea = new Rectangle(cursor,headerOffset,d.width,d.height - headerOffset - footerOffset); // draw right justified
	
		axis.draw((Graphics2D)graphics,
				cursor,
				plotArea,
				dataArea,
				edge,
				null);

		axis.setVisible(false);	//now make it invisible so chart doesn't show it
	}
}
