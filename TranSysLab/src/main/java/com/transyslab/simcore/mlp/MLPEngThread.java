package com.transyslab.simcore.mlp;

import java.util.concurrent.TimeUnit;

import com.transyslab.commons.tools.EngTread;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.commons.tools.TaskCenter;
import com.transyslab.simcore.AppSetup;

public class MLPEngThread extends EngTread{
	private int mode;
	protected double [][] paras2Cal;
	protected double [] bestfit;
	protected double fitVal;
	protected int runtimes;
	
	public MLPEngThread(String arg) {
		super("arg", null);//不经过任务中心
		AppSetup.modelType = 2;
		parameter = new MLPParameter();
		network = new MLPNetwork();
		engine = new MLPEngine();
		sim_clock = new SimulationClock();
		fitVal = Double.POSITIVE_INFINITY;
		runtimes = 100;
	}
	public MLPEngThread(String arg, TaskCenter tc) {
		super(arg,tc);
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
		
		case 1://calfitness testing
			if (paras2Cal == null) {
				break;
			}
			for (int i = 0; i < paras2Cal.length; i++) {
				System.out.println("fitness: " + mlp_eng.calFitness(null));
			}
			break;
			
		case 2://run
			mlp_eng.run(2);
			break;
			
		case 3://work in taskCenter
			while (!isDismissed()) {
				double[] task = null;
				task = retrieveTask();//尝试从任务中心taskCenter取回任务
				if (task != null) {
//					System.out.println(Thread.currentThread().getName() + " received TID " + (int) task[0]);
					double [] p = unzipTask(task);
					double fitVal = mlp_eng.calFitness(p);
					uploadResult((int) task[0], fitVal);//将结果返回任务中心taskCenter
				}
			}
			break;

		default:
			break;
		}
	}
	
	//此入口作为引擎测试用
	public static void main(String args[]) {
//		MLPEngThread myThread = new MLPEngThread("testingThread");
//		myThread.paras2Cal = new double [][]  {{17.11733627319336, 0.0, 0.16191235184669495, 3.1216490268707275, 2.041151285171509, 0.6942391395568848, 3.5411601066589355},
//																	  /*{15.446934, 0.0, 0.17046615, 4.099085, 1.6467584, 43.413956, 6.3020988},
//																	  {15.446934, 0.0, 0.17046615, 4.099085, 1.6467584, 43.413956, 6.3020988},
//																	  {15.446934, 0.0, 0.17046615, 4.099085, 1.6467584, 43.413956, 6.3020988},
//																	  {15.446934, 0.0, 0.17046615, 4.099085, 1.6467584, 43.413956, 6.3020988},
//																	  {15.446934, 0.0, 0.17046615, 4.099085, 1.6467584, 43.413956, 6.3020988}*/};
//		myThread.setMode(1);//updatefitness
//		((MLPEngine) myThread.engine).seedFixed = true;
//		((MLPEngine) myThread.engine).runningseed = 1490183749797l;
//		myThread.start();
		MLPEngThread myThread = new MLPEngThread("testingThread");
		myThread.setMode(2);
		myThread.start();
	}
}
