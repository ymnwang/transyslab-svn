/**
 *
 */
package com.transyslab.roadnetwork;

import java.util.HashMap;

/**
 * VehicleTable extern VehicleTable * theVehicleTable;
 *
 * @author YYL 2016-6-4
 */
public class VehicleTable {

	protected ODPair od_;
	protected Path path_;

	public VehicleTable() {

	}
	//ÐèÒªÖØÐ´
	public void init(int o, int d, int pid){

	}
	public ODPair getODPair(){
		return od_;
	}
	public Path getPath(){
		return path_;
	}
	public static VehicleTable getInstance(){
		HashMap<String, Integer> hm = RoadNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return RoadNetworkPool.getInstance().getVhcTable(threadid);
	}

}
