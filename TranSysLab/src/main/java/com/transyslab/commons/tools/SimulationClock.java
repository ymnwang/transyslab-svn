package com.transyslab.commons.tools;

import java.util.HashMap;
import java.util.TimeZone;

import com.transyslab.roadnetwork.RoadNetworkPool;
import com.transyslab.simcore.mlp.MLPEngThread;

public class SimulationClock {
	final int ONE_DAY = 86400;

	public static int NUM_STEPS = 5;
	public static int CLK_TCK = 1000;
	protected static long baseTime_; // time of 00:00:00AM today
	protected static long localTime_; // current local clock time

	protected double startTime_; // simulation start time
	protected double stopTime_; // simulation stop time
	protected double simulationTime_; // total simultion time
	protected double stepSize_; // simulation step size
	protected double currentTime_; // current simulation clock time
	protected double masterTime_; // current time of master clock

	protected int started_; // 1 if already started
	protected int paused_; // 1 if paused

	protected String startStringTime_;
	protected String stopStringTime_;

	// structure tms is defined in <sys/times.h> and filled by
	// calling times(&cputms_)

	// protected Struct tms cputimes_; // time-accounting information

	protected long baseClkTcks_; // returned by first call of times();

	// These three are filled in advance(double), which is assumed
	// to be called in every simulation cycle once and only once.

	protected long currentClkTcks_; // returns by last call of times();
	protected long lastStepCpuClkTcks_; // in CLK_TCKs
	protected long lastStepRealClkTcks_; // in CLK_TCKs

	protected double lastStepSize_; // last step size

	// These variables implements a circular array for calculate
	// average real time per step

	protected long[] latestStepRealClkTcks_;
	protected double[] latestStepSimTime_;
	protected int nSteps_;
	protected int front_;
	protected int rear_;
	protected long sumClkTcks_;
	protected double sumSimTime_;

	private double actualTimeFactor_; // running/real time
	private int desiredClkTcksPerStep_; // num of clk_tcks
	private double desiredTimeFactor_; // running/real time
	private int last = 0;
	// private int nice = ToolKit.NICE_PRIMARY;

	public SimulationClock() {
		actualTimeFactor_ = 0.0f;
		desiredTimeFactor_ = 0.0f;
		desiredClkTcksPerStep_ = 0;
		started_ = 0;
		paused_ = 1;
		masterTime_ = 86400.0;
		startTime_ = 12 * 3600f;
		stopTime_ = 14 * 3600 + 50 * 60f;
		startStringTime_ = null;
		stopStringTime_ = null;

		baseClkTcks_ = 0;
		currentClkTcks_ = 0;
		lastStepCpuClkTcks_ = 1;
		lastStepRealClkTcks_ = 1;
		lastStepSize_ = 1;

		latestStepRealClkTcks_ = new long[NUM_STEPS];
		latestStepSimTime_ = new double[NUM_STEPS];
		nSteps_ = 0;
		front_ = 0;
		rear_ = -1;
		sumClkTcks_ = 0;
		sumSimTime_ = 0.0f;
	}
	/*
	 * public SimulationClock(double start, double stop, double step = 1.0) {
	 * init(start, stop, step); }
	 */

	// Conversion of time strings
	/*
	 * public static double convertTime(String t) { double ss = 0.0; char []s;
	 * char [] buf=new char[12]; strncpy(buf, t, 12); if (s = strrchr(buf, ':'))
	 * { ss = atof(s[1]); s[0] = '\0'; } else { return atof(buf); } if (s =
	 * strrchr(buf, ':')) { ss += atof(s[1]) * 60; s[0] = '\0'; } else { return
	 * ss + atof(buf) * 60; } ss += atof(buf) * 3600; return ss; }
	 *
	 * public final String convertTimeLong(double t) { //static char[] buffer =
	 * new char[12]; char[] buffer = new char[12]; int hh, mm; int ss = ( int)t;
	 * t -= ss; hh = ss / 3600; ss %= 3600; mm = ss / 60; ss %= 60; t += ss;
	 * sprintf(buffer, "%2.2d:%2.2d:%04.1lf", hh, mm, t); return buffer; }
	 */

