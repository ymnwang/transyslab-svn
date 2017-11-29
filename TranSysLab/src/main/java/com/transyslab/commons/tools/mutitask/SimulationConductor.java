package com.transyslab.commons.tools.mutitask;

import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.simcore.SimulationEngine;

/**
 * Created by WangYimin on 2017/11/14.
 */
public interface SimulationConductor {
	void modifyEngineBeforeStart(SimulationEngine engine, SimSolution simSolution);
	double[] evaluateFitness(SimulationEngine engine);
	void modifySolutionBeforeEnd(SimulationEngine engine, SimSolution simSolution);
}
