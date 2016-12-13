/**
 *
 */
package com.transyslab.roadnetwork;

/**
 * VehicleTable extern VehicleTable * theVehicleTable;
 *
 * @author YYL 2016-6-4
 */
public class VehicleTable {
	protected static String name_;
	protected double startTime_;// dep time smaller than this is skipped
	protected double nextTime_;// time to read od pairs

	public VehicleTable() {

	}
	// Open trip table file and create a OD Parser
	// public void open(double start_time = -86400.0);
	// Read trip tables for next time period
	/*
	 * public double read(){ const char *fn ; if (parser_) { // continue parsing
	 * of current file fn = NULL ; } else { // open the new file parser_ = new
	 * VehicleTableParser(this); fn = name_; } while (nextTime_ <=
	 * theSimulationClock->currentTime()) { parser_->parse(fn); } return
	 * nextTime_; }
	 */

	public static String getName() {
		return name_;
	}
	// public static char** nameptr() { return &name_; }

	public double startTime() {
		return startTime_;
	}
	public double nextTime() {
		return nextTime_;
	}

	public boolean skip() {
		return (nextTime_ < startTime_);
	}
	// Called by parser to setup the od matrix and update time for next
	// vehicles departure.
	public int init(double s) {
		if (nextTime_ > -86400.0) { // && ToolKit::debug()) {
			// cout << nVehiclesParsed_
			// << " Vehicles parsed at "
			// << theSimulationClock->convertTime(nextTime_)
			// << "." << endl;
		}

		nVehiclesParsed_ = 0;
		nextTime_ = s;
		return 0;
	}

	// These may has to be overloaded by derived class

	// public RN_Vehicle* newVehicle() = 0;

	// private VehicleTableParser* parser_; // the parser
	private int nVehiclesParsed_; // vehicles parsed in this round
}
