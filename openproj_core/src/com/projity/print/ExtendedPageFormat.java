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

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.print.PrintService;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.Size2DSyntax;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

public class ExtendedPageFormat extends PageFormat implements Cloneable,Serializable{
	static final long serialVersionUID = 785552028119291L;
	protected static final int DEFAULT_MARGINS=10;
	public static final int BIG_PAGE=1;
	public static final int CUSTOM=2;
	protected MediaSizeName sizeName;
	protected PageSize size;
	protected MediaPrintableArea printableArea;
	protected int type=0;

	public ExtendedPageFormat() {
		super();
		setSizeName(getDefaultMediaSizeName());
		printableArea=getDefaultMediaPrintableArea(size);
		setOrientation(PageFormat.LANDSCAPE);
		updatePaper();
	}
	public ExtendedPageFormat(ExtendedPageFormat pageFormat) {
		super();
		pageFormat.copy(this);
	}

	public static MediaSizeName getDefaultMediaSizeName(){
		return MediaSizeName.ISO_A4;
	}
	public static MediaPrintableArea getDefaultMediaPrintableArea(Size2DSyntax size){
		//MediaSize size=MediaSize.getMediaSizeForName(getDefaultMediaSizeName());
		return new MediaPrintableArea(DEFAULT_MARGINS,DEFAULT_MARGINS,size.getX(MediaSize.MM)-2*DEFAULT_MARGINS,size.getY(MediaSize.MM)-2*DEFAULT_MARGINS,MediaSize.MM);
	}

	public static MediaSizeName getDefaultMediaSizeName(PrintService printService){
		if (printService instanceof PDFPrintService) return ExtendedPageFormat.getDefaultMediaSizeName();
		else{
			Object attr=printService.getDefaultAttributeValue(Media.class);
			if (attr instanceof MediaSizeName) return (MediaSizeName)attr;
			else return null;
		}
	}
	public static MediaPrintableArea getDefaultMediaPrintableArea(PrintService printService,MediaSizeName mediaSizeName){
		//if (printService instanceof PDFPrintService) return ExtendedPageFormat.getDefaultMediaPrintableArea();
		//else return (MediaPrintableArea)printService.getDefaultAttributeValue(MediaPrintableArea.class);
		MediaSize size=MediaSize.getMediaSizeForName(mediaSizeName==null?getDefaultMediaSizeName():mediaSizeName);
		return adaptMediaPrintableArea(ExtendedPageFormat.getDefaultMediaPrintableArea(size), printService, mediaSizeName);
		//return ExtendedPageFormat.getDefaultMediaPrintableArea();
	}

	public static MediaPrintableArea adaptMediaPrintableArea(MediaPrintableArea m,PrintService printService,MediaSizeName mediaSizeName){
		if (mediaSizeName==null) return m; //bigPage
		MediaPrintableArea max=getMaxMediaPrintableArea(printService, mediaSizeName);
		boolean changed=false;
		float x=m.getX(MediaSize.MM),y=m.getY(MediaSize.MM),w=m.getWidth(MediaSize.MM),h=m.getHeight(MediaSize.MM);
		float mx=m.getX(MediaSize.MM),my=m.getY(MediaSize.MM),mw=m.getWidth(MediaSize.MM),mh=m.getHeight(MediaSize.MM);
		if (x<0){
			x=0;
			changed=true;
		}
		if (x>mx){
			x=mx;
			changed=true;
		}
		if (y<0){
			y=0;
			changed=true;
		}
		if (y>my){
			y=my;
			changed=true;
		}
		if (w<0){
			w=0;
			changed=true;
		}
		if (w>mw){
			w=mw;
			changed=true;
		}
		if (h<0){
			h=0;
			changed=true;
		}
		if (h>mh){
			h=mh;
			changed=true;
		}
		if (changed) return new MediaPrintableArea(x,y,w,h,MediaSize.MM);
		else return m;
	}

	private static MediaPrintableArea getMaxMediaPrintableArea(PrintService printService,MediaSizeName mediaSizeName){
		MediaPrintableArea area=null;;
		if (mediaSizeName!=null&&!(printService instanceof PDFPrintService)){
			PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
			aset.add(mediaSizeName);
			MediaPrintableArea[] printableArea =(MediaPrintableArea[])printService.getSupportedAttributeValues(MediaPrintableArea.class, null, aset);
			if (printableArea!=null&&printableArea.length==1) area=printableArea[0];
		}
		if (area==null){
			MediaSize size=MediaSize.getMediaSizeForName(mediaSizeName);
			area= new MediaPrintableArea(0,0,size.getX(MediaSize.MM),size.getY(MediaSize.MM),MediaSize.MM); //use paper size
		}
		return area;
	}


	public ExtendedPageFormat(MediaSizeName sizeName,MediaPrintableArea printableArea) {
		super();
		setSizeName(sizeName);
		//System.out.println("ExtendedPageFormat: "+printableArea);
		this.printableArea=printableArea;
		setOrientation(PageFormat.LANDSCAPE);
		updatePaper();
	}

