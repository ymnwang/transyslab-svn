package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.*;

public class MLPNode extends Node{
	private LinkedList<MLPVehicle> statedVehs;
	protected double passSpd = 40.0/3.6;
	public int stopCount;
	protected List<MLPConnector> lcList;
	public MLPNode() {
		statedVehs = new LinkedList<>();
		stopCount = 0;
		lcList = new ArrayList<>();
	}
	public int serve(MLPVehicle veh) {
		MLPLane lane_ = veh.lane;
		MLPLink link_ = veh.link;
		double currentTime = veh.link.getNetwork().getSimClock().getCurrentTime();

		//if this is an intersection, deal with inner movements
		if (type(Constants.NODE_TYPE_INTERSECTION)!=0) {
			if (intersectionPass(currentTime,veh.getLink().getId(),veh.getNextLink().getId())) {
				//trip finished?
				if (veh.getNextLink() == null)
					return dump(veh);
				//innermovement
				MLPLane nextLane = lane_.successiveDnLaneInLink((MLPLink) veh.getNextLink());
				MLPConnector theConn = lane_.pickDnConn(nextLane.getId());
				if (theConn.checkVolume(veh)){
					//todo 逻辑存在漏洞 时间步长过大多车辆穿越时需重新设计
					double timeAtPoint = currentTime + veh.newDis/veh.newSpeed;//newDis<0 故为+
					statVeh(veh,timeAtPoint);
					//todo pass to lc
					return Constants.VEHICLE_NOT_RECYCLE;
				}
			}
			return holdTheVeh(veh);
		}

		//deal with non-intersection nodes
		if((!ExpSwitch.CAP_CTRL) || lane_.checkPass()){
			//passed output capacity checking
			//trip finished?
			if (veh.getNextLink() == null)
				return dump(veh);
//			List<MLPLane> candidates = lane.selectDnLane(veh.getNextLink().getStartSegment());//不以successiveDnLane运行
			MLPLane nextLane = lane_.successiveDnLaneInLink((MLPLink) veh.getNextLink());
			if (nextLane != null) {// at least one topology available down lane
				if (nextLane.checkVolum(MLPParameter.VEHICLE_LENGTH,0.0)) {//check every down lane' volume //before: checkVolum(veh)
					//no priority control for now
					boolean canpass = true;
//						for (int j = 0; j < nextLane.nUpLanes() && canpass; j++) { //不以successiveDnLane运行
//							MLPLane confLane = (MLPLane) nextLane.upLane(j); //不以successiveDnLane运行
					for (int j = 0; j < nextLane.successiveUpLanes.size() && canpass; j++) {
						MLPLane confLane = nextLane.successiveUpLanes.get(j);
						canpass &= confLane.getId() == lane_.getId() ||
								confLane.vehsOnLn.isEmpty() ||
								// lane.priority > confLane.priority || //路权较大时可以直接通过
								!need2Giveway(veh, confLane.vehsOnLn.get(0)) ||
								reachFirst(veh, confLane.vehsOnLn.get(0));//同等路权下先到先得，加入路权后替换成下面的代码
						//(lane.priority == confLane.priority && reachFirst(veh, confLane.vehsOnLn.get(0))
					}
					if (canpass && !checkPlaceTaken(veh, nextLane)) {//pass to this very nexlane
						double timeAtPoint = currentTime + veh.newDis/veh.newSpeed;//newDis<0 故为+
						statVeh(veh,timeAtPoint);
						transfer(veh,timeAtPoint);
						return Constants.VEHICLE_NOT_RECYCLE;
					}
//					else
//						System.out.println("BUG Can NOT pass or has been taken place");
				}
//				else
//					System.out.println("BUG Failed checking volume");
			}
//			else
//				System.out.println("BUG Error Next Lane is null");
		}
		else //can not pass capacity ctrl
			stopCount += 1;
		return holdTheVeh(veh);
	}
	private int holdTheVeh(MLPVehicle veh){
		veh.holdAtDnEnd();
		return Constants.VEHICLE_NOT_RECYCLE;
	}
	private int dump(MLPVehicle veh){
		MLPLane lane_ = veh.lane;
		MLPLink link_ = veh.link;
		double currentTime = veh.link.getNetwork().getSimClock().getCurrentTime();
		lane_.scheduleNextEmitTime();//passed upstream lane
		//arrived destination no constrain
		//record linkTravelTime
		link_.tripTime.add(new double[] {veh.timeEntersLink(), veh.dspLinkEntrance, currentTime + veh.newDis/veh.newSpeed});
		lane_.removeVeh(veh, true);
		return Constants.VEHICLE_RECYCLE;
	}
	private void transfer(MLPVehicle veh, double time){
		MLPLane lane_ = veh.lane;
		MLPLane nextLane = lane_.successiveDnLaneInLink((MLPLink) veh.getNextLink());
		veh.initLinkEntrance(time, 0.0);
		//processingVeh.lane/seg/link setting
		veh.lane = nextLane;
		veh.segment = nextLane.getSegment();
		veh.link = (MLPLink) nextLane.getLink();
		veh.newDis += nextLane.getLength();
		if (veh.newDis < 0.0) {//每次最多经过一个link
			//todo 逻辑存在漏洞 时间步长过大多车辆穿越时需重新设计
			veh.newDis = 0.0;
			SimulationClock simClock = veh.link.getNetwork().getSimClock();
			veh.newSpeed = (veh.getDistance() + nextLane.getLength()) / simClock.getStepSize();
		}
		veh.time2Dispatch = time;
		veh.onRouteChoosePath(veh.link.getDnNode(),veh.link.getNetwork());
	}
	private boolean need2Giveway(MLPVehicle vehPass, MLPVehicle vehCheck) {		
		double dis_headway = vehCheck.getDistance() - vehPass.newDis - vehCheck.getCurrentSpeed();
		double crSpeed;
		double followerLen;
		if (dis_headway > 0) {
			crSpeed = vehPass.newSpeed;
			followerLen = vehCheck.getLength();
		}
		else {
			dis_headway *= -1;
			crSpeed = vehCheck.getCurrentSpeed();
			followerLen = vehPass.getLength();
		}
		//TODO parameter获取方式待优化
		MLPParameter mlpParameter = (MLPParameter) vehPass.link.getNetwork().getSimParameter();
		return dis_headway - followerLen < mlpParameter.minGap(crSpeed);
	}
	private boolean reachFirst(MLPVehicle vehPass, MLPVehicle vehCheck) {
		//TODO clock获取方式待优化
		SimulationClock simClock = vehPass.link.getNetwork().getSimClock();
		double dis_headway = vehCheck.getDistance() - vehPass.newDis - vehCheck.getCurrentSpeed()*simClock.getStepSize();
		return dis_headway >= 0;
		//加入了checkPlaceTaken机制，可以不判断边界
//		if (dis_headway != 0) {
//			return dis_headway > 0;
//		}
//		else {
//			//边界问题，若相等，VID较大的等待 (或使用其他的随机规则；要求冲突两次查询结果一致)
//			return vehPass.getCode() < vehCheck.getCode();
//		}
		
	}
	private void statVeh(MLPVehicle veh, double time) {
		MLPLane lane_ = veh.lane;
		MLPLink link_ = veh.link;
		lane_.scheduleNextEmitTime();//passed upstream lane
		link_.tripTime.add(new double[] {veh.timeEntersLink(), veh.dspLinkEntrance, time});//record linkTravelTime
		lane_.removeVeh(veh, false);
		statedVehs.add(veh);
	}
	protected void dispatchStatedVeh() {
		//TODO clock获取方式待优化
		if (statedVehs.size() <= 0)
			return;
		SimulationClock simClock = getDnLink(0).getNetwork().getSimClock();
		double now = simClock.getCurrentTime();
		for (int i = 0; i < statedVehs.size(); i++) {
			MLPVehicle tmpveh = statedVehs.get(i);
			if (now >= tmpveh.time2Dispatch) {
				//进入新link，更新强制换道值di
				tmpveh.updateDi();
				tmpveh.lane.insertVeh(tmpveh);//增加一个倒序插入会更快
				statedVehs.remove(i);
				i -= 1;
			}
		}
	}
	protected boolean checkPlaceTaken(MLPVehicle veh, MLPLane nextLane){
		boolean ans = false;
		for (int i=0; i<statedVehs.size() && (!ans); i++){
			MLPVehicle v = statedVehs.get(i);
			if (v.lane.getId() == nextLane.getId()){
				double gap = veh.newDis + nextLane.getLength() - v.newDis - v.getLength();
				//TODO parameter获取方式待优化
				MLPParameter mlpParameter = (MLPParameter) veh.link.getNetwork().getSimParameter();
				ans |= ( gap < mlpParameter.minGap(veh.newSpeed) );
			}
		}
		return ans;
	}
	protected void clearStatedVehs() {
		statedVehs.clear();
		stopCount = 0;
	}
	public double getPassSpd() {
		return passSpd;
	}
	private boolean intersectionPass(double currentTime, long fLinkID, long tLinkID) {
		return type(Constants.NODE_TYPE_SIGNALIZED_INTERSECTION)==0 ||
				findPlan(currentTime).check(currentTime, fLinkID, tLinkID);
	}
	protected MLPNode update() {
		if (type(Constants.NODE_TYPE_INTERSECTION)!=0) {
			SimulationClock clock = upLinks.get(0).getNetwork().getSimClock();
			double stepSize = clock.getStepSize();
			double currentTime = clock.getCurrentTime();
			for (MLPConnector lc : lcList) {
				double passSpd = lc.calSpd();
				lc.vehsOnConn.forEach(veh -> {
					veh.newSpeed = passSpd;
					veh.newDis -= passSpd*stepSize;
					veh.time2Dispatch = currentTime + veh.newDis / Math.max(1e-5,passSpd);
					if (veh.newDis<0.0) {
						double timeAtPoint = currentTime + veh.newDis/veh.newSpeed;//newDis<0 故为+
						transfer(veh,timeAtPoint);
					}
				});
			}
		}
		return this;
	}
	protected MLPNode updateStatedVehs() {
		if (type(Constants.NODE_TYPE_INTERSECTION)!=0) {
			statedVehs.forEach(veh -> {

			});
		}
		return this;
	}
	public void addLC(MLPConnector lc) {
		lcList.add(lc);
	}
}
