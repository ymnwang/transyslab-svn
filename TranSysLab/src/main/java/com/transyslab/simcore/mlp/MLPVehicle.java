package com.transyslab.simcore.mlp;

import java.util.HashMap;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.Vehicle;

public class MLPVehicle extends Vehicle{
	protected MLPVehicle trailing_;// upstream vehicle
	protected MLPVehicle leading_;// downstream vehicle
	protected MLPLane lane_;
	protected MLPSegment segment_;
	protected MLPLink link_;
	public int platoonCode;
	protected int VirtualType_;//0 for real veh; num>0 for virual veh with the connected vheID
	protected int buffer_;//lane changing cold down remain frames
	protected int speedLevel_;
	protected boolean CFState_;
	public boolean resemblance;
	public boolean stopFlag;
	public double newSpeed;
	public double newDis;
	protected int usage;
	//private boolean active_;
	
	protected static int[] vhcCounter_ = new int[Constants.THREAD_NUM];  	//在网车辆数统计
	
	public MLPVehicle(){
		trailing_ = null;
		leading_ = null;
		platoonCode = 0;
		stopFlag = false;
	}
	
	public static void setVehicleCounter(int vhcnum){
		HashMap<String, Integer> hm = MLPNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		vhcCounter_[threadid] = vhcnum;
	}
	
	public void reset(){
		
	}
	
	public void resetPlatoonCode(){
		platoonCode = getCode();
	}
	
	public void calState() {
		if (leading_ != null && distance_-leading_.distance_<=MLPParameter.CELL_RSP_LOWER) {
			CFState_ = true;
		}
		else {
			CFState_ = false;
		}
		speedLevel_ = 1;
	}
	
	public MLPVehicle getUpStreamVeh() {
		if (trailing_ != null) {
			return trailing_;
		}
		else {
			JointLane jointLane = link_.findJointLane(lane_);
			int p = jointLane.lanesCompose.indexOf(lane_) + 1;
			while (p<jointLane.lanesCompose.size()-1) {
				 if (!jointLane.lanesCompose.get(p).vehsOnLn.isEmpty()) 
					 return  jointLane.lanesCompose.get(p).getHead();
				p += 1;
			}
			return (MLPVehicle) null;
		}
	}
	
	public double Displacement(){
		return Math.max(0.0, segment_.endDSP - distance_);
	}
	
	public boolean checkGapAccept(MLPLane tarLane){
		boolean frontCheck = true;
		boolean backCheck = true;
		MLPVehicle frontVeh = null;
		MLPVehicle backVeh = link_.findJointLane(tarLane).getFirstVeh();//效率有待提高
		if (backVeh == null) {//该车道上没有车
			return true;//路段太短的情况下也会直接返回true，不合理。待修改。
		}
		else {
			//front = ((MLPSegment) link_.getEndSegment()).endDSP;			
			while (backVeh != null && backVeh.Displacement()>Displacement()){
				frontVeh = backVeh;
				backVeh = backVeh.trailing_;				
			}			
			if (frontVeh != null) 
				frontCheck = (frontVeh.Displacement() - frontVeh.getLength() - Displacement() >=
										MLPParameter.getInstance().minGap(currentSpeed_));
			if (backVeh!=null) 
				backCheck = (Displacement() - length_ - backVeh.Displacement() >=
										MLPParameter.getInstance().minGap(backVeh.currentSpeed_));
			return (frontCheck && backCheck);
		}
	}
	
	private double calDLC(int turning, double fDSP, double tDSP, double PlatoonCount){
		try {
			double [] s = sum(turning, segment_, fDSP, tDSP, new double []{0.0,0.0});
			return ((s[0] + 1.0) /s[1] - PlatoonCount/(tDSP - fDSP)) / link_.dynaFun.sdPara[2];
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
		}
		return 0.18;		
	}
	
	private double [] sum(int turning, MLPSegment seg, double f, double t, double [] count){
		//double [] answer = {0.0,0.0};		
		if (f - seg.startDSP > -0.001)	{
			if (t - seg.endDSP < 0.001) {
				double [] answer = new double [2];
				MLPLane tarlane = lane_.getAdjacent(turning).getSamePosLane(seg);
				if (tarlane == null || !tarlane.enterAllowed || !tarlane.checkLCAllowen((turning+1)%2)) {
					answer[0] = count[0] + (t-f)*link_.dynaFun.sdPara[2];
				}
				else {
					answer[0] = count[0] + tarlane.countVehWhere(f, t);
				}
				answer[1] = count[1] + (t-f);
				return answer;
			}
			else {
				return sum(turning, seg.getDnSegment(), seg.endDSP, t, 
									sum(turning, seg, f, seg.endDSP, count));
			}
		}
		else {
			if (t<=seg.endDSP) {
				return sum(turning, seg.getUpSegment(), f, seg.startDSP, 
									sum(turning, seg, seg.startDSP, t, count));
			}
			else{
				return sum(turning, seg.getDnSegment(), seg.endDSP, t, 
									sum(turning, seg.getUpSegment(), f, seg.startDSP, 
											sum(turning, seg, seg.startDSP, seg.endDSP, count)));
			}
		}
	}
	
