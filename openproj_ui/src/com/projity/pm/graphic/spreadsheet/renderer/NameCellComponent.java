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
package com.projity.pm.graphic.spreadsheet.renderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import com.projity.graphic.configuration.CellFormat;
import com.projity.pm.graphic.ChangeAwareTextField;
import com.projity.pm.graphic.IconManager;
import com.projity.pm.graphic.model.cache.GraphicNode;
import com.projity.pm.graphic.spreadsheet.SpreadSheetParams;
import com.projity.pm.graphic.spreadsheet.common.CommonSpreadSheetModel;
/**
 *
 */
public class NameCellComponent extends JPanel {
	protected JComponent textComponent = null;
	protected JLabel iconLabel = null;
	protected Box.Filler filler = null;
	protected ImageIcon collapsedIcon;
	protected ImageIcon expandedIcon;
	protected ImageIcon leafIcon;
	protected ImageIcon emptyLeafIcon;
	protected String text = null;
	protected ImageIcon icon = null;
	protected boolean lazy = false;
	protected boolean fetched = true;
	protected ImageIcon unfetchedLazyIcon = null;
	protected ImageIcon fetchedLazyExpandedIcon = null;
	protected ImageIcon fetchedLazyCollapsedIcon = null;
	protected boolean offline;
	/**
	 *
	 */
	public NameCellComponent() {
		this(new JLabel(""));
	}
	/**
	 * textComponent is a JLabel or a JTextComponent
	 */
	public NameCellComponent(JComponent textComponent) {
		super();
		this.textComponent = textComponent;
		textComponent.setFont(getFont());
	}
	public void init() {
		setBackground(Color.WHITE);
		textComponent.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		if (getComponentCount() != 0)
			removeAll();
		leafIcon = IconManager.getIcon("spreadsheet.leaf.icon");
		emptyLeafIcon = IconManager.getIcon("spreadsheet.emptyleaf.icon");
		collapsedIcon = IconManager.getIcon("spreadsheet.collapsed.icon");
		expandedIcon = IconManager.getIcon("spreadsheet.expanded.icon");

		unfetchedLazyIcon = IconManager.getIcon("spreadsheet.unfetchedLazy.icon");
		fetchedLazyExpandedIcon = IconManager.getIcon("spreadsheet.fetchedLazyExpanded.icon");
		fetchedLazyCollapsedIcon = IconManager.getIcon("spreadsheet.fetchedLazyCollapsed.icon");

		filler = (Box.Filler) Box
				.createHorizontalStrut(leafIcon.getIconWidth());
		add(filler);
		iconLabel = new JLabel(leafIcon);
		iconLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(iconLabel);
		add(textComponent);
	}



	public boolean isOffline() {
		return offline;
	}
	public void setOffline(boolean offline) {
		this.offline = offline;
	}
	/**
	 * @return Returns the text component.
	 */
	public JComponent getTextComponent() {
		return textComponent;
	}
	/**
	 * @return Returns the label.
	 */
	public JLabel getIconLabel() {
		return iconLabel;
	}
	public void setText(String text) {
		if (this.text == text)
			return;
		this.text = text;
		if (textComponent instanceof JLabel)
			((JLabel) textComponent).setText(text);
		else if (textComponent instanceof JTextComponent)
			((JTextComponent) textComponent).setText(text);
	}
	public String getText() {
		if (textComponent instanceof JLabel)
			return ((JLabel) textComponent).getText();
		else if (textComponent instanceof JTextComponent)
			return ((JTextComponent) textComponent).getText();
		else
			return null;
	}
	public void setFont(Font font) {
		super.setFont(font);
		if (textComponent != null)
			textComponent.setFont(font);
	}
	public void setLeaf(boolean empty) {
		ImageIcon askedIcon = (empty) ? emptyLeafIcon : leafIcon;
		if (icon == askedIcon)
			return;
		icon = askedIcon;
		iconLabel.setIcon(icon);
	}

	public void setCollapsed(boolean value) {
		ImageIcon askedIcon = null;
		if (isLazy()) {
			if (isFetched())
				askedIcon = (value) ? fetchedLazyCollapsedIcon : fetchedLazyExpandedIcon;
			else
				askedIcon = unfetchedLazyIcon;
		} else {
			askedIcon =(value) ? collapsedIcon : expandedIcon;
		}
		if (icon == askedIcon)
			return;
		icon = askedIcon;
		iconLabel.setIcon(icon);
	}

	public boolean isLeaf() {
		return (icon == leafIcon);
	}
	public boolean isCollapsed() {
		return (icon == collapsedIcon  || icon == unfetchedLazyIcon || icon == fetchedLazyCollapsedIcon);
	}
	public void setLevel(int level) {
		setLevel(level, false);
	}

