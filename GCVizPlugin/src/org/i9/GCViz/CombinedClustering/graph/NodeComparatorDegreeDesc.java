package org.i9.GCViz.CombinedClustering.graph;

import java.util.Comparator;

public class NodeComparatorDegreeDesc implements Comparator<Node>{
	
		
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
			double deg1 = node1.getNeighbors().size();
			double deg2 = node2.getNeighbors().size();
			if(deg1 == deg2) {
				return 0;
			}
			if(deg1 > deg2){
               
				return -1;
			}
			
			return 1;
			
		}
}
