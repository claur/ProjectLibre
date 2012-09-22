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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.print.PageFormat;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.print.PrintService;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.offline_graphics.SVGRenderer;
import com.projity.pm.graphic.graph.GraphParams;
import com.projity.strings.Messages;
import com.projity.util.Alert;
import com.projity.util.Environment;
/**
 *
 */
public class PageSetup{
	private static final long serialVersionUID = 1L;

	public static final double INCH=2.54;
	public static final double POINT=INCH/72;

	private GraphPageable document;
	private ExtendedPageFormat pageFormat;
	private ExtendedPrintService extendedPrintService;

	public static PageSetup getInstance(GraphPageable document) {
		return new PageSetup(document);
	}
	private PageSetup(GraphPageable document) {
		this.document=document;
		pageFormat=document.getPageFormat();
		//HelpUtil.addDocHelp(this,"Print_Preview"); //TODO specific help
		extendedPrintService=ExtendedPrintServiceFactory.getExtendedPrintService();
	}


	protected JTabbedPane tabbedPane;

	protected JComboBox paperSize;
	protected JComboBox printers;
//	protected JComboBox settings;
//	protected String settingsLocal,settingsWebPDF;
	protected JFormattedTextField paperWidth,paperHeight;
	protected JLabel paperWidthLabel,paperHeightLabel;

	protected JLabel marginLeftLabel,marginRightLabel,marginTopLabel,marginBottomLabel;
	protected JFormattedTextField marginLeft,marginRight,marginTop,marginBottom;

	protected JComboBox orientation;
	protected String portrait,landscape,reverseLandscape;

	protected JCheckBox printSpreadSheet,printGantt;

	protected JRadioButton scaleTo,fitTo;
	protected JSpinner fitToWidth,fitToHeight,scaleToWidth,scaleToHeight;
	protected JLabel fitToWidthLabel,fitToHeightLabel,scaleToWidthLabel,scaleToHeightLabel,scaleToProportions,scaleToProportions2;
	protected JCheckBox constrainProportions;

	protected JButton saveSettings;//,saveWebPDFSettings;

	protected double svgPaperWidth,svgPaperHeight;

	protected PageSizes.MediaSizeNameModel sizesModel;

	protected void initFont(JComponent c){
		c.setFont(c.getFont().deriveFont(8));
	}

	protected int updating;
	protected void beginUpdate(){
		updating++;
	}
	protected void endUpdate(){
		updating--;
	}
	protected boolean isUpdaping(){
		return updating>0;
	}


	protected PrintServiceOption[] printerList;
	protected static class PrintServiceOption{
		private static final int maxLen=25;
		protected PrintService printService;
		public PrintServiceOption(PrintService printService) {
			super();
			this.printService = printService;
		}
		public String toString() {
			String name=printService.getName();
			if (name.length()>maxLen) name=name.substring(0,maxLen);
			return name;
		}
		public PrintService getValue() {
			return printService;
		}
		public boolean equals(Object o){
			if (o==null||!(o instanceof PrintServiceOption)) return false;
			PrintServiceOption p=(PrintServiceOption)o;
			return printService.equals(p.getValue());
		}
	}
	protected PrintServiceOption pdfPrintServiceOption;




	protected void initControls() {
		beginUpdate();
		PrintSettings printSettings=document.getPrintSettings();
		PrintService[] pss=document.getPrintServices();
		PrintService ps=document.getPrintService();
		PrintService pdfPrintService=document.getPdfPrintService();
		printerList=new PrintServiceOption[pss.length];
		PrintServiceOption selectedOption=null;
		for (int i=0;i<pss.length;i++){
			printerList[i]=new PrintServiceOption(pss[i]);
			if (pss[i].equals(ps)) selectedOption=printerList[i];
			if (pss[i].equals(pdfPrintService)) pdfPrintServiceOption=printerList[i];
		}
		printers=new JComboBox(printerList);
		printers.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (isUpdaping()) return;
				beginUpdate();
				PrintServiceOption option=(PrintServiceOption)printers.getSelectedItem();
				PrintService printService=option.getValue();
				document.setPrintService(printService,false);
				sizesModel.update(printService);
				handlePaperSizeChange();
				switch (pageFormat.getOrientation()){
					case PageFormat.LANDSCAPE: orientation.setSelectedItem(landscape);
						break;
					case PageFormat.REVERSE_LANDSCAPE: orientation.setSelectedItem(reverseLandscape);
						break;
					default: orientation.setSelectedItem(portrait);
				}
				orientation.setEnabled(!isBigPage());
				updateOrientation();
				endUpdate();
				refresh();
			}
		});
		printers.setSelectedItem(selectedOption);
		initFont(printers);

