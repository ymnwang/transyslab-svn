package com.transyslab.experiments;

import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.adapter.SimProblem;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.commons.tools.mutitask.EngThread;
import com.transyslab.commons.tools.mutitask.SimulationConductor;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mlp.*;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by WangYimin on 2017/12/12.
 */
public class FDProblem extends MLPProblem{

	public FDProblem(){	}
	public FDProblem(String masterFileName) {
		super(masterFileName);
	}

	@Override
	public void initProblem(String masterFileName) {
		super.initProblem(masterFileName);
		ExpSwitch.MAX_ACC_CTRL = true;
		ExpSwitch.APPROACH_CTRL = true;
	}

	@Override
	protected EngThread createEngThread(String name, String masterFileDir) {
		return  new EngThread(name,masterFileDir){
			@Override
			public void initEngine(String modelType, String masterFileDir) {
				setEngine(new MLPEngine(masterFileDir){
					@Override
					public void setParasRightBeforeRun() {
						double qm=0.5225, vf_cf=17.4178, vf_sd=21.0805,kj=0.1599,ts=0.4432,xc=33.3331,alpha=2.0846,beta=8.3574;
						setObservedParas(qm,vf_cf,vf_sd,120.0/3.6,0.12,0.2);
						setOptParas(kj,ts,xc,alpha,beta,free_paras[2],free_paras[3]);//gamma 另外输入
					}
				});
			}
		};
	}

	@Override
	public void setProblemBoundary() {
		//设置问题规模
		setNumberOfVariables(4);
		setNumberOfObjectives(1);
		setNumberOfConstraints(0);

		//设置边界值
		setLowerLimit(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 0.0}));
		setUpperLimit(Arrays.asList(new Double[]{10.0, 10.0, 10.0, 2.0}));
	}

	@Override
	protected SimulationConductor createConductor() {
		try {
			return new FDConductor();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
}
