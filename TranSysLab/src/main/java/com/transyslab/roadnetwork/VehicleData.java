package com.transyslab.roadnetwork;

import com.transyslab.commons.tools.GeoUtil;

//车辆轨迹数据
public class VehicleData {
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
	
	public int getVehicleID(){
		return vehicleID_;
	}                      
	public int getVehicleType(){
		return vehicleType_;
	}
	public double getVhcLocationX(){
		return headPosition.getLocationX();
	}
	public double getVhcLocationY(){
		return headPosition.getLocationY();
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
		vehicleID_ = vhc.id;
		vehicleType_ = vhc.getType();
		vehicleLength_ = vhc.getLength();
		specialFlag_ = specialflag;
		this.info = info;
		//车头位置
		double vhcHeadX, vhcHeadY;
		//车尾位置
		double vhcTrailX, vhcTrailY;
		if(isSegBased){
			Segment seg = vhc.getSegment();
			double l = seg.getLength();
			double s = l-vhc.getDistance();
			vhcHeadX = seg.getStartPnt().getLocationX() + s * (seg.getEndPnt().getLocationX() - seg.getStartPnt().getLocationX()) / l;
			vhcHeadY = seg.getStartPnt().getLocationY() + s * (seg.getEndPnt().getLocationY() - seg.getStartPnt().getLocationY()) / l;						
			//按车道数压缩
			s = s - vehicleLength_/seg.nLanes;
			vhcTrailX = seg.getStartPnt().getLocationX() + s * (seg.getEndPnt().getLocationX() - seg.getStartPnt().getLocationX()) / l;
			vhcTrailY = seg.getStartPnt().getLocationY() + s * (seg.getEndPnt().getLocationY() - seg.getStartPnt().getLocationY()) / l;
			// TODO 写死高度z
			this.headPosition = new GeoPoint(vhcHeadX, vhcHeadY, 0.5);
			GeoPoint trailPosition = new GeoPoint(vhcTrailX, vhcTrailY, 0.5);
			// TODO 检查路段宽度
			this.rectangle = GeoUtil.lineToRectangle(trailPosition, headPosition, seg.nLanes *Constants.LANE_WIDTH, false);
		}
		else{
			Lane lane = vhc.getLane();
			double l = lane.getLength();
			double s = l-vhc.getDistance();
			vhcHeadX = lane.getStartPnt().getLocationX() + s * (lane.getEndPnt().getLocationX() - lane.getStartPnt().getLocationX()) / l;
			vhcHeadY = lane.getStartPnt().getLocationY() + s * (lane.getEndPnt().getLocationY() - lane.getStartPnt().getLocationY()) / l;
			s = s - vehicleLength_;
			vhcTrailX = lane.getStartPnt().getLocationX() + s * (lane.getEndPnt().getLocationX() - lane.getStartPnt().getLocationX()) / l;
			vhcTrailY = lane.getStartPnt().getLocationY() + s * (lane.getEndPnt().getLocationY() - lane.getStartPnt().getLocationY()) / l;
			// TODO 写死高度z
			this.headPosition = new GeoPoint(vhcHeadX, vhcHeadY, 0.5);
			GeoPoint trailPosition = new GeoPoint(vhcTrailX, vhcTrailY, 0.5);
			// TODO 写死车宽
			this.rectangle = GeoUtil.lineToRectangle(trailPosition, headPosition, 1.25, true); 
		}
		
		
	}
	public void clean(){
		vehicleID_ = vehicleType_ = 0;
		vehicleLength_ = 0;
	}
}
