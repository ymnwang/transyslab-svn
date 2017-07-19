package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Lane;
import com.transyslab.roadnetwork.Loop;

public class MLPLoop extends Loop{
	String detName;
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
	public MLPLoop(MLPLane LN, MLPSegment Seg, MLPLink LNK, String name, double dsp, double present){
		this.lane = LN;
		lane = LN;
		segment = Seg;
		link = LNK;
		displacement = dsp;
		position = present;
		detName = name;
		distance = Seg.endDSP - dsp;
//		detectedSpds = new LinkedList<>();
		records = new LinkedList<>();
	}
	public String detect(double timeNow){//当处于seg边界上存在漏洞
		String str = "";
		String timeStr = String.format("%.1f", timeNow);
		for (MLPVehicle veh : ((MLPLane) lane).vehsOnLn) {
			if (veh.virtualType == 0 && veh.getDistance()>distance && veh.newDis<=distance) {
//				detectedSpds.add(veh.newSpeed);
				records.add(new double[] {timeNow, veh.newSpeed});
				str += timeStr + "," +
						   veh.getId() + "," +
						   veh.virtualType + "," +
						   veh.newSpeed + "," + 
						   ((MLPLane) lane).getLnPosNum() + "," +
						   link.getId() + "," +
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
			if (line[0]>ftime && line[0]<=ttime) {
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
			if (line[0]>ftime && line[0]<=ttime) {
				ans.add(line[1]);
			}
		}
		return ans;
	}
	public double countPeriodFlow(double ftime, double ttime){
		double sum = 0.0;
		for (double[] line : records) {
			if (line[0]>ftime && line[0]<=ttime) {
				sum += 1.0;
			}
		}
		return sum;
	}
	protected void clearRecords() {
		records.clear();
	}
}
