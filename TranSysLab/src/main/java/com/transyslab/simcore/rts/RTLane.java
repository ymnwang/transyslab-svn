package com.transyslab.simcore.rts;

import com.transyslab.commons.tools.GeoUtil;
import com.transyslab.roadnetwork.*;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ITSA405-35 on 2018/5/28.
 */
public class RTLane extends Lane implements Comparator<RTLane> {
	public static final double FREESPEED = 15;//54km/h
	private int lnPosNum_;
	//	private MLPVehicle head_;
//	private MLPVehicle tail_;
//	private double emitTime_;
	//private double capacity = 0.5;
//	private MLPLane upConectLane_;
//	private MLPLane dnConectLane_;
	public int lateralCutInAllowed; // 十位数字0(1)表示(不)允许左侧车道并线；个位数字0(1)表示(不)允许右侧车道并线
	//	public boolean LfCutinAllowed;	//left cut in allowed = true=允许左侧车道换道到此车道，
//	public boolean RtCutinAllowed;	//left cut in allowed = true=允许左侧车道换道到此车道，
	public boolean enterAllowed;	//true=允许（后方）车道车辆驶入;false=不允许车道车辆驶入(等于道路封闭)
	public RTLane connectedDnLane;
	public RTLane connectedUpLane;
	protected List<RTLane> successiveDnLanes;
	protected List<RTLane> successiveUpLanes;
	protected double queueLength;
	protected double avgSpeed;

	public RTLane(){
		lnPosNum_ = 0;
		lateralCutInAllowed = 0;
//		LfCutinAllowed = true;
//		RtCutinAllowed = true;
		enterAllowed = true;
		successiveDnLanes = new ArrayList<>();
		successiveUpLanes = new ArrayList<>();

	}

	public void calcState(List<VehicleData> queueVehicles, List<VehicleData> movingVehicles){
		queueLength = 0;
		avgSpeed = FREESPEED;
		if(queueVehicles!=null) {// 有车排队
			// 按位置升序排序，找出排队位置
			Collections.sort(queueVehicles);
			queueLength = getGeoLength() - queueVehicles.get(0).getDistance() + Constants.DEFAULT_VEHICLE_LENGTH;// 车长
		}
		if(movingVehicles!=null)// 有车在畅行区
			avgSpeed = movingVehicles.stream().mapToDouble(VehicleData::getCurSpeed).average().getAsDouble();
		/*
		this.queuePosition = startPnt.intermediate(endPnt, queueLength/getGeoLength());
		stateSurface = GeoUtil.lineToRectangle(startPnt,queuePosition,width, true);
		if(!vhcOnLn.isEmpty()){// 有车在畅行区
			avgSpeed = vhcOnLn.stream().mapToDouble(VehicleData::getCurSpeed).average().getAsDouble();
			vhcOnLn.clear();
		}*/
	}

	public void setAvgSpeed(double avgSpeed){
		this.avgSpeed = avgSpeed;
	}
	public double getAvgSpeed(){
		return this.avgSpeed;
	}
	public double getQueueLength(){
		return this.queueLength;
	}
	public void calLnPos() {
		lnPosNum_ = getId()%10;
	}

	public int getLnPosNum(){
		return lnPosNum_;
	}

	@Override
	public RTSegment getSegment(){
		return (RTSegment) segment;
	}


	public RTLane getAdjacent(int dir){
		if (dir == 0){
			return (RTLane) getRightLane();
		}
		else {
			if (dir == 1) {
				return (RTLane) getLeftLane();
			}
			else {
				return null;
			}
		}
	}

	public boolean checkLCAllowen(int turning){
		double a = turning%(Math.pow(10, turning+1));
		a = Math.floor(a / Math.pow(10, turning));
		if (a==0) {
			return true;
		}
		else {
			return false;
		}

	}

	public void checkConectedLane() {
		connectedUpLane = getSamePosLane(segment.getUpSegment());
		connectedDnLane = getSamePosLane(segment.getDnSegment());
	}
	public RTLane getSamePosLane(Segment seg) {
		if (seg != null && seg.nLanes()>=lnPosNum_) {
			return (RTLane) seg.getLane(lnPosNum_ - 1);
		}
		else{
			return null;
		}
	}

	public boolean connect2DnLanes(List<Lane> DnLanes) {
		for (Lane tmpLN: DnLanes){
			if (((RTLane) tmpLN).upLanes.contains(this))
				return true;
		}
		return false;
	}

	public boolean successivelyConnect2DnLanes(List<Lane> DnLanes) {
		for (Lane tmpLN: DnLanes){
			if (((RTLane) tmpLN).successiveUpLanes.contains(this))
				return true;
		}
		return false;
	}

	protected RTLane successiveDnLaneInLink(RTLink arg) {
		for (RTLane ln : successiveDnLanes) {
			if (ln.getLink().getId() == arg.getId())
				return ln;
		}
		return null;
	}

	public List<RTLane> selectDnLane(Segment nextSeg) {
		List<RTLane> tmp = new ArrayList<>();
		for (int i = 0; i < nextSeg.nLanes(); i++) {
			RTLane theLane = (RTLane) nextSeg.getLane(i);
			if (dnLanes.contains(theLane)) {
				tmp.add(theLane);
			}
		}
		tmp.sort(this);
		return tmp;
	}

	@Override
	public int compare(RTLane o1, RTLane o2) {
		RTLane tmp = null;
		for (int i = 0; i < successiveDnLanes.size(); i++) {
			tmp = successiveDnLanes.get(i);
			if (tmp.segment.getId() == o1.segment.getId()) {
				break;
			}
		}
		if (tmp == null)
			return 0;
		int d1 = Math.abs(tmp.lnPosNum_ - lnPosNum_);
		int d2 = Math.abs(tmp.lnPosNum_ - lnPosNum_);
		return ( d1 - d2 );
	}

	public String getSDnLnInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("SuccessiveDnLanes:\r\n");
		successiveDnLanes.stream().forEach(e -> sb.append(e.getId() + ", "));
		return sb.toString();
	}

}
