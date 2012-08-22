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

import java.awt.geom.GeneralPath;
import java.util.HashMap;

import com.projity.util.ArrayUtils;

/**
 * 
 */
public class PredefinedShape {
	private String name;
	private double[][] points = null;
	private double[][][] pointGrid = null; 

	public GeneralPath toGeneralPath(double hScale, double vScale, double hShift, double vShift) {
		GeneralPath path;
		if (points != null) {
			path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, points.length);
			int x, y;
			for (int i = 0; i < points.length; i++) {
				x = r(hScale * points[i][0] + hShift);
				y = r(vScale * points[i][1] + vShift);
				if (i == 0)
					path.moveTo(x, y);
				else
					path.lineTo(x, y);
			}
		} else {
			path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, pointGrid.length);
			int x, y;
			for (int i = 0; i < pointGrid.length; i++) {
				x = r(hScale * pointGrid[i][0][0] + vScale * pointGrid[i][0][1] + hShift * pointGrid[i][0][2] + vShift
						* pointGrid[i][0][3] + hShift);
				y = r(hScale * pointGrid[i][1][0] + vScale * pointGrid[i][1][1] + hShift * pointGrid[i][1][2] + vShift
						* pointGrid[i][1][3] + vShift);
				if (i == 0)
					path.moveTo(x, y);
				else
					path.lineTo(x, y);
			}
		}
		path.closePath();
		return path;
	}

	protected PredefinedShape(String name, double[][] points, double hScale, double vScale, double hShift, double vShift) {
		this.name = name;
		this.points = ArrayUtils.clone(points);
		scale(hScale, vScale);
		translate(hShift, vShift);

	}

	protected PredefinedShape(String name, double[][] points) {
		this(name, points, 1.0, 1.0, 0.0, 0.0);
	}

	protected PredefinedShape(String name, double[][][] matrixPoints) {
		this.name = name;
		this.pointGrid = ArrayUtils.clone(matrixPoints);
	}

	private void scale(double hScale, double vScale) {
		for (int i = 0; i < points.length; i++) {
			points[i][0] *= hScale;
			points[i][1] *= vScale;
		}
	}

	private void translate(double hShift, double vShift) {
		for (int i = 0; i < points.length; i++) {
			points[i][0] += hShift;
			points[i][1] += vShift;
		}
	}
	private static int r(double d) {
		return (int) Math.round(d);
	}

	private static void add(PredefinedShape predefinedShape) {
		predefinedShapeMap.put(predefinedShape.name, predefinedShape);
	}

	private static final double rectPoints[][] = new double[][] { { 1, .5 }, { 0, .5 }, { 0, -.5 }, { 1, -.5 }};

	public static final PredefinedShape FULL_HEIGHT = new PredefinedShape("FULL_HEIGHT", rectPoints);

	public static final PredefinedShape HALF_HEIGHT_TOP = new PredefinedShape("HALF_HEIGHT_TOP", rectPoints, 1, .5, 0, -.25);

	public static final PredefinedShape HALF_HEIGHT_BOTTOM = new PredefinedShape("HALF_HEIGHT_BOTTOM", rectPoints, 1, .5, 0, .25);

	public static final PredefinedShape HALF_HEIGHT_CENTER = new PredefinedShape("HALF_HEIGHT_CENTER", rectPoints, 1, .5, 0, 0);

	public static final PredefinedShape QUARTER_HEIGHT_CENTER = new PredefinedShape("QUARTER_HEIGHT_CENTER", rectPoints, 1, .25, 0, 0);

	public static final PredefinedShape SQUARE = new PredefinedShape("SQUARE", new double[][] { { .5, .5 }, { -.5, .5 }, { -.5, -.5 }, { .5, -.5 }

	});

	public static final PredefinedShape DIAMOND = new PredefinedShape("DIAMOND", new double[][] { { 0, -.5 }, { -.5, 0 }, { 0, .5 }, { .5, 0 } });

	public static final PredefinedShape PENTAGON_UP = new PredefinedShape("PENTAGON_UP", new double[][] { { 0, -.5 }, { -.5, 0 }, { -.5, .5 },
			{ .5, .5 }, { .5, 0 } });

	public static final PredefinedShape PENTAGON_DOWN = new PredefinedShape("PENTAGON_DOWN", new double[][] { { 0, .5 }, { -.5, 0 }, { -.5, -.5 },
			{ .5, -.5 }, { .5, 0 } });

	public static final PredefinedShape TRIANGLE_UP = new PredefinedShape("TRIANGLE_UP", new double[][] { { -.5, .5 }, { 0, -.5 }, { .5, .5 }, });

	public static final PredefinedShape TRIANGLE_DOWN = new PredefinedShape("TRIANGLE_DOWN", new double[][] { { -.5, -.5 }, { 0, .5 }, { .5, -.5 }, });

	public static final PredefinedShape TRIANGLE_RIGHT = new PredefinedShape("TRIANGLE_RIGHT",
			new double[][] { { -.5, -.5 }, { -.5, .5 }, { .5, 0 }, });

	public static final PredefinedShape TRIANGLE_LEFT = new PredefinedShape("TRIANGLE_LEFT", new double[][] { { .5, -.5 }, { .5, .5 }, { -.5, 0 }, });

	public static final PredefinedShape ARROW_UP = new PredefinedShape("ARROW_UP", new double[][] { { -.2, .5 }, { -.2, 0 }, { -.5, 0 }, { 0, -.5 },
			{ .5, 0 }, { .2, 0 }, { .2, .5 }, });

	public static final PredefinedShape ARROW_DOWN = new PredefinedShape("ARROW_DOWN", new double[][] { { -.2, -.5 }, { -.2, 0 }, { -.5, 0 },
			{ 0, .5 }, { .5, 0 }, { .2, 0 }, { .2, -.5 }, });

	public static final PredefinedShape LINK_ARROW1 = new PredefinedShape("LINK_ARROW1", new double[][] { { 0, 0 }, { 1, 1 }, { .7, 0 }, { 1, -1 },
			{ 0, 0 }, });

	// pert shapes
	public static final PredefinedShape HEXAGON = new PredefinedShape("HEXAGON", new double[][][] { { { 1, -.25, 0, 0 }, { 0, .5, 0, 0 } },
			{ { 0, .25, 0, 0 }, { 0, .5, 0, 0 } }, { { 0, 0, 0, 0 }, { 0, 0, 0, 0 } }, { { 0, .25, 0, 0 }, { 0, -.5, 0, 0 } },
			{ { 1, -.25, 0, 0 }, { 0, -.5, 0, 0 } }, { { 1, 0, 0, 0 }, { 0, 0, 0, 0 } }, });

	public static final PredefinedShape PARALLELOGRAM = new PredefinedShape("PARALLELOGRAM", new double[][][] { { { 1, 0, 0, 0 }, { 0, -.5, 0, 0 } },
			{ { 0, .25, 0, 0 }, { 0, -.5, 0, 0 } }, { { 0, 0, 0, 0 }, { 0, .5, 0, 0 } }, { { 1, -.25, 0, 0 }, { 0, .5, 0, 0 } }, });

	public static final PredefinedShape[] MIDDLE_LIST = { FULL_HEIGHT, HALF_HEIGHT_TOP, HALF_HEIGHT_BOTTOM, HALF_HEIGHT_CENTER, QUARTER_HEIGHT_CENTER };

	public static final PredefinedShape[] END_LIST = { SQUARE, DIAMOND, PENTAGON_UP, PENTAGON_DOWN, TRIANGLE_UP, TRIANGLE_DOWN, TRIANGLE_RIGHT,
			TRIANGLE_LEFT, ARROW_UP, ARROW_DOWN, LINK_ARROW1 };

	public static final PredefinedShape[] NETWORK_LIST = { FULL_HEIGHT, HEXAGON, PARALLELOGRAM };

	private static void initialize() {

		add(FULL_HEIGHT);
		add(HALF_HEIGHT_TOP);
		add(HALF_HEIGHT_BOTTOM);
		add(HALF_HEIGHT_CENTER);
		add(QUARTER_HEIGHT_CENTER);

		add(SQUARE);
		add(DIAMOND);
		add(PENTAGON_UP);
		add(PENTAGON_DOWN);
		add(TRIANGLE_UP);
		add(TRIANGLE_DOWN);
		add(TRIANGLE_RIGHT);
		add(TRIANGLE_LEFT);
		add(ARROW_UP);
		add(ARROW_DOWN);

		// pert shapes
		add(HEXAGON);
		add(PARALLELOGRAM);

		// links
		add(LINK_ARROW1);

	}

	private static HashMap predefinedShapeMap = null;

	private static HashMap getPredefinedShapeMap() {
		if (predefinedShapeMap == null) {
			predefinedShapeMap = new HashMap();
			initialize();
		}
		return predefinedShapeMap;
	}

	public static PredefinedShape find(String key) {
		PredefinedShape found = (PredefinedShape) getPredefinedShapeMap().get(key);
		return found;
	}
}
