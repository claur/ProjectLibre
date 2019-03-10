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
package com.projectlibre1.pm.graphic.network.rendering;


import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JComponent;

import com.projectlibre1.pm.graphic.graph.GraphParams;
import com.projectlibre1.pm.graphic.model.cache.GraphicNode;


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
