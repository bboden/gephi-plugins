package org.i9.GCViz.layout;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.layout.plugin.AbstractLayout;
import org.openide.util.NbBundle;
import org.i9.GCViz.utils.*;
import org.i9.GCViz.utils.forces.*;

/**
 *
 * @author Roman Haag
 */
public class GCVizLayout extends AbstractLayout implements Layout {

    private Graph graph;
    private String clusteringFile;
    private SubspaceClusteringReader scr;
    private double speed;
    private double p;
    private double epsilon;
    private boolean pInf;
    private double[] max_dist;
    private double[] avg_dist;
    private Map<Node, Map<Node, Force>> attr_force_map;
    private float nodeRadius;
    // forces    
    private Force antiOverlap;
    private Force repulsion;
    private Force attraction_edges;
    private Force attraction_clusters;
    private float c_antiOverlap;
    private float l_antiOverlap;
    private float c_repulsion;
    private float l_repulsion;
    private float c_attraction_edges;
    private float c_attraction_clusters;
    private float c_attributes;
    private float l_repulsion_attributes;
    // evaluation
    private double eval_longEdges;
    private double eval_clustering;
    private double eval_overlap;
    private double eval_attributes;

    public GCVizLayout(LayoutBuilder layoutBuilder) {
        super(layoutBuilder);
    }

    @Override
    public void initAlgo() {
        graph = graphModel.getGraphVisible();
        setConverged(false);

        eval_longEdges = 0.0;
        eval_clustering = 0.0;
        eval_overlap = 0.0;
        eval_attributes = 0.0;

        // read clustering information
        scr = new SubspaceClusteringReader(clusteringFile);
        scr.run();

        // Colors
        Colors colors = new Colors(scr.getClusterCount());
        Node[] nodes = graph.getNodes().toArray();
        nodeRadius = nodes[0].getNodeData().getRadius();

        antiOverlap = new OSRepulsive2(this.c_antiOverlap, nodeRadius * this.l_antiOverlap);
        repulsion = new OSRepulsive2(this.c_repulsion, nodeRadius * this.l_repulsion);
        attraction_edges = new OSAttractive(this.c_attraction_edges);
        attraction_clusters = new OSAttractive(this.c_attraction_clusters);

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

        // compute max and avg distances             
        max_dist = new double[scr.getClusterCount()];
        avg_dist = new double[scr.getClusterCount()];
        for (int i = 0; i < scr.getClusterCount(); i++) {
            for (Node n1 : nodes) {
                Integer id1 = Integer.parseInt(n1.getNodeData().getId());
                if (!scr.getClusters(id1).contains(i)) {
                    continue;
                }
                for (Node n2 : nodes) {
                    Integer id2 = Integer.parseInt(n2.getNodeData().getId());
                    if (n1 == n2 || !scr.getClusters(id2).contains(i)) {
                        continue;
                    }
                    double dist = attrDistance(n1, n2, i);
                    avg_dist[i] += dist;
                    if (max_dist[i] < dist) {
                        max_dist[i] = dist;
                    }
                }
            }
            double temp = scr.getNodes(i).size();
            avg_dist[i] /= temp * temp - temp;
        }

        // compute distances   
        attr_force_map = new HashMap<Node, Map<Node, Force>>();
        for (int i = 0; i < scr.getClusterCount(); i++) {
            for (Node n1 : nodes) {
                Integer id1 = Integer.parseInt(n1.getNodeData().getId());
                if (!scr.getClusters(id1).contains(i)) {
                    continue;
                }
                Map<Node, Force> other_nodes_map = new HashMap<Node, Force>();
                attr_force_map.put(n1, other_nodes_map);
                for (Node n2 : nodes) {
                    Integer id2 = Integer.parseInt(n2.getNodeData().getId());
                    if (n1 == n2 || !scr.getClusters(id2).contains(i)) {
                        continue;
                    }
                    double dist = attrDistance(n1, n2, i);
                    float temp = attrFactor3(dist, i);
                    if (temp == 1) {
                        other_nodes_map.put(n2, new OSAttractive(this.c_attributes * temp));
                    } else if (temp == -1) {
                        other_nodes_map.put(n2, new OSRepulsive2(this.c_attributes * -temp, this.l_repulsion_attributes));
                    }
                }
            }
        }
    }