	protected double calMLC(){
		double buff = MLPParameter.getInstance().getSegLenBuff();
		double len = segment_.getLength();
		if (len<=buff) {
			return 1.0;
		}
		else{
			return (Math.min(Displacement(), len-buff))/(len-buff);
		}
	}
	
	private double calH(int turning){
		int h = lane_.di - lane_.getAdjacent(turning).di;
		if (h==0){
			return 0;
		}
		else{
			if (h>0){
				return 1.0;
			}
			else {
				return -1.0;
			}
		}
	}
	
	public double calLCProbability(int turning, double fDSP, double tDSP, double PlatoonCount){
		double [] gamma = MLPParameter.getInstance().getLCPara();
		double u = gamma[0]*calH(turning)*calMLC() + gamma[1]*calDLC(turning, fDSP, tDSP, PlatoonCount);
		return Math.exp(u)/(1+Math.exp(u));
	}
	
	public void initInfo(int virType, MLPLink onLink, MLPSegment onSeg, MLPLane onLane){
		VirtualType_ = virType;
		link_ = onLink;
		segment_ = onSeg;
		lane_ = onLane;
	}
	
	public void init(int id, float len, float dis, float speed){
		 setCode(id);
		 type_ = 1;
		 length_ = len;
	     distance_ = dis;
	     currentSpeed_ = speed;
	 }
	
	public int updateMove() {
		if (buffer_ == 0 && VirtualType_>0) {
			lane_.removeVeh(this, true);
			return 1;
		}
		if (newDis < 0.0) 
			dealPassing();//Passing link or seg
		currentSpeed_ = (float) newSpeed;
		distance_ = (float) newDis;
		buffer_ = Math.max(0, buffer_-1);
		return 0;
	}
	
	public void dealPassing() {
		if (segment_.isEndSeg()) {//passing Link(暂时处理成到达
			lane_.removeVeh(this, true);
			return;
		}
		else {//passing Seg.
			lane_.passVeh2ConnDnLn(this);
			newDis = segment_.getLength() + newDis;
			if (newDis < 0.0) {
				dealPassing();
			}
		}
	}
	
	public void setNewState(double spd) {
		if (stopFlag) {
			newSpeed = 0.0;
			newDis = distance_;
			return;
		}
		if (leading_ != null) {
			double gap = leading_.Displacement() -leading_.getLength() - Displacement();
			double maxSpd = MLPParameter.getInstance().maxSpeed(gap);
			newSpeed = Math.min(spd,maxSpd);
		}
		else {
			newSpeed = spd;
		}
		newDis = distance() - newSpeed*SimulationClock.getInstance().getStepSize();
	}
	
	public void clearMLPProperties() {
		leading_ = null;
		trailing_ = null;
		lane_ = null;
		segment_ = null;
		link_ = null;
		platoonCode = 0;
		VirtualType_ = 0;
		buffer_ = 0;
		speedLevel_ = 0;
		CFState_ = false;
		resemblance = false;
		stopFlag = false;
		newSpeed = 0.0;
		newDis = 0.0;
		setCode(0);
		length_ = 0.0f;
		distance_ = 0.0f;
		currentSpeed_ = 0.0f;
	}
	
	public void updateUsage() {
		usage += 1;
	}
	
	public void updateLeadNTrail() {
		int p = lane_.vehsOnLn.indexOf(this);
		if (p==0) {
			//是lane上的第一辆车，先将前车为null
			leading_ = (MLPVehicle) null;
			//只要前方存在lane且允许通行，则一直取前方lane，直到前方lane上有车，此时取前方lane的最后一辆作为前车
			MLPLane thelane = lane_.connectedDnLane;
			while (thelane != null && thelane.enterAllowed) {
				if (!thelane.vehsOnLn.isEmpty()) {
					leading_ = thelane.vehsOnLn.getLast();
					break;
				}
				thelane = thelane.connectedDnLane;				
			}
		}
		else
			//非lane上第一辆车，可取index-1的车作为前车
			leading_ = lane_.vehsOnLn.get(p-1);
		if (p == lane_.vehsOnLn.size() - 1) {
			trailing_ = (MLPVehicle) null;
			MLPLane thelane = lane_.connectedUpLane;
			while (thelane != null && thelane.enterAllowed) {
				if (!thelane.vehsOnLn.isEmpty()) {
					trailing_ = thelane.vehsOnLn.getFirst();
					break;
				}
				thelane = thelane.connectedUpLane;				
			}
		}
		else 
			trailing_ = lane_.vehsOnLn.get(p+1);
	}
	/*public MLPVehicle getLateralLeading(MLPLane tarLN){		
	}
	
	public MLPVehicle getLaterallTrailing(){	
	}*/
	

}
