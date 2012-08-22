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

Attribution Information: Attribution Copyright Notice: Copyright © 2006, 2007 
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
package com.projity.reports.adapter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRGroup;
import net.sf.jasperreports.engine.JRTextElement;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignGroup;
import net.sf.jasperreports.engine.design.JRDesignLine;
import net.sf.jasperreports.engine.design.JRDesignReportFont;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JRDesignVariable;
import net.sf.jasperreports.engine.design.JasperDesign;

import com.projity.configuration.ReportColumns;
import com.projity.configuration.ReportDefinition;
import com.projity.field.Field;
import com.projity.graphic.configuration.SpreadSheetFieldArray;

/**
 *
 */
public class ReportAdapter {
	private static String GroupName = "group";

	private ReportDefinition reportDefinition;
	private JRDesignReportFont boldFont;
	private JRDesignReportFont italicFont;
	private JRDesignReportFont normalFont;
	private boolean hasAggregableField = false;
	
	private JasperDesign jasperDesign = new JasperDesign();
	
	public ReportAdapter(ReportDefinition reportDefinition) {
		this.reportDefinition = reportDefinition;
	}
	
	private boolean isAggregable(Field field) {
		return (Field.SUM == field.getSummaryForGroup());
	}
	
	private int neededWidth(SpreadSheetFieldArray fields) {
		int width = 0;
		
		// LEGAL: 612x1008
		// BORDERS: 20x30x20x30
		
		Iterator iterator = fields.iterator();
		while(iterator.hasNext()) {
			Field field = (Field)iterator.next();
			width += field.getColumnWidth();
			
		}
		return width;
	}
	
	private void generateBaseDesign()  throws JRException {
		
		if(reportDefinition.isTimeBased()) {
			jasperDesign.setProperty(DataSourceProvider.TIME_BASED, "true");
		}
		
		jasperDesign.setProperty(DataSourceProvider.COLLECTION_TYPE_PROPERTY
				, new Integer(reportDefinition.getCollectionType()).toString());
		
		jasperDesign.setName(reportDefinition.getName());
		jasperDesign.setPageWidth(1008);
		jasperDesign.setPageHeight(612);
		jasperDesign.setColumnWidth(968);
		jasperDesign.setColumnSpacing(0);
		jasperDesign.setLeftMargin(20);
		jasperDesign.setRightMargin(20);
		jasperDesign.setTopMargin(30);
		jasperDesign.setBottomMargin(30);
		
			//Fonts
			normalFont = new JRDesignReportFont();
			normalFont.setName("Arial_Normal");
			normalFont.setDefault(true);
			normalFont.setFontName("Arial");
			normalFont.setSize(10);
			normalFont.setPdfFontName("Helvetica");
			normalFont.setPdfEncoding("Cp1252");
			normalFont.setPdfEmbedded(false);
			jasperDesign.addFont(normalFont);
			
			boldFont = new JRDesignReportFont();
			boldFont.setName("Arial_Bold");
			boldFont.setDefault(false);
			boldFont.setFontName("Arial");
			boldFont.setSize(12);
			boldFont.setBold(true);
			boldFont.setPdfFontName("Helvetica-Bold");
			boldFont.setPdfEncoding("Cp1252");
			boldFont.setPdfEmbedded(false);
			jasperDesign.addFont(boldFont);
			
			italicFont = new JRDesignReportFont();
			italicFont.setName("Arial_Italic");
			italicFont.setDefault(false);
			italicFont.setFontName("Arial");
			italicFont.setSize(12);
			italicFont.setItalic(true);
			italicFont.setPdfFontName("Helvetica-Oblique");
			italicFont.setPdfEncoding("Cp1252");
			italicFont.setPdfEmbedded(false);
			jasperDesign.addFont(italicFont);

			//Title
			JRDesignBand band = new JRDesignBand();
			band.setHeight(50);
			JRDesignLine line = new JRDesignLine();
			line.setX(0);
			line.setY(0);
			line.setWidth(968);
			line.setHeight(0);
			band.addElement(line);
			
			JRDesignStaticText text = new JRDesignStaticText();
			
			text.setX(0);
			text.setY(10);
			text.setWidth(968);
			text.setHeight(30);
			text.setTextAlignment(JRTextElement.TEXT_ALIGN_CENTER);
			JRDesignReportFont bigFont = new JRDesignReportFont();
			bigFont.setName("Arial_Normal");
			bigFont.setDefault(true);
			bigFont.setFontName("Arial");
			bigFont.setSize(22);
			bigFont.setPdfFontName("Helvetica");
			bigFont.setPdfEncoding("Cp1252");
			bigFont.setPdfEmbedded(false);
			text.setFont(bigFont);
			text.setText(reportDefinition.getName());
			band.addElement(text);
			jasperDesign.setTitle(band);
			
	}
	
