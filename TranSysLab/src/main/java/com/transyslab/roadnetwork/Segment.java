/**
 *
 */
package com.transyslab.roadnetwork;

import com.transyslab.commons.tools.GeoUtil;

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
public class Segment implements NetworkObject {

	protected int id;
	protected String name;
	protected String objInfo;
	protected List<Lane> lanes;
	protected Segment upSegment;
	protected Segment dnSegment;
	protected int index; // index in array

	protected Link link; // pointer to link
	protected double grade; // grade of the segment

	//	protected int leftLaneIndex; // index to the left lane
	protected int speedLimit; // default speed limit
	protected double freeSpeed; // free flow speed
	protected double distance; // getDistance from dn node

	protected List<CtrlStation> ctrlStations; // first (upstream) control station
	protected List<Sensor> sensors; // first (upstream) sensor station


	protected int state;
	protected int localType; // head, tail, etc

	protected GeoPoint startPnt;
	protected GeoPoint endPnt;
	protected double bulge;
	protected GeoSurface surface;

	protected double startAngle;
	protected double endAngle;

	protected double length;
	protected boolean isSelected;

	public Segment() {
		distance = 0.0;
		localType = 0;
		state = 0;
		lanes = new ArrayList<>();
	}
	public int getId(){
		return this.id;
	}
	public String getName(){
		return this.name;
	}
	public String getObjInfo(){
		return this.objInfo;
	}
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
	public int isHead() {
		return (localType & 0x0001);
	}
	public int isTail() {
		return (localType & 0x0002);
	}
	public int isTheEnd() {
		return (localType & 0x0020);
	}
	public int isTheBeginning() {
		return (localType & 0x0010);
	}
	public int getIndex() {
		return index;
	}

	// Index within the link. 0 is the upstream.
	public int localIndex() {
		return (index - link.getStartSegment().index);
	}
	public int getType() {
		return (link.type());
	}
	public GeoPoint getStartPnt() {
		return startPnt;
	}
	public GeoPoint getEndPnt() {
		return endPnt;
	}
	public void setStartPnt(GeoPoint p) {
		startPnt = p;
	}
	public void setEndPnt(GeoPoint p) {
		endPnt = p;
	}
	public double getLength() {
		return length;
	}
	public double getBulge() {
		return bulge;
	}

	public double getStartAngle() {
		// For a straight line, startAngle is the angle of the
		// line. For a curve, startAngle is the angle of the line
		// from the center to the point startPnt.
		return startAngle;
	}
	public double getEndAngle() {
		// For a straight line, endAngle is the angle of the
		// line. For a curve, endAngle is the angle of the line from
		// the center to the point endPnt. Note: arcAngle is zero for
		// a straight line.
		return endAngle;
	}
	public GeoSurface getSurface(){
		return surface;
	}
	// Segment iterators in a link
	// Returns the upstream segment in the same link
	public Segment getUpSegment() {
		return this.upSegment;
	}
	public Segment getDnSegment() {
		return this.dnSegment;
	}

	// Lane iterators in a segmen
	// Returns the left most lane in the segment
	public Lane getLeftLane() {
		return lanes.get(0);
	}
	// Returns the right most lane in the segment

	public Lane getRightLane() {
		return lanes.get(lanes.size()-1);
	}
	// Returns the ith lane in the segment
	public Lane getLane(int index) {
		return lanes.get(index);
	}
	public Link getLink() {
		return link;
	}

	public List<CtrlStation> getCtrlList() {
		return ctrlStations;
	}
	public List<Sensor> getSensors() {
		return sensors;
	}
	public void addSensor(Sensor e){
		sensors.add(e);
	}
	public void addLane(Lane lane){
		this.lanes.add(lane);
	}
	public void init(int id, int speed_limit, int index, double speed, double grd, Link link) {
		// YYL
		if (this.link != null) {
			// cerr << "Error:: Segment <" << id << "> "
			System.out.print("cannot be initialized twice. ");
			return ;
		}
		else {
			this.link = link;
		}
		this.id = id;
		this.speedLimit = speed_limit;
		this.grade = grd;
		this.index = index;
		this.link.addSegment(this);
	}
	public void initArc(double x1, double y1, double b, double x2, double y2) {
		this.startPnt = new GeoPoint(x1, y1);
		this.endPnt = new GeoPoint(x2, y2);
		this.bulge = -b;
	}


	// Segment attributes

	public int nLanes() {
		return lanes.size();
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double x) {
		distance = x;
	}
	public int speedLimit() {
		return speedLimit;
	}
	public void setSpeedLimit(int sl) {
		speedLimit = sl;
	}
	/*
	public void setFreeSpeed(float f) {
		freeSpeed = f;
	}*/
	public double getFreeSpeed() {
		return this.freeSpeed;
	}

	public void setGrade(double g) {
		this.grade = g;
	}
	public double getGrade() {
		return this.grade;
	}
	public double calcCurrentTravelTime() {
		return 0;
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
		startPnt = world_space.worldSpacePoint(startPnt);
		endPnt = world_space.worldSpacePoint(endPnt);
		length = startPnt.distance(endPnt);
		startAngle = endAngle = startPnt.angle(endPnt);

	}

	// Calculate the data that do not change in the simulation.
	public void calcStaticInfo() {
		if ((getDnSegment() == null)) {
			localType |= 0x0001;
			if (getLink().nDnLinks() < 1 || getLink().getDnNode().type(0x0001) > 0) {
				localType |= 0x0020;
			}
		}
		if (getUpSegment() == null) {
			localType |= 0x0002;
			if (getLink().nUpLinks() < 1 || getLink().getUpNode().type(0x0001) > 0) {
				localType |= 0x0010;
			}
		}
		// 创建路段面
		createSurface();
	}
	// 路网世界坐标平移后再调用
	public void createSurface(){
		surface = GeoUtil.lineToRectangle(startPnt, endPnt, lanes.size()* Constants.LANE_WIDTH, false);
	}
	public double calcDensity() {
		return 0;
	}
	public double calcSpeed() {
		return 0;
	}
	public int calcFlow() {
		return 0;
	}
	// 虚拟方法，由子类继承实现流速密计算

	public int isNeighbor(Segment sgmt) {
		if (this.getLink() == sgmt.getLink()) {
			if ((this.getIndex() + 1) == sgmt.getIndex())
				return 1;
			else
				return 0;
		}
		else if (this.getDnSegment() != null || sgmt.getUpSegment() != null) {
			return 0;
		}
		else if (this.getLink().isNeighbor(sgmt.getLink()) == 0) {
			return 0;
		}
		return 1;
	}
	public void outputSegment() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(this.id).append(",");
		sb.append(startPnt.getLocationX()).append(",");
		sb.append(startPnt.getLocationY()).append(",");
		sb.append(endPnt.getLocationX()).append(",");
		sb.append(endPnt.getLocationY()).append("\n");
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

	public List<Lane> getLanes() {
		return lanes;
	}

	//wym
	public RoadNetwork getNetwork() {
		return link.getNetwork();
	}
}
