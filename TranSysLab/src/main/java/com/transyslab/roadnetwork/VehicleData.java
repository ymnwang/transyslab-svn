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
		//��ͷλ��
		double vhcHeadX, vhcHeadY;
		//��βλ��
		double vhcTrailX, vhcTrailY;
		if(isSegBased){
			Segment seg = vhc.getSegment();
			double l = seg.getLength();
			double s = l-vhc.getDistance();
			vhcHeadX = seg.getStartPnt().getLocationX() + s * (seg.getEndPnt().getLocationX() - seg.getStartPnt().getLocationX()) / l;
			vhcHeadY = seg.getStartPnt().getLocationY() + s * (seg.getEndPnt().getLocationY() - seg.getStartPnt().getLocationY()) / l;						
			//��������ѹ��
			s = s - vehicleLength_/seg.nLanes();
			vhcTrailX = seg.getStartPnt().getLocationX() + s * (seg.getEndPnt().getLocationX() - seg.getStartPnt().getLocationX()) / l;
			vhcTrailY = seg.getStartPnt().getLocationY() + s * (seg.getEndPnt().getLocationY() - seg.getStartPnt().getLocationY()) / l;
			// TODO д���߶�z
			this.headPosition = new GeoPoint(vhcHeadX, vhcHeadY, 0.5);
			GeoPoint trailPosition = new GeoPoint(vhcTrailX, vhcTrailY, 0.5);
			// TODO ���·�ο��
			this.rectangle = GeoUtil.lineToRectangle(trailPosition, headPosition, seg.nLanes() *Constants.LANE_WIDTH, false);
		}
		else{
			Lane lane = vhc.getLane();
			this.curLaneID = lane.getId();
			double l = lane.getLength();
			double s = l-vhc.getDistance();
			vhcHeadX = lane.getStartPnt().getLocationX() + s * (lane.getEndPnt().getLocationX() - lane.getStartPnt().getLocationX()) / l;
			vhcHeadY = lane.getStartPnt().getLocationY() + s * (lane.getEndPnt().getLocationY() - lane.getStartPnt().getLocationY()) / l;
			s = s - vehicleLength_;
			vhcTrailX = lane.getStartPnt().getLocationX() + s * (lane.getEndPnt().getLocationX() - lane.getStartPnt().getLocationX()) / l;
			vhcTrailY = lane.getStartPnt().getLocationY() + s * (lane.getEndPnt().getLocationY() - lane.getStartPnt().getLocationY()) / l;
			// TODO д���߶�z
			this.headPosition = new GeoPoint(vhcHeadX, vhcHeadY, 0.5);
			GeoPoint trailPosition = new GeoPoint(vhcTrailX, vhcTrailY, 0.5);
			// TODO д������
			this.rectangle = GeoUtil.lineToRectangle(trailPosition, headPosition, 1.25, true); 
		}
		
		
	}
	public void clean(){
		vehicleID_ = vehicleType_ = 0;
		vehicleLength_ = 0;
	}
}
