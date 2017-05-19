/**
 *
 */
package com.transyslab.commons.tools;

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
	protected float[] pos_;
	protected float fitness_;
	protected boolean flag_;
	protected static Random rnd_ = new Random();
	protected double[] constrainsViolate;
	protected double[] results;
	public Individual(int dims) {
		dims_ = dims;
		pos_ = new float[dims];
		fitness_ = Constants.FLT_INF;
		flag_ = true;
		results = new double[2];

	}
	public float[] getPos() {
		return pos_;
	}
	public void init(float[] pl, float[] pu) {

		for (int i = 0; i < dims_; i++) {
			pos_[i] = pl[i] + (pu[i] - pl[i]) * rnd_.nextFloat();
		}
	}
	public void setFitness(float fitness){
		fitness_ = fitness;
	}
	public void evaMRE(int[][] simflow, float[][] simavgtime, int[][] realflow, float[][] realavgtime, float w) {
		// 列为不同时间，行为不同link
		int col = simflow[0].length;
		int row = simflow.length;
		double sumOfLinkFlowError;
		double sumOfLinkTimeError;
		double sumError = 0;
		for (int j = 0; j < col; j++) {
			sumOfLinkFlowError = 0;
			sumOfLinkTimeError = 0;
			for (int i = 0; i < row; i++) {
				if (realflow[i][j] == 0)
					realflow[i][j] = 1;
				sumOfLinkFlowError = sumOfLinkFlowError + (Math.abs(realflow[i][j] - simflow[i][j])) / realflow[i][j];
				sumOfLinkTimeError = sumOfLinkTimeError
						+ (Math.abs(realavgtime[i][j] - simavgtime[i][j])) / realavgtime[i][j];
			}
			sumError = sumError + w * (sumOfLinkFlowError) + (1 - w) * (sumOfLinkTimeError);
		}
		fitness_ = (float) (sumError / col);
	}
}
