package com.transyslab.simcore.mlp.Functions;

import com.transyslab.commons.tools.Function;

public class KSDM_Eq extends Function{
	
	KSDMax ksd;
	
	public KSDM_Eq() {
		paras = new double [2];//[0]Kj; [1]Ksdmax_star
		ksd = new KSDMax();
	}
	
	public KSDM_Eq(double Kj, double Kstar) {
		paras = new double[] {Kj, Kstar};
		ksd = new KSDMax();
		ksd.setKj(Kj);
	}
	
	public void setKstar(double Kstar) {
		paras[1] = Kstar;
	}
	
	public void setParas(double Kj, double Kstar) {
		paras[0] = Kj;
		paras[1] = Kstar;
		ksd.setKj(Kj);
	}

	@Override
	public double cal(double[] inputs) {//[0]alpha; [1]beta
		return ksd.cal(inputs)-paras[1];
	}

	@Override
	public double[] cals(double[] inputs) {
		// TODO Auto-generated method stub
		return null;
	}
}
