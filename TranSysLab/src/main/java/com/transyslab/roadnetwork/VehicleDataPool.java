package com.transyslab.roadnetwork;

import java.util.concurrent.LinkedBlockingQueue;

//VehicleData对象池，用于回收和产生VehicleData
public class VehicleDataPool {
	
	private static VehicleDataPool vhcDataPool_ = new VehicleDataPool();;
	private int nRows_;
	//线程安全的双端队列，据说使用非互斥锁实现，可有效减少线程锁竞争
	private LinkedBlockingQueue<VehicleData> recycleList_;
	
	private VehicleDataPool(){
		recycleList_ = new LinkedBlockingQueue<VehicleData>();
		nRows_ = 0;
	}
	
	public static VehicleDataPool getVehicleDataPool(){
		return vhcDataPool_;
	}
	public void recycleVehicleData(VehicleData vd){
		vd.clean();
		recycleList_.offer(vd);
	}
	public VehicleData getVehicleData() /* get a vehicle from the list */
	{
		VehicleData vd;

		if (recycleList_.isEmpty()) { // list is empty
			vd = new VehicleData();
			nRows_++;
		}
		else { // get head from the list
			vd = recycleList_.poll();
		}
		return vd;
	}
	public int nRows(){
		return nRows_;
	}
}
