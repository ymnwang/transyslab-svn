/**
 *
 */
package com.transyslab.roadnetwork;

import java.util.*;

import com.transyslab.commons.tools.GeoUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Lane
 *
 * @author YYL
 *
 */
public class Lane implements NetworkObject {

	protected int id;
	protected String name;
	protected String objInfo;
	// 车道宽度
	public float width = 3.5f;
	protected int index;
	protected Segment segment;
	protected int rules;// lane use and change rules
	protected List<Lane> upLanes;
	protected List<Lane> dnLanes;
	protected GeoPoint startPnt;
	protected GeoPoint endPnt;
	protected double geoLength;
	protected GeoSurface surface;
	protected List<SignalArrow> signalArrows;
	protected int type;
	protected int state;
	protected int cmarker;// connection marker
	protected Lane leftLane;
	protected Lane rightLane;
	protected boolean isSelected;
	protected boolean isEntranceToIntersection;//TODO 并入type
	protected int turnType;
	public Lane() {
		segment = null;
		type = 0;
		state = 0;
		cmarker = 0;
		upLanes = new ArrayList<>();
		dnLanes = new ArrayList<>();
		signalArrows = new ArrayList<>();
	}
	public int getId(){
		return this.id;
	}
	public String getName(){
		return name;
	}
	public String getObjInfo(){return this.objInfo;}
	public boolean isSelected(){
		return this.isSelected;
	}
	public void setSelected(boolean flag){
		this.isSelected = flag;
	}
	public int state() {
		return (state & 0xFFFF);
	}
	public void setState(int s) {
		state |= s;
	}
	public void unsetState(int s) {
		state &= ~s;
	}
	public List<SignalArrow> getSignalArrows(){
		return this.signalArrows;
	}
	public Segment getSegment() {
		return segment;
	}

	public Link getLink() {
		return segment.getLink();
	}
	public int getIndex() {
		return index;
	}

	public int linkType() {
		return segment.getType();
	}

