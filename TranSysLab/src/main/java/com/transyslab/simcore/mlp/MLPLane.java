package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.Lane;
import com.transyslab.roadnetwork.Segment;

import jogamp.graph.curve.tess.HEdge;

public class MLPLane extends Lane implements Comparator<MLPLane>{
	private double capacity_;
	private double releaseTime_;
	private int lnPosNum_;
	public LinkedList<MLPVehicle> vehsOnLn;
//	private MLPVehicle head_;
//	private MLPVehicle tail_;
//	private double emitTime_;
	//private double capacity_ = 0.5;
//	private MLPLane upConectLane_;
//	private MLPLane dnConectLane_;
	public int lateralCutInAllowed; // ʮλ����0(1)��ʾ(��)������೵�����ߣ���λ����0(1)��ʾ(��)�����Ҳ೵������
//	public boolean LfCutinAllowed;	//left cut in allowed = true=������೵���������˳�����
//	public boolean RtCutinAllowed;	//left cut in allowed = true=������೵���������˳�����
	public boolean enterAllowed;	//true=�����󷽣���������ʻ��;false=������������ʻ��(���ڵ�·���)
	public MLPLane connectedDnLane;
	public MLPLane connectedUpLane;
	public int di;
	protected List<MLPLane> sucessiveLanes;
	
	public MLPLane(){
		capacity_ = MLPParameter.getInstance().capacity;
		lnPosNum_ = 0;
		vehsOnLn = new LinkedList<MLPVehicle>();
		lateralCutInAllowed = 0;
//		LfCutinAllowed = true;
//		RtCutinAllowed = true;
		enterAllowed = true;
		sucessiveLanes = new ArrayList<>();
	}
	
	public boolean checkPass() {
		if (releaseTime_<=SimulationClock.getInstance().getCurrentTime()) 
			return true;
		else 
			return false;
	}
	
	public void resetReleaseTime() {
		releaseTime_ = SimulationClock.getInstance().getCurrentTime();
		scheduleNextEmitTime();
	}
	
	public void scheduleNextEmitTime() {
		if (capacity_ > 1.E-6) {
			releaseTime_ += 1.0 / capacity_;
		}
		else {
			releaseTime_ = Constants.DBL_INF;
		}
	}
	
	public void setCapacity(double val) {
		capacity_ = val;
	}
	
	public void calLnPos() {
		lnPosNum_ = getCode()%10;
	}
	
	public int getLnPosNum(){
		return lnPosNum_;
	}
	
	@Override
	public MLPSegment getSegment(){
		return (MLPSegment) segment_;
	}
	
	public boolean checkVolum(MLPVehicle mlpv) {
		MLPVehicle tail_ = getTail();
		if (tail_ != null &&
			getLength() - tail_.distance() < 
			(mlpv.getLength() +MLPParameter.getInstance().minGap(mlpv.currentSpeed()))) {
			return false;
		}
		else
			return true;
	}
	
	public boolean checkVolum(double vehLen, double vehSpeed) {
		MLPVehicle tail_ = getTail();
		if (tail_ != null &&
			getLength() - tail_.distance() < 
			(vehLen +  MLPParameter.getInstance().minGap(vehSpeed))) {
			return false;
		}
		else
			return true;
	}
	
	//appendVeh(); removeVeh(); insertVeh(); check list:
	//1. ���lane.vehsOnLn�Ĺ��ظ���
	//2. ��س����ĸ�����ϵ�ĸ���
	//3. Network.veh_list & veh_pool recycle�Ĺ��ظ���
	//4. ת�Ƴ���(processing veh)��lane, seg, link�Ĺ��ع�ϵ����	
	//�Ƽ�����
	//S1 Network.veh_list.add();
	//S2 lane.vehsOnLn.add() & remove();
	//S3 processingVeh.updateLeadNTrail() 
	//     -> if lead or trail not null, lead.updateLeadNTrail() & trail.updateLeadNTrail()
	//S4 veh_pool.recycle() 
	//     -> if no need, processingVeh.lane/seg/link setting
	public void appendVeh(MLPVehicle mlpveh) {
		//�������lane��vehsOnLn & network.veh_list generate
		//network.veh_list has been taken care before called
		vehsOnLn.offer(mlpveh);		
		//�������veh��lead_&trail_
		mlpveh.updateLeadNTrail();
		if (mlpveh.leading_ != null) 
			mlpveh.leading_.updateLeadNTrail();
		if (mlpveh.trailing_ != null)
			mlpveh.trailing_.updateLeadNTrail();
		//processing veh��lane, seg, link��ע�� Ҳ��Ҫ�ڵ��ñ�����֮ǰ���
	}
	
	public void removeVeh(MLPVehicle mlpveh, boolean recycleNeeded){
		//�������lane��vehsOnLn
		vehsOnLn.remove(mlpveh);
		//�������veh���veh��lead_&trail_
		if (mlpveh.leading_ != null) 
			mlpveh.leading_.updateLeadNTrail();
		if (mlpveh.trailing_ != null)
			mlpveh.trailing_.updateLeadNTrail();
		if (recycleNeeded)
			//����network.veh_list recycle; 
			MLPNetwork.getInstance().veh_pool.recycle(mlpveh);
		else {
			//������Ҫ���գ������processing Veh�ĸ������¼�����ע��
			mlpveh.leading_ = (MLPVehicle) null;
			mlpveh.trailing_ = (MLPVehicle) null;
			mlpveh.lane_ = null;
			mlpveh.segment_ = null;
			mlpveh.link_ = null;
		}
	}
	
