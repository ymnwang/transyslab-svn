package com.transyslab.commons.tools.optimizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.transyslab.commons.tools.mutitask.SchedulerThread;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskWorker;
import org.apache.commons.csv.CSVRecord;

import com.transyslab.commons.io.CSVUtils;

public class Sensitive2 extends SchedulerThread {
	public Sensitive2(String thread_name, TaskCenter task_center) {
		super(thread_name, task_center);
		// TODO Auto-generated constructor stub
	}

//	public static void main(String[] args) {
//		int maxTasks = 10;
//		TaskCenter tc = new TaskCenter(maxTasks,21);
//		Sensitive2 st = new Sensitive2("SA2", tc);
//		st.start();
//		MLPEngThread mlp_eng_thread;
//		for (int i = 0; i < 10; i++) {
//			mlp_eng_thread = new MLPEngThread("Eng"+i, tc);
//			mlp_eng_thread.setMode(7);
////				((MLPEngine) mlp_eng_thread.engine).seedFixed = true;
//			mlp_eng_thread.start();
//		}
//	}

	@Override
	public void run() {
		// 运行100次仿真
		int tasks = 10;
		int iterationLim = 1;
		// 5min一个车速采样
		double[][] simSpeed = new double[20][tasks];
		double[] fitness = new double[tasks];
		System.out.println("java端启动...");  
		//创建一个流套接字并将其连接到指定主机上的指定端口号  
        Socket client;
		try {
			client = new Socket("127.0.0.1", 1025);
		     //读取服务器端数据    
	        BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
	        //向服务器端发送数据    
	        PrintWriter output = new PrintWriter(client.getOutputStream(), true);
			// 消息以\n结尾
	        String firstLine;
			
			firstLine = input.readLine();
			// 开始运行仿真任务
	        if (firstLine.equals("Connection built")){
				List<Task> taskList = new ArrayList<>();
	        	for (int iteri = 0; iteri < iterationLim; iteri++) {
	        		taskList.clear();
	        		boolean disturbSeed = false;
	        		// in modelParam
	        		int disturbIndex = 7;
	        		float disturbStep = 0.1f;
	        		double [] randomSeed = new double[]{52499178,89267437,83743587,3757102,99688480,89731513,44129919,
	        				 78995983,61932935,3493463};
		        	double[] modelParam = new double[]{0.5122,20.37,0.1928,
		        			0.14, 5.1846, 1.8,  5.0, 1.0, 1.0};/*{0.5122,20.37,0.1928,
		        			     0.14, 5.1846, 1.8, 5.0, 1.0, 1.0};fitness = 0.18*/
		        	double[] disturbParam = new double[tasks];
		        	for(int i=0;i<tasks;i++){
		        		disturbParam[i] = modelParam[disturbIndex] + i*disturbStep; 
		        	}
		        	double[][] param = new double[tasks][10];
		        	for(int i=0;i<tasks;i++){
		        		for(int j=0;j<10;j++){
		        			if(disturbSeed){
		        				if(j ==0)
			        				param[i][j] = randomSeed[i];
			        			else
			        				param[i][j] = modelParam[j-1];
		        			}
		        			else{
		        				if(j ==0)
		        					// 固定种子
			        				param[i][j] = randomSeed[1];
			        			else if(j == disturbIndex+1)
			        				// 扰动变量
			        				param[i][j] = disturbParam[i];
			        			else 
			        				param[i][j] = modelParam[j-1];
	
		        			}
		        			
		        		}
		        	}
		        	//set parameters
		        	for (int j = 0; j < tasks; j++) {
		        		taskList.add(dispatch(param[j], TaskWorker.ANY_WORKER));
					}
		        	for(int i=0;i<tasks;i++){
		        		double[] result_i = taskList.get(i).getObjectiveValues();
	    				for(int j=0;j<20;j++){
	    					simSpeed[j][i] = result_i[j+1];
	    				}
	    				fitness[i] = result_i[0];
	    			}// 一代仿真计算已完成
		        	CSVUtils.writeCSV("R:\\SimResults2.csv", null, simSpeed);
		        	CSVUtils.writeCSV("R:\\fitnessKS2.csv", null, fitness);
		            output.print("calcFitness");
		            output.flush();
		            while (true) {
		            	try {
		            		//消息以\n结尾
		                    String line = input.readLine();
		                    //接收python计算结果
		                    if(line.equals("done")){
		                    	try {
		                    		
		                    		//处理结果
		                    		List<CSVRecord> dataList = CSVUtils.readCSV("R:\\ADFullerTest2.csv", null);
		                			for (int j = 0; j <dataList.size(); j++) {
		                				double adftest = Double.parseDouble(dataList.get(j).get(0));
		                				if(adftest==0){
		                					// 非平稳序列,标记fitness
		                					fitness[j] = 100000;
		                				}	
		        					}
	
		                		} catch (IOException e) {
		                			e.printStackTrace();
		                		}
		                        if(iteri== iterationLim-1){
		                    		// 计算完成
			                    	output.print("exit");
			                    	output.flush();
			                    	// 跳过break 等待"Connection closed"
			                    	continue;
		                    	}
		                    	// 跳出while进行下一代计算
		                    	break;
		                    }
	
		                    if(line.equals("Connection closed")){
		                    	break;
		                    }
		    
		                      
		                } catch (Exception e) {
		                	e.printStackTrace();
		                    System.out.println("客户端异常:" + e.getMessage());   
		                } 
		            }
	
		        }// 优化迭代已结束
	        }// if(firstLine.equals("Connection built"))结束
            input.close();
            output.close();
            client.close();
            // socket try 结束
			
		}catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}    	

		dismissAllWorkingThreads();//stop eng线程。
		
	 }

}
