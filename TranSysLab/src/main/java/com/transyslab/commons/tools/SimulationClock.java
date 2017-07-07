package com.transyslab.commons.tools;

import java.util.HashMap;
import java.util.TimeZone;


public class SimulationClock {

	protected static long baseTime; // time of 00:00:00AM today
	protected static long localTime; // current local clock time

	protected double startTime; // simulation start time
	protected double stopTime; // simulation stop time
	protected double simulationTime; // total simultion time
	protected double stepSize; // simulation step size
	protected double currentTime; // current simulation clock time
	protected double masterTime; // current time of master clock

	public SimulationClock() {
		masterTime = 86400.0;
		startTime = 12 * 3600f;
		stopTime = 14 * 3600 + 50 * 60f;
	}


	/*public void init() {
		init(startTime, stopTime, stepSize);
	}*/

	public int init(double start, double stop, double step)// 1.0
	{
		setBaseTime();

		if (start > stop || step < 0.0) {
			return 1;
		}
		stepSize = step;
		currentTime = start;

		setStartTime(start);
		setStopTime(stop);

		return 0;
	}

	public void setStepSize(double step) {
		stepSize = step;

	}

	public double getMasterTime() {
		return masterTime;
	}
	public void setMasterTime(double m) {
		masterTime = m;
	}
	public boolean isWaiting() {
		return (currentTime >= masterTime);
	}


	public void setStartTime(double t) {
		startTime = t;
		currentTime = t;
		simulationTime = stopTime - startTime;
	}
	public void setStopTime(double t) {
		stopTime = t;
		simulationTime = stopTime - startTime;
	}

	public double getStartTime() {
		return startTime;
	}
	public double getStopTime() {
		return stopTime;
	}
	public double getStepSize() {
		return stepSize;
	}


	public void advance(double step) {
		// static long cycle_clks = 0;
		// long cycle_clks = 0;
		// This deals with the simulated time

		currentTime += step;
		int sec10 = (int) (10.0 * currentTime + 0.5);
		currentTime = sec10 / 10.0;

	}

	public double getCurrentTime() {
		return currentTime;
	}



	private void setBaseTime() {
		// These deal with simulated time

		baseTime = System.currentTimeMillis() / (1000 * 3600 * 24) * (1000 * 3600 * 24)
				- TimeZone.getDefault().getRawOffset();
		localTime = System.currentTimeMillis();;

		// Reference time of 00:00:00AM today

	}
	public double getDuration() {
		return simulationTime;
	}
}
