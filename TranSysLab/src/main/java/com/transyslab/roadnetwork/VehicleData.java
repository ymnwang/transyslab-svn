package com.transyslab.roadnetwork;

public class VehicleData {
	protected int vehicleID_;
	protected int vehicleType_;
	protected float vehicleLength_;
	protected double vhcLocationX_;
	protected double vhcLocationY_;
	
	public int getVehicleID(){
		return vehicleID_;
	}
	public int getVehicleType(){
		return vehicleType_;
	}
	public double getVhcLocationX(){
		return vhcLocationX_;
	}
	public double getVhcLocationY(){
		return vhcLocationY_;
	}
	public void init(int id, int type, double x, double y){
		vehicleID_ = id;
		vehicleType_ = type;
		vhcLocationX_ = x;
		vhcLocationY_ = y;
	}
	public void init(Vehicle vhc){
		vehicleID_ = vhc.getCode();
		vehicleType_ = vhc.getType();
		vehicleLength_ = vhc.getLength();
		Segment seg = vhc.segment();
		double l = seg.getLength();
		double s = l-vhc.distance();
		vhcLocationX_ = seg.getStartPnt().getLocationX() + s * (seg.getEndPnt().getLocationX() - seg.getStartPnt().getLocationX()) / l;
		vhcLocationY_ = seg.getStartPnt().getLocationY() + s * (seg.getEndPnt().getLocationY() - seg.getStartPnt().getLocationY()) / l;
		
	}
	public void clean(){
		vehicleID_ = vehicleType_ = 0;
		vhcLocationX_ = vhcLocationY_ = 0;
		vehicleLength_ = 0;
	}
}
