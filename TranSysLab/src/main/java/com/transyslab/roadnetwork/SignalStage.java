package com.transyslab.roadnetwork;

import org.apache.commons.collections.map.HashedMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignalStage {

	private Map<int[],String> linkPairs2Direction;
	private List<int[]> linkIDPairs;
	private int id;
	private int planId;

//	protected double cycle;

	public SignalStage(int id) {
		this.id = id;
		this.linkIDPairs = new ArrayList<>();
		this.linkPairs2Direction = new HashedMap();
	}
	public void setPlanId(int planId){
		this.planId = planId;
	}
	public int getPlanId(){
		return this.planId;
	}
	public String getDirection(int[] linkIdPair){
		return this.linkPairs2Direction.get(linkIdPair);
	}
	public List<String> getDirections(){
		return (List)linkPairs2Direction.values();
	}
	public boolean checkDir(long fLinkID, long tLinkID) {
		return linkIDPairs.stream().anyMatch(i -> i[0]==fLinkID && i[1]==tLinkID);
	}

	public void addLIDPair(int fLID, int tLID, String direction) {
		int[] linkIdPairs = new int[]{fLID,tLID};
		linkIDPairs.add(linkIdPairs);
		linkPairs2Direction.put(linkIdPairs,direction);
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
