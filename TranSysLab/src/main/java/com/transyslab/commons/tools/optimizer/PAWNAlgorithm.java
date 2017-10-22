package com.transyslab.commons.tools.optimizer;

import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

import java.util.List;

/**
 * Created by yali on 2017/10/19.
 */
public class PAWNAlgorithm implements GlobalSensitivityAnalysis{
	protected double[][] solutions;
	protected double[] upperBound;
	protected double[] lowerBound;
	protected int nc;//固定某一参数，扰动其它参数生成的解个数
	protected int nu;//可行域内所有随机解的个数
	protected int nStat;//用来固定某一参数的取值个数
	protected int nDim;//参数个数
	public PAWNAlgorithm(double[] upperBound, double[] lowerBound, int nc, int nu, int nStat){
		if(upperBound.length != lowerBound.length){
			System.out.println("Error");
			return;
		}
		this.upperBound =  upperBound;
		this.lowerBound = lowerBound;
		this.nDim = lowerBound.length;
		this.nc = nc;
		this.nu = nu;
		this.nStat = nStat;
		this.solutions = new double[nu + nStat * nc * nDim][nDim];
	}
	public void setNc(int nc){
		this.nc = nc;
	}
	public void setNu(int nu){
		this.nu = nu;
	}
	public void setNStat(int nStat){
		this.nStat = nStat;
	}

	@Override
	public void initializeUncertainSolution() {
		
	}

	@Override
	public void evaluateModel() {

	}

	@Override
	public void calcSensitivityIndex() {

	}
}
