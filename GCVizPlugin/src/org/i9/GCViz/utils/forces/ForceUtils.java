/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.i9.GCViz.utils.forces;

import org.gephi.graph.api.NodeData;
import org.i9.GCViz.utils.CustomLayoutData;

/**
 *
 * @author Roman
 */
public class ForceUtils {
    /*
    // applies repulsive force if force.calculateForce() is positive (or attractive force if negative)
    public static void applyForce(NodeData N1, NodeData N2, float c, float l, Force force) {
        double xDist = N1.x() - N2.x();
        double yDist = N1.y() - N2.y();
        float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);

        if (dist > 0) {
            double f = force.calculateForce(c, dist, l);

            CustomLayoutData N1L = N1.getLayoutData();
            CustomLayoutData N2L = N2.getLayoutData();

            N1L.dx += xDist / dist * f;
            N1L.dy += yDist / dist * f;

            N2L.dx -= xDist / dist * f;
            N2L.dy -= yDist / dist * f;
        } else {
            
        }
    }
     * */
}