    private float attrFactor1(double distance, int cluster) {
        return (float) (1 - distance / max_dist[cluster]);
    }

    private float attrFactor2(double distance, int cluster) {
        return (float) (max_dist[cluster] / distance);
    }

    private float attrFactor3(double distance, int cluster) {
        if (distance < epsilon) {
            return 1;
        } else if (distance > epsilon) {
            return -1;
        } else {
            return 0;
        }
    }

    private double attrDistance(Node n1, Node n2, int cluster) {
        double dist = 0;
        Integer[] subspace = scr.getSubspace(cluster);
        AttributeRow row1 = (AttributeRow) n1.getAttributes();
        AttributeRow row2 = (AttributeRow) n2.getAttributes();

        if (pInf) {
            for (int i = 2; i < n1.getAttributes().countValues(); i++) {
                if (subspace[i - 2] == 0) {
                    continue;
                }

                double d1 = Double.parseDouble(row1.getValue(i).toString());
                double d2 = Double.parseDouble(row2.getValue(i).toString());

                double temp = d1 - d2;
                if (temp < 0) {
                    temp = -temp;
                }
                if (temp > dist) {
                    dist = temp;
                }
            }
            return dist;
        }

        for (int i = 2; i < n1.getAttributes().countValues(); i++) {
            if (subspace[i - 2] == 0) {
                continue;
            }

            double d1 = Double.parseDouble(row1.getValue(i).toString());
            double d2 = Double.parseDouble(row2.getValue(i).toString());

            double temp = d1 - d2;
            if (temp < 0) {
                temp = -temp;
            }
            for (int k = 1; k < p; k++) {
                temp *= temp;
            }
            dist += temp;
        }
        return Math.pow(dist, 1d / p);
    }

    private void test0(Node[] nodes, Edge[] edges) {

        for (int i = 0; i < nodes.length; i++) {
            Node n1 = nodes[i];
            for (int j = i + 1; j < nodes.length; j++) {
                Node n2 = nodes[j];

                repulsion.apply(n1, n2);
            }
        }

        for (Edge e : edges) {
            attraction_edges.apply(e.getSource(), e.getTarget());
        }
    }

    private void test1(Node[] nodes, Edge[] edges) {

        boolean useRepulsion = this.c_repulsion > 0;
        boolean useAntiOverlap = this.c_antiOverlap > 0;
        boolean useClustering = this.c_attraction_clusters > 0;

        for (int i = 0; i < nodes.length; i++) {
            Node n1 = nodes[i];
            for (int j = i + 1; j < nodes.length; j++) {
                Node n2 = nodes[j];
                if (useRepulsion) {
                    repulsion.apply(n1, n2);
                }
                if (useAntiOverlap) {
                    antiOverlap.apply(n1, n2);
                }
                if (useClustering) {
                    Integer id1 = Integer.parseInt(n1.getNodeData().getId());
                    Integer id2 = Integer.parseInt(n2.getNodeData().getId());
                    int t = scr.countCommonCLusters(id1, id2);
                    for (int k = 0; k < t; k++) {
                        attraction_clusters.apply(n1, n2);
                    }
                }
            }
        }

        if (this.c_attributes > 0) {
            for (Node n1 : attr_force_map.keySet()) {
                Map<Node, Force> other_nodes_map = attr_force_map.get(n1);
                for (Node n2 : other_nodes_map.keySet()) {
                    other_nodes_map.get(n2).apply(n1, n2);
                }
            }
        }
        if (this.c_attraction_edges > 0) {
            for (Edge e : edges) {
                attraction_edges.apply(e.getSource(), e.getTarget());
            }
        }
    }

