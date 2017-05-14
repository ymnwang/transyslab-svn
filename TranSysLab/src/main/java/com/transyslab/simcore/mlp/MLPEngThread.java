package com.transyslab.simcore.mlp;

import java.util.concurrent.TimeUnit;

import com.transyslab.commons.tools.EngTread;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.commons.tools.TaskCenter;
import com.transyslab.commons.tools.TimeMeasureUtil;
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
			mlp_eng.needEmpData = true;
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
			
		case 3://work in taskCenter,带速度序列的结果返回
			mlp_eng.needEmpData = true;
			while (!isDismissed()) {
				double[] task = null;
				task = retrieveTask();//尝试从任务中心taskCenter取回任务
				if (task != null) {
//					System.out.println(Thread.currentThread().getName() + " received TID " + (int) task[0]);
					double [] p = unzipTask(task);
					double[] fitVal = mlp_eng.calFitness3(p);
					uploadResult((int) task[0], fitVal);//将结果返回任务中心taskCenter
				}
			}
			break;
			case 4:
				mlp_eng.needEmpData = true;
				mlp_eng.needRndETable = true;
				mlp_eng.seedFixed = true;
				if (mlp_eng.seedFixed) {
					mlp_eng.runningseed = 1490183749797l;
				}
				TimeMeasureUtil timer = new TimeMeasureUtil();
				for (int i = 0; i < 10; i++) {
					timer.tic();
					System.out.println(mlp_eng.calFitness(new double [] {16.87, 0.137, 0.2519, 1.8502, 1.3314, 33.3333}));
					System.out.println(timer.toc());
				}
				break;
			case 5://calculated para
				mlp_eng.needRndETable = true;
				mlp_eng.infoOn = true;
				//{17.251682, 0.13062555, 2.286993, 4.1652145, 1.0193955, 39.612854}
				System.out.println("Capacity activated: " + mlp_eng.validate(new double [] {20.37, 0.1042, 0.1458, 0.7649, 0.8069, 19.81}));
			break;
			case 6://work in workCenter Ver. 2
				mlp_eng.needEmpData = true;
				while (!isDismissed()) {
					double[] task = null;
					task = retrieveTask();//尝试从任务中心taskCenter取回任务
					if (task != null) {
//					System.out.println(Thread.currentThread().getName() + " received TID " + (int) task[0]);
						double [] p = unzipTask(task);
						double fitVal = mlp_eng.calFitness2(p);
						uploadResult((int) task[0], new double[]{fitVal});//将结果返回任务中心taskCenter
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
		myThread.setMode(4);
		myThread.start();
	}
}
