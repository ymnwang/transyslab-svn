/**
 *
 */
package com.transyslab.roadnetwork;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import com.transyslab.commons.tools.GeoUtil;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.SurvStation;

import oracle.jdbc.proxy.WeakIdentityHashMap;

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
public class SurvStation extends CodedObject implements Sensor{
	protected int type_; // sensor type 1:loop; 2:microwave;3:video;4:kakou
	protected int task_; // data items
	protected Segment segment_; // pointer to segment
	protected float zoneLength_; // length of detection zone
	protected float position_; // position in % from segment end
	protected List<Sensor> sensors_; // array of pointers to sensors
	protected int nSensors_; // (YYL) number of sensors
	protected int index_; // (YYL) index in segment.survlist
	protected int recordedCount_;
	protected float sectionAvgSpeed_;
	protected float recordedSpeed_;
	protected float interval_;
	protected float measureTime_;
	protected List<Float> speedList_; // measure speed in specific time
										// interval_
	protected List<Integer> flowList_; // measure flow in specific time
										// interval_
	// protected List<Integer> vhcidList_ = new ArrayList<Integer>();
	protected GeoSurface surface;
	public SurvStation() {
		index_ = -1;
		nSensors_ = 0;
		recordedCount_ = 0;
		sectionAvgSpeed_ = 0;
		recordedSpeed_ = 0;
	}
	public int type() {
		return type_;
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
		return sensors_.get(i);
	}

	// Connect ith point to the sensor

	public float getInterval() {
		return interval_;
	}

	public float zoneLength() {
		return zoneLength_;
	}
	public float getPosition() {
		return position_;
	}
	/*
	public void resetMeasureTime(){
		measureTime_ = (float) SimulationClock.getInstance().getCurrentTime() + interval_;
		sectionAvgSpeed_ = 0;
		recordedSpeed_ = 0;
		recordedCount_ = 0;
	}*/
	public List<Float> getSpeedList() {
		return speedList_;
	}
	public List<Integer> getflowList() {
		return flowList_;
	}
	public int init(int ty, float iv, float zone, int seg, int code, float pos) {

		type_ = ty; // sensor type
		interval_ = iv; // statistic time interval in seconds
		measureTime_ = (float) SimulationClock.getInstance().getCurrentTime() + interval_;
		zoneLength_ = zone; // * Parameter::lengthFactor(); in meter
		// position_ = (float) (1.0 - pos); // position in % from segment end

		setCode(code);
		speedList_ = new ArrayList<Float>();
		flowList_ = new ArrayList<Integer>();
		if ((segment_ = RoadNetwork.getInstance().findSegment(seg)) == null) {
			return -1;
		}
		position_ = (float) ((1.0 - pos) * segment_.getLength());
		// (YYL)
		sensors_ = new ArrayList<Sensor>();

		if (segment_.getSurvList() == null)
			segment_.survList_ = new ArrayList<SurvStation>();
		index_ = segment_.getSurvList().size();
		segment_.getSurvList().add(this);
		RoadNetwork.getInstance().addSurvStation(this);

		return 0;
	}
	/*
	public void outputToOracle(PreparedStatement ps) throws SQLException {
		int num = flowList_.size();
		int lanenum;
		// 卡口和视频属于断面检测
		if (type_ == 2 || type_ == 3)
			lanenum = station.nSensors_;
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
	// computes the flow across the section - used in incident detection
/*
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
	}*/

	/*
		 * public void addVehicleID(MESO_Vehicle pv){
		 * vhcidList_.add(pv.get_code()); }
		 */
	public void aggregate() {
		flowList_.add(recordedCount_);
		if (recordedCount_ != 0)
			sectionAvgSpeed_ = (recordedCount_) / recordedSpeed_;
		else
			sectionAvgSpeed_ = 0.0f;
		speedList_.add(sectionAvgSpeed_);
		recordedCount_ = 0;
		recordedSpeed_ = 0.0f;
	}
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
	public void record() {
		
	}
	@Override
	public void createSurface(){
		GeoPoint startPnt = new GeoPoint(segment_.getStartPnt(),segment_.getEndPnt(), position_);
		double lenScale = zoneLength_/segment_.getLength();
		GeoPoint endPnt = new GeoPoint(startPnt,segment_.getEndPnt(),lenScale);
		double width = segment_.nLanes_ * segment_.getLeftLane().getWidth();
		surface = GeoUtil.lineToRectangle(startPnt, endPnt, width,true);
	}
}
