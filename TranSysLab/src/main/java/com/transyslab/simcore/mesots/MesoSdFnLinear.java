/**
 *
 */
package com.transyslab.simcore.mesots;

/**
 * @author YYL 2016-6-6
 */
// Linear Speed-Density Function
public class MesoSdFnLinear extends MesoSdFn {

	private double kl, vh;
	private double kh, vl;
	private double deltaL, deltaM, deltaH;

	public MesoSdFnLinear() {
		kl = 0.35f;
		vh = 0.76f;
		kh = 0.56f;
		vl = 0.20f;
	}
	public MesoSdFnLinear(double cap, double spd, double kjam, double kl, double vh, double kh, double vl) {
		super(cap, spd, kjam);
		this.kl = kl;
		this.vh = vh;
		this.kh = kh;
		this.vl = vl;
		initialize();
	}

	// void print(ostream &os = cout);
	@Override
	public double densityToSpeed(/*float free_speed, */double density, int nlanes) {
		if (density < 1.0) {
			return freeSpeed;
		}
		else if (density + 1.0 > jamDensity) {
			return jamSpeed * (nlanes - 1);
		}
		else {
			double y = density / jamDensity;
			double v0 = jamSpeed * (nlanes - 1);
			double r;
			if (y < kl) {
				r = 1.0 - deltaL * y;
			}
			else if (y > kh) {
				r = vh - deltaM * (y - kl);
			}
			else {
				r = vl - deltaH * (y - kh);
			}
			return v0 + r * (freeSpeed - v0);
		}
	}
	public void updateParams(double[] params){
		// TODO ´ýÊµÏÖ
	}
	private void initialize() {
		deltaL = (1.0 - vh) / kl;
		deltaM = (vh - vl) / (kh - kl);
		deltaH = vl / (1.0 - kh);
	}
}
