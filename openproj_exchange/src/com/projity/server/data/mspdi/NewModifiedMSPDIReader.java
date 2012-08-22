/*
 * file:       MSPDIFile.java
 * author:     Jon Iles
 * copyright:  (c) Tapster Rock Limited 2002-2003
 * date:       20/02/2003
 */

/*
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */

package com.projity.server.data.mspdi;

import net.sf.mpxj.mspdi.MSPDIReader;

/**
 * This class is used to represent a Microsoft Project Data Interchange (MSPDI)
 * XML file. This implementation allows the file to be read, and the data it
 * contains exported as a set of MPX objects. These objects can be interrogated
 * to retrieve any required data, or stored as an MPX file.
 */
public class NewModifiedMSPDIReader extends MSPDIReader {
//	//static Log logg=LogFactory.getLog(ModifiedMSPDIFile.class);
//	/**
//	 * This constructor allows a new MSPDI file to be created from scratch.
//	 */
//	public NewModifiedMSPDIReader() {
//		super();
//	}
//
//   public ProjectFile read (InputStream stream) throws MPXJException {
////		return  super.read(stream);
//	   ProjectFile pf =  readSax(stream);
//		processPreds();
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
//	/**
//	 * This method extracts data for a single assignment from an MSPDI file.
//	 * 
//	 * @param assignment
//	 *            Assignment data
//	 */
//	public ResourceAssignment readAssignment(Project.Assignments.Assignment assignment) {
//		ResourceAssignment mpx = super.readAssignment(assignment);
//		if (mpx != null)
//			putTimephasedList(mpx, assignment.getTimephasedData()); // so as to extract timephased data later on
//		return mpx;
//	}
//
//
//	// Projity specific stuff below
//	
//	static Log log = LogFactory.getLog(NewModifiedMSPDIReader.class);
//	protected com.projity.pm.task.Project projityProject;
//	protected Map projityTaskMap = new HashMap();
//	protected Map projityAssignmentMap = new HashMap();
//	protected Map projitySnapshotIdMap = new HashMap();
//	protected Map timephasedMap = new HashMap();
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
//		while (i.hasNext()) {
//			service.readTimephased(projityAssignment, (TimephasedDataType) i.next());
//		}
//		projityAssignment.makeFlatIfPossible(); // don't use a contour if it's
//												// really flat
//	}
//
////	protected void readPredecessors (Project.TasksType.TaskType task) // projity made protected
////	   {
////	      Integer uid = task.getUID();
////	      if (uid != null)
////	      {
////	         Task currTask = m_projectFile.getTaskByUniqueID(uid);
////	         if (currTask != null)
////	         {
////	            List predecessors = task.getPredecessorLink();
////	            Iterator iter = predecessors.iterator();
////
////	            while (iter.hasNext() == true)
////	            {
////	            	Object o = iter.next();
////	  //             iter.remove(); //Projity clean up memory
////	           }
////	         }
////	      }
////	   }
////	   
////	   ArrayList<Object[]> preds = new ArrayList<Object[]>();
////	   protected void readPredecessorAsUID (Task currTask, Project.TasksType.TaskType.PredecessorLinkType link) {// projity made protected
////		   preds.add(new Object[] {currTask,link});
////	   }
////	   private void processPreds() {
////		   Iterator<Object[]> i = preds.iterator();
////		   while (i.hasNext()) {
////			   Object[] pred = i.next();
////			   super.readPredecessorAsUID((Task)pred[0],(Project.TasksType.TaskType.PredecessorLinkType)pred[1]);
////			   
////		   }
////	   }
}
