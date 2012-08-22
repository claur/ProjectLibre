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
package com.projity.pm.graphic.spreadsheet.common.transfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.collections.CollectionUtils;

import com.projity.field.Field;
import com.projity.field.FieldContext;
import com.projity.field.FieldParseException;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeFactory;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelDataFactory;
import com.projity.options.EditOption;
import com.projity.pm.graphic.spreadsheet.SpreadSheet;
import com.projity.pm.graphic.spreadsheet.common.CommonSpreadSheetModel;
import com.projity.pm.resource.ResourcePool;
import com.projity.pm.task.Project;
import com.projity.pm.task.Task;



/**
 *
 */
public class NodeListTransferable implements Transferable {
    private static final int NODE_LIST = 0;
    private static final int STRING = 1;
    private static final int PLAIN_TEXT = 2;
    
    public static final String NODE_LIST_MIME_TYPE=DataFlavor.javaJVMLocalObjectMimeType+";class=java.util.ArrayList";
    
    private DataFlavor[] flavors;
    private DataFlavor nodeListDataFlavor;
    private Set flavorSet;

	protected ArrayList nodeList;
	protected ArrayList fields;
	protected SpreadSheet spreadsheet;
	protected int[] rows,cols;
	protected boolean nodeSelection;
	//protected String sdata;

	public NodeListTransferable(ArrayList nodeList,ArrayList fields,SpreadSheet spreadSheet,int[] rows,int[] cols, boolean nodeSelection) {
		this.nodeSelection=nodeSelection;
		try {
			nodeListDataFlavor=new DataFlavor(NODE_LIST_MIME_TYPE);
		} catch (ClassNotFoundException e) {}
		if (nodeSelection){
				flavors=new DataFlavor[]{
						nodeListDataFlavor,
						DataFlavor.stringFlavor,
						DataFlavor.getTextPlainUnicodeFlavor()}; //TODO isRepresentationClassReader(||InputStream)||isFlavorTextType+flavor.getReaderForText()
			this.nodeList=nodeList;
			this.fields=fields;
		}else{
			flavors=new DataFlavor[]{
					DataFlavor.stringFlavor,
					DataFlavor.getTextPlainUnicodeFlavor()}; //TODO isRepresentationClassReader(||InputStream)||isFlavorTextType+flavor.getReaderForText()
			//sdata=nodeListToString(nodeList,spreadSheet,fields);
		}
		flavorSet=new HashSet();
		//Collections.addAll(flavorSet,flavors); //jdk 1.5
		//for (int i=0;i<flavors.length;i++) flavorSet.add(flavors[i]);
		CollectionUtils.addAll(flavorSet,flavors); //replaced JDK 1.5 code with this call
		this.spreadsheet=spreadSheet;
		this.rows=rows;
		this.cols=cols;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return (DataFlavor[])flavors.clone();
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (int i = 0; i < flavors.length; i++) {
    	    if (flavor.equals(flavors[i])) {
    	        return true;
    	    }
    	}
    	return false;
	}

	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (!flavorSet.contains(flavor)) throw new UnsupportedFlavorException(flavor);
		if (nodeListDataFlavor.equals(flavor)){
			NodeModel model=((CommonSpreadSheetModel)spreadsheet.getModel()).getCache().getModel();
//			ArrayList nl=nodeList;
//			nodeList=new ArrayList(nl.size());
//			nodeList.addAll(model.copy(nl,NodeModel.SILENT));
			return model.copy(nodeList,NodeModel.SILENT);
		}else if (DataFlavor.stringFlavor.equals(flavor))
		    return selectionToString(spreadsheet,rows,cols);
//		    return (sdata==null)?nodeListToString(nodeList,spreadsheet,fields):sdata;
		else if (DataFlavor.getTextPlainUnicodeFlavor().equals(flavor))
		    return new StringReader(selectionToString(spreadsheet,rows,cols));
	    	//return new StringReader((sdata==null)?nodeListToString(nodeList,spreadsheet,fields):sdata);
		else throw new UnsupportedFlavorException(flavor);
	}
	
