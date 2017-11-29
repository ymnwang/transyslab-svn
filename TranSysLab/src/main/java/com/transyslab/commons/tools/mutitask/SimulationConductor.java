package com.transyslab.commons.tools.mutitask;

import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.simcore.SimulationEngine;

/**
 * Created by WangYimin on 2017/11/14.
 */
public interface SimulationConductor {
	void alterEngineParameters(SimulationEngine engine, double[] inputVariables);
	double[] evaluateFitness(SimulationEngine engine);
	void modifySolutionBeforeEnd(SimSolution simSolution);
}
