package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.transyslab.commons.tools.Inflow;
import com.transyslab.roadnetwork.Lane;
import com.transyslab.roadnetwork.Link;
import com.transyslab.roadnetwork.RoadNetwork;
import com.transyslab.roadnetwork.Segment;

public class MLPNetwork extends RoadNetwork {
	protected int[] permuteLink;
	protected int nPermutedLinks;
	protected int newVehID_;
	public MLPVehPool veh_pool;
	public List<MLPVehicle> veh_list;
	public List<Loop> loops;
	public Random sysRand;

	public MLPNetwork() {
		newVehID_ = 0;
		veh_pool = new MLPVehPool();
		veh_list = new ArrayList<MLPVehicle>();
		loops = new ArrayList<Loop>();
		sysRand = new Random(System.currentTimeMillis());
	}

	public static MLPNetwork getInstance() {
		HashMap<String, Integer> hm = MLPNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return MLPNetworkPool.getInstance().getNetwork(threadid);
	}
/*
		 * public RN_Sensor newSensor() { return new MLP_Sensor(); }/* public
		 * RN_Signal newSignal() { return new MLP_Signal(); } public
		 * RN_TollBooth newTollBooth() { return new MLP_TollBooth(); }
		 */

	public MLPNode mlpNode(int i) {
		return (MLPNode) getNode(i);
	}
	public MLPLink mlpLink(int i) {
		return (MLPLink) getLink(i);
	}
	public MLPSegment mlpSegment(int i) {
		return (MLPSegment) getSegment(i);
	}
	public MLPLane mlpLane(int i){
		return (MLPLane) getLane(i);
	}

	public void calcStaticInfo() {
		superCalcStaticInfo();
		organize();
	}
	public void setsdIndex() {
		MLPSegment ps;
		for (int i = 0; i < nLinks(); i++) {
			if (MLPNetwork.getInstance().getLink(i).getCode() == 64) {
				ps = (MLPSegment) getLink(i).getEndSegment();
				while (ps != null) {
					ps.setSdIndex(0);
					ps = ps.getUpSegment();
				}
			}
			else if (MLPNetwork.getInstance().getLink(i).getCode() == 60) {
				ps = (MLPSegment) getLink(i).getEndSegment();
				while (ps != null) {
					ps.setSdIndex(0);
					ps = ps.getUpSegment();
				}
			}
			else if (MLPNetwork.getInstance().getLink(i).getCode() == 116) {
				ps = (MLPSegment) getLink(i).getEndSegment();
				while (ps != null) {
					ps.setSdIndex(1);
					ps = ps.getUpSegment();
				}
			}

		}
	}

	public void organize() {
		//���䳵����ŵ���Ϣ
		for (Lane l: lanes_){
			((MLPLane) l).calLnPos();
		}
		for (Lane l: lanes_){
			((MLPLane) l).checkConectedLane();
		}
		for (Lane l: lanes_){
			((MLPLane) l).calDi();
		}
		
		for (Segment seg: segments_){
			Segment tmpseg = seg;
			while (tmpseg.getUpSegment() != null) {
				tmpseg = tmpseg.getUpSegment();
				((MLPSegment) seg).startDSP += tmpseg.getLength();
			}
			((MLPSegment) seg).endDSP = ((MLPSegment) seg).startDSP + seg.getLength();
		}
		
		for (Link l: links_){
			//Ԥ��
			((MLPLink) l).checkConnectivity();
			//��jointLane��Ϣװ��Link��
			((MLPLink) l).addLnPosInfo();
		}
	}
	/*
	 * public void resetSensorReadings() { for (int i = 0; i < nSensors(); i ++
	 * ) { getSensor(i).resetSensorReadings(); } }
	 */

	public void calcSegmentData() {
		MLPSegment ps = new MLPSegment();
		for (int i = 0; i < nSegments(); i++) {
			ps = mlpSegment(i);
			ps.calcDensity();
			ps.calcSpeed();
		}
	}
/*	public void calcSegmentInfo() {
		MLPSegment ps;
		for (int i = 0; i < nSegments(); i++) {
			ps = mlpSegment(i);
			ps.calcState();
		}
	}*/
	/*
	 * --------------------------------------------------------------------
	 * Enter vehicles queued at starting link into the network.
	 * --------------------------------------------------------------------
	 */
	/*public void enterVehiclesIntoNetwork() {
		MLPVehicle pv;

		for (int i = 0; i < nLinks(); i++) {
			
			 * Find the first vehicle in the queue and enter it into the network
			 * if space is available. If the link is full, there is no need for
			 * checking other vehicles in the queue, skip to the next link.
			 

			while ((pv = ((MLPLink) getLink(i)).queueHead()) != null && (pv.enterNetwork() != 0)) {
				 push static vehicle attributes on message buffer 
				;
			}
		}
	}*/

