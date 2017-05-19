package com.transyslab.simcore.mlp.Functions;

import com.transyslab.commons.tools.Function;

public class QSDFun_Eq extends Function{
	QSDFun qsdFun;
	
	public QSDFun_Eq() {
		paras = new double[4];//[0]k [1]Qm_ob [2]VF [3]Kj
		qsdFun = new QSDFun();
	}

	public QSDFun_Eq(double k, double Qm_ob, double VF, double Kj) {
		paras = new double[] {k, Qm_ob, VF, Kj};
		qsdFun = new QSDFun();
		qsdFun.setParas(VF,Kj,1,1);
	}
	
	public void setPt(double k_, double Qm_ob_) {
		paras[0] = k_;
		paras[1] = Qm_ob_;
	}
	
	public void setParas(double k, double Qm_ob, double VF, double Kj) {
		paras[0] = k;
		paras[1] = Qm_ob;
		paras[2] = VF;
		paras[3] = Kj;
		qsdFun.setParas(VF, Kj, 1, 1);
	}

	@Override
	public double cal(double[] inputs) {//inputs: [0]alpha; [1]beta
		if (inputs.length!=2) 
			return Double.NaN;
		qsdFun.setAB(inputs[0], inputs[1]);
		double Qm_sd = qsdFun.cal(new double[] {paras[0]});
		return Qm_sd - paras[1];
	}

	@Override
	public double[] cals(double[] inputs) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
