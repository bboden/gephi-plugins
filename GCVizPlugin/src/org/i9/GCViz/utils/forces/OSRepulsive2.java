/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.i9.GCViz.utils.forces;

import org.gephi.graph.api.Node;

/**
 *
 * @author Roman
 */
public class OSRepulsive2 extends Force {
    private float l = 20;
    
    public OSRepulsive2(float c, float l) {
        this.c = c;
        this.l = l;
    }
    
    @Override
    float calculateForce(float dist) {
        if (dist < l) {
            return (c / dist);
        } else {
            return 0;
        }
    }
}
