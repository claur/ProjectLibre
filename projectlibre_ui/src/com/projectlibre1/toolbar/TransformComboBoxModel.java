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
package com.projectlibre1.toolbar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import com.projectlibre1.grouping.core.transform.CommonTransformFactory;
import com.projectlibre1.grouping.core.transform.TransformList;
import com.projectlibre1.grouping.core.transform.ViewConfiguration;
import com.projectlibre1.grouping.core.transform.ViewTransformer;
import com.projectlibre1.strings.Messages;

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
