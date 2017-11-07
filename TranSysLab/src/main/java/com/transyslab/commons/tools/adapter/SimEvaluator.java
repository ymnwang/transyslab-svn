package com.transyslab.commons.tools.adapter;

import com.transyslab.commons.tools.mutitask.TaskGiver;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.List;

/**
 * JMetal架构中的适配类，非必要。
 * Created by WangYimin on 2017/11/1.
 */
public class SimEvaluator implements SolutionListEvaluator{
	private TaskGiver scheduler;

	@Override
	public List evaluate(List solutionList, Problem problem) {
		//异步发送任务
		solutionList.forEach(s -> problem.evaluate(s));
		//任务结果同步
		solutionList.forEach(s -> ((SimSolution) s).getObjectiveValues());
		return solutionList;
	}

	@Override
	public void shutdown() {
		if (this.scheduler!=null)
			this.scheduler.dismissAllWorkingThreads();
	}

	public SimEvaluator setScheduler(TaskGiver scheduler) {
		this.scheduler = scheduler;
		return this;
	}
}
