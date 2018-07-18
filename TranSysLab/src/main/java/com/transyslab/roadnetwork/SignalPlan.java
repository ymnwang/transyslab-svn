package com.transyslab.roadnetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignalPlan {
	protected int id;
	private List<SignalStage> stages;
	private List<double[]> signalTable;
	private double fTime;
	private double tTime;
	private double amberTime;

	public SignalPlan(int id) {
		stages = new ArrayList<>();
		signalTable = new ArrayList<>();
		this.id = id;
		this.amberTime = 3.0;
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

	public void setFTime(double ft) {
		this.fTime = ft;
	}

	public void setTTime(double tt) {
		this.tTime = tt;
	}

	public boolean check(double t, int fLID, int tLID) {
		SignalStage stage = findStage(t);
		return (stage!=null && stage.checkDir(fLID,tLID));
	}

	public boolean beingApplied(double now) {
		return (fTime <= now && now < tTime);
	}

	public SignalStage findStage(int stageId) {
		return stages.stream().filter(s->s.getId()==stageId).findFirst().orElse(null);
	}

	private int findSID(double time) {
		double[] tmp = signalTable.stream().filter(r -> r[1]<=time && r[2]>time).findFirst().orElse(null);
		return (tmp==null ? -1 : (int)tmp[0]);
	}

	public SignalStage findStage(double time) {
		return findStage(findSID(time));
	}

	public double[] getStageTimeTable(double time) {
		double[] tmp = signalTable.stream().filter(r -> r[1]<=time && r[2]>time).findFirst().orElse(null);
		return tmp;
	}

	public boolean isAmber(double time) {
		double[] tmp = signalTable.stream().filter(r -> r[1]<=time && r[2]>time).findFirst().orElse(null);
		return (tmp!=null && tmp[2]-time<=amberTime);
	}

	public void addStage(int stageId) {
		stages.add(new SignalStage(stageId));
	}

	public void deleteStage(int stageId) {
		stages.removeIf(s -> s.getId() == stageId);
	}

	public void addSignalRow(int sid, double ft, double tt) {
		signalTable.add(new double[] {sid, ft, tt});
	}

	public double getAmberTime() {
		return amberTime;
	}

	public void setAmberTime(double amberTime) {
		this.amberTime = amberTime;
	}

}
