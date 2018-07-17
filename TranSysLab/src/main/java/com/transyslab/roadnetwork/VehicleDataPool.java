package com.transyslab.roadnetwork;

import java.util.concurrent.ConcurrentLinkedQueue;

//VehicleData对象池，用于回收和产生VehicleData
public class VehicleDataPool {
	
	private static VehicleDataPool vhcDataPool_ = new VehicleDataPool();;
	private int counter; // ConcurrentLinkedQueue.size()需要遍历集合，效率较低
	//线程安全队列
	private ConcurrentLinkedQueue<VehicleData> recycleList_;
	
	private VehicleDataPool(){
		recycleList_ = new ConcurrentLinkedQueue<>();
		counter = 0;
	}
	
	public static VehicleDataPool getInstance(){
		return vhcDataPool_;
	}
	public void recycle(VehicleData vd){
		vd.clean();
		recycleList_.offer(vd);
		counter ++;
	}
	public VehicleData newData() /* get a vehicle from the list */
	{
		VehicleData vd;
		if (recycleList_.isEmpty()) { // list is empty
			vd = new VehicleData();
		}
		else { // get head from the list
			vd = recycleList_.poll();
			counter --;
		}
		return vd;
	}
	public int nRows(){
		return counter;
	}
}
