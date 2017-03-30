package com.transyslab.commons.tools;


public class FitnessFunction {
	public static double evaRNSE(double[] sim, double[] obs){
		if(sim.length ==0 || sim.length != obs.length){
			System.out.print("Error:The length of two input arrays are not equal!");
			return Double.NaN;
		}
		double numerator = 0;
		double denominator = 0;
		for(int i=0;i<sim.length;i++){
			numerator += Math.pow(sim[i]-obs[i],2);
			denominator += obs[i];
		}
		return Math.sqrt(numerator*sim.length)/denominator;		
	}
	public static double evaMAPE(double[] sim, double[] obs){
		if (sim.length ==0 || sim.length != obs.length) {
			System.out.print("Error:The length of two input arrays are not equal!");
			return Double.NaN;
		}		
		double sum = 0.0;
		int count = 0;
		for (int i = 0; i < sim.length; i++) {
			double del = Math.abs(sim[i] - obs[i]);
			if (Math.abs(obs[i])>0.0001) {
				sum += del / obs[i];
				count += 1;
			}
		}		
		if (count > 0) 
			return (sum / count);
		else 
			return 0.0;
	}
}
