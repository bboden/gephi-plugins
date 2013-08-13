package org.i9.GCViz.CombinedClustering.base;

import org.i9.GCViz.CombinedClustering.graph.Node;

import java.util.Arrays;
import java.util.HashSet;

public class DenseCluster {
	protected HashSet<Node> nodes = new HashSet<Node>();
	protected HashSet<Node> borders = new HashSet<Node>();
	protected Subspace subspace;
	protected double quality;
	
	
	public HashSet<Node> getNodes() {
		return nodes;
	}
	public HashSet<Node> getBorders() {
		return borders;
	}
	public void setNodes(HashSet<Node> nodes) {
		this.nodes = nodes;
	}
	public void setBorders(HashSet<Node> nodes) {
		this.borders = nodes;
	}
	public Subspace getSubspace() {
		return subspace;
	}
	public double getQuality() {
		return quality;
	}
	public DenseCluster(HashSet<Node> nodes, Subspace subspace, double quality) {
		this.nodes = nodes;
		this.quality = quality;
		this.subspace = subspace;
	}
	public DenseCluster(HashSet<Node> nodes, HashSet<Node> borders, Subspace subspace, double quality) {
		this.nodes = nodes;
		this.borders = borders;
		this.quality = quality;
		this.subspace = subspace;
	}
	public String toString(){
		String result="";
		boolean[] dims = subspace.getDimensions();
		for(int i=0;i<dims.length;i++){
			result+=(dims[i]?"1":"0")+" ";
		}
		result+=(nodes.size()+borders.size())+" ";
		
		//Sort IDs ascendingly
		int[] sortedIDs = new int[nodes.size()+borders.size()];
		int i = 0;
		for(Node node : nodes) {
			sortedIDs[i]= node.getID();
			i++;
		}
		for(Node node : borders) {
			sortedIDs[i]= node.getID();
			i++;
		}
		Arrays.sort(sortedIDs);
		for(int j = 0;j<sortedIDs.length;j++){
			result+=sortedIDs[j]+" ";
		}
		return result;
	}

	public static double quality(int size, int dimensionality) {
		double quality = size * dimensionality;
		return quality;
	}
}
