/*
 * Copyright 2019 The TranSysLab Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.transyslab.commons.renderer;
import java.awt.Color;
public class ColorBar {


    /**
     *  ���쵽��ɫ
     */
    public final static Color[] GRADIENT_MAROON_TO_GOLD = createGradient(new Color(0xA0, 0x00, 0x00), new Color(0xFF, 0xFF, 0x00), 500);

    /**
     * ������
     */
    public final static Color[] GRADIENT_BLUE_TO_RED = createGradient(Color.BLUE, Color.RED, 500);

    /**
     * �ڵ���
     */
    public final static Color[] GRADIENT_BLACK_TO_WHITE = createGradient(Color.BLACK, Color.WHITE, 500);

    /**
     * �쵽��
     */
    public final static Color[] GRADIENT_RED_TO_GREEN = createGradient(Color.RED, Color.GREEN, 500);

    /**
     * ��, �٣��ƣ���
     */
    public final static Color[] GRADIENT_GREEN_YELLOW_ORANGE_RED = createMultiGradient(new Color[]{Color.red, Color.yellow, Color.green}, 250);

    /**
     * �ʺ�
     */
    public final static Color[] GRADIENT_RAINBOW = createMultiGradient(new Color[]{new Color(181, 32, 255), Color.blue, Color.green, Color.yellow, Color.orange, Color.red}, 500);

    /**
     *�ڣ��죬�٣��ƣ���
     */
    public final static Color[] GRADIENT_HOT = createMultiGradient(new Color[]{Color.black, new Color(87, 0, 0), Color.red, Color.orange, Color.yellow, Color.white}, 500);

    /**
     * ��, ��, ��, ��)
     */
    public final static Color[] GRADIENT_HEAT = createMultiGradient(new Color[]{Color.black, new Color(105, 0, 0), new Color(192, 23, 0), new Color(255, 150, 38), Color.white}, 500);

    /**
     * ��, ��, ��
     */
    public final static Color[] GRADIENT_ROY = createMultiGradient(new Color[]{Color.red, Color.orange, Color.yellow}, 500);

    /**
     * Creates an array of Color objects for use as a gradient, using a linear
     * interpolation between the two specified colors.
     * @param one Color used for the bottom of the gradient
     * @param two Color used for the top of the gradient
     * @param numSteps The number of steps in the gradient. 250 is a good number.
     */
    public static Color[] createGradient(final Color one, final Color two, final int numSteps)
    {
        int r1 = one.getRed();
        int g1 = one.getGreen();
        int b1 = one.getBlue();
        int a1 = one.getAlpha();

        int r2 = two.getRed();
        int g2 = two.getGreen();
        int b2 = two.getBlue();
        int a2 = two.getAlpha();

        int newR = 0;
        int newG = 0;
        int newB = 0;
        int newA = 0;

        Color[] gradient = new Color[numSteps];
        double iNorm;
        for (int i = 0; i < numSteps; i++)
        {
            iNorm = i / (double)numSteps; //a normalized [0:1] variable
            newR = (int) (r1 + iNorm * (r2 - r1));
            newG = (int) (g1 + iNorm * (g2 - g1));
            newB = (int) (b1 + iNorm * (b2 - b1));
            newA = (int) (a1 + iNorm * (a2 - a1));
            gradient[i] = new Color(newR, newG, newB, newA);
        }

        return gradient;
    }

    /**
     * Creates an array of Color objects for use as a gradient, using an array of Color objects. It uses a linear interpolation between each pair of points. The parameter numSteps defines the total number of colors in the returned array, not the number of colors per segment.
     * @param colors An array of Color objects used for the gradient. The Color at index 0 will be the lowest color.
     * @param numSteps The number of steps in the gradient. 250 is a good number.
     */
    public static Color[] createMultiGradient(Color[] colors, int numSteps)
    {
        //we assume a linear gradient, with equal spacing between colors
        //The final gradient will be made up of n 'sections', where n = colors.length - 1
        int numSections = colors.length - 1;
        int gradientIndex = 0; //points to the next open spot in the final gradient
        Color[] gradient = new Color[numSteps];
        Color[] temp;

        if (numSections <= 0)
        {
            throw new IllegalArgumentException("You must pass in at least 2 colors in the array!");
        }

        for (int section = 0; section < numSections; section++)
        {
            //we divide the gradient into (n - 1) sections, and do a regular gradient for each
            temp = createGradient(colors[section], colors[section+1], numSteps / numSections);
            for (int i = 0; i < temp.length; i++)
            {
                //copy the sub-gradient into the overall gradient
                gradient[gradientIndex++] = temp[i];
            }
        }

        if (gradientIndex < numSteps)
        {
            //The rounding didn't work out in our favor, and there is at least
            // one unfilled slot in the gradient[] array.
            //We can just copy the final color there
            for (/* nothing to initialize */; gradientIndex < numSteps; gradientIndex++)
            {
                gradient[gradientIndex] = colors[colors.length - 1];
            }
        }

        return gradient;
    }
    public static Color valueToColor(double min, double max, double value, int numStep){
        double step = (max - min)/numStep;
        for(int i=0; i<numStep;i++){
            if(value> min+ i* step && value<= min + (i+1)* step)
                return GRADIENT_GREEN_YELLOW_ORANGE_RED[i];
        }
        return null;
    }

}