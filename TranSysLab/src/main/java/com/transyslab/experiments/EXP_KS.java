package com.transyslab.experiments;

import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskWorker;
import com.transyslab.commons.tools.mutitask.SchedulerThread;
import com.transyslab.commons.tools.mutitask.EngThread;
import com.transyslab.simcore.mlp.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by WangYimin on 2017/7/18.
 * KS实验
 */

public class EXP_KS extends EngThread {
	public EXP_KS(String thread_name, TaskCenter task_center, String masterFileDir) {
		super(thread_name, masterFileDir, task_center);
	}

	@Override
	public double[] worksWith(Task task) {

		MLPEngine mlpEngine = (MLPEngine) getEngine();

		//仿真过程
		mlpEngine.runWithPara(task.getInputVariables());

		//获取特定结果
		List<MacroCharacter> records = mlpEngine.getNetwork().getSecStatRecords("det2");
		double[] kmSpd = records.stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
		System.out.println(Arrays.toString(kmSpd));

		//TODO：评价结果
		double[] evaluation = kmSpd;

		return evaluation;
	}

	public static void main(String[] args) {
		TaskCenter taskCenter = new TaskCenter();

		//TODO: for demo only
		SchedulerThread scheduler = new SchedulerThread("scheduler",taskCenter) {
			@Override
			public void run() {
				//测试参数输入处
				Task testingTask = dispatch(new double[]{0.5122,20.37,0.1928,45.50056,0.92191446,7.792739,1.6195029,0.6170239}, TaskWorker.ANY_WORKER);
				System.out.println(Arrays.toString(testingTask.getObjectiveValues()));
				dismissAllWorkingThreads();
				System.out.println("All workers are killed.");
			}
		};
		scheduler.start();

		EXP_KS exp_ks = new EXP_KS("worker1", taskCenter, "src/main/resources/demo_neihuan/scenario2/master.properties");
		exp_ks.start();
	}

}
