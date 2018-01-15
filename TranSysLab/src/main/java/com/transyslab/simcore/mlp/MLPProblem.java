package com.transyslab.simcore.mlp;

import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.tools.adapter.SimProblem;
import org.apache.commons.configuration2.Configuration;

/**
 * Created by wangyimin on 2017/12/29.
 */
public abstract class MLPProblem extends SimProblem {
	private double qm,vf,kj,vp,deltaT;
	protected Configuration config;
	public MLPProblem(){ }
	public MLPProblem(String masterFileName){
		initProblem(masterFileName);
	}

	@Override
	public void initProblem(String masterFileName) {
		this.config = ConfigUtils.createConfig(masterFileName);

		//parsing
		String obParaStr = config.getString("obParas");
		String[] parasStrArray = obParaStr.split(",");
		double[] ob_paras = new double[parasStrArray.length];
		for (int i = 0; i<parasStrArray.length; i++) {
			ob_paras[i] = Double.parseDouble(parasStrArray[i]);
		}

		this.qm = ob_paras[0];
		this.vf = ob_paras[1];
		this.kj = ob_paras[2];
		this.vp = ob_paras[3];
		this.deltaT = Double.parseDouble(config.getString("timeStep"));
	}

	public Configuration getConfig(){
		checkConfig();
		return config;
	}

	public void checkConfig(){
		if (config==null)
			System.err.println("this problem has no config info.");
	}

	/**
	 * Xc下界由观测参数和设置值确定
	 * */
	public double getXcLower() {
		checkConfig();
		return MLPParameter.xcLower(kj, qm, deltaT);
	}

	/**
	 * r上界由Xc取值确定
	 * */
	public double getRLower(double Xc) {
		checkConfig();
		return MLPParameter.rLowerFunc.findRoot(10000.0,new double[]{qm, vf, kj, vp, deltaT, Xc});
	}

	public void updateQm(double qm) {
		this.qm = qm;
	}

	public void updateKj(double kj) {
		this.kj = kj;
	}

	public void updateVf(double vf) {
		this.vf = vf;
	}

	public void updateVp(double vp) {
		this.vp = vp;
	}

	public void updateDeltaT(double deltaT) {
		this.deltaT = deltaT;
	}
}
