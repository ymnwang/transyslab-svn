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

	private int lnPosNum_;
	//	private MLPVehicle head_;
//	private MLPVehicle tail_;
//	private double emitTime_;
	//private double capacity = 0.5;
//	private MLPLane upConectLane_;
//	private MLPLane dnConectLane_;
	public int lateralCutInAllowed; // ʮλ����0(1)��ʾ(��)������೵�����ߣ���λ����0(1)��ʾ(��)�����Ҳ೵������
	//	public boolean LfCutinAllowed;	//left cut in allowed = true=������೵���������˳�����
//	public boolean RtCutinAllowed;	//left cut in allowed = true=������೵���������˳�����
	public boolean enterAllowed;	//true=�����󷽣���������ʻ��;false=������������ʻ��(���ڵ�·���)
	public RTLane connectedDnLane;
	public RTLane connectedUpLane;
	//	public int di;//����
	protected List<RTLane> successiveDnLanes;
	protected List<RTLane> successiveUpLanes;
	protected double queueLength;
	protected List<VehicleData> vhcOnLn;
	protected List<VehicleData> queueVehicles;
	protected GeoPoint queuePosition;
	protected double avgSpeed;
	protected GeoSurface stateSurface;

	public RTLane(){
		lnPosNum_ = 0;
		lateralCutInAllowed = 0;
//		LfCutinAllowed = true;
//		RtCutinAllowed = true;
		enterAllowed = true;
		successiveDnLanes = new ArrayList<>();
		successiveUpLanes = new ArrayList<>();
		vhcOnLn = new ArrayList<>();
		queueVehicles = new ArrayList<>();
	}
	public void addVehicleData(VehicleData vd){
		this.vhcOnLn.add(vd);
	}
	public void addQueueVD(VehicleData vd){
		this.queueVehicles.add(vd);
	}
	public List<VehicleData> getVDList(){
		return this.vhcOnLn;
	}
	public List<VehicleData> getQVDList(){
		return this.queueVehicles;
	}
	public GeoSurface getStateSurface(){
		return this.stateSurface;
	}
	public void calcState(){
		if(!vhcOnLn.isEmpty()){
			avgSpeed = 0;
			queueLength = getLength();
			// ��λ�����������ҳ��Ŷ�λ��
			if(!queueVehicles.isEmpty()) {
				Collections.sort(queueVehicles);
				queueLength = getLength() - queueVehicles.get(0).getDistance();
			}
			this.queuePosition = endPnt.intermediate(startPnt, queueLength/getLength());
			stateSurface = GeoUtil.lineToRectangle(startPnt,queuePosition,width, true);
			avgSpeed = vhcOnLn.stream().mapToDouble(VehicleData::getCurSpeed).average().getAsDouble();
			queueVehicles.clear();
			vhcOnLn.clear();
		}
	}

	public void setAvgSpeed(double avgSpeed){
		this.avgSpeed = avgSpeed;
	}
	public double getAvgSpeed(){
		return this.avgSpeed;
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
