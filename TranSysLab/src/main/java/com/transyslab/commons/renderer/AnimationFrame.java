package com.transyslab.commons.renderer;

import java.util.HashMap;
import java.util.LinkedList;
import com.transyslab.roadnetwork.VehicleData;

public class AnimationFrame{
	private int frameID_;
	private LinkedList<VehicleData> vhcDataQueue_;
	// 除车辆状态信息外，动画帧的其它信息
	private HashMap info;

	public AnimationFrame(){
		vhcDataQueue_ = new LinkedList<VehicleData>();
		info = new HashMap<String, Object>();
	}
	public LinkedList<VehicleData> getVhcDataQueue(){
		return vhcDataQueue_;
	}
	public void setInfo(String key,Object data){
		info.put(key,data);
	}
	public Object getInfo(String key){
		return info.get(key);
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
	public VehicleData getVehicleData(boolean needRetain){

		if(needRetain)//返回头部对象，不做移除
			return vhcDataQueue_.peekFirst();
		else//从头部移除对象
			return vhcDataQueue_.pollFirst();
	}

	public void clean(){
		frameID_ = 0;
		vhcDataQueue_.clear();
	}
}
