/**
 *
 */
package com.transyslab.roadnetwork;

import java.util.Date;
import java.util.HashMap;

import org.apache.commons.math3.ml.neuralnet.Network;

import com.transyslab.commons.tools.SimulationClock;

/**
 * LinkTime
 *
 * @author YYL 2016-6-4
 */
public class LinkTimes {

	protected String filename_; // the latest file name
	protected int mode_;// 新增属性，mode=0：默认模式，仿真结果只统计一次，mode=1：自定义模式，
						// 集计时间间隔按设定执行

	protected int infoStartPeriod_; // the start interval
	protected long infoStartTime_; // start time of link time table
	protected long infoStopTime_; // stop time of link time table
	protected long infoPeriodLength_; // length of each period (second)
	protected long infoTotalLength_; // total length
	protected int infoPeriods_; // number of time periods

	// Travel time are all measured in seconds.

	protected float[] linkTimes_; // 2D travel times on each link
	protected float[] avgLinkTime_; // 1D average travel time

	// 0 = used only when the vehicle passes a info node (e.g. vms)
	// 1 = also used as pretrip.
	// default = 1

	protected int preTripGuidance_;
	public static LinkTimes getInstance() {
		HashMap<String, Integer> hm = RoadNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return RoadNetworkPool.getInstance().getLinkTimes(threadid);
	}
	public LinkTimes() {
		linkTimes_ = null;
		avgLinkTime_ = null;
		infoPeriods_ = 0;
		preTripGuidance_ = 1;
		filename_ = null;
		// 自定义设置模式
		mode_ = 1;
	}/*
		 * private RN_LinkTimes(String filename){ linkTimes_ = null;
		 * avgLinkTime_ = null; infoPeriods_ = 0; preTripGuidance_ = 1;
		 * if(filename!=null){ filename_ = filename; } else filename_ = null; }
		 */
	public void initTravelTimes() {
		// 待完善的处理，割掉了读文件中每条link的traveltime信息
		if (mode_ != 0) {

			// Parameters will be read from the file

			// read(filename_);
			// start column
			infoStartPeriod_ = 0;

			// length in seconds per period
			infoPeriodLength_ = 300;
			// number of colomns
			infoPeriods_ = 49;
			infoTotalLength_ = infoPeriods_ * infoPeriodLength_;
			infoStartTime_ = Math.round(SimulationClock.getInstance().getStartTime())
					+ infoStartPeriod_ * infoPeriodLength_;
			infoStopTime_ = infoStartTime_ + infoTotalLength_;
			// link长度/freespeed
			calcLinkTravelTimes();

		}
		else {

			infoStartPeriod_ = 0;
			infoStartTime_ = Math.round(SimulationClock.getInstance().getStartTime());
			infoStopTime_ = Math.round(SimulationClock.getInstance().getStopTime());
			infoPeriodLength_ = infoStopTime_ - infoStartTime_;
			infoTotalLength_ = infoPeriodLength_;
			infoPeriods_ = 1;

			calcLinkTravelTimes();
		}
		/*
		 * if ((theSpFlag & INFO_FLAG_DYNAMIC) && infoPeriods() < 2) { // cerr
		 * << "Warning:: Time invariant link travel times used " // <<
		 * "as dynamic routing information." << endl // << "    File: " <<
		 * filename_ << endl // << " Sp Flag: " << theSpFlag << endl; }
		 */
	}
	public Date toDate(int period) {
		int hour = (int) ((infoStartTime_ + period * infoPeriodLength_) / 3600);
		int minute = (int) (((infoStartTime_ + period * infoPeriodLength_) - hour * 3600) / 60);
		int second = (int) ((infoStartTime_ + period * infoPeriodLength_) - hour * 3600 - minute * 60);
		// 月份为真实月份-1
		// 2016年1月25日
		Date date = new Date(2016 - 1900, 0, 21, hour, minute, second);
		return date;
	}
	public LinkTimes(LinkTimes rnt) {
		// 未处理
		// this = rnt;
	}

	// RN_LinkTimes& operator=(const RN_LinkTimes& rnt);

	/*
	 * public void filename(const char *f) { if (f != filename_) { if
	 * (filename_) delete [] filename_; filename_ = strdup(f); } }
	 */

	public String getFilename() {
		return filename_;
	}

	public int nLinks() {
		return RoadNetwork.getInstance().nLinks();
	}
	public int nNodes() {
		return RoadNetwork.getInstance().nNodes();
	}
	public int nDestNodes() {
		return RoadNetwork.getInstance().nDestNodes();
	}

	public int infoStartPeriod() {
		return infoStartPeriod_;
	}
	public long infoStartTime() {
		return infoStartTime_;
	}
	public long infoStopTime() {
		return infoStopTime_;
	}
	public int infoPeriods() {
		return infoPeriods_;
	}
	public long infoPeriodLength() {
		return infoPeriodLength_;
	}
	public long infoTotalLength() {
		return infoTotalLength_;
	}

