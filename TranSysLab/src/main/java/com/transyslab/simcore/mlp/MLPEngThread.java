package com.transyslab.simcore.mlp;

import java.util.concurrent.TimeUnit;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.commons.tools.TaskCenter;
import com.transyslab.simcore.AppSetup;
import com.transyslab.simcore.EngTread;

public class MLPEngThread extends EngTread{
	private TaskCenter taskCenter;
	private int mode;
	protected double [][] paras2Cal;
	protected double [] bestfit;
	protected double fitVal;
	
	public MLPEngThread(String arg) {
		setName(arg);
		AppSetup.modelType = 2;
		parameter = new MLPParameter();
		network = new MLPNetwork();
		engine = new MLPEngine();
		sim_clock = new SimulationClock();
		fitVal = Double.POSITIVE_INFINITY;
	}
	public MLPEngThread(String arg, TaskCenter tc) {
		setName(arg);
		taskCenter = tc;
		AppSetup.modelType = 2;
		parameter = new MLPParameter();
		network = new MLPNetwork();
		engine = new MLPEngine();
		sim_clock = new SimulationClock();
		fitVal = Double.POSITIVE_INFINITY;
	}
	
	public int getMode() {
		return mode;
	}
	public void setMode(int mode) {
		this.mode = mode;
	}
	@Override
	public void run() {
//		System.out.println(Thread.currentThread().getName() + " started");
		MLPEngine mlp_eng = (MLPEngine) engine;
		mlp_eng.needEmpData = true;
		mlp_eng.loadFiles();
		switch (getMode()) {
		case 1://calfitness solo testing
			if (paras2Cal == null) {
				break;
			}
			for (int i = 0; i < paras2Cal.length; i++) {
				System.out.println("fitness: " + mlp_eng.calFitness(paras2Cal[i]));
			}
			break;
		case 2://run only solo testing
			mlp_eng.run(0);
			break;
		case 3://work in taskCenter
			while (!taskCenter.isDismissed()) {
				double[] task = null;
				try {
					task = taskCenter.undoneTasks.poll(100, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (task != null) {
//					System.out.println(Thread.currentThread().getName() + " received TID " + (int) task[0]);
					double [] p = new double [task.length - 1];
					for (int i = 0; i < p.length; i++) {
						p[i] = task[i+1];
					}
					double fitVal = mlp_eng.calFitness(p);
					taskCenter.setResult((int) task[0], fitVal);
				}
			}		
		default:
			break;
		}
	}
	
	//此入口作为引擎测试用
	public static void main(String args[]) {
		MLPEngThread myThread = new MLPEngThread("testingThread");
		myThread.paras2Cal = new double [][]  {{15.13066, 0.0, 0.19726124, 5.8963842, 1.7379211, 6.7242527, 2.4392505},
																	  {15.13066, 0.0, 0.19726124, 5.8963842, 1.7379211, 6.7242527, 2.4392505},
																	  {15.13066, 0.0, 0.19726124, 5.8963842, 1.7379211, 6.7242527, 2.4392505},
																	  {15.13066, 0.0, 0.19726124, 5.8963842, 1.7379211, 6.7242527, 2.4392505},
																	  {15.13066, 0.0, 0.19726124, 5.8963842, 1.7379211, 6.7242527, 2.4392505},
																	  {15.13066, 0.0, 0.19726124, 5.8963842, 1.7379211, 6.7242527, 2.4392505}};
		myThread.setMode(1);//updatefitness
		((MLPEngine) myThread.engine).seedFixed = true;
		myThread.start();
	}
}
