package com.transyslab.experiments;

import com.mathworks.toolbox.javabuilder.MWException;
import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.commons.tools.mutitask.EngThread;
import com.transyslab.commons.tools.mutitask.SimulationConductor;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPProblem;
import com.transyslab.simcore.mlp.MacroCharacter;
import matlabfunctions.MatlabFunctions;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * 默认的MLP参数优化问题。
 * Created by WangYimin on 2017/10/27.
 */
public class DefaultMLPProblem extends MLPProblem {

	public DefaultMLPProblem(){ }

	public DefaultMLPProblem(String masterFileName) {
		super(masterFileName);
	}

	@Override
	protected SimulationConductor createConductor() {
		return new SimulationConductor() {

			@Override
			public void modifyEngineBeforeStart(SimulationEngine engine, SimSolution simSolution) {
				double[] var = simSolution.getInputVariables();
				((MLPEngine)engine).alterEngineFreeParas(Arrays.copyOfRange(var,0,4));
				((MLPEngine) engine).getSimParameter().setLCBuffTime(var[4]);
			}

			@Override
			public boolean checkStatusBeforeEvaluate(SimulationEngine engine) {
				return true;
			}

			@Override
			public double[] evaluateFitness(SimulationEngine engine) {
				HashMap simMap = engine.getSimMap();
				HashMap empMap = engine.getEmpMap();

				if (simMap != null && empMap != null) {

					List<Double> speedFitness = new ArrayList<>();
					List<Double> flowFitness = new ArrayList<>();

					empMap.forEach((k,v) -> {
						List<MacroCharacter> records = (List<MacroCharacter>) simMap.get(k);
						if (records != null && !records.isEmpty()) {

							double[] simSpeed = MacroCharacter.select(records, MacroCharacter.SELECT_SPEED);
							double[] empSpeed = MacroCharacter.select((List<MacroCharacter>) v, MacroCharacter.SELECT_SPEED);

							Double tmp = FitnessFunction.evaMAPE(simSpeed, empSpeed);

							speedFitness.add(tmp);

							double[] simFlow = MacroCharacter.select(records,MacroCharacter.SELECT_FLOW);
							double[] empFlow = MacroCharacter.select((List<MacroCharacter>) v,MacroCharacter.SELECT_FLOW);
							flowFitness.add(FitnessFunction.evaMAPE(simFlow,empFlow));

						}
					});

					if (!speedFitness.isEmpty()) {
						double nObj = (double) speedFitness.size();
						double avgSpdFitness = speedFitness.stream().mapToDouble(e->e/nObj).sum();
						double avgFlowFitness = flowFitness.stream().mapToDouble(e->e/nObj).sum();
						double output = avgFlowFitness;
						System.out.println("fitness = " + output);
						return new double[] {output};
					}
				}

				return new double[] {Double.POSITIVE_INFINITY};
			}

			@Override
			public void modifySolutionBeforeEnd(SimulationEngine engine, SimSolution simSolution) {

			}
		};
	}
}
