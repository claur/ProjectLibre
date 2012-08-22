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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.configuration.Configuration;
import com.projity.field.Field;
import com.projity.field.FieldConverter;
import com.projity.graphic.configuration.BarFormat;
import com.projity.graphic.configuration.FormBox;
import com.projity.graphic.configuration.FormBoxLayout;
import com.projity.graphic.configuration.FormFormat;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.model.NodeModel;
import com.projity.pm.graphic.ChangeAwareComponent;
import com.projity.pm.graphic.ChangeAwareTextField;
import com.projity.strings.Messages;
import com.projity.util.Alert;

public class FormComponent extends JPanel{
	protected int maxRows=3;
	protected int maxCols=2;
	protected Map fieldComponents;
	protected List selectedFormats;
	protected boolean editor;
	protected int zoom;
	protected boolean texture=true;
	
	
	public FormComponent(List selectedFormats,int zoom,boolean editor,boolean texture){
		super();
		fieldComponents=new HashMap();
		this.selectedFormats=selectedFormats;
		this.editor=editor;
		this.zoom=zoom;
		this.texture=texture;
		BarFormat format;
		if (selectedFormats==null||selectedFormats.size()==0) format=null;
		else format=(BarFormat)selectedFormats.get(0);
		init(format);
		setOpaque(false);
		setForeground(Color.BLACK);
		setBackground(Color.WHITE);
	}
	
	
	public boolean isEditor() {
		return editor;
	}
	public void setEditor(boolean editor) {
		this.editor = editor;
	}
	
	public void init(BarFormat format) {
		if (format==null) return;
		FormFormat form=format.getForm();
		if (form==null) return;
		List boxes=form.getBoxes();
		if (boxes==null||boxes.size()==0){
			return;
		}
		FormBoxLayout formBoxLayout=form.getLayout(zoom);
		FormLayout layout = new FormLayout(
				formBoxLayout.getColumnGrid(),
				formBoxLayout.getRowGrid());
		DefaultFormBuilder builder = new DefaultFormBuilder(this,layout);
		if (formBoxLayout.getBorder()==null) builder.setDefaultDialogBorder();
		else builder.setBorder(Borders.createEmptyBorder(formBoxLayout.getBorder()));
		CellConstraints cc = new CellConstraints();
		for (Iterator i=boxes.iterator();i.hasNext();){
			FormBox box=(FormBox)i.next();
			if (zoom<box.getMinZoom()) return;
			JComponent component;
			if(box.getFieldId()==null) component=new JLabel(Messages.getString(box.getTextId()));
			else{
				if (editor&&!box.getField().isReadOnly()){
					component=new ChangeAwareTextField();
					component.setBorder(null);
					//component.setOpaque(false);
				}else component=new JLabel();
				
				//if (box.getRow()==1&&!editor) ((JLabel)component).setHorizontalAlignment(SwingConstants.CENTER);
				//bug workaround, not possible to center with classic method when rowSpan>1
				
				fieldComponents.put(box.getFieldId(),component);
			}
			Font font=formBoxLayout.getFont(box.getFont());
			if (font!=null) component.setFont(font);
			builder.add(component,(box.getAlignment()==null)?
					cc.xywh(box.getColumn(),box.getRow(),box.getColumnSpan(),box.getRowSpan()):
					cc.xywh(box.getColumn(),box.getRow(),box.getColumnSpan(),box.getRowSpan(),box.getAlignment()));
		}
	}
	
	
	public Component getComponent(String fieldId){
		return (Component)fieldComponents.get(fieldId);
	}
	
	
	
	public void setFields(Node node,NodeModel model){
		for (Iterator i=fieldComponents.keySet().iterator();i.hasNext();){
			String fieldId=(String)i.next();
			Field field=Configuration.getFieldFromId(fieldId);
			Object value=field.getValue(node,model,null);
			
			String stringValue="";
			if (value != null)
				stringValue = FieldConverter.toString(value);
			Object textComp=fieldComponents.get(fieldId);
			if (textComp instanceof JLabel)
				((JLabel)textComp).setText(stringValue);
			else{
				((ChangeAwareTextField)textComp).setText(stringValue);
				((ChangeAwareComponent)textComp).resetChange();
			}
		}
	}
	
	
	public List getChange(){
		ArrayList change=new ArrayList();
		for (Iterator i=fieldComponents.keySet().iterator();i.hasNext();){
			String fieldId=(String)i.next();
			Object component=fieldComponents.get(fieldId);
			if (component instanceof ChangeAwareComponent&&
					((ChangeAwareComponent)component).hasChanged()){
				String stringValue;
				if (component instanceof JTextField)
					stringValue=((JTextField)component).getText();
				//hangle other components here
				else continue;
				
				Field field=Configuration.getFieldFromId(fieldId);
				try {
					Object value=FieldConverter.fromString(stringValue,field.getDisplayType());
					change.add(new FieldChange(field,value));
				} catch (Exception e) {
					Alert.error(e.getMessage());
				}
			}
		}
		return change;
	}
	
	

	void paintSelectedBars(Graphics2D g2, double width, double height){
		for (Iterator i=selectedFormats.iterator();i.hasNext();){
			BarFormat format = (BarFormat)i.next();
			if (format.getMiddle()!=null) format.getMiddle().draw(g2,
					width,
					height,
					0,
					+height/2,
					texture);
			if (format.getStart()!=null) format.getStart().draw(g2,
					width,
					height,
					0,
					+height/2,
					texture);
			if (format.getEnd()!=null) format.getEnd().draw(g2,
					width,
					height,
					0,
					+height/2,
					texture);
		}
		
	}
	
	
	
	public void paint(Graphics g) {
		/*CommonGraphCell cell=(CommonGraphCell)view.getCell();
		if (cell.getNode().isVoid()) return;*/
		
		Graphics2D g2=(Graphics2D)g;
		Dimension d=getSize();
		double w=d.getWidth();
		double h=d.getHeight();
		
		
		paintSelectedBars(g2,w-1,h-1);
//		ImageIcon link=IconManager.getIcon("common.link.image");
//		g2.drawImage(link.getImage(),(int)(w-link.getIconWidth()),(int)(h-link.getIconHeight()),this);
		//x=w and y=h are outside
		
		try {
			//if (preview && !isDoubleBuffered)
			//	setOpaque(false);
			super.paint(g);
			//paintSelectionBorder(g);
		} catch (IllegalArgumentException e) {
			// JDK Bug: Zero length string passed to TextLayout constructor
		}
	}
	
	
	
	
	
	
	
	

	/**
	 * Provided for subclassers to paint a selection border.
	 */
	/*protected void paintSelectionBorder(Graphics g) {
		((Graphics2D) g).setStroke(GraphConstants.SELECTION_STROKE);
		if (childrenSelected)
			g.setColor(graph.getGridColor());
		else if (hasFocus && selected)
			g.setColor(graph.getLockedHandleColor());
		else if (selected)
			g.setColor(graph.getHighlightColor());
		if (childrenSelected || selected) {
			Dimension d = getSize();
			g.drawRect(0, 0, d.width - 1, d.height - 1);
		}
	}*/
	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	protected void firePropertyChange(
		String propertyName,
		Object oldValue,
		Object newValue) {
		if (propertyName == "text")
			super.firePropertyChange(propertyName, oldValue, newValue);
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		byte oldValue,
		byte newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		char oldValue,
		char newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		short oldValue,
		short newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		int oldValue,
		int newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		long oldValue,
		long newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		float oldValue,
		float newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		double oldValue,
		double newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		boolean oldValue,
		boolean newValue) {
	}


	
	

}