	/*
	 * ------------------------------------------------------------------- Add
	 * number of vehicles allowed to move out during this time step to the
	 * segment balance.
	 * -------------------------------------------------------------------
	 */
/*	public void resetSegmentEmitTime() {
		for (int i = 0; i < nSegments(); i++) {
			mlpSegment(i).resetEmitTime();
		}
	}*/

	/*public void guidedVehiclesUpdatePaths() {
		MLPTrafficCell cell;
		MLPVehicle pv;
		int i;

		// Vehicles already in the network

		for (i = 0; i < nSegments(); i++) {
			cell = mlpSegment(i).firstCell();
			while (cell != null) {
				pv = cell.firstVehicle();
				while (pv != null) {
					if (pv.isGuided() != 0) {
						pv.changeRoute();
					}
					pv = pv.trailing();
				}
				cell = cell.trailing();
			}
		}

		// Vehicles waiting for entering the network

		for (i = 0; i < nLinks(); i++) {
			pv = mlpLink(i).queueHead();
			while (pv != null) {
				if (pv.isGuided() != 0) {
					pv.changeRoute();
				}
				pv = pv.trailing();
			}
		}
	}*/

	// Calculate capacity of the nodes

/*	public void updateNodeCapacities() {
		for (int i = 0; i < nNodes(); i++) {
			mlpNode(i).calcCapacities();
		}
	}*/

	// Update phase
	/*
	 * -------------------------------------------------------------------- For
	 * each traffic cells in the network, calculate its density and upSpeed.
	 * These two variables depend only on the state of a particular traffic cell
	 * itself.
	 * --------------------------------------------------------------------
	 */
/*	public void calcTrafficCellUpSpeed() {
		MLPSegment ps;
		for (int i = 0; i < nLinks(); i++) {
			ps = (MLPSegment) getLink(i).getEndSegment();
			while (ps != null) {
				ps.calcTrafficCellUpSpeed();
				ps = ps.getUpSegment();
			}
		}
	}*/
	/*
	 * -------------------------------------------------------------------- For
	 * each traffic cells in the network, calculate its dnSpeed. This variable
	 * variable depends on its own state and the states of the traffic cells
	 * ahead. This function is called after calcIndependentTrafficCellStates()
	 * is called.
	 * --------------------------------------------------------------------
	 */
/*	public void calcTrafficCellDnSpeeds() {
		MLPSegment ps;
		for (int i = 0; i < nLinks(); i++) {
			ps = (MLPSegment) getLink(i).getEndSegment();
			while (ps != null) { // downstream first
				ps.calcTrafficCellDnSpeeds();
				ps = ps.getUpSegment();
			}
		}
	}*/

	// Advance phase

	/*public void advanceVehicles() {
		permuteLink = null;
		nPermutedLinks = 0;
		int i;

		if (nPermutedLinks != nLinks()) { // this piece is executed only once
			if (permuteLink != null) {
				// δ���� delete [] permuteLink;
			}
			nPermutedLinks = nLinks();
			permuteLink = new int[nPermutedLinks];
			for (i = 0; i < nPermutedLinks; i++) {
				permuteLink[i] = i;
			}
		}

		// Randomize the link order

		Random.getInstance().get(Random.Misc).permute(nLinks(), permuteLink);
		for (i = 0; i < nLinks(); i++) {
			MLPLink p = mlpLink(permuteLink[i]);
			p.advanceVehicles();
		}
	}*/

	// Remove, merge, and split cells

/*	public void formatTrafficCells() {
		for (int i = 0; i < nSegments(); i++) {
			mlpSegment(i).formatTrafficCells();
		}
	}*/

	// Remove all vehicles in the network, including those in
	// pretrip queues

/*	public void clean() {
		int i;

		// Release current cells

		for (i = 0; i < nLinks(); i++) {
			mlpLink(i).clean();
		}

		// Restore capacities

		for (i = 0; i < nSegments(); i++) {
			MLPSegment ps = mlpSegment(i);
			ps.setCapacity(ps.defaultCapacity());
		}
	}*/


	/*public void recordLinkTravelTimeOfActiveVehicle() {
		MLPTrafficCell cell;
		MLPVehicle pv;
		int i;

		// Record travel time for vehicle still in the network

		for (i = 0; i < nSegments(); i++) {
			cell = mlpSegment(i).firstCell();
			while (cell != null) {
				pv = cell.firstVehicle();
				while (pv != null) {
					pv.link().recordExpectedTravelTime(pv);
					pv = pv.trailing();
				}
				cell = cell.trailing();
			}
		}

		// Record travel time for vehicle in ths spill back queues

		for (i = 0; i < nLinks(); i++) {
			pv = mlpLink(i).queueHead();
			while (pv != null) {
				pv.nextLink().recordExpectedTravelTime(pv);
				pv = pv.trailing();
			}
		}
	}*/
	
