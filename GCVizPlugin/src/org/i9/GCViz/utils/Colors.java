package org.i9.GCViz.utils;

import java.util.Random;

/**
 *
 * @author Roman Haag
 */
public class Colors {
    Random generator;
    
    private float[] r;
    private float[] g;
    private float[] b;
    private float[][] standardColors = {
        {0f, 0f, 1f},
        {0f, 1f, 0f},
        {1f, 0f, 0f},
        {1f, 1f, 0f},
        {1f, 0f, 1f},
        {0f, 1f, 1f},
        {0f, 0f, 0.5f},
        {0f, 0.5f, 0f},
        {0.5f, 0f, 0f},
        {0.5f, 0.5f, 0f},
        {0.5f, 0f, 0.5f},
        {0f, 0.5f, 0.5f},};

    public Colors(int number) {
        r = new float[number];
        g = new float[number];
        b = new float[number];
        generator = new Random();
        
        int i = 0;
        while ((i < number) && (i < standardColors.length)) {
            r[i] = standardColors[i][0];
            g[i] = standardColors[i][1];
            b[i] = standardColors[i][2];
            i++;
        }

        while (i < number) {
            r[i] = generator.nextFloat();
            g[i] = generator.nextFloat();
            b[i] = generator.nextFloat();
            i++;
        }
    }

    public float getR(int colorNumber) {
        return r[colorNumber];
    }

    public float getG(int colorNumber) {
        return g[colorNumber];
    }

    public float getB(int colorNumber) {
        return b[colorNumber];
    }
}
