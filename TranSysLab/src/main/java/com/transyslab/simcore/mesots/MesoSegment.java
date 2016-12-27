/**
 *
 */
package com.transyslab.simcore.mesots;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.LinkTimes;
import com.transyslab.roadnetwork.Segment;

/**
 * @author its312
 *
 */
public class MesoSegment extends Segment {

	// friend MESO_Vehicle;
	// friend MESO_TrafficCell;
	private int nCells_; // num of TCs

	private MesoTrafficCell firstCell_; // first downstream traffic cell
	private MesoTrafficCell lastCell_; // last upstream traffic cell

	private double capacity_; // default capacity (vps)
	private double emitTime_; // time to move next vehicle

	private float density_; // current density
	private float speed_; // current speed

	private List<Float> densityList_;
	private List<Float> speedList_;
	private List<Integer> flowList_;

	public MesoSegment() {
		nCells_ = 0;
		firstCell_ = null;
		lastCell_ = null;
		densityList_ = new ArrayList<Float>();
		speedList_ = new ArrayList<Float>();
		flowList_ = new ArrayList<Integer>();
	}

	@Override
	public MesoSegment getUpSegment() {
		return (MesoSegment) super.getUpSegment();
	}
	public MesoSegment getDnStream() {
		return (MesoSegment) super.getDnSegment();
	}

	public int nVehicles() {
		int num = 0;
		MesoTrafficCell cell = firstCell_;
		while (cell != null) {
			num += cell.nVehicles();
			cell = cell.trailing();
		}
		return num;
	}
	public MesoTrafficCell getFirstCell() {
		return firstCell_;
	}
	public MesoTrafficCell getLastCell() {
		return lastCell_;
	}
	public MesoVehicle firstVehicle() {
		if (firstCell_ != null)
			return firstCell_.firstVehicle();
		else
			return null;
	}
	public MesoVehicle lastVehicle() {
		if (lastCell_ != null)
			return lastCell_.lastVehicle();
		else
			return null;
	}

	public MesoTrafficCell firstCell() {
		return firstCell_;
	}
	public MesoTrafficCell lastCell() {
		return lastCell_;
	}

	public void append(MesoTrafficCell cell) {
		cell.segment_ = this;

		cell.leading_ = lastCell_;
		cell.trailing_ = null;

		if (lastCell_ != null) { // append at end
			lastCell_.trailing_ = cell;
		}
		else { // queue is empty
			firstCell_ = cell;
		}
		lastCell_ = cell;
		nCells_++;
	}
	public void remove(MesoTrafficCell cell) {
		if (cell.leading_ != null) { // not the first one
			cell.leading_.trailing_ = cell.trailing_;
		}
		else { // first one
			firstCell_ = cell.trailing_;
		}
		if (cell.trailing_ != null) { // not the last one
			cell.trailing_.leading_ = cell.leading_;
		}
		else { // last one
			lastCell_ = cell.leading_;
		}
		nCells_--;
	}

	@Override
	public float density() {
		return density_ / MesoParameter.densityFactor();
	}
	@Override
	public float speed() {
		return speed_ / MesoParameter.speedFactor();
	}
	@Override
	public int flow() {
		return calcFlow();
	}

	/*
	 * ------------------------------------------------------------------
	 * Calculate the density of a segment, in vehicle/kilometer
	 * ------------------------------------------------------------------
	 */
	@Override
	public float calcDensity() {
		density_ = (float) (1000.0 * nVehicles() / (length_ * nLanes()));
		return (density_);// vehicle/km
	}
	public void calcState() {
		density_ = (float) (1000.0f * nVehicles() / (length_ * nLanes()));
		densityList_.add(density_);
		if (nVehicles() <= 0) {
			speedList_.add(maxSpeed());
		}
		else {
			float sum = 0.0f;
			MesoTrafficCell cell = firstCell_;
			MesoVehicle pv;
			while (cell != null) {
				pv = cell.firstVehicle();
				while (pv != null) {
					if (pv.currentSpeed() > Constants.SPEED_EPSILON) {
						sum += 1.0f / pv.currentSpeed();
					}
					else {
						sum += 1.0f / Constants.SPEED_EPSILON;
					}
					pv = pv.trailing();
				}
				cell = cell.trailing();
			}
			speed_ = nVehicles() / sum;
			speedList_.add(speed_ * 3.6f);
		}
		float x = 3.6f * speed_ * density_;
		flowList_.add(Math.round(x));// vehicle/hour

		// return (density_);//vehicle/km
	}

