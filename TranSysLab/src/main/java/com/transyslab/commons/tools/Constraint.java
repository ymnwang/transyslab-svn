package com.transyslab.commons.tools;

/**
 * Created by WangYimin on 2018/2/22.
 */
public interface Constraint {
	boolean checkViolated(double arg, double[] relatedParas);
}
