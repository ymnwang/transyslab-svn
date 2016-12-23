/**
 *
 */
package com.transyslab.simcore.mesots;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.SdFn;

/**
 * @author its312
 *
 */
public class MesoTrafficCell {

	protected MesoSegment segment_; // container

	// Data members calculated in update phase

	protected float tailSpeed_; // upstream speed
	protected float tailPosition_; // upstream position

	protected int nHeads_; // number of heads
	protected float[] headSpeeds_; // downstream speeds
	protected float[] headPositions_; // downstream positions

	// Data members dynamically changed in advance phase

	protected int nVehicles_; // number of vehicles

	protected MesoVehicle firstVehicle_; // first vehicle (dn) in this TC
	protected MesoVehicle lastVehicle_; // last vehicle (up) in this TC

	// Bookkeeping data members. May change in both update and
	// advance phases

	protected MesoTrafficCell trailing_; // upstream traffic cell
	protected MesoTrafficCell leading_; // downstream traffic cell

	protected double queueTime_; // time was a queue

	public MesoTrafficCell() {
		trailing_ = null;
		leading_ = null;
		firstVehicle_ = null;
		lastVehicle_ = null;
		nVehicles_ = 0;
		headSpeeds_ = null;
		headPositions_ = null;
		nHeads_ = 0;
		segment_ = null;
	}

	public void clean() {
		while (firstVehicle_ != null) {
			lastVehicle_ = firstVehicle_;
			firstVehicle_ = firstVehicle_.trailing();
			MesoVehicleList.getInstance().recycle(lastVehicle_);
		}
		lastVehicle_ = null;
		nVehicles_ = 0;

		segment_ = null;

		if (headSpeeds_ != null) {
			// delete [] headSpeeds_;
			headSpeeds_ = null;
		}
		if (headPositions_ != null) {
			// Œ¥¥¶¿Ì delete [] headPositions_;
			headPositions_ = null;
		}
		nHeads_ = 0;
	}

	public MesoTrafficCell trailing() {
		return trailing_;
	}
	public MesoTrafficCell leading() {
		return leading_;
	}

	public void initialize() {
		queueTime_ = -Constants.FLT_INF;

		firstVehicle_ = lastVehicle_ = null;
		nVehicles_ = 0;

		nHeads_ = link().nDnLinks();

		if (segment_.isHead() == 0 || // not in the last segment in the link
				nHeads_ == 0) { // dead end or boundary link
			nHeads_ = 1;
		}

		headSpeeds_ = new float[nHeads_];
		headPositions_ = new float[nHeads_];

		tailSpeed_ = segment_.maxSpeed();

		tailPosition_ = (float) (segment_.getLength() - tailSpeed_ * SimulationClock.getInstance().getStepSize());
		for (int i = 0; i < nHeads_; i++) {
			headSpeeds_[i] = tailSpeed_;
			headPositions_[i] = tailPosition_;
		}
	}

	public MesoLink mesoLink() {
		return link();
	}

	public MesoVehicle firstVehicle() {
		return firstVehicle_;
	}
	public MesoVehicle lastVehicle() {
		return lastVehicle_;
	}
	public int nVehicles() {
		return nVehicles_;
	}

	// Insert a vehicle into the cell based on its distance position
	/*
	 * public void insert(MESO_Vehicle pv) { // Find the front vehicle
	 *
	 * MESO_Vehicle front = lastVehicle_; while (front!=null && front.distance()
	 * > pv.distance()) { front = front.leading_; }
	 *
	 * // Insert after the front vehicle
	 *
	 * if (front!=null) { // pv is NOT the first in cell pv.trailing_ =
	 * front.trailing_; front.trailing_ = pv; } else { // pv is the first in
	 * cell pv.trailing_ = firstVehicle_; firstVehicle_ = pv; } if
	 * (pv.trailing_!=null) { pv.trailing_.leading_ = pv; } else { lastVehicle_
	 * = pv; }
	 *
	 * pv.trafficCell_ = this; pv.calcSpaceInSegment();
	 *
	 * nVehicles_ ++; }
	 */

	// Append a vehicle at the end of the cell

