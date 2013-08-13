package org.i9.GCViz.CombinedClustering.base;

import org.i9.GCViz.CombinedClustering.graph.Node;
import java.util.HashSet;
import java.util.Arrays;

public class Cluster {
	protected HashSet<Node> nodes = new HashSet<Node>();
	protected Subspace subspace;
	protected double quality;
	
	
	public HashSet<Node> getNodes() {
		return nodes;
	}
	public void setNodes(HashSet<Node> nodes) {
		this.nodes = nodes;
	}
	public Subspace getSubspace() {
		return subspace;
	}
	public double getQuality() {
		return quality;
	}
	public Cluster(HashSet<Node> nodes, Subspace subspace, double quality) {
		this.nodes = nodes;
		this.quality = quality;
		this.subspace = subspace;
	}
	public String toString(){
		String result="";
		boolean[] dims = subspace.getDimensions();
		for(int i=0;i<dims.length;i++){
			result+=(dims[i]?"1":"0")+" ";
		}
		result+=nodes.size()+" ";
		
                //sort IDs ascendingly
		int[] sortedIDs = new int[nodes.size()];
		int i = 0;
		for(Node node : nodes) {
			sortedIDs[i]= node.getID();
			i++;
		}
		Arrays.sort(sortedIDs);
		for(int j = 0;j<sortedIDs.length;j++){
			result+=sortedIDs[j]+" ";
		}
		return result;
	}
		
	public static double quality(double density, int size, int dimensionality) {
		double quality = Math.pow(density, Parameter.param_a) * Math.pow(size, Parameter.param_b) * Math.pow(dimensionality,Parameter.param_c);
		return quality;
	}
}