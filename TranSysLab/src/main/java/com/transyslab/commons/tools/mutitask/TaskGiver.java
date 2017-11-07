package com.transyslab.commons.tools.mutitask;

/**
 * Created by WangYimin on 2017/7/18.
 */
public interface TaskGiver {
	default Task dispatch(double[] paras, String workerName) {
		Task task = new Task(paras, workerName);
		getTaskCenter().addTask(task);
		return task;
	}

	default Task dispatch(float[] paras, String workerName) {
		double[] paras2 = new double[paras.length];
		for (int i = 0; i < paras2.length; i++) {
			paras2[i] = (double) paras[i];
		}
		Task task = new Task(paras2, workerName);
		getTaskCenter().addTask(task);
		return task;
	}

	default void dispatch(Task task) {
		task.resetResult();
		getTaskCenter().addTask(task);
	}

	default void dismissAllWorkingThreads() {
		getTaskCenter().dismiss();
	}

	default void activateWorker(TaskWorker worker) {

	}

	TaskCenter getTaskCenter();
}
