package com.transyslab.experiments;


import com.transyslab.commons.tools.adapter.SimProblem;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.commons.tools.optimizer.DifferentialEvolution;
import com.transyslab.commons.tools.optimizer.DominanceComparator;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.Arrays;

/**
 * Created by yali on 2017/11/29.
 */
public class OptmKSProblem {
	public static void main(String[] args) {
		int popSize = 20;
		int maxGeneration = 2000;
		double crossOver_cr = 0.5;
		double crossOver_f = 0.5;
		String crossOver_variant = "rand/1/bin";
		String simMasterFileName = "src/main/resources/demo_neihuan/scenario2/optmks.properties";

		SimProblem problem = new KSProblem(simMasterFileName);
		DifferentialEvolution algorithm;
		DifferentialEvolutionSelection selection = new DifferentialEvolutionSelection();
		DifferentialEvolutionCrossover crossover = new DifferentialEvolutionCrossover(crossOver_cr, crossOver_f, crossOver_variant) ;

		algorithm = new DifferentialEvolution(problem,maxGeneration*popSize,popSize,
				crossover,selection,new SequentialSolutionListEvaluator<>());
		algorithm.setComparator(new DominanceComparator<>());
		algorithm.run();
		SimSolution bestSolution = (SimSolution) algorithm.getResult();
		System.out.println("BestFitness: " + Arrays.toString(bestSolution.getObjectiveValues()));
		System.out.println("BestSolution: " + Arrays.toString(bestSolution.getInputVariables()));
		System.out.println("BestSpeedSeries: "+ Arrays.toString((double[]) bestSolution.getAttribute("SimSpeed")));
		problem.closeProblem();

	}
}
