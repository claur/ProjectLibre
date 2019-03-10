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
package com.projectlibre1.print;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import com.projectlibre1.offline_graphics.SVGRenderer;
import com.projectlibre1.pm.graphic.graph.GraphParams;
import com.projectlibre1.job.JobRunnable;

public class DocumentPrintable implements Printable,Pageable{
	protected ViewPrintableParams printableParams;
	protected JobRunnable jr;
	protected PrinterJob printerJob;
	protected PageFormat pageFormat;
	public DocumentPrintable(){
	}
	public DocumentPrintable(ViewPrintableParams printableParams){
		this.printableParams=printableParams;
	}
//	public DocumentPrintable(ViewPrintableParams printableParams,JobRunnable jr,PrinterJob printerJob){
//		this.printableParams=printableParams;
//		this.jr=jr;
//		this.printerJob=printerJob;
//	}

	public int getNumberOfPages() {
		return printableParams.getDocumentRowCount()*printableParams.getDocumentColCount();
	}
	public int print (Graphics g, PageFormat pageFormat, int page) throws PrinterException {
		return print(g, page);
	}

	public JobRunnable getJr() {
		return jr;
	}
	public void setJr(JobRunnable jr) {
		this.jr = jr;
	}
	public PrinterJob getPrinterJob() {
		return printerJob;
	}
	public void setPrinterJob(PrinterJob printerJob) {
		this.printerJob = printerJob;
	}
	public PageFormat getPageFormat() {
		return pageFormat;
	}
	public void setPageFormat(PageFormat pageFormat) {
		this.pageFormat = pageFormat;
	}
	public PageFormat getPageFormat(int pageIndex){
		return pageFormat;
	}
	public Printable getPrintable(int pageIndex){
		return this;
	}

	public void update(){
		SVGRenderer renderer=printableParams.getRenderer();
		GraphParams params=renderer.getParams();
		int pageW=(int)Math.ceil((pageFormat.getImageableWidth()-1)/printableParams.getTotalZoomX());
		int pageH=(int)Math.ceil((pageFormat.getImageableHeight()-1)/printableParams.getTotalZoomY());
		params.setPrintBounds(new Rectangle(0,0,pageW,pageH));
		printableParams.setDocumentColCount(params.getPrintCols());
		printableParams.setDocumentRowCount(params.getPrintRows());
	}

	public int print (Graphics g, int page) throws PrinterException {
		int pageCount=getNumberOfPages();
		//System.out.println(page+"/"+(pageCount-1));
		if (page<pageCount){
			if (jr!=null&&jr.getJob().isCanceled()) printerJob.cancel();
			Graphics2D g2 = (Graphics2D) g;
			AffineTransform svgTransform=g2.getTransform();
			Color svgColor=g2.getColor();
			Stroke svgStroke=g2.getStroke();

			g2.transform(printableParams.getTransform());
			//System.out.println("Print transform="+printableParams.getTransform()+" zx="+printableParams.getTotalZoomX()+", zy="+printableParams.getTotalZoomY());
			g2.setStroke(spreadSheetStroke);
			g2.setColor(spreadSheetColor);

			printMain(g2,page);

			g2.setColor(svgColor);
			g2.setStroke(svgStroke);
			g2.setTransform(svgTransform);
			if (jr!=null){
				//System.out.println("Progress: "+(page+1));
				if (jr.getJob().isCanceled()){
					printerJob.cancel();
					return NO_SUCH_PAGE;
				}
				int pCount=getNumberOfPages();
				if (pCount==0) pCount=1;
				jr.setProgress(((float)(page+1))/pCount);
			}

			return PAGE_EXISTS;
		} return NO_SUCH_PAGE;
	}

	protected void printMain(Graphics2D g2, int page) throws PrinterException {

	}
	protected Stroke cellStroke=new BasicStroke(0.25f);
	protected Stroke spreadSheetStroke=new BasicStroke(0.5f);
	protected Color cellColor=Color.GRAY;
	protected Color spreadSheetColor=Color.BLACK;

}
