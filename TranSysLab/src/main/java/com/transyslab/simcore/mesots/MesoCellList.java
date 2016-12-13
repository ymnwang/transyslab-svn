/**
 *
 */
package com.transyslab.simcore.mesots;

import java.util.HashMap;

/**
 * @author its312
 *
 */
public class MesoCellList {

	private MesoTrafficCell head_;
	private MesoTrafficCell tail_;
	private int nCells_; /* number of vehicles */
	private int nPeakCells_; /* max number of vehicles */

	public MesoCellList() {
		head_ = null;
		tail_ = null;
		nCells_ = 0;
		nPeakCells_ = 0;
	}
	public static MesoCellList getInstance() {
		HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return MesoNetworkPool.getInstance().getCellList(threadid);
	}
	public void recycle(MesoTrafficCell cell) /* put a vehicle into the list */
	{
		cell.clean();
		cell.trailing_ = head_;
		if (head_ != null) { // at least one in the list
			head_.leading_ = cell;
		}
		else { // no cell in the list
			tail_ = cell;
		}
		head_ = cell;
		nCells_++; // one cell deposited in this list
		// theStatus.nCells(-1); // one cell become inactive
	}

	public MesoTrafficCell recycle() /* get a vehicle from the list */
	{
		MesoTrafficCell cell;

		if (head_ != null) { // get head from the list
			cell = head_;
			if (tail_ == head_) { // the only one cell in list
				head_ = tail_ = null;
			}
			else { // at least two cells in list
				head_ = head_.trailing_;
				head_.leading_ = null;
			}
			nCells_--;
		}
		else { // list is empty
			cell = new MesoTrafficCell(); // create a new cell
			nPeakCells_++;
		}

		// theStatus.nCells(1); // one cell become active
		return cell;
	}

	public MesoTrafficCell head() {
		return head_;
	}

	public MesoTrafficCell tail() {
		return tail_;
	}

	public int nCells() {
		return nCells_;
	}

	public int nPeakCells() {
		return nPeakCells_;
	}

	// Œ¥¥¶¿Ìextern MESO_CellList *theCellList; /* recyclable vehicles */

}
