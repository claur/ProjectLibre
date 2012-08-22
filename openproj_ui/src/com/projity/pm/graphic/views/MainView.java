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
package com.projity.pm.graphic.views;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JViewport;

import com.projity.pm.graphic.timescale.ScaledComponent;
import com.projity.pm.graphic.views.synchro.ScrollPaneSynchronizer;
import com.projity.pm.graphic.views.synchro.Synchronizer;
import com.projity.timescale.TimeScaleEvent;
import com.projity.timescale.TimeScaleListener;
import com.projity.workspace.SavableToWorkspace;
import com.projity.workspace.WorkspaceSetting;

/**
 *  
 */
public class MainView extends JSplitPane implements TimeScaleListener, SavableToWorkspace{
	private static final long serialVersionUID = -6427979080094712783L;

	protected int defaultDividerSize;

    protected double defaultDividerLocation=0.7;
    private Synchronizer synchronizer;
    /**
     *  
     */
    public MainView() {
        super(JSplitPane.VERTICAL_SPLIT);
        setOneTouchExpandable(true);
        defaultDividerSize = getDividerSize();
        setDividerSize(0);
        
    }
    public void setTop(Component top) {
    	if (top==null) return;
    	
		Component bottom = getBottomComponent();
		if (bottom != null) {
			setDividerSize(defaultDividerSize);
			setDividerLocation(defaultDividerLocation);
		}
		if (top instanceof SplittedView)
			((SplittedView) top).setParentView(this);
		setTopComponent(top);

		if (viewsSynchronizable()) {
		    if (bottom==null)
		        ((SplittedView) top).setDividerLocation(((SplittedView) bottom).getDividerLocation());
		    else ((SplittedView) top).setDividerLocationSilent(((SplittedView) bottom)
					.getDividerLocation()+((SplittedView) bottom).getDeltaDivider()-((SplittedView) top).getDeltaDivider()); //bottom not initialized yet, no sync
			addScaledComponentsSynchro();
		}
	}

    public void setBottom(Component bottom) {
    	if (bottom==null) return;
    	
		Component top = getTopComponent();
		if (top != null) {
			setDividerSize(defaultDividerSize);
			setDividerLocation(defaultDividerLocation);
		}
		if (bottom instanceof SplittedView)
			((SplittedView) bottom).setParentView(this);
		setBottomComponent(bottom);

		if (viewsSynchronizable()) {
			((SplittedView) bottom).setDividerLocationSilent(((SplittedView) top)
					.getDividerLocation()+((SplittedView) top).getDeltaDivider()-((SplittedView) bottom).getDeltaDivider()); //bottom not initialized yet, no sync
			addScaledComponentsSynchro();
		}
    }
    
    

    public void removeTop() {
    	removeScaledComponentsSynchro();
        setTopComponent(null);
        setDividerSize(0);
    }

    public void removeBottom() {
    	removeScaledComponentsSynchro();
        setBottomComponent(null);
        setDividerSize(0);
    }
    
    public boolean viewsSynchronizable(){
    	Component top=getTopComponent();
    	Component bottom=getBottomComponent();
    	return (top!=null&&bottom!=null&&(top instanceof SplittedView)&&(bottom instanceof SplittedView));
    }
    
    public void addScaledComponentsSynchro(){
    	if (viewsSynchronizable()){
    		SplittedView top=(SplittedView)getTopComponent();
    		SplittedView bottom=(SplittedView)getBottomComponent();
    		JViewport bottomViewport=bottom.rightScrollPane.getViewport();
    		JViewport topViewport=top.rightScrollPane.getViewport();
    		JComponent bottomComponent=(JComponent)bottomViewport.getComponent(0);
    		JComponent topComponent=(JComponent)topViewport.getComponent(0);
    		adjustSizes();
    		((ScaledComponent)topComponent).getCoord().addTimeScaleListener(this); // listener removed in DocumentFrame
    		synchronizer.addSynchro(top.getRightScrollPane(), bottom.getRightScrollPane(),
    				ScrollPaneSynchronizer.VERTICAL,bottom.isNeedVoidBar(),false);
    	}
    }
    
