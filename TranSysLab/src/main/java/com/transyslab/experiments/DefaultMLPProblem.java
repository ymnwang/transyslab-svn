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
	public void initProblem(String masterFileName) {
		super.initProblem(masterFileName);
		double xcLower = getXcLower();
		double[] plower = new double[]{xcLower+1E-5, 1E-5,0.0,0.0,1.0};
		double[] pupper = new double[]{200.0, 100.0, 10.0, 10.0,10.0};

		List<Double> lowerLimit;
		List<Double> upperLimit;
		Double[] doubleArray = ArrayUtils.toObject(plower);
		lowerLimit = Arrays.asList(doubleArray);
		doubleArray = ArrayUtils.toObject(pupper);
		upperLimit =  Arrays.asList(doubleArray);

		setName("Default MLP Parameters Optimization Problem");
		setNumberOfConstraints(0);//约束已在SIMEngine内部处理，所以此处为无约束问题。
		setNumberOfVariables(5);
		setNumberOfObjectives(1);//已在内部组合为单目标优化
		setLowerLimit(lowerLimit);
		setUpperLimit(upperLimit);

		prepareEng(masterFileName,Integer.parseInt(config.getString("numOfEngines")));
	}

	@Override
	protected EngThread createEngThread(String name, String masterFileDir) {
		return new EngThread(name,masterFileDir);
	}

	@Override
	protected SimulationConductor createConductor() {
		try {
			return new SimulationConductor() {

				MatlabFunctions matlabFunctions = new MatlabFunctions();

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

								try {
									matlabFunctions.displaySim(simSpeed,simFlow);
								} catch (MWException e) {
									e.printStackTrace();
								}
							}
						});

						if (!speedFitness.isEmpty()) {
							double nObj = (double) speedFitness.size();
							double avgSpdFitness = speedFitness.stream().mapToDouble(e->e/nObj).sum();
							double avgFlowFitness = flowFitness.stream().mapToDouble(e->e/nObj).sum();
							double output = (avgSpdFitness+avgFlowFitness)/2.0;
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
