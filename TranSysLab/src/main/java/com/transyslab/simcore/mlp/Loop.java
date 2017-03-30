package com.transyslab.simcore.mlp;

import java.util.LinkedList;

public class Loop {
	String detName;
	MLPLane lane;
	MLPSegment segment;
	MLPLink link;
	double displacement;
	double distance;
	LinkedList<Double> detectedSpds;
	
	public Loop(){
		detectedSpds = new LinkedList<>();
	}
	public Loop(MLPLane LN, MLPSegment Seg, MLPLink LNK, String name, double dsp){
		lane = LN;
		segment = Seg;
		link = LNK;
		displacement = dsp;
		detName = name;
		distance = Seg.endDSP - dsp;
		detectedSpds = new LinkedList<>();
	}
	public void init(String name, int segID, double present) {
		MLPNetwork mlp_network = MLPNetwork.getInstance();
		MLPSegment seg = (MLPSegment) mlp_network.findSegment(segID);
		MLPLink lnk = (MLPLink) seg.getLink();
		double dsp = seg.startDSP + seg.getLength()*present;
		for (int i = 0; i < seg.nLanes(); i++) {
			MLPLane ln = (MLPLane) seg.getLane(i);
			mlp_network.loops.add(new Loop(ln, seg, lnk, name, dsp));
		}
	}
	public String detect(String time){
		String str = "";
		for (MLPVehicle veh : lane.vehsOnLn) {
			if (veh.VirtualType_ == 0 && veh.distance()>distance && veh.newDis<=distance) {
				detectedSpds.add(veh.newSpeed);
				str += time + "," +
						   veh.getCode() + "," +  
						   veh.VirtualType_ + "," + 
						   veh.newSpeed + "," + 
						   lane.getLnPosNum() + "," +
						   link.getCode() + "," +
						   displacement + "\r\n";
			}
		}
		return str;
	}
	public double harmmeanNClear() {
		if (detectedSpds.isEmpty()) {
			return 0.0;
		}
		else {
			double n = detectedSpds.size();
			double sum = 0.0;
			while (!detectedSpds.isEmpty()) {
				sum += 1.0 / detectedSpds.poll();
			}
			return (n/sum);
		}
	}
}
