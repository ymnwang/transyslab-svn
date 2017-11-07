package com.transyslab.commons.tools.adapter;

import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskGiver;
import com.transyslab.commons.tools.mutitask.EngThread;
import com.transyslab.roadnetwork.Parameter;
import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

import java.util.HashMap;

/**
 * ����JMetal�ܹ����Ա�ʹ�ñ�׼�Ż����߽��в����Ż���
 * Created by WANG YiMin on 2017/10/26.
 */
public abstract class SimProblem extends AbstractDoubleProblem implements TaskGiver {
    private TaskCenter taskCenter;

    public SimProblem() {
        taskCenter = new TaskCenter();
    }

    @Override
    public void evaluate(DoubleSolution doubleSolution) {
        dispatch((SimSolution) doubleSolution);
    }

    @Override
    public DoubleSolution createSolution() {
        return new SimSolution(this);
    }

    @Override
    public TaskCenter getTaskCenter() {
        return taskCenter;
    }

    public void prepareEng(String masterFileDir, int numOfEngines) {
        for (int i = 0; i < numOfEngines; i++) {
            //��׼����ĳ�ʼ�������������
            EngThread engThread = new EngThread("eng"+i, masterFileDir);
            alterOtherParameters(engThread.getParameters());
            engThread.assignTo(this);
            engThread.start();
        }

    }

    public void closeProblem() {
        dismissAllWorkingThreads();
    }

    /**
     * ���ۺ�����Ŀ�꺯����
     * ��ͨ�����ش˺���ʵ��Ŀ�꺯�����滻��
     * Ϊ�˱�֤�̰߳�ȫ��Ӧ�ü���synchronized
     * @param simMap �����������������ܺ�۽����
     * @param empMap ʵ�����ݣ������ܺ�۽����
     * @return ����ֵ��
     */
    public abstract double[] evaluate(HashMap simMap, HashMap empMap);

    /**
     * �������������������ļ����趨�Ĳ�����
     * Ԥ���ӿڣ����ⲿ��д
     * @param parameter ���������
     */
    protected void alterOtherParameters(Parameter parameter) {

    }

}
