package com.transyslab.simcore.mlp;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.simcore.AppSetup;
import com.transyslab.simcore.EngTread;

public class MLPEngThread extends EngTread{
	protected int mode;
	protected double [][] paras2Cal;
	protected double [] bestfit;
	protected double fitVal;
	
	public MLPEngThread(String arg) {
		name = arg;
		AppSetup.modelType = 2;
		parameter = new MLPParameter();
		network = new MLPNetwork();
		engine = new MLPEngine();
		sim_clock = new SimulationClock();
	}
	
	@Override
	public void run() {
		engine.loadFiles();
		switch (mode) {
		case 1://calfitness
			doPatch();
			break;
		case 2://run only
			((MLPEngine) engine).run(0);
			break;
		default:
			break;
		}
	}
	
	//1. set the paras2Cal 2.give bestfit and related fitVal
	public void doPatch() {
		MLPEngine mlpEngine = (MLPEngine) engine;
		bestfit = null;
		fitVal = mlpEngine.calFitness(bestfit);
		System.out.println("fitVal is " + fitVal);
	};
	
	//此入口作为引擎测试用
	public static void main(String args[]) {
		MLPEngThread myThread = new MLPEngThread("testingThread");
		myThread.mode = 1;
		myThread.start();
	}
}
