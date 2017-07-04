package com.transyslab.roadnetwork;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.math.geom.AABBox;

public class GeoSurface extends CodedObject {
	protected List<GeoPoint> kerbPoints_;
	protected int segmentId_;
	protected int index_;
	protected AABBox aabBox;
	protected float[] maxCood;
	protected float[] minCood;
	public GeoSurface(){
		kerbPoints_ = new ArrayList<GeoPoint>();
		maxCood = new float[]{Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE};
		minCood = new float[]{Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE};
	}
	public void init(int code, int segmentid) {
		setCode(code);
		segmentId_ = segmentid;
		index_ = RoadNetwork.getInstance().nSurfaces();
		RoadNetwork.getInstance().addSurface(this);

	}
	public void addKerbPoint(GeoPoint p){
		kerbPoints_.add(p);
		//¾Ésurface
//		RoadNetwork.getInstance().getWorldSpace().recordExtremePoints(p);
	}
	public void translateInWorldSpace(WorldSpace world_space) {
		for(GeoPoint p:kerbPoints_){
			world_space.translateWorldSpacePoint(p);
			maxCood[0] = (float) Math.max(p.getLocationX(),maxCood[0]);
			maxCood[1] = (float) Math.max(p.getLocationY(),maxCood[1]);
			maxCood[2] = 0.0f;//(float) Math.max(p.getLocationZ(),maxCood[2]);
			
			minCood[0] = (float) Math.min(p.getLocationX(),minCood[0]);
			minCood[1] = (float) Math.min(p.getLocationY(),minCood[1]);
			minCood[2] = 0.0f;//(float) Math.min(p.getLocationZ(),minCood[2]);
		}
	}
	public void createAabBox(){
		for(GeoPoint p:kerbPoints_){
			maxCood[0] = (float) Math.max(p.getLocationX(),maxCood[0]);
			maxCood[1] = (float) Math.max(p.getLocationY(),maxCood[1]);
			maxCood[2] = 0.5f;//(float) Math.max(p.getLocationZ(),maxCood[2]);
			
			minCood[0] = (float) Math.min(p.getLocationX(),minCood[0]);
			minCood[1] = (float) Math.min(p.getLocationY(),minCood[1]);
			minCood[2] = 0.0f;//(float) Math.min(p.getLocationZ(),minCood[2]);
		}
		aabBox = new AABBox(minCood,maxCood);
	}
	public AABBox getAabBox(){
		return aabBox;
	}
	public List<GeoPoint> getKerbList(){
		return kerbPoints_;
	}
	
}
