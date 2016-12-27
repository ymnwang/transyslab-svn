package com.transyslab.simcore.mlp;

import com.transyslab.roadnetwork.Vehicle;

public class MLPVehicle extends Vehicle{
	protected MLPVehicle trailing_;// upstream vehicle
	protected MLPVehicle leading_;// downstream vehicle
	
	public MLPVehicle(){
		trailing_ = null;
		leading_ = null;
	}
}
