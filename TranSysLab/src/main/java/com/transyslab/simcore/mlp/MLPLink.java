package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import com.transyslab.commons.tools.Inflow;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.commons.tools.emitTable;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.Link;

public class MLPLink extends Link {	
	public emitTable emtTable;
	public List<JointLane> jointLanes;
	protected List<MLPVehicle> platoon;//���ڴ���ĳ���
	public Dynamics dynaFun;
	public double capacity_;//unit: veh/s/lane
	private double releaseTime_;
	
	
	public MLPLink(){
		emtTable = new emitTable();
		jointLanes = new ArrayList<JointLane>();
		platoon = new ArrayList<>();
		dynaFun = new Dynamics();
		capacity_ = MLPParameter.getInstance().capacity;
	}
	
	public void checkConnectivity(){
		
	}
	
	public boolean checkFirstEmtTableRec() {
		if (emtTable.getInflow().isEmpty()) {
			return false;
		}
		Inflow rec1 = emtTable.getInflow().getFirst();
		if (rec1.time<=SimulationClock.getInstance().getCurrentTime() &&
				MLPNetwork.getInstance().mlpLane(rec1.laneIdx).checkVolum(MLPParameter.VEHICLE_LENGTH
																			,rec1.speed))
			return true;
		else 
			return false;
	}
	
	public JointLane findJointLane(MLPLane ln) {
		if (!jointLanes.isEmpty()) {
			ListIterator<JointLane> lpIterator = jointLanes.listIterator();
			while (lpIterator.hasNext()){
				JointLane candidate = lpIterator.next();
				if (candidate.LPNum == ln.getLnPosNum()) {
					return candidate;
				}
			}			
		}
		return (JointLane) null;
	}
	
