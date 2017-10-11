package com.transyslab.simcore.mlp;

import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.Vehicle;

import java.util.HashMap;

public class MLPVehicle extends Vehicle{
	protected MLPVehicle trailing;// upstream vehicle
	protected MLPVehicle leading;// downstream vehicle
	protected MLPLane lane;
	protected MLPSegment segment;
	protected MLPLink link;
	protected int platoonCode;
	protected int virtualType;//0 for real veh; num>0 for virual veh with the connected vheID
	protected int buffer;//lane changing cold down remain frames
	protected int speedLevel;
	protected boolean cfState;
	protected boolean resemblance;
	protected boolean stopFlag;
	protected double newSpeed;
	protected double newDis;
	protected int usage;
//	public double TimeEntrance;
	protected double dspLinkEntrance;
	protected int rvId;
	protected double time2Dispatch;
	protected HashMap<MLPLane, Integer> diMap;
//	static public TXTUtils fout = new TXTUtils("src/main/resources/output/test.csv");
//	protected double TimeExit;
	//private boolean active_;

	//wym !!parameter在回收过程中不需要重置
	protected MLPParameter mlpParameter;
	
	
	public MLPVehicle(MLPParameter theParameter){
		trailing = null;
		leading = null;
		platoonCode = 0;
		stopFlag = false;
		mlpParameter = theParameter;
		diMap = new HashMap<>();
	}

	public MLPNetwork getMLPNetwork() {
		return (MLPNetwork) link.getNetwork();
	}
	
	@Override
	public MLPLink getLink() {
		return link;
	}
	
	public MLPSegment getSegment(){
		return segment;
	}
	public MLPLane getLane(){
		return lane;
	}
	public void reset(){
		
	}
	
	public void resetPlatoonCode(){
		platoonCode = getId();
	}
	
	public void calState() {
		if (leading != null && leading.Displacement() - Displacement() <= mlpParameter.CELL_RSP_LOWER) {
			cfState = true;
		}
		else {
			cfState = false;
		}
		speedLevel = 1;
	}
	
	public MLPVehicle getUpStreamVeh() {
		if (trailing != null) {
			return trailing;
		}
		else {
			JointLane jointLane = link.findJointLane(lane);
			int p = jointLane.lanesCompose.indexOf(lane) + 1;
			while (p<jointLane.lanesCompose.size()-1) {
				 if (!jointLane.lanesCompose.get(p).vehsOnLn.isEmpty()) 
					 return  jointLane.lanesCompose.get(p).getHead();
				p += 1;
			}
			return  null;
		}
	}
	
	public double Displacement(){
		return Math.max(0.0, segment.endDSP - distance);
	}
	
	public boolean checkGapAccept(MLPLane tarLane){
		boolean frontCheck = true;
		boolean backCheck = true;
		MLPVehicle frontVeh = null;
		MLPVehicle backVeh = link.findJointLane(tarLane).getFirstVeh();//效率有待提高
		if (backVeh == null) {//该车道上没有车
			return true;//TODO: 路段太短的情况下也会直接返回true，不合理。待修改。
		}
		else {
			//front = ((MLPSegment) link.getEndSegment()).endDSP;
			while (backVeh != null && backVeh.Displacement()>Displacement()){
				frontVeh = backVeh;
				backVeh = backVeh.trailing;
			}			
			if (frontVeh != null) 
				frontCheck = (frontVeh.Displacement() - frontVeh.getLength() - Displacement() >=
										mlpParameter.minGap(currentSpeed));//getCurrentSpeed
			if (backVeh!=null) 
				backCheck = (Displacement() - length - backVeh.Displacement() >=
										mlpParameter.minGap(backVeh.currentSpeed));//backVeh.getCurrentSpeed
			return (frontCheck && backCheck);
		}
	}
	
