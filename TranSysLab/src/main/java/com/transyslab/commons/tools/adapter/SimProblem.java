package com.transyslab.commons.tools.adapter;

import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskGiver;
import com.transyslab.commons.tools.mutitask.EngThread;
import com.transyslab.roadnetwork.Parameter;
import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

import java.util.HashMap;

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
     * 评价函数（目标函数）
     * 可通过重载此函数实现目标函数的替换。
     * 为了保证线程安全，应该加上synchronized
     * @param simMap 仿真输出结果（流速密宏观结果）
     * @param empMap 实测数据（流速密宏观结果）
     * @return 评价值。
     */
    public abstract double[] evaluate(HashMap simMap, HashMap empMap);

    /**
     * 设置其他不能在配置文件中设定的参数。
     * 预留接口，供外部重写
     * @param parameter 仿真参数类
     */
    protected void alterOtherParameters(Parameter parameter) {

    }

}
