package org.i9.GCViz.CombinedClustering.base;

import java.util.Comparator;

public class DenseClusterComparator implements Comparator<DenseCluster> {
	
	
	public int compare(DenseCluster cluster1, DenseCluster cluster2) {
		if(cluster1 ==null && cluster2 ==null) {
			return 0;
		}
		if(cluster1 ==null && cluster2 !=null) {
			return -1;
		}
		if(cluster1 !=null && cluster2 ==null) {
			return 1;
		}
		double quality1 = cluster1.getQuality();
		double quality2 = cluster2.getQuality();
		if(quality1 == quality2) {
			return 0;
		}
		if(quality1 > quality2){
			return -1;
		}
		
		return 1;
		
	}

}

