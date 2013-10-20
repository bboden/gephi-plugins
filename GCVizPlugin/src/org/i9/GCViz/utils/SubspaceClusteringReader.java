package org.i9.GCViz.utils;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

/**
 *
 * @author Roman Haag
 */
public class SubspaceClusteringReader {

    private String inputFilePath;
    private int clusterCount;
    private Map<Long, ArrayList<Integer>> clustering; // maps every node to a list of its clusters
    private Map<Integer, ArrayList<Long>> clustering2; // maps every cluster to a list of its nodes
    private Map<Integer, ArrayList<Integer>> interClusters; // maps every cluster to a list of intersecting clusters
    private Map<Integer, Integer[]> subspaces; // maps every cluster to its subspace 
    private Map<Integer, Long> clusterSizes; // maps every cluster to its size

    public SubspaceClusteringReader(String inputFilePath) {
        this.inputFilePath = inputFilePath;
        this.clustering = new HashMap<Long, ArrayList<Integer>>();
        this.clustering2 = new HashMap<Integer, ArrayList<Long>>();
        this.interClusters = new HashMap<Integer, ArrayList<Integer>>();
        this.subspaces = new HashMap<Integer, Integer[]>();
        this.clusterSizes = new HashMap<Integer, Long>();
        this.clusterCount = 0;
    }

    public void run() {
        FileReader fr = null;
        try {
            fr = new FileReader(inputFilePath);
        } catch (FileNotFoundException ex) {
           // ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Clustering File does not exist");
            return;
        }

        BufferedReader br = new BufferedReader(fr);
        String line = null;
        while (true) {
            try {
                line = br.readLine();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Clustering File could not be read");
                return;
            }
            if (line == null) {
                break;
            }

            String[] pieces = line.split(" ");

            // check if current line is a comment/etc
            // valid lines start with a number
            try {
                Integer.parseInt(pieces[0]);
            } catch (NumberFormatException e) {
                continue;
            }

            // read line
            int pos = 0;
            // read subspace information
            List<Integer> subspace = new ArrayList<Integer>();
            while (true) {
                long a = Long.parseLong(pieces[pos]);
                pos++;
                if (a < 2) {
                    subspace.add((int) a);
                } else {
                    subspaces.put(clusterCount, subspace.toArray(new Integer[0]));
                    clusterSizes.put(clusterCount, a);
                    break;
                }
            }

            // read cluster nodes
            ArrayList<Long> nodes = new ArrayList<Long>();
            clustering2.put(clusterCount, nodes);
            while (pos < pieces.length) {
                long a = Long.parseLong(pieces[pos]);
                nodes.add(a);
                ArrayList<Integer> clustersOfA = clustering.get(a);
                if (clustersOfA == null) {
                    clustersOfA = new ArrayList<Integer>();
                    clustering.put(a, clustersOfA);
                }
                clustersOfA.add(clusterCount);
                pos++;
            }

            clusterCount++;
        }
        findInterClusters();
    }

    private void findInterClusters() {
        for (int i = 0; i < clusterCount; i++) {
            HashSet<Integer> intersectingClusters = new HashSet<Integer>();
            ArrayList<Long> nodes = getNodes(i);
            for (long n = 0; n < nodes.size(); n++) {
                ArrayList<Integer> clusters = getClusters(n);
                intersectingClusters.addAll(clusters);
            }
            intersectingClusters.remove(i);
            ArrayList<Integer> temp = new ArrayList<Integer>();
            temp.addAll(intersectingClusters);
            interClusters.put(i, temp);
        }
    }

    public ArrayList<Integer> getClusters(long node) {
        ArrayList<Integer> result = clustering.get(node);
        if (result != null) {
            return result;
        }
        return new ArrayList<Integer>();
    }

    public ArrayList<Long> getNodes(int cluster) {
        ArrayList<Long> result = clustering2.get(cluster);
        if (result != null) {
            return result;
        }
        return new ArrayList<Long>();
    }

    public Integer[] getSubspace(int cluster) {
        Integer[] result = subspaces.get(cluster);
        if (result != null) {
            return result;
        }
        return new Integer[0];
    }

    public Long getClusterSize(int cluster) {
        Long result = clusterSizes.get(cluster);
        if (result != null) {
            return result;
        }
        return 0l;
    }

    public ArrayList<Integer> getIntersectingClusters(int cluster) {
        ArrayList<Integer> result = interClusters.get(cluster);
        if (result != null) {
            return result;
        }
        return new ArrayList<Integer>();
    }
    
    public boolean intersecting(int cluster1, int cluster2) {
        ArrayList<Integer> iClusters1 = getIntersectingClusters(cluster1);
        return iClusters1.contains(cluster2);
    }
    public int countCommonCLusters(long node1, long node2) {
        ArrayList<Integer> clusters1 = getClusters(node1);
        ArrayList<Integer> clusters2 = getClusters(node2);
        int result = 0;
        for (int i : clusters1) {
            for (int j : clusters2) {
                if (i == j) {
                    result++;
                }
            }
        }
        return result;
    }
    
    public int getClusterCount() {
        return clusterCount;
    }
}
