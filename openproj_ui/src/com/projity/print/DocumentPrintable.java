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
package com.projity.print;

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

import com.projity.job.JobRunnable;
import com.projity.offline_graphics.SVGRenderer;
import com.projity.pm.graphic.graph.GraphParams;

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
