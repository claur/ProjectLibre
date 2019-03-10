/*******************************************************************************
 * The contents of this file are subject to the Common Public Attribution License 
 * Version 1.0 (the "License"); you may not use this file except in compliance with 
 * the License. You may obtain a copy of the License at 
 * http://www.projectlibre.com/license . The License is based on the Mozilla Public 
 * License Version 1.1 but Sections 14 and 15 have been added to cover use of 
 * software over a computer network and provide for limited attribution for the 
 * Original Developer. In addition, Exhibit A has been modified to be consistent 
 * with Exhibit B. 
 *
 * Software distributed under the License is distributed on an "AS IS" basis, 
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the 
 * specific language governing rights and limitations under the License. The 
 * Original Code is ProjectLibre. The Original Developer is the Initial Developer 
 * and is ProjectLibre Inc. All portions of the code written by ProjectLibre are 
 * Copyright (c) 2012-2019. All Rights Reserved. All portions of the code written by 
 * ProjectLibre are Copyright (c) 2012-2019. All Rights Reserved. Contributor 
 * ProjectLibre, Inc.
 *
 * Alternatively, the contents of this file may be used under the terms of the 
 * ProjectLibre End-User License Agreement (the ProjectLibre License) in which case 
 * the provisions of the ProjectLibre License are applicable instead of those above. 
 * If you wish to allow use of your version of this file only under the terms of the 
 * ProjectLibre License and not to allow others to use your version of this file 
 * under the CPAL, indicate your decision by deleting the provisions above and 
 * replace them with the notice and other provisions required by the ProjectLibre 
 * License. If you do not delete the provisions above, a recipient may use your 
 * version of this file under either the CPAL or the ProjectLibre Licenses. 
 *
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text of the notices 
 * in the Source Code files of the Original Code. You should use the text of this 
 * Exhibit A rather than the text found in the Original Code Source Code for Your 
 * Modifications.] 
 *
 * EXHIBIT B. Attribution Information for ProjectLibre required
 *
 * Attribution Copyright Notice: Copyright (c) 2012-2019, ProjectLibre, Inc.
 * Attribution Phrase (not exceeding 10 words): 
 * ProjectLibre, open source project management software.
 * Attribution URL: http://www.projectlibre.com
 * Graphic Image as provided in the Covered Code as file: projectlibre-logo.png with 
 * alternatives listed on http://www.projectlibre.com/logo 
 *
 * Display of Attribution Information is required in Larger Works which are defined 
 * in the CPAL as a work which combines Covered Code or portions thereof with code 
 * not governed by the terms of the CPAL. However, in addition to the other notice 
 * obligations, all copies of the Covered Code in Executable and Source Code form 
 * distributed must, as a form of attribution of the original author, include on 
 * each user interface screen the "ProjectLibre" logo visible to all users. 
 * The ProjectLibre logo should be located horizontally aligned with the menu bar 
 * and left justified on the top left of the screen adjacent to the File menu. The 
 * logo must be at least 144 x 31 pixels. When users click on the "ProjectLibre" 
 * logo it must direct them back to http://www.projectlibre.com. 
 *******************************************************************************/
package com.projectlibre1.timescale;

import java.text.DateFormat;
import java.util.Date;

import com.projectlibre1.pm.time.HasStartAndEnd;

/**
 *
 */
public class TimeInterval implements HasStartAndEnd {
	protected long start1;
	protected long end1;
	protected String text1;
	protected long start2;
	protected long end2;
	protected String text2;
	
	public String toString(){
		StringBuffer buf=new StringBuffer();
		DateFormat df = DateFormat.getDateTimeInstance();
		
		buf.append("TimeInterval[")
			.append(df.format(new Date(start1)))
			.append(',')
			.append(df.format(new Date(end1)))
			.append(',')
			.append(text1)
			.append(df.format(new Date(start2)))
			.append(',')
			.append(df.format(new Date(end2)))
			.append(',')
			.append(text2)
			.append(']');
		return buf.toString();
	}
	
	/**
	 * @param start1
	 * @param end1
	 * @param text1
	 * @param start2
	 * @param end2
	 * @param text2
	 */
	public TimeInterval(long start1, long end1, String text1, long start2,
			long end2, String text2) {
		super();
		this.start1 = start1;
		this.end1 = end1;
		this.text1 = text1;
		this.start2 = start2;
		this.end2 = end2;
		this.text2 = text2;
	}
	
	/**
	 * @return Returns the end1.
	 */
	public long getEnd1() {
		return end1;
	}
	/**
	 * @return Returns the end2.
	 */
	public long getEnd2() {
		return end2;
	}
	/**
	 * @return Returns the start1.
	 */
	public long getStart1() {
		return start1;
	}
	/**
	 * @return Returns the start2.
	 */
	public long getStart2() {
		return start2;
	}
	/**
	 * @return Returns the text1.
	 */
	public String getText1() {
		return text1;
	}
	/**
	 * @return Returns the text2.
	 */
	public String getText2() {
		return text2;
	}

	/* (non-Javadoc)
	 * @see com.projectlibre1.pm.time.HasStartAndEnd#getStart()
	 */
	public long getStart() {
		return start1;
	}

	/* (non-Javadoc)
	 * @see com.projectlibre1.pm.time.HasStartAndEnd#getEnd()
	 */
	public long getEnd() {
		return end1;
	}
}
