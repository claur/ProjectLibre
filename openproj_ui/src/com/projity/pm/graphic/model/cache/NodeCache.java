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
package com.projity.pm.graphic.model.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.projity.pm.graphic.model.event.CacheEvent;



/**
 *
 */
public class NodeCache extends CellCache {

	public NodeCache() {
		super();
	}
	
	public void updateVisibleElements(Set updates){
	    //dumpVoids();
	    VisibleNodes v;
	    HashSet u=new HashSet();
	    for (Iterator i=visibleElements.iterator();i.hasNext();){
	        v=(VisibleNodes)i.next();
	        u.clear();
	        u.addAll(updates);
	        updateVisibleElements(v,u);
	    }
	}
	public void updateVisibleElements(VisibleNodes v, Set updates){
//		long t0=System.currentTimeMillis();

		ArrayList visibleElements=v.getElements();
	    ArrayList oldList=(ArrayList)visibleElements.clone();
		
		visibleElements.clear();
		int minLevel=-1;
		for(Iterator i=getCacheIterator();i.hasNext();){
			GraphicNode node=(GraphicNode)i.next();
			if (minLevel!=-1&&node.getLevel()>minLevel) continue;
			minLevel=-1;
			visibleElements.add(node);
			if (node.isComposite()&&node.isCollapsed()) minLevel=node.getLevel();
		}
//		long t1=System.currentTimeMillis();
//		System.out.println("\t\tcache NodeCache#1 ran in "+(t1-t0)+"ms");

		v.applyTransformer();
//		t0=System.currentTimeMillis();
//		System.out.println("\t\tcache NodeCache#2 ran in "+(t0-t1)+"ms");

		applyUpdates(oldList, visibleElements, updates, v.getEvents(), this);
//		t1=System.currentTimeMillis();
//		System.out.println("\t\tcache NodeCache#3 ran in "+(t1-t0)+"ms");

	}

	//for schedule caching option
//	public void updateCachedSchedule(){
//		GraphicNode node;
//		for (Iterator i=cache.iterator();i.hasNext();){
//			node=(GraphicNode)i.next();
//			node.updateScheduleCache();
//		}
//	}

	
	public static void applyUpdates(ArrayList oldList, ArrayList newList, Set updates, List events, Object source){
	    ArrayList o=(ArrayList)oldList.clone();
		ArrayList n=(ArrayList)newList.clone();
		
//		long t0=System.currentTimeMillis();
		ArrayList removeList=null;
		ArrayList removeNodeList=null;
		//if (removeFunctor!=null){
			removeList=new ArrayList();
			removeNodeList=new ArrayList();
			createRemoveDiff(o,n,removeNodeList,removeList,updates);
			if (removeList.size()>0){
				//removeFunctor.execute(removeNodeList,removeList);
			    events.add(new CacheEvent(source,CacheEvent.NODES_REMOVED,(List)removeNodeList.clone(),(List)removeList.clone()));
			}
		//}
//			long t1=System.currentTimeMillis();
//			System.out.println("\t\t\tcache applyUpdates#1 ran in "+(t1-t0)+"ms");
		
		ArrayList insertList=null;
		ArrayList insertNodeList=null;
		//if (insertFunctor!=null){
			insertList=new ArrayList();
			insertNodeList=new ArrayList();
			createRemoveDiff(n,o,insertNodeList,insertList,updates);
			if (insertList.size()>0){
			    events.add(new CacheEvent(source,CacheEvent.NODES_INSERTED,(List)insertNodeList.clone(),(List)insertList.clone()));
				//insertFunctor.execute(insertNodeList,insertList);
			}
		//}
//			t0=System.currentTimeMillis();
//			System.out.println("\t\t\tcache applyUpdates#2 ran in "+(t0-t1)+"ms");
		
		//if (removeFunctor!=null&&insertFunctor!=null){
			removeList.clear();
			removeNodeList.clear();
			insertList.clear();
			insertNodeList.clear();
			createPermutationDiff(o,n,removeNodeList,insertNodeList,removeList,insertList,updates);
			if (removeList.size()>0){
			    events.add(new CacheEvent(source,CacheEvent.NODES_REMOVED,removeNodeList,removeList));
				//removeFunctor.execute(removeNodeList,removeList);
			}
			if (insertList.size()>0){
			    events.add(new CacheEvent(source,CacheEvent.NODES_INSERTED,(List)insertNodeList.clone(),(List)insertList.clone()));
				//insertFunctor.execute(insertNodeList,insertList);
			}
//			t1=System.currentTimeMillis();
//			System.out.println("\t\t\tcache applyUpdates#3 ran in "+(t1-t0)+"ms");
		//}
		
		//if (updateFunctor!=null){
			insertList.clear();
			insertNodeList.clear();
			createUpdateDiff(newList,insertNodeList,insertList,updates);
			if (insertList.size()>0){
			    events.add(new CacheEvent(source,CacheEvent.NODES_CHANGED,insertNodeList,insertList));
			    //updateFunctor.execute(insertNodeList,insertList);
			}
		//}
//			t0=System.currentTimeMillis();
//			System.out.println("\t\t\tcache applyUpdates#4 ran in "+(t0-t1)+"ms");
		
	}
	
	
	protected static void createRemoveDiff(ArrayList oldList,ArrayList newList,ArrayList nodeDiff,ArrayList intervaldiff,Set updates){
		Collection newCol=getContainsCollection(newList);
		int row=0;
		int begin=-1;
		int end=-1;
		Object current;
		for (ListIterator i=oldList.listIterator();i.hasNext();row++){
			if (!newCol.contains(current=i.next())){
				nodeDiff.add(current);
				if (updates!=null) updates.remove(current); //to avoid remove/insert followed by update
				if (begin==-1){
					begin=row;
					end=row;
				}else{
					if (row==end+1) end=row;
					else{
						intervaldiff.add(new CacheInterval(begin,end));
						begin=row;
						end=row;
					}
				}
				i.remove();
			}
		}
		if (begin!=-1) intervaldiff.add(new CacheInterval(begin,end));
	}
	
	
	protected static void createPermutationDiff(ArrayList oldList,ArrayList newList,
			ArrayList removeNodeList,ArrayList insertNodeList,
			ArrayList removeIntervalList,ArrayList insertIntervalList,
			Set updates){
	    //oldList and newList have the same size and contains the same elements
	    ListIterator o=oldList.listIterator();
	    ListIterator n=newList.listIterator();
	    int startRow=-1;;
	    for(int row=0;o.hasNext();row++){
	        Object oelement=o.next();
	        Object nelement=n.next();
	        if (oelement.equals(nelement)){
	            if (startRow!=-1&&startRow<row){
	                CacheInterval interval=new CacheInterval(startRow,row-1);
	                removeIntervalList.add(interval);
	                insertIntervalList.add(interval);
	                startRow=-1;
	            }
	        }else{
	            if (startRow==-1) startRow=row;
	            removeNodeList.add(oelement);
	            insertNodeList.add(nelement);
	        }
	    }
        if (startRow!=-1){
            CacheInterval interval=new CacheInterval(startRow,oldList.size()-1);
            removeIntervalList.add(interval);
            insertIntervalList.add(interval);
        }
	}
	
	
	protected static void createUpdateDiff(ArrayList newList, ArrayList nodeDiff,ArrayList diff,Set updates){
		if (updates!=null&&updates.size()>0){
			Collection updatesCol=getContainsCollection(updates);
			int begin=-1;
			int end=-1;
			int row=0;
			Object current;
			for (Iterator i=newList.iterator();i.hasNext();row++){
				if (updatesCol.contains(current=i.next())){
				    nodeDiff.add(current);
					if (begin==-1){
						begin=row;
						end=row;
					}else{
						if (row==end+1) end=row;
						else{
							diff.add(new CacheInterval(begin,end));
							begin=row;
							end=row;
						}
					}
				}
			}		
			if (begin!=-1) diff.add(new CacheInterval(begin,end));
		}
		
	}
	