//		settingsLocal=Messages.getString("PageSetupDialog.Settings.Local");
//		settingsWebPDF=Messages.getString("PageSetupDialog.Settings.WebPDF");
//		settings=new JComboBox(new Object[]{settingsLocal,settingsWebPDF});
//		settings.addActionListener(new ActionListener(){
//			public void actionPerformed(ActionEvent e) {
//				if (isUpdaping()) return;
//				String setting=(String)settings.getSelectedItem();
//				if (settingsWebPDF.equals(setting)) printers.setSelectedItem(pdfPrintServiceOption);
//			}
//		});
//		initFont(settings);



		sizesModel=PageSizes.getInstance().createComboBoxModel(document.getPrintService());
		paperSize=new JComboBox(sizesModel);
		paperSize.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (isUpdaping()) return;
				beginUpdate();
				 orientation.setEnabled(!isBigPage());
				handlePaperSizeChange();
				endUpdate();
				refresh();
			}
		});
		initFont(paperSize);
		//paperSize.setEnabled(false);
		paperWidthLabel=new JLabel(Messages.getString("PageSetupDialog.PaperSizeSettings.Custom.Width"));
		paperWidthLabel.setEnabled(false);
		initFont(paperWidthLabel);
		DecimalFormat decimalFormat=new DecimalFormat("#0.0");
		paperWidth=new JFormattedTextField(decimalFormat);
		paperWidth.setEnabled(false);
		paperWidth.addPropertyChangeListener("value",new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e) {
				if (isUpdaping()) return;
				updateImageableArea(((Number)e.getNewValue()).doubleValue(),-1.0,ExtendedPageFormat.CUSTOM);
				refresh();
			}
		});
		initFont(paperWidth);
		paperHeightLabel=new JLabel(Messages.getString("PageSetupDialog.PaperSizeSettings.Custom.Height"));
		paperHeightLabel.setEnabled(false);
		initFont(paperHeightLabel);
		paperHeight=new JFormattedTextField(decimalFormat);
		paperHeight.setEnabled(false);
		paperHeight.addPropertyChangeListener("value",new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e) {
				if (isUpdaping()) return;
				updateImageableArea(-1.0,((Number)e.getNewValue()).doubleValue(),ExtendedPageFormat.CUSTOM);
				refresh();
			}
		});
		initFont(paperHeight);

		paperSize.setSelectedItem(PageSizes.getInstance().getPageSize(pageFormat));
		initFont(paperSize);

		//margins
		marginLeftLabel=new JLabel(Messages.getString("PageSetupDialog.Margins.Left"));
		initFont(marginLeftLabel);
		marginLeft=new JFormattedTextField(decimalFormat);
		marginLeft.addPropertyChangeListener("value",new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e) {
				if (isUpdaping()) return;
				beginUpdate();
				double[] margins=updateImageableArea(((Number)e.getNewValue()).doubleValue(), -1.0, -1.0, -1.0,0);
				if (margins!=null){
					marginLeft.setValue(margins[0]);
					marginRight.setValue(margins[1]);
					marginTop.setValue(margins[2]);
					marginBottom.setValue(margins[3]);

				}
				endUpdate();
				refresh();
			}
		});
		initFont(marginLeft);

		marginRightLabel=new JLabel(Messages.getString("PageSetupDialog.Margins.Right"));
		initFont(marginRightLabel);
		marginRight=new JFormattedTextField(decimalFormat);
		marginRight.addPropertyChangeListener("value",new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e) {
				if (isUpdaping()) return;
				beginUpdate();
				double[] margins=updateImageableArea(-1.0, ((Number)e.getNewValue()).doubleValue(), -1.0, -1.0,1);
				if (margins!=null){
					marginLeft.setValue(margins[0]);
					marginRight.setValue(margins[1]);
					marginTop.setValue(margins[2]);
					marginBottom.setValue(margins[3]);

				}
				endUpdate();
				refresh();
			}
		});
		initFont(marginRight);

		marginTopLabel=new JLabel(Messages.getString("PageSetupDialog.Margins.Top"));
		initFont(marginTopLabel);
		marginTop=new JFormattedTextField(decimalFormat);
		marginTop.addPropertyChangeListener("value",new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e) {
				if (isUpdaping()) return;
				beginUpdate();
				double[] margins=updateImageableArea(-1.0, -1.0, ((Number)e.getNewValue()).doubleValue(), -1.0,2);
				if (margins!=null){
					marginLeft.setValue(margins[0]);
					marginRight.setValue(margins[1]);
					marginTop.setValue(margins[2]);
					marginBottom.setValue(margins[3]);

				}
				endUpdate();
				refresh();
			}
		});
		initFont(marginTop);

		marginBottomLabel=new JLabel(Messages.getString("PageSetupDialog.Margins.Bottom"));
		initFont(marginBottomLabel);
		marginBottom=new JFormattedTextField(decimalFormat);
		marginBottom.addPropertyChangeListener("value",new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e) {
				if (isUpdaping()) return;
				beginUpdate();
				double[] margins=updateImageableArea(-1.0, -1.0, -1.0, ((Number)e.getNewValue()).doubleValue(),3);
				if (margins!=null){
					marginLeft.setValue(margins[0]);
					marginRight.setValue(margins[1]);
					marginTop.setValue(margins[2]);
					marginBottom.setValue(margins[3]);

				}
				endUpdate();
				refresh();
			}
		});
		initFont(marginBottom);

		portrait=Messages.getString("PageSetupDialog.Orientation.Portrait");
		landscape=Messages.getString("PageSetupDialog.Orientation.Landscape");
		reverseLandscape=Messages.getString("PageSetupDialog.Orientation.ReverseLandscape");
		orientation=new JComboBox(new Object[]{portrait,landscape/*,reverseLandscape*/});
		orientation.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (isUpdaping()) return;
				Object selected=orientation.getSelectedItem();
				if (portrait.equals(selected)) pageFormat.setOrientation(PageFormat.PORTRAIT);
				if (landscape.equals(selected)) pageFormat.setOrientation(PageFormat.LANDSCAPE);
				if (reverseLandscape.equals(selected)) pageFormat.setOrientation(PageFormat.REVERSE_LANDSCAPE);
				beginUpdate();
				updateOrientation();
				endUpdate();
				refresh();
			}
		});
		initFont(orientation);
		switch (printSettings.getPageFormat().getOrientation()) {
		case PageFormat.PORTRAIT: orientation.setSelectedItem(portrait);
			break;
		case PageFormat.REVERSE_LANDSCAPE: orientation.setSelectedItem(reverseLandscape);
			break;
		default: orientation.setSelectedItem(landscape);
		}
		orientation.setEnabled(!isBigPage());

		if (document.getRenderer().getParams().isSupportLeftAndRightParts()){

			printSpreadSheet=new JCheckBox(Messages.getString("PageSetupDialog.ShowSpreadSheet"),document.isShowSpreadsheet());
			printSpreadSheet.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent e) {
					if (isUpdaping()) return;
					boolean selected=printSpreadSheet.isSelected();
					GraphParams params=document.getRenderer().getParams();
					params.setLeftPartVisible(selected);
					if (!selected&&!printGantt.isSelected()){
						params.setRightPartVisible(true);
						beginUpdate();
						printGantt.setSelected(true);
						endUpdate();
					}

					refresh();
				}
			});
			initFont(printSpreadSheet);
			printGantt=new JCheckBox(Messages.getString("PageSetupDialog.ShowGantt"),document.isShowGantt());
			printGantt.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent e) {
					if (isUpdaping()) return;
					boolean selected=printGantt.isSelected();
					GraphParams params=document.getRenderer().getParams();
					params.setRightPartVisible(selected);
					if (!selected&&!printGantt.isSelected()){
						params.setLeftPartVisible(true);
						beginUpdate();
						printSpreadSheet.setSelected(true);
						endUpdate();
					}

					refresh();
				}
			});
			initFont(printGantt);
		}





		//scaling
		scaleToWidthLabel=new JLabel(Messages.getString("PageSetupDialog.Scaling.ScaleToWidth"));
		scaleToWidthLabel.setEnabled(document.isScaleToSelected());
		initFont(scaleToWidthLabel);
		scaleToWidth=new JSpinner(new SpinnerNumberModel(document.getScaleToSettings().getWidth(),0.1,10.0,0.05));
		scaleToWidth.setEditor(new JSpinner.NumberEditor(scaleToWidth,"#%"));
		scaleToWidth.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				double ratio=((Number)scaleToWidth.getValue()).doubleValue();
				//pageFormat.setScaleWRatio(ratio);
				beginUpdate();
				document.setZoomX(ratio);
				if (constrainProportions.isSelected()){
					//pageFormat.setScaleHRatio(ratio);
					document.setZoomY(ratio);
					scaleToHeight.setValue(ratio);
				}
				if (isBigPage()) handlePaperSizeChange();
				endUpdate();
				refresh();
			}
		});
		scaleToWidth.setEnabled(document.isScaleToSelected());
		initFont(scaleToWidth);

		scaleToHeightLabel=new JLabel(Messages.getString("PageSetupDialog.Scaling.ScaleToHeight"));
		scaleToHeightLabel.setEnabled(document.isScaleToSelected()&&!document.getScaleToSettings().isConstrainProportions());
		initFont(scaleToHeightLabel);
		scaleToHeight=new JSpinner(new SpinnerNumberModel(document.getScaleToSettings().getHeight(),0.1,10.0,0.05));
		scaleToHeight.setEditor(new JSpinner.NumberEditor(scaleToHeight,"#%"));
		scaleToHeight.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				if (isUpdaping()) return;
				beginUpdate();
				double ratio=((Number)scaleToHeight.getValue()).doubleValue();
				//pageFormat.setScaleHRatio(ratio);
				document.setZoomY(ratio);
				if (isBigPage()&&!constrainProportions.isSelected())  handlePaperSizeChange(); //will be done by scaleToWidth
				endUpdate();
				refresh();
			}
		});
		scaleToHeight.setEnabled(document.isScaleToSelected()&&!document.getScaleToSettings().isConstrainProportions());
		initFont(scaleToHeight);
		constrainProportions=new JCheckBox("",document.getScaleToSettings().isConstrainProportions());
		constrainProportions.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				boolean selected=constrainProportions.isSelected();
				scaleToHeightLabel.setEnabled(!selected);
				scaleToHeight.setEnabled(!selected);
				if (selected) scaleToHeight.setValue(scaleToWidth.getValue());
			}
		});
		constrainProportions.setEnabled(document.isScaleToSelected());
		initFont(constrainProportions);
		scaleToProportions=new JLabel(Messages.getString("PageSetupDialog.Scaling.ScaleToProportions"));
		scaleToProportions.setEnabled(document.isScaleToSelected());
		initFont(scaleToProportions);
		scaleToProportions2=new JLabel(Messages.getString("PageSetupDialog.Scaling.ScaleToProportions2"));
		scaleToProportions2.setEnabled(document.isScaleToSelected());
		initFont(scaleToProportions2);


		fitToWidthLabel=new JLabel(Messages.getString("PageSetupDialog.Scaling.FitToWidth"));
		fitToWidthLabel.setEnabled(!document.isScaleToSelected());
		initFont(fitToWidthLabel);
		fitToWidth=new JSpinner(new SpinnerNumberModel(document.getFitToSettings().getColumns(),1,99999,1));
		fitToWidth.setEnabled(!document.isScaleToSelected());
		fitToWidth.setEditor(new JSpinner.NumberEditor(fitToWidth,"#"));
		fitToWidth.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				if (isUpdaping()) return;
				//pageFormat.setPagesW(((Number)fitToWidth.getValue()).intValue());
				document.updateZoom(((Number)fitToWidth.getValue()).intValue(),((Number)fitToHeight.getValue()).intValue());
				refresh();
			}
		});
		initFont(fitToWidth);
		fitToHeightLabel=new JLabel(Messages.getString("PageSetupDialog.Scaling.FitToHeight"));
		fitToHeightLabel.setEnabled(!document.isScaleToSelected());
		initFont(fitToHeightLabel);
		fitToHeight=new JSpinner(new SpinnerNumberModel(document.getFitToSettings().getRows(),1,99999,1));
		fitToHeight.setEnabled(!document.isScaleToSelected());
		fitToHeight.setEditor(new JSpinner.NumberEditor(fitToHeight,"#"));
		fitToHeight.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				if (isUpdaping()) return;
				//pageFormat.setPagesH(((Number)fitToHeight.getValue()).intValue());
				document.updateZoom(((Number)fitToWidth.getValue()).intValue(),((Number)fitToHeight.getValue()).intValue());
				refresh();
			}
		});
		initFont(fitToHeight);


		scaleTo=new JRadioButton(Messages.getString("PageSetupDialog.Scaling.ScaleTo"),document.isScaleToSelected());
		scaleTo.setEnabled(true);
		initFont(scaleTo);
		fitTo=new JRadioButton(Messages.getString("PageSetupDialog.Scaling.FitTo"),!document.isScaleToSelected());
		fitTo.setEnabled(true);
		initFont(fitTo);
		ButtonGroup scalingRadio=new ButtonGroup();
		scalingRadio.add(scaleTo);
		scalingRadio.add(fitTo);
		scaleTo.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				boolean selected=scaleTo.isSelected();

				//pageFormat.setScalingOption(selected?ExtendedPageFormat.SCALE_TO:ExtendedPageFormat.FIT_TO);
				scaleToWidth.setEnabled(selected);
				scaleToWidthLabel.setEnabled(selected);
				boolean constrain=constrainProportions.isSelected();
				scaleToHeight.setEnabled(selected&&!constrain);
				scaleToHeightLabel.setEnabled(selected&&!constrain);
				constrainProportions.setEnabled(selected);
				scaleToProportions.setEnabled(selected);
				scaleToProportions2.setEnabled(selected);
				fitToWidth.setEnabled(!selected);
				fitToWidthLabel.setEnabled(!selected);
				fitToHeight.setEnabled(!selected);
				fitToHeightLabel.setEnabled(!selected);
				if (!selected){
					if (PageSizes.getInstance().isBigPageSize(paperSize.getSelectedItem())){
						paperSize.setSelectedItem(sizesModel.selectDefault(document.getPrintService(),false));
						paperSize.repaint();
					}
				}


				if (selected){
					document.restoreZoom();
				}else{
					document.saveZoom();
					updateZoom();
				}
				refresh();
			}
		});

		saveSettings=new JButton(Messages.getString("PageSetupDialog.SaveSettings"));
		saveSettings.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				saveSettings(true,true);
			}
		});
		saveSettings.setToolTipText(Messages.getString("PageSetupDialog.SaveSettings.ToolTip"));
		initFont(saveSettings);

