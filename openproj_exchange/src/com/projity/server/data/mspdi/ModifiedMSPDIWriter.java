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

import java.math.BigInteger;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Resource;
import net.sf.mpxj.ResourceAssignment;
import net.sf.mpxj.Task;
import net.sf.mpxj.mspdi.DatatypeConverter;
import net.sf.mpxj.mspdi.MSPDIWriter;
import net.sf.mpxj.mspdi.schema.Project;
import net.sf.mpxj.mspdi.schema.TimephasedDataType;

import com.projity.association.AssociationList;
import com.projity.configuration.Settings;
import com.projity.contrib.util.Log;
import com.projity.contrib.util.LogFactory;
import com.projity.exchange.ImportedCalendarService;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.calendar.CalendarService;
import com.projity.pm.resource.EnterpriseResource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.snapshot.Snapshottable;
import com.projity.pm.task.NormalTask;
import com.projity.pm.task.TaskSnapshot;
import com.projity.server.data.MPXConverter;
import com.projity.util.DateTime;

/**
 * This class is used to represent a Microsoft Project Data Interchange (MSPDI)
 * XML file. This implementation allows the file to be read, and the data it
 * contains exported as a set of MPX objects. These objects can be interrogated
 * to retrieve any required data, or stored as an MPX file.
 */
public class ModifiedMSPDIWriter extends MSPDIWriter {
	//static Log logg=LogFactory.getLog(ModifiedMSPDIFile.class);
	/**
	 * This constructor allows a new MSPDI file to be created from scratch.
	 */
	public ModifiedMSPDIWriter() {
		super();
	}

	protected Boolean formatOutput()  {
		return Boolean.FALSE;
	}

	public void setProjectFile(ProjectFile pf) {
		m_projectFile = pf;
	}

	private Resource unassignedResource() {
		// add the unassigned resource
		if (UNASSIGNED == null) {
			UNASSIGNED = m_projectFile.addResource();
			UNASSIGNED.setUniqueID(EnterpriseResource.UNASSIGNED_ID);
		}
		return UNASSIGNED;

	}
	protected void writeProjectCalendar(Project project) {
		// projity addition
		int id = 1; // if not found below, use the default standard calendar
		ProjectCalendar cal = ImportedCalendarService.getInstance().findExportedCalendar(
				CalendarService.findBaseCalendar(m_projectFile.getProjectHeader().getCalendarName()));
		if (cal != null) {
			id = cal.getUniqueID();
		} else {
			log.warn("EXPORT: Could not export project calendar: Project: " + m_projectFile.getProjectHeader().getName() + " calendar "
					+ m_projectFile.getProjectHeader().getCalendarName());
		}
		project.setCalendarUID(BigInteger.valueOf(id));

	}


	/**
	 * This method writes resource data to an MSPDI file.
	 *
	 * @param factory
	 *            ObjectFactory instance
	 * @param project
	 *            Root node of the MSPDI file
	 * @throws JAXBException
	 *             on xml creation errors
	 */
	@Override
	protected void writeResources(Project project){ //claur signature update
		Project.Resources resources = m_factory.createProjectResources(); //this ok?
		project.setResources(resources);
		List<Project.Resources.Resource> list = resources.getResource();

		Iterator iter = m_projectFile.getAllResources().iterator();
		Resource resource;
		while (iter.hasNext()) {
			resource = (Resource) iter.next();
			if (resource.getUniqueID() == EnterpriseResource.UNASSIGNED_ID) // don't
				continue;
			//DEF167699	 projity: Export  -> mspdi cannot have ',' or msp complains.
//			if (Environment.isNoPodServer()) { //claur
//				String name = resource.getName();
//				if (name.contains(",")) {
//					//Fannon, Tommy -> Tommy Fannon
//					String fname = name.substring(name.indexOf(',')+2);
//					//fname = fname.replace(" ", "");
//					String lname = name.substring(0, name.indexOf(','));
//					name = fname + " " + lname;
//					//name = name.replace(",", " ");
//					resource.setName(name);
//				}
//			}

			list.add(writeResource(/*factory,*/ resource));
		}
	}



	/**
	 * This method writes data for a single task to an MSPDI file.
	 *
	 * @param factory
	 *            ObjectFactory instance
	 * @param mpx
	 *            Task data
	 * @return new task instance
	 * @throws JAXBException
	 *             on xml creation errors
	 */
	@Override
	protected Project.Tasks.Task writeTask(Task mpx) { //signature updated

		/* DEF167859:  Projity: MS Project Export not working
		   mpxj doesn't handle NaN.  this would be better fixed in mpxj code itself in the DatatypeConverter, but
		   it would be a pain to setup the development environment to build this... so we will hack this for now
		   --TAF2009-07-29
	    */
		if (Double.isNaN(mpx.getFixedCost().doubleValue()))
			mpx.setFixedCost(null);

		Project.Tasks.Task xml = super.writeTask(mpx);
		if (!mpx.getNull())
			writeTaskBaselinesAndTimephased(xml, mpx);
		return xml;
	}

