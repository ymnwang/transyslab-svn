package com.transyslab.commons.tools.optimizer;

import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.mutitask.SchedulerThread;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskWorker;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.commons.tools.mutitask.EngThread;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPParameter;
import com.transyslab.simcore.mlp.MacroCharacter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yali on 2017/9/11.
 */

public class DERepeatedSim {

	protected List<Task>taskList;
	protected DEAlgorithm de;
	protected double[] bestSpeed;
	public DERepeatedSim() {
		this.de = new DEAlgorithm();
		this.taskList = new ArrayList<>();
	}

	public static void main(String[] args) {
		TaskCenter taskCenter = new TaskCenter();
		int pop = 20;
		int repeatedTimes = 5;
		double kjam = 0.1765, qmax = 0.4633, vfree = 21.7950,deltat = 0.2, vp = 33.333;//TODO:从文件读入。
		double xcLower = MLPParameter.xcLower(kjam, qmax,deltat);
		//TODO: rLower应该随着xc实际取值动态变化。
		double rlower = MLPParameter.rLowerFunc.findRoot(10,new double[]{qmax, vfree, kjam, vp, deltat, 200.0});
		double[] plower = new double[]{xcLower+0.00001,rlower,0.0,0.0};
		double[] pupper = new double[]{200.0, 100.0, 10.0, 10.0};//,180.0f,25,40,100};
		//Gbest : 0.10734763
		//Position : 15.475985 0.15889278 1.546905 6.5494165 29.030441 91.544785
		//Gbest : 0.10625457
		//Position : 15.993167 0.15445936 1.5821557 6.34795 33.02263 93.043655
		DERepeatedSim exp = new DERepeatedSim();
		exp.de.init(pop, plower.length,200,0.7f, 0.5f, plower, pupper);
		new SchedulerThread("ThreadManager", taskCenter) {
			@Override
			public void run() {
				exp.run(this);
				dismissAllWorkingThreads();//stop eng线程。
			}

		}.start();
		for (int i = 0; i < pop; i++) {
			new EngThread("Eng" + i, "src/main/resources/demo_neihuan/scenario2/kscalibration.properties", taskCenter) {
				@Override
				public double[] worksWith(Task task) {
					MLPEngine mlpEngine = (MLPEngine) getEngine();
					mlpEngine.getSimParameter().setLCDStepSize(2.0);
					double[][] simSpeeds = new double[repeatedTimes ][];
					for(int i = 0;i<repeatedTimes;i++){
						//仿真过程
						if(mlpEngine.runWithPara(task.getInputVariables()) == Constants.STATE_ERROR_QUIT){
							return new double[]{Double.MAX_VALUE};
						}
						//获取特定结果
						List<MacroCharacter> records = mlpEngine.getNetwork().getSecStatRecords("det2");
						simSpeeds[i] = records.stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
					}
					//评价结果
					int col = simSpeeds[0].length;
					double[] result = new double[col + 1];
					//统计多次仿真的平均车速
					double[] avgSpeedRst  = new double[col];
					for(int j=0;j<col;j++){
						double sum = 0;
						for(int i=0;i<repeatedTimes;i++){
							sum += simSpeeds[i][j];
						}
						avgSpeedRst[j] = sum/repeatedTimes;
					}
					result[0] = FitnessFunction.evaRNSE(avgSpeedRst,mlpEngine.getEmpData());
					System.arraycopy(avgSpeedRst,0,result,1,col);
					return result;
				}
			}.start();
		}
	}
	public void run(SchedulerThread manager){

		for (int i = 0; i < de.getMaxItrGeneration(); i++) {
			long tb = System.currentTimeMillis();
			for (int j = 0; j < de.getPopulation(); j++) {
				double[] parameters = new double[]{0.4633,21.7950,0.1765, 120.0/3.6,0.0, 0.0, 0.0, 0.0};//[Qm, Vfree, Kjam, VPhyLim]
				System.arraycopy(de.getNewPosition(j),0,parameters,4,de.getDim());
				taskList.add(manager.dispatch(parameters, TaskWorker.ANY_WORKER));
			}
			for (int j = 0; j < de.getPopulation(); j++) {
				double[] tmpResults = taskList.get(j).getObjectiveValues();
				if(de.evoluteIndividual(j,tmpResults[0])){
					if(bestSpeed == null)
						bestSpeed = new double[tmpResults.length - 1];
					System.arraycopy(tmpResults,1,bestSpeed,0,bestSpeed.length);
				}
			}
			System.out.println("Gbest : " + de.getGbestFitness());
			System.out.println("Position : " + de.showGBestPos());
			System.out.println("Gneration " + i + " used " + ((System.currentTimeMillis() - tb)/1000) + " sec");
			taskList.clear();
		}
		System.out.println(Arrays.toString(bestSpeed));
	}

}

