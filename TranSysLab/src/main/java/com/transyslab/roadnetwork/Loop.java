/**
 *
 */
package com.transyslab.roadnetwork;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.transyslab.commons.tools.GeoUtil;

/**
 * Sensors
 *
 * @author YYL 2016-6-4
 *
 */
// NOTE! The following forward declarations are required because of
// the multiple inheritance structure problem defined at the end of

public class Loop extends CodedObject implements Sensor{

	protected int index_; // index in SurvStation array
	protected Lane lane_; // pointer to lane
	protected int state_; // occupied/working etc
	protected float position_;
	protected int type;
	protected float zoneLength;
	protected int recordedCount_;
	protected float meanSpeed_;
	protected float recordedSpeed_;
	protected float interval_;
	protected List<Float> speedList_; // measure speed in specific time
	// interval_
	protected List<Integer> flowList_; // measure flow in specific time
	// interval_
	protected GeoSurface surface;
	
	public Loop() {
		index_ = -1;
		zoneLength = 2;
	}


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

	public int index() {
		return index_;
	}


	// public int intervalType() {return intervalType_;} //IEM(Apr26)

	public Lane getLane() {
		return lane_;
	}
	public Segment getSegment() {
		return lane_.getSegment();
	}
	public Link getLink() {
		return lane_.getLink();
	}
	// relative distance from the end of the segment
	public float getPosition() {
		return position_;
	}

	/*
	// Distance from the end of the link
	public float distance() {
		return station_.distance();
	}
	// Length of the detection zone
	public float zoneLength() {
		return station_.zoneLength();
	}*/

	// l=lanecode, c=LDTID

	public int init(int ty, float iv, float zoneLen,int laneId, int c, float pos) {

		setCode(c);
		type = ty;
		interval_ = iv;
		zoneLength = zoneLen;
		lane_ = RoadNetwork.getInstance().findLane(laneId);
		if(lane_ == null){
			System.out.println("Can't not find lane which id is"+ laneId);
			return -1;
		}			

		index_ = RoadNetwork.getInstance().nSensors();

		RoadNetwork.getInstance().addSensor(this);

		return 0;
	}
	
	// isLinkwide == true调用
	/*
	public void measureFromStation() {
		count_ = station_.sectionCount_ / station_.nSensors_;
		avgSpeed_ = station_.sectionAvgSpeed_;
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
	}*/
	// public float measurement() { return occupancy_; }

	@Override
	public void measure(float speed) {
		recordedCount_++;
		// 转换为km/h
		speed = 3.6f * speed;
		if (speed > Constants.SPEED_EPSILON) {
			recordedSpeed_ += 1.0f / speed;
		}
		else {
			recordedSpeed_ += 1.0f / Constants.SPEED_EPSILON;
		}
		
	}

	@Override
	public void aggregate() {
		flowList_.add(recordedCount_);
		if (recordedCount_ != 0)
			meanSpeed_ = (recordedCount_) / recordedSpeed_;
		else
			meanSpeed_ = 0.0f;
		speedList_.add(meanSpeed_);
		recordedCount_ = 0;
		recordedSpeed_ = 0.0f;
		
	}

	@Override
	public void record() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void createSurface() {
		GeoPoint startPnt = new GeoPoint(lane_.getStartPnt(),lane_.getEndPnt(), (1-position_));
		double lenScale = zoneLength/lane_.getLength();
		GeoPoint endPnt = new GeoPoint(startPnt,lane_.getEndPnt(),(1-lenScale));
		surface = GeoUtil.lineToRectangle(startPnt, endPnt, lane_.getWidth());
		surface.createAabBox();
	}
	public GeoSurface getSurface(){
		return surface;
	}
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
