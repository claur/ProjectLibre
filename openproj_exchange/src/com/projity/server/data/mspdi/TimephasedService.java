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
package com.projity.server.data.mspdi;

import java.util.Calendar;

import net.sf.mpxj.mspdi.schema.ObjectFactory;
import net.sf.mpxj.mspdi.schema.TimephasedDataType;

import com.projity.algorithm.Query;
import com.projity.algorithm.RangeIntervalGenerator;
import com.projity.algorithm.SelectFrom;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.HasTimeDistributedData;
import com.projity.pm.assignment.TimeDistributedHelper;
import com.projity.pm.assignment.functor.AssignmentFieldFunctor;
import com.projity.pm.scheduling.Schedule;
import com.projity.util.DateTime;

/**
 *
 */
public class TimephasedService {
	protected static TimephasedService instance=null;
	protected TimephasedService() {
	}

	public static TimephasedService getInstance(){
		if (instance==null) instance=new TimephasedService();
		return instance;
	}

	private void doQuery(Assignment assignment, ObjectFactory factory, TimephasedConsumer consumer,Object fieldType, int type, long id) {
		SelectFrom clause = SelectFrom.getInstance();
		AssignmentFieldFunctor dataFunctor = assignment.getDataSelect(fieldType,clause,false);
		TimephasedGetter getter = TimephasedGetter.getInstance(factory,consumer,dataFunctor,type,id);
		long end = assignment.getEnd();
		long start = assignment.getStart();

		if (fieldType == HasTimeDistributedData.ACTUAL_WORK)
			end = assignment.getStop();
		else if (fieldType == HasTimeDistributedData.REMAINING_WORK)
			start = assignment.getStop();
		RangeIntervalGenerator dailyInRange = RangeIntervalGenerator.getInstance(start, end, Calendar.DATE);

		Query.getInstance().selectFrom(clause)
		.groupBy(dailyInRange)
		.action(getter)
		.execute();
	}
	public void consumeTimephased(Schedule schedule,TimephasedConsumer consumer,Object factory){ //claur removed exception
		ObjectFactory mspdiTimephasedFactory=(ObjectFactory)factory;

		if (!(schedule instanceof Assignment))
			return; // only do assignments
		Assignment assignment = (Assignment)schedule;

		long id = 0;

		if ( assignment.getPercentComplete() > 0) {
			doQuery(assignment,mspdiTimephasedFactory, consumer,HasTimeDistributedData.ACTUAL_WORK, TimeDistributedTypeMapper.ASSIGNMENT_ACTUAL_WORK, id++);
		}
		doQuery(assignment,mspdiTimephasedFactory, consumer,HasTimeDistributedData.REMAINING_WORK, TimeDistributedTypeMapper.ASSIGNMENT_REMAINING_WORK, id++);


		Object fields[] = HasTimeDistributedData.baselineWorkTypes;
		Assignment baselineAssignment;
		for (int i = 0; i < fields.length; i++) {
			baselineAssignment = assignment.getBaselineAssignment(new Integer(i), false);
			if (baselineAssignment == null)
				continue;
			int mapType = TimeDistributedTypeMapper.getTimeDistributedType(i,false,baselineAssignment);
			doQuery(baselineAssignment,mspdiTimephasedFactory, consumer,HasTimeDistributedData.WORK, mapType, id++);
		}
	}

	public void readTimephased (Assignment assignment,TimephasedDataType t) {
		// if reading current info, do not bother unless the contour is nonstandard
//		if (TimeDistributedTypeMapper.isCurrent(t.getType().intValue()))// && assignment.getWorkContourType() != ContourTypes.CONTOURED)
//			return;

		if (!TimeDistributedHelper.isWork(t.getType())) //TODO do not treat costs for now
			return;

		Object type = TimeDistributedTypeMapper.getProjityField(t.getType());

		// do not treat current values for non contoured assignments
//		if (TimeDistributedTypeMapper.isCurrent(t.getType().intValue()) && assignment.getWorkContourType() != ContourTypes.CONTOURED)
//			return;
		long duration = XsdDuration.millis(t.getValue());
		assignment.setInterval(type,DateTime.gmt(t.getStart().getTime()),DateTime.gmt(t.getFinish().getTime()), duration);
	}
}
