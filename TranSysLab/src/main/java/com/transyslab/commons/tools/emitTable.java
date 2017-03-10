package com.transyslab.commons.tools;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.distribution.NormalDistribution;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.simcore.mlp.MLPLink;
import com.transyslab.simcore.mlp.MLPNetwork;
import com.transyslab.simcore.mlp.MLPSegment;
import com.transyslab.simcore.mlp.MLPSetup;

public class emitTable {
	protected LinkedList<Inflow> inflowlist_;
	protected Random r;
	
	public emitTable() {
		inflowlist_ = new LinkedList<Inflow>();
		r = new Random(System.currentTimeMillis());
		//r = new Random((long) 1);
	}
	public void createRndETables(){
		String filePath = MLPSetup.ODFormDir;
		String[] header = {"fLinkID","tLinkID","demand",
						   "fTime","tTime",
						   "mean","sd","vlim"};
		double startTime = SimulationClock.getInstance().getStartTime();
		try {
			List<CSVRecord> rows = CSVUtils.readCSV(filePath,header);
			for(int i = 1; i<rows.size(); i++){
				CSVRecord r = rows.get(i);
				int fLinkID = Integer.parseInt(r.get(0));
				int tLinkID = Integer.parseInt(r.get(1));
				int demand = Integer.parseInt(r.get(2));
				double [] time = {Double.parseDouble(r.get(3)) + startTime,
								  Double.parseDouble(r.get(4)) + startTime};
				double [] speed = {Double.parseDouble(r.get(5)),
								   Double.parseDouble(r.get(6)),
								   Double.parseDouble(r.get(7))};
				MLPLink thelink = (MLPLink) MLPNetwork.getInstance().findLink(fLinkID);
				int [] thelnIdxes = ((MLPSegment)thelink.getStartSegment()).getLaneIdxs();
				thelink.emtTable.generate(demand, speed, time, thelnIdxes, tLinkID);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readETables(){
		String filePath = MLPSetup.EmitFormDir;
		String[] header = {"laneID", "tLinkID", "time", "speed", "dis"};
		try {
			List<CSVRecord> rows = CSVUtils.readCSV(filePath,header);
			for(int i = 1; i<rows.size(); i++){
				CSVRecord r = rows.get(i);
				appendFromCSV(Integer.parseInt(r.get(0)),
										   Integer.parseInt(r.get(1)), 
										   Double.parseDouble(r.get(2)),
										   Double.parseDouble(r.get(3)),
										   Double.parseDouble(r.get(4)) );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void generate(int demand, double[] speed, double[] time, int[] laneIdxes, int tlnkID){
		double mean = speed[0];
		double sd = speed[1];
		double vlim = speed[2];
		double startTime = time[0];
		double endTime = time[1];
		int laneCount = laneIdxes.length;
		double simStep = SimulationClock.getInstance().getStepSize();
		int stepCount = (int) Math.floor((endTime-startTime)/simStep);		
		double expect =(double) demand/stepCount;
		NormalDistribution nd = new NormalDistribution(mean, sd);
		for (int i = 1; i<=stepCount; i++){
			if (r.nextDouble()<=expect){
				Inflow theinflow = new Inflow(startTime+i*simStep, 
											  Math.min(vlim, Math.max(0.01,nd.sample())), 
											  laneIdxes[r.nextInt(laneCount)],
											  tlnkID,
											  -1.0);
				inflowlist_.offer(theinflow);
			}
		}
	}

	private void appendFromCSV(int LnIdx, int TLnkID, double t, double v, double d){
		Inflow theinflow = new Inflow(LnIdx, TLnkID, t, v, d);
		inflowlist_.offer(theinflow);
	}
	
	public LinkedList<Inflow> getInflow() {
		return inflowlist_;		
	}
	
	public void setSeed(long val) {
		r.setSeed(val);
	}
	
	public void clearflow() {
		inflowlist_.clear();
	}
	
}
