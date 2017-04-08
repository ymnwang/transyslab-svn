package com.transyslab.roadnetwork;

import jhplot.math.LinearAlgebra;

public class Boundary extends CodedObject {
	protected GeoPoint startPnt_;
	protected GeoPoint endPnt_;
	protected int index_;
	public Boundary() {

	}
	public void init(int code, double beginx, double beginy, double endx, double endy) {
		setCode(code);
		startPnt_ = new GeoPoint(beginx, beginy);
		endPnt_ = new GeoPoint(endx, endy);
		RoadNetwork.getInstance().getWorldSpace().recordExtremePoints(startPnt_);
		RoadNetwork.getInstance().getWorldSpace().recordExtremePoints(endPnt_);
		index_ = RoadNetwork.getInstance().nBoundarys();
		RoadNetwork.getInstance().addBoundary(this);
	}
	public void translateInWorldSpace(WorldSpace world_space) {
		startPnt_ = world_space.worldSpacePoint(startPnt_);
		endPnt_ = world_space.worldSpacePoint(endPnt_);
	}
	public GeoPoint getStartPnt() {
		return startPnt_;
	}
	public GeoPoint getEndPnt() {
		return endPnt_;
	}
	public double[] getDelta() {
		return LinearAlgebra.minus(endPnt_.getLocCoods(), startPnt_.getLocCoods());
	}
}
