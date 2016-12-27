package com.transyslab.simcore.mesots;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.transyslab.commons.tools.DE;
import com.transyslab.commons.tools.Producer;
import com.transyslab.commons.tools.SPSA;
import com.transyslab.commons.tools.Worker;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.SimulationEngine;


public class Meso2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long begintime = System.currentTimeMillis();
		long[] endtime = new long[2000];
		float[] fitness = new float[2000];
//		int iteration = 0;
//	    float[] plower = new float[]{10.0f,80.0f,0f,0f};
//        float[] pupper = new float[]{80.0f,180.0f,10.0f,10.0f};
//        DE de = new DE();
//        de.initDE(30, 4, 0.5f, 0.5f, plower, pupper);
		float[] plower = new float[]{ 0f, 0f,100.0f,0.0f,13.9f};
		float[] pupper = new float[]{10f,10f,200.0f,5.5f,25.0f};
		float[] pinit = new float[]{5f,8f,150f,2f,20f};
        SPSA spsa = new SPSA(5);
        spsa.setAlgParameters(0.5, 50, 0.602, 0.1, 0.101);
        spsa.setBounderies(plower, pupper);
        spsa.setParameters(pinit);
        MesoNetworkPool infoarrays = MesoNetworkPool.getInstance();
	    
		MesoEngine[] engineList =  new MesoEngine[Constants.THREAD_NUM];		
		Worker[] workerList = new Worker[Constants.THREAD_NUM];
		Producer[] producerList = new Producer[Constants.THREAD_NUM];
		List<FutureTask<SimulationEngine>> taskList = new ArrayList<FutureTask<SimulationEngine>>();
		Thread[] threadList = new Thread[Constants.THREAD_NUM];
		for(int i=0;i<Constants.THREAD_NUM;i++){
			producerList[i] = new Producer(engineList[i],spsa);
			taskList.add(new FutureTask<SimulationEngine>(producerList[i]));
			threadList[i] = new Thread(taskList.get(i));
		}
		infoarrays.init(Constants.THREAD_NUM,infoarrays);
		infoarrays.organizeHM(threadList);
		
		for(int i=0; i<Constants.THREAD_NUM;i++){
			threadList[i].start();
		}

		int tempi = 0;
		for(FutureTask<SimulationEngine> task : taskList){
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
		System.out.println("�����ʼ�����������ʱ�䣺"+(endtime[0]-begintime)+"ms");
		//���Engine��ʼ��
		int runtimes = 1;
		while(runtimes<=20){
			
			if(runtimes>=2){
				//�ݶȱƽ�
				spsa.estimateGradient(engineList[0].getObjFunction(), engineList[1].getObjFunction());
				//����spsa�����parameter������[0,1]���䣩��ͬʱ���µ�����engine�Ĳ���
				spsa.updateParameters(runtimes, engineList[2].getParameters());	
			}
			//�Ŷ����������µ�һ������engine�Ĳ���
			spsa.perturbation(runtimes, engineList[0].getParameters(), engineList[1].getParameters());
			
			CyclicBarrier barrier = new CyclicBarrier(Constants.THREAD_NUM+1);
			for(int i=0; i<Constants.THREAD_NUM;i++){
				workerList[i] = new Worker(engineList[i],barrier);
				threadList[i] = new Thread(workerList[i]);
			}
			
			infoarrays.organizeHM(threadList);
			for(int i=0; i<Constants.THREAD_NUM;i++){
				threadList[i].start();
			}
			try {
				barrier.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
			/*
			for(int i=0;i<Constants.THREAD_NUM;i++){
				if(de.getGbestFitness() > engineList[i].getTempBestFitness()){
					de.setGbestFitness(engineList[i].getTempBestFitness());
					de.setGbest(engineList[i].getTempBest());
				}
			}
//			fitness[runtimes-1] = de.getGbestFitness();
			System.out.println("Fitness: "+de.getGbestFitness());
//			endtime[runtimes] = System.currentTimeMillis();*/
			//���²���
			System.out.println(engineList[2].getObjFunction());
			runtimes++;			
		}
		/*
		for(int i=1;i<(runtimes);i++){
			System.out.println("�������е�"+ (i-1) +"�����������ʱ�䣺"+(endtime[i]-endtime[i-1])/1000+"s");
		}
*//*
		for(int j=0;j<de.getDim();j++){
			System.out.println(de.getGbest()[j]);
		}*/
	}

}
