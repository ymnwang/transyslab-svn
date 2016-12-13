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
 * Link实体
 *
 * @author YYL 2016-5-31
 */
public class Link extends CodedObject {

	protected List<SurvStation> SurvList;
	protected List<CtrlStation> CtrlList;
	protected int index_;
	protected Label label_;
	protected int type_;
	protected Node upNode_;
	protected Node dnNode_;
	protected int nSegments_;
	protected int startSegmentIndex_;
	// RN_LinkTime属性

	// protected static int infoPeriodLength_; // length of each period (second)
	// protected static int infoTotalLength_; // total length
	// protected static int infoPeriods_ = 1; // number of time periods
	// THESE VARIABLES HELP TO REPRESENT TOPOLOGY AND SPEED UP SOME
	// CALCULATION.

	// rightDnIndex_ is the position of the right most downstream
	// link in the array dnLinks of dnNode

	protected int rightDnIndex_;

	// dnIndex_ is the position of this link in the array dnLinks of
	// the upNode

	protected int dnIndex_;

	// Each bit of this variable represents a connection to dnLink

	protected int dnLegal_; // 0=turn is prohibited

	// upIndex_ is the position of this link in the array upLinks of
	// the dnNode

	protected int upIndex_;

	protected int laneUseRules_;

	// Control and surveillance devices

	protected List ctrlStationList_;
	protected List survStationList_;

	protected double length_; // length of the link
	protected double travelTime_; // latest travel time
	protected double freeSpeed_; // free flow speed

	protected int state_;

	// Link travel time

	// protected static int nPeriods_;
	// protected static int nSecondsPerPeriod_;

	// These functions are used to record the travel time vehicles
	// spend in each link, aggregated by the time they enter the
	// link.

	protected int[] nSamplesTravelTimeEnteringAt_;
	protected float[] sumOfTravelTimeEnteringAt_;
	// protected List<Integer> idList_ = new ArrayList<Integer>();
	protected static int last = -1;
	// 未处理
	/*
	 * public List ctrlList; public List survList; public List lastCtrl; public
	 * List lastSurv; public List prevCtrl; public List nextCtrl;
	 *
	 * public List prevSurv; public List nextSurv;
	 */

	public Link() {
		state_ = 0;
		nSamplesTravelTimeEnteringAt_ = null;
		sumOfTravelTimeEnteringAt_ = null;
	}
	public void setType(int t) {
		type_ = t;
	}
	public int getIndex() {
		return index_;
	}
	public void setState(int s) {
		state_ |= s;
	}
	public void unsetState(int s) {
		state_ &= ~s;
	}
	public int type() {
		return type_;
	}
	public int linkType() {
		return type_ & Constants.LINK_TYPE_MASK;
	}
	public int isInTunnel() {
		return type_ & Constants.IN_TUNNEL_MASK;
	}
	public Node getUpNode() {
		return upNode_;
	}
	public Node getDnNode() {
		return dnNode_;
	}
	public int getnSegments() {
		return nSegments_;
	}
	public int getStartSegmentIndex() {
		return startSegmentIndex_;
	}
	public int nSegments() {
		return nSegments_;
	}
	public Segment getSegment(int i) {
		return RoadNetwork.getInstance().getSegment(startSegmentIndex_ + i);
	}
	public Segment getStartSegment() {
		return RoadNetwork.getInstance().getSegment(startSegmentIndex_);
	}
	public Segment getEndSegment() {
		return RoadNetwork.getInstance().getSegment(startSegmentIndex_ + nSegments_ - 1);
	}
	// 新增方法，用于组织二维数组，方便输出
	public int[] getSumOfFlow() {
		return nSamplesTravelTimeEnteringAt_;
	}
	// 新增方法，用于组织二维数组，方便输出
	public float[] getAvgTravelTime() {
		int num = sumOfTravelTimeEnteringAt_.length;
		float[] avgtraveltime = new float[num];
		for (int i = 0; i < num; i++) {
			avgtraveltime[i] = averageTravelTimeEnteringAt(i);
		}
		return avgtraveltime;
	}
	public float calcCurrentTravelTime() {
		float tt = 0.0f;
		for (int i = 0; i < nSegments(); i++) {
			tt += getSegment(i).calcCurrentTravelTime();
		}
		return tt;
	}
	public float calcTravelTime() {
		return (float) travelTime_;
	}
	public double travelTime() {
		return travelTime_;
	}
	public double length() {
		return length_;
	}
	public double freeSpeed() {
		return freeSpeed_;
	}
	public int nUpLinks() {
		return (upNode_.nUpLinks());
	}
	public int nDnLinks() {
		return (dnNode_.nDnLinks());
	}
	public Link upLink(int i) {
		return (upNode_.getUpLink(i));
	}
	public Link dnLink(int i) {
		return (dnNode_.getDnLink(i));
	}
	// Generalized travel time used in RN_TravelTime and shortest
	// path calculation. It takes into account the freeway biases.
	public float generalizeTravelTime(double x) {
		if (linkType() != Constants.LINK_TYPE_FREEWAY) {
			x /= Parameter.getInstance().freewayBias();
		}
		return (float) x;
	}
	public float getGenTravelTime() {
		return generalizeTravelTime(travelTime_);
	}

