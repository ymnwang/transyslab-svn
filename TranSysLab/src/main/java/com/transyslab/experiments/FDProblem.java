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