	public void insertVeh(MLPVehicle mlpveh) {
		//�ҵ�����ڵ㲢��vehsOnLn�ϲ���
		if (vehsOnLn.isEmpty()) {
			vehsOnLn.offer(mlpveh);
		}
		else {
			int p = 0;
			while (p<vehsOnLn.size() && vehsOnLn.get(p).distance() < mlpveh.distance()) {
				p += 1;
			}
			vehsOnLn.add(p, mlpveh);
		}
		//processingVeh.lane/seg/link setting
		mlpveh.lane_ = this;
		mlpveh.segment_ = (MLPSegment) segment_;
		mlpveh.link_ = (MLPLink) segment_.getLink();
		//updateLeadNTrail()
		mlpveh.updateLeadNTrail();
		if (mlpveh.leading_ != null) 
			mlpveh.leading_.updateLeadNTrail();
		if (mlpveh.trailing_ != null) 
			mlpveh.trailing_.updateLeadNTrail();		
	}
	
	public void insertVeh(MLPVehicle mlpveh, int p) {
		//��vehsOnLn�ϲ���
		vehsOnLn.add(p, mlpveh);
		//updateLeadNTrail()
		mlpveh.updateLeadNTrail();
		if (mlpveh.leading_ != null) 
			mlpveh.leading_.updateLeadNTrail();
		if (mlpveh.trailing_ != null) 
			mlpveh.trailing_.updateLeadNTrail();
		//processingVeh.lane/seg/link setting
		mlpveh.lane_ = this;
		mlpveh.segment_ = (MLPSegment) segment_;
		mlpveh.link_ = (MLPLink) segment_.getLink();
	}

	public void substitudeVeh(MLPVehicle rmVeh, MLPVehicle newVeh){
		/*if (!vehsOnLn.contains(rmVeh)) {
			System.err.println("err: rmVeh is not on this lane");
			return;
		}*/
		int p_ = vehsOnLn.indexOf(rmVeh);
		removeVeh(rmVeh, false);
		insertVeh(newVeh, p_);
	}
	
	public void passVeh2ConnDnLn(MLPVehicle theVeh) {
		if (connectedDnLane == null) {
			System.err.println("no connected downstream lane");
		}
		//S2
		vehsOnLn.remove(theVeh);
		connectedDnLane.vehsOnLn.offer(theVeh);
		//S3 NONEED
		//S4 
		theVeh.lane_ = connectedDnLane;
		theVeh.segment_ = (MLPSegment) segment_.getDnSegment();
	}
	
	public MLPVehicle getHead() {
		if (vehsOnLn.isEmpty()) 
			return (MLPVehicle) null;
		else 
			return vehsOnLn.getFirst();
	}
	
	public MLPVehicle getTail(){
		if (vehsOnLn.isEmpty()) 
			return (MLPVehicle) null;
		else
			return vehsOnLn.getLast();
	}
	
	public MLPLane getAdjacent(int dir){
		if (dir ==0){
			return (MLPLane) getRightLane();
		}
		else {
			if (dir == 1) {
				return (MLPLane) getLeftLane();
			}
			else {
				return (MLPLane) null;
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
		connectedUpLane = getSamePosLane(segment_.getUpSegment());
		connectedDnLane = getSamePosLane(segment_.getDnSegment());
	}
	public MLPLane getSamePosLane(Segment seg) {
		if (seg != null && seg.nLanes()>=lnPosNum_) {
			return (MLPLane) seg.getLane(lnPosNum_ - 1);
		}
		else{
			return (MLPLane) null;
		}
	}
	
	public int countVehWhere(double fdsp, double tdsp){
		if (vehsOnLn.isEmpty()) {
			return 0;
		}
		else {
			int c = 0;
			for (MLPVehicle veh : vehsOnLn) {
				if (veh.Displacement()>fdsp && veh.Displacement()<=tdsp)
					c += 1;
			}
			return c;
		}
	}
	
	public void calDi() {
		if (((MLPSegment) segment_).isEndSeg()) {
			di = 0;
			return;
		}
		if (connectedDnLane.enterAllowed) {
			di = 0;
		}
		else {
			MLPLane tmp = (MLPLane) getLeftLane();
			int count1 = 1;
			while(tmp != null){
				if (tmp.connectedDnLane.enterAllowed) {
					break;
				}
				count1 += 1;
				tmp = (MLPLane) tmp.getLeftLane();
			}
			tmp = (MLPLane) getRightLane();
			int count2 = 1;
			while (tmp != null) {
				if (tmp.connectedDnLane.enterAllowed) {
					break;
				}
				count2 += 1;
				tmp = (MLPLane) tmp.getRightLane();
			}
			di = Math.max(count1, count2);
		}
	}
	
	public List<MLPLane> selectDnLane(Segment nextSeg) {
		List<MLPLane> tmp = new ArrayList<>();
		for (int i = 0; i < nextSeg.nLanes(); i++) {
			MLPLane theLane = (MLPLane) nextSeg.getLane(i);
			if (dnLanes_.contains(theLane)) {
				tmp.add(theLane);
			}
		}
		tmp.sort(this);
		return tmp;
	}

	@Override
	public int compare(MLPLane o1, MLPLane o2) {
		MLPLane tmp = null;
		for (int i = 0; i < sucessiveLanes.size(); i++) {
			tmp = sucessiveLanes.get(i);
			if (tmp.segment_.getCode() == o1.segment_.getCode()) {
				break;
			}
		}
		if (tmp == null) 
			return 0;
		int d1 = Math.abs(tmp.lnPosNum_ - lnPosNum_);
		int d2 = Math.abs(tmp.lnPosNum_ - lnPosNum_);
		return ( d1 - d2 );
	}
}
