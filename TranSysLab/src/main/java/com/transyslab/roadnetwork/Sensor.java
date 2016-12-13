/**
 *
 */
package com.transyslab.roadnetwork;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

/**
 * Sensors
 *
 * @author YYL 2016-6-4
 *
 */
// NOTE! The following forward declarations are required because of
// the multiple inheritance structure problem defined at the end of
// RN_Sensor
public class Sensor extends CodedObject {

	protected int index_; // index in SurvStation array
	protected Lane lane_; // pointer to lane
	protected SurvStation station_; // station
	protected float prob_; // probability to function properly
	protected int state_; // occupied/working etc
	// protected int intervalType_; //IEM(Apr26) 0 for default, >=1 for
	// additional sensor
	// types, as defined in master.mitsim

	protected int count_; // counts
	protected float occupancy_; // occupied time
	protected float speed_; // sum of speeds
	protected float avgSpeed_;
	// protected float interval_; //(YYL) statistic time interval in seconds
	public Sensor() {
		// IEM(Apr26) added intervalType_ to be defaulted to 0
		// sorted_ = 1;
		lane_ = null;
		station_ = null;
		// interval_ = 5*60;
		index_ = -1;
		// state_ = 0;
		count_ = 0;
		occupancy_ = 0;
		speed_ = 0;
	}

	// public static int sorted() { return sorted_; }

	public int clearState(int flag) {
		return (state_ &= ~flag);
	}
	// Turn on particular bits
	public int setState(int flag) {
		return (state_ |= flag);
	}
	// used in incident detection
	public void addState(int flag) {
		// used in incident detection
		state_ += flag;
	}

	public int state() {
		return state_;
	}

	// Iterator on the global array
	/*
	 * public RN_Sensor prev(){ if (index_ > 0) return
	 * MESO_Network.getInstance().getSensor(index_ - 1); else return null; }
	 * public RN_Sensor next(){ if (index_ <
	 * (MESO_Network.getInstance().nSensors() - 1)) return
	 * MESO_Network.getInstance().getSensor(index_ + 1); else return null; }
	 */

	public int index() {
		return index_;
	}
	public SurvStation station() {
		return station_;
	}

	// public int intervalType() {return intervalType_;} //IEM(Apr26)

	public Lane lane() {
		return lane_;
	}
	public Segment getSegment() {
		return station_.segment();
	}
	public Link getLink() {
		return station_.segment().getLink();
	}
	// relative distance from the end of the segment
	public float position() {
		return station_.position();
	}
	// Returns 1 if the sensor is like-wide or 0 if it is lane specific
	public boolean isLinkWide() {
		return station_.isLinkWide();
	}
	public int getType() {
		return station_.type();
	}/*
		 * public int tasks(int flag ){ return station_.tasks(flag); } public
		 * int atask(){ return station_.atask(); } public int itask(){ return
		 * station_.itask(); } public int stask(){ return station_.stask(); }
		 */
	// Distance from the end of the link
	public float distance() {
		return station_.distance();
	}
	// Length of the detection zone
	public float zoneLength() {
		return station_.zoneLength();
	}
	public float getWidth() {
		if (station_.isLinkWide()) {
			return getSegment().nLanes() * Constants.LANE_WIDTH;
		}
		else {
			return Constants.LANE_WIDTH;
		}
	}
	// l=lanecode, c=LDTID
	// public int init(int c, float p, int l, int i){
	public int init(int c, int l) {

		/*
		 * if (ToolKit::debug()) { cout << indent << indent << "<" << c << endc
		 * << p << endc << l << ">" << endl; }
		 *
		 * if (station_! = null) { // cerr << "Error:: Sensor <" << c << "> " //
		 * << "cannot be initialized twice. "; return -1; } else { station_ =
		 * theNetwork.lastSurvStation(); }
		 *
		 * if (sorted_>0 && c <= last) sorted_ = 0; else last = c; code_ = c;
		 *
		 * prob_ = p; if (theRandomizers[Random::Misc]->brandom(prob_)) { state_
		 * = 0; } else { state_ = SENSOR_BROKEN; }
		 */

		setCode(c);
		station_ = RoadNetwork.getInstance().lastSurvStation();
		if (l < 0) {
			Segment ps = getSegment();

			// The central lane

			lane_ = RoadNetwork.getInstance().getLane(ps.getLeftLaneIndex() + ps.nLanes() / 2);

		}
		else {
			if ((lane_ = RoadNetwork.getInstance().findLane(l)) == null || (lane_.getSegment() != getSegment())) {
				// cerr << "Error:: Unknown lane ID <" << l << ">. ";
				return -1;
			}
		}

		// intervalType_ = i; //IEM(Apr26) Perhaps add error checking later.

		index_ = station_.nSensors();
		station_.addSensor(this);
		station_.nSensors_++;
		RoadNetwork.getInstance().addSensor(this);

		return 0;
	}
	// IEM(Apr26) added interval
	// IEM(Jun15) set it to 0
	public void laneMeasure(float curspeed) {
		count_++;
		speed_ = speed_ + curspeed;
	}
	// mode = 0，车道实际检测量；mode = 1，截面检测量的车道平均值
	public float getAvgSpeed(int mode) {
		if (mode == 1)
			return avgSpeed_;
		return speed_ / count_;
	}
	// isLinkwide == true调用
	public void measureFromStation() {
		count_ = station_.sectionCount_ / station_.nSensors_;
		avgSpeed_ = station_.sectionAvgSpeed_;
	}
	public int getCount() {
		return count_;
	}
	// public int flow() { return count_; }
	public float getOccupancy() {
		return occupancy_;
	}
	public float getSpeeds() {
		return speed_;
	}
	public void outputToOracle(PreparedStatement ps) throws SQLException {
		int num = station_.flowList_.size();
		int lanenum;
		// 卡口和视频属于断面检测
		if (station_.type_ == 2 || station_.type_ == 3)
			lanenum = station_.nSensors_;
		else
			lanenum = station_.segment_.nLanes();
		for (int i = 0; i < num; i++) {
			Date date1 = LinkTimes.getInstance().toDate(i);
			Date date2 = LinkTimes.getInstance().toDate((i + 1));
			// simtaskid，写死注意更改
			ps.setInt(1, 5);
			// 视频或卡口
			if (station_.type_ == 2 || station_.type_ == 3)
				ps.setInt(2, station_.getCode());
			else// 线圈
				ps.setInt(2, getCode());
			ps.setInt(3, station_.type_);
			ps.setDate(4, new java.sql.Date(date1.getTime()));
			ps.setTimestamp(4, new java.sql.Timestamp(date1.getTime()));
			ps.setDate(5, new java.sql.Date(date2.getTime()));
			ps.setTimestamp(5, new java.sql.Timestamp(date2.getTime()));
			ps.setInt(6, lanenum);
			ps.setInt(7, Math.round((float) (station_.flowList_.get(i)) / (float) lanenum));
			if (Float.isNaN(station_.speedList_.get(i))) {
				System.out.println("x");
			}
			ps.setFloat(8, station_.speedList_.get(i));
			ps.addBatch();
		}
		ps.executeBatch();
	}
	// public float measurement() { return occupancy_; }