	public void append(MesoVehicle vehicle) {
		vehicle.leading_ = lastVehicle_;
		vehicle.trailing_ = null;

		if (lastVehicle_ != null) { // append at end
			lastVehicle_.trailing_ = vehicle;
		}
		else { // queue is empty
			firstVehicle_ = vehicle;
		}
		lastVehicle_ = vehicle;
		nVehicles_++;

		vehicle.appendTo(this);

		if (nVehicles_ <= 1) { // first vehicle
			updateTailSpeed();
			updateHeadSpeeds();
		}
	}
	 public void appendSnapshot(MesoVehicle vehicle){
		vehicle.leading_ = lastVehicle_;
		vehicle.trailing_ = null;
		
		if (lastVehicle_!=null) {		// append at end
		      lastVehicle_.trailing_ = vehicle;
		} else {			// queue is empty
		      firstVehicle_ = vehicle;
		}
		lastVehicle_ = vehicle;
		nVehicles_ ++;
		
		vehicle.appendSnapshotTo(this);
		
		if (nVehicles_ <= 1) {		// first vehicle
			updateTailSpeed();
			updateHeadSpeeds();
		}
	}
	// Split the traffic cell into two cells at the first gap
	// smaller than the given threshold
	/*
	 * public void split() { MESO_Vehicle front = firstVehicle_; MESO_Vehicle pv
	 * = null; while (front!=null && (pv = front.trailing_)!=null &&
	 * pv.distance() <= front.upPos() +
	 * MESO_Parameter.getInstance().cellSplitGap()) { front = pv; } if
	 * (pv==null) return; // not need to split
	 *
	 * // SPLIT THE CELL BETWEEN front AND pv
	 *
	 * // Create a new cell and put it after the current cell
	 *
	 * MESO_TrafficCell cell = MESO_CellList.getInstance().recycle();
	 *
	 * // We do not call initialize() as usual, but we copy the variable // from
	 * this to the new cell
	 *
	 * cell.leading_ = this; cell.trailing_ = trailing_; if (trailing_!=null) {
	 * // this is not the last in segment trailing_.leading_ = cell; } else { //
	 * this is the last in segment segment_.lastCell_ = cell; } trailing_ =
	 * cell; segment_.nCells_ ++;
	 *
	 * // Copy variables from this cell to the new cell that follows
	 *
	 * cell.segment_ = segment_; cell.tailSpeed_ = tailSpeed_;
	 * cell.tailPosition_ = tailPosition_; cell.nHeads_ = nHeads_;
	 * cell.headSpeeds_ = new float [nHeads_]; cell.headPositions_ = new float
	 * [nHeads_]; for (int i = 0; i < nHeads_; i ++) { cell.headSpeeds_[i] =
	 * headSpeeds_[i]; cell.headPositions_[i] = headPositions_[i]; }
	 *
	 * // Cut the vehicle list into two
	 *
	 * pv.leading_ = null; // first in the new cell front.trailing_ = null; //
	 * last in this cell cell.firstVehicle_ = pv; // first in the new cell
	 * cell.lastVehicle_ = lastVehicle_; // last in the new cell lastVehicle_ =
	 * front; // last in this cell
	 *
	 * // Change container for the vehicles in the new cell
	 *
	 * cell.nVehicles_ = 0; while (pv!=null) { pv.trafficCell_ = cell; pv =
	 * pv.trailing_; cell.nVehicles_ ++; }
	 *
	 * // Update vehicle counter and length_
	 *
	 * nVehicles_ -= cell.nVehicles_; }
	 */

	public void remove(MesoVehicle vehicle) {
		if (vehicle.leading_ != null) { // not the first one
			vehicle.leading_.trailing_ = vehicle.trailing_;
		}
		else { // first one
			firstVehicle_ = vehicle.trailing_;
		}
		if (vehicle.trailing_ != null) { // not the last one
			vehicle.trailing_.leading_ = vehicle.leading_;
		}
		else { // last one
			lastVehicle_ = vehicle.leading_;
		}
		nVehicles_--;
	}

