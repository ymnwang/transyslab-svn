/**
 *
 */
package com.transyslab.roadnetwork;

import java.util.*;

import com.transyslab.commons.tools.SimulationClock;

/**
 * //-------------------------------------------------------------------- //
 * CLASS NAME: RN_SurvStation -- a surveillance station consists of // one or
 * more sensors. Surverllace station is stored in a sorted // list in each link
 * according to their distance from the end of the // link. The sorting is in
 * descending order (Upstream = // LongerDistance = Top)
 * //-----------------------
 *
 * @author YYL
 *
 */
public class SurvStation extends CodedObject {
	protected int type_; // sensor type 1:loop; 2:microwave;3:video;4:kakou
	protected int task_; // data items
	protected Segment segment_; // pointer to segment
	protected float distance_; // distance from the link end
	protected float zoneLength_; // length of detection zone
	protected float position_; // position in % from segment end
	protected List<Sensor> sensors_; // array of pointers to sensors
	protected boolean isLinkWide_; // (YYL) is it a section detector
	protected int nSensors_; // (YYL) number of sensors
	protected int index_; // (YYL) index in segment.survlist
	protected int sectionCount_;
	protected float sectionAvgSpeed_;
	protected float sectionSpeed_;
	protected float interval_;
	protected float measureTime_;
	protected List<Float> speedList_; // measure speed in specific time
										// interval_
	protected List<Integer> flowList_; // measure flow in specific time
										// interval_
	// protected List<Integer> vhcidList_ = new ArrayList<Integer>();
	public SurvStation() {
		index_ = -1;
		nSensors_ = 0;
		isLinkWide_ = true;
		sectionCount_ = 0;
		sectionAvgSpeed_ = 0;
		sectionSpeed_ = 0;
	}
	public int type() {
		return type_;
	}
	public boolean isLinkWide() {
		return isLinkWide_;
	}
	/*
	 * public int isForEqBus() { return (SENSOR_FOR_EQUIPPED_BUS & type_); }
	 * //IEM(Jul2) add 16 if for equipped buses only public int tasks(int flag
	 * /*= 0x0FFFFFFF) { return (task_ & flag); } public int atask() { return
	 * (task_ & SENSOR_AGGREGATE); } public int itask() { return (task_ &
	 * SENSOR_INDIVIDUAL); } public int stask() { return (task_ &
	 * SENSOR_SNAPSHOT); }
	 */

	public Segment segment() {
		return segment_;
	}
	public Link getLink() {
		// Returns the link contains the surveillance station
		return segment_.getLink();
	}

	public int nLanes() {
		return segment_.nLanes();
	}
	public int nSensors() {
		return sensors_.size();
	}
	public void addSensor(Sensor sensor) {
		sensors_.add(sensor);
	}

	// Returns pointer to the sensor in ith lane. It may be NULL if
	// there is no sensor in the ith lane.

	public Sensor getSensor(int i) {
		if (isLinkWide())
			return sensors_.get(0);
		else
			return sensors_.get(i);
	}

	// Connect ith point to the sensor

