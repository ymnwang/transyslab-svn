package com.transyslab.commons.tools.adapter;

import com.transyslab.commons.tools.mutitask.SimulationConductor;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskGiver;
import com.transyslab.commons.tools.mutitask.EngThread;
import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

/**
 * 适配JMetal架构，以便使用标准优化工具进行参数优化。
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
            //标准引擎的初始化与参数的设置
            EngThread engThread = createEngThread("eng" + i, masterFileDir);
            engThread.setSimConductor(createConductor());
            engThread.assignTo(this);
            engThread.start();
        }

    }

    public void closeProblem() {
        dismissAllWorkingThreads();
    }

    protected abstract EngThread createEngThread(String name, String masterFileDir);

    protected abstract SimulationConductor createConductor();
}
