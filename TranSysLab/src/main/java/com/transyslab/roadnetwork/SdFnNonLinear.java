/**
 *
 */
package com.transyslab.roadnetwork;

/**
 * @author YYL 2016-6-6
 */
public class SdFnNonLinear extends SdFn {
	// Non Linear Speed-Density Function
	private float speedBeta_; // parameters in nonlinear sd functions
	private float densityBeta_;


	public SdFnNonLinear() {
		speedBeta_ = 5;
		densityBeta_ = 1.8f;
	}
	public SdFnNonLinear(float cap, float spd, float kjam, float alpha, float beta) {
		super(cap, spd, kjam);
		densityBeta_ = alpha;
		speedBeta_ = beta;
	}
	public SdFnNonLinear(float alpha, float beta) {
		densityBeta_ = alpha;
		speedBeta_ = beta;
	}
	public void updateParameters(float cap, float minspeed, float maxspeed, float maxdensity, float a, float b){
		capacity_ = cap;
		jamSpeed_ = minspeed;
		freeSpeed_  = maxspeed;
		jamDensity_ = maxdensity;
		densityBeta_ = a;
		speedBeta_ = b;
	}
	@Override
	public float densityToSpeed(/*float free_speed,*/ float density, int nlanes) {
		if (density < 1.0) {
			return freeSpeed_;
		}
		else if (density + 1.0 > jamDensity_) {
			return jamSpeed_ * (nlanes - 1);
		}
		else {
			float y = density / jamDensity_;
			float k = (float) Math.pow(y, densityBeta_);
			float r = (float) Math.pow(1.0 - k, speedBeta_);
			float v0 = jamSpeed_ * (nlanes - 1);
			// cout << " r " << r <<" free speed" << free_speed <<" jam speed "
			// << jamSpeed_ ;
			return v0 + r * (freeSpeed_ - v0);
		}
	}
}
