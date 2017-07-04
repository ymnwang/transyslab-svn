/**
 *
 */
package com.transyslab.simcore.mesots;

/**
 * @author YYL 2016-6-6
 */
public class MesoSdFnNonLinear extends MesoSdFn {
	// Non Linear Speed-Density Function
	private double speedBeta; // parameters in nonlinear sd functions
	private double densityBeta;


	public MesoSdFnNonLinear() {
		speedBeta = 5;
		densityBeta = 1.8;
	}
	public MesoSdFnNonLinear(double cap, double spd, double kjam, double alpha, double beta) {
		super(cap, spd, kjam);
		densityBeta = alpha;
		speedBeta = beta;
	}
	public MesoSdFnNonLinear(double alpha, double beta) {
		densityBeta = alpha;
		speedBeta = beta;
	}
	public void updateParams(double[] params){
		if(params.length <6)
			System.out.print("Error: length of params is shorter than 6");
		capacity = params[0];
		jamSpeed = params[1];
		freeSpeed = params[2];
		jamDensity = params[3];
		densityBeta = params[4];
		speedBeta = params[5];
	}
	@Override
	public double densityToSpeed(/*float free_speed,*/ double density, int nlanes) {
		if (density < 1.0) {
			return freeSpeed;
		}
		else if (density + 1.0 > jamDensity) {
			return jamSpeed * (nlanes - 1);
		}
		else {
			double y = density / jamDensity;
			double k =  Math.pow(y, densityBeta);
			double r = Math.pow(1.0 - k, speedBeta);
			double v0 = jamSpeed * (nlanes - 1);
			return v0 + r * (freeSpeed - v0);
		}
	}
}
