package com.transyslab.roadnetwork;

import java.util.LinkedList;

public class VehicleDataPool {
	private static VehicleDataPool vhcDataPool_;
	private int nRows_;
	private LinkedList<VehicleData> recycleList_;
	
	private VehicleDataPool(){
		recycleList_ = new LinkedList<VehicleData>();
		nRows_ = 0;
	}
	
	public static VehicleDataPool getVehicleDataPool(){
		if(vhcDataPool_==null)
			vhcDataPool_ = new VehicleDataPool();
		return vhcDataPool_;
	}
	public void recycleVehicleData(VehicleData vd){
		vd.clean();
		recycleList_.offerLast(vd);
	}
	public VehicleData getVehicleData() /* get a vehicle from the list */
	{
		VehicleData vd;

		if (recycleList_.isEmpty()) { // list is empty
			vd = new VehicleData();
			nRows_++;
		}
		else { // get head from the list
			vd = recycleList_.pollFirst();
		}
		return vd;
	}
	public int nRows(){
		return nRows_;
	}
}
