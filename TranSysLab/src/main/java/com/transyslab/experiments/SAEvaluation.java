package com.transyslab.experiments;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.tools.ADFullerTest;
import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskWorker;
import com.transyslab.commons.tools.mutitask.SchedulerThread;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.commons.tools.mutitask.EngThread;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MacroCharacter;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by yali on 2017/10/22.
 */
public class SAEvaluation {
	public static void main(String[] args) throws IOException {
		final int NUMOFWORKERS = 20;
		TaskCenter taskCenter = new TaskCenter();
		List<Task>taskList = new ArrayList<>();
		List<CSVRecord> paramList = CSVUtils.readCSV("R://splitFile3.csv",null);
		CSVPrinter printer = CSVUtils.getCSVWriter("R://SAResult3.csv",null,false);
		int row = paramList.size();
		int col = paramList.get(0).size();
		for (int i = 0; i < NUMOFWORKERS; i++) {
			new EngThread("Eng" + i, "src/main/resources/demo_neihuan/scenario2/kscalibration.properties", taskCenter) {
				@Override
				public double[] worksWith(double[] paras, Map<Object, Object> attributes) {
					MLPEngine mlpEngine = (MLPEngine) engine;
					mlpEngine.getSimParameter().setLCDStepSize(0.0);
					mlpEngine.seedFixed = true;
					mlpEngine.runningSeed = (long)paras[10];
					//其他设置(properties中没有的设置)
					mlpEngine.getSimParameter().setDLower((float)paras[9]);//车队判别阈值
					mlpEngine.getSimParameter().setLCBuffTime(paras[8]);//换道影响时间
					// 待优化参数
					double[] optParams = Arrays.copyOfRange(paras,0,8);

					int vhcCount = 0;
					double[] simSpeeds;
					//仿真过程
					if (mlpEngine.runWithPara(optParams) == Constants.STATE_ERROR_QUIT) {
						return new double[]{Integer.MAX_VALUE};
					}
					//获取特定结果
					List<MacroCharacter> records = mlpEngine.getNetwork().getSecStatRecords("det2");
					simSpeeds = records.stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
					vhcCount = mlpEngine.countOnHoldVeh();

					// 剩余发车量、RMSE、车速ks距离
					double[] result = new double[simSpeeds.length+3];

					// TODO 总发车量
					result[0] = vhcCount / 5374.0;
					result[1] = FitnessFunction.evaRNSE(simSpeeds, mlpEngine.getEmpData());
					result[2] = FitnessFunction.evaKSDistance(ADFullerTest.seriesDiff(simSpeeds,1),
							ADFullerTest.seriesDiff(mlpEngine.getEmpData(),1));
					System.arraycopy(simSpeeds,0,result,3,simSpeeds.length);
					return result;
				}
			}.start();
		}
		new SchedulerThread("ThreadManager", taskCenter) {
			@Override
			public void run(){
				for(int i=0; i<row;i++){
					double[] parameters = new double[]{0.4633,21.7950,0.1765, 120.0/3.6,0.0, 0.0, 0.0, 0.0,
					0.0,0.0,0.0};//[Qm, Vfree, Kjam, VPhyLim]
					for (int j = 0; j < col; j++) {
						parameters[4+j] = Double.parseDouble(paramList.get(i).get(j));
					}
					taskList.add(dispatch(parameters, TaskWorker.ANY_WORKER));
					if((i+1)%((double)NUMOFWORKERS) == 0){
						for (int k = 0; k < NUMOFWORKERS; k++) {
							double[] tmpResults = taskList.get(k).getObjectiveValues();
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
