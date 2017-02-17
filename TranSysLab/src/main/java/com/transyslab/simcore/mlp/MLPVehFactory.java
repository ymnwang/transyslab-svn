package com.transyslab.simcore.mlp;

import org.apache.commons.pool2.*;
import org.apache.commons.pool2.impl.DefaultPooledObject;

//key==0����ʵ����key==1�����⳵
public class MLPVehFactory extends BaseKeyedPooledObjectFactory<Integer,MLPVehicle>{

	@Override
	public MLPVehicle create(Integer arg0) throws Exception {
		// TODO Auto-generated method stub
		MLPVehicle mlpv = new MLPVehicle();
		return mlpv;
	}

	@Override
	public PooledObject<MLPVehicle> wrap(MLPVehicle arg0) {
		// TODO Auto-generated method stub
		return new DefaultPooledObject<MLPVehicle>(arg0);
	}
}