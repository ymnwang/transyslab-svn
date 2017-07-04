package com.transyslab.commons.tools;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;

import com.transyslab.simcore.mlp.*;
import org.apache.commons.csv.CSVRecord;

import com.transyslab.commons.io.CSVUtils;

public class EMTTable {
	protected LinkedList<Inflow> inflowlist_;
	
	public EMTTable() {
		inflowlist_ = new LinkedList<>();
	}
	public static void createRndETables(){
		String filePath = MLPSetup.getODFormDir();
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

	public static void readETables(){
		String filePath = MLPSetup.getEmitForm_fileName();
		String[] header = {"laneID", "tLinkID", "time", "speed", "dis", "rvId"};
		try {
			List<CSVRecord> rows = CSVUtils.readCSV(filePath,header);
			int theLNID = Integer.MIN_VALUE;
			MLPLane theLN = null;
			MLPLink theLNK = null;
			for(int i = 1; i<rows.size(); i++){
				CSVRecord r = rows.get(i);
				int LNID = Integer.parseInt(r.get(0));
				if (theLNID != LNID) {
					theLN = (MLPLane) MLPNetwork.getInstance().findLane(LNID);
					theLNK = (MLPLink) theLN.getLink();
					theLNID = LNID;
				}
				theLNK.emtTable.appendFromCSV(LNID,
										   Integer.parseInt(r.get(1)), 
										   Double.parseDouble(r.get(2)),
										   Double.parseDouble(r.get(3)),
										   Double.parseDouble(r.get(4)),
										   Integer.parseInt(r.get(5)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void generate(int demand, double[] speed, double[] time, int[] laneIdxes, int tlnkID){
		Random r = MLPNetwork.getInstance().sysRand;
		double mean = speed[0];
		double sd = speed[1];
		double vlim = speed[2];
		double startTime = time[0];
		double endTime = time[1];
		int laneCount = laneIdxes.length;
		double simStep = SimulationClock.getInstance().getStepSize();
		int stepCount = (int) Math.floor((endTime-startTime)/simStep);		
		double expect =(double) demand/stepCount;
//		NormalDistribution nd = new NormalDistribution(mean, sd);		
		for (int i = 1; i<=stepCount; i++){
			if (r.nextDouble()<=expect){
				Inflow theinflow = new Inflow(startTime+i*simStep, 
											  Math.min(vlim, Math.max(0.01,r.nextGaussian()*sd+mean)), 
											  laneIdxes[r.nextInt(laneCount)],
											  tlnkID,
											  -1.0);
				inflowlist_.offer(theinflow);
			}
		}
	}

	private void appendFromCSV(int LnIdx, int TLnkID, double t, double v, double d, int RVID){
		Inflow theinflow = new Inflow(LnIdx, TLnkID, t, v, d);
		theinflow.RVID = RVID;
		inflowlist_.offer(theinflow);
	}
	
	public LinkedList<Inflow> getInflow() {
		return inflowlist_;
	}
	
	public void clearflow() {
		inflowlist_.clear();
	}
	
}
