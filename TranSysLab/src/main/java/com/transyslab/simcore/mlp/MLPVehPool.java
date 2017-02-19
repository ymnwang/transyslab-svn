package com.transyslab.simcore.mlp;

import java.util.LinkedList;

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
	
	public int countIdle(){
		return vehPool.size();
	}
}
