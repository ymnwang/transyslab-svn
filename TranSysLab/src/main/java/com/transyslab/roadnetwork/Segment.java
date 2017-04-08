/**
 *
 */
package com.transyslab.roadnetwork;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Segment
 *
 * @author YYL 2016-6-1
 */
public class Segment extends CodedObject {

	protected int index_; // index in array

	protected Link link_; // pointer to link
	protected float grade_; // grade of the segment
	protected int nLanes_; // number of lanes
	protected int leftLaneIndex_; // index to the left lane
	protected int speedLimit_; // default speed limit
	protected float freeSpeed_; // free flow speed
	protected double distance_; // distance from dn node

	protected List<CtrlStation> ctrlList_; // first (upstream) control station
	protected List<SurvStation> survList_; // first (upstream) sensor station

	protected int state_;
	protected int localType_; // head, tail, etc
	protected int sdIndex_; // index to the performance function

	protected GeoPoint startPnt_;
	protected GeoPoint endPnt_;
	protected double bulge_;

	protected double startAngle_;
	protected double endAngle_;
	/*
	 * protected double arcAngle_; protected Point center_; protected double
	 * radius_;
	 */
	protected double length_;

	public Segment() {
		distance_ = 0.0;
		nLanes_ = 0;
		link_ = null;
		ctrlList_ = null;
		survList_ = null;
		localType_ = 0;
		state_ = 0;
		sdIndex_ = 0;
	}

	public int state() {
		return (state_ & 0xFFFF);
	}
	public void setState(int s) {
		state_ |= s;
	}
	public void unsetState(int s) {
		state_ &= ~s;
	}
	public int isHead() {
		return (localType_ & 0x0001);
	}
	public int isTail() {
		return (localType_ & 0x0002);
	}
	public int isTheEnd() {
		return (localType_ & 0x0020);
	}
	public int isTheBeginning() {
		return (localType_ & 0x0010);
	}
	public int getIndex() {
		return index_;
	}

	// Index within the link. 0 is the upstream.
	public int localIndex() {
		return (index_ - link_.getStartSegment().index_);
	}
	public int getType() {
		return (link_.type());
	}
	public GeoPoint getStartPnt() {
		return startPnt_;
	}
	public GeoPoint getEndPnt() {
		return endPnt_;
	}
	public void setStartPnt(GeoPoint p) {
		startPnt_ = p;
	}
	public void setEndPnt(GeoPoint p) {
		endPnt_ = p;
	}
	public double getLength() {
		return length_;
	}
	public double getBulge() {
		return bulge_;
	}

	public double getStartAngle() {
		// For a straight line, startAngle is the angle of the
		// line. For a curve, startAngle is the angle of the line
		// from the center to the point startPnt.
		return startAngle_;
	}
	public double getEndAngle() {
		// For a straight line, endAngle is the angle of the
		// line. For a curve, endAngle is the angle of the line from
		// the center to the point endPnt. Note: arcAngle is zero for
		// a straight line.
		return endAngle_;
	}/*
		 * public double arcAngle(){ // Negative if endAngle < startAngle return
		 * arcAngle_; } public Point getCenter(){ return center_; }
		 */

	// Segment iterators in a link
	// Returns the upstream segment in the same link
	public Segment getUpSegment() {
		if (index_ != link_.getStartSegmentIndex())
			return RoadNetwork.getInstance().getSegment(index_ - 1);
		else
			return (null);
	}
	public Segment getDnSegment() {
		if (index_ != link_.getStartSegmentIndex() + link_.nSegments() - 1)
			return RoadNetwork.getInstance().getSegment(index_ + 1);
		else
			return (null);
	}

	// Lane iterators in a segment

	public int getLeftLaneIndex() {
		return leftLaneIndex_;
	}
	// Returns the left most lane in the segment
	public Lane getLeftLane() {
		return RoadNetwork.getInstance().getLane(leftLaneIndex_);
	}
	// Returns the right most lane in the segment

