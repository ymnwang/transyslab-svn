package com.transyslab.roadnetwork;

import jhplot.math.LinearAlgebra;

public class Boundary implements NetworkObject{
	protected GeoPoint startPnt;
	protected GeoPoint endPnt;
	protected int index;
	protected int id;
	protected String objInfo;
	protected boolean isSelected;
	public Boundary() {

	}
	public boolean isSelected(){
		return this.isSelected;
	}
	public void setSelected(boolean flag){
		this.isSelected = flag;
	}
	public void init(int id, int index,double beginx, double beginy, double endx, double endy ){
		this.id = id;
		this.index = index;
		startPnt = new GeoPoint(beginx,beginy);
		endPnt = new GeoPoint(endx,endy);
	}
	public int getId(){
		return this.id;
	}
	public String getObjInfo(){
		return this.objInfo;
	}
	public void translateInWorldSpace(WorldSpace world_space) {
		startPnt = world_space.worldSpacePoint(startPnt);
		endPnt = world_space.worldSpacePoint(endPnt);
	}
	public GeoPoint getStartPnt() {
		return startPnt;
	}
	public GeoPoint getEndPnt() {
		return endPnt;
	}
	public double[] getDelta() {
		return LinearAlgebra.minus(endPnt.getLocCoods(), startPnt.getLocCoods());
	}
}
