package com.transyslab.simcore.mlp;

public class Loop {
	MLPLane lane;
	MLPSegment segment;
	MLPLink link;
	double displacement;
	double distance;
	
	public Loop(){
		
	}
	public Loop(MLPLane LN, MLPSegment Seg, MLPLink LNK, double dsp){
		lane = LN;
		segment = Seg;
		link = LNK;
		displacement = dsp;
		distance = Seg.endDSP - dsp;
	}
	public String detect(){
		String str = "";
		for (MLPVehicle veh : lane.vehsOnLn) {
			if (veh.distance()>distance && veh.newDis<=distance) {
				str += veh.getCode() + "," +  
						   veh.VirtualType_ + "," + 
						   veh.newSpeed + "," + 
						   lane.getLnPosNum() + "," +
						   link.getCode() + "," +
						   displacement + "\r\n";
			}
		}
		return str;
	}
}
