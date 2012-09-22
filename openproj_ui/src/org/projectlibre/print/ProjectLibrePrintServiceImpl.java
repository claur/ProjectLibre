/*
The contents of this file are subject to the Common Public Attribution License
Version 1.0 (the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at
http://www.projectlibre.com/license . The License is based on the Mozilla Public
License Version 1.1 but Sections 14 and 15 have been added to cover use of
software over a computer network and provide for limited attribution for the
Original Developer. In addition, Exhibit A has been modified to be consistent
with Exhibit B.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
specific language governing rights and limitations under the License. The
Original Code is ProjectLibre. The Original Developer is the Initial Developer and
is ProjectLibre, Inc. All portions of the code written by ProjectLibre are Copyright (c)
2012. All Rights Reserved. Contributors ProjectLibre, Inc.

Alternatively, the contents of this file may be used under the terms of the
ProjectLibre End-User License Agreement (the ProjectLibre License), in which case the
provisions of the ProjectLibre License are applicable instead of those above. If you
wish to allow use of your version of this file only under the terms of the
ProjectLibre License and not to allow others to use your version of this file under
the CPAL, indicate your decision by deleting the provisions above and replace
them with the notice and other provisions required by the ProjectLibre  License. If
you do not delete the provisions above, a recipient may use your version of this
file under either the CPAL or the ProjectLibre License.

[NOTE: The text of this license may differ slightly from the text of the notices
in Exhibits A and B of the license at http://www.projectlibre.com/license. You should
use the latest text at http://www.projectlibre.com/license for your modifications.
You may not remove this license text from the source files.]

Attribution Information: Attribution Copyright Notice: Copyright (c) 2012
ProjectLibre, Inc. Attribution Phrase (not exceeding 10 words): Powered by ProjectLibre,
Attribution URL: http://www.projectlibre.com Graphic Image as provided in the Covered
Code as file:  projectlibre_logo.png with alternatives listed on 
http://www.projectlibre.com/logo

Display of Attribution Information is required in Larger Works which are defined
in the CPAL as a work which combines Covered Code or portions thereof with code
not governed by the terms of the CPAL. However, in addition to the other notice
obligations, all copies of the Covered Code in Executable and Source Code form
distributed must, as a form of attribution of the original author, include on
each user interface screen the "ProjectLibre" logo visible to all users.  The
ProjectLibre logo should be located horizontally aligned with the menu bar and left
justified on the top left of the screen adjacent to the File menu.  The logo
must be at least 100 x 25 pixels.  When users click on the "ProjectLibre" logo it
must direct them back to http://www.projectlibre.com.
*/
package org.projectlibre.print;

import java.util.Enumeration;

import javax.swing.table.TableColumn;

import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.pm.graphic.graph.GraphParams;
import com.projity.pm.graphic.spreadsheet.SpreadSheetParams;
import com.projity.pm.graphic.spreadsheet.SpreadSheetParamsImpl;
import com.projity.print.ExtendedPrintService;

public class ProjectLibrePrintServiceImpl implements ExtendedPrintService {

	public double getWRatio(int pageCount, double pageWidth,GraphParams params) {
		double newPageWidth;
		if (!(params instanceof SpreadSheetParamsImpl) || !params.isLeftPartVisible()){
			//no spreadsheet
			newPageWidth=params.getDrawingBounds().getWidth()/pageCount;
		}else{
			SpreadSheetParamsImpl spParams=(SpreadSheetParamsImpl)params;
			double totalWidth=getWidthWithPaging(-1.0,spParams);

			//use dichotomy to find the pageWidth matching the chosen pageCount
			double minPageWidth=totalWidth/pageCount;
			double maxPageWidth=totalWidth;
			newPageWidth=(minPageWidth+maxPageWidth)/2;
			double currentWidth;
			while (maxPageWidth-minPageWidth>0.5){
				currentWidth=getWidthWithPaging(newPageWidth,spParams);
				if (currentWidth<newPageWidth*pageCount) maxPageWidth=newPageWidth;
				else if (currentWidth>newPageWidth*pageCount) minPageWidth=newPageWidth;
				else {
					maxPageWidth=newPageWidth;
					break;
				}
				newPageWidth=(minPageWidth+maxPageWidth)/2;
			}
			newPageWidth=maxPageWidth;
		}
		double r=pageWidth/newPageWidth;
		//adding a margin, round issues
		double margin=1.0; //1 pixel
		if (r<1) r=pageWidth/(newPageWidth+margin/r);
		return r;
	}


	public double getHRatio(int pageCount,double pageHeight,GraphParams params) {
		if (params==null||!(params instanceof SpreadSheetParams)) return -1.0;
		SpreadSheetParams sp=(SpreadSheetParams)params;
		double newPageHeight=(Math.ceil(((double)params.getCache().getSize())/pageCount)*sp.getRowHeight()+GraphicConfiguration.getInstance().getColumnHeaderHeight()+GraphicConfiguration.getInstance().getPrintFooterHeight());
		return pageHeight/newPageHeight;
	}


	protected double getWidthWithPaging(double pageWidth,SpreadSheetParamsImpl spParams) {
		return getSpreadSheetWidthWithPaging(pageWidth,spParams)+getGanttWidthWithPaging(spParams);
	}

	protected double getSpreadSheetWidthWithPaging(double pageWidth,SpreadSheetParamsImpl spParams) {
		double spreadsheetWidth=0.0;		
		if (pageWidth<0) // return default page width
			spreadsheetWidth=spParams.getSpreadSheetWidth();
		else {
			//intitial spreadsheet Width
			double currentSpreadsheetWidth=GraphicConfiguration.getInstance().getRowHeaderWidth()+2*spParams.getIdColMargin();				
			for (Enumeration<TableColumn> e=spParams.getColumnModel().getColumns();e.hasMoreElements();){
				TableColumn col=e.nextElement();			
				int colWidth=col.getPreferredWidth()+2*spParams.getColMargin();
				//increment until it exceed page width
				if (currentSpreadsheetWidth+colWidth>pageWidth){
					spreadsheetWidth+=pageWidth; //next page to avoid cutting
					currentSpreadsheetWidth=0;
				}

				currentSpreadsheetWidth+=colWidth;
			}
			spreadsheetWidth+=currentSpreadsheetWidth;
		}
		return spreadsheetWidth;
	}

	protected double getGanttWidthWithPaging(SpreadSheetParamsImpl spParams) {
		if (spParams.isRightPartVisible())
			return spParams.getGanttBounds().getWidth();
		else return 0.0;
	}

}
