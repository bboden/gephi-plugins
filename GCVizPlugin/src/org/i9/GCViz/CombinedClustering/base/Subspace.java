package org.i9.GCViz.CombinedClustering.base;

import java.util.Arrays;
import java.text.DecimalFormat;
import org.i9.GCViz.CombinedClustering.graph.Node;


public class Subspace {
	
	private boolean[] dimensions;
	private double[] upper;
	private double[] lower;
	
	public boolean[] getDimensions() {
		return dimensions;
	}
	public void setDimensions(boolean[] dimensions) {
		this.dimensions = dimensions;
	}
	public double[] getUpper() {
		return upper;
	}
	public void setUpper(double[] upper) {
		this.upper = upper;
	}
	public double[] getLower() {
		return lower;
	}
	public void setLower(double[] lower) {
		this.lower = lower;
	}
	public void setDimension(int dim, double lower, double upper) {
		this.dimensions[dim]=true;
		this.lower[dim]=lower;
		this.upper[dim]=upper;
	}
	public void removeDimension(int dim){
		this.dimensions[dim] = false;
	}
	public boolean hasDimension(int dim) {
		return dimensions[dim];
	}
	
	public int size() {
		int count = 0;
		for (int i=0;i < dimensions.length; i++){
			if(dimensions[i]) {
				count++;
			}
		}
		return count;
	}
	
	
	public Subspace() {
		dimensions = new boolean[Parameter.numberOfAtts];
		upper = new double[Parameter.numberOfAtts];
		lower = new double[Parameter.numberOfAtts];
		Arrays.fill(dimensions, true);
		Arrays.fill(upper,Double.NaN);
		Arrays.fill(lower,Double.NaN);
	}
	
	public void intersect(Node v) {
		
		for(int i=0;i < Parameter.numberOfAtts; i++){
			if(dimensions[i]){
				
				double v_i = v.getAttribute(i);
				if(Double.isNaN(upper[i])){
					upper[i] = v_i;
					lower[i] = v_i;
				}
				if(v_i <= upper[i] && v_i>=lower[i]){
					//alles ok
				} else if(v_i<=upper[i] && (upper[i]-v_i)<=Parameter.w_max) {
					lower[i] = v_i;
				} else if(v_i>=lower[i] && (v_i-lower[i])<=Parameter.w_max) {
					upper[i] = v_i;
				} else {
					dimensions[i]=false;
				}
			}
		}
	}
	
	public Subspace copy() {
		Subspace new_sub = new Subspace();
		for(int dim=0;dim<dimensions.length;dim++){
			if(dimensions[dim]){
				new_sub.setDimension(dim, lower[dim], upper[dim]);
			} else {
				new_sub.dimensions[dim]=false;
			}
		}
		return new_sub;
	}
	
	public String toString() {
		DecimalFormat form = new DecimalFormat("#0.00");
		String dimensionText="";
		for(int i=0;i<dimensions.length;i++) {
			if(dimensions[i]) {
				dimensionText+="["+form.format(lower[i])+"-"+form.format(upper[i])+"]";
			} else {
				dimensionText+="[]";
			}
		}
		return dimensionText;
	}
	

	public int get_max_d() {
		int max =0;
		for(int i=0;i<dimensions.length;i++){
			if (dimensions[i]){
				max = i;
			}
		}
		return max;
	}
}
