package com.transyslab.simcore.mlp;

import java.util.LinkedList;
import java.util.List;

public class MLPVehPool{
	protected LinkedList<MLPVehicle> vehPool;
	
	public MLPVehPool() {
		vehPool = new LinkedList<MLPVehicle>();
	}
	
	public MLPVehicle generate() {
		MLPVehicle newVeh;
		if (!vehPool.isEmpty())
			newVeh = vehPool.poll();
		else 
			newVeh = new MLPVehicle();
		newVeh.updateUsage();
		MLPNetwork.getInstance().veh_list.add(newVeh);
		return newVeh;
	}
	
	public void recycle(MLPVehicle v) {
		MLPNetwork.getInstance().veh_list.remove(v);
		if (vehPool.size()<100) {
			v.clearMLPProperties();
			vehPool.offer(v);
		}
		else {
			v = null;
		}
	}
	
	public void recycleAll() {
		List<MLPVehicle> vList = MLPNetwork.getInstance().veh_list;
		if (!vList.isEmpty()) {
			for (MLPVehicle veh : vList) {
				veh.clearMLPProperties();
				vehPool.offer(veh);
			}
			vList.clear();
		}
	}
	
	public int countIdle(){
		return vehPool.size();
	}
}
