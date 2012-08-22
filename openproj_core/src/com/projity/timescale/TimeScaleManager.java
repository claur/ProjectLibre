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
package com.projity.timescale;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.digester.Digester;

import com.projity.configuration.Configuration;

/**
 *
 */
public class TimeScaleManager {
	
	protected int currentScaleIndex=-1;
	protected List scales;
	protected int defaultIndex;
	protected boolean normalWidth;
	/**
	 * 
	 */
	public TimeScaleManager() {
		scales=new LinkedList();
		normalWidth=true;
	}
	
	public void addTimeScale(TimeScale scale){
		scales.add(scale);
	}
	
	/**
	 * @return Returns the scale.
	 */
	public TimeScale getScale() {
		return (TimeScale)scales.get(getCurrentScaleIndex());
	}
	
	public int getMinWidth() {
		return getScale().getMinWidth();
	}
		
	/**
	 * @return Returns the defaultScaleIndex.
	 */
	public int getDefaultIndex() {
		return defaultIndex;
	}
	/**
	 * @param defaultScaleIndex The defaultScaleIndex to set.
	 */
	public void setDefaultIndex(int defaultIndex) {
		this.defaultIndex = defaultIndex;
	}
	
	/**
	 * @return Returns the currentScaleIndex.
	 */
	public int getCurrentScaleIndex() {
		if (currentScaleIndex==-1) currentScaleIndex=defaultIndex;
		return currentScaleIndex;
	}
	
	
    public boolean toggleMinWidth(boolean normal){
    	if (normal!=normalWidth){
    		normalWidth=normal;
    		for (Iterator i=scales.iterator();i.hasNext();){
    			TimeScale scale=(TimeScale)i.next();
    			scale.toggleWidth(normal);
    		}
    		return true;
    	}else return false;
    }

    public boolean isShowWholeDays(){
    	return getCurrentScaleIndex()<=2;
    }
	
	public boolean canZoomIn() {
		return getCurrentScaleIndex()>0;
	}
	public boolean canZoomOut() {
		return getCurrentScaleIndex()<scales.size()-1;
	}
	public boolean zoomIn(){
		if (getCurrentScaleIndex()>0){
			currentScaleIndex--;
			return true;
			//fireTimeScaleChanged(this);
		}else return false;
	}
	
	public boolean zoomOut(){
		if (getCurrentScaleIndex()<scales.size()-1){
			currentScaleIndex++;
			return true;
			//fireTimeScaleChanged(this);
		}else return false;
	}
	public boolean zoomReset(){
		if (currentScaleIndex!=defaultIndex){
			currentScaleIndex=defaultIndex;
			return true;
			//fireTimeScaleChanged(this);
		}else return false;
	}
	
	/*public static TimeScaleManager getInstance(){
		return Configuration.getInstance().getTimeScales();
	}*/
	public static TimeScaleManager createInstance(){
		TimeScaleManager tsManager=new TimeScaleManager();
		TimeScaleManager ref=Configuration.getInstance().getTimeScales();
		tsManager.defaultIndex=ref.defaultIndex;
		tsManager.currentScaleIndex=ref.currentScaleIndex;
		for (Iterator i=ref.scales.iterator();i.hasNext();){
			tsManager.scales.add(((TimeScale)i.next()).clone());
		}
		return tsManager;
	}
	
	public static void addDigesterEvents(Digester digester){
		digester.addObjectCreate("*/timescales", "com.projity.timescale.TimeScaleManager");
	    digester.addSetProperties("*/timescales");
		digester.addSetNext("*/timescales", "setTimeScales", "com.projity.timescale.TimeScaleManager");
		
		digester.addObjectCreate("*/timescales/timescale", "com.projity.timescale.TimeScale");
	    digester.addSetProperties("*/timescales/timescale");
		digester.addSetNext("*/timescales/timescale", "addTimeScale", "com.projity.timescale.TimeScale");

	}

	public final void setCurrentScaleIndex(int currentScaleIndex) {
		this.currentScaleIndex = currentScaleIndex;
	}
	
	
	
	
	
	
	//events handling
	
	/*protected EventListenerList listenerList = new EventListenerList();

	public void addTimeScaleListener(TimeScaleListener l) {
		listenerList.add(TimeScaleListener.class, l);
	}
	public void removeTimeScaleListener(TimeScaleListener l) {
		listenerList.remove(TimeScaleListener.class, l);
	}
	public TimeScaleListener[] getTimeScaleListeners() {
		return (TimeScaleListener[]) listenerList.getListeners(TimeScaleListener.class);
	}
	protected void fireTimeScaleChanged(Object source) {
		Object[] listeners = listenerList.getListenerList();
		TimeScaleEvent e = null;
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TimeScaleListener.class) {
				if (e == null) {
					e = new TimeScaleEvent(source);
				}
				((TimeScaleListener) listeners[i + 1]).timeScaleChanged(e);
			}
		}
	}
    public EventListener[] getListeners(Class listenerType) { 
    	return listenerList.getListeners(listenerType); 
       }
	*/	

	
}
