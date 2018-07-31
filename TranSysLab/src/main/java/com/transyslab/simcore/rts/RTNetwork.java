package com.transyslab.simcore.rts;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.io.DBWriter;
import com.transyslab.commons.io.SQLConnection;
import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.renderer.AnimationFrame;
import com.transyslab.commons.renderer.FrameQueue;
import com.transyslab.roadnetwork.*;
import org.apache.commons.csv.CSVRecord;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;


/**
 * Created by ITSA405-35 on 2018/5/28.
 */
public class RTNetwork extends RoadNetwork{

	private List<RTVehicle> vhcList;
	private LinkedList<RTVehicle> vhcPool;

	public RTNetwork() {
		simParameter = new RTParameter();//需要在RoadNetwork子类初始化
		vhcList = new ArrayList<>();
		vhcPool = new LinkedList<>();
	}

	@Override
	public void createNode(int id, int type, String name, double x, double y) {
		RTNode newNode = new RTNode();
		newNode.init(id, type, nNodes() ,name, x, y);
		this.nodes.add(newNode);
		this.addVertex(newNode);
	}

	@Override
	public void createLink(int id, int type, int upNodeId, int dnNodeId) {
		RTLink newLink = new RTLink();
		newLink.init(id,type,nLinks(),findNode(upNodeId),findNode(dnNodeId),this);
		links.add(newLink);
		this.addEdge(newLink.getUpNode(),newLink.getDnNode(),newLink);
		this.setEdgeWeight(newLink,Double.POSITIVE_INFINITY);
	}

	@Override
	public void createSegment(int id, int speedLimit, double freeSpeed, double grd, double beginX,
							  double beginY, double b, double endX, double endY) {
		RTSegment newSegment = new RTSegment();
		newSegment.init(id,speedLimit,nSegments(),freeSpeed,grd,links.get(nLinks()-1));
		newSegment.initArc(beginX,beginY,b,endX,endY);
		worldSpace.recordExtremePoints(newSegment.getStartPnt());
		worldSpace.recordExtremePoints(newSegment.getEndPnt());
		segments.add(newSegment);
	}

	@Override
	public void createLane(int id, int rule, double beginX, double beginY, double endX, double endY) {
		RTLane newLane = new RTLane();
		newLane.init(id,rule,nLanes(),beginX,beginY,endX,endY,segments.get(nSegments()-1));
		worldSpace.recordExtremePoints(newLane.getStartPnt());
		worldSpace.recordExtremePoints(newLane.getEndPnt());
		lanes.add(newLane);
	}

	@Override
	public void createSensor(int id, int type, String detName, int segId, double pos, double zone, double interval) {
		RTSegment seg = (RTSegment) findSegment(segId);
		RTLink lnk = (RTLink) seg.getLink();
		/* TODO 初始化检测器
		double dsp = seg.startDSP + seg.getLength()*pos;
		for (int i = 0; i < seg.nLanes(); i++) {
			RTLane ln = (RTLane)seg.getLane(i);
			RTLoop loop = new RTLoop(ln, seg, lnk, detName, dsp, pos);
			sensors.add(loop);
		}*/
	}

	public void calcStaticInfo() {
		super.calcStaticInfo();
		organize();
	}

	public void organize() {
		//补充车道编号的信息
		for (Lane l: lanes){
			((RTLane) l).calLnPos();
		}
		for (Lane l: lanes){
			((RTLane) l).checkConectedLane();
		}

		for (Segment seg: segments){
			// TODO 线性参考上移至父类
			Segment tmpseg = seg;
			double startDSP = 0;
			while (tmpseg.getUpSegment() != null) {
				tmpseg = tmpseg.getUpSegment();
				startDSP += tmpseg.getLength();
			}
			((RTSegment) seg).setStartDSP(startDSP);
			double endDSP = ((RTSegment) seg).getStartDSP() + seg.getLength();
			((RTSegment) seg).setEndDSP(endDSP);
		}

		for (Segment seg: segments) {
			((RTSegment) seg).setSucessiveLanes();
		}

		for (Link l: links){
			//预留
			((RTLink) l).checkConnectivity();
			//组织laneGraph
			segments.forEach(segment -> {
				lanes.forEach(lane -> {
					((RTLink) l).addLaneGraphVertex((RTLane) lane);
				});
			});
			for (Segment seg: segments) {
				for (int i = 0; i < seg.nLanes(); i++) {
					RTLane mlpLane = (RTLane) seg.getLane(i);
					if (i<seg.nLanes()-1)//可叠加实线判断
						((RTLink)l).addLaneGraphEdge(mlpLane, (RTLane) mlpLane.getRightLane(), 1.0);
					if (i>0)//可叠加实线判断
						((RTLink)l).addLaneGraphEdge(mlpLane, (RTLane) mlpLane.getLeftLane(),1.0);
					if (!((RTSegment) seg).isEndSeg())
						mlpLane.successiveDnLanes.forEach(suDnLane ->
								((RTLink)l).addLaneGraphEdge(mlpLane, (RTLane) suDnLane, 0.0));//可叠加封路判断
				}
			}
			//将jointLane信息装入Link中
			//((RTLink) l).addLnPosInfo();
		}
	}

