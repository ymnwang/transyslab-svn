package com.transyslab.simcore.mlp;

import java.util.LinkedList;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.Node;

public class MLPNode extends Node{
	private LinkedList<MLPVehicle> statedVehs;
	public int stopCount;
	public MLPNode() {
		statedVehs = new LinkedList<>();
		stopCount = 0;
	}
	public int serve(MLPVehicle veh) {
		MLPLane lane_ = veh.lane;
		MLPLink link_ = veh.link;
		if (lane_.checkPass()) {//lane.checkPass()
			//passed output capacity checking
			if (veh.getNextLink() == null) {
				lane_.scheduleNextEmitTime();//passed upstream lane
				//arrived destination no constrain
				link_.tripTime.add((double) veh.timeInLink());//record linkTravelTime
				lane_.removeVeh(veh, true);
				return 1;
			}
			//trying to pass to next link
			//if this is an intersection, deal with inner movements
			if (type(Constants.NODE_TYPE_INTERSECTION)!=0) {				
				//movement in an intersection not available for now
				lane_.scheduleNextEmitTime();//passed upstream lane
				return 0;
			}
			//deal with non-intersection nodes
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
						lane_.scheduleNextEmitTime();//passed upstream lane
						link_.tripTime.add((double) veh.timeInLink());//record linkTravelTime
						double now = SimulationClock.getInstance().getCurrentTime();
						veh.setTimeEntersLink(now);
						lane_.removeVeh(veh, false);
						veh.time2Dispatch = now;
						statVeh(veh, nextLane);
						veh.onRouteChoosePath(veh.link.getDnNode());
						return 0;
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
		else
			stopCount += 1;
		//hold still
		veh.holdAtDnEnd();
		return 0;	
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
		return dis_headway - followerLen < MLPParameter.getInstance().minGap(crSpeed);
	}
	private boolean reachFirst(MLPVehicle vehPass, MLPVehicle vehCheck) {
		double dis_headway = vehCheck.getDistance() - vehPass.newDis - vehCheck.getCurrentSpeed()*SimulationClock.getInstance().getStepSize();
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
	private void statVeh(MLPVehicle veh, MLPLane nextLane) {
		//processingVeh.lane/seg/link setting
		veh.lane = nextLane;
		veh.segment = nextLane.getSegment();
		veh.link = (MLPLink) nextLane.getLink();
		veh.newDis += nextLane.getLength();
		if (veh.newDis < 0.0) {//每次最多经过一个link
			veh.newDis = 0.0;
			veh.newSpeed = (veh.getDistance() + nextLane.getLength()) / SimulationClock.getInstance().getStepSize();
		}
		statedVehs.add(veh);
	}
	protected void dispatchStatedVeh() {
		double now = SimulationClock.getInstance().getCurrentTime();
		for (int i = 0; i < statedVehs.size(); i++) {
			MLPVehicle tmpveh = statedVehs.get(i);
			if (now >= tmpveh.time2Dispatch) {
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
				ans |= ( gap < MLPParameter.getInstance().minGap(veh.newSpeed) );
			}
		}
		return ans;
	}
	protected void clearStatedVehs() {
		statedVehs.clear();
		stopCount = 0;
	}
}
