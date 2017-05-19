package com.transyslab.simcore.mlp.Functions;

import com.transyslab.commons.tools.Function;

public class FunsCombination2 extends Function{
	QSDM_Eq qsdM_eq;
	KSDM_Eq ksdM_eq;
	
	public FunsCombination2() {
		paras = new double[4];//[0]VF [1]Kj [2] Qm_ob [3]Kstar
		qsdM_eq = new QSDM_Eq();
		ksdM_eq = new KSDM_Eq();
	}
	
	public FunsCombination2(double VF, double Kj, double Qm_ob, double Kstar) {
		paras = new double[] {VF, Kj, Qm_ob, Kstar};
		qsdM_eq = new QSDM_Eq(VF, Kj, Qm_ob);
		ksdM_eq = new KSDM_Eq(Kj, Kstar);
	}
	
	public void setParas(double VF, double Kj, double Qm_ob, double Kstar) {
		paras[0] = VF;
		paras[1] = Kj;
		paras[2] = Qm_ob;
		paras[3] = Kstar;
		qsdM_eq.setParas(VF, Kj, Qm_ob);
		ksdM_eq.setParas(Kj, Kstar);
	}
	
	public void setCondition(double Kstar) {
		ksdM_eq.setKstar(Kstar);
	}

	@Override
	public double cal(double[] inputs) {
		return Math.pow(qsdM_eq.cal(inputs), 2) + Math.pow(ksdM_eq.cal(inputs), 2);
	}

	@Override
	public double[] cals(double[] inputs) {//[0]alpha; [1]beta
		return new double[] {qsdM_eq.cal(inputs), ksdM_eq.cal(inputs)};
	}
}
