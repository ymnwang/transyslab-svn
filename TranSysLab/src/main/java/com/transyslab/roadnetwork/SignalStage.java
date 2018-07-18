package com.transyslab.roadnetwork;

import java.util.ArrayList;
import java.util.List;

public class SignalStage {
	private List<int[]> linkIDPairs;
	private int id;

//	protected double cycle;

	public SignalStage(int id) {
		this.id = id;
		linkIDPairs = new ArrayList<>();
	}

	public boolean checkDir(int fLinkID, int tLinkID) {
		return linkIDPairs.stream().anyMatch(i -> i[0]==fLinkID && i[1]==tLinkID);
	}

	public void addLIDPair(int fLID, int tLID) {
		linkIDPairs.add(new int[]{fLID,tLID});
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