	public int whichPeriod(double t) {
		// Returns the interval that contains time t
		int p = (int) t;
		if (p <= infoStartTime_) { // earlier
			return 0;
		}
		else if (p >= infoStopTime_) { // later
			return infoPeriods_ - 1;
		}
		else { // between
			return (int) ((t - infoStartTime_) / infoPeriodLength_);
		}
	}
	// Returns the time that represents interval p.
	public double whatTime(int p) {
		return infoStartTime_ + (p + 0.5) * infoPeriodLength_;
	}

	public float linkTime(Link i, double timesec) {
		return linkTime(i.getIndex(), timesec);
	}
	// Returns the expected link travel time at the given entry time
	public float linkTime(int k, double timesec) {
		if (infoPeriods_ > 1) {
			float[] y = linkTimes_;
			// float y = linkTimes_[k * infoPeriods_];
			float dt = (float) ((timesec - infoStartTime_) / infoPeriodLength_ + 0.5);
			int i = (int) dt;
			if (i < 1) {
				return y[k * infoPeriods_];
			}
			else if (i >= infoPeriods_) {
				return y[k * infoPeriods_ + infoPeriods_ - 1];
			}
			else {
				dt = (float) (timesec - infoStartTime_ - (i - 0.5) * infoPeriodLength_);
				float z = y[k * infoPeriods_ + i - 1];
				return z + (y[k * infoPeriods_ + i] - z) / infoPeriodLength_ * dt;
			}
		}
		else {
			return linkTimes_[k];
		}
	}
	// Returns the average travel time on a given link
	public float avgLinkTime(Link i) {
		// average
		return avgLinkTime(i.getIndex());
	}
	public float avgLinkTime(int i) {
		return avgLinkTime_[i];
	}

	// This function is called by Graph::labelCorrecting(...)

	public float cost(int i, double timesec) {
		return linkTime(i, timesec);
	}
	public float cost(int i) {
		return avgLinkTime_[i];
	}
	// Create the default travel times for each link. This function
	// should be called only once.
	public void calcLinkTravelTimes() {
		// should called only once
		int i, j, n = nLinks();

		if (linkTimes_ == null) {
			linkTimes_ = new float[nLinks() * infoPeriods_];
		}

		for (i = 0; i < n; i++) {
			float x = RoadNetwork.getInstance().getLink(i).getGenTravelTime();
			for (j = 0; j < infoPeriods_; j++) {
				linkTimes_[i * infoPeriods_ + j] = x;
			}
		}

		if (infoPeriods_ > 1) {
			if (avgLinkTime_ == null)
				avgLinkTime_ = new float[n];
			for (i = 0; i < n; i++) {
				float sum = 0;
				for (j = 0; j < infoPeriods_; j++) {
					sum += linkTimes_[i * infoPeriods_ + j];
				}
				avgLinkTime_[i] = sum / infoPeriods_;
			}
		}
		else {
			avgLinkTime_ = linkTimes_;
		}		
		update2Graph(RoadNetwork.getInstance());
	}
	// Update the link travel times. The result is a linear combination
	// of the previous data and new data calculated in the simulation
	// (e.g., based on the average of the expected travel times of all
	// vehicles that are currently in the link or based on the sensors
	// data collected in the most recent time interval, depends on the
	// current value of RN_Link::travelTime() is defined in the
	// derived class).
	public void updateLinkTravelTimes(float alpha) {
		float x;
		float[] py = linkTimes_;
		int i, j, n = nLinks();
		int k = whichPeriod(SimulationClock.getInstance().getCurrentTime());
		for (i = 0; i < n; i++) {
			Link pl = RoadNetwork.getInstance().getLink(i);

			// Calculate travel time based speed/density relationship

			float z = pl.calcCurrentTravelTime();

			x = pl.generalizeTravelTime(z);

			// Update travel time for all future intervals using the same
			// estimates. This is simulating the system that provides
			// guidance based on prevailing/instaneous traffic condition.

			for (j = k; j < infoPeriods_; j++) {
				// py = linkTimes_ + i * infoPeriods_ + j;
				py[i * infoPeriods_ + j] = (float) (alpha * x + (1.0 - alpha) * (py[i * infoPeriods_ + j]));
			}

			if (infoPeriods_ > 1) {
				avgLinkTime_[i] = x;
			}
		}
		update2Graph(RoadNetwork.getInstance());
	}
	// Read link travel time from a file. This is another way to update
	// the link travel time.
	public void read(String filename, float alpha) {

	}
	// Print time dependent travel times
	public void printLinkTimes() {

	}

	public void preTripGuidance(int flag) {
		preTripGuidance_ = flag;
	}
	public int preTripGuidance() {
		return preTripGuidance_;
	}
	//wym 将结果更新至RoadNetwork图结构中
	protected void update2Graph(RoadNetwork theRN) {		
		for (int i = 0; i < avgLinkTime_.length; i++) {
			Link theLink = theRN.getLink(i);
			theRN.setEdgeWeight(theLink, avgLinkTime_[i]);
		}
	}

}
