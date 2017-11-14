package com.transyslab.commons.tools.mutitask;

import java.util.Arrays;
import java.util.HashMap;

import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.tools.adapter.SimProblem;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mesots.MesoEngine;
import com.transyslab.simcore.mlp.MLPEngine;
import org.apache.commons.configuration2.Configuration;

public class EngThread extends Thread implements TaskWorker{
	private TaskCenter taskCenter;
	private SimulationEngine engine;
	private boolean logOn;
	private boolean taskSpecified;
	private SimulationConductor conductor;
	//TODO: 修改输出路径
	private static TXTUtils writer = new TXTUtils("src/main/resources/output/solutions.csv");

	public EngThread(String thread_name, String masterFileDir, TaskCenter taskCenter) {
		this(thread_name,masterFileDir);
		this.taskCenter = taskCenter;
	}

	public EngThread(String thread_name, String masterFileDir) {
		setName(thread_name);

		Configuration config = ConfigUtils.createConfig(masterFileDir);

		String modelType = config.getString("modelType");
		switch (modelType) {
			case "MesoTS":
				//TODO dir需要去掉文件名后缀
				engine = new MesoEngine(0,null);
				break;
			case "MLP":
				engine = new MLPEngine(masterFileDir);
				break;
			default:
				System.err.println("Unsupported model name");
		}

		logOn = Boolean.parseBoolean(config.getString("positionLogOn"));
		taskSpecified = false;//默认为false
	}

	@Override
	public double[] worksWith(Task task) {

		if (conductor == null)
			System.err.println("Engine behavior not been determined");

		//参数设置
		conductor.alterEngineParameters(engine, task.getInputVariables());

		//仿真过程
		engine.repeatRun();

		if (task.attributes == null)
			task.attributes = new HashMap<>();

		//结果评价
		double[] fitness = conductor.evaluateFitness(engine);

		//输出解的log
		if (logOn)
			writer.writeNFlush(Arrays.toString(task.getInputVariables())
						.replace(" ","")
						.replace("[","")
						.replace("]","") + "," +
					Arrays.toString(fitness).replace(" ","")
						.replace("[","")
						.replace("]","")+ "\r\n");

		return fitness;
	}

	@Override
	public void run() {
		if (taskCenter == null)
			System.err.println("Engine has not been assigned.");
		goToWork(taskCenter, taskSpecified);
	}

	@Override
	public void init() {
		engine.loadFiles();
	}

	@Override
	public void onDismiss() {
		engine.close();
	}

	public EngThread assignTo(TaskCenter tc) {
		this.taskCenter = tc;
		return this;
	}

	public EngThread assignTo(SimProblem problem) {
		return assignTo(problem.getTaskCenter());
	}

	protected EngThread setTaskSpecified(){
		taskSpecified = true;
		return this;
	}

	public EngThread setSimConductor(SimulationConductor conductor) {
		this.conductor = conductor;
		return this;
	}

	protected SimulationEngine getEngine() {
		return engine;
	}

}
