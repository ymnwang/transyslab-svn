package com.transyslab.simcore.mlp;

import java.util.LinkedList;

import com.transyslab.roadnetwork.Loop;

public class MLPLoop extends Loop{
	String detName;
	MLPLane lane;
	MLPSegment segment;
	MLPLink link;
	double displacement;
	double distance;
	LinkedList<Double> detectedSpds;
	
	public MLPLoop(){
		detectedSpds = new LinkedList<>();
	}
	public MLPLoop(MLPLane LN, MLPSegment Seg, MLPLink LNK, String name, double dsp){
		lane_ = LN;
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
			MLPLoop loop = new MLPLoop(ln, seg, lnk, name, dsp);
			loop.position_ = (float) present;
			mlp_network.loops.add(loop);
		}
	}
	public String detect(String time){//当处于seg边界上存在漏洞
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
