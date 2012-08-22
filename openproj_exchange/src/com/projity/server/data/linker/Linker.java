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
package com.projity.server.data.linker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Closure;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.hierarchy.NodeHierarchy;
import com.projity.pm.assignment.Assignment;

/**
 *
 */
public abstract class Linker {
	//protected boolean globalIdsOnly=true;
	protected Map transformationMap=new HashMap();
	protected Collection transformed=new ArrayList();
	protected Iterator iterator=null;
	protected Object parent;
	protected Object transformedParent;
	protected Object[] args=null;
	protected boolean incremental;

	public Map getTransformationMap() {
		return transformationMap;
	}
	public Collection getTransformed() {
		return transformed;
	}
//	public boolean isGlobalIdsOnly() {
//		return globalIdsOnly;
//	}
//	public void setGlobalIdsOnly(boolean globalIdsOnly) {
//		this.globalIdsOnly = globalIdsOnly;
//	}

	public boolean isIncremental() {
		return incremental;
	}
	public void setIncremental(boolean incremental) {
		this.incremental = incremental;
	}

	public Object getParent() {
		return parent;
	}
	public void setParent(Object parent) {
		this.parent = parent;
	}
	public Object getTransformedParent() {
		return transformedParent;
	}
	public void setTransformedParent(Object transformedParent) {
		this.transformedParent = transformedParent;
	}

	public Object[] getArgs() {
		return args;
	}
	public void setArgs(Object[] args) {
		this.args = args;
	}
	public boolean hasNext(){return (iterator==null)?false:iterator.hasNext();}
	public void addTransformedObjects() throws Exception{
		while(hasNext()){
			Object obj=executeNext();
			if (obj!=null){
				Object trans=addTransformedObjects(obj);
				if (trans!=null) transformed.add(trans);
			}
		}
		executeFinally();
	}
	public void init(){
		transformationMap.clear();
		transformed.clear();
		initIterator();
	}

	//private int lastIndex;
    public void addOutline(Node root){
    	final Set endVoids=new HashSet();
    	//lastIndex=0;
        getHierarchy().visitAll(root, true,new Closure(){
        	//int tmpIndex=0;
        	public void execute(Object arg){
        		Node node=(Node)arg;
        		Object nodeImpl=node.getImpl();
        		if (!(nodeImpl instanceof Assignment)){
//        			if (!node.isVoid()) lastIndex=tmpIndex;
//        			tmpIndex++;
        			if (node.isVoid()) endVoids.add(node);
        			else endVoids.clear();
        		}
        	}
        });

        getHierarchy().visitAllLevelOrder(root, true,new Closure(){
        	Node thisParent=null;
        	long position=0;
        	//int index=0;
        	public void execute(Object arg){
        		//if (index++>lastIndex) return;
        		Node node=(Node)arg;
        		if (endVoids.contains(node)) return;
        		Object nodeImpl=node.getImpl();
        		if (!(nodeImpl instanceof Assignment)){
        			Node currentParent=getHierarchy().getParent(node);
        			if (currentParent!=null&&currentParent.isRoot()) currentParent=null; //for compatibility
        			if (thisParent!=currentParent){
        				thisParent=currentParent;
        				position=0;
        			}
        			if (node.isVoid()||addOutlineElement(nodeImpl,(thisParent==null)?null:thisParent.getImpl(),position))
        				position++; //skip voids but increment position
        		}
        	}
        });
    }
//    public void addOutline(Node root){
//        getHierarchy().visitAllLevelOrder(root, true,new Closure(){
//        	Node thisParent=null;
//        	long position=0;
//        	public void execute(Object arg){
//        		Node node=(Node)arg;
//        		Object nodeImpl=node.getImpl();
//        		if (!(nodeImpl instanceof Assignment)&&!node.isVoid()){
//        			Node currentParent=getHierarchy().getParent(node);
//        			if (currentParent!=null&&currentParent.isRoot()) currentParent=null; //for compatibility
//        			if (thisParent!=currentParent){
//        				thisParent=currentParent;
//        				position=0;
//        			}
//        			if (addOutlineElement(nodeImpl,(thisParent==null)?null:thisParent.getImpl(),position))
//        				position++;
//        		}
//        	}
//        });
//    }






	protected void initIterator(){}
	public Object executeNext(){return null;}
	public void executeFinally(){}
    public abstract Object addTransformedObjects(Object child) throws Exception;
    public NodeHierarchy getHierarchy(){return null;}
    public boolean addOutlineElement(Object outlineChild,Object outlineParent,long position){return true;}
}
