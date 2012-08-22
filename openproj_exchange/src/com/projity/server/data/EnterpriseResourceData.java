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
package com.projity.server.data;

import com.projity.algorithm.buffer.GroupedCalculatedValues;


/**
 *
 */
public class EnterpriseResourceData extends SerializedDataObject {
	static final long serialVersionUID = 555524422442L;
    protected CalendarData calendar;
    protected long calendarId=-1;
    protected EnterpriseResourceData parentResource;
    protected long childPosition;
    protected long parentResourceId=-1;
//    protected float version=1.0f;
//    protected boolean defaultResource;
//    protected boolean unassigned=false;
    //protected Collection assignments;

    public static final SerializedDataObjectFactory FACTORY=new SerializedDataObjectFactory(){
        public SerializedDataObject createSerializedDataObject(){
            return new EnterpriseResourceData();
        }
    };

    /*public Collection getAssignments() {
        return assignments;
    }
    public void setAssignments(Collection assignments) {
        this.assignments = assignments;
    }*/
    public long getChildPosition() {
        return childPosition;
    }
    public void setChildPosition(long childPosition) {
        this.childPosition = childPosition;
    }
    public EnterpriseResourceData getParentResource() {
        return parentResource;
    }
    public void setParentResource(EnterpriseResourceData parentResource) {
        this.parentResource = parentResource;
        setParentResourceId((parentResource==null)?-1L:parentResource.getUniqueId());
    }
    public CalendarData getCalendar() {
        return calendar;
    }
    public void setCalendar(CalendarData calendar) {
        this.calendar = calendar;
        setCalendarId((calendar==null)?-1L:calendar.getUniqueId());
    }

    public int getType(){
        return DataObjectConstants.ENTERPRISE_RESOURCE_TYPE;
    }


    protected long externalId;
    protected String emailAddress;
	protected String userAccount;


	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public long getExternalId() {
		return externalId;
	}
	public void setExternalId(long externalId) {
		this.externalId = externalId;
	}
	public String getUserAccount() {
		return userAccount;
	}
	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}



//	public boolean isDefault() {
//		return getUniqueId()==EnterpriseResource.UNASSIGNED_ID;
//	}
//	public void setDefault(boolean unassigned) {
//		setUniqueId(EnterpriseResource.UNASSIGNED_ID);
//	}
//	public boolean isDefault() {
//		return defaultResource;
//	}
//	public void setDefault(boolean defaultResource) {
//		this.defaultResource = defaultResource;
//	}


	public long getParentResourceId() {
		return parentResourceId;
	}
	public void setParentResourceId(long parentResourceId) {
		this.parentResourceId = parentResourceId;
	}



    public long getCalendarId() {
		return calendarId;
	}
	public void setCalendarId(long calendarId) {
		this.calendarId = calendarId;
	}

	public void emtpy(){
    	super.emtpy();
    	emailAddress=null;
    	userAccount=null;
    	calendar=null;
    	parentResource=null;
    }


	protected GroupedCalculatedValues globalWorkVector;
	public GroupedCalculatedValues getGlobalWorkVector() {
		return globalWorkVector;
	}
	public void setGlobalWorkVector(GroupedCalculatedValues globalWorkVector) {
		this.globalWorkVector = globalWorkVector;
	}

    protected int[] authorizedRoles;
	public int[] getAuthorizedRoles() {
		return authorizedRoles;
	}
	public void setAuthorizedRoles(int[] authorizedRoles) {
		this.authorizedRoles = authorizedRoles;
	}

	protected int license;

	public int getLicense() {
		return license;
	}
	public void setLicense(int license) {
		this.license = license;
	}

	protected int licenseOptions;

	public int getLicenseOptions() {
		return licenseOptions;
	}
	public void setLicenseOptions(int licenseOptions) {
		this.licenseOptions = licenseOptions;
	}


}