	/*public void recordVehicleData(){
		MLPSegment ps;
		MLPTrafficCell tc;
		MLPVehicle vhc;
		VehicleData vd;
		//�Ӷ�����л�ȡframe����
//		JOGLAnimationFrame frame = JOGLFramePool.getFramePool().getFrame();
		ListIterator<Segment> i = segments_.listIterator();
		//����segment
		while (i.hasNext()) {
			ps = (MLPSegment) i.next();
			tc = ps.firstCell();
			//����cell
			while (tc != null) {
				vhc = tc.firstVehicle();
				//����vehicle
				while (vhc != null) {
					//�Ӷ���ػ�ȡvehicledata����
					vd = VehicleDataPool.getVehicleDataPool().getVehicleData();
					//��¼������Ϣ
					vd.init(vhc);
					//��vehicledata����frame
					try {
						JOGLFrameQueue.getInstance().offer(vd, MLPVehicle.nVehicles());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//��һ����
					vhc = vhc.trailing();
				}
				//��һ������
				tc = tc.trailing();
			}
		}
	}*/

	public void resetReleaseTime(){
		for (int i = 0; i<nLinks(); i++){
			mlpLink(i).resetReleaseTime();
		}
	}
	
	/*public void addNewVehID() {
		newVehID_ += 1;		
	}*/
	
	public int getNewVehID(){
		newVehID_ += 1;	
		return newVehID_ ;
	}
	
	public void loadEmtTable(){
		//double now = SimulationClock.getInstance().getCurrentTime();
		for (int i = 0; i<nLinks(); i++){
			while (mlpLink(i).checkFirstEmtTableRec()){
				Inflow emitVeh = mlpLink(i).emtTable.getInflow().poll();
				MLPVehicle newVeh = veh_pool.generate();
				newVeh.init2(0,mlpLink(i),(MLPSegment) mlpLink(i).getStartSegment(),mlpLane(emitVeh.laneIdx));
				newVeh.init(getNewVehID(), MLPParameter.VEHICLE_LENGTH, (float) emitVeh.dis, (float) emitVeh.speed);				
				//newVeh.init(getNewVehID(), 1, MLPParameter.VEHICLE_LENGTH, (float) emitVeh.dis, (float) now);
				mlpLane(emitVeh.laneIdx).appendVeh(newVeh);
			}
		}
	}
	
	public void platoonRecognize() {
		for (MLPVehicle mlpv : veh_list){
			mlpv.calState();
			if (mlpv.CFState_ && mlpv.speedLevel_==mlpv.leading_.speedLevel_) {
				mlpv.resemblance = true;
			}
			else {
				mlpv.resemblance = false;
			}
			mlpv.resetPlatoonCode();
		}
		/*List<MLPVehicle> vl = findResemblance();
		while (vl.size()>0){
			for (MLPVehicle v: vl){
				v.platoonCode = v.leading_.platoonCode;
				v.resemblance = v.leading_.resemblance;
			}
			vl = findResemblance();
		}*/
		
	}
	
	public List<MLPVehicle> findResemblance() {
		List<MLPVehicle> vList = new ArrayList<MLPVehicle>();
		for (MLPVehicle v: veh_list){
			if (v.resemblance)
				vList.add(v);
		}
		return vList;
	}
	
	public void setOverallCapacity(double arg) {
		for (int i = 0; i < nLinks(); i++) {
			mlpLink(i).capacity_ = arg;
		}
	}	
	public void setCapacity(int idx, double c) {
		mlpLink(idx).capacity_ = c;
	}
	
	public void setOverallSDParas(double [] args) {
		for (int i = 0; i < nLinks(); i++) {
			mlpLink(i).dynaFun.sdPara = args;
		}
	}
	public void setSDParas(int idx, double [] paras) {
		mlpLink(idx).dynaFun.sdPara = paras;
	}
	
	public void setLoopSection(int linkID, double p) {
		MLPLink theLink = (MLPLink) findLink(linkID);
		MLPSegment theSeg = null;
		MLPLane theLane = null;
		double dsp = ((MLPSegment) theLink.getEndSegment()).endDSP * p;
		for (int i = 0; i<theLink.nSegments(); i++) {
			theSeg = (MLPSegment) theLink.getSegment(i);
			if (theSeg.startDSP<dsp && theSeg.endDSP>=dsp) 
				break;
		}
		if (theSeg != null) {
			for (int k = 0; k<theSeg.nLanes(); k++) {
				theLane = (MLPLane) theSeg.getLane(k);
				loops.add(new Loop(theLane, theSeg, theLink, dsp));
			}
		}
		
	}
}