package com.transyslab.roadnetwork;

import jhplot.math.LinearAlgebra;

public class Boundary extends CodedObject {
	protected Point startPnt_;
	protected Point endPnt_;
	protected int index_;
	public Boundary() {

	}
	public void init(int code, double beginx, double beginy, double endx, double endy) {
		setCode(code);
		startPnt_ = new Point(beginx, beginy);
		endPnt_ = new Point(endx, endy);
		RoadNetwork.getInstance().getWorldSpace().recordExtremePoints(startPnt_);
		RoadNetwork.getInstance().getWorldSpace().recordExtremePoints(endPnt_);
		index_ = RoadNetwork.getInstance().nBoundarys();
		RoadNetwork.getInstance().addBoundary(this);
	}
	public void translateInWorldSpace(WorldSpace world_space) {
		startPnt_ = world_space.worldSpacePoint(startPnt_);
		endPnt_ = world_space.worldSpacePoint(endPnt_);
	}
	public Point getStartPnt() {
		return startPnt_;
	}
	public Point getEndPnt() {
		return endPnt_;
	}
	public double[] getDelta() {
		return LinearAlgebra.minus(endPnt_.getLocations(), startPnt_.getLocations());
	}
}
