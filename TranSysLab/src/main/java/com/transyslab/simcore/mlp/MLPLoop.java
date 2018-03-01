package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.HashMap;
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
	HashMap<Integer, Double> enterMap;
	LinkedList<double[]> records;
	
	public MLPLoop(){
		enterMap = new HashMap<>();
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
		enterMap = new HashMap<>();
		records = new LinkedList<>();
		this.createSurface();
	}
	public String detect(double timeNow){//当处于seg边界上存在漏洞(已修复)
		String str = "";
		String timeStr = String.format("%.1f", timeNow);
		//更新车头进入记录
		for (MLPVehicle veh : ((MLPLane) lane).vehsOnLn) {
			if (veh.virtualType == 0 &&
					veh.Displacement() < displacement &&
					veh.segment.endDSP - veh.newDis >= displacement) {
				enterMap.put(veh.getId(),timeNow);
			}
		}
		//更新车尾离开记录
		for (MLPVehicle veh : ((MLPLane) lane).vehsOnLn) {
			if (veh.virtualType == 0 &&
					veh.Displacement() - veh.getLength() < displacement &&
					veh.segment.endDSP - veh.newDis - veh.getLength() >= displacement) {
				Double timeEnter = enterMap.get(veh.getId());
				if (timeEnter == null) {
					System.out.println("Error: no enter record.");
				}
				else {
					enterMap.remove(veh.getId());
					double recordSpd = timeNow == timeEnter ? veh.newSpeed : veh.getLength() / (timeNow-timeEnter);
					records.add(new double[] {timeNow, recordSpd});
					str +=  detName + "," +
							timeStr + "," +
							veh.getId() + "," +
							veh.virtualType + "," +
							recordSpd + "," +
							((MLPLane) lane).getLnPosNum() + "," +
							link.getId() + "," +
							displacement + "\r\n";
				}
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
	public List<Double> getPeriodSpds(double ftime, double ttime, boolean dumpAfter){
		List<Double> ans = new ArrayList<>();
		records.stream().filter(l -> l[0]>ftime && l[0]<=ttime).forEach(r -> ans.add(r[1]));
		if (dumpAfter)
			records.removeAll(ans);
		return ans;
	}
	public double countPeriodFlow(double ftime, double ttime){
		/*double sum = 0.0;
		for (double[] line : records) {
			if (line[0]>ftime && line[0]<=ttime) {
				sum += 1.0;
			}
		}
		return sum;*/
		return records.stream().filter(l -> l[0]>ftime && l[0]<=ttime).count();
	}
	protected void clearRecords() {
		enterMap.clear();
		records.clear();
	}
	public String getName(){
		return detName;
	}
	public LinkedList<double[]> getRecords(){
		return records;
	}
}
