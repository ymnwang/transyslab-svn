package com.transyslab.roadnetwork;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.math.geom.AABBox;

public class GeoSurface implements NetworkObject{
	protected List<GeoPoint> kerbPoints;
	protected int segmentId;
	protected int index;
	protected int id;
	protected String objInfo;
	protected boolean isSelected;
	public GeoSurface(){
		kerbPoints = new ArrayList<GeoPoint>();
	}
	public void init(int code, int segmentid) {
		this.id =  code;
		segmentId = segmentid;

	}
	public int getId(){
		return this.id;
	}
	public void setSelected(boolean flag){
		this.isSelected = flag;
	}
	public String getObjInfo(){
		return this.objInfo;
	}
	public boolean isSelected(){
		return this.isSelected;
	}

	public void addKerbPoint(GeoPoint p){
		kerbPoints.add(p);

	}
	public void translateInWorldSpace(WorldSpace world_space) {
		for(GeoPoint p: kerbPoints){
			world_space.translateWorldSpacePoint(p);
		}
	}
	public List<GeoPoint> getKerbList(){
		return kerbPoints;
	}
	public void setKerbList(List<GeoPoint> kerbList){
		kerbPoints = kerbList;
	}

}
