package com.transyslab.commons.tools;

import java.util.ArrayList;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

import com.transyslab.roadnetwork.Constants;

public class DEWithGR {
	private float F_;
	private Individual[] newidvds_;
	private Individual[] idvds_;
	private int dims_;
	private int population_;
	private float[] plower_;
	private float[] pupper_;
	private float Cr_;
	private float gbestFitness_;
	private float[] gbest_;
	private int nOfConstrains;
	
	public int getDim() {
		return dims_;
	}
	public int getPopulation() {
		return population_;
	}
	public float[] getPosition(int i) {
		return idvds_[i].pos_;
	}
	public float[] getNewPosition(int i) {
		return newidvds_[i].pos_;
	}
	public Individual[] getIdvds() {
		return idvds_;
	}
	public Individual[] getNewIdvds() {
		return newidvds_;
	}
	public void initDE(int p, int dim, float f, float cr, float[] pl, float[] pu) {
		dims_ = dim;
		population_ = p;
		F_ = f;
		Cr_ = cr;
		gbestFitness_ = Constants.FLT_INF;
		gbest_ = new float[dims_];
		plower_ = pl;
		pupper_ = pu;
		idvds_ = new Individual[population_];
		newidvds_ = new Individual[population_];
		/// gbest_ = new float[mg];
		for (int i = 0; i < p; i++) {
			idvds_[i] = new Individual(dim);
			newidvds_[i] = new Individual(dim);
			//生成初始解
			idvds_[i].init(pl, pu);
			for (int j = 0; j < dims_; j++) {
				newidvds_[i].pos_[j] = idvds_[i].pos_[j];
			}
		}

	}
	public void selection(int pi) {
		//5.15选择策略文献
		if (newidvds_[pi].fitness_ < idvds_[pi].fitness_) {
			for (int j = 0; j < dims_; j++) {
				idvds_[pi].pos_[j] = newidvds_[pi].pos_[j];
			}
			idvds_[pi].fitness_ = newidvds_[pi].fitness_;
		}

	}
	public double constrains(double[] x,int consIndex){
		double funValue = 0;
		switch (consIndex) {
		case 0:
			
			break;
		
		default:
			break;
		}
		return funValue;
	}
	public int constrainFunc(int pi){
		newidvds_[pi].constrainsViolate = new double[nOfConstrains];
	//	newidvds_[pi].constrainsViolate[0] = constrains1();
	//	newidvds_[pi].constrainsViolate[1] = constrains2();
	//	newidvds_[pi].constrainsViolate[2] = constrains3();
	//	newidvds_[pi].constrainsViolate[3] = constrains4();

		//...
		return 0;
		
	}
	public void constrain2(){
		
	}
	public void constrain3(){
		
	}
	// 梯度矩阵行
	public double[] estGradient(float[] x,int consIndex){
		double[] results = new double[x.length];
		double [] perX = new double[x.length];
		System.arraycopy(x, 0, perX, 0, x.length);
		for(int i=0;i<x.length;i++){
			perX[i] +=0.001;
			results[i] = constrains(perX , consIndex)/0.001;
			perX[i] -=0.001; 
		}
		return results;
	}
	/*
	public int[] whichViolate(int pi){
		// -1:满足所有约束
		int[] checkResults = new int
		newidvds_[pi].constrainsViolate.clear();
		newidvds_[pi].constrainsViolate.add();
	}*/
	public void repairViolate(int pi){
		// 调用完constrainFunc
		int violateSize = 0;
		

		for(int i=0;i<nOfConstrains;i++){
			// 违反
			if(newidvds_[pi].constrainsViolate[i]>0)
				violateSize ++ ;
		}
		int[] consIndexs = new int[violateSize];
		int consCounter = 0;
		double[][] cViolate = new double[1][violateSize]; 
		for(int i=0;i<nOfConstrains;i++){
			// 违反
			if(newidvds_[pi].constrainsViolate[i]>0){
				cViolate[0][consCounter] = newidvds_[pi].constrainsViolate[i];
				consIndexs[consCounter] = i;
				consCounter++;
			}
		}
		double [][] graMatirx = new double[violateSize][];
		for(int i=0;i<violateSize;i++){
			graMatirx[i] = estGradient(newidvds_[pi].pos_, consIndexs[i]);
		}

		SimpleMatrix conVioMatrix = new SimpleMatrix(cViolate); 
		SimpleMatrix invert = new SimpleMatrix(graMatirx).pseudoInverse();
		SimpleMatrix detaX = invert.mult(conVioMatrix);//2 x 1
		// 修正解
		for(int i=0;i<dims_;i++){
			newidvds_[pi].pos_[i] += (float) detaX.get(i);
		}
	}
	public float[] getGbest() {
		return gbest_;
	}
	public float getGbestFitness() {
		return gbestFitness_;
	}
	public void setGbestFitness(float gbf) {
		gbestFitness_ = gbf;
	}
	public void setGbest(float[] gbest) {
		for (int i = 0; i < gbest.length; i++) {
			gbest_[i] = gbest[i];
		}
	}
	public float getFitness(int pi) {
		return idvds_[pi].fitness_;
	}
	public void changePos(int pi) {
		int pi1, pi2, pi3;
		pi1 = Individual.rnd_.nextInt(population_);
		do {
			pi2 = Individual.rnd_.nextInt(population_);
		} while (pi1 == pi2);
		do {
			pi3 = Individual.rnd_.nextInt(population_);
		} while (pi1 == pi3 || pi2 == pi3);
		// int counter=0;
		for (int j = 0; j < dims_; j++) {
			// 5.15
			int jrand = Individual.rnd_.nextInt(dims_);
			newidvds_[pi].pos_[j] = idvds_[pi1].pos_[j] + F_ * (idvds_[pi2].pos_[j] - idvds_[pi3].pos_[j]);
			// 5.15约束添加
			if (newidvds_[pi].pos_[j] > pupper_[j] || newidvds_[pi].pos_[j] < plower_[j])
				newidvds_[pi].pos_[j] = idvds_[pi].pos_[j];
			// 5.15添加&&
			if (Individual.rnd_.nextFloat() > Cr_ && j!=jrand)
				newidvds_[pi].pos_[j] = idvds_[pi].pos_[j];
		}
	}
	public void showresult() {
		System.out.println("程序求得的最优解是" + gbestFitness_);
		System.out.println("每一维的值是");
		for (int i = 0; i < dims_; i++) {
			System.out.println(gbest_[i]);
		}
	}
	
	public String showGBestPos() {
		String s = "";		
		for (int i = 0; i < gbest_.length; i++) {
			s += String.valueOf(gbest_[i]) + " ";
		}
		return s;
	}
	public static void main(String[] args) {/*
		double[][] data = new double[][]{{12,0},{1,0},{1,1}};
		SimpleMatrix test =  new SimpleMatrix(data);
		SimpleMatrix result = test.pseudoInverse();*/
	}
}