	@Override
	public float calcSpeed() {
		if (nVehicles() <= 0) {
			return (speed_ = maxSpeed());
		}
		float sum = 0.0f;
		MesoTrafficCell cell = firstCell_;
		MesoVehicle pv;
		while (cell != null) {
			pv = cell.firstVehicle();
			while (pv != null) {
				if (pv.currentSpeed() > Constants.SPEED_EPSILON) {
					sum += 1.0f / pv.currentSpeed();
				}
				else {
					sum += 1.0f / Constants.SPEED_EPSILON;
				}
				pv = pv.trailing();
			}
			cell = cell.trailing();
		}
		speed_ = nVehicles() / sum;
		return (speed_ * 3.6f);// km/hour
	}
	@Override
	public int calcFlow() {
		float x = 3.6f * speed_ * density_;
		return (int) (x + 0.5);// vehicle/hour
	}

	/*
	 * Calculate cell variables that do NOT depend on other cells. This
	 * functions is called by a function with the same name in class
	 * MESO_Network.
	 */
	public void calcTrafficCellUpSpeed() {
		// Calculate density and upSpeed for each traffic cell

		// cout << " XXXX Segment" ;
		MesoTrafficCell cell = firstCell_;
		while (cell != null) {
			cell.updateTailSpeed();
			cell = cell.trailing();
		}
	}

	/*
	 * Calculate cell variables that depend on other cells. This functions is
	 * called by a function with the same name in class MESO_Network.
	 */
	public void calcTrafficCellDnSpeeds() {
		MesoTrafficCell cell = firstCell_;
		while (cell != null) {
			cell.updateHeadSpeeds();
			cell = cell.trailing();
		}
	}

	public int isJammed() {
		if (lastCell_ == null) { // nobody in the segment
			return 0;
		}
		else {
			return lastCell_.isJammed();
		}
	}

	// Move vehicles based on their speeds

	public void advanceVehicles() {
		MesoTrafficCell cell = firstCell_;
		while (cell != null) {
			cell.advanceVehicles();
			cell = cell.trailing_;
		}
	}

	// Remove, merge and split traffic cells

	public void formatTrafficCells() {
		MesoTrafficCell cell;
		MesoTrafficCell front;

		// Remove the empty traffic cells

		cell = firstCell_;
		while ((front = cell) != null) {
			cell = cell.trailing_;
			if (front.nVehicles() <= 0 || front.firstVehicle() == null || front.lastVehicle() == null) { // no
																											// vehicle
																											// left

				// use vehicle count should be enough but it seems that there
				// is a bug somewhere that causes vehicle count to be 1 when
				// actually there is no vehicle left.

				remove(front);
				MesoCellList.getInstance().recycle(front);
			}
		}
	}

	// Append a vehicle at the end of the segment

	public void append(MesoVehicle vehicle) {
		if (lastCell_ == null || !lastCell_.isReachable()) {
			// isReachable T：cell尾车距segment末端的距离<车团分离阈值
			// F: cell尾车距segment末端的距离>=车团分离阈值
			// 若segment存在cell，新车与原lastcell距离大于分离阈值，则新生成lastcell
			append(MesoCellList.getInstance().recycle());
			lastCell_.initialize();
		}

		// Put the vehicle in the traffic cell

		lastCell_.append(vehicle);
	}
	// Insert a vehicle into the segment based on its distance
	// position
	/*
	 * public void insert(MESO_Vehicle vehicle) { if (lastCell_ == null) {
	 * append(MESO_CellList.getInstance().recycle()); lastCell_.initialize(); }
	 * lastCell_.insert(vehicle); }
	 */

	// Maximum speed for a given gap

	public float maxSpeed(float gap) {
		return MesoParameter.getInstance().maxSpeed(gap, nLanes_);
	}
	public float maxSpeed() {
		return freeSpeed_;
	}
	public void updateFreeSpeed(){
		freeSpeed_ = MesoNetwork.getInstance().getSdFn(sdIndex_).getFreeSpeed();
	}
	public float defaultCapacity() {
		float vph = MesoNetwork.getInstance().getSdFn(getSdIndex()).getCapacity();
		return nLanes_ * vph;
	}

	// Update number of vehicle allowed to move out.

