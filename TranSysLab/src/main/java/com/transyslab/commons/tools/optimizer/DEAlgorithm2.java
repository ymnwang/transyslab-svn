package com.transyslab.commons.tools.optimizer;

import com.transyslab.commons.io.TXTUtils;
import org.uma.jmetal.algorithm.singleobjective.differentialevolution.DifferentialEvolution;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.impl.DefaultDoubleSolution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yali on 2017/10/19.
 */
public class DEAlgorithm2 extends DifferentialEvolution{
	protected int generationCounter;
	protected TXTUtils txtWriter;

	public DEAlgorithm2(DoubleProblem problem, int maxEvaluations, int populationSize,
						DifferentialEvolutionCrossover crossoverOperator,
						DifferentialEvolutionSelection selectionOperator, SolutionListEvaluator<DoubleSolution> evaluator){
		super(problem,maxEvaluations,populationSize,crossoverOperator,selectionOperator,evaluator);
		generationCounter = 1;
		txtWriter = new TXTUtils("src/main/resources/output/particle.csv");
	}
	protected List<DoubleSolution> replacement(List<DoubleSolution> population,
													List<DoubleSolution> offspringPopulation){
		List<DoubleSolution> pop =super.replacement(population,offspringPopulation);
		int numberOfVariables = pop.get(0).getNumberOfVariables();
		for(int i=0;i<pop.size();i++){
			DefaultDoubleSolution solution = (DefaultDoubleSolution) pop.get(i);
			StringBuilder solutionInfo = new StringBuilder();
			for(int j = 0; j < numberOfVariables; j++){
				solutionInfo.append(solution.getVariableValueString(j)).append(",");
			}
			solutionInfo.append(solution.getObjective(0)).append(",");
			solutionInfo.append(generationCounter).append("\r\n");
			txtWriter.write(solutionInfo.toString());
		}
		txtWriter.flushBuffer();
		generationCounter ++;
		return pop;

	}
	public void closeTxtWriter(){
		txtWriter.closeWriter();
	}
}
