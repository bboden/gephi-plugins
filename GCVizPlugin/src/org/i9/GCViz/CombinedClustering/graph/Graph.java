package org.i9.GCViz.CombinedClustering.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.i9.GCViz.CombinedClustering.base.Parameter;
import java.util.Collections;
import java.util.HashMap;

public class Graph {

    private ArrayList<Node> nodes = new ArrayList<Node>();
    private int numberOfAtts;
    private ArrayList<HashSet<Node>> connectedComponents = new ArrayList<HashSet<Node>>();
    private boolean connectedComponentCalculated = false;

    public Graph(int number) {
        numberOfAtts = number;
    }
    
    public Graph(Collection<Node> nodes, int number){
    	this.nodes = new ArrayList<Node>(nodes);
    	numberOfAtts = number;
    }

    public int getNumberOfAtts() {
        return this.numberOfAtts;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public Node getNodeHavingID(int ID) {
        for (Node node : nodes) {
            if (node.getID() == ID) {

                return node;
            }
        }
        return null;
    }

    public void addNode(int index, double[] values) {
        nodes.add(index, new Node(index, values));
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void deleteNode(Node node) {
        // for all the neighbours of this node
        for (Node neighbour : node.getNeighbors()) {
            // remove this nodes reference from all of the neighbours
            neighbour.getNeighbors().remove(node);
        }
        // finally remove this node
        nodes.remove(node);
    }

    public void deleteNode(int ID) {
        // first, get the node
        Node node = getNodeHavingID(ID);
        deleteNode(node);
    }

    public void addEdge(int index1, int index2) {
        nodes.get(index1).addNeighbor(nodes.get(index2));
        nodes.get(index2).addNeighbor(nodes.get(index1));
    }

    public void addEdge(Node node1, Node node2) {

        node1.addNeighbor(node2);
        node2.addNeighbor(node1);

    }

    public void deleteEdgeWithNodeID(int ID1, int ID2) {
        getNodeHavingID(ID1).getNeighbors().remove(getNodeHavingID(ID2));
        getNodeHavingID(ID2).getNeighbors().remove(getNodeHavingID(ID1));
    }

    public void deleteEdgeWithNodes(Node node1, Node node2) {
        node1.getNeighbors().remove(node2);
        node2.getNeighbors().remove(node1);
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    
    public void pruneGraph() {
        if (Parameter.gamma_min == 0) {
            return;
        }
        boolean changed = false;
        do {
            changed = false;
            ArrayList<Node> cands = new ArrayList<Node>(this.nodes);
            for (Node node : this.nodes) {
             
                if (node.getNeighbors().size() < (Parameter.gamma_min * (Parameter.n_min - 1))) {
                    cands.remove(node);
                    for (Node neighbor : node.getNeighbors()) {
                        neighbor.getNeighbors().remove(node);
                    }
                    changed = true;
                } else {
                    changed = node.pruneEdges();
                }
            }
            this.nodes = cands;
        } while (changed);
    }

     
   
    public void densityPruning() {
        boolean changed = false;
        do {
            changed = false;
            ArrayList<Node> cands = new ArrayList<Node>(this.nodes);
            for (Node node : this.nodes) {
               
                if (node.k_neighborhood(cands,Parameter.k).size() < Parameter.min_pts -1) {
                    cands.remove(node);
                    for (Node neighbor : node.getNeighbors()) {
                        neighbor.getNeighbors().remove(node);
                    }
                    changed = true;
                } 
            }
            this.nodes = cands;
        } while (changed);
    }
    
    
    public ArrayList<HashSet<Node>> detect_cores() {
    	
    	
        boolean changed = false;
        do {
            changed = false;
            ArrayList<Node> cands = new ArrayList<Node>(this.nodes);
            for (Node node : this.nodes) {
               
                if (node.getNeighbors().size() < Parameter.min_pts -1) {
                    cands.remove(node);
                    for (Node neighbor : node.getNeighbors()) {
                        neighbor.getNeighbors().remove(node);
                    }
                    changed = true;
                } 
            }
            this.nodes = cands;
        } while (changed);
        
       
        ArrayList<HashSet<Node>> connected_comps = new ArrayList<HashSet<Node>>();
        ArrayList<Node> remainingNodes = new ArrayList<Node>(this.getNodes());
		int numComps=0;
		while(!remainingNodes.isEmpty()){
			Node firstnode = remainingNodes.get(0);
			HashSet<Node> connected = firstnode.connected_component(remainingNodes);
			connected.add(firstnode);
			remainingNodes.removeAll(connected);
			numComps++;
			
			
			if(connected.size()>=Parameter.min_pts){
				connected_comps.add(connected);
			}
		}
        
        return connected_comps;
    }
    
    
  

    @Override
    public String toString() {
     

        StringBuilder result = new StringBuilder();
        for (Node node : nodes) {
            result.append(node.getID()).append(" (");
            int i;
            for (i = 0; i < numberOfAtts - 1; i++) {
                result.append(node.getAttribute(i)).append(", ");
            }
            result.append(node.getAttribute(i)).append(") -> [");
            for (Node neighbour : node.getNeighbors()) {
                result.append(neighbour.getID()).append(" ");
            }
            result.append("]\n");
        }
        return result.toString();
    }

    
    public static void get_qclqs(HashSet<Node> cands, HashSet<HashSet<Node>> result, HashSet<HashSet<Node>> noQclqs) {

        int min_size = Parameter.n_min;
        double gamma_min = Parameter.gamma_min;
        if (cands.size() >= min_size) {
            if (is_qclq(cands, gamma_min)) {
               
                if (result.add(cands)) {
                    //System.out.print(cands+"\n");
                } else {
                    //System.out.print(cands+"Already present!\n");
                    return;
                }
            } else {
                if (!noQclqs.add(cands)) {
                    return;
                }
            }

        
            for (Iterator<Node> it = cands.iterator(); it.hasNext();) {
                Node cand = it.next();
                HashSet<Node> newcands = new HashSet<Node>(cands);
                newcands.remove(cand);
                get_qclqs(newcands, result, noQclqs);
            }
        }
    }

    public static boolean is_coherent(HashSet<Node> cands) {
        if (cands.isEmpty()) {
            return true;
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
        return visited.containsAll(cands);
    }
   

    public static boolean is_qclq(Collection<Node> nodes, double gamma_min) {
        double min_deg = Math.ceil(gamma_min * (nodes.size() - 1));
        boolean result = true;
        for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
            Node node = it.next();
            int counter = 0;
            for (Iterator<Node> it2 = node.getNeighbors().iterator(); it2.hasNext();) {
                Node neighbor = it2.next();
                if (nodes.contains(neighbor)) {
                    counter++;
                }
                if (counter >= min_deg) {
                    break;
                }
            }
            if (counter < min_deg) {
                return false;
            }
        }

        return result;
    }

    
    public static double qclq_density(HashSet<Node> nodes) {
        double min_deg = Math.ceil(Parameter.gamma_min * (nodes.size() - 1));
        double min_existing_deg = nodes.size() - 1;
        for (Node node : nodes) {
            int count = 0;
            for (Node neighbor : node.getNeighbors()) {
                if (nodes.contains(neighbor)) {
                    count++;
                }
            }
            if (count < min_deg) {
                return (double) count / (nodes.size() - 1);
            }
            if (count < min_existing_deg) {
                min_existing_deg = count;
            }
        }

        return  min_existing_deg / (nodes.size() - 1);
    }

    
    public static double exakt_qclq_density(HashSet<Node> nodes) {
        double min_existing_deg = nodes.size() - 1;
        for (Node node : nodes) {
            int count = 0;
            for (Node neighbor : node.getNeighbors()) {
                if (nodes.contains(neighbor)) {
                    count++;
                }
            }
            if (count < min_existing_deg) {
                min_existing_deg = count;
            }
        }

        return  min_existing_deg / (nodes.size() - 1);
    }

   
    public Graph copyWithoutAdjacency() {
        Graph copyGraph = new Graph(this.numberOfAtts);
        for (Node node : this.getNodes()) {
            copyGraph.addNode(node.copyWithoutNeighbours());
        }
        return copyGraph;
    }

    public Graph copy() {
     
        Graph copyGraph = copyWithoutAdjacency();
        for (Node node : getNodes()) {
            for (Node neighbour : node.getNeighbors()) {
                int nodeID = node.getID();
                int neighbourID = neighbour.getID();
                copyGraph.getNodeHavingID(nodeID).addNeighbor(copyGraph.getNodeHavingID(neighbourID));
                copyGraph.getNodeHavingID(neighbourID).addNeighbor(copyGraph.getNodeHavingID(nodeID));
            }
        }
        return copyGraph;
    }

    public int getMinDegreeOfNodes() {
        int minDegreeOfNodes = Integer.MAX_VALUE;
        for (Node node : getNodes()) {
            if (node.getNeighbors().size() < minDegreeOfNodes) {
                minDegreeOfNodes = node.getNeighbors().size();
            }
        }
        return minDegreeOfNodes;
    }

    private HashSet<Node> dfsVisit(HashMap<Node, Boolean> visited, Node node) {
        HashSet<Node> component = new HashSet<Node>();
        if (visited.get(node) == Boolean.TRUE) {
            return component;
        } else {
            component.add(node);
            visited.put(node, Boolean.TRUE);
        }
        for (Node neighbour : node.getNeighbors()) {
            component.addAll(dfsVisit(visited, neighbour));
        }
        return component;
    }

    public void calculateConnectedComponent() {
        HashMap<Node, Boolean> visited = new HashMap<Node, Boolean>(nodes.size());
        for (Node node : nodes) {
            visited.put(node, Boolean.FALSE);
        }
        for (Node node : nodes) {
            HashSet<Node> connectedComponent = dfsVisit(visited, node);
            if (!connectedComponent.isEmpty()) {
                connectedComponents.add(connectedComponent);
            }
        }

    }

    public boolean isConnected(Node node1, Node node2) {
        if (connectedComponentCalculated == false) {
            calculateConnectedComponent();
            connectedComponentCalculated = true;
        }
        for (HashSet<Node> component : connectedComponents) {
            if (component.contains(node1) && component.contains(node2)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<HashSet<Node>> getConnectedComponents() {
        if (connectedComponentCalculated == false){
            calculateConnectedComponent();
            connectedComponentCalculated = true;
        }
        return connectedComponents;
    }

    public void sortNodes(){
        Collections.sort(nodes);
    }
    
    
  }
