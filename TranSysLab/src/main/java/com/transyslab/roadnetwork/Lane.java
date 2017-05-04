/**
 *
 */
package com.transyslab.roadnetwork;

import java.util.*;

import com.transyslab.commons.tools.GeoUtil;

/**
 * Lane
 *
 * @author YYL
 *
 */
public class Lane extends CodedObject {

	// 车道宽度
	public float width = 3.5f;

	protected int index_;
	protected Segment segment_;
	protected int rules_;// lane use and change rules
	protected List<Lane> upLanes_;
	protected List<Lane> dnLanes_;
	protected GeoPoint startPnt_;
	protected GeoPoint endPnt_;
	protected GeoSurface laneSurface;
	protected int laneType_;
	protected int state_;
	protected int cmarker_;// connection marker

	public Lane() {
		segment_ = null;
		laneType_ = 0;
		state_ = 0;
		cmarker_ = 0;
		upLanes_ = new ArrayList<Lane>();
		dnLanes_ = new ArrayList<Lane>();
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

	public Segment getSegment() {
		return segment_;
	}

	public Link getLink() {
		return segment_.getLink();
	}
	public int index() {
		return index_;
	}
	public int localIndex() {
		return (index_ - segment_.getLeftLaneIndex());
	}

	public int linkType() {
		return segment_.getType();
	}
	public Lane getRight() {
		if (index_ != segment_.getLeftLaneIndex() + segment_.nLanes() - 1)
			return RoadNetwork.getInstance().getLane(index_ + 1);
		else
			return (null);
	}
	public Lane getLeft() {
		if (index_ != segment_.getLeftLaneIndex())
			return RoadNetwork.getInstance().getLane(index_ - 1);
		else
			return (null);
	}
	/*
	 * -------------------------------------------------------------------- Set
	 * laneType based on link type and info on connectivity.
	 * --------------------------------------------------------------------
	 */
	public void setLaneType() {
		/*
		 * check if this lane is a shoulder lane
		 */
		if (getRight() == null)
			laneType_ |= Constants.LANE_TYPE_RIGHT_MOST;
		if (getLeft() == null)
			laneType_ |= Constants.LANE_TYPE_LEFT_MOST;

		int i, j;
		Lane plane;

		/*
		 * check if this lane is connected to an on-ramp at the upstream end
		 */

		for (i = 0; i < nUpLanes(); i++) {
			if ((upLane(i).linkType() & Constants.LINK_TYPE_RAMP) != 0) {
				laneType_ |= Constants.LANE_TYPE_UP_ONRAMP;
				break;
			}
		}

		/*
		 * check if this lane shares the same upstream lane with an off-ramp
		 * lane (actually this info is not very useful as other info being
		 * calculated in this function in terms of drive behavior. It is coded
		 * anyway just in case some other algorithm may use it)
		 */

		for (i = 0; i < nUpLanes() && (laneType_ & Constants.LANE_TYPE_UP_OFFRAMP) == 0; i++) {
			plane = upLane(i);
			for (j = 0; j < plane.nDnLanes(); j++) {
				if ((plane.dnLane(j).linkType() & Constants.LINK_TYPE_RAMP) != 0) {
					laneType_ |= Constants.LANE_TYPE_UP_OFFRAMP;
					break;
				}
			}
		}

		/*
		 * check if this lane is connected to an off-ramp at the downstream end.
		 */

		for (i = 0; i < nDnLanes(); i++) {
			if ((dnLane(i).linkType() & Constants.LINK_TYPE_RAMP) != 0) {
				laneType_ |= Constants.LANE_TYPE_DN_OFFRAMP;
				break;
			}
		}

		/*
		 * check if this lane merges into the same downstream lane with an
		 * on-ramp lane.
		 */

		for (i = 0; i < nDnLanes() && ((laneType_ & Constants.LANE_TYPE_DN_ONRAMP) == 0); i++) {
			plane = dnLane(i);
			for (j = 0; j < plane.nUpLanes(); j++) {
				if ((plane.upLane(j).linkType() & Constants.LINK_TYPE_RAMP) != 0) {
					laneType_ |= Constants.LANE_TYPE_DN_ONRAMP;
					break;
				}
			}
		}

		// Check if this is the last lane

		if (getSegment().getDnSegment() == null && // last segment
													// ('->downstream()' added
													// by Angus)
				getLink().getDnNode().type(Constants.NODE_TYPE_EXTERNAL) != 0 && // external
																					// node
				nDnLanes() <= 0) { // no downstream lane
			laneType_ |= Constants.LANE_TYPE_BOUNDARY;
		}

		if (laneType(Constants.LANE_TYPE_BOUNDARY) == 0 && // not at the
															// boundary
				nDnLanes() <= 0) { // no downstream lane
			laneType_ |= Constants.LANE_TYPE_DROPPED;
		}
	}

	public int laneType(int mask) {
		return (laneType_ & mask);
	}
	public int isDropped() {
		return (laneType_ & Constants.LANE_TYPE_DROPPED);
	}
	public int isBoundary() {
		return (laneType_ & Constants.LANE_TYPE_BOUNDARY);
	}

	public int isEtcLane() {
		return rules_ & Constants.VEHICLE_ETC;
	}
	public int isHovLane() {
		return rules_ & Constants.VEHICLE_HOV;
	}
	public int isBusLane() {
		return rules_ & Constants.VEHICLE_COMMERCIAL;
	}

	public double getLength() {
		return segment_.getLength();
	}

	public Lane getRightLane() {
		if (index_ != segment_.getLeftLaneIndex() + segment_.nLanes() - 1)
			return RoadNetwork.getInstance().getLane(index_ + 1);
		else
			return (null);
	}
	public Lane getLeftLane() {
		if (index_ != segment_.getLeftLaneIndex())
			return RoadNetwork.getInstance().getLane(index_ - 1);
		else
			return (null);
	}
	public Lane getPrevLane() {
		if (index_ > 0)
			return RoadNetwork.getInstance().getLane(index_ - 1);
		else
			return null;
	}
	public Lane getNextLane() {
		if (index_ < RoadNetwork.getInstance().nLanes() - 1)
			return RoadNetwork.getInstance().getLane(index_ - 1);
		else
			return null;
	}

	public int nUpLanes() {
		return upLanes_.size();
	}
	public int nDnLanes() {
		return dnLanes_.size();
	}

	public Lane upLane(int i) {
		return upLanes_.get(i);
	}
	public Lane dnLane(int i) {
		return dnLanes_.get(i);
	}

	/*
	 * --------------------------------------------------------------------
	 * Check if a lane is one of the downstream lanes
	 * --------------------------------------------------------------------
	 */
	public Lane findInUpLane(int c) {
		ListIterator<Lane> i = upLanes_.listIterator();
		while (i.hasNext()) {
			Lane templabel = i.next();
			if (templabel.cmp(c) == 0) {
				// <c,return -1;>c,return 1;=c return 0;
				return templabel;
			}
		}
		return null;
	}

	// Find if a lane is one the downstream lanes
	public Lane findInDnLane(int c) {
		ListIterator<Lane> i = dnLanes_.listIterator();
		while (i.hasNext()) {
			Lane templabel = i.next();
			if (templabel.cmp(c) == 0) {
				// <c,return -1;>c,return 1;=c return 0;
				return templabel;
			}
		}
		return null;
	}

	public boolean isInDnLanes(Lane plane) {
		for (int i = 0; i < nDnLanes(); i++) {
			if (dnLane(i).getCode() == plane.getCode())
				return true;
		}
		return false;
	}
	public boolean isInUpLanes(Lane plane) {
		for (int i = 0; i < nUpLanes(); i++) {
			if (upLane(i).getCode() == plane.getCode())
				return true;
		}
		return false;
	}

	public int rules() {
		return rules_;
	}
	public void rulesExclude(int exclude) {
		rules_ &= ~exclude;
	}

	public int init(int c, int r, double beginx, double beginy, double endx, double endy) {
		/*
		 * if (ToolKit::debug()) { cout << indent << indent << indent << "<" <<
		 * c << endc << r << ">" << endl; }
		 */
		startPnt_ =new GeoPoint(beginx,beginy);
		endPnt_ =new GeoPoint(endx,endy);
		RoadNetwork.getInstance().getWorldSpace().recordExtremePoints(startPnt_);
		RoadNetwork.getInstance().getWorldSpace().recordExtremePoints(endPnt_);
		if (segment_ != null) {
			/*
			 * cerr << "Error:: Lane <" << c << "> " <<
			 * "cannot be initialized twice. ";
			 */
			return -1;
		}
		else {
			segment_ = RoadNetwork.getInstance().lastSegment();
		}
		setCode(c);
		rules_ = r;

		index_ = RoadNetwork.getInstance().nLanes();
		RoadNetwork.getInstance().addLane(this);

		segment_.nLanes_++;

		return 0;
	}
	// 路网世界坐标平移后再调用
	public void createLaneSurface(){
		laneSurface = GeoUtil.lineToRectangle(startPnt_, endPnt_, width, true);
		laneSurface.createAabBox();
	}
	public GeoSurface getLaneSurface(){
		return laneSurface;
	}
	public double getWidth() {
		return width;
	}
	@Override
	public void print() {

	}
	public void printDnConnectors() {

	}
	public GeoPoint getStartPnt() {
		return startPnt_;
	}
	public GeoPoint getEndPnt() {
		return endPnt_;
	}
	public void calcStaticInfo(WorldSpace world_space) {
		if (getLeftLane() == null)
			rulesExclude(Constants.LANE_CHANGE_LEFT);
		if (getRightLane() == null)
			rulesExclude(Constants.LANE_CHANGE_RIGHT);

		setLaneType();
		//起终点坐标平移
		startPnt_ = world_space.worldSpacePoint(startPnt_);
		endPnt_ = world_space.worldSpacePoint(endPnt_);
		//生成车道面
		createLaneSurface();
		
	}
	/*
	 * --------------------------------------------------------------------
	 * Returns the limiting speed in this lane. Speed is in meter/sec. This
	 * function should be overloaded to consider lane position (see class
	 * TS_Lane for more details)
	 * --------------------------------------------------------------------
	 */
	public double getFreeSpeed() {
		return segment_.getFreeSpeed();
	}
	/*
	 * --------------------------------------------------------------------
	 * Returns the grade of the lane, in percent. Used to calculate the
	 * Acceleration and limiting speed.
	 * --------------------------------------------------------------------
	 */
	public double getGrade() {
		return segment_.getGrade();
	}
	/*
	 * --------------------------------------------------------------------
	 * Returns the 1st surveillance station in this lane (upstream end)
	 * --------------------------------------------------------------------
	 */
	public List survList() {
		return (segment_.getSurvList());
	}
	/*
	 * --------------------------------------------------------------------
	 * Returns the 1st control station in this lane (upstream end)
	 * --------------------------------------------------------------------
	 */
	public List ctrlList() {
		return (segment_.getCtrlList());
	}

	public int hasMarker() {
		return state_ & Constants.STATE_MARKED;
	}

	// Check if the lane is connected to a lane upstream/downstream.
	// markConnectedUpLanes() or markConnecteDnLanes() must be
	// called (usually for each lane in a segment) before the use of
	// the function isConnected(signature_bit), where signatures is
	// the bit masks (left lane is 1, middle lane is 2, right lane
	// is 4, and so on) of the lanes that do the marking.

	public boolean isConnected(int signatures) {
		return (cmarker_ & signatures) != 0 ? true : false;
	}

	public void markConnectedUpLanes(int signature, float depth) {
		if ((cmarker_ & signature) > 0)
			return; // opps, a loop found

		cmarker_ |= signature; // mark this lane

		depth += getLength(); // accumulate the searched distance

		if (depth > Parameter.getInstance().visibility())
			return;

		for (int i = 0; i < nUpLanes(); i++) {
			upLane(i).markConnectedUpLanes(signature, depth);
		}
	}
	public void markConnectedDnLanes(int signature, float depth) {
		if ((cmarker_ & signature) > 0)
			return; // opps, a loop found

		cmarker_ |= signature; // mark this lane
		depth += getLength(); // accumulate the searched distance

		if (depth > Parameter.getInstance().visibility())
			return;

		for (int i = 0; i < nDnLanes(); i++) {
			dnLane(i).markConnectedDnLanes(signature, depth);
		}
	}

	public void unmarkConnectedUpLanes(int signature, float depth) {
		if (!((cmarker_ & signature) > 0))
			return; // opps, a loop found

		cmarker_ &= ~signature; // mark this lane

		depth += getLength(); // accumulate the searched distance

		if (depth > Parameter.getInstance().visibility())
			return;

		for (int i = 0; i < nUpLanes(); i++) {
			upLane(i).unmarkConnectedUpLanes(signature, depth);
		}
	}
	public void unmarkConnectedDnLanes(int signature, float depth) {
		if (!((cmarker_ & signature) > 0))
			return; // opps, a loop found

		cmarker_ &= ~signature; // mark this lane
		depth += getLength(); // accumulate the searched distance

		if (depth > Parameter.getInstance().visibility())
			return;

		for (int i = 0; i < nDnLanes(); i++) {
			dnLane(i).unmarkConnectedDnLanes(signature, depth);
		}
	}

	public void unMarkAllLanes() {
		for (int i = 0; i < RoadNetwork.getInstance().nLanes(); i++) {
			RoadNetwork.getInstance().getLane(i).unsetState(Constants.STATE_MARKED);
		}
	}

	public double getDistance() {
		return segment_.getDistance();
	}
	
}
