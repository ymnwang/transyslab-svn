package com.transyslab.simcore.mlp;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.commons.tools.emitTable;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.Vehicle;
import com.transyslab.simcore.SimulationEngine;


public class MLPEngine extends SimulationEngine{
	
	//protected int runTimes_; // �������д���
	protected float frequency_; // 1/step size
	protected double updateTime_;
	protected double LCDTime_;
	protected boolean firstEntry = true; // simulationLoop�е�һ��ѭ���ı��
	protected boolean needRndETable = true; //needRndETable==true,������ɷ�����needRndETable==false�����ļ����뷢����

	@Override
	public int simulationLoop() {
		final double epsilon = 1.0E-3;
		// ʵ����·������һ���̶߳�Ӧһ��ʵ��
		MLPNetwork mlp_network = MLPNetwork.getInstance();

		double now = SimulationClock.getInstance().getCurrentTime();

		if (firstEntry) {
			firstEntry = false;

			// This block is called only once just before the simulation gets
			// started.
			
			//set overall parameters
			mlp_network.setOverallCapacity(0.5);
			mlp_network.setOverallSDParas(new double [] {16.67,0.0,180.0,5.0,1.8});
			//reset update time
			mlp_network.resetReleaseTime();
			//load incidence
			/*empty for now*/
			//load snapshot Start
			/*to be done*/
			//initSnapshotData();
		}
		
		if (now>= updateTime_){
			mlp_network.resetReleaseTime();
			updateTime_ = now + MLPParameter.getInstance().updateStepSize_;
		}
		
		//���뷢����
		mlp_network.loadEmtTable();
		
		//·�����ڳ���������²Ž��м���
		if (mlp_network.veh_list.size()>0) {
			//����ʶ��
			mlp_network.platoonRecognize();
			
			//�����뻻������ʱ�̣����л��������복������ʶ��
			if (now >= LCDTime_) {
				for (int i = 0; i < mlp_network.nLinks(); i++){
					mlp_network.mlpLink(i).lanechange();
				}
				mlp_network.platoonRecognize();
				
				LCDTime_ = now + MLPParameter.getInstance().LCDStepSize_;
			}			
			//�˶����㡢�ڵ����(��ȱ)��д�뷢������ȱ��
			for (int i = 0; i < mlp_network.nLinks(); i++){
				mlp_network.mlpLink(i).move();
			}
			
			//��������(ͬʱ����)
			for (int k = 0; k<mlp_network.veh_list.size(); k++) {
				MLPVehicle theVeh = mlp_network.veh_list.get(k);
				if (theVeh.updateMove()==1) 
					k -=1;
			}
			
		}
		
		//System.out.println("t = " + String.format("%.1f", now-SimulationClock.getInstance().getStartTime()));
		/*if (!mlp_network.veh_list.isEmpty()) {
			for (MLPVehicle v : mlp_network.veh_list) {
				System.out.println("VID" + v.getCode() + " dis: " + v.distance() + " spd: " + v.currentSpeed());
			}
		}*/		
		SimulationClock.getInstance().advance(SimulationClock.getInstance().getStepSize());
		if (now > SimulationClock.getInstance().getStopTime() + epsilon) {
			return (state_ = Constants.STATE_DONE);// STATE_DONE�궨�� simulation
													// is done
		}
		else
			return state_ = Constants.STATE_OK;// STATE_OK�궨��
	}

	@Override
	public void loadFiles() {
		//Engine�����ĳ�ʼ��
		init();
		//��������ļ�
		loadSimulationFiles();
		//������ʼ�����̡�ĿǰΪ��
		start();
	}

	@Override
	public void start() {
		// TODO �Զ����ɵķ������
		
	}
	
	public int loadSimulationFiles(){
		
		//load xml
		//parse xml into parameter & network

		// ��ȡ·��xml
		MLPSetup.ParseNetwork();
		// ����·�����ݺ���֯·����ͬҪ�صĹ�ϵ
		MLPNetwork.getInstance().calcStaticInfo();
		//������ɻ��ȡ������
		buildemittable(needRndETable);	
		start();
		return 0;
	}
	
	public void init() {//Engine����Ҫ��ʼ��������
		SimulationClock.getInstance().init(61200,68700, 0.2);
		
		double now = SimulationClock.getInstance().getCurrentTime();
		updateTime_ = now;
		LCDTime_ = now;
		frequency_ = (float) (1.0 / SimulationClock.getInstance().getStepSize());
	}
	
	public void initSnapshotData(){
		
	}
	
	public void buildemittable(boolean needRET){
		if (needRET){
			emitTable.createRndETables();
		}
		else{
			emitTable.readETables();
		}
	}
	
	

}
