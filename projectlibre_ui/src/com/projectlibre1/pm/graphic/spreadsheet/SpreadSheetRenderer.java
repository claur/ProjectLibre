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
package com.projectlibre1.pm.graphic.spreadsheet;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.table.TableColumn;

import com.projectlibre1.pm.graphic.Renderer;
import com.projectlibre1.pm.graphic.graph.GraphParams;
import com.projectlibre1.pm.graphic.model.cache.GraphicNode;
import com.projectlibre1.pm.graphic.spreadsheet.renderer.OfflineRenderer;
import com.projectlibre1.field.Field;

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
