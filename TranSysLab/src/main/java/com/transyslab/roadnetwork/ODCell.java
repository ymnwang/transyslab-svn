/**
 *
 */
package com.transyslab.roadnetwork;
import java.util.Vector;

import com.transyslab.commons.tools.Random;
import com.transyslab.commons.tools.SimulationClock;

/**
 * ODCell
 *
 * @author YYL 2016-6-6
 */
public class ODCell {

	protected ODPair od_; // od pair

	protected Node oriNode_;
	protected Node desNode_;

	protected double headway_; // average headway (second)
	protected double nextTime_; // next departure time
	protected int type_; // vehicle type
	protected float randomness_; // 0=uniform 1=random

	protected int busTypeBRT_; // Dan: bus type for bus rapid transit assignment
	protected int runIDBRT_; // Dan: assigned bus run id for bus rapid transit

	protected Vector<Path> paths_; // an array of pointers to paths
	protected float[] splits_; // probabilities to choose each path

	protected static int nSplits_; // num of splits parsed so far for current
									// cell

	public ODCell() {
		nextTime_ = 0;
		splits_ = null;
	}
	public int cmp(ODCell c) {
		// for sorting
		final double epsilon = 1.0e-5;
		if (nextTime_ + epsilon < c.nextTime_)
			return -1;
		else if (nextTime_ > c.nextTime_ + epsilon)
			return 1;
		else
			return 0;
	}
	public int cmp(int c) {
		// for sorting
		int code = (getOriNode().getCode() << 16) | (getDesNode().getCode());
		if (code < c)
			return -1;
		else if (code > c)
			return 1;
		else
			return 0;
	}
	public boolean eq(ODCell c) {
		// for identification
		return (c.type_ == type_ && c.getOriNode() == getOriNode() && c.getDesNode() == getDesNode());
	}
	/*
	 * public bool operator ==(OD_Cell* cell) { return eq(cell); }
	 */

	public Node getOriNode() {
		return od_.getOriNode();
	}
	public Node getDesNode() {
		return od_.getDesNode();
	}
	public double rate() {
		return 3600.0 / headway_;
	}
	public double nextTime() {
		return nextTime_;
	}
	public int type() {
		return type_;
	}

	public int busTypeBRT() {
		return busTypeBRT_;
	}
	public int runIDBRT() {
		return runIDBRT_;
	}

	public int nPaths() {
		return paths_.size();
	}
	public Path path(int i) {
		if (i < 0)
			return null;
		else if (i >= paths_.size())
			return null;
		else
			return paths_.get(i);
	}

	public int addPath(int path_id) {
		int error = 0;
		if (paths_ == null)
			paths_ = new Vector<Path>();
		if (PathTable.getInstance() != null) {
			Path p = PathTable.getInstance().findPath(path_id);
			if (p == null) {
				// cerr << "Warning:: Unknown path. ";
				error = 1;
			}
			else if (p.getOriNode() != getOriNode() || p.getDesNode() != getDesNode()) {
				/*
				 * cerr << "Warning:: Path <" << path_id <<
				 * " does not connect OD pair (" << oriNode()->code() << "," <<
				 * desNode()->code() << "). ";
				 */
				error = 2;
			}
			else {
				paths_.add(p);
			}
		}
		else {
			// cerr << "Warning:: No path table. ";
			error = 3;
		}
		return error;
	}

