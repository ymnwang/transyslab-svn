package com.transyslab.simcore.mlp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.swing.text.StyledEditorKit.ForegroundAction;

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
	protected BufferedWriter writer;
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
			
			//establish writer
			establishTXTWriter();
			
			//set overall parameters
			mlp_network.setOverallCapacity(0.5);
			mlp_network.setOverallSDParas(new double [] {16.67,0.0,180.0,5.0,1.8});
			mlp_network.setLoopSection(mlp_network.getLink(0).getCode(), 0.5);
			//reset update time
			mlp_network.resetReleaseTime();
			//load incidence
			/*empty for now*/
			//load snapshot Start
			/*to be done*/
			//initSnapshotData();
			//����ͳ��
			int total = 0;
			for (int i = 0; i < mlp_network.nLinks(); i++) {
				total += mlp_network.mlpLink(i).emtTable.getInflow().size();				
			}
			writeNFlush("���������ʵ���� " + total + "\r\n");
			
		}
		
		if (now>= updateTime_){
			mlp_network.resetReleaseTime();
			flushBuffer();
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
			//��Ȧ���
			for (int j = 0; j < mlp_network.loops.size(); j++){
				msg = mlp_network.loops.get(j).detect();
				if (msg != "") {
					write(time + "," + msg);
				}
			}
			//��������(ͬʱ����)
			for (int k = 0; k<mlp_network.veh_list.size(); k++) {
				MLPVehicle theVeh = mlp_network.veh_list.get(k);
				if (theVeh.updateMove()==1) 
					k -=1;
			}
			
		}
		
		/*if (!mlp_network.veh_list.isEmpty()) {
			time = String.format("%.1f", now - startTime);
			for (MLPVehicle v : mlp_network.veh_list) {
				write(time + "," + 
						 v.getCode() + "," + 
						 v.Displacement() + "," + 
						 v.currentSpeed() + "\r\n");
			}
		}*/
		
		//System.out.println("t = " + String.format("%.1f", now-SimulationClock.getInstance().getStartTime()));
		/*if (!mlp_network.veh_list.isEmpty()) {
			for (MLPVehicle v : mlp_network.veh_list) {
				System.out.println("VID" + v.getCode() + " dis: " + v.distance() + " spd: " + v.currentSpeed());
			}
		}*/		
		SimulationClock.getInstance().advance(SimulationClock.getInstance().getStepSize());
		if (now > SimulationClock.getInstance().getStopTime() + epsilon) {
			//System.out.println("��������ʵ�������⳵��" + (mlp_network.getNewVehID()-1));
			writeNFlush("��������ʵ�������⳵��" + (mlp_network.getNewVehID()-1)+"\r\n");
			closeWriter();
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
	
	public void establishTXTWriter(){
		//establish csv printer
		int threadID = MLPNetworkPool.getInstance().getHashMap().
									get(Thread.currentThread().getName()).intValue();
		String filepath = "src/main/resources/output/Engine"+ threadID + ".txt";
		File file = new File(filepath);
		try {
			file.createNewFile();
			writer = new BufferedWriter(new FileWriter(file));
		} catch (Exception e) {
			// TODO: handle exception
		}
	}	
	private void writeNFlush(String str) {
		try {
			writer.write(str);
			writer.flush();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	protected void write(String str) {
		try {
			writer.write(str);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	private void flushBuffer() {
		try {
			writer.flush();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	private void closeWriter() {
		try {
			writer.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
