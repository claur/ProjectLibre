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
package com.projity.pm.graphic.spreadsheet;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.projity.configuration.Dictionary;
import com.projity.field.FieldContext;
import com.projity.graphic.configuration.SpreadSheetCategories;
import com.projity.graphic.configuration.SpreadSheetFieldArray;
import com.projity.pm.graphic.gantt.GanttParamsImpl;
import com.projity.pm.graphic.graph.GraphParams;
import com.projity.pm.graphic.spreadsheet.common.SpreadSheetRowHeaderColumnModel;
import com.projity.strings.Messages;

public class SpreadSheetParamsImpl extends GanttParamsImpl implements SpreadSheetParams {
	protected String spreadsheetCategory;
	protected SpreadSheetFieldArray fieldArray;
	protected List<Integer> colWidth;
	protected FieldContext fieldContext;
	protected SpreadSheetColumnModel columnModel;
	protected SpreadSheetRowHeaderColumnModel headerColumnModel;
	protected int spreadSheetWidth=-1,idColMargin=2,colMargin=2;
	//protected boolean printGantt;
//	public SpreadSheetParamsImpl(){
//		super();
//		headerColumnModel=new SpreadSheetRowHeaderColumnModel();
//		spreadsheetCategory=SpreadSheetCategories.taskSpreadsheetCategory;
//		setFieldArray((SpreadSheetFieldArray) Dictionary.get(spreadsheetCategory,Messages.getString("Spreadsheet.Task.entry")));
//	}
	public SpreadSheetParamsImpl(SpreadSheetFieldArray fieldArray,List<Integer> colWidth,boolean printGantt){
		super();
		headerColumnModel=new SpreadSheetRowHeaderColumnModel();
		spreadsheetCategory=SpreadSheetCategories.taskSpreadsheetCategory;
		setFieldArray(fieldArray==null?((SpreadSheetFieldArray) Dictionary.get(spreadsheetCategory,Messages.getString("Spreadsheet.Task.entry"))):fieldArray,fieldArray==null?null:(colWidth==null?fieldArray.getWidths():colWidth));
		setRightPartVisible(printGantt);

	}
	public SpreadSheetFieldArray getFieldArray() {
		return fieldArray;
	}
	public void setFieldArray(SpreadSheetFieldArray fieldArray,List<Integer> colWidth) {
		this.fieldArray = fieldArray;
		this.colWidth=colWidth;
		columnModel=new SpreadSheetColumnModel(fieldArray,colWidth);
		columnModel.setSvg(true);
		initColumns(columnModel, fieldArray.size());
		initColumns(headerColumnModel, 1);


		fieldContext = new FieldContext();
		fieldContext.setLeftAssociation(true);

		updateWidth();
	}

	public String getSpreadsheetCategory() {
		return spreadsheetCategory;
	}
	public FieldContext getFieldContext() {
		return fieldContext;
	}

	public void initColumns(TableColumnModel cm,int columnCount) {
		while (cm.getColumnCount() > 0) {
			cm.removeColumn(cm.getColumn(0));
		}

//		int index=0;
//		for (Iterator i=fieldArray.iterator();i.hasNext();index++) {
//			Field field=(Field)i.next();
//			TableColumn c = new TableColumn(index);
//			c.setHeaderValue(field.getName());
//			cm.addColumn(c);
//		}
		for (int col=0;col<columnCount;col++) {
			TableColumn c = new TableColumn(col);
			c.setHeaderValue(""+col);
			cm.addColumn(c);
		}
	}



	public Rectangle getSpreadSheetBounds(){
		//return new Rectangle(0,configuration.getColumnHeaderHeight(),spreadSheetWidth,configuration.getRowHeight()*cache.getSize());
		return new Rectangle(0,configuration.getColumnHeaderHeight(),spreadSheetWidth,getRowHeight()*cache.getSize());
	}
	public Rectangle getDrawingBounds() {
		return new Rectangle(0,configuration.getColumnHeaderHeight(),(isLeftPartVisible()?(getSpreadSheetBounds().width):0)+(isRightPartVisible()?(getGanttBounds().width):0),getSpreadSheetBounds().height+configuration.getColumnHeaderHeight());
	}