	public void resetEmitTime() { // once every update step
		emitTime_ = SimulationClock.getInstance().getCurrentTime();
		scheduleNextEmitTime();
	}

	public void scheduleNextEmitTime() {
		if (capacity_ > 1.E-6) {
			emitTime_ += 1.0 / capacity_;
		}
		else {
			emitTime_ = Constants.DBL_INF;
		}
	}

	public int isMoveAllowed() {
		if (SimulationClock.getInstance().getCurrentTime() >= emitTime_)
			return 1;
		else
			return 0;
	}

	// Add cap to current capacity

	public void addCapacity(float cap_delta) {
		setCapacity((float) (cap_delta + capacity_));
	}

	// Set current capacity to cap

	public void setCapacity(float cap) {
		float maxcap = defaultCapacity();
		if (cap < 0.0) {
			capacity_ = 0.0;
		}
		else if (cap > maxcap) {
			capacity_ = maxcap;
		}
		else {
			capacity_ = cap;
		}
		resetEmitTime();
	}

	// Overload the base class function

	@Override
	public void calcStaticInfo() {
		// 重写部分开始
		if ((getDnStream() == null)) {
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
		MesoNetwork.getInstance().totalLinkLength_ += length_;
		MesoNetwork.getInstance().totalLaneLength_ += length_ * nLanes_;

		if (sdIndex_ < 0 || sdIndex_ >= MesoNetwork.getInstance().nSdFns()) {
			// cerr << "Segment " << code_ << " has invalid sdIndex "
			// << sdIndex_ << "." << endl;
			sdIndex_ = 0;
		}
		// 重写部分结束
		//此处赋值freeSpeed
		freeSpeed_ = MesoNetwork.getInstance().getSdFn(sdIndex_).getFreeSpeed();
		density_ = 0.0f;
		speed_ = maxSpeed();

		// Set to the default maximum capacity
		setCapacity(defaultCapacity());
	}

	// remove all traffic cells
	public void clean() {

		while (firstCell_ != null) {
			lastCell_ = firstCell_;
			firstCell_ = firstCell_.trailing_;
			MesoCellList.getInstance().recycle(lastCell_);
		}
		lastCell_ = null;
		nCells_ = 0;
	}
	@Override
	public void outputToOracle(PreparedStatement ps) throws SQLException {
		int num = flowList_.size();
		for (int i = 0; i < num; i++) {
			Date date = LinkTimes.getInstance().toDate((i + 1));
			// simtaskid 写死，注意更改
			ps.setInt(1, 5);
			ps.setInt(2, getCode());
			ps.setDate(3, new java.sql.Date(date.getTime()));
			ps.setTimestamp(3, new java.sql.Timestamp(date.getTime()));
			ps.setInt(4, nLanes());
			ps.setInt(5, flowList_.get(i));
			ps.setFloat(6, speedList_.get(i));
			ps.setFloat(7, densityList_.get(i));
			ps.addBatch();
		}
		ps.executeBatch();
	}
	@Override
	public void outputVhcPosition() throws IOException {
		StringBuilder sb = new StringBuilder();
		int frameid = (int) Math
				.round((SimulationClock.getInstance().getCurrentTime() - SimulationClock.getInstance().getStartTime())
						/ SimulationClock.getInstance().getStepSize());
		double s, l, vx, vy;
		MesoTrafficCell cell = firstCell_;
		while (cell != null) {
			MesoVehicle vehicle = cell.firstVehicle_;
			while (vehicle != null) {
				l = getLength();
				s = l - vehicle.distance();
				vx = startPnt_.getLocationX() + s * (endPnt_.getLocationX() - startPnt_.getLocationX()) / l;
				vy = startPnt_.getLocationY() + s * (endPnt_.getLocationY() - startPnt_.getLocationY()) / l;
				sb.append(frameid).append(",");
				sb.append(getCode()).append(",");
				sb.append(vehicle.getCode()).append(",");
				sb.append(vx).append(",");
				sb.append(vy).append("\n");
				vehicle = vehicle.trailing_;
			}
			cell = cell.trailing_;
		}
		String filepath = "E:\\OutputPosition.txt";
		FileOutputStream out = new FileOutputStream(filepath, true);
		OutputStreamWriter osw = new OutputStreamWriter(out, "utf-8");
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(sb.toString());
		bw.close();

	}
}
