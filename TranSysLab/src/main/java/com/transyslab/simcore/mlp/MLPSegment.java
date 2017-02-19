package com.transyslab.simcore.mlp;

import com.jogamp.common.util.ReflectionUtil;
import com.transyslab.roadnetwork.RoadNetwork;
import com.transyslab.roadnetwork.Segment;

public class MLPSegment extends Segment{
	private int[] laneIdxs_;
	public double startDSP;//在当前link中的起点里程
	public double endDSP;//当前link中的segment终点里程
	
	public MLPSegment() {
		startDSP = 0;
		endDSP = 0;
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
		if (link_.getEndSegment().getCode()==this.getCode()) {
			return true;
		}
		else {
			return false;
		}
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

}
