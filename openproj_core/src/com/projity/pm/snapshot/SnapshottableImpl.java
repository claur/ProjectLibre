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
package com.projity.pm.snapshot;

import java.io.Serializable;

import org.apache.commons.collections.Closure;

import com.projity.configuration.Settings;
import com.projity.pm.task.Task;
import com.projity.pm.task.TaskSnapshot;
import com.projity.strings.Messages;

/**
 *
 */
public class SnapshottableImpl implements Snapshottable, Serializable {
	DataSnapshot snapshots[] = null;
	
	/**
	 * 
	 */
	public SnapshottableImpl(int number) {
		snapshots = new DataSnapshot[number];
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.snapshot.Snapshottable#getSnapshot(int)
	 */
	public DataSnapshot getSnapshot(Object snapshotId) {
		return snapshots[((Integer)snapshotId).intValue()];
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.snapshot.Snapshottable#setSnapshot(int, com.projity.pm.snapshot.DataSnapshot)
	 */
	public void setSnapshot(Object snapshotId, DataSnapshot snapshot) {
		snapshots[((Integer)snapshotId).intValue()] = cloneSnapshot(snapshot);
	}
	
	// functor this guy
	public void saveCurrentToSnapshot(Object snapshotId) {
		setSnapshot(snapshotId,snapshots[CURRENT.intValue()]);
	}	
	
	public DataSnapshot getCurrentSnapshot() {
		return snapshots[CURRENT.intValue()];
	}

	public void setCurrentSnapshot(DataSnapshot snapshot) {
		snapshots[CURRENT.intValue()] = snapshot;
	}	
	
	public void clearSnapshot(Object snapshotId) {
		snapshots[((Integer)snapshotId).intValue()] = null;
	}
	
	public static class SaveCurrentToSnapshotClosure implements Closure {
		Object snapshotId;
		public SaveCurrentToSnapshotClosure(Object snapshotId) {
			this.snapshotId = snapshotId;
		}
		/* (non-Javadoc)
		 * @see org.apache.commons.collections.Closure#execute(java.lang.Object)
		 */
		public void execute(Object arg0) {
			if (arg0 instanceof Snapshottable)
				((Snapshottable) arg0).saveCurrentToSnapshot(snapshotId);
		}
	}

	public static class ClearSnapshotClosure implements Closure {
		Object snapshotId;
		public ClearSnapshotClosure(Object snapshotId) {
			this.snapshotId = snapshotId;
		}
		/* (non-Javadoc)
		 * @see org.apache.commons.collections.Closure#execute(java.lang.Object)
		 */
		public void execute(Object arg0) {
			if (arg0 instanceof Snapshottable)			
				((Snapshottable) arg0).clearSnapshot(snapshotId);
		}
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.snapshot.Snapshottable#cloneSnapshot()
	 */
	public DataSnapshot cloneSnapshot(DataSnapshot snapshot) {
		return snapshot; //not a clone at all.  This should be overridden
	}

	public static String snapshotName(int baselineNumber) {
		String text = Messages.getString("Text.Baseline");
		if (baselineNumber > 0)
			text += " " + baselineNumber;
		return text;
	}
	
	private static String[] snapshotNames = null;
	public static String[] getSnapshotNames() {
		if (snapshotNames == null) {
			snapshotNames = new String[Settings.NUM_ARRAY_BASELINES];
			for (int i=0; i < Settings.NUM_ARRAY_BASELINES; i++)
				snapshotNames[i] = snapshotName(i);
		}
		return snapshotNames;
	}
	
	
	public Object clone(){ //Handle wbs outside
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
	public Object cloneWithTask(Task task){ //Handle wbs outside
			SnapshottableImpl s=(SnapshottableImpl)clone();
			s.snapshots=new DataSnapshot[snapshots.length];
			for (int i=0;i<snapshots.length;i++){
				s.snapshots[i]=(snapshots[i]==null)?null:(DataSnapshot)((TaskSnapshot)snapshots[i]).deepCloneWithTask(task);
			}
			return s;
	}

}
