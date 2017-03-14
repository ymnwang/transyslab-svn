package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.apache.commons.math3.analysis.function.Max;

import com.transyslab.commons.renderer.JOGLFrameQueue;
import com.transyslab.commons.tools.Inflow;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Lane;
import com.transyslab.roadnetwork.Link;
import com.transyslab.roadnetwork.RoadNetwork;
import com.transyslab.roadnetwork.Segment;
import com.transyslab.roadnetwork.VehicleData;
import com.transyslab.roadnetwork.VehicleDataPool;

public class MLPNetwork extends RoadNetwork {
	protected int[] permuteLink;
	protected int nPermutedLinks;
	protected int newVehID_;
	public MLPVehPool veh_pool;
	public List<MLPVehicle> veh_list;
	public List<Loop> loops;
	public Random sysRand;

	public MLPNetwork() {
		newVehID_ = 0;
		veh_pool = new MLPVehPool();
		veh_list = new ArrayList<MLPVehicle>();
		loops = new ArrayList<Loop>();
		sysRand = new Random(System.currentTimeMillis());
		//sysRand = new Random((long) 1);
	}

	public static MLPNetwork getInstance() {
		/*HashMap<String, Integer> hm = MLPNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return MLPNetworkPool.getInstance().getNetwork(threadid);*/
		MLPEngThread theThread = (MLPEngThread) Thread.currentThread();
		return (MLPNetwork) theThread.network;
	}
	
/*
		 * public RN_Sensor newSensor() { return new MLP_Sensor(); }/* public
		 * RN_Signal newSignal() { return new MLP_Signal(); } public
		 * RN_TollBooth newTollBooth() { return new MLP_TollBooth(); }
		 */

	public MLPNode mlpNode(int i) {
		return (MLPNode) getNode(i);
	}
	public MLPLink mlpLink(int i) {
		return (MLPLink) getLink(i);
	}
	public MLPSegment mlpSegment(int i) {
		return (MLPSegment) getSegment(i);
	}
	public MLPLane mlpLane(int i){
		return (MLPLane) getLane(i);
	}

	public void calcStaticInfo() {
		superCalcStaticInfo();
		organize();
	}
	public void setsdIndex() {
		MLPSegment ps;
		for (int i = 0; i < nLinks(); i++) {
			if (MLPNetwork.getInstance().getLink(i).getCode() == 64) {
				ps = (MLPSegment) getLink(i).getEndSegment();
				while (ps != null) {
					ps.setSdIndex(0);
					ps = ps.getUpSegment();
				}
			}
			else if (MLPNetwork.getInstance().getLink(i).getCode() == 60) {
				ps = (MLPSegment) getLink(i).getEndSegment();
				while (ps != null) {
					ps.setSdIndex(0);
					ps = ps.getUpSegment();
				}
			}
			else if (MLPNetwork.getInstance().getLink(i).getCode() == 116) {
				ps = (MLPSegment) getLink(i).getEndSegment();
				while (ps != null) {
					ps.setSdIndex(1);
					ps = ps.getUpSegment();
				}
			}

		}
	}

	public void organize() {
		//补充车道编号的信息
		for (Lane l: lanes_){
			((MLPLane) l).calLnPos();
		}
		for (Lane l: lanes_){
			((MLPLane) l).checkConectedLane();
		}
		for (Lane l: lanes_){
			((MLPLane) l).calDi();
		}
		
		for (Segment seg: segments_){
			Segment tmpseg = seg;
			while (tmpseg.getUpSegment() != null) {
				tmpseg = tmpseg.getUpSegment();
				((MLPSegment) seg).startDSP += tmpseg.getLength();
			}
			((MLPSegment) seg).endDSP = ((MLPSegment) seg).startDSP + seg.getLength();
		}
		
		for (Link l: links_){
			//预留
			((MLPLink) l).checkConnectivity();
			//将jointLane信息装入Link中
			((MLPLink) l).addLnPosInfo();
		}
	}
	/*
	 * public void resetSensorReadings() { for (int i = 0; i < nSensors(); i ++
	 * ) { getSensor(i).resetSensorReadings(); } }
	 */

	public void calcSegmentData() {
		MLPSegment ps = new MLPSegment();
		for (int i = 0; i < nSegments(); i++) {
			ps = mlpSegment(i);
			ps.calcDensity();
			ps.calcSpeed();
		}
	}

	public void buildemittable(boolean needRET){
		if (needRET) {
			mlpLink(0).emtTable.createRndETables();		
		}
		else {
			mlpLink(0).emtTable.readETables();
		}
	}

	public void resetReleaseTime(){
		for (int i = 0; i<nLanes(); i++){
			mlpLane(i).resetReleaseTime();
		}
	}
	
