package org.i9.GCViz.CombinedClustering.graph;

import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.i9.GCViz.CombinedClustering.base.Parameter;
import org.i9.GCViz.CombinedClustering.base.Subspace;

public class Node implements Comparable<Node>{

    private int ID;
    private double[] attributes;
    private HashSet<Node> neighbors = new HashSet<Node>();

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

     public Node copyWithoutNeighbours() {
        Node neu = new Node(ID, attributes);
        neu.neighbors = new HashSet<Node>();
        return neu;
    }

    public void setAttribute(int key, double value) {
        attributes[key] = value;
    }

    public void setAttributes(double[] values) {
        attributes = values;
    }

    public double getAttribute(int key) {
        return attributes[key];
    }

    public double[] getAttributes() {
        return attributes;
    }

    public HashSet<Node> getNeighbors() {
        return neighbors;
    }

    public void addNeighbor(Node x) {
        neighbors.add(x);
    }

    public String toString() {
        return Integer.toString(ID);
    }

    public Node(int myID, double[] atts) {
        ID = myID;
        attributes = atts;
    }

    public Node() {
        ID = -1;
    }

    public Node(int ID) {
        this.ID = ID;
    }
    
    public int overlap(Node other) {
        int count = 0;
        for (int i = 0; i < this.attributes.length; i++) {
            if (Math.abs(this.attributes[i] - other.attributes[i]) <= Parameter.w_max) {
                count++;
            }
        }
        return count;
    }


