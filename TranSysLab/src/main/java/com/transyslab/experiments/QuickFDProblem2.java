package com.transyslab.experiments;

import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.commons.tools.mutitask.SimulationConductor;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPLoop;
import com.transyslab.simcore.mlp.MLPNetwork;
import com.transyslab.simcore.mlp.MicroCharacter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by WangYimin on 2018/3/28.
 */
public class QuickFDProblem2 extends FDProblem{
	static boolean GENERALIZE = true;
	@Override
	public void setProblemBoundary() {
		setNumberOfVariables(2);
		setNumberOfObjectives(1);
		setNumberOfConstraints(0);

		setLowerLimit(Arrays.asList(new Double[]{0.2, 0.0}));
		setUpperLimit(Arrays.asList(new Double[]{10.0, 2.0}));
	}

	@Override
	protected SimulationConductor createConductor() {
		return new FDConductor() {
			@Override
			public void modifyEngineBeforeStart(SimulationEngine engine, SimSolution simSolution) {
				double[] var = simSolution.getInputVariables();
				((MLPEngine) engine).getSimParameter().setLCDStepSize(0.0);
				((MLPEngine) engine).getSimParameter().setLCBuffTime(var[0]);
				((MLPEngine) engine).getSimParameter().setLCSensitivity(var[1]);
			}
			@Override
			public double[] evaluateFitness(SimulationEngine engine) {
				double ans = 0.0;
				List<MicroCharacter> realSPD = ((MLPEngine)engine).getEmpMicroMap().get("det2");
				double startSec = 900, endSec = 8100, period = 600;
				for (double i = startSec; i < endSec; i+=period) {
					final double ft = i, tt = i+period;
					double[] real = realSPD.stream().filter(e -> e.getDetTime()>ft && e.getDetTime()<=tt)
							.mapToDouble(MicroCharacter::getSpeed).toArray();
					double[] sim = ((MLPNetwork)engine.getNetwork()).rawSectionDataFilter("det2",ft,tt, MLPLoop.SPEED)
							.stream().mapToDouble(Double::doubleValue).toArray();
					if (real!=null) {
						if (sim!=null)
							ans += GENERALIZE ?
									((double) real.length)/((double) realSPD.size()) * FitnessFunction.evaKS(sim,real,true) :
									FitnessFunction.evaKS(sim,real,false);
						else
							ans += GENERALIZE ?
									((double) real.length)/((double) realSPD.size()) * 1.0 :
									real.length;
					}

				}
				return new double[]{ans};
			}
		};
	}
}
