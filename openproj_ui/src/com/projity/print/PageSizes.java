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

import java.awt.Dimension;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import com.projity.strings.Messages;

public class PageSizes extends MediaSizeName{
	public static final int BIG_PAGE=ExtendedPageFormat.BIG_PAGE;
	public static final int CUSTOM=ExtendedPageFormat.CUSTOM;
	protected static PageSizes instance;
	protected Format[] sizes,sizesSystemNames;
	public static class Format{
		protected String name;
		protected MediaSizeName value;
		protected Dimension dimension;
		protected int type;
		public Format(String name,int type) {
			this.name=name;
			this.value=null;
			this.type=type;
		}
//		public Format(String name, MediaSizeName value) {
//			this.name=name;
//			this.value=value;
//			dimension=new Dimension();
//			MediaSize size=MediaSize.getMediaSizeForName(value);
//			dimension.setSize(size.getX(MediaSize.MM),size.getX(MediaSize.MM));
//		}
		public Format(String name, MediaSize size) {
			this.name=name;
			this.value=size.getMediaSizeName();
			dimension=new Dimension();
			dimension.setSize(size.getX(MediaSize.MM),size.getY(MediaSize.MM));
		}
		public String toString() {
			return name;
		}
		public Dimension getDimension() {
			return dimension;
		}
		public MediaSizeName getValue() {
			return value;
		}
		public int getType() {
			return type;
		}

	}

	protected PageSizes(){
		super(-1);
		String[] names=getStringTable();
		EnumSyntax[] values=getEnumValueTable();
		ArrayList<Format> s=new ArrayList<Format>(names.length+2);
		s.add(new Format(Messages.getString("PageSetupDialog.PaperFormat.Custom"),CUSTOM));
		s.add(new Format(Messages.getString("PageSetupDialog.PaperSizeSettings.SinglePage"),BIG_PAGE));
		ArrayList<Format> so=new ArrayList<Format>(names.length+2);
		so.add(new Format(Messages.getString("PageSetupDialog.PaperFormat.Custom"),CUSTOM));
		so.add(new Format(Messages.getString("PageSetupDialog.PaperSizeSettings.SinglePage"),BIG_PAGE));
		for (int i=0;i<names.length;i++){
			MediaSize size=MediaSize.getMediaSizeForName((MediaSizeName)values[i]);
			if (size==null) continue; //all MediaSizeName aren't necessary present in MediaSize
			String oname=names[i];
			String name=oname;
			if (name.startsWith("iso-")) name=name.substring(4);
			else if (name.startsWith("na-")) name=name.substring(3);
			else if (name.startsWith("jis-")&&name.length()>5) name=name.substring(0,5).toUpperCase()+name.substring(5);
			if (name.length()==0) continue;
			name=name.substring(0,1).toUpperCase()+name.substring(1);
			s.add(new Format(name,size));
			so.add(new Format(oname,size));
		}
		sizes=new Format[s.size()];
		sizes=s.toArray(sizes);
		sizesSystemNames=new Format[so.size()];
		sizesSystemNames=so.toArray(sizesSystemNames);
	}

	public static PageSizes getInstance(){
		if (instance==null){
			instance=new PageSizes();
		}
		return instance;
	}


	public Format[] getPageSizes(){
		return sizes;
	}

	public Dimension getPageDimension(Object ps){
		if (ps==null || ! (ps instanceof Format)) return null;
		return ((PageSizes.Format)ps).getDimension();

	}
	public boolean isCustomPageSize(Object ps){
		if (ps==null || ! (ps instanceof Format)) return false;
		return ((PageSizes.Format)ps).getType()==CUSTOM;
	}
	public boolean isBigPageSize(Object ps){
		if (ps==null || ! (ps instanceof Format)) return false;
		return ((PageSizes.Format)ps).getType()==BIG_PAGE;
	}

	public Format getPageSize(ExtendedPageFormat pageFormat){
		MediaSizeName name=pageFormat.getSizeName();
		if (name==null){
			int type=pageFormat.getType();
			for (int i=1;i<sizes.length;i++){
				if (sizesSystemNames[i].getType()==type) return sizes[i];
			}
		}else{
			for (int i=1;i<sizes.length;i++){
				if (name.equals(sizesSystemNames[i].getValue())) return sizes[i];
			}
		}
		return sizes[0];
	}


	public MediaSizeNameModel createComboBoxModel(PrintService printService){
		return new MediaSizeNameModel(sizes,sizesSystemNames,printService);
	}

	public static class MediaSizeNameModel extends AbstractListModel implements ComboBoxModel{
		protected Format[] sizes,sizesSystemNames;
		protected ArrayList<Format> currentSizes;
		protected MediaSizeNameModel(Format[] sizes,Format[] sizesSystemNames,PrintService printService){
			this.sizes=sizes;
			this.sizesSystemNames=sizesSystemNames;
			currentSizes=new ArrayList<Format>(sizes.length);
			update(printService);
		}

