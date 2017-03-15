package com.transyslab.simcore;

public class Scheduler {
	private int worker_amount;
	private EngTread[] workers;
	
	public Scheduler(EngTread[] engTreads) {
		worker_amount = engTreads.length;
		workers = engTreads;
	}
	
	
}
