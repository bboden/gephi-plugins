package org.i9.GCViz.utils;

import org.gephi.graph.spi.LayoutData;

/**
 *
 * @author Roman Haag
 */
public class CustomLayoutData implements LayoutData{
    
    public enum nodeType {
        Le, Dc, Dv, Dh
    }
    
    //Data
    public float dx = 0;
    public float dy = 0;
    public float old_dx = 0;
    public float old_dy = 0;
    public float freeze = 0f;
    
    public nodeType type = nodeType.Le;
    public int cluster = -1; // only used for dummy nodes
}
