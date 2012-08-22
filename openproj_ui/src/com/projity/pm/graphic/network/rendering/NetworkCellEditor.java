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
package com.projity.pm.graphic.network.rendering;


import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JComponent;

import com.projity.pm.graphic.graph.GraphParams;
import com.projity.pm.graphic.model.cache.GraphicNode;


public class NetworkCellEditor{
	protected GraphParams params;
	protected JComponent container;
	protected FormatSelector formatSelector;
	protected FormComponent form;
	protected GraphicNode node;
	
	
	public NetworkCellEditor(GraphParams params,JComponent container) {
		this.params=params;
		this.container=container;
		formatSelector=new FormatSelector(params);
	}


	public void initEditorComponent(GraphicNode node,int zoom,Rectangle bounds){
		cancel();
		if (node==null) return;
		//System.out.println("create editor node="+node);
		this.node=node;
		form=formatSelector.getForm(node,zoom,true);
		form.setFields(node.getNode(),params.getCache().getModel());
		
		container.add(form);
		form.setBounds(bounds);
		form.validate();
	}
	
	public void resetForms(){
		formatSelector.resetForms();
	}

	
	public void paintEditor(GraphicNode node){
		if (node==null||this.node!=node) return;
		//System.out.println("paint editor node="+node);
		paintComponentApart(form,form.getBounds());
	}
	protected void paintComponentApart(Component c,Rectangle bounds){
		boolean wasDoubleBuffered = false;
		if ((c instanceof JComponent) && ((JComponent)c).isDoubleBuffered()) {
		    wasDoubleBuffered = true;
		    ((JComponent)c).setDoubleBuffered(false);
		}

		Graphics cg = container.getGraphics().create(bounds.x, bounds.y, bounds.width, bounds.height);
		try {
			c.paint(cg);
		}
		finally {
		    cg.dispose();
		}

		if (wasDoubleBuffered && (c instanceof JComponent)) {
		    ((JComponent)c).setDoubleBuffered(true);
		}
	}
	
	public boolean isEditing(GraphicNode node){
		return node!=null&&this.node==node;
	}
	public void cancel(){
		if (node!=null){
			//System.out.println("cancel editor");
			Container parent=form.getParent();
			if (parent!=null) parent.remove(form);
			Rectangle bounds=form.getBounds();
			form=null;
			node=null;
			container.repaint(bounds);
		}
	}
	
	public List getCellEditorChange() {
		return (form==null)?null:form.getChange();
	}
	
	public GraphicNode getNode() {
		return node;
	}
	
	
	
//	   protected GraphicNode editedNode=null;
//	   public boolean isEditing(GraphicNode node){
//	   	return node==editedNode;
//	   }
//	   public void setEdited(GraphicNode node){
//	   		if (editedNode!=node){
//	   	   		editedNode=node;
//	   	   		Rectangle bounds=getBounds(node);
//	   	   		editor.getEditorComponent(node,bounds);
//	   	   		getGraph().repaint(bounds);
//	   		}
//	   }




	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	/*public void validate() {
	}*/

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	/*public void revalidate() {
	}*/

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	/*public void repaint(long tm, int x, int y, int width, int height) {
	}*/

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	/*public void repaint(Rectangle r) {
	}*/


}
