package com.transyslab.simcore.mlp;

import java.util.*;
import java.util.concurrent.BlockingDeque;

import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.renderer.FrameQueue;
import com.transyslab.commons.tools.EMTTable;
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
	private int newVehID_;
	public MLPVehPool veh_pool;
	public List<MLPVehicle> veh_list;
	public List<MLPLoop> loops;
//	public Random sysRand;//已移动至父类
	private TXTUtils writer = new TXTUtils("src/main/resources/output/EMTR.csv");

	public MLPNetwork() {
		newVehID_ = 0;
		veh_pool = new MLPVehPool();
		veh_list = new ArrayList<MLPVehicle>();
		loops = new ArrayList<MLPLoop>();
//		sysRand = new Random();
	}

	public static MLPNetwork getInstance() {
		HashMap<String, Integer> hm = MLPNetworkPool.getInstance().getHashMap();
		if (hm != null) {
			int threadid = hm.get(Thread.currentThread().getName()).intValue();
			return MLPNetworkPool.getInstance().getNetwork(threadid);
		}
		else {
			MLPEngThread theThread = (MLPEngThread) Thread.currentThread();
			return (MLPNetwork) theThread.network;
		}
	}
	
	@Override
	public MLPNode getNode(int i) {
		return (MLPNode) super.getNode(i);
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
	public void createLoopSurface(){
		// 创建 Loop面
		for(MLPLoop loop:loops){
			loop.createSurface();
		}
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
		
		for (Segment seg: segments_){
			Segment tmpseg = seg;
			while (tmpseg.getUpSegment() != null) {
				tmpseg = tmpseg.getUpSegment();
				((MLPSegment) seg).startDSP += tmpseg.getLength();
			}
			((MLPSegment) seg).endDSP = ((MLPSegment) seg).startDSP + seg.getLength();
		}

		for (int i = 0; i < nSegments(); i++) {
			getSegment(i).organizeLanes();
		}

		for (Segment seg: segments_) {
			((MLPSegment) seg).setSucessiveLanes();
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
			EMTTable.createRndETables();
			for (int i = 0; i < nLinks(); i++) {
				Collections.sort(mlpLink(i).emtTable.getInflow(), (a,b) -> a.time < b.time ? -1 : a.time > b.time ? 1 : 0);
			}
		}
		else {
			EMTTable.readETables();
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
		double now = SimulationClock.getInstance().getCurrentTime();
		int count = 0;
		for (int i = 0; i<nLinks(); i++){
			while (mlpLink(i).checkFirstEmtTableRec()){
				Inflow emitVeh = mlpLink(i).emtTable.getInflow().poll();
				MLPVehicle newVeh = veh_pool.generate();
				newVeh.initInfo(0,mlpLink(i),(MLPSegment) mlpLink(i).getStartSegment(),mlpLane(emitVeh.laneIdx),emitVeh.RVID);
				newVeh.init(getNewVehID(), MLPParameter.VEHICLE_LENGTH, (float) emitVeh.dis, (float) emitVeh.speed);
				newVeh.initPath(mlpLink(i).getUpNode(), findLink(emitVeh.tLinkID).getDnNode());
				newVeh.setPathIndex(1);//正式进入路网时需要设置
				newVeh.initEntrance(SimulationClock.getInstance().getCurrentTime(), mlpLane(emitVeh.laneIdx).getLength()-emitVeh.dis);
				//newVeh.init(getNewVehID(), 1, MLPParameter.VEHICLE_LENGTH, (float) emitVeh.dis, (float) now);
				mlpLane(emitVeh.laneIdx).appendVeh(newVeh);
				count += 1;
			}
		}
		if (count > 0)
			writer.write(now + "," + count + "\r\n");
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
	public void setOverallSDParas(double[] args, int mask) {
		for (int i = 0; i < nLinks(); i++) {
			mlpLink(i).dynaFun.setPartialSD(args, mask);
		}
	}
	public void setSDParas(int idx, double [] paras) {
		mlpLink(idx).dynaFun.sdPara = paras;
	}
	
	public void setLoopSection(String name, int linkID, double p) {
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
				loops.add(new MLPLoop(theLane, theSeg, theLink, name, dsp));
			}
		}
		
	}

	public double sectionMeanSpd(String det_name, double fTime, double tTime) {
		if (loops.size()<1) {
			System.err.println("no loops in network");
			return 0.0;
		}
		List<Double> tmpAll = new ArrayList<>();
		for (MLPLoop lp : loops) {
			if (lp.detName.equals(det_name)) {
				tmpAll.addAll(lp.getPeriodSpds(fTime, tTime));
			}
		}
		if (tmpAll.size()>0){
			double sum = 0.0;
			for(Double val : tmpAll)
				sum += val;
			return sum/tmpAll.size();
		}
		return 0.0;
	}

	public double sectionFlow(String det_name, double fTime, double tTime, boolean useMeanVal) {
		if (loops.size()<1) {
			System.err.println("no loops in network");
			return 0.0;
		}
		double sumFlow = 0.0;
		double laneCount = 0.0;
		for (MLPLoop lp : loops) {
			if (lp.detName.equals(det_name)) {
				laneCount += 1.0;
				sumFlow += lp.countPeriodFlow(fTime, tTime);
			}
		}
		return useMeanVal ?
				laneCount > 0.0 ?
						sumFlow / laneCount /(tTime-fTime) :
						0.0 :
				sumFlow;
	}

	public double sectionMeanFlow(String det_name, double fTime, double tTime, boolean useMeanVal) {
		return sectionFlow(det_name,fTime,tTime,true);
	}

	public double sectionSumFlow(String det_name, double fTime, double tTime, boolean useMeanVal) {
		return sectionFlow(det_name,fTime,tTime,false);
	}
	
	public void resetNetwork(boolean needRET, long seed) {
		sysRand.setSeed(seed);
		newVehID_ = 0;
		for (int i = 0; i < nLinks(); i++) {
			MLPLink LNK = mlpLink(i);
			LNK.emtTable.clearflow();//未发出的车从emtTable中移除
			LNK.tripTime.clear();//重置已记录的Trip Time
			Collections.sort(LNK.jointLanes, (a,b) -> a.jlNum <b.jlNum ? -1 : a.jlNum ==b.jlNum ? 0 : 1);//车道排列顺序复位
		}
		for (int i = 0; i < nLanes(); i++) {
			mlpLane(i).vehsOnLn.clear();//从lane上移除在网车辆
		}
		for (int i = 0; i < nNodes(); i++) {
			mlpNode(i).clearStatedVehs();//从Node上清除未加入路段的车辆
		}
		for (int i = 0; i < loops.size(); i++) {
			loops.get(i).clearRecords();//清除检测器记录结果
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
				vd.init(v,false,Math.min(1,v.VirtualType_),String.valueOf(v.nextLink()==null ? "NA" : v.lane_.successiveDnLanes.get(0).getLink().getCode()==v.nextLink().getCode()));
				//将vehicledata插入frame
				try {
					FrameQueue.getInstance().offer(vd, veh_list.size());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public MLPSegment getSegment(int i) {
		return (MLPSegment) super.getSegment(i);
	}
}
