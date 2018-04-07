package com.transyslab.experiments;

import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.commons.tools.mutitask.SimulationConductor;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPNetwork;
import com.transyslab.simcore.mlp.MacroCharacter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WangYimin on 2018/3/23.
 */
public class FDConductor implements SimulationConductor {
	@Override
	public void modifyEngineBeforeStart(SimulationEngine engine, SimSolution simSolution) {
		double[] var = simSolution.getInputVariables();
		((MLPEngine) engine).getSimParameter().setLCPara(new double[]{var[0],var[1]});
		((MLPEngine) engine).getSimParameter().setLCDStepSize(0.0);
		((MLPEngine) engine).getSimParameter().setLCBuffTime(var[2]);
		((MLPEngine) engine).getSimParameter().setLCSensitivity(var[3]);
	}

	@Override
	public boolean checkStatusBeforeEvaluate(SimulationEngine engine) {
		return true;
	}

	@Override
	public double[] evaluateFitness(SimulationEngine engine) {
		double binStart = 0.001;
		double binStep = 0.002;
		double nBins = 40;

		List<MacroCharacter> det2Sim = engine.getSimMap().get("lane18101");
		det2Sim.addAll(engine.getSimMap().get("lane18102"));
		det2Sim.addAll(engine.getSimMap().get("lane18103"));
		List<double[]> simQList = new ArrayList<>();
		for (double b = binStart; b < binStart + binStep*nBins; b += binStep) {
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
		for (double b = binStart; b < binStart + binStep*nBins; b += binStep) {
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
}
