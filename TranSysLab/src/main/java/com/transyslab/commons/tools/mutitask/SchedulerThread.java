package com.transyslab.commons.tools.mutitask;

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

	@Override
	public abstract void run();

	@Override
	public TaskCenter getTaskCenter() {
		return taskCenter;
	}
}
