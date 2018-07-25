package com.transyslab.roadnetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SignalStage {
	// TODO ´ýÉè¼Æ
	public static HashMap<String,int[]> mapDirInt;
	public static HashMap<String,Integer> mapDirString;
	private List<String> turnInfo;
	private List<int[]> linkIDPairs;
	private int id;
	private int planId;

//	protected double cycle;

	public SignalStage(int id) {
		this.id = id;
		this.linkIDPairs = new ArrayList<>();
		this.turnInfo = new ArrayList<>();
	}
	public void setPlanId(int planId){
		this.planId = planId;
	}
	public int getPlanId(){
		return this.planId;
	}
	public List<String> getTurnInfo(){
		return this.turnInfo;
	}
	public boolean checkDir(int fLinkID, int tLinkID) {
		return linkIDPairs.stream().anyMatch(i -> i[0]==fLinkID && i[1]==tLinkID);
	}

	public void addLIDPair(int fLID, int tLID) {
		linkIDPairs.add(new int[]{fLID,tLID});
	}
	public void addTurnInfo(String info){
		this.turnInfo.add(info);
	}

	public void deleteLIDPairs(int fLID, int tLID) {
		linkIDPairs.removeIf(p -> p[0]==fLID && p[1]==tLID);
	}

	public int getId(){
		return this.id;
	}

	public List<int[]> getLinkIDPairs () {
		return linkIDPairs;
	}
}
