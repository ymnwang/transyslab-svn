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
	public void createNode(int id, int type, String name) {
		RTNode newNode = new RTNode();
		newNode.init(id, type, nNodes() ,name);
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
	public void createLane(int id, int rule, double beginX, double beginY, double endX, double endY, int lbid, int rbid) {
		RTLane newLane = new RTLane();
		newLane.init(id,rule,nLanes(),beginX,beginY,endX,endY,segments.get(nSegments()-1),lbid,rbid);
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
	public void renderState(List<VehicleData> vds ){
		AnimationFrame af = new AnimationFrame();
		//数据分装
		for(VehicleData vd:vds){
			RTLane rtLane = (RTLane) findLane(vd.getCurLaneID());
			if(vd.isQueue()){
				// 排队车辆
				rtLane.addQueueVD(vd);
				// 保存数据
				af.addVehicleData(vd);
			}
			else
				// 非排队车辆
				rtLane.addVehicleData(vd);
		}
		for(int i=0;i<nLanes();i++){
			RTLane rtLane = (RTLane) getLane(i);
			// 根据行驶距离排序，找出队尾
			rtLane.calcState();
			StateData sd = new StateData(rtLane,rtLane.stateSurface,rtLane.queuePosition,rtLane.avgSpeed);
			af.addStateData(sd);
		}
		/*
		double avgSpeed = 0;
		double[] queuePostion = new double[]{380,380,380,380,380};
		List<VehicleData> vds1 = new ArrayList<>();
		List<VehicleData> vds2 = new ArrayList<>();
		List<VehicleData> vds3 = new ArrayList<>();
		List<VehicleData> vds4 = new ArrayList<>();
		List<VehicleData> vds5 = new ArrayList<>();
		List<Integer> queueLaneIds = new ArrayList<>();

		for(VehicleData vd:vds) {
			double l = findLane(vd.getCurLaneID()).getLength();
			double tail = l-vd.getDistance();
			switch (vd.getCurLaneID()) {
				case 1:
					vds1.add(vd);
					if(vd.isQueue()) {
						if (queuePostion[0] > l-vd.getDistance()) {
							queuePostion[0] = l-vd.getDistance();
						}
						af.addVehicleData(vd);
					}
					break;
				case 2:
					vds2.add(vd);
					if(vd.isQueue()) {
						if (queuePostion[1] > l-vd.getDistance()) {
							queuePostion[1] = l-vd.getDistance();
						}
						af.addVehicleData(vd);
					}
					break;
				case 4:
					vds3.add(vd);
					if(vd.isQueue()) {
						if (queuePostion[2] > l-vd.getDistance()) {
							queuePostion[2] = l-vd.getDistance();
						}
						af.addVehicleData(vd);
					}
					break;
				case 5:
					vds4.add(vd);
					if(vd.isQueue()) {
						if (queuePostion[3] > l-vd.getDistance()) {
							queuePostion[3] = l-vd.getDistance();
						}
						af.addVehicleData(vd);
					}
					break;
				case 6:
					vds5.add(vd);
					if(vd.isQueue()) {
						if (queuePostion[4] > l-vd.getDistance()) {
							queuePostion[4] = l-vd.getDistance();
						}
						af.addVehicleData(vd);
					}
					break;
				default:
					break;
			}

		}

		//减去长度 queuePostion = queuePostion-Constants.DEFAULT_VEHICLE_LENGTH;
		for (Lane lane : lanes) {
			RTLane rtLane = (RTLane) lane;

			switch (lane.getId()) {
				case 1:
					if(!vds1.isEmpty()) {
						if(queuePostion[0] == 380)
							queuePostion[0] = lane.getLength();
						rtLane.calState(queuePostion[0]);
						rtLane.setAvgSpeed(vds1.stream().mapToDouble(VehicleData::getCurSpeed).average().getAsDouble());
					}
					break;
				case 2:
					if(!vds2.isEmpty()) {
						if(queuePostion[1] == 380)
							queuePostion[1] = lane.getLength();
						rtLane.calState(queuePostion[1]);
						rtLane.setAvgSpeed(vds2.stream().mapToDouble(VehicleData::getCurSpeed).average().getAsDouble());
					}
					break;
				case 4:
					if(!vds3.isEmpty()) {
						if(queuePostion[2] == 380)
							queuePostion[2] = lane.getLength();
						rtLane.calState(queuePostion[2]);
						rtLane.setAvgSpeed(vds3.stream().mapToDouble(VehicleData::getCurSpeed).average().getAsDouble());
					}
					break;
				case 5:
					if(!vds4.isEmpty()) {
						if(queuePostion[3] == 380)
							queuePostion[3] = lane.getLength();
						rtLane.calState(queuePostion[3]);
						rtLane.setAvgSpeed(vds4.stream().mapToDouble(VehicleData::getCurSpeed).average().getAsDouble());
					}
					break;
				case 6:
					if(!vds5.isEmpty()) {
						if(queuePostion[4] == 380)
							queuePostion[4] = lane.getLength();
						rtLane.calState(queuePostion[4]);
						rtLane.setAvgSpeed(vds5.stream().mapToDouble(VehicleData::getCurSpeed).average().getAsDouble());
					}
					break;
				default:
					break;
			}
		}*/
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
				vd = VehicleDataPool.getVehicleDataPool().getVehicleData();
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
	public void renderVehicle(List<VehicleData> extVd){
		AnimationFrame af = new AnimationFrame();
		for(VehicleData vd:extVd){
			af.addVehicleData(vd);
		}
		try {
			FrameQueue.getInstance().offer(af);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int addLaneConnector(int up, int dn, int successiveFlag) {
		int ans = super.addLaneConnector(up, dn, successiveFlag);
		if (successiveFlag == Constants.SUCCESSIVE_LANE) {
			RTLane upLane = (RTLane) findLane(up);
			RTLane dnLane = (RTLane) findLane(dn);
			upLane.successiveDnLanes.add(dnLane);
			dnLane.successiveUpLanes.add(upLane);
			createConnector(upLane,dnLane);
		}
		return ans;
	}

}
