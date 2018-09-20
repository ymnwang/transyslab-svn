package com.transyslab.roadnetwork;

import java.util.List;

/**
 * Created by ITSA405-35 on 2018/5/22.
 */
public class Connector implements NetworkObject{
	protected long id;
	protected String name;
	protected String objInfo;
	protected boolean isSelected;
	protected long upLaneId;
	protected long dnLaneId;
	// 折线点集,按连接顺序存储
	protected List<GeoPoint> shapePoints;
	protected double[] linearRelation;

	public List<GeoPoint> getShapePoints(){
		return shapePoints;
	}
	public Connector(){

	}
	public Connector(long id,List<GeoPoint> shapePoints,long upLaneId, long dnLaneId){
		this.id = id;
		this.shapePoints = shapePoints;
		this.upLaneId = upLaneId;
		this.dnLaneId = dnLaneId;
	}
	public void init(long id, long upLaneId, long dnLaneId, List<GeoPoint> shapePoints){
		this.id = id;
		this.shapePoints = shapePoints;
		this.upLaneId = upLaneId;
		this.dnLaneId = dnLaneId;
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
	public long getId() {
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

}
