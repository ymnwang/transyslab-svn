package com.transyslab.commons.renderer;

import java.util.HashMap;
import java.util.LinkedList;
import com.transyslab.roadnetwork.VehicleData;

public class AnimationFrame{
	private int frameID_;
	private LinkedList<VehicleData> vhcDataQueue_;
	// ������״̬��Ϣ�⣬����֡��������Ϣ
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
		//��β���������
		vhcDataQueue_.offerLast(vd);
	}
	public VehicleData getVehicleData(boolean needRetain){

		if(needRetain)//����ͷ�����󣬲����Ƴ�
			return vhcDataQueue_.peekFirst();
		else//��ͷ���Ƴ�����
			return vhcDataQueue_.pollFirst();
	}

	public void clean(){
		frameID_ = 0;
		vhcDataQueue_.clear();
	}
}