	// Append the following cell to the end of this cell, the
	// following cell becomes empty but is NOT removed.
	/*
	 * public void append(MESO_TrafficCell cell) { MESO_Vehicle vehicle =
	 * cell.firstVehicle_;
	 *
	 * if (vehicle==null) { return; }
	 *
	 * // Change container for the vehicles in cell
	 *
	 * while (vehicle!=null) { vehicle.trafficCell_ = this; vehicle =
	 * vehicle.trailing_; }
	 *
	 * // Connect the two cells
	 *
	 * if (lastVehicle_!=null) { // this cell is not empty
	 * lastVehicle_.trailing_ = cell.firstVehicle_; } else { // this cell is
	 * empty firstVehicle_ = cell.firstVehicle_; } cell.firstVehicle_.leading_ =
	 * lastVehicle_; lastVehicle_ = cell.lastVehicle_;
	 *
	 * // Update vehicle counter
	 *
	 * nVehicles_ += cell.nVehicles_;
	 *
	 * // Update the pointers and vehicle counts in cell
	 *
	 * cell.nVehicles_ = 0; cell.firstVehicle_ = cell.lastVehicle_ = null; }
	 */

	public MesoLink link() {
		return ((segment_ != null) ? (MesoLink) segment_.getLink() : (MesoLink) null);
	}
	public MesoSegment segment() {
		return segment_;
	}

	// This function decide which speed-density function will be
	// used for this traffic cell

	public int sdIndex() {
		return segment_.getSdIndex();
	}

	// These are the dn position of the first and up position of the
	// last vehicles in the cell at the moment this function is
	// called.

	public float dnDistance() {
		if (firstVehicle_ != null) {
			return firstVehicle_.distance();
		}
		else {
			return maxReachablePosition();
		}
	}
	public float upDistance() {
		if (lastVehicle_ != null) {
			return lastVehicle_.upPos();
		}
		else {
			return maxReachablePosition();
		}
	}
	public float maxReachablePosition() {
		float dx = (float) (segment_.maxSpeed() * SimulationClock.getInstance().getStepSize());
		dx = (float) (segment_.getLength() - dx);
		if (leading_ != null) {
			float pos = leading_.upDistance();
			dx = (dx > pos) ? dx : pos;
		}
		return (dx > 0.0f ? dx : 0.0f);
	}

	public float length() {
		if (firstVehicle_ != null) {
			return upDistance() - firstVehicle_.distance();
		}
		else {
			return 0.0f;
		}
	}

	// These are the dn position of the first and the up position of
	// the last vehicles in the cell when this traffic cell is
	// updated. These values are changed at the update phase.

	public float tailPosition() {
		return tailPosition_;
	}

	public float tailSpeed() {
		return tailSpeed_;
	}

	public float headPosition(int i) {
		return (i < nHeads_) ? headPositions_[i] : headPositions_[0];
	}

	public float headSpeed(int i) {
		return (i < nHeads_) ? headSpeeds_[i] : headSpeeds_[0];
	}
	/*
	 * public float headSpeed() // average of the headSpeeds { if (nHeads_ < 2)
	 * { // single head return headSpeeds_[0]; } else { // multiple heads float
	 * sum = 0.0f; for (int i = 0; i < nHeads_; i ++) { sum += headSpeeds_[i]; }
	 * return sum / (float)nHeads_; } }
	 *
	 * public float speed() // average speed of the heads and tail { return 0.5f
	 * * (tailSpeed_ + headSpeed()); }
	 */

	// Set the speed of each head vehicle to the same speed and record the
	// reference position
	public void setHeadSpeeds(float spd, float pos) {
		for (int i = 0; i < nHeads_; i++) {
			headPositions_[i] = pos;
			if (headSpeeds_[i] > 0.1) { // This stream was moving
				float maxspd = MesoParameter.getInstance().queueReleasingSpeed(timeSinceDispatching(),
						segment_.maxSpeed());
				if (spd > maxspd) {
					headSpeeds_[i] = maxspd;
				}
				else {
					headSpeeds_[i] = spd;
				}
			}
			else { // stopped
				headSpeeds_[i] = spd;
				queueTime_ = SimulationClock.getInstance().getCurrentTime();
			}
		}
	}

