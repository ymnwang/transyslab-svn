package com.transyslab.experiments;

import com.transyslab.simcore.mlp.MLPEngine;

/**
 * Created by WangYimin on 2017/9/18.
 */
public class BaseEXP {
	public static void main(String[] args) {
		MLPEngine mlpEngine = new MLPEngine("src/main/resources/demo_neihuan/scenario2/default.properties");
		mlpEngine.loadFiles();

		mlpEngine.close();
	}
}
