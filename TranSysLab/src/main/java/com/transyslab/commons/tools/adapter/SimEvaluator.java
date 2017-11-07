package com.transyslab.commons.tools.adapter;

import com.transyslab.commons.tools.mutitask.TaskGiver;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.List;

/**
 * JMetal�ܹ��е������࣬�Ǳ�Ҫ��
 * Created by WangYimin on 2017/11/1.
 */
public class SimEvaluator implements SolutionListEvaluator{
	private TaskGiver scheduler;

	@Override
	public List evaluate(List solutionList, Problem problem) {
		//�첽��������
		solutionList.forEach(s -> problem.evaluate(s));
		//������ͬ��
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
