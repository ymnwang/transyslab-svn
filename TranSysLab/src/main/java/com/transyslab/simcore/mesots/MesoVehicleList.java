/**
 *
 */
package com.transyslab.simcore.mesots;

import java.util.HashMap;

/**
 * @author its312
 *
 */
public class MesoVehicleList {

	private MesoVehicle head_ ;
	private MesoVehicle tail_ ;
	private int nVehicles_; /* number of vehicles */
	private int nPeakVehicles_; /* max number of vehicles */

	public MesoVehicleList() {
		head_ = null;
		tail_ = null;
		nVehicles_ = 0;
		nPeakVehicles_ = 0;
	}
	public static MesoVehicleList getInstance() {
		HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return MesoNetworkPool.getInstance().getVhcList(threadid);
	}
	public void recycle(MesoVehicle pv) /* put a vehicle into the list */
	{
		pv.donePathIndex();
		// pv.countFlags_ = true;
		pv.SensorIDFlag_ = -100000;
		pv.trafficCell_ = null;

		pv.trailing_ = null;
		pv.leading_ = tail_;

		if (tail_ == null) { // no vehicle in the list
			tail_ = new MesoVehicle();
			tail_.trailing_ = pv;
		}
		else { // at least one in the list
			head_ = pv;
		}
		tail_ = pv;
		nVehicles_++;
	}

	public MesoVehicle recycle() /* get a vehicle from the list */
	{
		MesoVehicle pv;

		if (head_ != null) { // get head from the list
			pv = head_;
			if (tail_ == head_) { // the onle one vehicle in list
				head_ = tail_ = null;
			}
			else { // at least two vehicles in list
				head_ = head_.trailing_;
				head_.leading_ = null;
			}
			nVehicles_--;
		}
		else { // list is empty

			pv = new MesoVehicle();
			nPeakVehicles_++;
		}
		return (pv);
	}

	public MesoVehicle head() {
		return head_;
	}
	public MesoVehicle tail() {
		return tail_;
	}
	public int nVehicles() {
		return nVehicles_;
	}
	public int nPeakVehicles() {
		return nPeakVehicles_;
	}

}
