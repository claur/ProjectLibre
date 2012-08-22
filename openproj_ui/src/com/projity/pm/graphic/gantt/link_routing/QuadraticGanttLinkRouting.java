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
package com.projity.pm.graphic.gantt.link_routing;

import java.awt.geom.GeneralPath;

import com.projity.pm.dependency.DependencyType;

/**
 *
 */
public class QuadraticGanttLinkRouting extends GanttLinkRouting{
	private static final long serialVersionUID = -1617695945785858247L;

	public void routePath(GeneralPath path,double x0,double y0,double x1,double y1,double y2,double y1floor,double y1ceil,int type){
		this.path=path;
		int fromSign=(type==DependencyType.SF||type==DependencyType.SS)?-1:1;
		int toSign=(type==DependencyType.FS||type==DependencyType.SS)?-1:1;
		
		double fromDeltaX = 5.0;
		double toDeltaX = 15.0;
		double maxDeltaXVerticalArrow = 20;
		double deltaQ=10;
		double deltaXb=Math.min(deltaQ,Math.abs(y1-y0)/2);
		double signY=(y1>=y0)?1:-1;
		
		double x2=x0+fromSign*fromDeltaX;
		double x2b=x2+fromSign*2*deltaXb;
		double x3=x1+toSign*toDeltaX;
		double x3b=x3+toSign*2*deltaXb;
		double x2bb=(x2b+x2)/2;
		double x3bb=(x3b+x3)/2;
		
		double y0b=y0+signY*deltaXb;
		double y1b=y1-signY*deltaXb;
		double y0bb=y0+signY*Math.min(deltaQ,Math.abs(y2-y0)/2);
		double y1bb=y1-signY*Math.min(deltaQ,Math.abs(y1-y2)/2);
		double y2b0=y2-signY*Math.min(deltaQ,Math.abs(y2-y0)/2);
		double y2b1=y2+signY*Math.min(deltaQ,Math.abs(y1-y2)/2);

		resetLinkPoints();
		addLinkPoint(x0,y0);
		if (type==DependencyType.FS&&verticalArrow&&x1+maxDeltaXVerticalArrow>=x2bb){
			double x4=Math.max(x1,x2bb);
			double x4b=x4-deltaXb;
			addLinkPoint(x4b,y0);
			addLinkPoint(x4,y0,false);
			addLinkPoint(x4,y0b,false);
			quad();
			addLinkPoint(x4,(y1>=y0)?y1ceil:y1floor);
			return;
		}
		switch (type) {
		case DependencyType.FS:
		case DependencyType.SF:
			if (type==DependencyType.FS&&x3>=x2||x3<=x2&&type==DependencyType.SF){
				addLinkPoint(x3b,y0);
				addLinkPoint(x3bb,y0,false);
				addLinkPoint(x3bb,y0b,false);
				quad();
				if (y0b!=y1b) addLinkPoint(x3bb,y1b);
				addLinkPoint(x3bb,y1,false);
				addLinkPoint(x3,y1,false);
				quad();
			}else{
				addLinkPoint(x2,y0);
				addLinkPoint(x2bb,y0,false);
				addLinkPoint(x2bb,y0bb,false);
				quad();
				if (y0bb!=y2b0) addLinkPoint(x2bb,y2b0);
				addLinkPoint(x2bb,y2,false);
				addLinkPoint(x2,y2,false);
				quad();
				addLinkPoint(x3,y2);
				addLinkPoint(x3bb,y2,false);
				addLinkPoint(x3bb,y2b1,false);
				quad();
				if (y1bb!=y2b1) addLinkPoint(x3bb,y1bb);
				addLinkPoint(x3bb,y1,false);
				addLinkPoint(x3,y1,false);
				quad();
			}
			break;
		case DependencyType.SS:
		case DependencyType.FF:{
			double x5,x5b;
			if (type==DependencyType.SS){
				x5 = (x2 < x3) ? x2 : x3;
				x5b = (x2b < x3b) ? x2b : x3b;
			}else{
				x5 = (x2 > x3) ? x2 : x3;
				x5b = (x2b > x3b) ? x2b : x3b;
			}
			double x5bb=(x5b+x5)/2;
			addLinkPoint(x5, y0);
			addLinkPoint(x5bb, y0,false);
			addLinkPoint(x5bb, y0b,false);
			quad();
			if (y0b!=y1b) addLinkPoint(x5bb, y1b);
			addLinkPoint(x5bb, y1,false);
			addLinkPoint(x5, y1,false);
			quad();
			break;
		}
		default:
			return;
		}
		addLinkPoint(x1,y1);
	}
}
