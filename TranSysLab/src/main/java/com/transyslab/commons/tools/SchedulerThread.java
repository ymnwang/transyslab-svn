package com.transyslab.commons.tools;

public abstract class SchedulerThread extends Thread{
	private TaskCenter taskCenter;
	
	public SchedulerThread(String thread_name, TaskCenter task_center) {
		setName(thread_name);
		taskCenter = task_center;
	}
	
	protected boolean isVolumShorted(int idx) {
		return (taskCenter.results == null || taskCenter.results.length < idx + 1);
	}
	
	protected void resetTaskPool(int arg) {
		taskCenter.setTaskAmount(arg);
	}
	
	protected void dispatchTask(int idx, double[] paras) {
		if (isVolumShorted(idx)) {
			System.err.println("too many tasks");
			return;
		}
		double[] task = new double[paras.length+1];
		task[0] = idx;
		System.arraycopy(paras, 0, task, 1, paras.length);
		try {
			taskCenter.undoneTasks.put(task);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void dispatchTask(int idx, float[] paras) {
		if (isVolumShorted(idx)) {
			System.err.println("too many tasks");
			return;
		}
		double[] task = new double[paras.length + 1];
		task[0] = idx;
		for (int k = 0; k < paras.length; k++) {
			task[k+1] = paras[k];
		}
		try {
			taskCenter.undoneTasks.put(task);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected double[] fetchResult(int task_idx) {
		return taskCenter.getResult(task_idx);
	}
	
	protected void dismissAllWorkingThreads() {
		taskCenter.dismiss();
	}
	
	@Override
	public abstract void run();
	
}
