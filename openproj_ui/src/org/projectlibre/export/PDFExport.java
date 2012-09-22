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
package org.projectlibre.export;

import java.awt.Component;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import com.projity.job.Job;
import com.projity.job.JobQueue;
import com.projity.job.JobRunnable;
import com.projity.print.ExtendedPageFormat;
import com.projity.print.GraphPageable;
import com.projity.print.ViewPrintable;
import com.projity.session.SessionFactory;

public class PDFExport {
	public static void export(final GraphPageable pageable,Component parentComponent) throws IOException{
		final File file=chooseFile(pageable.getRenderer().getProject().getName(),parentComponent);
		final JobQueue jobQueue=SessionFactory.getInstance().getJobQueue();
		Job job=new Job(jobQueue,"PDF Export","Exporting PDF...",true,parentComponent);
		job.addRunnable(new JobRunnable("PDF Export",1.0f){
			public Object run() throws Exception{
				Document document = new Document();
				PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
				pageable.update();
				int pageCount = pageable.getNumberOfPages();
				if (pageCount>0){
					ViewPrintable printable=pageable.getSafePrintable();
					ExtendedPageFormat pageFormat=pageable.getSafePageFormat();
					double width=pageFormat.getWidth();
					double height=pageFormat.getHeight();
					float startIncrement=0.1f;
					float endIncrement=0.0f;						
					float progressIncrement = (1.0f-startIncrement-endIncrement)/pageCount;
					for (int p=0;p< pageCount;p++) {
						setProgress(startIncrement+p*progressIncrement);
						document.setPageSize(new Rectangle((float)width,(float)height));
						if (p==0) document.open();
						else document.newPage();
						
						Graphics2D g = writer.getDirectContent().createGraphics((float)width, (float)height);
						printable.print(g, p);
						g.dispose();
					}
					document.close();
				}
				setProgress(1.0f);
				return null;
			}
		});
		jobQueue.schedule(job);
	}

    private static JFileChooser chooser=null;
    private static File chooseFile(String projectName, Component parentComponent) {
    	if (chooser == null){
    		chooser = new JFileChooser();
    		chooser.putClientProperty("FileChooser.useShellFolder", Boolean.FALSE);
    		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
    		chooser.addChoosableFileFilter(new FileFilter(){
    		    public boolean accept(File f){
    		    	return f.isDirectory()||f.getName().toLowerCase().endsWith(".pdf");
    		    }
    		    public String getDescription(){
    		    	return "PDF (*.pdf)";
    		    }
    		});
    	}
		if (projectName.length()==0)
			projectName="project";
		chooser.setSelectedFile(new File(projectName+".pdf"));
		if (chooser.showDialog(parentComponent, null) == JFileChooser.APPROVE_OPTION){
			File file=chooser.getSelectedFile();
			if (!file.getName().endsWith(".pdf")) file=new File(file.getName()+".pdf"); //add pdf extension if missing
			return file;
		} else return null;
    }

}
