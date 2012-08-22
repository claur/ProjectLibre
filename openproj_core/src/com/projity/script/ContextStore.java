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
package com.projity.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.Converter;
import org.apache.commons.collections.Predicate;
import org.apache.commons.digester.Digester;

import com.projity.configuration.Dictionary;
import com.projity.configuration.NamedItem;
import com.projity.strings.Messages;


public class ContextStore  implements NamedItem {
	public static final String category="ContextStoreCategory";

	protected String name = null;
	protected String id = null;
	protected Map<Integer, List<ConverterContext>> contexts=new HashMap<Integer, List<ConverterContext>>();


	public String getCategory() {
		return category;
	}
	public String getName() {
		return name;
	}
	public final void setName(String name) {
		this.name = name;
	}
	public final String getId() {
		return id;
	}
	public final void setId(String id) {
		this.id = id;
		if (name == null)
			name = Messages.getString(id);
	}
    public static void addDigesterEvents(Digester digester){
		digester.addObjectCreate("*/converterContexts", "com.projity.script.ContextStore");
	    digester.addSetProperties("*/converterContexts");
		digester.addSetNext("*/converterContexts", "add", "com.projity.configuration.NamedItem");

		digester.addObjectCreate("*/converterContexts/context", "com.projity.script.ConverterContext");
	    digester.addSetProperties("*/converterContexts/context");
		digester.addSetNext("*/converterContexts/context", "addContext", "com.projity.script.ConverterContext");
	}

	public void addContext(ConverterContext ctx) {
		List<ConverterContext> list=contexts.get(ctx.getType());
		if (list==null){
			list=new ArrayList<ConverterContext>();
			contexts.put(ctx.getType(),list);
		}
		if (ctx.getName() == null) //TODO contexts should be named
			ctx.setName(Messages.getString(ctx.getFieldArrayId()));
		list.add(ctx);
	}


	public List<ConverterContext> getContexts(int type,Predicate filter){
//	System.out.println("getContext type="+type);
//	for (int t: contexts.keySet()){
//		System.out.println("type="+t);
//		for (ConverterContext ctx: contexts.get(t))
//			System.out.println("\tctx="+ctx);
//	}
		List<ConverterContext> ctxs=contexts.get(type);
		List<ConverterContext> c=null;
		if (ctxs!=null){
			c=new ArrayList<ConverterContext>(ctxs.size());
			for (ConverterContext ctx: ctxs){
				if (filter==null||filter.evaluate(ctx)) c.add((ConverterContext)ctx.clone());
			}
		}
		return c;
	}
//	public List<ConverterContext> getContexts(int type){
////		System.out.println("getContext type="+type);
////		for (int t: contexts.keySet()){
////			System.out.println("type="+t);
////			for (ConverterContext ctx: contexts.get(t))
////				System.out.println("\tctx="+ctx);
////		}
//		return contexts.get(type);
//	}
	public  ConverterContext createDefaultContext(int type){
		ConverterContext ctx=contexts.get(type).get(0);
		if (ctx==null) return null;
		else{
			ConverterContext c=(ConverterContext)ctx.clone();
			c.setDistribution(true); //should be outside but this method is only called by ctx that requires distribution
			return c;
		}
	}

	protected static ContextStore instance=null;
	public static ContextStore getInstance(){
	    if (instance==null){
	    	long t=System.currentTimeMillis();
	    	instance=(ContextStore)Dictionary.getInstance().get(category,"default");
	    	System.out.println("Configuration loaded in "+(System.currentTimeMillis()-t)+" ms");
	    }
	    return instance;
	}


}
