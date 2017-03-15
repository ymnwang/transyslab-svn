package com.transyslab.commons.tools.rawpso;

import java.util.Random;

public class Particle {
	protected double[] pos;
	protected double[] bestPos;
	protected double bestVal;
	private Random rand;
	
	public Particle(int dim) {		
		pos = new double[dim];
		rand = new Random();
		for (int i = 0; i < dim; i++) {
			pos[i] = rand.nextDouble();
		}
		bestPos = pos;
		bestVal = Double.POSITIVE_INFINITY;		
	}
	
	public void updateBest(double val) {
		if (val < bestVal) {
			bestVal = val;
			bestPos = pos;
		}
	}
}
