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
package com.projity.pm.graphic.gantt;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.apache.commons.collections.Closure;

import com.projity.field.Field;
import com.projity.field.FieldConverter;
import com.projity.functor.IntervalConsumer;
import com.projity.functor.ScheduleIntervalGenerator;
import com.projity.graphic.configuration.BarFormat;
import com.projity.graphic.configuration.BarStyles;
import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.graphic.configuration.TexturedShape;
import com.projity.graphic.configuration.shape.PredefinedPaint;
import com.projity.options.GanttOption;
import com.projity.pm.calendar.CalendarService;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.dependency.DependencyType;
import com.projity.pm.graphic.gantt.link_routing.GanttLinkRouting;
import com.projity.pm.graphic.graph.GraphParams;
import com.projity.pm.graphic.graph.GraphRenderer;
import com.projity.pm.graphic.graph.LinkRouting;
import com.projity.pm.graphic.model.cache.GraphicDependency;
import com.projity.pm.graphic.model.cache.GraphicNode;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.timescale.CoordinatesConverter;
import com.projity.pm.scheduling.ScheduleInterval;
import com.projity.pm.task.Project;
import com.projity.timescale.CalendarUtil;
import com.projity.timescale.TimeInterval;
import com.projity.timescale.TimeIterator;
import com.projity.util.DateTime;
import com.projity.util.Environment;
import com.projity.util.FontUtil;

