package org.i9.GCViz.CombinedClustering.base;

import org.i9.GCViz.CombinedClustering.graph.Node;

import java.util.ArrayList;
import java.util.HashSet;

public class DenseClusterCandidates extends DenseCluster{
	protected ArrayList<DenseCluster> parents; 
	protected ArrayList<DenseCluster> better_parents; 
	
	public DenseClusterCandidates(HashSet<Node> nodes, Subspace subspace, double q_max, ArrayList<DenseCluster> parents, ArrayList<DenseCluster> better_parents) {
		super(nodes,subspace,q_max);
		this.parents = parents;
		this.better_parents = better_parents;
	}
	
	public DenseClusterCandidates(HashSet<Node> nodes, HashSet<Node> borders, Subspace subspace, double q_max, ArrayList<DenseCluster> parents, ArrayList<DenseCluster> better_parents) {
		super(nodes,borders,subspace,q_max);
		this.parents = parents;
		this.better_parents = better_parents;
	}

	public ArrayList<DenseCluster> getParents() {
		return parents;
	}

	public ArrayList<DenseCluster> getBetterParents() {
		return better_parents;
	}
}
