package com.transyslab.commons.tools;

import java.util.concurrent.TimeUnit;

import com.transyslab.roadnetwork.Parameter;
import com.transyslab.roadnetwork.RoadNetwork;
import com.transyslab.simcore.SimulationEngine;

public abstract class EngTread extends Thread{	
	private TaskCenter taskCenter;
	//仿真相关的成员（原采用NetworkPool管理的类
	public Parameter parameter;
	public RoadNetwork network;
	protected SimulationEngine engine;
	protected SimulationClock sim_clock;

	public EngTread(String thread_name, TaskCenter task_center) {
		setName(thread_name);
		taskCenter = task_center;
	}
	
	protected double[] retrieveTask() {
		try {
			return taskCenter.undoneTasks.poll(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	protected void uploadResult(int idx, double[] fitVal) {
		taskCenter.setResult(idx, fitVal);
//		System.out.println("Task: " + idx + " finished.");
	}
	
	protected double[] unzipTask(double[] task) {
		double[] ans = new double[task.length-1];
		System.arraycopy(task, 1, ans, 0, ans.length);
		return ans;
	}
	
	protected boolean isDismissed() {
		return taskCenter.dismissAllowed();
	}
	
	@Override
	public abstract void run();
}
