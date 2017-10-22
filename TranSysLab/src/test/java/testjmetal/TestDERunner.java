package testjmetal;

import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.optimizer.DEBuilder;
import com.transyslab.commons.tools.optimizer.SimulationEvaluator;
import com.transyslab.commons.tools.optimizer.SimulationProblem;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.EngThread;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPParameter;
import com.transyslab.simcore.mlp.MacroCharacter;
import org.apache.commons.lang3.ArrayUtils;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.singleobjective.differentialevolution.DifferentialEvolutionBuilder;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.MultithreadedSolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yali on 2017/10/18.
 */
public class TestDERunner {

	/**
	 *  Usage: java org.uma.jmetal.runner.singleobjective.DifferentialEvolutionRunner [cores]
	 */
	public static void main(String[] args) throws Exception {

		DoubleProblem problem;
		Algorithm<DoubleSolution> algorithm;
		DifferentialEvolutionSelection selection;
		DifferentialEvolutionCrossover crossover;
		SolutionListEvaluator<DoubleSolution> evaluator ;

		TaskCenter taskCenter = new TaskCenter();
		int numOfVariables = 4;
		int repeatedTimes = 1;
		double kjam = 0.1765, qmax = 0.4633, vfree = 21.7950,deltat = 0.2;
		double xcLower = MLPParameter.xcLower(kjam, qmax,deltat);
		double rupper = MLPParameter.rUpper(10,vfree,kjam,qmax);
		double[] plower = new double[]{xcLower+0.00001,1e-5,0.0,0.0};
		double[] pupper = new double[]{200.0, rupper-1e-5, 10.0, 10.0};
		List<Double> lowerLimit;
		List<Double> upperLimit;
		Double[] doubleArray = ArrayUtils.toObject(plower);
		lowerLimit = Arrays.asList(doubleArray);
		doubleArray = ArrayUtils.toObject(pupper);
		upperLimit =  Arrays.asList(doubleArray);
		problem = new SimulationProblem(numOfVariables,1,0,
				lowerLimit,upperLimit);

		evaluator = new SimulationEvaluator("ThreadManager", taskCenter);
		crossover = new DifferentialEvolutionCrossover(0.5, 0.7, "rand/1/bin") ;
		selection = new DifferentialEvolutionSelection();

		DEBuilder builder = new DEBuilder(problem);
		algorithm = builder
				.setCrossover(crossover)
				.setSelection(selection)
				.setSolutionListEvaluator(evaluator)
				.setMaxEvaluations(8000)
				.setPopulationSize(20)
				.build() ;
		//启动线程
		for (int i = 0; i < 20; i++) {
			new EngThread("Eng" + i, taskCenter, "src/main/resources/demo_neihuan/scenario2/kscalibration.properties") {
				@Override
				public double[] worksUnder(double[] paras) {
					MLPEngine mlpEngine = (MLPEngine) engine;
					mlpEngine.getSimParameter().setLCDStepSize(2.0);
					double[][] simSpeeds = new double[repeatedTimes ][];
					int[] vhcCount = new int[repeatedTimes];
					for(int i = 0;i<repeatedTimes;i++){
						//仿真过程
						if(mlpEngine.runWithPara(paras) == Constants.STATE_ERROR_QUIT){
							return new double[]{Integer.MAX_VALUE};
						}
						//获取特定结果
						List<MacroCharacter> records = mlpEngine.getMlpNetwork().getSecStatRecords("det2");
						simSpeeds[i] = records.stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
						vhcCount[i] = mlpEngine.countOnHoldVeh();
					}
					//评价结果
					int col = simSpeeds[0].length;
					// 剩余发车量、RMSE、最佳车速序列
					double[] result = new double[col + 2];
					//统计多次仿真的平均车速
					double[] avgSpeedRst  = new double[col];

					//统计多次仿真的平均剩余发车数
					double sum;
					for(int j=0;j<col;j++){
						sum = 0;
						for(int i=0;i<repeatedTimes;i++){
							sum += simSpeeds[i][j];
						}
						avgSpeedRst[j] = sum/repeatedTimes;
					}
					sum = 0;
					for(int i=0;i<repeatedTimes;i++){
						sum += vhcCount[i];
					}
					// TODO 总发车量
					result[0] = sum/repeatedTimes/5374.0;
					result[1] = FitnessFunction.evaRNSE(avgSpeedRst,mlpEngine.getEmpData());
					System.arraycopy(avgSpeedRst,0,result,2,col);
					return result;
				}
			}.start();
		}
		AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
				.execute() ;

		DoubleSolution solution = algorithm.getResult() ;
		long computingTime = algorithmRunner.getComputingTime() ;

		List<DoubleSolution> population = new ArrayList<>(1) ;
		population.add(solution) ;
		/*
		new SolutionListOutput(population)
				.setSeparator("\t")
				.setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
				.setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv"))
				.print();

		JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
		JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
		JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");*/

		builder.close();

	}
}
