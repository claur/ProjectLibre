///*
//The contents of this file are subject to the Common Public Attribution License
//Version 1.0 (the "License"); you may not use this file except in compliance with
//the License. You may obtain a copy of the License at
//http://www.projity.com/license . The License is based on the Mozilla Public
//License Version 1.1 but Sections 14 and 15 have been added to cover use of
//software over a computer network and provide for limited attribution for the
//Original Developer. In addition, Exhibit A has been modified to be consistent
//with Exhibit B.
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
//specific language governing rights and limitations under the License. The
//Original Code is OpenProj. The Original Developer is the Initial Developer and
//is Projity, Inc. All portions of the code written by Projity are Copyright (c)
//2006, 2007. All Rights Reserved. Contributors Projity, Inc.
//
//Alternatively, the contents of this file may be used under the terms of the
//Projity End-User License Agreeement (the Projity License), in which case the
//provisions of the Projity License are applicable instead of those above. If you
//wish to allow use of your version of this file only under the terms of the
//Projity License and not to allow others to use your version of this file under
//the CPAL, indicate your decision by deleting the provisions above and replace
//them with the notice and other provisions required by the Projity  License. If
//you do not delete the provisions above, a recipient may use your version of this
//file under either the CPAL or the Projity License.
//
//[NOTE: The text of this license may differ slightly from the text of the notices
//in Exhibits A and B of the license at http://www.projity.com/license. You should
//use the latest text at http://www.projity.com/license for your modifications.
//You may not remove this license text from the source files.]
//
//Attribution Information: Attribution Copyright Notice: Copyright © 2006, 2007
//Projity, Inc. Attribution Phrase (not exceeding 10 words): Powered by OpenProj,
//an open source solution from Projity. Attribution URL: http://www.projity.com
//Graphic Image as provided in the Covered Code as file:  openproj_logo.png with
//alternatives listed on http://www.projity.com/logo
//
//Display of Attribution Information is required in Larger Works which are defined
//in the CPAL as a work which combines Covered Code or portions thereof with code
//not governed by the terms of the CPAL. However, in addition to the other notice
//obligations, all copies of the Covered Code in Executable and Source Code form
//distributed must, as a form of attribution of the original author, include on
//each user interface screen the "OpenProj" logo visible to all users.  The
//OpenProj logo should be located horizontally aligned with the menu bar and left
//justified on the top left of the screen adjacent to the File menu.  The logo
//must be at least 100 x 25 pixels.  When users click on the "OpenProj" logo it
//must direct them back to http://www.projity.com.
//*/
//
//package com.projity.server.data.mspdi;
//
//import java.io.InputStream;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//import net.sf.mpxj.Duration;
//import net.sf.mpxj.MPXJException;
//import net.sf.mpxj.ProjectFile;
//import net.sf.mpxj.Resource;
//import net.sf.mpxj.ResourceAssignment;
//import net.sf.mpxj.SplitTaskFactory;
//import net.sf.mpxj.TimeUnit;
//import net.sf.mpxj.TimephasedWorkNormaliser;
//import net.sf.mpxj.mspdi.MSPDIReader;
//import net.sf.mpxj.mspdi.schema.Project;
//import net.sf.mpxj.mspdi.schema.TimephasedDataType;
//
//import com.projity.contrib.util.Log;
//import com.projity.contrib.util.LogFactory;
//import com.projity.pm.assignment.Assignment;
//import com.projity.pm.calendar.WorkCalendar;
//import com.projity.pm.resource.EnterpriseResource;
//
///**
// * This class is used to represent a Microsoft Project Data Interchange (MSPDI)
// * XML file. This implementation allows the file to be read, and the data it
// * contains exported as a set of MPX objects. These objects can be interrogated
// * to retrieve any required data, or stored as an MPX file.
// */
//public class ModifiedMSPDIReader extends MSPDIReader {
//	//static Log logg=LogFactory.getLog(ModifiedMSPDIFile.class);
//	/**
//	 * This constructor allows a new MSPDI file to be created from scratch.
//	 */
//	public ModifiedMSPDIReader() {
//		super();
//	}
//
//   public ProjectFile read (InputStream stream) throws MPXJException {
////		return  super.read(stream);
//	   Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
////	   ProjectFile pf =  readSax(stream);
//	   ProjectFile pf =  super.read(stream); //claur Sax is now used in mxpj
////	   processPreds();
//		return pf;
//   }
//
//
//	private Resource unassignedResource() {
//		// add the unassigned resource
//		if (UNASSIGNED == null) {
//			UNASSIGNED = m_projectFile.addResource();
//			UNASSIGNED.setUniqueID(EnterpriseResource.UNASSIGNED_ID);
//		}
//		return UNASSIGNED;
//
//	}
//	protected void readResources(Project project, HashMap calendarMap) {
//		super.readResources(project,calendarMap);
//		unassignedResource();
//
//	}
//
//
//
//	// Projity specific stuff below
//
//	static Log log = LogFactory.getLog(ModifiedMSPDIReader.class);
//	protected com.projity.pm.task.Project projityProject;
//	protected Map projityTaskMap = new HashMap();
//	protected Map projityAssignmentMap = new HashMap();
//	protected Map projitySnapshotIdMap = new HashMap();
//	private static Resource UNASSIGNED = null;
//
//
//	public void setProjityProject(com.projity.pm.task.Project projityProject) {
//		this.projityProject = projityProject;
//	}
//
//	public void putProjityTaskMap(Object mpx, Object projity) {
//		projityTaskMap.put(mpx, projity);
//	}
//
//	public void putProjityAssignmentMap(Object mpx, Object projity) {
//		projityAssignmentMap.put(mpx, projity);
//	}
//
//	public void putProjitySnapshotIdMap(Object mpx, Object projity) {
//		projitySnapshotIdMap.put(mpx, projity);
//	}
//
//
//	/**
//	 * overloads default behavior to return the "unassigned" resource
//	 */
//	public Resource getResourceByUniqueID(int id) {
//		Resource r;
//		if (id == EnterpriseResource.UNASSIGNED_ID)
//			r = unassignedResource();
//		else
//			r = m_projectFile.getResourceByUniqueID(id);
//		return r;
//
//	}
//
//	public static void readAssignmentBaselinesAndTimephased(Assignment projityAssignment, List timePhasedList) {
//		Iterator i = timePhasedList.iterator();
//		TimephasedService service = TimephasedService.getInstance();
//
////		boolean isPodServer = !Environment.isNoPodServer(); //claur
////		if (isPodServer){
//			while (i.hasNext()) {
//				service.readTimephased(projityAssignment, (TimephasedDataType) i.next());
//			}
////		} else {
////			// JGao - 9/16/2009 Added logic to combine all time phased data on the same day
////			// This way, the contour on pod is set correctly without worring about the calendar differences
////			TimephasedDataType tempData = null;
////			while (i.hasNext()) {
////				TimephasedDataType currentData = (TimephasedDataType) i.next();
////				if (tempData == null){
////					tempData = new TimephasedDataType();
////					CloneTimephasedData( currentData, tempData );
////					if (i.hasNext())
////						continue;
////
////				} else {
////					boolean dataCombined = CombineTimephasedDataIfOnSameDay( tempData, currentData );
////					if (dataCombined && i.hasNext())
////						continue;
////				}
////
////				service.readTimephased( projityAssignment, tempData );
////
////				tempData = null;
////				tempData = new TimephasedDataType();
////				CloneTimephasedData( currentData, tempData );
////				if (!i.hasNext()){
////					service.readTimephased( projityAssignment, tempData );
////				}
////			}
////		}
//		projityAssignment.makeFlatIfPossible(); // don't use a contour if it's
//												// really flat
//	}
//
//	private static void CloneTimephasedData(TimephasedDataType from, TimephasedDataType to){
//		to.setType( from.getType() );
//		to.setUID( from.getUID() );
//		to.setUnit( from.getUnit() );
////		// Set time to 12 midnight
////		Calendar newCalendarValue = Calendar.getInstance();
////		newCalendarValue.setTimeInMillis( DateTime.dayFloor( DateTime.gmt(from.getStart().getTime())) );
//		to.setStart( (Calendar) from.getStart().clone() );
////		newCalendarValue = Calendar.getInstance();
////		newCalendarValue.setTimeInMillis( DateTime.dayFloor( DateTime.gmt(from.getFinish().getTime())) );
//		to.setFinish( (Calendar) from.getStart().clone() );
//		to.getFinish().add( Calendar.DAY_OF_MONTH, 1 );
//		to.setValue( from.getValue() );
//	}
//
//	private static boolean CombineTimephasedDataIfOnSameDay( TimephasedDataType original, TimephasedDataType newData ){
//		boolean dataCombined = false;
//		Date newStart = newData.getStart().getTime();
//		Date newFinish = newData.getFinish().getTime();
//		Date originalStart = original.getStart().getTime();
//		Date originalFinish = original.getFinish().getTime();
//		if (newStart.after(originalStart) && newFinish.before(originalFinish)){
//			long sumValue = XsdDuration.millis( original.getValue() ) + XsdDuration.millis( newData.getValue() );
//			Duration combined = Duration.getInstance( sumValue / WorkCalendar.MILLIS_IN_MINUTE, TimeUnit.MINUTES );
//			original.setValue( new XsdDuration(combined).toString() );
//			dataCombined = true;
//		}
//
//		return dataCombined;
//	}
//	
//	
//	
//	/**
//	 * This method extracts data for a single assignment from an MSPDI file.
//	 *
//	 * @param assignment
//	 *            Assignment data
//	 */
//	public ResourceAssignment readAssignment(Project.Assignments.Assignment assignment, SplitTaskFactory splitFactory, TimephasedWorkNormaliser normaliser) { //claur
//		ResourceAssignment mpx = super.readAssignment(assignment,splitFactory,normaliser);
//		if (mpx != null)
//			putTimephasedList(mpx, assignment.getTimephasedData()); // so as to extract timephased data later on
//		return mpx;
//	}
//	protected Map<ResourceAssignment,List<TimephasedDataType>> timephasedMap = new HashMap<ResourceAssignment,List<TimephasedDataType>>();
//	public void putTimephasedList(ResourceAssignment mpx, List<TimephasedDataType> timephasedList) { //claur
//		if (mpx == null || timephasedList == null)
//			return;
//		timephasedMap.put(mpx, timephasedList);
//	}
//
//	public List<TimephasedDataType> getTimephasedList(ResourceAssignment mpx) {
//		return (List<TimephasedDataType>) timephasedMap.get(mpx);
//	}
//
//}
