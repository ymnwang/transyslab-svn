package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class JointLane {
	protected int jlNum;
	protected List<MLPLane> lanesCompose;
	public double stPtDsp;
	public double endPtDsp;

	public JointLane(){
		jlNum = 0;
		lanesCompose = new ArrayList<MLPLane>();
	}

	public JointLane(int num){
		jlNum = num;
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
						if (VehIterator.next().virtualType == 0) {
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