	protected void updatePaper(){
		Paper paper=new Paper();
		paper.setSize(Math.round(size.getX(PageSize.INCH)*PageSize.POINTS_PER_INCH), Math.round(size.getY(PageSize.INCH)*PageSize.POINTS_PER_INCH));
		//System.out.println("Paper size: "+paper.getWidth()+"x"+paper.getHeight());
		paper.setImageableArea(Math.floor(printableArea.getX(PageSize.INCH)*PageSize.POINTS_PER_INCH), Math.floor(printableArea.getY(PageSize.INCH)*PageSize.POINTS_PER_INCH), Math.ceil(printableArea.getWidth(PageSize.INCH)*PageSize.POINTS_PER_INCH), Math.ceil(printableArea.getHeight(PageSize.INCH)*PageSize.POINTS_PER_INCH));
		setPaper(paper);
	}

	public PageSize getSize() {
		return size;
	}


	public void setSize(PageSize size,int type){
		this.type=type;
		this.sizeName=null;
		this.size=size;
		updatePaper();
	}


	public MediaSizeName getSizeName() {
		return sizeName;
	}
	public void setSizeName(MediaSizeName sizeName) {
		type=0;
		this.sizeName=sizeName;
		MediaSize s=MediaSize.getMediaSizeForName(sizeName);
		if (s==null) size=null;
		else size=new PageSize(s.getX(PageSize.MM),s.getY(PageSize.MM),PageSize.MM);
	}
	public MediaPrintableArea getPrintableArea() {
		return printableArea;
	}


	public void setPrintableArea(MediaPrintableArea printableArea) {
		//System.out.println("setPrintableArea: "+printableArea);
		this.printableArea = printableArea;
		updatePaper();
	}

	public double[] getMargins(boolean rotation){
		int ori=getOrientation();
		double left=printableArea.getX(MediaSize.MM);
		double right=size.getX(MediaSize.MM)-printableArea.getWidth(MediaSize.MM)-printableArea.getX(MediaSize.MM);
		double top=printableArea.getY(MediaSize.MM);
		double bottom=size.getY(MediaSize.MM)-printableArea.getHeight(MediaSize.MM)-printableArea.getY(MediaSize.MM);
		if (!rotation) return new double[]{left,right,top,bottom};
		switch (ori){
			case PageFormat.LANDSCAPE:
				return new double[]{bottom,top,left,right};
			case PageFormat.REVERSE_LANDSCAPE:
				return new double[]{top,bottom,right,left};
			default:
				return new double[]{left,right,top,bottom};
		}
	}

	public Object clone() {
		//try {
			ExtendedPageFormat p=(ExtendedPageFormat)super.clone();
			copy(p);
			return p;
		//} catch (CloneNotSupportedException e) {}
		//return null;
	}
	public void copy(ExtendedPageFormat p) {
		p.setOrientation(getOrientation());
		p.type=type;
		p.sizeName=sizeName;
		p.size=new PageSize(size.getX(PageSize.MM),size.getY(PageSize.MM),PageSize.MM);
		p.printableArea=new MediaPrintableArea(printableArea.getX(MediaSize.MM),printableArea.getY(MediaSize.MM),printableArea.getWidth(MediaSize.MM),printableArea.getHeight(MediaSize.MM),MediaSize.MM);
		p.updatePaper();
	}


	public void addAttributes(AttributeSet attr){
//		MediaSizeName mediaSizeName=MediaSize.findMedia((float)paper.getWidth(),(float)paper.getHeight(),MediaSize.INCH*72);
//		System.out.println("mediaSizeName="+mediaSizeName);
		attr.add(sizeName);
//		attr.add(new MediaPrintableArea((float)paper.getImageableX(),(float)paper.getImageableY(),(float)paper.getImageableWidth(),(float)paper.getImageableHeight(),MediaSize.INCH*72));
		attr.add(printableArea);
		OrientationRequested orientation;
		switch (getOrientation()) {
		case PageFormat.PORTRAIT: orientation=OrientationRequested.PORTRAIT;
			break;
		case PageFormat.LANDSCAPE: orientation=OrientationRequested.LANDSCAPE;
			break;
		case PageFormat.REVERSE_LANDSCAPE: orientation=OrientationRequested.REVERSE_LANDSCAPE;
		default: orientation=OrientationRequested.PORTRAIT;
			break;
		}
		attr.add(orientation);
	}

	private static final float VERSION=1.1f;
	private void writeObject(ObjectOutputStream s) throws IOException {
		s.writeFloat(VERSION);
		s.writeObject(sizeName);
		s.writeObject(sizeName==null?size:null);
		s.writeObject(printableArea);
		s.writeInt(getOrientation());
		s.writeInt(type);
	}
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException  {
		float version=s.readFloat();
		sizeName=(MediaSizeName)s.readObject();
		size=(PageSize)s.readObject();
		if (sizeName!=null) setSizeName(sizeName);
		printableArea=(MediaPrintableArea)s.readObject();
		int ori=s.readInt();
		setOrientation(ori);
		if (version>=1.1f){
			type=s.readInt();
			if (type==BIG_PAGE&&ori==LANDSCAPE){
				//to fix old landscape format
				setOrientation(PORTRAIT);
				size=new PageSize(size.getY(PageSize.MM),size.getX(PageSize.MM),PageSize.MM);
				printableArea=new MediaPrintableArea(printableArea.getY(PageSize.MM),printableArea.getX(PageSize.MM),printableArea.getHeight(PageSize.MM),printableArea.getWidth(PageSize.MM),PageSize.MM);
			}
		}
		updatePaper();
	}

	public int getType() {
		return type;
	}




}
