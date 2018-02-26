package com.transyslab.simcore.mlp;

import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.tools.adapter.SimProblem;
import com.transyslab.commons.tools.mutitask.EngThread;
import org.apache.commons.configuration2.Configuration;

import java.util.Arrays;

/**
 * Created by wangyimin on 2017/12/29.
 */
public abstract class MLPProblem extends SimProblem {
	protected Configuration config;
	InterConstraints interConstraints;
	double[] ob_paras;
	public MLPProblem(){ }
	public MLPProblem(String masterFileName){
		initProblem(masterFileName);
	}

	@Override
	public void initProblem(String masterFileName) {
		this.config = ConfigUtils.createConfig(masterFileName);

		//parsing
		String obParaStr = config.getString("obParas");
		String[] parasStrArray = obParaStr.split(",");
		ob_paras = new double[parasStrArray.length];
		for (int i = 0; i<parasStrArray.length; i++) {
			ob_paras[i] = Double.parseDouble(parasStrArray[i]);
		}

		interConstraints = new InterConstraints(ob_paras[5],ob_paras[4],ob_paras[0],ob_paras[1],ob_paras[2]);


		//设置问题规模
		setNumberOfVariables(5);
		setNumberOfObjectives(1);
		setNumberOfConstraints(0);

		//设置边界值
		double kjUpper = ob_paras[5];
		double kjLower = ob_paras[4];
		setLowerLimit(Arrays.asList(new Double[]{kjLower, 0.0025, 0.0, 0.0, 1.0}));
		setUpperLimit(Arrays.asList(new Double[]{kjUpper, 40.0, 10.0, 10.0, 10.0}));

		prepareEng(masterFileName,Integer.parseInt(config.getString("numOfEngines")));
	}

	@Override
	protected EngThread createEngThread(String name, String masterFileDir) {
		return  new EngThread(name,masterFileDir);
	}

	public Configuration getConfig(){
		checkConfig();
		return config;
	}

	public void checkConfig(){
		if (config==null)
			System.err.println("this problem has no config info.");
	}
}
