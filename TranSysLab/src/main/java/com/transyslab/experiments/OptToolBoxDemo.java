package com.transyslab.experiments;

import com.transyslab.commons.tools.adapter.DefaultMLPProblem;
import com.transyslab.commons.tools.adapter.SimProblem;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.singleobjective.differentialevolution.DifferentialEvolutionBuilder;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 * Created by WangYimin on 2017/10/27.
 */
public class OptToolBoxDemo {
	public static void main(String[] args) {

		JMetalRandom.getInstance().setSeed(2017);
		int popSize = 5;
		int maxGeneration = 5;
		double crossOver_cr = 0.5;
		double crossOver_f = 0.5;
		String crossOver_variant = "rand/1/bin";
		String simMasterFileName = "src/main/resources/demo_neihuan/scenario2/default.properties";

		SimProblem problem = new DefaultMLPProblem(simMasterFileName);
		Algorithm<DoubleSolution> algorithm;
		DifferentialEvolutionSelection selection = new DifferentialEvolutionSelection();
		DifferentialEvolutionCrossover crossover = new DifferentialEvolutionCrossover(crossOver_cr, crossOver_f, crossOver_variant) ;


		algorithm = new DifferentialEvolutionBuilder(problem)
				.setCrossover(crossover)
				.setSelection(selection)
				.setMaxEvaluations(maxGeneration*popSize)
				.setPopulationSize(popSize)
				.build();
		algorithm.run();

		problem.closeProblem();

	}
}