	public void init() {
		init(startTime_, stopTime_, stepSize_);
	}
	/*
	 * public void read(Reader is) { double start, stop, step; start =
	 * is.gettime(); stop = is.gettime(); step = is.gettime(); if (init(start,
	 * stop, step)==1) { is.reference(); } }
	 */
	public int init(double start, double stop, double step)// 1.0
	{
		setBaseTime();

		if (start > stop || step < 0.0) {
			return 1;
		}

		lastStepSize_ = stepSize_ = step;
		currentTime_ = start;

		setStartTime(start);
		setStopTime(stop);

		// double n = stepSize_ * desiredTimeFactor_ * CLK_TCK;
		// desiredClkTcksPerStep_ = (long)n;

		// These deals with the real time and cpu times

		currentClkTcks_ = baseClkTcks_;// = times(cputimes_); // in CLK_TCKs

		return 0;
	}

	public void setStepSize(double step) {
		stepSize_ = step;
		// double n = stepSize_ * desiredTimeFactor_ * CLK_TCK;
		// desiredClkTcksPerStep_ = (int)n;
	}

	public double getMasterTime() {
		return masterTime_;
	}
	public void setMasterTime(double m) {
		masterTime_ = m;
	}
	public boolean isWaiting() {
		return (currentTime_ >= masterTime_);
	}

	public final String startStringTime() {
		return startStringTime_;
	}

	public final String stopStringTime() {
		return stopStringTime_;
	}
	/*
	 * public final String currentStringTime() { return
	 * convertTimeLong(currentTime()); }
	 */

	public void setStartTime(double t) {
		startTime_ = t;
		currentTime_ = t;
		simulationTime_ = stopTime_ - startTime_;
		// Copy(startStringTime_, convertTime(startTime_));
	}
	public void setStopTime(double t) {
		stopTime_ = t;
		simulationTime_ = stopTime_ - startTime_;
	}
	// Copy(stopStringTime_, convertTime(stopTime_));
	/*
	 * public void startTime(String t) { startTime_ = convertTime(t);
	 * simulationTime_ = stopTime_ - startTime_; // Copy(startStringTime_, t); }
	 * public void stopTime(String t) { stopTime_ = convertTime(t);
	 * simulationTime_ = stopTime_ - startTime_; // Copy(stopStringTime_, t); }
	 */
	public double getStartTime() {
		return startTime_;
	}
	public double getStopTime() {
		return stopTime_;
	}
	public double getStepSize() {
		return stepSize_;
	}
	/*
	 * public double desiredRealTimePerStep() { return (stepSize_ *
	 * desiredTimeFactor_); } public long desiredClkTcksPerStep() { return
	 * desiredClkTcksPerStep_; }
	 */
	// returns 1 if it took a nice nap

	// public int advance() { return advance(stepSize_); }

	public void advance(double step) {
		// static long cycle_clks = 0;
		// long cycle_clks = 0;
		// This deals with the simulated time

		currentTime_ += step;
		int sec10 = (int) (10.0 * currentTime_ + 0.5);
		currentTime_ = sec10 / 10.0;
		lastStepSize_ = step;
		// simtime不能被step整除
		// These deal with the real time and cpu times

		/*
		 * long prev_clktcks = currentClkTcks_;
		 *
		 * // Struct tms now; currentClkTcks_ = times(now); time_t sec =
		 * time(null); // in seconds localTime_ = localtimesec;
		 *
		 *
		 * // We assume that the flap point is prev_clktcks (actual flap // time
		 * could be larger than prev_clktcks).
		 *
		 * if (currentClkTcks_ < prev_clktcks) { cycle_clks += prev_clktcks -
		 * baseClkTcks_; baseClkTcks_ = 0; } currentClkTcks_ += cycle_clks;
		 *
		 * lastStepRealClkTcks_ = currentClkTcks_ - prev_clktcks;
		 * actualTimeFactor_ = (double)lastStepRealClkTcks_ / (double)CLK_TCK /
		 * step;
		 *
		 * lastStepCpuClkTcks_ = (now.tms_utime + now.tms_stime) -
		 * (cputimes_.tms_utime + cputimes_.tms_stime);
		 *
		 * memcpy(cputimes_, now, sizeof(struct tms));
		 *
		 * // Record the real time of the last step and calculate the sum of //
		 * real time of the lastest several steps
		 *
		 * if (nSteps_ < NUM_STEPS) { nSteps_ ++ ; rear_ ++ ; } else {
		 * sumClkTcks_ -= latestStepRealClkTcks_[front_]; sumSimTime_ -=
		 * latestStepSimTime_[front_]; front_ = (front_ + 1) % NUM_STEPS; rear_
		 * = (rear_ + 1) % NUM_STEPS; } sumClkTcks_ += lastStepCpuClkTcks_;
		 * sumSimTime_ += step; latestStepRealClkTcks_[rear_] =
		 * lastStepCpuClkTcks_; latestStepSimTime_[rear_] = step;
		 *
		 * return take_a_nice_nap();
		 */
	}

