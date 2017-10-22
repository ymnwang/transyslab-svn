package com.transyslab.commons.tools.optimizer;

import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskWorker;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.impl.DefaultDoubleSolution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yali on 2017/10/18.
 */
public class SimulationEvaluator<S> extends SchedulerThread implements SolutionListEvaluator<S>{
	protected List<Task>taskList;
	public SimulationEvaluator(String thread_name, TaskCenter task_center){
		super(thread_name, task_center);
		this.taskList = new ArrayList<>();
		/*
		this.generationCounter = 1;
		this.txtWriter = new TXTUtils("src/main/resources/output/particle.csv");*/
	}
	@Override
	public void run() {

	}

	@Override
	public List<S> evaluate(List<S> solutionList, Problem<S> problem) {
		String attribute = "Speed";
		double weight= 0.5;
		int numberOfVariables = problem.getNumberOfVariables() ;
		for (int j = 0; j < solutionList.size(); j++) {
			DoubleSolution solution = (DoubleSolution) solutionList.get(j);
			double[] parameters = new double[]{0.4633,21.7950,0.1765, 120.0/3.6,0.0, 0.0, 0.0, 0.0};//[Qm, Vfree, Kjam, VPhyLim]

			double[] x = new double[numberOfVariables] ;

			for (int i = 0; i < numberOfVariables; i++) {
				x[i] = solution.getVariableValue(i) ;
			}
			System.arraycopy(x,0,parameters,4,numberOfVariables);
			taskList.add(this.dispatch(parameters, TaskWorker.ANY_WORKER));
		}
		for (int j = 0; j < solutionList.size(); j++) {
			DefaultDoubleSolution solution = (DefaultDoubleSolution) solutionList.get(j);
			double[] tmpResults = taskList.get(j).getOutputs();
			double[] speed = new double[tmpResults.length - 2];
			System.arraycopy(tmpResults,2,speed,0,speed.length);
			double fitness = weight * tmpResults[0] + (1-weight) * tmpResults[1];
			solution.setObjective(0, fitness);
			solution.setAttribute(attribute,speed);
			/*
			StringBuilder solutionInfo = new StringBuilder();
			for(int i = 0; i < numberOfVariables; i++){
				solutionInfo.append(solution.getVariableValueString(i)).append(",");
			}
			solutionInfo.append(fitness).append(",");
			solutionInfo.append(generationCounter).append("\r\n");
			txtWriter.write(solutionInfo.toString());*/
		}
		taskList.clear();
		return solutionList;
	}

	@Override
	public void shutdown() {
		//算法结束时调用
		dismissAllWorkingThreads();//stop eng线程。
	}
}
