package com.transyslab.roadnetwork;

import java.util.ArrayList;
import java.util.List;

public class Surface extends CodedObject{
	protected List<Point> kerbPoints_;
	protected int segmentId_;
	protected int index_;
	public Surface(){
		
	}
	public void init(int code, int segmentid) {
		setCode(code);
		segmentId_ = segmentid;
		kerbPoints_ = new ArrayList<Point>();
		index_ = RoadNetwork.getInstance().nSurfaces();
		RoadNetwork.getInstance().addSurface(this);
	}
	public void addKerbPoint(Point p){
		kerbPoints_.add(p);
		RoadNetwork.getInstance().getWorldSpace().recordExtremePoints(p);
	}
	public void translateInWorldSpace(WorldSpace world_space) {
		for(Point p:kerbPoints_){
			world_space.translateWorldSpacePoint(p);
		}
	}
	public List getKerbList(){
		return kerbPoints_;
	}
	
}
