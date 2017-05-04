package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.Lane;
import com.transyslab.roadnetwork.Segment;

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
	public int lateralCutInAllowed; // 十位数字0(1)表示(不)允许左侧车道并线；个位数字0(1)表示(不)允许右侧车道并线
//	public boolean LfCutinAllowed;	//left cut in allowed = true=允许左侧车道换道到此车道，
//	public boolean RtCutinAllowed;	//left cut in allowed = true=允许左侧车道换道到此车道，
	public boolean enterAllowed;	//true=允许（后方）车道车辆驶入;false=不允许车道车辆驶入(等于道路封闭)
	public MLPLane connectedDnLane;
	public MLPLane connectedUpLane;
//	public int di;//弃用
	protected List<MLPLane> successiveDnLanes;
	protected List<MLPLane> successiveUpLanes;
	
	public MLPLane(){
		capacity_ = MLPParameter.getInstance().capacity;
		lnPosNum_ = 0;
		vehsOnLn = new LinkedList<MLPVehicle>();
		lateralCutInAllowed = 0;
//		LfCutinAllowed = true;
//		RtCutinAllowed = true;
		enterAllowed = true;
		successiveDnLanes = new ArrayList<>();
		successiveUpLanes = new ArrayList<>();
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
	//1. 相关lane.vehsOnLn的挂载更新
	//2. 相关车辆的跟车关系的更新
	//3. Network.veh_list & veh_pool recycle的挂载更新
	//4. 转移车辆(processing veh)的lane, seg, link的挂载关系更新	
	//推荐流程
	//S1 Network.veh_list.add();
	//S2 lane.vehsOnLn.add() & remove();
	//S3 processingVeh.updateLeadNTrail() 
	//     -> if lead or trail not null, lead.updateLeadNTrail() & trail.updateLeadNTrail()
	//S4 veh_pool.recycle() 
	//     -> if no need, processingVeh.lane/seg/link setting
	public void appendVeh(MLPVehicle mlpveh) {
		//处理相关lane的vehsOnLn & network.veh_list generate
		//network.veh_list has been taken care before called
		vehsOnLn.offer(mlpveh);		
		//处理相关veh的lead_&trail_
		mlpveh.updateLeadNTrail();
		if (mlpveh.leading_ != null) 
			mlpveh.leading_.updateLeadNTrail();
		if (mlpveh.trailing_ != null)
			mlpveh.trailing_.updateLeadNTrail();
		//processing veh的lane, seg, link的注册 也需要在调用本函数之前完成
	}
	
	public void removeVeh(MLPVehicle mlpveh, boolean recycleNeeded){
		//处理相关lane的vehsOnLn
		vehsOnLn.remove(mlpveh);
		//处理与此veh相关veh的lead_&trail_
		if (mlpveh.leading_ != null) 
			mlpveh.leading_.updateLeadNTrail();
		if (mlpveh.trailing_ != null)
			mlpveh.trailing_.updateLeadNTrail();
		if (recycleNeeded)
			//处理network.veh_list recycle; 
			MLPNetwork.getInstance().veh_pool.recycle(mlpveh);
		else {
			//若不需要回收，则完成processing Veh的跟车更新及挂载注册
			mlpveh.leading_ = (MLPVehicle) null;
			mlpveh.trailing_ = (MLPVehicle) null;
			mlpveh.lane_ = null;
			mlpveh.segment_ = null;
			mlpveh.link_ = null;
		}
	}
	
	public void insertVeh(MLPVehicle mlpveh) {
		//找到插入节点并在vehsOnLn上插入
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
		//在vehsOnLn上插入
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
		if (dir == 0){
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

	private boolean connect2DnLanes(List<MLPLane> DnLanes) {
		for (MLPLane tmpLN: DnLanes){
			if (tmpLN.upLanes_.contains(this))
				return true;
		}
		return false;
	}

	protected MLPLane successiveDnLaneInLink(MLPLink arg) {
		for (MLPLane ln : successiveDnLanes) {
			if (ln.getLink().getCode() == arg.getCode())
				return ln;
		}
		return null;
	}
	
	public int calDi(MLPVehicle theVeh) {
		//last seg of this link
		if (((MLPSegment) segment_).isEndSeg()) {
			MLPLink nextLink = (MLPLink) theVeh.nextLink();

			//on the last link
			if (nextLink == null){
				return 0;
			}

			//this lane connects with next link; find out if nextNode is an intersection

			//next node is an intersection
			if (getLink().getDnNode().type(Constants.NODE_TYPE_INTERSECTION)!=0) {
				List<MLPLane> nextValidLanes = ((MLPSegment) nextLink.getStartSegment()).getValidLanes(theVeh);
				if (connect2DnLanes(nextValidLanes)) {
					return 0;
				}
				//check neighbor lane
				MLPLane tmpLN = (MLPLane) getLeft();
				int count1 = 1;
				while(tmpLN != null && !tmpLN.connect2DnLanes(nextValidLanes)) {
					tmpLN = (MLPLane) tmpLN.getLeft();
					count1 += 1;
				}
				if (index_ - count1 < segment_.getLeftLaneIndex())
					count1 = Integer.MAX_VALUE;
				tmpLN = (MLPLane) getRight();
				int count2 = 1;
				while(tmpLN != null && !tmpLN.connect2DnLanes(nextValidLanes)) {
					tmpLN = (MLPLane) tmpLN.getRight();
					count2 += 1;
				}
				if (index_ + count2 > segment_.getLeftLaneIndex() + segment_.nLanes() - 1)
					count2 = Integer.MAX_VALUE;
				return Math.min(count1, count2);
			}

			//next node is NOT an intersection
			List<MLPLane> nextValidLanes = ((MLPSegment) nextLink.getStartSegment()).getValidLanes(theVeh);
			MLPLane theSuDnLane = successiveDnLaneInLink(nextLink);
			if (nextValidLanes.contains(theSuDnLane))
				return 0;
			int count = Integer.MAX_VALUE;
			for (int i = 0; i < segment_.nLanes(); i++) {
				MLPLane tmpLN = (MLPLane) segment_.getLane(i);
				if ( tmpLN != this && nextValidLanes.contains(tmpLN.successiveDnLaneInLink(nextLink)) ) {
					int tmp = Math.abs(tmpLN.getLnPosNum() - lnPosNum_);
					count = tmp<=count ? tmp : count;
				}
			}
			return count;
		}

		//break point between Segments: (within the link)
		if (connectedDnLane!=null && connectedDnLane.enterAllowed) {
			return 0;
		}
		MLPLane tmp = (MLPLane) getLeftLane();
		int count1 = 1;
		while(tmp != null){
			if (tmp.connectedDnLane!=null && tmp.connectedDnLane.enterAllowed) {
				break;
			}
			count1 += 1;
			tmp = (MLPLane) tmp.getLeftLane();
		}
		if (index_ - count1 < segment_.getLeftLaneIndex())
			count1 = Integer.MAX_VALUE;
		tmp = (MLPLane) getRightLane();
		int count2 = 1;
		while (tmp != null) {
			if (tmp.connectedDnLane!=null && tmp.connectedDnLane.enterAllowed) {
				break;
			}
			count2 += 1;
			tmp = (MLPLane) tmp.getRightLane();
		}
		if (index_ + count2 > segment_.getLeftLaneIndex() + segment_.nLanes() - 1)
			count2 = Integer.MAX_VALUE;
		return Math.min(count1, count2);
	}

	public boolean diEqualsZero(MLPVehicle theVeh){
		if (((MLPSegment) segment_).isEndSeg()) {//last seg of this link
			MLPLink nextLink = (MLPLink) theVeh.nextLink();
			return ( nextLink == null ||
					 connect2DnLanes( ( (MLPSegment) nextLink.getStartSegment() ).getValidLanes(theVeh) ) );
		}
		return (connectedDnLane!=null && connectedDnLane.enterAllowed);
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
		for (int i = 0; i < successiveDnLanes.size(); i++) {
			tmp = successiveDnLanes.get(i);
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
