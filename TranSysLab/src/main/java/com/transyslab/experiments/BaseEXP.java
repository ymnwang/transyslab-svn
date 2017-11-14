package com.transyslab.experiments;

import com.transyslab.simcore.mlp.MLPEngine;

/**
 * Created by WangYimin on 2017/9/18.
 */
public class BaseEXP {
	public static void main(String[] args) {
		MLPEngine mlpEngine = new MLPEngine("src/main/resources/demo_neihuan/scenario2/baseEXP.properties"){
			@Override
			public int simulationLoop() {
				System.out.println(getSimClock().getCurrentTime());
				return super.simulationLoop();
			}

			@Override
			public void loadFiles() {
				super.loadFiles();

			}

			@Override
			public void close() {
				super.close();
				System.out.println("This Engine has run " + this.countRunTimes() + " time(s)");
			}
		};
		mlpEngine.loadFiles();
		mlpEngine.repeatRun();
		mlpEngine.close();
	}
}
