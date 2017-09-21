package com.transyslab.experiments;

import com.transyslab.commons.tools.TimeMeasureUtil;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPLink;
import com.transyslab.simcore.mlp.MacroCharacter;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by WangYimin on 2017/9/18.
 */
public class BaseEXP {
	public static void main(String[] args) {
		MLPEngine mlpEngine = new MLPEngine("src/main/resources/demo_neihuan/scenario2/kscalibration_固化路径.properties");
		mlpEngine.loadFiles();
		Random random = new Random();
		double[] fullParas = new double[]{0.4633, 21.7950, 0.1765, 120/3.6, 32.92910283972989, 0.191151120786746, 0.04481781290131005, 1.5857846782395244};
		mlpEngine.seedFixed = true;//强制
		mlpEngine.runningSeed = 1500613842660l;
		TimeMeasureUtil timer = new TimeMeasureUtil();
		for (int i = 0; i < 10; i++) {
			timer.tic();
			mlpEngine.runWithPara(fullParas);
			//统计发车
			int vehHoldCount = 0;
			for (int k = 0; k<mlpEngine.getNetwork().nLinks(); k++) {
				vehHoldCount += ((MLPLink) mlpEngine.getNetwork().getLink(k)).countHoldingInflow();
			}
			System.out.println("未发车辆数：" + vehHoldCount + "辆");
			System.out.println("time " + timer.toc() + " ms");
			List<MacroCharacter> records = mlpEngine.getNetwork().getSecStatRecords("det2");
			double[] kmSpd = records.stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
			System.out.println(Arrays.toString(kmSpd));
		}
	}
}
