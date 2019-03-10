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
package com.projectlibre1.graphic.configuration;

import org.apache.commons.digester.Digester;

import com.projectlibre1.configuration.Configuration;

/**
 *
 */
public class GraphicConfiguration {
	protected int columnHeaderHeight;
	protected int printFooterHeight;
	protected int rowHeaderWidth;
	protected int rowChartHeaderWidth;
	protected int rowHeight;
	protected int ganttBarHeight;
	protected int ganttBarYOffset;
	protected int ganttBarAnnotationXOffset;
	protected int ganttBarAnnotationYOffset;
	protected int ganttProgressBarHeight;
	protected int ganttBarMinWidth;
	protected int baselineHeight;
	protected int pertCellWidth;
	protected int pertCellHeight;
	protected int pertXOffset;
	protected int pertYOffset;
	protected int treeCellWidth;
	protected int treeCellHeight;
	protected int treeXOffset;
	protected int treeYOffset;
	protected int collapseLevel;
	protected double selectionSquare;
	protected double networkCellSelectionSquare;
	protected double selectionProgress0;
	protected double selectionProgress1;
	protected double selectionResize0;
	protected double selectionResize1;
	protected double linkFlatness=0;
	
	public static GraphicConfiguration getInstance(){
		return Configuration.getInstance().getGraphicConfiguation();
	}
	
	public static void addDigesterEvents(Digester digester){
		digester.addObjectCreate("*/graphic", "com.projectlibre1.graphic.configuration.GraphicConfiguration");
	    digester.addSetProperties("*/graphic");
		digester.addSetNext("*/graphic", "setGraphicConfiguation", "com.projectlibre1.graphic.configuration.GraphicConfiguration");

	}
	
	
	/**
	 * @return Returns the columnHeaderHeight.
	 */
	public int getColumnHeaderHeight() {
		return columnHeaderHeight;
	}
	/**
	 * @param columnHeaderHeight The columnHeaderHeight to set.
	 */
	public void setColumnHeaderHeight(int columnHeaderHeight) {
		this.columnHeaderHeight = columnHeaderHeight;
	}
    public int getPrintFooterHeight() {
		return printFooterHeight;
	}

	public void setPrintFooterHeight(int printFooterHeight) {
		this.printFooterHeight = printFooterHeight;
	}

	public int getRowHeaderWidth() {
        return rowHeaderWidth;
    }
    public void setRowHeaderWidth(int rowHeaderWidth) {
        this.rowHeaderWidth = rowHeaderWidth;
    }
	/**
	 * @return Returns the rowChartHeaderWidth.
	 */
	public int getRowChartHeaderWidth() {
		return rowChartHeaderWidth;
	}
	/**
	 * @param rowChartHeaderWidth The rowChartHeaderWidth to set.
	 */
	public void setRowChartHeaderWidth(int rowChartHeaderWidth) {
		this.rowChartHeaderWidth = rowChartHeaderWidth;
	}
	/**
	 * @return Returns the rowHeight.
	 */
	public int getRowHeight() {
		return rowHeight;
	}
	/**
	 * @param rowHeight The rowHeight to set.
	 */
	public void setRowHeight(int rowHeight) {
		this.rowHeight = rowHeight;
	}
	/**
	 * @return Returns the ganttBarHeight.
	 */
	public int getGanttBarHeight() {
		return ganttBarHeight;
	}
	/**
	 * @param ganttBarHeight The ganttBarHeight to set.
	 */
	public void setGanttBarHeight(int ganttBarHeight) {
		this.ganttBarHeight = ganttBarHeight;
	}
	/**
	 * @return Returns the ganttProgressBarHeight.
	 */
	public int getGanttProgressBarHeight() {
		return ganttProgressBarHeight;
	}
	/**
	 * @param ganttProgressBarHeight The ganttProgressBarHeight to set.
	 */
	public void setGanttProgressBarHeight(int ganttProgressBarHeight) {
		this.ganttProgressBarHeight = ganttProgressBarHeight;
	}
    /**
     * @return Returns the ganttBarYOffset.
     */
    public int getGanttBarYOffset() {
        return ganttBarYOffset;
    }
    /**
     * @param ganttBarYOffset The ganttBarYOffset to set.
     */
    public void setGanttBarYOffset(int ganttBarYOffset) {
        this.ganttBarYOffset = ganttBarYOffset;
    }
    
