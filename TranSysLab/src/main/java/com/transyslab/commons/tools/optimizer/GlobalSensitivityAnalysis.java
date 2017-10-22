package com.transyslab.commons.tools.optimizer;

/**
 * Created by yali on 2017/10/19.
 */
public interface GlobalSensitivityAnalysis {
	void initializeUncertainSolution();
	void evaluateModel();
	void calcSensitivityIndex();

}
