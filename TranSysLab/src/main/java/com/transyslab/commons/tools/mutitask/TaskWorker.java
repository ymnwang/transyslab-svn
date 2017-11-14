package com.transyslab.commons.tools.mutitask;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by WangYimin on 2017/7/18.
 */
public interface TaskWorker {

	String ANY_WORKER = null;

	double[] worksWith(Task task);

	default void init() {}

	default void onDismiss() {}

	default void goToWork(TaskCenter tc, boolean taskSpecified){
		init();
		while (!tc.dismissAllowed()) {

			//尝试从任务中心taskCenter取回任务
			Task task;
			if (!taskSpecified)
				task = tc.fetchUnspecificTask();
			else
				task = tc.fetchSpecificTask();

			if (task != null) {
//					System.out.println(Thread.currentThread().getName() + " received TID " + (int) task[0]);
				double[] fitness = worksWith(task);
				task.setResults(fitness, task.attributes);
			}
		}
		onDismiss();
	}
}