	public int getBaselineHeight() {
		return baselineHeight;
	}
	public void setBaselineHeight(int baselineHeight) {
		this.baselineHeight = baselineHeight;
	}
	
	
	
	public int getPertCellHeight() {
		return pertCellHeight;
	}
	public void setPertCellHeight(int pertCellHeight) {
		this.pertCellHeight = pertCellHeight;
	}
	public int getPertCellWidth() {
		return pertCellWidth;
	}
	public void setPertCellWidth(int pertCellWidth) {
		this.pertCellWidth = pertCellWidth;
	}
	public int getPertXOffset() {
		return pertXOffset;
	}
	public void setPertXOffset(int pertXOffset) {
		this.pertXOffset = pertXOffset;
	}
	public int getPertYOffset() {
		return pertYOffset;
	}
	public void setPertYOffset(int pertYOffset) {
		this.pertYOffset = pertYOffset;
	}
	
	
	
	
	public int getTreeCellHeight() {
		return treeCellHeight;
	}
	public void setTreeCellHeight(int treeCellHeight) {
		this.treeCellHeight = treeCellHeight;
	}
	public int getTreeCellWidth() {
		return treeCellWidth;
	}
	public void setTreeCellWidth(int treeCellWidth) {
		this.treeCellWidth = treeCellWidth;
	}
	public int getTreeXOffset() {
		return treeXOffset;
	}
	public void setTreeXOffset(int treeXOffset) {
		this.treeXOffset = treeXOffset;
	}
	public int getTreeYOffset() {
		return treeYOffset;
	}
	public void setTreeYOffset(int treeYOffset) {
		this.treeYOffset = treeYOffset;
	}
	
	
	public int getCollapseLevel() {
		return collapseLevel;
	}
	public void setCollapseLevel(int collapseLevel) {
		this.collapseLevel = collapseLevel;
	}
	
	
	public double getSelectionSquare() {
		return selectionSquare;
	}
	public void setSelectionSquare(double selectionSquare) {
		this.selectionSquare = selectionSquare;
	}
	
	
	public double getLinkFlatness() {
		return linkFlatness;
	}
	public void setLinkFlatness(double linkFlatness) {
		this.linkFlatness = linkFlatness;
	}
	
	
	public double getSelectionProgress0() {
		return selectionProgress0;
	}
	public void setSelectionProgress0(double selectionProgress0) {
		this.selectionProgress0 = selectionProgress0;
	}
	public double getSelectionProgress1() {
		return selectionProgress1;
	}
	public void setSelectionProgress1(double selectionProgress1) {
		this.selectionProgress1 = selectionProgress1;
	}
	public double getSelectionResize0() {
		return selectionResize0;
	}
	public void setSelectionResize0(double selectionResize0) {
		this.selectionResize0 = selectionResize0;
	}
	public double getSelectionResize1() {
		return selectionResize1;
	}
	public void setSelectionResize1(double selectionResize1) {
		this.selectionResize1 = selectionResize1;
	}
	
	
	public double getNetworkCellSelectionSquare() {
		return networkCellSelectionSquare;
	}
	public void setNetworkCellSelectionSquare(double networkCellSelectionSquare) {
		this.networkCellSelectionSquare = networkCellSelectionSquare;
	}

	public int getGanttBarAnnotationXOffset() {
		return ganttBarAnnotationXOffset;
	}

	public void setGanttBarAnnotationXOffset(int ganttBarAnnotationXOffset) {
		this.ganttBarAnnotationXOffset = ganttBarAnnotationXOffset;
	}

	public int getGanttBarAnnotationYOffset() {
		return ganttBarAnnotationYOffset;
	}

	public void setGanttBarAnnotationYOffset(int ganttBarAnnotationYOffset) {
		this.ganttBarAnnotationYOffset = ganttBarAnnotationYOffset;
	}

	public int getGanttBarMinWidth() {
		return ganttBarMinWidth;
	}

	public void setGanttBarMinWidth(int ganttBarMinWidth) {
		this.ganttBarMinWidth = ganttBarMinWidth;
	}

	
}