	public JRDesignTextField getPageFooter() {
		JRDesignTextField textField = new JRDesignTextField();
		textField.setX(0);
		textField.setY(10);
		textField.setWidth(968);
		textField.setHeight(15);
		textField.setTextAlignment(JRTextElement.TEXT_ALIGN_CENTER);
		textField.setFont(normalFont);
		JRDesignExpression expression = new JRDesignExpression();
		expression.setValueClass(java.lang.String.class);
		expression.setText("\"Page \" + String.valueOf($V{PAGE_NUMBER})"); //  + \" of\"");
		textField.setExpression(expression);
		return textField;
	}
	
	private String getFieldName(Field field, boolean asDuration) {
		String fieldName = field.getId();
		String returnFieldName = "";
		
		String mod = "";
		
		fieldName = fieldName.substring(new String("Field.").length());
		
		if( (!field.isMoney())) {
			if(!asDuration) {
				mod = "MODText" + mod;
				mod = mod + "FIELD";
			}
		}
		
		returnFieldName = mod + fieldName;
		
		return returnFieldName;
	}
	
	private Class getFieldClass(Field field, boolean asDuration) {
		if(field.isMoney()) {
			return Double.class;
		} else if(field.isDurationOrWork() && asDuration) {
			return Long.class;
		} else {
			return String.class;
		}
	}
	
	private String getFieldPattern(Field field) {
		if(field.isMoney()) {
			return "$ #,##0.00";
		} else {
			return null;
		}
	}
	
	private void addFields(ArrayList fields) throws JRException {
		Iterator iterator = fields.iterator();
		while(iterator.hasNext()) {
			Field field = (Field)iterator.next();
			
			JRDesignField designField = new JRDesignField();
			designField.setName(getFieldName(field, false));
			designField.setValueClass(getFieldClass(field, false));


//			System.out.println("field is " + designField.getName());
			try{ //TOTO try catch is to avoid problems with duplicate field. Find the cause.
				jasperDesign.addField(designField);
	
				if(isAggregable(field)) {
					hasAggregableField = true;
	
					if(field.isDurationOrWork()) {
						// add extra Long field (used for calculations)
						designField = new JRDesignField();
						designField.setName(getFieldName(field, true));
						designField.setValueClass(getFieldClass(field, true));
						jasperDesign.addField(designField);
					}
				}
			}catch(JRException e){System.out.println(e.getMessage());}
		}
	}
	
/**
 * Add fields which have sums.  If a group is passed in, assign the filds to the group so that the sums
 * are reset after each group. 
 * @param fields
 * @param group
 * @throws JRException
 */
	private void addAggregableFields(ArrayList fields, JRDesignGroup group) throws JRException {
		Iterator iterator = fields.iterator();
		while(iterator.hasNext()) {
			Field field = (Field)iterator.next();

			if(isAggregable(field)) {
				String fieldName = getFieldName(field, true);
				JRDesignVariable variable = new JRDesignVariable();
				variable.setName(fieldName + "Sum");
				variable.setValueClass(getFieldClass(field, true));
				variable.setCalculation(JRDesignVariable.CALCULATION_SUM);
				if (group == null) {
					variable.setResetType(JRDesignVariable.RESET_TYPE_REPORT);
				} else {
					variable.setResetType(JRDesignVariable.RESET_TYPE_GROUP);
					variable.setResetGroup(group);
					
				}
				
				JRDesignExpression expression = new JRDesignExpression();
				expression.setValueClass(getFieldClass(field, true));
				expression.setText("$F{" + fieldName + "}");
				variable.setExpression(expression);
				jasperDesign.addVariable(variable);
				
			}
		}
	}
	
