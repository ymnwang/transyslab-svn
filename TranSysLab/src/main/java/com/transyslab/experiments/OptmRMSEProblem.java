package com.transyslab.experiments;

import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.tools.adapter.SimProblem;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.commons.tools.optimizer.DifferentialEvolution;
import com.transyslab.commons.tools.optimizer.DominanceComparator;
import com.transyslab.simcore.mlp.ExpSwitch;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.Arrays;

public class OptmRMSEProblem {
    public static void main(String[] args) {

        ExpSwitch.APPROACH_CTRL = true;
        ExpSwitch.MAX_ACC_CTRL = true;
        int popSize = 20;
        int maxGeneration = 1000;
        double crossOver_cr = 0.5;
        double crossOver_f = 0.5;
        String crossOver_variant = "rand/1/bin";
        String simMasterFileName = "src/main/resources/optmksidvd.properties";

        SimProblem problem = new RMSEProblem(simMasterFileName);
        DifferentialEvolution algorithm;
        DifferentialEvolutionSelection selection = new DifferentialEvolutionSelection();
        DifferentialEvolutionCrossover crossover = new DifferentialEvolutionCrossover(crossOver_cr, crossOver_f, crossOver_variant) ;

        algorithm = new DifferentialEvolution(problem,maxGeneration*popSize,popSize,
                crossover,selection,new SequentialSolutionListEvaluator<>());
//        algorithm.setComparator(new DominanceComparator<>());
        algorithm.setSolutionWriter(new TXTUtils("src/main/resources/rs(RMSE).csv"));
        algorithm.run();
        SimSolution bestSolution = (SimSolution) algorithm.getResult();
        System.out.println("BestFitness: " + Arrays.toString(bestSolution.getObjectiveValues()));
        System.out.println("BestSolution: " + Arrays.toString(bestSolution.getInputVariables()));
        System.out.println("AlgSeed: " + JMetalRandom.getInstance().getSeed());
        problem.closeProblem();

    }
}