	public Lane getRightLane() {
		return RoadNetwork.getInstance().getLane(leftLaneIndex_ + nLanes_ - 1);
	}
	// Returns the ith lane in the segment
	public Lane getLane(int i) {
		return RoadNetwork.getInstance().getLane(leftLaneIndex_ + i);
	}

	public Lane centralLane() {
		return RoadNetwork.getInstance().getLane(leftLaneIndex_ + nLanes_ / 2);
	}
	public Link getLink() {
		return link_;
	}
	/*
	 * 已被重载 public WcsPoint centerPoint(float fraction ) {//0.5 return
	 * intermediatePoint(fraction) ; }
	 */

	public List<CtrlStation> getCtrlList() {
		return ctrlList_;
	}
	public List<SurvStation> getSurvList() {
		return survList_;
	}

	public int init(int id, int speed_limit, float speed, float grd) {
		/*
		 * if (ToolKit::debug()) { cout << indent << indent << "<" << id << endc
		 * << speed_limit << endc << speed << endc << grd << ">" << endl; }
		 */
		// YYL

		if (link_ != null) {
			// cerr << "Error:: Segment <" << id << "> "
			// << "cannot be initialized twice. ";
			return -1;
		}
		else {
			link_ = RoadNetwork.getInstance().lastLink();
		}
		setCode(id);

		speedLimit_ = speed_limit;
//		freeSpeed_ = (float) (speed * 0.447);
		grade_ = grd;

		index_ = RoadNetwork.getInstance().nSegments();
		nLanes_ = 0;
		leftLaneIndex_ = RoadNetwork.getInstance().nLanes();
		RoadNetwork.getInstance().addSegment(this);

		RoadNetwork.getInstance().lastLink().nSegments_++;

		return 0;
	}
	public int initArc(double x1, double y1, double b, double x2, double y2) {
		/*
		 * if (ToolKit::debug()) { cout << indent << indent << "<" << x1 << endc
		 * << y1 << endc << b << endc << x2 << endc << y2 << ">" << endl; }
		 */
		// int ans = initSuperArc(x1, y1, b, x2, y2);
		startPnt_ = new GeoPoint(x1, y1);
		endPnt_ = new GeoPoint(x2, y2);
		bulge_ = -b;

		RoadNetwork.getInstance().getWorldSpace().recordExtremePoints(startPnt_);
		RoadNetwork.getInstance().getWorldSpace().recordExtremePoints(endPnt_);
		return 0;
	}
	@Override
	public void print() {

	}
	public void printLanes() {

	}

	// Segment attributes

	public int nLanes() {
		return nLanes_;
	}
	public double getDistance() {
		return distance_;
	}
	public void setDistance(float x) {
		distance_ = x;
	}
	public int speedLimit() {
		return speedLimit_;
	}
	public void setSpeedLimit(int sl) {
		speedLimit_ = sl;
	}
	/*
	public void setFreeSpeed(float f) {
		freeSpeed_ = f;
	}*/
	public double getFreeSpeed() {
		return freeSpeed_;
	}

	public void setGrade(float g) {
		grade_ = g;
	}
	public float getGrade() {
		return grade_;
	}
	public double calcCurrentTravelTime() {
		float min_spd = 2.22f; // 5 mph
		if (length_ < 50.0) {

			// Speed density function will not work for very short segment

			return (length_ / freeSpeed_);
		}

		SdFn sdf = RoadNetwork.getInstance().getSdFn(sdIndex_);
		float spd = sdf.densityToSpeed(/*(float) getFreeSpeed(),*/ calcDensity(), nLanes());

		if (spd < min_spd)
			spd = min_spd;
		return (length_ / spd);
	}
	public void setSdIndex(int i) {
		sdIndex_ = i;
	}
	public int getSdIndex() {
		return sdIndex_;
	}

	public void printSdIndex() {

	}

	// Make end point of each pair of connected segments snapped at
	// the same point

