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
public class OSAttractive extends Force {

    public OSAttractive(float c) {
        this.c = c;
    }
    
    @Override
    float calculateForce(float dist) {
        return -c * dist;
    }
}
