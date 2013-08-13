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
public class FAAttractive extends Force {
       
    public FAAttractive(float c) {
        this.c = c;
    }
    
    @Override
    public float calculateForce(float dist) {
        return 0.01f * -c * dist;
    }

}
