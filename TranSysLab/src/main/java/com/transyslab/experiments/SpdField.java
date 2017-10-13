package com.transyslab.experiments;

import com.transyslab.commons.tools.TimeMeasureUtil;
import com.transyslab.simcore.mlp.*;
import org.encog.util.Stopwatch;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by WANG YiMin on 2017/10/12.
 */
public class SpdField {

    public static void addLoop(MLPLink mlpLink, double distanceInterval) {
        MLPNetwork mlpNetwork = (MLPNetwork) mlpLink.getNetwork();
        double currentP = 0;
        double lnkLen = mlpLink.length();
        int n = 0;
        while (currentP <= lnkLen) {
            mlpNetwork.setLoopsOnLink(String.valueOf(n), mlpLink.getId(), currentP / lnkLen);
            currentP += distanceInterval;
            n += 1;
        }
        mlpNetwork.setLoopsOnLink("det_" + mlpLink.getId() + "_" + n,
                mlpLink.getId(),
                1.0);
    }

    public static void main(String[] args) {
        //制定路径+读取文件
        MLPEngine mlpEngine = new MLPEngine("src/main/resources/demo_neihuan/scenario2/速度场测试.properties");
        mlpEngine.loadFiles();

        //运行参数设置
        double[] fullParas = MLPParameter.DEFAULT_PARAMETERS;
        mlpEngine.seedFixed = true;//强制
        mlpEngine.runningSeed = 1500613842660l;

        //生成路段虚拟线圈
        SpdField.addLoop(mlpEngine.getMlpNetwork().findLink(111), 50);
        SpdField.addLoop(mlpEngine.getMlpNetwork().findLink(112), 50);
        SpdField.addLoop(mlpEngine.getMlpNetwork().findLink(26), 50);
        SpdField.addLoop(mlpEngine.getMlpNetwork().findLink(113), 50);
        SpdField.addLoop(mlpEngine.getMlpNetwork().findLink(25), 50);

        //初始化计时器
        Stopwatch timer = new Stopwatch();
        timer.start();

        //仿真运行
        mlpEngine.runWithPara(fullParas);

        //统计发车
        System.out.println("未发车辆数：" + mlpEngine.countOnHoldVeh() + "辆");
        timer.stop();
        System.out.println("time " + timer.getElapsedMilliseconds() + " ms");

        //关闭引擎
        mlpEngine.close();
    }
}
