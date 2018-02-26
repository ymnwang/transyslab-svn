package com.transyslab.simcore.mlp;

import com.transyslab.commons.tools.Constraint;
import com.transyslab.commons.tools.NewtonFunction;

import java.util.HashMap;

/**
 * Created by WangYimin on 2018/2/22.
 */
public class InterConstraints {
	public HashMap<String, Constraint> cMap;
	private double kjUpper, kjLower, qm, vf_CF, vf_SD;

	/**
	 * paras: [0]qm, [1]kj, [2]vf_CF, [3]vf_SD*/
	public static NewtonFunction alphaUpperFunc = new NewtonFunction() {
		@Override
		public double calculate(double input, double[] paras) {
			double qm = paras[0], kj = paras[1], vf_CF = paras[2], vf_SD = paras[3];
			double beta = calBeta(input, kj, qm, vf_CF, vf_SD);
			return kj / Math.pow(1+input*beta,1/input) - qm/vf_CF;
		}
	};

	public InterConstraints(){
		cMap = new HashMap();
	}

	public InterConstraints(double[] paras){
		cMap = new HashMap();
		init(paras[0],paras[1],paras[2],paras[3],paras[4]);
	}

	public InterConstraints(double kjUpper, double kjLower, double qm, double vf_CF, double vf_SD){
		cMap = new HashMap();
		init(kjUpper,kjLower,qm,vf_CF,vf_SD);
	}

	public InterConstraints init(double kjUpper, double kjLower, double qm, double vf_CF, double vf_SD) {
		this.kjUpper = kjUpper;
		this.kjLower = kjLower;
		this.qm = qm;
		this.vf_CF = vf_CF;
		this.vf_SD = vf_SD;
		return initConstraint();
	}

	private InterConstraints initConstraint(){
		cMap.put("kj",
				new Constraint() {
					@Override
					public boolean checkViolated(double arg, double[] relatedParas){
						return (arg<kjLower || arg>kjUpper);
					}
				});
		cMap.put("alpha",
				new Constraint() {
					double alphaLower = 0.0;
					@Override
					public boolean checkViolated(double arg, double[] relatedParas){
						double kj = relatedParas[0];
						double alphaUpper = calAlphaUpper(kj);
						return (arg<=alphaLower || arg>alphaUpper);
					}
				});
		cMap.put("vp",
				new Constraint() {
					@Override
					public boolean checkViolated(double arg, double[] relatedParas){
						return (arg<=vf_CF || arg<=vf_SD);
					}
				});
		cMap.put("deltaT",
				new Constraint() {
					@Override
					public boolean checkViolated(double arg, double[] relatedParas){
						double kj = relatedParas[0];
						return arg >= 1.0/qm - 1.0/kj/Math.min(vf_CF,vf_SD);
					}
				});
		return this;
	}

	public double calBeta(double r, double kj) {
		return Math.log(qm/kj/vf_SD) / (Math.log(r/(r+1)) - Math.pow(r,-1.0)*Math.log(r+1));
	}

	public double calXc() {
		return vf_CF/qm;
	}

	public double calTs(double kj, double deltaT) {
		double phi = (calXc() - 1/kj) / vf_CF;
		return phi - deltaT;
	}

	public Constraint getConstraint(String paraName) {
		return cMap.get(paraName);
	}

	public double calAlphaUpper(double kj_input){
		return alphaUpperFunc.findRoot(0.05, new double[]{qm,kj_input,vf_CF,vf_SD});
	}

	public static double calBeta(double alpha, double kj, double qm, double vf_CF, double vf_SD){
		return Math.exp(vf_CF / vf_SD) / Math.exp(1-Math.pow(qm/vf_CF/kj,alpha));
	}

	public static double calDeltaTUpper(double qm, double vf, double kj) {
		return 1.0/qm - 1.0/vf/kj;
	}

}
