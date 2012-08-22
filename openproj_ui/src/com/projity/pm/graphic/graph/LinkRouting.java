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
package com.projity.pm.graphic.graph;

import java.awt.geom.GeneralPath;

/**
 *
 */
public abstract class LinkRouting {

	protected float lx0,lx1,lx2,ly0,ly1,ly2,fx0,fx1,fy0,fy1;
	protected int nbPoints;
	protected GeneralPath path;
	protected void addLinkPoint(double x,double y){
		addLinkPoint(x,y,true);
	}
	protected void addLinkPoint(double x,double y,boolean line){
		lx2=lx1;
		ly2=ly1;
		lx1=lx0;
		ly1=ly0;
		lx0=(int)Math.round(x);
		ly0=(int)Math.round(y);
		if (nbPoints==0){
			fx0=lx0;
			fy0=ly0;
			path.moveTo(lx0,ly0);
		}else{
			if (nbPoints==1){
				fx1=lx0;
				fy1=ly0;
			}
			if (line)path.lineTo(lx0,ly0);
		}
		nbPoints++;
	}
	protected void resetLinkPoints(){
		nbPoints=0;
		lx0=-1;
		lx1=-1;
		ly0=-1;
		ly1=-1;
		fx0=-1;
		fx1=-1;
		fy0=-1;
		fy1=-1;
		path.reset();
	}
	protected void line(){
		path.lineTo(lx0,ly0);
	}
	protected void quad(){
		path.quadTo(lx1,ly1,lx0,ly0);
	}
	protected void curve(){
		path.curveTo(lx2,ly2,lx1,ly1,lx0,ly0);
	}
	
	//public abstract void routePath(GeneralPath path,double x0,double y0,double x1,double y1,double[] extraPoints, int type);
	
	
	
	public float getFirstX() {
		return fx0;
	}
	public float getFirstY() {
		return fy0;
	}
	public float getLastX() {
		return lx0;
	}
	public float getLastY() {
		return ly0;
	}
	
	public double getFirstAngle() {
		return Math.atan2(fy1-fy0,fx0-fx1);
	}
	public double getLastAngle() {
		return Math.atan2(ly1-ly0,lx0-lx1);
	}
}
