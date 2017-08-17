package com.transyslab.experiments;

import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.TimeMeasureUtil;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskWorker;
import com.transyslab.commons.tools.optimizer.SchedulerThread;
import com.transyslab.simcore.EngThread;
import com.transyslab.simcore.mlp.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by WangYimin on 2017/7/18.
 * ks实验
 */

public class EXP1 extends EngThread {
	public EXP1(String thread_name, TaskCenter task_center, String masterFileDir) {
		super(thread_name, task_center, masterFileDir);
	}

	@Override
	public double[] worksUnder(double[] paras) {
		TimeMeasureUtil timer = new TimeMeasureUtil();
		timer.tic();
		MLPEngine mlpEngine = (MLPEngine) engine;

		//初始化引擎的固定参数（时间）
		mlpEngine.resetEngine();
		mlpEngine.setParas(new double[]{0.5122,20.37,0.1928}, paras);

		//设置fitness fun的变量
		double calStep = 300;
		double caltime = calStep+900;
		int sampleSize = (int) ((mlpEngine.getSimClock().getDuration()-900) / calStep);
		double []  simTrT = new double [sampleSize];
		double []  simSpeed = new double [sampleSize];
		double []  simLinkFlow = new double [sampleSize];
		int idx = 0;
		//TODO: 待取消 将统计功能集成到Network下，EngThread不需要访问mlpNetwork对象, 以及simulationLoop
		//运行仿真，定时进行输出统计
		while(mlpEngine.simulationLoop()>=0) {
			double now = mlpEngine.getSimClock().getCurrentTime();
			if (now>=caltime) {

				//线圈检测地点速度
				simSpeed[idx] = mlpEngine.getMlpNetwork().sectionMeanSpd("det2", caltime-calStep, caltime)*3.6;

				caltime += calStep;
				idx += 1;
			}

		}
		System.out.println(Arrays.toString(simSpeed));
		List<MacroCharacter> records = mlpEngine.getMlpNetwork().getSecStatRecords("det2");
		double[] test = records.stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
		System.out.println(Arrays.toString(test));
		double[] realLoopDetect = mlpEngine.getEmpData();
		// 差分数据长度-1，故+2
		double[] results = new double[realLoopDetect.length+1];
		// 一次差分
		double fitnessVal = FitnessFunction.evaKSDistance(simSpeed, realLoopDetect);//ADFullerTestPy.seriesDiff(simSpeed,1)
		results[0] = fitnessVal;
		System.arraycopy(simSpeed, 0, results, 1, simSpeed.length);
		System.out.println("time used :" + timer.toc());
		return results;
	}

	public static void main(String[] args) {
		TaskCenter taskCenter = new TaskCenter();

		SchedulerThread scheduler = new SchedulerThread("scheduler",taskCenter) {
			@Override
			public void run() {
				//测试参数输入处
				Task testingTask = dispatch(new double[]{45.50056, 0.92191446, 7.792739, 1.6195029, 0.6170239}, TaskWorker.ANY_WORKER);
				System.out.println(Arrays.toString(testingTask.getOutputs()));
				dismissAllWorkingThreads();
				System.out.println("All workers are killed.");
			}
		};
		scheduler.start();

		EXP1 exp1 = new EXP1("worker1", taskCenter, "src/main/resources/demo_neihuan/scenario2/master.properties");
		exp1.start();
	}

}
