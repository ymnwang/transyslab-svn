package com.transyslab.experiments;

import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.tools.adapter.SimProblem;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.commons.tools.optimizer.DifferentialEvolution;
import com.transyslab.commons.tools.optimizer.DominanceComparator;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.Arrays;

public class OptmKSProblemIdvd {
    public static void main(String[] args) {
        int popSize = 20;
        int maxGeneration = 1000;
        double crossOver_cr = 0.5;
        double crossOver_f = 0.5;
        String crossOver_variant = "rand/1/bin";
        String simMasterFileName = "src/main/resources/demo_neihuan/scenario2/optmksidvd.properties";

        SimProblem problem = new KSIdvdProblem(simMasterFileName);
        DifferentialEvolution algorithm;
        DifferentialEvolutionSelection selection = new DifferentialEvolutionSelection();
        DifferentialEvolutionCrossover crossover = new DifferentialEvolutionCrossover(crossOver_cr, crossOver_f, crossOver_variant) ;

        algorithm = new DifferentialEvolution(problem,maxGeneration*popSize,popSize,
                crossover,selection,new SequentialSolutionListEvaluator<>());
        algorithm.setComparator(new DominanceComparator<>());
        algorithm.setSolutionWriter(new TXTUtils("src/main/resources/demo_neihuan/scenario2/ksIdvd.csv"));
        algorithm.run();
        SimSolution bestSolution = (SimSolution) algorithm.getResult();
        System.out.println("BestFitness: " + Arrays.toString(bestSolution.getObjectiveValues()));
        System.out.println("BestSolution: " + Arrays.toString(bestSolution.getInputVariables()));
        System.out.println("BestSpeedSeries: "+ Arrays.toString((double[]) bestSolution.getAttribute("SimSpeed")));
        System.out.println("BestSimSeed: "+ Arrays.toString((double[]) bestSolution.getAttribute("SimSeed")));
        problem.closeProblem();
    }
}

