package com.transyslab.commons.tools.mutitask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.tools.adapter.SimProblem;
import com.transyslab.roadnetwork.Parameter;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mesots.MesoEngine;
import com.transyslab.simcore.mlp.MLPEngine;
import org.apache.commons.configuration2.Configuration;

public class EngThread extends Thread implements TaskWorker{
	protected SimProblem problem;
	protected TaskCenter taskCenter;
	protected SimulationEngine engine;
	private boolean logOn;
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
				//TODO dir��Ҫȥ���ļ�����׺
				engine = new MesoEngine(0,null);
				break;
			case "MLP":
				engine = new MLPEngine(masterFileDir);
				break;
			default:
				System.err.println("Unsupported model name");
		}

		logOn = Boolean.parseBoolean(config.getString("positionLogOn"));
	}

	@Override
	public double[] worksWith(double[] paras, Map<Object, Object> attributes) {

		HashMap exportedStatMap = engine.repeatProcess(paras);

		//��¼ͳ�����ݵ�Task�У�attributes��
		if (exportedStatMap!=null)
			exportedStatMap.forEach((k,v) -> attributes.put(k,v));

		//�������
		double[] fitness = problem.evaluate(exportedStatMap, engine.getEmpMap());

		//������log
		if (logOn)
			writer.writeNFlush(Arrays.toString(paras).replace(" ","")
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
		goToWork(taskCenter, false);
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
		this.problem = problem;
		return assignTo(problem.getTaskCenter());
	}

	public Parameter getParameters() {
		return engine.getNetwork().getSimParameter();
	}

}
