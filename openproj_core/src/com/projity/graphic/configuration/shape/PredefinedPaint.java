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
package com.projity.graphic.configuration.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 *
 */
public class PredefinedPaint extends TexturePaint {
	private Color foreground;
	private Color background;
	private int width;
	private int height;
	private int[] points;
	private float alphaValue=1.0f;


	public PredefinedPaint(int width, int height, int[] array) {
		this(width, height, array, Color.black, Color.white);
	}
	public PredefinedPaint(PredefinedPaint pattern, Color foreground, Color background) {
		this(pattern.width, pattern.height, pattern.points, foreground,
				background);
	}

	private PredefinedPaint(int width, int height, int[] points, Color foreground,
			Color background) {
		super(createTexture(width, height, points, foreground, background),
				new Rectangle(0, 0, width, height));
		this.width = width;
		this.height = height;
		this.foreground = foreground;
		this.background = background;
		this.points = points;
		float sum=0;
		for (int i=0; i<points.length;i++){
			sum+=points[i];
		}
		alphaValue = (float) Math.pow(sum/points.length,1.5); //modify brightness
	}

	private static BufferedImage createTexture(int width, int height,
			int[] array, Color foreground, Color background) {
		BufferedImage bufferedImage = new BufferedImage(width, height,BufferedImage.TYPE_INT_ARGB);
		for (int w = 0; w < width; w++) {
			for (int h = 0; h < height; h++) {
				bufferedImage.setRGB(w, h, array[w + h * width] > 0 ? foreground.getRGB() : background.getRGB());
			}
		}
		return bufferedImage;
	}

	public static final PredefinedPaint TRANSPARENT =  new PredefinedPaint(4, 4,
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
	public static final PredefinedPaint SOLID = new PredefinedPaint(4, 4,
			new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1});
	public static final PredefinedPaint DEFAULT = new PredefinedPaint(4, 4,
			new int[]{1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1});
	public static final PredefinedPaint SPACED_DOTS = new PredefinedPaint(4, 4,
			new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
	public static final PredefinedPaint DIAGONAL = new PredefinedPaint(4, 4,
			new int[]{0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0});
	public static final PredefinedPaint DOT_LINE = new PredefinedPaint(4, 4,
			new int[]{1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0});
	public static final PredefinedPaint DASH_LINE = new PredefinedPaint(1, 4,
			new int[]{1, 1, 0, 0});
	public static final PredefinedPaint DOT_LINE2 = new PredefinedPaint(1, 2,
			new int[]{1, 0});
	public static final PredefinedPaint VERY_SPACED_DOTS = new PredefinedPaint(8, 8,
			new int[]{
			1, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0
			});
	
	private static Object[][] data = {
	  {"TRANSPARENT",TRANSPARENT},
	  {"SOLID",	SOLID},
	  {"DEFAULT",DEFAULT},
	  {"SPACED_DOTS",SPACED_DOTS},
	  {"VERY_SPACED_DOTS",VERY_SPACED_DOTS},
	  {"DASH_LINE",DASH_LINE},
	  {"DIAGONAL",DIAGONAL},
	  {"DOT_LINE2",DOT_LINE2},
	  {"DOT_LINE",DOT_LINE}
	};
	private static HashMap shapePaintMap = null;
	
	public static HashMap getShapePaints() {
		if (shapePaintMap == null) {
			shapePaintMap = new HashMap();
			for (int i = 0; i < data.length; i++) {
				Object row[] = data[i];
				shapePaintMap.put(row[0], row[1]);
			}
		}
		return shapePaintMap;
	}

	public static PredefinedPaint find(String key) {
		return(PredefinedPaint) getShapePaints().get(key);
	}


	public void applyPaint(Graphics2D g2,boolean texture){
		//if ("SVGGraphics2D".equals(g2.getClass().getSimpleName()))
		if (texture) {
			g2.setPaint(this); // the paint already has the color set
		} else {
			g2.setColor(new Color(bar(foreground.getRed(),background.getRed(),alphaValue),bar(foreground.getGreen(),background.getGreen(),alphaValue),bar(foreground.getBlue(),background.getBlue(),alphaValue)));
		}
		
	}
	private int bar(float a,float b,float w){
		return Math.round(a*w+(1.0f-w)*b);
	}

    
}
