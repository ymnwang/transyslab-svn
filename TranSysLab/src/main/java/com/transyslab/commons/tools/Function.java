package com.transyslab.commons.tools;

public abstract class Function {
	protected double [] paras;
//	public Function(double[] args) {
//		paras = args;
//	}	
	public abstract double cal(double[] inputs);
	public abstract double[] cals(double[] inputs);
	public double[] translate(float[] args) {
		if (args.length <= 0) {
			return null;
		}
		double[] ans = new double[args.length];
		for (int i = 0; i < ans.length; i++) {
			ans[i] = args[i];
		}
		return ans;
	}
}
