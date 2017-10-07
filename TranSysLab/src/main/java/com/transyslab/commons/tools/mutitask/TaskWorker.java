package com.transyslab.commons.tools.mutitask;

/**
 * Created by WangYimin on 2017/7/18.
 */
public interface TaskWorker {

	String ANY_WORKER = null;

	double[] worksUnder(double[] paras);

	default void init() {}

	default void dismiss() {}

	default void goToWork(TaskCenter tc, boolean taskSpecified){
		init();
		while (!tc.dismissAllowed()) {

			//���Դ���������taskCenterȡ������
			Task task;
			if (!taskSpecified)
				task = tc.fetchUnspecificTask();
			else
				task = tc.fetchSpecificTask();

			if (task != null) {
//					System.out.println(Thread.currentThread().getName() + " received TID " + (int) task[0]);
				double [] p = task.getInputs();
				double[] fitVal = worksUnder(p);
				task.setOutputs(fitVal);
			}
		}
		dismiss();
	}
}
