package com.transyslab.simcore.mlp;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Vehicle;

public class MLPVehicle extends Vehicle{
	protected MLPVehicle trailing_;// upstream vehicle
	protected MLPVehicle leading_;// downstream vehicle
	protected MLPLane lane_;
	protected MLPSegment segment_;
	protected MLPLink link_;
	protected int platoonCode;
	protected int VirtualType_;//0 for real veh; num>0 for virual veh with the connected vheID
	protected int buffer_;//lane changing cold down remain frames
	protected int speedLevel_;
	protected boolean CFState_;
	protected boolean resemblance;
	protected boolean stopFlag;
	protected double newSpeed;
	protected double newDis;
	protected int usage;
//	public double TimeEntrance;
	protected double DSPEntrance;
	protected int RVID;
	protected double time2Dispatch;
//	static public TXTUtils fout = new TXTUtils("src/main/resources/output/test.csv");
//	protected double TimeExit;
	//private boolean active_;
	
	
	public MLPVehicle(){
		trailing_ = null;
		leading_ = null;
		platoonCode = 0;
		stopFlag = false;
	}
	
	@Override
	public MLPLink getLink() {
		return link_;
	}
	
	public MLPSegment getSegment(){
		return segment_;
	}
	public MLPLane getLane(){
		return lane_;
	}
	public void reset(){
		
	}
	
	public void resetPlatoonCode(){
		platoonCode = getCode();
	}
	
