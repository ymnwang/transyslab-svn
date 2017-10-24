package com.transyslab.experiments;

import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPLink;

/**
 * Created by WangYimin on 2017/9/20.
 */
public class FD {
	public static void main(String[] args) {
		MLPEngine mlpEngine = new MLPEngine("src/main/resources/demo_neihuan/scenario2/FD.properties");
		mlpEngine.getSimParameter().setLCDStepSize(2.0);
		mlpEngine.loadFiles();
		MLPLink launchLink = (MLPLink) mlpEngine.getNetwork().findLink(112);
		double[] fullParas = new double[] {0.4633, 21.7950, 0.1765, 120/3.6, 48.42777377001352,0.5259902833066845,8.940854882903562,6.885166468931501};
		double[] speed = new double[] {15, 2, 20};
		double[] time = new double[] {0, 86400};
		int demand = 43200;
		for (int i = 0; i < 10; i++) {
			if (i!=9)
				continue;
			mlpEngine.setParas(fullParas);
			launchLink.generateInflow(demand,speed,time,launchLink.getStartSegment().getLanes(),112);
			System.out.println("generates: " + launchLink.countHoldingInflow());
			mlpEngine.run();
			System.out.println("remains: " + launchLink.countHoldingInflow());
			demand += 24000;
		}
	}
}
