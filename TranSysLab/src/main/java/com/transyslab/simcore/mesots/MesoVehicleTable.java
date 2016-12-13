/**
 *
 */
package com.transyslab.simcore.mesots;

import com.transyslab.roadnetwork.Vehicle;
import com.transyslab.roadnetwork.VehicleTable;

/**
 * @author its312
 *
 */
public class MesoVehicleTable extends VehicleTable {

	public MesoVehicleTable() {
	}

	public Vehicle newVehicle() {
		return MesoVehicleList.getInstance().recycle();
	}

}
