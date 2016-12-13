/**
 *
 */
package com.transyslab.roadnetwork;

//import MESO.MESO_Parameter;

/**
 *
 * @author YYL 2016-6-6
 */
public class SdFn {
	protected float capacity_;
	protected float jamSpeed_;
	protected float jamDensity_;

	public float getJamDensity() {
		return jamDensity_;
	}
	public float getCapacity() {
		return capacity_;
	}

	// public void print(ostream &os = cout);

	public float densityToSpeed(float freeSpeed, float density, int nlanes) {
		return freeSpeed;
	}

	protected SdFn() {
		capacity_ = 0.5f; // 1800 vph in vps
		jamSpeed_ = 0; // 5 mph in mps
		jamDensity_ = 180.0f; // in vehicles/km
	}
	protected SdFn(float cap, float spd, float kjam) {
		// capacity_ = (float) (cap / 3600.0);
		capacity_ = cap;
		jamSpeed_ = spd;// * Parameter.speedFactor();
		jamDensity_ = kjam;// * Parameter.densityFactor();
	}
	public void setCapacity(float c) {
		capacity_ = c;
	}

}
