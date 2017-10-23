package com.transyslab.commons.tools.optimizer;

import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskGiver;
import com.transyslab.commons.tools.mutitask.TaskWorker;

/**
 * Created by WangYimin on 2017/7/18.
 */
public abstract class SchedulerThread extends Thread implements TaskGiver{
	private TaskCenter taskCenter;

	public SchedulerThread(String thread_name, TaskCenter task_center) {
		setName(thread_name);
		taskCenter = task_center;
	}

	protected void dismissAllWorkingThreads() {
		dismissAllWorkingThreadsIn(taskCenter);
	}

	public Task dispatch(double[] paras, String workerName) {
		return dispatchTask(taskCenter, paras, workerName);
	}

	public Task dispatch(float[] paras, String workerName) {
		return dispatchTask(taskCenter, paras, workerName);
	}

	public void dispatch(Task arg) {
		dispatchTask(taskCenter, arg);
	}

	@Override
	public abstract void run();
}
