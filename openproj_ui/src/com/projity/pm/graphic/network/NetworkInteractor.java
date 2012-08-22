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
package com.projity.pm.graphic.network;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

import com.projity.field.FieldParseException;
import com.projity.grouping.core.model.NodeModel;
import com.projity.pm.graphic.graph.GraphInteractor;
import com.projity.pm.graphic.graph.GraphUI;
import com.projity.pm.graphic.model.cache.GraphicNode;
import com.projity.pm.graphic.network.rendering.FieldChange;

/**
 *
 */
public class NetworkInteractor extends GraphInteractor{
	private static final long serialVersionUID = 5365103090789265267L;
	protected static final int BAR_SELECTION=4;
	/**
	 *
	 */
	public NetworkInteractor(GraphUI ui) {
		super(ui);
		//popup=new NetworkPopupMenu(this);
	}



    protected void computeNodeSelection(double x,double y){
    	if (((NetworkUI)ui).isOnBarEdge((GraphicNode)selected,x,y))
			state=BAR_MOVE;
		else state=BAR_SELECTION;
    }


    protected Shape getBarShadowBounds(double x,double y){
		//if (state!=BAR_MOVE) return null;
		GraphicNode node=(GraphicNode)selected;
    	GeneralPath shape=getShape(node);
    	return shape.createTransformedShape(AffineTransform.getTranslateInstance(x-x0,y-y0));
    }

    protected GeneralPath getShape(GraphicNode node){
    	return ((NetworkRenderer)((NetworkUI)ui).getGraphRenderer()).getShape(node);
    }

    protected Rectangle2D getLinkSelectionShadowBounds(GraphicNode node){
    	GeneralPath shape=getShape(node);
    	return shape.getBounds2D();
    }



    protected void setLinkOrigin(){
    	GraphicNode node=(GraphicNode)selected;
    	Point2D center= ((NetworkRenderer)((NetworkUI)ui).getGraphRenderer()).getCenter(node);
		x0link=center.getX();
		y0link=center.getY();

    }

    protected boolean switchOnLinkCreation(double x, double y){
    	if (state!=BAR_SELECTION) return false;
		GraphicNode node=(GraphicNode)selected;
		GeneralPath shape=getShape(node);
		if (shape==null) return false;
		return (shape.contains(x,y));
    }

//    public Cursor selectCursor(){
//    	Cursor cursor=null;
//    	switch (state) {
//		case BAR_SELECTION:
//			cursor=getLinkCursor();
//			break;
//		}
//    	if (cursor==null) super.selectCursor();
//    	else getGraph().setCursor(cursor);
//    	return cursor;
//    }

    protected void select(int x,int y){
    	if (selection){
       		selectedZone=ui.getObjectAt(x,y);
    		if (selectedZone!=null) selected=selectedZone.getObject();
    		int savedState=state;
	    	if (selected==null){
	    		state=NOTHING_SELECTED;
	    	}else{
	    		 findState(x,y);
	    	}
	    	if (state!=savedState){
	    		if (savedState==BAR_SELECTION){
	    			NetworkUI nui=(NetworkUI)ui;
	    			List changes=nui.getEditorChange();
	    			GraphicNode node=nui.getEditorNode();
	    			nui.editNode(null);
	    			if (changes!=null) for (Iterator i=changes.iterator();i.hasNext();){
	    				FieldChange change=(FieldChange)i.next();
	    				try {
							nui.getGraph().getCache().getModel().setFieldValue(change.getField(),node.getNode(), this, change.getValue(), null,NodeModel.NORMAL);
						} catch (FieldParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    			}
	    		}
	    		selectCursor();
	    	}
    	}
    }

    public boolean executeAction(double x,double y){
    	if (selected==null) return false;
    	switch (state) {
		case BAR_SELECTION:
			((NetworkUI)ui).editNode((GraphicNode)selected);
			return true;
		case BAR_MOVE:
			 ((NetworkRenderer)((NetworkUI)ui).getGraphRenderer()).translateShape((GraphicNode)selected,x-x0,y-y0);
			return true;

    	}
    	return false;
    }



}
