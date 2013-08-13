package org.i9.GCViz.CombinedClustering.algorithms;

import org.i9.GCViz.CombinedClustering.base.DenseCluster;
import org.i9.GCViz.CombinedClustering.base.DenseClusterCandidates;
import org.i9.GCViz.CombinedClustering.base.DenseClusterComparator;
import org.i9.GCViz.CombinedClustering.base.Log;
import org.i9.GCViz.CombinedClustering.base.Parameter;
import org.i9.GCViz.CombinedClustering.base.Subspace;
import org.i9.GCViz.CombinedClustering.base.Timer;
import org.i9.GCViz.CombinedClustering.graph.Graph;
import org.i9.GCViz.CombinedClustering.graph.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

public class DBCSC {
	
	static boolean erweitert=false;
	static boolean lookahead=false;

	public static void main(String foundfile,Graph myGraph) {
		
        //log file
        Log found = new Log(foundfile,true,false,true);

        Timer timer= new Timer();
        timer.start();
		
	myGraph.densityPruning();
		
    	DenseClusterComparator comp = new DenseClusterComparator();
    	PriorityQueue<DenseCluster> clustering = new PriorityQueue<DenseCluster>(10,comp);
    	
    	//Result set
    	ArrayList<DenseCluster> non_red_clusters = new ArrayList<DenseCluster>();
    	
    	Subspace empty_sub = new Subspace();
    	for (int dim=0;dim<Parameter.numberOfAtts;dim++){
    		empty_sub.removeDimension(dim);
    	}
    	
    	for (int dim =0;dim < Parameter.numberOfAtts;dim++){
    		Subspace one_dim = empty_sub.copy();
    		one_dim.setDimension(dim, Double.NaN, Double.NaN);
    		
    		dfs_traversal(one_dim,myGraph.getNodes(),new ArrayList<DenseCluster>(), clustering, myGraph);
    	}
    	
    	while(!clustering.isEmpty()) {
    		DenseCluster cluster1 = clustering.poll();
    		if(cluster1 instanceof DenseClusterCandidates) {
    			
    			boolean better_cluster_in_output =false;
    			
    			for (DenseCluster better_cluster : ((DenseClusterCandidates) cluster1).getBetterParents() ){
    				if(non_red_clusters.contains(better_cluster)){
    					better_cluster_in_output = true;
    					break;
    				}
    			}
    			
    			if(!better_cluster_in_output) {
    				
    				dfs_traversal(cluster1.getSubspace(), cluster1.getNodes(),((DenseClusterCandidates) cluster1).getParents(), clustering, myGraph);
    			} 
    			    			
    		} else {	
	    		double quality1= cluster1.getQuality();
	    		
	    		boolean redundant = false; 
	    		for(DenseCluster cluster2 : non_red_clusters) {
	    			double quality2=cluster2.getQuality();
	    			if(quality1 < quality2) {
	    				
	    				ArrayList<Node> intersection = new ArrayList<Node>(cluster1.getNodes());
	    				intersection.retainAll(cluster2.getNodes());
	    				if(intersection.size()>=Parameter.r_obj*cluster1.getNodes().size()) {
	    					
	    					boolean[]dim1 = cluster1.getSubspace().getDimensions();
	    					boolean[]dim2 = cluster2.getSubspace().getDimensions();
	    					int intersectDims = 0;
	    					for(int i =0;i<dim1.length;i++) {
	    						if(dim1[i] && dim2[i]){
	    							intersectDims++;
	    						}
	    					}
	    					if(intersectDims >= Parameter.r_dim*cluster1.getSubspace().size()) {
		    					
		    					redundant=true;
		    					break;
	    					}
	    				}
		    		} else {
		    			break;
		    		}
	    		}
	    		if(!redundant){
					non_red_clusters.add(cluster1);
					found.log(cluster1.toString()+"\n");
				}
    		}
    	}
    	String runtime = timer.toString();
    	found.log(runtime);
    }
		
		
	public static void dfs_traversal(Subspace sub, Collection<Node> cands, Collection<DenseCluster> parents, PriorityQueue<DenseCluster> queue, Graph orig_graph){
		
		
		
		ArrayList<DenseCluster> found_clusters = new ArrayList<DenseCluster>();
		ArrayList<Collection<Node>> prelim_clusters = new ArrayList<Collection<Node>>();
		prelim_clusters.add(cands);
		
		while(!prelim_clusters.isEmpty()){
			HashSet<Node> current_orig = new HashSet<Node>(prelim_clusters.get(0));
			prelim_clusters.remove(0);
			
			HashSet<Node> current = new HashSet<Node>();
			
			for(Node node : current_orig){
				Node new_node = node.copyWithoutNeighbours();
				current.add(new_node);
			}
			
			for(Node node : orig_graph.getNodes()){
				
				for(Node current_node: current){
					if(current_node.getID() == node.getID()){
						for(Node neighbor : node.getNeighbors()){
							for (Node current_neighbor : current){
								if(current_neighbor.getID() == neighbor.getID()){
									current_node.addNeighbor(current_neighbor);
									current_neighbor.addNeighbor(current_node);
									break;
								}
							}
						}
						break;
					}
				}
			}
			
			
			
			Graph enriched = getEnrichedSubgraph(new Graph(current,Parameter.numberOfAtts), sub);
			
			ArrayList<HashSet<Node>> cores = enriched.detect_cores();
			
			if(cores.size()==1 && cores.get(0).size() == current.size()){
				
				HashSet<Node> node_set = new HashSet<Node>();
				for(Node node : current){
					node_set.add(orig_graph.getNodeHavingID(node.getID()));
				}
				DenseCluster cluster = new DenseCluster(node_set, sub, DenseCluster.quality(current.size(), sub.size()));
				found_clusters.add(cluster);
			} else {
				prelim_clusters.addAll(cores);
			}
			
		}
		if(sub.size()>=Parameter.s_min) {
			queue.addAll(found_clusters);
		}
		
		for(DenseCluster cluster: found_clusters){
			
			for (int dim=sub.get_max_d()+1;dim<Parameter.numberOfAtts;dim++){
				
				Subspace new_sub = sub.copy();
				new_sub.setDimension(dim, Double.NaN, Double.NaN);
				int addable_dims = Parameter.numberOfAtts - new_sub.get_max_d() - 1; 
				
				ArrayList<DenseCluster> new_parents = new ArrayList<DenseCluster>(parents);
				if(sub.size()>=Parameter.s_min){
					new_parents.add(cluster);
				}
				
				
				ArrayList<DenseCluster> new_better_parents =new ArrayList<DenseCluster>();
				double q_max=cluster.getNodes().size() * (new_sub.size() + addable_dims); 
                                
				for(DenseCluster parent : new_parents){
					
					if(parent.getQuality()> q_max && parent.getSubspace().size() >= Parameter.r_dim * (new_sub.size() + addable_dims)){
						new_better_parents.add(parent);
					}
				}
				
				if (new_better_parents.isEmpty()){
					
					boolean redundant = false; 
					if(erweitert){
						
						
			    		for(DenseCluster cluster2 : queue) {
			    			double quality2=cluster2.getQuality();
			    			if(q_max < quality2) {
			    				if(cluster2 instanceof DenseCluster){
				    				
				    				ArrayList<Node> non_overlapping = new ArrayList<Node>(cluster.getNodes());
				    				non_overlapping.removeAll(cluster2.getNodes()); 
				    				if(non_overlapping.size()<=Parameter.min_pts * (1 - Parameter.r_obj)) { 
				    					
				    					boolean[]dim1 = cluster.getSubspace().getDimensions();
				    					boolean[]dim2 = cluster2.getSubspace().getDimensions();
				    					int overlapping_Dims=0; 
				    					for(int i =0;i<dim1.length;i++) {
				    						if(dim1[i] && dim2[i] || (i > cluster.getSubspace().get_max_d() && dim2[i])){
				    							overlapping_Dims++;
				    						}
				    					}
				    					
				    					if(overlapping_Dims >= cluster.getSubspace().size() + addable_dims) {
					    					redundant=true;
					    					new_better_parents.add(cluster2);
					    					break;
				    					}
				    				}
			    				}
				    		} else {
				    			break;
				    		}
			    		}
					}
					
					
					if(!redundant && lookahead){
						Subspace lookahead_sub = cluster.getSubspace().copy();
						for(int add_dim=lookahead_sub.get_max_d()+1;add_dim < Parameter.numberOfAtts;add_dim++){
							lookahead_sub.setDimension(add_dim, Double.NaN, Double.NaN);
						}
						
						HashSet<Node> clusterWithInternalEdges = new HashSet<Node>();
						for(Node node : cluster.getNodes()){
							Node copy = node.copyWithoutNeighbours();
							clusterWithInternalEdges.add(copy);
						}
						for(Node node : clusterWithInternalEdges)
							for(Node neighbor : node.getNeighbors()){
								if(clusterWithInternalEdges.contains(neighbor)){
									node.addNeighbor(neighbor);
								}
							}
						
						
						Graph lookahead_enriched = getEnrichedSubgraph(new Graph(clusterWithInternalEdges,Parameter.numberOfAtts), lookahead_sub);
						
						
						ArrayList<HashSet<Node>> cores = lookahead_enriched.detect_cores();
						
						if(cores.size()==1 && cores.get(0).size() == cluster.getNodes().size()){
							
							
							DenseCluster lookahead_cluster = new DenseCluster(new HashSet<Node>(cluster.getNodes()), lookahead_sub, DenseCluster.quality(cluster.getNodes().size(), lookahead_sub.size()));
							queue.add(lookahead_cluster);
							redundant = true;
							new_better_parents.add(lookahead_cluster);
						} 
					}
					
					
					if(!erweitert || !redundant){
						dfs_traversal(new_sub, new HashSet<Node>(cluster.getNodes()), new_parents , queue, orig_graph);
					} else {
						
						DenseClusterCandidates subtree = new DenseClusterCandidates(new HashSet<Node>(cluster.getNodes()), new_sub, q_max, new_parents, new_better_parents);
						queue.add(subtree);
					}
				} else {
					
					DenseClusterCandidates subtree = new DenseClusterCandidates(new HashSet<Node>(cluster.getNodes()), new_sub, q_max, new_parents, new_better_parents);
					queue.add(subtree);
				}
			}
		}
	}
	
	
	
	
	 public static Graph getEnrichedSubgraph(Graph baseGraph, Subspace sub) {
		 Graph result = getEnrichedSubgraph(baseGraph, Parameter.k);
		 
		 
		
		 for (Node node :  result.getNodes()){
			 for (Iterator<Node> it= node.getNeighbors().iterator();it.hasNext();){
				 Node neighbour = it.next();
				 for(int i=0;i<Parameter.numberOfAtts;i++){
					 if(sub.hasDimension(i)){
						 if(Math.abs(node.getAttribute(i) - neighbour.getAttribute(i))>Parameter.epsilon){
							 it.remove();
							 neighbour.getNeighbors().remove(node);
							 break;
						 }
					 }
				 }
			 }
		 }
		 
		 return result;
	 }

	
	 private static Graph getEnrichedSubgraph(Graph baseGraph, int k_min) {
	      
	        Graph enrichedGraph = baseGraph.copy();
	        while (k_min > 1) {
	            Graph tempGraph = enrichedGraph.copy();
	            for (Node node : enrichedGraph.getNodes()) {
	                for (Node neighbour : node.getNeighbors()) {
	                    for (Node extendedNeighbour : neighbour.getNeighbors()) {
	                        // check 1. if the new proposed edge is already an existing edge
	                        if (node.getNeighbors().contains(extendedNeighbour) == false && node.getID() != extendedNeighbour.getID()) {
	                            Node n1 = tempGraph.getNodeHavingID(node.getID());
	                            Node n2 = tempGraph.getNodeHavingID(extendedNeighbour.getID());
	                            tempGraph.addEdge(n1, n2);
	                        }
	                    }
	                }
	            }
	            k_min--;
	            enrichedGraph = tempGraph;
	        }
	        return enrichedGraph;
	    }


}
