package com.transyslab.roadnetwork;

import com.transyslab.commons.tools.GeoUtil;

//�����켣����
public class VehicleData implements NetworkObject{
	//����id
	protected int vehicleID_;
	//��������
	protected int vehicleType_;
	//����������Ⱦ�ı��,0:�ޱ�ǣ�1:MLPģ�����⳵
	protected int specialFlag_;
	//��������
	protected double vehicleLength_;
	//������segment��lane������λ��
	protected GeoPoint headPosition;
	//������״
	protected GeoSurface rectangle;
	//������Ϣ
	protected String info;
	protected String pathInfo;
	protected int oriNodeID;
	protected int desNodeID;
	protected double curSpeed;
	protected int curLaneID;
	protected boolean isSelected;
	public int getVehicleID(){
		return vehicleID_;
	}
	public int getVehicleType(){
		return vehicleType_;
	}
	public double getVhcLength(){
		return vehicleLength_;
	}
	public double getVhcLocationX(){
		return headPosition.getLocationX();
	}
	public double getVhcLocationY(){
		return headPosition.getLocationY();
	}
	public double getCurSpeed(){
		return curSpeed;
	}
	public int getOriNodeID(){
		return oriNodeID;
	}
	public int getDesNodeID(){
		return desNodeID;
	}
	public int getCurLaneID(){
		return curLaneID;
	}
	public String getPathInfo(){
		return pathInfo;
	}
	public boolean isSelected(){
		return this.isSelected;
	}
	public void setSelected(boolean flag){
		this.isSelected = flag;
	}
	public int getId(){
		return this.vehicleID_;
	}
	public String getObjInfo(){
		return this.info;
	}
	public int getSpecialFlag(){
		return specialFlag_;
	}
	public GeoSurface getVhcShape(){
		return this.rectangle;
	}
	public String getVhcInfo(){
		return this.info;
	}
	public void init(Vehicle vhc, boolean isSegBased, int specialflag, String info){
		this.vehicleID_ = vhc.id;
		this.vehicleType_ = vhc.getType();
		this.vehicleLength_ = vhc.getLength();
		this.specialFlag_ = specialflag;
		this.info = info;
		this.curSpeed = vhc.getCurrentSpeed();
		this.oriNodeID = vhc.oriNode().getId();
		this.desNodeID = vhc.desNode().getId();
		StringBuilder sb = new StringBuilder();
		int nLinks = vhc.path.getLinks().size();
		for(int i =0;i<vhc.path.getLinks().size();i++){
			sb.append(vhc.path.getLink(i).getId());
			if(i!=nLinks-1){
				sb.append("->");
			}
		}
		this.pathInfo = sb.toString();
		Object moveOn;
		if(isSegBased)
			moveOn = vhc.getSegment();
		else
			moveOn = vhc.getLane();
		calcShapePoint(moveOn,vhc.getDistance(),false);
	}
	// distReverse ��ʻ�����Ƿ���·�ο�����Ϊ���
	public void calcShapePoint(Object moveOn, double distance, boolean distReverse){
		double l, width;
		GeoPoint startPnt, endPnt;
		// ��������չ
		boolean bothSize;
		if(moveOn instanceof Segment){
			Segment seg = (Segment) moveOn;
			l = seg.getLength();
			startPnt = seg.getStartPnt();
			endPnt = seg.getEndPnt();
			//��������ѹ��
			vehicleLength_ = vehicleLength_/seg.nLanes();
			//����
			width = seg.nLanes()*Constants.LANE_WIDTH;
			//
			bothSize = false;
		}
		else if(moveOn instanceof Lane){
			Lane lane = (Lane) moveOn;
			this.curLaneID = lane.getId();
			l = lane.getLength();
			startPnt = lane.getStartPnt();
			endPnt = lane.getEndPnt();
			// TODO д������
			width = 1.25;
			bothSize = true;
		}
		else{
			System.out.println("Error: Unknown class");
			return;
		}
		double s = distance;
		if(!distReverse)
			s = l-distance;
		//��ͷλ��
		double vhcHeadX = startPnt.getLocationX() + s * (endPnt.getLocationX() - startPnt.getLocationX()) / l;
		double vhcHeadY = startPnt.getLocationY() + s * (endPnt.getLocationY() - startPnt.getLocationY()) / l;
		s = s - vehicleLength_;
		//��βλ��
		double vhcTrailX = startPnt.getLocationX() + s * (endPnt.getLocationX() - startPnt.getLocationX()) / l;
		double vhcTrailY = startPnt.getLocationY() + s * (endPnt.getLocationY() - startPnt.getLocationY()) / l;
		// TODO д���߶�z
		this.headPosition = new GeoPoint(vhcHeadX, vhcHeadY, 0.5);
		GeoPoint trailPosition = new GeoPoint(vhcTrailX, vhcTrailY, 0.5);
		// ע�⣺�������Ƶ��
		this.rectangle = GeoUtil.lineToRectangle(trailPosition, headPosition, width,bothSize);
	}
	public void init(int id,Object moveOn,double vhcLength,double distance,boolean distReverse){
		this.vehicleID_ = id;
		this.vehicleLength_ = vhcLength;
		if(moveOn == null) {
			System.out.println("Error: Could not find the Lane");
			return;
		}
		calcShapePoint(moveOn,distance,distReverse);
	}
	public void clean(){
		vehicleID_ = vehicleType_ = 0;
		vehicleLength_ = 0;
	}
	//wym
	public GeoPoint getHeadPosition(){
		return headPosition;
	}
}
