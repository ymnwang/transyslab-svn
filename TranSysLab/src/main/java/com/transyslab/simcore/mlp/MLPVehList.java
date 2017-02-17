package com.transyslab.simcore.mlp;

import java.util.HashMap;

public class MLPVehList {
	private MLPVehicle head_ ;
	private MLPVehicle tail_ ;
	private int nVehicles_;  //number of vehicles 
	private int nPeakVehicles_;  //max number of vehicles 
	

	public MLPVehList() {
		head_ = null;
		tail_ = null;
		nVehicles_ = 0;
		nPeakVehicles_ = 0;

	}
	
	public static MLPVehList getInstance() {
		HashMap<String, Integer> hm = MLPNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return MLPNetworkPool.getInstance().getVhcList(threadid);
	}
	
	public void recycle(MLPVehicle pv)//put a veh into the list
	{
		pv.donePathIndex();
		
		//在此重设属性
		pv.trailing_ = null;
		pv.leading_ = tail_;
		
		if (tail_ == null){// no vehicle in the list
			tail_ = new MLPVehicle();
			tail_.trailing_ = pv;			
		}
		else{// at least one in the list
			head_ = pv;
		}
		tail_ = pv;
		nVehicles_ ++;		
	}
	
	public MLPVehicle recycle()  //get a vehicle from the list 
	{
		MLPVehicle pv;

		if (head_ != null) { // get head from the list
			pv = head_;
			if (tail_ == head_) { // the onle one vehicle in list
				head_ = tail_ = null;
			}
			else { // at least two vehicles in list
				head_ = head_.trailing_;
				head_.leading_ = null;
			}
			nVehicles_--;
		}
		else { // list is empty

			pv = new MLPVehicle();
			nPeakVehicles_++;
		}
		return (pv);
	}
	
	public MLPVehicle head(){
		return head_;
	}
	
	public MLPVehicle tail(){
		return tail_;
	}
	
	public int nVehicles(){
		return nVehicles_;
	}
	
	public int nPeakVehicles(){
		return nPeakVehicles_;
	}

}