//	public Object getTransferData(DataFlavor[] flavors) throws UnsupportedFlavorException, IOException {
//		for (int i=0;i<flavors.length;i++){
//			if (isDataFlavorSupported(flavors[i]))
//				return getTransferData(flavors[i]);
//		}
//		throw new UnsupportedFlavorException(flavors[0]);
//}
	
	public static String nodeListToString(List nodeList,SpreadSheet spreadsheet,List fields){
		StringBuffer sb=new StringBuffer();
		for (Iterator i=nodeList.iterator();i.hasNext();){
			nodeToString((Node)i.next(),sb,spreadsheet,fields);
		}
		return sb.toString();
	}
	public static void nodeToString(Node node,StringBuffer sb,SpreadSheet spreadsheet,List fields){
		CommonSpreadSheetModel model=(CommonSpreadSheetModel)spreadsheet.getModel();
		Object value;
		Field field;
		Iterator fieldsIterator=fields.iterator();
		boolean first=true;
		//String s=null;
		while(fieldsIterator.hasNext()){
			field=(Field)fieldsIterator.next();
			value=field.getValue(node,model.getCache().getWalkersModel(),model.getFieldContext());
			if (first) first=false;
			else sb.append('\t');
			sb.append((value==null)?"":value.toString());
			//s=sb.toString();
			//System.out.println("s="+s);
		}
		sb.append('\n');
		for (Iterator i=node.childrenIterator();i.hasNext();)
			nodeToString((Node)i.next(),sb,spreadsheet,fields);
	}
	
	
	
	public static String selectionToString(SpreadSheet spreadsheet,int[] rows, int[] cols){
		StringBuffer sb=new StringBuffer();
		Object value;
		for (int r=0;r<rows.length;r++){
			for (int c=0;c<cols.length;c++){
				value=spreadsheet.getValueAt(rows[r],cols[c]);
				if (value!=null&&!(value instanceof Task))
					if (value instanceof Date)
						sb.append(EditOption.getInstance().getDateFormat().format((Date)value));
					else sb.append(value.toString());
				if (c<cols.length-1) sb.append('\t');
				else sb.append('\n');
			}
		}
		return sb.toString();
	}
	
	
	

	public static ArrayList stringToNodeList(String s,SpreadSheet spreadsheet,List fields,NodeModelDataFactory factory){
		ArrayList list=new ArrayList();
		StringTokenizer st=new StringTokenizer(s,"\n\r");
		Node node;
		while (st.hasMoreTokens()){
			node=stringToNode(st.nextToken(),spreadsheet,fields,factory);
			if (node!=null) list.add(node);
		}
		return list;
	}
	public static Node stringToNode(String s,SpreadSheet spreadsheet,List fields,NodeModelDataFactory factory){
		String category=spreadsheet.getSpreadSheetCategory();
		Node node=null;
		String delim="\t";
		StringTokenizer st=new StringTokenizer(s,delim,true);
		if (st.hasMoreTokens()){
			if (SpreadSheet.TASK_CATEGORY.equals(category)) node=NodeFactory.getInstance().createTask((Project)factory);
			else if (SpreadSheet.RESOURCE_CATEGORY.equals(category)) node=NodeFactory.getInstance().createResource((ResourcePool)factory);
			else return null;
			
			CommonSpreadSheetModel model=(CommonSpreadSheetModel)spreadsheet.getModel();
			String valueS;
			Field field;
			Iterator fieldsIterator=fields.iterator();
			while(st.hasMoreTokens()&&fieldsIterator.hasNext()){
				valueS=st.nextToken();
				if (delim.equals(valueS)) valueS="";
				else if (st.hasMoreTokens()) st.nextToken();
				field=(Field)fieldsIterator.next();
				try {
					field.setValue(node,model.getCache().getWalkersModel(),spreadsheet,valueS,model.getFieldContext());
				} catch (FieldParseException e) {}
			}
		}
		return node;
	}

	public static void pasteString(String s,SpreadSheet spreadsheet){
		int[] rows=spreadsheet.getSelectedRows();
		int[] cols=spreadsheet.getSelectedColumns();
		if (rows.length>0&&cols.length>0)
			pasteString(s,spreadsheet,rows[0],cols[0]);
	}
	public static void pasteString(String s,SpreadSheet spreadsheet,int row0, int col0){
		StringTokenizer st=new StringTokenizer(s,"\n");
		int row=row0;//,maxRow=spreadsheet.getRowCount()-1;
		while(st.hasMoreTokens()/*&&row<=maxRow*/) //maxRow useless, maxRow increased automatically 
			pasteStringLine(st.nextToken(),spreadsheet,row++,col0);
	}
	public static void pasteStringLine(String s,SpreadSheet spreadsheet,int row0, int col0){
		String valueS;
		CommonSpreadSheetModel model=(CommonSpreadSheetModel)spreadsheet.getModel();
		String delim="\t";
		StringTokenizer st=new StringTokenizer(s,delim,true);
		int col=col0,maxCol=spreadsheet.getColumnCount()-1;
		FieldContext fieldContext=model.getFieldContext();
		boolean round=fieldContext.isRound();
		fieldContext.setRound(true);
		while(st.hasMoreTokens()&&col<=maxCol){
			valueS=st.nextToken();
			if (delim.equals(valueS)) valueS="";
			else if (st.hasMoreTokens()) st.nextToken();
			try{
				model.setValueAt(valueS,row0,++col);
			}catch(Exception e){}
		}
		fieldContext.setRound(round);
	}

	
//	public boolean isNodeSelection() {
//		return nodeSelection;
//	}
//	
//	public ArrayList getSelectedFields(){
//		return (nodeSelection)?spreadsheet.getSelectableFields():spreadsheet.getSelectedFields();
//	}

}
