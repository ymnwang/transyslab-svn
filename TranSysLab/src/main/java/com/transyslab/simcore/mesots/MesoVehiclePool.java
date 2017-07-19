/**
 *
 */
package com.transyslab.simcore.mesots;

/**
 * @author its312
 *
 */
public class MesoVehiclePool {

	private MesoVehicle head;
	private MesoVehicle tail;
	private int nVehicles; /* number of vehicles in the pool*/
	private int nPeakVehicles; // max number of vehicles,包括未进入路网的车辆，用于车辆编号


	public MesoVehiclePool() {
		nVehicles = 0;
		nPeakVehicles = 0;
	}

	public void recycle(MesoVehicle pv) /* put a vehicle into the list */
	{
		pv.donePathIndex();
		// pv.countFlags_ = true;
		pv.sensorIDFlag = -100000;
		pv.trafficCell = null;
		pv.needRecycle = false;
		pv.trailing = null;
		pv.leading = tail;

		if (tail == null) { // no vehicle in the list
			tail = new MesoVehicle();
			tail.trailing = pv;
		}
		else { // at least one in the list
			head = pv;
		}
		tail = pv;
		nVehicles++;

	}

	public MesoVehicle recycle() /* get a vehicle from the list */
	{
		MesoVehicle pv;

		if (head != null) { // get head from the list
			pv = head;
			if (tail == head) { // the onle one vehicle in list
				head = tail = null;
			}
			else { // at least two vehicles in list
				head = head.trailing;
				head.leading = null;
			}
			nVehicles--;
		}
		else { // list is empty
			pv = new MesoVehicle();
		}
		pv.setId(nPeakVehicles);
		nPeakVehicles++;
		return (pv);
	}

	public MesoVehicle head() {
		return head;
	}
	public MesoVehicle tail() {
		return tail;
	}
	public int nVehicles() {
		return nVehicles;
	}
	public int nPeakVehicles() {
		return nPeakVehicles;
	}

}
