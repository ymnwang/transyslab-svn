package com.transyslab.commons.tools;

public abstract class Function {
	protected double [] paras;
//	public Function(double[] args) {
//		paras = args;
//	}	
	public abstract double cal(double[] inputs);
	public abstract double[] cals(double[] inputs);

}
