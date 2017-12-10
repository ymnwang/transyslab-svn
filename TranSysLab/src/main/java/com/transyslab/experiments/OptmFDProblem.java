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
 * Created by ITSA405-35 on 2017/12/12.
 */
public class OptmFDProblem {
	public static void main(String[] args) {
		int popSize = 20;
		int maxGeneration = 10;
		double crossOver_cr = 0.5;
		double crossOver_f = 0.5;
		String crossOver_variant = "rand/1/bin";
		String simMasterFileName = "src/main/resources/demo_neihuan/scenario2/FD.properties";/*/home/wym/runtime/demo_neihuan/scenario2/FD.properties*/

		SimProblem problem = new FDProblem(simMasterFileName);
		DifferentialEvolution algorithm;
		DifferentialEvolutionSelection selection = new DifferentialEvolutionSelection();
		DifferentialEvolutionCrossover crossover = new DifferentialEvolutionCrossover(crossOver_cr, crossOver_f, crossOver_variant) ;

		algorithm = new DifferentialEvolution(problem,maxGeneration*popSize,popSize,
				crossover,selection,new SequentialSolutionListEvaluator<>());
		algorithm.run();
		problem.closeProblem();

	}
}
