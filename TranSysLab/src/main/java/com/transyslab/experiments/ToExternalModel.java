package com.transyslab.experiments;

import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.adapter.SimProblem;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.commons.tools.mutitask.*;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPParameter;
import com.transyslab.simcore.mlp.MacroCharacter;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * Created by yali on 2017/12/7.
 */
public class ToExternalModel implements TaskGiver{
	private List<Task> taskList;
	private TaskCenter taskCenter;
	private String masterFileDir;
	public ToExternalModel(String masterFileDir){
		this.masterFileDir = masterFileDir;
		taskList = new ArrayList<>();
		taskCenter = new TaskCenter();
	}
	public void startSimEngines(){
		Configuration config = ConfigUtils.createConfig(masterFileDir);
		//parsing
		int numOfEngines = Integer.parseInt(config.getString("numOfEngines"));
		String obParaStr = config.getString("obParas");
		String[] parasStrArray = obParaStr.split(",");
		double[] ob_paras = new double[parasStrArray.length];
		for (int i = 0; i<parasStrArray.length; i++) {
			ob_paras[i] = Double.parseDouble(parasStrArray[i]);
		}
		for (int i = 0; i < numOfEngines; i++) {
			//标准引擎的初始化与参数的设置
			EngThread engThread = this.createEngThread("eng" + i, masterFileDir);
			engThread.assignTo(taskCenter);
			engThread.start();
		}
		System.out.println( "Success to start "+numOfEngines + " simulation engines.");
	}
	public void closeSimEngines(){
		dismissAllWorkingThreads();
	}
	public void dispatchTask(double[] params){
		if(params.length!=5){
			System.out.println("Check your length of params");
			return;
		}
		taskList.add(dispatch(params,TaskWorker.ANY_WORKER));
	}
	public double[] getTaskResult(int taskId, String resultName){
		return (double[]) taskList.get(taskId).getAttribute(resultName);
	}
	public void clearTaskList(){
		taskList.clear();
	}
	@Override
	public TaskCenter getTaskCenter() {
		return this.taskCenter;
	}
	private EngThread createEngThread(String name, String masterFileDir) {
		return new EngThread(name,masterFileDir){
			public double[] worksWith(Task task) {
				MLPEngine engine = (MLPEngine) getEngine();
				double[] var  = task.getInputVariables();
				engine.alterEngineFreeParas(Arrays.copyOfRange(var,0,4));
				engine.getSimParameter().setLCDStepSize(2.0);
				engine.getSimParameter().setLCBuffTime(var[4]);
				// 运行仿真
				engine.repeatRun();
				Map<String, List<MacroCharacter>> simMap = engine.getSimMap();
				if(simMap !=null){
					List<MacroCharacter> simRecords = simMap.get("det2");
					if(simRecords ==null || simRecords.isEmpty()){
						System.out.println("Error: Can not find \"det2\"");
						return new double[]{Double.POSITIVE_INFINITY};
					}
					double[] simSpeed = MacroCharacter.select(simRecords, MacroCharacter.SELECT_SPEED);
					double[] simFlow = MacroCharacter.select(simRecords,MacroCharacter.SELECT_FLOW);
					task.setAttribute("SimSpeed",simSpeed);
					task.setAttribute("SimFlow",simFlow);
					task.setAttribute("Seed",new double[]{engine.runningSeed});

				}
				return new double[]{Double.POSITIVE_INFINITY};
			}
		};
	}



}