	/**
	 * This method writes data for a single assignment to an MSPDI file.
	 *
	 * @param factory
	 *            ObjectFactory instance
	 * @param mpx
	 *            Resource assignment data
	 * @param uid
	 *            Unique ID for the new assignment
	 * @return New MSPDI assignment instance
	 * @throws JAXBException
	 *             on xml creation errors
	 */
	@Override
	protected Project.Assignments.Assignment writeAssignment(ResourceAssignment mpx) {
		Project.Assignments.Assignment xml = super.writeAssignment(mpx);

		//Microsoft Project does something strange: The unassigned resource has a 0 id for the resource, but assignments use the (short)-1 value.
		if (mpx.getResourceUniqueID() == 0)
			xml.setResourceUID(BigInteger.valueOf(EnterpriseResource.UNASSIGNED_ID));

		Assignment projityAssignment = (Assignment) projityAssignmentMap.get(mpx);
		Calendar stop = DateTime.calendarInstance();
		stop.setTimeInMillis(projityAssignment.getStop());
		xml.setStop(stop);
		Calendar resume = DateTime.calendarInstance();
		resume.setTimeInMillis(projityAssignment.getResume());
		xml.setResume(resume);
		writeAssigmentBaselinesAndTimephased(xml, mpx);

		return (xml);
	}

	// Projity specific stuff below

	static Log log = LogFactory.getLog(ModifiedMSPDIWriter.class);
	protected com.projity.pm.task.Project projityProject;
	protected Map projityTaskMap = new HashMap();
	protected Map projityAssignmentMap = new HashMap();
	protected Map projitySnapshotIdMap = new HashMap();
	protected Map timephasedMap = new HashMap();
	private static Resource UNASSIGNED = null;


	public void setProjityProject(com.projity.pm.task.Project projityProject) {
		this.projityProject = projityProject;
	}

	public void putProjityTaskMap(Object mpx, Object projity) {
		projityTaskMap.put(mpx, projity);
	}

	public void putProjityAssignmentMap(Object mpx, Object projity) {
		projityAssignmentMap.put(mpx, projity);
	}

	public void putProjitySnapshotIdMap(Object mpx, Object projity) {
		projitySnapshotIdMap.put(mpx, projity);
	}

	public void putTimephasedList(Object mpx, Object timephasedList) {
		if (mpx == null || timephasedList == null)
			return;
		timephasedMap.put(mpx, timephasedList);
	}

	public List getTimephasedList(Object mpx) {
		return (List) timephasedMap.get(mpx);
	}

	/**
	 * overloads default behavior to return the "unassigned" resource
	 */
	public Resource getResourceByUniqueID(int id) {
		Resource r;
		if (id == EnterpriseResource.UNASSIGNED_ID)
			r = unassignedResource();
		else
			r = m_projectFile.getResourceByUniqueID(id);
		return r;

	}
	private void writeTaskBaselinesAndTimephased(final Project.Tasks.Task xml, Task mpx){
		// baselines
		final List baselineList = xml.getBaseline();

		NormalTask projityTask = (NormalTask) projityTaskMap.get(mpx);
		if (projityTask == null)
			return;
		for (int s = 0; s < Settings.numBaselines(); s++) {
			if (s == Snapshottable.CURRENT.intValue())
				continue;
			TaskSnapshot snapshot = (TaskSnapshot) projityTask.getSnapshot(new Integer(s));
			if (snapshot == null)
				continue;
			AssociationList snapshotAssignments = snapshot.getHasAssignments().getAssignments();
			if (snapshotAssignments.size() > 0) {
				for (Iterator j = snapshotAssignments.iterator(); j.hasNext();) {
					Assignment assignment = (Assignment) j.next();
					ResourceImpl r = (ResourceImpl) assignment.getResource();
					if (r.isDefault())
						continue;

					Project.Assignments.Assignment.Baseline baseline = m_factory
							.createProjectAssignmentsAssignmentBaseline();
					// For some silly reason, the baseline fields are all
					// strings so they need to be converted

					// baseline duration is missing :(
					baseline.setNumber(s + "");
					baseline.setStart(MPXConverter.dateToXMLString(DateTime.fromGmt(projityTask.getBaselineStart(s))));
					baseline.setFinish(MPXConverter.dateToXMLString(DateTime.fromGmt(projityTask.getBaselineFinish(s))));
					baseline.setWork(DatatypeConverter.printDuration(this, MPXConverter.toMPXDuration((long) projityTask.getBaselineWork(s))));
					baselineList.add(baseline);
				}
			}

		}
// There is no need to write out task timephased info since it is all in assignments
//		final List timephasedList = xml.getTimephasedData();
//		TimephasedService.getInstance().consumeTimephased(projityTask, new TimephasedConsumer() {
//			public void consumeTimephased(Object timephased) {
//				((TimephasedDataType) timephased).setUID(xml.getUID());
//				timephasedList.add(timephased);
//			}
//		}, factory);
	}
	private void writeAssigmentBaselinesAndTimephased(final Project.Assignments.Assignment xml,
			ResourceAssignment mpx){ //claur signature changed
		int snapshotId = ((Integer) projitySnapshotIdMap.get(mpx)).intValue();
		final Assignment projityAssignment = (Assignment) projityAssignmentMap.get(mpx);
		// baselines
		final List timephasedList = xml.getTimephasedData();
		TimephasedService.getInstance().consumeTimephased(projityAssignment, new TimephasedConsumer() {
			public void consumeTimephased(Object timephased) {
				TimephasedDataType t=(TimephasedDataType) timephased;
				//if ("PT0H0M0S".equals(t.getValue())) return;
				((TimephasedDataType) timephased).setUID(xml.getUID());
				timephasedList.add(timephased);
			}
			public boolean acceptValue(double value) { //TODO hack, consumeTimephased shouldn't give PT0H0M0S
				return value!=0.0;
			}
		}, m_factory);
	}
	public ProjectFile getProjectFile() {
		return m_projectFile;
	}

}
