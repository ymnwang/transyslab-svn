/**
 *
 */
package com.transyslab.simcore.mesots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.transyslab.roadnetwork.Node;
import com.transyslab.roadnetwork.ODPair;
import com.transyslab.roadnetwork.PathTable;
import com.transyslab.roadnetwork.Vehicle;
import com.transyslab.roadnetwork.VehicleTable;

/**
 * @author its312
 *
 */
public class MesoVehicleTable extends VehicleTable {
	protected List<MesoVehicle> vhcList_;
	
	public MesoVehicleTable() {
	}

	public Vehicle newVehicle() {
		return MesoVehicleList.getInstance().recycle();
	}
	//重写init方法，根据车辆od，路径id来生成车辆
	public void init(int o, int d, int pid){
		vhcList_ = new ArrayList<MesoVehicle>();
	    Node ori = MesoNetwork.getInstance().findNode(o);
	    Node des = MesoNetwork.getInstance().findNode(d);
	    od_ = new ODPair(ori,des);
	    path_ = PathTable.getInstance().findPath(pid);
	}
	public List<MesoVehicle> getVhcList(){
		return vhcList_;
	}
	public static MesoVehicleTable getInstance() {
		HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return MesoNetworkPool.getInstance().getVhcTable(threadid);
	}

}