	public double inboundAngle() {
		Segment ps = getEndSegment();
		return ps.getEndAngle();

	}
	public double outboundAngle() {
		Segment ps = getStartSegment();
		return ps.getStartAngle();
	}

	public void calcIndicesAtNodes() {
		dnIndex_ = upNode_.whichDnLink(this);
		upIndex_ = dnNode_.whichUpLink(this);
	}
	public void calcStaticInfo() {
		double alpha = inboundAngle() + Math.PI;
		double beta, min_beta;
		int i;
		int narcs = nDnLinks();

		// TOPOLOGY

		// We want angle in range [0, 2PI)

		if (alpha >= 2 * Math.PI)
			alpha -= 2 * Math.PI;

		min_beta = Constants.DBL_INF;
		rightDnIndex_ = 0xff;

		for (i = 0; i < narcs; i++) {

			beta = dnLink(i).outboundAngle() - alpha;

			// Skip the U turn

			// if (beta < 0.0 && -beta < v_angle) continue;
			// else if (beta > 0.0 && beta > u_angle) continue;

			// if (beta < 0.0) beta += TWO_PI; // angle is in range (0,2PI)
			if (beta <= 0.0)
				beta += 2 * Math.PI;

			// Choose the smallest angle, which is the "right down link"

			if (beta < min_beta) {
				min_beta = beta;
				rightDnIndex_ = (char) i;
			}
		}

		// LENGTH, FREE FLOW TRAVEL TIME AND SPEED

		Segment ps = getEndSegment();
		length_ = 0.0;
		travelTime_ = 0.0;
		while (ps != null) {
			ps.setDistance((float) length_);
			length_ += ps.getLength();
			travelTime_ += ps.getLength() / ps.getFreeSpeed();
			ps = ps.getUpSegment();
		}
		freeSpeed_ = length_ / travelTime_;

		// CONNECTIVITY

		// Connectivity to dnLinks

		dnLegal_ = 0; // set all bits to 0

		Lane plane = getEndSegment().getRightLane();
		while (plane != null) {

			// Check each downstream lane connected to plane

			int n = plane.nDnLanes();
			for (i = 0; i < n; i++) {
				includeTurn(plane.dnLane(i).getLink());
			}
			plane = plane.getLeft();
		}

		// LANE USE RULES

		laneUseRules_ = Constants.VEHICLE_LANE_USE;
		plane = getStartSegment().getRightLane();
		while (laneUseRules_ != 0 && plane != null) {
			laneUseRules_ &= (plane.rules() & Constants.VEHICLE_LANE_USE);
			plane = plane.getLeft();
		}
	}
	public int signalIndex(Link link) {
		int i;
		if (rightDnIndex_ != 0xFF && // defined
				link != null) {
			int n = nDnLinks();
			i = (link.getDnIndex() - rightDnIndex_ + n) % n;
		}
		else {
			i = 0;
		}
		return i;
	}
	public int getDnLegal() {
		return dnLegal_;
	}
	public int getDnIndex() {
		return dnIndex_;
	}
	public int getRightDnIndex() {
		return rightDnIndex_;
	}
	public int getUpIndex() {
		return upIndex_;
	}
	public void includeTurn(Link link) {
		dnLegal_ |= (1 << link.getDnIndex());
	}
	public void excludeTurn(Link link) {
		dnLegal_ &= ~(1 << link.getDnIndex());
	}
	public int isMovementAllowed(Link other) {
		return (dnLegal_ & (1 << other.getDnIndex()));
	}
	public int laneUseRules() {
		return laneUseRules_;
	}
	/*
	 * public void assignCtrlListInSegments(){ // Find the first control device
	 * or event in a segment by looking // the distance of the devices in that
	 * segment.
	 *
	 * RN_Segment ps; List ctrl = ctrlList(); while (ctrl) { ps =
	 * (ctrl).segment(); if (ps.ctrlList_ == null) { ps.ctrlList_ = ctrl; } else
	 * if (ctrl.distance() > ((ps.ctrlList_).distance()) { ps.ctrlList_ = ctrl;
	 * } ctrl = ctrl.next(); }
	 *
	 * // If no devices in a segment, the pointer should point to the // first
	 * device in the downstream segment of the link.
	 *
	 * ps = getEndSegment(); ctrl = ps.ctrlList_; while (ps = ps.upstream()) {
	 * if (ps.ctrlList_ == null) { ps.ctrlList_ = ctrl; } else { ctrl =
	 * ps.ctrlList_; } } } public void assignSurvListInSegments(){ // Find the
	 * first surveillance device in a segment by looking the // distance of the
	 * devives in that segment.
	 *
	 * RN_Segment ps; SurvList surv = survList(); while (surv) { ps =
	 * surv.segment(); if (ps.survList() == null) { ps.survList_ = surv; } else
	 * if (surv.distance() > ((ps.survList_)).distance()) { ps.survList_ = surv;
	 * } surv = surv.next(); }
	 *
	 * // If no devices in a segment, the pointer should point to the // first
	 * device in the downstream segment of the link
	 *
	 * ps = getEndSegment(); surv = ps.survList_; while (ps = ps.upstream()) {
	 * if (ps.survList_ == null) { ps.survList_ = surv; } else { surv =
	 * ps.survList_; } } }
	 *
	 * public List getCtrlStationList() { return ctrlStationList_; } public List
	 * getSurvStationList() { return survStationList_; }
	 */
	// This function is called by RN_Parser. It resturns -1 if a fatal
	// error occurs, 1 if warning errors, and 0 if no error.
	// notSolved

