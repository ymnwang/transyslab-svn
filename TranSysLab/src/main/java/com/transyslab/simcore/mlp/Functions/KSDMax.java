package com.transyslab.simcore.mlp.Functions;

import com.transyslab.commons.tools.Function;

public class KSDMax extends Function{
	
	public KSDMax() {
		paras = new double[1];
	}
	
	public void setKj(double kj) {
		paras[0] = kj;
	}
	@Override
	public double cal(double[] inputs) {//[0]alpha; [1]beta
		if (inputs.length!=2) 
			return Double.NaN;
		return paras[0]/Math.pow(1+inputs[0]*inputs[1], 1/inputs[0]);
	}

	@Override
	public double[] cals(double[] inputs) {
		// TODO Auto-generated method stub
		return null;
	}
}