	private JRDesignBand addFieldsHeader(JRDesignBand band, SpreadSheetFieldArray fields, boolean isSub) {
//		JRDesignRectangle rectangle = new JRDesignRectangle();
//		rectangle.setX(0);
//		rectangle.setY(5);
//		rectangle.setWidth(515);
//		rectangle.setHeight(15);
////		if(isSub) {
////			rectangle.setForecolor(new Color(0x99, 0x99, 0x99));
////			rectangle.setBackcolor(new Color(0x99, 0x99, 0x99));
////		} else {
////			rectangle.setForecolor(new Color(0x33, 0x33, 0x33));
////			rectangle.setBackcolor(new Color(0x33, 0x33, 0x33));
////		}
//		band.addElement(rectangle);
		
		// columns in page header
		Iterator iterator = fields.iterator();
		int x = 0;
		while(iterator.hasNext()) {
			Field field = (Field)iterator.next();
			JRDesignStaticText staticText = new JRDesignStaticText();
			staticText.setX(x);
			if(isSub) {
				staticText.setY(20);
			} else {
				staticText.setY(5);
			}
			staticText.setWidth(field.getColumnWidth());
			staticText.setHeight(15);
			staticText.setForecolor(Color.white);
			if(isSub) {
				staticText.setBackcolor(new Color(0x99, 0x99, 0x99));
			} else {
				staticText.setBackcolor(new Color(0x33, 0x33, 0x33));
			}
			staticText.setMode(JRElement.MODE_OPAQUE);
			staticText.setTextAlignment(JRTextElement.TEXT_ALIGN_RIGHT);
			staticText.setFont(boldFont);
			staticText.setText(field.getName());
			band.addElement(staticText);
			
			x += field.getColumnWidth();
		}
		
		return band;
	}
	
	private JRDesignBand getFieldsHeader(SpreadSheetFieldArray fields, boolean isSub) throws JRException {
		//Page header
		JRDesignBand band = new JRDesignBand();
		band.setHeight(20);
		return addFieldsHeader(band, fields, isSub);
	}
	
	private JRDesignBand addDetail(JRDesignBand band, SpreadSheetFieldArray fields, JRGroup group) throws JRException {
		Iterator iterator = fields.iterator();
		int x = 0;
		if(null != group) {
			band.setHeight(40);
		} else {
			band.setHeight(15);
		}
		while(iterator.hasNext()) {
			Field field = (Field)iterator.next();
			JRDesignTextField textField = new JRDesignTextField();
			if(null != group) {
				textField.setEvaluationTime(JRDesignExpression.EVALUATION_TIME_GROUP);
				textField.setEvaluationGroup(group);
//				textField.setBackcolor(Color.black);
//				textField.setForecolor(Color.white);
				textField.setY(5);
				textField.setFont(boldFont);
				textField.setHeight(15);
			}
			else {
				textField.setY(0);
				textField.setFont(normalFont);
				textField.setHeight(12);
			}
			textField.setX(x);
			textField.setWidth(field.getColumnWidth());
			textField.setTextAlignment(JRTextElement.TEXT_ALIGN_RIGHT);

			String fieldName = getFieldName(field, false);
			if(field.isMoney()) {
				// Double
				textField.setPattern(getFieldPattern(field));
			}

			JRDesignExpression expression = new JRDesignExpression();
			expression.setValueClass(getFieldClass(field, false));
			expression.setText("$F{" + fieldName + "}");

			textField.setExpression(expression);
			band.addElement(textField);

			x += field.getColumnWidth();
		}
		
		return band;
	}
	
	private JRDesignBand getDetail(SpreadSheetFieldArray fields, JRGroup group) throws JRException {
		//Detail
		JRDesignBand band = new JRDesignBand();
		return addDetail(band, fields, group);
	}
	
	private JRDesignBand getAggregatableFooter(SpreadSheetFieldArray fields) throws JRException {
		
		JRDesignBand band = new JRDesignBand();
		band.setHeight(40);
		Iterator iterator = fields.iterator();
		int x = 0;
		while(iterator.hasNext()) {
			Field field = (Field)iterator.next();
			if(isAggregable(field)) {
				JRDesignLine line = new JRDesignLine();
				line.setX(x);
				line.setY(0);
				line.setWidth(field.getColumnWidth());
				line.setHeight(0);
				band.addElement(line);
				line.setY(2);
				band.addElement(line);

				JRDesignTextField textField = new JRDesignTextField();
				textField.setX(x);
				textField.setY(4);
				textField.setWidth(field.getColumnWidth());
				textField.setHeight(12);
				textField.setTextAlignment(JRTextElement.TEXT_ALIGN_RIGHT);
				textField.setFont(normalFont);
				JRDesignExpression expression = new JRDesignExpression();

				
				// Money (double) or Duration(long)
				if(field.isMoney()) {
					expression.setValueClass(getFieldClass(field, true));
					textField.setPattern(getFieldPattern(field));
					expression.setText("$V{" + getFieldName(field, true) + "Sum}");
				} else if (field.isWork()) {
					expression.setValueClass(String.class);
					expression.setText("com.projity.datatype.DurationFormat.formatWork($V{" + getFieldName(field, true) + "Sum})" );
				} else if(field.isDuration()) {
					expression.setValueClass(String.class);
					expression.setText("com.projity.datatype.DurationFormat.format($V{" + getFieldName(field, true) + "Sum})" );
				}

				textField.setExpression(expression);
				band.addElement(textField);
			}

			x += field.getColumnWidth();
		}

		return band;

	}
	