	public int init(int c, int t, int up, int dn/* , int l */) {

		/*
		 * if (ToolKit::debug()) { cout << indent << "<" << c << endc << t <<
		 * endc << up << endc << dn << endc << l << ">" << endl; }
		 */

		setCode(c);

		type_ = t;

		// Find the nodes coded as "up" and "dn"

		upNode_ = RoadNetwork.getInstance().findNode(up);
		dnNode_ = RoadNetwork.getInstance().findNode(dn);

		if (upNode_ == null) {
			// cerr << "Error:: Unknown UpNode <" << up << ">. ";
			return (-1);
		}
		else if (dnNode_ == null) {
			// cerr << "Error:: Unknown DnNode <" << dn << ">. ";
			return (-1);
		}

		upNode_.addDnLink(this);
		dnNode_.addUpLink(this);

		// Find pointer to the street name
		/*
		 * if (l > 0 && (label_ = theNetwork->findLabel(l)) == NULL) { // cerr
		 * << "Warning:: Unknown label <" << l << ">. "; return -1; } else {
		 * label_ = null; }
		 */

		index_ = RoadNetwork.getInstance().nLinks();
		nSegments_ = 0;
		startSegmentIndex_ = RoadNetwork.getInstance().nSegments();
		RoadNetwork.getInstance().addLink(this);

		return 0;
	}

	@Override
	public void print() {

	}
	public void printSensors() {

	}
	public void printSignals() {

	}
	public void printTollPlazas() {

	}

	public void printBusStops() {
		// margaret
	}

