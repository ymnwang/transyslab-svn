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
					//todo �߼�����©�� ʱ�䲽������೵����Խʱ���������
					double timeAtPoint = currentTime + veh.newDis/veh.newSpeed;//newDis<0 ��Ϊ+
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
//			List<MLPLane> candidates = lane.selectDnLane(veh.getNextLink().getStartSegment());//����successiveDnLane����
			MLPLane nextLane = lane_.successiveDnLaneInLink((MLPLink) veh.getNextLink());
			if (nextLane != null) {// at least one topology available down lane
				if (nextLane.checkVolum(MLPParameter.VEHICLE_LENGTH,0.0)) {//check every down lane' volume //before: checkVolum(veh)
					//no priority control for now
					boolean canpass = true;
//						for (int j = 0; j < nextLane.nUpLanes() && canpass; j++) { //����successiveDnLane����
//							MLPLane confLane = (MLPLane) nextLane.upLane(j); //����successiveDnLane����
					for (int j = 0; j < nextLane.successiveUpLanes.size() && canpass; j++) {
						MLPLane confLane = nextLane.successiveUpLanes.get(j);
						canpass &= confLane.getId() == lane_.getId() ||
								confLane.vehsOnLn.isEmpty() ||
								// lane.priority > confLane.priority || //·Ȩ�ϴ�ʱ����ֱ��ͨ��
								!need2Giveway(veh, confLane.vehsOnLn.get(0)) ||
								reachFirst(veh, confLane.vehsOnLn.get(0));//ͬ��·Ȩ���ȵ��ȵã�����·Ȩ���滻������Ĵ���
						//(lane.priority == confLane.priority && reachFirst(veh, confLane.vehsOnLn.get(0))
					}
					if (canpass && !checkPlaceTaken(veh, nextLane)) {//pass to this very nexlane
						double timeAtPoint = currentTime + veh.newDis/veh.newSpeed;//newDis<0 ��Ϊ+
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
		if (veh.newDis < 0.0) {//ÿ����ྭ��һ��link
			//todo �߼�����©�� ʱ�䲽������೵����Խʱ���������
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
		//TODO parameter��ȡ��ʽ���Ż�
		MLPParameter mlpParameter = (MLPParameter) vehPass.link.getNetwork().getSimParameter();
		return dis_headway - followerLen < mlpParameter.minGap(crSpeed);
	}
	private boolean reachFirst(MLPVehicle vehPass, MLPVehicle vehCheck) {
		//TODO clock��ȡ��ʽ���Ż�
		SimulationClock simClock = vehPass.link.getNetwork().getSimClock();
		double dis_headway = vehCheck.getDistance() - vehPass.newDis - vehCheck.getCurrentSpeed()*simClock.getStepSize();
		return dis_headway >= 0;
		//������checkPlaceTaken���ƣ����Բ��жϱ߽�
//		if (dis_headway != 0) {
//			return dis_headway > 0;
//		}
//		else {
//			//�߽����⣬����ȣ�VID�ϴ�ĵȴ� (��ʹ���������������Ҫ���ͻ���β�ѯ���һ��)
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
		//TODO clock��ȡ��ʽ���Ż�
		if (statedVehs.size() <= 0)
			return;
		SimulationClock simClock = getDnLink(0).getNetwork().getSimClock();
		double now = simClock.getCurrentTime();
		for (int i = 0; i < statedVehs.size(); i++) {
			MLPVehicle tmpveh = statedVehs.get(i);
			if (now >= tmpveh.time2Dispatch) {
				//������link������ǿ�ƻ���ֵdi
				tmpveh.updateDi();
				tmpveh.lane.insertVeh(tmpveh);//����һ�������������
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
				//TODO parameter��ȡ��ʽ���Ż�
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
						double timeAtPoint = currentTime + veh.newDis/veh.newSpeed;//newDis<0 ��Ϊ+
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
