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
package com.projity.pm.graphic.chart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.Closure;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.projity.algorithm.TimeIteratorGenerator;
import com.projity.algorithm.buffer.CalculatedValues;
import com.projity.algorithm.buffer.GroupedCalculatedValues;
import com.projity.algorithm.buffer.NonGroupedCalculatedValues;
import com.projity.algorithm.buffer.SeriesCallback;
import com.projity.field.Field;
import com.projity.options.CalendarOption;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.HasAssignments;
import com.projity.pm.assignment.HasTimeDistributedData;
import com.projity.pm.assignment.TimeDistributedConstants;
import com.projity.pm.graphic.timescale.CoordinatesConverter;
import com.projity.pm.graphic.views.ChartView;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.task.Project;
import com.projity.timescale.TimeInterval;
import com.projity.timescale.TimeIterator;
import com.projity.util.Environment;


public class ChartModel implements TimeDistributedConstants, Serializable {
	private static final long serialVersionUID = -1617376166476506096L;
	private ChartView chartModel;
	XYSeriesCollection seriesCollection;
	XYSeriesCollection secondSeriesCollection = null;
	CoordinatesConverter coord;

	public ChartModel(CoordinatesConverter coord) {
		this.coord = coord;
	}
	
	public CalculatedValues computeTrace(Iterator taskIterator, List resources, Object trace,  boolean histogram, boolean cumulative) {
		if (taskIterator == null || !taskIterator.hasNext()) // if no task selected, dont change chart
			return null;
		CalculatedValues calculatedValues;
		if (histogram)
			calculatedValues = new GroupedCalculatedValues();
		else
			calculatedValues = new NonGroupedCalculatedValues(cumulative,coord.getOrigin());
		

		TimeIterator timeIterator = null;
		TimeIteratorGenerator generator;
		Iterator i = taskIterator;
		Object current;
		Assignment assignment;	
		double resourceMaxUnits = 0;
		ArrayList assignmentResourcesUsed = new ArrayList();
		boolean hasValues = false;
		while (i.hasNext()) { //loop thru tasks
			current = i.next();
			if (current instanceof HasAssignments) {
				List list = ((HasAssignments)current).getAssignments();

				Iterator a = list.iterator();
				while (a.hasNext()) { // loop through assignments,
					
					assignment = (Assignment)a.next();
					if (histogram) {
						timeIterator = coord.getProjectTimeIterator();
						generator = histogram ? TimeIteratorGenerator.getInstance(timeIterator) : null;
					} else {
						generator = null;
					}
					if (isTaskBased(trace)) {
						hasValues = true;
						assignment.calcDataBetween(trace,generator,calculatedValues);
						break;
					}
					if (!assignment.isDefault()) {// skip dummy assignment
						if (resources == null || resources.contains(assignment.getResource())) {
							hasValues = true;
							assignment.calcDataBetween(trace,generator,calculatedValues);
						}
					}
				}
			}
		}
		if (!hasValues) // if nothing processed
			return null;
		return calculatedValues;
	}


	public CalculatedValues computeAvailability(List resources) {
		CalculatedValues calculatedValues = new GroupedCalculatedValues();
		if (resources != null) {
			Iterator r = resources.iterator();
			while (r.hasNext()) {
				Object obj = r.next();
				if (!(obj instanceof Resource))
					continue;
				Resource res = (Resource)obj;
				TimeIterator timeIterator = coord.getProjectTimeIterator();
				TimeIteratorGenerator generator = TimeIteratorGenerator.getInstance(timeIterator);
				Assignment.calcResourceAvailabilityBetween(res, generator, calculatedValues);
			}
		}
		return calculatedValues;
	}
	