	// These two are based on current state and referenced to the
	// last vehicle in the cell

	public float calcDensity() {
		float len = length() * segment_.nLanes();
		if (len > MesoParameter.getInstance().rspLower()) {
			return 1000.0f * nVehicles_ / len;
		}
		else {
			return 0.0f;
		}
	}

	// Calculate the speed of the last vehicle
	public float calcSpeed() {
		// cout << " First Calc Speed " ;
		float len = length();
		if (len > MesoParameter.getInstance().cellSplitGap()) {
			return calcSpeed((float) (1000.0 * nVehicles_ / (len * segment_.nLanes())));
		}
		else if (len < MesoParameter.getInstance().rspLower()) {
			// for very short cell consider the leading cell also
			if (leading_ != null) {
				len += distance(leading_) + leading_.length();
				int num = nVehicles_ + leading_.nVehicles();
				return calcSpeed((float) (1000.0 * num / (len * segment_.nLanes())));
			}
			else {
				return segment_.maxSpeed();
			}
		}
		else {
			// speed-density function will not work for short cells
			return segment_.maxSpeed(len * segment_.nLanes() / nVehicles_);
		}
	}

	// This is the current speed of last vehicle in the cell
	public float upSpeed() {
		if (lastVehicle_ != null) {
			return lastVehicle_.currentSpeed();
		}
		else {
			return segment_.maxSpeed();
		}
	}

	// Speed for a given density

	public float calcSpeed(float density) {
		SdFn sdf = MesoNetwork.getInstance().getSdFn(sdIndex());
		return sdf.densityToSpeed(segment_.maxSpeed(), density, segment_.nLanes());
	}

	// These calculations are based on states at the beginning of
	// current update interval

	public void updateTailSpeed() {
		tailPosition_ = upDistance();
		tailSpeed_ = calcSpeed();
	}

	// Calculates the downstream speeds based on the space from and speed
	// of the downstream traffic cells in ealier. This function is called
	// at least once in every update phase and when cells are created or
	// combined
	public void updateHeadSpeeds() {
		int i;

		// downstream position of the cell

		float dnx = dnDistance();

		if (segment_.isMoveAllowed() == 0 && dnx < 1.0) {

			// The output capacity is a constraint

			setHeadSpeeds(0.0f, dnx);
			return;
		}

		if (leading_ != null) {

			// There is a cell ahead, speed is determined based on the
			// reaction to that cell.

			setHeadSpeeds(calcHeadSpeed(leading_), dnx);
			return;

		}
		else if (dnx > MesoParameter.getInstance().rspUpper()) {

			// Since no cell ahead and distance is greater than a threshold,
			// it use free flow speed.

			setHeadSpeeds(segment_.maxSpeed(), dnx);
			return;

		}
		else if (segment_.isHead() == 0) {

			// Not the last segment in the link, speed is based on
			// downstrean condition

			setHeadSpeeds(calcHeadSpeed(segment_.downstream().lastCell()), dnx);
			return;

		}
		else if (nHeads_ > 1) {

			// At the end of the last segment in the link and there is no
			// cell ahead. Need to check where the vehicles in this cell
			// want to go and the condition in the downstream links.

			for (i = 0; i < nHeads_; i++) {
				calcHeadSpeed(i, dnx);
			}
			return;

		}
		else if (segment_.isTheEnd() != 0) {

			// Boundary link, no constrains

			setHeadSpeeds(segment_.maxSpeed(), dnx);
			return;

		}
		else {

			// At the end of the last segment in the link. This link has
			// one outgoing link.

			MesoSegment ps = (MesoSegment) link().dnLink(0).getStartSegment();
			setHeadSpeeds(calcHeadSpeed(ps.lastCell()), dnx);

			return;
		}
	}