	public void printNotConnectedDnLinks() {

	}
	public int countNotConnectedDnLinks() {
		Link dnl;
		int cnt = 0;
		for (int i = 0; i < nDnLinks(); i++) {
			dnl = dnLink(i);
			if (!(isMovementAllowed(dnl) > 0)) {
				cnt++;
			}
		}
		return cnt;
	}
	// Find the first link (either a upLink or a dnLink) on the right side
	// of this link at the upstream node of this link. Search inbound
	// link first.
	public Link upRightNeighbor() {
		Link neighbor = null;
		Link link;
		int i, n;
		double alpha, beta;
		double min_beta = Constants.DBL_INF;

		alpha = outboundAngle();

		// For inbound links at the upstream node

		for (i = 0, n = upNode_.nUpLinks(); i < n; i++) {
			link = upNode_.getUpLink(i);
			beta = link.inboundAngle();
			beta += ((beta < Math.PI) ? (Math.PI) : (-Math.PI));
			beta = alpha - beta;
			if (beta < 0)
				beta += 2 * Math.PI;
			if (beta < Constants.U_ANGLE || beta > Constants.V_ANGLE)
				continue;
			if (beta < min_beta) {
				min_beta = beta;
				neighbor = link;
			}
		}

		// For outbound links at the upstream node

		for (i = 0, n = upNode_.nDnLinks(); i < n; i++) {
			link = upNode_.getDnLink(i);
			if (link == this)
				continue;
			beta = alpha - link.outboundAngle();
			if (beta < 0.0)
				beta += 2 * Math.PI;
			if (beta < Constants.U_ANGLE || beta > Constants.V_ANGLE)
				continue;
			if (beta + 1.E-6 < min_beta) {

				// This needs to be absolutely smaller considering float
				// number approximate error.

				min_beta = beta;
				neighbor = link;
			}
		}

		return neighbor;
	}
	// Find the first link (either a upLink or a dnLink) on the right side
	// of this link at the downstream node of this link. Searches
	// outbound link first.

	public Link dnRightNeighbor() {
		Link neighbor = null;
		Link link;
		int i, n;
		double alpha, beta;
		double min_beta = Constants.DBL_INF;

		alpha = inboundAngle();
		alpha += ((alpha < Math.PI) ? (Math.PI) : (-Math.PI));

		// For outbound links at the downstream node

		for (i = 0, n = dnNode_.nDnLinks(); i < n; i++) {
			link = dnNode_.getDnLink(i);
			beta = link.outboundAngle() - alpha;
			if (beta < 0.0)
				beta += 2 * Math.PI;
			if (beta < Constants.U_ANGLE || beta > Constants.V_ANGLE)
				continue;
			if (beta < min_beta) {
				min_beta = beta;
				neighbor = link;
			}
		}

		// For inbound links at the downstream node

		for (i = 0, n = dnNode_.nUpLinks(); i < n; i++) {
			link = dnNode_.getUpLink(i);
			if (link == this)
				continue;
			beta = link.inboundAngle();
			beta += ((beta < Math.PI) ? (Math.PI) : (-Math.PI));
			beta = beta - alpha;
			if (beta < 0)
				beta += 2 * Math.PI;
			if (beta < Constants.U_ANGLE || beta > Constants.V_ANGLE)
				continue;
			if (beta + 1.E-5 < min_beta) {

				// This needs to be absolutely smaller and considering the
				// float number approximate error.

				min_beta = beta;
				neighbor = link;
			}
		}

		return neighbor;
	}

	public int isMarked() {
		return state_ & Constants.STATE_MARKED;
	}

