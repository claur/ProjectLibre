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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.table.TableColumn;

import com.projity.field.Field;
import com.projity.pm.graphic.Renderer;
import com.projity.pm.graphic.graph.GraphParams;
import com.projity.pm.graphic.model.cache.GraphicNode;
import com.projity.pm.graphic.spreadsheet.renderer.OfflineRenderer;

public class SpreadSheetRenderer extends Renderer{
//	protected Stroke cellStroke=new BasicStroke(0.25f);
//	protected Stroke spreadSheetStroke=new BasicStroke(0.5f);
//	protected Color cellColor=Color.GRAY;
//	protected Color spreadSheetColor=Color.BLACK;

	protected SpreadSheetParams params;
	public SpreadSheetRenderer(GraphParams graphInfo){
		super(graphInfo);
		params=(SpreadSheetParams)graphInfo;
	}
	public SpreadSheetRenderer(){
		super();
	}
	public void paint(Graphics g){
		paint(g,-1,-1);
	}
	public void paint(Graphics g,int prow, int pcol) {
		Graphics2D g2=(Graphics2D)g;
		int h=params.getSpreadSheetBounds().y;
		int rowH=params.getRowHeight();
		int row0=0;
		int row1=Integer.MAX_VALUE;
		int col0=0;
		int col1=Integer.MAX_VALUE;
		Rectangle spreadsheetBounds=params.getSpreadSheetBounds();
		if (prow!=-1){
			SpreadSheetParamsImpl.PageInfo colInfo=params.getColPageInfo(pcol);
			SpreadSheetParamsImpl.PageInfo rowInfo=params.getRowPageInfo(prow);
			col0=colInfo.getStart();
			col1=colInfo.getEnd();
			row0=rowInfo.getStart();
			row1=rowInfo.getEnd();
			Rectangle printSpreadsheetBounds=((SpreadSheetParamsImpl)params).getSpreadsheetPrintBounds(prow, pcol,false);
			spreadsheetBounds=new Rectangle(spreadsheetBounds.x,spreadsheetBounds.y,printSpreadsheetBounds.width,printSpreadsheetBounds.height);
		}
//		System.out.println("spreadsheetBounds="+spreadsheetBounds);
		int row=row0;
		for (Iterator i=graphInfo.getCache().getIterator(row0);i.hasNext()&&row<=row1;row++){
			GraphicNode gnode=(GraphicNode)i.next();
			paintRow(g2, row, row0, h,col0,col1,gnode,spreadsheetBounds);
			h+=rowH;
		}
		paintColumnHeader(g2,col0,col1,spreadsheetBounds);
	}

	private int idColMargin=2,colMargin=2,idColumnIndex=0;
	protected int getColMargin(int colIndex){
		return (idColumnIndex==colIndex)?idColMargin:colMargin;
	}

	protected void paintColumnHeader(Graphics2D g2,int col0,int col1,Rectangle spreadsheetBounds){
		TableColumn c;
		int w=spreadsheetBounds.x;
		int h=spreadsheetBounds.y-params.getConfiguration().getColumnHeaderHeight();
		int col=0;
		for (Iterator i=params.getColumnIterator();i.hasNext()&&col<=col1;col++){
			c=(TableColumn)i.next();
			if (col<col0) continue;

	    	int cwidth=c.getPreferredWidth()+2*getColMargin(col);

			OfflineRenderer renderer=(OfflineRenderer)c.getHeaderRenderer();
			if (renderer!=null){ //rowHeader is null
				JComponent component=(JComponent)renderer.getComponent(((Field)params.getFieldArray().get(col)).getName(), null, (Field)params.getFieldArray().get(col), params);
		    	boolean opaque=component.isOpaque();
		    	//component.setDoubleBuffered(false);
		    	component.setOpaque(false);
		    	//component.setForeground(Color.BLACK);
				component.setSize(cwidth, params.getConfiguration().getColumnHeaderHeight());
		    	g2.translate(w,h);
		    	component.doLayout();
		    	//g2.setClip(0, 0, cwidth, params.getConfiguration().getColumnHeaderHeight());
		    	component.print(g2);
		    	//g2.setClip(null);
		    	g2.translate(-w,-h);
				component.setOpaque(opaque);
			}
			w+=cwidth;
//			g2.setStroke(spreadSheetStroke);
//			g2.setColor(spreadSheetColor);
			g2.draw(new Line2D.Double(w,h,w,spreadsheetBounds.getMaxY()));
			//g2.drawLine(w,h,w,spreadsheetBounds.y+spreadsheetBounds.height);
		}
	}

	protected void paintRow(Graphics2D g2, int row, int row0, int h,int col0,int col1,GraphicNode node,Rectangle spreadsheetBounds){
		TableColumn c;
		int w=spreadsheetBounds.x;
		int col=0;
		for (Iterator i=params.getColumnIterator();i.hasNext()&&col<=col1;col++){
			c=(TableColumn)i.next();
			if (col<col0) continue;
			//cell content
			//GraphicNode node = SpreadSheetUtils.getNodeFromCacheRow(row,1/*rowMultiple*/,params.getCache());
			Object value=SpreadSheetUtils.getValueAt(node.getNode(), col, params.getCache(), params.getColumnModel(), params.getFieldContext());

	    	Field field=(Field)params.getFieldArray().get(col);

			int compWidth=c.getPreferredWidth();
	    	int cwidth=compWidth+2*getColMargin(col);

			OfflineRenderer renderer=(OfflineRenderer)c.getCellRenderer();
			JComponent component=(JComponent)renderer.getComponent(value, node, field, params);
	    	//component.setDoubleBuffered(false);
	    	boolean opaque=component.isOpaque();
			component.setOpaque(false);
	    	//component.setForeground(Color.BLACK);
			component.setSize(compWidth, params.getRowHeight());
	    	g2.translate(w+getColMargin(col),h);
	    	//g2.setClip(0, 0, compWidth, params.getRowHeight());
	    	component.doLayout();
	    	component.print(g2);
	    	//g2.setClip(null);
	    	g2.translate(-w-getColMargin(col),-h);
			component.setOpaque(opaque);
			w+=cwidth;
		}
		if (row!=row0) g2.draw(new Line2D.Double(0,h,w,h));
	}




}
