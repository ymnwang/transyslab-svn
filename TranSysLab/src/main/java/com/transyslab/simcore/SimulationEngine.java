package com.transyslab.simcore;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;

public abstract class SimulationEngine {

	protected String master_;
	protected static int chosenOutput_;
	protected int state_;

	protected int mode_;

	protected int nBreakPoints_; // number of break points
	protected double[] breakPoints_; // preset break points
	protected int nextBreakPoint_;

	public SimulationEngine() {
		master_ = null;
		state_ = Constants.STATE_NOT_STARTED;
		chosenOutput_ = 0;
		mode_ = 0;
		nBreakPoints_ = 0;
		breakPoints_ = null;
		nextBreakPoint_ = 0;
	}
	public abstract int simulationLoop();
	public abstract void loadFiles();
	public void state(int s) {
		state_ = s;
	}
	public int state() {
		return state_;
	}

	// public int isDemoMode() { return mode_ &
	// (DefinedConstant.MODE_DEMO|MODE_DEMO_PAUSE); }
	public int mode(int mask) {
		return (mode_ & mask);
	}
	public void unsetMode(int s) {
		mode_ &= ~s;
	}
	public void setMode(int s) {
		mode_ |= s;
	}

	public static int chosenOutput(int mask) {
		return (chosenOutput_ & mask);
	}
	public void chooseOutput(int mask) {
		chosenOutput_ |= mask;
	}
	public void removeOutput(int mask) {
		chosenOutput_ &= ~mask;
	}

	public final String title() {
		return master_;
	}
	public void setMaster(String name) {
		master_ = name;
	}
	public final String getMaster() {
		return master_;
	}
	public String ext() {
		return "";
	}

	// Returns 0 if no error, negative if fatal error and positive
	// if warning error

	public int superLoadMasterFile() {
		// 找到masterfile的真实路径
		/*
		 * if (master_!=null) { // find real name realname =
		 * ExpandEnvVars(master_); if (! HasStrFromRight(realname, ext())) {
		 * realname = Str("%s%s", realname, ext()); } warning = 1; // show
		 * warning message if error } else { // no specified, use default
		 * realname = Str("master%s", ext()); warning = 0; }
		 */

		setMaster(master_);
		return 0;
		/*
		 * if (ToolKit.fileExists(master_)) { return 0; } else if (warning) {
		 * return -1; } else { return 1; }
		 */
	}
	public int loadSimulationFiles() {
		return 0;
	}

	public int canStart() {
		if (master_ == null)
			return 1;
		return 0;
		// 检查master是否为有效路径
		/*
		 * final String name = Str("master%s", ext()); return
		 * ToolKit.fileExists(name);
		 */
	}/*
		 * public int isRunning() { if
		 * (!(SimulationClock.getInstance().isPaused()>0 &&
		 * SimulationClock.getInstance().isStarted()>0)) { return 1; } else {
		 * return 0; } }
		 */

	public boolean isWaiting() {
		return SimulationClock.getInstance().isWaiting();
	}

	public int start() {
		// SimulationClock.getInstance().init();
		// SimulationClock.getInstance().start();
		return 0;
	}

	// This call simulationLoop in a loop. You have to overload this
	// function in graphical mode. In batch mode, this function
	// would be good enough. This function will NOT return until the
	// simulation is done.

	public void run() {
		while (simulationLoop() >= 0);
	}

	// One step of the simulation. This function needs to be
	// overloaded in derived class to do the real things. The dummy
	// function just prints the current time in the console window.

}
