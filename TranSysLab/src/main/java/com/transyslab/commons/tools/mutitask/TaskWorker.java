package com.transyslab.commons.tools.mutitask;

/**
 * Created by WangYimin on 2017/7/18.
 */
public interface TaskWorker {
	double[] worksUnder(double[] paras);

	default void gotowork(TaskCenter tc){
		while (!tc.dismissAllowed()) {

			//���Դ���������taskCenterȡ������
			Task task = tc.fetchTask();

			if (task != null) {
//					System.out.println(Thread.currentThread().getName() + " received TID " + (int) task[0]);
				double [] p = task.getInputs();
				double[] fitVal = worksUnder(p);
				task.setOutputs(fitVal);
			}
		}
	}
}