	public int setSplit(float split) {
		int n = nPaths();
		if (nSplits_ >= n) {
			return -1; // too many splits
		}
		if (splits_ == null) {
			splits_ = new float[n];
		}
		else {
			split += splits_[nSplits_ - 1];
		}

		if (split < 0.0)
			return -2;
		if (split > 1.0)
			return -3;

		splits_[nSplits_] = split;
		nSplits_++;

		return 0;
	}
	public static int nSplits() {
		return nSplits_;
	}
	public float split(int i) {
		return splits_[i];
	}
	public float[] splits() {
		return splits_;
	}
	// This function is called by OD_Parser. Return 0 if sucess or -1 if
	// fail
	public int init(int ori, int des, double rate, double var, float r) {
		// OD_Cell* OD_Cell::workingCell_ = NULL;
		workingCell_ = this;

		if (splits_ != null)
			splits_ = null;
		splits_ = null;
		nSplits_ = 0;

		int error = 0;

		Node o = RoadNetwork.getInstance().findNode(ori);
		Node d = RoadNetwork.getInstance().findNode(des);

		if (o == null) {
			// cerr << "Error:: Unknown origin node <" << ori << ">. ";
			return (-1);
		}
		else if (d == null) {
			// cerr << "Error:: Unknown destination node <" << des << ">. ";
			return (-1);
		}
		else if (d.getDestIndex() == -1) {
			// cerr << "Error:: Node <" << des
			// << "> is not a destination node in this network.";
			return (-1);
		}
		// theODPairs 是od对的list，这里不将od对存进list
		// 原来把odpair存进list是为了查询是否已存在od对，存在则不用新生成，不存在则要生成，减少内存占用
		od_ = new ODPair(o, d);
		// OD_Pair odpair = new OD_Pair(o, d);
		/*
		 * // PtrOD_Pair odptr(&odpair); OdPairSetType::iterator i =
		 * theOdPairs.find(odptr); if (i == theOdPairs.end()) { // not found od_
		 * = new OD_Pair(odpair); theOdPairs.insert(od_); } else { // found od_
		 * = (*i).p(); }
		 */

		od_.getOriNode().type_ |= Constants.NODE_TYPE_ORI;
		od_.getDesNode().type_ |= Constants.NODE_TYPE_DES;

		// Departure rate, assume a normal distribution

		rate *= ODTable.getInstance().scale();
		if (var > 1.0E-4) {
			var *= ODTable.getInstance().scale();
			rate = Random.getInstance().get(Random.Departure).nrandom(rate, var);
		}
		randomness_ = r;

		if (rate >= Constants.RATE_EPSILON) {
			headway_ = 3600.0 / rate;
			// double test = ((Random)
			// Random.getInstance().get(Random.Departure)).urandom();
			nextTime_ = SimulationClock.getInstance().getCurrentTime()
					- Math.log(Random.getInstance().get(Random.Departure).urandom()) * headway_;

		}
		else {
			headway_ = Constants.DBL_INF;
			nextTime_ = Constants.DBL_INF;
		}
		type_ = ODTable.getInstance().getType();
		ODTable.getInstance().insert(this);

		/*
		 * if (ToolKit::debug()) { print(); }
		 */

		ODTable.getInstance().nCellsParsed_++;

		return error;
		// return 1;
	}
	/*
	 * public int initBRT(int ori, int des, double rate, double var, float r,
	 * int bt, int rid){ workingCell_ = this;
	 *
	 * if (splits_) delete [] splits_ ; splits_ = 0 ; nSplits_ = 0 ;
	 *
	 * int error = 0;
	 *
	 * RN_Node *o = theNetwork->findNode(ori); RN_Node *d =
	 * theNetwork->findNode(des);
	 *
	 * if (!o) { cerr << "Error:: Unknown origin node <" << ori << ">. "; return
	 * (-1); } else if (!d) { cerr << "Error:: Unknown destination node <" <<
	 * des << ">. "; return (-1); } else if( d->destIndex()==-1 ) { cerr <<
	 * "Error:: Node <" << des << "> is not a destination node in this network."
	 * ; return (-1); }
	 *
	 * OD_Pair odpair(o, d); PtrOD_Pair odptr(&odpair); OdPairSetType::iterator
	 * i = theOdPairs.find(odptr); if (i == theOdPairs.end()) { // not found od_
	 * = new OD_Pair(odpair); theOdPairs.insert(od_); } else { // found od_ =
	 * (*i).p(); }
	 *
	 * od_->oriNode()->type_ |= NODE_TYPE_ORI; od_->desNode()->type_ |=
	 * NODE_TYPE_DES;
	 *
	 * // Departure rate, assume a normal distribution
	 *
	 * rate *= theODTable->scale(); if (var > 1.0E-4) { var *=
	 * theODTable->scale(); rate =
	 * theRandomizers[Random::Departure]->nrandom(rate, var); } randomness_ = r;
	 *
	 * busTypeBRT_ = bt; runIDBRT_ = rid;
	 *
	 * if (rate >= RATE_EPSILON) { headway_ = 3600.0 / rate; nextTime_ =
	 * theSimulationClock->currentTime() -
	 * log(theRandomizers[Random::Departure]->urandom()) * headway_; } else {
	 * headway_ = DBL_INF; nextTime_ = DBL_INF; }
	 *
	 * type_ = theODTable->type(); theODTable->insert(this);
	 *
	 * if (ToolKit::debug()) { print(); }
	 *
	 * theODTable->nCellsParsed_ ++;
	 *
	 * return error; }
	 */

