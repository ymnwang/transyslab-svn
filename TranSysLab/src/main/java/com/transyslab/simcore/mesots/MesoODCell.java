/**
 *
 */
package com.transyslab.simcore.mesots;

import com.transyslab.roadnetwork.ODCell;
import com.transyslab.roadnetwork.Vehicle;

/**
 * @author its312
 *
 */
public class MesoODCell extends ODCell {

	public MesoODCell() {
	}

	@Override
	public void emitVehicles() {
		MesoVehicle pv;
		while ((pv = (MesoVehicle) superEmitVehicle()) != null) {
			pv.enterPretripQueue();
		}
	}
	@Override
	public Vehicle newVehicle() {
		return MesoVehicleList.getInstance().recycle();
	}

}
