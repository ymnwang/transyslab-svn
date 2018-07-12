package com.transyslab.roadnetwork;

import java.util.ArrayList;
import java.util.List;

public class SignalPlan {
	protected int id;
	private List<SignalStage> stages;
	private List<Double> timeSeries;
	private double fTime;
	private double tTime;
	private int pointer;

	public SignalPlan(int id) {
		stages = new ArrayList<>();
		pointer = 0;
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void addStage(SignalStage stage) {
		stages.add(stage);
	}

	public void setTime(double ft, double tt, List<Double> ts) {
		this.fTime = ft;
		this.tTime = tt;
		this.timeSeries = ts;
		pointer = 0;
	}

	public boolean check(double t, int fLID, int tLID) {
		while (timeSeries.get(pointer) > t)
			pointer ++;
		return stages.get(Math.floorMod(pointer,stages.size())).checkDir(fLID,tLID);
	}

	public SignalStage findStage(int stageId) {
		return stages.stream().filter(s->s.getId()==stageId).findFirst().orElse(null);
	}

	public void addDir(int stageId, int fLID, int tLID) {
		SignalStage target = findStage(stageId);
		if (target!=null)
			target.addLIDPair(fLID,tLID);
	}

	public void deleteDir(int stageId, int fLID, int tLID) {
		SignalStage target = findStage(stageId);
		if (target!=null)
			target.deleteLIDPairs(fLID,tLID);
	}

	public void addStage(int stageId) {
		stages.add(new SignalStage(stageId));
	}

	public void deleteStage(int stageId) {
		stages.removeIf(s -> s.getId() == stageId);
	}

}
