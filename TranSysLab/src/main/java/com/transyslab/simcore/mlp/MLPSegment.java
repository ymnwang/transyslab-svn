package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.List;

import com.transyslab.roadnetwork.RoadNetwork;
import com.transyslab.roadnetwork.Segment;

public class MLPSegment extends Segment{
	private int[] laneIdxs_;
	public double startDSP;//在当前link中的起点里程
	public double endDSP;//当前link中的segment终点里程
	protected List<MLPLane> lanes;//seg 含有的Lanes
	
	public MLPSegment() {
		startDSP = 0;
		endDSP = 0;
		lanes = new ArrayList<>();
		//laneIdxs_ = new int[nLanes_];//不能在此实例化laneIdxsx此时nLanes_的值未被计算
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
			localType_ |= 0x0001;
			if (getLink().nDnLinks() < 1 || getLink().getDnNode().type(0x0001) > 0) {
				localType_ |= 0x0020;
			}
		}
		if (getUpSegment() == null) {
			localType_ |= 0x0002;
			if (getLink().nUpLinks() < 1 || getLink().getUpNode().type(0x0001) > 0) {
				localType_ |= 0x0010;
			}
		}
		RoadNetwork.getInstance().totalLinkLength_ += length_;
		RoadNetwork.getInstance().totalLaneLength_ += length_ * nLanes_;

		if (sdIndex_ < 0 || sdIndex_ >= RoadNetwork.getInstance().nSdFns()) {
			// cerr << "Segment " << code_ << " has invalid sdIndex "
			// << sdIndex_ << "." << endl;
			sdIndex_ = 0;
		}
		
		//延迟实例化,因为需要nLanes_被解释（计算）之后laneIdxs才能被正确地初始化
		if (laneIdxs_==null) 
			laneIdxs_ = new int[nLanes_];
		for(int i= 0;i<nLanes_;i++){
			laneIdxs_[i] = leftLaneIndex_ + i;
		}
		
	}
	
	public boolean isEndSeg() {
		return link_.getEndSegment().getCode() == this.getCode();
	}

	public boolean isStartSeg() {
		return link_.getStartSegment().getCode() == this.getCode();
	}
	/*public void calcState() {
		density_ = (float) (1000.0f * nVehicles() / (length_ * nLanes()));
		densityList_.add(density_);
		if (nVehicles() <= 0) {
			speedList_.add(maxSpeed());
		}
		else {
			float sum = 0.0f;
			MesoTrafficCell cell = firstCell_;
			MesoVehicle pv;
			while (cell != null) {
				pv = cell.firstVehicle();
				while (pv != null) {
					if (pv.currentSpeed() > Constants.SPEED_EPSILON) {
						sum += 1.0f / pv.currentSpeed();
					}
					else {
						sum += 1.0f / Constants.SPEED_EPSILON;
					}
					pv = pv.trailing();
				}
				cell = cell.trailing();
			}
			speed_ = nVehicles() / sum;
			speedList_.add(speed_ * 3.6f);
		}
		float x = 3.6f * speed_ * density_;
		flowList_.add(Math.round(x));// vehicle/hour

		// return (density_);//vehicle/km
	}*/

	public void setSucessiveLanes() {
		MLPSegment dnSeg = getDnSegment();
		if (dnSeg != null) {
			dealSuccessive(dnSeg);
			return;
		}
		int ndnLinks = link_.nDnLinks();
		if (ndnLinks > 0) {
			for (int i = 0; i < ndnLinks; i++) {
				MLPSegment startSeg = (MLPSegment) link_.dnLink(i).getStartSegment();
				dealSuccessive(startSeg);
			}
		}
	}
	private void dealSuccessive(MLPSegment dnSeg) {
		if (nLanes_ == dnSeg.nLanes()) {
			for (int i = 0; i < nLanes_; i++) {
				getLane(i).successiveDnLanes.add(dnSeg.getLane(i));
				dnSeg.getLane(i).successiveUpLanes.add(getLane(i));
			}
		}
		else {
			int m = Math.min(nLanes_, dnSeg.nLanes());
			double sumLF = 0.0, sumLFSquared = 0.0, sumRT = 0.0, sumRTSquared = 0.0;
			for (int i = 0; i < m; i++) {
				double tmpLF = getLane(i).getEndPnt().distanceSquared(dnSeg.getLane(i).getStartPnt());
				double tmpRT = getLane(nLanes_-1-i).getEndPnt().distanceSquared(dnSeg.getLane(dnSeg.nLanes()-1-i).getStartPnt());
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
					getLane(nLanes_-1-i).successiveDnLanes.add(dnSeg.getLane(dnSeg.nLanes()-1-i));
					dnSeg.getLane(dnSeg.nLanes()-1-i).successiveUpLanes.add(getLane(nLanes_-1-i));
				}
			}
		}
	}

	@Override
	public MLPLane getLane(int i) {
		return (MLPLane) super.getLane(i);
	}
	public void organizeLanes(){
		for (int i = 0; i < nLanes_; i++) {
			lanes.add(getLane(i));
		}
	}

	protected List<MLPLane> getValidLanes(MLPVehicle veh){
		List<MLPLane> ans = new ArrayList<>();
		for(MLPLane LN: lanes){
			if (LN.enterAllowed){//下版本改变
				ans.add(LN);
			}
		}
		return ans;
	}
}
