/**
 *
 */
package com.transyslab.roadnetwork;
import java.util.*;

import com.transyslab.commons.tools.SimulationClock;

/**
 * ODTable外部文件
 *
 * @author YYL 2016-6-6
 */
public class ODTable {

	protected double nextTime_; // time to read od pairs
	protected int type_; // vehicle type
	protected double scale_; // scaling factor
	protected List<ODCell> cells_; // list of OD cells

	public static String name_/* = strdup("od.dat") */; // file name

	public ODTable() {
		nextTime_ = 0;
	}
	public static ODTable getInstance() {
		HashMap<String, Integer> hm = RoadNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return RoadNetworkPool.getInstance().getODTable(threadid);
	}
	// Open trip table file and create a OD Parser
	// public void open(const char *fn = 0);
	// public double read();

	public static String getName() {
		return name_;
	}
	// static inline char** nameptr() { return &name_; }

	public double scale() {
		return scale_;
	}
	public double getNextTime() {
		return nextTime_;
	}
	public void setNextTime(double t) {
		nextTime_ = t;
	}
	public List<ODCell> getCells() {
		return cells_;
	}
	public int nCells() {
		return cells_.size();
	}
	public int getType() {
		return type_;
	}
	// Called by parser to setup the od matrix and update time for next
	// vehicles departure.
	public int init(double s, int t, double f) {
		if (nextTime_ > -86400.0 /* && ToolKit::verbose() */) {
			/*
			 * cout << nCellsParsed_ << " OD cells (type " << type_ <<
			 * ") parsed at " << theSimulationClock->convertTime(nextTime_) <<
			 * "." << endl;
			 */
		}
		// 按时段读入od数据
		// 清空上一时段遗留的odcell
		if (cells_ != null)
			cells_ = null;
		cells_ = new ArrayList<ODCell>();
		nCellsParsed_ = 0;
		nextTime_ = s;
		type_ = t;
		scale_ = f;
		return 0;
	}
	public int init(double start) {
		return init(start, type_, scale_);
	}
	// Read OD table and return next updating time.
	public void insert(ODCell cell) {
		/*
		 * 遍历，查看是否有重复cell OD_Cell matched =null; OD_Cell tempcell;
		 * ListIterator<OD_Cell> i = cells_.listIterator(); while(i.hasNext()){
		 * tempcell = i.next(); if(tempcell.eq(cell)){ matched = tempcell ; } }
		 * if(cells_.size()!=0){ if (matched != cells_.get(cells_.size()-1)) {
		 * cells_.remove(matched); matched = null; } }
		 */
		if (cell.rate() > Constants.RATE_EPSILON) {
			//
			cells_.add(cell);
		}
	}

	// These may need to be overloaded by derived class

	public ODCell newOD_Cell() {
		return null;
	}
	// Emit vehicles until no more vehicle wants to departure at this
	// time. When vehicles are created, the corresponding OD cell is
	// dropped down in the list based on the departure time for next
	// vehicle.
	public void emitVehicles() {
		// List<OD_Cell> */i;
		ODCell i;
		ODCell c;

		// 必须按cell.nextTime_从小到大排序，每次都从nextTime最小的cell开始发车
		// 若大的在前会导致nextTime>currentTime，符合条件的小的cell无法发车
		// 调用了emitVehicle后，cell.nextTime会更新，需要重新排序
		//
		while ((i = cells_.get(0)) != null && (c = i) != null
				&& c.nextTime() <= SimulationClock.getInstance().getCurrentTime()) {
			cells_.remove(i);
			c.emitVehicles();
			//
			cells_.add(i);
			sortODCell();
		}
	}
	public void sortODCell() {
		Comparator<ODCell> comparator = new Comparator<ODCell>() {
			@Override
			public int compare(ODCell c1, ODCell c2) {
				// 按nextTime_排序
				return (c1.nextTime() < c2.nextTime() ? -1 : (c1.nextTime() == c2.nextTime() ? 0 : 1));
			}
		};
		Collections.sort(cells_, comparator);
	}
	// public void clean();

	// private OD_Parser parser_; // the OD table parser
	public int nCellsParsed_; // od cells parsed
}
