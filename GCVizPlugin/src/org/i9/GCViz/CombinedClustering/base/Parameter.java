package org.i9.GCViz.CombinedClustering.base;

public class Parameter {

    public static int n_min=6;
    public static double gamma_min=0.6;
    public static int s_min=2;
    public static double w_max=1;
    public static int k=2;
    public static double epsilon=1;
    public static int min_pts=5;
    public static int numberOfAtts; //Read from the graph file
    
    public static double param_a=1; 	//density
    public static double param_b=1;	//number of nodes
    public static double param_c=1;  //dimensionality
    public static double r_dim=0.5;
    public static double r_obj=0.5;
    
    //pruning techniques
    public static boolean subspace_pruning=true;
    public static boolean lookahead=true;
    public static boolean quality_estimation_pruning=true;
    public static boolean diameter_pruning=true;
    public static boolean degree_pruning=true;
    public static boolean upper_bound_pruning=true;
    public static boolean lower_bound_pruning=true;
    public static boolean redundancy_test=true;
     

}
