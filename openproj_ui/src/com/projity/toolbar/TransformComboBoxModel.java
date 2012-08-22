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
package com.projity.toolbar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import com.projity.grouping.core.transform.CommonTransformFactory;
import com.projity.grouping.core.transform.TransformList;
import com.projity.grouping.core.transform.ViewConfiguration;
import com.projity.grouping.core.transform.ViewTransformer;
import com.projity.strings.Messages;

/**
 * 
 */
public class TransformComboBoxModel extends AbstractListModel implements ComboBoxModel {
	public static final int FILTER=1;
	public static final int SORTER=2;
	public static final int GROUPER=3;

	protected int type;
	protected String stype;
	protected TransformList transformList;
	protected CommonTransformFactory selected;
	protected ViewConfiguration view;
	protected Map viewMap=new HashMap();
	private String tipText;
	/**
	 * 
	 */
	public TransformComboBoxModel(int type) {
		this.type=type;
		switch (type) {
		case SORTER:
			stype="user_sorters";
			tipText=Messages.getString("Text.Sort");
			break;
		case GROUPER:
			stype="user_groupers";
			tipText=Messages.getString("Text.Group");
			break;
		default:
			stype="user_filters";
			tipText=Messages.getString("Text.Filter");
			break;
		}
		transformList=TransformList.getInstance(stype);
		//selected=(CommonTransformFactory)getElementAt(0);
	}
	
	
	public void setView(ViewConfiguration view){
		if (view==null) return;
		int max=getSize()-1;
		if (max>=0) fireIntervalRemoved(this,0,max);
		this.view=view;
		factories=transformList.getFactories(view,stype);
		selected=(CommonTransformFactory)viewMap.get(view);
		if (selected==null){
			ViewTransformer transformer=view.getTransform();
			switch (type) {
			case SORTER:
				selected=transformList.getFactory(transformer.getUserSorterId());
				break;
			case GROUPER:
				selected=transformList.getFactory(transformer.getUserGrouperId());
				break;
			default:
				selected=transformList.getFactory(transformer.getUserFilterId());
				break;
			}
			viewMap.put(view,selected);
		}
		int max2=getSize()-1;
		if (max2>=0) fireIntervalAdded(this,0,max2);
		
		
	}
	
	public ViewConfiguration getView() {
		return view;
	}
	
	public void changeTransform(CommonTransformFactory factory){
		if (view==null) return;	
		ViewTransformer transformer=view.getTransform();
		switch (type) {
			case SORTER:
				transformer.setUserSorterId(factory.getId());
				break;
			case GROUPER:
				transformer.setUserGrouperId(factory.getId());
				break;
			default:
				transformer.setUserFilterId(factory.getId());
				break;
			}
	}
	
	public Object getSelectedItem() {
		return selected;
	}
	public void setSelectedItem(Object obj) {
		selected=(CommonTransformFactory)obj;
		viewMap.put(view,selected);
	}

    
    
	protected List factories=null;
	public int getSize() {
	    if (factories==null) return 0;
	    return factories.size();
	}
	public Object getElementAt(int index) {
	   return factories.get(index);
	}


	final String getTipText() {
		return tipText;
	}


}
