package org.i9.GCViz.CombinedClustering.algorithms;

import org.i9.GCViz.CombinedClustering.graph.NodeComparatorInDegreeDesc;
import org.i9.GCViz.CombinedClustering.graph.Node;
import org.i9.GCViz.CombinedClustering.graph.Graph;
import org.i9.GCViz.CombinedClustering.base.Timer;
import org.i9.GCViz.CombinedClustering.base.ClusterCandidates;
import org.i9.GCViz.CombinedClustering.base.Cluster;
import org.i9.GCViz.CombinedClustering.base.Subspace;
import org.i9.GCViz.CombinedClustering.base.ClusterComparator;
import org.i9.GCViz.CombinedClustering.base.Log;
import org.i9.GCViz.CombinedClustering.base.Parameter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.ArrayList;


public class GAMER {
	
	static NodeComparatorInDegreeDesc nodecomp;

	public static void main(String config_file,Graph myGraph) {
		
		
		String foundfile = config_file.replace(".properties", "_gamer.found");
		Log found = new Log(foundfile,true,false,true);
    	
    	Timer timer= new Timer();
		timer.start();
		
		
    	myGraph.pruneGraph();
    	double quality_sum = 0;
		
		ArrayList<Node> remainingNodes = new ArrayList<Node>(myGraph.getNodes());
		ArrayList<HashSet<Node>> connectedComponents = new ArrayList<HashSet<Node>>();
		while(!remainingNodes.isEmpty()){
			Node node = remainingNodes.get(0);
			HashSet<Node> connected = node.connected_component(remainingNodes);
			connected.add(node);
			if(connected.size()>=Parameter.n_min){
				connectedComponents.add(connected);
			}
			remainingNodes.removeAll(connected);
		}
		for(HashSet<Node> v_i : connectedComponents){
	    	
	    	ClusterComparator comp = new ClusterComparator();
	    	PriorityQueue<Cluster> clustering = new PriorityQueue<Cluster>(10,comp);
	    	
	    	detectCluster(new HashSet<Node>(),v_i, new Subspace(),clustering, null,null, Parameter.subspace_pruning, Parameter.lookahead, Parameter.quality_estimation_pruning);
	    	
	    	ArrayList<Cluster> non_red_clusters = new ArrayList<Cluster>();
	    	
	    	while(!clustering.isEmpty()) {
	    		Cluster cluster1 = clustering.poll();
	    		if(cluster1 instanceof ClusterCandidates) {
	    			
	    			if(((ClusterCandidates) cluster1).getBetterCluster() == null|| !non_red_clusters.contains( ((ClusterCandidates) cluster1).getBetterCluster())) {
	    				
	    				boolean redundant = false;
	    				if(Parameter.redundancy_test){
		    				for(Cluster cluster2 : non_red_clusters){
		    					if(cluster1.getQuality()< cluster2.getQuality()){
		    						int dims_yohnec=0;
		    						boolean[]dim1 = cluster1.getSubspace().getDimensions();
			    					boolean[]dim2 = cluster2.getSubspace().getDimensions();
		    						for(int dim =0;dim < Parameter.numberOfAtts;dim ++){
		    							if(dim1[dim] && !dim2[dim]){
		    								dims_yohnec++;
		    							}
		    						}
		    						if((Parameter.s_min - dims_yohnec)/Parameter.s_min >= Parameter.r_dim){
		    							HashSet<Node> intersect =new HashSet<Node>(cluster1.getNodes());
		    							intersect.retainAll(cluster2.getNodes());
		    							HashSet<Node> candyohnec = new HashSet<Node>(((ClusterCandidates)cluster1).getCands());
		    							candyohnec.removeAll(cluster2.getNodes());
		    							if((intersect.size()+Math.max(0,Parameter.n_min-cluster1.getNodes().size()-candyohnec.size()))/Math.max(Parameter.n_min, cluster1.getNodes().size()+candyohnec.size())>=Parameter.r_obj){
		    								redundant=true;
		    								//System.out.println("zu anderem redundant");
		    								break;
		    							}
		    						}
		    					}
		    				}
	    				}
	    				if(!redundant){
	    					
	    					detectCluster(cluster1.getNodes(), ((ClusterCandidates) cluster1).getCands(), cluster1.getSubspace(), clustering, null,null,Parameter.subspace_pruning, false,Parameter.quality_estimation_pruning);
	    				}
	    			} 
	    			
	    		} else {	
		    		double quality1= cluster1.getQuality();
		    	
		    		boolean redundant = false; 
		    		for(Cluster cluster2 : non_red_clusters) {
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
			    		}
		    		}
		    		if(!redundant){
		    			quality_sum+=cluster1.getQuality();
						non_red_clusters.add(cluster1);
						found.log(cluster1.toString()+"\n");
					}
	    		}
	    	}
		}
    	DecimalFormat form = new DecimalFormat("#0.00");
    	found.log("sum: "+form.format(quality_sum)+"\n");
    	String runtime = timer.toString();
    	found.log(runtime);
    }
	
	private static void detectCluster(Collection<Node> set_X, Collection<Node> cand_X, Subspace sub_X, PriorityQueue<Cluster> result, HashMap<Node,Integer> indegs_X, HashMap<Node,Integer> exdegs_X, boolean subspace_pruning, boolean lookahead, boolean quality_estimation_pruning){
		
		if(cand_X.isEmpty()){
			return;
		}
		
		
		ArrayList<Node> neighbors_X = new ArrayList<Node> (cand_X);
		if(!set_X.isEmpty()){
			for(Iterator<Node> it = neighbors_X.iterator();it.hasNext();){
				Node node = it.next();
				if(!node.hasEdgeIn(set_X)){
					it.remove();
				}
			}
		}
		
		Subspace lookahead_sub;
		lookahead_sub = sub_X.copy();
		
		for(Node node : cand_X) {
			lookahead_sub.intersect(node);
		}
		
		
		if(lookahead_sub.size() >= Parameter.s_min) {
			subspace_pruning = false;
		}
		
		if(indegs_X ==null || exdegs_X ==null){
			
			indegs_X = new HashMap<Node, Integer>();
			exdegs_X = new HashMap<Node, Integer>();
			for(Node node : set_X){
				int[] indegs = node.indeg(set_X, sub_X, subspace_pruning);
				int[] exdegs = node.exdeg(cand_X, sub_X, subspace_pruning,indegs);
				Arrays.sort(indegs);
				Arrays.sort(exdegs);
				indegs_X.put(node, Math.max(indegs[indegs.length-Parameter.s_min],0));
				exdegs_X.put(node, exdegs[exdegs.length-Parameter.s_min]);
			}
			for(Node node : cand_X){
				int[] indegs = node.indeg(set_X, sub_X, subspace_pruning);
				int[] exdegs = node.exdeg(cand_X, sub_X, subspace_pruning,indegs);
				Arrays.sort(indegs);
				Arrays.sort(exdegs);
				indegs_X.put(node, Math.max(indegs[indegs.length-Parameter.s_min],0));
				exdegs_X.put(node, exdegs[exdegs.length-Parameter.s_min]);
			}
		}
		
		if(lookahead){
			
			HashSet<Node> lookahead_set = new HashSet<Node>(set_X);
			lookahead_set.addAll(cand_X);
			double lookahead_density= Graph.qclq_density(lookahead_set);
			int lookahead_dimensionality = lookahead_sub.size();
			if(lookahead_set.size()>=Parameter.n_min && lookahead_dimensionality >= Parameter.s_min && lookahead_density>=Parameter.gamma_min){
				
				double quality = Cluster.quality(lookahead_density, lookahead_set.size(), lookahead_dimensionality);
				
				
				int min_deg = set_X.size();
				for(Node node : set_X) {
					
					int deg = indegs_X.get(node) + exdegs_X.get(node);
					if (deg < min_deg) {
						min_deg = deg;
					}
				}
				int n_max = lookahead_set.size() -1;
				double gamma_max =((double)min_deg)/(set_X.size()); 
				int s_max;
				if(sub_X != null) {
					s_max = sub_X.size();
				}else{
					s_max = 0;
				}
					
				double max_quality = Cluster.quality(gamma_max,n_max,s_max);
				
				if(max_quality < quality && (s_max * Parameter.r_dim) <= lookahead_sub.size()) {
					
					
					Cluster cluster = new Cluster(lookahead_set, lookahead_sub, quality);
					result.add(cluster);
					
					result.add(new ClusterCandidates(new HashSet<Node>(set_X),sub_X.copy(),new HashSet<Node>(cand_X),max_quality,cluster));
					return;
				}
			}
		}

		nodecomp = new NodeComparatorInDegreeDesc(indegs_X);
		
	
		Collections.sort(neighbors_X, nodecomp);
		
		for(Node v : neighbors_X){
			
			
			if(set_X.size() + cand_X.size() < Parameter.n_min) {
				return;
			}
			
			HashSet<Node> set_Y = new HashSet<Node>(set_X);
			set_Y.add(v);
			cand_X.remove(v);
			
			Subspace sub_Y = new Subspace();
			
			
			sub_Y = sub_X.copy();
			sub_Y.intersect(v);
			if(sub_Y.size() < Parameter.s_min) {
    			continue;
    		}
			
			
        	int n = set_Y.size()+cand_X.size();
        	int k = n;
        	
        	if(Parameter.gamma_min > ((n-2.0)/(n-1.0))) {
        		k = 1;
        	} else if(Parameter.gamma_min>=0.5)  {
        		k = 2;
        	} else if(Parameter.gamma_min>=(2.0/(n-1)) && n%Math.ceil(Parameter.gamma_min*(n-1)+1) == 0)  {
        		k = 3 * (int) Math.floor(n/(Parameter.gamma_min*(n-1)+1)) - 3;
        	} else if(Parameter.gamma_min>=(2.0/(n-1)) && n%Math.ceil(Parameter.gamma_min*(n-1)+1) == 1)  {
        		k = 3 * (int) Math.floor(n/(Parameter.gamma_min*(n-1)+1)) - 2;
        	} else if(Parameter.gamma_min>=(2.0/(n-1)) && n%Math.ceil(Parameter.gamma_min*(n-1)+1) >= 2)  {
        		k = 3 * (int) Math.floor(n/(Parameter.gamma_min*(n-1)+1)) - 1;
        	} else if(Parameter.gamma_min>=1.0/(n-1))  {
        		k= n-1;
        	}
			
        	HashSet<Node> cand_Y;
        	if(Parameter.diameter_pruning){
	        
				if(subspace_pruning) {
					cand_Y = v.k_neighborhood(cand_X, sub_Y, k);
				} else {
					cand_Y = v.k_neighborhood(cand_X, k);
				}
        	} else{
        		cand_Y = new HashSet<Node>(cand_X);
        	}
			int lower_bound_Y=0;
			int upper_bound_Y=0;
			HashSet<Node> set_Z = new HashSet<Node>();
			HashMap<Node,Integer> indegs_Y;
			HashMap<Node,Integer> exdegs_Y;
			
			do{
				
				indegs_Y = new HashMap<Node, Integer>();
				exdegs_Y = new HashMap<Node, Integer>();
			
				
				if(Parameter.upper_bound_pruning){
					upper_bound_Y = upper(set_Y,cand_Y, sub_Y,subspace_pruning, indegs_Y, exdegs_Y);
				}
				if(Parameter.lower_bound_pruning){
					lower_bound_Y = lower(set_Y,cand_Y, sub_Y,subspace_pruning,indegs_Y);
				}
				
				set_Z.clear();
				
				for (Node node : set_X) {
        			boolean test1,test2,test3;
        			int indeg = 0;
        			int exdeg =0;
        			if(Parameter.upper_bound_pruning){        			
	        			indeg = indegs_Y.get(node);
	        			exdeg = exdegs_Y.get(node);
        			} else {
        				int[] indegs = node.indeg(set_Y, sub_Y, subspace_pruning);
        				int[] exdegs = node.exdeg(cand_Y, sub_Y, subspace_pruning,indegs);
        				Arrays.sort(indegs);
        				Arrays.sort(exdegs);
        				indeg = Math.max(indegs[indegs.length-Parameter.s_min],0);
        				exdeg = exdegs[exdegs.length-Parameter.s_min];
        				indegs_Y.put(node,indeg);
        				exdegs_Y.put(node,exdeg);
        			}
        			
        			int size = set_Y.size();

        			test1 = (Parameter.degree_pruning?indeg+exdeg < Math.ceil(Parameter.gamma_min * (size + exdeg-1)):false);
        			test2 = (Parameter.upper_bound_pruning?indeg + upper_bound_Y < Math.ceil(Parameter.gamma_min * (size + upper_bound_Y-1)):false);
        			test3 = (Parameter.lower_bound_pruning?indeg + exdeg < Math.ceil(Parameter.gamma_min * (size + lower_bound_Y -1)):false);
        				
        			if(test1 || test2 || test3) {
        				set_Z.add(node);
        			}
        		}
        		if(!set_Z.isEmpty()){
        			cand_Y.clear();
        		}
        		
        		set_Z.clear();
        		
        		for (Node node : cand_Y) {
        			boolean test1,test2,test3;
        			int indeg = 0;
        			int exdeg =0;
        			if(Parameter.upper_bound_pruning){        			
	        			indeg = indegs_Y.get(node);
	        			exdeg = exdegs_Y.get(node);
        			} else {
        				int[] indegs = node.indeg(set_Y, sub_Y, subspace_pruning);
        				int[] exdegs = node.exdeg(cand_Y, sub_Y, subspace_pruning,indegs);
        				Arrays.sort(indegs);
        				Arrays.sort(exdegs);
        				indeg = Math.max(indegs[indegs.length-Parameter.s_min],0);
        				exdeg = exdegs[exdegs.length-Parameter.s_min];
        				indegs_Y.put(node,indeg);
        				exdegs_Y.put(node,exdeg);
        			}
        			int size = set_Y.size();
        			
        			test1 = (Parameter.degree_pruning?indeg + exdeg < Math.ceil(Parameter.gamma_min * (size + exdeg)):false);
        			test2 = (Parameter.upper_bound_pruning?indeg + upper_bound_Y -1 < Math.ceil(Parameter.gamma_min * (size + upper_bound_Y -1)):false);
        			test3 = (Parameter.lower_bound_pruning?indeg + exdeg < Math.ceil(Parameter.gamma_min * (size + lower_bound_Y -1)):false);
        				
        			if(test1 || test2 || test3) {
        				set_Z.add(node);
        			}
        		}
        		
        		cand_Y.removeAll(set_Z);         
				
			} while(!cand_Y.isEmpty() && (!Parameter.upper_bound_pruning || lower_bound_Y<=upper_bound_Y) && !set_Z.isEmpty());
			
			if(!Parameter.upper_bound_pruning){
				
				int[] indegs = v.indeg(set_Y, sub_Y, subspace_pruning);
				int[] exdegs = v.exdeg(cand_Y, sub_Y, subspace_pruning,indegs);
				Arrays.sort(indegs);
				Arrays.sort(exdegs);
				indegs_Y.put(v,Math.max(indegs[indegs.length-Parameter.s_min],0));
				exdegs_Y.put(v, exdegs[exdegs.length-Parameter.s_min]);
			}
			
			double density = Graph.qclq_density(set_Y);
			int dimensionality = sub_Y.size();
			if(set_Y.size()>=Parameter.n_min && dimensionality >= Parameter.s_min && density >=Parameter.gamma_min){
				
				double quality = Cluster.quality(density, set_Y.size(), dimensionality);
				Cluster cluster = new Cluster(set_Y, sub_Y, quality);
				result.add(cluster);
				
				if(quality_estimation_pruning && !cand_Y.isEmpty()){
					
					int min_deg = set_Y.size();
					
					for(Node node : set_Y) {
						
						int deg = indegs_Y.get(node) + exdegs_Y.get(node);
						if (deg < min_deg) {
							min_deg = deg;
						}
					}
					int n_max = (int)Math.floor(min_deg/Parameter.gamma_min) + 1;
					if(n_max > (set_Y.size()+cand_Y.size())){
						n_max = set_Y.size()+cand_Y.size();
					}
					double gamma_max =((double)min_deg)/(set_Y.size()); 
					int s_max = sub_Y.size();
					double max_quality = Cluster.quality(gamma_max,n_max,s_max);
					
					if(n_max < set_Y.size() || gamma_max < Parameter.gamma_min){
						
						cand_Y.clear();
					} else if(max_quality < quality && (n_max * Parameter.r_obj) <= (set_Y.size())) {
						
						result.add(new ClusterCandidates(new HashSet<Node>(set_Y),sub_Y.copy(),new HashSet<Node>(cand_Y),max_quality,cluster));
						cand_Y.clear();
					}
				}
			}
			
			if((!Parameter.upper_bound_pruning || lower_bound_Y <= upper_bound_Y) && !cand_Y.isEmpty() && (set_Y.size() + cand_Y.size()) >= Parameter.n_min && sub_Y.size() >= Parameter.s_min) {
				detectCluster(set_Y, cand_Y, sub_Y, result, indegs_Y,exdegs_Y,subspace_pruning, lookahead, quality_estimation_pruning);
			}
		}
	}
	
	
	//Hilfsfunktionen
	
	protected static int upper(Collection<Node> set, Collection<Node> cands, Subspace sub, boolean subspace_pruning, HashMap<Node,Integer> indegs,HashMap<Node,Integer> exdegs) {
		
		Iterator<Node> it = set.iterator();
		Node node = it.next();
		
		int indeg_sum =0; //Summe der indeg-Werte in Y
		int[] indegsdims = node.indeg(set, sub, subspace_pruning);
		int[] exdegsdims = node.exdeg(cands, sub, subspace_pruning,indegsdims);
		Arrays.sort(indegsdims);
		Arrays.sort(exdegsdims);
	        			
		int exdeg = exdegsdims[exdegsdims.length-Parameter.s_min];
		int indeg = Math.max(indegsdims[indegsdims.length-Parameter.s_min],0);
		indegs.put(node, indeg);
		exdegs.put(node, exdeg);
		int deg_min = indeg + exdeg;
		indeg_sum +=indeg;
		while (it.hasNext()) {
			node = it.next();
			indegsdims = node.indeg(set, sub, subspace_pruning);
			exdegsdims = node.exdeg(cands, sub, subspace_pruning,indegsdims);
			Arrays.sort(indegsdims);
			Arrays.sort(exdegsdims);
		        			
			indeg = Math.max(indegsdims[indegsdims.length-Parameter.s_min],0);
			exdeg = exdegsdims[exdegsdims.length-Parameter.s_min];
			indegs.put(node, indeg);
			exdegs.put(node, exdeg);
			
			
			int deg = indeg + exdeg;
			if (deg<deg_min) deg_min = deg;
			indeg_sum += indeg;
		}
		int u_min = (int) Math.floor(deg_min/Parameter.gamma_min) + 1 - set.size();
		if (u_min > cands.size()) {
			u_min = cands.size();
		}
			
		int u=0;
		
		int[] indegscand = new int[cands.size()]; 
		int i =0; 
		for(Node node2 : cands) {
			indegsdims = node2.indeg(set, sub, subspace_pruning);
			exdegsdims = node2.exdeg(cands, sub, subspace_pruning,indegsdims);
			Arrays.sort(indegsdims);
			Arrays.sort(exdegsdims);
		        			
			indeg = Math.max(indegsdims[indegsdims.length-Parameter.s_min],0);
			exdeg = exdegsdims[exdegsdims.length-Parameter.s_min];
			indegs.put(node2, indeg);
			exdegs.put(node2, exdeg);
			
			
			indegscand[i]=indeg;
			i++;
		}
		
		Arrays.sort(indegscand);
		int sum_cand =0;
		
		for (int t=1;t<=u_min;t++) {
			sum_cand+=indegscand[indegscand.length-t];
						
			if(indeg_sum+sum_cand >= set.size() * (Math.ceil(Parameter.gamma_min*(set.size()+t-1)))) {
				u=t;
			}
		}
		
		return (u);
	}
	
	
	protected static int lower(Collection<Node> set, Collection<Node> cands, Subspace sub, boolean subspace_pruning, HashMap<Node,Integer> indegs){
		
		int indeg_sum =0; 
		Iterator<Node>it = set.iterator();
		Node node = it.next();
		int indeg =0;
		
		if (Parameter.upper_bound_pruning){
			indeg = indegs.get(node);
		} else {
			int [] indegsdims = node.indeg(set, sub, subspace_pruning);
			Arrays.sort(indegsdims);
		        			
			indeg = Math.max(indegsdims[indegsdims.length-Parameter.s_min],0);
			indegs.put(node, indeg);
		}
		int indeg_min = indeg;
		indeg_sum += indeg;
		while (it.hasNext()) {
			node = it.next();
			if (Parameter.upper_bound_pruning){
				indeg = indegs.get(node);
			} else {
				int [] indegsdims = node.indeg(set, sub, subspace_pruning);
				Arrays.sort(indegsdims);
			        			
				indeg = Math.max(indegsdims[indegsdims.length-Parameter.s_min],0);
				indegs.put(node, indeg);
			}
			if (indeg<indeg_min) {
				indeg_min = indeg;
			}
			indeg_sum +=indeg;
		}
		
		int[] indegscand = new int[cands.size()]; 
		int i =0;
		for(it=cands.iterator();it.hasNext();) {
			node = it.next();
			if(Parameter.upper_bound_pruning){
				indegscand[i]=indegs.get(node);
			} else {
				int [] indegsdims = node.indeg(set, sub, subspace_pruning);
				Arrays.sort(indegsdims);
    			
				indeg = Math.max(indegsdims[indegsdims.length-Parameter.s_min],0);
				indegs.put(node, indeg);
				indegscand[i]=indeg;
			}
				
			i++;
		}		
		Arrays.sort(indegscand); 
		
		int l_min = 0;
		for(int t=0;;t++){
			if(indeg_min+t >= (int) Math.ceil(Parameter.gamma_min *(set.size()+t-1))) {
				l_min =t;
				break;
			}
		}
		
		int l = cands.size()+1;
		int sum_cand =0;
		for (int t=0;t<=cands.size();t++){
			if(t>=1) {
				sum_cand+=indegscand[indegscand.length-t];
			}
			if(t>=l_min && indeg_sum + sum_cand >= set.size() * Math.ceil(Parameter.gamma_min * (set.size()+t-1))){
				l=t;
				break;
			}
		}
		
		return l;
	}
}
