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

//SPSA����У׼����
public class Meso2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long begintime = System.currentTimeMillis();
		long[] endtime = new long[2000];
		float[] fitness = new float[2000];

		float[] plower = new float[]{1f,5.0f,150.0f,18.0f,25,85};//,   130,   16,20,50};
		float[] pupper = new float[]{4f,8.0f,170.0f,23.0f,35,95};//,180.0f,25,40,100};
		//0.45f,0.0f, 21.95f, 156.25f,1.61f,6.31f �������
		//2.2822566,5.56166,154.72292,19.469088,32.80778,91.904686 0.108
//		float[] pinit = new float[]{1.61f,6.31f,156.25f,21.95f,30.48f,91.44f};/*,150f,2.5f,20f};*/
//		float[] pinit = new float[]{30.48f,91.44f};/*,150f,2.5f,20f};*/
		float[] pinit = new float[]{1.61f,6.31f,156.25f,21.95f,30.48f,91.44f};
        SPSA spsa = new SPSA(6);
        //Spall����20��100��0.602��1.9��0.101
        spsa.setAlgParameters(0.5, 50, 0.602, 0.1, 0.101);
        spsa.setBounderies(plower, pupper);
        spsa.setParameters(pinit);
        MesoNetworkPool infoarrays = MesoNetworkPool.getInstance();
	    
		SimulationEngine[] engineList =  new SimulationEngine[Constants.THREAD_NUM];		
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
				engineList[tempi] = task.get();
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
		while(runtimes<=3){

			if(runtimes>=2){
				//�ݶȱƽ�
				spsa.estimateGradient(((MesoEngine) engineList[0]).getObjFunction(), ((MesoEngine) engineList[1]).getObjFunction());
				//����spsa�����parameter������[0,1]���䣩��ͬʱ���µ�����engine�Ĳ���
				spsa.updateParameters(runtimes, ((MesoEngine) engineList[2]).getParameters());	
			}
			//�Ŷ����������µ�һ������engine�Ĳ���
			spsa.perturbation(runtimes, ((MesoEngine) engineList[0]).getParameters(), ((MesoEngine) engineList[1]).getParameters());
			
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
			//���²���
			System.out.println(((MesoEngine) engineList[2]).getObjFunction());
			if(((MesoEngine) engineList[2]).getObjFunction()<=0.13){
				for(int i=0;i<((MesoEngine) engineList[2]).getParameters().length;i++){
					System.out.println(((MesoEngine) engineList[2]).getParameters()[i]);
				}
//				break;
				
			}
			runtimes++;			
		}
	}

}
