package com.transyslab.simcore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.commons.csv.CSVRecord;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.tools.Producer;
import com.transyslab.commons.tools.Worker;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.RoadNetworkPool;
import com.transyslab.simcore.mesots.MesoNetworkPool;
import com.transyslab.simcore.mlp.MLPNetworkPool;

public class ConnPyMain {

	public static void main(String[] args) throws UnknownHostException, IOException {
		//MLPģ��
		AppSetup.modelType = 2;
		RoadNetworkPool infoarrays;
		//ѡ�����ģ��
		if(AppSetup.modelType == 1)
			infoarrays = MesoNetworkPool.getInstance();
		else {
			infoarrays = MLPNetworkPool.getInstance();
		}
		SimulationEngine[] engines = new SimulationEngine[Constants.THREAD_NUM];

		Producer[] producerList = new Producer[Constants.THREAD_NUM];
		List<FutureTask<SimulationEngine>> taskList = new ArrayList<FutureTask<SimulationEngine>>();
		Thread[] threadList = new Thread[Constants.THREAD_NUM];
		for (int i = 0; i < Constants.THREAD_NUM; i++) {
			
			producerList[i] = new Producer(engines[i]);
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
				engines[tempi] = task.get();
			}
			catch (InterruptedException | ExecutionException e1) {
				
				e1.printStackTrace();
			}
			tempi++;
		}
		
		Worker[] workerList = new Worker[Constants.THREAD_NUM];

		for (int i = 0; i < Constants.THREAD_NUM; i++) {
			workerList[i] = new Worker(engines[i]);
			threadList[i] = new Thread(workerList[i]);
		}
		RoadNetworkPool.getInstance().organizeHM(threadList);
		for (int i = 0; i < Constants.THREAD_NUM; i++) {
			threadList[i].start();
		}
		while(threadList[0].isAlive()){
			try {
				Thread.currentThread().sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("�ͻ�������...");  
		//����һ�����׽��ֲ��������ӵ�ָ�������ϵ�ָ���˿ں�  
        Socket client = new Socket("127.0.0.1", 1025);
        //��ȡ������������    
        BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
        //��������˷�������    
        PrintWriter output = new PrintWriter(client.getOutputStream(), true);
		// ��Ϣ��\n��β
        String firstLine = input.readLine();
        if (firstLine.equals("Connection built")){
        	System.out.println("Connection success");
            output.print("calcFitness");
            output.flush();
            while (true) {
            	try {
            		//��Ϣ��\n��β
                    String line = input.readLine();
                    //����python������
                    if(line.equals("done")){
                    	try {
                			List<CSVRecord> dataList = CSVUtils.readCSV("R:\\ADFullerTest.csv", null);

                		} catch (IOException e) {
                			e.printStackTrace();
                		}
                    	output.print("exit");
                    	output.flush();
                    }
                    if(line.equals("Connection closed")){
                    	break;
                    } 
                      
                } catch (Exception e) {  
                    System.out.println("�ͻ����쳣:" + e.getMessage());   
                } 
            }
            input.close();
            output.close();
            client.close();
        }
 
	}

}
