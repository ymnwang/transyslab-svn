package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.List;

import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.Lane;
import com.transyslab.roadnetwork.Segment;

import jogamp.graph.curve.tess.HEdge;

public class MLPLane extends Lane {
	private int lnPosNum_;
	public List<MLPVehicle> vehsOnLn;
	private MLPVehicle head_;
	private MLPVehicle tail_;
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
	public int di;
	
	public MLPLane(){
		lnPosNum_ = 0;
		vehsOnLn = new ArrayList<MLPVehicle>();
		lateralCutInAllowed = 0;
//		LfCutinAllowed = true;
//		RtCutinAllowed = true;
		enterAllowed = true;
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
		if (tail_ != null &&
			getLength() - tail_.distance() < 
			(mlpv.getLength() +MLPParameter.getInstance().minGap(mlpv.currentSpeed()))) {
			return false;
		}
		else
			return true;
	}
	
	public boolean checkVolum(double vehLen, double vehSpeed) {
		if (tail_ != null &&
			getLength() - tail_.distance() < 
			(vehLen +  MLPParameter.getInstance().minGap(vehSpeed))) {
			return false;
		}
		else
			return true;
	}
	
	public void appendVeh(MLPVehicle mlpveh) {
		if (vehsOnLn.isEmpty()) {
			head_ = mlpveh;
		}
		else {
			tail_.trailing_ = mlpveh;
			mlpveh.leading_ = tail_;
		}
		vehsOnLn.add(mlpveh);
		tail_ = mlpveh;			
	}
	
	public void insertVeh(MLPVehicle mlpveh){		
		if (vehsOnLn.isEmpty()){
			MLPLane theDnLane = getSamePosLane(segment_.getDnSegment());
			if (theDnLane != null && !theDnLane.vehsOnLn.isEmpty()) {
				mlpveh.leading_ = theDnLane.getTail();
				theDnLane.getTail().trailing_ = mlpveh;
			}
			else {
				mlpveh.leading_ = null;
			}
			MLPLane theUpLane = getSamePosLane(segment_.getUpSegment());
			if (theUpLane != null && !theUpLane.vehsOnLn.isEmpty()) {
				mlpveh.trailing_ = theUpLane.getHead();
				theUpLane.getHead().leading_ = mlpveh;
			}
			else {
				mlpveh.trailing_ = null;
			}
			head_ = mlpveh;
			tail_ = mlpveh;
		}
		else {
			if (head_.Displacement()<mlpveh.Displacement()){
				mlpveh.trailing_ = head_;
				mlpveh.leading_ = head_.leading_;
				head_.leading_ = mlpveh;
				if (mlpveh.leading_ != null) {
					mlpveh.leading_.trailing_ = mlpveh;
				}				
				head_ = mlpveh;
			}
			else {
				MLPVehicle tmp = head_;
				while(tmp!=tail_){
					if (tmp.trailing_.Displacement()<mlpveh.Displacement()) {
						mlpveh.leading_ = tmp;
						mlpveh.trailing_ = tmp.trailing_;
						tmp.trailing_ = mlpveh;
						mlpveh.trailing_.leading_ = mlpveh;
						break;
					}					
					tmp = tmp.trailing_;					
				}
				if (tmp==tail_) {//队伍最后
					mlpveh.leading_ = tail_;
					mlpveh.trailing_ = tail_.trailing_;
					tail_.trailing_ = mlpveh;
					if (mlpveh.trailing_ != null) {
						mlpveh.trailing_.leading_ = mlpveh;
					}
					tail_ = mlpveh;
				}
			}			
		}
		vehsOnLn.add(mlpveh);
	}
	
	public void substitudeVeh(MLPVehicle veh, MLPVehicle newVeh){
		if (veh.trailing_ != null) {
			newVeh.trailing_ = veh.trailing_;
			veh.trailing_ .leading_= newVeh; 
		}
		if (veh.leading_  != null) {
			newVeh.leading_ = veh.leading_;
			veh.leading_.trailing_ = newVeh;			
		}
		if (head_==veh) {
			head_ = newVeh;
		}
		if (tail_ == veh) {
			tail_ = newVeh;
		}
		vehsOnLn.remove(veh);
		vehsOnLn.add(newVeh);
	}
	
	public void removeVeh(MLPVehicle mlpveh){
		vehsOnLn.remove(mlpveh);
		if (mlpveh.leading_ != null) {
			mlpveh.leading_.trailing_ = mlpveh.trailing_;
		}
		if (mlpveh.trailing_ != null) {
			mlpveh.trailing_.leading_ = mlpveh.leading_;
		}
		if (vehsOnLn.isEmpty()) {
			head_ = null;
			tail_ = null;
		}
		else {
			if (head_.getCode() == mlpveh.getCode()) {
				head_ = mlpveh.trailing_;
			}
			else if (tail_.getCode() == mlpveh.getCode()) {
				tail_ = mlpveh.leading_;
			}
		}
			
	}
	
	public void passVeh2ConnDnLn(MLPVehicle theVeh) {
		if (connectedDnLane == null) {
			System.err.println("no connected downstream lane");
		}
		vehsOnLn.remove(theVeh);
		head_ = theVeh.trailing_;
		connectedDnLane.vehsOnLn.add(theVeh);
		connectedDnLane.tail_ = theVeh;
	}
	
	public MLPVehicle getHead() {
		return head_;
	}
	
	public MLPVehicle getTail(){
		return tail_;
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
}
