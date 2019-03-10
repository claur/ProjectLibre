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
package com.projectlibre1.util;

import java.util.Arrays;

import org.apache.commons.collections.Closure;

/**
 * Utility methods for working with arrays.
 * 
 */
public abstract class ArrayUtils {
    
    /**
     * Clones a two dimensional array of floats.
     * 
     * @param array  the array.
     * 
     * @return A clone of the array.
     */
    public static double[][] clone(final double[][] array) {
    
        if (array == null) {
            return null;
        }
        final double[][] result = new double[array.length][];
        System.arraycopy(array, 0, result, 0, array.length);

        for (int i = 0; i < array.length; i++) {
            final double[] child = array[i];
            final double[] copychild = new double[child.length];
            System.arraycopy(child, 0, copychild, 0, child.length);
            result[i] = copychild;
        }

        return result;
    
    }
    public static double[][][] clone(final double[][][] array) { //works only for arrays of n*2 matrix
        
            if (array == null) {
                return null;
            }
            final double[][][] result = new double[array.length][2][];
            System.arraycopy(array, 0, result, 0, array.length);

            for (int i = 0; i < array.length; i++) {
                final double[] child0 = array[i][0];
                final double[] child1 = array[i][1];
                final double[] copychild0 = new double[child0.length];
                final double[] copychild1 = new double[child1.length];
                System.arraycopy(child0, 0, copychild0, 0, child0.length);
                System.arraycopy(child1, 0, copychild1, 0, child1.length);
                result[i] = new double[2][];
                result[i][0] = copychild0;
                result[i][1] = copychild1;
            }

            return result;
        
     }
    
    /**
     * Tests two double arrays for equality.
     * 
     * @param array1  the first array.
     * @param array2  the second arrray.
     * 
     * @return A boolean.
     */
    public static boolean equal(final double[][] array1, final double[][] array2) {
        if (array1 == null) {
            return (array2 == null);
        }

        if (array2 == null) {
            return false;
        }

        if (array1.length != array2.length) {
            return false;
        }

        for (int i = 0; i < array1.length; i++) {
            if (!Arrays.equals(array1[i], array2[i])) {
                return false;
            }
        }
        return true;
    }
    public static void forAllDo(Object[] array, Closure c) {
    	for (int i = 0; i < array.length; i++) {
    		c.execute(array[i]);
    	}
    }
}
