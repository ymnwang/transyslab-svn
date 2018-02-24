package com.transyslab.experiments;

import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.tools.adapter.SimProblem;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.commons.tools.optimizer.DifferentialEvolution;
import com.transyslab.commons.tools.optimizer.DominanceComparator;
import org.apache.commons.configuration2.Configuration;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.Arrays;

/**
 * Created by ITSA405-35 on 2017/12/12.
 */
public class OptToolBox {
	public static void main(String[] args) {
		int popSize = 20;
		int maxGeneration = 10;
		double crossOver_cr = 0.5;
		double crossOver_f = 0.5;
		String crossOver_variant = "rand/1/bin";
		String simMasterFileName = args.length>0 ? args[0]
												 : "src/main/resources/demo_neihuan/scenario2/shortTerm.properties";
		System.out.println("using: " + simMasterFileName.substring(simMasterFileName.lastIndexOf('/') + 1));
		Configuration config = ConfigUtils.createConfig(simMasterFileName);

		String problemName = config.getString("problemName");
		System.out.println("problem name: " + problemName);
		SimProblem problem = (SimProblem) ProblemUtils.<DoubleSolution> loadProblem(problemName);
		problem.initProblem(simMasterFileName);
		DifferentialEvolution algorithm;
		DifferentialEvolutionSelection selection = new DifferentialEvolutionSelection();
		DifferentialEvolutionCrossover crossover = new DifferentialEvolutionCrossover(crossOver_cr, crossOver_f, crossOver_variant) ;

		algorithm = new DifferentialEvolution(problem,maxGeneration*popSize,popSize,
				crossover,selection,new SequentialSolutionListEvaluator<>());
		algorithm.run();
		problem.closeProblem();

	}
}
