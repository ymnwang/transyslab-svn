package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Loop;

public class MLPLoop extends Loop{
	String detName;
	MLPLane lane;
	MLPSegment segment;
	MLPLink link;
	double displacement;
	double distance;
//	LinkedList<Double> detectedSpds;
	LinkedList<double[]> records;
	
	public MLPLoop(){
//		detectedSpds = new LinkedList<>();
		records = new LinkedList<>();
	}
	public MLPLoop(MLPLane LN, MLPSegment Seg, MLPLink LNK, String name, double dsp){
		lane_ = LN;
		lane = LN;
		segment = Seg;
		link = LNK;
		displacement = dsp;
		detName = name;
		distance = Seg.endDSP - dsp;
//		detectedSpds = new LinkedList<>();
		records = new LinkedList<>();
	}
	public static void init(String name, int segID, double present) {
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
		double now = SimulationClock.getInstance().getCurrentTime();
		for (MLPVehicle veh : lane.vehsOnLn) {
			if (veh.VirtualType_ == 0 && veh.distance()>distance && veh.newDis<=distance) {
//				detectedSpds.add(veh.newSpeed);
				records.add(new double[] {now, veh.newSpeed});
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
/*	public double harmmeanNClear() {
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
	}*/
	public double calPeriodAvgSpd(double ftime, double ttime){
		double sum = 0.0, count = 0.0;
		for (double[] line : records) {
			if (line[0]>ftime && line[1]<=ttime) {
				count += 1;
				sum += line[1];
			}
		}
		if (count>0)
			return sum/count;
		else
			return 0.0;
	}
	public List<Double> getPeriodSpds(double ftime, double ttime){
		double sum = 0.0, count = 0.0;
		List<Double> ans = new ArrayList<>();
		for (double[] line : records) {
			if (line[0]>ftime && line[1]<=ttime) {
				ans.add(line[1]);
			}
		}
		return ans;
	}
	public double countPeriodFlow(double ftime, double ttime){
		double sum = 0.0;
		for (double[] line : records) {
			if (line[0]>ftime && line[1]<=ttime) {
				sum += 1.0;
			}
		}
		return sum;
	}
	protected void clearRecords() {
		records.clear();
	}
}
