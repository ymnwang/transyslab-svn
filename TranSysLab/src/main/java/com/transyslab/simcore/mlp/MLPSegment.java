package com.transyslab.simcore.mlp;

import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.Segment;

public class MLPSegment extends Segment{
	
	
	@Override
	public MLPSegment getUpSegment() {
		return (MLPSegment) super.getUpSegment();
	}
	
	/*public void calcState() {
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
*/
}
