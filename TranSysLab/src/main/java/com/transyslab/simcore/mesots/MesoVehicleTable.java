/**
 *
 */
package com.transyslab.simcore.mesots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.transyslab.roadnetwork.*;

/**
 * @author its312
 *
 */
public class MesoVehicleTable {
	protected List<MesoVehicle> vhcList;
	protected ODPair odPair;
	protected Path path;
	
	public MesoVehicleTable() {
	}
	public ODPair getODPair(){
		return odPair;
	}
	public Path getPath(){
		return path;
	}

	//重写init方法，根据车辆od，路径id来生成车辆
	public void init(int o, int d, int pid){
		// TODO 待实现, 考虑车辆的不同OD
		vhcList = new ArrayList<MesoVehicle>();
		/*
	    Node ori = MesoNetwork.getInstance().findNode(o);
	    Node des = MesoNetwork.getInstance().findNode(d);
	    odPair = new ODPair(ori,des);
	    path = PathTable.getInstance().findPath(pid);*/
	}
	public List<MesoVehicle> getVhcList(){
		return vhcList;
	}

}
