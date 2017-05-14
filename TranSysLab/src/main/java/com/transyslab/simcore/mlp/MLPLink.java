package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import com.transyslab.commons.tools.Inflow;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.commons.tools.EMTTable;
import com.transyslab.roadnetwork.Link;

public class MLPLink extends Link {	
	public EMTTable emtTable;
	public List<JointLane> jointLanes;
	protected List<MLPVehicle> platoon;//���ڴ���ĳ���
	public Dynamics dynaFun;
	public List<Double> tripTime;
//	private TXTUtils tmpWriter = new TXTUtils("src/main/resources/output/rand.csv");
//	public double capacity_;//unit: veh/s/lane
//	private double releaseTime_;
	
	
	public MLPLink(){
		emtTable = new EMTTable();
		jointLanes = new ArrayList<JointLane>();
		platoon = new ArrayList<>();
		dynaFun = new Dynamics();
		tripTime = new ArrayList<>();
//		capacity_ = MLPParameter.getInstance().capacity;
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
																			,0.0))//rec1.speed
			return true;
		else 
			return false;
	}
	
	public JointLane findJointLane(MLPLane ln) {
		if (!jointLanes.isEmpty()) {
			ListIterator<JointLane> lpIterator = jointLanes.listIterator();
			while (lpIterator.hasNext()){
				JointLane candidate = lpIterator.next();
				if (candidate.lanesCompose.contains(ln) ||
					(!ln.getSegment().isEndSeg() && !ln.successiveDnLanes.isEmpty() && candidate.lanesCompose.contains(ln.successiveDnLanes.get(0))) || //��ĩ��Seg��laneֻ��һ��successiveLn
						(!ln.getSegment().isStartSeg() && !ln.successiveUpLanes.isEmpty() && candidate.lanesCompose.contains(ln.successiveUpLanes.get(0)))) //��ʼ��Seg��laneֻ��һ��successiveUpLn
					return candidate;
			}
		}
		return null;
	}
	
	public boolean hasNoVeh(boolean virtualCount) {
//		boolean r = true;
//		for (JointLane jl : jointLanes) {
//			r = r && jl.hasNoVeh();
//		}
//		return r;
		ListIterator<JointLane> JLIterator = jointLanes.listIterator();
		while (JLIterator.hasNext()) {
			if (!JLIterator.next().hasNoVeh(virtualCount)) 
				return false;
		}
		return true;
	}
	
	public void addLnPosInfo() {
		MLPSegment theSeg = (MLPSegment) getEndSegment();
		int JLNUM = 1;
		while (theSeg != null){
			for (int i = 0; i<theSeg.nLanes(); i++){
				MLPLane ln = (MLPLane) theSeg.getLane(i);
				JointLane tmpJLn = findJointLane(ln);
				if (tmpJLn == null) {
					JointLane newJLn = new JointLane(JLNUM);
					JLNUM += 1;
					newJLn.lanesCompose.add(ln);
					jointLanes.add(newJLn);
				}
				else {
					tmpJLn.lanesCompose.add(ln);
				}
			}
			theSeg = (MLPSegment) theSeg.getUpSegment();
		}
	}
	
	/*public void resetReleaseTime() {
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
	
	public boolean checkPass() {
		if (releaseTime_<=SimulationClock.getInstance().getCurrentTime()) 
			return true;
		else 
			return false;
	}*/
	
	public void lanechange() {
		double platoonhead;
		double platoontail;
		MLPVehicle theveh;//�����еĳ���
		Random r = MLPNetwork.getInstance().sysRand;
		Collections.shuffle(jointLanes,r);//���⳵��λ������
		for (JointLane JLn: jointLanes){
			//�������г�������ɳ����Ժ�ͨ��deal������
			if (!JLn.hasNoVeh(false)){
				platoon.clear();
				theveh = JLn.getFirstVeh();
				platoon.add(theveh);
//				platoonhead = theveh.Displacement();		
				platoonhead = ((MLPSegment) getSegment(nSegments_-1)).endDSP;
				platoontail = Math.max(0.0, theveh.Displacement() - theveh.getLength());
				while (theveh.getUpStreamVeh() != null){
					theveh = theveh.getUpStreamVeh();
					if (theveh.resemblance) {						
						platoon.add(theveh);
						platoontail = Math.max(0.0, theveh.Displacement() - theveh.getLength());
					}
					else {
//						platoontail = theveh.Displacement();//��β ������
						dealLC(platoonhead,platoontail);
						platoon.clear();
						platoon.add(theveh);//�³���
//						platoonhead = theveh.Displacement();//��ͷ
						platoonhead = platoontail;
						platoontail = Math.max(0.0, theveh.Displacement() - theveh.getLength());
					}
				}
//				platoontail = theveh.Displacement();//���µ�β ������
				dealLC(platoonhead,platoontail);
			}
		}
	}
	
	public void dealLC(double headDsp, double tailDsp) {
		Random r = MLPNetwork.getInstance().sysRand;
		Collections.shuffle(platoon,r);
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
						pr[i] = veh.calLCProbability(i, tailDsp, headDsp, (double) platoon.size());
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
					if (veh.lane_.getAdjacent(turning[0]).diEqualsZero(veh)) {
						veh.stopFlag = false;
					}
//					tmpWriter.write(pr[0] + "\r\n");
					LCOperate(veh, turning[0]);
				}
				else{
					if (r.nextDouble()<pr[1]){
						if (veh.lane_.getAdjacent(turning[1]).diEqualsZero(veh)) {
							veh.stopFlag = false;
						}
//						tmpWriter.write(pr[1] + "\r\n");
						LCOperate(veh, turning[1]);
					}					
				}
			}
			if ((!veh.lane_.diEqualsZero(veh))){
				if (veh.calMLC()>0.99)
					veh.stopFlag = true;
//				if (veh.calMLC()>0.7)
//					System.out.println("BUG: too late to LC");
			}
		}
	}
	
	public void LCOperate(MLPVehicle veh, int turn) {
		MLPLane thisLane = veh.lane_;
		MLPLane tarLane = thisLane.getAdjacent(turn);
		//���⳵(����->��ʼ��*2->��buff->�滻)
		MLPVehicle newVeh = MLPNetwork.getInstance().veh_pool.generate();
		newVeh.initInfo(veh.getCode(),veh.link_,veh.segment_,veh.lane_,veh.RVID);
		newVeh.init(MLPNetwork.getInstance().getNewVehID(), MLPParameter.VEHICLE_LENGTH, veh.distance(), veh.currentSpeed());
		newVeh.buffer_ = MLPParameter.getInstance().getLCBuff();
		thisLane.substitudeVeh(veh, newVeh);
		//������(��buff->insert)
		veh.buffer_ = MLPParameter.getInstance().getLCBuff();
		tarLane.insertVeh(veh);
	}
	
	public void move() {
		double platoonhead;
		double platoontail;
		MLPVehicle theveh;//�����еĳ���
		Random r = MLPNetwork.getInstance().sysRand;
		Collections.shuffle(jointLanes,r);//���⳵��λ������
		for (JointLane JLn: jointLanes){
			//�������г�������ɳ����Ժ�ͨ��deal������
			if (!JLn.hasNoVeh(true)){
				platoon.clear();
				theveh = JLn.getFirstVeh();
				platoon.add(theveh);
//				platoonhead = theveh.Displacement();
				platoonhead = ((MLPSegment) getSegment(nSegments_-1)).endDSP;
				platoontail = Math.max(0.0, theveh.Displacement() - theveh.getLength());
				while (theveh.getUpStreamVeh() != null){
					theveh = theveh.getUpStreamVeh();
					if (theveh.resemblance) {
						platoon.add(theveh);
						platoontail = Math.max(0.0, theveh.Displacement() - theveh.getLength());
					}
					else {
//						platoontail = theveh.Displacement();//��β ������
						dealMove(platoonhead,platoontail);
						platoon.clear();
						platoon.add(theveh);//�³���
//						platoonhead = theveh.Displacement();//��ͷ
						platoonhead = platoontail;
						platoontail = Math.max(0.0, theveh.Displacement() - theveh.getLength());
					}
				}
//				platoontail = theveh.Displacement();//���µ�β ������
				dealMove(platoonhead,platoontail);
			}
		}
	}
	
	public void dealMove(double headDsp, double tailDsp){
		double k = ((double)platoon.size())/(headDsp-tailDsp);
		double tailspeed = dynaFun.sdFun(k);
		double headspeed = dynaFun.updateHeadSpd(platoon.get(0));
		if (platoon.size()>1) {
			double headPt = platoon.get(0).Displacement();
			double len = headPt - platoon.get(platoon.size()-1).Displacement();
//			if (len == 0.0){
//				System.out.println("BUG ���ӳ���Ϊ0");
//			}
			for (MLPVehicle veh : platoon) {
				double r = (headPt - veh.Displacement())/len;
				double newspd = (1-r)*headspeed+r*tailspeed;
				veh.setNewState(newspd);
			}
		}
		else {
			MLPVehicle veh = platoon.get(0);
			veh.setNewState(headspeed);
		}
	}
	
	
}
