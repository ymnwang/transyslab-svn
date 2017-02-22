package com.transyslab.simcore.mlp;

import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.commons.tools.emitTable;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.SimulationEngine;


public class MLPEngine extends SimulationEngine{
	
	//protected int runTimes_; // �������д���
	protected float frequency_; // 1/step size
	protected double updateTime_;
	protected double LCDTime_;
	protected boolean firstEntry = true; // simulationLoop�е�һ��ѭ���ı��
	protected boolean needRndETable = true; //needRndETable==true,������ɷ�����needRndETable==false�����ļ����뷢����
	protected TXTUtils loopRecWriter;
	protected boolean loopRecOn = false;
	protected TXTUtils trackWriter;
	protected boolean trackOn = false;
	protected TXTUtils infoWriter;
	protected boolean infoOn = false;
	protected String msg = "";

	@Override
	public int simulationLoop() {
		final double epsilon = 1.0E-3;
		// ʵ����·������һ���̶߳�Ӧһ��ʵ��
		MLPNetwork mlp_network = MLPNetwork.getInstance();

		double now = SimulationClock.getInstance().getCurrentTime();
		double startTime = SimulationClock.getInstance().getStartTime();
		String time = String.format("%.1f", now - startTime);

		if (firstEntry) {
			firstEntry = false;

			// This block is called only once just before the simulation gets
			// started.
			
			//establish writers
			int threadID = MLPNetworkPool.getInstance().getHashMap().
									get(Thread.currentThread().getName()).intValue();
			if (loopRecOn) {
				loopRecWriter = new TXTUtils("src/main/resources/output/loop" + threadID + ".csv");
				loopRecWriter.write("TIME,VID,VIRTYPE,SPD,POS,LINK,LOCATION\r\n");
			}				
			if (trackOn) {
				trackWriter = new TXTUtils("src/main/resources/output/track" + threadID + ".csv");
				trackWriter.write("TIME,VID,VIRTYPE,BUFF,POS,LINK,DSP,SPD\r\n");
			}				
			if (infoOn)
				infoWriter = new TXTUtils("src/main/resources/output/info" + threadID + ".txt");
			
			
			//set overall parameters
			mlp_network.setOverallCapacity(0.5);
			mlp_network.setOverallSDParas(new double [] {16.67,0.0,180.0,5.0,1.8});
			mlp_network.setLoopSection(mlp_network.getLink(0).getCode(), 0.5);
			
			//reset update time
			mlp_network.resetReleaseTime();
			
			//��Ϣͳ�ƣ�������
			if (infoOn) {
				int total = 0;
				for (int i = 0; i < mlp_network.nLinks(); i++) {
					total += mlp_network.mlpLink(i).emtTable.getInflow().size();				
				}
				infoWriter.writeNFlush("���������ʵ���� " + total + "\r\n");	
			}		
		}
		
		if (now>= updateTime_){
			mlp_network.resetReleaseTime();
			if (loopRecOn) 
				loopRecWriter.flushBuffer();
			if (trackOn)
				trackWriter.flushBuffer();
			if (infoOn)
				infoWriter.flushBuffer();
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
			for (int i = 0; i < mlp_network.nLanes(); i++) {
				MLPLane theLN = mlp_network.mlpLane(i);
				MLPVehicle claimTail = theLN.getTail();
				MLPVehicle claimHead = theLN.getHead();
				if ((claimTail != null && !theLN.vehsOnLn.contains(claimTail)) ||
						(claimHead != null && !theLN.vehsOnLn.contains(claimHead)) ||
						((claimTail == null || claimHead == null) && !theLN.vehsOnLn.isEmpty()) ) {
					System.out.println("BUG");
				}
			}
			//�˶����㡢�ڵ����(��ȱ)��д�뷢������ȱ��
			for (int i = 0; i < mlp_network.nLinks(); i++){
				mlp_network.mlpLink(i).move();
			}
			//��Ȧ���
			if (loopRecOn) {
				for (int j = 0; j < mlp_network.loops.size(); j++){
					msg = mlp_network.loops.get(j).detect();
					if (msg != "") {
						loopRecWriter.write(time + "," + msg);
					}
				}
			}
			
			//��������(ͬʱ����)
			for (int k = 0; k<mlp_network.veh_list.size(); k++) {
				MLPVehicle theVeh = mlp_network.veh_list.get(k);
				if (theVeh.updateMove()==1) 
					k -=1;
			}			
		}
		
		//���ӻ���Ⱦ
//		mlp_network.recordVehicleData();
		
		for (int i = 0; i < mlp_network.nLanes(); i++) {
			MLPLane theLN = mlp_network.mlpLane(i);
			MLPVehicle claimTail = theLN.getTail();
			MLPVehicle claimHead = theLN.getHead();
			if ((claimTail != null && !theLN.vehsOnLn.contains(claimTail)) ||
					(claimHead != null && !theLN.vehsOnLn.contains(claimHead)) ||
					((claimTail == null || claimHead == null) && !theLN.vehsOnLn.isEmpty()) ) {
				System.out.println("BUG");
			}
		}
		if (trackOn) {
			if (!mlp_network.veh_list.isEmpty()) {
				for (MLPVehicle v : mlp_network.veh_list) {
					trackWriter.write(time + "," + 
							 				  v.getCode() + "," +
							 				  v.VirtualType_ + "," +
							 				  v.buffer_ + "," +
							 				  v.lane_.getLnPosNum() + "," +
							 				  v.segment_.getCode() + "," +
							 				  v.Displacement() + "," + 
							 				  v.currentSpeed() + "\r\n");
				}
			}
		}
		System.out.println(time);
		SimulationClock.getInstance().advance(SimulationClock.getInstance().getStepSize());
		if (now > SimulationClock.getInstance().getStopTime() + epsilon) {			
			if (infoOn)
				infoWriter.writeNFlush("��������ʵ�������⳵��" + (mlp_network.getNewVehID()-1)+"\r\n");			
			if (loopRecOn) 
				loopRecWriter.closeWriter();
			if (trackOn)
				trackWriter.closeWriter();
			if (infoOn)
				infoWriter.closeWriter();			
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
		MLPNetwork.getInstance().buildemittable(needRndETable);	
		start();
		return 0;
	}
	
	public void init() {//Engine����Ҫ��ʼ��������
		SimulationClock.getInstance().init(0, 3600, 0.2);
		
		double now = SimulationClock.getInstance().getCurrentTime();
		updateTime_ = now;
		LCDTime_ = now;
		frequency_ = (float) (1.0 / SimulationClock.getInstance().getStepSize());
	}
	
	public void initSnapshotData(){
		
	}

}