	// Other Projects
//	public CalculatedValues computeOtherProjects(List tasks, List resources) {
//		if ((resources!=null&&resources.size()>0)||(tasks!=null&&tasks.size()>0)){ //resources can be put in tasks list, is it a bug?
//			if (resources == null || resources.size() == 0)
//				return null;
//			ResourceImpl resource = (ResourceImpl) resources.get(0);
//			Iterator j=((resources==null||resources.size()==0)?tasks:resources).iterator();
//			TimeIterator timeIterator = coord.getTimeIteratorFromDates(coord.getOrigin(),coord.getEnd());
//			TimeIteratorGenerator generator = TimeIteratorGenerator.getInstance(timeIterator);
//			CalculatedValues values = new GroupedCalculatedValues();
//			SimpleCalculatedValuesFunctor visitor = SimpleCalculatedValuesFunctor.getInstance(values, generator);
//			List vList = resource.getGlobalResource().getGlobalWorkVector().getValues();
//
//			DateValueListIntervalGenerator intGen = DateValueListIntervalGenerator.getDateInstance(vList);
//
//			SelectFrom clause = SelectFrom.getInstance();
//			clause.whereInRange(generator.getStart(),generator.getEnd()); // automatically also adds a generator to limit range
//			clause.from(intGen);
//			Query query = Query.getInstance();
//			query.selectFrom(clause);
//			query.execute();
//			return values;
//		}
//		return null;
//	}
	// Other Projects
	public CalculatedValues computeOtherProjects(List tasks, List resources) {
		CalculatedValues calculatedValues = new GroupedCalculatedValues();
		if ((resources!=null&&resources.size()>0)||(tasks!=null&&tasks.size()>0)){ //resources can be put in tasks list, is it a bug?
			Iterator j=((resources==null||resources.size()==0)?tasks:resources).iterator();
			GroupedCalculatedValues c=(GroupedCalculatedValues)calculatedValues;
			
			TimeIterator timeIterator = coord.getProjectTimeIterator();
			//TODO uncomment when scheduling is corrected
			for (int k=0;timeIterator.hasNext();k++){
				TimeInterval interval=timeIterator.next();
				c.set(k,interval.getStart1(),interval.getEnd1(),0.0,null);
			}
				//return calculatedValues; 
			while (j.hasNext()){
				ResourceImpl resource;
				Object obj=j.next();
				if (obj instanceof Assignment)
					resource=(ResourceImpl)((Assignment)obj).getResource();
				else if (obj instanceof ResourceImpl)
					resource=(ResourceImpl)obj;
				else
					continue;
				GroupedCalculatedValues global=resource.getGlobalResource().getGlobalWorkVector();
				if (global != null) {
					global=global.dayByDayConvert();
					c.mergeIn(global);
				}
			}
			return c;
		}
		return calculatedValues;
	}

	public void computeHistogram(Project project, List tasks, List resources,Object[] traces) {
		boolean stackCurrentOnTop = traces == (Environment.getStandAlone()?HasTimeDistributedData.histogramTypes:HasTimeDistributedData.serverHistogramTypes);

		// Availability
		GroupedCalculatedValues availabilityCalculatedValues = (GroupedCalculatedValues) computeAvailability(resources);

		// Other Projects
		GroupedCalculatedValues otherProjectsCalculatedValues = null;
		if (!Environment.getStandAlone()) otherProjectsCalculatedValues=(GroupedCalculatedValues) computeOtherProjects(tasks,resources);
		
		//This Project
		GroupedCalculatedValues thisProjectCalculatedValues = (GroupedCalculatedValues) computeTrace(project.getTaskOutlineIterator(), resources, WORK,  true, false);

		//Selected
		GroupedCalculatedValues selectedCalculatedValues = (GroupedCalculatedValues) computeTrace(tasks==null?null:tasks.iterator(),resources, WORK,  true, false);


		// stack so that order is (from botom to top) other projects, this project, selected
		for (int i = 0; i < availabilityCalculatedValues.size(); i++) {
			double thisProject = thisProjectCalculatedValues != null ? thisProjectCalculatedValues.getUnscaledValue(i) : 0D;
			double allProjects = thisProject;
			if (otherProjectsCalculatedValues!=null) allProjects+= otherProjectsCalculatedValues.getUnscaledValue(i); // stack
			double selected = selectedCalculatedValues != null ? selectedCalculatedValues.getUnscaledValue(i) : 0D;
			if (stackCurrentOnTop) {
				if (selectedCalculatedValues != null)
					selectedCalculatedValues.setValue(i, allProjects);
				if (thisProjectCalculatedValues != null)
					thisProjectCalculatedValues.setValue(i, allProjects - selected);
			} else {
				if (otherProjectsCalculatedValues!=null) otherProjectsCalculatedValues.setValue(i,allProjects);
			}
		}
		
		XYSeries availabilitySeries = buildHistogramSeries(AVAILABILITY,availabilityCalculatedValues);
		XYSeries otherProjectsSeries = null;
		if (otherProjectsCalculatedValues!=null) otherProjectsSeries=buildHistogramSeries(OTHER_PROJECTS,otherProjectsCalculatedValues);
		XYSeries thisProjectSeries = buildHistogramSeries(THIS_PROJECT,thisProjectCalculatedValues);
		XYSeries selectedSeries = buildHistogramSeries(SELECTED,selectedCalculatedValues);
		
		
		seriesCollection = new XYSeriesCollection();
		if (stackCurrentOnTop) {
			seriesCollection.addSeries(selectedSeries);
			seriesCollection.addSeries(thisProjectSeries);
			if (otherProjectsSeries!=null) seriesCollection.addSeries(otherProjectsSeries);
		} else {
			if (otherProjectsSeries!=null) seriesCollection.addSeries(otherProjectsSeries);
			seriesCollection.addSeries(thisProjectSeries);
			seriesCollection.addSeries(selectedSeries);
		}

		secondSeriesCollection = new XYSeriesCollection();
		secondSeriesCollection.addSeries(availabilitySeries);
	}
	
