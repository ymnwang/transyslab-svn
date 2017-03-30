package com.transyslab.commons.tools;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class TaskCenter {
	public BlockingQueue<double[]> undoneTasks;
	private Integer finishCount;
	private double [] results;
	private boolean killThreadSignal;
	
	public TaskCenter(int arg0) {
		undoneTasks = new ArrayBlockingQueue<>(arg0);
		killThreadSignal = false;
	}
	
	public void setTaskAmount(int arg) {
		results = new double [arg];
		finishCount = 0;
	}
	public synchronized double getResult(int i) {
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
	
	public synchronized void setResult(int idx, double fitVal) {
		results[idx] = fitVal;
		finishCount += 1;
		if (finishCount >= results.length) 
			notify();
//		System.out.println(Thread.currentThread().getName() + " finished TID " + idx);
	}
	
	public synchronized double[] getTask() {
		double[] ans = null;
		try {
			ans = undoneTasks.poll(100, TimeUnit.MILLISECONDS);
			if (ans == null) 
				wait();
			System.out.println(Thread.currentThread().getName() + "waiting for task");
		} catch (Exception e) {
			// TODO: handle exception
		}
		return ans;
	}
	
	public synchronized void addTask(double[] arg) {
		try {
			undoneTasks.put(arg);
			notify();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public synchronized boolean isDismissed() {
		return killThreadSignal;
	}
	
	public synchronized void Dismiss() {
		killThreadSignal = true;
		notify();
	}
}
