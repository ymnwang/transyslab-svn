package com.transyslab.commons.tools;

import com.transyslab.simcore.mlp.MLPNetwork;

public class Inflow {
	public double time;
	public double speed; // unit m/s
	public int laneIdx; 
	public int tLinkID;
	public double dis;
	
	public Inflow(double[] row){
		time = row[0];
		speed = row[1];
		int laneID = (int) row[2];
		laneIdx = MLPNetwork.getInstance().findLane(laneID).index();
		tLinkID = (int) row[3];
		if (row.length<=4)
			dis = MLPNetwork.getInstance().mlpLane(laneIdx).getLength();
		else
			dis = row[4];
	}
	
	public Inflow(double t, double v, int LnIdx, int TLnkID, double d){
		time = t;
		speed = v;
		laneIdx = LnIdx;
		tLinkID = TLnkID;
		dis = d;
	}
	
	public Inflow(double t, double v, int LnIdx, int TLnkID){
		time = t;
		speed = v;
		laneIdx = LnIdx;
		tLinkID = TLnkID;
		dis = MLPNetwork.getInstance().mlpLane(LnIdx).getLength();
	}
}
