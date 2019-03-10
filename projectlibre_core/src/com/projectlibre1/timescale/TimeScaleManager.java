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
package com.projectlibre1.timescale;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.digester.Digester;

import com.projectlibre1.configuration.Configuration;

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
		digester.addObjectCreate("*/timescales", "com.projectlibre1.timescale.TimeScaleManager");
	    digester.addSetProperties("*/timescales");
		digester.addSetNext("*/timescales", "setTimeScales", "com.projectlibre1.timescale.TimeScaleManager");
		
		digester.addObjectCreate("*/timescales/timescale", "com.projectlibre1.timescale.TimeScale");
	    digester.addSetProperties("*/timescales/timescale");
		digester.addSetNext("*/timescales/timescale", "addTimeScale", "com.projectlibre1.timescale.TimeScale");

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