	public void unmarkLanes() {
		Segment ps = getStartSegment();
		while (ps != null) {
			Lane pl = ps.getRightLane();
			while (pl != null) {
				pl.unsetState(Constants.STATE_MARKED);
				pl = pl.getLeft();
			}
			ps = ps.getDnSegment();
		}
	}
	public void unmarkLanesInUpLinks() {
		int narcs = nUpLinks();
		for (int i = 0; i < narcs; i++) {
			upLink(i).unmarkLanes();
		}
	}
	public void unmarkLanesInDnLinks() {
		int narcs = nDnLinks();
		for (int i = 0; i < narcs; i++) {
			dnLink(i).unmarkLanes();
		}
	}
	/*
	 * //待处理 public void calcPathCommonalityFactors(){ int i, j; Map<int, int,
	 * less<int> > lid_type; Set<int, less<int> > nid_type;
	 *
	 * // Destination nodes completed so far
	 *
	 * nid_type nids; nid_type::iterator ni;
	 *
	 * RN_PathPointer pp; RN_Path p; RN_Link pl; RN_Node pn;
	 *
	 * for (int di = 0; di < nPathPointers(); di ++) { pp = pathPointer(di); pn
	 * = pp.desNode(); ni = nids.find(pn.get_code()); if (ni != nids.end())
	 * continue; // already processed
	 *
	 * nids.insert(pn.get_code()); // register the node being processed
	 *
	 * // Number of paths that connected to the same destination and // share a
	 * given link
	 *
	 * lid_type lids; lid_type::iterator li; // cout <<
	 * " I am in the Outer Loop \n" ;
	 *
	 * // Count the number of paths that share each link
	 *
	 * for (i = 0; i < nPathPointers(); i ++) {
	 *
	 * // cout << " number of Paths " << nPathPointers() << "\n"; pp =
	 * pathPointer(i); if (pp->desNode() != pn) continue; // path not connected
	 * to pn
	 *
	 * p = pp->path(); // for (j = pp->position() + 1; j < p->nLinks(); j ++) {
	 * for (j = pp->position(); j < p->nLinks(); j ++) { // cout << "Position "
	 * << pp->position() << " j " << j << " Number of Links " << p->nLinks() <<
	 * "\n"; pl = p->link(j); li = lids.find(pl->code()); if (li == lids.end())
	 * { // first time lids[pl->code()] = 1; } else { // count the use of the
	 * link pl lids[pl->code()] += 1; } // cout << " lids for " << pl->code() <<
	 * "is " << lids[pl->code()] << "\n"; } }
	 *
	 * // Calculate commonality factors for paths connected to node pn
	 *
	 * for (i = 0; i < nPathPointers(); i ++) { pp = pathPointer(i); if
	 * (pp->desNode() != pn) continue; // path not connected to pn
	 *
	 * p = pp->path();
	 *
	 * float cf = 0.0; float total_length = 0.0; // for (j = pp->position() + 1;
	 * j < p->nLinks(); j ++) { for (j = pp->position(); j < p->nLinks(); j ++)
	 * { pl = p->link(j); total_length += pl->length(); cf += lids[pl->code()] *
	 * pl->length(); // cout << " Inside the first cf loop " << cf << "\n"; } if
	 * (cf > FLT_MIN) { cf = log(cf / total_length); } pp->cf(cf); // cout <<
	 * " cf " << cf << "\n"; }
	 *
	 * } } public void printPathCommonalityFactors(){
	 *
	 * }
	 */

	public int isCorrect(Vehicle pv) {
		// 未处理，int转boolean
		if (laneUseRules_ == 0 || (laneUseRules_ & (pv.types() & Constants.VEHICLE_LANE_USE)) != 0)
			return 1;
		else
			return 0;
	}
	/*
	 * public static int infoPeriods(){ return theGuidedRoute.infoPeriods(); }
	 * public static int infoPeriodLength(){ return
	 * theGuidedRoute.infoPeriodLength(); }
	 */
	public void resetStatistics(int col, int ncols) {
		int num = col + ncols;
		num = Math.min(LinkTimes.getInstance().infoPeriods(), num);
		for (int i = col; i < num; i++) {
			nSamplesTravelTimeEnteringAt_[i] = 0;
			sumOfTravelTimeEnteringAt_[i] = 0;
		}
	}
	// 新增代码，复位统计Link流量和旅行时间的数组，用于参数校准，仿真重启
	public void resetStatistics() {
		int num = LinkTimes.getInstance().infoPeriods();
		for (int i = 0; i < num; i++) {
			nSamplesTravelTimeEnteringAt_[i] = 0;
			sumOfTravelTimeEnteringAt_[i] = 0;
		}
	}
	// Link travel time

	public void initializeStatistics() {
		int n = LinkTimes.getInstance().infoPeriods();
		nSamplesTravelTimeEnteringAt_ = new int[n];
		sumOfTravelTimeEnteringAt_ = new float[n];
		resetStatistics(0, n);
	}

	// called when a vehicle leaves the link

	public void recordTravelTime(Vehicle pv) {
		// Tavel time spent in current segment

		float tt = pv.timeInLink();

		// These are for calculating average travel time for the vehicle
		// who ENTER this segment during the reporting time interval.

		// Calculate the ID of the entry time period.

		int i = LinkTimes.getInstance().whichPeriod(pv.timeEntersLink());

		// Time spent in this link
		// idList_.add(pv.get_code());
		sumOfTravelTimeEnteringAt_[i] += tt;
		nSamplesTravelTimeEnteringAt_[i]++;
	}

	// called for each vehicle in the network, include these in
	// the pretrip queues, at the end of the simulation