	// Calculate head speed for the ith traffic stream.
	public void calcHeadSpeed(int ith, float dnx) {
		// Capacity based speed

		float maxspeed = MesoParameter.getInstance().queueReleasingSpeed(timeSinceDispatching(), segment_.maxSpeed());

		MesoLink dnlink = (MesoLink) link().dnLink(ith);

		// Find the first vehicle heading to the ith downstream link and
		// number of vehicles ahead (in the same cell)

		MesoVehicle vehicle = firstVehicle_;
		int n = 0;
		while (vehicle != null && vehicle.nextLink() != dnlink) {
			vehicle = vehicle.trailing_;
			n++;
		}

		MesoSegment dnseg = (MesoSegment) dnlink.getStartSegment();
		MesoTrafficCell cell = dnseg.lastCell();

		if (vehicle != null) {
			headPositions_[ith] = dnx = vehicle.distance();
		}
		else {
			headPositions_[ith] = dnx;
		}

		// Speed in response to the downstream traffic cell

		float spd_by_rsp;
		if (cell != null) {
			float gap = (float) (dnx + dnseg.getLength() - cell.upDistance());
			spd_by_rsp = calcFollowingCellSpeed(gap, cell.tailSpeed());
		}
		else {
			spd_by_rsp = maxspeed;
		}

		// Density-based speed

		float spd_by_den;
		int nlanes = segment_.nLanes();
		if (vehicle == null || dnx < 10.0 * nlanes) {
			spd_by_den = spd_by_rsp;
		}
		else if (n > nlanes) {
			float k = 1000.0f * n / (dnx * nlanes);
			spd_by_den = calcSpeed(k);
			if (nlanes > 1 && spd_by_den < MesoParameter.getInstance().minSpeed()) {
				int num = link().dnLink(ith).getStartSegment().nLanes();
				spd_by_den = MesoParameter.getInstance().minSpeed() * num;
			}
		}
		else {
			spd_by_den = maxspeed;
		}

		float spd = Math.min(spd_by_rsp, spd_by_den);

		if (spd < maxspeed) {
			headSpeeds_[ith] = spd;
		}
		else {
			headSpeeds_[ith] = maxspeed;
		}

		if (headSpeeds_[ith] < 0.1) {
			queueTime_ = SimulationClock.getInstance().getCurrentTime();
		}
	}

	// Calculate the speed based on the relationship with the given
	// downstream traffic cell
	public float calcHeadSpeed(MesoTrafficCell cell) {
		float rspspd;
		if (cell != null) {
			rspspd = calcFollowingCellSpeed(distance(cell), cell.tailSpeed());
		}
		else {
			rspspd = segment_.maxSpeed();
		}
		return rspspd;
	}

	// Calculate the speed based on the headway distance from the leading
	// cell
	public float calcFollowingCellSpeed(float x, float v) {
		float maxgap = MesoParameter.getInstance().rspUpper();
		if (x >= maxgap) {
			return segment_.maxSpeed();
		}
		else if (x <= 0.1) {
			return v;
		}
		else {
			float r = x / maxgap;
			return r * segment_.maxSpeed(x) + (1.0f - r) * v;
		}
	}

	public void advanceVehicles() {
		MesoVehicle vehicle = firstVehicle_;
		MesoVehicle next;
		while (vehicle != null) {
			next = vehicle.trailing_;
			if (vehicle.isProcessed() == 0) {
				vehicle.move();
			}
			vehicle = next;
		}
	}

	public float distance(MesoTrafficCell cell) {
		// skip the empty cells

		while (cell != null && cell.lastVehicle_ == null) {
			cell = cell.leading_;
		}

		float dis = (float) ((firstVehicle_ != null) ? dnDistance() : segment_.getLength());
		if (cell != null) {
			if (cell.segment() == segment()) { // same segment
				dis -= cell.upDistance();
			}
			else { // different segment
				dis += cell.segment().getLength() - cell.upDistance();
			}
		}
		else { // no cell ahead
			dis = Constants.FLT_INF;
		}
		return dis;
	}

	public int isJammed() {
		if (lastVehicle_ != null && lastVehicle_.upPos() >= segment_.getLength()) {
			return 1;
		}
		else {
			return 0;
		}
	}
	public boolean isReachable() {
		return (segment_.getLength() - upDistance()) < MesoParameter.getInstance().cellSplitGap();
	}

	public float timeSinceDispatching() {
		return (float) (SimulationClock.getInstance().getCurrentTime() - queueTime_);
	}

}
