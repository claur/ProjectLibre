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
package com.projity.pm.key.uniqueid;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.projity.pm.time.MutableInterval;
import com.projity.session.Session;
import com.projity.session.SessionFactory;

/**
 *
 */
public class UniqueIdPool {
	protected static int MIN_SIZE=10;
	protected static int DEFAULT_SIZE=500;
	protected static UniqueIdPool instance;
	
	public static UniqueIdPool getInstance(){
		if (instance==null) instance=new UniqueIdPool();
		return instance;
	}
	
	protected List serverIntervals;
	protected int reservationSem;
	
	protected long lastIdReservation=-1;
	
	protected UniqueIdPool(){
		serverIntervals=new LinkedList();
	}
	
	public synchronized long getId(Session session) throws UniqueIdException{
		int idCount=getIdCount();
		if (serverIntervals.size()==0){
			//if (onlyGlobal){
				try{
					makeServerReservationSync(idCount,session);
				}catch(Exception e){
					e.printStackTrace();
					throw new UniqueIdException("Server exception");
				}
//			}
//			else makeServerReservationAsync(idCount);
		}
		
		MutableInterval interval;
		long id=-1;
		int size=0;
		synchronized(serverIntervals){
			for (Iterator i=serverIntervals.iterator();i.hasNext();){
				interval=(MutableInterval)i.next();
				if (id==-1){
					id=interval.getStart();
					interval.setStart(id+1);
					if (interval.getStart()>interval.getEnd()){
						i.remove();
						continue;
					}
				}
				size+=interval.getEnd()-interval.getStart()+1;
			}
		}
		if (size<getMinIdCount()) makeServerReservationAsync(idCount-size,session);
//		long r=(id==-1&&!onlyGlobal)?getLocalId():id;
//		return r;
		return id;
	}

	
	
	protected int getIdCount(){
		long t=System.currentTimeMillis();
		if (lastIdReservation!=-1&&t-lastIdReservation<10000) return DEFAULT_SIZE*10;
		return DEFAULT_SIZE;
	}
	protected int getMinIdCount(){
		return MIN_SIZE;
	}
	
	protected void makeServerReservationAsync(final int count,final Session session){
		Thread idBookingThread=new Thread(){
			public void run(){
				synchronized(this){
					if (reservationSem>0) return;
					reservationSem++;
				}
				try {
					makeServerReservation(count,session);
				} catch (Exception e) {
					System.out.println("Id cannot be retrieved: "+e);
				}finally{
					synchronized(this){
						reservationSem--;
					}
				}

			}
		};
		idBookingThread.start();
	}
	
	protected void makeServerReservationSync(final int count,Session session) throws Exception{
		synchronized(this){
			reservationSem++;
		}
		try{
			makeServerReservation(count,session);
		}finally{
			synchronized(this){
				reservationSem--;
			}
		}
	}
	
	protected void makeServerReservation(final int count,Session session) throws Exception{
		System.out.println("ID reservation...");
		lastIdReservation=System.currentTimeMillis();
		MutableInterval interval=(MutableInterval)SessionFactory.call(session,"bookUIDInterval",new Class[]{int.class},new Object[]{count});
		synchronized(serverIntervals){serverIntervals.add(interval);}
		System.out.println("ID reservation, new pool: "+dump());
	}
	
	public String dump(){
		StringBuffer buf=new StringBuffer();
		buf.append('{');
		synchronized(serverIntervals){
			for (Iterator i=serverIntervals.iterator();i.hasNext();){
				MutableInterval interval=(MutableInterval)i.next();
				buf.append('[').append(interval.getStart()).append(',').append(interval.getEnd()).append(']');
				if (i.hasNext()) buf.append(',');
			}
		}
		buf.append('}');
		return buf.toString();
	}
}
