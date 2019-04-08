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
package com.projectlibre1.reports.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.sf.jasperreports.compilers.JRBshCompiler;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import com.projectlibre1.configuration.Dictionary;
import com.projectlibre1.configuration.ReportDefinition;
import com.projectlibre1.graphic.configuration.SpreadSheetFieldArray;
import com.projectlibre1.timescale.TimeIterator;
import com.projectlibre1.util.ClassLoaderUtils;

/**
 *
 */
public class ReportUtil {
	private static final String REPORT_ROOT = "com/projectlibre1/reports/definition/";	
	
/*
 * 
 	private static InputStream openReport(String fileName) {
		String urlName = REPORT_ROOT + fileName;
		URL url = ReportUtil.class.getClassLoader().getResource(urlName);
		try {
			return url.openStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
*/
	
	private static InputStream openReport(String fileName) {
		String urlName = REPORT_ROOT + fileName;
		URL url = ClassLoaderUtils.getLocalClassLoader().getResource(urlName);
		try {
			return url.openStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static JasperReport getReport(ReportDefinition reportDefinition, TimeIterator iterator, SpreadSheetFieldArray columns) throws JRException {
	    JasperReport report = (JasperReport) reportDefinition.getReportObject(columns); // if it is already compiled, reuse it
		if (report == null) {
			
			JasperDesign jasperDesign = null;
			
			if(null != reportDefinition.getFile()) {
				// regular jrxml file
				InputStream reportDefinitionStream = openReport(reportDefinition.getFile());
				jasperDesign = JRXmlLoader.load(reportDefinitionStream);
			} else {
				// jasper design made by ReportAdapter
				ReportAdapter reportAdapter = new ReportAdapter(reportDefinition);
				reportAdapter.generateDesign(columns);
				jasperDesign = reportAdapter.getJasperDesign();
			}
			

			// check if design needs timescale
//			System.out.println("is report time based: " + jasperDesign.getProperty("timeBased"));
//			if(null != jasperDesign.getProperty("timeBased")) {
			//				jasperDesign = DataSourceProvider.addTimescale(jasperDesign, iterator, java.lang.String.class);
			//		}

		
			// for running in webstart, need to use bsh compiler
			JRBshCompiler theCompiler = new JRBshCompiler();
			report = theCompiler.compileReport(jasperDesign);
		//	report = JasperCompileManager.compileReport(reportDefinitionStream);
			
			reportDefinition.setReportObject(report,columns);
		}
		return report;
	}
	
	public static Object[] getReportDefinitions() {
		return Dictionary.getAll(ReportDefinition.CATEGORY);
	}
	
	public static ReportDefinition getFromName(String name) {
		Object[] defs = getReportDefinitions();
		for (int i =0; i < defs.length; i++) {
			ReportDefinition def = (ReportDefinition)defs[i];
			if (def.getName().equals(name))
				return def;
		}
		return null;
	}
}
