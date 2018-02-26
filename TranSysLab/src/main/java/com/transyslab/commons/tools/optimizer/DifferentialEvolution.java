package com.transyslab.commons.tools.optimizer;

import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.tools.adapter.SimSolution;
import org.uma.jmetal.algorithm.impl.AbstractDifferentialEvolution;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.*;

/**
 * Created by yali on 2017/11/27.
 * 重写DE算法的选择操作，应对单目标函数（KS）多解的问题
 * 当目标函数取值相同时，根据其他约束或目标评价当前解是否更优（帕累托支配）
 */
public class DifferentialEvolution extends AbstractDifferentialEvolution<DoubleSolution> {
	private int populationSize;
	private int maxEvaluations;
	private SolutionListEvaluator<DoubleSolution> evaluator;
	private Comparator<DoubleSolution> comparator;
	private int evaluations;
	private TXTUtils solutionWriter;
	private boolean needOutput;
	/**
	 * Constructor
	 *
	 * @param problem           Problem to solve
	 * @param maxEvaluations    Maximum number of evaluations to perform
	 * @param populationSize
	 * @param crossoverOperator
	 * @param selectionOperator
	 * @param evaluator
	 */
	public DifferentialEvolution(DoubleProblem problem, int maxEvaluations, int populationSize, DifferentialEvolutionCrossover crossoverOperator, DifferentialEvolutionSelection selectionOperator, SolutionListEvaluator<DoubleSolution> evaluator) {
		setProblem(problem);
		this.maxEvaluations = maxEvaluations;
		this.populationSize = populationSize;
		this.crossoverOperator = crossoverOperator;
		this.selectionOperator = selectionOperator;
		this.evaluator = evaluator;
		comparator = new ObjectiveComparator<DoubleSolution>(0);
	}
	public void setComparator(Comparator<DoubleSolution> comparator){
		this.comparator = comparator;
	}
	public int getEvaluations() {
		return evaluations;
	}

	public void setEvaluations(int evaluations) {
		this.evaluations = evaluations;
	}
	public void setSolutionWriter(TXTUtils writer){
		needOutput = true;
		solutionWriter = writer;
	}
	@Override protected void initProgress() {
		evaluations = populationSize;
	}

	@Override protected void updateProgress() {
		evaluations += populationSize;
	}

	@Override protected boolean isStoppingConditionReached() {
		return evaluations >= maxEvaluations;
	}

	@Override protected List<DoubleSolution> createInitialPopulation() {
		List<DoubleSolution> population = new ArrayList<>(populationSize);
		for (int i = 0; i < populationSize; i++) {
			DoubleSolution newIndividual = getProblem().createSolution();
			population.add(newIndividual);
		}
		return population;
	}

	@Override protected List<DoubleSolution> evaluatePopulation(List<DoubleSolution> population) {
		return evaluator.evaluate(population, getProblem());
	}

	@Override protected List<DoubleSolution> selection(List<DoubleSolution> population) {
		return population;
	}

	@Override protected List<DoubleSolution> reproduction(List<DoubleSolution> matingPopulation) {
		List<DoubleSolution> offspringPopulation = new ArrayList<>();

		for (int i = 0; i < populationSize; i++) {
			selectionOperator.setIndex(i);
			List<DoubleSolution> parents = selectionOperator.execute(matingPopulation);

			crossoverOperator.setCurrentSolution(matingPopulation.get(i));
			List<DoubleSolution> children = crossoverOperator.execute(parents);

			offspringPopulation.add(children.get(0));
		}

		return offspringPopulation;
	}

	@Override protected List<DoubleSolution> replacement(List<DoubleSolution> population,
														 List<DoubleSolution> offspringPopulation) {
		List<DoubleSolution> pop = new ArrayList<>();

		for (int i = 0; i < populationSize; i++) {
			if (comparator.compare(population.get(i), offspringPopulation.get(i)) < 0) {
				pop.add(population.get(i));
			} else {
				pop.add(offspringPopulation.get(i));
			}
		}

		Collections.sort(pop, comparator) ;
		if(needOutput){
			for (int i = 0; i < populationSize; i++) {
				SimSolution solution = ((SimSolution)pop.get(i));
				solutionWriter.writeNFlush(Arrays.toString(solution.getInputVariables())
						.replace(" ","")
						.replace("[","")
						.replace("]","") + "," +
						Arrays.toString(solution.getObjectiveValues()).replace(" ","")
								.replace("[","")
								.replace("]","")+ "\r\n");
			}

		}
		return pop;
	}

	/**
	 * Returns the best individual
	 */
	@Override public DoubleSolution getResult() {
		Collections.sort(getPopulation(), comparator) ;

		return getPopulation().get(0);
	}

	@Override public String getName() {
		return "DE" ;
	}

	@Override public String getDescription() {
		return "Differential Evolution Algorithm" ;
	}
}