	private void addLastPageFooter(SpreadSheetFieldArray fields) throws JRException {
		JRDesignBand band = getAggregatableFooter(fields);
		band.addElement(getPageFooter());
		
		jasperDesign.setLastPageFooter(band);
	}
	private void addPageFooter() throws JRException {
		//page footer
		JRDesignBand band = new JRDesignBand();
		band.setHeight(30);
		JRDesignLine line = new JRDesignLine();
		line.setX(0);
		line.setY(0);
		line.setWidth(968);
		line.setHeight(0);
		band.addElement(line);
		
		band.addElement(getPageFooter());
		jasperDesign.setPageFooter(band);
	}
	
	public void generateDesign(SpreadSheetFieldArray fieldArray) throws JRException {
		hasAggregableField = false;
		generateBaseDesign();
		ArrayList columnsList = (ArrayList) reportDefinition.getColumnsList();

		ReportColumns columns;
		if(columnsList.size() == 1) {
			// simple flat report
			SpreadSheetFieldArray fields;
			if (fieldArray != null)
				fields =  fieldArray;
			else {
				columns = (ReportColumns)columnsList.get(0);
				fields = columns.getFieldArray();
			}	
			addFields(fields);
			
			if(hasAggregableField) {
				addAggregableFields(fields,null);
			}
			
			jasperDesign.setPageHeader(getFieldsHeader(fields, false));
			
			jasperDesign.setDetail(getDetail(fields, null));
			
			addPageFooter();
			
			// last page footer (if any)
			if(hasAggregableField) {
				addLastPageFooter(fields);
			}
			
//			int neededW = neededWidth(fields);
//			System.out.println("Needed width is " + neededW);
//			System.out.println("columns number is " + fields.size());
		} else if(columnsList.size() == 2) {
			// reports & subreports
			columns = (ReportColumns)columnsList.get(0);
			String groupByField = columns.getGroupbyField();
			SpreadSheetFieldArray mainFields = columns.getFieldArray();
			columns = (ReportColumns)columnsList.get(1);
			SpreadSheetFieldArray detailFields;
			if (fieldArray != null)
				detailFields = fieldArray;
			else
				detailFields = columns.getFieldArray();
			
			addFields(mainFields);
			addFields(detailFields);
			
			JRDesignGroup group = new JRDesignGroup();
			if(hasAggregableField) {
				addAggregableFields(detailFields,group);
			}
			
			
			group.setName(GroupName);
			group.setStartNewColumn(false);
			group.setStartNewPage(false);
			JRDesignExpression expression = new JRDesignExpression();
			
			Iterator iterator = mainFields.iterator();
			while(iterator.hasNext()) {
				Field f = (Field)iterator.next();

				if(groupByField.equals(f.getId())) 
				{
					expression.setText("$F{" + getFieldName(f, false) + "}");
					expression.setValueClass(getFieldClass(f, false));
					group.setExpression(expression);
					break;
				}
			}
			
			JRDesignBand band = getDetail(mainFields, group);
			band = addFieldsHeader(band, detailFields, true);
			group.setGroupHeader(band);

			if(hasAggregableField) {
				group.setGroupFooter(getAggregatableFooter(detailFields));
			}
			jasperDesign.addGroup(group);

			jasperDesign.setPageHeader(getFieldsHeader(mainFields, false));
			jasperDesign.setDetail(getDetail(detailFields, null));
			addPageFooter();
			
		} else {
			throw new JRException("report definition must contain either one or two columns (see view.xml)");
		}
		
	}
	
	/**
	 * @return Returns the jasperDesign.
	 */
	public JasperDesign getJasperDesign() {
		return jasperDesign;
	}
}