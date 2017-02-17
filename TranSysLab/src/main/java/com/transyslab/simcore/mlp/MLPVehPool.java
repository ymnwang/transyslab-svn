package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/*import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

public class MLPVehPool extends GenericKeyedObjectPool{
	
	//public static GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();

	public MLPVehPool(MLPVehFactory factory) {
		super(factory);
		//config.setMaxTotalPerKey((int) 1e5);
	}
	
	public MLPVehicle generate(int key){
		try {			
			return (MLPVehicle) borrowObject(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}	
	public void recycle(int key, MLPVehicle v) {
		returnObject(key, v);
	}
	
}*/

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
}