	private double calDLC(int turning, double fDSP, double tDSP, double PlatoonCount){
		try {
			double [] s = sum(turning, segment, fDSP, tDSP, new double []{0.0,0.0});
			return (PlatoonCount/(tDSP - fDSP) - (s[0] + 1.0) /s[1]) / link.dynaFun.sdPara[2];
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
				MLPLane tarlane = lane.getAdjacent(turning).getSamePosLane(seg);
				if (tarlane == null || !tarlane.enterAllowed || !tarlane.checkLCAllowen((turning+1)%2)) {
					answer[0] = count[0] + (t-f)* link.dynaFun.sdPara[2];
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
		double buff = mlpParameter.getSegLenBuff();
		double len = segment.getLength();
		if (len<=buff) {
			return 1.0;
		}
		else{
			return (Math.min(Displacement(), len-buff))/(len-buff);
		}
	}
	
	private double calH(int turning){
//		int h = lane.calDi(this) - lane.getAdjacent(turning).calDi(this);//旧方法 重复计算
		int h = diMap.get(lane) - diMap.get(lane.getAdjacent(turning));
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
		double [] gamma = mlpParameter.getLCPara();
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
	
	public void initNetworkEntrance(double time, double dsp) {
		departTime = (float) time;
		initLinkEntrance(time,dsp);
	}

	public void initLinkEntrance(double time, double dsp) {
		timeEntersLink = (float) time;
		dspLinkEntrance = dsp;
	}
	
	public void initInfo(int virType, MLPLink onLink, MLPSegment onSeg, MLPLane onLane, int rvid){
		virtualType = virType;
		link = onLink;
		segment = onSeg;
		lane = onLane;
		rvId = rvid;
	}
	
	public void init(int id, double len, double dis, double speed){
		 setId(id);
		 type = 1;
		 length = len;
	     distance = dis;
	     currentSpeed = speed;
	 }
	
	public int updateMove() {
//		if ( Math.abs(getDistance - newDis - newSpeed*SimulationClock.getInstance().getStepSize()) > 0.001 )
//			System.out.println("BUG 未在本计算帧内处理此车");
		if (virtualType >0 && buffer == 0) {
			lane.removeVeh(this, true);
			return Constants.VEHICLE_RECYCLE;
		}
		if (newDis < 0.0) 
			return dealPassing();//Passing link or seg
		return Constants.VEHICLE_NOT_RECYCLE;
	}
	
	public void advance() {
		currentSpeed = (float) newSpeed;
		distance = (float) newDis;
		buffer = Math.max(0, buffer -1);
	}
	
	public int dealPassing() {
		if (segment.isEndSeg()) {
			if (virtualType != 0) {
				//虚车最多影响到Link末端
				holdAtDnEnd();
				return Constants.VEHICLE_NOT_RECYCLE;
			}
			MLPNode server = (MLPNode) link.getDnNode();
			return server.serve(this);
		}
		else {//deal passing Seg.
			/*if (lane.checkPass()) {
				lane.passVeh2ConnDnLn(this);
				newDis = segment.getLength() + newDis;
				if (newDis < 0.0) {
					dealPassing();
				}
			}
			else {//hold in this seg
				newDis = 0.0;
				if (getCurrentSpeed>0.0)
					newSpeed = (getDistance-newDis)/SimulationClock.getInstance().getStepSize();
			}*/
			if (lane.connectedDnLane == null) {//has no successive lane
				if (virtualType == 0){
					System.err.println("Vehicle No. " + getId() +" has no successive lane to go");
					holdAtDnEnd();
				}
				else
					lane.removeVeh(this, true);//如果虚车触发此条件（successive lane不可用），则消失
				return Constants.VEHICLE_NOT_RECYCLE;
			}
			lane.passVeh2ConnDnLn(this);
			newDis = segment.getLength() + newDis;
			if (newDis < 0.0) {
				dealPassing();
			}
			//车辆移动至新的segment，更新强制换道参考值di
			updateDi();
			return Constants.VEHICLE_NOT_RECYCLE;
		}
	}
	
	protected void holdAtDnEnd() {
		newDis = 0.0;
		if (currentSpeed >0.0)
			newSpeed = (distance -newDis) / getMLPNetwork().getSimClock().getStepSize();
	}
	
	public void setNewState(double spd) {
		if (stopFlag) {
			newSpeed = 0.0;
			newDis = distance;
			return;
		}
		if (leading != null) {
			double gap = leading.Displacement() - leading.getLength() - Displacement();
			double maxSpd = ((MLPParameter) getMLPNetwork().getSimParameter()).maxSpeed(gap);
			newSpeed = Math.min(spd,maxSpd);
		}
		else {
			newSpeed = spd;
		}
		newDis = getDistance() - newSpeed * getMLPNetwork().getSimClock().getStepSize();
	}
	
	public void clearMLPProperties() {
		type = 0;
		leading = null;
		trailing = null;
		lane = null;
		segment = null;
		link = null;
		platoonCode = 0;
		virtualType = 0;
		buffer = 0;
		speedLevel = 0;
		cfState = false;
		resemblance = false;
		stopFlag = false;
		newSpeed = 0.0;
		newDis = 0.0;
		setId(0);
		length = 0.0f;
		distance = 0.0f;
		currentSpeed = 0.0f;
		departTime = 0.0f;
		timeEntersLink = 0.0f;
		dspLinkEntrance = 0.0;
		rvId = 0;
		time2Dispatch = 0.0;
		donePathIndex();
//		TimeExit = 0.0;
		diMap.clear();
	}
	
	public void updateUsage() {
		usage += 1;
	}
	
	public void updateLeadNTrail() {
		int p = lane.vehsOnLn.indexOf(this);
		if (p==0) {
			//是lane上的第一辆车，先将前车为null
			leading = (MLPVehicle) null;
			//只要前方存在lane且允许通行，则一直取前方lane，直到前方lane上有车，此时取前方lane的最后一辆作为前车
//			MLPLane thelane = lane.connectedDnLane;
//			while (thelane != null && thelane.enterAllowed) {
//				if (!thelane.vehsOnLn.isEmpty()) {
//					leading = thelane.vehsOnLn.getLast();
//					break;
//				}
//				thelane = thelane.connectedDnLane;
//			}
			MLPSegment theSeg = segment;
			MLPLane theLN = lane;
			while (!theSeg.isEndSeg() && theLN.successiveDnLanes.size()==1){
				//检查下游LN，并推进
				theLN = theLN.successiveDnLanes.get(0);
				theSeg = theLN.getSegment();
				if (!theLN.vehsOnLn.isEmpty()) {
					leading = theLN.vehsOnLn.getLast();
					break;
				}
			}
		}
		else
			//非lane上第一辆车，可取index-1的车作为前车
			leading = lane.vehsOnLn.get(p-1);
		if (p == lane.vehsOnLn.size() - 1) {
			trailing = (MLPVehicle) null;
//			MLPLane thelane = lane.connectedUpLane;
//			while (thelane != null && thelane.enterAllowed) {
//				if (!thelane.vehsOnLn.isEmpty()) {
//					trailing = thelane.vehsOnLn.getFirst();
//					break;
//				}
//				thelane = thelane.connectedUpLane;
//			}
			MLPSegment theSeg = segment;
			MLPLane theLN = lane;
			while (!theSeg.isStartSeg() && theLN.successiveUpLanes.size()==1){
				//检查上游LN，并推进
				theLN = theLN.successiveUpLanes.get(0);
				theSeg = theLN.getSegment();
				if (!theLN.vehsOnLn.isEmpty()) {
					trailing = theLN.vehsOnLn.getFirst();
					break;
				}
			}
		}
		else 
			trailing = lane.vehsOnLn.get(p+1);
	}
	/*public MLPVehicle getLateralLeading(MLPLane tarLN){		
	}
	
	public MLPVehicle getLaterallTrailing(){	
	}*/
	public void updateDi() {
		diMap.clear();
		for (int i = 0; i<segment.nLanes(); i++) {
			MLPLane theLane = segment.getLane(i);
			diMap.put(theLane, theLane.calDi(this));
		}
	}
	public String getInfo(){
		StringBuilder sb = new StringBuilder();
//		if (lane != null && diMap.get(lane)==null)
//			System.out.println("DEBUG MESSAGE");
		sb.append("MLC\n" + String.format("%.2f",calMLC()));//(diMap.get(lane)==0 ? 0 : )
		sb.append("\n前车距离\n" + (leading==null ? "Inf" : String.format("%.2f",leading.Displacement() - Displacement())));
		return sb.toString();
	}
}