	public void resetNetwork(long seed) {
		sysRand.setSeed(seed);//重置系统种子
	}
	public void generateVehicle(int id, int laneId, double speed, double distance){
		RTVehicle newVehicle = vhcPool.poll();
		if(newVehicle == null)
			newVehicle = new RTVehicle();
		newVehicle.init(id,(RTLane) findLane(laneId),speed,distance);
		this.vhcList.add(newVehicle);
	}
	public void removeVehicle(RTVehicle vehicle){
		vhcList.remove(vehicle);
		vhcPool.offer(vehicle);
	}
	public void renderState(List<VehicleData> vds,double secondOfDay){

		AnimationFrame af = new AnimationFrame();
		List<VehicleData> queueVehicles = new ArrayList<>();
		List<VehicleData> movingVehicles = new ArrayList<>();
		// 按车道id分组
		for(VehicleData vd:vds){
			if(vd.isQueue()) {
				queueVehicles.add(vd);
				af.addVehicleData(vd);// 渲染车辆
			}
			else
				movingVehicles.add(vd);
		}
		Map<Integer,List<VehicleData>> qvdsByLane = queueVehicles.stream().collect(groupingBy(VehicleData::getCurLaneID));
		Map<Integer,List<VehicleData>> mvdsByLane = movingVehicles.stream().collect(groupingBy(VehicleData::getCurLaneID));
		for(int i=0;i<nLanes();i++){
			RTLane rtLane = (RTLane) getLane(i);
			int key = rtLane.getId();
			rtLane.calcState(qvdsByLane.get(key),mvdsByLane.get(key));
			StateData sd = new StateData(rtLane);
			af.addStateData(sd);
		}
		setArrowColor(secondOfDay, af.getSignalColors());
		af.setSimTimeInSeconds(secondOfDay);
		try {
			FrameQueue.getInstance().offer(af);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void recordVehicleData(){
		VehicleData vd;
		AnimationFrame af;
		if (!vhcList.isEmpty()) {
			af = new AnimationFrame();
			//遍历vehicle
			for (RTVehicle v : vhcList) {
				//从对象池获取vehicledata对象
				vd = VehicleDataPool.getInstance().newData();
				//记录车辆信息
				vd.init(v,
						false,
						1,
						//String.valueOf(v.getNextLink()==null ? "NA" : v.lane.successiveDnLanes.get(0).getLink().getId()==v.getNextLink().getId())
						v.toString());
				//将vehicledata插入frame
				af.addVehicleData(vd);

			}
			//添加额外信息(帧号)
			/*
			int count = 0;
			for(int i = 0; i< nSensors(); i++){
				RTLoop tmpSensor = (RTLoop) getSensor(i);
				count = count + tmpSensor.getRecords().size();
			}
			af.setInfo("Count",count);*/
			try {
				FrameQueue.getInstance().offer(af);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public void renderVehicle(List<VehicleData> extVd, double secondOfDay){
		AnimationFrame af = new AnimationFrame();
		for(VehicleData vd:extVd){
			af.addVehicleData(vd);
		}
		setArrowColor(secondOfDay, af.getSignalColors());
		af.setSimTimeInSeconds(secondOfDay);
		try {
			FrameQueue.getInstance().offer(af);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int addLaneConnector(int id, int up, int dn, int successiveFlag, List<GeoPoint> polyline) {
		int ans = super.addLaneConnector(id,up, dn, successiveFlag,polyline);
			RTLane upLane = (RTLane) findLane(up);
			RTLane dnLane = (RTLane) findLane(dn);
			//upLane.successiveDnLanes.add(dnLane);
			//dnLane.successiveUpLanes.add(upLane);
			createConnector(id,polyline,upLane,dnLane);
		return ans;
	}

}
