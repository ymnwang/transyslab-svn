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

	public boolean hasNoVeh(boolean virtualCount){
		ListIterator<MLPLane> iterator = lanesCompose.listIterator();
		while (iterator.hasNext()) {
			MLPLane LN = iterator.next();
			if (!LN.vehsOnLn.isEmpty()){
				if (!virtualCount) {
					ListIterator<MLPVehicle> VehIterator = LN.vehsOnLn.listIterator();
					while (VehIterator.hasNext()) {
						if (VehIterator.next().VirtualType_ == 0) {
							return false;
						}
					}
				}
				else
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