    public void removeScaledComponentsSynchro(){
    	if (viewsSynchronizable()){
    		SplittedView top=(SplittedView)getTopComponent();
    		SplittedView bottom=(SplittedView)getBottomComponent();
   // 		JViewport bottomViewport=bottom.rightScrollPane.getViewport();
    		JViewport topViewport=top.rightScrollPane.getViewport();
    //		JComponent bottomComponent=(JComponent)bottomViewport.getComponent(0);
    		JComponent topComponent=(JComponent)topViewport.getComponent(0);
    		((ScaledComponent)topComponent).getCoord().removeTimeScaleListener(this);
    		synchronizer.removeSynchro(top.getRightScrollPane(), bottom == null ? null : bottom.getRightScrollPane(),
    				ScrollPaneSynchronizer.VERTICAL);
    	}
    }
    
    
    
    public void setChildrenDividerLocation(Object source,int pos){
    	SplittedView top=null;
    	SplittedView bottom=null;
         Component c=getBottomComponent();
        if (c!=null&&(c instanceof SplittedView)) bottom=(SplittedView)c;
        c=getTopComponent();
        if (c!=null&&(c instanceof SplittedView)) top=(SplittedView)c;
        if (top==null||bottom==null) return;
        if (bottom.getDeltaDivider()<top.getDeltaDivider()){
        	SplittedView tmp=bottom;
        	bottom=top;
        	top=tmp;
        }
        int delta=bottom.getDeltaDivider()-top.getDeltaDivider();
        
        if (source==top&&bottom!=null){
    		int min=top.getMinimumDividerLocation();
    		int max=top.getMaximumDividerLocation();
    		if (pos>=max){
    			top.setDividerLocationSilent(Integer.MAX_VALUE);
    			bottom.setDividerLocationSilent(Integer.MAX_VALUE);
    		}
    		else if (pos<=min+delta){
    			top.setDividerLocationSilent(1+delta);
    			bottom.setDividerLocationSilent(1);
    		}
        	else bottom.setDividerLocationSilent(pos-delta);
        }
        if (source==bottom&&top!=null){
    		int min=bottom.getMinimumDividerLocation();
    		int max=bottom.getMaximumDividerLocation();
    		if (pos>=max-delta){
    			top.setDividerLocationSilent(Integer.MAX_VALUE);
    			bottom.setDividerLocationSilent(Integer.MAX_VALUE);
    		}
    		else if (pos<=min){
    			top.setDividerLocationSilent(1+delta);
    			bottom.setDividerLocationSilent(1);
    		}
        	else top.setDividerLocationSilent(pos+delta);
        }
    }
    
    
    
    public void adjustSizes(){
		SplittedView top=(SplittedView)getTopComponent();
		SplittedView bottom=(SplittedView)getBottomComponent();
		JViewport bottomViewport=bottom.rightScrollPane.getViewport();
		JViewport topViewport=top.rightScrollPane.getViewport();
		JComponent bottomComponent=(JComponent)bottomViewport.getView();
		JComponent topComponent=(JComponent)topViewport.getView();
		
		Dimension dtop=topComponent.getPreferredSize();
		Dimension dbottom=bottomComponent.getPreferredSize();
		dbottom=new Dimension((int)dtop.getWidth(),(int)dbottom.getHeight());
		bottomComponent.setPreferredSize(dbottom);
    }
    
    //to be notified when the time window changed
    public void timeScaleChanged(TimeScaleEvent e) {
    	if (viewsSynchronizable()){
    	    adjustSizes();
    	}
    }
	public Synchronizer getSynchronizer() {
		if (synchronizer == null)
			synchronizer = new Synchronizer();
		return synchronizer;
	}
	public WorkspaceSetting createWorkspace(int context) {
		Workspace ws = new Workspace();
		ws.dividerLocation = getDividerLocation();
		return ws;
	}
	public void restoreWorkspace(WorkspaceSetting w, int context) {
		Workspace ws = (Workspace) w;
		setDividerLocation(ws.dividerLocation);
	}
	public static class Workspace implements WorkspaceSetting {
		private static final long serialVersionUID = -8129925562216728220L;
		int dividerLocation;
		public int getDividerLocation() {
			return dividerLocation;
		}
		public void setDividerLocation(int dividerLocation) {
			this.dividerLocation = dividerLocation;
		}
	}	
}