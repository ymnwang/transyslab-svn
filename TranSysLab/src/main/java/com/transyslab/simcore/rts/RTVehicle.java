package com.transyslab.simcore.rts;

import com.transyslab.roadnetwork.Link;
import com.transyslab.roadnetwork.Vehicle;

/**
 * Created by ITSA405-35 on 2018/5/29.
 */
public class RTVehicle extends Vehicle{
	private RTLink curLink;
	private RTLane curLane;
	@Override
	public Link getLink() {
		return curLink;
	}
	public void init(int id, RTLane lane,double curSpeed, double distance){
		this.id = id;
		this.currentSpeed = curSpeed;
		this.distance = distance;
		this.curLane = lane;
	}
	public String toString(){
		return Integer.toString(id) + Long.toString(curLane.getId()) + Double.toString(distance);
	}
}
