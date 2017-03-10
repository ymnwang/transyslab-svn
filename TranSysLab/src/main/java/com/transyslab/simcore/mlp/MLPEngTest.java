package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.transyslab.commons.tools.Producer;
import com.transyslab.commons.tools.Worker;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.SimulationEngine;

public class MLPEngTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MLPNetworkPool infoarrays = MLPNetworkPool.getInstance();
		SimulationEngine[] engineList = new SimulationEngine[1];
		Worker[] workerList = new Worker[1];
		Producer[] producerList = new Producer[1];
		List<FutureTask<SimulationEngine>> taskList = new ArrayList<FutureTask<SimulationEngine>>();
		Thread[] threadList = new Thread[1];
		for (int i = 0; i < 1; i++) {
			producerList[i] = new Producer(engineList[i]);
			taskList.add(new FutureTask<SimulationEngine>(producerList[i]));
			threadList[i] = new Thread(taskList.get(i));
		}
		infoarrays.init(1, infoarrays);
		infoarrays.organizeHM(threadList);
		
		for (int i = 0; i < Constants.THREAD_NUM; i++) {
			threadList[i].start();
		}

		int tempi = 0;
		for (FutureTask<SimulationEngine> task : taskList) {
			try {
				engineList[tempi] = (MLPEngine) task.get();
			}
			catch (InterruptedException | ExecutionException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			tempi++;
		}

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
	}

}