	// public void print(ostream &os = cout);
	// Calculate the inter departure time for the next vehicle
	public double randomizedHeadway() {
		if (randomness_ < 1.0e-10 || (Random.getInstance().get(Random.Departure).brandom(randomness_)) == 0) {
			// uniform distribution
			return (headway_);
		}
		else { // random distribution
			return (-Math.log(Random.getInstance().get(Random.Departure).urandom()) * headway_);
		}
	}
	// 未处理
	// public RN_Vehicle* newVehicle() = 0;
	public Vehicle newVehicle() {
		return null;
	}

	// Application program should overload these functions

	public void emitVehicles() {
		Vehicle pv;
		while ((pv = superEmitVehicle()) != null) {
			/*
			 * cout << "Vehicle " << pv->code() << " created for OD pair (" <<
			 * oriNode()->code() << "," << desNode()->code() << ")." << endl;
			 */
			pv = null;
		}
	}

	// Returns a created vehicle if it is time for a vehicle to depart or
	// NULL if no vehicle needs to depart at this time. It also update the
	// time that next vehicle departs if a vehicle is created.

	public Vehicle superEmitVehicle() {
		if (nextTime_ <= SimulationClock.getInstance().getCurrentTime()) {
			Vehicle pv = newVehicle();

			if ((type() & Constants.VEHICLE_BUS_RAPID) != 0) {
				// pv.initRapidBus(type_, od_, runIDBRT_, busTypeBRT_,
				// headway_);
			}
			else
				pv.init(0, type_, od_, null);

			// Find the first link to travel. Variable 'oriNode', 'desNode' and
			// 'type' must have valid values before the route choice model is
			// called.

			if ((type() & Constants.VEHICLE_BUS_RAPID) != 0) {
				pv.PretripChoosePath();
			}
			else
				pv.PretripChoosePath(this);

			/*
			 * if (SimulationEngine.chosenOutput(DefinedConstant.
			 * OUTPUT_VEHICLE_DEP_RECORDS)!=0) { pv.saveDepartureRecord(); }
			 */

			nextTime_ += randomizedHeadway();
			return pv;
		}
		return null;
	}
	/*
	 * // Return a route if path table and splits are specified public RN_Path
	 * chooseRoute(RN_Vehicle pv){ int i =
	 * theRandomizers[Random::Routing]->drandom(nPaths(), splits_); return
	 * path(i) ; }
	 */

	// Used when parsing the od table
	// Return a route if path table and splits are specified

	Path chooseRoute(Vehicle pv) {
		int i = (Random.getInstance().get(2)).drandom(nPaths(), splits_);
		return path(i);
	}
	public ODCell workingCell() {
		return workingCell_;
	}

	private ODCell workingCell_;
}