	public void desiredTimeFactor(double x) {
		double n = stepSize_ * x * CLK_TCK;
		desiredTimeFactor_ = x;
		desiredClkTcksPerStep_ = (int) n;
	}
	public double desiredTimeFactor() {
		return desiredTimeFactor_;
	}
	public double actualTimeFactor() {
		return actualTimeFactor_;
	}
	public double getCurrentTime() {
		return currentTime_;
	}
	public void setCurrentTime(double x) {
		currentTime_ = x;
	}

	/*
	 * public double cpuTimeSinceStart() { tms tmp; times(tmp); return
	 * (double)(tmp.tms_utime + tmp.tms_stime) / (double)CLK_TCK; }
	 */

	// These two functions return time information for last interval
	// and are measured in ClkTcks.

	public long lastStepCpuClkTcks() {
		return lastStepCpuClkTcks_;
	}
	public long lastStepRealClkTcks() {
		return lastStepRealClkTcks_;
	}

	// These two returns time in seconds

	public double lastStepCpuTime() {
		return (double) lastStepCpuClkTcks_ / (double) CLK_TCK;
	}

	public double lastStepRealTime() {
		return (double) lastStepRealClkTcks_ / (double) CLK_TCK;
	}

	/*
	 * public double lastStepCpuUsage() { if (lastStepRealClkTcks!=0) { return
	 * (double)lastStepCpuClkTcks_ / (double)lastStepRealClkTcks_; } else {
	 * return 0.0; } }
	 */

	/*
	 * public double recentCpuUsage() { if (sumSimTime_ > 0.01) { return
	 * (double)sumClkTcks_ / (double)CLK_TCK / sumSimTime_; } else { return 0; }
	 * }
	 */

	// The returned value of this function is used to register
	// callbacks.

	/*
	 * public long desiredCallbackTimeIntervals() { final double regulator =
	 * 0.8; // static int num = 0; int num = 0; if (desiredClkTcksPerStep_ <= 0)
	 * num = 1; else {
	 *
	 * // The 10 is convert CLT_TCK to milliseconds
	 *
	 * double inc = num + 10.0 * regulator * (desiredClkTcksPerStep_ -
	 * lastStepRealClkTcks_); num = (inc > 1.0) ? (int)(inc) : (1); }
	 *
	 * return num; }
	 */

	// Force the program to sleep for nSeconds

	/*
	 * public void delay(double nSeconds) { double num = nSeconds * CLK_TCK;
	 *
	 * // This is really a stupid way to slow down the simulation.
	 *
	 * struct tms tmp; for (long start = times(tmp), now = start, stop = start +
	 * (int)num; now < stop; now = times(tmp)) { // Loop until nSeconds expires
	 * } }
	 */

