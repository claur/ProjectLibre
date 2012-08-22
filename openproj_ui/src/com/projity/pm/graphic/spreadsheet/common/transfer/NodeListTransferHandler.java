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
package com.projity.pm.graphic.spreadsheet.common.transfer;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelDataFactory;
import com.projity.pm.graphic.spreadsheet.SpreadSheet;
import com.projity.pm.graphic.spreadsheet.SpreadSheetModel;
import com.projity.pm.graphic.spreadsheet.common.CommonSpreadSheetModel;
import com.projity.pm.task.NormalTask;
import com.projity.pm.task.Project;
import com.projity.pm.task.SubProj;
import com.projity.pm.task.Task;
import com.projity.strings.Messages;
import com.projity.util.Alert;

/**
 *
 */
public class NodeListTransferHandler extends TransferHandler {
	    public NodeListTransferHandler(SpreadSheet spreadSheet){
	    	super();
	    	this.spreadSheet=spreadSheet;
	    }
	    transient protected SpreadSheet spreadSheet;
	    
	    public SpreadSheet getSpreadSheet() {
			return spreadSheet;
		}
		public void setSpreadSheet(SpreadSheet spreadSheet) {
			this.spreadSheet = spreadSheet;
		}
		public void exportToClipboard(JComponent c, Clipboard clip, int action) {
	        boolean exportSuccess = false;
	        Transferable t = null;

	        if (action != NONE) {
	            t = createTransferable(c,action);
	            if (t != null) {
	                clip.setContents(t, null);
	                exportSuccess = true;
	            }
	        }

	        if (exportSuccess) {
	            exportDone(c, t, action);
	        } else {
	            exportDone(c, null, NONE);
	        }
	    }
		private boolean transformSubprojectBranches(Node parent,NodeModelDataFactory dataFactory,Predicate p){
			if (dataFactory instanceof Project &&
					parent.getImpl() instanceof SubProj
//					&&!((Project)dataFactory).getSubprojectHandler().canInsertProject( ((SubProj)parent.getImpl()).getSubprojectUniqueId() )
//TODO disabled, improve paste of subprojects
			){
				if (!p.evaluate(parent)) return false;
				
			}
			for (Enumeration e=parent.children();e.hasMoreElements();){
					Node node=(Node)e.nextElement();
					if (!transformSubprojectBranches(node,dataFactory,p)) return false;

			}
			return true;
		}
		
	    protected Transferable createTransferable(JComponent c, int action) {
	    	SpreadSheet spreadSheet=getSpreadSheet(c);
	    	if (spreadSheet==null) return null;
	    	ArrayList nodes=(ArrayList)spreadSheet.getSelectedNodes().clone();
	    	
	    	ArrayList fields=spreadSheet.getSelectedFields();
	    	boolean nodeSelection=(fields==null);
	    	if (fields==null) fields=spreadSheet.getSelectableFields();
		    if (action==TransferHandler.COPY){
		    	if (nodeSelection){
		    		SpreadSheet.SpreadSheetAction a=getNodeListCopyAction().getSpreadSheetAction();
			    	a.execute(nodes);
		    	}
		    	return new NodeListTransferable(nodes,fields,spreadSheet,spreadSheet.getSelectedRows(),spreadSheet.getSelectedColumns(),nodeSelection);
		    }else if (action==TransferHandler.MOVE){//cut
		    	if (nodeSelection){
		    		SpreadSheet.SpreadSheetAction a=((nodeSelection)?getNodeListCutAction():getNodeListCopyAction()).getSpreadSheetAction();
		    		
        	    	for (Iterator i=nodes.iterator();i.hasNext();) {
        	    		Node node=(Node)i.next();
        	    		final boolean[] okForAll=new boolean[]{false}; 
	        			if (!transformSubprojectBranches(node,spreadSheet.getCache().getModel().getDataFactory(),new Predicate(){
								public boolean evaluate(Object arg0) {
									if (okForAll[0]) return true;
									Node parent=(Node)arg0;
									boolean r=Alert.okCancel( Messages.getString("Message.subprojectCut") );
									if (r) okForAll[0]=true;
									return r;
								}
								
						})) return null;
        			}

		    		
		    		a.execute(nodes);
		    	}
		    	return new NodeListTransferable(nodes,fields,spreadSheet,spreadSheet.getSelectedRows(),spreadSheet.getSelectedColumns(),nodeSelection);
		    } else return null;
	    }
	    protected void exportDone(JComponent source, Transferable data, int action) {
	    }
	    public boolean importData(JComponent c, Transferable t) {
	    	SpreadSheet spreadSheet=getSpreadSheet(c);
	    	if (spreadSheet==null) return false;
	    	DataFlavor flavor=getFlavor(t.getTransferDataFlavors());
	        if (flavor!=null) {
	            try {
	            	NodeModel model=((CommonSpreadSheetModel)spreadSheet.getModel()).getCache().getModel();
	            	Object data=t.getTransferData(flavor);
	        		if (data==null) return false;
	            	List nodes=null;
	        		if (data instanceof ArrayList){
	        			nodes=(List)data;
	        			
	        	    	for (Iterator i=nodes.iterator();i.hasNext();) {
	        	    		Node node=(Node)i.next();
	        				transformSubprojectBranches(node,model.getDataFactory(),new Predicate(){
								public boolean evaluate(Object arg0) {
									Node parent=(Node)arg0;
									//change implementation
									NormalTask task=new NormalTask();
									Task source=((Task)parent.getImpl());
									source.cloneTo(task);
									//task.setDuration(source.getActualDuration());
									parent.setImpl(task);
									return true;
								}	 
							});
	        			}

		    	    	SpreadSheet.SpreadSheetAction a=getNodeListPasteAction().getSpreadSheetAction();
				    	a.execute(nodes);
	        		}else if (data instanceof String){
//	        			ArrayList fields=spreadSheet.getSelectedFields();
//	        			if (fields==null){
//	        				fields=spreadSheet.getSelectableFields(); //The whole line is selected
//		        			nodes=NodeListTransferable.stringToNodeList((String)data,spreadSheet,fields,model.getDataFactory());
//	        			}else{
//	        				NodeListTransferable.pasteString((String)data,spreadSheet);
//	        			}
        				NodeListTransferable.pasteString((String)data,spreadSheet);
	        		}else return false;
	        		
	                return true;
	            } catch (UnsupportedFlavorException ufe) {
	            } catch (IOException ioe) { }
	        }
	        return false;
	    }
	    
