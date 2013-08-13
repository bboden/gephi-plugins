package org.i9.GCViz.CombinedClustering.graph;

import java.util.Comparator;
import java.util.HashMap;

public class NodeComparatorInDegreeDesc implements Comparator<Node>{
	HashMap<Node, Integer> indegs;
		public NodeComparatorInDegreeDesc(HashMap<Node, Integer> indegs){
			this.indegs = indegs;
		}
		
		public int compare(Node node1, Node node2) {
			if(node1 ==null && node2 ==null) {
				return 0;
			}
			if(node1 ==null && node2 !=null) {
				return -1;
			}
			if(node1 !=null && node2 ==null) {
				return 1;
			}
			double deg1 = indegs.get(node1); 
			double deg2 = indegs.get(node2);
			if(deg1 == deg2) {
				return 0;
			}
			if(deg1 > deg2){
				return -1;
			}
			
			return 1;
			
		}
}