	public void addLnPosInfo() {
		MLPSegment theSeg = (MLPSegment) getEndSegment();
		while (theSeg != null){
			for (int i = theSeg.getLeftLaneIndex(); i<theSeg.nLanes(); i++){
				MLPLane ln = MLPNetwork.getInstance().mlpLane(i);
				JointLane tmpJLn = findJointLane(ln);
				if (tmpJLn == null) {
					JointLane newJLn = new JointLane(ln.getLnPosNum());
					newJLn.lanesCompose.add(ln);
					jointLanes.add(newJLn);
				}
				else {
					tmpJLn.lanesCompose.add(ln);
				}
			}
			theSeg = (MLPSegment) theSeg.getDnSegment();
		}
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
	
	public void lanechange() {
		double platoonhead;
		double platoontail;
		MLPVehicle theveh;//�����еĳ���
		Collections.shuffle(jointLanes);//���⳵��λ������		
		for (JointLane JLn: jointLanes){
			//�������г�������ɳ����Ժ�ͨ��deal������
			if (!JLn.emptyVeh()){
				platoon.clear();
				theveh = JLn.getFirstVeh();
				platoon.add(theveh);
				platoonhead = theveh.Displacement();
				while (theveh.getUpStreamVeh() != null){
					theveh = theveh.getUpStreamVeh();
					if (theveh.resemblance) {						
						platoon.add(theveh);
					}
					else {
						platoontail = theveh.Displacement();//��β ������
						dealLC(platoonhead,platoontail);
						platoon.clear();
						platoon.add(theveh);//�³���
						platoonhead = theveh.Displacement();//��ͷ
					}
				}
				platoontail = theveh.Displacement();//���µ�β ������
				dealLC(platoonhead,platoontail);
			}
		}
	}
	
	public void dealLC(double headDsp, double tailDsp) {
		Random r = MLPNetwork.getInstance().sysRand;
		Collections.shuffle(platoon);
		for (MLPVehicle veh: platoon){
			//���⳵����ȴ�еĳ����ܻ���
			if (veh.VirtualType_== 0 && veh.buffer_== 0) {
				//����acceptance����·���򣬻�ȡ�ɻ���������Ϣ��������ʲ�����
				double [] pr = new double [] {0.0, 0.0};
				int [] turning = new int [] {0,1};
				for (int i = 0; i<2; i++){//i=0��ת��i=1��ת��
					MLPLane tarLane = veh.lane_.getAdjacent(i);
					if (tarLane != null && //�������
							tarLane.checkLCAllowen((i+1)%2) &&
							//tarLane.RtCutinAllowed &&
							veh.checkGapAccept(tarLane)) {
						//�������ʼ���						
						pr[i] = veh.calLCProbability(i, tailDsp, headDsp);
					}
				}
				//����
				if (pr[0]<pr[1]){
					turning[0] = 1;
					turning[1] = 0;
					double tmp = pr[0];
					pr[0] = pr[1];
					pr[1] = tmp;
				}
				//���Ⱥ�˳�������ؿ��壬�����ɹ��Ľ��л��������ɹ�������MLC������ͣ����ʶ����
				if (r.nextDouble()<pr[0]){
					if (veh.lane_.getAdjacent(turning[0]).di==0) {
						veh.stopFlag = false;
					}
					LCOperate(veh, turning[0]);
				}
				else{
					if (r.nextDouble()<pr[1]){
						if (veh.lane_.getAdjacent(turning[1]).di==0) {
							veh.stopFlag = false;
						}
						LCOperate(veh, turning[1]);
					}					
				}
			}
			if (veh.lane_.di>0 && veh.calMLC()>0.99){
				veh.stopFlag = true;
			}
		}
	}
	
	public void LCOperate(MLPVehicle veh, int turn) {
		//���⳵
		MLPVehicle newVeh = MLPNetwork.getInstance().veh_pool.generate();
		newVeh.init2(veh.getCode(),veh.link_,veh.segment_,veh.lane_);
		newVeh.init(MLPNetwork.getInstance().getNewVehID(), MLPParameter.VEHICLE_LENGTH, veh.distance(), veh.currentSpeed());
		newVeh.buffer_ = MLPParameter.getInstance().getLCBuff();
		veh.lane_.substitudeVeh(veh, newVeh);
		//������
		veh.lane_ = veh.lane_.getAdjacent(turn);
		veh.buffer_ = MLPParameter.getInstance().getLCBuff();
		veh.lane_.insertVeh(veh);
	}
	
	public void move() {
		double platoonhead;
		double platoontail;
		MLPVehicle theveh;//�����еĳ���
		Collections.shuffle(jointLanes);//���⳵��λ������		
		for (JointLane JLn: jointLanes){
			//�������г�������ɳ����Ժ�ͨ��deal������
			if (!JLn.emptyVeh()){
				platoon.clear();
				theveh = JLn.getFirstVeh();
				platoon.add(theveh);
				platoonhead = theveh.Displacement();
				while (theveh.getUpStreamVeh() != null){
					theveh = theveh.getUpStreamVeh();
					if (theveh.resemblance) {						
						platoon.add(theveh);
					}
					else {
						platoontail = theveh.Displacement();//��β ������
						dealMove(platoonhead,platoontail);
						platoon.clear();
						platoon.add(theveh);//�³���
						platoonhead = theveh.Displacement();//��ͷ
					}
				}
				platoontail = theveh.Displacement();//���µ�β ������
				dealMove(platoonhead,platoontail);
			}
		}
	}
	
	public void dealMove(double headDsp, double tailDsp){
		double tailspeed = dynaFun.sdFun(((double)platoon.size())/(headDsp-tailDsp));
		//System.out.println("platoonK=" + ((double)platoon.size())/(headDsp-tailDsp));
		double headspeed = dynaFun.updateHeadSpd(platoon.get(0));
		if (platoon.size()>1) {
			double headPt = platoon.get(0).Displacement();
			double len = headPt - platoon.get(platoon.size()-1).Displacement();
			for (MLPVehicle veh : platoon) {
				double r = (headPt - veh.Displacement())/len;
				veh.setNewState( (1-r)*headspeed+r*tailspeed );
			}
		}
		else {
			MLPVehicle veh = platoon.get(0);
			veh.setNewState(headspeed);
		}
	}
	
	
}