//		saveWebPDFSettings=new JButton(Messages.getString("PageSetupDialog.SaveWebPDFSettings"));
//		saveWebPDFSettings.addActionListener(new ActionListener(){
//			public void actionPerformed(ActionEvent e) {
//				if (!(document.getPrintService() instanceof PDFPrintService)){
//					Alert.warnWithOnceOption(Messages.getString("PageSetupDialog.NotPDFPrintServiceForWebDefault"),"NotPDFPrintServiceForWebDefaultWarned");
//				}
//
//				saveSettings(false,true);
//			}
//		});
//		saveWebPDFSettings.setToolTipText(Messages.getString("PageSetupDialog.SaveWebPDFSettings.ToolTip"));
//		initFont(saveWebPDFSettings);


		updatePageSize(true);
		updateOrientation();
		updateFitTo();
		endUpdate();
	}

	protected void saveSettings(boolean local,boolean persist){
		PrintSettings printSettings=new PrintSettings();
		printSettings.setPageFormat((ExtendedPageFormat)document.getPageFormat().clone());
		printSettings.setPdfService(document.getPrintService() instanceof PDFPrintService);

		ArrayList<ViewSettings> viewSettings=null;
		if (printSpreadSheet!=null&&printGantt!=null){
			GanttSettings s=new GanttSettings();
			s.setSpreadSheetVisible(printSpreadSheet.isSelected());
			s.setGanttVisible(printGantt.isSelected());
			viewSettings=new ArrayList<ViewSettings>(1);
			viewSettings.add(s);
		}else{
			ArrayList<ViewSettings> documentViewSettings=document.getPrintSettings().getViewSettings();
			if (documentViewSettings!=null) viewSettings=(ArrayList<ViewSettings>)documentViewSettings.clone();
		}
		printSettings.setViewSettings(viewSettings);

		if (!Environment.getStandAlone()){
			ArrayList<ScalingSettings> scalingSettings=new ArrayList<ScalingSettings>(2);
			ScaleToSettings scaleToSettings=new ScaleToSettings();
			scaleToSettings.setWidth(((Number)scaleToWidth.getValue()).doubleValue());
			scaleToSettings.setHeight(((Number)scaleToHeight.getValue()).doubleValue());
			scaleToSettings.setConstrainProportions(constrainProportions.isSelected());
			FitToSettings fitToSettings=new FitToSettings();
			fitToSettings.setColumns(((Number)fitToWidth.getValue()).intValue());
			fitToSettings.setRows(((Number)fitToHeight.getValue()).intValue());
			scalingSettings.add(scaleToSettings);
			scalingSettings.add(fitToSettings);
			printSettings.setScalingSettings(scalingSettings);
			printSettings.setScalingIndex(scaleTo.isSelected()?0:1);
		}
		if (!local) printSettings.setFieldArray(document.getRenderer().getProject().getFieldArray());
		printSettings.updateWorkspace();

		PrintSettingsManager.saveSettings(printSettings,local?null:document.getRenderer().getProject(),persist);

	}

	private boolean isBigPage(){
		return PageSizes.getInstance().isBigPageSize(paperSize.getSelectedItem());
	}

	protected void handlePaperSizeChange(){
		Object item=paperSize.getSelectedItem();
		boolean customPaper=PageSizes.getInstance().isCustomPageSize(item);
		boolean bigPage=isBigPage();
		paperWidthLabel.setEnabled(customPaper);
		paperWidth.setEnabled(customPaper);
		paperHeightLabel.setEnabled(customPaper);
		paperHeight.setEnabled(customPaper);

		if (bigPage){
			orientation.setSelectedItem(portrait);
			scaleTo.setSelected(true);
			svgPaperWidth=pageFormat.getPaper().getWidth();
			svgPaperHeight=pageFormat.getPaper().getHeight();
			SVGRenderer renderer=document.getRenderer();
			Dimension d=renderer.getCanvasSize();
			GraphParams params=renderer.getParams();
			double w=pageFormat.getOrientation()==PageFormat.PORTRAIT?d.getWidth():d.getHeight();
			double h=pageFormat.getOrientation()==PageFormat.PORTRAIT?d.getHeight():d.getWidth();
			int footerH=params.getConfiguration().getPrintFooterHeight();
			if (pageFormat.getOrientation()==PageFormat.PORTRAIT) h+=footerH;
			else w+=footerH;
			w=(w*PageSize.INCH)/(PageSize.POINTS_PER_INCH*PageSize.MM); //be sure the pages aren't cut due to the loss of precision
			h=(h*PageSize.INCH)/(PageSize.POINTS_PER_INCH*PageSize.MM);
			double[] margins=pageFormat.getMargins(false);
			double zx=document.getTotalZoomX();
			double zy=document.getTotalZoomY();
			//zx=pageFormat.getOrientation()==PageFormat.PORTRAIT?zx:zy;
			//zy=pageFormat.getOrientation()==PageFormat.PORTRAIT?zy:zx;
			w=Math.ceil(w*zx+margins[0]+margins[1]);
			h=Math.ceil(h*zy+margins[2]+margins[3]);
			updateImageableArea(w,h,ExtendedPageFormat.BIG_PAGE);
		}else{
			Dimension d=PageSizes.getInstance().getPageDimension(item);
			if (d!=null){
				MediaSizeName mediaSizeName=null;
				if (!customPaper&&!bigPage){
					mediaSizeName=((PageSizes.Format)item).getValue();
				}
				updateImageableArea(mediaSizeName,true);
				paperWidth.setValue(d.getWidth());
				paperHeight.setValue(d.getHeight());
			}
		}
	}


	public void refresh(){
		if (fitTo.isSelected()) updateZoom();
		document.update();
		document.getPrintPreviewFrame().updatePanel();
		beginUpdate();
		if (!fitTo.isSelected()) updateFitTo();
		endUpdate();
	}
	public void updateFitTo(){
		GraphParams params=document.getRenderer().getParams();
		fitToWidth.setValue(params.getPrintCols());
		fitToHeight.setValue(params.getPrintRows());
	}

	private void updateZoom(){
		document.updateZoom(((Number)fitToWidth.getValue()).intValue(),((Number)fitToHeight.getValue()).intValue());
	}


	private void updateImageableArea(MediaSizeName mediaSizeName,boolean verify){ //margin fixed
		boolean mchanged=false;
		double[] margins=pageFormat.getMargins(false);
		double left=margins[0];
		double right=margins[1];
		double top=margins[2];
		double bottom=margins[3];
		pageFormat.setSizeName(mediaSizeName);
		PageSize size=pageFormat.getSize();
		double w=size.getX(MediaSize.MM);
		double h=size.getY(MediaSize.MM);
		if (left<0.0){
			left=0.0;
			mchanged=true;
		}
		if (right<0.0){
			right=0.0;
			mchanged=true;
		}
		if (left+right>=w){
			left=Math.floor(w/4);
			right=Math.floor(w/4);
			mchanged=true;
		}
		if (top<0.0){
			top=0.0;
			mchanged=true;
		}
		if (bottom<0.0){
			bottom=0.0;
			mchanged=true;
		}
		if (top+bottom>=h){
			top=Math.floor(h/4);
			bottom=Math.floor(h/4);
			mchanged=true;
		}
		MediaPrintableArea area=new MediaPrintableArea((float)left, (float)top, (float)(w-left-right), (float)(h-top-bottom),MediaSize.MM);
		if (verify){
			MediaPrintableArea adaptedArea=ExtendedPageFormat.adaptMediaPrintableArea(area, document.getPrintService(), mediaSizeName);
			pageFormat.setPrintableArea(adaptedArea);
			if (area!=adaptedArea){
				//update margins
				mchanged=true;
			}
		}else{
			pageFormat.setPrintableArea(area);
		}
		if (mchanged) updateOrientation();
	}
	private void updateImageableArea(double w,double h,int type){ //margin fixed
		boolean mchanged=false;
		double[] margins=pageFormat.getMargins(false);
		double left=margins[0];
		double right=margins[1];
		double top=margins[2];
		double bottom=margins[3];
		PageSize size=pageFormat.getSize();
		if (w<0) w=size.getX(MediaSize.MM);
		if (h<0) h=size.getY(MediaSize.MM);
		pageFormat.setSize(new PageSize((float)w, (float)h,MediaSize.MM),type);
		if (left<0.0){
			left=0.0;
			mchanged=true;
		}
		if (right<0.0){
			right=0.0;
			mchanged=true;
		}
		if (left+right>=w){
			left=Math.floor(w/4);
			right=Math.floor(w/4);
			mchanged=true;
		}
		if (top<0.0){
			top=0.0;
			mchanged=true;
		}
		if (bottom<0.0){
			bottom=0.0;
			mchanged=true;
		}
		while (top+bottom>=h){
			top=Math.floor(h/4);
			bottom=Math.floor(h/4);
			mchanged=true;
		}
		MediaPrintableArea area=new MediaPrintableArea((float)left, (float)top, (float)(w-left-right), (float)(h-top-bottom),MediaSize.MM);
		pageFormat.setPrintableArea(area);
		if (mchanged){
			double[] m=pageFormat.getMargins(true);
			marginLeft.setValue(m[0]);
			marginRight.setValue(m[1]);
			marginTop.setValue(m[2]);
			marginBottom.setValue(m[3]);

		}
	}
	private double[] updateImageableArea(double oleft,double oright,double otop, double obottom,int index){ //size fixed
		boolean changed=false;
		int ori=pageFormat.getOrientation();
		double left=oleft;
		double right=oright;
		double top=otop;
		double bottom=obottom;
		switch (ori){
			case PageFormat.LANDSCAPE:
				left=otop;
				right=obottom;
				top=oright;
				bottom=oleft;
				break;
			case PageFormat.REVERSE_LANDSCAPE:
				left=obottom;
				right=otop;
				top=oleft;
				bottom=oright;
				break;
		}

		double[] oldMargins=pageFormat.getMargins(false);
		double oldLeft=oldMargins[0];
		double oldRight=oldMargins[1];
		double oldTop=oldMargins[2];
		double oldBottom=oldMargins[3];
		if (left<0){
			left=oldLeft;
			if (index==0) changed=true;
		}
		if (right<0){
			right=oldRight;
			if (index==1) changed=true;
		}
		if (top<0){
			top=oldTop;
			if (index==2) changed=true;
		}
		if (bottom<0){
			bottom=oldBottom;
			if (index==3) changed=true;
		}

		PageSize oldSize=pageFormat.getSize();
		double w=oldSize.getX(MediaSize.MM),h=oldSize.getY(MediaSize.MM);
		if (isBigPage()){
			w+=left-oldLeft+right-oldRight;
			h+=top-oldTop+bottom-oldBottom;
			pageFormat.setSize(new PageSize((float)w,(float)h,MediaSize.MM),ExtendedPageFormat.BIG_PAGE);
		}else{
			if (left+right>=w){
				left=oldLeft;
				right=oldRight;
				changed=true;
			}
			if (left+right>=w){
				left=Math.floor(w/4);
				right=Math.floor(w/4);
				changed=true;
			}
			if (top+bottom>=h){
				top=oldTop;
				bottom=oldBottom;
				changed=true;
			}
			if (top+bottom>=h){
				top=Math.floor(h/4);
				bottom=Math.floor(h/4);
				changed=true;
			}
		}
		MediaPrintableArea area=new MediaPrintableArea((float)left, (float)top, (float)(w-left-right), (float)(h-top-bottom),MediaSize.MM);
		//if (verify){
			MediaPrintableArea adaptedArea=ExtendedPageFormat.adaptMediaPrintableArea(area, document.getPrintService(), pageFormat.getSizeName());
			pageFormat.setPrintableArea(adaptedArea);
			if (area!=adaptedArea){
				//update margins
				changed=true;
			}
		//}else{
				pageFormat.setPrintableArea(area);
		//}
		if (changed){
			double[] m=pageFormat.getMargins(true);
			return new double[]{m[0],m[1],m[2],m[3]};
		} else return null;

	}

	private void updatePageSize(boolean round){ //round for a Linux bug
//		Paper paper=pageFormat.getPaper();
//		double w=round?Math.round(paper.getWidth()):paper.getWidth();
//		double h=round?Math.round(paper.getHeight()):paper.getHeight();
		PageSize size=pageFormat.getSize();
		paperWidth.setValue(size.getX(MediaSize.MM));
		paperHeight.setValue(size.getY(MediaSize.MM));
	}



	private void updateOrientation(){
		double[] margins=pageFormat.getMargins(true);
		double left=margins[0];
		double right=margins[1];
		double top=margins[2];
		double bottom=margins[3];
		marginLeft.setValue(left);
		marginRight.setValue(right);
		marginTop.setValue(top);
		marginBottom.setValue(bottom);
		//orientation.setSelectedItem(portrait);
	}



