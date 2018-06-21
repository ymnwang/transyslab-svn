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
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Scanner;

/**
 * Created by ITSA405-35 on 2017/12/12.
 */
public class OptToolBox {
	public static void main(String[] args) {
		DifferentialEvolution alg = createAlgorithm(args);
		Thread algThread = new Thread(alg);
		algThread.start();
		Scanner scanner = new Scanner(System.in);
		while (algThread.isAlive()) {
			if (scanner.hasNextLine()){
				switch (scanner.nextLine()) {
					case "q": {
						System.out.println("progress is shutting down, please wait......");
						alg.shutdown();
					}break;
				}
			}
		}
		System.out.println(alg.getStopInfo());
	}

	public static DifferentialEvolution createAlgorithm(String[] args) {
		String simMasterFileName = args[0];
		System.out.println("using: " + simMasterFileName.substring(simMasterFileName.lastIndexOf('/') + 1));
		Configuration config = ConfigUtils.createConfig(simMasterFileName);

		int popSize = 20;
		int maxGeneration = 1000;
		double crossOver_cr = 0.5;
		double crossOver_f = 0.5;
		String crossOver_variant = "rand/1/bin";

		String problemName = config.getString("problemName");
		System.out.println("problem name: " + problemName);
		DoubleProblem problem = (DoubleProblem) ProblemUtils.<DoubleSolution> loadProblem(problemName);
		if (problem instanceof SimProblem)
			((SimProblem)problem).initProblem(simMasterFileName);
		DifferentialEvolutionSelection selection = new DifferentialEvolutionSelection();
		DifferentialEvolutionCrossover crossover = new DifferentialEvolutionCrossover(crossOver_cr, crossOver_f, crossOver_variant) ;

		DifferentialEvolution algorithm = new DifferentialEvolution(problem,maxGeneration*popSize,popSize,
				crossover,selection,new SequentialSolutionListEvaluator<>());
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		algorithm.setSolutionWriter(new TXTUtils(new File(simMasterFileName).getParent() + "/" +
				config.getString("outputPath") + "/" +
				"AdvancedSolutionRecord_" + timeStamp + "_" + JMetalRandom.getInstance().getSeed() + ".csv"));

		return algorithm;
	}
}
