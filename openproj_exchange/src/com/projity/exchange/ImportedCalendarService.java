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
package com.projity.exchange;

import java.util.HashMap;
import java.util.Iterator;

import net.sf.mpxj.ProjectCalendar;

import com.projity.pm.calendar.CalendarService;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.calendar.WorkingCalendar;

/**
 * Facade for manipulating calendars via a dialog or web interface
 */
public class ImportedCalendarService {
	private static ImportedCalendarService instance = null;

	public static ImportedCalendarService getInstance() {
		if (instance == null)
			instance = new ImportedCalendarService();
		return instance;
	}
	HashMap importedCalendarMap = new HashMap();
	public void addImportedCalendar(WorkingCalendar cal, ProjectCalendar mpxCal) {
		importedCalendarMap.put(mpxCal,cal);
		CalendarService calendarService=CalendarService.getInstance();
		if (cal.isBaseCalendar()) {
			if (calendarService.findBaseCalendar(cal.getName(),true) != null)
				return;
// cal.setName(cal.getName() + PLACE_HOLDER_NAME);
		}
		calendarService.add(cal);
	}

	public WorkCalendar findImportedCalendar(ProjectCalendar mpxCal) {
		return (WorkCalendar) importedCalendarMap.get(mpxCal);
	}
	public ProjectCalendar findImportedMPXCalendar(String name) {
		Iterator i = importedCalendarMap.keySet().iterator();
		ProjectCalendar cal;
		if (name == null)
			return null;
		while (i.hasNext()) {
			cal = (ProjectCalendar)i.next();
			if (name.equals(cal.getName()))
				return cal;
		}
		return null;
	}

	HashMap exportedCalendarMap = new HashMap();
	public void addExportedCalendar(ProjectCalendar mpxCal,WorkingCalendar cal) {
		exportedCalendarMap.put(cal,mpxCal);
	}

	public ProjectCalendar findExportedCalendar(WorkCalendar cal) {
		return (ProjectCalendar) exportedCalendarMap.get(cal);
	}

	public static void cleanUp(){
		instance=null;
	}

}
