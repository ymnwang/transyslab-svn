package com.transyslab.simcore.mlp.Functions;

import com.transyslab.commons.tools.Function;

public class QSDM_Eq extends Function{
	
	QSDMax qsdMax;
	
	public QSDM_Eq() {
		paras = new double[3];//[0]VF [1]Kj [2] Qm_ob
		qsdMax = new QSDMax();
	}
	
	public QSDM_Eq(double VF, double Kj, double Qm_ob) {
		paras = new double[] {VF, Kj, Qm_ob};
		qsdMax = new QSDMax(VF, Kj);
	}
	
	public void setParas(double VF, double Kj, double Qm_ob) {
		paras[0] = VF;
		paras[1] = Kj;
		paras[2] = Qm_ob;		
		qsdMax.setParas(VF, Kj);
	}

	@Override
	public double cal(double[] inputs) {//[0]alpha; [1]beta
		return qsdMax.cal(inputs) - paras[2];
	}

	@Override
	public double[] cals(double[] inputs) {
		// TODO Auto-generated method stub
		return null;
	}

}
