package com.transyslab.simcore.mlp;

import java.util.Arrays;
import java.util.Random;

import com.transyslab.commons.tools.SchedulerThread;
import com.transyslab.commons.tools.TaskCenter;

public class PSA extends SchedulerThread{
	private Random psa_rnd;
	protected double[] pos;
	
	public PSA(String str, TaskCenter tc, int dim) {
		super(str, tc);
		psa_rnd = new Random();
		pos = new double[dim];
	}
	
	protected void shufflePosition(double[] lower, double[] upper) {
		for (int i = 0; i < pos.length; i++) {
			pos[i] = (upper[i] - lower[i])*psa_rnd.nextDouble() + lower[i];
		}
	}
	
	@Override
	public void run() {
		
	}

}