	public void recordExpectedTravelTime(Vehicle pv) {
		// Tavel time spent in current link

		double pos;

		if (pv.segment() != null) {
			pos = pv.distanceFromDownNode();
		}
		else {
			pos = length_;
		}

		// Calculate the ID of the entry time period.

		int i = LinkTimes.getInstance().whichPeriod(pv.timeEntersLink());

		float ht = LinkTimes.getInstance().linkTime(this, pv.timeEntersLink());
		float tt = pv.timeInLink();

		// Section added Joseph Scariza 11/6/01
		if (pos < 0.25 * length_) {
			tt += tt * pos / (length_ - pos);
		}
		// Accumulate vehicle's time in this link

		sumOfTravelTimeEnteringAt_[i] += tt;
		nSamplesTravelTimeEnteringAt_[i]++;

	}

	// These are valid only after all the vehicles entered the
	// link in interval i have left the link.

	public float averageTravelTimeEnteringAt(double enter) {
		int i = LinkTimes.getInstance().whichPeriod(enter);
		return averageTravelTimeEnteringAt(i);
	}
	public float averageTravelTimeEnteringAt(int i) {
		int num = nSamplesTravelTimeEnteringAt_[i];
		if (num > 0) {
			return sumOfTravelTimeEnteringAt_[i] / num;// 这玩意儿坑人！：/*/0.3047;*/
		}
		else { // no sample
			int p = i - 1, n = i + 1;
			int m = LinkTimes.getInstance().infoPeriods();

			// Find the first no empty previous time intervals
			while (p >= 0 && nSamplesTravelTimeEnteringAt_[p] == 0)
				p--;

			// Find the first no empty next time intervals
			while (n < m && nSamplesTravelTimeEnteringAt_[n] == 0)
				n++;

			float tt = 0;
			num = 0;
			if (p >= 0) { // use a previous interval
				tt += sumOfTravelTimeEnteringAt_[p];
				num += nSamplesTravelTimeEnteringAt_[p];
			}
			if (n < m) { // use a next interval
				tt += sumOfTravelTimeEnteringAt_[n];
				num += nSamplesTravelTimeEnteringAt_[n];
			}

			if (num > 0) {
				return tt / num;
			}
			else { // cout<<"fs"<<freeSpeed_<<endl; // use free flow travel time
				return (float) (length_ / freeSpeed_);// 同理坑人0.3047 ;
			}
		}
	}

	public void printTravelTimes() {

	}
	public void printReloadableTravelTimes() {

	}
	public void printFlowPlusTravelTimes(int runtimes) throws IOException {
		String opstr = aryToString(sumOfTravelTimeEnteringAt_, nSamplesTravelTimeEnteringAt_);
		String s = String.valueOf(runtimes);
		String filepath = "E:\\MesoOutput" + s + ".txt";
		FileOutputStream out = new FileOutputStream(filepath, true);
		OutputStreamWriter osw = new OutputStreamWriter(out, "utf-8");
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(opstr);
		bw.close();
	}

	private String aryToString(float[] ary1, int[] ary2) {
		StringBuilder sb = new StringBuilder();
		sb.append("LinkCode:").append(getCode());
		sb.append(",Length:").append(length());
		sb.append('\r');
		sb.append('\n');
		for (int i = 0; i < ary1.length; i++) {
			double t = averageTravelTimeEnteringAt(i);
			sb.append(t).append(",");
			if (i != ary1.length - 1) {
				sb.append(ary2[i]).append('\t');
			}
			else {
				sb.append(ary2[i]).append('\r');
				sb.append('\n');
			}
		}
		return sb.toString();
	}
	public void outputToOracle(PreparedStatement ps) throws SQLException {
		// String sql = "insert into MESO_OUTPUT_TEST (LinkID, CTime, Flow,
		// TravelTime) values (?, ?, ?, ?)";
		// Connection connection = JdbcUtils.getConnection();
		// ps = con.prepareStatement(sql);
		int num = nSamplesTravelTimeEnteringAt_.length;
		for (int i = 0; i < num; i++) {
			Date date = LinkTimes.getInstance().toDate((i + 1));
			ps.setInt(1, getCode());
			ps.setDate(2, new java.sql.Date(date.getTime()));
			ps.setTimestamp(2, new java.sql.Timestamp(date.getTime()));
			ps.setInt(3, nSamplesTravelTimeEnteringAt_[i]);
			ps.setFloat(4, averageTravelTimeEnteringAt(i));
			ps.addBatch();
		}
		ps.executeBatch();
	}

}
