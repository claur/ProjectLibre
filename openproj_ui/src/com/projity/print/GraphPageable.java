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

Attribution Information: Attribution Copyright Notice: Copyright (c) 2006, 2007
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


import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import com.projity.job.Job;
import com.projity.job.JobQueue;
import com.projity.job.JobRunnable;
import com.projity.offline_graphics.SVGRenderer;
import com.projity.pm.graphic.graph.GraphParams;
import com.projity.pm.graphic.spreadsheet.SpreadSheetParams;
import com.projity.pm.graphic.spreadsheet.renderer.FontManager;
import com.projity.session.SessionFactory;
import com.projity.strings.Messages;
import com.projity.util.Alert;

public class GraphPageable extends PrintDocument implements ViewPrintableParams{
	protected SVGRenderer renderer;
	protected int documentRowCount,documentColCount;
	protected ViewPrintable defaultPrintable;

	public GraphPageable (SVGRenderer renderer,boolean printOnly,boolean pdfAsDefault,boolean localSettings) {
		this(renderer,false,printOnly,pdfAsDefault,localSettings);
   }
	private GraphPageable (SVGRenderer renderer,boolean pdfOnly,boolean printOnly,boolean pdfAsDefault,boolean localSettings) {
		   super(renderer.getProject(),pdfOnly,printOnly,pdfAsDefault,localSettings);
		   FontManager.setOfflineDefaultFont(FontManager.DEFAULT_FONT);
		   this.renderer=renderer;

		   GraphParams params=renderer.getParams();

		   if (params instanceof SpreadSheetParams && params.isLeftPartVisible() && params.isRightPartVisible()){
			   params.setLeftPartVisible(showSpreadsheet);
			   params.setRightPartVisible(showGantt);
		   }else{
			   showSpreadsheet=params.isLeftPartVisible();
			   showGantt=params.isRightPartVisible();
		   }

			zoomX=scaleToSettings.getWidth();
			zoomY=scaleToSettings.isConstrainProportions()?zoomX:scaleToSettings.getHeight();
			saveZoom();
			if (!scaleToSelected){
				updateZoom(fitToSettings.getColumns(), fitToSettings.getRows());
			}

	    }


	public int getNumberOfPages() {
		return documentRowCount*documentColCount;
	}
	public PageFormat getPageFormat(int page) throws IndexOutOfBoundsException {
		return getPageFormat();
	}
	public Printable getPrintable(int page) throws IndexOutOfBoundsException {
		return getDefaultPrintable();
	}
	public ViewPrintable getSafePrintable(){
		return new ViewPrintable(new ViewPrintableParamsImpl(getTransform(),renderer.createSafePrintCopy(),documentRowCount,documentColCount,getTotalZoomX(),getTotalZoomY()));
	}
//	public ViewPrintable getSafePrintable(JobRunnable j,PrinterJob pjob){
//		return new ViewPrintable(new ViewPrintableParamsImpl(getTransform(),renderer.createSafePrintCopy(),documentRowCount,documentColCount),j,pjob);
//	}

	public ExtendedPageFormat getSafePageFormat(){
		return new ExtendedPageFormat(getPageFormat());
	}

	public  PrintPreviewFrame getPrintPreviewFrame(){
		if (printPreview==null){
			printPreview = new PrintPreviewFrame(this);
		}
		return printPreview;
	}

	public void preview() {
		getPrintPreviewFrame().pack();
		getPrintPreviewFrame().setVisible(true);
	}
//	public void print() {
//		if (job==null) return;
//		getPrintPreviewFrame().pack();
//		//job.setPageable(this);
//		job.setPrintable(getSafePrintable(), getSafePageFormat());
//		if (job.printDialog()) {
//				update();
//
//
//				final PrinterJob pj=job;
//				final JobQueue jobQueue=SessionFactory.getInstance().getJobQueue();
//				Job j=new Job(jobQueue,"Printing","Printing...",true,getPrintPreviewFrame());
//				//job.setCustomCriticalSection(true);
//				j.addRunnable(new JobRunnable("Printing",1.0f){
//					public Object run() throws Exception{
//						try {
//							pj.setPrintable(getSafePrintable(this,pj), getSafePageFormat());
//							pj.print();
//						}catch (PrinterException e) {
//							Alert.error(e.getMessage());
//						}catch (Exception e) {
//							e.printStackTrace();
//						}
//						return null;
//					}
//				});
//				jobQueue.schedule(j);
//		}
//	}


