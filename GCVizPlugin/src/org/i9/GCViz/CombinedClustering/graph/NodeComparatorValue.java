package org.i9.GCViz.CombinedClustering.graph;

import java.util.Comparator;

public class NodeComparatorValue implements Comparator<Node>{
	
		private int dim;
		
		public NodeComparatorValue(int dim) {
			this.dim = dim;
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
			double val1 = node1.getAttribute(dim);
			double val2 = node2.getAttribute(dim);
			if(val1 == val2) {
				return 0;
			}
			if(val1 > val2){
				return 1;
			}
			return -1;
			
		}
}
