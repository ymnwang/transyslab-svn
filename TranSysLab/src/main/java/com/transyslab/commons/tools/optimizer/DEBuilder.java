package com.transyslab.commons.tools.optimizer;

import org.uma.jmetal.algorithm.singleobjective.differentialevolution.DifferentialEvolution;
import org.uma.jmetal.algorithm.singleobjective.differentialevolution.DifferentialEvolutionBuilder;
import org.uma.jmetal.problem.DoubleProblem;

/**
 * Created by yali on 2017/10/19.
 */
public class DEBuilder extends DifferentialEvolutionBuilder{
	private DEAlgorithm2 de;
	public DEBuilder(DoubleProblem problem){
		super(problem);
	}
	public DifferentialEvolution build(){
		de = new DEAlgorithm2(getProblem(),getMaxEvaluations(),getPopulationSize(),getCrossoverOperator(),
				getSelectionOperator(),getSolutionListEvaluator());
		return de;
	}
	public void close(){
		this.getSolutionListEvaluator().shutdown();
		de.closeTxtWriter();
	}
}
