package com.transyslab.experiments;

import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.adapter.SimProblem;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.commons.tools.mutitask.EngThread;
import com.transyslab.commons.tools.mutitask.SimulationConductor;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPParameter;
import com.transyslab.simcore.mlp.MLPProblem;
import com.transyslab.simcore.mlp.MacroCharacter;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by WangYimin on 2017/12/12.
 */
public class FDProblem extends MLPProblem{

	public FDProblem(){	}

	@Override
	protected SimulationConductor createConductor() {
		return new SimulationConductor() {
			@Override
			public void modifyEngineBeforeStart(SimulationEngine engine, SimSolution simSolution) {
				double[] var = simSolution.getInputVariables();
				((MLPEngine)engine).alterEngineFreeParas(Arrays.copyOfRange(var,0,4));
				((MLPEngine) engine).getSimParameter().setLCDStepSize(2.0);
				((MLPEngine) engine).getSimParameter().setLCBuffTime(var[4]);
			}

			@Override
			public boolean checkStatusBeforeEvaluate(SimulationEngine engine) {
				return true;
			}

			@Override
			public double[] evaluateFitness(SimulationEngine engine) {
				double binStart = 0.006;
				double binStep = 0.0044;
				double nBins = 36;

				List<MacroCharacter> det2Sim = engine.getSimMap().get("det2");
				List<double[]> simQList = new ArrayList<>();
				for (double b = binStart; b <= binStart + binStep*(nBins-1); b += binStep) {
					double lower = b - 0.5*binStep;
					double upper = b + 0.5*binStep;
					double[] qs = det2Sim.stream()
									.filter(r -> r.getKmDensity()/1000.0 > lower && r.getKmDensity()/1000.0 <= upper)
									.mapToDouble(r -> r.getHourFlow()/3600.0)
									.toArray();
					simQList.add(qs);
				}

				List<MacroCharacter> det2Emp = engine.getEmpMap().get("det2");
				List<double[]> empQList = new ArrayList<>();
				for (double b = binStart; b <= binStart + binStep*(nBins-1); b += binStep) {
					double lower = b - 0.5*binStep;
					double upper = b + 0.5*binStep;
					double[] qs = det2Emp.stream()
							.filter(r -> r.getKmDensity()/1000.0 > lower && r.getKmDensity()/1000.0 <= upper)
							.mapToDouble(r -> r.getHourFlow()/3600.0)
							.toArray();
					empQList.add(qs);
				}

				double nSamples = empQList.stream().mapToDouble(a -> a.length).sum();
				double fitness = 0.0;
				for (int i = 0; i < nBins; i++) {
					double[] simArry = simQList.get(i);
					double[] empArry = empQList.get(i);
					if (simArry.length == 0)
						fitness += empArry.length / nSamples * 1.0;
					else if (empArry.length == 0)
						fitness += 0.0;
					else
						fitness += empArry.length / nSamples * FitnessFunction.evaKSDistance(simArry, empArry);
				}
				System.out.println(Thread.currentThread().getName() + " returned " + fitness);
				return new double[] {fitness};
			}

			@Override
			public void modifySolutionBeforeEnd(SimulationEngine engine, SimSolution simSolution) {

			}
		};
	}
}
