package com.transyslab.commons.tools.mutitask;

/**
 * Created by WangYimin on 2017/7/18.
 */
public interface TaskGiver {
	default Task dispatchTask(TaskCenter tc, double[] paras, String workerName) {
		Task task = new Task(paras, workerName);
		tc.addTask(task);
		return task;
	}

	default Task dispatchTask(TaskCenter tc, float[] paras, String workerName) {
		double[] paras2 = new double[paras.length];
		for (int i = 0; i < paras2.length; i++) {
			paras2[i] = (double) paras[i];
		}
		Task task = new Task(paras2, workerName);
		tc.addTask(task);
		return task;
	}

	default void dispatchTask(TaskCenter tc, Task task) {
		task.resetResult();
		tc.addTask(task);
	}

	default void dismissAllWorkingThreadsIn(TaskCenter tc) {
		tc.dismiss();
	}
}
