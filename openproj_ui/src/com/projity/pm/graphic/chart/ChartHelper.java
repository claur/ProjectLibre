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

/**
 * 
 */

import java.awt.Color;
import java.util.HashMap;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import com.projity.graphic.configuration.shape.Colors;
import com.projity.pm.assignment.TimeDistributedConstants;
import com.projity.util.Environment;

/**
 * A simple demonstration application showing how to create a vertical bar
 * chart.
 * 
 */
public class ChartHelper implements TimeDistributedConstants {
	public static final int BOTTOM_INSET = 7;// replace domain legend

	public static JFreeChart createChart(final XYDataset dataset, boolean bar,final XYDataset secondDataset) {
		
		JFreeChart chart;
		if (secondDataset != null)
			chart = createBarLineChart(dataset,secondDataset);
		else
			chart = bar ? createBarChart(dataset) : createLineChart(dataset);
		chart.setAntiAlias(false);// faster
		chart.setBorderVisible(false);
		return chart;
	}


	/**
	 * Creates a new chart.
	 * 
	 * @param dataset
	 *            the dataset.
	 * 
	 * @return The chart.
	 */
	public static JFreeChart createBarChart(final XYDataset dataset) {
		ValueAxis domainAxis = null;
		NumberAxis axis = new NumberAxis(null);
		axis.setAutoRangeIncludesZero(false);
		domainAxis = axis;

		ValueAxis valueAxis = new NumberAxis(null);
		XYItemRenderer barRenderer = new XYStepAreaRenderer(XYStepAreaRenderer.AREA, new StandardXYToolTipGenerator(), null);

		XYPlot plot = new XYPlot(dataset, domainAxis, valueAxis, barRenderer);
		plot.setOrientation(PlotOrientation.VERTICAL);
		JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
		removeAxisAndInsets(chart);
		return chart;
	}

	public static JFreeChart createBarLineChart(final XYDataset barDataset, final XYDataset lineDataset) {
		JFreeChart chart =  createBarChart(barDataset);
		XYItemRenderer lineRenderer = new StandardXYItemRenderer(StandardXYItemRenderer.LINES);
		chart.getXYPlot().setDataset(1,lineDataset);
		chart.getXYPlot().setRenderer(1,lineRenderer);
		chart.getXYPlot().setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
		chart.getXYPlot().setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD); // draw the line after the bar so it's superimposed
		return chart;
	}


	public static JFreeChart createLineChart(final XYDataset dataset) {
		NumberAxis xAxis = new NumberAxis(null);
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(null);
		XYItemRenderer renderer = new StandardXYItemRenderer(StandardXYItemRenderer.LINES);
		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		plot.setOrientation(PlotOrientation.VERTICAL);
		renderer.setToolTipGenerator(new StandardXYToolTipGenerator());
		JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
		removeAxisAndInsets(chart);
		return chart;
	}

	public static void removeAxisAndInsets(JFreeChart chart) {
		XYPlot plot = chart.getXYPlot();
		removeAxisAndInsets(plot);
	}

	public static void removeAxisAndInsets(XYPlot plot) {
		plot.getRangeAxis().setVisible(false);
		plot.getDomainAxis().setVisible(false);
		plot.setDomainGridlinesVisible(false);
		plot.setInsets(new RectangleInsets(0, 0, BOTTOM_INSET, 0));
	}

	private static HashMap map = null;

	private static HashMap getMap() {
		if (map == null) {
			map = new HashMap();
			map.put(PERCENT_ALLOC, Colors.RED);
			map.put(OVERALLOCATED, Colors.RED);
			if (!Environment.getStandAlone()) map.put(OTHER_PROJECTS, Colors.GRAY);
			map.put(AVAILABILITY, Colors.BLACK);
			map.put(SELECTED, Colors.BLUE);
			map.put(THIS_PROJECT, Colors.GREEN);
			map.put(WORK, Colors.RED);
			map.put(ACTUAL_WORK, Colors.BROWN);
			map.put(REMAINING_WORK, Colors.PURPLE);
			map.put(BASELINE_WORK, Colors.DARK_SLATE_GRAY);
			map.put(COST, Colors.RED);
			map.put(ACTUAL_COST, Colors.BROWN);
			map.put(FIXED_COST, Colors.CORAL);
			map.put(ACTUAL_FIXED_COST, Colors.BURLY_WOOD);
			map.put(REMAINING_COST, Colors.PURPLE);
			map.put(BASELINE_COST, Colors.DARK_SLATE_GRAY);
			map.put(ACWP, Colors.RED);
			map.put(BCWP, Colors.OLIVE_DRAB);
			map.put(BCWS, Colors.GOLD);
			map.put(BASELINE1_WORK, Colors.MAGENTA);
			map.put(BASELINE2_WORK, Colors.KHAKI);
			map.put(BASELINE3_WORK, Colors.TAN);
			map.put(BASELINE4_WORK, Colors.NAVY);
			map.put(BASELINE5_WORK, Colors.TURQUOISE);
			map.put(BASELINE6_WORK, Colors.VIOLET);
			map.put(BASELINE7_WORK, Colors.MAROON);
			map.put(BASELINE8_WORK, Colors.SALMON);
			map.put(BASELINE9_WORK, Colors.ORANGE);
			map.put(BASELINE10_WORK, Colors.CYAN);
			map.put(BASELINE1_COST, Colors.MAGENTA);
			map.put(BASELINE2_COST, Colors.KHAKI);
			map.put(BASELINE3_COST, Colors.TAN);
			map.put(BASELINE4_COST, Colors.NAVY);
			map.put(BASELINE5_COST, Colors.TURQUOISE);
			map.put(BASELINE6_COST, Colors.VIOLET);
			map.put(BASELINE7_COST, Colors.MAROON);
			map.put(BASELINE8_COST, Colors.SALMON);
			map.put(BASELINE9_COST, Colors.ORANGE);
			map.put(BASELINE10_COST, Colors.CYAN);
		}
		return map;
	}

	public static Color getColorForField(Object field) {
		Color result = (Color) getMap().get(field);
		if (result == null)
			result = Color.BLACK;
		return result;

	}

}
