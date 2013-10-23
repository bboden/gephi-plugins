package org.i9.GCViz.CombinedClustering.base;

import org.i9.GCViz.CombinedClustering.graph.Node;

import java.util.HashSet;


public class ClusterCandidates extends Cluster{
	
	protected HashSet<Node> cands;
	protected Cluster betterCluster;
	
	public ClusterCandidates(HashSet<Node> nodes, Subspace subspace, HashSet<Node> cands, double quality, Cluster better) {
		super(nodes,subspace,quality);
		this.cands = cands;
		this.betterCluster = better;
	}
	
	
	public HashSet<Node> getCands() {
		return cands;
	}

	public Cluster getBetterCluster() {
		return betterCluster;
	}
}
