package com.transyslab.experiments;

import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.TimeMeasureUtil;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPLink;
import com.transyslab.simcore.mlp.MLPParameter;
import com.transyslab.simcore.mlp.MacroCharacter;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by WangYimin on 2017/9/18.
 */
public class BaseEXP {
	public static void main(String[] args) {
		MLPEngine mlpEngine = new MLPEngine("src/main/resources/demo_neihuan/scenario2/optwym.properties");
		mlpEngine.loadFiles();
		Random random = new Random();
		double[] fullParas = /*MLPParameter.DEFAULT_PARAMETERS;//*/new double[]{0.4633, 21.7950, 0.1765, 120/3.6, 68.1539877988404, 0.590727919856555, 2.86399188655377, 0.0727678252529464};
//		mlpEngine.seedFixed = true;//强制
//		mlpEngine.runningSeed = 1;
		mlpEngine.getSimParameter().setLCDStepSize(2.0);
		mlpEngine.getSimParameter().setDLower(100.0f);
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
			System.out.println("RMSE: " + FitnessFunction.evaRNSE(kmSpd, mlpEngine.getEmpData()));
		}
		mlpEngine.close();
	}
}
