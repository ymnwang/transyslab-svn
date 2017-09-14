/**
 *
 */
package com.transyslab.commons.tools.optimizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.transyslab.roadnetwork.Constants;

/**
 * @author yali
 *
 */
public class Individual {
	protected int dims_;
	protected double[] pos_;
	protected double fitness_;
	protected boolean flag_;
	protected static Random rnd_ = new Random();
	protected double[] constrainsViolate;
	protected double[] results;
	public Individual(int dims) {
		dims_ = dims;
		pos_ = new double[dims];
		fitness_ = Constants.FLT_INF;
		flag_ = true;
		results = new double[2];

	}
	public double[] getPos() {
		return pos_;
	}
	public void init(double[] pl, double[] pu) {
		for (int i = 0; i < dims_; i++) {
			pos_[i] = pl[i] + (pu[i] - pl[i]) * rnd_.nextFloat();
		}
	}
	public void setFitness(double fitness){
		fitness_ = fitness;
	}
}