	    protected SpreadSheet getSpreadSheet(JComponent c){
			if (c instanceof SpreadSheet){
	    		return (SpreadSheet)c;
			}else return null;
	    }

	    protected DataFlavor getFlavor(DataFlavor[] flavors) {
//    		for (int i=0;i<flavors.length;i++){
//    			System.out.println("flavor #"+i+": "+flavors[i]);
//    		}
	        NodeListTransferable t=new NodeListTransferable(null,null,null,null,null,true);
    		for (int i=0;i<flavors.length;i++){
    			if (t.isDataFlavorSupported(flavors[i]))
    				return flavors[i];
    		}
	        return null;
	    }

	    public boolean canImport(JComponent c, DataFlavor[] flavors) {
	        return getFlavor(flavors)!=null;
	    }
	    
	    public static void registerWith(SpreadSheet spreadSheet){
	    	NodeListTransferHandler handler=new NodeListTransferHandler(spreadSheet);
//	    	if (c instanceof SpreadSheet){
//	    		SpreadSheet spreadSheet=(SpreadSheet)c;
//	    		handler.setSpreadSheet(spreadSheet);
//	    	}
			spreadSheet.setTransferHandler(handler);
			
			InputMap imap = spreadSheet.getInputMap();
			imap.put(KeyStroke.getKeyStroke("ctrl X"),
					NodeListTransferHandler.getCutAction().getValue(Action.NAME));
			imap.put(KeyStroke.getKeyStroke("ctrl C"),
					NodeListTransferHandler.getCopyAction().getValue(Action.NAME));
			imap.put(KeyStroke.getKeyStroke("ctrl V"),
					NodeListTransferHandler.getPasteAction().getValue(Action.NAME));
			//c.setInputMap(JComponent.WHEN_FOCUSED,imap);
			
			ActionMap amap = spreadSheet.getActionMap();
			amap.put(NodeListTransferHandler.getCutAction().getValue(Action.NAME),
					NodeListTransferHandler.getCutAction());
			amap.put(NodeListTransferHandler.getCopyAction().getValue(Action.NAME),
					NodeListTransferHandler.getCopyAction());
			amap.put(NodeListTransferHandler.getPasteAction().getValue(Action.NAME),
					NodeListTransferHandler.getPasteAction());

	    }
	    
	    
	    protected transient NodeListTransfertAction nodeListCutAction, nodeListCopyAction,nodeListPasteAction;
	    
	    protected void initCutAction(SpreadSheet.SpreadSheetAction a) {
	    	nodeListCutAction=new NodeListTransfertAction(getCutAction(),a,spreadSheet);
	    	nodeListCutAction.putValue("Name",Messages.getString("Spreadsheet.Action.cut"));
	    }
	    protected void initCopyAction(SpreadSheet.SpreadSheetAction a) {
	    	nodeListCopyAction=new NodeListTransfertAction(getCopyAction(),a,spreadSheet);
	    	nodeListCopyAction.putValue("Name",Messages.getString("Spreadsheet.Action.copy"));
	    }
	    protected void initPasteAction(SpreadSheet.SpreadSheetAction a) {
	    	nodeListPasteAction=new NodeListTransfertAction(getPasteAction(),a,spreadSheet);
	    	nodeListPasteAction.putValue("Name",Messages.getString("Spreadsheet.Action.paste"));
	    }
		public NodeListTransfertAction getNodeListCopyAction() {
			if (nodeListCopyAction==null) initCopyAction(spreadSheet.getCopyAction());
			return nodeListCopyAction;
		}
		public NodeListTransfertAction getNodeListCutAction() {
			if (nodeListCutAction==null) initCutAction(spreadSheet.getCutAction());
			return nodeListCutAction;
		}
		public NodeListTransfertAction getNodeListPasteAction() {
			if (nodeListPasteAction==null) initPasteAction(spreadSheet.getPasteAction());
			return nodeListPasteAction;
		}
	    

	    
	}
