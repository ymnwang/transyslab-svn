package com.transyslab.roadnetwork;

import com.transyslab.commons.tools.GeoUtil;

//车辆轨迹数据
public class VehicleData implements NetworkObject,Comparable<VehicleData>{
	//车辆id
	protected int vehicleID_;
	//车辆类型
	protected int vehicleType_;
	//车辆特殊渲染的标记,0:无标记；1:MLP模型虚拟车
	protected int specialFlag_;
	//车辆长度
	protected double vehicleLength_;
	//车辆沿segment或lane的坐标位置
	protected GeoPoint headPosition;
	//车辆形状
	protected GeoSurface rectangle;
	//车辆信息
	protected String info;
	protected String pathInfo;
	protected int oriNodeID;
	protected int desNodeID;
	protected double curSpeed;
	protected int curLaneID;
	protected boolean isSelected;
	protected double distance;
	protected int tarLaneID;// TODO ffff
	protected boolean queueFlag;
	public boolean isQueue(){
		return this.queueFlag;
	}
	public int getTarLaneID(){
		return this.tarLaneID;
	}
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
	public double getDistance(){
		return this.distance;
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
	// distReverse 行驶距离是否以路段开端作为起点
	public void calcShapePoint(Object moveOn, double distance, boolean distReverse){
		double l, width;
		GeoPoint startPnt, endPnt;
		// 向两侧扩展
		boolean bothSize;
		// TODO 换至初始化函数中
		this.distance = distance;
		if(moveOn instanceof Segment){
			Segment seg = (Segment) moveOn;
			l = seg.getLength();
			startPnt = seg.getStartPnt();
			endPnt = seg.getEndPnt();
			//按车道数压缩
			vehicleLength_ = vehicleLength_/seg.nLanes();
			//车宽
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
			// TODO 写死车宽
			width = 1.8;
			bothSize = true;
		}
		else{
			System.out.println("Error: Unknown class");
			return;
		}
		double s = distance;
		if(!distReverse)
			s = l-distance;
		//车头位置
		double vhcHeadX = startPnt.getLocationX() + s * (endPnt.getLocationX() - startPnt.getLocationX()) / l;
		double vhcHeadY = startPnt.getLocationY() + s * (endPnt.getLocationY() - startPnt.getLocationY()) / l;
		s = s - vehicleLength_;
		//车尾位置
		double vhcTrailX = startPnt.getLocationX() + s * (endPnt.getLocationX() - startPnt.getLocationX()) / l;
		double vhcTrailY = startPnt.getLocationY() + s * (endPnt.getLocationY() - startPnt.getLocationY()) / l;
		// TODO 写死高度z
		this.headPosition = new GeoPoint(vhcHeadX, vhcHeadY, 1.0);
		GeoPoint trailPosition = new GeoPoint(vhcTrailX, vhcTrailY, 1.0);
		// 注意：对象更替频繁
		this.rectangle = GeoUtil.lineToRectangle(trailPosition, headPosition, width,bothSize);
	}
	public void init(int id,Object moveOn,double vhcLength,double distance,double speed,int tarLaneID,boolean queueFlag,boolean distReverse){
		this.vehicleID_ = id;
		this.tarLaneID = tarLaneID;
		this.vehicleLength_ = vhcLength;
		this.curSpeed = speed;
		this.queueFlag = queueFlag;
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

	@Override
	public int compareTo(VehicleData vd) {
		if(this.distance > vd.distance)
			return 1;
		else if(this.distance < vd.distance)
			return -1;
		else
			return 0;
	}
}
