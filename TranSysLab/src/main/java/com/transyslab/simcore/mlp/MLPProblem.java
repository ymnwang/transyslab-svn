package com.transyslab.simcore.mlp;

import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.tools.adapter.SimProblem;
import com.transyslab.commons.tools.mutitask.EngThread;
import org.apache.commons.configuration2.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wangyimin on 2017/12/29.
 */
public abstract class MLPProblem extends SimProblem {
	protected Configuration config;
	public MLPProblem(){ }
	public MLPProblem(String masterFileName){
		initProblem(masterFileName);
	}

	@Override
	public void initProblem(String masterFileName) {
		readFromProperties(masterFileName);
		setProblemBoundary();
		prepareEng(masterFileName,config.getInt("numOfEngines"));
	}

	public void	readFromProperties(String master) {
		this.config = ConfigUtils.createConfig(master);
	}

	public void setProblemBoundary() {
		String[] lowerStrArray = config.getString("lower").split(",");
		String[] upperStrArray = config.getString("upper").split(",");

		if (lowerStrArray.length<=0 || lowerStrArray.length!=upperStrArray.length)
			System.err.println("Boundary input error!");

		//设置问题规模
		setNumberOfVariables(lowerStrArray.length);
		setNumberOfObjectives(1);
		setNumberOfConstraints(0);

		//设置边界值
		List<Double> lower = new ArrayList<>();
		for (int i = 0; i<lowerStrArray.length; i++) {
			lower.add(Double.parseDouble(lowerStrArray[i]));
		}
		List<Double> upper = new ArrayList<>();
		for (int i = 0; i<upperStrArray.length; i++) {
			upper.add(Double.parseDouble(upperStrArray[i]));
		}
		setLowerLimit(lower);
		setUpperLimit(upper);
	}

	@Override
	protected EngThread createEngThread(String name, String masterFileDir) {
		return new EngThread(name,masterFileDir);
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