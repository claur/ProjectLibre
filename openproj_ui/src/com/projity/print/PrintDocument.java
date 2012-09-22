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

import java.awt.geom.AffineTransform;
import java.awt.print.Pageable;
import java.util.List;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSizeName;

import com.projity.pm.task.Project;

/**
 *
 */
public abstract class PrintDocument implements Pageable{
	public static final double DEFAULT_ZOOM=0.77;
	protected ExtendedPageFormat pageFormat;
	protected PrintService[] printServices;
	protected PrintService[] realPrintServices;
	protected PrintService printService;
	protected PDFPrintService pdfPrintService;
	//protected DocPrintJob printJob;;
	//protected PrinterJob job;
	protected PrintPreviewFrame printPreview;
	protected PrintSettings printSettings;



	boolean scaleToSelected=true;
	ScaleToSettings scaleToSettings=null;
	FitToSettings fitToSettings=null;
	boolean showSpreadsheet=true;
	boolean showGantt=true;
	protected double svgZoomX,svgZoomY;

	/**
	 *
	 */
	public PrintDocument(Project project,boolean pdfOnly,boolean printOnly, boolean pdfAsDefault,boolean localSettings) {
		printSettings=PrintSettingsManager.getSettings(localSettings?null:project);
		//if (printSettings==null) System.out.println("PrintSettings: null");
		//else System.out.println("PrintSettings: "+printSettings.getPrintServiceName()+", "+printSettings.getPageFormat().getSizeName()+", "+printSettings.getPageFormat().getOrientation()+", "+printSettings.getPageFormat().getSize()+", "+printSettings.getPageFormat().getPrintableArea());
		pdfPrintService=new PDFPrintService(); //Don't want to define a printService for PDF
		if (pdfOnly){
			realPrintServices=new PrintService[]{};
			printServices=new PrintService[]{pdfPrintService};
			setPrintService(pdfPrintService,true);
		}else{
			realPrintServices=PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
			PrintService defaultService=PrintServiceLookup.lookupDefaultPrintService();
			if (realPrintServices.length==0&&defaultService!=null) realPrintServices=new PrintService[]{defaultService}; //strange but it can occur
			printServices=new PrintService[realPrintServices.length+1];
			printServices[0]=pdfPrintService;
			for (int i=0;i<realPrintServices.length;i++) printServices[i+1]=realPrintServices[i];

			if (!printOnly&&pdfAsDefault) defaultService=pdfPrintService;

			if (!printOnly&&printSettings.isPdfService()){
				defaultService=pdfPrintService;
			}else if (printSettings.getPrintServiceName()!=null){
				PrintService d=null;
				for (int i=0;i<printServices.length;i++){
					if (printSettings.getPrintServiceName().equals(printServices[i].getName())){
						d=printServices[i];
						break;
					}
				}
				if (d!=null) defaultService=d;
			}
			setPrintService(defaultService==null?pdfPrintService:defaultService,true);
		}
		List<ViewSettings> viewSettings=printSettings.getViewSettings();
		if (viewSettings!=null){
			for (ViewSettings v: viewSettings){
				if (v instanceof GanttSettings){
					GanttSettings gs=(GanttSettings)v;
					showGantt=gs.isGanttVisible();
					showSpreadsheet=gs.isSpreadSheetVisible();
				}
			}
		}

		List<ScalingSettings> scalingSettings=printSettings.getScalingSettings();
		if (scalingSettings!=null){
			int index=0;
			for (ScalingSettings s:scalingSettings){
				if (s instanceof ScaleToSettings){
					scaleToSettings=(ScaleToSettings)s;
					if (printSettings.scalingIndex==index) scaleToSelected=true;
				}
				else if (s instanceof FitToSettings){
					fitToSettings=(FitToSettings)s;
					if (printSettings.scalingIndex==index) scaleToSelected=false;
				}
				index++;
			}
		}
		if (scaleToSettings==null) scaleToSettings=new ScaleToSettings();
		if (fitToSettings==null) fitToSettings=new FitToSettings();


	}


	public PrintService[] getPrintServices() {
		return printServices;
	}


	public PrintService getPrintService() {
		return printService;
	}


