package com.transyslab.simcore.mlp;

import java.io.IOException;
import java.util.*;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.renderer.FrameQueue;
import com.transyslab.roadnetwork.*;
import org.apache.commons.csv.CSVRecord;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

public class MLPNetwork extends RoadNetwork {
	private int newVehID_;
	public List<MLPVehicle> veh_list;
	protected LinkedList<MLPVehicle> vehPool;
	public List<MLPLoop> loops;

	public MLPNetwork() {
		newVehID_ = 0;
		veh_list = new ArrayList<>();
		vehPool = new LinkedList<>();
		loops = new ArrayList<>();
		simParameter = new MLPParameter();
	}

	@Override
	public void createNode(int id, int type, String name) {
		MLPNode newNode = new MLPNode();
		newNode.init(id, type, nNodes() ,name);
		this.nodes.add(newNode);
		this.addVertex(newNode);
	}

	@Override
	public void createLink(int id, int type, int upNodeId, int dnNodeId) {
		MLPLink newLink = new MLPLink();
		newLink.init(id,type,nLinks(),findNode(upNodeId),findNode(dnNodeId),this);
		links.add(newLink);
		this.addEdge(newLink.getUpNode(),newLink.getDnNode(),newLink);
		this.setEdgeWeight(newLink,Double.POSITIVE_INFINITY);
	}

	@Override
	public void createSegment(int id, int speedLimit, double freeSpeed, double grd, double beginX,
							  double beginY, double b, double endX, double endY) {
		MLPSegment newSegment = new MLPSegment();
		newSegment.init(id,speedLimit,nSegments(),freeSpeed,grd,links.get(nLinks()-1));
		newSegment.initArc(beginX,beginY,b,endX,endY);
		worldSpace.recordExtremePoints(newSegment.getStartPnt());
		worldSpace.recordExtremePoints(newSegment.getEndPnt());
		segments.add(newSegment);
	}

	@Override
	public void createLane(int id, int rule, double beginX, double beginY, double endX, double endY) {
		MLPLane newLane = new MLPLane();
		newLane.init(id,rule,nLanes(),beginX,beginY,endX,endY,segments.get(nSegments()-1));
		worldSpace.recordExtremePoints(newLane.getStartPnt());
		worldSpace.recordExtremePoints(newLane.getEndPnt());
		lanes.add(newLane);
	}