	public void setSensor(int i, Sensor s) {
		//
		if (isLinkWide())
			i = 0;
		sensors_.add(i, s);
	}
	public float getInterval() {
		return interval_;
	}
	public float distance() {
		return distance_;
	}
	public float zoneLength() {
		return zoneLength_;
	}
	public float position() {
		return position_;
	}
	public void nextMeasureTime() {
		measureTime_ = measureTime_ + interval_;
	}
	public float getMeasureTime() {
		return measureTime_;
	}
	public void resetMeasureTime(){
		measureTime_ = (float) SimulationClock.getInstance().getCurrentTime() + interval_;
		sectionAvgSpeed_ = 0;
		sectionSpeed_ = 0;
		sectionCount_ = 0;
	}
	public List<Float> getSpeedList() {
		return speedList_;
	}
	public List<Integer> getflowList() {
		return flowList_;
	}
	public int init(int ty, float iv, float zone, int seg, int code, float pos) {

		/*
		 * if (ToolKit::debug()) { cout << indent << "<" << ty << endc << ta <<
		 * endc << zone << endc << seg << endc << pos << ">" << endl; }
		 */

		type_ = ty; // sensor type
		interval_ = iv; // statistic time interval in seconds
		measureTime_ = (float) SimulationClock.getInstance().getCurrentTime() + interval_;
		zoneLength_ = zone; // * Parameter::lengthFactor(); in meter
		// position_ = (float) (1.0 - pos); // position in % from segment end

		setCode(code);
		speedList_ = new ArrayList<Float>();
		flowList_ = new ArrayList<Integer>();
		if ((segment_ = RoadNetwork.getInstance().findSegment(seg)) == null) {
			// cerr << "Error:: Unknown segment <" << seg << ">. ";
			return -1;
		}
		position_ = (float) ((1.0 - pos) * segment_.getLength());
		// (YYL)
		sensors_ = new ArrayList<Sensor>();
		distance_ = (float) (segment_.getDistance() + position_);
		/*
		 * if (isLinkWide()) { sensors_.add(new RN_Sensor()); sensors_.get(0). }
		 * else { int n = segment_.nLanes(); sensors_.reserve(n); while (n > 0)
		 * { n --; sensors_[n] = null; } }
		 */
		if (segment_.getSurvList() == null)
			segment_.survList_ = new ArrayList<SurvStation>();
		index_ = segment_.getSurvList().size();
		segment_.getSurvList().add(this);
		// 方便输出
		RoadNetwork.getInstance().addSurvStation(this);

		return 0;
	}/*
		 * public void print(){
		 *
		 * }
		 *
		 * public int cmp(CodedObject other){ RN_SurvStation surv =
		 * (RN_SurvStation)other; if (distance_ < surv.distance_) return 1; else
		 * if (distance_ > surv.distance_) return -1; else return 0; } public
		 * int cmp(int c){ return this.cmp(c); }
		 */

	// functions for incident detection
	/*
	 * public int prmGrpID() { return -1; } public int stationState() { return
	 * -1;}
	 *
	 * public void setprmGrpID(int n) { } public int setState(int n){ return 1;
	 * } public int clearState(int n) { return 1; } public int addState(int n) {
	 * return 1; } public int incDcrPersistCount() { return -1; }
	 */

	// computes the flow across the section - used in incident detection

	public int sumLaneCount() {
		int sum = 0;

		// int nlanes = nLanes();

		for (int i = 0; i < nSensors_; i++)
			sum += (getSensor(i)).getCount();

		return sum;
	}
	public float sumLaneAvgSpeed() {
		float sumspeed = 0.0f;
		// int nlanes = nLanes();
		for (int i = 0; i < nSensors_; i++) {
			sumspeed += (getSensor(i)).getAvgSpeed(0);
		}
		sumspeed = sumspeed / nSensors_;
		return sumspeed;
	}
	public int getSectionCount() {
		return sectionCount_;
	}
	public float getSectionAvgSpeed() {
		sectionAvgSpeed_ = sectionSpeed_ / (sectionCount_);
		return sectionAvgSpeed_;
	}
	public void sectionMeasure(float vehspeed) {
		sectionCount_++;
		// 转换为km/h
		vehspeed = 3.6f * vehspeed;
		if (vehspeed > Constants.SPEED_EPSILON) {
			sectionSpeed_ += 1.0f / vehspeed;
		}
		else {
			sectionSpeed_ += 1.0f / Constants.SPEED_EPSILON;
		}
	}/*
		 * public void addVehicleID(MESO_Vehicle pv){
		 * vhcidList_.add(pv.get_code()); }
		 */
	public void aggregate() {
		flowList_.add(sectionCount_);
		if (sectionCount_ != 0)
			sectionAvgSpeed_ = (sectionCount_) / sectionSpeed_;
		else
			sectionAvgSpeed_ = 0.0f;
		speedList_.add(sectionAvgSpeed_);
		sectionCount_ = 0;
		sectionSpeed_ = 0.0f;
	}

}
