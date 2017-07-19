package com.transyslab.experiments;

import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.optimizer.SchedulerThread;
import com.transyslab.simcore.EngThread;
import com.transyslab.simcore.mlp.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by WangYimin on 2017/7/18.
 * ksʵ��
 */

public class EXP1 extends EngThread {
	public EXP1(String thread_name, TaskCenter task_center, String masterFileDir) {
		super(thread_name, task_center, masterFileDir);
	}

	@Override
	public double[] worksUnder(double[] paras) {
		MLPEngine mlpEngine = (MLPEngine) engine;

		//��ʼ������Ĺ̶�������ʱ�䣩
		mlpEngine.resetEngine();
		double[] paras1 = new double[]{0.5122,20.37,0.1928};
		// xc,alpha,beta,gama1,gama2
		/*double[] paras2 = new double[5];
		for(int i =0;i<paras.length;i++){
			if(i<3)
				paras1[i] = paras[i];
			else
				paras2[i-3] = paras[i];
		}*/
		MLPParameter mlp_paras = mlpEngine.getSimParameter();
		double ts = mlp_paras.genSolution2(paras1, paras[0]);
		//�����Ż�����
		mlpEngine.setObservedParas(paras1);
		double[] orgParas2 = new double[6];
		orgParas2[0] = ts;
		orgParas2[1] = paras[0];
		orgParas2[4] = paras[3];
		orgParas2[5] = paras[4];
		orgParas2[2] = paras[1];
		orgParas2[3] = paras[2];
		//[0]ts, [1]xc, [2]alpha, [3]beta, [4]gamma1, [5]gamma2
		mlpEngine.setOptParas(orgParas2);

		//����fitness fun�ı���
		double calStep = 300;
		double caltime = calStep+900;
		int sampleSize = (int) ((mlpEngine.getSimClock().getDuration()-900) / calStep);
		double []  simTrT = new double [sampleSize];
		double []  simSpeed = new double [sampleSize];
		double []  simLinkFlow = new double [sampleSize];
		int idx = 0;
		//TODO: ��ȡ�� ��ͳ�ƹ��ܼ��ɵ�Network�£�EngThread����Ҫ����mlpNetwork����, �Լ�simulationLoop
		//���з��棬��ʱ�������ͳ��
		while(mlpEngine.simulationLoop()>=0) {
			double now = mlpEngine.getSimClock().getCurrentTime();
			if (now>=caltime) {
				List<Double> trTlist = mlpEngine.getMlpNetwork().mlpLink(0).tripTime;
				trTlist.clear();

				//˲ʱƽ�������ٶ�
				/*double avg_ExpSpeed = 0.0;
				if (!mlp_network.mlpLink(0).hasNoVeh(false)) {
					int count = 0;
					double sum = 0.0;
					for (JointLane JL : mlp_network.mlpLink(0).jointLanes) {
						for (MLPLane LN : JL.lanesCompose) {
							for (MLPVehicle Veh : LN.vehsOnLn) {
								if (Veh.VirtualType_ == 0) {
									sum += (Veh.Displacement() - Veh.DSPEntrance)  /  (now - Veh.TimeEntrance);
									count += 1;
								}
							}
						}
					}
					avg_ExpSpeed = sum / count;
				}
				simSpeed[idx] = avg_ExpSpeed;*/

				//��Ȧ���ص��ٶ�
				simSpeed[idx] = mlpEngine.getMlpNetwork().sectionMeanSpd("det2", caltime-calStep, caltime)*3.6;

				caltime += calStep;
				idx += 1;
			}

		}
		/*
		double[][] realLoopDetect =(double[][]) empData;
		double [] realSpeed = new double [realLoopDetect.length];
		for (int k = 0; k < realLoopDetect.length; k++) {
			realSpeed[k] = realLoopDetect[k][0];
		}
		double[] tmpSim = new double[simSpeed.length-4];
		double[] tmpReal = new double[simSpeed.length-4];
		System.arraycopy(simSpeed, 4, tmpSim, 0, simSpeed.length-4);
		System.arraycopy(realSpeed, 4, tmpReal, 0, simSpeed.length-4);*/
		double[] realLoopDetect = mlpEngine.getEmpData();
		// ������ݳ���-1����+2
		double[] results = new double[realLoopDetect.length+1];
		// һ�β��
		double fitnessVal = FitnessFunction.evaKSDistance(simSpeed, realLoopDetect);//ADFullerTestPy.seriesDiff(simSpeed,1)
		results[0] = fitnessVal;
		System.arraycopy(simSpeed, 0, results, 1, simSpeed.length);
		return results;
	}

	public static void main(String[] args) {
		TaskCenter taskCenter = new TaskCenter();

		SchedulerThread scheduler = new SchedulerThread("scheduler",taskCenter) {
			@Override
			public void run() {
				//���Բ������봦
				Task testingTask = dispatch(new double[]{45.50056, 0.92191446, 7.792739, 1.6195029, 0.6170239});
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
