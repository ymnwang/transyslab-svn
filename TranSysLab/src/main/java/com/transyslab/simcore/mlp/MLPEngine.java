package com.transyslab.simcore.mlp;

import com.transyslab.commons.tools.DE;
import com.transyslab.commons.tools.PSO;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.simcore.SimulationEngine;

public class MLPEngine extends SimulationEngine{
	
	protected int runTimes_; // 仿真运行次数
	protected float frequency_; // 1/step size
	protected double updateTime_;
	protected int iteration_; // id of the iteration
	protected int firstEntry = 1; // simulationLoop中第一次循环的标记
	private int parseODID_;
	private double updateDetTime_;
	private double detStepSize_;

	@Override
	public int simulationLoop() {
		// TODO 自动生成的方法存根
		return 0;
	}

	@Override
	public void loadFiles() {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void start() {
		// TODO 自动生成的方法存根
		
	}
	
	public int loadSimulationFiles(){
		
		//load xml
		//parse xml into parameter & network
		
		init();		
		start();
		parseODID_ = 1;
		return 0;
	}
	
	public void init() {//Engine中需要初始化的属性
		
		double now = SimulationClock.getInstance().getCurrentTime();
		updateTime_ = now;
		updateDetTime_ = now + detStepSize_;
		frequency_ = (float) (1.0 / SimulationClock.getInstance().getStepSize());
	}

}
