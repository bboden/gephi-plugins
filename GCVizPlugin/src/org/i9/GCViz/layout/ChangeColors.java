/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.i9.GCViz.layout;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.layout.plugin.AbstractLayout;
import org.openide.util.NbBundle;
import org.i9.GCViz.utils.*;

/**
 *
 * @author Roman
 */
public class ChangeColors extends AbstractLayout implements Layout {

    private Graph graph;
    private String clusteringFile;
    private SubspaceClusteringReader scr;
   
    public ChangeColors(LayoutBuilder layoutBuilder) {
        super(layoutBuilder);
    }

    @Override
    public void initAlgo() {
        graph = graphModel.getGraphVisible();
        setConverged(false);
        scr = new SubspaceClusteringReader(clusteringFile);
        scr.run();
        Colors colors = new Colors(scr.getClusterCount());
        Node[] nodes = graph.getNodes().toArray();
        for (Node n : nodes) {
            ArrayList<Integer> clusters = scr.getClusters(Integer.parseInt(n.getNodeData().getId()));
            if (clusters.size() == 1) {
                int cluster = clusters.get(0);
                n.getNodeData().setR(colors.getR(cluster));
                n.getNodeData().setG(colors.getG(cluster));
                n.getNodeData().setB(colors.getB(cluster));
            } else if (clusters.size() > 1) {
                n.getNodeData().setR(0);
                n.getNodeData().setG(0);
                n.getNodeData().setB(0);
            }
        }
                
    }

   
    
   
    @Override
    public void goAlgo() {
        graph = graphModel.getGraphVisible();
       /* Node[] nodes = graph.getNodes().toArray();
        Edge[] edges = graph.getEdges().toArray();

        // reset dx and dy
        for (Node n : nodes) {
            if (n.getNodeData().getLayoutData() == null || !(n.getNodeData().getLayoutData() instanceof CustomLayoutData)) {
                n.getNodeData().setLayoutData(new CustomLayoutData());
            }
            CustomLayoutData layoutData = n.getNodeData().getLayoutData();
            layoutData.dx = 0;
            layoutData.dy = 0;
        }*/
       
   
    }

    @Override
    public void endAlgo() {
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String TESTLAYOUT = "ChangeColors";
        try {
            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    NbBundle.getMessage(ChangeColors.class, "TestLayout.clusteringFile.name"),
                    TESTLAYOUT,
                    "TestLayout.clusteringFile.name",
                    NbBundle.getMessage(ChangeColors.class, "TestLayout.clusteringFile.desc"),
                    "getClusteringFile", "setClusteringFile"));
          
        } catch (Exception e) {
            e.printStackTrace();
        }


        return properties.toArray(new LayoutProperty[0]);
    }

    public String getClusteringFile() {
        return clusteringFile;
    }

    public void setClusteringFile(String file) {
        this.clusteringFile = file;
    }
    
 

   @Override
    public void resetPropertiesValues() {
        clusteringFile = "C:\\Users\\boden\\example_graph.true";
        
    }
}
