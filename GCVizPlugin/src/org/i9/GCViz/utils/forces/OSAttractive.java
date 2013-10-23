package org.i9.GCViz.utils.forces;


public class OSAttractive extends Force {

    public OSAttractive(float c) {
        this.c = c;
    }
    
    @Override
    float calculateForce(float dist) {
        return -c * dist;
    }
}