	// Force the program to sleep for a certain period of time if
	// necessary to achieve desired speed factor. This function
	// should be called after advance() is called. It returns 1 if
	// it has imposed an artificial delay (simulation is run too
	// faster) or 0 if no artificial delay has been imposed.
	/*
	 * public int wait() { final double epsilon = 0.01; if (desiredTimeFactor_ <
	 * epsilon) return 0; double dt = waitTime(); if (dt < epsilon) return 0;
	 * else delay(dt); return 1; }
	 */
	/*
	 * public double waitTime() { return desiredTimeFactor_ * lastStepSize_ -
	 * lastStepRealTime(); }
	 *
	 * public void start() { started_ = 1; paused_ = 0; } public int isStarted()
	 * { return started_; } public void pause() { paused_ = 1; } public void
	 * resume() { paused_ = 0; } public int isPaused() { return paused_; }
	 *
	 * // Returns time in seconds of the SIMULATED time
	 *
	 * public double timeSimulated() { return (currentTime_ - startTime_); }
	 *
	 * public double timeToBeSimulated() { return (stopTime_ - currentTime_); }
	 *
	 * // Return time in seconds of the REAL TIME
	 *
	 * public double timeSinceStart() { return (double)(currentClkTcks_ -
	 * baseClkTcks_) / (double)CLK_TCK; }
	 *
	 * public double expectedTimeToGo() { double done = currentTime_ -
	 * startTime_; double togo = stopTime_ - currentTime_; if (done > 60.0) {
	 * return togo * timeSinceStart() / done; } else { return 0; } }
	 *
	 * // Return current clock time with 00:00:00AM TODAY as reference // point
	 * (Not 00:00:00AM on January 1st, 1970 returned by // function time())
	 *
	 * public int currentClockTime() { long seconds =localTime_.tm_sec +60 *
	 * localTime_.tm_min +3600 * localTime_.tm_hour;
	 *
	 * return seconds; }
	 *
	 * public double expectedClockTimeToStop() { return (currentClockTime() +
	 * expectedTimeToGo()); }
	 *
	 * public final String clockTimeSinceStart() { return
	 * convertTime(timeSinceStart()); }
	 *
	 * public final String clockTimeNow() { return
	 * convertTime(currentClockTime()); }
	 *
	 * public final String clockTimeStop() { return
	 * convertTime(currentClockTime() + expectedTimeToGo()); }
	 *
	 * public double percentLeft() { return 100.0 * (stopTime_ - currentTime_) /
	 * simulationTime_; }
	 *
	 * public double percentDone() { return 100.0 * (currentTime_ - startTime_)
	 * / simulationTime_; }
	 *
	 * public double progress() { return (currentTime_ - startTime_) /
	 * simulationTime_; }
	 *
	 * public int take_a_nice_nap() {
	 *
	 * // nap time (1/100 sec) in each iteration for 3 nice level
	 *
	 * static int scale[] = { 0, // dedicated (night and weekend) 10 * CLK_TCK /
	 * 1000, // primary 20 * CLK_TCK / 1000 // shared };
	 *
	 * // Decide if run the program in nice mode
	 *
	 * //static int nice = ToolKit.NICE_PRIMARY; // static long last = 0; //
	 * Only adjust the parameter every 10 seconds
	 *
	 * if (ToolKit.nice() >= ToolKit.NICE_AUTOMATIC) { // automatic if
	 * (currentClkTcks_ > last + CLK_TCK * 10) { if (localTime_.tm_hour < 9 ||
	 * // 00:00 -- 09:00AM Everyday localTime_.tm_hour > 19 || // 07:00 --
	 * 12:00PM Everyday localTime_.tm_wday == 0 || // Sunday localTime_.tm_wday
	 * == 6) { // Saturday nice = ToolKit.NICE_DEDICATED; // Set to dedicated
	 * mode } else { nice = ToolKit.NICE_PRIMARY; // Set to primary mode } } }
	 * else { nice = ToolKit.nice(); }
	 *
	 * if (nice) { sginap(scale[nice]); return 1; }
	 *
	 * return 0; }
	 */
	private void setBaseTime() {
		// These deal with simulated time

		baseTime_ = System.currentTimeMillis() / (1000 * 3600 * 24) * (1000 * 3600 * 24)
				- TimeZone.getDefault().getRawOffset();
		localTime_ = System.currentTimeMillis();;

		// Reference time of 00:00:00AM today

		// baseTime_ -= currentClockTime();
	}
	public static SimulationClock getInstance() {
		RoadNetworkPool pool = RoadNetworkPool.getInstance();
		if (pool != null) {
			HashMap<String, Integer> hm = pool.getHashMap();
			int threadid = hm.get(Thread.currentThread().getName()).intValue();
			return RoadNetworkPool.getInstance().getSimulationClock(threadid);
		}
		else {
			return ((MLPEngThread) Thread.currentThread()).sim_clock;
		}
		
	}
	public double getDuration() {
		return simulationTime_;
	}
}
