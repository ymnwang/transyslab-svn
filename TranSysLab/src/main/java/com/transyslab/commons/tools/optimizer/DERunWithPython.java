package com.transyslab.commons.tools.optimizer;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.tools.ADFullerTest;
import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskWorker;
import com.transyslab.simcore.EngThread;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MacroCharacter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yali on 2017/9/14.
 */
public class DERunWithPython {
	protected DEAlgorithm de;
	protected int simRstLength;
	private double[][] simResults;
	private double[] fvals;
	private double[] randSeed;
	private double bestRandSeed;
	//��¼ƽ�Ȳ���ʧ�ܵĲ������
	private List<Integer> failIndex;
	private List<Task> taskList;


	public DERunWithPython() {
		failIndex = new ArrayList<>();
		taskList = new ArrayList<>();
		de = new DEAlgorithm();
	}



	public void run(SchedulerThread manager) {
		System.out.println("java������...");
		//����һ�����׽��ֲ��������ӵ�ָ�������ϵ�ָ���˿ں�
		Socket client;
		//int testCounter = 0, testCounter2 = 0;
		// ���鲻ͨ���������еļ���
		int reRunCounter = 0;
		try {
			client = new Socket("127.0.0.1", 1025);
			//��ȡ������������
			BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			//��������˷�������
			PrintWriter output = new PrintWriter(client.getOutputStream(), true);
			// ��Ϣ��\n��β
			String firstLine;

			firstLine = input.readLine();

			if (firstLine.equals("Connection built")){
				System.out.println("Connection success");
				// ��ʼDE����
				for (int i = 0; i < de.getMaxItrGeneration(); i++) {
					long tb = System.currentTimeMillis();
					for (int j = 0; j < de.getPopulation(); j++) {
						double[] parameters = new double[]{0.4633,21.7950,0.1765, 0.0, 0.0, 0.0, 0.0, 0.0};
						System.arraycopy(de.getNewPosition(j),0,parameters,3,de.getDim());
						taskList.add(manager.dispatch(parameters, TaskWorker.ANY_WORKER));
					}
					simResults = new double[simRstLength][de.getPopulation()];
					for (int j = 0; j < de.getPopulation(); j++) {
						double[] tmpResults = taskList.get(j).getOutputs();
						fvals[j] = tmpResults[0];//fetch result
						randSeed[j] = tmpResults[1];
						for(int k=0;k <tmpResults.length-2;k++){
							simResults[k][j] = tmpResults[k+2];
						}
					}
					// ȡ�����з������������csv
					CSVUtils.writeCSV("R://SimResults.csv", null, simResults);

					// ����python��ʼ��ƽ���Լ���
					output.print("calcFitness");
					output.flush();
					while (true) {
						try {
							//��Ϣ��\n��β
							String line = input.readLine();
							//����python������
							if(line.equals("done")){
								try {
									// ����python������
									int tmpPreFail = failIndex.size();
									int failParams = 0;// ��ƽ�Ȳ�������
									//String filePath = "R://ADFullerTest"+testCounter+".csv";
									//System.out.println("���ڶ�ȡ"+filePath);
									List<CSVRecord> pyResults = CSVUtils.readCSV("R://ADFullerTest.csv", null);
	                    			/*if(testCounter!= testCounter2-1)
	                    				System.out.println("");
	                    			testCounter ++ ;*/
									int ri = 0;
									for(CSVRecord row:pyResults){
										if(Double.parseDouble(row.get(0)) != 1.0){
											if(reRunCounter == 0)// ��һ������
												failIndex.add(ri);
											failParams ++ ;
										}
										else{
											if(reRunCounter!=0){//��������Ч,ɾ������
												int di = ri-(tmpPreFail-failIndex.size());
												if(di>= failIndex.size())
													System.out.println("");
												failIndex.remove(ri-(tmpPreFail-failIndex.size()));
											}
										}
										ri++;
									}
									System.out.println("��"+i+"�����鲻ͨ���Ĳ�������"+failParams);
									if(failParams == 0){// ȫΪƽ������
										reRunCounter = 5 + 1;
									}
									else{
										reRunCounter ++ ;
									}
									if(reRunCounter <= 5 ){// ��������������//41.017685 1.2402349 8.9746275 1.2300695 0.24200015,//0.10526316
										// 38.37435 0.8800591 6.7561803 3.3607302 0.5300002 //0.10526316
										// TODO failParams�ɱ�failIndex.size()�滻
										if(failIndex.size()!=failParams)
											System.out.println("There is something wrong");

										for (int kj = 0; kj < failParams; kj++) {
											manager.dispatch(taskList.get(failIndex.get(kj)));//dispatch task
										}
										// TODO �������ݵ����
										simResults = new double[simRstLength][failIndex.size()];
										for (int kj = 0; kj < failParams; kj++) {
											double[] tmpResults = taskList.get(failIndex.get(kj)).getOutputs();
											fvals[failIndex.get(kj)] = (float)tmpResults[0];//fetch result
											randSeed[failIndex.get(kj)] = tmpResults[1];
											for(int kk=0;kk <tmpResults.length-2;kk++){
												simResults[kk][kj] = tmpResults[kk+2];
											}
										}
										// ȡ�����з������������csv
										//CSVUtils.writeCSV("R://SimResults"+testCounter2+".csv", null, simResults);
										CSVUtils.writeCSV("R://SimResults.csv", null, simResults);
										//System.out.println("���ڶ�ȡ"+"R://SimResults"+testCounter2+".csv");
										//testCounter2++;
										// ����python��ʼ��ƽ���Լ���
										output.print("calcFitness");
										output.flush();

									}
									else{
										// �Ѵ������з�ƽ�����л�����Ѵﵽ��ֵ
										// ���ü���
										reRunCounter = 0;
										// DE����
										for (int j = 0; j < de.getPopulation(); j++) {
											if(failIndex.size()!=0 && failIndex.contains(j))
												de.evoluteIndividual(j, Double.MAX_VALUE);
											else
												de.evoluteIndividual(j, fvals[j]);
										}
										bestRandSeed = randSeed[de.getBestIdvdIndex()];
										failIndex.clear();
										taskList.clear();
										// ����while������һ����������
										break;
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
								if(i== de.getMaxItrGeneration()-1){
									// �������
									output.print("exit");
									output.flush();
								}

							}
							if(line.equals("Connection closed")){
								break;
							}

						} catch (Exception e) {
							System.out.println("�ͻ����쳣:" + e.getMessage());
						}
					}
					System.out.println("Gbest : " + de.getGbestFitness());
					System.out.println("Position : " + de.showGBestPos());
					System.out.println("RandomSeed: " + bestRandSeed);
					System.out.println("Gneration " + i + " used " + ((System.currentTimeMillis() - tb)/1000) + " sec");

				}// DE������е���
				input.close();
				output.close();
				client.close();

			}

		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	public static void main(String[] args) {
		TaskCenter taskCenter = new TaskCenter();
		int pop = 20;
		double[] plower = new double[]{5.7787,0.1,0.1,0.00,0.00};
		double[] pupper = new double[]{65.0787,10,10,10.00,10.00};//,180.0f,25,40,100};
		DERunWithPython exp = new DERunWithPython();
		exp.de.init(pop, plower.length,200,0.7f, 0.5f, plower, pupper);
		new SchedulerThread("ThreadManager", taskCenter) {
			@Override
			public void run() {
				exp.run(this);
				dismissAllWorkingThreads();//stop eng�̡߳�
			}
		}.start();
		for (int i = 0; i < pop; i++) {
			new EngThread("Eng" + i, taskCenter, "src/main/resources/demo_neihuan/scenario2/kscalibration.properties") {
				@Override
				public double[] worksUnder(double[] paras) {
					MLPEngine mlpEngine = (MLPEngine) engine;
						//�������
					mlpEngine.runWithPara(paras);

					//��ȡ�ض����
					List<MacroCharacter> records = mlpEngine.getMlpNetwork().getSecStatRecords("det2");
					double[] simSpeeds = records.stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
					//���۽��
					exp.simRstLength = simSpeeds.length;
					double[] result = new double[exp.simRstLength + 2];
					//fitness
					//Caution: һ�β��
					result[0] = FitnessFunction.evaKSDistance(ADFullerTest.seriesDiff(simSpeeds,1),
							ADFullerTest.seriesDiff(mlpEngine.getEmpData(),1));
					//random seed
					result[1] = mlpEngine.runningSeed;
					System.arraycopy(simSpeeds,0,result,2,exp.simRstLength);
					return result;
				}
			}.start();
		}

	}
	// 0.10526316 1.500553306214E12 //54.831276 0.6536175 7.813163 6.253295 0.7737541
	// 0.10526316 1.500557647197E12 //6.1381183 1.0044184 9.940359 0.7673085 0.26010168
	// 0.10526316 1.50061384266E12	//63.532787 0.7297639 4.5515146 6.1431623 0.86933017
	//                              //62.684875 1.4288251 8.873928 5.5012074 0.5790217

}
