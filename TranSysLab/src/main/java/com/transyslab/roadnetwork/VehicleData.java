package com.transyslab.roadnetwork;

//�����켣����
public class VehicleData {
	//����id
	protected int vehicleID_;
	//��������
	protected int vehicleType_;
	//����������Ⱦ�ı��,0:�ޱ�ǣ�1:MLPģ�����⳵
	protected int specialFlag_;
	//��������
	protected float vehicleLength_;
	//������segment��lane������λ��
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
	public int getSpecialFlag(){
		return specialFlag_;
	}
	public void init(int id, int type, double x, double y){
		vehicleID_ = id;
		vehicleType_ = type;
		vhcLocationX_ = x;
		vhcLocationY_ = y;
	}
	public void init(Vehicle vhc, boolean isSegBased, int specialflag){
		vehicleID_ = vhc.getCode();
		vehicleType_ = vhc.getType();
		vehicleLength_ = vhc.getLength();
		specialFlag_ = specialflag;
		if(isSegBased){
			Segment seg = vhc.getSegment();
			double l = seg.getLength();
			double s = l-vhc.distance();
			vhcLocationX_ = seg.getStartPnt().getLocationX() + s * (seg.getEndPnt().getLocationX() - seg.getStartPnt().getLocationX()) / l;
			vhcLocationY_ = seg.getStartPnt().getLocationY() + s * (seg.getEndPnt().getLocationY() - seg.getStartPnt().getLocationY()) / l;						
		}
		else{
			Lane lane = vhc.getLane();
			double l = lane.getLength();
			double s = l-vhc.distance();
//			vhcLocationX_ = lane.getStartPnt().getLocationX() + s * (lane.getEndPnt().getLocationX() - lane.getStartPnt().getLocationX()) / l;
//			vhcLocationY_ = lane.getStartPnt().getLocationY() + s * (lane.getEndPnt().getLocationY() - lane.getStartPnt().getLocationY()) / l;
			vhcLocationX_ = lane.getEndPnt().getLocationX() + s * (lane.getStartPnt().getLocationX() - lane.getEndPnt().getLocationX()) / l;
			vhcLocationY_ = lane.getEndPnt().getLocationY() + s * (lane.getStartPnt().getLocationY() - lane.getEndPnt().getLocationY()) / l;
		}
		
	}
	public void init(int laneid, float distance){
		Lane lane = RoadNetwork.getInstance().findLane(laneid);
		double l = lane.getLength();
		double s = l-distance;
		vhcLocationX_ = lane.getStartPnt().getLocationX() + s * (lane.getEndPnt().getLocationX() - lane.getStartPnt().getLocationX()) / l;
		vhcLocationY_ = lane.getStartPnt().getLocationY() + s * (lane.getEndPnt().getLocationY() - lane.getStartPnt().getLocationY()) / l;
		
	}
	public void clean(){
		vehicleID_ = vehicleType_ = 0;
		vhcLocationX_ = vhcLocationY_ = 0;
		vehicleLength_ = 0;
	}
}
