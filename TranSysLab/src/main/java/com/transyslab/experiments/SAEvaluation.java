package com.transyslab.experiments;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.tools.ADFullerTest;
import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskWorker;
import com.transyslab.commons.tools.mutitask.SchedulerThread;
import com.transyslab.commons.tools.mutitask.EngThread;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MacroCharacter;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yali on 2017/10/22.
 */
public class SAEvaluation {
	public static void main(String[] args) throws IOException {
		final int NUMOFWORKERS = 10;
		TaskCenter taskCenter = new TaskCenter();
		List<Task>taskList = new ArrayList<>();
		List<CSVRecord> paramList = CSVUtils.readCSV("R://sample3.csv",null);
		CSVPrinter printer = CSVUtils.getCSVWriter("R://SAResult3.csv",null,false);
		List<CSVRecord> flow = CSVUtils.readCSV("R://detFlow2016-06-20.csv",null);
		double[] detFlow = new double[flow.size()];
		for(int i=0;i<detFlow.length;i++){
			detFlow[i] = Double.parseDouble(flow.get(i).get(0));
		}
		int row = paramList.size();
		int col = paramList.get(0).size();
		for (int i = 0; i < NUMOFWORKERS; i++) {
			new EngThread("Eng" + i, "src/main/resources/demo_neihuan/scenario2/sarun.properties", taskCenter) {
				@Override
				public double[] worksWith(Task task) {
					MLPEngine mlpEngine = (MLPEngine) getEngine();
					mlpEngine.getSimParameter().setLCDStepSize(2.0);
					mlpEngine.seedFixed = true;
					mlpEngine.runningSeed = (long)task.getInputVariableValue(5);
					//其他设置(properties中没有的设置)
					mlpEngine.getSimParameter().setLCBuffTime(task.getInputVariableValue(4));//换道影响时间
					int vhcCount = 0;
					//仿真过程
					mlpEngine.repeatRun();
					//获取特定结果
					List<MacroCharacter> simRecords = mlpEngine.getNetwork().getSecStatRecords("det2");
					List<MacroCharacter> detRecords =  mlpEngine.getEmpMap().get("det2");

					double[] simSpeeds = simRecords.stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
					double[] simFlows = simRecords.stream().mapToDouble(r -> r.getHourFlow()/12.0*3).toArray();
					double[] detSpeeds = detRecords.stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
					double[] detFlows = detRecords.stream().mapToDouble(r -> r.getHourFlow()/12.0*3).toArray();
					task.setAttribute("flow",simFlows);
					task.setAttribute("speed",simSpeeds);
					vhcCount = mlpEngine.countOnHoldVeh();

					// 剩余发车量、车速RMSNE、流量RMSNE、车速ks距离
					double[] result = new double[4];
					// TODO 总发车量
					result[0] = vhcCount / 5557.0;

					result[1] = FitnessFunction.evaRNSE(simSpeeds, mlpEngine.getEmpData());
					result[2] = FitnessFunction.evaRNSE(simFlows,detFlow);
					result[3] = FitnessFunction.evaKSDistance(ADFullerTest.seriesDiff(simSpeeds,1),
							ADFullerTest.seriesDiff(mlpEngine.getEmpData(),1));
					return result;
				}
			}.start();
		}
		new SchedulerThread("ThreadManager", taskCenter) {
			@Override
			public void run(){
				for(int i=0; i<row;i++){
					double[] parameters = new double[6];
					for (int j = 0; j < col; j++) {
						parameters[4+j] = Double.parseDouble(paramList.get(i).get(j));
					}
					taskList.add(dispatch(parameters, TaskWorker.ANY_WORKER));
					if((i+1)%((double)NUMOFWORKERS) == 0){
						for (int k = 0; k < NUMOFWORKERS; k++) {
							double[] tmpResults = taskList.get(k).getObjectiveValues();
							Object test = taskList.get(k).getAttribute("key");
							List<Double> output = new ArrayList<>(tmpResults.length);
							for(int l =0;l<tmpResults.length;l++) {
								output.add(tmpResults[l]);
							}
							try {
								printer.printRecord(output);
								printer.flush();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						taskList.clear();
					}
				}
				dismissAllWorkingThreads();//stop eng线程。
				try {
					printer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();

	}
}
