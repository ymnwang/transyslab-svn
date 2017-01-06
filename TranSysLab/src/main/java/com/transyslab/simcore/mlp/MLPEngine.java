package com.transyslab.simcore.mlp;

import java.util.List;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mesots.MesoCellList;
import com.transyslab.simcore.mesots.MesoIncident;
import com.transyslab.simcore.mesots.MesoNetwork;
import com.transyslab.simcore.mesots.MesoParameter;
import com.transyslab.simcore.mesots.MesoSegment;
import com.transyslab.simcore.mesots.MesoVehicle;


public class MLPEngine extends SimulationEngine{
	
	protected int runTimes_; // �������д���
	protected float frequency_; // 1/step size
	protected double updateTime_;
	protected int iteration_; // id of the iteration
	protected int firstEntry = 1; // simulationLoop�е�һ��ѭ���ı��
	private int parseODID_;
	private double updateDetTime_;
	private double detStepSize_;
	private List<MLPVehicle> snapshotList_;//��ʼ֡�����������б�

	@Override
	public int simulationLoop() {
		// ʵ����·������һ���̶߳�Ӧһ��ʵ��
		MLPNetwork mlp_network = MLPNetwork.getInstance();

		double now = SimulationClock.getInstance().getCurrentTime();

		if (firstEntry != 0) {
			firstEntry = 0;

			// This block is called only once just before the simulation gets
			// started.
			//mlp_network.resetSegmentEmitTime();
			if(mode_==2||mode_==3){
				//��ʼ��·�����г�����������Ϣ
				initSnapshotData();
			}

		}
		
		return 0;
	}

	@Override
	public void loadFiles() {
		loadSimulationFiles();
		
	}

	@Override
	public void start() {
		// TODO �Զ����ɵķ������
		
	}
	
	
	public int loadSimulationFiles(){
		
		//load xml
		//parse xml into parameter & network
		
		//����ĳ�ʼ��
		init();

		// ��ȡ·��xml
		MLPSetup.ParseNetwork();
		// ����·�����ݺ���֯·����ͬҪ�صĹ�ϵ
		MLPNetwork.getInstance().calcStaticInfo();
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
	
	public void initSnapshotData(){
		int vhcnum = snapshotList_.size();
		//����������ͳ��
		MLPVehicle.setVehicleCounter(vhcnum);;
		MLPSegment seg = (MLPSegment) MLPNetwork.getInstance().getSegment(0);
		/*seg.append(MesoCellList.getInstance().recycle());
		seg.getLastCell().initialize();
		for(int i=0;i<vhcnum-1;i++){
			if(snapshotList_.get(i+1).distance()-snapshotList_.get(i).distance()<MesoParameter.getInstance().cellSplitGap()){
				seg.getLastCell().appendSnapshot(snapshotList_.get(i));
				if(i==vhcnum-2){
					//���һ����
					seg.getLastCell().appendSnapshot(snapshotList_.get(i+1));
				}
			}
			else{
				seg.append(MesoCellList.getInstance().recycle());
				seg.getLastCell().initialize();
				seg.getLastCell().appendSnapshot(snapshotList_.get(i+1));
			}
		}*/
	}

}