	public void snapCoordinates() {
		Segment ups = getUpSegment();
		if (ups != null) {// not the first segment in the link
			// Find the middle point

			GeoPoint p = new GeoPoint(ups.getEndPnt(), getStartPnt(), 0.5);

			// Snap to the middle point

			ups.setEndPnt(p);
			setStartPnt(p);
		}
	}

	public void calcArcInfo(WorldSpace world_space) {
		startPnt_ = world_space.worldSpacePoint(startPnt_);
		endPnt_ = world_space.worldSpacePoint(endPnt_);
		length_ = startPnt_.distance(endPnt_);
		startAngle_ = endAngle_ = startPnt_.angle(endPnt_);
		// computeArc();

	}
	public void computeArc() {
		/*
		 * radius_ = 0.0; arcAngle_ = 0.0; //midPoint center_ = new
		 * Point(startPnt_, endPnt_, 0.5); length_ =
		 * startPnt_.distance(endPnt_); startAngle_ = endAngle_ =
		 * startPnt_.angle(endPnt_);
		 */
	}

	// Calculate the data that do not change in the simulation.
	public void calcStaticInfo() {
		if ((getDnSegment() == null)) {
			localType_ |= 0x0001;
			if (getLink().nDnLinks() < 1 || getLink().getDnNode().type(0x0001) > 0) {
				localType_ |= 0x0020;
			}
		}
		if (getUpSegment() == null) {
			localType_ |= 0x0002;
			if (getLink().nUpLinks() < 1 || getLink().getUpNode().type(0x0001) > 0) {
				localType_ |= 0x0010;
			}
		}
		RoadNetwork.getInstance().totalLinkLength_ += length_;
		RoadNetwork.getInstance().totalLaneLength_ += length_ * nLanes_;

		if (sdIndex_ < 0 || sdIndex_ >= RoadNetwork.getInstance().nSdFns()) {
			// cerr << "Segment " << code_ << " has invalid sdIndex "
			// << sdIndex_ << "." << endl;
			sdIndex_ = 0;
		}
	}

	public float calcDensity() {
		return density();
	}
	public float calcSpeed() {
		return speed();
	}
	public int calcFlow() {
		return flow();
	}
	// 虚拟方法，由子类继承实现流速密计算
	public float density() {
		return 0;
	}
	public float speed() {
		return freeSpeed_;
	}
	public int flow() {
		return 0;
	}

	public void markConnectedUpLanes() {
		for (int i = 0; i < nLanes(); i++) {
			getLane(i).markConnectedUpLanes(1 << i, 0.0f);
		}
	}
	public void markConnectedDnLanes() {
		for (int i = 0; i < nLanes(); i++) {
			getLane(i).markConnectedDnLanes(1 << i, 0.0f);
		}
	}
	public void unmarkConnectedUpLanes() {
		for (int i = 0; i < nLanes(); i++) {
			getLane(i).unmarkConnectedUpLanes(1 << i, 0.0f);
		}
	}
	public void unmarkConnectedDnLanes() {
		for (int i = 0; i < nLanes(); i++) {
			getLane(i).unmarkConnectedDnLanes(1 << i, 0.0f);
		}
	}
	public void outputSegment() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(getCode()).append(",");
		sb.append(startPnt_.getLocationX()).append(",");
		sb.append(startPnt_.getLocationY()).append(",");
		sb.append(endPnt_.getLocationX()).append(",");
		sb.append(endPnt_.getLocationY()).append("\n");
		String filepath = "E:\\OutputRoadNetwork.txt";
		FileOutputStream out = new FileOutputStream(filepath, true);
		OutputStreamWriter osw = new OutputStreamWriter(out, "utf-8");
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(sb.toString());
		bw.close();
	}

	// Export geometry data in MapInfo format
	// 输出地图信息
	public void ExportMapInfo() {

	}
	public void Export() {

	}

	public void outputToOracle(PreparedStatement ps) throws SQLException {
		// TODO 自动生成的方法存根

	}

	public void outputVhcPosition() throws IOException {
		// TODO 自动生成的方法存根

	}
}
