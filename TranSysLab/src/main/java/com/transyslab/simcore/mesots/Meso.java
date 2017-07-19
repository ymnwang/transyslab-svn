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

import com.transyslab.commons.renderer.FrameQueue;
import com.transyslab.commons.tools.Producer;
import com.transyslab.commons.tools.Worker;
import com.transyslab.gui.MainWindow;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.SimulationEngine;

/**
 * @author yali
 *
 */
public class Meso {


	public static void main(String[] args) {
		MesoEngine engine = new MesoEngine(0,"E:\\test\\");
		engine.loadFiles();
		engine.run(0);
	}
		/*
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
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
			tempi++;
		}
		endtime[0] = System.currentTimeMillis();
		System.out.println("�����ʼ�����������ʱ�䣺" + (endtime[0] - begintime) + "ms");
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MainWindow drawnetwork = MainWindow.getInstance(); // run the constructor
//				drawnetwork.init();
				drawnetwork.setVisible(true);
				drawnetwork.render();
			}
		});
		FrameQueue.getInstance().initFrameQueue();
		//�ӳٿ�ʼ��������
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
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
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
			
			runtimes++;
		}

		System.out.print("done");

	}*/

}