		public Format update(PrintService printService){
			Set<MediaSizeName> mediaSizeNames=null;
			if (!(printService instanceof PDFPrintService)){
				Media[] m=(Media[])printService.getSupportedAttributeValues(Media.class,DocFlavor.SERVICE_FORMATTED.PRINTABLE,null);
				mediaSizeNames=new HashSet<MediaSizeName>();
				for (int i=0;i<m.length;i++){
					if (m[i] instanceof MediaSizeName) mediaSizeNames.add((MediaSizeName)m[i]);
				}
			}
			currentSizes.clear();
			boolean lastSelectedItemFound=false;
			MediaSizeName selected=selectedItem==null?null:((Format)selectedItem).getValue();
			for (int i=0;i<sizes.length;i++){
				MediaSizeName m=sizesSystemNames[i].getValue();
				if (m==null&&mediaSizeNames!=null) continue;
				if (mediaSizeNames==null||mediaSizeNames.contains(m)){
					if (m!=null&&m.equals(selected)) lastSelectedItemFound=true;
					currentSizes.add(sizes[i]);
				}
			}

			Format sel=null;
			if (!lastSelectedItemFound){
				sel= selectDefault(printService,true);
			}

			fireContentsChanged(this, 0, currentSizes.size());
			return sel;
		}

		protected Object selectedItem;

		public Object getSelectedItem() {
			return selectedItem;
		}

		public void setSelectedItem(Object selectedItem) {
			this.selectedItem = selectedItem;
		}
		public Format selectDefault(PrintService printService,boolean select){
			MediaSizeName name=ExtendedPageFormat.getDefaultMediaSizeName(printService);
			for(Format f: currentSizes){
				if (f.getValue()==null) continue;
				if (f.getValue().equals(name)){
					if (select) setSelectedItem(f);
					return f;
				}
			}
			return null;

		}



		public Object getElementAt(int index) {
			return currentSizes.get(index);
		}

		public int getSize() {
			return currentSizes.size();
		}

//		public void addListDataListener(ListDataListener l) {
//			// TODO Auto-generated method stub
//
//		}
//
//		public void removeListDataListener(ListDataListener l) {
//			// TODO Auto-generated method stub
//
//		}

	}



//	public static Dimension pageSizeToDimension(Object ps){
//		if (ps==null || ! (ps instanceof PageSizes.Format)) return null;
//		Dimension r=((PageSizes.Format)ps).getDimension();
//		if (r==null) return null;
//		Dimension d=new Dimension();
//		d.setSize(r.getWidth(), r.getHeight());
//		return d;
//	}
//	public static Object dimensionToPageSize(Dimension d){
//		if (d==null) return null;
//		PageSizes.Format[] ps=PageSizes.getPageSizes();
//		PageSizes.Format r=null;
//		for (int i=0;i<ps.length;i++){
//			PageSizes.Format f=ps[i];
//			if (f.getDimension()==null) r=f;
//			else{
//				if (Math.abs(f.width*f.unit-d.getWidth())<1.0&&Math.abs(f.height*f.unit-d.getHeight())<1.0)
//					return f;
//			}
//		}
//		return r;
//	}
//
//	protected static final Format[] sizez={
////		new Format("4A0",1682,2378),
////		new Format("2A0",1189,1682),
//		new Format("A0",841,1189),
//		new Format("A1",594,841),
//		new Format("A2",420,594),
//		new Format("A3",297,420),
//		new Format("A4",210,297),
//		new Format("A5",148,210),
//		new Format("A6",105,148),
//		new Format("A7",74,105),
//		new Format("A8",52,74),
//		new Format("A9",37,52),
//		new Format("A10",26,37),
//		new Format("B0",1000,1414),
//		new Format("B1",707,1000),
//		new Format("B2",500,707),
//		new Format("B3",353,500),
//		new Format("B4",250,353),
//		new Format("B5",176,250),
//		new Format("B6",125,176),
//		new Format("B7",88,125),
//		new Format("B8",62,88),
//		new Format("B9",44,62),
//		new Format("B10",31,44),
//		new Format("Letter",8.5,11,INCH),
//		new Format("Legal",8.5,14,INCH),
//		new Format("Ledger",17,11,INCH),
//		new Format("Tabloid",11,17,INCH),
//		new Format("Executive",7.5,10.5,INCH),
//		new Format("Super-B",13,19,INCH),
//		new Format("Half Letter",5.5,8.5,INCH),
//		new Format("Architectural-A",9,12,INCH),
//		new Format("Architectural-B",12,18,INCH),
//		new Format("Architectural-C",18,24,INCH),
//		new Format("Architectural-D",22.5,36,INCH),
//		new Format("Architectural-E",36,48,INCH),
//		new Format("ANSI-A",8.5,11,INCH),
//		new Format("ANSI-B",11,17,INCH),
//		new Format("ANSI-C",17,22,INCH),
//		new Format("ANSI-D",22,34,INCH),
//		new Format("ANSI-E",34,44,INCH)
//	};
}