public class GanttRenderer extends GraphRenderer implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -7437190083991277084L;
	protected NodeRenderer nodeRenderer = new NodeRenderer();
	protected LinkRenderer linkRenderer = new LinkRenderer();
	protected HorizontalLineRenderer horizontalLineRenderer = new HorizontalLineRenderer();
	protected AnnotationRenderer annotationRenderer = new AnnotationRenderer();

	protected GraphicConfiguration config;
	protected JComponent container;


	public GanttRenderer(){
		super();
		config=GraphicConfiguration.getInstance();
	}
	public GanttRenderer(GraphParams graphInfo){
		super(graphInfo);
		config=GraphicConfiguration.getInstance();
		if (graphInfo instanceof JComponent)
			container=(JComponent)graphInfo;
	}



	private class NodeRenderer implements Closure, IntervalConsumer, Serializable {
		private static final long serialVersionUID = -1348039741030744803L;
		GraphicNode node;
		Graphics2D g2;
		protected GanttBarSingleIntervalGenerator singleIntervalGenerator=new GanttBarSingleIntervalGenerator();
		protected ScheduleInterval interval;
		protected BarFormat format;
		protected int yrow;
		protected int maxLayer=Integer.MAX_VALUE;
		protected int minLayer=0;

		public void initialize(Graphics2D g2, GraphicNode node) {
			this.g2 = g2;
			this.node = node;
			int rowHeight=((GanttParams)graphInfo).getRowHeight();
			yrow=node.getRow()*rowHeight;
			setLayers(BarFormat.MIN_FOREGROUND_LAYER,BarFormat.MAX_FOREGROUND_LAYER);
		}

		public int getMaxLayer() {
			return maxLayer;
		}
		public void setMaxLayer(int maxLayer) {
			this.maxLayer = maxLayer;
		}

		public int getMinLayer() {
			return minLayer;
		}
		public void setMinLayer(int minLayer) {
			this.minLayer = minLayer;
		}

		public void setLayers(int minLayer,int maxLayer) {
			this.minLayer = minLayer;
			this.maxLayer = maxLayer;
		}

/**
 * This is the callback which is called from barStyles.apply() below
 */
		public void execute(Object arg0) {
			format = (BarFormat)arg0;
			if (format.getLayer()>maxLayer||format.getLayer()<minLayer) return;



		    ScheduleIntervalGenerator intervalGenerator;
			if (format.getScheduleIntervalGenerator()==null){
				singleIntervalGenerator.initialize(graphInfo.getCache().getModel(),format.getFromField(),format.getToField());
				intervalGenerator=singleIntervalGenerator;
			}else{
				intervalGenerator=format.getScheduleIntervalGenerator();
			}

			intervalGenerator.consumeIntervals(node,this);

		}



		public void consumeInterval(ScheduleInterval interval){
//			System.out.println("GanttUI consuming interval " + new java.util.Date(interval.getStart()) + " " + new java.util.Date(interval.getEnd()));
//			if (interval.getEnd() < interval.getStart())
//				return;
			CoordinatesConverter coord=((GanttParams)graphInfo).getCoord();
			if (interval.getEnd()>100000000000000L){
				// this hasn't happened in years. whatever caused it is fixed, but keeping just in case
				System.out.println("ERROR!!! leads to OutOfMemoryError, consumeInterval interval="+interval.getStart()+", "+CalendarUtil.toString(interval.getStart())+", "+interval.getEnd()+", "+CalendarUtil.toString(interval.getEnd())+"...");
				return;
			}
			double x=coord.toX(interval.getStart());
			double width=CoordinatesConverter.adaptSmallBarEndX(x,coord.toX(interval.getEnd()),node,config)-x;
//			double width=coord.toW(interval.getEnd()-interval.getStart());
			double height;
			double y=yrow+config.getGanttBarYOffset();
			int row=format.getRow();
		    if (row==1){
		    	height=config.getGanttBarHeight();
		    }
		    else{
		    	height=config.getBaselineHeight();
			    y+=config.getGanttBarHeight()+config.getBaselineHeight()*(row-2);
		    }
	    	y+=height/2;

			double dw=height;

			if (format.getMiddle()!=null){

				if (g2==null&&format.isMain()){
					Shape shape=format.getMiddle().toGeneralPath(
							width,
							height,
							x,
							y,
							null);
					Rectangle2D bounds=shape.getBounds2D();
					node.setGanttShapeOffset(bounds.getY()-y+height/2);
					node.setGanttShapeHeight(bounds.getHeight());
				}else{
					Shape shape=format.getMiddle().draw(g2,
							width,
							height,
							x,
							y,
							useTextures());

				}
				// draw middle before ends
			}
			if (g2==null) return;

			if (format.getStart()!=null) format.getStart().draw(g2,
					dw,
					height,
					x ,
					y,
					useTextures());
			if (format.getEnd()!=null) format.getEnd().draw(g2,
					dw,
					height,
					x+width,
					y,
					useTextures()); //TODO case when no start symbol

			//TODO style and format of completion should be treated with bar prefererences instead of a special case
			if (format.isMain()&&!node.isSummary()&&node.isStarted()){
				long completedT=node.getCompleted();
				if (completedT>=interval.getStart()){
					double completedW=coord.toX(completedT)-x;
					if (completedW>width && !GanttOption.getInstance().isCompletionIsContiguous())
						completedW=width;
					completedW=CoordinatesConverter.adaptSmallBarEndX(x, x+completedW, node,config)-x;
					Rectangle2D progressBar=new Rectangle2D.Double(x,y-config.getGanttProgressBarHeight()/2,completedW,config.getGanttProgressBarHeight());
					g2.setColor(Color.BLACK);
					g2.fill(progressBar);
				}
			}


		}

	}

	private class AnnotationRenderer implements Closure, Serializable {
		private static final long serialVersionUID = -137778741030744803L;
		protected BarFormat format;
		GraphicNode node;
		Graphics2D g2;
		protected int yrow;
		protected JLabel component=new JLabel();
		protected CellRendererPane rendererPane;
		FontMetrics fontMetrics;

		public void initialize(Graphics2D g2, GraphicNode node) {
			this.g2 = g2;
			this.node = node;
			int rowHeight=((GanttParams)graphInfo).getRowHeight();
			config=((GanttParams)graphInfo).getConfiguration();
			yrow=node.getRow()*rowHeight;

			if (container!=null){
				rendererPane=new CellRendererPane();
				container.add(rendererPane);
			}
			component.setFont(FontUtil.getFont(null,Environment.GANTT_ANNOTATIONS_FONT));
			fontMetrics=component.getFontMetrics(component.getFont());
		}

		public void execute(Object arg0) {
			format = (BarFormat)arg0;
			Field field=format.getField();
			if (field==null) return;
			Object value=field.getValue(node.getNode(),graphInfo.getCache().getModel(),null);
			if (value==null) return;
			CoordinatesConverter coord=((GanttParams)graphInfo).getCoord();

//			int y=yrow+config.getGanttBarHeight()+config.getGanttBarYOffset();
//			int x=(int)Math.ceil(coord.toX(node.getEnd()))+config.getGanttBarAnnotationXOffset();
//			Color oldColor=g2.getColor();
//			g2.setColor(format.getMiddle().getColor());
//			g2.drawString(ObjectConverterManager.toString(value,value.getClass()), x, y);
//			if (oldColor!=null) g2.setColor(oldColor);
			String s;
			if (value instanceof Date){
				Date d=(Date)value;
				s=DateFormat.getDateInstance(DateFormat.SHORT).format(d);
				int i=s.lastIndexOf('/');
				if (i>0) s=s.substring(0, i);
			}
			else s=FieldConverter.toString(value,value.getClass(),null);
			component.setText(s); //field.getClazz()?
			int y=yrow+config.getGanttBarYOffset();//+config.getGanttBarAnnotationYOffset();
			double x0=coord.toX(node.getStart());
			double x1=coord.toX(node.getEnd());
			x1=CoordinatesConverter.adaptSmallBarEndX(x0,x1,node,config);

			int x=(int)Math.ceil(x1)+config.getGanttBarAnnotationXOffset();
			int w=fontMetrics.stringWidth(s);//config.getGanttBarAnnotationMaxWidth();
			int h=config.getGanttBarHeight();

			if (container==null){
		    	component.setDoubleBuffered(false);
		    	component.setOpaque(false);
		    	component.setForeground(format.getMiddle().getColor());
		    	component.setSize(w,h);
		    	g2.translate(x,y);
		    	component.doLayout();
		    	component.print(g2);
		    	g2.translate(-x,-y);
			}
			else rendererPane.paintComponent(g2,component,container,x,y,w,h,true);



		}



	}

	private class HorizontalLineRenderer implements Closure, Serializable {
		private static final long serialVersionUID = -6350307720624037262L;
		protected BarFormat format;
		GraphicNode node;
		Graphics2D g2;
		protected int yrow;

		public void initialize(Graphics2D g2, GraphicNode node) {
			this.g2 = g2;
			this.node = node;
			int rowHeight=((GanttParams)graphInfo).getRowHeight();
			config=((GanttParams)graphInfo).getConfiguration();
			yrow=(node.getRow()+1)*rowHeight -1; // draws under each row

		}

		public void execute(Object arg0) {
			format = (BarFormat)arg0;
			Rectangle bounds = g2.getClipBounds();
			Stroke oldStroke = g2.getStroke();
			Color oldColor = g2.getColor();
			g2.setColor(format.getMiddle().getColor());
			g2.drawLine(bounds.x,yrow,bounds.x+bounds.width,yrow);
			g2.setColor(oldColor);
		}
	}


	private class LinkRenderer implements Closure, Serializable {
		private static final long serialVersionUID = -2031158189787837110L;
		protected BarFormat format;
		protected GraphicDependency dependency;
		protected Graphics2D g2;
		void initialize(Graphics2D g2, GraphicDependency dependency) {
			this.g2 = g2;
			this.dependency = dependency;
		}


		private double[] extraPoints=new double[3];
		public void execute(Object arg0) {
			format = (BarFormat)arg0;

			GanttLinkRouting routing=(GanttLinkRouting)((GanttParams)graphInfo).getRouting();
			CoordinatesConverter coord=((GanttParams)graphInfo).getCoord();
			//if (format.getMiddle()!=null){
			    GraphicNode from=dependency.getPredecessor();
			    GraphicNode to=dependency.getSuccessor();
			    int type=dependency.getType();
				int fromSign=(type==DependencyType.SF||type==DependencyType.SS)?-1:1;
				int toSign=(type==DependencyType.FS||type==DependencyType.SS)?-1:1;
				double fx0=coord.toX(from.getStart());
				double fx1=coord.toX(from.getEnd());
				fx1=CoordinatesConverter.adaptSmallBarEndX(fx0,fx1,from,config);
				double tx0=coord.toX(to.getStart());
				double tx1=coord.toX(to.getEnd());
				tx1=CoordinatesConverter.adaptSmallBarEndX(tx0,tx1,to,config);
				double x0=fromSign<0?fx0:fx1;
				double x1=toSign<0?tx0:tx1;
				int rowHeight=((GanttParams)graphInfo).getRowHeight();
				int yOffset=config.getGanttBarYOffset()+config.getGanttBarHeight()/2;
				int y0=rowHeight*from.getRow();
				int y1=rowHeight*to.getRow();
				double y2=Math.max(y0,y1);
				y0+=yOffset;
				y1+=yOffset;

				GeneralPath path=dependency.getPath();
				((GanttLinkRouting)routing).routePath(path,x0,y0,x1,y1,y2,y1+to.getGanttShapeHeight()/2,y1-to.getGanttShapeHeight()/2,type);



				Color oldColor=g2.getColor();
				Stroke oldStroke = g2.getStroke();
				Dependency dep = dependency.getDependency();
				if (dep.isDisabled()) g2.setStroke(DISABLED_LINK_STROKE);
				if (dep.isCrossProject()) g2.setColor(/*dep.isDirty()?Color.ORANGE:*/EXTERNAL_LINK_COLOR);
				else g2.setColor(/*dep.isDirty()?Color.RED:*/format.getMiddle().getColor());
				g2.draw(path);

			//}
			if (format.getStart()==null&&format.getEnd()==null) return;
			if (format.getStart()!=null){
				double theta=routing.getFirstAngle();
				AffineTransform transform=(theta==0)?null:AffineTransform.getRotateInstance(theta,routing.getFirstX(),routing.getFirstY());
				drawLinkArrows(dep,transform,format.getStart());
			}
			if (format.getEnd()!=null){
				double theta=routing.getLastAngle();
				AffineTransform transform=(theta==Math.PI||theta==-Math.PI)?null:AffineTransform.getRotateInstance(Math.PI-theta,routing.getLastX(),routing.getLastY());
				drawLinkArrows(dep,transform,format.getEnd());
			}

			if (oldColor!=null) g2.setColor(oldColor);
			if (oldStroke!= null) g2.setStroke(oldStroke);
		}

		private void drawLinkArrows(Dependency dep, AffineTransform transform, TexturedShape shape) {
			Color oldEndColor = format.getEnd().getColor();
			if (dep.isCrossProject())
				shape.setPaint(EXTERNAL_LINK_COLOR);
			g2.setColor(shape.getColor());
			LinkRouting routing=((GanttParams)graphInfo).getRouting();
			shape.draw(g2,routing.getLastX(),routing.getLastY(),transform,useTextures());
			if (dep.isCrossProject())
				shape.setPaint(oldEndColor);
		}
	}


    public void updateShapes(ListIterator nodeIterator){

    	Rectangle bounds = ((GanttParams)graphInfo).getGanttBounds();
    	CoordinatesConverter coord=((GanttParams)graphInfo).getCoord();
    	if (coord==null) return;
		double rowHeight=((GanttParams)graphInfo).getRowHeight();

		int i0=(int)Math.floor(bounds.getY()/rowHeight);
		int i1=(int)Math.ceil(bounds.getMaxY()/rowHeight);
		double t0=coord.toTime(bounds.getX());
		double t1=coord.toTime(bounds.getMaxX());

		GraphicNode node;
		for (ListIterator i=nodeIterator;i.hasNext();){
			node=(GraphicNode)i.next();
			node.setRow(i.previousIndex());
			if (i.previousIndex()>=i0&&i.previousIndex()<i1){
				if (!node.isVoid()) updateShape(node);
			}
		}
    }

    public void updateShape(GraphicNode node){
    	if (((GanttParams)graphInfo).getCoord()==null) return; //not initialized
		BarStyles barStyles = graphInfo.getBarStyles();
		nodeRenderer.initialize(null,node);
		barStyles.apply(node.getNode().getImpl(),nodeRenderer);

    }

	public void paintNode(Graphics2D g2,GraphicNode node, boolean background){
		BarStyles barStyles = graphInfo.getBarStyles();
		nodeRenderer.initialize(g2,node);

		if (background)
			nodeRenderer.setLayers(BarFormat.MIN_BACKGROUND_LAYER,BarFormat.MAX_BACKGROUND_LAYER);
		else nodeRenderer.setLayers(BarFormat.MIN_FOREGROUND_LAYER,BarFormat.MAX_FOREGROUND_LAYER);
		barStyles.apply(node.getNode().getImpl(),nodeRenderer);

	}

	public void paintAnnotation(Graphics2D g2,GraphicNode node){
		BarStyles barStyles = graphInfo.getBarStyles();
		annotationRenderer.initialize(g2,node);
		barStyles.apply(node.getNode().getImpl(),annotationRenderer,false,true,false, false);
	}

	public void paintHorizontalLine(Graphics2D g2,GraphicNode node){
		BarStyles barStyles = graphInfo.getBarStyles();
		horizontalLineRenderer.initialize(g2,node);
		barStyles.apply(node.getNode().getImpl(),horizontalLineRenderer,false,false,false, true);
	}

	public void paintLink(Graphics2D g2, GraphicDependency dependency){
		BarStyles barStyles = graphInfo.getBarStyles();
		linkRenderer.initialize(g2,dependency);
		barStyles.apply(dependency,linkRenderer,true,false,false, false);
	}


	protected BarFormat calendarFormat;
	protected Closure calendarClosure=new Closure(){
		public void execute(Object arg0) {
			calendarFormat = (BarFormat)arg0;
		}
	};
	protected BarFormat getCalendarFormat(){
		calendarFormat=null;
		if (calendarFormat==null){
			BarStyles barStyles = graphInfo.getBarStyles();
			barStyles.apply(null,calendarClosure,false,false,true, false);
		}
		return calendarFormat;
	}

	public void paintNonWorkingDays(Graphics2D g2,Rectangle bounds){
		BarFormat calFormat=getCalendarFormat();
		if (calFormat==null) return;
		//non working days
			Color oldColor=g2.getColor();
			Paint oldPaint=g2.getPaint();
			CoordinatesConverter coord=((GanttParams)graphInfo).getCoord();
			Project project=coord.getProject();
			WorkingCalendar wc=(WorkingCalendar)project.getWorkCalendar();

			if (coord.getTimescaleManager().isShowWholeDays()){
				boolean useScale2=coord.getTimescaleManager().getCurrentScaleIndex()==0; //valid only for current time scales
				TimeIterator i=coord.getTimeIterator(bounds.getX(), bounds.getMaxX(),useScale2);
				long startNonworking=-1L,endNonWorking=-1L;
				Calendar cal=DateTime.calendarInstance();

				PredefinedPaint paint=(PredefinedPaint)calFormat.getMiddle().getPaint();//new PredefinedPaint(PredefinedPaint.DOT_LINE,Colors.VERY_LIGHT_GRAY,Color.WHITE);
				paint.applyPaint(g2, useTextures());
				while (i.hasNext()){
					TimeInterval interval=i.next();
					long s=interval.getStart();
					if (CalendarService.getInstance().getDay(wc, s).isWorking()){
						if (startNonworking!=-1L){
							drawNonWorking(g2, startNonworking, endNonWorking, cal, coord, bounds,useScale2);
							startNonworking=endNonWorking=-1L;
						}
					}else{
						if (startNonworking==-1L) startNonworking=s;
						endNonWorking=s;

					}
				}
				if (startNonworking!=-1L){
					drawNonWorking(g2, startNonworking, endNonWorking, cal, coord, bounds,useScale2);
					startNonworking=endNonWorking=-1L;
				}
			}

		if (container!=null){
			//scale2 separation lines
			TimeIterator i=coord.getTimeIterator(bounds.getX(), bounds.getMaxX(),true);
			g2.setPaint(new PredefinedPaint(PredefinedPaint.DOT_LINE2,Color.GRAY,g2.getBackground()));
			while (i.hasNext()){
				TimeInterval interval=i.next();
				int startX=(int)Math.round(coord.toX(interval.getStart()));
				g2.drawLine(startX,bounds.y,startX,bounds.y+bounds.height);
			}

			//project start
			int projectStartX=(int)Math.round(coord.toX(project.getStart()));
			if (projectStartX>=bounds.getX()&&projectStartX<=bounds.getMaxX()){
				g2.setPaint(new PredefinedPaint(PredefinedPaint.DASH_LINE,Color.GRAY,g2.getBackground()));
				g2.drawLine(projectStartX,bounds.y,projectStartX,bounds.y+bounds.height);
			}

			//project start
			long statusDate = project.getStatusDate();
			if (statusDate != 0) {
				int statusDateX=(int)Math.round(coord.toX(statusDate));
				if (statusDateX>=bounds.getX()&&statusDateX<=bounds.getMaxX()){
					g2.setPaint(new PredefinedPaint(PredefinedPaint.DOT_LINE2,Color.GREEN,g2.getBackground()));
					g2.drawLine(statusDateX,bounds.y,statusDateX,bounds.y+bounds.height);
				}
			}


			if (oldColor!=null) g2.setColor(oldColor);
			if (oldPaint!=null) g2.setPaint(oldPaint);

		}
	}

	private void drawNonWorking(Graphics2D g2,long startNonworking,long endNonWorking, Calendar cal,CoordinatesConverter coord, Rectangle bounds,boolean userScale2){
		cal.setTimeInMillis(endNonWorking);
		if (userScale2) coord.getTimescaleManager().getScale().increment2(cal);
		else coord.getTimescaleManager().getScale().increment1(cal);
		endNonWorking=cal.getTimeInMillis();
		g2.fillRect((int)Math.round(coord.toX(startNonworking)), bounds.y, (int)Math.round(coord.toW(endNonWorking-startNonworking)), bounds.height);
	}


	ArrayList nodeList=new ArrayList();
    public void paint(Graphics g) {
    	paint(g,null);
    }
    public void paint(Graphics g,Rectangle visibleBounds) {
		Graphics2D g2=(Graphics2D)g;
    	//CoordinatesConverter coord=((GanttParams)graphInfo).getCoord();

		Rectangle clipBounds = g2.getClipBounds();
		Rectangle svgClip=clipBounds;
		if (clipBounds==null){
			clipBounds=((GanttParams)getGraphInfo()).getGanttBounds();
			//start at O,O because it's already translated
			if (visibleBounds==null) clipBounds=new Rectangle(0,1,clipBounds.width,clipBounds.height-2);//1 pixel offset needed for edge
//			else clipBounds=new Rectangle(visibleBounds.x-clipBounds.x,visibleBounds.y-clipBounds.y,visibleBounds.width,visibleBounds.height);
			else {
				clipBounds=visibleBounds;
				g2.setClip(clipBounds);
			}
		}

		paintNonWorkingDays(g2,clipBounds);

		//Modif for offline graphics

		double rowHeight=((GanttParams)graphInfo).getRowHeight();

		int i0=(int)Math.floor(clipBounds.getY()/rowHeight);
		int i1;
		if (visibleBounds==null) i1=(int)Math.ceil(clipBounds.getMaxY()/rowHeight);
		else i1=(int)Math.floor(clipBounds.getMaxY()/rowHeight);
		//double t0=coord.toTime(clipBounds.getX());
		//double t1=coord.toTime(clipBounds.getMaxX());

		nodeList.clear();

		GraphicNode node;
//		for (ListIterator i=graph.getModel().getNodeIterator(i0);i.hasNext()&&i.nextIndex()<=i1;){
//			node=(GraphicNode)i.next();
//			if (!node.isSchedule()) continue;
//			nodeList.add(node);
//			node.setRow(i.previousIndex());
//			paintNode(g2,node,true);
//		} //Because row not initialized for some nodes

		NodeModelCache cache=graphInfo.getCache();
		for (ListIterator i=cache.getIterator();i.hasNext();){
			node=(GraphicNode)i.next();
			node.setRow(i.previousIndex());
			if (i.previousIndex()>=i0&&i.previousIndex()<i1){
				if (!node.isSchedule()) continue;
				nodeList.add(node);
				paintAnnotation(g2,node);
				paintNode(g2,node,true);
				paintHorizontalLine(g2,node);
			}
		}

		GraphicDependency dependency;
		for (Iterator i=cache.getEdgesIterator();i.hasNext();){
			dependency=(GraphicDependency)i.next();
			//if (nodeList.contains(dependency.getPredecessor())||nodeList.contains(dependency.getSuccessor()))
				paintLink(g2,dependency);
		}

		for (ListIterator i=nodeList.listIterator();i.hasNext();){
			node=(GraphicNode)i.next();
			paintNode(g2,node,false);
		}

		if (visibleBounds!=null) g2.setClip(svgClip);

	}



}