	// used for traffic sensors in incident detection

	/*
	 * public int getRegion(double n) { return -1; } public int sensorIncState()
	 * { return 0; } public int incClrPersistCount() { return 0; } public int
	 * incDcrPersistCount() { return 0; }
	 *
	 * public float occPrev(int i) { return -1; } public float spdPrev(int i) {
	 * return -1; } public void setOccPrev(int i, float f){} public void
	 * setSpdPrev(int i, float f){}
	 */

	// WARNING!!
	// ----------------
	// The following functions were added because Peter and Yang Qi
	// are damn sick of this thing and they are becoming lazy!
	// These functions are nonsensical in the context of RN_Sensor.
	// They are here to avoid casting to derived classes which is
	// not possible based on the multiple inheritance / virtual
	// inheritance we have used for sensors. See Section 10.6c of
	// The Annotated C++ Reference Manual (pg 227) by Ellis and
	// Stroustrup for more info.
	// -----------------
	// WARNING!!

	// -- DRN_Sensor Functions --
	/*
	 * public int draw(DRN_DrawingArea *) { return 0; } public void
	 * calcGeometricData() { } public WcsPoint center() { return
	 * theWcsDummyPoint; }
	 */
	// -- TS_Sensor Functions --
	/*
	 * public void calcActivatingData(TS_Vehicle*, float, float, float) {}
	 * public void calcPassingData(TS_Vehicle*, float, float, float ) {} public
	 * void calcSnapShotData() {} public void resetSensorReadings() { public
	 * count_ = 0; public occupancy_ = 0.0; public speed_ = 0.0; public
	 * clearState(SENSOR_ACTIVATED); } public void convertSensorReadings() { }
	 */

	// -- TC_Sensor functions --
	/*
	 * public void send(IOService &) { } public void receive(IOService &) { }
	 * public void send_i(IOService &, RN_Vehicle *) { } public void
	 * receive_i(IOService &) { } public void send_s(IOService &, RN_Vehicle *)
	 * { } public void receive_s(IOService &) { }
	 *
	 * public void write(ostream &os) { } public void write_i(ostream &os,
	 * RN_Vehicle *) { } public void write_s(ostream &os, RN_Vehicle *) { }
	 *
	 * // -- TMS_Sensor functions --
	 *
	 * public void loadDbFlow(Reader &) { } public void loadDbSpeed(Reader &) {
	 * }
	 */
}
