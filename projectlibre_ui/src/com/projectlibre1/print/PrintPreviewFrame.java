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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.MissingListenerException;

import com.projectlibre1.contrib.ClassLoaderUtils;
import com.projectlibre1.help.HelpUtil;
import com.projectlibre1.menu.MenuActionConstants;
import com.projectlibre1.menu.MenuActionsMap;
import com.projectlibre1.menu.MenuManager;
import com.projectlibre1.pm.graphic.IconManager;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.util.Alert;


public class PrintPreviewFrame extends JFrame implements  ActionMap, MenuActionConstants{

   private PagePanel pagePanel;
   private JScrollPane pageScrollPane;

   protected GraphPageable document;
   private int pageIndex = 0;
   private double zoom;

    public PrintPreviewFrame (GraphPageable document) {
       super ();
      this.document = document;
      zoom=1.0;
      init ();
      HelpUtil.addDocHelp(this,"Print_Preview");
   }

    public double getZoomX(){
    	return zoom;
    }
    public double getZoomY(){
    	return zoom;
    }

	public void correctPageIndex() {
		int pageCount=document.getNumberOfPages();
		if (pageIndex==0) return;
		if (pageIndex>=pageCount) pageIndex=pageCount-1;
	}

   protected MenuManager menuManager;
	public void init() {
		//setSize(800,600);
		setExtendedState(getExtendedState()|MAXIMIZED_BOTH);
		setTitle(Messages.getString("PrintPreviewFrame.PrintPreview")); //$NON-NLS-1$
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (pageSetup!=null) pageSetup.saveSettings(true,false);
			}
		});

		menuManager = MenuManager.getInstance(this);
		JToolBar toolBar = menuManager.getToolBar(MenuManager.PRINT_PREVIEW_TOOL_BAR);
        getContentPane().add(toolBar, BorderLayout.BEFORE_FIRST_LINE);
         actionsMap.setEnabledDocumentMenuActions(true);

         pagePanel=new PagePanel();
         //pagePanel.setPreferredSize(new Dimension((int)Math.floor(document.getPageFormat().getWidth()),(int)Math.floor(document.getPageFormat().getHeight())));
         updateSize();
         pageScrollPane=new JScrollPane(pagePanel,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
         getContentPane().add(pageScrollPane,BorderLayout.CENTER);

         addFormatPanel();
	}


   protected MenuActionsMap actionsMap;
	public void addHandlers() {
		actionsMap = new MenuActionsMap(this,menuManager);
		actionsMap.addHandler(ACTION_PRINTPREVIEW_PRINT, new PrintAction());
		actionsMap.addHandler(ACTION_PRINTPREVIEW_PDF, new PDFAction());
		actionsMap.addHandler(ACTION_PRINTPREVIEW_FORMAT, new FormatAction());
		actionsMap.addHandler(ACTION_PRINTPREVIEW_BACK, new BackAction());
		actionsMap.addHandler(ACTION_PRINTPREVIEW_FORWARD, new ForwardAction());
		actionsMap.addHandler(ACTION_PRINTPREVIEW_UP, new UpAction());
		actionsMap.addHandler(ACTION_PRINTPREVIEW_DOWN, new DownAction());
		actionsMap.addHandler(ACTION_PRINTPREVIEW_FIRST, new FirstAction());
		actionsMap.addHandler(ACTION_PRINTPREVIEW_LAST, new LastAction());
		actionsMap.addHandler(ACTION_PRINTPREVIEW_ZOOMIN, new ZoomInAction());
		actionsMap.addHandler(ACTION_PRINTPREVIEW_ZOOMOUT, new ZoomOutAction());
		actionsMap.addHandler(ACTION_PRINTPREVIEW_ZOOMRESET, new ZoomResetAction());
		actionsMap.addHandler(ACTION_PRINTPREVIEW_LEFT_VIEW, new LeftViewAction());
		actionsMap.addHandler(ACTION_PRINTPREVIEW_RIGHT_VIEW, new RightViewAction());
	}
	public Action getAction(String key) throws MissingListenerException {
		if (actionsMap == null)
			addHandlers();

		Action action = actionsMap.getConcreteAction(key);
		if (action == null)
			throw new MissingListenerException("no listener for PrintPreviewFrame", getClass().getName(),key); //$NON-NLS-1$

		return action;
	}
	public String getStringFromAction(Action action) throws MissingListenerException {
		if (actionsMap == null)
			addHandlers();
		return actionsMap.getStringFromAction(action);
	}

	public class PrintAction extends MenuActionsMap.GlobalMenuAction {
		public void actionPerformed(ActionEvent arg0) {
			if (document.getPrintService() instanceof PDFPrintService){
				Alert.error(Messages.getString("PageSetupDialog.NotValidPrinter"),PrintPreviewFrame.this);
				return;
			}
			document.print();
		}
	}
	public class PDFAction extends MenuActionsMap.GlobalMenuAction {
		public void actionPerformed(ActionEvent arg0) {
//claur - PDF enabled
//			if (Environment.isProjectLibre()) {
//				PODOnlyFeature.doDialog(PrintPreviewFrame.this);
//				return;
//			}
			try {
				Class generator=ClassLoaderUtils.forName("org.projectlibre.export.ImageExport");
				generator.getMethod("export", new Class[]{GraphPageable.class,Component.class}).invoke(null,new Object[]{document,PrintPreviewFrame.this});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public class FormatAction extends MenuActionsMap.GlobalMenuAction {
		public void actionPerformed(ActionEvent arg0) {
			toggleFormatPanel();
		}
	}

	protected PageSetup pageSetup;
	protected JComponent pageSetupComponent;
	public void toggleFormatPanel(){
		if (pageSetupComponent==null) return;
		boolean visible=!pageSetupComponent.isVisible();
		pageSetupComponent.setVisible(visible);
//		ArrayList buttons=menuManager.getToolBarFactory().getButtonsFromId("PrintPreviewFormat");
//		if (buttons!=null&&buttons.size()==1){
//			JButton b=(JButton)buttons.get(0);
//			b.setIcon(IconManager.getIcon("print.format"));
//		}
	}
	public void addFormatPanel(){
		if (pageSetup==null){
			pageSetup=PageSetup.getInstance((GraphPageable)document);
			pageSetupComponent=new JScrollPane(pageSetup.createContentPanel(true));
			pageSetupComponent.setFont(pageSetupComponent.getFont().deriveFont(8));
			getContentPane().add(pageSetupComponent,BorderLayout.EAST);
		}

	}
//	public class FormatAction extends MenuActionsMap.GlobalMenuAction {
//		public void actionPerformed(ActionEvent arg0) {
//			if (pageSetup==null){
//				pageSetup=PageSetup.getInstance(document);
//				formatDialog = PageSetupDialog.getInstance(PrintPreviewFrame.this,pageSetup);
//				formatDialog.pack();
//				//setLocationRelativeTo(PrintPreviewFrame.this);
//				formatDialog.setModal(false);
//				formatDialog.setVisible(true);
//			}else{
//				formatDialog.setVisible(true);
//			}
//		}
//	}
	public class BackAction extends MenuActionsMap.GlobalMenuAction {
		public void actionPerformed(ActionEvent arg0) {
			previousPage();
		}
	}
	public class ForwardAction extends MenuActionsMap.GlobalMenuAction {
		public void actionPerformed(ActionEvent arg0) {
			nextPage();
		}
	}
	public class UpAction extends MenuActionsMap.GlobalMenuAction {
		public void actionPerformed(ActionEvent arg0) {
			upperPage();
		}
	}
	public class DownAction extends MenuActionsMap.GlobalMenuAction {
		public void actionPerformed(ActionEvent arg0) {
			lowerPage();
		}
	}
	public class FirstAction extends MenuActionsMap.GlobalMenuAction {
		public void actionPerformed(ActionEvent arg0) {
			firstPage();
		}
	}
	public class LastAction extends MenuActionsMap.GlobalMenuAction {
		public void actionPerformed(ActionEvent arg0) {
			lastPage();
		}
	}
	public class ZoomInAction extends MenuActionsMap.GlobalMenuAction {
		public void actionPerformed(ActionEvent arg0) {
			zoomIn();
		}
	}
	public class ZoomResetAction extends MenuActionsMap.GlobalMenuAction {
		public void actionPerformed(ActionEvent arg0) {
			zoomReset();
		}
	}
	public class ZoomOutAction extends MenuActionsMap.GlobalMenuAction {
		public void actionPerformed(ActionEvent arg0) {
			zoomOut();
		}
	}
	public class LeftViewAction extends MenuActionsMap.GlobalMenuAction {
		public void actionPerformed(ActionEvent arg0) {
			toggleLeftView();
		}
		protected boolean needsDocument() {return true;} //otherwise allowed won't be checked
		protected boolean allowed(boolean enable){
			if (!(document instanceof GraphPageable)) return false;
			GraphPageable gp=(GraphPageable)document;
			return gp.getRenderer().getParams().isSupportLeftAndRightParts();
		}
	}
	public class RightViewAction extends MenuActionsMap.GlobalMenuAction {
		public void actionPerformed(ActionEvent arg0) {
			toggleRightView();
		}
		protected boolean needsDocument() {return true;} //otherwise allowed won't be checked
		protected boolean allowed(boolean enable){
			if (!(document instanceof GraphPageable)) return false;
			GraphPageable gp=(GraphPageable)document;
			return gp.getRenderer().getParams().isSupportLeftAndRightParts();
		}
	}




//	public void setZoomOut (double zoom) {
//		this.zoom = zoom;
//		Dimension2D size=pagePanel.getSize ();
//		pagePanel.setSize((int)(size.getWidth() * zoom),(int)(size.getHeight() * zoom));
//		//TODO loose of precision due to (int)
//	}


	public double getZoom () {
		return zoom;
	}


	public void nextPage () {
		 correctPageIndex();
		if (pageIndex < document.getNumberOfPages() - 1){
			pageIndex++;
			updatePanel();
		}
	}
	public void previousPage () {
		 correctPageIndex();
		if (pageIndex > 0){
			pageIndex--;
			updatePanel();
		}
	}
	public void lowerPage () {
		 correctPageIndex();
		if (pageIndex < document.getNumberOfPages() - document.getColumnCount()){
			pageIndex+=document.getColumnCount();
			updatePanel();
		}
	}
	public void upperPage () {
		 correctPageIndex();
		if (pageIndex >= document.getColumnCount()){
			pageIndex-=document.getColumnCount();
			updatePanel();
		}
	}
	public void firstPage () {
		 correctPageIndex();
		if (pageIndex!=0){
			pageIndex = 0;
			updatePanel();
		}
	}
	public void lastPage () {
		 correctPageIndex();
		if (pageIndex!=document.getNumberOfPages()  - 1){
			pageIndex = document.getNumberOfPages()  - 1;
			updatePanel();
		}
	}
	public void print () {
		document.print ();
	}

	protected void update () {
		document.update();
		updatePanel();
	}
	protected void updatePanel () {
		updateSize();
		pagePanel.repaint();
	}
	protected void updateSize () {
		ExtendedPageFormat p=document.getPageFormat();
		Dimension d=new Dimension();
		PageSize size=p.getSize();
		if (p.getOrientation() == PageFormat.PORTRAIT){
			d.setSize(Math.round(size.getX(PageSize.INCH)*PageSize.POINTS_PER_INCH)*zoom,Math.round(size.getY(PageSize.INCH)*PageSize.POINTS_PER_INCH)*zoom);
		}else d.setSize(Math.round(size.getY(PageSize.INCH)*PageSize.POINTS_PER_INCH)*zoom,Math.round(size.getX(PageSize.INCH)*PageSize.POINTS_PER_INCH)*zoom);
		pagePanel.setPreferredSize(d);
		pagePanel.revalidate();
		//System.out.println("d="+d);
	}

	protected void zoomIn() {
		zoom*=1.25;
		updatePanel();
	}
	protected void zoomOut() {
		zoom/=1.25;
		updatePanel();
	}
	protected void zoomReset() {
		zoom=1.0;
		updatePanel();
	}

	protected void toggleLeftView(){
		if (document instanceof GraphPageable){
			GraphPageable gp=(GraphPageable)document;
			boolean visible=gp.getRenderer().getParams().isLeftPartVisible();
			gp.getRenderer().getParams().setLeftPartVisible(!visible);
			if (visible) gp.getRenderer().getParams().setRightPartVisible(true);
			pageIndex=0;
			updateButtons();
			update();
		}
	}
	protected void toggleRightView(){
		if (document instanceof GraphPageable){
			GraphPageable gp=(GraphPageable)document;
			boolean visible=gp.getRenderer().getParams().isRightPartVisible();
			gp.getRenderer().getParams().setRightPartVisible(!visible);
			if (visible) gp.getRenderer().getParams().setLeftPartVisible(true);
			pageIndex=0;
			updateButtons();
			update();
		}
	}

	protected void updateButtons(){
		if (document instanceof GraphPageable){
			GraphPageable gp=(GraphPageable)document;

			boolean visible=gp.getRenderer().getParams().isLeftPartVisible();
			ArrayList buttons=menuManager.getToolBarFactory().getButtonsFromId("PrintPreviewLeftView");
			if (buttons!=null&&buttons.size()==1){
				JButton b=(JButton)buttons.get(0);
				b.setIcon(IconManager.getIcon(visible?"print.leftView":"print.leftViewHidden"));
			}
			menuManager.setActionSelected(ACTION_PRINTPREVIEW_LEFT_VIEW,visible);

			visible=gp.getRenderer().getParams().isRightPartVisible();
			buttons=menuManager.getToolBarFactory().getButtonsFromId("PrintPreviewRightView");
			if (buttons!=null&&buttons.size()==1){
				JButton b=(JButton)buttons.get(0);
				b.setIcon(IconManager.getIcon(visible?"print.rightView":"print.rightViewHidden"));
			}
			menuManager.setActionSelected(ACTION_PRINTPREVIEW_RIGHT_VIEW,visible);
		}
	}







public PagePanel getPagePanel() {
	return pagePanel;
}
	public class PagePanel extends JPanel implements MouseListener,MouseMotionListener{
		protected AffineTransform transform=null;
		protected double w,h;

		public PagePanel(){
			super();
			document.update();
			addMouseListener(this);
			addMouseMotionListener(this);
		}

		public AffineTransform updateTransform(){
			Dimension size = getSize ();
			double pw=size.getWidth();
			double ph=size.getHeight();
			double zX=getZoomX();
			double zY=getZoomY();
			ExtendedPageFormat pageFormat=document.getPageFormat();
			w=pageFormat.getWidth();
			h=pageFormat.getHeight();
			double w_=w*zX;
			double h_=h*zY;
			double dw=pw>w_?(pw-w_)/2:0.0;
			double dh=ph>h_?(ph-h_)/2:0.0;
			transform=new AffineTransform(zX,0.0,0.0,zY,dw,dh);
			return transform;
		}

		public void paint (Graphics g) {

			super.paint (g);

			setBackground(Color.GRAY);

			Dimension size = getSize ();
			double pw=size.getWidth();
			double ph=size.getHeight();



			BufferedImage doubleBuffer = new BufferedImage (size.width, size.height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = (Graphics2D) doubleBuffer.getGraphics ();

			AffineTransform svgTransform=g2.getTransform();
			Color svgColor=g2.getColor();
			//Stroke svgStroke=g2.getStroke();

			g2.setColor(Color.GRAY);
			g2.fill(new Rectangle2D.Double(0.0,0.0, pw,ph));

			g2.transform(updateTransform());
			g2.setColor (Color.WHITE);

			g2.fill (new Rectangle2D.Double(0.0,0.0,w,h));

			g2.setColor(svgColor);
			//g2.setStroke(svgStroke);

			try {
				 correctPageIndex();
				document.printWithDefault( g2, pageIndex);
			} catch (PrinterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			g2.setTransform(svgTransform);
			if (doubleBuffer != null)
				g.drawImage (doubleBuffer, 0, 0, this);
		}

		private double fx,fy;
		public void mousePressed(MouseEvent e) {
	    	Rectangle visibleRect=getVisibleRect();
			fx=e.getX()-visibleRect.getX();
			fy=e.getY()-visibleRect.getY();
	    }
		public void mouseClicked(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}


		public void mouseDragged(MouseEvent e){
			Rectangle visibleRect=getVisibleRect();
			double x=e.getX()-visibleRect.getX();
			double y=e.getY()-visibleRect.getY();
			double ox=visibleRect.getX()-x+fx;
			double oy=visibleRect.getY()-y+fy;
			fx=x;
			fy=y;
			Dimension size=getSize();
			if (ox<0.0) ox=0.0;
			else if (ox+visibleRect.getWidth()>size.getWidth()) ox=size.getWidth()-visibleRect.getWidth();
			if (oy<0.0) oy=0.0;
			else if (oy+visibleRect.getHeight()>size.getHeight()) oy=size.getHeight()-visibleRect.getHeight();
			visibleRect.setFrame(ox,oy,visibleRect.getWidth(),visibleRect.getHeight());
			scrollRectToVisible(visibleRect);

		}
	    private Point point=new Point();
	    private Cursor defaultCursor=null;
	    public void mouseMoved(MouseEvent e){
	    	if (transform==null) return;
	    	try {
				transform.inverseTransform(e.getPoint(),point);
				if (point.getX()>=0.0&&point.getX()<=w&&point.getY()>=0.0&&point.getY()<=h){
					if (defaultCursor==null){
						defaultCursor=getCursor();
						setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					}
				}else{
					if (defaultCursor!=null){
						setCursor(defaultCursor);
						defaultCursor=null;
					}
				}
			} catch (NoninvertibleTransformException e1) {}
	    }
	}
}



















