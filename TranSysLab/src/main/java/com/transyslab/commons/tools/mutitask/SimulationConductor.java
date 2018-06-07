package com.transyslab.commons.tools.mutitask;

import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.simcore.SimulationEngine;

/**
 * Created by WangYimin on 2017/11/14.
 */
public interface SimulationConductor {
	void modifyEngineBeforeStart(SimulationEngine engine, SimSolution simSolution);
	default boolean violateConstraints(SimulationEngine engine){return false;}
	default boolean needRerun(SimulationEngine engine){ return false;}
	double[] evaluateFitness(SimulationEngine engine);
	default void modifySolutionBeforeEnd(SimulationEngine engine, SimSolution simSolution){}
}
