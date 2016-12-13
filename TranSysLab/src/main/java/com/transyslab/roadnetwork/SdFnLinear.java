/**
 *
 */
package com.transyslab.roadnetwork;

/**
 * @author YYL 2016-6-6
 */
// Linear Speed-Density Function
public class SdFnLinear extends SdFn {

	private float kl_, vh_;
	private float kh_, vl_;
	private float delta_l_, delta_m_, delta_h_;

	public SdFnLinear() {
		kl_ = 0.35f;
		vh_ = 0.76f;
		kh_ = 0.56f;
		vl_ = 0.20f;
	}
	public SdFnLinear(float cap, float spd, float kjam, float kl, float vh, float kh, float vl) {
		super(cap, spd, kjam);
		kl_ = kl;
		vh_ = vh;
		kh_ = kh;
		vl_ = vl;
		initialize();
	}

	// void print(ostream &os = cout);
	@Override
	public float densityToSpeed(float free_speed, float density, int nlanes) {
		if (density < 1.0) {
			return free_speed;
		}
		else if (density + 1.0 > jamDensity_) {
			return jamSpeed_ * (nlanes - 1);
		}
		else {
			float y = density / jamDensity_;
			float v0 = jamSpeed_ * (nlanes - 1);
			float r;
			if (y < kl_) {
				r = (float) (1.0 - delta_l_ * y);
			}
			else if (y > kh_) {
				r = vh_ - delta_m_ * (y - kl_);
			}
			else {
				r = vl_ - delta_h_ * (y - kh_);
			}
			// cout << " r " << r <<" free speed" << free_speed <<" jam speed "
			// << jamSpeed_ << "Jam Density " << jamDensity_<< "Capacity " <<
			// capacity_;
			return v0 + r * (free_speed - v0);
		}
	}

	private void initialize() {
		delta_l_ = (float) ((1.0 - vh_) / kl_);
		delta_m_ = (vh_ - vl_) / (kh_ - kl_);
		delta_h_ = (float) (vl_ / (1.0 - kh_));
	}
}
