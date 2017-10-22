package com.transyslab.commons.tools.optimizer;

import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

import java.util.List;

/**
 * Created by yali on 2017/10/18.
 */
public class SimulationProblem extends AbstractDoubleProblem{


	public SimulationProblem(int numOfVariables, int numOfObjectives, int numOfConstrants,
							 List<Double> lowerLimit, List<Double> upperLimit){
		setNumberOfVariables(numOfVariables);
		setNumberOfObjectives(numOfObjectives);
		setNumberOfConstraints(numOfConstrants) ;
		setName("RMSE");
		setLowerLimit(lowerLimit);
		setUpperLimit(upperLimit);
	}


	@Override
	public void evaluate(DoubleSolution solution) {

	}

}
