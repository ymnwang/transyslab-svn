package com.transyslab.simcore.mlp;

public class Inflow {
	public double time;
	public double speed; // unit m/s
	public int laneIdx; 
	public long tLinkID;
	public double dis;
	public int realVID;
	
	public Inflow(double[] row, MLPNetwork mlpNetwork){
		if (row.length != 5) {
			System.err.println("length not match");
			return;
		}
		time = row[0];
		speed = row[1];
		int laneID = (int) row[2];
		laneIdx = mlpNetwork.findLane(laneID).getIndex();
		tLinkID = (int) row[3];
		if ( row[4] < 0.0)
			dis = mlpNetwork.mlpLane(laneIdx).getLength();
		else
			dis = row[4];
	}
	
	public Inflow(double time, double speed, int laneIdx, long tLinkID, double dis){
		this.time = time;
		this.speed = speed;
		this.laneIdx = laneIdx;
		this.tLinkID = tLinkID;
		this.dis = dis;
	}

	public Inflow(double time, double speed, int laneIdx, long tLinkID, double dis, int realVID){
		this.time = time;
		this.speed = speed;
		this.laneIdx = laneIdx;
		this.tLinkID = tLinkID;
		this.dis = dis;
		this.realVID = realVID;
	}

}
