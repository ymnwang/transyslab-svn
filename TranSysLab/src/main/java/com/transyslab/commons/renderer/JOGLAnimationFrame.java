package com.transyslab.commons.renderer;

import java.util.LinkedList;
import com.transyslab.roadnetwork.VehicleData;

public class JOGLAnimationFrame{
	private int frameID_;
	private LinkedList<VehicleData> vhcDataQueue_;

	public JOGLAnimationFrame(){
		vhcDataQueue_ = new LinkedList<VehicleData>();
	}
	public LinkedList<VehicleData> getVhcDataQueue(){
		
		return vhcDataQueue_;
	}
	public void setFrameID(int id){
		frameID_ = id;
	}
	public int getFrameID(){
		return frameID_;
	}
	public void addVehicleData(VehicleData vd){
		//从尾部插入对象
		vhcDataQueue_.offerLast(vd);
	}
	public VehicleData getVehicleData(){
		//从头部移除对象
		return vhcDataQueue_.pollFirst();
	}
	public void clean(){
		frameID_ = 0;
		vhcDataQueue_.clear();
	}
}
