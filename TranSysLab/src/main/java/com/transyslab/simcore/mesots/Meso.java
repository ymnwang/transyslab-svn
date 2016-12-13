/**
 *
 */
package com.transyslab.simcore.mesots;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.SwingUtilities;

import com.transyslab.commons.renderer.JOGL_RenderContainer;
import com.transyslab.commons.tools.Producer;
import com.transyslab.commons.tools.Worker;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.SimulationEngine;

/**
 * @author yali
 *
 */
public class Meso {

	/**
	 * @param args
	 */
	/*
	 * public static void main(String[] args) { long begintime =
	 * System.currentTimeMillis(); long[] endtime = new long[2000]; float[]
	 * fitness = new float[2000]; // int iteration = 0; float[] plower = new
	 * float[]{10.0f,80.0f,0f,0f}; float[] pupper = new
	 * float[]{80.0f,180.0f,10.0f,10.0f}; // float[] plower = new
	 * float[]{0f,0f}; // float[] pupper = new float[]{10.0f,10.0f}; DE de = new
	 * DE(); de.initDE(30, 4, 0.5f, 0.5f, plower, pupper);
	 *
	 *
	 * MESO_Engine[] engineList = new MESO_Engine[DefinedConstant.THREAD_NUM];
	 * Worker[] workerList = new Worker[DefinedConstant.THREAD_NUM]; Producer[]
	 * producerList = new Producer[DefinedConstant.THREAD_NUM];
	 * List<FutureTask<MESO_Engine>> taskList = new
	 * ArrayList<FutureTask<MESO_Engine>>(); Thread[] threadList = new
	 * Thread[DefinedConstant.THREAD_NUM]; for(int
	 * i=0;i<DefinedConstant.THREAD_NUM;i++){ producerList[i] = new
	 * Producer(engineList[i],de); taskList.add(new
	 * FutureTask<MESO_Engine>(producerList[i])); threadList[i] = new
	 * Thread(taskList.get(i)); }
	 * MESO_InfoArrays.getInstance().init(DefinedConstant.THREAD_NUM);
	 * MESO_InfoArrays.getInstance().organizeHM(threadList);
	 *
	 * for(int i=0; i<DefinedConstant.THREAD_NUM;i++){ threadList[i].start(); }
	 *
	 * int tempi = 0; for(FutureTask<MESO_Engine> task:taskList){ try {
	 * engineList[tempi] = task.get(); } catch (InterruptedException |
	 * ExecutionException e) { // TODO 自动生成的 catch 块 e.printStackTrace(); }
	 * tempi++; } endtime[0] = System.currentTimeMillis();
	 * System.out.println("引擎初始化所需的运行时间："+(endtime[0]-begintime)+"ms");
	 * //完成Engine初始化 int runtimes = 1; while(runtimes<=5000){ CyclicBarrier
	 * barrier = new CyclicBarrier(DefinedConstant.THREAD_NUM+1); for(int i=0;
	 * i<DefinedConstant.THREAD_NUM;i++){ workerList[i] = new
	 * Worker(engineList[i],barrier); threadList[i] = new Thread(workerList[i]);
	 * }
	 *
	 * MESO_InfoArrays.getInstance().organizeHM(threadList); for(int i=0;
	 * i<DefinedConstant.THREAD_NUM;i++){ threadList[i].start(); } try {
	 * barrier.await(); } catch (InterruptedException | BrokenBarrierException
	 * e) { // TODO 自动生成的 catch 块 e.printStackTrace(); } for(int
	 * i=0;i<DefinedConstant.THREAD_NUM;i++){ if(de.getGbestFitness() >
	 * engineList[i].getTempBestFitness()){
	 * de.setGbestFitness(engineList[i].getTempBestFitness());
	 * de.setGbest(engineList[i].getTempBest()); } } // fitness[runtimes-1] =
	 * de.getGbestFitness(); System.out.println("Fitness: "
	 * +de.getGbestFitness()); // endtime[runtimes] =
	 * System.currentTimeMillis(); runtimes++; } /* for(int
	 * i=1;i<(runtimes);i++){ System.out.println("程序运行第"+ (i-1)
	 * +"代所需的运行时间："+(endtime[i]-endtime[i-1])/1000+"s"); }
	 */
	/*
	 * for(int j=0;j<de.getDim();j++){ System.out.println(de.getGbest()[j]); } }
	 */

	public static void main(String[] args) {
		long begintime = System.currentTimeMillis();
		long[] endtime = new long[20];
		MesoNetworkPool infoarrays = MesoNetworkPool.getInstance();
		MesoEngine[] engineList = new MesoEngine[Constants.THREAD_NUM];
		Worker[] workerList = new Worker[Constants.THREAD_NUM];
		Producer[] producerList = new Producer[Constants.THREAD_NUM];
		List<FutureTask<SimulationEngine>> taskList = new ArrayList<FutureTask<SimulationEngine>>();
		Thread[] threadList = new Thread[Constants.THREAD_NUM];
		for (int i = 0; i < Constants.THREAD_NUM; i++) {
			producerList[i] = new Producer(engineList[i]);
			taskList.add(new FutureTask<SimulationEngine>(producerList[i]));
			threadList[i] = new Thread(taskList.get(i));
		}
		infoarrays.init(Constants.THREAD_NUM, infoarrays);
		infoarrays.organizeHM(threadList);

		for (int i = 0; i < Constants.THREAD_NUM; i++) {
			threadList[i].start();
		}

		int tempi = 0;
		for (FutureTask<SimulationEngine> task : taskList) {
			try {
				engineList[tempi] = (MesoEngine) task.get();
			}
			catch (InterruptedException | ExecutionException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			tempi++;
		}
		endtime[0] = System.currentTimeMillis();
		System.out.println("引擎初始化所需的运行时间：" + (endtime[0] - begintime) + "ms");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JOGL_RenderContainer drawnetwork = new JOGL_RenderContainer(); // run the constructor
				drawnetwork.init();
				drawnetwork.render();
			}
		});
		int runtimes = 1;
		while (runtimes <= 1) {
			CyclicBarrier barrier = new CyclicBarrier(Constants.THREAD_NUM + 1);
			for (int i = 0; i < Constants.THREAD_NUM; i++) {
				workerList[i] = new Worker(engineList[i], barrier);
				threadList[i] = new Thread(workerList[i]);
			}

			infoarrays.organizeHM(threadList);
			for (int i = 0; i < Constants.THREAD_NUM; i++) {
				threadList[i].start();
			}
			try {
				barrier.await();
			}
			catch (InterruptedException | BrokenBarrierException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			runtimes++;
		}

		System.out.print("done");

	}

}
