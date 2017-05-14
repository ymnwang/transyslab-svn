package com.transyslab.commons.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import org.apache.commons.csv.CSVRecord;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.simcore.mlp.MLPEngThread;

public class SensitiveTest extends SchedulerThread{

	public SensitiveTest(String thread_name, TaskCenter task_center) {
		super(thread_name, task_center);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		int maxTasks = 100;
		TaskCenter tc = new TaskCenter(maxTasks);
		SensitiveTest st = new SensitiveTest("SA", tc);
		st.start();
		MLPEngThread mlp_eng_thread;
		for (int i = 0; i < 100; i++) {
			mlp_eng_thread = new MLPEngThread("Eng"+i, tc);
			mlp_eng_thread.setMode(3);
//				((MLPEngine) mlp_eng_thread.engine).seedFixed = true;
			mlp_eng_thread.start();
		}
	}

	@Override
	public void run() {
		// ����100�η���
		int tasks = 100;
		int iterationLim = 1;
		// 30��һ�����ٲ���
		double[][] simSpeed = new double[200][tasks];
		double[] fitness = new double[tasks];
		System.out.println("java������...");  
		//����һ�����׽��ֲ��������ӵ�ָ�������ϵ�ָ���˿ں�  
        Socket client;
		try {
			client = new Socket("127.0.0.1", 1025);
		     //��ȡ������������    
	        BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
	        //��������˷�������    
	        PrintWriter output = new PrintWriter(client.getOutputStream(), true);
			// ��Ϣ��\n��β
	        String firstLine;
			
			firstLine = input.readLine();
			// ��ʼ���з�������
	        if (firstLine.equals("Connection built")){
	        	for (int iteri = 0; iteri < iterationLim; iteri++) {
	        		resetTaskPool(tasks);
		        	double[] param = new double[]{0.5122,20.37,0.1928,
		        			     15.993167, 0.15445936, 1.5, 5.0, 1.0, 1.0};
		        	//set parameters
		        	for (int j = 0; j < tasks; j++) {
						dispatchTask(j, param);//dispatch task
					}
		            output.print("calcFitness");
		            output.flush();
		            for(int i=0;i<tasks;i++){
	    				for(int j=0;j<200;j++){
	    					simSpeed[j][i] = fetchResult(i)[j+1];
	    				}
	    				fitness[i] = fetchResult(i)[0];
	    			}// һ��������������
		            while (true) {
		            	try {
		            		//��Ϣ��\n��β
		                    String line = input.readLine();
		                    //����python������
		                    if(line.equals("done")){
		                    	try {
		                    		
		                    		//������
		                			List<CSVRecord> dataList = CSVUtils.readCSV("R:\\ADFullerTest.csv", null);
		                			for (int j = 0; j <dataList.size(); j++) {
		                				int adftest = Integer.parseInt(dataList.get(j).get(0));
		                				if(adftest==0){
		                					// ��ƽ������,���fitness
		                					fitness[j] = 100000;
		                				}	
		        					}
	
		                		} catch (IOException e) {
		                			e.printStackTrace();
		                		}
		                        if(iteri== iterationLim-1){
		                    		// �������
			                    	output.print("exit");
			                    	output.flush();
			                    	// ����break �ȴ�"Connection closed"
			                    	continue;
		                    	}
		                    	// ����while������һ������
		                    	break;
		                    }
	
		                    if(line.equals("Connection closed")){
		                    	break;
		                    }
		    
		                      
		                } catch (Exception e) {  
		                    System.out.println("�ͻ����쳣:" + e.getMessage());   
		                } 
		            }
	
		        }// �Ż������ѽ���
	        }// if(firstLine.equals("Connection built"))����
            input.close();
            output.close();
            client.close();
            // socket try ����
			
		}catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}    	

		dismissAllWorkingThreads();//stop eng�̡߳�
		
	 }

}
