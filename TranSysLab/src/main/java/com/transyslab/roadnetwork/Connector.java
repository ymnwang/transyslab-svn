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
	protected double[] linearRelation;

	public List<GeoPoint> getShapePoints(){
		return shapePoints;
	}
	public Connector(int id, List<GeoPoint> shapePoints, Lane upLane, Lane dnLane){
		this.id = id;
		this.shapePoints = shapePoints;
		this.upLane = upLane;
		this.dnLane = dnLane;
		initLinearRelation();
	}
	private void initLinearRelation(){
		if(!shapePoints.isEmpty()){
			int size = shapePoints.size();
			linearRelation = new double[size];
			linearRelation[0] = 0;
			if(size>1){
				for(int i=1;i<size;i++){
					linearRelation[i] = shapePoints.get(i).distance(shapePoints.get(i-1))+linearRelation[i-1];
				}
			}
		}


	}
	public double[] getLinearRelation(){
		return this.linearRelation;
	}
	public void translateInWorldSpace(WorldSpace world_space) {
		for(GeoPoint p: shapePoints){
			world_space.translateWorldSpacePoint(p);
		}
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
