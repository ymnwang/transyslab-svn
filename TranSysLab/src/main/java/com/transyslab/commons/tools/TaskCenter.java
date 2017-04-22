package com.transyslab.commons.tools;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class TaskCenter {
	protected BlockingQueue<double[]> undoneTasks;
	private Integer finishCount;
	protected double [] results;
	private boolean killThreadSignal;
	
	public TaskCenter(int arg0) {
		undoneTasks = new ArrayBlockingQueue<>(arg0);
		results = new double [arg0];
		killThreadSignal = false;
	}
	
	public TaskCenter() {
		undoneTasks = new ArrayBlockingQueue<>(100);
		results = new double [100];
		killThreadSignal = false;
	}
	
	protected void setTaskAmount(int arg) {
		if (arg > results.length) {
			results = new double [arg];
		}
		finishCount = 0;
	}
	
	protected synchronized double getResult(int i) {
		while (finishCount < results.length) {
			try {
				System.out.println("waiting for result ");
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		System.out.println("TID " + i + " retrieved");
		return results[i];
	}
	
	protected synchronized void setResult(int idx, double fitVal) {
		results[idx] = fitVal;
		finishCount += 1;
		if (finishCount >= results.length) 
			notify();
//		System.out.println(Thread.currentThread().getName() + " finished TID " + idx);
	}
	
	protected synchronized boolean dismissAllowed() {
		return killThreadSignal;
	}
	
	protected synchronized void dismiss() {
		killThreadSignal = true;
		notify();
	}
}
