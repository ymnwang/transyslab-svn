package com.transyslab.experiments;

import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.optimizer.SchedulerThread;
import com.transyslab.roadnetwork.Lane;
import com.transyslab.simcore.EngThread;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPLink;
import com.transyslab.simcore.mlp.MLPNetwork;
import com.transyslab.simcore.mlp.MacroCharacter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by WangYimin on 2017/8/12.
 * ODʵ��
 */
public class ODEngThread extends EngThread {

	double periodEndTime;

	public ODEngThread(String thread_name, TaskCenter task_center, String masterFileDir) {
		super(thread_name, task_center, masterFileDir);
	}

	@Override
	public void run() {
		//worker�̵߳����з�ʽ
		//��ʵ���У��������ָ���������������У����ڴ˳�ʼ������reset
		//����·���ļ�
		engine.loadFiles();
		//��ʼ������
		engine.resetBeforeSimLoop();

		//�����̹߳ҵ�TC�п�ʼѭ������
		goToWork(taskCenter, true);
	}

	//worker�̵߳�fitness����
	@Override
	public double[] worksUnder(double[] paras) {
		MLPEngine mlpEngine = (MLPEngine) engine;

		//����������Լ�ͳ�ƽ��������״̬����
		mlpEngine.getMlpNetwork().clearInflows();
		mlpEngine.getMlpNetwork().clearSecStat();
		mlpEngine.getMlpNetwork().clearLinkStat();

		//���������������ʱ��+OD
		processParas(paras);

		//�ⲿ����simulationLoop��ִ��
		double now = mlpEngine.getSimClock().getCurrentTime();
		while (now <= periodEndTime) {
			mlpEngine.simulationLoop();
			now = mlpEngine.getSimClock().getCurrentTime();
		}

		//TODO: ���fitness��� ����ʱ�����ͳ�Ƽ����û�����
		List<MacroCharacter> records = mlpEngine.getMlpNetwork().getSecStatRecords("det2");
		return records==null ? null : records.stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
	}

	@Override
	public void dismiss() {
		((MLPEngine) engine).close();
	}

	public void processParas(double[] paras) {

		//����������ʽ
		//paras = {periodEndTime, fLinkId_1, tLinkId_1, demand_1, ... fLinkId_n, tLinkId_n, demand_n }

		MLPNetwork mlpNetwork = ((MLPEngine) engine).getMlpNetwork();
		double[] speed = {15, 2, 20};//�ѱ궨Ĭ��ֵ
		double[] time = {periodEndTime, paras[0]};
		periodEndTime = paras[0];
		for (int i = 1; i < paras.length - 2; i++) {
			MLPLink launchLink = mlpNetwork.findLink((int)paras[i]);
			List<Lane> lanes = launchLink.getStartSegment().getLanes();
			launchLink.generateInflow((int) paras[i+2], speed, time, lanes, (int) paras[i+1]);
		}
	}

	public static void main(String[] args) {
		//ʵ����TC
		TaskCenter taskCenter = new TaskCenter();

		//TODO: ����߼����������齫�ⲿ�ִ����ƶ���ODEstimator extends Scheduler�����ֻ��Ϊ��ʾ
		//����ʵ����̣�ʵ���������������߳�
		SchedulerThread scheduler = new SchedulerThread("scheduler",taskCenter) {
			//ʵ����̵Ķ���
			@Override
			public void run() {
				List<Task> taskList = new ArrayList<>();

				//TODO: OD���ƹ���
				//��һ�׶�
				taskList.clear();
				//�ɷ�����
				taskList.add(dispatch(new double[]{60, 162, 162, 60}, "Eng1"));
				taskList.add(dispatch(new double[]{60, 162, 162, 60}, "Eng2"));
				taskList.add(dispatch(new double[]{60, 162, 162, 60}, "Eng3"));
				//ȡ�ؽ��
				for (int i = 0; i < 3; i++) {
					System.out.println(Arrays.toString(taskList.get(i).getOutputs()));
				}
				//�ڶ��׶�
				taskList.clear();
				taskList.add(dispatch(new double[]{120, 162, 162, 60}, "Eng1"));
				taskList.add(dispatch(new double[]{120, 162, 162, 60}, "Eng2"));
				taskList.add(dispatch(new double[]{120, 162, 162, 60}, "Eng3"));
				for (int i = 0; i < 3; i++) {
					System.out.println(Arrays.toString(taskList.get(i).getOutputs()));
				}

				//��ɢ���й����߳�
				dismissAllWorkingThreads();
			}
		};
		scheduler.start();

		//ʵ���������������߳�
		//TODO: ��ʵ����Ҫȷ���������߳�������
		for (int i = 0; i < 4; i++) {
			new ODEngThread("Eng" + (i+1), taskCenter, "src/main/resources/demo_neihuan/scenario2/neverEnd.properties").start();
		}
	}
}