	public void print() {
		if (printService instanceof PDFPrintService){
			Alert.error(Messages.getString("PageSetupDialog.NotValidPrinter"));
			return;
		}
		try {
			final PrinterJob printerJob=PrinterJob.getPrinterJob();
			printerJob.setPrintService(printService);

			ViewPrintable vp=getSafePrintable();
			vp.setPageFormat(printerJob.validatePage(getSafePageFormat()));
			vp.update();
//			printerJob.setPageable(printable);
			printerJob.setPrintable(vp, vp.getPageFormat());

			ViewPrintable printable=getSafePrintable();
			ExtendedPageFormat pageFormat=getSafePageFormat();
			printerJob.setPrintable(printable, pageFormat);
//			printable.setPageFormat(getSafePageFormat());
//			printerJob.setPageable(printable);
			if (printerJob.printDialog()) {
				//update();


				final JobQueue jobQueue=SessionFactory.getInstance().getJobQueue();
				Job j=new Job(jobQueue,"Printing","Printing...",true,getPrintPreviewFrame());
				j.addRunnable(new JobRunnable("Printing",1.0f){
					public Object run() throws Exception{
						try {
							ViewPrintable vp=getSafePrintable();
							vp.setJr(this);
							vp.setPrinterJob(printerJob);
							vp.setPageFormat(printerJob.validatePage(getSafePageFormat()));
							vp.update();
//							printerJob.setPageable(printable);
							printerJob.setPrintable(vp, vp.getPageFormat());
							vp.setJr(this);
							vp.setPrinterJob(printerJob);
							printerJob.print();
						}catch (PrinterException e) {
							Alert.error(e.getMessage());
						}catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}
				});
				jobQueue.schedule(j);
			}
		} catch (PrinterException e) {
			Alert.error(e.getMessage());
		}
	}

//	public void print() {
//		if (printService instanceof PDFPrintService){
//			Alert.error(Messages.getString("PageSetupDialog.NotValidPrinter"));
//			return;
//		}
//		try {
//			PrintRequestAttributeSet pattr=new HashPrintRequestAttributeSet();
//			pageFormat.addAttributes(pattr);
//
//
//			PrintService selectedService = ServiceUI.printDialog ( null, 200, 200, realPrintServices, printService, DocFlavor.SERVICE_FORMATTED.PRINTABLE, pattr) ;
//			if (selectedService!=null){
//				getPrintPreviewFrame().pack();
//				update();
//				DocPrintJob printJob = selectedService.createPrintJob (  ) ;
//				printJob.addPrintJobListener(new PrintJobListener(){
//					public void printDataTransferCompleted(PrintJobEvent pje) {
//						System.out.println("printDataTransferCompleted");
//
//					}
//					public void printJobCanceled(PrintJobEvent pje) {
//						System.out.println("printJobCanceled");
//
//					}
//					public void printJobCompleted(PrintJobEvent pje) {
//						System.out.println("printJobCompleted");
//
//					}
//					public void printJobFailed(PrintJobEvent pje) {
//						System.out.println("printJobFailed");
//
//					}
//					public void printJobNoMoreEvents(PrintJobEvent pje) {
//						System.out.println("printJobNoMoreEvents");
//
//					}
//					public void printJobRequiresAttention(PrintJobEvent pje) {
//						System.out.println("printJobRequiresAttention");
//
//					}
//				});
////				DocAttributeSet attr=new HashDocAttributeSet();
////				pageFormat.addAttributes(attr);
//				ViewPrintable printable=getSafePrintable();
//				//printable.setPageFormat(getSafePageFormat());
//
//				Doc doc=new SimpleDoc(printable,DocFlavor.SERVICE_FORMATTED.PRINTABLE,null);
//				printJob.print(doc, pattr);
//			}
//		}catch (PrintException e) {
//			e.printStackTrace();
//			Alert.error(e.getMessage());
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
////		}
//}


	public int getColumnCount(){
		return documentColCount;
	}


	public void update(){
		GraphParams params=renderer.getParams();
		double zx=getTotalZoomX();
		double zy=getTotalZoomY();
		//zx=pageFormat.getOrientation()==PageFormat.PORTRAIT?zx:zy;
		//zy=pageFormat.getOrientation()==PageFormat.PORTRAIT?zy:zx;
		int pageW=(int)Math.ceil((pageFormat.getImageableWidth()-1)/zx);
		int pageH=(int)Math.ceil((pageFormat.getImageableHeight()-1)/zy);
		params.setPrintBounds(new Rectangle(0,0,pageW,pageH));
		documentColCount=params.getPrintCols();
		documentRowCount=params.getPrintRows();
	}


	public ViewPrintable getDefaultPrintable(){
		if (defaultPrintable==null) defaultPrintable=new ViewPrintable(this);
		return defaultPrintable;
	}
	public void printWithDefault(Graphics g,int page) throws PrinterException{
		getDefaultPrintable().print(g,page);
	}





	public SVGRenderer getRenderer() {
		return renderer;
	}


	public int getDocumentColCount() {
		return documentColCount;
	}


	public int getDocumentRowCount() {
		return documentRowCount;
	}
	public void setDocumentRowCount(int documentRowCount) {
		this.documentRowCount = documentRowCount;
	}
	public void setDocumentColCount(int documentColCount) {
		this.documentColCount = documentColCount;
	}

	ExtendedPrintService extendedPrintService=ExtendedPrintServiceFactory.getExtendedPrintService();
	public void updateZoom(int pw,int ph){
		double iw=pageFormat.getImageableWidth();
		double ih=pageFormat.getImageableHeight();
		SVGRenderer renderer=getRenderer();
		GraphParams params=renderer.getParams();
		double zw=extendedPrintService.getWRatio(pw, iw,params);
		double zh=extendedPrintService.getHRatio(ph, ih,params);
		setTotalZoomX(zw);
		setTotalZoomY(zh);
	}


}