    @Override
    public void goAlgo() {
        graph = graphModel.getGraphVisible();
        Node[] nodes = graph.getNodes().toArray();
        Edge[] edges = graph.getEdges().toArray();

        // reset dx and dy
        for (Node n : nodes) {
            if (n.getNodeData().getLayoutData() == null || !(n.getNodeData().getLayoutData() instanceof CustomLayoutData)) {
                n.getNodeData().setLayoutData(new CustomLayoutData());
            }
            CustomLayoutData layoutData = n.getNodeData().getLayoutData();
            layoutData.dx = 0;
            layoutData.dy = 0;
        }

        // apply forces
        test1(nodes, edges);


        // speed
        for (Node n : nodes) {
            CustomLayoutData layoutData = n.getNodeData().getLayoutData();
            layoutData.dx *= speed;
            layoutData.dy *= speed;
        }

        // gravity
        for (Node n : nodes) {
            NodeData nodeData = n.getNodeData();
            CustomLayoutData layoutData = nodeData.getLayoutData();
            float d = (float) Math.sqrt(nodeData.x() * nodeData.x() + nodeData.y() * nodeData.y());
            float gf = 0.001f;
            layoutData.dx -= gf * nodeData.x() / d;
            layoutData.dy -= gf * nodeData.y() / d;
        }


        // move nodes
        for (Node n : nodes) {
            CustomLayoutData layoutData = n.getNodeData().getLayoutData();
            n.getNodeData().setX(n.getNodeData().x() + layoutData.dx);
            n.getNodeData().setY(n.getNodeData().y() + layoutData.dy);
        }

    }

    // dist_res
    private double dist(NodeData N1, NodeData N2) {
        double xDist = N1.x() - N2.x();
        double yDist = N1.y() - N2.y();
        return Math.sqrt(xDist * xDist + yDist * yDist);
    }

