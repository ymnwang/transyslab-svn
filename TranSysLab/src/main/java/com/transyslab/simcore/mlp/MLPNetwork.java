package com.transyslab.simcore.mlp;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import com.transyslab.commons.io.*;
import com.transyslab.commons.renderer.FrameQueue;
import com.transyslab.roadnetwork.*;
import org.apache.commons.csv.CSVRecord;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

public class MLPNetwork extends RoadNetwork {
	private int newVehID_;
	public List<MLPVehicle> veh_list;
	protected LinkedList<MLPVehicle> vehPool;
//	public List<MLPLoop> sensors;

	//�����������
	protected HashMap<MLPLink, List<MacroCharacter>> linkStatMap;
	protected HashMap<MLPLoop[], List<MacroCharacter>> sectionStatMap;

	public MLPNetwork() {
		simParameter = new MLPParameter();//��Ҫ��RoadNetwork�����ʼ��

		newVehID_ = 0;
		veh_list = new ArrayList<>();
		vehPool = new LinkedList<>();

		linkStatMap = new HashMap<>();
		sectionStatMap = new HashMap<>();
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
			sensors.add(loop);
		}
	}


	public MLPNode mlpNode(int i) {
		return (MLPNode) getNode(i);
	}
	public MLPLink mlpLink(int i) {
		return (MLPLink) getLink(i);
	}
	public MLPLane mlpLane(int i){
		return (MLPLane) getLane(i);
	}

	@Override
	public MLPLink findLink(int id) {
		return (MLPLink) super.findLink(id);
	}

	public void calcStaticInfo() {
		super.calcStaticInfo();
		organize();

	}


	public void organize() {
		//���䳵����ŵ���Ϣ
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
			//Ԥ��
			((MLPLink) l).checkConnectivity();
			//��jointLane��Ϣװ��Link��
			((MLPLink) l).addLnPosInfo();
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
	
	public int getNewVehID(){
		newVehID_ += 1;	
		return newVehID_ ;
	}
	
	public void loadEmtTable(){
		for (int i = 0; i<nLinks(); i++){
			MLPLink launchingLink = mlpLink(i);
			while (launchingLink.checkFirstEmtTableRec()){
				Inflow emitVeh = launchingLink.getInflow().poll();
				MLPVehicle newVeh = generateVeh();
				//TODO: δʵ�ִ�·���м���濪ʼ����
				newVeh.initInfo(0,launchingLink,mlpLane(emitVeh.laneIdx).getSegment(),mlpLane(emitVeh.laneIdx),emitVeh.realVID);
				newVeh.init(getNewVehID(), MLPParameter.VEHICLE_LENGTH, (float) emitVeh.dis, (float) emitVeh.speed);
				assignPath(newVeh, (MLPNode) launchingLink.getUpNode(), (MLPNode) findLink(emitVeh.tLinkID).getDnNode(), false);
				//todo ���Խ׶���ʱ�̶�·��
				newVeh.fixPath();
				newVeh.initNetworkEntrance(simClock.getCurrentTime(), mlpLane(emitVeh.laneIdx).getLength()-emitVeh.dis);
				//����·������ʼ��ǿ�ƻ����ο�ֵdi
				newVeh.updateDi();
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
	}
	
	public void setOverallCapacity(double arg) {
		for (int i = 0; i < nLanes(); i++) {
			mlpLane(i).setCapacity(arg);
		}
	}	
	public void setCapacity(int laneIdx, double capacity) {
		mlpLane(laneIdx).setCapacity(capacity);
	}

	public void setOverallSDParas(double[] args, int mask) {
		for (int i = 0; i < nLinks(); i++) {
			mlpLink(i).dynaFun.setPartialSD(args, mask);
		}
	}
	public void setSDParas(int linkIdx, double[] args, int mask) {
		mlpLink(linkIdx).dynaFun.setPartialSD(args, mask);
	}
	
	public void setLoopsOnLink(String name, int linkID, double p) {
		MLPLink theLink = findLink(linkID);
		MLPSegment theSeg = null;
		MLPLane theLane = null;
		//�����p����Link���İٷ�λλ��
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
				sensors.add(new MLPLoop(theLane, theSeg, theLink, name, dsp, portion));
			}
		}
		
	}

	public void setLoopsOnSeg(String name, int segId, double portion) {
		//�����portion����seg���İٷֱ�
		MLPSegment theSeg = (MLPSegment) findSegment(segId);
		MLPLane theLane = null;
		double dsp = theSeg.startDSP + theSeg.getLength() * portion;
		for (int k = 0; k<theSeg.nLanes(); k++) {
			theLane = theSeg.getLane(k);
			sensors.add(new MLPLoop(theLane, theSeg, (MLPLink) theSeg.getLink(), name, dsp, portion));
		}
	}

	public double sectionMeanSpd(String det_name, double fTime, double tTime) {
		if (sensors.size()<1) {
			System.err.println("no sensors in network");
			return 0.0;
		}
		List<Double> tmpAll = new ArrayList<>();
		for (Sensor lp : sensors) {
			MLPLoop mlpLoop = (MLPLoop) lp;
			if (mlpLoop.detName.equals(det_name)) {
				tmpAll.addAll(mlpLoop.getPeriodSpds(fTime, tTime));
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

	public void sectionStatistics(double fTime, double tTime, int avgMode) {
		List<Double> spdRecords = new ArrayList<>();
		for (MLPLoop[] sec : sectionStatMap.keySet()) {
			Arrays.stream(sec).forEach(loop -> spdRecords.addAll(loop.getPeriodSpds(fTime, tTime)));
			//cal flow
			double flow = spdRecords.size();
			//cal meanSpd
			double meanSpd = flow <= 0 ? 0.0 :
					avgMode == Constants.ARITHMETIC_MEAN ? spdRecords.stream().mapToDouble(d->d).sum() / flow :
							avgMode == Constants.HARMONIC_MEAN ? flow / spdRecords.stream().mapToDouble(d -> 1/d).sum() :
									0.0;
			spdRecords.clear();
			flow = flow / (tTime-fTime) / sec.length;
			sectionStatMap.get(sec).add(new MacroCharacter(flow, meanSpd, flow <= 0 ? 0.0 : flow / meanSpd, Double.NaN));
		}
	}

	public void linkStatistics(double fTime, double tTime) {
		/*flowΪlink��ʱ����ڷ����������trip�ĸ�����speedΪ�ܷ������/�ܷ���ʱ�䣻 TrTΪǰ���ߵ���*/
		double now = getSimClock().getCurrentTime();
		for (MLPLink mlpLink : linkStatMap.keySet()) {
			double linkLen = mlpLink.length();

			if (simClock.getCurrentTime()<=tTime) { //��ǰʱ�����ڷ���ĳ���Ҳ��������
				Object[] servingVehsObj = veh_list.stream().filter(v ->
						v.virtualType>0
								&& v.getLink().equals(mlpLink)
								&& v.timeEntersLink()<now).toArray();
				MLPVehicle[] servingVehs = Arrays.copyOf(servingVehsObj,servingVehsObj.length,MLPVehicle[].class);
				double onLinkTripSum = Arrays.stream(servingVehs).mapToDouble(v -> (v.Displacement()-v.dspLinkEntrance)/linkLen).sum();
				double onLinkTrTSum = Arrays.stream(servingVehs).mapToDouble(v -> (now - v.timeEntersLink())).sum();
			}

			Object[] servedRecordsObj = mlpLink.tripTime.stream().filter(trT -> trT[MLPLink.TIMEIN_MASK]>fTime && trT[MLPLink.TIMEOUT_MASK]<=tTime).toArray();
			double[][] servedRecords = Arrays.copyOf(servedRecordsObj,servedRecordsObj.length,double[][].class);
			double servedTripSum = Arrays.stream(servedRecords).mapToDouble(r -> (linkLen-r[MLPLink.DSPIN_MASK])/linkLen).sum();
			double servedTrTSum = Arrays.stream(servedRecords).mapToDouble(r -> r[MLPLink.TIMEOUT_MASK] - r[MLPLink.TIMEIN_MASK]).sum();

			/*double flow = onLinkTripSum + servedTripSum;
			double meanSpd = flow<=0.0 ? 0.0 : linkLen*flow/(onLinkTrTSum+servedTrTSum);*/
			double flowSum = servedTripSum;
			double meanSpd = flowSum<=0.0 ? 0.0 : linkLen*flowSum/servedTrTSum;
			double trT = meanSpd<=0.0 ? 0.0 : linkLen / meanSpd;

			double flow = flowSum / (tTime-fTime);
			linkStatMap.get(mlpLink).add(new MacroCharacter(flow, meanSpd, flow <= 0 ? 0.0 : flow/meanSpd, trT));
		}
	}

	public void linkStatistics_Old(double fTime, double tTime) {
		double now = getSimClock().getCurrentTime();
		for (MLPLink mlpLink : linkStatMap.keySet()) {
			double[][] servedRecords = (double[][]) mlpLink.tripTime.stream().filter(trT -> trT[0]>fTime && trT[0]<=tTime).toArray();
			if (servedRecords.length > 0) {
				double flow = servedRecords.length;
				double trT = Arrays.stream(servedRecords).mapToDouble(r -> r[1]).sum()/flow;
				double meanSpd = flow * mlpLink.length() / trT;
				double density = flow/meanSpd;
				flow = flow / (tTime-fTime);
				linkStatMap.get(mlpLink).add(new MacroCharacter(flow, meanSpd, density, trT));
			}
			else
				linkStatMap.get(mlpLink).add(new MacroCharacter(0,0,0,0));
		}
	}
	
	public void resetNetwork(boolean needRET, String odFileDir, String emitFileDir, long seed) {
		sysRand.setSeed(seed);//����ϵͳ����
		newVehID_ = 0;//���ó������
		resetReleaseTime();//����capacity��������ʱ��
		for (int i = 0; i < nLinks(); i++) {
			MLPLink LNK = mlpLink(i);
			LNK.clearInflow();//δ�����ĳ���emtTable���Ƴ�
			LNK.tripTime.clear();//�����Ѽ�¼��Trip Time
			Collections.sort(LNK.jointLanes, (a,b) -> a.jlNum <b.jlNum ? -1 : a.jlNum ==b.jlNum ? 0 : 1);//��������˳��λ
		}
		for (int i = 0; i < nLanes(); i++) {
			mlpLane(i).vehsOnLn.clear();//��lane���Ƴ���������
		}
		for (int i = 0; i < nNodes(); i++) {
			mlpNode(i).clearStatedVehs();//��Node�����δ����·�εĳ���
		}
		for (int i = 0; i < sensors.size(); i++) {
			((MLPLoop) sensors.get(i)).clearRecords();//����������¼���
		}
		recycleAllVehs();//����������������
		buildEmitTable(needRET, odFileDir, emitFileDir);//���½�������

		//�����������
		clearSecStat();
		clearLinkStat();
	}
	
	public void recordVehicleData(){
		VehicleData vd;
		if (!veh_list.isEmpty()) {
			//����vehicle
			for (MLPVehicle v : veh_list) {
				//�Ӷ���ػ�ȡvehicledata����
				vd = VehicleDataPool.getVehicleDataPool().getVehicleData();
				//��¼������Ϣ
				vd.init(v,
						false,
						(v.resemblance ? Constants.FOLLOWING : 0) + Math.min(1,v.virtualType),
						//String.valueOf(v.getNextLink()==null ? "NA" : v.lane.successiveDnLanes.get(0).getLink().getId()==v.getNextLink().getId())
						v.getInfo());
				//��vehicledata����frame
				try {
					FrameQueue.getInstance().offer(vd, veh_list.size());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void assignPath(MLPVehicle mlpVeh, MLPNode oriNode, MLPNode desNode, boolean isImported) {
		if (isImported) {
			// todo ���Ƶ���·�����߼�
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
		if (thePair == null) {
			// todo Ӧ�������п���·���������·
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
		if (odFileDir==null || odFileDir.equals(""))
			return;
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
				MLPLink theLink = findLink(fLinkID);
				List<Lane> lanes = theLink.getStartSegment().getLanes();
				theLink.generateInflow(demand, speed, time, lanes, tLinkID);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readETablesFrom(String filePath){
		if (filePath==null || filePath.equals(""))
			return;
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

	public void clearInflows() {
		links.stream().forEach(l -> ((MLPLink) l).clearInflow());
	}
	public void initLinkStatMap(String linkIdStr) {
		String[] parts = linkIdStr.split(",");
		if (parts.length<=0 || parts[0].equals("")) return;
		Arrays.stream(parts).forEach(p -> linkStatMap.put(findLink(Integer.parseInt(p)), new ArrayList<>()));
	}
	public void initSectionStatMap(String detNameStr) {
		String[] parts = detNameStr.split(",");
		if (parts.length<=0 || parts[0].equals("")) return;
		for (String p : parts) {
			Object[] secObj = sensors.stream().filter(l -> ((MLPLoop) l).detName.equals(p)).toArray();
			MLPLoop[] sec = Arrays.copyOf(secObj, secObj.length, MLPLoop[].class);
			sectionStatMap.put(sec, new ArrayList<>());
		}
	}
	public List<MacroCharacter> getSecStatRecords(String detName) {
		MLPLoop[] theSec = sectionStatMap.keySet().stream().
				filter(sec -> sec[0].detName.equals(detName)).
				findFirst().
				orElse(null);
		return theSec == null ? null : sectionStatMap.get(theSec);
	}
	public List<MacroCharacter> getLinkStatRecords(int LinkId) {
		MLPLink theLink = linkStatMap.keySet().stream().
				filter(link -> link.getId()==LinkId).
				findFirst().
				orElse(null);
		return theLink == null ? null : linkStatMap.get(theLink);
	}
	public void	writeStat(String filename){
		TXTUtils writer = new TXTUtils(filename);
		writer.writeNFlush("DET,TIME_PERIOD,FLOW,SPEED,DENSITY,TRAVEL_TIME\r\n");
		sectionStatMap.forEach((k,v) -> {
			String det = k[0].detName;
			for (int i = 0; i<v.size(); i++) {
				MacroCharacter r = v.get(i);
				writer.write(det + "," +
						(i+1) + "," +
						r.getHourFlow() + "," +
						r.getKmSpeed() + "," +
						r.getKmDensity() + "," +
						r.travelTime + "\r\n");
			}
		});
		writer.flushBuffer();
		linkStatMap.forEach((k,v) -> {
			String det = "Link" + k.getId();
			for (int i = 0; i<v.size(); i++) {
				MacroCharacter r = v.get(i);
				writer.write(det + "," +
						(i+1) + "," +
						r.getHourFlow() + "," +
						r.getKmSpeed() + "," +
						r.getKmDensity() + "," +
						r.travelTime + "\r\n");
			}
		});
		writer.flushBuffer();
		writer.closeWriter();
	}

	public void writeStat2Db(String tag, LocalDateTime dt) {
		DBWriter loopWriter = new DBWriter("insert into simloop(det, time_period, flow, speed, density, travel_time, tag, create_time) values(?,?,?,?,?,?,?,?)");
		sectionStatMap.forEach((k,v) -> {
			String det = k[0].detName;
			for (int i = 0; i<v.size(); i++) {
				MacroCharacter r = v.get(i);
				loopWriter.write(new Object[] {det, (i+1), r.flow, r.speed, r.density, r.travelTime, tag, dt});
			}
		});
		linkStatMap.forEach((k,v) -> {
			String det = "Link" + k.getId();
			for (int i = 0; i<v.size(); i++) {
				MacroCharacter r = v.get(i);
				loopWriter.write(new Object[] {det, (i+1), r.flow, r.speed, r.density, r.travelTime, tag, dt});
			}
		});
		loopWriter.flush();
		loopWriter.close();
	}

	public void clearSecStat() {
		sectionStatMap.forEach((k,v) -> v.clear());
	}

	public void clearLinkStat() {
		linkStatMap.forEach((k,v) -> v.clear());
	}

	public HashMap<String, List<MacroCharacter>> exportStat() {
		HashMap<String, List<MacroCharacter>> statMap = new HashMap<>();
		linkStatMap.forEach((k,v) -> statMap.put("link"+k.getId(),v));
		sectionStatMap.forEach((k,v) -> statMap.put(k[0].detName,v));
		return statMap;
	}

}