//	public double toUserUnit(double points){
//		return points*POINT;
//	}
//	public double toPoints(double cm){
//		return cm/POINT;
//	}







	public JComponent createContentPanel(boolean bar) {
		initControls();
		if (bar) return createVerticalPanel();
		else return createDialogPanel();
//		FormLayout layout = new FormLayout("350dlu:grow","fill:250dlu:grow");
//		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
//		builder.setDefaultDialogBorder();
//		builder.add(createPagePanel());
//		return builder.getPanel();
	}

	private JComponent createDialogPanel(){
		FormLayout layout = new FormLayout("p, 3dlu, p, 3dlu","p, 3dlu,p, 3dlu, p, 3dlu,p,3dlu");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		CellConstraints cc = new CellConstraints();
		builder.setDefaultDialogBorder();
		builder.add(createPrinterPanel(),cc.xyw(1,1,3));
		builder.add(createPaperPanel(),cc.xywh(1,3,1,3));
		builder.add(createOrientationPanel(),cc.xy(3,3));
		builder.add(createMarginsPanel(),cc.xy(3,5));
		builder.add(createScalingPanel(),cc.xyw(1,7,3));
		return builder.getPanel();
	}

	private JComponent createPrinterPanel(){
		FormLayout layout = new FormLayout("p","p, 3dlu");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.append(printers);
		return builder.getPanel();
	}

	private JComponent createVerticalPanel(){
		FormLayout layout = new FormLayout("p",printSpreadSheet==null?"p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu":"p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
//		builder.append(createFlowSettingsPanel());
//		builder.nextLine(2);
		builder.append(createFlowPrinterPanel());
		builder.nextLine(2);
		builder.append(createFlowOrientationPanel());
		builder.nextLine(2);
		builder.append(createFlowPaperPanel());
		builder.nextLine(2);
		builder.append(createFlowMarginsPanel());
		builder.nextLine(2);
		if (printSpreadSheet!=null){
			builder.append(createFlowPartPanel());
			builder.nextLine(2);
		}
		builder.append(createFlowScalingPanel());
		builder.nextLine(2);
		builder.append(saveSettings);
//		builder.nextLine(2);
//		builder.append(saveWebPDFSettings);
		return builder.getPanel();
	}

	private JComponent createFlowPrinterPanel(){
		FormLayout layout = new FormLayout("1dlu,p:grow,1dlu","p, 3dlu");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.nextColumn();
		builder.append(printers);
		JPanel panel=builder.getPanel();
		panel.setBorder(new TitledBorder(Messages.getString("PageSetupDialog.Printers")));
		return panel;
	}
//	private JComponent createFlowSettingsPanel(){
//		FormLayout layout = new FormLayout("1dlu,p:grow,1dlu","p, 3dlu");
//		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
//		builder.nextColumn();
//		builder.append(settings);
//		JPanel panel=builder.getPanel();
//		panel.setBorder(new TitledBorder(Messages.getString("PageSetupDialog.Settings.Label")));
//		return panel;
//	}


	private JComponent createPaperPanel(){
		FormLayout layout = new FormLayout(
		        "8dlu, p,p,8dlu",
				"p, 3dlu:grow,p, 2dlu");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		CellConstraints cc = new CellConstraints();
		builder.add(createPaperSizePanel(),cc.xyw(2,1,2));
		JPanel panel=builder.getPanel();
		panel.setBorder(new TitledBorder(Messages.getString("PageSetupDialog.Paper")));
		return panel;
	}
	private JComponent createFlowPaperPanel(){
		FormLayout layout = new FormLayout(
		        "1dlu, p,p,p:grow,1dlu",
				"p, 3dlu:grow, 2dlu");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		CellConstraints cc = new CellConstraints();
		builder.add(createFlowPaperSizePanel(),cc.xyw(2,1,3));
		JPanel panel=builder.getPanel();
		panel.setBorder(new TitledBorder(Messages.getString("PageSetupDialog.Paper")));
		return panel;
	}
	private JComponent createPaperSizePanel(){
		FormLayout layout = new FormLayout("p, 3dlu,50dlu","p, 3dlu, p, 3dlu, p");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		CellConstraints cc = new CellConstraints();
		builder.add(paperSize,cc.xyw(1, 1, 3));
		builder.add(paperWidthLabel,cc.xy(1,3));
		builder.add(paperWidth,cc.xy(3,3));
		builder.add(paperHeightLabel,cc.xy(1,5));
		builder.add(paperHeight,cc.xy(3,5));
		return builder.getPanel();
	}
	private JComponent createFlowPaperSizePanel(){
		FormLayout layout = new FormLayout("p, 3dlu,40dlu","p, 3dlu, p, 3dlu, p");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		CellConstraints cc = new CellConstraints();
		builder.add(paperSize,cc.xyw(1, 1, 3));
		builder.add(paperWidthLabel,cc.xy(1,3));
		builder.add(paperWidth,cc.xy(3,3));
		builder.add(paperHeightLabel,cc.xy(1,5));
		builder.add(paperHeight,cc.xy(3,5));
		return builder.getPanel();
	}

	private JComponent createOrientationPanel(){
		FormLayout layout = new FormLayout(
		        "8dlu, 100dlu, 8dlu",
				"p,2dlu");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		CellConstraints cc = new CellConstraints();
		builder.add(orientation,cc.xy(2,1));
		JPanel panel=builder.getPanel();
		panel.setBorder(new TitledBorder(Messages.getString("PageSetupDialog.Orientation")));
		return panel;
	}
	private JComponent createFlowOrientationPanel(){
		FormLayout layout = new FormLayout(
		        "1dlu, p:grow, 1dlu",
				"p,2dlu");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		CellConstraints cc = new CellConstraints();
		builder.add(orientation,cc.xy(2,1));
		JPanel panel=builder.getPanel();
		panel.setBorder(new TitledBorder(Messages.getString("PageSetupDialog.Orientation")));
		return panel;
	}


	private JComponent createMarginsPanel(){
		FormLayout layout = new FormLayout(
		        "8dlu, 50dlu, 3dlu, 50dlu, 8dlu",
				"p,1dlu,p,3dlu, p,1dlu,p,2dlu");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		CellConstraints cc = new CellConstraints();
		builder.add(marginLeftLabel,cc.xy(2,1));
		builder.add(marginLeft,cc.xy(2,3));
		builder.add(marginRightLabel,cc.xy(4,1));
		builder.add(marginRight,cc.xy(4,3));
		builder.add(marginTopLabel,cc.xy(2,5));
		builder.add(marginTop,cc.xy(2,7));
		builder.add(marginBottomLabel,cc.xy(4,5));
		builder.add(marginBottom,cc.xy(4,7));
		JPanel panel=builder.getPanel();
		panel.setBorder(new TitledBorder(Messages.getString("PageSetupDialog.Margins")));
		return panel;
	}
	private JComponent createFlowMarginsPanel(){
		FormLayout layout = new FormLayout(
		        "1dlu, 41dlu, 3dlu, 41dlu, 1dlu",
				"p,1dlu,p,3dlu, p,1dlu,p,2dlu");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		CellConstraints cc = new CellConstraints();
		builder.add(marginLeftLabel,cc.xy(2,1));
		builder.add(marginLeft,cc.xy(2,3));
		builder.add(marginRightLabel,cc.xy(4,1));
		builder.add(marginRight,cc.xy(4,3));
		builder.add(marginTopLabel,cc.xy(2,5));
		builder.add(marginTop,cc.xy(2,7));
		builder.add(marginBottomLabel,cc.xy(4,5));
		builder.add(marginBottom,cc.xy(4,7));
		JPanel panel=builder.getPanel();
		panel.setBorder(new TitledBorder(Messages.getString("PageSetupDialog.Margins")));
		return panel;
	}

	private JComponent createFlowPartPanel(){
		FormLayout layout = new FormLayout(
		        "1dlu, p, 1dlu",
				"p,3dlu, p");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		CellConstraints cc = new CellConstraints();
		if (printSpreadSheet!=null) builder.add(printSpreadSheet,cc.xy(2,1));
		if (printGantt!=null) builder.add(printGantt,cc.xy(2,3));
		JPanel panel=builder.getPanel();
		panel.setBorder(new TitledBorder(Messages.getString("PageSetupDialog.ShowParts")));
		return panel;
	}

	private JComponent createScalingPanel(){
		FormLayout layout = new FormLayout(
		        "8dlu, p, 3dlu,30dlu,3dlu,p,3dlu,p,p,p,8dlu",
		        "p, 3dlu,p,10dlu,p,3dlu,p,3dlu");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		CellConstraints cc = new CellConstraints();
		builder.add(scaleTo,cc.xy(2,1));
		builder.add(scaleToWidth,cc.xy(4, 1));
		builder.add(scaleToWidthLabel,cc.xy(6, 1));
		builder.add(constrainProportions,cc.xy(8, 1));
		JLabel l=new JLabel(Messages.getString("PageSetupDialog.Scaling.FitToProportions")+" "+Messages.getString("PageSetupDialog.Scaling.FitToProportions2"));
		initFont(l);
		builder.add(scaleToProportions,cc.xy(9,1));
		builder.add(scaleToProportions2,cc.xy(10,1));
		builder.add(scaleToHeight,cc.xy(4, 3));
		builder.add(scaleToHeightLabel,cc.xy(6, 3));

		builder.add(fitTo,cc.xy(2,5));
		builder.add(fitToWidth,cc.xy(4, 5));
		builder.add(fitToWidthLabel,cc.xy(6, 5));
		builder.add(fitToHeight,cc.xy(4, 7));
		builder.add(fitToHeightLabel,cc.xy(6, 7));
		JPanel panel=builder.getPanel();
		panel.setBorder(new TitledBorder(Messages.getString("PageSetupDialog.Scaling")));
		return panel;
	}
	private JComponent createFlowScalingPanel(){
		FormLayout layout = new FormLayout(
		        "1dlu, 5dlu,p,22dlu,3dlu,p,1dlu",
		        "p, 3dlu,p,3dlu,p,3dlu,p,p,10dlu,p,3dlu,p,3dlu,p,3dlu");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		CellConstraints cc = new CellConstraints();
		builder.add(scaleTo,cc.xyw(2,1,5));
		builder.add(scaleToWidth,cc.xyw(3, 3,2));
		builder.add(scaleToWidthLabel,cc.xy(6, 3));
		builder.add(scaleToHeight,cc.xyw(3, 5,2));
		builder.add(scaleToHeightLabel,cc.xy(6, 5));
		builder.add(constrainProportions,cc.xy(3,7));
		builder.add(scaleToProportions,cc.xyw(4,7,3));
		builder.add(scaleToProportions2,cc.xyw(4,8,4));

		builder.add(fitTo,cc.xyw(2,10,5));
		builder.add(fitToWidth,cc.xyw(3, 12,2));
		builder.add(fitToWidthLabel,cc.xy(6, 12));
		builder.add(fitToHeight,cc.xyw(3, 14,2));
		builder.add(fitToHeightLabel,cc.xy(6, 14));
		JPanel panel=builder.getPanel();
		panel.setBorder(new TitledBorder(Messages.getString("PageSetupDialog.Scaling")));
		return panel;
	}


}
