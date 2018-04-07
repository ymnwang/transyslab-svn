package com.transyslab.experiments;

import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.io.TXTUtils;
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
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by ITSA405-35 on 2017/12/12.
 */
public class OptToolBox {
	public static void main(String[] args) {

		String simMasterFileName = args.length>0 ? args[0]
												 : "/home/wym/master/QuickFD.properties";
		System.out.println("using: " + simMasterFileName.substring(simMasterFileName.lastIndexOf('/') + 1));
		Configuration config = ConfigUtils.createConfig(simMasterFileName);

		int popSize = 20;
		int maxGeneration = 1000;
		double crossOver_cr = 0.5;
		double crossOver_f = 0.5;
		String crossOver_variant = "rand/1/bin";

		String problemName = config.getString("problemName");
		System.out.println("problem name: " + problemName);
		SimProblem problem = (SimProblem) ProblemUtils.<DoubleSolution> loadProblem(problemName);
		problem.initProblem(simMasterFileName);
		DifferentialEvolution algorithm;
		DifferentialEvolutionSelection selection = new DifferentialEvolutionSelection();
		DifferentialEvolutionCrossover crossover = new DifferentialEvolutionCrossover(crossOver_cr, crossOver_f, crossOver_variant) ;

		algorithm = new DifferentialEvolution(problem,maxGeneration*popSize,popSize,
				crossover,selection,new SequentialSolutionListEvaluator<>());
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		algorithm.setSolutionWriter(new TXTUtils(new File(simMasterFileName).getParent() + "/" +
				config.getString("outputPath") + "/" +
				"AdvancedSolutionRecord_" + timeStamp + "_" + JMetalRandom.getInstance().getSeed() + ".csv"));
		algorithm.run();
		SimSolution bestSolution = (SimSolution) algorithm.getResult();
		System.out.println("BestFitness: " + Arrays.toString(bestSolution.getObjectiveValues()));
		System.out.println("BestSolution: " + Arrays.toString(bestSolution.getInputVariables()));
		System.out.println("SimSeed: " + bestSolution.getAttribute("SimSeed"));
		System.out.println("AlgSeed: " + JMetalRandom.getInstance().getSeed());
		problem.closeProblem();

	}
}
