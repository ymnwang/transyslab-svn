package com.transyslab.simcore.mlp;

import java.util.HashMap;

import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.Vehicle;

public class MLPVehicle extends Vehicle{
	protected MLPVehicle trailing_;// upstream vehicle
	protected MLPVehicle leading_;// downstream vehicle
	
	protected static int[] vhcCounter_ = new int[Constants.THREAD_NUM];  	//在网车辆数统计
	
	public MLPVehicle(){
		trailing_ = null;
		leading_ = null;
	}
	
	public static void setVehicleCounter(int vhcnum){
		HashMap<String, Integer> hm = MLPNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		vhcCounter_[threadid] = vhcnum;
	}

}
