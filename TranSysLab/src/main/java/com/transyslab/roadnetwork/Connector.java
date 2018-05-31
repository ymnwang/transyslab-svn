package com.transyslab.roadnetwork;

import java.util.List;

/**
 * Created by ITSA405-35 on 2018/5/22.
 */
public class Connector implements NetworkObject{
	protected int id;
	protected String name;
	protected String objInfo;
	protected boolean isSelected;
	protected Lane upLane;
	protected Lane dnLane;
	// 折线点集,按连接顺序存储
	protected List<GeoPoint> shapePoints;

	public List<GeoPoint> getShapePoints(){
		return shapePoints;
	}
	public Connector(int id, List<GeoPoint> shapePoints){
		this.id = id;
		this.shapePoints = shapePoints;
	}
	public Connector(Lane upLane, Lane dnLane){
		this.upLane = upLane;
		this.dnLane = dnLane;
	}
	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getObjInfo() {
		return objInfo;
	}

	@Override
	public boolean isSelected() {
		return isSelected;
	}

	@Override
	public void setSelected(boolean flag) {
		this.isSelected = flag;
	}
	public GeoPoint getStartPoint(){
		return upLane.getEndPnt();
	}
	public GeoPoint getEndPoint(){
		return dnLane.getStartPnt();
	}
}
