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
package com.projectlibre1.pm.graphic.model.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.projectlibre1.pm.graphic.model.event.CacheEvent;

/**
 *
 */
public class DependencyCache extends CellCache {

	public DependencyCache() {
		super();
	}
	
	public void updateAllVisibleElements(){
	    VisibleDependencies v;
	    for (Iterator i=visibleElements.iterator();i.hasNext();){
	        updateAllVisibleElements((VisibleDependencies)i.next());
	    }
	}
	public void updateAllVisibleElements(VisibleDependencies v){	    
	    ArrayList visibleDependencies=v.getElements();
	    ArrayList visibleNodes=v.getVisibleNodes().getElements();
	    visibleDependencies.clear();
		for(Iterator i=getCacheIterator();i.hasNext();){
			GraphicDependency dep=(GraphicDependency)i.next();
			if (visibleNodes.contains(dep.getPredecessor())&&
					visibleNodes.contains(dep.getSuccessor()))
			    visibleDependencies.add(dep);
		}
	}
	
	
	
	public void updateVisibleElements(Set change){
	    VisibleDependencies v;
	    for (Iterator i=visibleElements.iterator();i.hasNext();){
	        updateVisibleElements((VisibleDependencies)i.next(),change);
	    }
	}
	public void updateVisibleElements(VisibleDependencies v,Set change){
	    ArrayList visibleNodes=v.getVisibleNodes().getElements();
	    ArrayList removed=new ArrayList();
		ArrayList inserted=new ArrayList();
		ArrayList changed=new ArrayList();
		changed.addAll(change);
        updateVisibleElements(v.getElements(),visibleNodes,removed,inserted,changed);
		if (removed.size()>0) v.addEvent(new CacheEvent(this,CacheEvent.NODES_REMOVED,removed,null));
		if (inserted.size()>0) v.addEvent(new CacheEvent(this,CacheEvent.NODES_INSERTED,inserted,null));
		if (changed.size()>0) v.addEvent(new CacheEvent(this,CacheEvent.NODES_CHANGED,changed,null));
	}
	private void updateVisibleElements(ArrayList visibleDependencies,ArrayList visibleNodes, ArrayList removed, ArrayList inserted, ArrayList changed){
		Collection visibleNodesCol=getContainsCollection(visibleNodes);
		Collection visibleDependenciesCol=getContainsCollection(visibleDependencies);
		
//		long t0=System.currentTimeMillis();
		boolean containsPredecessor,containsSuccessor,containsDependency;
		for(Iterator i=getCacheIterator();i.hasNext();){
			GraphicDependency dep=(GraphicDependency)i.next();
			containsPredecessor=visibleNodesCol.contains(dep.getPredecessor());
			containsSuccessor=visibleNodesCol.contains(dep.getSuccessor());
			containsDependency=visibleDependenciesCol.contains(dep);
			
//System.out.println("contains " + dep.getPredecessor() + " / " + dep.getSuccessor() + " pred " + containsPredecessor + " succ " + containsSuccessor + " dep " + containsDependency);			
			if (containsPredecessor&&containsSuccessor&&!containsDependency){
			    visibleDependencies.add(dep);
				inserted.add(dep);
				changed.remove(dep);
			}else if ((!containsPredecessor||
					!containsSuccessor)&&containsDependency){
			    visibleDependencies.remove(dep);
			    removed.add(dep);
				changed.remove(dep);
			}
		}
//		long t1=System.currentTimeMillis();
//		System.out.println("\t\tDependencyCache#1 ran in "+(t1-t0)+"ms");
		Collection cacheCol=getContainsCollection(cache);
		for(Iterator i=visibleDependencies.iterator();i.hasNext();){
			GraphicDependency dep=(GraphicDependency)i.next();
			if (!cacheCol.contains(dep)){
			    i.remove();
			    removed.add(dep);
				changed.remove(dep);
			}
		}
//		t0=System.currentTimeMillis();
//		System.out.println("\t\tDependencyCache#2 ran in "+(t0-t1)+"ms");

	}

	public Object getBase(Object base) {
		return ((GraphicDependency)base).getDependency();
	}
	
	
	
	/*protected void fireEdgesCreated(Object source, Object[] edges) {
	    for (Iterator i=visibleElements.iterator();i.hasNext();)
	        ((VisibleDependencies)i.next()).fireEdgesCreated(source,edges);
	}

	protected void fireEdgesRemoved(Object source, Object[] edges) {
	    for (Iterator i=visibleElements.iterator();i.hasNext();)
	        ((VisibleDependencies)i.next()).fireEdgesRemoved(source,edges);
	}

	protected void fireEdgesUpdated(Object source, Object[] edges) {
	    for (Iterator i=visibleElements.iterator();i.hasNext();)
	        ((VisibleDependencies)i.next()).fireEdgesUpdated(source,edges);
	}*/

	
	
}