	/*
	 * -------------------------------------------------------------------- Set
	 * type based on link type and info on connectivity.
	 * --------------------------------------------------------------------
	 */
	public void setLaneType() {
		// 检查是否为交叉口进口道
		for(Lane dnLane: dnLanes){
			if(this.getLink().getId() != dnLane.getLink().getId()){
				isEntranceToIntersection = true;
				break;
			}
		}
		/*
		 * check if this lane is a shoulder lane
		 */
		if (getRightLane() == null)
			type |= Constants.LANE_TYPE_RIGHT_MOST;
		if (getLeftLane() == null)
			type |= Constants.LANE_TYPE_LEFT_MOST;

		int i, j;
		Lane plane;

		/*
		 * check if this lane is connected to an on-ramp at the upstream end
		 */

		for (i = 0; i < nUpLanes(); i++) {
			if ((upLane(i).linkType() & Constants.LINK_TYPE_RAMP) != 0) {
				type |= Constants.LANE_TYPE_UP_ONRAMP;
				break;
			}
		}

		/*
		 * check if this lane shares the same upstream lane with an off-ramp
		 * lane (actually this info is not very useful as other info being
		 * calculated in this function in terms of drive behavior. It is coded
		 * anyway just in case some other algorithm may use it)
		 */

		for (i = 0; i < nUpLanes() && (type & Constants.LANE_TYPE_UP_OFFRAMP) == 0; i++) {
			plane = upLane(i);
			for (j = 0; j < plane.nDnLanes(); j++) {
				if ((plane.dnLane(j).linkType() & Constants.LINK_TYPE_RAMP) != 0) {
					type |= Constants.LANE_TYPE_UP_OFFRAMP;
					break;
				}
			}
		}

		/*
		 * check if this lane is connected to an off-ramp at the downstream end.
		 */

		for (i = 0; i < nDnLanes(); i++) {
			if ((dnLane(i).linkType() & Constants.LINK_TYPE_RAMP) != 0) {
				type |= Constants.LANE_TYPE_DN_OFFRAMP;
				break;
			}
		}

		/*
		 * check if this lane merges into the same downstream lane with an
		 * on-ramp lane.
		 */

		for (i = 0; i < nDnLanes() && ((type & Constants.LANE_TYPE_DN_ONRAMP) == 0); i++) {
			plane = dnLane(i);
			for (j = 0; j < plane.nUpLanes(); j++) {
				if ((plane.upLane(j).linkType() & Constants.LINK_TYPE_RAMP) != 0) {
					type |= Constants.LANE_TYPE_DN_ONRAMP;
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
			type |= Constants.LANE_TYPE_BOUNDARY;
		}

		if (laneType(Constants.LANE_TYPE_BOUNDARY) == 0 && // not at the
															// boundary
				nDnLanes() <= 0) { // no downstream lane
			type |= Constants.LANE_TYPE_DROPPED;
		}
	}

	public int laneType(int mask) {
		return (type & mask);
	}
	public int isDropped() {
		return (type & Constants.LANE_TYPE_DROPPED);
	}
	public int isBoundary() {
		return (type & Constants.LANE_TYPE_BOUNDARY);
	}

	public int isEtcLane() {
		return rules & Constants.VEHICLE_ETC;
	}
	public int isHovLane() {
		return rules & Constants.VEHICLE_HOV;
	}
	public int isBusLane() {
		return rules & Constants.VEHICLE_COMMERCIAL;
	}

	public double getLength() {
		return segment.getLength();
	}
	public double getGeoLength(){
		return this.geoLength;
	}
	public Lane getRightLane() {
		return rightLane;
	}
	public Lane getLeftLane() {
		return leftLane;
	}


	public int nUpLanes() {
		return upLanes.size();
	}
	public int nDnLanes() {
		return dnLanes.size();
	}

	public Lane upLane(int i) {
		return upLanes.get(i);
	}
	public Lane dnLane(int i) {
		return dnLanes.get(i);
	}

	/*
	 * --------------------------------------------------------------------
	 * Check if a lane is one of the downstream upLanes
	 * --------------------------------------------------------------------
	 */
	public Lane findInUpLane(int c) {
		ListIterator<Lane> i = upLanes.listIterator();
		while (i.hasNext()) {
			Lane tempLane = i.next();
			if (tempLane.id == c) {
				return tempLane;
			}
		}
		return null;
	}

	// Find if a lane is one the downstream upLanes
	public Lane findInDnLane(int c) {
		ListIterator<Lane> i = dnLanes.listIterator();
		while (i.hasNext()) {
			Lane tempLane = i.next();
			if (tempLane.id == c) {
				return tempLane;
			}
		}
		return null;
	}

	public boolean isInDnLanes(Lane plane) {
		for (int i = 0; i < nDnLanes(); i++) {
			if (dnLane(i).id == plane.id)
				return true;
		}
		return false;
	}
	public boolean isInUpLanes(Lane plane) {
		for (int i = 0; i < nUpLanes(); i++) {
			if (upLane(i).id == plane.id)
				return true;
		}
		return false;
	}

	public int rules() {
		return rules;
	}
	public void rulesExclude(int exclude) {
		rules &= ~exclude;
	}


	public void init(int id, int r, int index, double beginx, double beginy, double endx, double endy, Segment seg) {

		startPnt =new GeoPoint(beginx,beginy);
		endPnt =new GeoPoint(endx,endy);
		geoLength = startPnt.distance(endPnt);
		if (this.segment != null) {
			System.out.print("Can't not init segment twice");
			return ;
		}
		else {
			this.segment = seg;
		}
		this.id = id;
		this.rules = r;
		this.index = index;
		this.segment.addLane(this);
	}
	// 路网世界坐标平移后再调用
	public void createLaneSurface(){
		surface = GeoUtil.lineToRectangle(startPnt, endPnt, width, true);
		//surface.createAabBox();
	}
	public GeoSurface getSurface(){
		return surface;
	}
	public double getWidth() {
		return width;
	}

	public GeoPoint getStartPnt() {
		return startPnt;
	}
	public GeoPoint getEndPnt() {
		return endPnt;
	}

	public void calcStaticInfo(WorldSpace worldSpace) {
		if (getLeftLane() == null)
			rulesExclude(Constants.LANE_CHANGE_LEFT);
		if (getRightLane() == null)
			rulesExclude(Constants.LANE_CHANGE_RIGHT);
		setLaneType();
		//起终点坐标平移
		startPnt = worldSpace.worldSpacePoint(startPnt);
		endPnt = worldSpace.worldSpacePoint(endPnt);
		//生成车道面
		createLaneSurface();
		//信控标识
		if(isEntranceToIntersection){
			for(Lane dnLane:dnLanes){
				// TODO 判断有哪几类转向,new signalArrows,然后加入signalArrows
				// 写死类型
				signalArrows.add(new SignalArrow(0,SignalArrow.STRAIGHTARROW,this.getLink().getId(),dnLane.getLink().getId(),this));
//				signalArrows.add(new SignalArrow(0,SignalArrow.LEFTARROW,this));
//				signalArrows.add(new SignalArrow(0,SignalArrow.RIGHTARROW,this));
			}

			if(signalArrows.size()>1) {
				Collections.sort(signalArrows);
				Vector3D translate = new Vector3D(surface.getKerbList().get(0).getLocationX() - startPnt.getLocationX(),
						surface.getKerbList().get(0).getLocationY() - startPnt.getLocationY(),
						surface.getKerbList().get(0).getLocationZ() - startPnt.getLocationZ());//←
				translate = translate.normalize();
				if(signalArrows.size()==2){
					GeoUtil.calcTranslation(signalArrows.get(0).getArrowTip(),translate.scalarMultiply(0.3));
					GeoUtil.calcTranslation(signalArrows.get(0).getPolyline(),translate.scalarMultiply(0.3));
					GeoUtil.calcTranslation(signalArrows.get(1).getArrowTip(),translate.scalarMultiply(-0.3));
					GeoUtil.calcTranslation(signalArrows.get(1).getPolyline(),translate.scalarMultiply(-0.3));
				}
				else{
					GeoUtil.calcTranslation(signalArrows.get(0).getArrowTip(),translate.scalarMultiply(0.6));
					GeoUtil.calcTranslation(signalArrows.get(0).getPolyline(),translate.scalarMultiply(0.6));
					GeoUtil.calcTranslation(signalArrows.get(2).getArrowTip(),translate.scalarMultiply(-0.6));
					GeoUtil.calcTranslation(signalArrows.get(2).getPolyline(),translate.scalarMultiply(-0.6));
				}
			}

		}
	}
	/*
	 * --------------------------------------------------------------------
	 * Returns the limiting speed in this lane. Speed is in meter/sec. This
	 * function should be overloaded to consider lane position (see class
	 * TS_Lane for more details)
	 * --------------------------------------------------------------------
	 */
	public double getFreeSpeed() {
		return segment.getFreeSpeed();
	}
	/*
	 * --------------------------------------------------------------------
	 * Returns the grade of the lane, in percent. Used to calculate the
	 * Acceleration and limiting speed.
	 * --------------------------------------------------------------------
	 */
	public double getGrade() {
		return segment.getGrade();
	}
	/*
	 * --------------------------------------------------------------------
	 * Returns the 1st surveillance station in this lane (upstream end)
	 * --------------------------------------------------------------------
	 */
	public List survList() {
		return (segment.getSensors());
	}
	/*
	 * --------------------------------------------------------------------
	 * Returns the 1st control station in this lane (upstream end)
	 * --------------------------------------------------------------------
	 */
	public List ctrlList() {
		return (segment.getCtrlList());
	}

	public int hasMarker() {
		return state & Constants.STATE_MARKED;
	}

	// Check if the lane is connected to a lane upstream/downstream.
	// markConnectedUpLanes() or markConnecteDnLanes() must be
	// called (usually for each lane in a segment) before the use of
	// the function isConnected(signature_bit), where signatures is
	// the bit masks (left lane is 1, middle lane is 2, right lane
	// is 4, and so on) of the upLanes that do the marking.

	public boolean isConnected(int signatures) {
		return (cmarker & signatures) != 0 ? true : false;
	}

	public double getDistance() {
		return segment.getDistance();
	}

	//wym
	public RoadNetwork getNetwork() {
		return segment.getNetwork();
	}
	
}