	public void setPrintService(PrintService printService,boolean useDefautSettings) {
		//boolean pageFormatAlreadyExists=true;
		this.printService = printService;
		if (useDefautSettings&&printSettings!=null&&pageFormat!=null){
			ExtendedPageFormat newPageFormat=printSettings.getPageFormat();
			if (newPageFormat!=null) newPageFormat.copy(pageFormat);
		}
		if (pageFormat==null){
			try{
				if (printSettings.isEmpty()||printSettings.getPageFormat()==null){
						MediaSizeName mediaSizeName=getDefaultMediaSizeName();
						MediaPrintableArea mediaPrintableArea=getDefaultMediaPrintableArea(mediaSizeName);
						pageFormat=new ExtendedPageFormat(mediaSizeName,mediaPrintableArea);
				}else{
					pageFormat=(ExtendedPageFormat)printSettings.getPageFormat().clone();
				}
				//pageFormatAlreadyExists=false;
			}catch (Exception e) {e.printStackTrace();}
		}
		if (pageFormat==null){
			pageFormat=new ExtendedPageFormat();
			//pageFormatAlreadyExists=false;
		}
//		if (!pageFormatAlreadyExists){
//			pageFormat.setOrientation(PageFormat.LANDSCAPE);
//		}

		if (printSettings.isEmpty()){
			printSettings.setPdfService(printService instanceof PDFPrintService);
			printSettings.setPrintServiceName(printService.getName());
			printSettings.setPageFormat(pageFormat);
			printSettings.setEmpty(false);
		}
	}

	public MediaSizeName getDefaultMediaSizeName(){
		return ExtendedPageFormat.getDefaultMediaSizeName(printService);
	}
	public MediaPrintableArea getDefaultMediaPrintableArea(MediaSizeName mediaSizeName){
		return ExtendedPageFormat.getDefaultMediaPrintableArea(printService,mediaSizeName);
	}


	public ExtendedPageFormat getPageFormat() {
		return pageFormat;
	}
//	public void setPageFormat(ExtendedPageFormat pageFormat) {
//		this.pageFormat = pageFormat;
//		if (printPreview!=null) printPreview.update();
//	}



//	public void preview() {
//		printPreview = new PrintPreviewFrame(this);
//		printPreview.show();
//	}
	public abstract void print();

//	/**
//	 *
//	 * @param pageFormat
//	 * @return a new instance of PageFormat or null if pageFormat hasn't changed
//	 */
//	public PageFormat printerPageFormatDialog(PageFormat pageFormat) {
//		if (job==null) return null;
//		PageFormat pf=job.pageDialog(pageFormat);
//		return pf==pageFormat?null:pf;
//	}

	public int getColumnCount(){
		return 1;
	}



	public void update(){}

	protected double zoomX=1.0;
	public double getZoomX() {
		return zoomX;
	}
	public void setZoomX(double zoomX) {
		this.zoomX = zoomX;
	}
	protected double zoomY=1.0;
	public double getZoomY() {
		return zoomY;
	}
	public void setZoomY(double zoomY) {
		this.zoomY = zoomY;
	}

	public double getTotalZoomX(){
		return zoomX*DEFAULT_ZOOM;
	}
	public void setTotalZoomX(double z){
		zoomX=z/DEFAULT_ZOOM;
	}
	public double getTotalZoomY(){
		return zoomY*DEFAULT_ZOOM;
	}
	public void setTotalZoomY(double z){
		zoomY=z/DEFAULT_ZOOM;
	}

	public AffineTransform getTransform(){
		return new AffineTransform(zoomX*DEFAULT_ZOOM,0.0,0.0,zoomY*DEFAULT_ZOOM,pageFormat.getImageableX(),pageFormat.getImageableY());
	}


	public PrintSettings getPrintSettings() {
		return printSettings;
	}


	public FitToSettings getFitToSettings() {
		return fitToSettings;
	}


	public void setFitToSettings(FitToSettings fitToSettings) {
		this.fitToSettings = fitToSettings;
	}


	public boolean isScaleToSelected() {
		return scaleToSelected;
	}


	public void setScaleToSelected(boolean scaleToSelected) {
		this.scaleToSelected = scaleToSelected;
	}


	public ScaleToSettings getScaleToSettings() {
		return scaleToSettings;
	}


	public void setScaleToSettings(ScaleToSettings scaleToSettings) {
		this.scaleToSettings = scaleToSettings;
	}


	public boolean isShowGantt() {
		return showGantt;
	}


	public void setShowGantt(boolean showGantt) {
		this.showGantt = showGantt;
	}


	public boolean isShowSpreadsheet() {
		return showSpreadsheet;
	}


	public void setShowSpreadsheet(boolean showSpreadsheet) {
		this.showSpreadsheet = showSpreadsheet;
	}


	public double getSvgZoomX() {
		return svgZoomX;
	}



	public double getSvgZoomY() {
		return svgZoomY;
	}


	public void saveZoom() {
		svgZoomX=zoomX;
		svgZoomY=zoomY;
	}
	public void restoreZoom() {
		zoomX=svgZoomX;
		zoomY=svgZoomY;
	}


	public PDFPrintService getPdfPrintService() {
		return pdfPrintService;
	}




}
