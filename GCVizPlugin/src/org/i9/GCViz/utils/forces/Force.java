package org.i9.GCViz.utils.forces;

import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.i9.GCViz.utils.CustomLayoutData;

/**
 *
 * @author Roman
 */
public abstract class Force {
    
    float c = 1;
    
    abstract float calculateForce(float dist);

    public void apply(Node n1, Node n2) {
        NodeData N1 = n1.getNodeData();
        NodeData N2 = n2.getNodeData();
        
        double xDist = N1.x() - N2.x();
        double yDist = N1.y() - N2.y();
        float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);

        if (dist > 0) {
            double f = calculateForce(dist);

            CustomLayoutData N1L = N1.getLayoutData();
            CustomLayoutData N2L = N2.getLayoutData();

            N1L.dx += xDist / dist * f;
            N1L.dy += yDist / dist * f;

            N2L.dx -= xDist / dist * f;
            N2L.dy -= yDist / dist * f;
        } else {
        }
    }
}
