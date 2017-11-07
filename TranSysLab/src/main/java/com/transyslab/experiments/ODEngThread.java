package com.transyslab.experiments;

import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.SchedulerThread;
import com.transyslab.roadnetwork.Lane;
import com.transyslab.commons.tools.mutitask.EngThread;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPLink;
import com.transyslab.simcore.mlp.MLPNetwork;
import com.transyslab.simcore.mlp.MacroCharacter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by WangYimin on 2017/8/12.
 * OD实验
 */
public class ODEngThread extends EngThread {

	double periodEndTime;

	public ODEngThread(String thread_name, TaskCenter task_center, String masterFileDir) {
		super(thread_name, masterFileDir, task_center);
	}

	@Override
	public void run() {
		//worker线程的运行方式
		//此实验中，引擎接受指定任务，且连续运行，即在此初始化后不再reset
		//加载路网文件
		engine.loadFiles();

		//工作线程挂到TC中开始循环工作
		goToWork(taskCenter, true);
	}

	//worker线程的fitness计算
	@Override
	public double[] worksWith(double[] paras, Map<Object, Object> attributes) {
		MLPEngine mlpEngine = (MLPEngine) engine;

		//清除发车表以及统计结果，其余状态保持
		mlpEngine.getNetwork().clearInflows();
		mlpEngine.getNetwork().clearSecStat();
		mlpEngine.getNetwork().clearLinkStat();

		//处理输入参数截至时间+OD
		processParas(paras);

		//外部控制simulationLoop的执行
		double now = mlpEngine.getSimClock().getCurrentTime();
		while (now <= periodEndTime) {
			mlpEngine.simulationLoop();
			now = mlpEngine.getSimClock().getCurrentTime();
		}

		//TODO: 输出fitness结果 仿真时间短于统计间隔会没有输出
		List<MacroCharacter> records = mlpEngine.getNetwork().getSecStatRecords("det2");
		return records==null ? null : records.stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
	}

	@Override
	public void onDismiss() {
		((MLPEngine) engine).close();
	}

	public void processParas(double[] paras) {

		//参数参数格式
		//paras = {periodEndTime, fLinkId_1, tLinkId_1, demand_1, ... fLinkId_n, tLinkId_n, demand_n }

		MLPNetwork mlpNetwork = ((MLPEngine) engine).getNetwork();
		double[] speed = {15, 2, 20};//已标定默认值
		double[] time = {periodEndTime, paras[0]};
		periodEndTime = paras[0];
		for (int i = 1; i < paras.length - 2; i++) {
			MLPLink launchLink = mlpNetwork.findLink((int)paras[i]);
			List<Lane> lanes = launchLink.getStartSegment().getLanes();
			launchLink.generateInflow((int) paras[i+2], speed, time, lanes, (int) paras[i+1]);
		}
	}

	public static void main(String[] args) {
		//实例化TC
		TaskCenter taskCenter = new TaskCenter();

		//TODO: 如果逻辑过长，建议将这部分代码移动至ODEstimator extends Scheduler里，这里只作为演示
		//定义实验过程，实例化并启动管理线程
		SchedulerThread scheduler = new SchedulerThread("scheduler",taskCenter) {
			//实验过程的定义
			@Override
			public void run() {
				List<Task> taskList = new ArrayList<>();

				//TODO: OD估计过程
				//第一阶段
				taskList.clear();
				//派发任务
				taskList.add(dispatch(new double[]{60, 162, 162, 60}, "Eng1"));
				taskList.add(dispatch(new double[]{60, 162, 162, 60}, "Eng2"));
				taskList.add(dispatch(new double[]{60, 162, 162, 60}, "Eng3"));
				//取回结果
				for (int i = 0; i < 3; i++) {
					System.out.println(Arrays.toString(taskList.get(i).getObjectiveValues()));
				}
				//第二阶段
				taskList.clear();
				taskList.add(dispatch(new double[]{120, 162, 162, 60}, "Eng1"));
				taskList.add(dispatch(new double[]{120, 162, 162, 60}, "Eng2"));
				taskList.add(dispatch(new double[]{120, 162, 162, 60}, "Eng3"));
				for (int i = 0; i < 3; i++) {
					System.out.println(Arrays.toString(taskList.get(i).getObjectiveValues()));
				}

				//解散所有工作线程
				dismissAllWorkingThreads();
			}
		};
		scheduler.start();

		//实例化并启动工作线程
		//TODO: 按实际需要确定数量和线程命名。
		for (int i = 0; i < 4; i++) {
			new ODEngThread("Eng" + (i+1), taskCenter, "src/main/resources/demo_neihuan/scenario2/neverEnd.properties").start();
		}
	}
}