	@Override
	public void createSensor(int id, int type, String detName, int segId, double pos, double zone, double interval) {
		MLPSegment seg = (MLPSegment) findSegment(segId);
		MLPLink lnk = (MLPLink) seg.getLink();
		double dsp = seg.startDSP + seg.getLength()*pos;
		for (int i = 0; i < seg.nLanes(); i++) {
			MLPLane ln = seg.getLane(i);
			MLPLoop loop = new MLPLoop(ln, seg, lnk, detName, dsp, pos);
			loops.add(loop);
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
		return getNode(i);
	}
	public MLPLink mlpLink(int i) {
		return (MLPLink) getLink(i);
	}
	public MLPSegment mlpSegment(int i) {
		return getSegment(i);
	}
	public MLPLane mlpLane(int i){
		return (MLPLane) getLane(i);
	}

	public void calcStaticInfo() {
		super.calcStaticInfo();
		organize();

	}
	public void createLoopSurface(){
		// 创建 Loop面
		for(MLPLoop loop:loops){
			loop.createSurface();
		}
	}

	public void organize() {
		//补充车道编号的信息
		for (Lane l: lanes){
			((MLPLane) l).calLnPos();
		}
		for (Lane l: lanes){
			((MLPLane) l).checkConectedLane();
		}
		
		for (Segment seg: segments){
			Segment tmpseg = seg;
			while (tmpseg.getUpSegment() != null) {
				tmpseg = tmpseg.getUpSegment();
				((MLPSegment) seg).startDSP += tmpseg.getLength();
			}
			((MLPSegment) seg).endDSP = ((MLPSegment) seg).startDSP + seg.getLength();
		}

		for (Segment seg: segments) {
			((MLPSegment) seg).setSucessiveLanes();
		}

		for (Link l: links){
			//预留
			((MLPLink) l).checkConnectivity();
			//将jointLane信息装入Link中
			((MLPLink) l).addLnPosInfo();
		}
	}

	public void calcSegmentData() {
		MLPSegment ps = new MLPSegment();
		for (int i = 0; i < nSegments(); i++) {
			ps = mlpSegment(i);
			ps.calcDensity();
			ps.calcSpeed();
		}
	}

	public void buildEmitTable(boolean needRET, String odFileDir, String emitFileDir){
		if (needRET) {
			createRndETables(odFileDir);
			for (int i = 0; i < nLinks(); i++) {
				Collections.sort(mlpLink(i).getInflow(), (a,b) -> a.time < b.time ? -1 : a.time > b.time ? 1 : 0);
			}
		}
		else {
			readETablesFrom(emitFileDir);
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
		double now = simClock.getCurrentTime();
		for (int i = 0; i<nLinks(); i++){
			while (mlpLink(i).checkFirstEmtTableRec()){
				Inflow emitVeh = mlpLink(i).getInflow().poll();
				MLPVehicle newVeh = generateVeh();
				newVeh.initInfo(0,mlpLink(i),(MLPSegment) mlpLink(i).getStartSegment(),mlpLane(emitVeh.laneIdx),emitVeh.realVID);
				newVeh.init(getNewVehID(), MLPParameter.VEHICLE_LENGTH, (float) emitVeh.dis, (float) emitVeh.speed);
				//todo 调试阶段暂时固定路径
				newVeh.fixPath();
				newVeh.initEntrance(simClock.getCurrentTime(), mlpLane(emitVeh.laneIdx).getLength()-emitVeh.dis);
				//newVeh.init(getNewVehID(), 1, MLPParameter.VEHICLE_LENGTH, (float) emitVeh.dis, (float) now);
				mlpLane(emitVeh.laneIdx).appendVeh(newVeh);
			}
		}
	}
	
	public void platoonRecognize() {
		for (MLPVehicle mlpv : veh_list){
			mlpv.calState();
			if (mlpv.cfState && mlpv.speedLevel ==mlpv.leading.speedLevel) {
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
				v.platoonCode = v.leading.platoonCode;
				v.resemblance = v.leading.resemblance;
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
	
	public void setLoopsOnLink(String name, int linkID, double p) {
		MLPLink theLink = (MLPLink) findLink(linkID);
		MLPSegment theSeg = null;
		MLPLane theLane = null;
		//这里的p是自Link起点的百分位位置
		double dsp = ((MLPSegment) theLink.getEndSegment()).endDSP * p;
		for (int i = 0; i<theLink.nSegments(); i++) {
			theSeg = (MLPSegment) theLink.getSegment(i);
			if (theSeg.startDSP<dsp && theSeg.endDSP>=dsp) 
				break;
		}
		if (theSeg != null) {
			MLPSegment endSeg = (MLPSegment) theLink.getEndSegment();
			double portion = (dsp - endSeg.startDSP) / endSeg.getLength();
			for (int k = 0; k<theSeg.nLanes(); k++) {
				theLane = theSeg.getLane(k);
				loops.add(new MLPLoop(theLane, theSeg, theLink, name, dsp, portion));
			}
		}
		
	}

	public void setLoopsOnSeg(String name, int segId, double portion) {
		//这里的portion是自seg起点的百分比
		MLPSegment theSeg = (MLPSegment) findSegment(segId);
		MLPLane theLane = null;
		double dsp = theSeg.startDSP + theSeg.getLength() * portion;
		for (int k = 0; k<theSeg.nLanes(); k++) {
			theLane = theSeg.getLane(k);
			loops.add(new MLPLoop(theLane, theSeg, (MLPLink) theSeg.getLink(), name, dsp, portion));
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
	
	public void resetNetwork(boolean needRET, String odFileDir, String emitFileDir, long seed) {
		sysRand.setSeed(seed);
		newVehID_ = 0;
		for (int i = 0; i < nLinks(); i++) {
			MLPLink LNK = mlpLink(i);
			LNK.clearInflow();//未发出的车从emtTable中移除
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
		recycleAllVehs();//回收所有在网车辆
		buildEmitTable(needRET, odFileDir, emitFileDir);
	}
	
	public void recordVehicleData(){
		VehicleData vd;
		if (!veh_list.isEmpty()) {
			//遍历vehicle
			for (MLPVehicle v : veh_list) {
				//从对象池获取vehicledata对象
				vd = VehicleDataPool.getVehicleDataPool().getVehicleData();
				//记录车辆信息
				vd.init(v,false,Math.min(1,v.virtualType),String.valueOf(v.getNextLink()==null ? "NA" : v.lane.successiveDnLanes.get(0).getLink().getId()==v.getNextLink().getId()));
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

	private void assignPath(MLPVehicle mlpVeh, MLPNode oriNode, MLPNode desNode, boolean isImported) {
		if (isImported) {
			// todo 完善导入路径的逻辑
			mlpVeh.fixPath();
		}
		else {
			ODPair thePair = findODPair(oriNode, desNode);
			mlpVeh.setPath(thePair.assignRoute(mlpVeh),1);
		}
	}

	@Override
	public ODPair findODPair(Node oriNode, Node desNode) {
		ODPair thePair = super.findODPair(oriNode, desNode);
//		ODPair thePair = odPairs.stream().
//				filter(x -> x.getOriNode().equals(oriNode) && x.getDesNode().equals(desNode)).
//				findFirst().
//				orElse(null);
		if (thePair == null) {
			// todo 应加入所有可行路径，非最短路
			GraphPath<Node, Link> gpath = DijkstraShortestPath.findPathBetween(this, oriNode, desNode);
			ODPair newPair = new ODPair(oriNode, desNode);
			newPair.addPath(new Path(gpath));
			odPairs.add(newPair);
			return newPair;
		}
		else {
			return thePair;
		}
	}

	//MLPVehicle recycle operations
	public MLPVehicle generateVeh() {
		MLPVehicle newVeh;
		if (!vehPool.isEmpty())
			newVeh = vehPool.poll();
		else
			newVeh = new MLPVehicle((MLPParameter) simParameter);
		newVeh.updateUsage();
		veh_list.add(newVeh);
		return newVeh;
	}

	public void recycleVeh(MLPVehicle v) {
		veh_list.remove(v);
		if (vehPool.size()<100) {
			v.clearMLPProperties();
			vehPool.offer(v);
		}
		else v = null;
	}

	public void recycleAllVehs() {
		if (!veh_list.isEmpty()) {
			for (MLPVehicle veh : veh_list) {
				veh.clearMLPProperties();
				vehPool.offer(veh);
			}
			veh_list.clear();
		}
	}

	public int countIdleVeh(){
		return vehPool.size();
	}

	private void createRndETables(String odFileDir){
		String[] header = {"fLinkID","tLinkID","demand",
				"fTime","tTime",
				"mean","sd","vlim"};
		try {
			List<CSVRecord> rows = CSVUtils.readCSV(odFileDir,header);
			for(int i = 1; i<rows.size(); i++){
				CSVRecord r = rows.get(i);
				int fLinkID = Integer.parseInt(r.get(0));
				int tLinkID = Integer.parseInt(r.get(1));
				int demand = Integer.parseInt(r.get(2));
				double [] time = {Double.parseDouble(r.get(3)),
								  Double.parseDouble(r.get(4))};
				double [] speed = {Double.parseDouble(r.get(5)),
								   Double.parseDouble(r.get(6)),
								   Double.parseDouble(r.get(7))};
				MLPLink theLink = (MLPLink) findLink(fLinkID);
				List<Lane> lanes = (theLink.getStartSegment()).getLanes();
				theLink.generateInflow(demand, speed, time, lanes, tLinkID);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readETablesFrom(String filePath){
		String[] header = {"laneID", "tLinkID", "time", "speed", "dis", "rvId"};
		try {
			List<CSVRecord> rows = CSVUtils.readCSV(filePath,header);
			int theLNID = Integer.MIN_VALUE;
			MLPLane theLN = null;
			MLPLink theLNK = null;
			for(int i = 1; i<rows.size(); i++){
				CSVRecord r = rows.get(i);
				int LNID = Integer.parseInt(r.get(0));
				if (theLNID != LNID) {
					theLN = (MLPLane) findLane(LNID);
					theLNK = (MLPLink) theLN.getLink();
					theLNID = LNID;
				}
				theLNK.appendInflowFromCSV(LNID,
						Integer.parseInt(r.get(1)),
						Double.parseDouble(r.get(2)),
						Double.parseDouble(r.get(3)),
						Double.parseDouble(r.get(4)),
						Integer.parseInt(r.get(5)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
