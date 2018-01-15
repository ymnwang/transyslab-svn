package com.transyslab.commons.tools;

/**
 * Created by wangyimin on 2017/12/28.
 */
public interface NewtonFunction {
	double calculate(double input, double[] paras);
	default double findRoot(double start, double[] paras){
		double epsilon = 1e-12;
		double maxIteration = 1e6;
		double x = start;
		double f = this.calculate(x,paras);
		int iterTimes = 0;
		while (Math.abs(f)>epsilon) {
			if (iterTimes>maxIteration) {
				System.err.println("方程不收敛");
				return Double.NaN;
			}
			x = x - f*epsilon/(this.calculate(x+epsilon,paras)-f);//迭代x(k+1)
			f = this.calculate(x,paras);
			iterTimes += 1;
		}
		if (x < 0 || Double.isNaN(f)) {
			return findRoot(start*10,paras);
		}
		return x;
	}
}