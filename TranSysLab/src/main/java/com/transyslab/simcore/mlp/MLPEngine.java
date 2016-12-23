package com.transyslab.simcore.mlp;

import com.transyslab.commons.tools.DE;
import com.transyslab.commons.tools.PSO;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.simcore.SimulationEngine;

public class MLPEngine extends SimulationEngine{
	
	protected int runTimes_; // �������д���
	protected float frequency_; // 1/step size
	protected double updateTime_;
	protected int iteration_; // id of the iteration
	protected int firstEntry = 1; // simulationLoop�е�һ��ѭ���ı��
	private int parseODID_;
	private double updateDetTime_;
	private double detStepSize_;

	@Override
	public int simulationLoop() {
		// TODO �Զ����ɵķ������
		return 0;
	}

	@Override
	public void loadFiles() {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public void start() {
		// TODO �Զ����ɵķ������
		
	}
	
	public int loadSimulationFiles(){
		
		//load xml
		//parse xml into parameter & network
		
		init();		
		start();
		parseODID_ = 1;
		return 0;
	}
	
	public void init() {//Engine����Ҫ��ʼ��������
		
		double now = SimulationClock.getInstance().getCurrentTime();
		updateTime_ = now;
		updateDetTime_ = now + detStepSize_;
		frequency_ = (float) (1.0 / SimulationClock.getInstance().getStepSize());
	}

}
