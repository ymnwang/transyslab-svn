package com.transyslab.simcore;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskWorker;
import com.transyslab.roadnetwork.Parameter;
import com.transyslab.roadnetwork.RoadNetwork;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mesots.MesoEngine;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPParameter;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public abstract class EngThread extends Thread implements TaskWorker{
	private TaskCenter taskCenter;
	protected SimulationEngine engine;

	public EngThread(String thread_name, TaskCenter task_center, String masterFileDir) {
		setName(thread_name);
		taskCenter = task_center;
		String modelType = getModelType(masterFileDir);
		switch (modelType) {
			case "MesoTS":
				//TODO 待确认
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
	public void run() {
		engine.loadFiles();
		gotowork(taskCenter);
	}

	public String getModelType(String fileDir) {
		File testFile = new File(fileDir);
		Configurations configs = new Configurations();
		Configuration config = null;
		try {
			config = configs.properties(testFile);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

		return config.getString("modelType");
	}

}
