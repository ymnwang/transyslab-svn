package com.transyslab.simcore.rts;

import com.transyslab.roadnetwork.Segment;

/**
 * Created by ITSA405-35 on 2018/5/28.
 */
public class RTSegment extends Segment {
	private double startDSP;//在当前link中的起点里程
	private double endDSP;//当前link中的segment终点里程
	public void setStartDSP(double startDSP){
		this.startDSP = startDSP;
	}
	public void setEndDSP(double endDSP){
		this.endDSP = endDSP;
	}
	public double getStartDSP(){
		return this.startDSP;
	}
	public double getEndDSP(){
		return this.endDSP;
	}
	@Override
	public RTSegment getUpSegment() {
		return (RTSegment) super.getUpSegment();
	}

	@Override
	public RTSegment getDnSegment(){
		return (RTSegment) super.getDnSegment();
	}
	@Override
	public RTLane getLane(int index) {
		return (RTLane) super.getLane(index);
	}
	public boolean isEndSeg() {
		return link.getEndSegment().equals(this);
	}

	public boolean isStartSeg() {
		return link.getStartSegment().equals(this);
	}



}
