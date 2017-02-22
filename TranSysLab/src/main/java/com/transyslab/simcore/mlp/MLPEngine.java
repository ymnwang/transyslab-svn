package com.transyslab.simcore.mlp;

import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.commons.tools.emitTable;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.SimulationEngine;


public class MLPEngine extends SimulationEngine{
	
	//protected int runTimes_; // 仿真运行次数
	protected float frequency_; // 1/step size
	protected double updateTime_;
	protected double LCDTime_;
	protected boolean firstEntry = true; // simulationLoop中第一次循环的标记
	protected boolean needRndETable = true; //needRndETable==true,随机生成发车表，needRndETable==false，从文件读入发车表
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
		// 实例化路网对象，一个线程对应一个实例
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
			
			//信息统计：发车数
			if (infoOn) {
				int total = 0;
				for (int i = 0; i < mlp_network.nLinks(); i++) {
					total += mlp_network.mlpLink(i).emtTable.getInflow().size();				
				}
				infoWriter.writeNFlush("随机发出真实车： " + total + "\r\n");	
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
		
		//读入发车表
		mlp_network.loadEmtTable();
		
		//路网存在车辆的情况下才进行计算
		if (mlp_network.veh_list.size()>0) {
			//车队识别
			mlp_network.platoonRecognize();
			
			//若进入换道决策时刻，进行换道计算与车队重新识别
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
			//运动计算、节点服务(暂缺)、写入发车表（暂缺）
			for (int i = 0; i < mlp_network.nLinks(); i++){
				mlp_network.mlpLink(i).move();
			}
			//线圈检测
			if (loopRecOn) {
				for (int j = 0; j < mlp_network.loops.size(); j++){
					msg = mlp_network.loops.get(j).detect();
					if (msg != "") {
						loopRecWriter.write(time + "," + msg);
					}
				}
			}
			
			//车辆更新(同时更新)
			for (int k = 0; k<mlp_network.veh_list.size(); k++) {
				MLPVehicle theVeh = mlp_network.veh_list.get(k);
				if (theVeh.updateMove()==1) 
					k -=1;
			}			
		}
		
		//可视化渲染
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
				infoWriter.writeNFlush("共产生真实车与虚拟车：" + (mlp_network.getNewVehID()-1)+"\r\n");			
			if (loopRecOn) 
				loopRecWriter.closeWriter();
			if (trackOn)
				trackWriter.closeWriter();
			if (infoOn)
				infoWriter.closeWriter();			
			return (state_ = Constants.STATE_DONE);// STATE_DONE宏定义 simulation
													// is done
		}
		else
			return state_ = Constants.STATE_OK;// STATE_OK宏定义
	}

	@Override
	public void loadFiles() {
		//Engine参数的初始化
		init();
		//读入仿真文件
		loadSimulationFiles();
		//其他初始化过程。目前为空
		start();
	}

	@Override
	public void start() {
		// TODO 自动生成的方法存根
		
	}
	
	public int loadSimulationFiles(){
		
		//load xml
		//parse xml into parameter & network

		// 读取路网xml
		MLPSetup.ParseNetwork();
		// 读入路网数据后组织路网不同要素的关系
		MLPNetwork.getInstance().calcStaticInfo();
		//随机生成或读取发车表
		MLPNetwork.getInstance().buildemittable(needRndETable);	
		start();
		return 0;
	}
	
	public void init() {//Engine中需要初始化的属性
		SimulationClock.getInstance().init(0, 3600, 0.2);
		
		double now = SimulationClock.getInstance().getCurrentTime();
		updateTime_ = now;
		LCDTime_ = now;
		frequency_ = (float) (1.0 / SimulationClock.getInstance().getStepSize());
	}
	
	public void initSnapshotData(){
		
	}

}
