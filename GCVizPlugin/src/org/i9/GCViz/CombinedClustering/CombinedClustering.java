package org.i9.GCViz.CombinedClustering;

import org.i9.GCViz.CombinedClustering.base.Parameter;
import java.util.*;
import org.gephi.clustering.api.Cluster;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.i9.GCViz.CombinedClustering.algorithms.DBCSC;
import org.i9.GCViz.CombinedClustering.algorithms.GAMER;
import org.apache.commons.collections15.map.FastHashMap;
import org.gephi.data.attributes.api.AttributeRow;


public class CombinedClustering {
     
 
    private Cluster[] clusters;
    private ProgressTicket progressTicket;
    private boolean cancelled;

    public void execute(GraphModel model, String algorithm, String path) {
        cancelled = false;
        Progress.start(progressTicket);
        Progress.setDisplayName(progressTicket, "Combined Clustering");

        org.gephi.graph.api.Graph gephi_graph = model.getGraphVisible();
        gephi_graph.readLock();
        
        //Determine attributes in graph
        HashSet<String> attributes= new HashSet<String>();
        
        for (Iterator<org.gephi.graph.api.Node> it = gephi_graph.getNodes().iterator(); it.hasNext();) {
            org.gephi.graph.api.Node node = it.next();
            AttributeRow row = (AttributeRow) node.getAttributes();
            for (int i = 2; i < node.getAttributes().countValues(); i++) {
                attributes.add(row.getColumnAt(i).getTitle());
            }
        }
        
        ArrayList<String> attribute_list = new ArrayList<String>(attributes);
        Collections.sort(attribute_list);
            
        //Convert graph to the i9 graph format
        org.i9.GCViz.CombinedClustering.graph.Graph myGraph = new org.i9.GCViz.CombinedClustering.graph.Graph(attribute_list.size());
        Parameter.numberOfAtts=attribute_list.size();
        
        HashMap<org.gephi.graph.api.Node,org.i9.GCViz.CombinedClustering.graph.Node> node_map = new FastHashMap<Node, org.i9.GCViz.CombinedClustering.graph.Node>();
        
        for (Iterator<org.gephi.graph.api.Node> it = gephi_graph.getNodes().iterator(); it.hasNext();) {
            org.gephi.graph.api.Node node = it.next();
            int id=Integer.parseInt(node.getNodeData().getId());
            AttributeRow row = (AttributeRow) node.getAttributes();
            double atts[] = new double[attribute_list.size()];
            for (int i=0;i<atts.length;i++) {
                String att_name = attribute_list.get(i);
                Object value_obj = node.getAttributes().getValue(att_name);
                if(value_obj==null){
                    atts[i]=Double.NaN;
                } else {
                    atts[i]=Double.parseDouble(value_obj.toString());
                }
            }
            org.i9.GCViz.CombinedClustering.graph.Node new_node = new org.i9.GCViz.CombinedClustering.graph.Node(id, atts);
            myGraph.addNode(new_node);
            node_map.put(node, new_node);
        }
        //add edges
        for (Iterator<org.gephi.graph.api.Edge> it = gephi_graph.getEdges().iterator(); it.hasNext();) {
            org.gephi.graph.api.Edge edge = it.next();
            org.i9.GCViz.CombinedClustering.graph.Node node1 = node_map.get(edge.getSource());
            org.i9.GCViz.CombinedClustering.graph.Node node2 = node_map.get(edge.getTarget());
            if(node1!=node2){
                myGraph.addEdge(node1,node2);
                myGraph.addEdge(node2,node1);
            }
        }
        
        
        
        if (algorithm.equals("DB-CSC")) {
            DBCSC.main(path, myGraph);
        }	        

        else if (algorithm.equals("GAMer")){
            GAMER.main(path, myGraph); 
        }

        
        

       

        gephi_graph.readUnlock();

      

        if (cancelled) {
            return;
        }

   

        if (cancelled) {
            return;
        }
     Progress.finish(progressTicket);
    }

    public Cluster[] getClusters() {
        return clusters;
    }

    public boolean cancel() {
        cancelled = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

  
     
    //Getters and Setters
    public int getN_min() {
        return Parameter.n_min;
    }

    public void setN_min(int n_min) {
        Parameter.n_min = n_min;
    }
    //-------------------
    public double getGamma_min() {
        return Parameter.gamma_min;
    }

    public void setGamma_min(double gamma_min) {
        Parameter.gamma_min = gamma_min;
    }
    //---------------------
    public int getS_min() {
        return Parameter.s_min;
    }

    public void setS_min(int s_min) {
        Parameter.s_min = s_min;
    }
    //--------------------
    public double getParam_a() {
        return Parameter.param_a;
    }

    public void setParam_a(double param_a) {
        Parameter.param_a = param_a;
    }
    //-----------------------
    public double getParam_b() {
        return Parameter.param_a;
    }

    public void setParam_b(double param_b) {
        Parameter.param_b = param_b;
    }
    //-----------------------
    public double getParam_c() {
        return Parameter.param_c;
    }

    public void setParam_c(double param_c) {
        Parameter.param_c = param_c;
    }
    //-----------------------
     public double getW_max() {
        return Parameter.w_max;
    }

    public void setW_max(double w_max) {
        Parameter.w_max = w_max;
    }
    
    public int getK() {
        return Parameter.k;
    }

    public void setK(int k) {
        Parameter.k = k;
    }
    //-----------------------
    public double getEpsilon() {
        return Parameter.epsilon;
    }

    public void setEpsilon(double epsilon) {
        Parameter.epsilon = epsilon;
    }
    //-----------------------
    public int getMin_pts() {
        return Parameter.min_pts;
    }

    public void setMin_pts(int min_pts) {
        Parameter.min_pts = min_pts;
    }
    //-----------------------
    public double getR_dim() {
        return Parameter.r_dim;
    }

    public void setR_dim(double r_dim) {
        Parameter.r_dim = r_dim;
    }
    //------------------------
    public double getR_obj() {
        return Parameter.r_obj;
    }

    public void setR_obj(double r_obj) {
        Parameter.r_obj = r_obj;
    }
        
}
