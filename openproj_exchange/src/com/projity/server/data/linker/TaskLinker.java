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

Attribution Information: Attribution Copyright Notice: Copyright � 2006, 2007
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
package com.projity.server.data.linker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.Predicate;

import com.projity.grouping.core.hierarchy.NodeHierarchy;
import com.projity.grouping.core.model.NodeModel;
import com.projity.pm.task.Project;
import com.projity.pm.task.Task;
import com.projity.server.data.AssignmentData;
import com.projity.server.data.SerializeOptions;
import com.projity.server.data.SerializedDataObject;
import com.projity.server.data.TaskData;
import com.projity.server.data.TypeSystemConverter;
import com.projity.server.data.TypeSystemConverterFactory;

/**
 *
 */
public abstract class TaskLinker extends Linker {
	public void initIterator(){
		iterator=((Project)getParent()).getTaskOutlineIterator();
	}
	public Object executeNext(){
        Task task=(Task)iterator.next();
        //if (globalIdsOnly) CommonDataObject.makeGlobal(task);
     	return task;
	}

	public NodeHierarchy getHierarchy(){return ((Project)getParent()).getTaskOutline().getHierarchy();}

	protected Collection flatAssignments;

	public Collection getFlatAssignments() {
		return flatAssignments;
	}
	public void setFlatAssignments(Collection flatAssignments) {
		this.flatAssignments = flatAssignments;
	}
//	protected ArrayList<Long> unchanged;
//
//	public ArrayList<Long> getUnchanged() {
//		return unchanged;
//	}
//	public void setUnchanged(ArrayList<Long> unchanged) {
//		this.unchanged = unchanged;
//	}





	//extra field union needed for rollup fields
	protected class PreparedAttributes{
		protected SerializedDataObject data;
		protected Object obj;
		protected Collection extrafields; //extra fields
		protected List fieldArray;
		protected NodeModel model;
		public PreparedAttributes(SerializedDataObject data, Object obj, Collection extrafields, List fieldArray, NodeModel model) {
			super();
			this.data = data;
			this.obj = obj;
			this.extrafields = extrafields;
			this.fieldArray = fieldArray;
			this.model = model;
		}
		public SerializedDataObject getData() {
			return data;
		}
		public void setData(SerializedDataObject data) {
			this.data = data;
		}
		public Collection getExtrafields() {
			return extrafields;
		}
		public void setExtrafields(Collection extrafields) {
			this.extrafields = extrafields;
		}
		public List getFieldArray() {
			return fieldArray;
		}
		public void setFieldArray(List fieldArray) {
			this.fieldArray = fieldArray;
		}
		public NodeModel getModel() {
			return model;
		}
		public void setModel(NodeModel model) {
			this.model = model;
		}
		public Object getObj() {
			return obj;
		}
		public void setObj(Object obj) {
			this.obj = obj;
		}
	}
//	protected List<PreparedAttributes> preparedAttributes; //claur
//
//	public void addPreparedAttributes(SerializedDataObject data, Object obj, NodeModel model,SerializeOptions options) {
//		if (preparedAttributes==null) preparedAttributes=new ArrayList<PreparedAttributes>();
//		TypeSystemConverter converter=TypeSystemConverterFactory.getInstance().getConverter();
//		Predicate fieldFilter=options==null?null:options.getFieldFilter();
//   		if (data instanceof TaskData) preparedAttributes.add(new PreparedAttributes(data,obj,converter.getDirtyExtraFields(obj,fieldFilter),converter.getExposedTaskFields(fieldFilter),model));
//		else if (data instanceof AssignmentData) preparedAttributes.add(new PreparedAttributes(data,obj,converter.getDirtyExtraFields(obj,fieldFilter),converter.getExposedAssignmentFields(fieldFilter),model));
//
//	}
	
	public void computeAttributes(){
//        if (Environment.isNoPodServer()){
//           	TypeSystemConverter converter=TypeSystemConverterFactory.getInstance().getConverter();
//           	ArrayList<Field> unionExtraTaskFields=new ArrayList<Field>();
//           	ArrayList<Field> unionExtraAssignmentFields=new ArrayList<Field>();
//           	/*DEF164438: 	Projity: Error exporting task plan to .xml
//           	  this stops the bombout which occurs.  may require revisiting if we find
//           	  that this code path is needed for msp export --TAF090707*/
//           	if (preparedAttributes ==  null) return;
//        	for (PreparedAttributes attrs:preparedAttributes){
//        		if (attrs.getExtrafields()==null) continue;
//        		if (attrs.getData() instanceof TaskData){
//        			unionExtraTaskFields.addAll(attrs.getExtrafields());
//        		}
//        		else if (attrs.getData() instanceof AssignmentData){
//        			unionExtraAssignmentFields.addAll(attrs.getExtrafields());
//        		}
//        	}
//        	for (PreparedAttributes attrs:preparedAttributes){
//        		SerializedDataObject data=attrs.getData();
//        		if (data instanceof TaskData){
//            		Map<String,Object> exposedAttributes=converter.convertFieldsAndCustomAttributes(attrs.getObj(), unionExtraTaskFields, attrs.getFieldArray(), attrs.getModel(),false);
//        			((TaskData)data).setAttributes(exposedAttributes);
//        		}
//        		else if (data instanceof AssignmentData){
//            		Map<String,Object> exposedAttributes=converter.convertFieldsAndCustomAttributes(attrs.getObj(), unionExtraAssignmentFields, attrs.getFieldArray(), attrs.getModel(),false);
//        			((AssignmentData)data).setAttributes(exposedAttributes);
//        		}
//        	}
//        }

	}

	public void addTransformedObjects() throws Exception{
		super.addTransformedObjects();
		computeAttributes();
	}
//	public List<PreparedAttributes> getPreparedAttributes() { //claur
//		return preparedAttributes;
//	}
//	public void setPreparedAttributes(List<PreparedAttributes> preparedAttributes) {
//		this.preparedAttributes = preparedAttributes;
//	}

	protected SerializeOptions options;
	public SerializeOptions getOptions() {
		return options;
	}
	public void setOptions(SerializeOptions options) {
		this.options = options;
	}


}
