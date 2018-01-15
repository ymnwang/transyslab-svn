package com.transyslab.experiments;

import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.tools.ADFullerTest;
import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.commons.tools.mutitask.EngThread;
import com.transyslab.commons.tools.mutitask.SimulationConductor;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mlp.*;
import matlabfunctions.MatlabFunctions;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.ArrayUtils;
import java.util.*;

/**
 * Created by yali on 2017/11/26.
 */
public class KSProblem extends MLPProblem {

	public KSProblem(){}

	public KSProblem(String masterFileName) {
		super(masterFileName);
	}

	@Override
	public void initProblem(String masterFileName) {
		super.initProblem(masterFileName);
		double xcLower = getXcLower();
		double rlower = getRLower(200.0);
		//'xc','r','gama1','gama2','lcb_ime'
		double[] plower = new double[]{xcLower+1E-5,rlower+1E-5,0.0,0.0,1.0};
		double[] pupper = new double[]{200.0, 100.0, 10.0, 10.0,10.0};

		List<Double> lowerLimit;
		List<Double> upperLimit;
		Double[] doubleArray = ArrayUtils.toObject(plower);
		lowerLimit = Arrays.asList(doubleArray);
		doubleArray = ArrayUtils.toObject(pupper);
		upperLimit =  Arrays.asList(doubleArray);

		setName("KS Based MLP Parameters Optimization Problem");
		setNumberOfConstraints(0);//约束已在SIMEngine内部处理，所以此处为无约束问题。
		setNumberOfVariables(5);
		setNumberOfObjectives(3);
		setLowerLimit(lowerLimit);
		setUpperLimit(upperLimit);

		prepareEng(masterFileName,Integer.parseInt(config.getString("numOfEngines")));
	}

	@Override
	protected EngThread createEngThread(String name, String masterFileDir) {
		return  new EngThread(name,masterFileDir);
	}

	@Override
	protected SimulationConductor createConductor() {
		try {
			return new SimulationConductor() {

				MatlabFunctions mlFunctions = new MatlabFunctions();
				int checkFailTimes = 0;
				boolean checkPassFlag = false;
				@Override
				public void modifyEngineBeforeStart(SimulationEngine engine, SimSolution simSolution) {
					double[] var = simSolution.getInputVariables();
					((MLPEngine)engine).alterEngineFreeParas(Arrays.copyOfRange(var,0,4));
					((MLPEngine) engine).getSimParameter().setLCDStepSize(2.0);
					((MLPEngine) engine).getSimParameter().setLCBuffTime(var[4]);
				}

				@Override
				public boolean checkStatusBeforeEvaluate(SimulationEngine engine)  {
					List<MacroCharacter> simRecords = engine.getSimMap().get("det2");
					double[] simSpeed = MacroCharacter.select(simRecords, MacroCharacter.SELECT_SPEED);
					MWNumericArray mlInput = new MWNumericArray(simSpeed);
					// 结果大小为1
					Object[] tmp;
					try {
						checkFailTimes ++;
						tmp = mlFunctions.adfullerTest(1,mlInput,1);
						double[] result = ((double[][])((MWNumericArray)tmp[0]).toDoubleArray())[0];
						if(result[0] ==1 || checkFailTimes>= Constants.REPEAT_TEST_TIMES) {
							if (result[0] == 1)
								checkPassFlag = true;
							return true;
						}
						else
							return false;
					} catch (MWException e) {
						e.printStackTrace();
						System.exit(1);
						return false;
					}
				}

				@Override
				public double[] evaluateFitness(SimulationEngine engine) {
					Map<String, List<MacroCharacter>> simMap = engine.getSimMap();
					Map<String, List<MacroCharacter>> empMap = engine.getEmpMap();
					// 重置检验计数器
					checkFailTimes = 0;
					// 检验是否通过
					if (simMap != null && empMap != null && checkPassFlag){
						List<Double> resultList = new ArrayList<>();
						List<MacroCharacter> empRecords = empMap.get("det2");
						if(empRecords == null || empRecords.isEmpty()) {
							System.out.println("Error: Can not find \"det2\"");
							return new double[]{Double.POSITIVE_INFINITY};
						}
						List<MacroCharacter> simRecords = simMap.get("det2");
						if (simRecords != null && !simRecords.isEmpty() ) {

							double[] simSpeed = MacroCharacter.select(simRecords, MacroCharacter.SELECT_SPEED);
							double[] empSpeed = MacroCharacter.select(empRecords, MacroCharacter.SELECT_SPEED);

							double ksDis = FitnessFunction.evaKSDistance(ADFullerTest.seriesDiff(simSpeed,1),
									ADFullerTest.seriesDiff(empSpeed,1));
							MLPLink tmpLink = (MLPLink)engine.getNetwork().findLink(111);
							double vhcPropotion = tmpLink.countHoldingInflow()/(double)(tmpLink.getEmitNum()+tmpLink.countHoldingInflow());
							resultList.add(ksDis);
							// 发车数量
							resultList.add(vhcPropotion);
							// 速度序列初值的AE
							resultList.add(Math.abs(simSpeed[0]-empSpeed[0]));
						}
						double[] results = resultList.stream().mapToDouble(Double::doubleValue).toArray();
						return results;
					}
					return new double[] {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
				}
				@Override
				public void modifySolutionBeforeEnd(SimulationEngine engine, SimSolution simSolution) {
					if(checkPassFlag){
						List<MacroCharacter> simRecords = engine.getSimMap().get("det2");
						double[] simSpeed = MacroCharacter.select(simRecords, MacroCharacter.SELECT_SPEED);
						simSolution.setAttribute("SimSpeed",simSpeed);
						checkPassFlag = false;
					}
				}
			};
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
}
