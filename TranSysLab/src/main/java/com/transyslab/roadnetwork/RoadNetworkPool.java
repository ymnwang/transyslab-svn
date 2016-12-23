package com.transyslab.roadnetwork;

import java.util.HashMap;
import java.util.Vector;
import com.transyslab.commons.tools.SimulationClock;

import com.transyslab.commons.tools.*;

public abstract class RoadNetworkPool {

	protected PathTable[] pathTableArray_;
	protected LinkTimes[] linkTimesArray_;
	protected Vector<Vector<Random>> randomArray_;
	protected HashMap<String, Integer> threadMap_;

	private static RoadNetworkPool theRNPool_;
	public static RoadNetworkPool getInstance() {
		return theRNPool_;
	}
	public void setInstance(RoadNetworkPool pool) {
		theRNPool_ = pool;
	}

	protected RoadNetworkPool() {
	}
	//
	public abstract void init(int n, RoadNetworkPool pool);

	public abstract void initArrays();

	public abstract RoadNetwork getNetwork(int i);
	public abstract Parameter getParameter(int i);
	public abstract ODTable getODTable(int i);
	public abstract SimulationClock getSimulationClock(int i);
	public abstract VehicleTable getVhcTable(int i);
	public HashMap<String, Integer> getHashMap() {
		return threadMap_;
	}
	public PathTable getPathTable(int i) {
		return pathTableArray_[i];
	}
	public LinkTimes getLinkTimes(int i) {
		return linkTimesArray_[i];
	}
	public Vector<Random> getRandom(int i) {
		return randomArray_.get(i);
	}

}
