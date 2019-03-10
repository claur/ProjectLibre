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
package com.projectlibre1.pm.graphic.model.event;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.ListUtils;

/**
 *
 */
public class CompositeCacheEvent extends EventObject {
    protected List nodeEvents;
    protected List edgeEvents;
   
    /**
     * @param source
     * @param nodeEvents
     * @param edgeEvents
     */
    public CompositeCacheEvent(Object source,List nodeEvents,
            List edgeEvents) {
        super(source);
        this.nodeEvents = nodeEvents;
        this.edgeEvents = edgeEvents;
    }
   
    public List getEdgeEvents() {
        return edgeEvents;
    }
    public void setEdgeEvents(List edgeEvents) {
        this.edgeEvents = edgeEvents;
    }
    public List getNodeEvents() {
        return nodeEvents;
    }
    public void setNodeEvents(List nodeEvents) {
        this.nodeEvents = nodeEvents;
    }
    
//	public ScheduleEvent getScheduleEvent() {
//		return scheduleEvent;
//	}
//	public ObjectEvent getObjectEvent() {
//		return objectEvent;
//	}
	
    public String toString(){
        return "CompositeGraphicNodeEvent: \n\t"+nodeEvents+" \n\t"+edgeEvents;
    }

    
    protected List insertedNodes;
    protected List removedNodes;
    protected List updatedNodes;
    protected List insertedEdges;
    protected List removedEdges;
    protected List updatedEdges;
    protected boolean diffListsGenerated=false;
    private void generateDiffLists(){
        if (diffListsGenerated) return;
        
        
        //nodes
        CacheEvent event;
        List nodes;
        for (Iterator i=nodeEvents.iterator();i.hasNext();){
            event=(CacheEvent)i.next();
            nodes=event.getNodes();
            switch (event.getType()) {
            case CacheEvent.NODES_CHANGED:
                if (nodes!=null&&nodes.size()>0){
                    if (updatedNodes==null) updatedNodes=new ArrayList(nodes.size());
                    updatedNodes.addAll(nodes);
                }
                break;
            case CacheEvent.NODES_INSERTED:
//              check for hidden updates
                if (removedNodes!=null){
                    List inter=ListUtils.intersection(nodes,removedNodes);
                    if (inter.size()>0){
                        removedNodes.removeAll(inter);
                        nodes.removeAll(inter);
                        if (updatedNodes==null) updatedNodes=new ArrayList(nodes.size());
                        updatedNodes.addAll(inter);
                    }
                }
                
                if (nodes!=null&&nodes.size()>0){
                    if (insertedNodes==null) insertedNodes=new ArrayList(nodes.size());
                    insertedNodes.addAll(nodes);
                }
                break;
            case CacheEvent.NODES_REMOVED:
                //INSERT FOLLOWED BY REMOVE NEVER HAPPENS
                //nothing special to handle
                if (nodes!=null&&nodes.size()>0){
                    if (removedNodes==null) removedNodes=new ArrayList(nodes.size());
                    removedNodes.addAll(nodes);
                }
                break;
            default:
                break;
            }
        }
        
        
        //edges
        for (Iterator i=edgeEvents.iterator();i.hasNext();){
            event=(CacheEvent)i.next();
            nodes=event.getNodes();
            switch (event.getType()) {
            case CacheEvent.NODES_CHANGED:
                if (nodes!=null&&nodes.size()>0){
                    if (updatedEdges==null) updatedEdges=new ArrayList(nodes.size());
                    updatedEdges.addAll(nodes);
                }
                break;
            case CacheEvent.NODES_INSERTED:
                if (nodes!=null&&nodes.size()>0){
                    if (insertedEdges==null) insertedEdges=new ArrayList(nodes.size());
                    insertedEdges.addAll(nodes);
                }
                break;
            case CacheEvent.NODES_REMOVED:
                if (nodes!=null&&nodes.size()>0){
                    if (removedEdges==null) removedEdges=new ArrayList(nodes.size());
                    removedEdges.addAll(nodes);
                }
                break;
            default:
                break;
            }
        }
        
        diffListsGenerated=true;
    }

    
    
    public List getInsertedNodes() {
        generateDiffLists();
        return insertedNodes;
    }
    public List getRemovedNodes() {
        generateDiffLists();
        return removedNodes;
    }
    public List getUpdatedNodes() {
        generateDiffLists();
        return updatedNodes;
    }
    public List getInsertedEdges() {
        generateDiffLists();
        return insertedEdges;
    }
    public List getRemovedEdges() {
        generateDiffLists();
        return removedEdges;
    }
    public List getUpdatedEdges() {
        generateDiffLists();
        return updatedEdges;
    }
    
    public boolean isNodeHierarchy(){
    	return nodeEvents!=null;
    }
}