    public boolean hasEdgeIn(Collection<Node> nodes) {
        for (Node node : this.neighbors) {
            if (nodes.contains(node)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasNeighbourWithID(int ID) {
        for (Node neighbour : this.getNeighbors()) {
            if (neighbour.getID() == ID) {
                return true;
            }
        }
        return false;
    }

    
    public int deg(Collection<Node> nodes, Subspace sub, boolean subspace_pruning) {
        HashSet<Node> neighborNodes = new HashSet<Node>(nodes);
        neighborNodes.retainAll(this.neighbors);
        if (!subspace_pruning || Parameter.s_min == 0 || nodes.isEmpty()) {
            return neighborNodes.size();
        }

        int[] degs = new int[sub.size()];
        int i = 0;
        for (int dim = 0; dim < attributes.length; dim++) {
            if (sub.hasDimension(dim)) {
                degs[i] = this.deg(neighborNodes, dim);
                i++;
            }
        }
        if (i == 0) {
            return 0;
        }
        Arrays.sort(degs);
        return degs[degs.length - Parameter.s_min];
    }

   
    private int deg(HashSet<Node> nodes, int dim) {
        int count = 0;
        for (Node node : nodes) {
            
            if (Math.abs(this.attributes[dim] - node.attributes[dim]) <= Parameter.w_max) {
                count++;
            }
        }
      
        return count;
    }

   
    public int deg(Collection<Node> X) {
        ArrayList<Node> in = new ArrayList<Node>(X);
        in.retainAll(this.neighbors);
        return in.size();
    }

    
    public int[] indeg(Collection<Node> nodes, Subspace sub, boolean subspace_pruning) {
        HashSet<Node> neighborNodes = new HashSet<Node>(nodes);
        neighborNodes.retainAll(this.neighbors);
        int[] degs = new int[sub.size()];
        if (!subspace_pruning || Parameter.s_min == 0 || nodes.isEmpty()) {
            Arrays.fill(degs, neighborNodes.size());
            return degs;
        }

        int deg = this.deg(nodes);
        int i = 0;
        for (int dim = 0; dim < attributes.length; dim++) {
            if (sub.hasDimension(dim)) {
                degs[i] = ((sub.getLower()[dim] + Parameter.w_max >= this.attributes[dim] && sub.getUpper()[dim] - Parameter.w_max <= this.attributes[dim]) ? deg : -1);
                i++;
            }
        }
        if (i == 0) {
            Arrays.fill(degs, neighborNodes.size());
            return degs;
        }
        return degs;
    }

    public int[] exdeg(Collection<Node> nodes, Subspace sub, boolean subspace_pruning, int[] indegs) {
        HashSet<Node> neighborNodes = new HashSet<Node>(nodes);
        neighborNodes.retainAll(this.neighbors);
        int[] degs = new int[sub.size()];
        if (!subspace_pruning || Parameter.s_min == 0 || nodes.isEmpty()) {
            Arrays.fill(degs, neighborNodes.size());
            return degs;
        }

        int i = 0;
        for (int dim = 0; dim < attributes.length; dim++) {
            if (sub.hasDimension(dim)) {
                if (indegs[i] == -1) {
                    degs[i] = 0;
                } else {
                    int count = 0;
                    for (Node node : neighborNodes) {
                        if (sub.getLower()[dim] + Parameter.w_max >= node.attributes[dim] && sub.getUpper()[dim] - Parameter.w_max <= node.attributes[dim]
                                && Math.abs(node.attributes[dim] - this.attributes[dim]) <= Parameter.w_max) {
                            count++;
                        }
                    }
                    degs[i] = count;
                }
                i++;
            }
        }
        if (i == 0) {
            Arrays.fill(degs, neighborNodes.size());
            return degs;
        }
        return degs;
    }

    public HashSet<Node> k_neighborhood(Collection<Node> cands, Subspace sub, int k) {
        HashSet<Node> k_neighbors = new HashSet<Node>();
        k_neighborhood_rek(new HashSet<Node>(cands), sub, k, k_neighbors);
        k_neighbors.remove(this);
        return k_neighbors;
    }

   
    public void k_neighborhood_rek(HashSet<Node> cands, Subspace sub, int k, HashSet<Node> k_neighbors) {
       
        if (cands.contains(this)) {

           
            int count = 0;
            for (int i = 0; i < Parameter.numberOfAtts; i++) {
                if (sub.getDimensions()[i]) {
                    if ((this.attributes[i] <= sub.getUpper()[i] && this.attributes[i] >= sub.getLower()[i])
                            || (this.attributes[i] < sub.getLower()[i] && (sub.getUpper()[i] - this.attributes[i]) <= Parameter.w_max)
                            || (this.attributes[i] > sub.getUpper()[i] && (this.attributes[i] - sub.getLower()[i]) <= Parameter.w_max)) {
                        count++;
                    }
                }
            }

            cands.remove(this);

            if (count >= Parameter.s_min) {
                k_neighbors.add(this);

            } else {
               
                return;
            }

        }
        if (k <= 0 || cands.isEmpty()) {
            return;
        }
       
        for (Node node : this.neighbors) {
            node.k_neighborhood_rek(cands, sub, k - 1, k_neighbors);
        }

    }

    
    public HashSet<Node> k_neighborhood(Collection<Node> cands, int k) {
        HashSet<Node> k_neighbors = new HashSet<Node>();
        k_neighborhood_rek(new HashSet<Node>(cands), k, k_neighbors);
        k_neighbors.remove(this);
        return k_neighbors;
    }
    
   
    public void k_neighborhood_rek(HashSet<Node> cands, int k, HashSet<Node> k_neighbors) {
        //Tiefensuche
        if (cands.contains(this)) {
            k_neighbors.add(this);
            cands.remove(this);
        }
        if (k <= 0 || cands.isEmpty()) {
            return;
        }
      
        for (Node node : this.neighbors) {
            node.k_neighborhood_rek(cands, k - 1, k_neighbors);
        }
    }

  
    public HashSet<Node> connected_component(Collection<Node> cands) {
        if (cands.isEmpty()) {
            return new HashSet<Node>();
        }
       
        Node firstnode = cands.iterator().next();
        HashSet<Node> visited = new HashSet<Node>();
        LinkedList<Node> queue = new LinkedList<Node>();
        queue.add(firstnode);
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            visited.add(node);
            for (Node neighbor : node.getNeighbors()) {
                if (cands.contains(neighbor) && !visited.contains(neighbor) && !queue.contains(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
        return visited;
    }

  
    public void propagateAttributes(int dim, ArrayList<Node> unseenNodes, double p, double dist, Random rand) {
        ArrayList<Node> unseenNeighbors = new ArrayList<Node>(this.neighbors);
        unseenNeighbors.retainAll(unseenNodes);
        if (unseenNeighbors.isEmpty()) {
            return;
        }
       
        for (Iterator<Node> it = unseenNeighbors.iterator(); it.hasNext();) {
            Node node = it.next();

            if (Double.isNaN(node.getAttribute(dim))) {
                unseenNodes.remove(node);

                
                double random = rand.nextDouble();
                if (random <= p / 2) {
                    node.setAttribute(dim, (this.getAttribute(dim) + dist));
                    node.propagateAttributes(dim, unseenNodes, p, dist, rand);
                } else if (random <= p) {
                    node.setAttribute(dim, (this.getAttribute(dim) + dist));
                    node.propagateAttributes(dim, unseenNodes, p, dist, rand);
                } else {
                    
                    node.setAttribute(dim, rand.nextDouble());
                    it.remove();
                    
                    unseenNodes.add(node);
                }
            } else {
               
                it.remove();
            }
        }
        	
    }


    public boolean pruneEdges() {
        boolean changed = false;
        for (Iterator<Node> it = this.neighbors.iterator(); it.hasNext();) {
            Node node = it.next();
            if (this.overlap(node) < Parameter.s_min) {
                it.remove();
                node.neighbors.remove(this);
                changed = true;
            }
        }
        return changed;
    }

    public int getDegree(){
        return getNeighbors().size();
    }

    public int compareTo(Node o) {
        return this.getID() - o.getID();
    }
    
    public boolean check_MinPts(ArrayList<Node> cands, Subspace sub, int k, double epsilon, int MinPts){
    	int good_neighbors=0;
    	for(Node k_neighbor : this.k_neighborhood(cands, k)){
    		boolean similar = true;
    		for(int dim=0;dim<sub.getDimensions().length;dim++){
    			if(sub.hasDimension(dim)){
		    		
		    		if(Math.abs(this.getAttribute(dim)-k_neighbor.getAttribute(dim))>epsilon){
		    			similar =false;
		    		}
    			}
    		}
    		if(similar){
    			good_neighbors++;
    		}
    	}
    	return good_neighbors >= MinPts-1;
    }

}
