package com.transyslab.simcore.mlp.Functions;

import com.transyslab.commons.tools.Function;

public class QSDMax extends Function{
	
	public QSDMax() {
		paras = new double[2];//[0]VF [1]Kj
	}
	
	public QSDMax(double VF, double Kj) {
		paras = new double[] {VF, Kj};
	}
	
	public void setParas(double VF, double Kj) {
		paras[0] = VF;
		paras[1] = Kj;
	}

	@Override
	public double cal(double[] inputs) {//[0]alpha; [1]beta
		if (inputs.length!=2) 
			return Double.NaN;
		double tmp = inputs[0]*inputs[1];
		return ( paras[0] * paras[1] * Math.pow(tmp/(1+tmp), inputs[1]) ) / Math.pow(1+tmp, 1/inputs[0]);
	}

	@Override
	public double[] cals(double[] inputs) {
		// TODO Auto-generated method stub
		return null;
	}

}
