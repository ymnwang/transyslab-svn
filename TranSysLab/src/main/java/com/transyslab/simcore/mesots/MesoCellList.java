/**
 *
 */
package com.transyslab.simcore.mesots;

/**
 * @author its312
 *
 */
public class MesoCellList {

	private MesoTrafficCell head;
	private MesoTrafficCell tail;
	private MesoVehiclePool recycleVhcList;
	private int nCells; /* number of vehicles */
	private int nPeakCells; /* max number of vehicles */
	private double simStepSize; // 初始化Cell全局变量

	public MesoCellList(MesoVehiclePool recycleVhcList) {
		nCells = 0;
		nPeakCells = 0;
		this.recycleVhcList = recycleVhcList;
	}

	public void recycle(MesoTrafficCell cell) /* put a vehicle into the list */
	{
		//cell.clean();
		while (cell.firstVehicle != null) {
			cell.lastVehicle = cell.firstVehicle;
			cell.firstVehicle = cell.firstVehicle.trailing();
			//cell.lastVehicle.needRecycle = true;
			this.recycleVhcList.recycle(cell.lastVehicle);
		}
		cell.lastVehicle = null;
		cell.nVehicles = 0;

		cell.segment = null;

		if (cell.headSpeeds != null) {
			cell.headSpeeds = null;
		}
		if (cell.headPositions != null) {
			cell.headPositions = null;
		}
		cell.nHeads = 0;

		cell.trailing = head;
		if (head != null) { // at least one in the list
			head.leading = cell;
		}
		else { // no cell in the list
			tail = cell;
		}
		head = cell;
		nCells++; // one cell deposited in this list
		// theStatus.nCells(-1); // one cell become inactive
	}

	public MesoTrafficCell recycle() /* get a vehicle from the list */
	{
		MesoTrafficCell cell;

		if (head != null) { // get head from the list
			cell = head;
			if (tail == head) { // the only one cell in list
				head = tail = null;
			}
			else { // at least two cells in list
				head = head.trailing;
				head.leading = null;
			}
			nCells--;
		}
		else { // list is empty
			cell = new MesoTrafficCell(); // create a new cell
			nPeakCells++;
		}

		// theStatus.nCells(1); // one cell become active
		return cell;
	}

	public MesoTrafficCell head() {
		return head;
	}

	public MesoTrafficCell tail() {
		return tail;
	}

	public int nCells() {
		return nCells;
	}

	public int nPeakCells() {
		return nPeakCells;
	}

}
