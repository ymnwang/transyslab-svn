package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class JointLane {
	public int LPNum;
	public List<MLPLane> lanesCompose;
	public double stPtDsp;
	public double endPtDsp;

	public JointLane(){
		LPNum = 0;
		lanesCompose = new ArrayList<MLPLane>();
	}

	public JointLane(int num){
		LPNum = num;
		lanesCompose = new ArrayList<MLPLane>();
	}

	public boolean emptyVeh(){
		ListIterator<MLPLane> iterator = lanesCompose.listIterator();
		while (iterator.hasNext()) {
			if (!iterator.next().vehsOnLn.isEmpty()){
				return false;
			}			
		}
		return true;
	}

	public MLPVehicle getFirstVeh() {
		ListIterator<MLPLane> iterator = lanesCompose.listIterator();
		while (iterator.hasNext()) {
			MLPLane lane = iterator.next();
			if (!lane.vehsOnLn.isEmpty()){
				return lane.getHead();
			}
		}
		return null;
	}
}
