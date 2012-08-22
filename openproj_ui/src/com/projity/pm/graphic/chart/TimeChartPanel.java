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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Rectangle;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CustomXYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

import com.projity.datatype.Money;
import com.projity.field.Field;
import com.projity.graphic.configuration.shape.PredefinedStroke;
import com.projity.pm.assignment.HasTimeDistributedData;
import com.projity.pm.graphic.timescale.CoordinatesConverter;
import com.projity.pm.graphic.timescale.ScaledComponent;

public class TimeChartPanel extends ChartPanel implements Scrollable, ScaledComponent {
	private static final long serialVersionUID = 2034704461047717965L;

	ChartInfo chartInfo;

	JViewport viewport;

	/**
	 * @param chart
	 */
	public TimeChartPanel(ChartInfo chartInfo) {
		super(chartInfo.setChart(buildChart(chartInfo.getModel())), true);
		this.chartInfo = chartInfo;
		setMaximumDrawWidth(4000);
		setMaximumDrawHeight(1000);
	}

	// protected JScrollPane scrollPane;
	public void configureScrollPaneHeaders(JScrollPane scrollPane, JComponent rowHeader) {
		viewport = scrollPane.getViewport();
		if (viewport == null || viewport.getView() != this)
			return;

		JViewport vp = new JViewport();
		vp.setView(rowHeader);
		vp.setPreferredSize(rowHeader.getPreferredSize());
		scrollPane.setRowHeader(vp);

		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, new ChartCorner(this));

		Border border = scrollPane.getBorder();
		if (border == null || border instanceof UIResource) {
			scrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
		}

		// left scale synchro
		viewport.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateTimeScaleComponentSize();
			}
		});

	}

	// left scale synchro
	private Dimension olddmain = null;

	public void updateTimeScaleComponentSize() {
		Dimension dmain = viewport.getViewSize();

		if (dmain.equals(olddmain))
			return;
		olddmain = dmain;
		Dimension d = chartInfo.getAxisPanel().getPreferredSize();
		d.setSize(d.getWidth(), dmain.getHeight());
		chartInfo.getAxisPanel().revalidate();
	}

	protected JFreeChart buildChart() {
		JFreeChart newChart = ChartHelper.createChart(chartInfo.getModel().getDataset(), chartInfo.isHistogram(), chartInfo.getModel()
				.getSecondDataset());
		NumberFormat numberFormat = NumberFormat.getPercentInstance(); // default
		Object[] traces = chartInfo.getTraces();
		// chartInfo.getModel().dumpDataset(traces);
		if (!chartInfo.isSimple() && (traces.length > 0 && traces[0] instanceof Field)) {
			Field field = (Field) traces[0];
			if (field.isMoney()){
				numberFormat=new NumberFormat(){

					@Override
					public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
						return toAppendTo.append(Money.formatCurrency(number, true));
					}

					@Override
					public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public Number parse(String source, ParsePosition parsePosition) {
						// TODO Auto-generated method stub
						return null;
					}
				};
			}else{
				Format format = field.getFormat();
				if (format instanceof NumberFormat)
					numberFormat = (NumberFormat) format;
				else
					numberFormat = NumberFormat.getNumberInstance();
			}
		}

		((NumberAxis) newChart.getXYPlot().getRangeAxis()).setNumberFormatOverride(numberFormat);
		return newChart;
	}

	public static JFreeChart buildChart(ChartModel model) {
		return ChartHelper.createChart(model.getDataset(), true, model.getSecondDataset());
	}

	public void updateChart() {
		JFreeChart chart = chartInfo.getChart();
		final Object[] traces = chartInfo.getTraces();
		setChart(chart);

		Color color;
		Paint paint;
		int series = 0;
		for (int i = 0; i < traces.length; i++) {
			color = ChartHelper.getColorForField(traces[i]);

			if (traces[i] == HasTimeDistributedData.AVAILABILITY) {
				chart.getXYPlot().getRenderer(1).setSeriesPaint(0, color);
				chart.getXYPlot().getRenderer(1).setSeriesStroke(0, PredefinedStroke.LARGE_FRAMED);
				continue; // do not increment series
			}

			chart.getXYPlot().getRenderer().setSeriesPaint(series, color);

			chart.getXYPlot().getRenderer().setToolTipGenerator(new CustomXYToolTipGenerator() {
				public String generateToolTip(XYDataset data, int series, int item) {
					return traces[0] + " ";
				}

			});
			series++; // excludes availability from count
		}
		// chart.getXYPlot().addRangeMarker(new ValueMarker(1.0));

		chart.getXYPlot().getDomainAxis().setLowerBound(chartInfo.getCoord().getOrigin());
		chart.getXYPlot().getDomainAxis().setUpperBound(Math.max(chartInfo.getCoord().getEnd(), chartInfo.getCoord().toTime(viewport.getWidth())));
	}

	/**
	 * Gets space used by legend and headers if any
	 * 
	 * @return
	 */
	public double getNonPlotHeight() {
		ChartRenderingInfo info = getChartRenderingInfo();
		return info.getChartArea().getHeight() - info.getPlotInfo().getDataArea().getHeight();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.projity.pm.graphic.timescale.ScaledComponent#setCoord(com.projity.pm.graphic.timescale.CoordinatesConverter)
	 */
	public void setCoord(CoordinatesConverter coord) {
		chartInfo.setCoord(coord);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.projity.pm.graphic.timescale.ScaledComponent#getCoord()
	 */
	public CoordinatesConverter getCoord() {
		return chartInfo.getCoord();
	}

	protected boolean verticalScrolling = false;

	/**
	 * @return Returns the verticalScrolling.
	 */
	public boolean isVerticalScrolling() {
		return verticalScrolling;
	}

	/**
	 * @param verticalScrolling
	 *            The verticalScrolling to set.
	 */
	public void setVerticalScrolling(boolean verticalScrolling) {
		this.verticalScrolling = verticalScrolling;
	}

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return (orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width;
	}

	public boolean getScrollableTracksViewportHeight() {
		if (getParent() instanceof JViewport) {
			if (((JViewport) getParent()).getHeight() > getPreferredSize().height)
				return true;
			else
				return !verticalScrolling;
		}
		return false;
	}

	public boolean getScrollableTracksViewportWidth() {
		if (getParent() instanceof JViewport) {
			return (((JViewport) getParent()).getWidth() > getPreferredSize().width);
		}
		return false;
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.VERTICAL) {
			return 2;
		}
		return 4;
	}
	JMenuItem verticalScrollingItem;
	protected JPopupMenu createPopupMenu(boolean arg0, boolean arg1, boolean arg2, boolean arg3) {
		JPopupMenu menu = super.createPopupMenu(false, arg1, arg2, false); // hide
																			// properties
																			// and
																			// zoom
//		menu.add(new JSeparator());
//		menu.add(verticalScrollingItem = TimeChartPopupMenu.buildVerticalScrollingItem(this));
		return menu;
	}

}
