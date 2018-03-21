package com.transyslab.experiments;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.tools.ADFullerTest;
import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskWorker;
import com.transyslab.commons.tools.mutitask.SchedulerThread;
import com.transyslab.commons.tools.mutitask.EngThread;
import com.transyslab.simcore.mlp.*;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.*;

/**
 * Created by yali on 2017/10/22.
 */
public class SAEvaluation {
	public static void main(String[] args) throws IOException {
		ExpSwitch.APPROACH_CTRL = true;
		ExpSwitch.MAX_ACC_CTRL = true;
		// 读取参数
		CSVPrinter printer = CSVUtils.getCSVWriter("src/main/resources/SAResult3.csv",null,false);
		// 运行结果
		List<CSVRecord> inputRecords = CSVUtils.readCSV("src/main/resources/sarun3.csv",null);
		double[][] paramSets = new double[inputRecords.size()][inputRecords.get(0).size()];
		for(int i=0;i<paramSets.length;i++){
			for(int j=0;j<paramSets[0].length;j++){
				paramSets[i][j] = Double.parseDouble(inputRecords.get(i).get(j));
			}
		}
		// 初始化引擎
		ToExternalModel runner = new ToExternalModel("src/main/resources/optmksidvd.properties") {
			// 重写engine计算内容
			public EngThread createEngThread(String name, String masterFileDir) {
				return new EngThread(name, masterFileDir) {

					public double[] worksWith(Task task) {

						MLPEngine engine = (MLPEngine) getEngine();
						double[] var = task.getInputVariables();

						engine.alterEngineFreeParas(Arrays.copyOfRange(var, 0, 4));
						engine.getSimParameter().setLCDStepSize(0.0);
						engine.getSimParameter().setLCBuffTime(var[4]);
						engine.getSimParameter().setLCSensitivity(var[5]);
						engine.seedFixed = true;
						engine.runningSeed = (long)var[6];
						// 运行仿真
						engine.repeatRun();
						// 计算分布差异
						MLPNetwork mlpNetwork = engine.getNetwork();
						LinkedList<double[]> simIdvdMap = new LinkedList<>();
						for (int j = 0; j < mlpNetwork.nSensors(); j++){
							MLPLoop tmpLoop = (MLPLoop) mlpNetwork.getSensor(j);
							if (tmpLoop.getName().equals("det2")) {
								//遍历车道
								for(double[] data:tmpLoop.getRecords()){
									simIdvdMap.add(data);
								}
							}
						}
						Map<String, List<MicroCharacter>> empMicroMap = engine.getEmpMicroMap();
						List<Double> resultList = new ArrayList<>();

						if (simIdvdMap != null && empMicroMap != null ){

							List<MicroCharacter> empRecords = empMicroMap.get("det2");
							if(empRecords == null || empRecords.isEmpty()) {
								System.out.println("Error: Can not find \"det2\"");
								return new double[]{Double.POSITIVE_INFINITY};
							}
							//List<MicroCharacter> simRecords = simMap.get("det2");
							if (!simIdvdMap.isEmpty() ) {
								// 车辆数
								double sumEmpFlow = empRecords.size();

								// 10min分布
								int horizon = 10 * 60;
								// TODO 修改horizon
								int numOfDistr = 12;

								double ksDists1[] = new double[numOfDistr];
								double oriKSDist[] = new double[numOfDistr];
								for(int i=0;i<numOfDistr;i++){
									// ?°15min?€??
									// ????15min·???
									final int periodId = i + 1;
									double[] tmpSimSpeed = simIdvdMap.stream().filter(l -> l[0] >= periodId * horizon && l[0] <= (periodId + 1) * horizon).mapToDouble(e -> e[1]).toArray();
									double[] tmpEmpSpeed = empRecords.stream().filter(l -> l.getDetTime() >= periodId * horizon && l.getDetTime() <= (periodId + 1) * horizon).mapToDouble(e -> e.getSpeed()).toArray();
									double nVhc = tmpEmpSpeed.length;
									oriKSDist[i] = FitnessFunction.evaKSDistance(tmpSimSpeed, tmpEmpSpeed);
									ksDists1[i] = nVhc / sumEmpFlow * oriKSDist[i];

								}
								double avgKSDist = Arrays.stream(ksDists1).sum();
								double avgKSDist2 = Arrays.stream(oriKSDist).average().getAsDouble();
								// W-KS
								resultList.add(avgKSDist);
								// Avg-KS
								resultList.add(avgKSDist2);

								MLPLink tmpLink = engine.getNetwork().findLink(111);
								double vhcPropotion = tmpLink.countHoldingInflow()/(double)(tmpLink.getEmitNum()+tmpLink.countHoldingInflow());
								// ·???????
								resultList.add(vhcPropotion);

								/*
								 * RMSE
								 * */

								Map<String, List<MacroCharacter>> simMap = engine.getSimMap();
								Map<String, List<MacroCharacter>> empMap = engine.getEmpMap();
								if (simMap != null && empMap != null ) {
									List<MacroCharacter> empRecords2 = empMap.get("det2");
									if (empRecords2 == null || empRecords.isEmpty()) {
										System.out.println("Error: Can not find \"det2\"");
										return new double[]{Double.POSITIVE_INFINITY};
									}
									List<MacroCharacter> simRecords = simMap.get("det2");
									if (simRecords != null && !simRecords.isEmpty()) {

										double[] simSpeed = MacroCharacter.select(simRecords, MacroCharacter.SELECT_SPEED);
										double[] empSpeed = MacroCharacter.select(empRecords2, MacroCharacter.SELECT_SPEED);

										resultList.add(FitnessFunction.evaRMSE(simSpeed, empSpeed));
									}
								}
								double[] results = resultList.stream().mapToDouble(Double::doubleValue).toArray();
								String sr = Arrays.toString(results);
								String[] singleResults = sr.substring(1, sr.length() - 1).split(",");
								task.setAttribute("RandomResults",singleResults);
							}

						}
						return new double[]{1};
					}
				};
			}
		};
		// ??????????
		runner.startSimEngines();
		// ?ù??????????????·???
		int numOfParamSets = paramSets.length;
		for(int i = 0;i<numOfParamSets;i++){
			runner.dispatchTask(paramSets[i],i%20);
			if((i+1)%20 == 0){
				for(int j=0;j<20;j++) {
					String[] result2Write = (String[])runner.getTaskAttribution(j,"RandomResults");
					printer.printRecord(result2Write);
				}
				runner.clearTaskList();
				printer.flush();
			}
		}
		printer.close();
		runner.closeSimEngines();
	}
}