	public Object getBase(Object base) {
		return ((GraphicNode)base).getNode();
	}
	
//	
//	private void dumpVoids(){
//	    Object current;
//	    List vn;
//	    GraphicNode node;
//	    for (Iterator i=voidNodes.keySet().iterator();i.hasNext();){
//	        current=i.next();
//	        System.out.println(current+":");
//	        vn=(List)voidNodes.get(current);
//	        for (Iterator j=vn.iterator();j.hasNext();){
//	            node=(GraphicNode)j.next();
//	            System.out.println("\t"+node+": "+node.getLevel());
//	        }
//	    }
//	}

	protected void fireEvents(Object source, List nodeEvents, List edgeEvents) {
        if (nodeEvents.size()>0||edgeEvents.size()>0)
	    for (Iterator i=visibleElements.iterator();i.hasNext();)
	        ((VisibleNodes)i.next()).fireGraphicNodesCompositeEvent(source,nodeEvents,edgeEvents);
	}
//	protected void fireScheduleEvent(Object source, ScheduleEvent scheduleEvent) {
//	    for (Iterator i=visibleElements.iterator();i.hasNext();)
//	        ((VisibleNodes)i.next()).fireGraphicNodesCompositeEvent(source,null,null,scheduleEvent,null);
//	}
//	protected void fireObjectEvent(Object source, ObjectEvent objectEvent) {
//	    for (Iterator i=visibleElements.iterator();i.hasNext();)
//	        ((VisibleNodes)i.next()).fireGraphicNodesCompositeEvent(source,null,null,null, objectEvent);
//	}
	public void fireEvents(Object source, VisibleNodes nodes) {
        List nodeEvents=nodes.getEvents();
        List edgeEvents=nodes.getVisibleDependencies().getEvents();
        if (nodeEvents.size()>0||edgeEvents.size()>0){
		    nodes.fireGraphicNodesCompositeEvent(source,nodeEvents,edgeEvents);
	        nodes.clearEvents();
	        nodes.getVisibleDependencies().clearEvents();
	
        }
	}
	public void fireEvents(Object source) {
	    for (Iterator i=visibleElements.iterator();i.hasNext();){
	        VisibleNodes v=(VisibleNodes)i.next();
	        List nodeEvents=v.getEvents();
	        List edgeEvents=v.getVisibleDependencies().getEvents();
	        if (nodeEvents.size()>0||edgeEvents.size()>0){
	            v.fireGraphicNodesCompositeEvent(source,nodeEvents,edgeEvents);
	            v.clearEvents();
	            v.getVisibleDependencies().clearEvents();
	        }
	    }
	}

}