    @Override
    public void endAlgo() {
        graph = graphModel.getGraphVisible();
        Node[] nodes = graph.getNodes().toArray();
        Edge[] edges = graph.getEdges().toArray();
        
        // computations of evaluation output
        // edges
        int longEdges = 0;
        double l = nodeRadius * 50;

        for (int i = 0; i < edges.length; i++) {
            if (dist(edges[i].getSource().getNodeData(), edges[i].getTarget().getNodeData()) > l) {
                longEdges++;
            }
        }
        this.eval_longEdges = (double) longEdges / edges.length;

        // overlap
        int overlappingNodes = 0;
        for (int i = 0; i < nodes.length; i++) {
            NodeData N1 = nodes[i].getNodeData();
            for (int j = i + 1; j < nodes.length; j++) {
                if (dist(N1, nodes[j].getNodeData()) < nodeRadius * 2) {
                    overlappingNodes += 2;
                }
            }
        }
        this.eval_overlap = (double) overlappingNodes / nodes.length;

        // clustering
        double temp = 0;
        for (Node n1 : nodes) {
            NodeData N1 = n1.getNodeData();
            Integer id1 = Integer.parseInt(N1.getId());
            double distCluster = 0;
            double distOthers = 0;
            for (Node n2 : nodes) {
                if (n1 == n2) {
                    continue;
                }
                Integer id2 = Integer.parseInt(n2.getNodeData().getId());
                int t = scr.countCommonCLusters(id1, id2);
                if (t > 0) {
                    distCluster += dist(N1, n2.getNodeData());
                } else {
                    distOthers += dist(N1, n2.getNodeData());
                }
            }
            temp += distCluster / distOthers;
        }
        temp /= nodes.length;
        this.eval_clustering = temp;

        // attributes
        // compute average dist_res dist_attr factor
        double factor = 0;
        int counter = 0;
        for (int i = 0; i < scr.getClusterCount(); i++) {
            for (Node n1 : nodes) {
                Integer id1 = Integer.parseInt(n1.getNodeData().getId());
                if (!scr.getClusters(id1).contains(i)) {
                    continue;
                }
                for (Node n2 : nodes) {
                    Integer id2 = Integer.parseInt(n2.getNodeData().getId());
                    if (n1 == n2 || !scr.getClusters(id2).contains(i)) {
                        continue;
                    }

                    double dist_attr = attrDistance(n1, n2, i);
                    if (dist_attr == 0) {
                        continue;
                    }

                    double dist_res = dist(n1.getNodeData(), n2.getNodeData());
                    factor += dist_res / dist_attr;
                    counter++;
                }
            }
        }
        double dist_res_epsilon = factor / counter * this.epsilon;

        // count pairs with dist_res > dist_res_epsilon        
        temp = 0;
        for (int i = 0; i < scr.getClusterCount(); i++) {
            for (Node n1 : nodes) {
                Integer id1 = Integer.parseInt(n1.getNodeData().getId());
                if (!scr.getClusters(id1).contains(i)) {
                    continue;
                }
                for (Node n2 : nodes) {
                    Integer id2 = Integer.parseInt(n2.getNodeData().getId());
                    if (n1 == n2 || !scr.getClusters(id2).contains(i)) {
                        continue;
                    }
                    double dist_attr = attrDistance(n1, n2, i);
                    if (dist_attr == 0) {
                        continue;
                    }
                    double dist_res = dist(n1.getNodeData(), n2.getNodeData());
                    
                    if ((dist_res > dist_res_epsilon && dist_attr < this.epsilon)
                            || (dist_res < dist_res_epsilon && dist_attr > this.epsilon)) {
                        temp++;
                    }
                }
            }
        }
        this.eval_attributes = temp / counter;

    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String TESTLAYOUT_GENERAL = NbBundle.getMessage(GCVizLayout.class, "TestLayout.general");
        final String TESTLAYOUT_FORCES = NbBundle.getMessage(GCVizLayout.class, "TestLayout.forces");
        final String TESTLAYOUT_EVALOUT = NbBundle.getMessage(GCVizLayout.class, "TestLayout.evalout");
        try {
            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    NbBundle.getMessage(GCVizLayout.class, "TestLayout.clusteringFile.name"),
                    TESTLAYOUT_GENERAL,
                    "TestLayout.clusteringFile.name",
                    NbBundle.getMessage(GCVizLayout.class, "TestLayout.clusteringFile.desc"),
                    "getClusteringFile", "setClusteringFile"));
                    //"getClusteringFile", "setClusteringFile", PathEditor.class));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(GCVizLayout.class, "TestLayout.speed.name"),
                    TESTLAYOUT_GENERAL,
                    "TestLayout.speed.name",
                    NbBundle.getMessage(GCVizLayout.class, "TestLayout.speed.desc"),
                    "getSpeed", "setSpeed"));
            /*properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.p.name"),
                    TESTLAYOUT_GENERAL,
                    "GCVizLayout.p.name",
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.p.desc"),
                    "getP", "setP"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.pInf.name"),
                    TESTLAYOUT_GENERAL,
                    "GCVizLayout.pInf.name",
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.pInf.desc"),
                    "ispInf", "setpInf"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.c_antiOverlap.name"),
                    TESTLAYOUT_FORCES,
                    "GCVizLayout.c_antiOverlap.name",
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.c_antiOverlap.desc"),
                    "getC_antiOverlap", "setC_antiOverlap"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.l_antiOverlap.name"),
                    TESTLAYOUT_FORCES,
                    "GCVizLayout.l_antiOverlap.name",
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.l_antiOverlap.desc"),
                    "getL_antiOverlap", "setL_antiOverlap"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.c_repulsion.name"),
                    TESTLAYOUT_FORCES,
                    "GCVizLayout.c_repulsion.name",
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.c_repulsion.desc"),
                    "getC_repulsion", "setC_repulsion"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.l_repulsion.name"),
                    TESTLAYOUT_FORCES,
                    "GCVizLayout.l_repulsion.name",
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.l_repulsion.desc"),
                    "getL_repulsion", "setL_repulsion"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.c_attraction_edges.name"),
                    TESTLAYOUT_FORCES,
                    "GCVizLayout.c_attraction_edges.name",
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.c_attraction_edges.desc"),
                    "getC_attraction_edges", "setC_attraction_edges"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.c_attraction_clusters.name"),
                    TESTLAYOUT_FORCES,
                    "GCVizLayout.c_attraction_clusters.name",
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.c_attraction_clusters.desc"),
                    "getC_attraction_clusters", "setC_attraction_clusters"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.c_attributes.name"),
                    TESTLAYOUT_FORCES,
                    "GCVizLayout.c_attributes.name",
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.c_attributes.desc"),
                    "getC_attributes", "setC_attributes"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.l_repulsion_attributes.name"),
                    TESTLAYOUT_FORCES,
                    "GCVizLayout.l_repulsion_attributes.name",
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.l_repulsion_attributes.desc"),
                    "getL_repulsion_attributes", "setL_repulsion_attributes"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.epsilon.name"),
                    TESTLAYOUT_FORCES,
                    "GCVizLayout.epsilon.name",
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.epsilon.desc"),
                    "getEpsilon", "setEpsilon"));*/
        /*    properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.eval_longEdges.name"),
                    TESTLAYOUT_EVALOUT,
                    "GCVizLayout.eval_longEdges.name",
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.eval_longEdges.desc"),
                    "getEval_LongEdges", "setEval_LongEdges"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.eval_Clustering.name"),
                    TESTLAYOUT_EVALOUT,
                    "GCVizLayout.eval_Clustering.name",
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.eval_Clustering.desc"),
                    "getEval_Clustering", "setEval_Clustering"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.eval_Overlap.name"),
                    TESTLAYOUT_EVALOUT,
                    "GCVizLayout.eval_Overlap.name",
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.eval_Overlap.desc"),
                    "getEval_Overlap", "setEval_Overlap"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.eval_Attributes.name"),
                    TESTLAYOUT_EVALOUT,
                    "GCVizLayout.eval_Attributes.name",
                    NbBundle.getMessage(GCVizLayout.class, "GCVizLayout.eval_Attributes.desc"),
                    "getEval_Attributes", "setEval_Attributes"));*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties.toArray(new LayoutProperty[0]);
    }

    public String getClusteringFile() {
        return this.clusteringFile;
    }

    public void setClusteringFile(String file) {
        this.clusteringFile = file;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public double getP() {
        return p;
    }

    public void setP(Double p) {
        this.p = p;
    }

    public boolean ispInf() {
        return pInf;
    }

    public void setpInf(Boolean pInf) {
        this.pInf = pInf;
    }

    public float getC_antiOverlap() {
        return c_antiOverlap;
    }

    public void setC_antiOverlap(Float c_anitOverlap) {
        this.c_antiOverlap = c_anitOverlap;
    }

    public float getC_attributes() {
        return c_attributes;
    }

    public void setC_attributes(Float c_attributes) {
        this.c_attributes = c_attributes;
    }

    public float getC_attraction_clusters() {
        return c_attraction_clusters;
    }

    public void setC_attraction_clusters(Float c_attraction_clusters) {
        this.c_attraction_clusters = c_attraction_clusters;
    }

    public float getC_attraction_edges() {
        return c_attraction_edges;
    }

    public void setC_attraction_edges(Float c_attraction_edges) {
        this.c_attraction_edges = c_attraction_edges;
    }

    public float getC_repulsion() {
        return c_repulsion;
    }

    public void setC_repulsion(Float c_repulsion) {
        this.c_repulsion = c_repulsion;
    }

    public float getL_antiOverlap() {
        return l_antiOverlap;
    }

    public void setL_antiOverlap(Float l_antiOverlap) {
        this.l_antiOverlap = l_antiOverlap;
    }

    public float getL_repulsion() {
        return l_repulsion;
    }

    public void setL_repulsion(Float l_repulsion) {
        this.l_repulsion = l_repulsion;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(Double epsilon) {
        this.epsilon = epsilon;
    }

    public float getL_repulsion_attributes() {
        return l_repulsion_attributes;
    }

    public void setL_repulsion_attributes(Float l_repulsion_attributes) {
        this.l_repulsion_attributes = l_repulsion_attributes;
    }

    public double getEval_LongEdges() {
        return this.eval_longEdges;
    }

    public void setEval_LongEdges(Double x) {
        this.eval_longEdges = x;
    }

    public double getEval_Clustering() {
        return this.eval_clustering;
    }

    public void setEval_Clustering(Double x) {
        this.eval_clustering = x;
    }

    public double getEval_Overlap() {
        return this.eval_overlap;
    }

    public void setEval_Overlap(Double x) {
        this.eval_overlap = x;
    }

    public double getEval_Attributes() {
        return this.eval_attributes;
    }

    public void setEval_Attributes(Double x) {
        this.eval_attributes = x;
    }

    @Override
    public void resetPropertiesValues() {
        //clusteringFile = "T:/users/haag/Bachelorarbeit/Graphen/Graph1.true";
        //clusteringFile = "T:/users/haag/Bachelorarbeit/Graphen/beispiel_dichtebasiert0.true";
        //clusteringFile = "T:/users/haag/Bachelorarbeit/Graphen/beispiel_dichtebasiert_geringeDichte.true";
        //clusteringFile = "T:/users/haag/Bachelorarbeit/Graphen/beispiel_beispiel_dichtebasiert_kleineCluster.true";
        //clusteringFile = "T:/users/haag/Bachelorarbeit/Graphen/beispiel_dichtebasiert_mitUeberlappung.true";
        //clusteringFile = "T:/users/haag/Bachelorarbeit/Graphen/beispiel_dichtebasiert_grosserGraph.true";
        //clusteringFile = "T:/users/haag/Bachelorarbeit/Graphen/beispiel_gamer.true";        
        //clusteringFile = "T:/users/haag/Bachelorarbeit/Graphen/beispiel_dichtebasiert_vielNoise.true";
        //clusteringFile = "T:/users/haag/Bachelorarbeit/Graphen/beispiel_dichtebasiert_vielNoise2.true";
        
        //clusteringFile = "C:/Users/Roman/Desktop/Bachelorarbeit/Graphen/Graph1.true";
        //clusteringFile = "C:/Users/Roman/Desktop/Bachelorarbeit/Graphen/beispiel_dichtebasiert0.true";
        //clusteringFile = "C:/Users/Roman/Desktop/Bachelorarbeit/Graphen/beispiel_dichtebasiert_kleineCluster.true";
        //clusteringFile = "C:/Users/Roman/Desktop/Bachelorarbeit/Graphen/beispiel_dichtebasiert_mitUeberlappung.true";
        //clusteringFile = "C:/Users/Roman/Desktop/Bachelorarbeit/Graphen/Graph1.true";
        //clusteringFile = "C:/Users/Roman/Desktop/Bachelorarbeit/Graphen/beispiel_gamer.true";

        clusteringFile = "D:/test.found";
        
        speed = 1.0;
        pInf = true;
        p = 2.0;

        c_antiOverlap = 0f;
        l_antiOverlap = 5f;
        c_repulsion = 10f;
        l_repulsion = 100f;
        c_attraction_edges = 0.001f;
        c_attraction_clusters = 0f;
        c_attributes = 0.002f;
        l_repulsion_attributes = 20f;
        epsilon = 0.1;

        //eval_longEdges = 0.0;
        //eval_clustering = 0.0;
        //eval_overlap = 0.0;
        //eval_attributes = 0.0;
    }
}
