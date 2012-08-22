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
package com.projity.dialog;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.undo.UndoableEditSupport;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.dialog.util.FieldComponentMap;
import com.projity.document.Document;
import com.projity.document.ObjectEvent;
import com.projity.grouping.core.Node;
import com.projity.pm.graphic.frames.DocumentSelectedEvent;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeEvent;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeListener;
import com.projity.pm.resource.Resource;
import com.projity.pm.scheduling.Schedule;
import com.projity.pm.scheduling.ScheduleEvent;
import com.projity.pm.scheduling.ScheduleEventListener;
import com.projity.pm.task.BelongsToDocument;
import com.projity.pm.task.Project;
import com.projity.pm.task.Task;
import com.projity.strings.Messages;
import com.projity.util.DataUtils;


/**
 *
 */
public abstract class FieldDialog extends AbstractDialog  implements ObjectEvent.Listener,ScheduleEventListener,SelectionNodeListener, DocumentSelectedEvent.Listener {
	private boolean multipleObjects;
	private Class objectClass;
	private UndoableEditSupport undoableEditSupport;
	protected FieldDialog(Frame owner, String title, boolean modal, boolean multipleObjects/*,UndoableEditSupport undoableEditSupport*/) {
		super(owner,title,modal);
		this.multipleObjects = multipleObjects;
		//this.undoableEditSupport=undoableEditSupport;
		
		// need to update all initially, but do later on once things are set
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateAll();
			}});
	}

	protected ArrayList maps = new ArrayList();
	protected Object object;
	protected ArrayList collection = new ArrayList();
	private JComponent dirtyComponent;
	protected JComponent mainComponent = null;
	
	protected FieldComponentMap createMap() {
		FieldComponentMap map;
		if (multipleObjects)
			map = new FieldComponentMap(collection);
		else
			map = new FieldComponentMap(object);
			
		maps.add(map);
		return map;
	}

	protected Object getObject() {
		return object;
	}
	protected Collection getCollection() {
		return collection;
	}

	protected Object getFirstObject() {
		if (collection == null)
			return object;
		Iterator i = collection.iterator();
		if (i.hasNext())
			return i.next();
		return null;
	}

//	public void hide() {
//		setObject(null);
//		super.hide();
//	}

	protected void onCancel() {
		updateAll();
	}


	public void setType(boolean task){
		objectClass=(task)?Task.class:Resource.class;
		setTitle((task)?Messages.getString("FieldDialog.TaskInformation"):Messages.getString("FieldDialog.ResourceInformation")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void setObjectClass(Class objectClass) {
		this.objectClass = objectClass;
	}
	
	public Class getObjectClass() {
		return objectClass;
	}
	public void objectChanged(ObjectEvent objectEvent) {
		if (!isVisible()) return;
		if (multipleObjects && collection.contains(objectEvent.getObject())) {
			updateAll(); // if in list, need to update all
		} else if (objectEvent.getObject() == getObject()) {
			updateAll();
		}
	}
	public void scheduleChanged(ScheduleEvent scheduleEvent){
		if (!isVisible()) return;
		if (multipleObjects) {
			//to be more precise could see if one of the collection's objects is dirty 
			updateAll(); // if in list, need to update all
		} else if (getObject()!=null&&((Schedule)getObject()).isJustModified()) {
			updateAll();
		}
	}

	public void documentSelected(DocumentSelectedEvent evt) {
		System.out.println(Messages.getString("FieldDialog.document")+evt.getCurrent()); //$NON-NLS-1$
	}

	public void selectionChanged(SelectionNodeEvent e) {
		if (!isVisible())
			return;
		Node selected;
		Object nodeObject;
		if (multipleObjects) {
			setCollection(e.getNodes());
		} else {
			selected = e.getCurrentNode();
			if (selected == null)
				return;
			nodeObject = selected.getImpl();
			nodeObject = DataUtils.extractObjectOfClass(nodeObject,objectClass);
//			if (nodeObject == null)
//				return;
			setObject(nodeObject);
			updateAll();
		}
	}

	public void setCollection(Collection nodeList) {
		DataUtils.extractObjectsOfClassFromNodeList(collection,nodeList,objectClass);
	}
	
	public void setObject(Object object) {
		if (object == this.object)
			return;
		if (this.object != null && this.object instanceof BelongsToDocument) {
			Document document=((BelongsToDocument)this.object).getDocument();
			document.removeObjectListener(this);
			if (document instanceof Project)
				((Project)document).removeScheduleListener(this);
		}
		this.object = object;
		if (object != null && object instanceof BelongsToDocument) {
			Document document=((BelongsToDocument)this.object).getDocument();
			document.addObjectListener(this);
			if (document instanceof Project)
				((Project)document).addScheduleListener(this);
		}
	}

	protected void activateListeners() {
	}

	protected void desactivateListeners() {
		setObject(null);
	}

	public JComponent createContentPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	protected void updateAll() {
		setVisibleAndEnabledState();
		Iterator i = maps.iterator();
		FieldComponentMap map;
		while (i.hasNext()) {
			map = (FieldComponentMap)i.next();
			map.setObject(object);
			map.updateAll();
		}
	}
	
	protected void setVisibleAndEnabledState() {
		boolean showing = (object != null);
		if (mainComponent != null)
			mainComponent.setEnabled(showing);
	}

	public void setDirtyComponent(JComponent dirtyComponent) {
		this.dirtyComponent = dirtyComponent;
	}

/**
 * On pressing enter key, check any unvalidated component
 */	
	public void onOk() {
		if (dirtyComponent != null) {
			InputVerifier verifier = dirtyComponent.getInputVerifier();
			if (!verifier.shouldYieldFocus(dirtyComponent))
				return;
		}
		super.onOk();
	}

protected JComponent createFieldsPanel(FieldComponentMap map, Collection fields) {
	if (fields == null || fields.size() == 0)
		return null;
	 
	FormLayout layout = new FormLayout(
			"p, 3dlu, fill:160dlu:grow", //$NON-NLS-1$
			StringUtils.chomp(StringUtils.repeat("p,3dlu,", fields.size()))); // repeats and gets rid of last comma //$NON-NLS-1$
	DefaultFormBuilder builder = new DefaultFormBuilder(layout);
	map.append(builder,fields);
	return builder.getPanel();
}
}
