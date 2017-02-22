package com.transyslab.commons.tools;

import com.transyslab.simcore.mlp.MLPNetwork;

public class Inflow {
	public double time;
	public double speed; // unit m/s
	public int laneIdx; 
	public int tLinkID;
	public double dis;
	
	public Inflow(double[] row){
		if (row.length != 5) {
			System.err.println("length not match");
			return;
		}
		time = row[0];
		speed = row[1];
		int laneID = (int) row[2];
		laneIdx = MLPNetwork.getInstance().findLane(laneID).index();
		tLinkID = (int) row[3];
		if ( row[4] < 0.0)
			dis = MLPNetwork.getInstance().mlpLane(laneIdx).getLength();
		else
			dis = row[4];
	}
	
	public Inflow(double t, double v, int LnIdx, int TLnkID, double d){
		time = t;
		speed = v;
		laneIdx = LnIdx;
		tLinkID = TLnkID;
		if ( d < 0.0)
			dis = MLPNetwork.getInstance().mlpLane(laneIdx).getLength();
		else
			dis = d;
	}
	
	public Inflow(int LNID, int TLnkID, double t, double v, double d){
		time = t;
		speed = v;
		laneIdx = MLPNetwork.getInstance().findLane(LNID).index();
		tLinkID = TLnkID;
		if ( d < 0.0)
			dis = MLPNetwork.getInstance().mlpLane(laneIdx).getLength();
		else
			dis = d;
	}
}