	public int getSpreadSheetWidth() {
		//return isLeftPartVisible()?spreadSheetWidth:0;
		return spreadSheetWidth;
	}
	public SpreadSheetColumnModel getColumnModel() {
		return columnModel;
	}
	public SpreadSheetRowHeaderColumnModel getHeaderColumnModel() {
		return headerColumnModel;
	}
	public Iterator getColumnIterator(){
		return new Iterator(){
			protected Enumeration headerE=headerColumnModel.getColumns();
			protected Enumeration e=columnModel.getColumns();
			public boolean hasNext() {
				return headerE.hasMoreElements()||e.hasMoreElements();
			}
			public Object next() {
				if (headerE.hasMoreElements()) return headerE.nextElement();
				else return e.nextElement();
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public void updateDrawingBounds(){
		updateWidth();
	}
	private void updateWidth(){
		spreadSheetWidth=calculateSpreadSheetWidth();
	}
	private int calculateSpreadSheetWidth(){
		int spWidth=getConfiguration().getRowHeaderWidth()+2*idColMargin;
		TableColumn c;
		for (Enumeration e=columnModel.getColumns();e.hasMoreElements();){
			c=(TableColumn)e.nextElement();
			int cwidth=c.getPreferredWidth()+2*colMargin;
			spWidth+=cwidth;
		}
		return spWidth;
	}
	public void setPrintBounds(Rectangle printBounds) {
		super.setPrintBounds(printBounds);
		updatePages();
	}
	private void updatePages(){
		if (printBounds==null) return;
		if (colPageInfo==null) colPageInfo=new ArrayList();
		else  colPageInfo.clear();
		if (rowPageInfo==null) rowPageInfo=new ArrayList();
		else  rowPageInfo.clear();

		int w=getConfiguration().getRowHeaderWidth()+2*idColMargin;
		TableColumn c;
		int start=0;
		int current=1;
		int x=0;
		for (Enumeration e=columnModel.getColumns();e.hasMoreElements();current++){
			c=(TableColumn)e.nextElement();
			if (w+c.getPreferredWidth()+2*colMargin>printBounds.width){
				colPageInfo.add(new PageInfo(start,current-1,x,w));
				start=current;
				x+=w;
				w=0;
			}
			w+=c.getPreferredWidth()+2*colMargin;
		}
		colPageInfo.add(new PageInfo(start,current-1,x,w));

		int rowsPerPage=getRowsPerPage();
		int count=cache.getSize();
		int pageCount=count/rowsPerPage;
		for (current=0;current<pageCount;current++)
			rowPageInfo.add(new PageInfo(current*rowsPerPage,(current+1)*rowsPerPage-1,current*rowsPerPage*getRowHeight(),rowsPerPage*getRowHeight()));
		int lastRows=count%rowsPerPage;
		if (lastRows!=0) rowPageInfo.add(new PageInfo(current*rowsPerPage,count-1,current*rowsPerPage*getRowHeight(),lastRows*getRowHeight()));
	}

	public class PageInfo{
		protected int start,end,x,width;

		public PageInfo(int start, int end,int x,int width) {
			super();
			this.start = start;
			this.end = end;
			this.x=x;
			this.width=width;
		}

		public int getEnd() {
			return end;
		}

		public void setEnd(int end) {
			this.end = end;
		}
		public int getStart() {
			return start;
		}

		public void setStart(int start) {
			this.start = start;
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}
	}

	protected ArrayList colPageInfo,rowPageInfo;
	public PageInfo getRowPageInfo(int row){
		return (row<0||row>=rowPageInfo.size())?null:(PageInfo)rowPageInfo.get(row);
	}
	public PageInfo getColPageInfo(int col){
		return (col<0||col>=colPageInfo.size())?null:(PageInfo)colPageInfo.get(col);
	}

	public int getRowsPerPage(){
//		int rowsPerPage=(printBounds.height-configuration.getColumnHeaderHeight()-configuration.getPrintFooterHeight())/configuration.getRowHeight();
		int rowsPerPage=(printBounds.height-configuration.getColumnHeaderHeight()-configuration.getPrintFooterHeight())/getRowHeight();
		if (rowsPerPage<=0){
			System.out.println("Error bad rowsPerPage");
			rowsPerPage=1;
		}
		return rowsPerPage;
	}

	public Rectangle getSpreadsheetPrintBounds(int row,int col,boolean ifVisibleOnly){
		if (ifVisibleOnly&&!isLeftPartVisible()) return null;
		PageInfo colInfo=getColPageInfo(col);
		PageInfo rowInfo=getRowPageInfo(row);
		if (colInfo==null||rowInfo==null) return null;
		return new Rectangle(colInfo.getX(),rowInfo.getX()+configuration.getColumnHeaderHeight(),colInfo.getWidth(),rowInfo.getWidth());
	}

	public Rectangle getGanttPrintBounds(int row,int col){
		if (!isRightPartVisible()) return null;
		int spLastCol=colPageInfo.size()-1;
		if (col<spLastCol&&isLeftPartVisible()) return null;
		int totalWidth=getGanttBounds().width;
		Rectangle spreadsheetBounds=getSpreadsheetPrintBounds(row,spLastCol,true);
		if ((col==spLastCol||!isLeftPartVisible())&&spreadsheetBounds!=null){
			int x=spreadsheetBounds.width;
			int width=getPrintBounds().width-spreadsheetBounds.width;
			//if (x+width>totalWidth) width=totalWidth-x;
			if (width>totalWidth) width=totalWidth;
			Rectangle ganttBounds=new Rectangle(0,spreadsheetBounds.y-configuration.getColumnHeaderHeight(),width,spreadsheetBounds.height);
			if (ganttBounds.width==0) return null;
			else return ganttBounds;
		}else{
			spreadsheetBounds=getSpreadsheetPrintBounds(row,spLastCol,false);
			int x=-getGanttDeltaX(row, col);
			int width=getPrintBounds().width;
			if (x+width>totalWidth) width=totalWidth-x;
			Rectangle ganttBounds=new Rectangle(x,spreadsheetBounds==null?0:spreadsheetBounds.y-configuration.getColumnHeaderHeight(),width,spreadsheetBounds==null?0:spreadsheetBounds.height);
			return ganttBounds;
		}
	}
	public int getGanttDeltaX(int row,int col){
		int spLastCol=colPageInfo.size()-1;
		if (col<spLastCol&&isLeftPartVisible()) return -1;
		Rectangle spreadsheetBounds=getSpreadsheetPrintBounds(row,spLastCol,true);
		if (spreadsheetBounds==null)
			return -col*getPrintBounds().width;
		else return spreadsheetBounds.width-(col-spLastCol)*getPrintBounds().width;
	}


	public int getPrintCols(){
		int spColCount=colPageInfo.size();
		PageInfo colInfo=getColPageInfo(spColCount-1);
		int width=(isLeftPartVisible()?colInfo.width:0)+(isRightPartVisible()?getGanttBounds().width:0);
		return (isLeftPartVisible()?(spColCount-1):0)+(int)Math.ceil(width/getPrintBounds().getWidth());
	}
	public int getPrintRows(){
		int size=rowPageInfo.size();
		return size==0?1:size;
	}

	public int getColMargin() {
		return colMargin;
	}
	public int getIdColMargin() {
		return idColMargin;
	}

	public GraphParams createSafePrintCopy(){
		SpreadSheetParamsImpl c=(SpreadSheetParamsImpl)super.createSafePrintCopy();
		if (c.colPageInfo!=null) c.colPageInfo=(ArrayList)colPageInfo.clone();
		if (c.rowPageInfo!=null) c.rowPageInfo=(ArrayList)rowPageInfo.clone();
		return c;
	}


}