package com.transyslab.commons.tools;

import org.apache.commons.math3.distribution.BinomialDistribution;

/**
 * Created by yali on 2017/11/5.
 */
public class VariablePerturbation {
	public static double[] pertubate(double[] lower, double[] upper, double step,double[] variable){
		if(lower.length!=upper.length || lower.length!=variable.length){
			System.out.println("Pertubate variables fail, check the length of input arrays");
			return null;
		}
		int dim = variable.length;
		BinomialDistribution bd = new BinomialDistribution(1,0.5);
		int[] bdsamples = bd.sample(dim);
		double[] result = new double[dim];
		for(int i=0;i<dim;i++){
			if(bdsamples[i] == 0)
				bdsamples[i] = -1;
			result[i] = variable[i] + bdsamples[i]* step * variable[i];
		}
		return result;
	}
}
