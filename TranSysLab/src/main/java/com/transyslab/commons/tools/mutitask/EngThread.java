package com.transyslab.commons.tools.mutitask;

import java.io.File;
import java.util.Arrays;

import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.tools.adapter.SimProblem;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mesots.MesoEngine;
import com.transyslab.simcore.mlp.MLPEngine;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang.time.StopWatch;

public class EngThread extends Thread implements TaskWorker{
	private TaskCenter taskCenter;
	private SimulationEngine engine;
	private boolean logOn;
	private boolean taskSpecified;
	private SimulationConductor conductor;
	private static TXTUtils writer;
	private boolean broadcastNeeded;

	public EngThread(String thread_name, String masterFileDir, TaskCenter taskCenter) {
		this(thread_name,masterFileDir);
		this.taskCenter = taskCenter;
	}

	public EngThread(String thread_name, String masterFileDir) {
		setName(thread_name);

		Configuration config = ConfigUtils.createConfig(masterFileDir);

		String modelType = config.getString("modelType");
		broadcastNeeded = config.getBoolean("broadcast");
		initEngine(modelType, masterFileDir);

		logOn = Boolean.parseBoolean(config.getString("positionLogOn"));
		if (writer==null && logOn){
			String rootDir = new File(masterFileDir).getParent() + "/";
			String outputPath = rootDir + config.getString("outputPath");
			writer = new TXTUtils(  outputPath + "/" +"solutions.csv");
		}
		taskSpecified = false;//默认为false
	}

	public void initEngine(String modelType, String masterFileDir) {
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
	}

	@Override
	public double[] worksWith(Task task) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.reset();
		stopWatch.start();

		if (conductor == null)
			System.err.println("Engine behavior not been determined");

		//参数设置
		conductor.modifyEngineBeforeStart(engine, (SimSolution) task);

		if (engine instanceof MLPEngine)
			((MLPEngine) engine).fileOutTag = Arrays.toString(task.getInputVariables());

		//仿真过程
		do {
			engine.repeatRun();
		}
		while (!conductor.checkStatusBeforeEvaluate(engine));

		//结果评价
		double[] fitness = conductor.evaluateFitness(engine);

		//修改solution的属性
		conductor.modifySolutionBeforeEnd(engine, (SimSolution) task);

		//输出解的log
		if (logOn) {
			stopWatch.stop();
			if(broadcastNeeded) {
				System.out.println(getName() + "runtimes: " + engine.countRunTimes() + " timer: " + stopWatch.getTime());
				System.out.println("parameter: " + Arrays.toString(task.getInputVariables()) + "fitness: " + Arrays.toString(fitness));
			}
			writer.writeNFlush(Arrays.toString(task.getInputVariables())
					.replace(" ","")
					.replace("[","")
					.replace("]","") + "," +
					Arrays.toString(fitness).replace(" ","")
							.replace("[","")
							.replace("]","") + "," +
					Thread.currentThread().getName() + "_" + engine.countRunTimes() + "_" + Arrays.toString(task.getInputVariables())
					+ "\r\n");
		}

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

	protected void setEngine(SimulationEngine engine) {
		this.engine = engine;
	}

}
