package com.transyslab.commons.tools.adapter;

import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.simcore.mlp.MLPParameter;
import com.transyslab.simcore.mlp.MacroCharacter;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * 默认的MLP参数优化问题。
 * Created by WangYimin on 2017/10/27.
 */
public class DefaultMLPProblem extends SimProblem {

	private double[] p;

	public DefaultMLPProblem(String masterFileDir){
		initProblem(masterFileDir);
	}

	protected void initProblem(String masterFileDir){
		Configuration config = ConfigUtils.createConfig(masterFileDir);

		//parsing
		double simStepSize = Double.parseDouble(config.getString("timeStep"));
		int numOfEngines = Integer.parseInt(config.getString("numOfEngines"));
		String obParaStr = config.getString("obParas");
		String[] parasStrArray = obParaStr.split(",");
		double[] ob_paras = new double[parasStrArray.length];
		for (int i = 0; i<parasStrArray.length; i++) {
			ob_paras[i] = Double.parseDouble(parasStrArray[i]);
		}
		String PartitionStr = config.getString("ObjectivePartition");
		String[] PartitionStrArray = PartitionStr.split(",");
		p = new double[PartitionStrArray.length];
		for (int i = 0; i<PartitionStrArray.length; i++) {
			p[i] = Double.parseDouble(PartitionStrArray[i]);
		}

		double qmax = ob_paras[0], vfree = ob_paras[1], kjam = ob_paras[2];
		double xcLower = MLPParameter.xcLower(kjam, qmax, simStepSize);
		double rupper = MLPParameter.rUpper(10, vfree, kjam, qmax);
		double[] plower = new double[]{xcLower+1E-5,1e-5,0.0,0.0};
		double[] pupper = new double[]{200.0, rupper-1e-5, 10.0, 10.0};

		List<Double> lowerLimit;
		List<Double> upperLimit;
		Double[] doubleArray = ArrayUtils.toObject(plower);
		lowerLimit = Arrays.asList(doubleArray);
		doubleArray = ArrayUtils.toObject(pupper);
		upperLimit =  Arrays.asList(doubleArray);

		setName("Default MLP Parameters Optimization Problem");
		setNumberOfConstraints(0);//约束已在SIMEngine内部处理，所以此处为无约束问题。
		setNumberOfVariables(4);
		setNumberOfObjectives(1);//已在内部组合为单目标优化
		setLowerLimit(lowerLimit);
		setUpperLimit(upperLimit);

		prepareEng(masterFileDir,numOfEngines);
	}

	@Override
	public synchronized double[] evaluate(HashMap simMap, HashMap empMap) {

		if (simMap != null && empMap != null) {

			List<Double> resultList = new ArrayList<>();

			empMap.forEach((k,v) -> {
				List<MacroCharacter> records = (List<MacroCharacter>) simMap.get(k);
				if (records != null && !records.isEmpty()) {

					//transfer sim results
					List<double[]> simDetection = MacroCharacter.transfer(records);

					//transfer emp results
					List<double[]> empDetection = MacroCharacter.transfer((List<MacroCharacter>) v);

					Double tmp = 0.0;
					for (int i = 0; i < 3; i++)
						tmp += p[i]*FitnessFunction.evaRNSE(simDetection.get(i), empDetection.get(i));
					resultList.add(tmp);
				}
			});

			if (!resultList.isEmpty()) {
				double nObj = (double) resultList.size();
				double avgFitness = resultList.stream().mapToDouble(e->e/nObj).sum();
				return new double[] {avgFitness};
			}
		}

		return new double[] {Double.POSITIVE_INFINITY};
	}

}
