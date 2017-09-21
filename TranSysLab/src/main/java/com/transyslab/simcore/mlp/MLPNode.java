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
			//TODO ��ȡcurrentTime���Ż�
			double currentTime = veh.link.getNetwork().getSimClock().getCurrentTime();
			//passed output capacity checking
			if (veh.getNextLink() == null) {
				lane_.scheduleNextEmitTime();//passed upstream lane
				//arrived destination no constrain
				//record linkTravelTime
				link_.tripTime.add(new double[] {veh.timeEntersLink(), veh.dspLinkEntrance, currentTime + veh.newDis/veh.newSpeed});
				lane_.removeVeh(veh, true);
				return Constants.VEHICLE_RECYCLE;
			}
			//trying to pass to next link
			//if this is an intersection, deal with inner movements
			if (type(Constants.NODE_TYPE_INTERSECTION)!=0) {				
				//movement in an intersection not available for now
				lane_.scheduleNextEmitTime();//passed upstream lane
				return Constants.VEHICLE_NOT_RECYCLE;
			}
			//deal with non-intersection nodes
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
						lane_.scheduleNextEmitTime();//passed upstream lane
						double timeAtPoint = currentTime + veh.newDis/veh.newSpeed;//newDis<0 ��Ϊ+
						link_.tripTime.add(new double[] {veh.timeEntersLink(), veh.dspLinkEntrance, timeAtPoint});//record linkTravelTime
						veh.initLinkEntrance(timeAtPoint, 0.0);
						lane_.removeVeh(veh, false);
						veh.time2Dispatch = currentTime;
						statVeh(veh, nextLane);
						veh.onRouteChoosePath(veh.link.getDnNode(),veh.link.getNetwork());
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
		else
			stopCount += 1;
		//hold still
		veh.holdAtDnEnd();
		return Constants.VEHICLE_NOT_RECYCLE;
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
	private void statVeh(MLPVehicle veh, MLPLane nextLane) {
		//processingVeh.lane/seg/link setting
		veh.lane = nextLane;
		veh.segment = nextLane.getSegment();
		veh.link = (MLPLink) nextLane.getLink();
		veh.newDis += nextLane.getLength();
		if (veh.newDis < 0.0) {//ÿ����ྭ��һ��link
			veh.newDis = 0.0;
			//TODO clock��ȡ��ʽ���Ż�
			SimulationClock simClock = veh.link.getNetwork().getSimClock();
			veh.newSpeed = (veh.getDistance() + nextLane.getLength()) / simClock.getStepSize();
		}
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
}