	int level=-1;
	public void setLevel(int level,boolean offline) {
		if (this.level==level)return;
		this.level=level;
		if (level == 0)
			return;
		int width = (leafIcon == null) ? 0 : (level - 1)
				* leafIcon.getIconWidth();
		filler.changeShape(new Dimension(width, 0), new Dimension(width, 0),
				new Dimension(width, Short.MAX_VALUE));
		if (offline) invalidate(); //needed for offline, otherwise filler don't change
	}
	public static NameCellComponent getInstance() {
		NameCellComponent instance = new NameCellComponent();
		instance.init();
		return instance;
		//problem in icon position in isOnIcon when reusing the same instance
	}
	public static boolean isOnIcon(Point pos, Dimension cellSize, int level) {
		NameCellComponent reference = getInstance();
		reference.setSize(cellSize);
		reference.setLevel(level);
		reference.doLayout();
		Rectangle bounds = reference.getIconLabel().getBounds();
		bounds.grow(4,4); // be more permissive in clicking on +/-
		return bounds.contains(pos);
	}
	public static boolean isOnText(Point pos, Dimension cellSize, int level) {
		NameCellComponent reference = getInstance();
		reference.setSize(cellSize);
		reference.setLevel(level);
		reference.doLayout();
		return reference.getTextComponent().getBounds().contains(pos);
	}


	protected static NameCellComponent rendererComponent;
	protected static NameCellComponent editorComponent;
	protected static Font savedRendererFont,savedEditorFont;
	protected static NameCellComponent getUninitializedComponent(boolean hasFocus){
		if (hasFocus){
			if (editorComponent==null){
				JComponent textComponent=new JTextField();
				editorComponent=new NameCellComponent(textComponent);
				savedEditorFont=editorComponent.getFont();
				textComponent.setBorder(null);
				editorComponent.init();
			}else editorComponent.setFont(savedEditorFont);
			return editorComponent;
		}else{
			if (rendererComponent==null){
				JComponent textComponent=new JLabel();
				rendererComponent=new NameCellComponent(textComponent);
				savedRendererFont=UIManager.getFont("Table.font");//rendererComponent.getFont();
				textComponent.setBorder(null);
				rendererComponent.init();
			}else rendererComponent.setFont(savedRendererFont);
			return rendererComponent;

		}
	}

	public static NameCellComponent getComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		NameCellComponent component = getUninitializedComponent(hasFocus);
		//JComponent textComponent=component.getTextComponent();

//		CellUtility.setAppearance(table, value, isSelected, hasFocus, row,
//				column, textComponent);
		component.setOffline(false);
		CellUtility.setAppearance(table, value, isSelected, hasFocus, row,
				column, component);
		CommonSpreadSheetModel model = (CommonSpreadSheetModel) table.getModel();
		GraphicNode node = model.getNode(row);
		component.setText(value == null ? "" : value.toString());
		int level=model.getCache().getLevel(node);
		component.setLevel((node.isVoid())?(level+1):level);
		component.setLazy(node.isLazyParent());
		component.setFetched(node.isFetched());
		if (model.getCellProperties(node).isCompositeIcon()) {
			component.setCollapsed(node.isCollapsed());
		} else {
			component.setLeaf(node.isVoid());
		}
		FontManager.setComponentFont(model.getCellProperties(node),component);
		component.doLayout();
		return component;
	}

	public static Component getComponent(Object value, GraphicNode node, SpreadSheetParams params){
		NameCellComponent component = getUninitializedComponent(false);
		CellFormat format=params.getFieldArray().getCellStyle().getCellFormat(node);
		component.getTextComponent().setBorder(null);
		component.setOffline(true);
		CellUtility.setAppearance(format,component);
		String valueS=value == null? " " : value.toString();//to avoid void textComponents with no height
		if (valueS.length()==0) valueS=" ";
		component.setText(valueS);
		int level=params.getCache().getLevel(node);
		component.setLevel((node.isVoid())?(level+1):level,true);
		component.setLazy(node.isLazyParent());
		component.setFetched(node.isFetched());
		if (format.isCompositeIcon()) {
			component.setCollapsed(node.isCollapsed());
		} else {
			component.setLeaf(node.isVoid());
		}
		FontManager.setComponentFont(format,component);
		return component;
	}



	public void doLayout() {
		super.doLayout();
		if (offline){
			textComponent.setSize(getWidth()-textComponent.getX(), textComponent.getHeight());
		}
	}
	/* (non-Javadoc)
	 * @see java.awt.Component#setBackground(java.awt.Color)
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if (textComponent!=null)textComponent.setBackground(bg);
	}
	/* (non-Javadoc)
	 * @see java.awt.Component#setForeground(java.awt.Color)
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);
		if (textComponent!=null)textComponent.setForeground(fg);
	}




	public void requestFocus() {
		if (textComponent instanceof ChangeAwareTextField) {
			textComponent.setVisible(true);
			textComponent.setEnabled(true);
			textComponent.setFocusable(true);
			textComponent.requestFocus();
			System.out.println("delegating focus request to text component");
		}
	}
	/*public boolean requestFocus(boolean temporary) {
		return textComponent.requestFocus(temporary);
	}
	public boolean requestFocusInWindow() {
		return textComponent.requestFocusInWindow();
	}*/
	public final boolean isLazy() {
		return lazy;
	}
	public final void setLazy(boolean lazy) {
		this.lazy = lazy;
	}
	public final boolean isFetched() {
		return fetched;
	}
	public final void setFetched(boolean fetched) {
		this.fetched = fetched;
	}


}