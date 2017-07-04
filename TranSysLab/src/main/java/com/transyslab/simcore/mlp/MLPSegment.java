package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.List;

import com.transyslab.roadnetwork.RoadNetwork;
import com.transyslab.roadnetwork.Segment;

public class MLPSegment extends Segment{
	private int[] laneIdxs_;
	public double startDSP;//�ڵ�ǰlink�е�������
	public double endDSP;//��ǰlink�е�segment�յ����
	protected List<MLPLane> lanes;//seg ���е�Lanes
	
	public MLPSegment() {
		startDSP = 0;
		endDSP = 0;
		lanes = new ArrayList<>();
		//laneIdxs_ = new int[nLanes];//�����ڴ�ʵ����laneIdxsx��ʱnLanes_��ֵδ������
	}
	
	@Override
	public MLPSegment getUpSegment() {
		return (MLPSegment) super.getUpSegment();
	}
	
	@Override
	public MLPSegment getDnSegment(){
		return (MLPSegment) super.getDnSegment();
	}
	public int[] getLaneIdxs(){
		return laneIdxs_;
	}
	
	@Override
	public void calcStaticInfo() {
		if ((getDnSegment() == null)) {
			localType |= 0x0001;
			if (getLink().nDnLinks() < 1 || getLink().getDnNode().type(0x0001) > 0) {
				localType |= 0x0020;
			}
		}
		if (getUpSegment() == null) {
			localType |= 0x0002;
			if (getLink().nUpLinks() < 1 || getLink().getUpNode().type(0x0001) > 0) {
				localType |= 0x0010;
			}
		}
		RoadNetwork.getInstance().totalLinkLength += length;
		RoadNetwork.getInstance().totalLaneLength += length * nLanes;

		if (sdIndex < 0 || sdIndex >= RoadNetwork.getInstance().nSdFns()) {
			// cerr << "Segment " << code_ << " has invalid sdIndex "
			// << sdIndex << "." << endl;
			sdIndex = 0;
		}
		
		//�ӳ�ʵ����,��Ϊ��ҪnLanes_�����ͣ����㣩֮��laneIdxs���ܱ���ȷ�س�ʼ��
		if (laneIdxs_==null) 
			laneIdxs_ = new int[nLanes];
		for(int i = 0; i< nLanes; i++){
			laneIdxs_[i] = leftLaneIndex + i;
		}
		
	}
	
	public boolean isEndSeg() {
		return link.getEndSegment().getCode() == this.getCode();
	}

	public boolean isStartSeg() {
		return link.getStartSegment().getCode() == this.getCode();
	}
	/*public void calcState() {
		density_ = (float) (1000.0f * nVehicles() / (length * nLanes()));
		densityList_.add(density_);
		if (nVehicles() <= 0) {
			speedList.add(maxSpeed());
		}
		else {
			float sum = 0.0f;
			MesoTrafficCell cell = firstCell;
			MesoVehicle pv;
			while (cell != null) {
				pv = cell.firstVehicle();
				while (pv != null) {
					if (pv.getCurrentSpeed() > Constants.SPEED_EPSILON) {
						sum += 1.0f / pv.getCurrentSpeed();
					}
					else {
						sum += 1.0f / Constants.SPEED_EPSILON;
					}
					pv = pv.trailing();
				}
				cell = cell.trailing();
			}
			speed_ = nVehicles() / sum;
			speedList.add(speed_ * 3.6f);
		}
		float x = 3.6f * speed_ * density_;
		flowList.add(Math.round(x));// vehicle/hour

		// return (density_);//vehicle/km
	}*/

	public void setSucessiveLanes() {
		MLPSegment dnSeg = getDnSegment();
		if (dnSeg != null) {
			dealSuccessive(dnSeg);
			return;
		}
		int ndnLinks = link.nDnLinks();
		if (ndnLinks > 0) {
			for (int i = 0; i < ndnLinks; i++) {
				MLPSegment startSeg = (MLPSegment) link.dnLink(i).getStartSegment();
				dealSuccessive(startSeg);
			}
		}
	}
	private void dealSuccessive(MLPSegment dnSeg) {
		if (nLanes == dnSeg.nLanes()) {
			for (int i = 0; i < nLanes; i++) {
				getLane(i).successiveDnLanes.add(dnSeg.getLane(i));
				dnSeg.getLane(i).successiveUpLanes.add(getLane(i));
			}
		}
		else {
			int m = Math.min(nLanes, dnSeg.nLanes());
			double sumLF = 0.0, sumLFSquared = 0.0, sumRT = 0.0, sumRTSquared = 0.0;
			for (int i = 0; i < m; i++) {
				double tmpLF = getLane(i).getEndPnt().distanceSquared(dnSeg.getLane(i).getStartPnt());
				double tmpRT = getLane(nLanes -1-i).getEndPnt().distanceSquared(dnSeg.getLane(dnSeg.nLanes()-1-i).getStartPnt());
				sumLFSquared += tmpLF;
				sumLF += Math.sqrt(tmpLF);
				sumRTSquared += tmpRT;
				sumRT += Math.sqrt(tmpRT);
			}
			double coefVarLF = Math.sqrt(sumLFSquared/m - Math.pow(sumLF/m, 2))/(sumLF/m);
			double coefVarRT = Math.sqrt(sumRTSquared/m - Math.pow(sumRT/m, 2))/(sumRT/m);
			if (coefVarLF <= coefVarRT) {
				for (int i = 0; i < m; i++) {
					getLane(i).successiveDnLanes.add(dnSeg.getLane(i));
					dnSeg.getLane(i).successiveUpLanes.add(getLane(i));
				}
			}
			else {
				for (int i = 0; i < m; i++) {
					getLane(nLanes -1-i).successiveDnLanes.add(dnSeg.getLane(dnSeg.nLanes()-1-i));
					dnSeg.getLane(dnSeg.nLanes()-1-i).successiveUpLanes.add(getLane(nLanes -1-i));
				}
			}
		}
	}

	@Override
	public MLPLane getLane(int i) {
		return (MLPLane) super.getLane(i);
	}
	public void organizeLanes(){
		for (int i = 0; i < nLanes; i++) {
			lanes.add(getLane(i));
		}
	}

	protected List<MLPLane> getValidLanes(MLPVehicle veh){
		List<MLPLane> ans = new ArrayList<>();
		for(MLPLane LN: lanes){
			if (LN.enterAllowed){//�°汾�ı�
				ans.add(LN);
			}
		}
		return ans;
	}
}
