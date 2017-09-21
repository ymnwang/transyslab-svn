package com.transyslab.experiments;

import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPLink;

/**
 * Created by WangYimin on 2017/9/20.
 */
public class FD {
	public static void main(String[] args) {
		MLPEngine mlpEngine = new MLPEngine("src/main/resources/demo_neihuan/scenario2/FD.properties");
		mlpEngine.loadFiles();
		MLPLink launchLink = (MLPLink) mlpEngine.getNetwork().findLink(112);
		double[] fullParas = new double[] {0.4633, 21.7950, 0.1765, 120/3.6, 32.92910283972989, 0.191151120786746, 0.04481781290131005, 1.5857846782395244};
		double[] speed = new double[] {15, 2, 20};
		double[] time = new double[] {0, 86400};
		int demand = 43200;
		for (int i = 0; i < 10; i++) {
			mlpEngine.resetBeforeSimLoop();
			mlpEngine.setParas(fullParas);
			launchLink.generateInflow(demand,speed,time,launchLink.getStartSegment().getLanes(),112);
			System.out.println("generates: " + launchLink.countHoldingInflow());
			mlpEngine.run(0);
			System.out.println("remains: " + launchLink.countHoldingInflow());
			demand += 24000;
		}
	}
}
