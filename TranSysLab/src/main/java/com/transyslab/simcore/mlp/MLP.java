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

public class MLP {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		long begintime = System.currentTimeMillis();
		long[] endtime = new long[20];
		MLPNetworkPool infoarrays = MLPNetworkPool.getInstance();
		MLPEngine[] engineList = new MLPEngine[Constants.THREAD_NUM];
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
				engineList[tempi] = (MLPEngine) task.get();
			}
			catch (InterruptedException | ExecutionException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			tempi++;
		}
		endtime[0] = System.currentTimeMillis();
		System.out.println("引擎初始化所需的运行时间：" + (endtime[0] - begintime) + "ms");
/*		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JOGLRenderContainer drawnetwork = new JOGLRenderContainer(); // run the constructor
				drawnetwork.init();
				drawnetwork.render();
			}
		});
		JOGLFrameQueue.getInstance().initFrameQueue();
		//延迟开始仿真任务
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
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
