/**
 *
 */
package com.transyslab.simcore.mesots;

import com.transyslab.roadnetwork.Link;
import com.transyslab.roadnetwork.Segment;

/**
 * @author its312
 *
 */
public class MesoLink extends Link {
	// friend class MESO_Network
	protected MesoVehicle queueHead_ = new MesoVehicle(); // first vehicle in
															// the queue
	protected MesoVehicle queueTail_ = new MesoVehicle(); // last vehicle in the
															// queue
	protected int queueLength_; // number of vehicle in the queue

	public MesoLink() {
		queueHead_ = null;
		queueTail_ = null;
		queueLength_ = 0;
	}

	public MesoVehicle queueHead() {
		return queueHead_;
	}
	public MesoVehicle queueTail() {
		return queueTail_;
	}
	public int queueLength() {
		return queueLength_;
	}

	/*
	 * Returns the 1st traffic cell in this link
	 */
	public MesoTrafficCell firstCell() {
		return ((MesoSegment) getEndSegment()).firstCell();
	}
	/*
	 * Returns the last traffic cell in this link
	 */
	public MesoTrafficCell lastCell() {
		return ((MesoSegment) getStartSegment()).lastCell();
	}

	/*
	 * Print the number of vehicles queue for enter a link
	 */
	/*
	 * Î´´¦Àí public int MESO_Link::reportQueueLength(ostream &os) { const int
	 * MinQueueStepSize = 20; if (queueLength_ > MinQueueStepSize) { os << " ("
	 * << code_ << ":" << queueLength_ << ")"; return 1; } return 0; }
	 */

	// These two maintains the virtual queue before entering the
	// network

	public void dequeue(MesoVehicle pv) {
		if (pv.leading() != null) {
			pv.leading().trailing(pv.trailing());
		}
		else { /* first vehicle in the queue */
			queueHead_ = pv.trailing();
		}
		if (pv.trailing() != null) {
			pv.trailing().leading(pv.leading());
		}
		else { /* last vehicle in the queue */
			queueTail_ = pv.leading();
		}
		queueLength_--;
		// theStatus.nInQueue(-1);
	}

	public void queue(MesoVehicle vehicle) {
		if (queueTail_ != null) { /* current queue is not empty */
			queueTail_.trailing(vehicle);
			vehicle.leading(queueTail_);
			queueTail_ = vehicle;
		}
		else { /* current queue is empty */
			vehicle.leading(null);
			queueHead_ = queueTail_ = vehicle;
		}
		vehicle.trailing(null);
		queueLength_++;
		// theStatus.nInQueue(1);
	}

	public void prequeue(MesoVehicle vehicle) {
		if (queueHead_ != null) { /* current queue is not empty */
			queueHead_.leading(vehicle);
			vehicle.trailing(queueHead_);
			queueHead_ = vehicle;
		}
		else { /* current queue is empty */
			vehicle.trailing(null);
			queueHead_ = queueTail_ = vehicle;
		}
		vehicle.leading(null);
		queueLength_++;
		// theStatus.nInQueue(1);
	}

	// These are used in moving vehicles
	/*
	 * Move vehicles based on current cell speeds. This function is called by
	 * MESO_Node::advanceVehicles() in random permuted order.
	 */
	public void advanceVehicles() {
		MesoSegment ps = (MesoSegment) getEndSegment();
		while (ps != null) {
			ps.advanceVehicles();
			ps.formatTrafficCells();
			ps = ps.getUpSegment();
		}
	}
	/*
	 * Add a vehicle at the upstream end of the link.
	 */
	public void append(MesoVehicle vehicle) {
		MesoSegment ps = (MesoSegment) getStartSegment();
		ps.append(vehicle);
	}

	public void checkConnectivity() {
	}

	public int isJammed() {
		return ((MesoSegment) getStartSegment()).isJammed();
	}

	public void clean() {
		// Remove vehicles in pretrip queue

		while (queueHead_ != null) {
			queueTail_ = queueHead_;
			queueHead_ = queueHead_.trailing_;
			MesoVehicleList.getInstance().recycle(queueTail_);
		}
		queueTail_ = null;
		queueLength_ = 0;

		// Remove vehicles in each segment

		Segment ps = getStartSegment();
		while (ps != null) {
			((MesoSegment) ps).clean();
			ps = ps.getDnSegment();
		}
	}

	@Override
	public float calcTravelTime() // virtual
	{
		MesoSegment ps = (MesoSegment) getStartSegment();
		MesoTrafficCell pc = new MesoTrafficCell();
		MesoVehicle pv = new MesoVehicle();
		double sum = 0.0;
		int cnt = 0;
		float tt = (float) travelTime();

		// Vehicles already on the link

		while (ps != null) {
			pc = firstCell();
			while (pc != null) {
				pv = pc.firstVehicle();
				while (pv != null) {
					float pos = pv.distanceFromDownNode();
					sum += pv.timeInLink() + pos / length() * tt;
					cnt++;
					pv = pv.trailing();
				}
				pc = pc.trailing();
			}
			ps = ps.getDnStream();
		}

		// Vehicles in pretrip queue

		pv = queueHead_;
		while (pv != null) {
			sum += pv.timeInLink() + tt * 1.25;
			cnt++;
			pv = pv.trailing();
		}

		if (cnt != 0) {
			return (float) (sum / cnt);
		}
		else {
			return tt;
		}
	}
}