	public void calState() {
		if (leading_ != null && distance_-leading_.distance_<=MLPParameter.getInstance().CELL_RSP_LOWER) {
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
		MLPVehicle backVeh = link_.findJointLane(tarLane).getFirstVeh();//Ч���д����
		if (backVeh == null) {//�ó�����û�г�
			return true;//·��̫�̵������Ҳ��ֱ�ӷ���true�����������޸ġ�
		}
		else {
			//front = ((MLPSegment) link_.getEndSegment()).endDSP;			
			while (backVeh != null && backVeh.Displacement()>Displacement()){
				frontVeh = backVeh;
				backVeh = backVeh.trailing_;				
			}			
			if (frontVeh != null) 
				frontCheck = (frontVeh.Displacement() - frontVeh.getLength() - Displacement() >=
										MLPParameter.getInstance().minGap(currentSpeed_));//currentSpeed_
			if (backVeh!=null) 
				backCheck = (Displacement() - length_ - backVeh.Displacement() >=
										MLPParameter.getInstance().minGap(backVeh.currentSpeed_));//backVeh.currentSpeed_
			return (frontCheck && backCheck);
		}
	}
	
	private double calDLC(int turning, double fDSP, double tDSP, double PlatoonCount){
		try {
			double [] s = sum(turning, segment_, fDSP, tDSP, new double []{0.0,0.0});
			return (PlatoonCount/(tDSP - fDSP) - (s[0] + 1.0) /s[1]) / link_.dynaFun.sdPara[2];
		} catch (Exception e) {
			e.printStackTrace();
		}
		//failed	
		return 0.0;		
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
		int h = lane_.calDi(this) - lane_.getAdjacent(turning).calDi(this);
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
		double lambda1 = MLPParameter.LC_Lambda1;
		double lambda2 = MLPParameter.LC_Lambda2;
		double h = calH(turning);
		double Umlc = calMLC();
		double Udlc = calDLC(turning, fDSP, tDSP, PlatoonCount);
		double W = gamma[0]*h*Umlc + gamma[1]*Udlc;// - (gamma[0] + gamma[1])*0.5
		double U = lambda1*W + lambda2;
		double pr = Math.exp(U)/(1+Math.exp(U));
//		fout.writeNFlush(u + "," + pr + "\r\n");
		return pr;
	}
	
	public void initEntrance(double time, double dsp) {
		departTime_ = (float) time;
		timeEntersLink_ = (float) time;
		DSPEntrance = dsp;
	}
	
	public void initInfo(int virType, MLPLink onLink, MLPSegment onSeg, MLPLane onLane, int rvid){
		VirtualType_ = virType;
		link_ = onLink;
		segment_ = onSeg;
		lane_ = onLane;
		RVID = rvid;
	}
	
	public void init(int id, float len, float dis, float speed){
		 setCode(id);
		 type_ = 1;
		 length_ = len;
	     distance_ = dis;
	     currentSpeed_ = speed;
	 }
	
	public int updateMove() {
//		if ( Math.abs(distance_ - newDis - newSpeed*SimulationClock.getInstance().getStepSize()) > 0.001 )
//			System.out.println("BUG δ�ڱ�����֡�ڴ���˳�");
		if (VirtualType_>0 && buffer_ == 0) {
			lane_.removeVeh(this, true);
			return 1;
		}
		if (newDis < 0.0) 
			return dealPassing();//Passing link or seg
		return 0;
	}
	
	public void advance() {
		currentSpeed_ = (float) newSpeed;
		distance_ = (float) newDis;
		buffer_ = Math.max(0, buffer_-1);
	}
	
	public int dealPassing() {
		if (segment_.isEndSeg()) {
			if (VirtualType_ != 0) {
				//�鳵���Ӱ�쵽Linkĩ��
//				lane_.removeVeh(this, true);
//				return 1;
				holdAtDnEnd();
				return 0;
			}
			MLPNode server = (MLPNode) link_.getDnNode();
			return server.serve(this);
		}
		else {//deal passing Seg.
			/*if (lane_.checkPass()) {
				lane_.passVeh2ConnDnLn(this);
				newDis = segment_.getLength() + newDis;
				if (newDis < 0.0) {
					dealPassing();
				}
			}
			else {//hold in this seg
				newDis = 0.0;
				if (currentSpeed_>0.0) 
					newSpeed = (distance_-newDis)/SimulationClock.getInstance().getStepSize();
			}*/
			if (lane_.connectedDnLane == null) {//has no successive lane
				if (VirtualType_ == 0){
					System.err.println("Vehicle No. " + getCode() +" has no successive lane to go");
					holdAtDnEnd();
				}
				else
					lane_.removeVeh(this, true);//����鳵������������successive lane�����ã�������ʧ
				return 0;
			}
			lane_.passVeh2ConnDnLn(this);
			newDis = segment_.getLength() + newDis;
			if (newDis < 0.0) {
				dealPassing();
			}
			return 0;
		}
	}
	
	protected void holdAtDnEnd() {
		newDis = 0.0;
		if (currentSpeed_>0.0) 
			newSpeed = (distance_-newDis)/SimulationClock.getInstance().getStepSize();
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
		type_ = 0;
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
		departTime_ = 0.0f;
		timeEntersLink_ = 0.0f;
		DSPEntrance = 0.0;
		RVID = 0;
		time2Dispatch = 0.0;
		donePathIndex();
//		TimeExit = 0.0;
	}
	
	public void updateUsage() {
		usage += 1;
	}
	
	public void updateLeadNTrail() {
		int p = lane_.vehsOnLn.indexOf(this);
		if (p==0) {
			//��lane�ϵĵ�һ�������Ƚ�ǰ��Ϊnull
			leading_ = (MLPVehicle) null;
			//ֻҪǰ������lane������ͨ�У���һֱȡǰ��lane��ֱ��ǰ��lane���г�����ʱȡǰ��lane�����һ����Ϊǰ��
//			MLPLane thelane = lane_.connectedDnLane;
//			while (thelane != null && thelane.enterAllowed) {
//				if (!thelane.vehsOnLn.isEmpty()) {
//					leading_ = thelane.vehsOnLn.getLast();
//					break;
//				}
//				thelane = thelane.connectedDnLane;
//			}
			MLPSegment theSeg = segment_;
			MLPLane theLN = lane_;
			while (!theSeg.isEndSeg() && theLN.successiveDnLanes.size()==1){
				//�������LN�����ƽ�
				theLN = theLN.successiveDnLanes.get(0);
				theSeg = theLN.getSegment();
				if (!theLN.vehsOnLn.isEmpty()) {
					leading_ = theLN.vehsOnLn.getLast();
					break;
				}
			}
		}
		else
			//��lane�ϵ�һ��������ȡindex-1�ĳ���Ϊǰ��
			leading_ = lane_.vehsOnLn.get(p-1);
		if (p == lane_.vehsOnLn.size() - 1) {
			trailing_ = (MLPVehicle) null;
//			MLPLane thelane = lane_.connectedUpLane;
//			while (thelane != null && thelane.enterAllowed) {
//				if (!thelane.vehsOnLn.isEmpty()) {
//					trailing_ = thelane.vehsOnLn.getFirst();
//					break;
//				}
//				thelane = thelane.connectedUpLane;
//			}
			MLPSegment theSeg = segment_;
			MLPLane theLN = lane_;
			while (!theSeg.isStartSeg() && theLN.successiveUpLanes.size()==1){
				//�������LN�����ƽ�
				theLN = theLN.successiveUpLanes.get(0);
				theSeg = theLN.getSegment();
				if (!theLN.vehsOnLn.isEmpty()) {
					trailing_ = theLN.vehsOnLn.getFirst();
					break;
				}
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