	/*public void addNewVehID() {
		newVehID_ += 1;		
	}*/
	
	public int getNewVehID(){
		newVehID_ += 1;	
		return newVehID_ ;
	}
	
	public void loadEmtTable(){
		//double now = SimulationClock.getInstance().getCurrentTime();
		for (int i = 0; i<nLinks(); i++){
			while (mlpLink(i).checkFirstEmtTableRec()){
				Inflow emitVeh = mlpLink(i).emtTable.getInflow().poll();
				MLPVehicle newVeh = veh_pool.generate();
				newVeh.initInfo(0,mlpLink(i),(MLPSegment) mlpLink(i).getStartSegment(),mlpLane(emitVeh.laneIdx));
				newVeh.init(getNewVehID(), MLPParameter.VEHICLE_LENGTH, (float) emitVeh.dis, (float) emitVeh.speed);
				newVeh.initEntrance(SimulationClock.getInstance().getCurrentTime(), mlpLane(emitVeh.laneIdx).getLength()-emitVeh.dis);
				//newVeh.init(getNewVehID(), 1, MLPParameter.VEHICLE_LENGTH, (float) emitVeh.dis, (float) now);
				mlpLane(emitVeh.laneIdx).appendVeh(newVeh);
			}
		}
	}
	
	public void platoonRecognize() {
		for (MLPVehicle mlpv : veh_list){
			mlpv.calState();
			if (mlpv.CFState_ && mlpv.speedLevel_==mlpv.leading_.speedLevel_) {
				mlpv.resemblance = true;
			}
			else {
				mlpv.resemblance = false;
			}
			mlpv.resetPlatoonCode();
		}
		/*List<MLPVehicle> vl = findResemblance();
		while (vl.size()>0){
			for (MLPVehicle v: vl){
				v.platoonCode = v.leading_.platoonCode;
				v.resemblance = v.leading_.resemblance;
			}
			vl = findResemblance();
		}*/
		
	}
	
	public List<MLPVehicle> findResemblance() {
		List<MLPVehicle> vList = new ArrayList<MLPVehicle>();
		for (MLPVehicle v: veh_list){
			if (v.resemblance)
				vList.add(v);
		}
		return vList;
	}
	
	public void setOverallCapacity(double arg) {
		for (int i = 0; i < nLanes(); i++) {
			mlpLane(i).setCapacity(arg);
		}
	}	
	public void setCapacity(int idx, double c) {
		mlpLane(idx).setCapacity(c);
	}
	
	public void setOverallSDParas(double [] args) {
		for (int i = 0; i < nLinks(); i++) {
			mlpLink(i).dynaFun.sdPara = args;
		}
	}
	public void setSDParas(int idx, double [] paras) {
		mlpLink(idx).dynaFun.sdPara = paras;
	}
	
	public void setLoopSection(int linkID, double p) {
		MLPLink theLink = (MLPLink) findLink(linkID);
		MLPSegment theSeg = null;
		MLPLane theLane = null;
		double dsp = ((MLPSegment) theLink.getEndSegment()).endDSP * p;
		for (int i = 0; i<theLink.nSegments(); i++) {
			theSeg = (MLPSegment) theLink.getSegment(i);
			if (theSeg.startDSP<dsp && theSeg.endDSP>=dsp) 
				break;
		}
		if (theSeg != null) {
			for (int k = 0; k<theSeg.nLanes(); k++) {
				theLane = (MLPLane) theSeg.getLane(k);
				loops.add(new Loop(theLane, theSeg, theLink, dsp));
			}
		}
		
	}
	
	public void resetNInit(boolean needRET, long seed) {
		sysRand.setSeed(seed);
		for (int i = 0; i < nLinks(); i++) {
			MLPLink LNK = mlpLink(i);
			LNK.emtTable.clearflow();//未发出的车从emtTable中移除
			LNK.tripTime.clear();//重置已记录的Trip Time
			LNK.emtTable.setSeed(seed);//设置随机种子
		}
		for (int i = 0; i < nLanes(); i++) {
			mlpLane(i).vehsOnLn.clear();//从lane上移除在网车辆
		}
		veh_pool.recycleAll();//回收所有在网车辆
		buildemittable(needRET);
	}
	
	public void recordVehicleData(){
		VehicleData vd;
		if (!veh_list.isEmpty()) {
			//遍历vehicle
			for (MLPVehicle v : veh_list) {
				//从对象池获取vehicledata对象
				vd = VehicleDataPool.getVehicleDataPool().getVehicleData();
				//记录车辆信息
				vd.init(v,false,Math.min(1,v.VirtualType_));
				//将vehicledata插入frame
				try {
					JOGLFrameQueue.getInstance().offer(vd, veh_list.size());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
