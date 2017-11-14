package com.transyslab.commons.tools.adapter;

import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskWorker;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.HashMap;

/**
 * Created by WangYimin on 2017/11/5.
 */
public class SimSolution extends Task implements DoubleSolution {
	protected SimProblem problem;
	protected final JMetalRandom randomGenerator;

	public SimSolution(SimProblem problem, String workerName) {
		super(new double[problem.getNumberOfVariables()], workerName);
		//objectiveValues 的初始化只是为了与JMetal对应，
		//在此为冗余操作。
		objectiveValues = new double[problem.getNumberOfObjectives()];
		this.problem = problem;
		randomGenerator = JMetalRandom.getInstance();
		initializeDoubleVariables();
	}

	public SimSolution(SimProblem problem) {
		this(problem, TaskWorker.ANY_WORKER);
	}

	public SimSolution(SimSolution solution) {
		this(solution.problem, solution.workerName);

		for (int i = 0; i < problem.getNumberOfVariables(); i++) {
			setVariableValue(i, solution.getVariableValue(i));
		}

		//objectiveValues 的初始化只是为了与JMetal对应，
		//在此为冗余操作。

		for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
			objectiveValues[i] = solution.getObjective(i);
		}
	}

	/**
	 * 禁止从这个函数进行objective的设置，
	 * 只有TaskWorker才能进行目标函数的评定.
	 * 故设置为空函数。
	 */
	@Override
	public void setObjective(int index, double value) {

	}

	/**
	 * 获取Task异步计算结果，可能会触发阻塞。
	 * @param index 评价值的索引
	 * @return 评价值
	 */
	@Override
	public double getObjective(int index) {
		return getObjectiveValues()[index];
	}

	@Override
	public Double getVariableValue(int index) {
		return getInputVariableValue(index);
	}

	@Override
	public void setVariableValue(int index, Double value) {
		setInputVariableValue(index, value);
	}

	@Override
	public String getVariableValueString(int index) {
		return getVariableValue(index).toString();
	}

	@Override
	public int getNumberOfVariables() {
		return inputVariables.length;
	}

	@Override
	public int getNumberOfObjectives() {
		return objectiveValues.length;
	}

	@Override
	public Solution copy() {
		return new SimSolution(this);
	}

	@Override
	public Double getLowerBound(int index) {
		return problem.getLowerBound(index);
	}

	@Override
	public Double getUpperBound(int index) {
		return problem.getUpperBound(index);
	}

	private void initializeDoubleVariables() {
		for (int i = 0 ; i < problem.getNumberOfVariables(); i++) {
			Double value = randomGenerator.nextDouble(getLowerBound(i), getUpperBound(i)) ;
			setVariableValue(i, value) ;
		}
	}
}