	private XYSeries buildHistogramSeries(Object trace, CalculatedValues values) {
		if (values == null)
			return dummySeries(trace);
		XYSeries series = new XYSeries(trace.toString(),false,true); // dont bother sorting it already is
		makeSeries(series, trace, false, values);
		return series;
	}

	private XYSeries dummySeries(Object trace) {
		return new XYSeries(trace.toString(),false,true); // dont bother sorting it already is
		
	}
	private int findTrace(Object[] traces,Object trace) {
		for (int i = 0; i < traces.length; i++)
			if (traces[i] == trace)
				return i;
		return -1;
	}
	
	public void dumpDataset(Object[] traces) {
		for (int i = 0; i < seriesCollection.getSeriesCount(); i++) {
			System.out.println("series " + i + " " + traces[i]);
			dumpSeries(seriesCollection.getSeries(i));
		}
	}
	public static void dumpSeries(XYSeries series) {
		for (int i = 0; i < series.getItemCount(); i++) {
			System.out.println(new java.util.Date(series.getX(i).longValue()) + " " + series.getY(i));
		}
	}
	
	private boolean isTraceRectilinear(Object trace) {
		return trace == AVAILABILITY;
	}
	private void makeSeries(XYSeries series, final Object trace, boolean cumulative, CalculatedValues calculatedValues) {
		final double scaleFactor = getScaleFactor(trace); 
		final XYSeries _series = series;
		if (isTraceRectilinear(trace)) {
			calculatedValues.makeRectilinearSeries(new SeriesCallback() {
				public void add(int index, double x, double y) {
					_series.add(x,y / scaleFactor);
				}
			});
		} else {
			calculatedValues.makeSeries(cumulative,new SeriesCallback() {
				public void add(int index, double x, double y) {
					_series.add(x,y / scaleFactor);
				}
			});
		}
	}
	
	
	
	public void computeValues(List tasks, List resources, boolean cumulative, Object[] traces, boolean histogram) {
		if (tasks == null)
			return;
		CalculatedValues valuesArray[] = new CalculatedValues[traces.length];
		seriesCollection = new XYSeriesCollection();

		secondSeriesCollection = null;
		XYSeries series;
		for (int i = 0; i < traces.length; i++) {
			//System.out.println("\n trace #"+i);
			valuesArray[i] = computeTrace(tasks==null?null:tasks.iterator(),resources,traces[i],histogram,cumulative);
		}
		
		
		// done in a second step in case traces depend on each other.  Right now, there is no case like that
		for (int i = 0; i < traces.length; i++) {
			series = new XYSeries(traces[i].toString(),false,true); // dont bother sorting it already is
			if (valuesArray[i] == null) {
				System.out.println("skipping null values array " + traces[i]);
				continue;
			}
			makeSeries(series,traces[i],cumulative,valuesArray[i]);

			seriesCollection.addSeries(series);
			
		}
	}
	
	
	private double getScaleFactor(Object trace) {
		if (trace instanceof Field && !((Field)trace).isDurationOrWork())
			return 1.0;
		double hourScale = CalendarOption.getInstance().getHoursPerDay() / 24.0D; // need to adjust for number of working hours compared to 24 hours in a day
		return hourScale * coord.getIntervalDuration();
	}
	/**
	 * @return Returns the dataset.
	 */
	public AbstractXYDataset getDataset() {
		return seriesCollection;
	}
	
	private final boolean isTaskBased(Object trace) {
		return (trace == FIXED_COST ||
				trace == ACTUAL_FIXED_COST);
	}

	public XYDataset getSecondDataset() {
		return secondSeriesCollection;
	}
	
}