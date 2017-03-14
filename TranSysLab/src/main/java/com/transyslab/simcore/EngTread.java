package com.transyslab.simcore;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Parameter;
import com.transyslab.roadnetwork.RoadNetwork;

public abstract class EngTread extends Thread{
	protected String name;
	public Parameter parameter;
	public RoadNetwork network;
	public SimulationEngine engine;
	public SimulationClock sim_clock;
	
	public abstract void doPatch();
}
